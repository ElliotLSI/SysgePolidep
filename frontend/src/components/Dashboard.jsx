import { useState, useEffect } from "react"
import Sidebar from "./Sidebar"
import Usuarios from "./Usuarios"
import Instalaciones from "./Instalaciones"
import Membresias from "./Membresias"
import Reservas from "./Reservas"
import Pagos from "./Pagos"
import Reportes from "./Reportes"

function Dashboard() {
    const [usuario, setUsuario] = useState(
        JSON.parse(sessionStorage.getItem("usuario"))
    )

    const [seccionActual, setSeccionActual] = useState("inicio")

    const [notificacionesAdmin, setNotificacionesAdmin] = useState({
        pagosPendientes: 0,
        reprogramacionesPendientes: 0,
        membresiasVencidas: 0
    })

    const [notificacionesCliente, setNotificacionesCliente] = useState({
        reservasPendientesPago: 0,
        reservasAFavorPorVencer: 0,
        proximaReserva: null
    })

    const permisos = {
        inicio: ["ADMINISTRADOR", "EMPLEADO", "SOCIO", "USUARIO"],
        usuarios: ["ADMINISTRADOR"],
        membresias: ["ADMINISTRADOR", "EMPLEADO", "USUARIO"],
        instalaciones: ["ADMINISTRADOR", "EMPLEADO"],
        reservas: ["ADMINISTRADOR", "EMPLEADO", "SOCIO", "USUARIO"],
        pagos: ["ADMINISTRADOR", "SOCIO", "USUARIO"],
        reportes: ["ADMINISTRADOR"]
    }

    const roles = usuario?.roles ?? []
    const esAdministrador = roles.includes("ADMINISTRADOR")
    const esCliente = roles.includes("SOCIO") || roles.includes("USUARIO")

    const DIAS_AVISO_VENCIMIENTO = 15

    const seccionPermitida = (seccion) =>
        permisos[seccion]?.some(rol => roles.includes(rol)) ?? false

    const formatearFecha = (fechaStr) => {
        if (!fechaStr) return "-"
        const [anio, mes, dia] = fechaStr.split("-")
        return `${dia}/${mes}/${anio}`
    }

    const obtenerFechaActualStr = () => {
        const ahora = new Date()
        const año = ahora.getFullYear()
        const mes = String(ahora.getMonth() + 1).padStart(2, "0")
        const dia = String(ahora.getDate()).padStart(2, "0")
        return `${año}-${mes}-${dia}`
    }

    const calcularDiasRestantes = (fechaVencStr) => {
        if (!fechaVencStr) return null
        const hoy = new Date()
        hoy.setHours(0, 0, 0, 0)
        const venc = new Date(fechaVencStr + "T00:00:00")
        return Math.ceil((venc - hoy) / (1000 * 60 * 60 * 24))
    }

    const cerrarSesion = () => {
        sessionStorage.removeItem("usuario")
        sessionStorage.removeItem("token")
        setUsuario(null)
    }

    useEffect(() => {
        if (!usuario) {
            window.location.reload()
        }
    }, [usuario])

    useEffect(() => {
        if (!seccionPermitida(seccionActual)) {
            setSeccionActual("inicio")
        }
    }, [seccionActual, usuario])

    // Cargar notificaciones en Inicio (Admin y Clientes)
    useEffect(() => {
        if (!usuario || seccionActual !== "inicio") return

        const token = sessionStorage.getItem("token")
        const headers = { Authorization: `Bearer ${token}` }

        const cargarNotificacionesAdmin = async () => {
            try {
                const [resPagos, resReservas, resMembresias] = await Promise.all([
                    fetch("http://localhost:8080/api/pagos", { headers }).catch(() => null),
                    fetch("http://localhost:8080/api/reservas", { headers }).catch(() => null),
                    fetch("http://localhost:8080/api/membresias", { headers }).catch(() => null)
                ])

                const pagos = resPagos?.ok ? await resPagos.json() : []
                const reservas = resReservas?.ok ? await resReservas.json() : []
                const membresias = resMembresias?.ok ? await resMembresias.json() : []

                const hoy = new Date()
                hoy.setHours(0, 0, 0, 0)

                const pagosPendientes = pagos.filter(
                    p => p.estado === "PENDIENTE"
                ).length

                const reprogramacionesPendientes = reservas.filter(
                    r => r.estado === "PENDIENTE_APROBACION"
                ).length

                const membresiasVencidas = membresias.filter(m => {
                    if (m.estado === "VENCIDA") return true
                    if (m.estado === "VIGENTE" && m.fechaVenc) {
                        const venc = new Date(m.fechaVenc + "T00:00:00")
                        return venc < hoy
                    }
                    return false
                }).length

                setNotificacionesAdmin({
                    pagosPendientes,
                    reprogramacionesPendientes,
                    membresiasVencidas
                })
            } catch (err) {
                console.error("Error al cargar notificaciones de admin:", err)
            }
        }

        const cargarNotificacionesCliente = async () => {
            try {
                const [resReservas, resFavor] = await Promise.all([
                    fetch("http://localhost:8080/api/reservas", { headers }).catch(() => null),
                    usuario?.idUsuario
                        ? fetch(
                            `http://localhost:8080/api/reservas-a-favor/usuario/${usuario.idUsuario}/disponibles`,
                            { headers }
                        ).catch(() => null)
                        : Promise.resolve(null)
                ])

                const reservas = resReservas?.ok ? await resReservas.json() : []
                const reservasAFavor = resFavor?.ok ? await resFavor.json() : []

                const misReservas = reservas.filter(
                    r => !r.usuario?.idUsuario || r.usuario.idUsuario === usuario.idUsuario
                )

                const reservasPendientesPago = misReservas.filter(
                    r => r.estado === "PENDIENTE"
                ).length

                const reservasAFavorPorVencer = reservasAFavor.filter(rf => {
                    const dias = calcularDiasRestantes(rf.fechaVencimiento)
                    return dias !== null && dias >= 0 && dias <= DIAS_AVISO_VENCIMIENTO
                }).length

                const hoyStr = obtenerFechaActualStr()
                const confirmadasFuturas = misReservas
                    .filter(r => r.estado === "CONFIRMADA" && r.fechaReserva && r.fechaReserva >= hoyStr)
                    .sort((a, b) => {
                        const fechaHoraA = `${a.fechaReserva}T${a.horaInicio || "00:00"}`
                        const fechaHoraB = `${b.fechaReserva}T${b.horaInicio || "00:00"}`
                        return fechaHoraA.localeCompare(fechaHoraB)
                    })

                setNotificacionesCliente({
                    reservasPendientesPago,
                    reservasAFavorPorVencer,
                    proximaReserva: confirmadasFuturas[0] || null
                })
            } catch (err) {
                console.error("Error al cargar notificaciones de cliente:", err)
            }
        }

        if (esAdministrador) {
            cargarNotificacionesAdmin()
        } else if (esCliente) {
            cargarNotificacionesCliente()
        }
    }, [seccionActual, usuario, esAdministrador, esCliente])

    if (!usuario) {
        return null
    }

    const titulos = {
        inicio: "Inicio",
        usuarios: "Usuarios",
        membresias: "Membresías",
        instalaciones: "Instalaciones",
        reservas: "Reservas",
        pagos: "Pagos",
        reportes: "Reportes"
    }

    const alertasAdmin = [
        {
            id: "pagos",
            titulo: "Solicitudes de pago",
            cantidad: notificacionesAdmin.pagosPendientes,
            descripcion: "Pagos pendientes de aprobación.",
            destino: "pagos"
        },
        {
            id: "reservas",
            titulo: "Reservas a favor",
            cantidad: notificacionesAdmin.reprogramacionesPendientes,
            descripcion: "Reprogramaciones esperando tu aceptación.",
            destino: "reservas"
        },
        {
            id: "membresias",
            titulo: "Membresías vencidas",
            cantidad: notificacionesAdmin.membresiasVencidas,
            descripcion: "Cuotas vencidas que requieren atención.",
            destino: "membresias"
        }
    ]

    const totalAlertasAdmin = alertasAdmin.reduce((acc, a) => acc + a.cantidad, 0)

    const hayAvisosCliente =
        notificacionesCliente.reservasPendientesPago > 0 ||
        notificacionesCliente.reservasAFavorPorVencer > 0 ||
        Boolean(notificacionesCliente.proximaReserva)

    const renderizarSeccion = () => {
        if (!seccionPermitida(seccionActual)) {
            return <p>No tenés permisos para acceder a esta sección.</p>
        }

        switch (seccionActual) {
            case "usuarios":
                return <Usuarios />

            case "membresias":
                return <Membresias />

            case "instalaciones":
                return <Instalaciones />

            case "reservas":
                return <Reservas />

            case "pagos":
                return <Pagos />

            case "reportes":
                return <Reportes />

            case "inicio":
            default:
                return (
                    <section>
                        <div className="bienvenida">
                            <h2>¡Bienvenido, {usuario.nombre}!</h2>
                            <p>
                                Desde este panel podés acceder a las
                                funcionalidades disponibles según tu rol.
                            </p>
                        </div>

                        {/* PANEL DE NOTIFICACIONES (SOLO ADMINISTRADOR) */}
                        {esAdministrador && (
                            <section style={{ marginBottom: "24px" }}>
                                <h3 style={{ margin: "0 0 12px", color: "#5C0613" }}>
                                    Pendientes de revisión ({totalAlertasAdmin})
                                </h3>

                                {totalAlertasAdmin === 0 ? (
                                    <div
                                        style={{
                                            background: "#F0FDF4",
                                            border: "1px solid #86EFAC",
                                            borderLeft: "5px solid #16A34A",
                                            borderRadius: "8px",
                                            padding: "14px 16px",
                                            color: "#166534",
                                            fontSize: "14px"
                                        }}
                                    >
                                        <strong>Todo al día:</strong> no tenés solicitudes de pago, reprogramaciones ni membresías vencidas pendientes.
                                    </div>
                                ) : (
                                    <div className="tarjetas">
                                        {alertasAdmin
                                            .filter(alerta => alerta.cantidad > 0)
                                            .map(alerta => (
                                                <div
                                                    key={alerta.id}
                                                    className="tarjeta"
                                                    onClick={() => setSeccionActual(alerta.destino)}
                                                    role="button"
                                                    tabIndex={0}
                                                    onKeyDown={e => {
                                                        if (e.key === "Enter") {
                                                            setSeccionActual(alerta.destino)
                                                        }
                                                    }}
                                                    style={{
                                                        borderLeft: "4px solid #D97706",
                                                        background: "#FFFDF5",
                                                        cursor: "pointer"
                                                    }}
                                                >
                                                    <div
                                                        style={{
                                                            display: "flex",
                                                            justifyContent: "space-between",
                                                            alignItems: "center",
                                                            gap: "8px",
                                                            marginBottom: "6px"
                                                        }}
                                                    >
                                                        <h3 style={{ margin: 0 }}>{alerta.titulo}</h3>
                                                        <span
                                                            className="estado"
                                                            style={{
                                                                background: "#FEF3C7",
                                                                color: "#B45309",
                                                                fontWeight: "bold"
                                                            }}
                                                        >
                                                            {alerta.cantidad}
                                                        </span>
                                                    </div>
                                                    <p style={{ marginBottom: "10px" }}>
                                                        {alerta.descripcion}
                                                    </p>
                                                    <small style={{ color: "#5C0613", fontWeight: "bold" }}>
                                                        Ir a {titulos[alerta.destino]} →
                                                    </small>
                                                </div>
                                            ))}
                                    </div>
                                )}
                            </section>
                        )}

                        {/* PANEL DE NOTIFICACIONES (SOCIOS Y USUARIOS) */}
                        {!esAdministrador && esCliente && hayAvisosCliente && (
                            <section style={{ marginBottom: "24px" }}>
                                <h3 style={{ margin: "0 0 12px", color: "#5C0613" }}>
                                    Tus avisos
                                </h3>
                                <div className="tarjetas">
                                    {notificacionesCliente.proximaReserva && (
                                        <div
                                            className="tarjeta"
                                            onClick={() => setSeccionActual("reservas")}
                                            role="button"
                                            tabIndex={0}
                                            onKeyDown={e => {
                                                if (e.key === "Enter") setSeccionActual("reservas")
                                            }}
                                            style={{
                                                borderLeft: "4px solid #16A34A",
                                                background: "#F0FDF4",
                                                cursor: "pointer"
                                            }}
                                        >
                                            <h3 style={{ margin: "0 0 6px" }}>Próxima reserva</h3>
                                            <p style={{ marginBottom: "10px" }}>
                                                Tu reserva está para el día{" "}
                                                <strong>
                                                    {formatearFecha(notificacionesCliente.proximaReserva.fechaReserva)}
                                                </strong>{" "}
                                                a las{" "}
                                                <strong>
                                                    {notificacionesCliente.proximaReserva.horaInicio?.slice(0, 5)} hs
                                                </strong>
                                                {notificacionesCliente.proximaReserva.instalacion?.nombre
                                                    ? ` (${notificacionesCliente.proximaReserva.instalacion.nombre})`
                                                    : ""}.
                                            </p>
                                            <small style={{ color: "#166534", fontWeight: "bold" }}>
                                                Ver mis reservas →
                                            </small>
                                        </div>
                                    )}

                                    {notificacionesCliente.reservasPendientesPago > 0 && (
                                        <div
                                            className="tarjeta"
                                            onClick={() => setSeccionActual("pagos")}
                                            role="button"
                                            tabIndex={0}
                                            onKeyDown={e => {
                                                if (e.key === "Enter") setSeccionActual("pagos")
                                            }}
                                            style={{
                                                borderLeft: "4px solid #D97706",
                                                background: "#FFFDF5",
                                                cursor: "pointer"
                                            }}
                                        >
                                            <h3 style={{ margin: "0 0 6px" }}>Pago pendiente</h3>
                                            <p style={{ marginBottom: "10px" }}>
                                                {notificacionesCliente.reservasPendientesPago === 1
                                                    ? "Tenés una reserva pendiente de pago."
                                                    : `Tenés ${notificacionesCliente.reservasPendientesPago} reservas pendientes de pago.`}
                                            </p>
                                            <small style={{ color: "#5C0613", fontWeight: "bold" }}>
                                                Ir a Pagos →
                                            </small>
                                        </div>
                                    )}

                                    {notificacionesCliente.reservasAFavorPorVencer > 0 && (
                                        <div
                                            className="tarjeta"
                                            onClick={() => setSeccionActual("reservas")}
                                            role="button"
                                            tabIndex={0}
                                            onKeyDown={e => {
                                                if (e.key === "Enter") setSeccionActual("reservas")
                                            }}
                                            style={{
                                                borderLeft: "4px solid #DC2626",
                                                background: "#FEF2F2",
                                                cursor: "pointer"
                                            }}
                                        >
                                            <h3 style={{ margin: "0 0 6px" }}>Reserva a favor por vencer</h3>
                                            <p style={{ marginBottom: "10px" }}>
                                                {notificacionesCliente.reservasAFavorPorVencer === 1
                                                    ? "Tu reserva a favor está próxima a vencer. Reprogramala antes de perderla."
                                                    : `Tenés ${notificacionesCliente.reservasAFavorPorVencer} reservas a favor próximas a vencer.`}
                                            </p>
                                            <small style={{ color: "#B91C1C", fontWeight: "bold" }}>
                                                Reprogramar en Reservas →
                                            </small>
                                        </div>
                                    )}
                                </div>
                            </section>
                        )}

                        <section className="tarjetas">
                            {Object.entries(permisos)
                                .filter(([id]) =>
                                    id !== "inicio" && seccionPermitida(id)
                                )
                                .map(([id]) => (
                                    <div
                                        className="tarjeta"
                                        key={id}
                                        onClick={() => setSeccionActual(id)}
                                        role="button"
                                        tabIndex={0}
                                        onKeyDown={e => {
                                            if (e.key === "Enter") {
                                                setSeccionActual(id)
                                            }
                                        }}
                                    >
                                        <h3>{titulos[id]}</h3>
                                        <p>
                                            Acceder a {titulos[id].toLowerCase()}.
                                        </p>
                                    </div>
                                ))}
                        </section>
                    </section>
                )
        }
    }

    return (
        <div className="dashboard">
            <Sidebar
                cerrarSesion={cerrarSesion}
                seccionActual={seccionActual}
                setSeccionActual={setSeccionActual}
                roles={roles}
            />

            <main className="contenido">
                <header className="encabezado">
                    <div>
                        <h1>{titulos[seccionActual]}</h1>
                        <p>Panel de SysGe PoliDep</p>
                    </div>

                    <div className="usuario-header">
                        <span>{usuario.nombre}</span>
                        <small>{roles.join(", ")}</small>
                    </div>
                </header>

                {renderizarSeccion()}
            </main>
        </div>
    )
}

export default Dashboard