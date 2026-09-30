function Sidebar({
  cerrarSesion,
  seccionActual,
  setSeccionActual
}) {

  return (
    <aside className="sidebar">

      <div className="sidebar-logo">

        <h2>
          SysGe
        </h2>

        <span>
          PoliDep
        </span>

      </div>


      <nav>

        {/* INICIO */}

        <button
          className={`menu-item ${
            seccionActual === "inicio" ? "activo" : ""
          }`}
          onClick={() => setSeccionActual("inicio")}
        >
          Inicio
        </button>


        {/* USUARIOS */}

        <button
          className={`menu-item ${
            seccionActual === "usuarios" ? "activo" : ""
          }`}
          onClick={() => setSeccionActual("usuarios")}
        >
          Usuarios
        </button>


        {/* MEMBRESÍAS */}

        <button
          className={`menu-item ${
            seccionActual === "membresias" ? "activo" : ""
          }`}
          onClick={() => setSeccionActual("membresias")}
        >
          Membresías
        </button>


        {/* INSTALACIONES */}

        <button
          className={`menu-item ${
            seccionActual === "instalaciones" ? "activo" : ""
          }`}
          onClick={() => setSeccionActual("instalaciones")}
        >
          Instalaciones
        </button>


        {/* RESERVAS */}

        <button
          className={`menu-item ${
            seccionActual === "reservas" ? "activo" : ""
          }`}
          onClick={() => setSeccionActual("reservas")}
        >
          Reservas
        </button>


        {/* PAGOS */}

        <button
          className={`menu-item ${
            seccionActual === "pagos" ? "activo" : ""
          }`}
          onClick={() => setSeccionActual("pagos")}
        >
          Pagos
        </button>

      </nav>


      {/* CERRAR SESIÓN */}

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