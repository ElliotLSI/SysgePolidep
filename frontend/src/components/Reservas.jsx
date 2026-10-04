import { useEffect, useState } from "react"

function Reservas() {

    const [reservas, setReservas] = useState([])
    const [usuarios, setUsuarios] = useState([])
    const [instalaciones, setInstalaciones] = useState([])

    const [reservasAFavor, setReservasAFavor] = useState([])

    const [mostrarFormulario, setMostrarFormulario] = useState(false)
    const [mostrarFormularioReservaAFavor, setMostrarFormularioReservaAFavor] = useState(false)

    const [reservaAFavorSeleccionada, setReservaAFavorSeleccionada] = useState(null)

    const [nuevaReserva, setNuevaReserva] = useState({
        idUsuario: "",
        idInstalacion: "",
        fechaReserva: "",
        horaInicio: "",
        duracionHoras: 1,
        montoTotal: ""
    })

    const [nuevaReservaAFavor, setNuevaReservaAFavor] = useState({
        idInstalacion: "",
        fechaReserva: "",
        horaInicio: "",
        duracionHoras: 1
    })

    const [error, setError] = useState("")
    const [mensaje, setMensaje] = useState("")

    const token = sessionStorage.getItem("token")

    const usuarioActual = JSON.parse(
        sessionStorage.getItem("usuario")
    )

    const roles = usuarioActual?.roles ?? []

    const esAdministrador =
        roles.includes("ADMINISTRADOR")

    const esEmpleado =
        roles.includes("EMPLEADO")

    const esSocio =
        roles.includes("SOCIO")


    // =========================================================
    // CARGAR DATOS
    // =========================================================

    const cargarDatos = async () => {

        try {

            const [
                respuestaReservas,
                respuestaUsuarios,
                respuestaInstalaciones
            ] = await Promise.all([

                fetch("http://localhost:8080/api/reservas", {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }),

                fetch("http://localhost:8080/api/usuarios", {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }),

                fetch("http://localhost:8080/api/instalaciones", {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                })

            ])


            if (
                !respuestaReservas.ok ||
                !respuestaUsuarios.ok ||
                !respuestaInstalaciones.ok
            ) {
                throw new Error(
                    "No se pudieron obtener los datos."
                )
            }


            const datosReservas =
                await respuestaReservas.json()

            const datosUsuarios =
                await respuestaUsuarios.json()

            const datosInstalaciones =
                await respuestaInstalaciones.json()


            setReservas(datosReservas)
            setUsuarios(datosUsuarios)
            setInstalaciones(datosInstalaciones)


            // =====================================================
            // CARGAR RESERVAS A FAVOR DEL SOCIO
            // =====================================================

            if (
                esSocio &&
                usuarioActual?.idUsuario
            ) {

                const respuestaReservasAFavor =
                    await fetch(
                        `http://localhost:8080/api/reservas-a-favor/usuario/${usuarioActual.idUsuario}/disponibles`,
                        {
                            headers: {
                                Authorization:
                                    `Bearer ${token}`
                            }
                        }
                    )


                if (
                    respuestaReservasAFavor.ok
                ) {

                    const datosReservasAFavor =
                        await respuestaReservasAFavor.json()

                    setReservasAFavor(
                        datosReservasAFavor
                    )

                } else {

                    setReservasAFavor([])

                }

            } else {

                setReservasAFavor([])

            }

        } catch (error) {

            console.error(error)

            setError(
                "No se pudieron obtener los datos."
            )

        }
    }


    useEffect(() => {

        // eslint-disable-next-line react-hooks/set-state-in-effect
        cargarDatos()

    }, [])


    // =========================================================
    // ABRIR FORMULARIO NUEVA RESERVA
    // =========================================================

    const abrirFormulario = () => {

        setError("")
        setMensaje("")

        if (
            esSocio &&
            usuarioActual?.idUsuario
        ) {

            setNuevaReserva(prev => ({
                ...prev,
                idUsuario:
                usuarioActual.idUsuario
            }))

        } else {

            setNuevaReserva({
                idUsuario: "",
                idInstalacion: "",
                fechaReserva: "",
                horaInicio: "",
                duracionHoras: 1,
                montoTotal: ""
            })

        }

        setMostrarFormulario(true)
    }


    // =========================================================
    // CAMBIAR FORMULARIO NUEVA RESERVA
    // =========================================================

    const manejarCambio = (e) => {

        const { name, value } = e.target

        setNuevaReserva(prev => ({
            ...prev,
            [name]: value
        }))


        if (
            name === "idInstalacion" ||
            name === "duracionHoras"
        ) {

            const idInstalacion =
                name === "idInstalacion"
                    ? value
                    : nuevaReserva.idInstalacion


            const duracion =
                name === "duracionHoras"
                    ? Number(value)
                    : Number(
                        nuevaReserva.duracionHoras
                    )


            const instalacion =
                instalaciones.find(
                    i =>
                        i.idInstalacion ===
                        Number(idInstalacion)
                )


            if (
                instalacion &&
                duracion > 0
            ) {

                setNuevaReserva(prev => ({
                    ...prev,
                    [name]: value,
                    montoTotal:
                        Number(
                            instalacion.tarifaBase
                        ) * duracion
                }))

            }

        }

    }


    // =========================================================
    // CREAR RESERVA
    // =========================================================

    const crearReserva = async (e) => {

        e.preventDefault()

        setError("")
        setMensaje("")


        try {

            const datos = {

                usuario: {
                    idUsuario:
                        Number(
                            nuevaReserva.idUsuario
                        )
                },

                instalacion: {
                    idInstalacion:
                        Number(
                            nuevaReserva.idInstalacion
                        )
                },

                fechaReserva:
                nuevaReserva.fechaReserva,

                horaInicio:
                nuevaReserva.horaInicio,

                duracionHoras:
                    Number(
                        nuevaReserva.duracionHoras
                    ),

                montoTotal:
                    Number(
                        nuevaReserva.montoTotal
                    ),

                estado:
                    "PENDIENTE"

            }


            const respuesta =
                await fetch(
                    "http://localhost:8080/api/reservas",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json",

                            Authorization:
                                `Bearer ${token}`
                        },

                        body:
                            JSON.stringify(datos)
                    }
                )


            if (!respuesta.ok) {

                const mensaje =
                    await respuesta.text()

                throw new Error(
                    mensaje ||
                    "No se pudo crear la reserva."
                )

            }


            limpiarFormulario()

            await cargarDatos()

        } catch (error) {

            console.error(error)

            setError(error.message)

        }

    }


    // =========================================================
    // CANCELAR RESERVA
    // =========================================================

    const cancelarReserva = async (id) => {

        const reserva =
            reservas.find(
                r =>
                    r.idReserva === id
            )


        if (!reserva) {
            return
        }


        if (
            esSocio &&
            reserva.usuario?.idUsuario !==
            usuarioActual?.idUsuario
        ) {

            setError(
                "No podés cancelar una reserva de otro usuario."
            )

            return
        }


        if (
            !window.confirm(
                "¿Querés cancelar esta reserva?"
            )
        ) {
            return
        }


        try {

            setError("")
            setMensaje("")


            const respuesta =
                await fetch(
                    `http://localhost:8080/api/reservas/${id}/cancelar`,
                    {
                        method: "PUT",

                        headers: {
                            Authorization:
                                `Bearer ${token}`
                        }
                    }
                )


            if (!respuesta.ok) {

                const mensaje =
                    await respuesta.text()

                throw new Error(
                    mensaje ||
                    "No se pudo cancelar la reserva."
                )

            }


            setMensaje(
                "La reserva fue cancelada correctamente. Si correspondía, se generó una reserva a favor."
            )


            await cargarDatos()

        } catch (error) {

            console.error(error)

            setError(error.message)

        }

    }


    // =========================================================
    // ABRIR FORMULARIO RESERVA A FAVOR
    // =========================================================

    const abrirFormularioReservaAFavor = (
        reservaAFavor
    ) => {

        setError("")
        setMensaje("")

        setReservaAFavorSeleccionada(
            reservaAFavor
        )

        setNuevaReservaAFavor({
            idInstalacion: "",
            fechaReserva: "",
            horaInicio: "",
            duracionHoras: 1
        })

        setMostrarFormularioReservaAFavor(
            true
        )

    }


    // =========================================================
    // CAMBIAR FORMULARIO RESERVA A FAVOR
    // =========================================================

    const manejarCambioReservaAFavor = (e) => {

        const { name, value } = e.target

        setNuevaReservaAFavor(prev => ({
            ...prev,
            [name]: value
        }))

    }


    // =========================================================
    // USAR RESERVA A FAVOR
    // =========================================================

    const usarReservaAFavor = async (e) => {

        e.preventDefault()

        setError("")
        setMensaje("")


        if (!reservaAFavorSeleccionada) {

            setError(
                "No se seleccionó una reserva a favor."
            )

            return
        }


        try {

            const params =
                new URLSearchParams({

                    fecha:
                    nuevaReservaAFavor.fechaReserva,

                    hora:
                    nuevaReservaAFavor.horaInicio,

                    duracion:
                    nuevaReservaAFavor.duracionHoras,

                    instalacion:
                    nuevaReservaAFavor.idInstalacion

                })


            const respuesta =
                await fetch(
                    `http://localhost:8080/api/reservas-a-favor/${reservaAFavorSeleccionada.idReservaAFavor}/reprogramar?${params.toString()}`,
                    {
                        method: "POST",

                        headers: {
                            Authorization:
                                `Bearer ${token}`
                        }
                    }
                )


            if (!respuesta.ok) {

                const mensaje =
                    await respuesta.text()

                throw new Error(
                    mensaje ||
                    "No se pudo utilizar la reserva a favor."
                )

            }


            setMensaje(
                "La reserva a favor fue utilizada correctamente y se creó la nueva reserva."
            )


            setMostrarFormularioReservaAFavor(
                false
            )

            setReservaAFavorSeleccionada(
                null
            )

            setNuevaReservaAFavor({
                idInstalacion: "",
                fechaReserva: "",
                horaInicio: "",
                duracionHoras: 1
            })


            await cargarDatos()

        } catch (error) {

            console.error(error)

            setError(error.message)

        }

    }


    // =========================================================
    // LIMPIAR FORMULARIO
    // =========================================================

    const limpiarFormulario = () => {

        setNuevaReserva({
            idUsuario: "",
            idInstalacion: "",
            fechaReserva: "",
            horaInicio: "",
            duracionHoras: 1,
            montoTotal: ""
        })

        setMostrarFormulario(false)

    }


    // =========================================================
    // CERRAR FORMULARIO RESERVA A FAVOR
    // =========================================================

    const cerrarFormularioReservaAFavor = () => {

        setMostrarFormularioReservaAFavor(
            false
        )

        setReservaAFavorSeleccionada(
            null
        )

        setNuevaReservaAFavor({
            idInstalacion: "",
            fechaReserva: "",
            horaInicio: "",
            duracionHoras: 1
        })

    }


    // =========================================================
    // INSTALACIÓN SELECCIONADA
    // =========================================================

    const instalacionSeleccionada =
        instalaciones.find(
            instalacion =>
                instalacion.idInstalacion ===
                Number(
                    nuevaReserva.idInstalacion
                )
        )


    // =========================================================
    // INSTALACIÓN SELECCIONADA PARA RESERVA A FAVOR
    // =========================================================

    const instalacionReservaAFavor =
        instalaciones.find(
            instalacion =>
                instalacion.idInstalacion ===
                Number(
                    nuevaReservaAFavor.idInstalacion
                )
        )


    return (
        <section className="usuarios">

            {/* ================================================= */}
            {/* TÍTULO */}
            {/* ================================================= */}

            <div className="seccion-titulo">

                <div>

                    <h2>Reservas</h2>

                    <p>
                        Gestión de reservas de instalaciones deportivas.
                    </p>

                </div>


                <button
                    className="boton-principal"
                    onClick={abrirFormulario}
                >
                    Nueva reserva
                </button>

            </div>


            {/* ================================================= */}
            {/* MENSAJE */}
            {/* ================================================= */}

            {mensaje && (
                <div className="success-message">
                    {mensaje}
                </div>
            )}


            {/* ================================================= */}
            {/* ERROR */}
            {/* ================================================= */}

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}


            {/* ================================================= */}
            {/* RESERVAS A FAVOR */}
            {/* ================================================= */}

            {esSocio &&
                reservasAFavor.length > 0 && (

                    <section className="seccion-reserva-favor">

                        <div className="seccion-titulo">

                            <div>

                                <h2>
                                    Reservas a favor
                                </h2>

                                <p>
                                    Podés utilizar una reserva a favor vigente para realizar una nueva reserva.
                                </p>

                            </div>

                        </div>


                        <div className="tarjetas">

                            {reservasAFavor.map(
                                reservaAFavor => (

                                    <div
                                        className="tarjeta"
                                        key={
                                            reservaAFavor.idReservaAFavor
                                        }
                                    >

                                        <h3>
                                            Reserva a favor
                                        </h3>


                                        <p>
                                            <strong>
                                                Monto:
                                            </strong>{" "}
                                            $
                                            {
                                                reservaAFavor.montoAcreditado
                                            }
                                        </p>


                                        <p>
                                            <strong>
                                                Generada:
                                            </strong>{" "}
                                            {
                                                reservaAFavor.fechaGeneracion
                                            }
                                        </p>


                                        <p>
                                            <strong>
                                                Vence:
                                            </strong>{" "}
                                            {
                                                reservaAFavor.fechaVencimiento
                                            }
                                        </p>


                                        <button
                                            className="boton-principal"
                                            onClick={() =>
                                                abrirFormularioReservaAFavor(
                                                    reservaAFavor
                                                )
                                            }
                                        >
                                            Usar reserva a favor
                                        </button>

                                    </div>

                                )
                            )}

                        </div>

                    </section>

                )}


            {/* ================================================= */}
            {/* FORMULARIO NUEVA RESERVA */}
            {/* ================================================= */}

            {mostrarFormulario && (

                <form
                    className="formulario-usuario"
                    onSubmit={crearReserva}
                >

                    <h3>
                        Nueva reserva
                    </h3>


                    <div className="formulario-grid">

                        <div>

                            <label>
                                Usuario
                            </label>


                            {esSocio ? (

                                <input
                                    type="text"
                                    value={
                                        `${usuarioActual?.nombre || ""} ${usuarioActual?.apellido || ""}`
                                    }
                                    readOnly
                                />

                            ) : (

                                <select
                                    name="idUsuario"
                                    value={
                                        nuevaReserva.idUsuario
                                    }
                                    onChange={
                                        manejarCambio
                                    }
                                    required
                                >

                                    <option value="">
                                        Seleccionar usuario
                                    </option>


                                    {usuarios.map(
                                        usuario => (

                                            <option
                                                key={
                                                    usuario.idUsuario
                                                }
                                                value={
                                                    usuario.idUsuario
                                                }
                                            >

                                                {
                                                    usuario.nombre
                                                }{" "}

                                                {
                                                    usuario.apellido
                                                }

                                            </option>

                                        )
                                    )}

                                </select>

                            )}

                        </div>


                        <div>

                            <label>
                                Instalación
                            </label>


                            <select
                                name="idInstalacion"
                                value={
                                    nuevaReserva.idInstalacion
                                }
                                onChange={
                                    manejarCambio
                                }
                                required
                            >

                                <option value="">
                                    Seleccionar instalación
                                </option>


                                {instalaciones
                                    .filter(
                                        instalacion =>
                                            instalacion.estado ===
                                            "DISPONIBLE"
                                    )
                                    .map(
                                        instalacion => (

                                            <option
                                                key={
                                                    instalacion.idInstalacion
                                                }
                                                value={
                                                    instalacion.idInstalacion
                                                }
                                            >

                                                {
                                                    instalacion.nombre
                                                }

                                            </option>

                                        )
                                    )}

                            </select>


                            {instalacionSeleccionada && (

                                <small>

                                    Tarifa: $
                                    {
                                        instalacionSeleccionada.tarifaBase
                                    }{" "}
                                    por hora

                                </small>

                            )}

                        </div>


                        <div>

                            <label>
                                Fecha
                            </label>


                            <input
                                type="date"
                                name="fechaReserva"
                                value={
                                    nuevaReserva.fechaReserva
                                }
                                onChange={
                                    manejarCambio
                                }
                                required
                            />

                        </div>


                        <div>

                            <label>
                                Hora de inicio
                            </label>


                            <input
                                type="time"
                                name="horaInicio"
                                value={
                                    nuevaReserva.horaInicio
                                }
                                onChange={
                                    manejarCambio
                                }
                                required
                            />

                        </div>


                        <div>

                            <label>
                                Duración (horas)
                            </label>


                            <input
                                type="number"
                                name="duracionHoras"
                                min="1"
                                value={
                                    nuevaReserva.duracionHoras
                                }
                                onChange={
                                    manejarCambio
                                }
                                required
                            />

                        </div>


                        <div>

                            <label>
                                Monto total
                            </label>


                            <input
                                type="number"
                                name="montoTotal"
                                value={
                                    nuevaReserva.montoTotal
                                }
                                readOnly
                            />

                        </div>

                    </div>


                    <div className="formulario-botones">

                        <button
                            type="button"
                            className="boton-secundario"
                            onClick={
                                limpiarFormulario
                            }
                        >
                            Cancelar
                        </button>


                        <button
                            type="submit"
                            className="boton-principal"
                        >
                            Crear reserva
                        </button>

                    </div>

                </form>

            )}


            {/* ================================================= */}
            {/* FORMULARIO USAR RESERVA A FAVOR */}
            {/* ================================================= */}

            {mostrarFormularioReservaAFavor && (

                <form
                    className="formulario-usuario"
                    onSubmit={
                        usarReservaAFavor
                    }
                >

                    <h3>
                        Usar reserva a favor
                    </h3>


                    <p>

                        Monto disponible:{" "}

                        <strong>

                            $
                            {
                                reservaAFavorSeleccionada?.montoAcreditado
                            }

                        </strong>

                    </p>


                    <div className="formulario-grid">

                        <div>

                            <label>
                                Instalación
                            </label>


                            <select
                                name="idInstalacion"
                                value={
                                    nuevaReservaAFavor.idInstalacion
                                }
                                onChange={
                                    manejarCambioReservaAFavor
                                }
                                required
                            >

                                <option value="">
                                    Seleccionar instalación
                                </option>


                                {instalaciones
                                    .filter(
                                        instalacion =>
                                            instalacion.estado ===
                                            "DISPONIBLE"
                                    )
                                    .map(
                                        instalacion => (

                                            <option
                                                key={
                                                    instalacion.idInstalacion
                                                }
                                                value={
                                                    instalacion.idInstalacion
                                                }
                                            >

                                                {
                                                    instalacion.nombre
                                                }

                                            </option>

                                        )
                                    )}

                            </select>


                            {instalacionReservaAFavor && (

                                <small>

                                    Tarifa: $
                                    {
                                        instalacionReservaAFavor.tarifaBase
                                    }{" "}
                                    por hora

                                </small>

                            )}

                        </div>


                        <div>

                            <label>
                                Fecha
                            </label>


                            <input
                                type="date"
                                name="fechaReserva"
                                value={
                                    nuevaReservaAFavor.fechaReserva
                                }
                                onChange={
                                    manejarCambioReservaAFavor
                                }
                                required
                            />

                        </div>


                        <div>

                            <label>
                                Hora de inicio
                            </label>


                            <input
                                type="time"
                                name="horaInicio"
                                value={
                                    nuevaReservaAFavor.horaInicio
                                }
                                onChange={
                                    manejarCambioReservaAFavor
                                }
                                required
                            />

                        </div>


                        <div>

                            <label>
                                Duración (horas)
                            </label>


                            <input
                                type="number"
                                name="duracionHoras"
                                min="1"
                                max="24"
                                value={
                                    nuevaReservaAFavor.duracionHoras
                                }
                                onChange={
                                    manejarCambioReservaAFavor
                                }
                                required
                            />

                        </div>

                    </div>


                    <div className="formulario-botones">

                        <button
                            type="button"
                            className="boton-secundario"
                            onClick={
                                cerrarFormularioReservaAFavor
                            }
                        >
                            Cancelar
                        </button>


                        <button
                            type="submit"
                            className="boton-principal"
                        >
                            Usar reserva a favor
                        </button>

                    </div>

                </form>

            )}


            {/* ================================================= */}
            {/* TABLA DE RESERVAS */}
            {/* ================================================= */}

            <div className="tabla-contenedor">

                <table>

                    <thead>

                    <tr>

                        <th>ID</th>
                        <th>Usuario</th>
                        <th>Instalación</th>
                        <th>Fecha</th>
                        <th>Hora</th>
                        <th>Duración</th>
                        <th>Monto</th>
                        <th>Estado</th>
                        <th>Acciones</th>

                    </tr>

                    </thead>


                    <tbody>

                    {reservas.map(
                        reserva => (

                            <tr
                                key={
                                    reserva.idReserva
                                }
                            >

                                <td>
                                    {
                                        reserva.idReserva
                                    }
                                </td>


                                <td>

                                    {
                                        reserva.usuario?.nombre
                                    }{" "}

                                    {
                                        reserva.usuario?.apellido
                                    }

                                </td>


                                <td>

                                    {
                                        reserva.instalacion?.nombre
                                    }

                                </td>


                                <td>

                                    {
                                        reserva.fechaReserva
                                    }

                                </td>


                                <td>

                                    {
                                        reserva.horaInicio
                                    }

                                </td>


                                <td>

                                    {
                                        reserva.duracionHoras
                                    }{" "}
                                    hs

                                </td>


                                <td>

                                    $
                                    {
                                        reserva.montoTotal
                                    }

                                </td>


                                <td>

                                    {
                                        reserva.estado
                                    }

                                </td>


                                <td>

                                    {reserva.estado !==
                                        "CANCELADA" && (

                                            <button
                                                className="boton-eliminar"
                                                onClick={() =>
                                                    cancelarReserva(
                                                        reserva.idReserva
                                                    )
                                                }
                                            >
                                                Cancelar
                                            </button>

                                        )}

                                </td>

                            </tr>

                        )
                    )}

                    </tbody>

                </table>

            </div>

        </section>
    )
}

export default Reservas