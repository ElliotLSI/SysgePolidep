import { useEffect, useState } from "react"

function Pagos() {
    const token = sessionStorage.getItem("token")
    const usuarioActual = JSON.parse(sessionStorage.getItem("usuario"))
    const roles = usuarioActual?.roles ?? []
    const esAdministrador = roles.includes("ADMINISTRADOR")

    const [pagos, setPagos] = useState([])
    const [reservas, setReservas] = useState([])
    const [membresias, setMembresias] = useState([])
    const [abiertos, setAbiertos] = useState({})

    const [filtroTipo, setFiltroTipo] = useState("TODOS") // TODOS | RESERVA | MEMBRESIA
    const [filtroEstado, setFiltroEstado] = useState("TODOS") // TODOS | APROBADO | PENDIENTE | RECHAZADO
    const [busqueda, setBusqueda] = useState("")

    const [mostrarFormulario, setMostrarFormulario] = useState(false)
    const [tipoPago, setTipoPago] = useState("RESERVA")

    const [nuevoPago, setNuevoPago] = useState({
        idReserva: "",
        idMembresia: "",
        montoTotal: "",
        medioPago: "EFECTIVO",
        estado: "PENDIENTE"
    })

    const [error, setError] = useState("")
    const [mensaje, setMensaje] = useState("")

    const formatearMonto = (monto) =>
        Number(monto ?? 0).toLocaleString("es-AR", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })

    const claseEstado = (estado) => {
        if (!estado) return "estado"
        return `estado estado-${estado.toLowerCase()}`
    }

    const obtenerEstadoRealMembresia = (m) => {
        if (m.estado === "VIGENTE" && m.fechaVenc) {
            const hoy = new Date()
            hoy.setHours(0, 0, 0, 0)
            const venc = new Date(m.fechaVenc + "T00:00:00")
            if (venc < hoy) return "VENCIDA"
        }
        return m.estado
    }

    const cargarDatos = async () => {
        try {
            const headers = { Authorization: `Bearer ${token}` }
            const [resPag, resRes, resMemb] = await Promise.all([
                fetch("http://localhost:8080/api/pagos", { headers }),
                fetch("http://localhost:8080/api/reservas", { headers }),
                fetch("http://localhost:8080/api/membresias", { headers }).catch(() => null)
            ])

            if (!resPag.ok || !resRes.ok) {
                throw new Error("No se pudieron obtener los datos.")
            }

            setPagos(await resPag.json())
            setReservas(await resRes.json())
            setMembresias(resMemb?.ok ? await resMemb.json() : [])
        } catch (err) {
            console.error(err)
            setError("No se pudieron obtener los datos.")
        }
    }

    useEffect(() => {
        cargarDatos()
    }, [])

    // Una sola membresía por socio (la más reciente) filtrada al usuario logueado si no es Admin
    const membresiasUnicasPorSocio = (() => {
        const mapa = new Map()
        const lista = esAdministrador
            ? membresias
            : membresias.filter(m => m.socio?.idUsuario === usuarioActual?.idUsuario)

        lista.forEach(m => {
            const clave = m.socio?.idUsuario ? String(m.socio.idUsuario) : `m-${m.idMembresia}`
            const actual = mapa.get(clave)
            if (!actual || (m.fechaVenc || "") > (actual.fechaVenc || "")) {
                mapa.set(clave, m)
            }
        })
        return Array.from(mapa.values())
    })()

    const manejarCambio = (e) => {
        const { name, value } = e.target
        setNuevoPago(prev => ({ ...prev, [name]: value }))

        if (name === "idReserva") {
            const reserva = reservas.find(r => r.idReserva === Number(value))
            if (reserva) {
                setNuevoPago(prev => ({
                    ...prev,
                    idReserva: value,
                    montoTotal: reserva.montoTotal
                }))
            }
        }

        if (name === "idMembresia") {
            const membresia = membresiasUnicasPorSocio.find(m => m.idMembresia === Number(value))
            if (membresia) {
                setNuevoPago(prev => ({
                    ...prev,
                    idMembresia: value,
                    montoTotal: membresia.categoria?.costo || ""
                }))
            }
        }
    }

    const cambiarTipoPago = (tipo) => {
        setTipoPago(tipo)
        setNuevoPago({
            idReserva: "",
            idMembresia: "",
            montoTotal: "",
            medioPago: "EFECTIVO",
            estado: "PENDIENTE"
        })
        setError("")
    }

    const crearPago = async (e) => {
        e.preventDefault()
        setError("")
        setMensaje("")

        try {
            const datos = {
                montoTotal: Number(nuevoPago.montoTotal),
                medioPago: nuevoPago.medioPago,
                estado: "PENDIENTE"
            }

            if (tipoPago === "RESERVA") {
                datos.reserva = { idReserva: Number(nuevoPago.idReserva) }
            } else {
                datos.idMembresia = Number(nuevoPago.idMembresia)
            }

            const respuesta = await fetch("http://localhost:8080/api/pagos", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(datos)
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo enviar la solicitud de pago.")
            }

            limpiarFormulario()
            setMensaje("Solicitud de pago enviada correctamente. Quedó en estado PENDIENTE de aprobación.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const cambiarEstadoPago = async (id, nuevoEstado) => {
        if (!esAdministrador) return
        const accion = nuevoEstado === "APROBADO" ? "aprobar" : "rechazar"
        if (!window.confirm(`¿Querés ${accion} esta solicitud de pago?`)) return

        setError("")
        setMensaje("")
        try {
            const respuesta = await fetch(
                `http://localhost:8080/api/pagos/${id}/estado?estado=${nuevoEstado}`,
                {
                    method: "PUT",
                    headers: { Authorization: `Bearer ${token}` }
                }
            )

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo actualizar el estado del pago.")
            }

            setMensaje(`Pago ${nuevoEstado.toLowerCase()} correctamente.`)
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const eliminarPago = async (id) => {
        if (!esAdministrador) return
        if (!window.confirm("¿Querés eliminar este pago?")) return

        setError("")
        setMensaje("")
        try {
            const respuesta = await fetch(`http://localhost:8080/api/pagos/${id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` }
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo eliminar el pago.")
            }

            setMensaje("Pago eliminado correctamente.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const limpiarFormulario = () => {
        setNuevoPago({
            idReserva: "",
            idMembresia: "",
            montoTotal: "",
            medioPago: "EFECTIVO",
            estado: "PENDIENTE"
        })
        setTipoPago("RESERVA")
        setMostrarFormulario(false)
        setError("")
    }

    const obtenerNombreUsuarioReserva = (r) =>
        r?.usuario ? `${r.usuario.nombre || ""} ${r.usuario.apellido || ""}`.trim() : "Sin usuario"

    const obtenerNombreSocioMembresia = (m) =>
        m?.socio?.usuario
            ? `${m.socio.usuario.nombre || ""} ${m.socio.usuario.apellido || ""}`.trim()
            : "Sin socio"

    const obtenerDetallePago = (pago) => {
        if (pago.reserva) {
            const u = pago.reserva.usuario
            return {
                claveCliente: u?.idUsuario ? String(u.idUsuario) : "sin-asignar",
                dni: u?.dni || "",
                tipo: "Reserva",
                cliente: obtenerNombreUsuarioReserva(pago.reserva),
                referencia: `Reserva #${pago.reserva.idReserva}`,
                extra: pago.reserva.instalacion?.nombre || ""
            }
        }
        const membresia = membresias.find(m => m.idMembresia === pago.idMembresia)
        const u = membresia?.socio?.usuario
        return {
            claveCliente: u?.idUsuario ? String(u.idUsuario) : `memb-${pago.idMembresia}`,
            dni: u?.dni || "",
            tipo: "Membresía",
            cliente: membresia ? obtenerNombreSocioMembresia(membresia) : "Socio",
            referencia: `Membresía #${pago.idMembresia}`,
            extra: membresia?.categoria?.nombre || ""
        }
    }

    const reservasPendientes = reservas.filter(r => r.estado === "PENDIENTE")
    const solicitudesPendientes = pagos.filter(p => p.estado === "PENDIENTE")

    const totalPagosReservas = pagos.filter(p => Boolean(p.reserva)).length
    const totalPagosMembresias = pagos.filter(p => !p.reserva).length

    const pagosFiltrados = pagos.filter(p => {
        const esReserva = Boolean(p.reserva)
        if (filtroTipo === "RESERVA" && !esReserva) return false
        if (filtroTipo === "MEMBRESIA" && esReserva) return false
        if (filtroEstado !== "TODOS" && p.estado !== filtroEstado) return false

        const texto = busqueda.trim().toLowerCase()
        if (!texto) return true

        const detalle = obtenerDetallePago(p)
        return (
            detalle.cliente.toLowerCase().includes(texto) ||
            detalle.referencia.toLowerCase().includes(texto) ||
            detalle.extra.toLowerCase().includes(texto) ||
            (p.medioPago || "").toLowerCase().includes(texto)
        )
    })

    const gruposPorCliente = (() => {
        const mapa = new Map()
        pagosFiltrados.forEach(p => {
            const detalle = obtenerDetallePago(p)
            if (!mapa.has(detalle.claveCliente)) {
                mapa.set(detalle.claveCliente, {
                    clave: detalle.claveCliente,
                    cliente: detalle.cliente,
                    dni: detalle.dni,
                    items: [],
                    subtotal: 0
                })
            }
            const grupo = mapa.get(detalle.claveCliente)
            grupo.items.push(p)
            grupo.subtotal += Number(p.montoTotal ?? 0)
        })
        return Array.from(mapa.values())
    })()

    const toggleGrupo = (clave) => {
        setAbiertos(prev => ({ ...prev, [clave]: !prev[clave] }))
    }

    const montoAcumulado = pagosFiltrados.reduce(
        (acc, p) => acc + Number(p.montoTotal ?? 0),
        0
    )

    return (
        <section className="usuarios">
            <div className="seccion-titulo">
                <div>
                    <h2>Pagos</h2>
                    <p>
                        {esAdministrador
                            ? "Aprobá o rechazá las solicitudes de pago enviadas por los clientes."
                            : "Enviá tus solicitudes de pago de reservas o membresías y consultá su estado."}
                    </p>
                </div>

                {!esAdministrador && (
                    <button
                        className="boton-principal"
                        onClick={() => {
                            setError("")
                            setMensaje("")
                            setMostrarFormulario(!mostrarFormulario)
                        }}
                    >
                        {mostrarFormulario ? "Cerrar formulario" : "+ Solicitar pago"}
                    </button>
                )}
            </div>

            {mensaje && <div className="mensaje-exito">{mensaje}</div>}
            {error && <div className="mensaje-error">{error}</div>}

            {/* PANEL DE SOLICITUDES DE PAGO PENDIENTES (SOLO ADMIN) */}
            {esAdministrador && (
                <section style={{ marginBottom: "24px" }}>
                    <h3 style={{ margin: "0 0 10px", color: "#5C0613" }}>
                        Solicitudes de pago pendientes ({solicitudesPendientes.length})
                    </h3>

                    {solicitudesPendientes.length === 0 ? (
                        <div
                            className="tabla-contenedor"
                            style={{ padding: "18px", color: "#64748B", fontSize: "14px" }}
                        >
                            No hay solicitudes de pago pendientes de aprobación.
                        </div>
                    ) : (
                        <div className="tarjetas">
                            {solicitudesPendientes.map(p => {
                                const detalle = obtenerDetallePago(p)
                                return (
                                    <div
                                        className="tarjeta"
                                        key={p.idPago}
                                        style={{ borderLeft: "4px solid #D97706", background: "#FFFDF5" }}
                                    >
                                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "8px", marginBottom: "8px" }}>
                                            <h3 style={{ margin: 0 }}>{detalle.cliente}</h3>
                                            <span className="estado estado-pendiente">PENDIENTE</span>
                                        </div>
                                        <p><strong>Concepto:</strong> {detalle.referencia} {detalle.extra ? `— ${detalle.extra}` : ""}</p>
                                        <p><strong>Medio:</strong> {p.medioPago}</p>
                                        <p style={{ marginBottom: "14px" }}>
                                            <strong>Monto:</strong>{" "}
                                            <span style={{ color: "#5C0613", fontWeight: "bold" }}>
                                                ${formatearMonto(p.montoTotal)}
                                            </span>
                                        </p>
                                        <div style={{ display: "flex", gap: "8px" }}>
                                            <button
                                                className="boton-editar"
                                                style={{ background: "#15803D", marginRight: 0 }}
                                                onClick={() => cambiarEstadoPago(p.idPago, "APROBADO")}
                                            >
                                                Aprobar
                                            </button>
                                            <button
                                                className="boton-secundario"
                                                onClick={() => cambiarEstadoPago(p.idPago, "RECHAZADO")}
                                            >
                                                Rechazar
                                            </button>
                                        </div>
                                    </div>
                                )
                            })}
                        </div>
                    )}
                </section>
            )}

            {/* FORMULARIO SOLICITUD DE PAGO (SOLO CLIENTES) */}
            {!esAdministrador && mostrarFormulario && (
                <form className="formulario-usuario" onSubmit={crearPago}>
                    <h3>Solicitar pago</h3>

                    <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", marginBottom: "18px" }}>
                        <button
                            type="button"
                            className={tipoPago === "RESERVA" ? "boton-principal" : "boton-secundario"}
                            onClick={() => cambiarTipoPago("RESERVA")}
                        >
                            Pago de reserva ({reservasPendientes.length} pendientes)
                        </button>
                        <button
                            type="button"
                            className={tipoPago === "MEMBRESIA" ? "boton-principal" : "boton-secundario"}
                            onClick={() => cambiarTipoPago("MEMBRESIA")}
                        >
                            Pago de membresía ({membresiasUnicasPorSocio.length})
                        </button>
                    </div>

                    <div className="formulario-grid">
                        {tipoPago === "RESERVA" && (
                            <div>
                                <label>Reserva pendiente</label>
                                <select
                                    name="idReserva"
                                    value={nuevoPago.idReserva}
                                    onChange={manejarCambio}
                                    required
                                >
                                    <option value="">Seleccionar reserva pendiente</option>
                                    {reservasPendientes.map(r => (
                                        <option key={r.idReserva} value={r.idReserva}>
                                            Reserva #{r.idReserva} — {r.instalacion?.nombre || ""} — ${formatearMonto(r.montoTotal)}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}

                        {tipoPago === "MEMBRESIA" && (
                            <div>
                                <label>Membresía</label>
                                <select
                                    name="idMembresia"
                                    value={nuevoPago.idMembresia}
                                    onChange={manejarCambio}
                                    required
                                >
                                    <option value="">Seleccionar membresía</option>
                                    {membresiasUnicasPorSocio.map(m => (
                                        <option key={m.idMembresia} value={m.idMembresia}>
                                            Membresía #{m.idMembresia} — {m.categoria?.nombre || ""} ({obtenerEstadoRealMembresia(m)}) — ${formatearMonto(m.categoria?.costo)}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}

                        <div>
                            <label>Monto total ($)</label>
                            <input
                                type="number"
                                name="montoTotal"
                                min="0"
                                step="0.01"
                                value={nuevoPago.montoTotal}
                                readOnly
                                required
                            />
                        </div>

                        <div>
                            <label>Medio de pago</label>
                            <select
                                name="medioPago"
                                value={nuevoPago.medioPago}
                                onChange={manejarCambio}
                                required
                            >
                                <option value="EFECTIVO">Efectivo</option>
                                <option value="MERCADOPAGO">Mercado Pago</option>
                                <option value="TRANSFERENCIA">Transferencia</option>
                                <option value="TARJETA">Tarjeta</option>
                            </select>
                        </div>

                        <div>
                            <label>Estado inicial</label>
                            <input type="text" value="PENDIENTE (esperando aprobación)" readOnly />
                        </div>
                    </div>

                    <div className="formulario-botones">
                        <button type="button" className="boton-secundario" onClick={limpiarFormulario}>
                            Cancelar
                        </button>
                        <button type="submit" className="boton-principal">
                            Enviar solicitud de pago
                        </button>
                    </div>
                </form>
            )}

            {/* BARRA DE FILTROS: TIPO + BUSCADOR + ESTADO */}
            <div
                style={{
                    display: "flex",
                    flexWrap: "wrap",
                    justifyContent: "space-between",
                    alignItems: "center",
                    gap: "12px",
                    marginBottom: "14px"
                }}
            >
                <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
                    <button
                        type="button"
                        className={filtroTipo === "TODOS" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroTipo("TODOS")}
                    >
                        Todos ({pagos.length})
                    </button>
                    <button
                        type="button"
                        className={filtroTipo === "RESERVA" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroTipo("RESERVA")}
                    >
                        Reservas ({totalPagosReservas})
                    </button>
                    <button
                        type="button"
                        className={filtroTipo === "MEMBRESIA" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroTipo("MEMBRESIA")}
                    >
                        Membresías ({totalPagosMembresias})
                    </button>
                </div>

                <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
                    <input
                        type="text"
                        placeholder="Buscar cliente, cancha, medio..."
                        value={busqueda}
                        onChange={e => setBusqueda(e.target.value)}
                        style={{
                            width: "240px",
                            padding: "8px 12px",
                            border: "1px solid #CBD5E1",
                            borderRadius: "7px",
                            fontSize: "14px"
                        }}
                    />

                    <select
                        value={filtroEstado}
                        onChange={e => setFiltroEstado(e.target.value)}
                        style={{
                            padding: "8px 12px",
                            border: "1px solid #CBD5E1",
                            borderRadius: "7px",
                            fontSize: "14px",
                            background: "white"
                        }}
                    >
                        <option value="TODOS">Todos los estados</option>
                        <option value="APROBADO">Aprobados</option>
                        <option value="PENDIENTE">Pendientes</option>
                        <option value="RECHAZADO">Rechazados</option>
                    </select>
                </div>
            </div>

            {/* RESUMEN DEL LISTADO ACTUAL */}
            <div className="resumen-descuento" style={{ marginBottom: "16px" }}>
                <div>
                    {esAdministrador ? (
                        <>
                            <strong>{gruposPorCliente.length}</strong> cliente(s) —{" "}
                            <strong>{pagosFiltrados.length}</strong> pago(s)
                        </>
                    ) : (
                        <>
                            Mostrando <strong>{pagosFiltrados.length}</strong> pago(s)
                        </>
                    )}
                    {filtroTipo === "RESERVA" && " de reservas"}
                    {filtroTipo === "MEMBRESIA" && " de membresías"}
                </div>
                <div>
                    Total acumulado:{" "}
                    <strong style={{ color: "#5C0613", fontSize: "15px" }}>
                        ${formatearMonto(montoAcumulado)}
                    </strong>
                </div>
            </div>

            {/* LISTADO: AGRUPADO POR CLIENTE PARA ADMIN / TABLA DIRECTA PARA CLIENTE */}
            {esAdministrador ? (
                gruposPorCliente.length === 0 ? (
                    <div className="tabla-contenedor" style={{ padding: "28px", textAlign: "center", color: "#64748B" }}>
                        No se encontraron pagos con los filtros seleccionados.
                    </div>
                ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                        {gruposPorCliente.map(grupo => {
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
                                            <strong style={{ color: "#1E293B", fontSize: "15px" }}>
                                                {grupo.cliente}
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
                                                {grupo.items.length} pago(s)
                                            </span>
                                            <strong style={{ color: "#5C0613" }}>
                                                ${formatearMonto(grupo.subtotal)}
                                            </strong>
                                        </div>
                                    </button>

                                    {estaAbierto && (
                                        <div style={{ overflowX: "auto" }}>
                                            <table>
                                                <thead>
                                                <tr>
                                                    <th>ID</th>
                                                    <th>Tipo</th>
                                                    <th>Detalle</th>
                                                    <th>Fecha</th>
                                                    <th>Medio</th>
                                                    <th>Monto</th>
                                                    <th>Estado</th>
                                                    <th>Acciones</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                {grupo.items.map(p => {
                                                    const detalle = obtenerDetallePago(p)
                                                    return (
                                                        <tr key={p.idPago}>
                                                            <td>#{p.idPago}</td>
                                                            <td>
                                                                <span
                                                                    className="estado"
                                                                    style={{
                                                                        background: p.reserva ? "#F8EDEF" : "#EFF6FF",
                                                                        color: p.reserva ? "#5C0613" : "#1D4ED8"
                                                                    }}
                                                                >
                                                                    {detalle.tipo}
                                                                </span>
                                                            </td>
                                                            <td>
                                                                <div>{detalle.referencia}</div>
                                                                {detalle.extra && (
                                                                    <small style={{ color: "#64748B" }}>{detalle.extra}</small>
                                                                )}
                                                            </td>
                                                            <td>
                                                                {p.fecha
                                                                    ? new Date(p.fecha).toLocaleString("es-AR", {
                                                                        dateStyle: "short",
                                                                        timeStyle: "short"
                                                                    })
                                                                    : "-"}
                                                            </td>
                                                            <td>{p.medioPago}</td>
                                                            <td><strong>${formatearMonto(p.montoTotal)}</strong></td>
                                                            <td>
                                                                <span className={claseEstado(p.estado)}>
                                                                    {p.estado}
                                                                </span>
                                                            </td>
                                                            <td style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
                                                                {p.estado === "PENDIENTE" && (
                                                                    <>
                                                                        <button
                                                                            className="boton-editar"
                                                                            style={{ background: "#15803D", marginRight: 0 }}
                                                                            onClick={() => cambiarEstadoPago(p.idPago, "APROBADO")}
                                                                        >
                                                                            Aprobar
                                                                        </button>
                                                                        <button
                                                                            className="boton-secundario"
                                                                            style={{ padding: "7px 10px", fontSize: "13px" }}
                                                                            onClick={() => cambiarEstadoPago(p.idPago, "RECHAZADO")}
                                                                        >
                                                                            Rechazar
                                                                        </button>
                                                                    </>
                                                                )}
                                                                <button
                                                                    className="boton-eliminar"
                                                                    onClick={() => eliminarPago(p.idPago)}
                                                                >
                                                                    Eliminar
                                                                </button>
                                                            </td>
                                                        </tr>
                                                    )
                                                })}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </div>
                            )
                        })}
                    </div>
                )
            ) : (
                <div className="tabla-contenedor">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tipo</th>
                            <th>Detalle</th>
                            <th>Fecha</th>
                            <th>Medio</th>
                            <th>Monto</th>
                            <th>Estado</th>
                        </tr>
                        </thead>
                        <tbody>
                        {pagosFiltrados.length === 0 ? (
                            <tr>
                                <td colSpan={7} className="tabla-vacia">
                                    No se encontraron pagos con los filtros seleccionados.
                                </td>
                            </tr>
                        ) : (
                            pagosFiltrados.map(p => {
                                const detalle = obtenerDetallePago(p)
                                return (
                                    <tr key={p.idPago}>
                                        <td>#{p.idPago}</td>
                                        <td>
                                            <span
                                                className="estado"
                                                style={{
                                                    background: p.reserva ? "#F8EDEF" : "#EFF6FF",
                                                    color: p.reserva ? "#5C0613" : "#1D4ED8"
                                                }}
                                            >
                                                {detalle.tipo}
                                            </span>
                                        </td>
                                        <td>
                                            <div>{detalle.referencia}</div>
                                            {detalle.extra && (
                                                <small style={{ color: "#64748B" }}>{detalle.extra}</small>
                                            )}
                                        </td>
                                        <td>
                                            {p.fecha
                                                ? new Date(p.fecha).toLocaleString("es-AR", {
                                                    dateStyle: "short",
                                                    timeStyle: "short"
                                                })
                                                : "-"}
                                        </td>
                                        <td>{p.medioPago}</td>
                                        <td><strong>${formatearMonto(p.montoTotal)}</strong></td>
                                        <td>
                                            <span className={claseEstado(p.estado)}>
                                                {p.estado}
                                            </span>
                                        </td>
                                    </tr>
                                )
                            })
                        )}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    )
}

export default Pagos