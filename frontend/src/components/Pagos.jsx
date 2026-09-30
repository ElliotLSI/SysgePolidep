import { useEffect, useState } from "react"

function Pagos() {

    const [pagos, setPagos] = useState([])
    const [reservas, setReservas] = useState([])
    const [membresias, setMembresias] = useState([])

    const [mostrarFormulario, setMostrarFormulario] = useState(false)

    const [tipoPago, setTipoPago] = useState("RESERVA")

    const [nuevoPago, setNuevoPago] = useState({
        idReserva: "",
        idMembresia: "",
        montoTotal: "",
        medioPago: "EFECTIVO",
        estado: "APROBADO"
    })

    const [error, setError] = useState("")

    const token = sessionStorage.getItem("token")

    const usuarioActual = JSON.parse(
        sessionStorage.getItem("usuario")
    )

    const roles = usuarioActual?.roles ?? []

    const esAdministrador =
        roles.includes("ADMINISTRADOR")

    const cargarDatos = async () => {

        try {

            const [
                respuestaPagos,
                respuestaReservas,
                respuestaMembresias
            ] = await Promise.all([

                fetch(
                    "http://localhost:8080/api/pagos",
                    {
                        headers: {
                            Authorization: `Bearer ${token}`
                        }
                    }
                ),

                fetch(
                    "http://localhost:8080/api/reservas",
                    {
                        headers: {
                            Authorization: `Bearer ${token}`
                        }
                    }
                ),

                fetch(
                    "http://localhost:8080/api/membresias",
                    {
                        headers: {
                            Authorization: `Bearer ${token}`
                        }
                    }
                )

            ])

            if (
                !respuestaPagos.ok ||
                !respuestaReservas.ok ||
                !respuestaMembresias.ok
            ) {
                throw new Error(
                    "No se pudieron obtener los datos."
                )
            }

            const datosPagos =
                await respuestaPagos.json()

            const datosReservas =
                await respuestaReservas.json()

            const datosMembresias =
                await respuestaMembresias.json()

            setPagos(datosPagos)
            setReservas(datosReservas)
            setMembresias(datosMembresias)

        } catch (error) {

            console.error(error)

            setError(
                "No se pudieron obtener los datos."
            )
        }
    }

    useEffect(() => {
        cargarDatos()
    }, [])

    const manejarCambio = (e) => {

        const { name, value } = e.target

        setNuevoPago(prev => ({
            ...prev,
            [name]: value
        }))

        if (name === "idReserva") {

            const reserva =
                reservas.find(
                    r =>
                        r.idReserva ===
                        Number(value)
                )

            if (reserva) {

                setNuevoPago(prev => ({
                    ...prev,
                    idReserva: value,
                    montoTotal:
                    reserva.montoTotal
                }))
            }
        }

        if (name === "idMembresia") {

            const membresia =
                membresias.find(
                    m =>
                        m.idMembresia ===
                        Number(value)
                )

            if (membresia) {

                setNuevoPago(prev => ({
                    ...prev,
                    idMembresia: value,
                    montoTotal:
                        membresia.categoria?.costo || ""
                }))
            }
        }
    }

    const cambiarTipoPago = (tipo) => {

        setTipoPago(tipo)

        setNuevoPago({
            idReserva: "",
            idMembresia: "",
            montoTotal: "",
            medioPago: "EFECTIVO",
            estado: "APROBADO"
        })

        setError("")
    }

    const crearPago = async (e) => {

        e.preventDefault()

        setError("")

        try {

            const datos = {
                montoTotal:
                    Number(nuevoPago.montoTotal),

                medioPago:
                nuevoPago.medioPago,

                estado: "APROBADO"
            }

            if (tipoPago === "RESERVA") {

                datos.reserva = {
                    idReserva:
                        Number(nuevoPago.idReserva)
                }

            } else {

                datos.idMembresia =
                    Number(nuevoPago.idMembresia)
            }

            const respuesta = await fetch(
                "http://localhost:8080/api/pagos",
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
                    "No se pudo registrar el pago."
                )
            }

            limpiarFormulario()

            await cargarDatos()

        } catch (error) {

            console.error(error)

            setError(error.message)
        }
    }

    const eliminarPago = async (id) => {

        if (!esAdministrador) {
            return
        }

        if (
            !window.confirm(
                "¿Querés eliminar este pago?"
            )
        ) {
            return
        }

        try {

            const respuesta = await fetch(
                `http://localhost:8080/api/pagos/${id}`,
                {
                    method: "DELETE",

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
                    "No se pudo eliminar el pago."
                )
            }

            await cargarDatos()

        } catch (error) {

            console.error(error)

            setError(error.message)
        }
    }

    const limpiarFormulario = () => {

        setNuevoPago({
            idReserva: "",
            idMembresia: "",
            montoTotal: "",
            medioPago: "EFECTIVO",
            estado: "APROBADO"
        })

        setTipoPago("RESERVA")

        setMostrarFormulario(false)

        setError("")
    }

    const obtenerNombreUsuarioReserva = (
        reserva
    ) => {

        if (!reserva?.usuario) {
            return "Sin usuario"
        }

        return `${reserva.usuario.nombre || ""} ${
            reserva.usuario.apellido || ""
        }`
    }

    const obtenerNombreSocioMembresia = (
        membresia
    ) => {

        if (!membresia?.socio?.usuario) {
            return "Sin socio"
        }

        return `${membresia.socio.usuario.nombre || ""} ${
            membresia.socio.usuario.apellido || ""
        }`
    }

    return (

        <section className="usuarios">

            <div className="seccion-titulo">

                <div>

                    <h2>
                        Pagos
                    </h2>

                    <p>
                        Gestión de pagos de reservas y membresías.
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
                    Nuevo pago
                </button>

            </div>

            {error && (

                <div className="error-message">
                    {error}
                </div>

            )}

            {mostrarFormulario && (

                <form
                    className="formulario-usuario"
                    onSubmit={crearPago}
                >

                    <h3>
                        Registrar pago
                    </h3>

                    <div className="formulario-botones">

                        <button
                            type="button"
                            className={
                                tipoPago === "RESERVA"
                                    ? "boton-principal"
                                    : "boton-secundario"
                            }
                            onClick={() =>
                                cambiarTipoPago(
                                    "RESERVA"
                                )
                            }
                        >
                            Pago de reserva
                        </button>

                        <button
                            type="button"
                            className={
                                tipoPago === "MEMBRESIA"
                                    ? "boton-principal"
                                    : "boton-secundario"
                            }
                            onClick={() =>
                                cambiarTipoPago(
                                    "MEMBRESIA"
                                )
                            }
                        >
                            Pago de membresía
                        </button>

                    </div>

                    <div className="formulario-grid">

                        {tipoPago === "RESERVA" && (

                            <div>

                                <label>
                                    Reserva
                                </label>

                                <select
                                    name="idReserva"
                                    value={
                                        nuevoPago.idReserva
                                    }
                                    onChange={
                                        manejarCambio
                                    }
                                    required
                                >

                                    <option value="">
                                        Seleccionar reserva
                                    </option>

                                    {reservas
                                        .filter(
                                            reserva =>
                                                reserva.estado !==
                                                "CANCELADA"
                                        )
                                        .map(
                                            reserva => (

                                                <option
                                                    key={
                                                        reserva.idReserva
                                                    }
                                                    value={
                                                        reserva.idReserva
                                                    }
                                                >
                                                    Reserva #
                                                    {
                                                        reserva.idReserva
                                                    }
                                                    {" - "}
                                                    {
                                                        obtenerNombreUsuarioReserva(
                                                            reserva
                                                        )
                                                    }
                                                    {" - $"}
                                                    {
                                                        reserva.montoTotal
                                                    }
                                                </option>

                                            )
                                        )}

                                </select>

                            </div>

                        )}

                        {tipoPago === "MEMBRESIA" && (

                            <div>

                                <label>
                                    Membresía
                                </label>

                                <select
                                    name="idMembresia"
                                    value={
                                        nuevoPago.idMembresia
                                    }
                                    onChange={
                                        manejarCambio
                                    }
                                    required
                                >

                                    <option value="">
                                        Seleccionar membresía
                                    </option>

                                    {membresias.map(
                                        membresia => (

                                            <option
                                                key={
                                                    membresia.idMembresia
                                                }
                                                value={
                                                    membresia.idMembresia
                                                }
                                            >
                                                Membresía #
                                                {
                                                    membresia.idMembresia
                                                }
                                                {" - "}
                                                {
                                                    obtenerNombreSocioMembresia(
                                                        membresia
                                                    )
                                                }
                                                {" - $"}
                                                {
                                                    membresia
                                                        .categoria
                                                        ?.costo
                                                }
                                            </option>

                                        )
                                    )}

                                </select>

                            </div>

                        )}

                        <div>

                            <label>
                                Monto total
                            </label>

                            <input
                                type="number"
                                name="montoTotal"
                                min="0"
                                step="0.01"
                                value={
                                    nuevoPago.montoTotal
                                }
                                readOnly
                                required
                            />

                        </div>

                        <div>

                            <label>
                                Medio de pago
                            </label>

                            <select
                                name="medioPago"
                                value={
                                    nuevoPago.medioPago
                                }
                                onChange={
                                    manejarCambio
                                }
                                required
                            >

                                <option value="EFECTIVO">
                                    Efectivo
                                </option>

                                <option value="MERCADOPAGO">
                                    Mercado Pago
                                </option>

                                <option value="TRANSFERENCIA">
                                    Transferencia
                                </option>

                                <option value="TARJETA">
                                    Tarjeta
                                </option>

                            </select>

                        </div>

                        <div>

                            <label>
                                Estado
                            </label>

                            <input
                                type="text"
                                value="Aprobado"
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
                            Registrar pago
                        </button>

                    </div>

                </form>

            )}

            <div className="tabla-contenedor">

                <table>

                    <thead>

                    <tr>

                        <th>ID</th>
                        <th>Tipo</th>
                        <th>Referencia</th>
                        <th>Fecha</th>
                        <th>Monto</th>
                        <th>Medio</th>
                        <th>Estado</th>

                        {esAdministrador && (
                            <th>Acciones</th>
                        )}

                    </tr>

                    </thead>

                    <tbody>

                    {pagos.map(
                        pago => (

                            <tr
                                key={
                                    pago.idPago
                                }
                            >

                                <td>
                                    {
                                        pago.idPago
                                    }
                                </td>

                                <td>
                                    {pago.reserva
                                        ? "Reserva"
                                        : "Membresía"}
                                </td>

                                <td>
                                    {pago.reserva
                                        ? `Reserva #${pago.reserva.idReserva}`
                                        : `Membresía #${pago.idMembresia}`}
                                </td>

                                <td>
                                    {pago.fecha
                                        ? new Date(
                                            pago.fecha
                                        ).toLocaleString(
                                            "es-AR"
                                        )
                                        : "-"}
                                </td>

                                <td>
                                    $
                                    {
                                        pago.montoTotal
                                    }
                                </td>

                                <td>
                                    {
                                        pago.medioPago
                                    }
                                </td>

                                <td>
                                    {
                                        pago.estado
                                    }
                                </td>

                                {esAdministrador && (

                                    <td>

                                        <button
                                            className="boton-eliminar"
                                            onClick={() =>
                                                eliminarPago(
                                                    pago.idPago
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

export default Pagos