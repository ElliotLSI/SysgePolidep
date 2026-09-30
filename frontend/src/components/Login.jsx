import { useState } from "react"

function Login() {
  const [usuario, setUsuario] = useState("")
  const [contrasena, setContrasena] = useState("")
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState("")

  const manejarLogin = async (e) => {
    e.preventDefault()

    setError("")
    setCargando(true)

    try {
      const respuesta = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          nombreUsuario: usuario,
          password: contrasena
        })
      })

      const datos = await respuesta.json()

      if (!respuesta.ok) {
        throw new Error(datos.message || datos.mensaje || "Error al iniciar sesión")
      }

      // Guardar el token y los datos del usuario
      sessionStorage.setItem("token", datos.token)
      sessionStorage.setItem("usuario", JSON.stringify(datos))

      console.log("Inicio de sesión exitoso")

      alert(`¡Bienvenido, ${datos.nombre}!`)

      window.location.reload()

    } catch (error) {
      console.error("Error al iniciar sesión:", error)
      setError(error.message || "No se pudo conectar con el servidor")
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>SysGe PoliDep</h1>
        <p>Iniciar sesión</p>

        <form onSubmit={manejarLogin}>
          <div className="form-group">
            <label>Usuario</label>
            <input
              type="text"
              placeholder="Ingrese su usuario"
              value={usuario}
              onChange={(e) => setUsuario(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Contraseña</label>
            <input
              type="password"
              placeholder="Ingrese su contraseña"
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
              required
            />
          </div>

          {error && <p className="error-message">{error}</p>}

          <button type="submit" disabled={cargando}>
            {cargando ? "Ingresando..." : "Ingresar"}
          </button>
        </form>
      </div>
    </div>
  )
}

export default Login