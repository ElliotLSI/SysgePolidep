import logo from "../assets/logo-sgp.png"

function Sidebar({
                     cerrarSesion,
                     seccionActual,
                     setSeccionActual,
                     roles
                 }) {

    const menus = [
        {
            id: "inicio",
            nombre: "Inicio",
            roles: [
                "ADMINISTRADOR",
                "EMPLEADO",
                "SOCIO",
                "USUARIO"
            ]
        },
        {
            id: "usuarios",
            nombre: "Usuarios",
            roles: [
                "ADMINISTRADOR"
            ]
        },
        {
            id: "membresias",
            nombre: "Membresías",
            roles: [
                "ADMINISTRADOR",
                "EMPLEADO"
            ]
        },
        {
            id: "instalaciones",
            nombre: "Instalaciones",
            roles: [
                "ADMINISTRADOR",
                "EMPLEADO"
            ]
        },
        {
            id: "reservas",
            nombre: "Reservas",
            roles: [
                "ADMINISTRADOR",
                "EMPLEADO",
                "SOCIO"
            ]
        },
        {
            id: "pagos",
            nombre: "Pagos",
            roles: [
                "ADMINISTRADOR",
                "SOCIO"
            ]
        }
    ]

    const menusPermitidos = menus.filter(menu =>
        menu.roles.some(rol =>
            roles?.includes(rol)
        )
    )

    return (
        <aside className="sidebar">

            <div className="sidebar-logo">
                <img
                    src={logo}
                    alt="SysGe PoliDep"
                    className="logo-sgp"
                />
            </div>

            <nav>
                {menusPermitidos.map(menu => (
                    <button
                        key={menu.id}
                        className={`menu-item ${
                            seccionActual === menu.id
                                ? "activo"
                                : ""
                        }`}
                        onClick={() =>
                            setSeccionActual(menu.id)
                        }
                    >
                        {menu.nombre}
                    </button>
                ))}
            </nav>

            <button
                className="cerrar-sesion"
                onClick={cerrarSesion}
            >
                Cerrar sesión
            </button>

        </aside>
    )
}

export default Sidebar