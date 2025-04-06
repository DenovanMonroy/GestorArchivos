# Gestor de archivos

## Descripción

Esta es una aplicación móvil de gestión de archivos desarrollada en Android Studio utilizando Kotlin y Jetpack Compose. La aplicación permite a los usuarios explorar directorios de almacenamiento interno y externo, visualizar la estructura jerárquica de carpetas y archivos, y abrir y visualizar archivos de texto e imágenes.

## Características

- **Exploración de Archivos**: Navega por los directorios del almacenamiento interno y externo del dispositivo.
- **Visualización de Estructura de Archivos**: Muestra la jerarquía de carpetas y archivos en una lista ordenada.
- **Apertura de Archivos de Texto**: Abre y visualiza archivos de texto (.txt, .md, etc.).
- **Visualización de Imágenes**: Abre y visualiza imágenes comunes (JPG, PNG, GIF, BMP, WEBP).
- **Interfaz de Usuario Intuitiva**: Utiliza Jetpack Compose para una experiencia de usuario fluida y moderna.
- **Zoom y Desplazamiento en Imágenes**: Permite hacer zoom y desplazarse por las imágenes con gestos.

## Requisitos

- Android Studio
- Kotlin
- Jetpack Compose
- Dispositivo o Emulador con Android API 21 o superior

## Instalación

1. **Clonar el Repositorio**:
   ```sh
[   git clone https://github.com/DenovanMonroy/file-explorer-app.git
](https://github.com/DenovanMonroy/file-explorer-app.git)   ```

2. **Abrir el Proyecto en Android Studio**:
   - Abre Android Studio.
   - Selecciona `File > Open` y navega hasta el directorio donde clonaste el repositorio.
   - Selecciona la carpeta del proyecto y haz clic en `OK`.

3. **Configurar Dependencias**:
   - Asegúrate de que tienes configuradas las dependencias necesarias en el archivo `build.gradle.kts` a nivel de módulo:
     ```kotlin
     dependencies {
         implementation(libs.androidx.core.ktx)
         implementation(libs.androidx.lifecycle.runtime.ktx)
         implementation(libs.androidx.activity.compose)
         implementation(platform(libs.androidx.compose.bom))
         implementation(libs.androidx.ui)
         implementation(libs.androidx.ui.graphics)
         implementation(libs.androidx.ui.tooling.preview)
         implementation(libs.androidx.material3)
         implementation(libs.androidx.lifecycle.viewmodel.compose)
         implementation(libs.androidx.material.icons.extended)
         implementation(libs.androidx.lifecycle.viewmodel.ktx)
         implementation(libs.kotlinx.coroutines.android)
     }
     ```

4. **Ejecutar la Aplicación**:
   - Conecta un dispositivo físico o inicia un emulador.
   - Haz clic en el botón `Run` en Android Studio para compilar y ejecutar la aplicación.

## Uso

### Exploración de Archivos

- Navega por las carpetas tocando en las carpetas listadas.
- Usa el botón de navegación superior para volver a un directorio anterior.

### Visualización de Archivos de Texto

- Toca en un archivo de texto para abrirlo.
- El contenido del archivo se mostrará en una nueva pantalla.

### Visualización de Imágenes

- Toca en un archivo de imagen para abrirlo.
- Puedes hacer zoom y desplazarte por la imagen utilizando gestos.

## Capturas de Pantalla

### Pantalla principal 
![image](https://github.com/user-attachments/assets/3be7c73a-639e-4303-ba93-f80752125363)

### Archivos de texto

![image](https://github.com/user-attachments/assets/28d1f2cc-940d-48b5-a068-5b84f2f3d6e3)

### Imagenes
![image](https://github.com/user-attachments/assets/3d630281-b3f7-4322-bd5e-650475943b3a)

### Archivos no reconocidos 

![image](https://github.com/user-attachments/assets/008b5642-0193-4663-a20c-d0f610100ec0)

```

