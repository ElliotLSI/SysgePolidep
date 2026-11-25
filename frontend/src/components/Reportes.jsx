import { useEffect, useState } from "react"

function Reportes() {
    const reportes = [
        {
            nombre: "Reservas",
            descripcion: "Agrupado por cliente con detalle desplegable de cada reserva.",
            ruta: "reservas",
            conFiltrosAvanzados: true,
            estados: ["PENDIENTE", "CONFIRMADA", "CANCELADA"]
        },
        {
            nombre: "Pagos",
            descripcion: "Agrupado por cliente con detalle desplegable de cobros.",
            ruta: "pagos",
            conFiltrosAvanzados: true,
            estados: ["APROBADO", "PENDIENTE", "RECHAZADO"]
        },
        {
            nombre: "Socios y Membresías",
            descripcion: "Agrupado por socio con historial desplegable de cuotas.",
            ruta: "socios-membresias",
            estados: ["VIGENTE", "VENCIDA", "CANCELADA"]
        },
        {
            nombre: "Usuarios",
            descripcion: "Listado general de cuentas registradas y estado de acceso.",
            ruta: "usuarios",
            estados: ["ACTIVO", "INACTIVO"]
        },
        {
            nombre: "Instalaciones",
            descripcion: "Listado de canchas y espacios, capacidad, tarifas y estado.",
            ruta: "instalaciones",
            estados: ["DISPONIBLE", "MANTENIMIENTO", "CLAUSURADA"]
        }
    ]

    const meses = [
        { valor: "1", nombre: "Enero" },
        { valor: "2", nombre: "Febrero" },
        { valor: "3", nombre: "Marzo" },
        { valor: "4", nombre: "Abril" },
        { valor: "5", nombre: "Mayo" },
        { valor: "6", nombre: "Junio" },
        { valor: "7", nombre: "Julio" },
        { valor: "8", nombre: "Agosto" },
        { valor: "9", nombre: "Septiembre" },
        { valor: "10", nombre: "Octubre" },
        { valor: "11", nombre: "Noviembre" },
        { valor: "12", nombre: "Diciembre" }
    ]

    const hoy = new Date()
    const anioActual = hoy.getFullYear()
    const mesActual = hoy.getMonth() + 1
    const diaActual = hoy.getDate()

    const filtrosIniciales = {
        anio: "",
        mes: "",
        dia: "",
        idUsuario: "",
        idInstalacion: "",
        estado: "",
        busqueda: ""
    }

    const [reporteSeleccionado, setReporteSeleccionado] = useState(reportes[0])
    const [usuarios, setUsuarios] = useState([])
    const [instalaciones, setInstalaciones] = useState([])
    const [reservas, setReservas] = useState([])
    const [pagos, setPagos] = useState([])
    const [membresias, setMembresias] = useState([])

    const [filtros, setFiltros] = useState(filtrosIniciales)
    const [abiertos, setAbiertos] = useState({})
    const [cargandoDatos, setCargandoDatos] = useState(true)
    const [generandoPdf, setGenerandoPdf] = useState(false)
    const [error, setError] = useState("")

    useEffect(() => {
        const token = sessionStorage.getItem("token")
        const headers = { Authorization: "Bearer " + token }

        Promise.all([
            fetch("http://localhost:8080/api/usuarios", { headers }).then(r => (r.ok ? r.json() : [])).catch(() => []),
            fetch("http://localhost:8080/api/instalaciones", { headers }).then(r => (r.ok ? r.json() : [])).catch(() => []),
            fetch("http://localhost:8080/api/reservas", { headers }).then(r => (r.ok ? r.json() : [])).catch(() => []),
            fetch("http://localhost:8080/api/pagos", { headers }).then(r => (r.ok ? r.json() : [])).catch(() => []),
            fetch("http://localhost:8080/api/membresias", { headers }).then(r => (r.ok ? r.json() : [])).catch(() => [])
        ])
            .then(([u, inst, res, pag, memb]) => {
                setUsuarios(Array.isArray(u) ? u : [])
                setInstalaciones(Array.isArray(inst) ? inst : [])
                setReservas(Array.isArray(res) ? res : [])
                setPagos(Array.isArray(pag) ? pag : [])
                setMembresias(Array.isArray(memb) ? memb : [])
            })
            .finally(() => setCargandoDatos(false))
    }, [])

    // ==================== HELPERS DE FORMATO Y FECHAS ====================

    const formatearMonto = (monto) =>
        Number(monto ?? 0).toLocaleString("es-AR", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })

    const formatearFecha = (fechaStr) => {
        if (!fechaStr) return "-"
        const soloFecha = fechaStr.split("T")[0]
        const [anio, mes, dia] = soloFecha.split("-")
        return `${dia}/${mes}/${anio}`
    }

    const claseEstado = (estado) => {
        if (!estado) return "estado"
        const norm = estado.toLowerCase()
        if (norm.includes("mantenimiento")) return "estado estado-mantenimiento"
        return `estado estado-${norm}`
    }

    const obtenerEstadoRealMembresia = (m) => {
        if (m.estado === "VIGENTE" && m.fechaVenc) {
            const hoySinHora = new Date()
            hoySinHora.setHours(0, 0, 0, 0)
            const venc = new Date(m.fechaVenc + "T00:00:00")
            if (venc < hoySinHora) return "VENCIDA"
        }
        return m.estado
    }

    const obtenerDiasDelMes = (anioStr, mesStr) => {
        if (!mesStr) return 31
        const mesNum = Number(mesStr)
        const anioNum = anioStr ? Number(anioStr) : 2024
        return new Date(anioNum, mesNum, 0).getDate()
    }

    const obtenerMaxDia = (anioStr, mesStr) => {
        const diasDelMes = obtenerDiasDelMes(anioStr, mesStr)
        if (
            anioStr &&
            mesStr &&
            Number(anioStr) === anioActual &&
            Number(mesStr) === mesActual
        ) {
            return Math.min(diasDelMes, diaActual)
        }
        return diasDelMes
    }

    const maxDiaPermitido = obtenerMaxDia(filtros.anio, filtros.mes)

    const mesesDisponibles =
        filtros.anio && Number(filtros.anio) === anioActual
            ? meses.filter(m => Number(m.valor) <= mesActual)
            : meses

    const coincideFecha = (fechaStr) => {
        if (!filtros.anio && !filtros.mes && !filtros.dia) return true
        if (!fechaStr) return false
        const soloFecha = fechaStr.split("T")[0]
        const [anio, mes, dia] = soloFecha.split("-").map(Number)
        if (filtros.anio && anio !== Number(filtros.anio)) return false
        if (filtros.mes && mes !== Number(filtros.mes)) return false
        if (filtros.dia && dia !== Number(filtros.dia)) return false
        return true
    }

    const seleccionarReporte = (reporte) => {
        setReporteSeleccionado(reporte)
        setFiltros(filtrosIniciales)
        setAbiertos({})
        setError("")
    }

    const handleChange = (e) => {
        const { name, value } = e.target
        setError("")

        setFiltros(prev => {
            const siguiente = { ...prev, [name]: value }

            if (
                name === "anio" &&
                Number(value) === anioActual &&
                siguiente.mes &&
                Number(siguiente.mes) > mesActual
            ) {
                siguiente.mes = ""
                siguiente.dia = ""
            }

            const nuevoMaxDia = obtenerMaxDia(siguiente.anio, siguiente.mes)
            if (siguiente.dia && Number(siguiente.dia) > nuevoMaxDia) {
                siguiente.dia = ""
            }

            return siguiente
        })
    }

    const seleccionarEstadoRapido = (estado) => {
        setFiltros(prev => ({ ...prev, estado }))
    }

    const limpiarFiltros = () => {
        setFiltros(filtrosIniciales)
        setError("")
    }

    const validarFechas = () => {
        if (!reporteSeleccionado.conFiltrosAvanzados) return null

        const anioNum = filtros.anio ? Number(filtros.anio) : null
        const mesNum = filtros.mes ? Number(filtros.mes) : null
        const diaNum = filtros.dia ? Number(filtros.dia) : null

        if (anioNum !== null && anioNum > anioActual) {
            return `El año no puede ser posterior a ${anioActual}.`
        }
        if (anioNum === anioActual && mesNum !== null && mesNum > mesActual) {
            return "El mes seleccionado no puede ser posterior al mes actual."
        }
        if (diaNum !== null) {
            const diasDelMes = obtenerDiasDelMes(filtros.anio, filtros.mes)
            if (diaNum < 1 || diaNum > diasDelMes) {
                return `El mes seleccionado solo tiene ${diasDelMes} días.`
            }
            if (anioNum === anioActual && mesNum === mesActual && diaNum > diaActual) {
                return "La fecha del reporte no puede ser posterior a la fecha de hoy."
            }
        }
        return null
    }

    // ==================== EXPORTAR PDF ====================

    const generarReportePdf = async (e) => {
        if (e) e.preventDefault()
        setError("")

        const errorFecha = validarFechas()
        if (errorFecha) {
            setError(errorFecha)
            return
        }

        setGenerandoPdf(true)

        try {
            const params = new URLSearchParams()

            if (reporteSeleccionado.conFiltrosAvanzados) {
                if (filtros.anio) params.append("anio", filtros.anio)
                if (filtros.mes) params.append("mes", filtros.mes)
                if (filtros.dia) params.append("dia", filtros.dia)
                if (filtros.idUsuario) params.append("idUsuario", filtros.idUsuario)
                if (filtros.idInstalacion) params.append("idInstalacion", filtros.idInstalacion)
            }

            if (filtros.estado) {
                params.append("estado", filtros.estado)
            }

            const queryString = params.toString()
            const urlEndpoint = `http://localhost:8080/api/reportes/${reporteSeleccionado.ruta}${
                queryString ? "?" + queryString : ""
            }`

            const respuesta = await fetch(urlEndpoint, {
                headers: {
                    Authorization: "Bearer " + sessionStorage.getItem("token")
                }
            })

            if (!respuesta.ok) {
                throw new Error("No se pudo generar el PDF (HTTP " + respuesta.status + ").")
            }

            const blob = await respuesta.blob()
            const url = URL.createObjectURL(blob)
            window.open(url, "_blank")
        } catch (err) {
            console.error(err)
            setError(err.message || "No se pudo generar el PDF.")
        } finally {
            setGenerandoPdf(false)
        }
    }

    // ==================== FILTRADO Y AGRUPADO EN VIVO ====================

    const textoBusqueda = filtros.busqueda.trim().toLowerCase()

    const obtenerUsuarioDePago = (p) => {
        if (p.reserva?.usuario) return p.reserva.usuario
        if (p.idMembresia) {
            const memb = membresias.find(m => m.idMembresia === p.idMembresia)
            return memb?.socio?.usuario || null
        }
        return null
    }

    // Bases sin filtro de estado (para contar los botones correctamente)
    const reservasBase = reservas.filter(r => {
        if (!coincideFecha(r.fechaReserva)) return false
        if (filtros.idUsuario && r.usuario?.idUsuario !== Number(filtros.idUsuario)) return false
        if (filtros.idInstalacion && r.instalacion?.idInstalacion !== Number(filtros.idInstalacion)) return false
        if (textoBusqueda) {
            const nombre = `${r.usuario?.nombre || ""} ${r.usuario?.apellido || ""}`.toLowerCase()
            const inst = (r.instalacion?.nombre || "").toLowerCase()
            if (!nombre.includes(textoBusqueda) && !inst.includes(textoBusqueda)) return false
        }
        return true
    })

    const pagosBase = pagos.filter(p => {
        if (!coincideFecha(p.fecha)) return false
        const u = obtenerUsuarioDePago(p)
        if (filtros.idUsuario && (!u || u.idUsuario !== Number(filtros.idUsuario))) return false
        if (filtros.idInstalacion) {
            if (!p.reserva?.instalacion || p.reserva.instalacion.idInstalacion !== Number(filtros.idInstalacion)) {
                return false
            }
        }
        if (textoBusqueda) {
            const nombre = `${u?.nombre || ""} ${u?.apellido || ""}`.toLowerCase()
            const medio = (p.medioPago || "").toLowerCase()
            if (!nombre.includes(textoBusqueda) && !medio.includes(textoBusqueda)) return false
        }
        return true
    })

    const membresiasBase = membresias.filter(m => {
        if (!textoBusqueda) return true
        const nombre = `${m.socio?.usuario?.nombre || ""} ${m.socio?.usuario?.apellido || ""}`.toLowerCase()
        const dni = (m.socio?.usuario?.dni || "").toLowerCase()
        const nro = String(m.socio?.nroSocio || "").toLowerCase()
        const cat = (m.categoria?.nombre || "").toLowerCase()
        return nombre.includes(textoBusqueda) || dni.includes(textoBusqueda) || nro.includes(textoBusqueda) || cat.includes(textoBusqueda)
    })

    const usuariosBase = usuarios.filter(u => {
        if (!textoBusqueda) return true
        const nombre = `${u.nombre || ""} ${u.apellido || ""}`.toLowerCase()
        return (
            nombre.includes(textoBusqueda) ||
            (u.dni || "").toLowerCase().includes(textoBusqueda) ||
            (u.nombreUsuario || "").toLowerCase().includes(textoBusqueda) ||
            (u.email || "").toLowerCase().includes(textoBusqueda)
        )
    })

    const instalacionesBase = instalaciones.filter(i => {
        if (!textoBusqueda) return true
        return (
            (i.nombre || "").toLowerCase().includes(textoBusqueda) ||
            (i.tipo || "").toLowerCase().includes(textoBusqueda)
        )
    })

    // Listas finales filtradas por estado
    const reservasFiltradas = filtros.estado
        ? reservasBase.filter(r => r.estado === filtros.estado)
        : reservasBase

    const pagosFiltrados = filtros.estado
        ? pagosBase.filter(p => p.estado === filtros.estado)
        : pagosBase

    const membresiasFiltradas = filtros.estado
        ? membresiasBase.filter(m => obtenerEstadoRealMembresia(m) === filtros.estado)
        : membresiasBase

    const usuariosFiltrados = filtros.estado
        ? usuariosBase.filter(u => u.estado === filtros.estado)
        : usuariosBase

    const instalacionesFiltradas = filtros.estado
        ? instalacionesBase.filter(i => i.estado === filtros.estado)
        : instalacionesBase

    // Agrupadores
    const agruparPorUsuario = (items, getUsuario) => {
        const mapa = new Map()
        items.forEach(item => {
            const u = getUsuario(item)
            const clave = u?.idUsuario ? String(u.idUsuario) : "sin-asignar"
            if (!mapa.has(clave)) {
                mapa.set(clave, {
                    clave,
                    nombre: u ? `${u.nombre || ""} ${u.apellido || ""}`.trim() : "Sin asignar",
                    dni: u?.dni || "",
                    items: [],
                    subtotal: 0
                })
            }
            const grupo = mapa.get(clave)
            grupo.items.push(item)
            grupo.subtotal += Number(item.montoTotal ?? 0)
        })
        return Array.from(mapa.values())
    }

    const agruparMembresiasPorSocio = (lista) => {
        const mapa = new Map()
        lista.forEach(m => {
            const idSocio = m.socio?.idUsuario ? String(m.socio.idUsuario) : `m-${m.idMembresia}`
            if (!mapa.has(idSocio)) {
                mapa.set(idSocio, {
                    clave: idSocio,
                    nroSocio: m.socio?.nroSocio || "-",
                    nombre: m.socio?.usuario
                        ? `${m.socio.usuario.nombre || ""} ${m.socio.usuario.apellido || ""}`.trim()
                        : "Sin socio",
                    dni: m.socio?.usuario?.dni || "",
                    items: []
                })
            }
            mapa.get(idSocio).items.push(m)
        })

        // Ordenar cuotas de cada socio (más reciente primero) y calcular su estado principal
        return Array.from(mapa.values()).map(g => {
            g.items.sort((a, b) => (b.fechaVenc || "").localeCompare(a.fechaVenc || ""))
            const tieneVigente = g.items.some(m => obtenerEstadoRealMembresia(m) === "VIGENTE")
            g.estadoPrincipal = tieneVigente ? "VIGENTE" : obtenerEstadoRealMembresia(g.items[0])
            return g
        })
    }

    const gruposReservas = agruparPorUsuario(reservasFiltradas, r => r.usuario)
    const gruposPagos = agruparPorUsuario(pagosFiltrados, p => obtenerUsuarioDePago(p))
    const gruposSocios = agruparMembresiasPorSocio(membresiasFiltradas)

    const toggleGrupo = (clave) => {
        setAbiertos(prev => ({ ...prev, [clave]: !prev[clave] }))
    }

    const expandirTodos = (grupos) => {
        const nuevo = {}
        grupos.forEach(g => {
            nuevo[g.clave] = true
        })
        setAbiertos(nuevo)
    }

    const contraerTodos = () => setAbiertos({})

    // Contadores para los botones rápidos según el reporte activo
    const obtenerContadoresEstado = () => {
        if (reporteSeleccionado.ruta === "reservas") {
            return {
                total: reservasBase.length,
                porEstado: est => reservasBase.filter(r => r.estado === est).length
            }
        }
        if (reporteSeleccionado.ruta === "pagos") {
            return {
                total: pagosBase.length,
                porEstado: est => pagosBase.filter(p => p.estado === est).length
            }
        }
        if (reporteSeleccionado.ruta === "socios-membresias") {
            return {
                total: membresiasBase.length,
                porEstado: est => membresiasBase.filter(m => obtenerEstadoRealMembresia(m) === est).length
            }
        }
        if (reporteSeleccionado.ruta === "usuarios") {
            return {
                total: usuariosBase.length,
                porEstado: est => usuariosBase.filter(u => u.estado === est).length
            }
        }
        return {
            total: instalacionesBase.length,
            porEstado: est => instalacionesBase.filter(i => i.estado === est).length
        }
    }

    const contadores = obtenerContadoresEstado()

    // ==================== RENDER DE ACORDEONES ====================

    const renderAcordeon = (grupos, tipo) => {
        const totalRegistros = grupos.reduce((acc, g) => acc + g.items.length, 0)
        const totalMonto = grupos.reduce((acc, g) => acc + (g.subtotal || 0), 0)

        if (grupos.length === 0) {
            return (
                <div className="tabla-contenedor" style={{ padding: "28px", textAlign: "center", color: "#64748B" }}>
                    No se encontraron registros con los filtros seleccionados.
                </div>
            )
        }

        return (
            <div>
                <div className="resumen-descuento" style={{ marginBottom: "14px" }}>
                    <div>
                        <strong>{grupos.length}</strong> {tipo === "socios" ? "socio(s)" : "cliente(s)"} —{" "}
                        <strong>{totalRegistros}</strong>{" "}
                        {tipo === "reservas" ? "reserva(s)" : tipo === "pagos" ? "pago(s)" : "membresía(s)"}
                        {tipo !== "socios" && (
                            <>
                                {" "}— Total: <strong style={{ color: "#5C0613" }}>${formatearMonto(totalMonto)}</strong>
                            </>
                        )}
                    </div>
                    <div style={{ display: "flex", gap: "8px" }}>
                        <button
                            type="button"
                            className="boton-secundario"
                            style={{ padding: "6px 10px", fontSize: "13px" }}
                            onClick={() => expandirTodos(grupos)}
                        >
                            Expandir todos
                        </button>
                        <button
                            type="button"
                            className="boton-secundario"
                            style={{ padding: "6px 10px", fontSize: "13px" }}
                            onClick={contraerTodos}
                        >
                            Contraer todos
                        </button>
                    </div>
                </div>

                <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                    {grupos.map(grupo => {
                        const estaAbierto = Boolean(abiertos[grupo.clave])
                        return (
                            <div
                                key={grupo.clave}
                                style={{
                                    border: "1px solid #E2E8F0",
                                    borderRadius: "10px",
                                    overflow: "hidden",
                                    background: "#FFFFFF"
                                }}
                            >
                                <button
                                    type="button"
                                    onClick={() => toggleGrupo(grupo.clave)}
                                    style={{
                                        width: "100%",
                                        display: "flex",
                                        justifyContent: "space-between",
                                        alignItems: "center",
                                        flexWrap: "wrap",
                                        gap: "10px",
                                        padding: "14px 16px",
                                        background: estaAbierto ? "#F8EDEF" : "#F8FAFC",
                                        border: "none",
                                        borderBottom: estaAbierto ? "1px solid #E2E8F0" : "none",
                                        cursor: "pointer",
                                        textAlign: "left"
                                    }}
                                >
                                    <div style={{ display: "flex", alignItems: "center", gap: "10px", flexWrap: "wrap" }}>
                                        <span style={{ color: "#5C0613", fontWeight: "bold", fontSize: "14px" }}>
                                            {estaAbierto ? "▼" : "▶"}
                                        </span>
                                        {tipo === "socios" && (
                                            <span style={{ color: "#64748B", fontSize: "13px", fontWeight: "bold" }}>
                                                #{grupo.nroSocio}
                                            </span>
                                        )}
                                        <strong style={{ color: "#1E293B", fontSize: "15px" }}>
                                            {grupo.nombre}
                                        </strong>
                                        {grupo.dni && (
                                            <span style={{ color: "#64748B", fontSize: "13px" }}>
                                                DNI: {grupo.dni}
                                            </span>
                                        )}
                                    </div>

                                    <div style={{ display: "flex", alignItems: "center", gap: "12px", fontSize: "14px" }}>
                                        <span
                                            className="estado"
                                            style={{ background: "#FFFFFF", color: "#334155", border: "1px solid #CBD5E1" }}
                                        >
                                            {grupo.items.length}{" "}
                                            {tipo === "reservas" ? "reserva(s)" : tipo === "pagos" ? "pago(s)" : "cuota(s)"}
                                        </span>
                                        {tipo === "socios" ? (
                                            <span className={claseEstado(grupo.estadoPrincipal)}>
                                                {grupo.estadoPrincipal}
                                            </span>
                                        ) : (
                                            <strong style={{ color: "#5C0613" }}>
                                                ${formatearMonto(grupo.subtotal)}
                                            </strong>
                                        )}
                                    </div>
                                </button>

                                {estaAbierto && (
                                    <div style={{ overflowX: "auto" }}>
                                        <table>
                                            {tipo === "reservas" && (
                                                <>
                                                    <thead>
                                                    <tr>
                                                        <th>ID</th>
                                                        <th>Instalación</th>
                                                        <th>Fecha</th>
                                                        <th>Hora</th>
                                                        <th>Duración</th>
                                                        <th>Estado</th>
                                                        <th>Monto</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    {grupo.items.map(r => (
                                                        <tr key={r.idReserva}>
                                                            <td>#{r.idReserva}</td>
                                                            <td><strong>{r.instalacion?.nombre || "-"}</strong></td>
                                                            <td>{formatearFecha(r.fechaReserva)}</td>
                                                            <td>{r.horaInicio?.slice(0, 5) || "-"}</td>
                                                            <td>{r.duracionHoras} hs</td>
                                                            <td>
                                                                    <span className={claseEstado(r.estado)}>
                                                                        {r.estado}
                                                                    </span>
                                                            </td>
                                                            <td><strong>${formatearMonto(r.montoTotal)}</strong></td>
                                                        </tr>
                                                    ))}
                                                    </tbody>
                                                </>
                                            )}

                                            {tipo === "pagos" && (
                                                <>
                                                    <thead>
                                                    <tr>
                                                        <th>ID</th>
                                                        <th>Fecha y Hora</th>
                                                        <th>Concepto</th>
                                                        <th>Detalle</th>
                                                        <th>Medio</th>
                                                        <th>Estado</th>
                                                        <th>Monto</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    {grupo.items.map(p => {
                                                        const memb = p.idMembresia
                                                            ? membresias.find(m => m.idMembresia === p.idMembresia)
                                                            : null
                                                        const concepto = p.reserva
                                                            ? `Reserva #${p.reserva.idReserva}`
                                                            : `Membresía #${p.idMembresia}`
                                                        const detalle = p.reserva
                                                            ? p.reserva.instalacion?.nombre || "-"
                                                            : memb?.categoria?.nombre || "-"

                                                        return (
                                                            <tr key={p.idPago}>
                                                                <td>#{p.idPago}</td>
                                                                <td>
                                                                    {p.fecha
                                                                        ? new Date(p.fecha).toLocaleString("es-AR", {
                                                                            dateStyle: "short",
                                                                            timeStyle: "short"
                                                                        })
                                                                        : "-"}
                                                                </td>
                                                                <td>{concepto}</td>
                                                                <td><strong>{detalle}</strong></td>
                                                                <td>{p.medioPago}</td>
                                                                <td>
                                                                        <span className={claseEstado(p.estado)}>
                                                                            {p.estado}
                                                                        </span>
                                                                </td>
                                                                <td><strong>${formatearMonto(p.montoTotal)}</strong></td>
                                                            </tr>
                                                        )
                                                    })}
                                                    </tbody>
                                                </>
                                            )}

                                            {tipo === "socios" && (
                                                <>
                                                    <thead>
                                                    <tr>
                                                        <th>ID</th>
                                                        <th>Categoría</th>
                                                        <th>Descuento</th>
                                                        <th>Inicio</th>
                                                        <th>Vencimiento</th>
                                                        <th>Estado</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    {grupo.items.map(m => {
                                                        const est = obtenerEstadoRealMembresia(m)
                                                        return (
                                                            <tr key={m.idMembresia}>
                                                                <td>#{m.idMembresia}</td>
                                                                <td><strong>{m.categoria?.nombre || "-"}</strong></td>
                                                                <td>{m.categoria?.porcDescuento ?? 0}%</td>
                                                                <td>{formatearFecha(m.fechaInicio)}</td>
                                                                <td>{formatearFecha(m.fechaVenc)}</td>
                                                                <td>
                                                                        <span className={claseEstado(est)}>
                                                                            {est}
                                                                        </span>
                                                                </td>
                                                            </tr>
                                                        )
                                                    })}
                                                    </tbody>
                                                </>
                                            )}
                                        </table>
                                    </div>
                                )}
                            </div>
                        )
                    })}
                </div>
            </div>
        )
    }

    return (
        <section className="usuarios">
            <div className="seccion-titulo">
                <div>
                    <h2>Reportes</h2>
                    <p>Consultá los resultados agrupados en pantalla o exportalos a PDF.</p>
                </div>
            </div>

            {error && <div className="mensaje-error">{error}</div>}

            {/* TARJETAS DE SELECCIÓN */}
            <section className="tarjetas" style={{ marginBottom: "24px" }}>
                {reportes.map(reporte => {
                    const activo = reporteSeleccionado.ruta === reporte.ruta
                    return (
                        <div
                            className="tarjeta"
                            key={reporte.ruta}
                            onClick={() => seleccionarReporte(reporte)}
                            role="button"
                            tabIndex={0}
                            style={{
                                border: activo ? "2px solid #5C0613" : undefined,
                                backgroundColor: activo ? "#F8EDEF" : undefined
                            }}
                            onKeyDown={e => {
                                if (e.key === "Enter") seleccionarReporte(reporte)
                            }}
                        >
                            <h3 style={{ color: activo ? "#5C0613" : undefined }}>
                                {reporte.nombre}
                            </h3>
                            <p>{reporte.descripcion}</p>
                        </div>
                    )
                })}
            </section>

            {/* PANEL DE FILTROS */}
            <form className="formulario-usuario" onSubmit={generarReportePdf}>
                <h3>Filtros para: {reporteSeleccionado.nombre}</h3>

                {/* BOTONES RÁPIDOS DE ESTADO + BUSCADOR */}
                <div
                    style={{
                        display: "flex",
                        flexWrap: "wrap",
                        justifyContent: "space-between",
                        alignItems: "center",
                        gap: "12px",
                        marginBottom: reporteSeleccionado.conFiltrosAvanzados ? "18px" : "8px"
                    }}
                >
                    <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
                        <button
                            type="button"
                            className={filtros.estado === "" ? "boton-principal" : "boton-secundario"}
                            onClick={() => seleccionarEstadoRapido("")}
                        >
                            Todos ({contadores.total})
                        </button>
                        {reporteSeleccionado.estados.map(est => (
                            <button
                                key={est}
                                type="button"
                                className={filtros.estado === est ? "boton-principal" : "boton-secundario"}
                                onClick={() => seleccionarEstadoRapido(est)}
                            >
                                {est} ({contadores.porEstado(est)})
                            </button>
                        ))}
                    </div>

                    <input
                        type="text"
                        name="busqueda"
                        placeholder="Buscar..."
                        value={filtros.busqueda}
                        onChange={handleChange}
                        style={{
                            width: "250px",
                            padding: "8px 12px",
                            border: "1px solid #CBD5E1",
                            borderRadius: "7px",
                            fontSize: "14px"
                        }}
                    />
                </div>

                {reporteSeleccionado.conFiltrosAvanzados && (
                    <div className="formulario-grid">
                        <div>
                            <label>Año</label>
                            <input
                                type="number"
                                name="anio"
                                placeholder={`Ej: ${anioActual}`}
                                min="2020"
                                max={anioActual}
                                value={filtros.anio}
                                onChange={handleChange}
                            />
                        </div>

                        <div>
                            <label>Mes</label>
                            <select name="mes" value={filtros.mes} onChange={handleChange}>
                                <option value="">Todos los meses</option>
                                {mesesDisponibles.map(m => (
                                    <option key={m.valor} value={m.valor}>
                                        {m.nombre}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label>Día</label>
                            <input
                                type="number"
                                name="dia"
                                placeholder={`1 a ${maxDiaPermitido}`}
                                min="1"
                                max={maxDiaPermitido}
                                value={filtros.dia}
                                onChange={handleChange}
                            />
                        </div>

                        <div>
                            <label>Cliente</label>
                            <select name="idUsuario" value={filtros.idUsuario} onChange={handleChange}>
                                <option value="">Todos los clientes</option>
                                {usuarios.map(u => (
                                    <option key={u.idUsuario} value={u.idUsuario}>
                                        {u.apellido}, {u.nombre} ({u.dni})
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label>Instalación / Plaza</label>
                            <select
                                name="idInstalacion"
                                value={filtros.idInstalacion}
                                onChange={handleChange}
                            >
                                <option value="">Todas las instalaciones</option>
                                {instalaciones.map(inst => (
                                    <option key={inst.idInstalacion} value={inst.idInstalacion}>
                                        {inst.nombre}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                )}

                <div className="formulario-botones">
                    <button
                        type="button"
                        className="boton-secundario"
                        onClick={limpiarFiltros}
                        disabled={generandoPdf}
                    >
                        Limpiar filtros
                    </button>

                    <button type="submit" className="boton-principal" disabled={generandoPdf}>
                        {generandoPdf ? "Generando PDF..." : "Exportar PDF"}
                    </button>
                </div>
            </form>

            {/* VISTA INTERACTIVA EN PANTALLA */}
            {cargandoDatos ? (
                <p>Cargando datos del reporte...</p>
            ) : (
                <>
                    {reporteSeleccionado.ruta === "reservas" &&
                        renderAcordeon(gruposReservas, "reservas")}

                    {reporteSeleccionado.ruta === "pagos" &&
                        renderAcordeon(gruposPagos, "pagos")}

                    {reporteSeleccionado.ruta === "socios-membresias" &&
                        renderAcordeon(gruposSocios, "socios")}

                    {reporteSeleccionado.ruta === "usuarios" && (
                        <div className="tabla-contenedor">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre y Apellido</th>
                                    <th>DNI</th>
                                    <th>Usuario</th>
                                    <th>Email</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>
                                <tbody>
                                {usuariosFiltrados.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="tabla-vacia">
                                            No se encontraron usuarios.
                                        </td>
                                    </tr>
                                ) : (
                                    usuariosFiltrados.map(u => (
                                        <tr key={u.idUsuario}>
                                            <td>#{u.idUsuario}</td>
                                            <td><strong>{u.nombre} {u.apellido}</strong></td>
                                            <td>{u.dni}</td>
                                            <td>{u.nombreUsuario}</td>
                                            <td>{u.email}</td>
                                            <td>
                                                    <span className={claseEstado(u.estado)}>
                                                        {u.estado}
                                                    </span>
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {reporteSeleccionado.ruta === "instalaciones" && (
                        <div className="tabla-contenedor">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Tipo</th>
                                    <th>Capacidad</th>
                                    <th>Tarifa Base</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>
                                <tbody>
                                {instalacionesFiltradas.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="tabla-vacia">
                                            No se encontraron instalaciones.
                                        </td>
                                    </tr>
                                ) : (
                                    instalacionesFiltradas.map(inst => (
                                        <tr key={inst.idInstalacion}>
                                            <td>#{inst.idInstalacion}</td>
                                            <td><strong>{inst.nombre}</strong></td>
                                            <td>{inst.tipo}</td>
                                            <td>{inst.capacidad ? `${inst.capacidad} pers.` : "-"}</td>
                                            <td><strong>${formatearMonto(inst.tarifaBase)}</strong> / h</td>
                                            <td>
                                                    <span className={claseEstado(inst.estado)}>
                                                        {inst.estado}
                                                    </span>
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    )}
                </>
            )}
        </section>
    )
}

export default Reportes