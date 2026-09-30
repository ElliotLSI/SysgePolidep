import { useEffect, useState } from "react"

function Reservas() {

    const [reservas, setReservas] = useState([])
    const [usuarios, setUsuarios] = useState([])
    const [instalaciones, setInstalaciones] = useState([])

    const [mostrarFormulario, setMostrarFormulario] = useState(false)

    const [nuevaReserva, setNuevaReserva] = useState({
        idUsuario: "",
        idInstalacion: "",
        fechaReserva: "",
        horaInicio: "",
        duracionHoras: 1,
        montoTotal: ""
    })

    const [error, setError] = useState("")

    const token = sessionStorage.getItem("token")


    // =========================
    // CARGAR DATOS
    // =========================

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
                throw new Error("No se pudieron obtener los datos.")
            }


            const datosReservas = await respuestaReservas.json()
            const datosUsuarios = await respuestaUsuarios.json()
            const datosInstalaciones = await respuestaInstalaciones.json()


            setReservas(datosReservas)
            setUsuarios(datosUsuarios)
            setInstalaciones(datosInstalaciones)


        } catch (error) {

            console.error(error)

            setError("No se pudieron obtener los datos.")

        }
    }


    useEffect(() => {
        cargarDatos()
    }, [])


    // =========================
    // CAMBIAR FORMULARIO
    // =========================

    const manejarCambio = (e) => {

        const { name, value } = e.target

        setNuevaReserva({
            ...nuevaReserva,
            [name]: value
        })


        // Calcular monto automáticamente
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
                    : Number(nuevaReserva.duracionHoras)


            const instalacion =
                instalaciones.find(
                    i => i.idInstalacion === Number(idInstalacion)
                )


            if (instalacion && duracion > 0) {

                setNuevaReserva(prev => ({
                    ...prev,
                    [name]: value,
                    montoTotal:
                        Number(instalacion.tarifaBase) * duracion
                }))

            }

        }

    }


    // =========================
    // CREAR RESERVA
    // =========================

    const crearReserva = async (e) => {

        e.preventDefault()

        setError("")


        try {

            const datos = {

                usuario: {
                    idUsuario:
                        Number(nuevaReserva.idUsuario)
                },

                instalacion: {
                    idInstalacion:
                        Number(nuevaReserva.idInstalacion)
                },

                fechaReserva:
                    nuevaReserva.fechaReserva,

                horaInicio:
                    nuevaReserva.horaInicio,

                duracionHoras:
                    Number(nuevaReserva.duracionHoras),

                montoTotal:
                    Number(nuevaReserva.montoTotal),

                estado:
                    "PENDIENTE"

            }


            const respuesta = await fetch(
                "http://localhost:8080/api/reservas",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`
                    },

                    body: JSON.stringify(datos)
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


    // =========================
    // CANCELAR RESERVA
    // =========================

    const cancelarReserva = async (id) => {

        if (
            !window.confirm(
                "¿Querés cancelar esta reserva?"
            )
        ) {
            return
        }


        try {

            const respuesta = await fetch(
                `http://localhost:8080/api/reservas/${id}/cancelar`,
                {
                    method: "PUT",

                    headers: {
                        Authorization: `Bearer ${token}`
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


            await cargarDatos()


        } catch (error) {

            console.error(error)

            setError(error.message)

        }

    }


    // =========================
    // LIMPIAR FORMULARIO
    // =========================

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


    // =========================
    // INSTALACIÓN SELECCIONADA
    // =========================

    const instalacionSeleccionada =
        instalaciones.find(
            instalacion =>
                instalacion.idInstalacion ===
                Number(nuevaReserva.idInstalacion)
        )


    return (

        <section className="usuarios">

            {/* ========================= */}
            {/* ENCABEZADO */}
            {/* ========================= */}

            <div className="seccion-titulo">

                <div>

                    <h2>
                        Reservas
                    </h2>

                    <p>
                        Gestión de reservas de instalaciones deportivas.
                    </p>

                </div>


                <button
                    className="boton-principal"
                    onClick={() => {

                        setError("")

                        setMostrarFormulario(
                            !mostrarFormulario
                        )

                    }}
                >
                    Nueva reserva
                </button>

            </div>


            {/* ========================= */}
            {/* ERROR */}
            {/* ========================= */}

            {error && (

                <div className="error-message">
                    {error}
                </div>

            )}


            {/* ========================= */}
            {/* FORMULARIO */}
            {/* ========================= */}

            {mostrarFormulario && (

                <form
                    className="formulario-usuario"
                    onSubmit={crearReserva}
                >

                    <h3>
                        Nueva reserva
                    </h3>


                    <div className="formulario-grid">

                        {/* USUARIO */}

                        <div>

                            <label>
                                Usuario
                            </label>

                            <select
                                name="idUsuario"
                                value={nuevaReserva.idUsuario}
                                onChange={manejarCambio}
                                required
                            >

                                <option value="">
                                    Seleccionar usuario
                                </option>


                                {usuarios.map(usuario => (

                                    <option
                                        key={usuario.idUsuario}
                                        value={usuario.idUsuario}
                                    >
                                        {usuario.nombre}{" "}
                                        {usuario.apellido}
                                    </option>

                                ))}

                            </select>

                        </div>


                        {/* INSTALACIÓN */}

                        <div>

                            <label>
                                Instalación
                            </label>

                            <select
                                name="idInstalacion"
                                value={nuevaReserva.idInstalacion}
                                onChange={manejarCambio}
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
                                    .map(instalacion => (

                                        <option
                                            key={
                                                instalacion.idInstalacion
                                            }
                                            value={
                                                instalacion.idInstalacion
                                            }
                                        >
                                            {instalacion.nombre}
                                        </option>

                                    ))}

                            </select>


                            {instalacionSeleccionada && (

                                <small>
                                    Tarifa: $
                                    {instalacionSeleccionada.tarifaBase}
                                    {" "}por hora
                                </small>

                            )}

                        </div>


                        {/* FECHA */}

                        <div>

                            <label>
                                Fecha
                            </label>

                            <input
                                type="date"
                                name="fechaReserva"
                                value={nuevaReserva.fechaReserva}
                                onChange={manejarCambio}
                                required
                            />

                        </div>


                        {/* HORA */}

                        <div>

                            <label>
                                Hora de inicio
                            </label>

                            <input
                                type="time"
                                name="horaInicio"
                                value={nuevaReserva.horaInicio}
                                onChange={manejarCambio}
                                required
                            />

                        </div>


                        {/* DURACIÓN */}

                        <div>

                            <label>
                                Duración (horas)
                            </label>

                            <input
                                type="number"
                                name="duracionHoras"
                                min="1"
                                value={nuevaReserva.duracionHoras}
                                onChange={manejarCambio}
                                required
                            />

                        </div>


                        {/* MONTO */}

                        <div>

                            <label>
                                Monto total
                            </label>

                            <input
                                type="number"
                                name="montoTotal"
                                value={nuevaReserva.montoTotal}
                                readOnly
                            />

                        </div>

                    </div>


                    {/* BOTONES */}

                    <div className="formulario-botones">

                        <button
                            type="button"
                            className="boton-secundario"
                            onClick={limpiarFormulario}
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


            {/* ========================= */}
            {/* TABLA */}
            {/* ========================= */}

            <div className="tabla-contenedor">

                <table>

                    <thead>

                        <tr>

                            <th>
                                ID
                            </th>

                            <th>
                                Usuario
                            </th>

                            <th>
                                Instalación
                            </th>

                            <th>
                                Fecha
                            </th>

                            <th>
                                Hora
                            </th>

                            <th>
                                Duración
                            </th>

                            <th>
                                Monto
                            </th>

                            <th>
                                Estado
                            </th>

                            <th>
                                Acciones
                            </th>

                        </tr>

                    </thead>


                    <tbody>

                        {reservas.map(reserva => (

                            <tr
                                key={
                                    reserva.idReserva
                                }
                            >

                                <td>
                                    {reserva.idReserva}
                                </td>


                                <td>

                                    {reserva.usuario?.nombre}{" "}
                                    {reserva.usuario?.apellido}

                                </td>


                                <td>

                                    {reserva.instalacion?.nombre}

                                </td>


                                <td>

                                    {reserva.fechaReserva}

                                </td>


                                <td>

                                    {reserva.horaInicio}

                                </td>


                                <td>

                                    {reserva.duracionHoras} hs

                                </td>


                                <td>

                                    ${reserva.montoTotal}

                                </td>


                                <td>

                                    {reserva.estado}

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

                        ))}

                    </tbody>

                </table>

            </div>

        </section>
    )
}

export default Reservas