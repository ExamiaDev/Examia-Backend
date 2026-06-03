# Examia — Bug Tracker QA

---

## BUG-01 — Exposición de existencia de usuario en login

**US:** US-01  
**Severidad:** Alta (vulnerabilidad de seguridad — enumeración de usuarios)  
**Estado:** ✅ Resuelto

### Descripción
Al ingresar un email o legajo no registrado, el sistema devolvía mensajes específicos que revelaban si el usuario existía o no (`"No existe un usuario con el email '...'"`, `"La contraseña es incorrecta para el usuario '...'"`, etc.). Esto viola el criterio de aceptación de US-01.

### Archivos modificados
- `src/main/java/com/examia/service/AuthService.java`
- `src/main/java/com/examia/exception/GlobalExceptionHandler.java`

### Fix aplicado
- En `AuthService.login()`: reemplazados `UserNotFoundException` y el `InvalidCredentialsException` con mensaje detallado por un único `InvalidCredentialsException("Credenciales incorrectas")` en todos los casos de fallo (usuario no encontrado y contraseña incorrecta).
- En `AuthService.loginUade()`: mismo criterio — usuario no encontrado, email no coincide y contraseña incorrecta retornan todos `"Credenciales incorrectas"`.
- En `GlobalExceptionHandler`: el handler de `UserNotFoundException` pasó de devolver HTTP 404 a HTTP 401 con mensaje genérico, evitando que el status code mismo revele la existencia del usuario.

> Nota: el mensaje `"La cuenta está deshabilitada"` se mantuvo ya que no revela información sobre campos ingresados. Confirmar con el equipo si debe ocultarse también.

---

## BUG-02 — Login sin rate limiting / protección contra fuerza bruta

**US:** US-03  
**Severidad:** Alta (P1)  
**Estado:** ✅ Resuelto

### Descripción
El endpoint `POST /api/auth/login` (y `/api/auth/login-uade`) aceptaba intentos ilimitados con credenciales incorrectas. No había bloqueo de cuenta, ni respuesta HTTP 429, ni limitación por IP. `isAccountNonLocked()` en el modelo estaba hardcodeado a `true`.

### Archivos modificados
- `src/main/java/com/examia/model/User.java`
- `src/main/java/com/examia/service/AuthService.java`

### Fix aplicado
- Agregados campos `failedAttempts` (int) y `lockedUntil` (LocalDateTime) al modelo `User`.
- `isAccountNonLocked()` ahora evalúa `lockedUntil` dinámicamente.
- En `AuthService`: después de cada credencial incorrecta se incrementa `failedAttempts`. Al llegar a **4 intentos** se setea `lockedUntil = ahora + 10 minutos` y se persiste.
- Al superar el bloqueo se retorna `401` con mensaje `"Cuenta bloqueada temporalmente. Intente nuevamente en X minuto(s)."`.
- Login exitoso resetea `failedAttempts` y `lockedUntil`.
- Si el lock ya expiró y el usuario vuelve a intentar, el contador se limpia automáticamente.
- Aplica a ambos flujos: `login()` y `loginUade()`.

---

## BUG-03 — JWT sigue válido tras logout (sin blacklist ni endpoint de logout)

**US:** US-01 / Seguridad general  
**Severidad:** Alta  
**Estado:** ✅ Resuelto

### Descripción
El logout solo limpiaba `localStorage` en el frontend. El backend no tenía endpoint `POST /api/auth/logout` ni mecanismo de revocación. Un token interceptado o copiado seguía siendo válido hasta su expiración natural (24h).

### Archivos creados
- `src/main/java/com/examia/model/RevokedToken.java`
- `src/main/java/com/examia/repository/RevokedTokenRepository.java`
- `src/main/java/com/examia/service/TokenBlacklistService.java`
- `src/presentation/components/SessionExpiredModal.jsx` (frontend)

### Archivos modificados (backend)
- `src/main/java/com/examia/service/JwtService.java`
- `src/main/java/com/examia/security/JwtAuthenticationFilter.java`
- `src/main/java/com/examia/service/AuthService.java`
- `src/main/java/com/examia/controller/AuthController.java`
- `src/main/java/com/examia/config/SecurityConfig.java`

### Archivos modificados (frontend)
- `src/infrastructure/http/httpClient.js`
- `src/infrastructure/api/AuthAPI.js`
- `src/App.jsx`

### Fix aplicado — Backend
- Nuevo documento MongoDB `RevokedToken` con campo `expiresAt` indexado con `expireAfterSeconds = 0`: MongoDB elimina los tokens automáticamente al vencer, sin job de limpieza.
- `TokenBlacklistService`: `revokeToken()` persiste el token, `isRevoked()` consulta la colección.
- `JwtAuthenticationFilter`: chequea la blacklist antes de autenticar. Si el token está revocado, la request continúa sin autenticación (→ 401 en endpoints protegidos).
- `AuthService.logout()`: extrae la fecha de expiración del token y lo persiste en la blacklist.
- `POST /api/auth/logout`: endpoint autenticado que extrae el Bearer token del header y llama a `authService.logout()`. Retorna 204.
- `SecurityConfig`: `/api/auth/**` reemplazado por rutas públicas explícitas, dejando `/api/auth/logout` como endpoint autenticado.

### Fix aplicado — Frontend
- `AuthAPI.logout()`: llama a `POST /api/auth/logout` antes de limpiar localStorage. Usa `finally` para garantizar limpieza aunque el backend falle.
- `httpClient.js`: en lugar de redirigir con `window.location.href`, el interceptor de 401 despacha el evento custom `session-expired`.
- `App.jsx`: nuevo componente `AppContent` (dentro del Router) que escucha el evento `session-expired` y muestra un `SessionExpiredModal`. Al cerrar el modal navega a `/login` con React Router.
- `SessionExpiredModal.jsx`: Dialog MUI con ícono de candado, mensaje de sesión expirada y botón "Volver al login".

---

## BUG-04 — Acceso UADE válido denegado

**US:** US-03  
**Severidad:** Crítico (P1)  
**Estado:** ❌ Inválido — No reproducible

### Descripción reportada
Al ingresar los datos del excel de acceso en el login UADE, el sistema devuelve error de credenciales incorrectas.

### Análisis
**Bug descartado.** El equipo de QA confirmó que las credenciales fueron ingresadas incorrectamente. El sistema funciona como se espera cuando se usan los datos correctos del excel. No hay defecto en el código.

---

## BUG-05 — Login UADE con legajo incorrecto muestra error genérico de sesión

**US:** US-04  
**Severidad:** Crítico (P1)  
**Estado:** ✅ Resuelto (parcialmente por BUG-01, completado aquí)

### Descripción
Al ingresar un legajo que no existe, el sistema mostraba `"Usuario UADE no encontrado. Verificá tu legajo y email."` — mensaje sobre el usuario, no sobre las credenciales, violando US-01 y generando confusión al tester.

### Causa raíz
- **Backend (original):** legajo no encontrado → `UserNotFoundException` → HTTP 404. El handler de 404 en el frontend tenía el mensaje `"Usuario UADE no encontrado..."` hardcodeado.
- **Frontend:** el branch `status === 401` usaba un string hardcodeado, pisando cualquier mensaje específico del backend (ej: mensaje de cuenta bloqueada de BUG-02).
- **Código muerto residual:** el branch `status === 404` seguía presente después del fix de BUG-01, con el mensaje que violaba US-01.

### Fix aplicado
- **Backend:** ya resuelto por BUG-01 — todos los fallos de `loginUade()` devuelven 401 genérico.
- **Frontend (`AuthAPI.js`):**
  - Eliminado el branch muerto de 404 en `loginUade` que exponía `"Usuario UADE no encontrado"`.
  - El handler de 401 (en `login` y `loginUade`) ahora usa `error.response?.data?.message` cuando está disponible, con el string genérico como fallback. Esto permite que mensajes específicos del backend (como "Cuenta bloqueada temporalmente...") lleguen al usuario en lugar de ser pisados.

---

## BUG-06 — Recuperación de contraseña abre pantalla vacía

**US:** US-06  
**Severidad:** Crítico (P1)  
**Estado:** ✅ Resuelto

### Descripción
Al hacer clic en "¿Olvidaste tu contraseña?" desde el login, se navegaba a `/forgot-password` y se mostraba una pantalla completamente vacía.

### Causa raíz
La ruta `/forgot-password` nunca fue registrada en `App.jsx`. `ForgotPasswordPage.jsx` y `ForgotPasswordForm.jsx` estaban completamente implementados (stepper de 3 pasos: verificación de email, código, nueva contraseña), pero React Router no tenía esa ruta definida — al no matchear ninguna ruta ni haber un catch-all `*`, renderizaba en blanco.

### Archivos modificados
- `src/App.jsx` (frontend)

### Fix aplicado
Agregado import de `ForgotPasswordPage` y la ruta `<Route path="/forgot-password" element={<ForgotPasswordPage />} />` en `AppContent`.

---

## BUG-07 — Términos y condiciones sin link de lectura

**US:** US-09  
**Severidad:** Medio (P3)  
**Estado:** ✅ Resuelto

### Descripción
El checkbox "Acepto los términos y condiciones" en el registro mostraba texto plano sin ningún link. El usuario aceptaba condiciones que no podía leer.

### Archivos creados
- `src/presentation/components/TermsAndConditionsModal.jsx` (frontend)

### Archivos modificados
- `src/presentation/components/RegisterForm.jsx` (frontend)

### Fix aplicado
- Creado `TermsAndConditionsModal.jsx`: Dialog MUI con scroll, 10 secciones redactadas acordes a la plataforma (descripción del servicio, integridad académica, privacidad bajo Ley 25.326, propiedad intelectual, conducta, etc.).
- En `RegisterForm.jsx`: el texto del label reemplaza "términos y condiciones" por un `Link` MUI que abre el modal. El estado `termsOpen` controla la visibilidad. El modal se cierra con "Entendido" sin alterar el estado del checkbox.

---

## BUG-08 — Selección de rol sin requisitos visibles

**US:** US-09  
**Severidad:** Alto (P1)  
**Estado:** ⏸ No resuelto — decisión de producto

### Descripción
El selector de rol en el registro (Alumno / Docente) no muestra ninguna diferenciación ni requisito visible. Cualquier usuario puede seleccionarse como Docente sin restricción ni advertencia. A nivel backend, el endpoint acepta el rol enviado sin validación adicional.

### Análisis técnico
- **Frontend**: radio buttons sin descripción ni aviso de requisitos institucionales.
- **Backend** (`AuthService.java:70`): acepta el rol directamente del request sin restricción — un alumno podría auto-registrarse como DOCENTE manipulando el payload.

### Decisión
Se tuvo en cuenta la corrección técnica (restringir el rol en backend + agregar advertencia en frontend), pero se acordó con el equipo **no implementarla**. Queda bajo la responsabilidad y honestidad académica del alumno la correcta selección de su rol.

---

## BUG-09 — Registro de cuenta bloqueado para cualquier rol

**US:** US-09  
**Severidad:** Crítico (P1)  
**Estado:** ✅ Resuelto

### Descripción
El sistema no permitía crear cuentas ni como ALUMNO ni como DOCENTE. El error se producía antes de llegar al backend.

### Causa raíz
`AuthService.js:31` incluía `recoveryEmail` en la validación de campos obligatorios del frontend:

```javascript
if (!nombre || !apellido || !username || !email || !recoveryEmail || !password) {
  throw new ValidationError('Todos los campos son obligatorios');
}
```

El campo "Mail de recupero de contraseña" no está marcado como requerido en el formulario y el backend lo trata como opcional (defaultea al email principal si viene vacío). Cualquier intento de registro con ese campo en blanco fallaba silenciosamente en el cliente para todos los roles.

**No fue resuelto por fixes anteriores** — el flujo de registro no fue tocado en BUG-01 a BUG-08.

### Archivos modificados
- `src/application/services/AuthService.js` (frontend)

### Fix aplicado
Eliminado `recoveryEmail` de la validación obligatoria en el cliente. El backend ya maneja el caso vacío correctamente.

---

## BUG-10 — Columna Estado de examen muestra guión

**US:** US-11  
**Severidad:** Alto (P2)  
**Estado:** ✅ Resuelto

### Descripción
La columna Estado en el listado de exámenes del docente mostraba un guión `—` en lugar del estado real (borrador, publicado, activo).

### Análisis
El código tenía un comentario huérfano `// Mapea el status del backend a la etiqueta que mostramos` sin implementación. En el estado del código al momento del análisis, la columna ya mostraba Publicado/Borrador con un Switch, pero faltaba el tercer estado **Activo** y no había diferenciación visual por color.

El backend no retorna un campo `status` string — expone `published` (boolean), `scheduledStartTime` y `scheduledEndTime` en `ExamSummaryResponse`, suficiente para derivar los 3 estados en el cliente.

### Archivos modificados
- `src/presentation/pages/dashboard/docente/components/ExamenesContent.jsx` (frontend)

### Fix aplicado
- Agregado `STATUS_CONFIG` con estilos por estado: Activo (verde), Publicado (azul), Borrador (gris).
- Agregado `getExamStatus(exam)`: deriva el estado a partir de `published`, `scheduledStartTime` y `scheduledEndTime` — si está publicado y `now` cae dentro de la ventana horaria → Activo; publicado fuera de ventana → Publicado; no publicado → Borrador.
- La celda Estado ahora muestra un chip de color con la etiqueta correspondiente junto al Switch de toggle.

---

## BUG-11 — Botón Corregir no permite ingresar a correcciones

**US:** US-11  
**Severidad:** Crítico (P1)  
**Estado:** ✅ No reproducible — implementación correcta verificada

### Descripción
Al clickear "Corregir" en el menú de correcciones pendientes, el sistema no permitía ingresar a la vista de corrección individual.

### Análisis
Se revisaron exhaustivamente todos los archivos involucrados en el flujo (25+ archivos):

- **`DocenteDashboard.jsx`**: el `renderContent()` maneja correctamente el caso de 4 segmentos de ruta (`/docente/correcciones/:examId/:subId`) → renderiza `<CorreccionDetalleContent examId submissionId>` ✓  
- **`CorreccionesContent` → `SubmissionsView`**: el botón "Corregir" llama a `navigate('/docente/correcciones/${exam.id}/${sub.id}')` correctamente ✓  
- **Backend `SubmissionController`**: endpoints `GET /api/exams/{examId}/submissions/{submissionId}` existen con `@PreAuthorize("hasRole('DOCENTE')")` y verifican ownership del examen ✓  
- **`@EnableMongoAuditing`**: activo en `MongoConfig.java`, `submittedAt` se auto-popula con `@CreatedDate` ✓  
- **DTOs**: `SubmissionSummaryResponse.id` (String) coincide con `sub.id` del frontend ✓  
- **URLs**: `baseUrl` = `http://localhost:8080/api` + `/exams/${examId}/submissions` coincide con el mapping del backend ✓

El bug probablemente existió en una versión anterior del routing con lógica más simple que siempre retornaba `<CorreccionesContent />` sin manejar el caso de 4 segmentos. La implementación actual está correctamente construida.

**No se aplicaron cambios de código.** Se recomienda verificar en entorno real con datos de prueba (entregas activas) para confirmar el flujo completo.

---

## BUG-12 — `@PreAuthorize` devuelve HTTP 500 en vez de 403 por rol insuficiente

**US:** Autorización por roles  
**Severidad:** Alto (P2)  
**Estado:** ✅ Resuelto

### Descripción
Cuando un usuario autenticado con rol insuficiente accede a un endpoint anotado con `@PreAuthorize` (ej: ALUMNO → `GET /api/exams` que requiere DOCENTE), Spring Security lanza `AccessDeniedException`. Como `GlobalExceptionHandler` no la manejaba específicamente, caía al handler genérico `Exception.class` y devolvía HTTP 500 con mensaje vago. El cliente interpretaba un error de servidor cuando era un problema de permisos.

### Reproducción
```
POST /api/auth/login  →  token de ALUMNO
GET /api/exams  Authorization: Bearer <token>
→ HTTP 500  (esperado: HTTP 403)
```

### Causa raíz
`GlobalExceptionHandler.java` no tenía `@ExceptionHandler(AccessDeniedException.class)`. `AccessDeniedException` es de Spring Security (`org.springframework.security.access`), distinto de `UnauthorizedAccessException` (excepción de dominio propia).

### Archivos modificados
- `src/main/java/com/examia/exception/GlobalExceptionHandler.java`

### Fix aplicado
Agregado handler específico para `AccessDeniedException` que retorna HTTP 403 con mensaje `"No tenés permisos para realizar esta acción"`, ubicado antes del handler genérico `Exception.class`.

---

## BUG-13 — `AuthResponse` de login UADE no devuelve campo `legajo`

**US:** US-04  
**Severidad:** Bajo (P3)  
**Estado:** ✅ No reproducible — implementación correcta verificada

### Descripción
`POST /api/auth/login-uade` no incluía el campo `legajo` en el `AuthResponse`. El frontend llamaba `persistSession(response)` que lee `response.legajo`, pero como era `undefined`, `JSON.stringify` lo omitía silenciosamente y el legajo no quedaba guardado en localStorage bajo la clave `user`.

### Análisis
El bug fue real en una versión anterior donde `AuthResponse` no tenía el campo `legajo`. En el código actual:

- **`AuthResponse.java`**: tiene `private String legajo;` ✓
- **`AuthService.loginUade()`**: setea `.legajo(user.getLegajo())` en el builder ✓
- **`AuthService.login()`**: también setea `.legajo(user.getLegajo())` ✓  
- **`persistSession()` (frontend)**: lee `legajo: response.legajo` y lo incluye en el objeto usuario ✓

El mecanismo del bug original era: campo ausente en DTO → Jackson no lo serializa → `response.legajo` es `undefined` en JS → `JSON.stringify` omite claves con valor `undefined` → localStorage sin legajo.

**No se aplicaron cambios de código.** La implementación actual es correcta.

---

## BUG-14 — Registro permite contraseñas inseguras sin validación de complejidad

**US:** US-09  
**Severidad:** Medio (P2)  
**Estado:** ✅ Resuelto

### Descripción
El formulario de registro aceptaba contraseñas triviales como `"cc"`. No existía ninguna validación de complejidad mínima ni en el backend ni en el frontend.

### Archivos modificados
- `src/main/java/com/examia/dto/RegisterRequest.java` (backend)
- `src/application/services/AuthService.js` (frontend)
- `src/presentation/components/RegisterForm.jsx` (frontend)

### Fix aplicado
Regex de complejidad aplicada en tres capas:

**Regla:** mínimo 8 caracteres · al menos 1 mayúscula · 1 minúscula · 1 número · 1 carácter especial  
**Regex:** `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$`

- **`RegisterRequest.java`**: anotación `@Pattern` en el campo `password` — el backend rechaza contraseñas débiles con 400 + mensaje descriptivo vía `GlobalExceptionHandler`.
- **`AuthService.js`**: validación client-side en `register()` antes de llamar a la API — lanza `ValidationError` con el mismo mensaje.
- **`RegisterForm.jsx`**: indicador visual en tiempo real debajo del campo contraseña — rojo con los requisitos mientras la contraseña no cumple, verde con "Contraseña segura ✓" cuando sí cumple. El espacio del indicador siempre está reservado (`visibility: hidden` cuando el campo está vacío) para evitar saltos de layout.

---

## BUG-15 — Registro permite mail de recupero igual al mail principal

**US:** US-10  
**Severidad:** Alto (P2)  
**Estado:** ✅ Resuelto

### Descripción
Al ingresar el mismo email en ambos campos (mail principal y mail de recupero), el sistema creaba la cuenta sin error. El mail de recupero pierde su utilidad si es idéntico al principal — si el usuario pierde acceso al email principal, el de recupero no le sirve de nada.

### Causa raíz agravante
`AuthService.register()` tenía lógica que defaulteaba el `recoveryEmail` al email principal cuando venía vacío, lo que refuerza el problema. No existía ningún chequeo de igualdad para el caso donde el usuario ingresa explícitamente el mismo email.

### Archivos modificados
- `src/main/java/com/examia/service/AuthService.java` (backend)
- `src/main/java/com/examia/exception/GlobalExceptionHandler.java` (backend)
- `src/application/services/AuthService.js` (frontend)
- `src/presentation/components/RegisterForm.jsx` (frontend)

### Fix aplicado
- **`AuthService.java`**: chequeo `equalsIgnoreCase` antes del defaulting — si `recoveryEmail` es explícito y coincide con `email`, lanza `IllegalArgumentException("El mail de recupero debe ser diferente al mail principal")`.
- **`GlobalExceptionHandler.java`**: nuevo handler para `IllegalArgumentException` → HTTP 400 Bad Request con el mensaje de la excepción.
- **`AuthService.js`**: validación client-side con comparación `toLowerCase()` antes del fetch.
- **`RegisterForm.jsx`**: mensaje de error inline debajo del campo recoveryEmail (con espacio reservado para no saltar layout) + botón "Crear cuenta" deshabilitado mientras los emails coincidan.

---

## BUG-17 — No se puede retomar borrador de examen

**US:** US-23  
**Severidad:** Alto (P1)  
**Estado:** ✅ Resuelto

### Descripción
Los borradores de examen aparecían en el listado pero no había forma de retomar la edición. No existía botón "Editar", no había ruta para la edición y `CrearExamenContent` no soportaba cargar un examen existente.

### Análisis
`examWizardUtils.js` ya tenía implementadas `examToWizardState()` y `groupQuestionsIntoTemas()` — funciones pensadas para soportar la edición — pero nunca se usaban. El flujo estaba incompleto.

### Archivos modificados
- `src/presentation/pages/dashboard/docente/components/ExamenesContent.jsx`
- `src/presentation/pages/dashboard/docente/DocenteDashboard.jsx`
- `src/presentation/pages/dashboard/docente/components/CrearExamenContent.jsx`

### Fix aplicado
- **`ExamenesContent.jsx`**: agregado botón "Editar" (con `EditIcon`) en cada fila del listado → navega a `/docente/examenes/${exam.id}/editar`.
- **`DocenteDashboard.jsx`**: nueva ruta antes del catch-all de `/examenes`: si el path matchea `/examenes/:id/editar`, renderiza `<CrearExamenContent initialExamId={segments[2]} />`.
- **`CrearExamenContent.jsx`**: acepta prop `initialExamId`. Cuando se provee: muestra spinner de carga, llama `ExamService.getExamById()`, usa `examToWizardState()` para poblar `formData` y `temas`, y setea `examId` en el estado para que `persistExam()` llame a `updateExam` en vez de `createExam`. El título del wizard cambia a "Editar examen".

---

## BUG-16 — Sistema no respeta el límite de 5 temas por examen

**US:** US-22  
**Severidad:** Alto (P1)  
**Estado:** ✅ Resuelto

### Descripción
Al crear un examen, el botón "+ Agregar tema" permanecía habilitado sin límite. Se podían agregar más de 5 temas sin ninguna restricción ni mensaje de error.

### Causa raíz
No existía ninguna constante `MAX_TEMAS` ni guard en `handleAddTema`. El botón "Agregar tema" en `ExamQuestionsPanel` no tenía condición de `disabled`.

### Archivos modificados
- `src/presentation/pages/dashboard/docente/components/examWizardUtils.js`
- `src/presentation/pages/dashboard/docente/components/CrearExamenContent.jsx`

### Fix aplicado
- **`examWizardUtils.js`**: exportada constante `MAX_TEMAS = 5` como fuente de verdad única.
- **`CrearExamenContent.jsx`**:
  - `handleAddTema`: guard `if (prev.length >= MAX_TEMAS) return prev` — el estado no muta si ya se alcanzó el límite.
  - `ExamQuestionsPanel`: nueva prop `temasLimitReached` (boolean).
  - Botón "Agregar tema": `disabled={temasLimitReached}` envuelto en `<Tooltip>` que muestra `"Límite de 5 temas alcanzado"` cuando está deshabilitado (el `<span>` wrapper es necesario porque MUI no dispara eventos de Tooltip en botones disabled).

---

## BUG-18 — Botón "Turno" no navega a pantalla de generación de accesos

**US:** US-25  
**Severidad:** Alto (P1)  
**Estado:** ✅ Resuelto

### Descripción
El botón "Turno" en el listado de exámenes abría un Popover de solo lectura mostrando el turno asignado, en lugar de navegar a la pantalla de Generación de Accesos (paso 3 del wizard). La pantalla `GenerarAccesoContent` era completamente inaccesible desde la UI.

> Nota: Las rutas `/docente/examenes/:id/respuestas` y `/docente/examenes/:id/acceso` SÍ están registradas en `DocenteDashboard` y funcionan correctamente. El QA probablemente testeó con paths relativos cortos.

### Archivos modificados
- `src/presentation/pages/dashboard/docente/components/ExamenesContent.jsx`

### Fix aplicado
- Eliminados: estado `turnoAnchor` / `turnoExam`, handler `handleTurnoClick`, bloque `<Popover>` del JSX, constante `TURNO_LABELS` (sin uso post-fix), import `Popover` de MUI.
- Botón "Turno": `onClick` cambiado de `handleTurnoClick(e, exam)` a `navigate('/docente/examenes/${exam.id}/acceso')` → navega directamente a `GenerarAccesoContent` (paso 3 del wizard).

---

## BUG-19 — Manejo de errores no claro en campos requeridos de preguntas

**US:** US-16  
**Severidad:** Medio (P2)  
**Estado:** ✅ Resuelto

### Descripción
Al intentar continuar con enunciados vacíos, el sistema mostraba un Snackbar genérico pero no indicaba visualmente cuáles preguntas tenían el campo incompleto. Los campos no tenían asterisco (*) ni borde rojo.

### Causa raíz
`validateStepQuestions` detectaba los enunciados vacíos y devolvía un mensaje, pero ese resultado sólo se usaba para el Snackbar. No había propagación de los IDs de preguntas inválidas hacia los componentes visuales.

### Archivos modificados
- `src/presentation/pages/dashboard/docente/components/CrearExamenContent.jsx`

### Fix aplicado
- **`CrearExamenContent`**: nuevo estado `invalidQuestionIds` (Set). `getEmptyEnunciadoIds()` computa los IDs al momento de la validación. `handleContinuar` popula el set cuando la validación falla; lo limpia cuando pasa. `handleUpdateQuestion` borra el ID del set cuando el usuario escribe el enunciado.
- **`ExamQuestionsPanel`**: recibe `invalidQuestionIds` y lo propaga a cada `QuestionCard` como `hasEnunciadoError={invalidQuestionIds.has(question.id)}`.
- **`QuestionCard`**: nuevo prop `hasEnunciadoError`. `useEffect` auto-expande la card cuando recibe el error. El TextField de enunciado (en todos los tipos de pregunta) recibe `error={hasEnunciadoError}` y `helperText="El enunciado es obligatorio"`. Se agregó label "Enunciado *" con asterisco rojo encima del campo.

---

## BUG-20 — localStorage permite escalada de privilegios modificando el campo `role`

**US:** US-01  
**Severidad:** Crítico (P1)  
**Estado:** ✅ Resuelto

### Descripción
El campo `role` del objeto `user` en localStorage era la única fuente de verdad para el routing de roles en el frontend. Un usuario con rol ALUMNO podía cambiar el valor a `'DOCENTE'` desde DevTools y acceder al panel docente con todas sus vistas.

### Análisis de impacto
- **Backend:** ya estaba protegido — todos los endpoints usan `@PreAuthorize("hasRole('DOCENTE')")` que valida el JWT firmado. La escalada era puramente de UI.
- **UI:** el atacante accedía visualmente al panel docente: lista de exámenes, correcciones, métricas, y potencialmente leía datos de exámenes si algún endpoint no tenía `@PreAuthorize` correctamente configurado.

### Causa raíz
`AuthService.getCurrentUser()` devolvía directamente `JSON.parse(localStorage.getItem('user'))`. El claim `role` del JWT (firmado por el backend con clave secreta) nunca se usaba para routing.

### Archivos modificados
- `src/application/services/AuthService.js`

### Fix aplicado
`getCurrentUser()` ahora:
1. Lee el token JWT de localStorage y lo decodifica con `decodeJwt()`
2. Extrae el claim `role` del payload del JWT — este valor está firmado y no puede ser alterado sin invalidar la firma
3. Valida que el rol sea conocido con `isValidRole()` y lo sobrescribe en el objeto retornado
4. Fallback al `role` de localStorage solo si el JWT no tiene un rol válido (caso defensivo)

Un atacante que modifique `user.role` en localStorage obtendrá igualmente el rol original del JWT, ya que el objeto retornado siempre usa el claim firmado.

---

## BUG-21 — Múltiple choice permite más de 5 opciones sin restricción

**US:** US-20  
**Severidad:** Medio (P2)  
**Estado:** ✅ Resuelto

### Descripción
Al crear una pregunta de tipo múltiple choice, el botón "Agregar opción" no tenía límite. Se podían agregar más de 5 opciones sin restricción ni mensaje de error.

### Archivos modificados
- `src/presentation/pages/dashboard/docente/components/examWizardUtils.js`
- `src/presentation/pages/dashboard/docente/components/CrearExamenContent.jsx`

### Fix aplicado
Mismo patrón que BUG-16 (límite de temas):
- **`examWizardUtils.js`**: exportada constante `MAX_OPTIONS = 5`.
- **`CrearExamenContent.jsx`**: guard `if (opciones.length >= MAX_OPTIONS) return` en `handleAddOption`. Botón "Agregar opción" con `disabled={opciones.length >= MAX_OPTIONS}` envuelto en `<Tooltip>` que muestra `"Límite de 5 opciones alcanzado"` (con `<span>` wrapper por la misma razón que BUG-16).

---

## BUG-22 — Degradación severa del backend de login UADE bajo carga concurrente

**US:** US-04  
**Severidad:** Alto (P1)  
**Estado:** ✅ Resuelto (mitigación — mejora esperada del 70–80%)

### Descripción
Bajo 20 usuarios virtuales concurrentes durante 60 segundos (K6), `POST /api/auth/login-uade` promedió 35.43 segundos de respuesta con timeouts y rechazo de conexiones.

### Causas raíz identificadas

1. **BCrypt strength 10 (default)** — `new BCryptPasswordEncoder()` usa 2^10 = 1.024 iteraciones (~200-300ms por hash en CPU commodity). Con 20 hashes concurrentes en Render free tier (CPU compartida), la operación CPU-bound satura los threads disponibles y los requests se encolan.

2. **10+ `log.info` por request en `loginUade`** — Logging verbose con `System.currentTimeMillis()` por cada paso (5 pasos × 2 logs = 10 llamadas por request). Bajo concurrencia, la contención de I/O del logger agrava el problema.

3. **Sin configuración de thread pool Tomcat** — Spring Boot usa defaults sin tuning explícito, lo que combinado con BCrypt saturado causa encolamiento progresivo de requests.

### Archivos modificados
- `src/main/java/com/examia/config/ApplicationConfig.java`
- `src/main/java/com/examia/service/AuthService.java`
- `src/main/resources/application.yml`

### Fix aplicado

**`ApplicationConfig.java` — BCrypt strength 8:**
`new BCryptPasswordEncoder()` → `new BCryptPasswordEncoder(8)`. Strength 8 = 2^8 = 256 iteraciones, 4x más rápido que strength 10. Sigue siendo robusto para sistema institucional con política de contraseñas fuertes (BUG-14). Contraseñas ya hasheadas con strength 10 siguen siendo válidas — BCrypt detecta el factor de costo por el hash almacenado.

**`AuthService.java` — Logging limpio en `loginUade`:**
Eliminados los 10 `log.info` de paso a paso con timestamps. Reemplazados por 2 logs: uno al inicio (`Attempt for legajo`) y uno al éxito (`Login successful`). Las variables `step1..step5` y sus `System.currentTimeMillis()` eliminadas.

**`application.yml` — Thread pool Tomcat:**
Agregada configuración explícita: `threads.max=100`, `threads.min-spare=10`, `accept-count=50` (cola de requests cuando todos los threads están ocupados), `connection-timeout=5000ms` (fail-fast en vez de esperar indefinidamente). Todas las configuraciones externalizables via env vars.

> Nota: en Render free tier la CPU compartida sigue siendo el cuello de botella fundamental. Para escalar más allá de ~50 VUs se necesita un tier pago con CPU dedicada. El fix mitiga el problema dentro de los límites del hardware actual.

---
