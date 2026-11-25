import { useEffect, useState } from "react"

function Usuarios() {
    const [usuarios, setUsuarios] = useState([])
    const [membresias, setMembresias] = useState([])
    const [busqueda, setBusqueda] = useState("")
    const [filtroMembresia, setFiltroMembresia] = useState("TODOS") // TODOS | CON_MEMBRESIA | SIN_MEMBRESIA
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState("")
    const [mensaje, setMensaje] = useState("")

    const [modoEdicion, setModoEdicion] = useState(false)
    const [usuarioEditando, setUsuarioEditando] = useState(null)
    const [mostrarFormulario, setMostrarFormulario] = useState(false)

    const estadoInicial = {
        nombre: "",
        apellido: "",
        dni: "",
        fechaNacimiento: "",
        domicilio: "",
        telefono: "",
        email: "",
        nombreUsuario: "",
        password: "",
        pertenencia: "NINGUNO",
        legajo: "",
        estado: "ACTIVO"
    }

    const [nuevoUsuario, setNuevoUsuario] = useState(estadoInicial)

    const obtenerFechaActual = () => {
        const hoy = new Date()
        const año = hoy.getFullYear()
        const mes = String(hoy.getMonth() + 1).padStart(2, "0")
        const dia = String(hoy.getDate()).padStart(2, "0")
        return `${año}-${mes}-${dia}`
    }

    const fechaMaximaNacimiento = obtenerFechaActual()

    const obtenerUsuarios = async () => {
        try {
            const token = sessionStorage.getItem("token")
            const headers = { Authorization: `Bearer ${token}` }

            const [resUsuarios, resMembresias] = await Promise.all([
                fetch("http://localhost:8080/api/usuarios", { headers }),
                fetch("http://localhost:8080/api/membresias", { headers }).catch(() => null)
            ])

            if (!resUsuarios.ok) throw new Error("No se pudieron obtener los usuarios")

            setUsuarios(await resUsuarios.json())
            setMembresias(resMembresias?.ok ? await resMembresias.json() : [])
        } catch (err) {
            console.error(err)
            setError(err.message)
        } finally {
            setCargando(false)
        }
    }

    useEffect(() => {
        obtenerUsuarios()
    }, [])

    // Coincide exactamente con la vigencia real de Membresias.jsx
    const tieneMembresiaVigente = (idUsuario) => {
        const hoy = new Date()
        hoy.setHours(0, 0, 0, 0)

        return membresias.some(m => {
            if (m.socio?.idUsuario !== idUsuario) return false
            if (m.estado !== "VIGENTE") return false
            if (m.fechaVenc) {
                const venc = new Date(m.fechaVenc + "T00:00:00")
                if (venc < hoy) return false
            }
            return true
        })
    }

    const manejarCambio = (e) => {
        const { name, value } = e.target
        setNuevoUsuario({ ...nuevoUsuario, [name]: value })
    }

    const editarUsuario = (usuario) => {
        setError("")
        setMensaje("")
        setUsuarioEditando(usuario)
        setNuevoUsuario({
            nombre: usuario.nombre || "",
            apellido: usuario.apellido || "",
            dni: usuario.dni || "",
            fechaNacimiento: usuario.fechaNacimiento || "",
            domicilio: usuario.domicilio || "",
            telefono: usuario.telefono || "",
            email: usuario.email || "",
            nombreUsuario: usuario.nombreUsuario || "",
            password: "",
            pertenencia: usuario.pertenencia || "NINGUNO",
            legajo: usuario.legajo || "",
            estado: usuario.estado || "ACTIVO"
        })
        setModoEdicion(true)
        setMostrarFormulario(true)
    }

    const guardarUsuario = async (e) => {
        e.preventDefault()
        setError("")
        setMensaje("")

        if (nuevoUsuario.fechaNacimiento && nuevoUsuario.fechaNacimiento > fechaMaximaNacimiento) {
            setError("La fecha de nacimiento no puede ser posterior a la fecha actual.")
            return
        }

        try {
            const token = sessionStorage.getItem("token")
            const url = modoEdicion
                ? `http://localhost:8080/api/usuarios/${usuarioEditando.idUsuario}`
                : "http://localhost:8080/api/usuarios"

            const respuesta = await fetch(url, {
                method: modoEdicion ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(nuevoUsuario)
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo guardar el usuario")
            }

            cancelarFormulario()
            setMensaje(modoEdicion ? "Usuario actualizado correctamente." : "Usuario creado correctamente.")
            await obtenerUsuarios()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const cancelarFormulario = () => {
        setMostrarFormulario(false)
        setModoEdicion(false)
        setUsuarioEditando(null)
        setNuevoUsuario(estadoInicial)
    }

    const nuevoUsuarioFormulario = () => {
        setError("")
        setMensaje("")
        setModoEdicion(false)
        setUsuarioEditando(null)
        setNuevoUsuario(estadoInicial)
        setMostrarFormulario(true)
    }

    const eliminarUsuario = async (id) => {
        if (!window.confirm("¿Estás seguro de que querés eliminar este usuario?")) return
        setError("")
        setMensaje("")

        try {
            const token = sessionStorage.getItem("token")
            const respuesta = await fetch(`http://localhost:8080/api/usuarios/${id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` }
            })
            if (!respuesta.ok) throw new Error("No se pudo eliminar el usuario")
            setMensaje("Usuario eliminado correctamente.")
            await obtenerUsuarios()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const totalConMembresia = usuarios.filter(u => tieneMembresiaVigente(u.idUsuario)).length
    const totalSinMembresia = usuarios.length - totalConMembresia

    const usuariosFiltrados = usuarios.filter(u => {
        const esSocioVigente = tieneMembresiaVigente(u.idUsuario)
        if (filtroMembresia === "CON_MEMBRESIA" && !esSocioVigente) return false
        if (filtroMembresia === "SIN_MEMBRESIA" && esSocioVigente) return false

        const texto = busqueda.trim().toLowerCase()
        if (!texto) return true
        return (
            `${u.nombre} ${u.apellido}`.toLowerCase().includes(texto) ||
            (u.nombreUsuario || "").toLowerCase().includes(texto) ||
            (u.dni || "").toLowerCase().includes(texto) ||
            (u.email || "").toLowerCase().includes(texto)
        )
    })

    if (cargando) return <p>Cargando usuarios...</p>

    return (
        <section className="usuarios">
            <div className="seccion-titulo">
                <div>
                    <h2>Usuarios</h2>
                    <p>Control de cuentas registradas y acceso al sistema (Activo / Inactivo).</p>
                </div>

                <button className="boton-principal" onClick={nuevoUsuarioFormulario}>
                    + Nuevo usuario
                </button>
            </div>

            {mensaje && <div className="mensaje-exito">{mensaje}</div>}
            {error && <div className="mensaje-error">{error}</div>}

            {mostrarFormulario && (
                <form className="formulario-usuario" onSubmit={guardarUsuario}>
                    <h3>{modoEdicion ? "Editar usuario" : "Nuevo usuario"}</h3>

                    <div className="formulario-grid">
                        <div>
                            <label>Nombre</label>
                            <input type="text" name="nombre" value={nuevoUsuario.nombre} onChange={manejarCambio} required />
                        </div>
                        <div>
                            <label>Apellido</label>
                            <input type="text" name="apellido" value={nuevoUsuario.apellido} onChange={manejarCambio} required />
                        </div>
                        <div>
                            <label>DNI</label>
                            <input type="text" name="dni" value={nuevoUsuario.dni} onChange={manejarCambio} required />
                        </div>
                        <div>
                            <label>Fecha de nacimiento</label>
                            <input
                                type="date"
                                name="fechaNacimiento"
                                value={nuevoUsuario.fechaNacimiento}
                                onChange={manejarCambio}
                                max={fechaMaximaNacimiento}
                            />
                        </div>
                        <div>
                            <label>Domicilio</label>
                            <input type="text" name="domicilio" value={nuevoUsuario.domicilio} onChange={manejarCambio} />
                        </div>
                        <div>
                            <label>Teléfono</label>
                            <input type="text" name="telefono" value={nuevoUsuario.telefono} onChange={manejarCambio} />
                        </div>
                        <div>
                            <label>Email</label>
                            <input type="email" name="email" value={nuevoUsuario.email} onChange={manejarCambio} required />
                        </div>
                        <div>
                            <label>Nombre de usuario</label>
                            <input type="text" name="nombreUsuario" value={nuevoUsuario.nombreUsuario} onChange={manejarCambio} required />
                        </div>
                        <div>
                            <label>Contraseña {modoEdicion && "(opcional)"}</label>
                            <input
                                type="password"
                                name="password"
                                value={nuevoUsuario.password}
                                onChange={manejarCambio}
                                required={!modoEdicion}
                                placeholder={modoEdicion ? "Dejar vacío para mantenerla" : "Ingrese una contraseña"}
                            />
                        </div>
                        <div>
                            <label>Pertenencia</label>
                            <select name="pertenencia" value={nuevoUsuario.pertenencia} onChange={manejarCambio}>
                                <option value="NINGUNO">Ninguno</option>
                                <option value="DOCENTE">Docente</option>
                                <option value="ALUMNO">Alumno</option>
                                <option value="ADMINISTRATIVO">Administrativo</option>
                            </select>
                        </div>
                        <div>
                            <label>Legajo</label>
                            <input type="text" name="legajo" value={nuevoUsuario.legajo} onChange={manejarCambio} />
                        </div>
                        <div>
                            <label>Estado de acceso</label>
                            <select name="estado" value={nuevoUsuario.estado} onChange={manejarCambio}>
                                <option value="ACTIVO">ACTIVO (Con acceso)</option>
                                <option value="INACTIVO">INACTIVO (Sin acceso)</option>
                            </select>
                        </div>
                    </div>

                    <div className="formulario-botones">
                        <button type="button" className="boton-secundario" onClick={cancelarFormulario}>
                            Cancelar
                        </button>
                        <button type="submit" className="boton-principal">
                            {modoEdicion ? "Guardar cambios" : "Guardar usuario"}
                        </button>
                    </div>
                </form>
            )}

            {/* BUSCADOR + FILTROS DE MEMBRESÍA */}
            <div
                style={{
                    display: "flex",
                    flexWrap: "wrap",
                    justifyContent: "space-between",
                    alignItems: "center",
                    gap: "12px",
                    marginBottom: "16px"
                }}
            >
                <input
                    type="text"
                    placeholder="Buscar por nombre, apellido, DNI, usuario o email..."
                    value={busqueda}
                    onChange={e => setBusqueda(e.target.value)}
                    style={{
                        flex: "1 1 280px",
                        maxWidth: "420px",
                        padding: "9px 12px",
                        border: "1px solid #CBD5E1",
                        borderRadius: "7px",
                        fontSize: "14px"
                    }}
                />

                <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
                    <button
                        type="button"
                        className={filtroMembresia === "TODOS" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroMembresia("TODOS")}
                    >
                        Todos ({usuarios.length})
                    </button>
                    <button
                        type="button"
                        className={filtroMembresia === "CON_MEMBRESIA" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroMembresia("CON_MEMBRESIA")}
                    >
                        Con membresía vigente ({totalConMembresia})
                    </button>
                    <button
                        type="button"
                        className={filtroMembresia === "SIN_MEMBRESIA" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroMembresia("SIN_MEMBRESIA")}
                    >
                        Sin membresía vigente ({totalSinMembresia})
                    </button>
                </div>
            </div>

            <div className="tabla-contenedor">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre y Apellido</th>
                        <th>DNI</th>
                        <th>Usuario</th>
                        <th>Email</th>
                        <th>Pertenencia</th>
                        <th>Acceso</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    {usuariosFiltrados.length === 0 ? (
                        <tr>
                            <td colSpan="8" className="tabla-vacia">
                                No se encontraron usuarios con los filtros seleccionados.
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
                                <td>{u.pertenencia}</td>
                                <td>
                                    <span className={`estado ${u.estado === "ACTIVO" ? "estado-activo" : "estado-inactivo"}`}>
                                        {u.estado}
                                    </span>
                                </td>
                                <td>
                                    <button className="boton-editar" onClick={() => editarUsuario(u)}>
                                        Editar
                                    </button>
                                    <button className="boton-eliminar" onClick={() => eliminarUsuario(u.idUsuario)}>
                                        Eliminar
                                    </button>
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

export default Usuarios