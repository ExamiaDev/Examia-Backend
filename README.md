# Examia Backend

Backend API para la aplicación Examia - Sistema de corrección automática de exámenes mediante IA.

## Tecnologías

- **Java 17**
- **Spring Boot 3.2.5**
- **MongoDB** (Atlas para producción)
- **JWT** para autenticación
- **Gradle** como build tool

## 📬 Colección de Postman

Incluimos una colección de Postman para facilitar las pruebas de la API.

### Archivos disponibles

```
postman/
├── Examia-Backend.postman_collection.json    # Colección con todos los endpoints
├── Examia-Local.postman_environment.json     # Entorno local (localhost:8080)
└── Examia-Production.postman_environment.json # Entorno producción (Render)
```

### Cómo importar en Postman

1. Abrir Postman
2. Click en **Import** (arriba a la izquierda)
3. Arrastrar los 3 archivos JSON de la carpeta `postman/`
4. Seleccionar el entorno deseado (Local o Production) en el dropdown de arriba a la derecha

### Características de la colección

- ✅ **Variables de entorno**: Cambiá fácilmente entre Local y Producción
- ✅ **Token automático**: Después del login, el token se guarda automáticamente
- ✅ **Ejemplos de respuesta**: Cada endpoint tiene ejemplos de respuestas exitosas y errores
- ✅ **Documentación inline**: Descripción de cada endpoint y sus parámetros

> **Nota**: Los usuarios son cargados directamente en la base de datos por un administrador. No hay registro público.

### Entornos disponibles

| Entorno | URL |
|---------|-----|
| **Local** | `http://localhost:8080` |
| **Production** | `https://examia-backend-tzwg.onrender.com` |

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

> **Nota**: Los usuarios son cargados directamente en la base de datos por un administrador. No hay endpoint de registro público.

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

## 🔐 Autenticación JWT

Este proyecto utiliza **JWT (JSON Web Token)** para la autenticación de usuarios. JWT es un estándar abierto (RFC 7519) que permite transmitir información de forma segura entre partes como un objeto JSON.

### ¿Cómo funciona?

1. **Registro/Login**: El usuario envía sus credenciales
2. **Generación del Token**: El servidor valida las credenciales y genera un JWT
3. **Uso del Token**: El cliente incluye el token en cada request a endpoints protegidos
4. **Validación**: El servidor valida el token en cada request

### Estructura del Token JWT

El token tiene 3 partes separadas por puntos (`.`):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlBST0ZFU09SIiwiaWF0IjoxNjE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
│                                    │                                                                              │
└─────── Header ─────────────────────┴─────────────────────── Payload ───────────────────────────────────────────────┴─── Signature ───
```

- **Header**: Algoritmo (HS256) y tipo de token
- **Payload**: Datos del usuario (email, rol, nombre, fecha de expiración)
- **Signature**: Firma para verificar que el token no fue alterado

### Contenido del Token (Payload)

Cuando decodificas el token, el payload contiene:

```json
{
  "sub": "usuario@ejemplo.com",    // Email del usuario (subject)
  "role": "PROFESOR",              // Rol del usuario
  "nombre": "Juan",                // Nombre
  "apellido": "Pérez",             // Apellido
  "iat": 1616239022,               // Fecha de emisión (issued at)
  "exp": 1616325422                // Fecha de expiración
}
```

### Configuración del Token

| Parámetro | Valor por defecto | Descripción |
|-----------|-------------------|-------------|
| Algoritmo | HS256 | HMAC con SHA-256 |
| Expiración | 24 horas | Configurable via `JWT_EXPIRATION` |
| Secret | Variable de entorno | Mínimo 256 bits (Base64) |

### Uso del Token en Requests

Para acceder a endpoints protegidos, incluye el token en el header `Authorization`:

```http
GET /api/protected-endpoint
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Ejemplo con cURL

```bash
# 1. Login para obtener el token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@ejemplo.com","password":"miPassword123"}' \
  | jq -r '.token')

# 2. Usar el token en requests protegidos
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer $TOKEN"
```

### Ejemplo con JavaScript (Fetch)

```javascript
// Login
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'usuario@ejemplo.com',
    password: 'miPassword123'
  })
});

const { token } = await loginResponse.json();

// Guardar token (localStorage, sessionStorage, o estado de la app)
localStorage.setItem('token', token);

// Usar token en requests protegidos
const response = await fetch('http://localhost:8080/api/protected-endpoint', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

### Ejemplo con Axios

```javascript
import axios from 'axios';

// Configurar interceptor para agregar token automáticamente
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Login
const { data } = await axios.post('/api/auth/login', {
  email: 'usuario@ejemplo.com',
  password: 'miPassword123'
});
localStorage.setItem('token', data.token);

// Requests protegidos (el token se agrega automáticamente)
const exams = await axios.get('/api/exams');
```

### Manejo de Errores de Autenticación

| Código | Situación | Acción recomendada |
|--------|-----------|-------------------|
| 401 | Token inválido/expirado | Redirigir a login |
| 403 | Sin permisos (rol incorrecto) | Mostrar mensaje de acceso denegado |

### Endpoints Públicos vs Protegidos

| Endpoint | Acceso | Descripción |
|----------|--------|-------------|
| `POST /api/auth/register` | 🌐 Público | Registro de usuarios |
| `POST /api/auth/login` | 🌐 Público | Inicio de sesión |
| `GET /api/auth/health` | 🌐 Público | Health check |
| `GET /actuator/health` | 🌐 Público | Health check (Actuator) |
| `*` (resto) | 🔒 Protegido | Requiere token JWT válido |

### Decodificar Token (Debug)

Para ver el contenido de un token durante desarrollo, puedes usar:

- [jwt.io](https://jwt.io) - Decodificador online
- Extensión de navegador "JWT Debugger"

> ⚠️ **Nota**: Nunca compartas tokens de producción en herramientas online

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

## 📋 Buenas Prácticas para el Equipo

### Al agregar nuevos endpoints

Cada vez que se agregue un nuevo endpoint a la API, se debe:

1. **Actualizar la colección de Postman**
   - Agregar el nuevo endpoint en `postman/Examia-Backend.postman_collection.json`
   - Incluir ejemplos de request y response
   - Documentar los posibles códigos de error
   
2. **Probar en ambos entornos**
   - Verificar que funcione en Local
   - Verificar que funcione en Production (después del deploy)

3. **Documentar en el README** (si es necesario)
   - Agregar el endpoint en la sección "Endpoints de la API"
   - Documentar parámetros y respuestas esperadas

### Convenciones de código

- **Commits**: Usar [Conventional Commits](https://www.conventionalcommits.org/)
  - `feat:` nueva funcionalidad
  - `fix:` corrección de bugs
  - `docs:` cambios en documentación
  - `refactor:` refactorización de código

### 🔀 Flujo de Branches (GitFlow)

Este proyecto usa GitFlow con PRs obligatorios y backport automático.

```
feature/xxx ─────► develop ─────► release/vX.X.X ─────► main
                      ▲                                   │
                      │                                   │
                      └───────── backport (auto) ─────────┘
```

#### Tipos de branches

| Prefijo | Destino | Descripción |
|---------|---------|-------------|
| `feature/*` | `develop` | Nuevas funcionalidades |
| `fix/*` | `develop` | Corrección de bugs |
| `release/*` | `main` | Nueva versión |
| `hotfix/*` | `main` | Correcciones urgentes en prod |
| `backport/*` | `develop` | Sincronización auto main→develop |

#### Workflows automáticos

- **CI**: Build & Test en cada PR
- **Validate PR**: Valida el flujo de branches
- **Release**: Crea tag automático en merge de `release/*` a `main`
- **Backport**: Crea PR automático para sincronizar `main` → `develop`

#### Ejemplo de flujo

```bash
# 1. Crear feature
git checkout develop && git pull
git checkout -b feature/mi-feature
# ... hacer cambios ...
git push origin feature/mi-feature
# Crear PR → develop

# 2. Crear release
git checkout develop && git pull
git checkout -b release/v1.0.0
git push origin release/v1.0.0
# Crear PR → main
# Auto: se crea tag v1.0.0 + PR backport a develop
```

> 📄 Ver `.github/BRANCH_PROTECTION.md` para configurar reglas de protección.

### Variables de entorno

Nunca commitear credenciales. Usar:
- `application-local.yml` para desarrollo (está en `.gitignore`)
- Variables de entorno en Render para producción

## Licencia

MIT

---

## Historial de Cambios

### 13/05/2026 — Felipe Massun

#### Corrección de CORS para producción

- **`SecurityConfig.java`**: reescritura completa de la configuración CORS para soportar previews dinámicos de Vercel y desarrollo local:
  - Eliminado `setAllowedOrigins` con wildcard (no funciona en ese método, Spring lo trata como string literal).
  - Reemplazado por `setAllowedOriginPatterns(List.of("http://localhost:*", "https://*.vercel.app"))` que sí soporta wildcards.
  - `setAllowedHeaders(List.of("*"))` y `setExposedHeaders(List.of("*"))` para no bloquear ningún header.
  - `setAllowCredentials(true)` requerido para que el browser acepte respuestas de preflight con header `Authorization`.
  - Agregado `.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()` para que Spring Security nunca bloquee preflight antes del filtro CORS.
  - Import de `org.springframework.http.HttpMethod` limpiado al nivel de clase.

#### Configuración de deploy

- **`Dockerfile`**: corregido el `HEALTHCHECK` cuyo `--start-period=5s` mataba el container antes de que Spring Boot + MongoDB terminaran de inicializar (~20-40s). Nuevos valores: `--start-period=90s`, `--timeout=10s`, `--retries=5`.
- **`render.yaml`**: creado para declarar el servicio en Render con tipo `web`, runtime `docker`, `healthCheckPath: /actuator/health` y declaración de variables de entorno requeridas (`MONGODB_URI`, `JWT_SECRET`, `JWT_EXPIRATION`, `PORT`).

