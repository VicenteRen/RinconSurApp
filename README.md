
# Rincón Sur - Aplicación Android

Aplicación móvil desarrollada en **Kotlin y Jetpack Compose** para el registro y control de accesos y observaciones de transeúntes en un entorno comercial.

## Tecnologías utilizadas

- Kotlin y Jetpack Compose (Material Design 3)
- Arquitectura MVVM
- Retrofit y Gson
- Kotlin Coroutines y StateFlow
- Navigation Compose
- JUnit y Mockito
- Android Studio y Gradle
- Git y GitHub

## Funcionalidades principales

La aplicación cuenta con dos roles: **Administrador** y **Observador**.

- Inicio de sesión y recuperación de contraseña.
- Gestión de usuarios y perfiles.
- Registro, consulta y anulación de observaciones.
- Administración de puntos de acceso.
- Historial de registros y estadísticas con gráficos.
- Validaciones de formularios y control de permisos según el rol.

## Integración con microservicios

La aplicación utiliza Retrofit para comunicarse con un backend desarrollado en Spring Boot mediante un API Gateway.

| Servicio | Puerto | Función |
|---|---|---|
| API Gateway | 8080 | Centraliza las solicitudes |
| Usuarios | 8082 | Autenticación y usuarios |
| Observaciones | 8081 | Gestión de observaciones |
| Accesos | 8083 | Gestión de puntos de acceso |

**Repositorio del backend:**

https://github.com/VicenteRen/Microservicios-RS

## Comunicación con el backend

La aplicación consume APIs REST mediante Retrofit, utilizando el API Gateway para comunicarse con los microservicios de Usuarios, Observaciones y Accesos.

Los endpoints y su documentación técnica se encuentran disponibles en el repositorio del backend:

https://github.com/VicenteRen/Microservicios-RS

## Instalación y ejecución

1. Clonar el repositorio:

```bash
git clone https://github.com/VicenteRen/RinconSurApp.git
```

2. Abrir el proyecto en Android Studio y sincronizar Gradle.
3. Iniciar MySQL y los microservicios del backend.
4. Conectar un emulador o dispositivo Android.

Para conectar Android con el API Gateway local (`127.0.0.1:8080`), ejecutar:

```bat
adb reverse tcp:8080 tcp:8080
```

5. Ejecutar la aplicación desde Android Studio.

## Pruebas unitarias

Se implementaron pruebas utilizando JUnit y Mockito para verificar componentes de la aplicación.

Para ejecutarlas en Windows:

```bat
gradlew.bat :app:testDebugUnitTest
```

Los resultados se encuentran en:

`app/build/reports/tests/testDebugUnitTest/index.html`

## Distribución

Para la entrega se generan:

- **APK (.apk):** aplicación instalable.
- **AAB (.aab):** paquete firmado para distribución.
- **JKS (.jks):** almacén de claves utilizado para la firma.

Estos archivos se entregan en un ZIP independiente del código fuente. El archivo JKS y sus contraseñas deben mantenerse protegidos y no publicarse en GitHub.

## Control de versiones

El desarrollo se administra mediante Git y GitHub, utilizando commits, ramas y Pull Requests.

## 👥 Autores

* **Emilia Gallardo** 
* **Vicente Ponce**
* **Vicente Céspedes** 
