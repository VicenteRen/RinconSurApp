# RinconSurApp - Registro y Control de Accesos [MVP]

App móvil nativa desarrollada en Android (Kotlin) para gestionar controles de acceso y segmentar el flujo de clientes (transeúntes) hacia la tienda de manera intuitiva y expedita. 

Este proyecto fue desarrollado como parte de la carrera de Ingeniería en Informática (Duoc UC).

## 📌 Características Principales

La aplicación es un MVP que cuenta con un sistema de perfiles y una base de datos local para el manejo seguro de la información:

* **Sistema de Roles:** Acceso diferenciado para **Observador** (quien registra en terreno) y **Administrador** (quien gestiona el sistema).
* **Registro Rápido (Observación):** Interfaz ágil para registrar variables clave de los transeúntes:
  * Punto de acceso de destino (Principal, Lateral, Parking, Conexión).
  * Clasificación (Tránsito, Visita, etc.).
  * Rango etario y tipo de compañía (Solo, Pareja, Niños, Grupo).
  * Variable de compras externas.
* **Gestión de Usuarios:** Módulo de administración para crear, editar, deshabilitar y restaurar credenciales de los empleados.
* **Seguridad:** Encriptación de contraseñas mediante algoritmo **SHA-256** y sistema de recuperación con preguntas de seguridad.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Base de Datos:** SQLite nativo (`SQLiteOpenHelper`)
* **Control de Versiones:** Git / GitHub 

## 🚀 Cómo ejecutar el proyecto

1. Clona este repositorio en tu equipo local: `git clone https://github.com/TU_USUARIO/RinconSurApp.git`
2. Abre el proyecto utilizando **Android Studio**.
3. Espera a que Gradle sincronice las dependencias del proyecto.
4. Ejecuta la aplicación en un emulador o en un dispositivo físico con Android (mediante depuración USB).

## 👥 Autores

* **Emilia Gallardo** 
* **Vicente Ponce**
* **Vicente Céspedes** 
