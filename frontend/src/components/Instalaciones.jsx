import { useEffect, useState } from "react"

function Instalaciones() {
    const [instalaciones, setInstalaciones] = useState([])
    const [busqueda, setBusqueda] = useState("")
    const [filtroEstado, setFiltroEstado] = useState("TODAS") // TODAS | DISPONIBLE | MANTENIMIENTO | CLAUSURADA
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState("")
    const [mensaje, setMensaje] = useState("")

    const [modoEdicion, setModoEdicion] = useState(false)
    const [instalacionEditando, setInstalacionEditando] = useState(null)
    const [mostrarFormulario, setMostrarFormulario] = useState(false)

    const estadoInicial = {
        nombre: "",
        tipo: "",
        capacidad: "",
        tarifaBase: "",
        estado: "DISPONIBLE"
    }

    const [nuevaInstalacion, setNuevaInstalacion] = useState(estadoInicial)

    const usuario = JSON.parse(sessionStorage.getItem("usuario"))
    const roles = usuario?.roles ?? []
    const esAdministrador = roles.includes("ADMINISTRADOR")

    const formatearMonto = (monto) =>
        Number(monto ?? 0).toLocaleString("es-AR", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })

    const claseEstado = (estado) => {
        if (!estado) return "estado"
        const normalizado = estado.toLowerCase()
        if (normalizado.includes("mantenimiento")) return "estado estado-mantenimiento"
        return `estado estado-${normalizado}`
    }

    const normalizarEstado = (estado) => (estado || "").trim().toUpperCase()

    const obtenerInstalaciones = async () => {
        try {
            const token = sessionStorage.getItem("token")
            const respuesta = await fetch("http://localhost:8080/api/instalaciones", {
                headers: { Authorization: `Bearer ${token}` }
            })
            if (!respuesta.ok) throw new Error("No se pudieron obtener las instalaciones.")
            setInstalaciones(await respuesta.json())
        } catch (err) {
            console.error("Error al obtener instalaciones:", err)
            setError(err.message)
        } finally {
            setCargando(false)
        }
    }

    useEffect(() => {
        obtenerInstalaciones()
    }, [])

    const manejarCambio = (e) => {
        const { name, value } = e.target
        setNuevaInstalacion({ ...nuevaInstalacion, [name]: value })
    }

    const editarInstalacion = (instalacion) => {
        setError("")
        setMensaje("")
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
        setError("")
        setMensaje("")

        try {
            const token = sessionStorage.getItem("token")
            const estabaEditando = modoEdicion
            const url = modoEdicion
                ? `http://localhost:8080/api/instalaciones/${instalacionEditando.idInstalacion}`
                : "http://localhost:8080/api/instalaciones"

            const respuesta = await fetch(url, {
                method: modoEdicion ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(nuevaInstalacion)
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo guardar la instalación.")
            }

            cancelarFormulario()
            setMensaje(
                estabaEditando
                    ? "Instalación actualizada correctamente."
                    : "Instalación creada correctamente."
            )
            await obtenerInstalaciones()
        } catch (err) {
            console.error("Error al guardar instalación:", err)
            setError(err.message)
        }
    }

    const nuevaInstalacionFormulario = () => {
        setError("")
        setMensaje("")
        setModoEdicion(false)
        setInstalacionEditando(null)
        setNuevaInstalacion(estadoInicial)
        setMostrarFormulario(true)
    }

    const cancelarFormulario = () => {
        setMostrarFormulario(false)
        setModoEdicion(false)
        setInstalacionEditando(null)
        setNuevaInstalacion(estadoInicial)
    }

    const eliminarInstalacion = async (id) => {
        if (!window.confirm("¿Estás seguro de que querés eliminar esta instalación?")) return
        setError("")
        setMensaje("")

        try {
            const token = sessionStorage.getItem("token")
            const respuesta = await fetch(`http://localhost:8080/api/instalaciones/${id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` }
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo eliminar la instalación.")
            }

            setMensaje("Instalación eliminada correctamente.")
            await obtenerInstalaciones()
        } catch (err) {
            console.error("Error al eliminar instalación:", err)
            setError(err.message)
        }
    }

    const totalDisponibles = instalaciones.filter(i => normalizarEstado(i.estado) === "DISPONIBLE").length
    const totalMantenimiento = instalaciones.filter(i => normalizarEstado(i.estado).includes("MANTENIMIENTO")).length
    const totalClausuradas = instalaciones.filter(i => normalizarEstado(i.estado) === "CLAUSURADA").length

    const instalacionesFiltradas = instalaciones.filter(inst => {
        const estadoNorm = normalizarEstado(inst.estado)
        if (filtroEstado === "DISPONIBLE" && estadoNorm !== "DISPONIBLE") return false
        if (filtroEstado === "MANTENIMIENTO" && !estadoNorm.includes("MANTENIMIENTO")) return false
        if (filtroEstado === "CLAUSURADA" && estadoNorm !== "CLAUSURADA") return false

        const texto = busqueda.trim().toLowerCase()
        if (!texto) return true
        return (
            (inst.nombre || "").toLowerCase().includes(texto) ||
            (inst.tipo || "").toLowerCase().includes(texto) ||
            (inst.estado || "").toLowerCase().includes(texto)
        )
    })

    if (cargando) return <p>Cargando instalaciones...</p>

    return (
        <section className="usuarios">
            <div className="seccion-titulo">
                <div>
                    <h2>Instalaciones</h2>
                    <p>Espacios deportivos, tarifas por hora y disponibilidad.</p>
                </div>

                {esAdministrador && (
                    <button className="boton-principal" onClick={nuevaInstalacionFormulario}>
                        + Nueva instalación
                    </button>
                )}
            </div>

            {mensaje && <div className="mensaje-exito">{mensaje}</div>}
            {error && <div className="mensaje-error">{error}</div>}

            {mostrarFormulario && esAdministrador && (
                <form className="formulario-usuario" onSubmit={guardarInstalacion}>
                    <h3>{modoEdicion ? "Editar instalación" : "Nueva instalación"}</h3>

                    <div className="formulario-grid">
                        <div>
                            <label>Nombre</label>
                            <input
                                type="text"
                                name="nombre"
                                value={nuevaInstalacion.nombre}
                                onChange={manejarCambio}
                                placeholder="Ej: Cancha de fútbol 5"
                                required
                            />
                        </div>

                        <div>
                            <label>Tipo</label>
                            <input
                                type="text"
                                name="tipo"
                                value={nuevaInstalacion.tipo}
                                onChange={manejarCambio}
                                placeholder="Ej: Cancha, Pileta, SUM"
                                required
                            />
                        </div>

                        <div>
                            <label>Capacidad (personas)</label>
                            <input
                                type="number"
                                name="capacidad"
                                value={nuevaInstalacion.capacidad}
                                onChange={manejarCambio}
                                min="1"
                                placeholder="Ej: 20"
                            />
                        </div>

                        <div>
                            <label>Tarifa base por hora ($)</label>
                            <input
                                type="number"
                                name="tarifaBase"
                                value={nuevaInstalacion.tarifaBase}
                                onChange={manejarCambio}
                                min="0"
                                step="0.01"
                                placeholder="Ej: 5000"
                                required
                            />
                        </div>

                        <div>
                            <label>Estado</label>
                            <select
                                name="estado"
                                value={nuevaInstalacion.estado}
                                onChange={manejarCambio}
                            >
                                <option value="DISPONIBLE">Disponible</option>
                                <option value="MANTENIMIENTO">Mantenimiento</option>
                                <option value="CLAUSURADA">Clausurada</option>
                            </select>
                        </div>
                    </div>

                    <div className="formulario-botones">
                        <button type="button" className="boton-secundario" onClick={cancelarFormulario}>
                            Cancelar
                        </button>
                        <button type="submit" className="boton-principal">
                            {modoEdicion ? "Guardar cambios" : "Guardar instalación"}
                        </button>
                    </div>
                </form>
            )}

            {/* BUSCADOR + FILTROS DE ESTADO */}
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
                    placeholder="Buscar por nombre, tipo o estado..."
                    value={busqueda}
                    onChange={e => setBusqueda(e.target.value)}
                    style={{
                        flex: "1 1 260px",
                        maxWidth: "380px",
                        padding: "9px 12px",
                        border: "1px solid #CBD5E1",
                        borderRadius: "7px",
                        fontSize: "14px"
                    }}
                />

                <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
                    <button
                        type="button"
                        className={filtroEstado === "TODAS" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstado("TODAS")}
                    >
                        Todas ({instalaciones.length})
                    </button>
                    <button
                        type="button"
                        className={filtroEstado === "DISPONIBLE" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstado("DISPONIBLE")}
                    >
                        Disponibles ({totalDisponibles})
                    </button>
                    <button
                        type="button"
                        className={filtroEstado === "MANTENIMIENTO" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstado("MANTENIMIENTO")}
                    >
                        Mantenimiento ({totalMantenimiento})
                    </button>
                    <button
                        type="button"
                        className={filtroEstado === "CLAUSURADA" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstado("CLAUSURADA")}
                    >
                        Clausuradas ({totalClausuradas})
                    </button>
                </div>
            </div>

            <div className="tabla-contenedor">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Tipo</th>
                        <th>Capacidad</th>
                        <th>Tarifa Base</th>
                        <th>Estado</th>
                        {esAdministrador && <th>Acciones</th>}
                    </tr>
                    </thead>
                    <tbody>
                    {instalacionesFiltradas.length === 0 ? (
                        <tr>
                            <td colSpan={esAdministrador ? 7 : 6} className="tabla-vacia">
                                No se encontraron instalaciones con los filtros seleccionados.
                            </td>
                        </tr>
                    ) : (
                        instalacionesFiltradas.map(inst => (
                            <tr key={inst.idInstalacion}>
                                <td>#{inst.idInstalacion}</td>
                                <td><strong>{inst.nombre}</strong></td>
                                <td>{inst.tipo}</td>
                                <td>{inst.capacidad ? `${inst.capacidad} pers.` : "-"}</td>
                                <td><strong>${formatearMonto(inst.tarifaBase)}</strong> / h</td>
                                <td>
                                    <span className={claseEstado(inst.estado)}>
                                        {inst.estado}
                                    </span>
                                </td>
                                {esAdministrador && (
                                    <td>
                                        <button
                                            className="boton-editar"
                                            onClick={() => editarInstalacion(inst)}
                                        >
                                            Editar
                                        </button>
                                        <button
                                            className="boton-eliminar"
                                            onClick={() => eliminarInstalacion(inst.idInstalacion)}
                                        >
                                            Eliminar
                                        </button>
                                    </td>
                                )}
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </section>
    )
}

export default Instalaciones