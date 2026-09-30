import { useState } from "react"
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

    const cerrarSesion = () => {

        sessionStorage.removeItem("usuario")
        sessionStorage.removeItem("token")

        setUsuario(null)
    }

    if (!usuario) {
        window.location.reload()
        return null
    }

    return (
        <div className="dashboard">

            <Sidebar
                cerrarSesion={cerrarSesion}
                seccionActual={seccionActual}
                setSeccionActual={setSeccionActual}
            />

            <main className="contenido">

                <header className="encabezado">

                    <div>

                        <h1>
                            {seccionActual === "inicio" && "Inicio"}

                            {seccionActual === "usuarios" && "Usuarios"}

                            {seccionActual === "membresias" && "Membresías"}

                            {seccionActual === "instalaciones" && "Instalaciones"}

                            {seccionActual === "reservas" && "Reservas"}

                            {seccionActual === "pagos" && "Pagos"}
                        </h1>

                        <p>
                            Panel de administración
                        </p>

                    </div>

                    <div className="usuario-header">

                        <span>
                            {usuario.nombre}
                        </span>

                        <small>
                            {usuario.roles.join(", ")}
                        </small>

                    </div>

                </header>


                {/* ========================= */}
                {/* INICIO */}
                {/* ========================= */}

                {seccionActual === "inicio" && (

                    <section>

                        <div className="bienvenida">

                            <h2>
                                ¡Bienvenido, {usuario.nombre}!
                            </h2>

                            <p>
                                Desde este panel podés administrar
                                las operaciones de SysGe PoliDep.
                            </p>

                        </div>


                        <section className="tarjetas">

                            <div className="tarjeta">

                                <h3>
                                    Usuarios
                                </h3>

                                <p>
                                    Administración de usuarios del sistema.
                                </p>

                            </div>


                            <div className="tarjeta">

                                <h3>
                                    Membresías
                                </h3>

                                <p>
                                    Gestión de socios, membresías y categorías.
                                </p>

                            </div>


                            <div className="tarjeta">

                                <h3>
                                    Instalaciones
                                </h3>

                                <p>
                                    Gestión de espacios deportivos.
                                </p>

                            </div>


                            <div className="tarjeta">

                                <h3>
                                    Reservas
                                </h3>

                                <p>
                                    Consulta y gestión de reservas.
                                </p>

                            </div>


                            <div className="tarjeta">

                                <h3>
                                    Pagos
                                </h3>

                                <p>
                                    Consulta de pagos registrados.
                                </p>

                            </div>

                        </section>

                    </section>

                )}


                {/* ========================= */}
                {/* USUARIOS */}
                {/* ========================= */}

                {seccionActual === "usuarios" && (

                    <Usuarios />

                )}


                {/* ========================= */}
                {/* MEMBRESÍAS */}
                {/* ========================= */}

                {seccionActual === "membresias" && (

                    <Membresias />

                )}


                {/* ========================= */}
                {/* INSTALACIONES */}
                {/* ========================= */}

                {seccionActual === "instalaciones" && (

                    <Instalaciones />

                )}


                {/* ========================= */}
                {/* RESERVAS */}
                {/* ========================= */}

                {seccionActual === "reservas" && (
                    <Reservas />
                )}


                {/* ========================= */}
                {/* PAGOS */}
                {/* ========================= */}

                {seccionActual === "pagos" && (
                    <Pagos />
                )}
            </main>

        </div>
    )
}

export default Dashboard