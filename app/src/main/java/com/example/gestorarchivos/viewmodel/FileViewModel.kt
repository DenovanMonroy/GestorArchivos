package com.example.gestorarchivos.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorarchivos.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileViewModel : ViewModel() {

    private val _currentDirectory = MutableStateFlow<File?>(null)
    val currentDirectory: StateFlow<File?> = _currentDirectory.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _fileContent = MutableStateFlow<String>("")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    // Nuevo estado para imágenes
    private val _currentImage = MutableStateFlow<ImageBitmap?>(null)
    val currentImage: StateFlow<ImageBitmap?> = _currentImage.asStateFlow()

    // Estado para controlar qué tipo de archivo se está visualizando
    private val _viewerType = MutableStateFlow(ViewerType.NONE)
    val viewerType: StateFlow<ViewerType> = _viewerType.asStateFlow()

    // Estado para mantener el nombre del archivo actual
    private val _currentFileName = MutableStateFlow<String>("")
    val currentFileName: StateFlow<String> = _currentFileName.asStateFlow()

    // Historial para navegación
    private val directoryHistory = mutableListOf<File>()

    fun initializeDirectory(context: Context) {
        viewModelScope.launch {
            val initialDir = Environment.getExternalStorageDirectory()
            if (initialDir.exists() && initialDir.canRead()) {
                navigateToDirectory(initialDir)
            } else {
                // Usar almacenamiento interno como fallback
                navigateToDirectory(context.filesDir)
            }
        }
    }

    fun navigateToDirectory(directory: File) {
        if (directory.isDirectory && directory.canRead()) {
            _currentDirectory.value?.let { directoryHistory.add(it) }
            _currentDirectory.value = directory
            loadFiles(directory)
        }
    }

    fun navigateBack(): Boolean {
        if (_viewerType.value != ViewerType.NONE) {
            closeViewer()
            return true
        }

        if (directoryHistory.isEmpty()) return false

        val previousDir = directoryHistory.removeAt(directoryHistory.size - 1)
        _currentDirectory.value = previousDir
        loadFiles(previousDir)
        return true
    }

    fun navigateUp(): Boolean {
        val current = _currentDirectory.value ?: return false
        val parent = current.parentFile ?: return false

        if (parent.canRead()) {
            directoryHistory.add(current)
            _currentDirectory.value = parent
            loadFiles(parent)
            return true
        }
        return false
    }

    private fun loadFiles(directory: File) {
        viewModelScope.launch {
            Log.d("FileExplorer", "Cargando archivos de: ${directory.absolutePath}")
            val filesList = directory.listFiles()?.mapNotNull { file ->
                if (file.canRead()) {
                    Log.d("FileExplorer", "Archivo encontrado: ${file.name}, Es directorio: ${file.isDirectory}")
                    FileItem(file)
                } else {
                    Log.d("FileExplorer", "Archivo sin permiso de lectura: ${file.name}")
                    null
                }
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()

            _files.value = filesList
            Log.d("FileExplorer", "Total de archivos cargados: ${filesList.size}")
        }
    }

    fun openFile(fileItem: FileItem) {
        _currentFileName.value = fileItem.name

        if (fileItem.isDirectory) {
            navigateToDirectory(fileItem.file)
        } else if (isImageFile(fileItem.name)) {
            openImageFile(fileItem.file)
        } else if (isTextFile(fileItem.name)) {
            readTextFile(fileItem.file)
        } else {
            _fileContent.value = "Este tipo de archivo no puede ser visualizado directamente.\nRuta: ${fileItem.path}\nTamaño: ${FileItem.formatSize(fileItem.size)}"
            _viewerType.value = ViewerType.TEXT
        }
    }

    private fun readTextFile(file: File) {
        viewModelScope.launch {
            try {
                if (file.exists()) {
                    if (file.canRead()) {
                        val content = file.readText()
                        if (content.isBlank()) {
                            _fileContent.value = "[El archivo está vacío]"
                        } else {
                            _fileContent.value = content
                        }
                        _viewerType.value = ViewerType.TEXT
                    } else {
                        _fileContent.value = "El archivo existe pero no se puede leer debido a permisos insuficientes.\nRuta: ${file.absolutePath}"
                        _viewerType.value = ViewerType.TEXT
                    }
                } else {
                    _fileContent.value = "El archivo no existe.\nRuta: ${file.absolutePath}"
                    _viewerType.value = ViewerType.TEXT
                }
            } catch (e: Exception) {
                _fileContent.value = "Error al leer el archivo: ${e.message}\nTipo: ${e.javaClass.simpleName}\nRuta: ${file.absolutePath}"
                _viewerType.value = ViewerType.TEXT
            }
        }
    }

    private fun openImageFile(file: File) {
        viewModelScope.launch {
            try {
                if (file.exists() && file.canRead()) {
                    withContext(Dispatchers.IO) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            _currentImage.value = bitmap.asImageBitmap()
                            _viewerType.value = ViewerType.IMAGE
                        } else {
                            _fileContent.value = "No se pudo cargar la imagen. El formato podría no ser compatible."
                            _viewerType.value = ViewerType.TEXT
                        }
                    }
                } else {
                    _fileContent.value = "No se puede acceder a la imagen.\nRuta: ${file.absolutePath}"
                    _viewerType.value = ViewerType.TEXT
                }
            } catch (e: Exception) {
                _fileContent.value = "Error al abrir la imagen: ${e.message}"
                _viewerType.value = ViewerType.TEXT
            }
        }
    }

    fun closeViewer() {
        _fileContent.value = ""
        _currentImage.value = null
        _viewerType.value = ViewerType.NONE
        _currentFileName.value = ""
    }

    fun setFileContent(content: String) {
        _fileContent.value = content
    }

    fun isTextFile(fileName: String): Boolean {
        val textExtensions = listOf(
            "txt", "md", "json", "xml", "html", "css", "js", "kt", "java",
            "py", "c", "cpp", "h", "hpp", "csv", "log", "ini", "properties",
            "yaml", "yml", "toml", "gradle", "gitignore", "sh", "bat", "config"
        )
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in textExtensions
    }

    fun isImageFile(fileName: String): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in imageExtensions
    }
}

enum class ViewerType {
    NONE,
    TEXT,
    IMAGE
}