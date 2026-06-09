# Variables de Entorno

## Configuración Requerida

### Base de Datos MongoDB
```bash
MONGODB_URI=mongodb://localhost:27017/examia
# Para MongoDB Atlas: mongodb+srv://usuario:password@cluster.mongodb.net/examia
```

### JWT Secret
```bash
# Generar un secret seguro (256 bits / 32 bytes mínimo)
# En Linux/Mac: openssl rand -base64 32
# En Windows PowerShell: [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
JWT_SECRET=YOUR_BASE64_ENCODED_SECRET_HERE
```

### Puerto del Servidor (opcional)
```bash
PORT=8080
```

### Tiempo de Expiración JWT (opcional)
```bash
JWT_EXPIRATION=86400000  # 24 horas en milisegundos
```

### Envío de Email

La app soporta **dos transportes** intercambiables vía la variable `MAIL_PROVIDER`:

| Valor              | Cómo funciona                                | Cuándo usarlo                                                                 |
|--------------------|----------------------------------------------|-------------------------------------------------------------------------------|
| `smtp` *(default)* | Usa `JavaMailSender` contra un relay SMTP    | **Local** (red sin restricciones)                                             |
| `brevo-api`        | Usa la API REST HTTP de Brevo (puerto 443)   | **Render** y cualquier PaaS que bloquee SMTP outbound (Render Free lo bloquea)|

#### Local (SMTP — Brevo)
```bash
MAIL_PROVIDER=smtp          # opcional, es el default
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=<tu-login-SMTP-de-brevo>
MAIL_PASSWORD=<tu-SMTP-key-de-brevo>
MAIL_FROM=no-reply@tudominio.com   # debe estar verificado en Brevo
MAIL_FROM_NAME=Examia
```

#### Producción / Render (API HTTP — Brevo)
```bash
MAIL_PROVIDER=brevo-api
BREVO_API_KEY=<tu-api-key-v3-de-brevo>   # generada en Account > SMTP & API > API Keys
MAIL_FROM=no-reply@tudominio.com          # verificado en Brevo
MAIL_FROM_NAME=Examia
# BREVO_API_URL es opcional; default: https://api.brevo.com/v3/smtp/email
```

> ℹ️ Render bloquea las conexiones SMTP salientes en su plan Free.
> Por eso en prod usamos la API HTTPS que va por puerto 443 (siempre abierto).
> La misma cuenta de Brevo sirve para ambos modos: solo cambian las credenciales
> (SMTP key vs API key v3).

## Archivo de Configuración

1. Copia `application.yml.example` a `application.yml`
2. Configura las variables de entorno según tu entorno
3. Para desarrollo local, puedes usar valores por defecto
4. Para producción, siempre usa variables de entorno

## Seguridad

- **Nunca** subas `application.yml` con valores reales al repositorio
- Usa variables de entorno para valores sensibles
- El archivo `application.yml` actual tiene valores de desarrollo seguros
