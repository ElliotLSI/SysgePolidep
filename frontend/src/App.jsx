import Login from "./components/Login"
import Dashboard from "./components/Dashboard"

function App() {

  const usuario = sessionStorage.getItem("usuario")

  if (usuario) {
    return <Dashboard />
  }

  return <Login />
}

export default App