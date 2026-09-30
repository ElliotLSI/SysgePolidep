import { useEffect, useState } from "react"

function Membresias() {

    const [membresias, setMembresias] = useState([])
    const [usuarios, setUsuarios] = useState([])
    const [socios, setSocios] = useState([])
    const [categorias, setCategorias] = useState([])

    const [mostrarFormularioMembresia, setMostrarFormularioMembresia] = useState(false)
    const [mostrarFormularioCategoria, setMostrarFormularioCategoria] = useState(false)

    const [categoriaEditando, setCategoriaEditando] = useState(null)

    const [nuevaMembresia, setNuevaMembresia] = useState({
        idUsuario: "",
        idCategoria: ""
    })

    const [nuevaCategoria, setNuevaCategoria] = useState({
        nombre: "",
        costo: "",
        porcDescuento: "",
        descripBeneficios: "",
        duracionMeses: "",
        activo: true
    })

    const [error, setError] = useState("")

    const token = sessionStorage.getItem("token")

    const cargarDatos = async () => {

        try {

            const [respuestaMembresias, respuestaUsuarios,
                respuestaSocios, respuestaCategorias] = await Promise.all([

                fetch("http://localhost:8080/api/membresias", {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }),

                fetch("http://localhost:8080/api/usuarios", {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }),

                fetch("http://localhost:8080/api/socios", {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }),

                fetch("http://localhost:8080/api/categorias", {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                })

            ])

            if (!respuestaMembresias.ok ||
                !respuestaUsuarios.ok ||
                !respuestaSocios.ok ||
                !respuestaCategorias.ok) {

                throw new Error("No se pudieron obtener los datos.")
            }

            const datosMembresias = await respuestaMembresias.json()
            const datosUsuarios = await respuestaUsuarios.json()
            const datosSocios = await respuestaSocios.json()
            const datosCategorias = await respuestaCategorias.json()

            setMembresias(datosMembresias)
            setUsuarios(datosUsuarios)
            setSocios(datosSocios)
            setCategorias(datosCategorias)

        } catch (error) {

            console.error(error)
            setError("No se pudieron obtener los datos.")

        }

    }

    useEffect(() => {
        cargarDatos()
    }, [])

    const manejarCambioMembresia = (e) => {

        setNuevaMembresia({
            ...nuevaMembresia,
            [e.target.name]: e.target.value
        })

    }

    const manejarCambioCategoria = (e) => {

        const { name, value, type, checked } = e.target

        setNuevaCategoria({
            ...nuevaCategoria,
            [name]: type === "checkbox" ? checked : value
        })

    }

    const crearMembresia = async (e) => {

        e.preventDefault()
        setError("")

        try {

            const respuesta = await fetch(
                "http://localhost:8080/api/socios/alta",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`
                    },
                    body: JSON.stringify({
                        idUsuario: Number(nuevaMembresia.idUsuario),
                        idCategoria: Number(nuevaMembresia.idCategoria)
                    })
                }
            )

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje || "No se pudo crear el socio."
                )
            }

            setNuevaMembresia({
                idUsuario: "",
                idCategoria: ""
            })

            setMostrarFormularioMembresia(false)

            await cargarDatos()

        } catch (error) {

            console.error(error)
            setError(error.message)

        }

    }

    const renovarMembresia = async (id) => {

        if (!window.confirm("¿Querés renovar esta membresía?")) {
            return
        }

        try {

            const respuesta = await fetch(
                `http://localhost:8080/api/membresias/${id}/renovar`,
                {
                    method: "PUT",
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            )

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje || "No se pudo renovar la membresía."
                )
            }

            await cargarDatos()

        } catch (error) {

            console.error(error)
            setError(error.message)

        }

    }

    const eliminarMembresia = async (id) => {

        if (!window.confirm("¿Querés eliminar esta membresía?")) {
            return
        }

        try {

            const respuesta = await fetch(
                `http://localhost:8080/api/membresias/${id}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            )

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje || "No se pudo eliminar la membresía."
                )
            }

            await cargarDatos()

        } catch (error) {

            console.error(error)
            setError(error.message)

        }

    }

    const guardarCategoria = async (e) => {

        e.preventDefault()
        setError("")

        try {

            const datos = {
                nombre: nuevaCategoria.nombre,
                costo: Number(nuevaCategoria.costo),
                porcDescuento: Number(nuevaCategoria.porcDescuento),
                descripBeneficios: nuevaCategoria.descripBeneficios,
                duracionMeses: Number(nuevaCategoria.duracionMeses),
                activo: nuevaCategoria.activo
            }

            let respuesta

            if (categoriaEditando) {

                respuesta = await fetch(
                    `http://localhost:8080/api/categorias/${categoriaEditando}`,
                    {
                        method: "PUT",
                        headers: {
                            "Content-Type": "application/json",
                            Authorization: `Bearer ${token}`
                        },
                        body: JSON.stringify(datos)
                    }
                )

            } else {

                respuesta = await fetch(
                    "http://localhost:8080/api/categorias",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            Authorization: `Bearer ${token}`
                        },
                        body: JSON.stringify(datos)
                    }
                )

            }

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje || "No se pudo guardar la categoría."
                )
            }

            limpiarFormularioCategoria()
            await cargarDatos()

        } catch (error) {

            console.error(error)
            setError(error.message)

        }

    }

    const editarCategoria = (categoria) => {

        setCategoriaEditando(categoria.idCategoria)

        setNuevaCategoria({
            nombre: categoria.nombre,
            costo: categoria.costo,
            porcDescuento: categoria.porcDescuento,
            descripBeneficios: categoria.descripBeneficios || "",
            duracionMeses: categoria.duracionMeses,
            activo: categoria.activo
        })

        setMostrarFormularioCategoria(true)

    }

    const eliminarCategoria = async (id) => {

        if (!window.confirm("¿Querés eliminar esta categoría?")) {
            return
        }

        try {

            const respuesta = await fetch(
                `http://localhost:8080/api/categorias/${id}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            )

            if (!respuesta.ok) {

                const mensaje = await respuesta.text()

                throw new Error(
                    mensaje || "No se pudo eliminar la categoría."
                )
            }

            await cargarDatos()

        } catch (error) {

            console.error(error)
            setError(error.message)

        }

    }

    const limpiarFormularioCategoria = () => {

        setNuevaCategoria({
            nombre: "",
            costo: "",
            porcDescuento: "",
            descripBeneficios: "",
            duracionMeses: "",
            activo: true
        })

        setCategoriaEditando(null)
        setMostrarFormularioCategoria(false)

    }

    const usuariosDisponibles = usuarios.filter(usuario => {

        return !socios.some(
            socio => socio.idUsuario === usuario.idUsuario
        )

    })

    return (
        <div>

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {/* ========================= */}
            {/* MEMBRESÍAS */}
            {/* ========================= */}

            <section className="usuarios">

                <div className="seccion-titulo">

                    <div>
                        <h2>Membresías</h2>

                        <p>
                            Gestión de socios y membresías.
                        </p>
                    </div>

                    <button
                        className="boton-principal"
                        onClick={() => {
                            setError("")
                            setMostrarFormularioMembresia(
                                !mostrarFormularioMembresia
                            )
                        }}
                    >
                        Nueva membresía
                    </button>

                </div>

                {mostrarFormularioMembresia && (

                    <form
                        className="formulario-usuario"
                        onSubmit={crearMembresia}
                    >

                        <h3>
                            Alta de socio
                        </h3>

                        <div className="formulario-grid">

                            <div>
                                <label>Usuario</label>

                                <select
                                    name="idUsuario"
                                    value={nuevaMembresia.idUsuario}
                                    onChange={manejarCambioMembresia}
                                    required
                                >

                                    <option value="">
                                        Seleccionar usuario
                                    </option>

                                    {usuariosDisponibles.map(usuario => (

                                        <option
                                            key={usuario.idUsuario}
                                            value={usuario.idUsuario}
                                        >
                                            {usuario.nombre} {usuario.apellido}
                                        </option>

                                    ))}

                                </select>

                            </div>

                            <div>
                                <label>Categoría</label>

                                <select
                                    name="idCategoria"
                                    value={nuevaMembresia.idCategoria}
                                    onChange={manejarCambioMembresia}
                                    required
                                >

                                    <option value="">
                                        Seleccionar categoría
                                    </option>

                                    {categorias
                                        .filter(categoria => categoria.activo)
                                        .map(categoria => (

                                            <option
                                                key={categoria.idCategoria}
                                                value={categoria.idCategoria}
                                            >
                                                {categoria.nombre}
                                            </option>

                                        ))}

                                </select>

                            </div>

                        </div>

                        <div className="formulario-botones">

                            <button
                                type="button"
                                className="boton-secundario"
                                onClick={() => {
                                    setMostrarFormularioMembresia(false)
                                }}
                            >
                                Cancelar
                            </button>

                            <button
                                type="submit"
                                className="boton-principal"
                            >
                                Crear socio
                            </button>

                        </div>

                    </form>

                )}

                <div className="tabla-contenedor">

                    <table>

                        <thead>

                            <tr>
                                <th>ID</th>
                                <th>Socio</th>
                                <th>N.º Socio</th>
                                <th>Categoría</th>
                                <th>Inicio</th>
                                <th>Vencimiento</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>

                        </thead>

                        <tbody>

                            {membresias.map(membresia => (

                                <tr key={membresia.idMembresia}>

                                    <td>
                                        {membresia.idMembresia}
                                    </td>

                                    <td>
                                        {membresia.socio?.usuario?.nombre}{" "}
                                        {membresia.socio?.usuario?.apellido}
                                    </td>

                                    <td>
                                        {membresia.socio?.nroSocio}
                                    </td>

                                    <td>
                                        {membresia.categoria?.nombre}
                                    </td>

                                    <td>
                                        {membresia.fechaInicio}
                                    </td>

                                    <td>
                                        {membresia.fechaVenc}
                                    </td>

                                    <td>
                                        {membresia.estado}
                                    </td>

                                    <td>

                                        <button
                                            className="boton-editar"
                                            onClick={() =>
                                                renovarMembresia(
                                                    membresia.idMembresia
                                                )
                                            }
                                        >
                                            Renovar
                                        </button>

                                        <button
                                            className="boton-eliminar"
                                            onClick={() =>
                                                eliminarMembresia(
                                                    membresia.idMembresia
                                                )
                                            }
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


            {/* ========================= */}
            {/* CATEGORÍAS */}
            {/* ========================= */}

            <section
                className="usuarios"
                style={{ marginTop: "25px" }}
            >

                <div className="seccion-titulo">

                    <div>
                        <h2>Categorías</h2>

                        <p>
                            Planes y tipos de membresía disponibles.
                        </p>
                    </div>

                    <button
                        className="boton-principal"
                        onClick={() => {

                            setError("")
                            setCategoriaEditando(null)

                            setNuevaCategoria({
                                nombre: "",
                                costo: "",
                                porcDescuento: "",
                                descripBeneficios: "",
                                duracionMeses: "",
                                activo: true
                            })

                            setMostrarFormularioCategoria(
                                !mostrarFormularioCategoria
                            )

                        }}
                    >
                        Nueva categoría
                    </button>

                </div>


                {mostrarFormularioCategoria && (

                    <form
                        className="formulario-usuario"
                        onSubmit={guardarCategoria}
                    >

                        <h3>
                            {categoriaEditando
                                ? "Editar categoría"
                                : "Nueva categoría"}
                        </h3>

                        <div className="formulario-grid">

                            <div>
                                <label>Nombre</label>

                                <input
                                    type="text"
                                    name="nombre"
                                    value={nuevaCategoria.nombre}
                                    onChange={manejarCambioCategoria}
                                    required
                                />
                            </div>

                            <div>
                                <label>Costo</label>

                                <input
                                    type="number"
                                    name="costo"
                                    min="0"
                                    step="0.01"
                                    value={nuevaCategoria.costo}
                                    onChange={manejarCambioCategoria}
                                    required
                                />
                            </div>

                            <div>
                                <label>Descuento (%)</label>

                                <input
                                    type="number"
                                    name="porcDescuento"
                                    min="0"
                                    max="100"
                                    step="0.01"
                                    value={nuevaCategoria.porcDescuento}
                                    onChange={manejarCambioCategoria}
                                    required
                                />
                            </div>

                            <div>
                                <label>Duración (meses)</label>

                                <input
                                    type="number"
                                    name="duracionMeses"
                                    min="1"
                                    value={nuevaCategoria.duracionMeses}
                                    onChange={manejarCambioCategoria}
                                    required
                                />
                            </div>

                            <div style={{ gridColumn: "1 / -1" }}>
                                <label>Beneficios</label>

                                <textarea
                                    name="descripBeneficios"
                                    value={nuevaCategoria.descripBeneficios}
                                    onChange={manejarCambioCategoria}
                                    rows="3"
                                />
                            </div>

                            <div>
                                <label>
                                    <input
                                        type="checkbox"
                                        name="activo"
                                        checked={nuevaCategoria.activo}
                                        onChange={manejarCambioCategoria}
                                    />

                                    {" "}Categoría activa
                                </label>
                            </div>

                        </div>

                        <div className="formulario-botones">

                            <button
                                type="button"
                                className="boton-secundario"
                                onClick={limpiarFormularioCategoria}
                            >
                                Cancelar
                            </button>

                            <button
                                type="submit"
                                className="boton-principal"
                            >
                                {categoriaEditando
                                    ? "Guardar cambios"
                                    : "Crear categoría"}
                            </button>

                        </div>

                    </form>

                )}


                <div className="tabla-contenedor">

                    <table>

                        <thead>

                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Costo</th>
                                <th>Descuento</th>
                                <th>Duración</th>
                                <th>Activo</th>
                                <th>Acciones</th>
                            </tr>

                        </thead>

                        <tbody>

                            {categorias.map(categoria => (

                                <tr key={categoria.idCategoria}>

                                    <td>
                                        {categoria.idCategoria}
                                    </td>

                                    <td>
                                        {categoria.nombre}
                                    </td>

                                    <td>
                                        ${categoria.costo}
                                    </td>

                                    <td>
                                        {categoria.porcDescuento}%
                                    </td>

                                    <td>
                                        {categoria.duracionMeses} meses
                                    </td>

                                    <td>
                                        {categoria.activo
                                            ? "Sí"
                                            : "No"}
                                    </td>

                                    <td>

                                        <button
                                            className="boton-editar"
                                            onClick={() =>
                                                editarCategoria(categoria)
                                            }
                                        >
                                            Editar
                                        </button>

                                        <button
                                            className="boton-eliminar"
                                            onClick={() =>
                                                eliminarCategoria(
                                                    categoria.idCategoria
                                                )
                                            }
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

        </div>
    )
}

export default Membresias