import { useEffect, useState } from "react"

function Usuarios() {

    const [usuarios, setUsuarios] = useState([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState("")

    const [modoEdicion, setModoEdicion] = useState(false)
    const [usuarioEditando, setUsuarioEditando] = useState(null)

    const [mostrarFormulario, setMostrarFormulario] = useState(false)

    const [nuevoUsuario, setNuevoUsuario] = useState({
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
    })

    const obtenerUsuarios = async () => {

        try {

            const token = sessionStorage.getItem("token")

            const respuesta = await fetch(
                "http://localhost:8080/api/usuarios",
                {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`
                    }
                }
            )

            if (!respuesta.ok) {
                throw new Error("No se pudieron obtener los usuarios")
            }

            const datos = await respuesta.json()

            setUsuarios(datos)

        } catch (error) {

            console.error("Error al obtener usuarios:", error)
            setError(error.message)

        } finally {

            setCargando(false)

        }
    }

    useEffect(() => {
        obtenerUsuarios()
    }, [])

    const manejarCambio = (e) => {

        const { name, value } = e.target

        setNuevoUsuario({
            ...nuevoUsuario,
            [name]: value
        })
    }

    const editarUsuario = (usuario) => {

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

        try {

            const token = sessionStorage.getItem("token")

            let respuesta

            if (modoEdicion) {

                respuesta = await fetch(
                    `http://localhost:8080/api/usuarios/${usuarioEditando.idUsuario}`,
                    {
                        method: "PUT",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${token}`
                        },
                        body: JSON.stringify(nuevoUsuario)
                    }
                )

            } else {

                respuesta = await fetch(
                    "http://localhost:8080/api/usuarios",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${token}`
                        },
                        body: JSON.stringify(nuevoUsuario)
                    }
                )
            }

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje || "No se pudo guardar el usuario"
                )
            }

            const datos = await respuesta.json()

            console.log("Usuario guardado:", datos)

            setMostrarFormulario(false)
            setModoEdicion(false)
            setUsuarioEditando(null)

            setNuevoUsuario({
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
            })

            await obtenerUsuarios()

            if (modoEdicion) {
                alert("Usuario actualizado correctamente")
            } else {
                alert("Usuario creado correctamente")
            }

        } catch (error) {

            console.error("Error al guardar usuario:", error)

            alert(error.message)

        }
    }

    const cancelarFormulario = () => {

        setMostrarFormulario(false)
        setModoEdicion(false)
        setUsuarioEditando(null)

        setNuevoUsuario({
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
        })
    }

    const nuevoUsuarioFormulario = () => {

        setModoEdicion(false)
        setUsuarioEditando(null)

        setNuevoUsuario({
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
        })

        setMostrarFormulario(true)
    }

    const eliminarUsuario = async (id) => {

        const confirmar = window.confirm(
            "¿Estás seguro de que querés eliminar este usuario?"
        )

        if (!confirmar) {
            return
        }

        try {

            const token = sessionStorage.getItem("token")

            const respuesta = await fetch(
                `http://localhost:8080/api/usuarios/${id}`,
                {
                    method: "DELETE",
                    headers: {
                        "Authorization": `Bearer ${token}`
                    }
                }
            )

            if (!respuesta.ok) {
                throw new Error("No se pudo eliminar el usuario")
            }

            await obtenerUsuarios()

            alert("Usuario eliminado correctamente")

        } catch (error) {

            console.error("Error al eliminar usuario:", error)

            alert(error.message)

        }
    }

    if (cargando) {
        return <p>Cargando usuarios...</p>
    }

    if (error) {
        return <p className="error-message">{error}</p>
    }

    return (
        <section className="usuarios">

            <div className="seccion-titulo">

                <div>
                    <h2>Usuarios</h2>

                    <p>
                        Usuarios registrados en el sistema.
                    </p>
                </div>

                <button
                    className="boton-principal"
                    onClick={nuevoUsuarioFormulario}
                >
                    Nuevo usuario
                </button>

            </div>

            {mostrarFormulario && (

                <div className="formulario-usuario">

                    <h3>
                        {modoEdicion
                            ? "Editar usuario"
                            : "Nuevo usuario"
                        }
                    </h3>

                    <div className="formulario-grid">

                        <div className="form-group">
                            <label>Nombre</label>

                            <input
                                type="text"
                                name="nombre"
                                value={nuevoUsuario.nombre}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>Apellido</label>

                            <input
                                type="text"
                                name="apellido"
                                value={nuevoUsuario.apellido}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>DNI</label>

                            <input
                                type="text"
                                name="dni"
                                value={nuevoUsuario.dni}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>Fecha de nacimiento</label>

                            <input
                                type="date"
                                name="fechaNacimiento"
                                value={nuevoUsuario.fechaNacimiento}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>Domicilio</label>

                            <input
                                type="text"
                                name="domicilio"
                                value={nuevoUsuario.domicilio}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>Teléfono</label>

                            <input
                                type="text"
                                name="telefono"
                                value={nuevoUsuario.telefono}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>Email</label>

                            <input
                                type="email"
                                name="email"
                                value={nuevoUsuario.email}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>Nombre de usuario</label>

                            <input
                                type="text"
                                name="nombreUsuario"
                                value={nuevoUsuario.nombreUsuario}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>
                                Contraseña
                                {modoEdicion && " (opcional)"}
                            </label>

                            <input
                                type="password"
                                name="password"
                                value={nuevoUsuario.password}
                                onChange={manejarCambio}
                                placeholder={
                                    modoEdicion
                                        ? "Dejar vacío para mantenerla"
                                        : "Ingrese una contraseña"
                                }
                            />
                        </div>

                        <div className="form-group">
                            <label>Pertenencia</label>

                            <select
                                name="pertenencia"
                                value={nuevoUsuario.pertenencia}
                                onChange={manejarCambio}
                            >
                                <option value="NINGUNO">
                                    Ninguno
                                </option>

                                <option value="DOCENTE">
                                    Docente
                                </option>

                                <option value="ALUMNO">
                                    Alumno
                                </option>

                                <option value="ADMINISTRATIVO">
                                    Administrativo
                                </option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Legajo</label>

                            <input
                                type="text"
                                name="legajo"
                                value={nuevoUsuario.legajo}
                                onChange={manejarCambio}
                            />
                        </div>

                        <div className="form-group">
                            <label>Estado</label>

                            <select
                                name="estado"
                                value={nuevoUsuario.estado}
                                onChange={manejarCambio}
                            >
                                <option value="ACTIVO">
                                    Activo
                                </option>

                                <option value="INACTIVO">
                                    Inactivo
                                </option>
                            </select>
                        </div>

                    </div>

                    <div className="formulario-botones">

                        <button
                            type="button"
                            className="boton-secundario"
                            onClick={cancelarFormulario}
                        >
                            Cancelar
                        </button>

                        <button
                            type="button"
                            className="boton-principal"
                            onClick={guardarUsuario}
                        >
                            {modoEdicion
                                ? "Guardar cambios"
                                : "Guardar usuario"
                            }
                        </button>

                    </div>

                </div>

            )}

            <div className="tabla-contenedor">

                <table>

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Apellido</th>
                            <th>Usuario</th>
                            <th>Email</th>
                            <th>Pertenencia</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>

                    </thead>

                    <tbody>

                        {usuarios.map((usuario) => (

                            <tr key={usuario.idUsuario}>

                                <td>
                                    {usuario.idUsuario}
                                </td>

                                <td>
                                    {usuario.nombre}
                                </td>

                                <td>
                                    {usuario.apellido}
                                </td>

                                <td>
                                    {usuario.nombreUsuario}
                                </td>

                                <td>
                                    {usuario.email}
                                </td>

                                <td>
                                    {usuario.pertenencia}
                                </td>

                                <td>
                                    {usuario.estado}
                                </td>

                                <td>

                                    <button
                                        className="boton-editar"
                                        onClick={() => editarUsuario(usuario)}
                                    >
                                        Editar
                                    </button>

                                    <button
                                        className="boton-eliminar"
                                        onClick={() => eliminarUsuario(usuario.idUsuario)}
                                    >
                                        Eliminar
                                    </button>

                                </td>

                            </tr>

                        ))}

                    </tbody>

                </table>

            </div>

        </section>
    )
}

export default Usuarios
