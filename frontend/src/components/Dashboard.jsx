import { useState, useEffect } from "react"
import Sidebar from "./Sidebar"
import Usuarios from "./Usuarios"
import Instalaciones from "./Instalaciones"
import Membresias from "./Membresias"
import Reservas from "./Reservas"
import Pagos from "./Pagos"

function Dashboard() {
    const [usuario, setUsuario] = useState(
        JSON.parse(sessionStorage.getItem("usuario"))
    )

    const [seccionActual, setSeccionActual] = useState("inicio")

    const permisos = {
        inicio: ["ADMINISTRADOR", "EMPLEADO", "SOCIO", "USUARIO"],
        usuarios: ["ADMINISTRADOR"],
        membresias: ["ADMINISTRADOR", "EMPLEADO"],
        instalaciones: ["ADMINISTRADOR", "EMPLEADO"],
        reservas: ["ADMINISTRADOR", "EMPLEADO", "SOCIO", "USUARIO"],
        pagos: ["ADMINISTRADOR", "EMPLEADO", "SOCIO", "USUARIO"]
    }

    const roles = usuario?.roles ?? []

    const seccionPermitida = (seccion) =>
        permisos[seccion]?.some(rol => roles.includes(rol)) ?? false

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

    if (!usuario) {
        return null
    }

    const titulos = {
        inicio: "Inicio",
        usuarios: "Usuarios",
        membresias: "Membresías",
        instalaciones: "Instalaciones",
        reservas: "Reservas",
        pagos: "Pagos"
    }

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