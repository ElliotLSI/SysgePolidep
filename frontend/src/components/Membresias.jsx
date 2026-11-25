import { useEffect, useState } from "react"

function Membresias() {
    const [membresias, setMembresias] = useState([])
    const [usuarios, setUsuarios] = useState([])
    const [socios, setSocios] = useState([])
    const [categorias, setCategorias] = useState([])

    const [filtroEstadoMembresia, setFiltroEstadoMembresia] = useState("TODAS") // TODAS | VIGENTE | VENCIDA
    const [filtroEstadoCategoria, setFiltroEstadoCategoria] = useState("TODAS") // TODAS | ACTIVAS | INACTIVAS

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
    const [mensaje, setMensaje] = useState("")

    const usuarioActual = JSON.parse(sessionStorage.getItem("usuario"))
    const roles = usuarioActual?.roles ?? []

    const esAdministrador = roles.includes("ADMINISTRADOR")
    const esEmpleado = roles.includes("EMPLEADO")
    const esUsuario = roles.includes("USUARIO")

    const puedeGestionarMembresias = esAdministrador || esEmpleado
    const esUsuarioComun = esUsuario && !puedeGestionarMembresias
    const puedeRenovar = esAdministrador || esEmpleado

    const token = sessionStorage.getItem("token")

    const formatearFecha = (fechaStr) => {
        if (!fechaStr) return "-"
        const [anio, mes, dia] = fechaStr.split("-")
        return `${dia}/${mes}/${anio}`
    }

    const formatearMonto = (monto) =>
        Number(monto ?? 0).toLocaleString("es-AR", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })

    const obtenerAvisoVencimiento = (membresia) => {
        if (!membresia.fechaVenc || membresia.estado === "CANCELADA") return null
        const hoy = new Date()
        hoy.setHours(0, 0, 0, 0)
        const venc = new Date(membresia.fechaVenc + "T00:00:00")
        const diffDias = Math.ceil((venc - hoy) / (1000 * 60 * 60 * 24))

        if (diffDias < 0) {
            return { texto: "Cuota vencida", clase: "vencido" }
        }
        if (diffDias === 0) {
            return { texto: "Vence hoy", clase: "alerta" }
        }
        if (diffDias <= 7) {
            return { texto: `Vence en ${diffDias} día(s)`, clase: "alerta" }
        }
        return null
    }

    const obtenerEstadoReal = (membresia) => {
        if (membresia.estado === "VIGENTE" && membresia.fechaVenc) {
            const hoy = new Date()
            hoy.setHours(0, 0, 0, 0)
            const venc = new Date(membresia.fechaVenc + "T00:00:00")
            if (venc < hoy) return "VENCIDA"
        }
        return membresia.estado
    }

    const claseEstado = (estado) => {
        if (!estado) return "estado"
        return `estado estado-${estado.toLowerCase()}`
    }

    const cargarDatos = async () => {
        try {
            const headers = { Authorization: `Bearer ${token}` }
            const [resMemb, resUsu, resSoc, resCat] = await Promise.all([
                fetch("http://localhost:8080/api/membresias", { headers }).catch(() => null),
                esUsuarioComun
                    ? Promise.resolve(null)
                    : fetch("http://localhost:8080/api/usuarios", { headers }).catch(() => null),
                esUsuarioComun
                    ? Promise.resolve(null)
                    : fetch("http://localhost:8080/api/socios", { headers }).catch(() => null),
                fetch("http://localhost:8080/api/categorias", { headers })
            ])

            if (!resCat.ok || (!esUsuarioComun && (!resMemb?.ok || !resUsu?.ok || !resSoc?.ok))) {
                throw new Error("No se pudieron obtener los datos.")
            }

            setMembresias(resMemb?.ok ? await resMemb.json() : [])
            setUsuarios(resUsu?.ok ? await resUsu.json() : [])
            setSocios(resSoc?.ok ? await resSoc.json() : [])
            setCategorias(await resCat.json())
        } catch (err) {
            console.error(err)
            setError("No se pudieron obtener los datos.")
        }
    }

    useEffect(() => {
        cargarDatos()
    }, [])

    const manejarCambioMembresia = (e) => {
        setNuevaMembresia({ ...nuevaMembresia, [e.target.name]: e.target.value })
    }

    const manejarCambioCategoria = (e) => {
        const { name, value, type, checked } = e.target
        setNuevaCategoria({
            ...nuevaCategoria,
            [name]: type === "checkbox" ? checked : value
        })
    }

    const solicitarMembresiaUsuario = async (idCategoria) => {
        if (!window.confirm("¿Querés solicitar esta membresía?")) return
        setError("")
        setMensaje("")

        try {
            const respuesta = await fetch("http://localhost:8080/api/socios/alta", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({
                    idUsuario: usuarioActual.idUsuario,
                    idCategoria: Number(idCategoria)
                })
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo solicitar la membresía.")
            }

            setMensaje(
                "¡Membresía solicitada correctamente! Recordá enviar el pago desde la sección Pagos y volver a iniciar sesión para actualizar tu rol a Socio."
            )
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const crearMembresia = async (e) => {
        e.preventDefault()
        setError("")
        setMensaje("")
        if (!puedeGestionarMembresias) return

        try {
            const respuesta = await fetch("http://localhost:8080/api/socios/alta", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({
                    idUsuario: Number(nuevaMembresia.idUsuario),
                    idCategoria: Number(nuevaMembresia.idCategoria)
                })
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo crear el socio.")
            }

            setNuevaMembresia({ idUsuario: "", idCategoria: "" })
            setMostrarFormularioMembresia(false)
            setMensaje("Socio y membresía registrados correctamente.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const renovarMembresia = async (id) => {
        if (!puedeRenovar) return
        if (!window.confirm("¿Querés renovar esta membresía?")) return

        setError("")
        setMensaje("")
        try {
            const respuesta = await fetch(`http://localhost:8080/api/membresias/${id}/renovar`, {
                method: "PUT",
                headers: { Authorization: `Bearer ${token}` }
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo renovar la membresía.")
            }

            setMensaje("Membresía renovada correctamente.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const eliminarMembresia = async (id) => {
        if (!esAdministrador) return
        if (!window.confirm("¿Querés eliminar esta membresía?")) return

        setError("")
        setMensaje("")
        try {
            const respuesta = await fetch(`http://localhost:8080/api/membresias/${id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` }
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo eliminar la membresía.")
            }

            setMensaje("Membresía eliminada correctamente.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const guardarCategoria = async (e) => {
        e.preventDefault()
        if (!esAdministrador) return
        setError("")
        setMensaje("")

        try {
            const datos = {
                nombre: nuevaCategoria.nombre,
                costo: Number(nuevaCategoria.costo),
                porcDescuento: Number(nuevaCategoria.porcDescuento),
                descripBeneficios: nuevaCategoria.descripBeneficios,
                duracionMeses: Number(nuevaCategoria.duracionMeses),
                activo: nuevaCategoria.activo
            }

            const url = categoriaEditando
                ? `http://localhost:8080/api/categorias/${categoriaEditando}`
                : "http://localhost:8080/api/categorias"

            const respuesta = await fetch(url, {
                method: categoriaEditando ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(datos)
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo guardar la categoría.")
            }

            limpiarFormularioCategoria()
            setMensaje(categoriaEditando ? "Categoría actualizada." : "Categoría creada.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
        }
    }

    const editarCategoria = (categoria) => {
        if (!esAdministrador) return
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
        if (!esAdministrador) return
        if (!window.confirm("¿Querés eliminar esta categoría?")) return

        setError("")
        setMensaje("")
        try {
            const respuesta = await fetch(`http://localhost:8080/api/categorias/${id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` }
            })

            if (!respuesta.ok) {
                const msg = await respuesta.text()
                throw new Error(msg || "No se pudo eliminar la categoría.")
            }

            setMensaje("Categoría eliminada correctamente.")
            await cargarDatos()
        } catch (err) {
            console.error(err)
            setError(err.message)
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

    // ============================================================
    // VISTA EXCLUSIVA PARA USUARIO COMÚN (SOLICITAR MEMBRESÍA)
    // ============================================================
    if (esUsuarioComun) {
        const miMembresia = membresias.find(
            m => m.socio?.idUsuario === usuarioActual?.idUsuario
        )
        const categoriasActivas = categorias.filter(c => c.activo)

        return (
            <section className="usuarios">
                <div className="seccion-titulo">
                    <div>
                        <h2>Solicitar membresía</h2>
                        <p>Elegí un plan para asociarte y acceder a descuentos en tus reservas.</p>
                    </div>
                </div>

                {mensaje && <div className="mensaje-exito">{mensaje}</div>}
                {error && <div className="mensaje-error">{error}</div>}

                {miMembresia ? (
                    <div
                        className="tarjeta"
                        style={{ borderLeft: "4px solid #5C0613", maxWidth: "560px" }}
                    >
                        <h3 style={{ marginTop: 0 }}>Ya tenés una membresía registrada</h3>
                        <p>
                            <strong>Plan:</strong> {miMembresia.categoria?.nombre || "-"}
                        </p>
                        <p>
                            <strong>Estado actual:</strong>{" "}
                            <span className={claseEstado(obtenerEstadoReal(miMembresia))}>
                                {obtenerEstadoReal(miMembresia)}
                            </span>
                        </p>
                        <p>
                            <strong>Vencimiento:</strong> {formatearFecha(miMembresia.fechaVenc)}
                        </p>
                        <p style={{ color: "#64748B", fontSize: "14px", marginBottom: 0 }}>
                            Si tu cuota está vencida o pendiente de pago, podés abonar la renovación directamente desde la sección <strong>Pagos</strong>.
                        </p>
                    </div>
                ) : categoriasActivas.length === 0 ? (
                    <div className="tabla-contenedor" style={{ padding: "24px", textAlign: "center", color: "#64748B" }}>
                        No hay planes de membresía disponibles en este momento.
                    </div>
                ) : (
                    <div className="tarjetas">
                        {categoriasActivas.map(c => (
                            <div
                                className="tarjeta"
                                key={c.idCategoria}
                                style={{ borderLeft: "4px solid #5C0613" }}
                            >
                                <h3>{c.nombre}</h3>
                                <p style={{ fontSize: "20px", fontWeight: "bold", color: "#5C0613", margin: "8px 0" }}>
                                    ${formatearMonto(c.costo)}
                                </p>
                                <p>
                                    <strong>Descuento en reservas:</strong> {c.porcDescuento}%
                                </p>
                                <p>
                                    <strong>Duración:</strong> {c.duracionMeses} mes(es)
                                </p>
                                {c.descripBeneficios && (
                                    <p style={{ color: "#475569", fontSize: "14px", marginBottom: "14px" }}>
                                        {c.descripBeneficios}
                                    </p>
                                )}
                                <button
                                    className="boton-principal"
                                    onClick={() => solicitarMembresiaUsuario(c.idCategoria)}
                                >
                                    Solicitar esta membresía
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        )
    }

    // ============================================================
    // VISTA ADMINISTRADOR / EMPLEADO
    // ============================================================
    const usuariosDisponibles = usuarios.filter(
        u => !socios.some(s => s.idUsuario === u.idUsuario)
    )

    const totalVigentes = membresias.filter(m => obtenerEstadoReal(m) === "VIGENTE").length
    const totalVencidas = membresias.filter(m => obtenerEstadoReal(m) === "VENCIDA").length

    const membresiasFiltradas = membresias.filter(m => {
        const estadoReal = obtenerEstadoReal(m)
        if (filtroEstadoMembresia === "VIGENTE" && estadoReal !== "VIGENTE") return false
        if (filtroEstadoMembresia === "VENCIDA" && estadoReal !== "VENCIDA") return false
        return true
    })

    const totalCatActivas = categorias.filter(c => c.activo).length
    const totalCatInactivas = categorias.length - totalCatActivas

    const categoriasFiltradas = categorias.filter(c => {
        if (filtroEstadoCategoria === "ACTIVAS" && !c.activo) return false
        if (filtroEstadoCategoria === "INACTIVAS" && c.activo) return false
        return true
    })

    return (
        <div>
            {mensaje && <div className="mensaje-exito">{mensaje}</div>}
            {error && <div className="mensaje-error">{error}</div>}

            <section className="usuarios">
                <div className="seccion-titulo">
                    <div>
                        <h2>Membresías</h2>
                        <p>Gestión de socios, vigencia de cuotas y descuentos.</p>
                    </div>

                    {puedeGestionarMembresias && (
                        <button
                            className="boton-principal"
                            onClick={() => {
                                setError("")
                                setMensaje("")
                                setNuevaMembresia({ idUsuario: "", idCategoria: "" })
                                setMostrarFormularioMembresia(!mostrarFormularioMembresia)
                            }}
                        >
                            {mostrarFormularioMembresia ? "Cerrar formulario" : "+ Nueva membresía"}
                        </button>
                    )}
                </div>

                {mostrarFormularioMembresia && puedeGestionarMembresias && (
                    <form className="formulario-usuario" onSubmit={crearMembresia}>
                        <h3>Alta de socio</h3>
                        <div className="formulario-grid">
                            <div>
                                <label>Usuario</label>
                                <select
                                    name="idUsuario"
                                    value={nuevaMembresia.idUsuario}
                                    onChange={manejarCambioMembresia}
                                    required
                                >
                                    <option value="">Seleccionar usuario</option>
                                    {usuariosDisponibles.map(u => (
                                        <option key={u.idUsuario} value={u.idUsuario}>
                                            {u.nombre} {u.apellido} (DNI: {u.dni})
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
                                    <option value="">Seleccionar categoría</option>
                                    {categorias
                                        .filter(c => c.activo)
                                        .map(c => (
                                            <option key={c.idCategoria} value={c.idCategoria}>
                                                {c.nombre} — ${formatearMonto(c.costo)} ({c.porcDescuento}% desc.)
                                            </option>
                                        ))}
                                </select>
                            </div>
                        </div>

                        <div className="formulario-botones">
                            <button
                                type="button"
                                className="boton-secundario"
                                onClick={() => setMostrarFormularioMembresia(false)}
                            >
                                Cancelar
                            </button>
                            <button type="submit" className="boton-principal">
                                Crear socio
                            </button>
                        </div>
                    </form>
                )}

                {/* FILTROS DE MEMBRESÍAS */}
                <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", marginBottom: "16px" }}>
                    <button
                        type="button"
                        className={filtroEstadoMembresia === "TODAS" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstadoMembresia("TODAS")}
                    >
                        Todas ({membresias.length})
                    </button>
                    <button
                        type="button"
                        className={filtroEstadoMembresia === "VIGENTE" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstadoMembresia("VIGENTE")}
                    >
                        Vigentes ({totalVigentes})
                    </button>
                    <button
                        type="button"
                        className={filtroEstadoMembresia === "VENCIDA" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstadoMembresia("VENCIDA")}
                    >
                        Vencidas ({totalVencidas})
                    </button>
                </div>

                <div className="tabla-contenedor">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Socio</th>
                            <th>N.º Socio</th>
                            <th>Categoría</th>
                            <th>Descuento</th>
                            <th>Inicio</th>
                            <th>Vencimiento</th>
                            <th>Estado</th>
                            {(puedeRenovar || esAdministrador) && <th>Acciones</th>}
                        </tr>
                        </thead>
                        <tbody>
                        {membresiasFiltradas.length === 0 ? (
                            <tr>
                                <td colSpan="9" className="tabla-vacia">
                                    No hay membresías en este estado.
                                </td>
                            </tr>
                        ) : (
                            membresiasFiltradas.map(m => {
                                const aviso = obtenerAvisoVencimiento(m)
                                const estadoReal = obtenerEstadoReal(m)
                                return (
                                    <tr key={m.idMembresia}>
                                        <td>#{m.idMembresia}</td>
                                        <td>
                                            <strong>
                                                {m.socio?.usuario?.nombre} {m.socio?.usuario?.apellido}
                                            </strong>
                                        </td>
                                        <td>{m.socio?.nroSocio}</td>
                                        <td>{m.categoria?.nombre}</td>
                                        <td>{m.categoria?.porcDescuento ?? 0}%</td>
                                        <td>{formatearFecha(m.fechaInicio)}</td>
                                        <td>
                                            <div>{formatearFecha(m.fechaVenc)}</div>
                                            {aviso && (
                                                <span className={`aviso-vencimiento ${aviso.clase}`}>
                                                    {aviso.texto}
                                                </span>
                                            )}
                                        </td>
                                        <td>
                                            <span className={claseEstado(estadoReal)}>
                                                {estadoReal}
                                            </span>
                                        </td>
                                        {(puedeRenovar || esAdministrador) && (
                                            <td>
                                                {puedeRenovar && (
                                                    <button
                                                        className="boton-editar"
                                                        onClick={() => renovarMembresia(m.idMembresia)}
                                                    >
                                                        Renovar
                                                    </button>
                                                )}
                                                {esAdministrador && (
                                                    <button
                                                        className="boton-eliminar"
                                                        onClick={() => eliminarMembresia(m.idMembresia)}
                                                    >
                                                        Eliminar
                                                    </button>
                                                )}
                                            </td>
                                        )}
                                    </tr>
                                )
                            })
                        )}
                        </tbody>
                    </table>
                </div>
            </section>

            {/* CATEGORÍAS */}
            <section className="usuarios" style={{ marginTop: "25px" }}>
                <div className="seccion-titulo">
                    <div>
                        <h2>Categorías</h2>
                        <p>Planes, costos y porcentajes de descuento disponibles.</p>
                    </div>

                    {esAdministrador && (
                        <button
                            className="boton-principal"
                            onClick={() => {
                                setError("")
                                setMensaje("")
                                setCategoriaEditando(null)
                                setNuevaCategoria({
                                    nombre: "",
                                    costo: "",
                                    porcDescuento: "",
                                    descripBeneficios: "",
                                    duracionMeses: "",
                                    activo: true
                                })
                                setMostrarFormularioCategoria(!mostrarFormularioCategoria)
                            }}
                        >
                            {mostrarFormularioCategoria ? "Cerrar formulario" : "+ Nueva categoría"}
                        </button>
                    )}
                </div>

                {mostrarFormularioCategoria && esAdministrador && (
                    <form className="formulario-usuario" onSubmit={guardarCategoria}>
                        <h3>{categoriaEditando ? "Editar categoría" : "Nueva categoría"}</h3>
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
                                <label>Costo ($)</label>
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
                                <label>Descuento en reservas (%)</label>
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
                                <label style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                                    <input
                                        type="checkbox"
                                        name="activo"
                                        checked={nuevaCategoria.activo}
                                        onChange={manejarCambioCategoria}
                                        style={{ width: "auto", minHeight: "auto" }}
                                    />
                                    Categoría activa
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
                            <button type="submit" className="boton-principal">
                                {categoriaEditando ? "Guardar cambios" : "Crear categoría"}
                            </button>
                        </div>
                    </form>
                )}

                {/* FILTROS DE CATEGORÍAS */}
                <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", marginBottom: "16px" }}>
                    <button
                        type="button"
                        className={filtroEstadoCategoria === "TODAS" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstadoCategoria("TODAS")}
                    >
                        Todas ({categorias.length})
                    </button>
                    <button
                        type="button"
                        className={filtroEstadoCategoria === "ACTIVAS" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstadoCategoria("ACTIVAS")}
                    >
                        Activas ({totalCatActivas})
                    </button>
                    <button
                        type="button"
                        className={filtroEstadoCategoria === "INACTIVAS" ? "boton-principal" : "boton-secundario"}
                        onClick={() => setFiltroEstadoCategoria("INACTIVAS")}
                    >
                        Inactivas ({totalCatInactivas})
                    </button>
                </div>

                <div className="tabla-contenedor">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Costo</th>
                            <th>Descuento</th>
                            <th>Duración</th>
                            <th>Estado</th>
                            {esAdministrador && <th>Acciones</th>}
                        </tr>
                        </thead>
                        <tbody>
                        {categoriasFiltradas.length === 0 ? (
                            <tr>
                                <td colSpan={esAdministrador ? 7 : 6} className="tabla-vacia">
                                    No hay categorías en este estado.
                                </td>
                            </tr>
                        ) : (
                            categoriasFiltradas.map(c => (
                                <tr key={c.idCategoria}>
                                    <td>#{c.idCategoria}</td>
                                    <td><strong>{c.nombre}</strong></td>
                                    <td>${formatearMonto(c.costo)}</td>
                                    <td>{c.porcDescuento}%</td>
                                    <td>{c.duracionMeses} mes(es)</td>
                                    <td>
                                        <span className={`estado ${c.activo ? "estado-activo" : "estado-inactivo"}`}>
                                            {c.activo ? "ACTIVA" : "INACTIVA"}
                                        </span>
                                    </td>
                                    {esAdministrador && (
                                        <td>
                                            <button
                                                className="boton-editar"
                                                onClick={() => editarCategoria(c)}
                                            >
                                                Editar
                                            </button>
                                            <button
                                                className="boton-eliminar"
                                                onClick={() => eliminarCategoria(c.idCategoria)}
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
        </div>
    )
}

export default Membresias