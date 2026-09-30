import { useEffect, useState } from "react"

function Instalaciones() {

    const [instalaciones, setInstalaciones] = useState([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState("")

    const [modoEdicion, setModoEdicion] = useState(false)
    const [instalacionEditando, setInstalacionEditando] = useState(null)

    const [mostrarFormulario, setMostrarFormulario] = useState(false)

    const [nuevaInstalacion, setNuevaInstalacion] = useState({
        nombre: "",
        tipo: "",
        capacidad: "",
        tarifaBase: "",
        estado: "DISPONIBLE"
    })

    const usuario = JSON.parse(
        sessionStorage.getItem("usuario")
    )

    const roles = usuario?.roles ?? []

    const esAdministrador =
        roles.includes("ADMINISTRADOR")


    const obtenerInstalaciones = async () => {

        try {

            const token = sessionStorage.getItem("token")

            const respuesta = await fetch(
                "http://localhost:8080/api/instalaciones",
                {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`
                    }
                }
            )

            if (!respuesta.ok) {
                throw new Error(
                    "No se pudieron obtener las instalaciones"
                )
            }

            const datos = await respuesta.json()

            setInstalaciones(datos)

        } catch (error) {

            console.error(
                "Error al obtener instalaciones:",
                error
            )

            setError(error.message)

        } finally {

            setCargando(false)

        }
    }


    useEffect(() => {
        obtenerInstalaciones()
    }, [])


    const manejarCambio = (e) => {

        const { name, value } = e.target

        setNuevaInstalacion({
            ...nuevaInstalacion,
            [name]: value
        })
    }


    const editarInstalacion = (instalacion) => {

        setInstalacionEditando(instalacion)

        setNuevaInstalacion({
            nombre: instalacion.nombre || "",
            tipo: instalacion.tipo || "",
            capacidad: instalacion.capacidad || "",
            tarifaBase: instalacion.tarifaBase || "",
            estado: instalacion.estado || "DISPONIBLE"
        })

        setModoEdicion(true)
        setMostrarFormulario(true)
    }


    const guardarInstalacion = async (e) => {

        e.preventDefault()

        try {

            const token = sessionStorage.getItem("token")

            const estabaEditando = modoEdicion

            let respuesta

            if (modoEdicion) {

                respuesta = await fetch(
                    `http://localhost:8080/api/instalaciones/${instalacionEditando.idInstalacion}`,
                    {
                        method: "PUT",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${token}`
                        },
                        body: JSON.stringify(nuevaInstalacion)
                    }
                )

            } else {

                respuesta = await fetch(
                    "http://localhost:8080/api/instalaciones",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${token}`
                        },
                        body: JSON.stringify(nuevaInstalacion)
                    }
                )
            }

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje ||
                    "No se pudo guardar la instalación"
                )
            }

            const datos = await respuesta.json()

            console.log(
                "Instalación guardada:",
                datos
            )

            setMostrarFormulario(false)
            setModoEdicion(false)
            setInstalacionEditando(null)

            setNuevaInstalacion({
                nombre: "",
                tipo: "",
                capacidad: "",
                tarifaBase: "",
                estado: "DISPONIBLE"
            })

            await obtenerInstalaciones()

            if (estabaEditando) {

                alert(
                    "Instalación actualizada correctamente"
                )

            } else {

                alert(
                    "Instalación creada correctamente"
                )
            }

        } catch (error) {

            console.error(
                "Error al guardar instalación:",
                error
            )

            alert(error.message)

        }
    }


    const nuevaInstalacionFormulario = () => {

        setModoEdicion(false)
        setInstalacionEditando(null)

        setNuevaInstalacion({
            nombre: "",
            tipo: "",
            capacidad: "",
            tarifaBase: "",
            estado: "DISPONIBLE"
        })

        setMostrarFormulario(true)
    }


    const cancelarFormulario = () => {

        setMostrarFormulario(false)
        setModoEdicion(false)
        setInstalacionEditando(null)

        setNuevaInstalacion({
            nombre: "",
            tipo: "",
            capacidad: "",
            tarifaBase: "",
            estado: "DISPONIBLE"
        })
    }


    const eliminarInstalacion = async (id) => {

        const confirmar = window.confirm(
            "¿Estás seguro de que querés eliminar esta instalación?"
        )

        if (!confirmar) {
            return
        }

        try {

            const token = sessionStorage.getItem("token")

            const respuesta = await fetch(
                `http://localhost:8080/api/instalaciones/${id}`,
                {
                    method: "DELETE",
                    headers: {
                        "Authorization": `Bearer ${token}`
                    }
                }
            )

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje ||
                    "No se pudo eliminar la instalación"
                )
            }

            await obtenerInstalaciones()

            alert(
                "Instalación eliminada correctamente"
            )

        } catch (error) {

            console.error(
                "Error al eliminar instalación:",
                error
            )

            alert(error.message)

        }
    }


    if (cargando) {
        return <p>Cargando instalaciones...</p>
    }


    if (error) {
        return (
            <p className="error-message">
                {error}
            </p>
        )
    }


    return (
        <section className="usuarios">

            <div className="seccion-titulo">

                <div>

                    <h2>
                        Instalaciones
                    </h2>

                    <p>
                        Instalaciones deportivas registradas.
                    </p>

                </div>


                {esAdministrador && (

                    <button
                        className="boton-principal"
                        onClick={
                            nuevaInstalacionFormulario
                        }
                    >
                        Nueva instalación
                    </button>

                )}

            </div>


            {mostrarFormulario && esAdministrador && (

                <form
                    className="formulario-usuario"
                    onSubmit={guardarInstalacion}
                >

                    <h3>
                        {modoEdicion
                            ? "Editar instalación"
                            : "Nueva instalación"
                        }
                    </h3>


                    <div className="formulario-grid">

                        <div className="form-group">

                            <label>
                                Nombre
                            </label>

                            <input
                                type="text"
                                name="nombre"
                                value={
                                    nuevaInstalacion.nombre
                                }
                                onChange={manejarCambio}
                                placeholder="Ej: Cancha de fútbol"
                                required
                            />

                        </div>


                        <div className="form-group">

                            <label>
                                Tipo
                            </label>

                            <input
                                type="text"
                                name="tipo"
                                value={
                                    nuevaInstalacion.tipo
                                }
                                onChange={manejarCambio}
                                placeholder="Ej: Cancha"
                                required
                            />

                        </div>


                        <div className="form-group">

                            <label>
                                Capacidad
                            </label>

                            <input
                                type="number"
                                name="capacidad"
                                value={
                                    nuevaInstalacion.capacidad
                                }
                                onChange={manejarCambio}
                                min="1"
                                placeholder="Ej: 20"
                            />

                        </div>


                        <div className="form-group">

                            <label>
                                Tarifa base
                            </label>

                            <input
                                type="number"
                                name="tarifaBase"
                                value={
                                    nuevaInstalacion.tarifaBase
                                }
                                onChange={manejarCambio}
                                min="0"
                                step="0.01"
                                placeholder="Ej: 5000"
                                required
                            />

                        </div>


                        <div className="form-group">

                            <label>
                                Estado
                            </label>

                            <select
                                name="estado"
                                value={
                                    nuevaInstalacion.estado
                                }
                                onChange={manejarCambio}
                            >

                                <option value="DISPONIBLE">
                                    Disponible
                                </option>

                                <option value="MANTENIMIENTO">
                                    Mantenimiento
                                </option>

                                <option value="CLAUSURADA">
                                    Clausurada
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
                            type="submit"
                            className="boton-principal"
                        >
                            {modoEdicion
                                ? "Guardar cambios"
                                : "Guardar instalación"
                            }
                        </button>

                    </div>

                </form>

            )}


            <div className="tabla-contenedor">

                <table>

                    <thead>

                    <tr>

                        <th>
                            ID
                        </th>

                        <th>
                            Nombre
                        </th>

                        <th>
                            Tipo
                        </th>

                        <th>
                            Capacidad
                        </th>

                        <th>
                            Tarifa base
                        </th>

                        <th>
                            Estado
                        </th>

                        {esAdministrador && (
                            <th>
                                Acciones
                            </th>
                        )}

                    </tr>

                    </thead>


                    <tbody>

                    {instalaciones.map(
                        (instalacion) => (

                            <tr
                                key={
                                    instalacion.idInstalacion
                                }
                            >

                                <td>
                                    {
                                        instalacion.idInstalacion
                                    }
                                </td>

                                <td>
                                    {
                                        instalacion.nombre
                                    }
                                </td>

                                <td>
                                    {
                                        instalacion.tipo
                                    }
                                </td>

                                <td>
                                    {
                                        instalacion.capacidad ||
                                        "-"
                                    }
                                </td>

                                <td>
                                    $
                                    {
                                        instalacion.tarifaBase
                                    }
                                </td>

                                <td>
                                    {
                                        instalacion.estado
                                    }
                                </td>


                                {esAdministrador && (

                                    <td>

                                        <button
                                            className="boton-editar"
                                            onClick={() =>
                                                editarInstalacion(
                                                    instalacion
                                                )
                                            }
                                        >
                                            Editar
                                        </button>


                                        <button
                                            className="boton-eliminar"
                                            onClick={() =>
                                                eliminarInstalacion(
                                                    instalacion.idInstalacion
                                                )
                                            }
                                        >
                                            Eliminar
                                        </button>

                                    </td>

                                )}

                            </tr>

                        )
                    )}

                    </tbody>

                </table>

            </div>

        </section>
    )
}

export default Instalaciones