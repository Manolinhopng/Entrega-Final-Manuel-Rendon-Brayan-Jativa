# 🧪 Plan de Pruebas para las APIs de GymTracker

## 📋 Resumen de Endpoints

### Base URL
```
http://localhost:3000
```

### Endpoints Disponibles

1. **Autenticación** (`/api/auth`)
   - `POST /api/auth/register` - Registrar nuevo usuario
   - `POST /api/auth/login` - Iniciar sesión

2. **Perfil** (`/api/profile`)
   - `GET /api/profile/:userId` - Obtener perfil de usuario
   - `PUT /api/profile/:userId` - Actualizar perfil de usuario

3. **Rutinas** (`/api/routines`)
   - `GET /api/routines?userId=XXX` - Obtener rutinas de un usuario
   - `POST /api/routines` - Crear nueva rutina

---

## 🚀 Plan de Pruebas Paso a Paso

### **FASE 1: Verificar Servidor**

#### 1.1 Health Check
```bash
# Verificar que el servidor está corriendo
curl http://localhost:3000/
```

**Respuesta esperada:**
```json
{
  "message": "GymTracker Backend - API v1 ✅"
}
```

---

### **FASE 2: Autenticación (Auth)**

#### 2.1 Registrar Usuario Nuevo ✅
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "age": 25,
    "email": "juan@example.com",
    "weight": 75,
    "height": 175,
    "gender": "male"
  }'
```

**Respuesta esperada (201):**
```json
{
  "success": true,
  "userId": "abc123...",
  "message": "Registro exitoso"
}
```

**⚠️ IMPORTANTE:** Guarda el `userId` para las siguientes pruebas.

---

#### 2.2 Registrar Usuario Duplicado ❌
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "age": 25,
    "email": "juan@example.com",
    "weight": 75,
    "height": 175,
    "gender": "male"
  }'
```

**Respuesta esperada (400):**
```json
{
  "success": false,
  "message": "Correo ya registrado"
}
```

---

#### 2.3 Login con Usuario Existente ✅
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "password": "tu_password_aqui"
  }'
```

**Nota:** Actualmente el login busca por email pero no valida password (necesitas agregar password al registro primero).

**Respuesta esperada (200):**
```json
{
  "success": true,
  "userId": "abc123...",
  "message": "Login exitoso"
}
```

---

#### 2.4 Login con Usuario Inexistente ❌
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "noexiste@example.com",
    "password": "password123"
  }'
```

**Respuesta esperada (401):**
```json
{
  "success": false,
  "message": "Usuario no encontrado"
}
```

---

### **FASE 3: Perfil (Profile)**

**⚠️ Necesitas un `userId` válido de la Fase 2**

#### 3.1 Obtener Perfil Existente ✅
```bash
# Reemplaza USER_ID con el userId obtenido en el registro
curl http://localhost:3000/api/profile/LJKEThbpJ0e3p5z69cRg
```

**Respuesta esperada (200):**
```json
{
  "id": "abc123...",
  "name": "Juan Pérez",
  "age": 25,
  "email": "juan@example.com",
  "weight": 75,
  "height": 175,
  "gender": "male",
  "trainingFrequency": "",
  "dietType": "",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

#### 3.2 Obtener Perfil Inexistente ❌
```bash
curl http://localhost:3000/api/profile/usuario_inexistente_123
```

**Respuesta esperada (404):**
```json
{
  "message": "Usuario no encontrado"
}
```

---

#### 3.3 Actualizar Perfil ✅
```bash
# Reemplaza USER_ID con el userId obtenido
curl -X PUT http://localhost:3000/api/profile/USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "weight": 80,
    "height": 175,
    "trainingFrequency": "5 veces por semana",
    "dietType": "Proteica"
  }'
```

**Respuesta esperada (200):**
```json
{
  "id": "abc123...",
  "name": "Juan Pérez",
  "age": 25,
  "email": "juan@example.com",
  "weight": 80,
  "height": 175,
  "gender": "male",
  "trainingFrequency": "5 veces por semana",
  "dietType": "Proteica"
}
```

---

### **FASE 4: Rutinas (Routines)**

**⚠️ Necesitas un `userId` válido**

#### 4.1 Obtener Rutinas de un Usuario ✅
```bash
# Reemplaza USER_ID con el userId obtenido
curl "http://localhost:3000/api/routines?userId=USER_ID"
```

**Respuesta esperada (200):**
```json
[]
```
O si hay rutinas:
```json
[
  {
    "id": "routine123...",
    "userId": "abc123...",
    "date": "2024-01-15",
    "exercises": [...]
  }
]
```

---

#### 4.2 Obtener Rutinas sin userId ❌
```bash
curl http://localhost:3000/api/routines
```

**Respuesta esperada (400):**
```json
{
  "message": "userId requerido"
}
```

---

#### 4.3 Crear Nueva Rutina ✅
```bash
curl -X POST http://localhost:3000/api/routines \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "USER_ID",
    "date": "2024-01-15",
    "name": "Rutina de Pecho",
    "exercises": [
      {
        "name": "Press de Banca",
        "sets": 4,
        "reps": 10,
        "weight": 80
      },
      {
        "name": "Aperturas",
        "sets": 3,
        "reps": 12,
        "weight": 25
      }
    ]
  }'
```

**Respuesta esperada (201):**
```json
{
  "id": "routine456...",
  "userId": "abc123...",
  "date": "2024-01-15",
  "name": "Rutina de Pecho",
  "exercises": [...]
}
```

---

## 🛠️ Herramientas de Prueba

### Opción 1: cURL (Línea de comandos)
Usa los comandos curl mostrados arriba.

### Opción 2: Postman
1. Importa la colección (ver abajo)
2. Configura la variable `base_url` = `http://localhost:3000`
3. Configura la variable `userId` después del registro

### Opción 3: Thunder Client (VS Code)
1. Instala la extensión Thunder Client
2. Crea las requests manualmente usando los ejemplos

### Opción 4: HTTPie
```bash
# Instalar HTTPie
pip install httpie

# Ejemplo de uso
http POST localhost:3000/api/auth/register name="Juan" age:=25 email="juan@test.com"
```

---

## 📝 Checklist de Pruebas

### ✅ Autenticación
- [ ] Registrar usuario nuevo (éxito)
- [ ] Registrar usuario duplicado (error)
- [ ] Login con usuario existente (éxito)
- [ ] Login con usuario inexistente (error)
- [ ] Login con contraseña incorrecta (error)

### ✅ Perfil
- [ ] Obtener perfil existente (éxito)
- [ ] Obtener perfil inexistente (error 404)
- [ ] Actualizar perfil (éxito)
- [ ] Actualizar perfil inexistente (error)

### ✅ Rutinas
- [ ] Obtener rutinas con userId válido (éxito)
- [ ] Obtener rutinas sin userId (error 400)
- [ ] Crear rutina nueva (éxito)
- [ ] Verificar que las rutinas se guardan correctamente

---

## 🔍 Verificación en Firebase Console

Después de las pruebas, verifica en Firebase Console:

1. **Firestore Database** → Colección `users`
   - Debe tener los usuarios registrados
   - Verifica que los campos estén correctos

2. **Firestore Database** → Colección `routines`
   - Debe tener las rutinas creadas
   - Verifica que el `userId` coincida

---

## 🐛 Problemas Comunes

### Error: "Cannot find module"
- Verifica que todas las rutas de importación sean correctas
- Ejecuta `npm install` si falta alguna dependencia

### Error: "Firebase Admin no inicializado"
- Verifica que el archivo `.env` esté configurado correctamente
- Verifica que las credenciales de Firebase sean válidas

### Error: "Collection not found"
- Las colecciones se crean automáticamente al agregar el primer documento
- Verifica que tengas permisos en Firebase

### Error: "userId requerido"
- Asegúrate de pasar el `userId` como query parameter o en el body según el endpoint

---

## 📊 Orden Recomendado de Pruebas

1. ✅ Verificar servidor (GET `/`)
2. ✅ Registrar usuario (POST `/api/auth/register`)
3. ✅ Guardar el `userId` recibido
4. ✅ Login (POST `/api/auth/login`)
5. ✅ Obtener perfil (GET `/api/profile/:userId`)
6. ✅ Actualizar perfil (PUT `/api/profile/:userId`)
7. ✅ Crear rutina (POST `/api/routines`)
8. ✅ Obtener rutinas (GET `/api/routines?userId=XXX`)

---

## 🎯 Próximos Pasos

Después de probar todas las APIs:

1. **Agregar validación de datos** (Joi, express-validator)
2. **Agregar autenticación JWT** para proteger endpoints
3. **Agregar manejo de errores más detallado**
4. **Agregar tests automatizados** (Jest, Supertest)
5. **Documentar con Swagger/OpenAPI**

