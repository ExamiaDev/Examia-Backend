# Examia Backend

Backend API para la aplicación Examia - Sistema de corrección automática de exámenes mediante IA.

## Tecnologías

- **Java 17**
- **Spring Boot 3.2.5**
- **MongoDB** (Atlas para producción)
- **JWT** para autenticación
- **Gradle** como build tool

## Requisitos Previos

- Java 17 o superior
- Gradle 8.x (o usar el wrapper incluido)
- MongoDB (local para desarrollo o Atlas para producción)

## Configuración Local

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/Examia-Backend.git
cd Examia-Backend
```

### 2. Configurar MongoDB Local

Asegúrate de tener MongoDB corriendo localmente en `mongodb://localhost:27017`.

### 3. Ejecutar la aplicación

```bash
# Windows
.\gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8080`.

## Endpoints de la API

### Autenticación

#### Registrar Usuario
```http
POST /api/auth/register
Content-Type: application/json

{
    "email": "usuario@ejemplo.com",
    "password": "miPassword123",
    "nombre": "Juan",
    "apellido": "Pérez",
    "role": "ALUMNO"  // o "PROFESOR"
}
```

**Respuesta exitosa (201 Created):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "usuario@ejemplo.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "role": "ALUMNO",
    "message": "Usuario registrado exitosamente"
}
```

**Error - Usuario ya existe (409 Conflict):**
```json
{
    "status": 409,
    "error": "Conflict",
    "message": "El email 'usuario@ejemplo.com' ya está registrado",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/auth/register"
}
```

#### Iniciar Sesión
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "usuario@ejemplo.com",
    "password": "miPassword123"
}
```

**Respuesta exitosa (200 OK):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "usuario@ejemplo.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "role": "ALUMNO",
    "message": "Inicio de sesión exitoso"
}
```

**Error - Usuario no encontrado (404 Not Found):**
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "No existe un usuario con el email 'usuario@ejemplo.com'",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/auth/login"
}
```

**Error - Contraseña incorrecta (401 Unauthorized):**
```json
{
    "status": 401,
    "error": "Unauthorized",
    "message": "La contraseña es incorrecta para el usuario 'usuario@ejemplo.com'",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/auth/login"
}
```

### Health Check
```http
GET /actuator/health
```

## Configuración de MongoDB Atlas (Producción)

### 1. Crear cuenta en MongoDB Atlas

1. Ve a [mongodb.com/cloud/atlas](https://mongodb.com/cloud/atlas)
2. Crea una cuenta gratuita
3. Crea un nuevo proyecto llamado "Examia"

### 2. Crear un Cluster

1. Click en "Build a Database"
2. Selecciona "M0 Sandbox" (gratuito - 512MB)
3. Elige el proveedor y región más cercana
4. Click en "Create Cluster"

### 3. Configurar Acceso a la Base de Datos

#### Crear Usuario de Base de Datos
1. Ve a "Database Access" en el menú lateral
2. Click en "Add New Database User"
3. Elige "Password" como método de autenticación
4. Ingresa un nombre de usuario (ej: `examia-backend`)
5. Genera una contraseña segura (guárdala!)
6. En "Database User Privileges", selecciona "Read and write to any database"
7. Click en "Add User"

#### Configurar IP Whitelist
1. Ve a "Network Access" en el menú lateral
2. Click en "Add IP Address"
3. Para desarrollo: Click en "Add Current IP Address"
4. **Para producción (Render)**: Click en "Allow Access from Anywhere" (0.0.0.0/0)
   - Nota: Esto es necesario porque Render usa IPs dinámicas
5. Click en "Confirm"

### 4. Obtener Connection String

1. Ve a "Database" y click en "Connect" en tu cluster
2. Selecciona "Connect your application"
3. Copia el connection string. Se verá así:
   ```
   mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
4. Reemplaza:
   - `<username>` con tu usuario de base de datos
   - `<password>` con tu contraseña
   - Agrega el nombre de la base de datos antes de `?`:
   ```
   mongodb+srv://examia-backend:TU_PASSWORD@cluster0.xxxxx.mongodb.net/examia?retryWrites=true&w=majority
   ```

## Deploy en Render

### 1. Preparar el Repositorio

Asegúrate de que el código esté en GitHub y el `Dockerfile` esté en la raíz.

### 2. Crear Web Service en Render

1. Ve a [render.com](https://render.com) y crea una cuenta
2. Click en "New +" → "Web Service"
3. Conecta tu repositorio de GitHub
4. Configura el servicio:
   - **Name**: `examia-backend`
   - **Environment**: `Docker`
   - **Region**: La más cercana a tus usuarios
   - **Branch**: `main`
   - **Instance Type**: Free (para comenzar)

### 3. Configurar Variables de Entorno

En la sección "Environment", agrega:

| Variable | Valor |
|----------|-------|
| `MONGODB_URI` | `mongodb+srv://user:pass@cluster.mongodb.net/examia?retryWrites=true&w=majority` |
| `JWT_SECRET` | Una cadena aleatoria de al menos 64 caracteres (Base64) |
| `JWT_EXPIRATION` | `86400000` (24 horas en ms) |

#### Generar JWT_SECRET seguro:

```bash
# Linux/Mac
openssl rand -base64 64

# O usar una herramienta online como:
# https://generate-secret.vercel.app/64
```

### 4. Configurar Health Check

- **Health Check Path**: `/actuator/health`

### 5. Deploy

Click en "Create Web Service". El primer deploy tomará varios minutos.

Tu API estará disponible en: `https://examia-backend.onrender.com`

## Uso del Token JWT

Para endpoints protegidos (cuando los implementes), incluye el token en el header:

```http
GET /api/protected-endpoint
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Estructura del Proyecto

```
src/main/java/com/examia/
├── ExamiaApplication.java       # Clase principal
├── config/
│   ├── ApplicationConfig.java   # Configuración de beans
│   ├── MongoConfig.java         # Configuración de MongoDB
│   └── SecurityConfig.java      # Configuración de Spring Security
├── controller/
│   └── AuthController.java      # Endpoints de autenticación
├── dto/
│   ├── AuthResponse.java        # Respuesta de auth
│   ├── ErrorResponse.java       # Respuesta de errores
│   ├── LoginRequest.java        # Request de login
│   └── RegisterRequest.java     # Request de registro
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── InvalidCredentialsException.java
│   ├── UserAlreadyExistsException.java
│   └── UserNotFoundException.java
├── model/
│   ├── Role.java                # Enum de roles
│   └── User.java                # Entidad usuario
├── repository/
│   └── UserRepository.java      # Repositorio MongoDB
├── security/
│   └── JwtAuthenticationFilter.java
└── service/
    ├── AuthService.java         # Lógica de autenticación
    └── JwtService.java          # Manejo de tokens JWT
```

## Próximos Pasos

- [ ] Implementar gestión de exámenes (CRUD)
- [ ] Implementar rangos horarios para exámenes
- [ ] Integrar API de Gemini 2.5 para corrección automática
- [ ] Implementar sistema de calificaciones
- [ ] Agregar tests unitarios e integración

## Licencia

MIT

