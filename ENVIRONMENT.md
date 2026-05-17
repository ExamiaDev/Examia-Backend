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

## Archivo de Configuración

1. Copia `application.yml.example` a `application.yml`
2. Configura las variables de entorno según tu entorno
3. Para desarrollo local, puedes usar valores por defecto
4. Para producción, siempre usa variables de entorno

## Seguridad

- **Nunca** subas `application.yml` con valores reales al repositorio
- Usa variables de entorno para valores sensibles
- El archivo `application.yml` actual tiene valores de desarrollo seguros
