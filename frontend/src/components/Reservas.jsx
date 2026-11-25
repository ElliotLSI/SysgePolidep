import { useEffect, useState } from "react"

function Reservas() {
    const [reservas, setReservas] = useState([])
    const [usuarios, setUsuarios] = useState([])
    const [instalaciones, setInstalaciones] = useState([])
    const [membresias, setMembresias] = useState([])
    const [reservasAFavor, setReservasAFavor] = useState([])

    const [filtroEstado, setFiltroEstado] = useState("TODAS") // TODAS | PENDIENTE | PENDIENTE_APROBACION | CONFIRMADA | CANCELADA

    const [mostrarFormulario, setMostrarFormulario] = useState(false)
    const [mostrarFormularioReservaAFavor, setMostrarFormularioReservaAFavor] = useState(false)
    const [reservaAFavorSeleccionada, setReservaAFavorSeleccionada] = useState(null)

    const [nuevaReserva, setNuevaReserva] = useState({
        idUsuario: "",
        idInstalacion: "",
        fechaReserva: "",
        horaInicio: "",
        duracionHoras: 1,
        montoTotal: ""
    })

    const [nuevaReservaAFavor, setNuevaReservaAFavor] = useState({
        fechaReserva: "",
        horaInicio: ""
    })

    const [error, setError] = useState("")
    const [mensaje, setMensaje] = useState("")

    const token = sessionStorage.getItem("token")
    const usuarioActual = JSON.parse(sessionStorage.getItem("usuario"))
    const roles = usuarioActual?.roles ?? []
    const esAdministrador = roles.includes("ADMINISTRADOR")
    const esEmpleado = roles.includes("EMPLEADO")
    const esCliente = !esAdministrador && !esEmpleado

    const DIAS_AVISO_VENCIMIENTO = 15

    const obtenerFechaActual = () => {
        const ahora = new Date()
        const año = ahora.getFullYear()
        const mes = String(ahora.getMonth() + 1).padStart(2, "0")
        const dia = String(ahora.getDate()).padStart(2, "0")
        return `${año}-${mes}-${dia}`
    }

    const obtenerHoraActual = () => {
        const ahora = new Date()
        const horas = String(ahora.getHours()).padStart(2, "0")
        const minutos = String(ahora.getMinutes()).padStart(2, "0")
        return `${horas}:${minutos}`
    }

    const formatearFecha = (fechaStr) => {
        if (!fechaStr) return "-"
        const [anio, mes, dia] = fechaStr.split("-")
        return `${dia}/${mes}/${anio}`
    }

    const calcularDiasRestantes = (fechaVencStr) => {
        if (!fechaVencStr) return null
        const hoy = new Date()
        hoy.setHours(0, 0, 0, 0)
        const venc = new Date(fechaVencStr + "T00:00:00")
        return Math.ceil((venc - hoy) / (1000 * 60 * 60 * 24))
    }

    const puedeCancelarReserva = (r) => {
        if (!r || r.estado === "CANCELADA" || r.estado === "PENDIENTE_APROBACION" || !r.fechaReserva || !r.horaInicio) {
            return false
        }

        const inicio = new Date(`${r.fechaReserva}T${r.horaInicio.slice(0, 5)}:00`)
        const ahora = new Date()

        if (ahora >= inicio) return false

        const limite = new Date(inicio)
        let horasHabiles = 0

        while (horasHabiles < 48) {
            limite.setHours(limite.getHours() - 1)
            const diaSemana = limite.getDay() // 1 = lunes ... 5 = viernes
            if (diaSemana >= 1 && diaSemana <= 5) {
                horasHabiles++
            }
        }

        return ahora <= limite
    }

    const formatearMonto = (monto) =>
        Number(monto ?? 0).toLocaleString("es-AR", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })

    const claseEstado = (estado) => {
        if (!estado) return "estado"
        if (estado === "PENDIENTE_APROBACION") return "estado estado-pendiente"
        return `estado estado-${estado.toLowerCase()}`
    }

    const textoEstado = (estado) => {
        if (estado === "PENDIENTE_APROBACION") return "POR APROBAR"
        return estado
    }

    const obtenerMembresiaVigente = (idUsuario) => {
        if (!idUsuario) return null
        const hoy = obtenerFechaActual()
        return membresias.find(m =>
            m.socio?.idUsuario === Number(idUsuario) &&
            m.estado === "VIGENTE" &&
            (!m.fechaInicio || m.fechaInicio <= hoy) &&
            (!m.fechaVenc || m.fechaVenc >= hoy)
        ) || null
    }

    const calcularMontos = (idUsuario, idInstalacion, duracionHoras) => {
        const inst = instalaciones.find(i => i.idInstalacion === Number(idInstalacion))
        const duracion = Number(duracionHoras)
        if (!inst || !duracion || duracion <= 0) {
            return { base: 0, descuento: 0, final: "", categoria: null }
        }
        const base = Number(inst.tarifaBase) * duracion
        const membresia = obtenerMembresiaVigente(idUsuario)
        const descuento = membresia?.categoria?.porcDescuento ?? 0
        const final = Math.max(0, base - (base * descuento) / 100)
        return {
            base,
            descuento,
            final,
            categoria: membresia?.categoria?.nombre || null
        }
    }

    const cargarDatos = async () => {
        try {
            const headers = { Authorization: `Bearer ${token}` }
            const [resRes, resUsu, resInst, resMemb] = await Promise.all([
                fetch("http://localhost:8080/api/reservas", { headers }),
                esCliente
                    ? Promise.resolve(null)
                    : fetch("http://localhost:8080/api/usuarios", { headers }).catch(() => null),
                fetch("http://localhost:8080/api/instalaciones", { headers }),
                fetch("http://localhost:8080/api/membresias", { headers }).catch(() => null)
            ])

            if (!resRes.ok || !resInst.ok) {
                throw new Error("No se pudieron obtener los datos.")
            }

            setReservas(await resRes.json())
            setUsuarios(resUsu?.ok ? await resUsu.json() : [])
            setInstalaciones(await resInst.json())
            setMembresias(resMemb?.ok ? await resMemb.json() : [])

            const hoy = obtenerFechaActual()

            if (esCliente && usuarioActual?.idUsuario) {
                const resFavor = await fetch(
                    `http://localhost:8080/api/reservas-a-favor/usuario/${usuarioActual.idUsuario}/disponibles`,
                    { headers }
                )
                setReservasAFavor(resFavor.ok ? await resFavor.json() : [])
            } else if (!esCliente) {
                const resFavor = await fetch(
                    "http://localhost:8080/api/reservas-a-favor",
                    { headers }
                )
                if (resFavor.ok) {
                    const todas = await resFavor.json()
                    const disponibles = todas.filter(
                        rf => !rf.utilizada && (!rf.fechaVencimiento || rf.fechaVencimiento > hoy)
                    )
                    setReservasAFavor(disponibles)
                } else {
                    setReservasAFavor([])
                }
            } else {
                setReservasAFavor([])
            }
        } catch (err) {
            console.error(err)
            setError("No se pudieron obtener los datos.")
        }
    }

    useEffect(() => {
        cargarDatos()
    }, [])

    const abrirFormulario = () => {
        setError("")
        setMensaje("")
        setMostrarFormularioReservaAFavor(false)
        setNuevaReserva({
            idUsuario: esCliente && usuarioActual?.idUsuario ? usuarioActual.idUsuario : "",
            idInstalacion: "",
            fechaReserva: "",
            horaInicio: "",
            duracionHoras: 1,
            montoTotal: ""
        })
        setMostrarFormulario(true)
    }

    const manejarCambio = (e) => {
        const { name, value } = e.target
        const actualizado = { ...nuevaReserva, [name]: value }

        if (name === "idInstalacion" || name === "duracionHoras" || name === "idUsuario") {
            const calculo = calcularMontos(
                actualizado.idUsuario,
                actualizado.idInstalacion,
                actualizado.duracionHoras
            )
            actualizado.montoTotal = calculo.final
        }

        setNuevaReserva(actualizado)
    }

    const crearReserva = async (e) => {
        e.preventDefault()
        setError("")
        setMensaje("")

        const fechaActual = obtenerFechaActual()
        const horaActual = obtenerHoraActual()

        if (nuevaReserva.fechaReserva < fechaActual) {
            setError("No podés realizar una reserva en una fecha anterior a la actual.")
            return
        }
        if (nuevaReserva.fechaReserva === fechaActual && nuevaReserva.horaInicio < horaActual) {
            setError("No podés realizar una reserva en una hora anterior a la actual.")
            return
        }

        try {
            const datos = {
                usuario: { idUsuario: Number(nuevaReserva.idUsuario) },
                instalacion: { idInstalacion: Number(nuevaReserva.idInstalacion) },
                fechaReserva: nuevaReserva.fechaReserva,
                horaInicio: nuevaReserva.horaInicio,
                duracionHoras: Number(nuevaReserva.duracionHoras),
                montoTotal: Number(nuevaReserva.montoTotal),
                estado: "PENDIENTE"
            }

            const respuesta = await fetch("http://localhost:8080/api/reservas", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(datos)
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo crear la reserva.")
            }

            limpiarFormulario()
            setMensaje("Reserva creada correctamente. Recordá registrar el pago para confirmarla.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const cancelarReserva = async (id) => {
        const reserva = reservas.find(r => r.idReserva === id)
        if (!reserva || reserva.estado === "CANCELADA") return

        if (esCliente && reserva.usuario?.idUsuario !== usuarioActual?.idUsuario) {
            setError("No podés cancelar una reserva de otro usuario.")
            return
        }

        const estabaConfirmada = reserva.estado === "CONFIRMADA"
        const textoConfirmacion = estabaConfirmada
            ? "¿Querés cancelar esta reserva? Se generará una reserva a favor válida por 3 meses."
            : "¿Querés cancelar esta reserva pendiente? Como aún no tiene pago confirmado, no generará reserva a favor."

        if (!window.confirm(textoConfirmacion)) {
            return
        }

        try {
            setError("")
            setMensaje("")
            const respuesta = await fetch(`http://localhost:8080/api/reservas/${id}/cancelar`, {
                method: "PUT",
                headers: { Authorization: `Bearer ${token}` }
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo cancelar la reserva.")
            }

            setMensaje(
                estabaConfirmada
                    ? "La reserva fue cancelada correctamente. Tenés 3 meses para reprogramarla con tu reserva a favor."
                    : "La reserva pendiente fue cancelada correctamente."
            )
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const abrirFormularioReservaAFavor = (reservaAFavor) => {
        setError("")
        setMensaje("")
        setMostrarFormulario(false)
        setReservaAFavorSeleccionada(reservaAFavor)
        setNuevaReservaAFavor({ fechaReserva: "", horaInicio: "" })
        setMostrarFormularioReservaAFavor(true)
    }

    const manejarCambioReservaAFavor = (e) => {
        const { name, value } = e.target
        setNuevaReservaAFavor(prev => ({ ...prev, [name]: value }))
    }

    const usarReservaAFavor = async (e) => {
        e.preventDefault()
        setError("")
        setMensaje("")

        if (!reservaAFavorSeleccionada) {
            setError("No se seleccionó una reserva a favor.")
            return
        }

        const fechaActual = obtenerFechaActual()
        const horaActual = obtenerHoraActual()

        if (nuevaReservaAFavor.fechaReserva < fechaActual) {
            setError("No podés utilizar una reserva a favor en una fecha pasada.")
            return
        }
        if (nuevaReservaAFavor.fechaReserva === fechaActual && nuevaReservaAFavor.horaInicio < horaActual) {
            setError("No podés utilizar una reserva a favor en una hora pasada.")
            return
        }

        try {
            const params = new URLSearchParams({
                fecha: nuevaReservaAFavor.fechaReserva,
                hora: nuevaReservaAFavor.horaInicio
            })

            const respuesta = await fetch(
                `http://localhost:8080/api/reservas-a-favor/${reservaAFavorSeleccionada.idReservaAFavor}/reprogramar?${params.toString()}`,
                {
                    method: "POST",
                    headers: { Authorization: `Bearer ${token}` }
                }
            )

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo utilizar la reserva a favor.")
            }

            setMensaje(
                esCliente
                    ? "Solicitud de reprogramación enviada. Quedó pendiente de aprobación del administrador."
                    : "La reserva fue reprogramada y confirmada utilizando la reserva a favor."
            )
            cerrarFormularioReservaAFavor()
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const responderReprogramacion = async (idReserva, accion) => {
        if (esCliente) return
        if (!window.confirm(`¿Querés ${accion} esta solicitud de reprogramación?`)) return

        setError("")
        setMensaje("")
        try {
            const respuesta = await fetch(
                `http://localhost:8080/api/reservas-a-favor/reprogramacion/${idReserva}/${accion}`,
                {
                    method: "PUT",
                    headers: { Authorization: `Bearer ${token}` }
                }
            )
            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || `No se pudo ${accion} la reprogramación.`)
            }
            setMensaje(
                accion === "aprobar"
                    ? "Reprogramación aprobada. La reserva quedó CONFIRMADA."
                    : "Reprogramación rechazada. Se devolvió la reserva a favor al cliente."
            )
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const limpiarFormulario = () => {
        setNuevaReserva({
            idUsuario: "",
            idInstalacion: "",
            fechaReserva: "",
            horaInicio: "",
            duracionHoras: 1,
            montoTotal: ""
        })
        setMostrarFormulario(false)
    }

    const cerrarFormularioReservaAFavor = () => {
        setMostrarFormularioReservaAFavor(false)
        setReservaAFavorSeleccionada(null)
        setNuevaReservaAFavor({ fechaReserva: "", horaInicio: "" })
    }

    const instalacionSeleccionada = instalaciones.find(
        i => i.idInstalacion === Number(nuevaReserva.idInstalacion)
    )

    const calculoActual = calcularMontos(
        nuevaReserva.idUsuario,
        nuevaReserva.idInstalacion,
        nuevaReserva.duracionHoras
    )

    const reservasPorVencer = reservasAFavor.filter(rf => {
        const dias = calcularDiasRestantes(rf.fechaVencimiento)
        return dias !== null && dias >= 0 && dias <= DIAS_AVISO_VENCIMIENTO
    })

    const solicitudesReprogramacion = reservas.filter(r => r.estado === "PENDIENTE_APROBACION")

    const totalPendientes = reservas.filter(r => r.estado === "PENDIENTE").length
    const totalPorAprobar = solicitudesReprogramacion.length
    const totalConfirmadas = reservas.filter(r => r.estado === "CONFIRMADA").length
    const totalCanceladas = reservas.filter(r => r.estado === "CANCELADA").length

    const reservasFiltradas =
        filtroEstado === "TODAS"
            ? reservas
            : reservas.filter(r => r.estado === filtroEstado)

    return (
        <section className="usuarios">
            <div className="seccion-titulo">
                <div>
                    <h2>Reservas</h2>
                    <p>Gestión de reservas de instalaciones deportivas (horario de 10:00 a 03:00).</p>
                </div>

                <button className="boton-principal" onClick={abrirFormulario}>
                    + Nueva reserva
                </button>
            </div>

            {mensaje && <div className="mensaje-exito">{mensaje}</div>}
            {error && <div className="mensaje-error">{error}</div>}

            {/* NOTIFICACIÓN DE RESERVAS A FAVOR PRÓXIMAS A PERDERSE */}
            {reservasPorVencer.length > 0 && (
                <div
                    style={{
                        background: "#FFFBEB",
                        border: "1px solid #F59E0B",
                        borderLeft: "5px solid #D97706",
                        borderRadius: "8px",
                        padding: "14px 16px",
                        marginBottom: "20px",
                        color: "#92400E"
                    }}
                >
                    <strong style={{ display: "block", marginBottom: "4px" }}>
                        Aviso: {reservasPorVencer.length === 1
                        ? "Tenés 1 reserva a favor próxima a vencer"
                        : `Tenés ${reservasPorVencer.length} reservas a favor próximas a vencer`}
                    </strong>
                    <span style={{ fontSize: "14px" }}>
                        Reprogramala antes de su fecha de vencimiento (plazo de 3 meses desde la cancelación) para no perderla.
                    </span>
                </div>
            )}

            {/* SOLICITUDES DE REPROGRAMACIÓN PENDIENTES (SOLO ADMIN/EMPLEADO) */}
            {!esCliente && solicitudesReprogramacion.length > 0 && (
                <section style={{ marginBottom: "24px" }}>
                    <h3 style={{ margin: "0 0 12px", color: "#5C0613" }}>
                        Solicitudes de Reserva a Favor por aprobar ({solicitudesReprogramacion.length})
                    </h3>
                    <div className="tarjetas">
                        {solicitudesReprogramacion.map(r => (
                            <div
                                className="tarjeta"
                                key={r.idReserva}
                                style={{ borderLeft: "4px solid #D97706", background: "#FFFDF5" }}
                            >
                                <h3>{r.instalacion?.nombre}</h3>
                                <p><strong>Cliente:</strong> {r.usuario?.nombre} {r.usuario?.apellido}</p>
                                <p><strong>Fecha propuesta:</strong> {formatearFecha(r.fechaReserva)} ({r.horaInicio?.slice(0, 5)} hs)</p>
                                <p style={{ marginBottom: "14px" }}><strong>Duración:</strong> {r.duracionHoras} hs (ya paga)</p>
                                <div style={{ display: "flex", gap: "8px" }}>
                                    <button
                                        className="boton-editar"
                                        style={{ background: "#15803D", marginRight: 0 }}
                                        onClick={() => responderReprogramacion(r.idReserva, "aprobar")}
                                    >
                                        Aceptar
                                    </button>
                                    <button
                                        className="boton-secundario"
                                        onClick={() => responderReprogramacion(r.idReserva, "rechazar")}
                                    >
                                        Rechazar
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            )}

            {/* RESERVAS A FAVOR DISPONIBLES */}
            {reservasAFavor.length > 0 && (
                <section style={{ marginBottom: "24px" }}>
                    <h3 style={{ margin: "0 0 6px", color: "#5C0613" }}>
                        {esCliente
                            ? `Tenés ${reservasAFavor.length} reserva(s) a favor disponible(s)`
                            : `Reservas a favor disponibles (${reservasAFavor.length})`}
                    </h3>
                    <p style={{ margin: "0 0 12px", color: "#64748B", fontSize: "14px" }}>
                        Cada reserva a favor tiene un plazo de 3 meses para reprogramar otro día y horario; pasado ese plazo se pierde.
                    </p>
                    <div className="tarjetas">
                        {reservasAFavor.map(rf => {
                            const diasRestantes = calcularDiasRestantes(rf.fechaVencimiento)
                            const porVencer = diasRestantes !== null && diasRestantes <= DIAS_AVISO_VENCIMIENTO

                            return (
                                <div
                                    className="tarjeta"
                                    key={rf.idReservaAFavor}
                                    style={{
                                        borderLeft: porVencer ? "4px solid #D97706" : "4px solid #5C0613",
                                        background: porVencer ? "#FFFDF5" : undefined
                                    }}
                                >
                                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "8px", marginBottom: "8px" }}>
                                        <h3 style={{ margin: 0 }}>
                                            {rf.reservaOrigen?.instalacion?.nombre || "Instalación"}
                                        </h3>
                                        {diasRestantes !== null && (
                                            <span
                                                className="estado"
                                                style={{
                                                    background: porVencer ? "#FEF3C7" : "#F1F5F9",
                                                    color: porVencer ? "#B45309" : "#334155",
                                                    fontSize: "12px"
                                                }}
                                            >
                                                {diasRestantes <= 0
                                                    ? "Vence hoy"
                                                    : porVencer
                                                        ? `¡Vence en ${diasRestantes} día(s)!`
                                                        : `Quedan ${diasRestantes} días`}
                                            </span>
                                        )}
                                    </div>

                                    {!esCliente && rf.usuario && (
                                        <p>
                                            <strong>Cliente:</strong> {rf.usuario.nombre} {rf.usuario.apellido}
                                        </p>
                                    )}
                                    <p>
                                        <strong>Duración:</strong> {rf.reservaOrigen?.duracionHoras || "-"} hora(s)
                                    </p>
                                    <p>
                                        <strong>Día reservado original:</strong>{" "}
                                        {formatearFecha(rf.reservaOrigen?.fechaReserva)}
                                        {rf.reservaOrigen?.horaInicio ? ` (${rf.reservaOrigen.horaInicio.slice(0, 5)} hs)` : ""}
                                    </p>
                                    <p>
                                        <strong>Día que se canceló:</strong> {formatearFecha(rf.fechaGeneracion)}
                                    </p>
                                    <p style={{ marginBottom: "14px", color: porVencer ? "#B45309" : undefined }}>
                                        <strong>Vence el:</strong> {formatearFecha(rf.fechaVencimiento)}
                                    </p>

                                    <button
                                        className="boton-principal"
                                        onClick={() => abrirFormularioReservaAFavor(rf)}
                                    >
                                        Usar reserva a favor
                                    </button>
                                </div>
                            )
                        })}
                    </div>
                </section>
            )}

            {/* FORMULARIO NUEVA RESERVA */}
            {mostrarFormulario && (
                <form className="formulario-usuario" onSubmit={crearReserva}>
                    <h3>Nueva reserva</h3>

                    <div className="formulario-grid">
                        <div>
                            <label>Usuario</label>
                            {esCliente ? (
                                <input
                                    type="text"
                                    value={`${usuarioActual?.nombre || ""} ${usuarioActual?.apellido || ""}`}
                                    readOnly
                                />
                            ) : (
                                <select
                                    name="idUsuario"
                                    value={nuevaReserva.idUsuario}
                                    onChange={manejarCambio}
                                    required
                                >
                                    <option value="">Seleccionar usuario</option>
                                    {usuarios.map(u => (
                                        <option key={u.idUsuario} value={u.idUsuario}>
                                            {u.nombre} {u.apellido} ({u.dni})
                                        </option>
                                    ))}
                                </select>
                            )}
                        </div>

                        <div>
                            <label>Instalación</label>
                            <select
                                name="idInstalacion"
                                value={nuevaReserva.idInstalacion}
                                onChange={manejarCambio}
                                required
                            >
                                <option value="">Seleccionar instalación</option>
                                {instalaciones
                                    .filter(i => i.estado === "DISPONIBLE")
                                    .map(i => (
                                        <option key={i.idInstalacion} value={i.idInstalacion}>
                                            {i.nombre} — ${formatearMonto(i.tarifaBase)}/h
                                        </option>
                                    ))}
                            </select>
                            {instalacionSeleccionada && (
                                <small>
                                    Tarifa base: ${formatearMonto(instalacionSeleccionada.tarifaBase)} por hora
                                </small>
                            )}
                        </div>

                        <div>
                            <label>Fecha</label>
                            <input
                                type="date"
                                name="fechaReserva"
                                min={obtenerFechaActual()}
                                value={nuevaReserva.fechaReserva}
                                onChange={manejarCambio}
                                required
                            />
                        </div>

                        <div>
                            <label>Hora de inicio (10:00 a 03:00)</label>
                            <input
                                type="time"
                                name="horaInicio"
                                value={nuevaReserva.horaInicio}
                                onChange={manejarCambio}
                                required
                            />
                        </div>

                        <div>
                            <label>Duración (horas)</label>
                            <input
                                type="number"
                                name="duracionHoras"
                                min="1"
                                max="17"
                                value={nuevaReserva.duracionHoras}
                                onChange={manejarCambio}
                                required
                            />
                        </div>

                        <div>
                            <label>Monto final ($)</label>
                            <input
                                type="number"
                                name="montoTotal"
                                value={nuevaReserva.montoTotal}
                                readOnly
                            />
                        </div>

                        {instalacionSeleccionada && nuevaReserva.idUsuario && (
                            <div className="resumen-descuento">
                                <div>
                                    {calculoActual.descuento > 0 ? (
                                        <>
                                            <strong>Descuento aplicado ({calculoActual.categoria}):</strong>{" "}
                                            {calculoActual.descuento}% por cuota vigente
                                        </>
                                    ) : (
                                        <span>Sin descuento de membresía vigente.</span>
                                    )}
                                </div>
                                <div>
                                    Subtotal: <strong>${formatearMonto(calculoActual.base)}</strong>{" "}
                                    → Total: <strong style={{ color: "#5C0613" }}>${formatearMonto(calculoActual.final)}</strong>
                                </div>
                            </div>
                        )}
                    </div>

                    <div className="formulario-botones">
                        <button type="button" className="boton-secundario" onClick={limpiarFormulario}>
                            Cancelar
                        </button>
                        <button type="submit" className="boton-principal">
                            Crear reserva
                        </button>
                    </div>
                </form>
            )}

            {/* FORMULARIO RESERVA A FAVOR */}
            {mostrarFormularioReservaAFavor && (
                <form className="formulario-usuario" onSubmit={usarReservaAFavor}>
                    <h3>Usar reserva a favor</h3>
                    <p style={{ marginTop: 0, color: "#64748B", fontSize: "14px" }}>
                        Solo puede utilizarse para la misma instalación y duración de la reserva cancelada.
                    </p>

                    <div className="formulario-grid">
                        <div>
                            <label>Instalación (fija)</label>
                            <input
                                type="text"
                                value={reservaAFavorSeleccionada?.reservaOrigen?.instalacion?.nombre || "No disponible"}
                                readOnly
                            />
                        </div>
                        <div>
                            <label>Duración (fija)</label>
                            <input
                                type="text"
                                value={
                                    reservaAFavorSeleccionada?.reservaOrigen?.duracionHoras
                                        ? `${reservaAFavorSeleccionada.reservaOrigen.duracionHoras} hora(s)`
                                        : "No disponible"
                                }
                                readOnly
                            />
                        </div>
                        <div>
                            <label>Nueva fecha</label>
                            <input
                                type="date"
                                name="fechaReserva"
                                min={obtenerFechaActual()}
                                value={nuevaReservaAFavor.fechaReserva}
                                onChange={manejarCambioReservaAFavor}
                                required
                            />
                        </div>
                        <div>
                            <label>Nueva hora de inicio (10:00 a 03:00)</label>
                            <input
                                type="time"
                                name="horaInicio"
                                value={nuevaReservaAFavor.horaInicio}
                                onChange={manejarCambioReservaAFavor}
                                required
                            />
                        </div>
                    </div>

                    <div className="formulario-botones">
                        <button type="button" className="boton-secundario" onClick={cerrarFormularioReservaAFavor}>
                            Cancelar
                        </button>
                        <button type="submit" className="boton-principal">
                            Confirmar reprogramación
                        </button>
                    </div>
                </form>
            )}

            {/* BOTONES DE FILTRO POR ESTADO */}
            <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", marginBottom: "16px" }}>
                <button
                    type="button"
                    className={filtroEstado === "TODAS" ? "boton-principal" : "boton-secundario"}
                    onClick={() => setFiltroEstado("TODAS")}
                >
                    Todas ({reservas.length})
                </button>
                <button
                    type="button"
                    className={filtroEstado === "PENDIENTE" ? "boton-principal" : "boton-secundario"}
                    onClick={() => setFiltroEstado("PENDIENTE")}
                >
                    Pendientes de pago ({totalPendientes})
                </button>
                {totalPorAprobar > 0 && (
                    <button
                        type="button"
                        className={filtroEstado === "PENDIENTE_APROBACION" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstado("PENDIENTE_APROBACION")}
                    >
                        Por aprobar ({totalPorAprobar})
                    </button>
                )}
                <button
                    type="button"
                    className={filtroEstado === "CONFIRMADA" ? "boton-principal" : "boton-secundario"}
                    onClick={() => setFiltroEstado("CONFIRMADA")}
                >
                    Confirmadas ({totalConfirmadas})
                </button>
                <button
                    type="button"
                    className={filtroEstado === "CANCELADA" ? "boton-principal" : "boton-secundario"}
                    onClick={() => setFiltroEstado("CANCELADA")}
                >
                    Canceladas ({totalCanceladas})
                </button>
            </div>

            {/* TABLA */}
            <div className="tabla-contenedor">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Usuario</th>
                        <th>Instalación</th>
                        <th>Fecha</th>
                        <th>Hora</th>
                        <th>Duración</th>
                        <th>Monto</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    {reservasFiltradas.length === 0 ? (
                        <tr>
                            <td colSpan="9" className="tabla-vacia">
                                No hay reservas en este estado.
                            </td>
                        </tr>
                    ) : (
                        reservasFiltradas.map(r => (
                            <tr key={r.idReserva}>
                                <td>#{r.idReserva}</td>
                                <td><strong>{r.usuario?.nombre} {r.usuario?.apellido}</strong></td>
                                <td>{r.instalacion?.nombre}</td>
                                <td>{formatearFecha(r.fechaReserva)}</td>
                                <td>{r.horaInicio?.slice(0, 5)}</td>
                                <td>{r.duracionHoras} hs</td>
                                <td><strong>${formatearMonto(r.montoTotal)}</strong></td>
                                <td>
                                    <span className={claseEstado(r.estado)}>
                                        {textoEstado(r.estado)}
                                    </span>
                                </td>
                                <td>
                                    {puedeCancelarReserva(r) ? (
                                        <button
                                            className="boton-eliminar"
                                            onClick={() => cancelarReserva(r.idReserva)}
                                        >
                                            Cancelar
                                        </button>
                                    ) : (
                                        <span style={{ color: "#94A3B8", fontSize: "13px" }}>—</span>
                                    )}
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </section>
    )
}

export default Reservas