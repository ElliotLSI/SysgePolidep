SysGe PoliDep

Sistema de gestión integral para un polideportivo, desarrollado como proyecto académico para la carrera de **Licenciatura en Sistemas de Información** de la **Universidad Nacional de Santiago del Estero (UNSE)**.

El sistema permite centralizar la gestión de usuarios, socios, membresías, instalaciones, reservas y pagos, aplicando diferentes permisos según el rol de cada usuario.

## Tecnologías

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* Lombok
* Maven
* MySQL

### Frontend

* React
* Vite
* JavaScript
* CSS

### Herramientas

* Git
* GitHub
* Postman
* MySQL Workbench
* IntelliJ IDEA
* Visual Studio Code

## Arquitectura

El proyecto está dividido en dos aplicaciones principales:

```text
SysGe PoliDep
│
├── Backend
│   ├── Entidades
│   ├── Repositories
│   ├── Services
│   ├── Controllers
│   ├── DTOs
│   └── Seguridad
│
└── Frontend
    ├── Componentes React
    ├── Formularios
    ├── Tablas
    ├── Gestión de sesión
    └── Interfaz de usuario
```

El **frontend** consume la API REST proporcionada por el **backend**, mientras que el backend se comunica con la base de datos MySQL.

## Funcionalidades

### Usuarios

El sistema permite:

* Registrar usuarios.
* Consultar usuarios.
* Modificar usuarios.
* Eliminar usuarios.
* Gestionar el estado del usuario.
* Gestionar pertenencia.
* Autenticar usuarios mediante usuario y contraseña.

### Socios y membresías

Permite:

* Dar de alta socios.
* Consultar socios.
* Crear membresías.
* Renovar membresías.
* Consultar membresías.
* Gestionar categorías.
* Definir costos.
* Definir porcentajes de descuento.
* Definir beneficios.
* Controlar vigencia de las membresías.

### Categorías

Las categorías permiten definir diferentes tipos de membresía.

Cada categoría puede contener:

* Nombre.
* Costo.
* Porcentaje de descuento.
* Beneficios.
* Duración.
* Estado de actividad.

La gestión de categorías corresponde al rol **ADMINISTRADOR**.

### Instalaciones

Permite administrar los espacios disponibles del polideportivo.

Entre ellas pueden encontrarse:

* Canchas.
* Piletas.
* SUM.
* Otros espacios deportivos.

Las instalaciones pueden tener los siguientes estados:

```text
DISPONIBLE
MANTENIMIENTO
CLAUSURADA
```

Las instalaciones que se encuentran en mantenimiento o clausuradas no pueden ser utilizadas para nuevas reservas.

### Reservas

El módulo de reservas permite:

* Crear reservas.
* Consultar reservas.
* Cancelar reservas.
* Asociar usuarios a reservas.
* Asociar instalaciones.
* Definir fecha y horario.
* Definir duración.
* Calcular el importe.
* Aplicar descuentos correspondientes.
* Evitar superposición de reservas.

El sistema valida que una instalación no tenga dos reservas activas para el mismo período.

### Reservas a favor

Cuando corresponde una cancelación, el sistema puede generar una **Reserva a Favor**, funcionando como crédito para el usuario.

El crédito registra:

* Reserva de origen.
* Usuario.
* Importe.
* Fecha de generación.
* Fecha de vencimiento.
* Estado de utilización.

Esto permite utilizar el crédito posteriormente para una nueva reserva.

### Pagos

El sistema permite:

* Registrar pagos.
* Consultar pagos.
* Asociar pagos a reservas.
* Asociar pagos a membresías.
* Registrar el medio de pago.
* Registrar el estado del pago.
* Eliminar pagos según los permisos correspondientes.

Los estados principales son:

```text
PENDIENTE
APROBADO
RECHAZADO
```

Un pago aprobado puede confirmar una reserva.

## Roles

El sistema trabaja con diferentes perfiles de acceso.

### Administrador

Puede acceder a las principales funciones administrativas:

* Usuarios.
* Membresías.
* Categorías.
* Instalaciones.
* Reservas.
* Pagos.
* Configuración general.

### Empleado

Puede realizar tareas operativas como:

* Gestionar membresías.
* Gestionar instalaciones.
* Gestionar reservas.
* Consultar información necesaria para la administración diaria.

### Socio

Puede realizar operaciones relacionadas con sus propias actividades:

* Consultar sus reservas.
* Realizar reservas.
* Cancelar sus reservas.
* Renovar su membresía.
* Consultar pagos.
* Utilizar créditos disponibles.

### Usuario

Corresponde a un usuario registrado que todavía no posee una membresía activa.

Cuenta con acceso a las funcionalidades generales disponibles para usuarios registrados.

## Seguridad

El backend utiliza **Spring Security** para proteger los recursos de la API.

El acceso a las operaciones se controla mediante autenticación y roles.

Flujo general:

```text
Usuario
   │
   ▼
Login
   │
   ▼
Autenticación
   │
   ▼
Token
   │
   ▼
Dashboard
   │
   ├── Usuarios
   ├── Membresías
   ├── Instalaciones
   ├── Reservas
   └── Pagos
```

Las operaciones administrativas se encuentran protegidas para evitar accesos no autorizados.

## Base de datos

El sistema utiliza **MySQL**.

Nombre de la base de datos:

```text
sysge_polidep
```

Principales tablas:

```text
usuario
empleado
administrador
socio
categoria
membresia
instalacion
reserva
reserva_a_favor
pago
historial_mantenimiento
```

La base de datos utiliza relaciones, restricciones e índices para mantener la integridad de la información.

También se aplican validaciones para evitar reservas superpuestas.

## Estructura del proyecto

```text
SysGePolidep/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── assets/
│   │   │   └── logo-sgp.png
│   │   │
│   │   ├── components/
│   │   │   ├── App.jsx
│   │   │   ├── Login.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Sidebar.jsx
│   │   │   ├── Usuarios.jsx
│   │   │   ├── Membresias.jsx
│   │   │   ├── Instalaciones.jsx
│   │   │   ├── Reservas.jsx
│   │   │   └── Pagos.jsx
│   │   │
│   │   ├── index.css
│   │   └── main.jsx
│   │
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

## Requisitos

Para ejecutar el proyecto se necesita tener instalado:

* Java 17 o superior.
* Maven.
* Node.js.
* npm.
* MySQL.
* Git.

## Instalación

### 1. Clonar el repositorio

```bash
git clone URL_DEL_REPOSITORIO
```

Ingresar al proyecto:

```bash
cd SysGePolidep
```

## Configuración del Backend

Crear la base de datos:

```sql
CREATE DATABASE sysge_polidep;
```

Configurar la conexión en:

`backend/src/main/resources/application.properties`

Ejemplo:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sysge_polidep
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> No subir credenciales reales al repositorio.

## Ejecutar Backend

Ingresar a la carpeta:

```bash
cd backend
```

Ejecutar:

```bash
mvn spring-boot:run
```

El backend estará disponible en:

`http://localhost:8080`

## Configuración del Frontend

Ingresar a la carpeta:

```bash
cd frontend
```

Instalar las dependencias:

```bash
npm install
```

## Ejecutar Frontend

Ejecutar:

```bash
npm run dev
```

La aplicación estará disponible normalmente en:

`http://localhost:5173`

Usuarios de prueba

## Usuarios de prueba

El sistema incluye usuarios de prueba precargados automáticamente al iniciar el backend. Estos usuarios permiten probar las diferentes funcionalidades según el rol asignado.

| Rol               | Usuario    | Contraseña    | Descripción                             |
| ----------------- | ---------- | ------------- | --------------------------------------- |
| **Administrador** | `admin`    | `Admin123`    | Acceso a la gestión general del sistema |
| **Empleado**      | `empleado` | `Empleado123` | Acceso a operaciones administrativas    |
| **Socio**         | `socio`    | `Socio123`    | Usuario con membresía vigente           |
| **Usuario**       | `usuario`  | `Usuario123`  | Usuario registrado sin membresía        |

### Permisos principales por rol

* **Administrador:** gestión de usuarios, membresías, instalaciones, reservas y pagos.
* **Empleado:** gestión de membresías, instalaciones y reservas.
* **Socio:** gestión de sus reservas y pagos, además de funcionalidades relacionadas con su membresía.
* **Usuario:** acceso básico al sistema. Puede registrarse y realizar operaciones permitidas para usuarios sin membresía.

> **Nota:** Los usuarios de prueba son creados automáticamente mediante `DataInitializer` al iniciar el backend. Las contraseñas indicadas son únicamente para el entorno de demostración y pruebas del proyecto.

## API REST

Principales endpoints:

```text
/api/auth
/api/usuarios
/api/socios
/api/membresias
/api/categorias
/api/instalaciones
/api/reservas
/api/reservas-a-favor
/api/pagos
/api/mantenimientos
```

Métodos HTTP utilizados:

```text
GET
POST
PUT
DELETE
```

## Reglas de negocio principales

Entre las principales reglas implementadas se encuentran:

* Solo los usuarios autenticados pueden acceder a los recursos protegidos.
* Las categorías de membresía son gestionadas por el administrador.
* Las instalaciones pueden estar disponibles, en mantenimiento o clausuradas.
* No se permiten reservas superpuestas.
* Una instalación no disponible no puede ser reservada.
* Las reservas pueden ser canceladas según las reglas del sistema.
* Una cancelación puede generar una reserva a favor.
* Las reservas a favor poseen un período de validez.
* Los pagos pueden asociarse a reservas o membresías.
* Un pago aprobado puede confirmar una reserva.
* Los descuentos dependen de la categoría de membresía.
* Las operaciones administrativas se encuentran protegidas mediante roles.

## Diseño de la interfaz

La interfaz utiliza una identidad visual basada principalmente en el color institucional:

**Burdeos / Vino — `#5C0613`**

La paleta se complementa con:

* Tonos blancos.
* Grises neutros.
* Tonos oscuros para navegación.
* Detalles dorados.
* Estados visuales para operaciones exitosas, pendientes o con errores.

El diseño busca mantener una interfaz:

* Moderna.
* Empresarial.
* Clara.
* Simple.
* Responsive.

## Estado del proyecto

### MVP

El proyecto cuenta actualmente con un MVP funcional.

* [x] Autenticación
* [x] Gestión de usuarios
* [x] Gestión de socios
* [x] Gestión de membresías
* [x] Gestión de categorías
* [x] Gestión de instalaciones
* [x] Gestión de reservas
* [x] Cancelación de reservas
* [x] Reservas a favor
* [x] Gestión de pagos
* [x] Control de acceso por roles
* [x] Integración frontend/backend
* [x] Persistencia en MySQL
* [x] Interfaz web responsive

## Equipo

Proyecto desarrollado en la:

**Universidad Nacional de Santiago del Estero**

**Facultad de Ciencias Exactas y Tecnologías**

**Licenciatura en Sistemas de Información**

### Integrantes

* **Contreras, Elliot Alejandro**
* **Ruiz Corvalán, Facundo**
* **Garnica, Martín**
* **Jiménez, Juan Gabriel**

## Objetivo del proyecto

El objetivo de **SysGe PoliDep** es desarrollar una plataforma que permita centralizar y facilitar la gestión de un polideportivo.

El sistema busca integrar en una única plataforma la administración de:

```text
Usuarios
   ↓
Socios
   ↓
Membresías
   ↓
Instalaciones
   ↓
Reservas
   ↓
Pagos
```

permitiendo aplicar reglas de negocio, controlar el acceso según los diferentes roles y mantener la información centralizada.

## Proyecto académico

Este proyecto fue desarrollado con fines académicos en el marco de la carrera:

**Licenciatura en Sistemas de Información**

**FCEyT - UNSE**

## Licencia

Proyecto desarrollado con fines académicos.

**Universidad Nacional de Santiago del Estero (UNSE).**
