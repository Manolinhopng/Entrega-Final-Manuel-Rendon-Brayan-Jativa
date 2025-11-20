# 🏋️‍♂️ GymTracker — Aplicación de Seguimiento de Entrenamientos

> *Proyecto Final del Curso de Desarrollo de Aplicaciones Móviles — Unicatolica*  
> **Equipo:** Manuel Fernando Rendón Orrego & Brayan Arnaldo Jativa Cuarán  
> **Tecnologías:** Kotlin, Android (XML + Activities), Firebase Authentication & Firestore  


---

## 📌 Descripción de la Aplicación

**GymTracker** es una aplicación móvil diseñada para que los usuarios registren, gestionen y analicen sus rutinas de entrenamiento en el gimnasio de forma sencilla, segura y sin depender de una conexión constante a internet.

La app permite:

- ✅ **Registrar y autenticar usuarios** mediante correo y contraseña (Firebase Authentication).  
- ✅ **Crear, editar y eliminar rutinas** de entrenamiento con nombre, duración, fecha y ejercicios.  
- ✅ **Agregar ejercicios** a cada rutina con peso, series y repeticiones.  
- ✅ **Visualizar el historial de rutinas** ordenado por fecha descendente.  
- ✅ **Calcular automáticamente el volumen total levantado** (series × repeticiones × peso).  
- ✅ **Exportar el historial de entrenamientos** como archivo **CSV o PDF**.  
- ✅ **Editar el perfil personal** del usuario (nombre, edad, peso, altura, género, frecuencia de entrenamiento, tipo de dieta).  
- ✅ **Almacenar todos los datos en la nube** mediante **Firebase Firestore**, garantizando sincronización, respaldo y acceso desde cualquier dispositivo.

La aplicación está desarrollada con **Kotlin y Android XML Layouts** (Jetpack Compose), usando el patrón **MVVM** para una arquitectura limpia, escalable y fácil de mantener. Todos los datos se comunican con un backend REST mediante **Retrofit**, y se almacenan en **Firebase Firestore** para máxima confiabilidad.

> 💡 *Ideal para usuarios que buscan un control personalizado, privado y sin anuncios de sus progresos en el gimnasio.*

---

## 👥 Equipo de Desarrollo

| Nombre | Rol |
|--------|-----|
| **Manuel Fernando Rendón Orrego ID: 407384** | Desarrollador Frontend, UI/UX, Gestión de APIs |
| **Brayan Arnaldo Jativa Cuarán ID: 407368** | Desarrollador Backend, Base de Datos, Exportación de Archivos |

---

## ▶️ Cómo Ejecutar la Aplicación

### 🔧 Requisitos Previos

- Android Studio (Dolphin or superior recomendado)
- Java Development Kit (JDK) 11 o superior
- Android SDK with API level 28 o superior
- Gradle

- Abre Android Studio
- Selecciona "Open an existing project" y navega al directorio del proyecto
- Haz clic en "Open"
- Conecta un dispositivo físico o inicia un emulador Android
- Haz clic en el botón "Run" (el ícono de triángulo verde) o presiona Shift + F10
---

## 📁 Estructura del Proyecto

```
app/
├── manifests/
│   └── AndroidManifest.xml
├── java/
│   └── com.unicatolica.gymtracker/
│       ├── api/
│       │   ├── ApiClient.kt        
│       │   └── ApiService.kt        
│       ├── data/
│       │   ├── ApiRepository.kt      
│       │   ├── Exercise.kt          
│       │   ├── Routine.kt            
│       │   ├── User.kt               
│       │   ├── CreateRoutineRequest.kt
│       │   ├── LoginRequest.kt
│       │   ├── RoutineResponse.kt
│       │   └── ... (otros modelos)
│       ├── ui.theme/                 
│       │   └── Theme.kt
│       └── viewmodel/                
│           ├── MainActivity.kt
│           ├── DashboardActivity.kt
│           ├── TrainingHistoryActivity.kt
│           ├── ProfileActivity.kt
│           ├── RegisterActivity.kt
│           ├── EditRoutineActivity.kt
│           └── RoutineInProgressActivity.kt
├── res/
│   ├── drawable/                     
│   ├── layout/                       
│   ├── values/
│   │   ├── colors.xml                
│   │   ├── strings.xml               
│   │   └── themes.xml                
│   └── mipmap/                       
└── build.gradle.kts                  
```

---

## ✅ Características Técnicas

| Característica | Tecnología |
|----------------|------------|
| Lenguaje | Kotlin |
| UI Framework | Android XML Layouts |
| Arquitectura | MVVM (Model-View-ViewModel) |
| Base de Datos | Firebase Firestore |
| Autenticación | Firebase Authentication |
| API REST | Retrofit + Gson |
| Persistencia Local | SharedPreferences |
| Exportación | CSV y PDF (iText) |
| Manejo de errores | Toasts y respuestas HTTP controladas |
| Internacionalización | `strings.xml` con soporte para múltiples idiomas |


## 📚 Referencias

- [Firebase Documentation](https://firebase.google.com/docs)
- [Retrofit Official Guide](https://square.github.io/retrofit/)
- [Android Developer Guide](https://developer.android.com/guide)

---

## 🤝 Contribuciones

¡Este proyecto es abierto! Si deseas mejorar la app, puedes:

- Añadir soporte para Jetpack Compose
- Implementar notificaciones de recordatorio
- Agregar gráficas de progreso
- Optimizar el rendimiento

Haz un *Fork*, crea tu rama y envía un *Pull Request*.

---

> 💡 **Nota final**: Esta app fue desarrollada como proyecto final académico. No utiliza publicidad ni recopila datos personales fuera del propósito de registro de entrenamientos. Todos los datos son propiedad del usuario.

---

