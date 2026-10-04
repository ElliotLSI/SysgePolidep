function Reportes() {

    const reportes = [
        {
            nombre: "Usuarios",
            descripcion: "Listado general de usuarios registrados en el sistema.",
            ruta: "usuarios"
        },
        {
            nombre: "Socios y Membresías",
            descripcion: "Información de socios y sus membresías registradas.",
            ruta: "socios-membresias"
        },
        {
            nombre: "Reservas",
            descripcion: "Listado de reservas realizadas en las instalaciones.",
            ruta: "reservas"
        },
        {
            nombre: "Pagos",
            descripcion: "Listado de pagos registrados en el sistema.",
            ruta: "pagos"
        },
        {
            nombre: "Instalaciones",
            descripcion: "Listado de instalaciones, capacidad, tarifas y estado.",
            ruta: "instalaciones"
        }
    ]

    const generarReporte = async (ruta) => {

        try {

            const respuesta = await fetch(
                `http://localhost:8080/api/reportes/${ruta}`,
                {
                    headers: {
                        Authorization:
                            "Bearer " + sessionStorage.getItem("token")
                    }
                }
            )

            if (!respuesta.ok) {
                throw new Error(
                    "Error HTTP: " + respuesta.status
                )
            }

            const blob = await respuesta.blob()

            const url = URL.createObjectURL(blob)

            window.open(url, "_blank")

        } catch (error) {

            console.error(error)

            alert("No se pudo generar el reporte.")
        }
    }

    return (
        <section>

            <div className="bienvenida">
                <h2>Reportes</h2>

                <p>
                    Seleccioná un reporte para generar su archivo PDF.
                </p>
            </div>

            <section className="tarjetas">

                {reportes.map(reporte => (

                    <div
                        className="tarjeta"
                        key={reporte.ruta}
                        onClick={() =>
                            generarReporte(reporte.ruta)
                        }
                        role="button"
                        tabIndex={0}
                        onKeyDown={e => {
                            if (e.key === "Enter") {
                                generarReporte(reporte.ruta)
                            }
                        }}
                    >

                        <h3>
                            {reporte.nombre}
                        </h3>

                        <p>
                            {reporte.descripcion}
                        </p>

                    </div>

                ))}

            </section>

        </section>
    )
}

export default Reportes