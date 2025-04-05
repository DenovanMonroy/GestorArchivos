package com.example.gestorarchivos.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gestorarchivos.model.FileItem
import com.example.gestorarchivos.viewmodel.FileViewModel
import com.example.gestorarchivos.viewmodel.ViewerType
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerApp(
    viewModel: FileViewModel,
    modifier: Modifier = Modifier
) {
    val currentDirectory by viewModel.currentDirectory.collectAsState()
    val files by viewModel.files.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()
    val currentImage by viewModel.currentImage.collectAsState()
    val viewerType by viewModel.viewerType.collectAsState()
    val currentFileName by viewModel.currentFileName.collectAsState()
    val scope = rememberCoroutineScope()

    val currentPath = currentDirectory?.absolutePath ?: "/"

    DisposableEffect(Unit) {
        Log.d("FileExplorer", "FileExplorerApp iniciado")
        onDispose {
            Log.d("FileExplorer", "FileExplorerApp eliminado")
        }
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (viewerType != ViewerType.NONE) currentFileName else currentDirectory?.name ?: "Explorador de Archivos",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (viewerType == ViewerType.NONE) {
                        Text(
                            text = currentPath,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    scope.launch {
                        if (viewerType != ViewerType.NONE) {
                            viewModel.closeViewer()
                        } else {
                            Log.d("FileExplorer", "Intentando navegar hacia arriba desde: $currentPath")
                            viewModel.navigateUp()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás"
                    )
                }
            }
        )

        when (viewerType) {
            ViewerType.TEXT -> {
                Log.d("FileExplorer", "Mostrando visor de texto con contenido de longitud: ${fileContent.length}")
                TextFileViewer(
                    content = fileContent,
                    onClose = {
                        Log.d("FileExplorer", "Cerrando visor de texto")
                        viewModel.closeViewer()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            ViewerType.IMAGE -> {
                Log.d("FileExplorer", "Mostrando visor de imagen")
                currentImage?.let { image ->
                    ImageViewer(
                        image = image,
                        onClose = {
                            Log.d("FileExplorer", "Cerrando visor de imagen")
                            viewModel.closeViewer()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            else -> {
                Log.d("FileExplorer", "Mostrando lista de archivos: ${files.size} elementos")
                FilesList(
                    files = files,
                    onFileClick = { fileItem ->
                        Log.d("FileExplorer", "Clic en archivo: ${fileItem.name}, Es directorio: ${fileItem.isDirectory}")
                        viewModel.openFile(fileItem)
                    },
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel
                )
            }
        }

        // Barra inferior con información
        BottomAppBar(
            modifier = Modifier.height(48.dp),
            content = {
                Text(
                    text = when (viewerType) {
                        ViewerType.NONE -> if (files.isEmpty()) "No hay archivos" else "${files.size} elementos"
                        ViewerType.TEXT -> "Visor de texto: $currentFileName"
                        ViewerType.IMAGE -> "Visor de imagen: $currentFileName"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        )
    }
}

@Composable
fun FilesList(
    files: List<FileItem>,
    onFileClick: (FileItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FileViewModel
) {
    if (files.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Carpeta vacía",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Carpeta vacía",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(files) { fileItem ->
                FileListItem(
                    fileItem = fileItem,
                    onItemClick = onFileClick,
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    fileItem: FileItem,
    onItemClick: (FileItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FileViewModel
) {
    val isText = viewModel.isTextFile(fileItem.name)
    val isImage = viewModel.isImageFile(fileItem.name)

    Row(
        modifier = modifier
            .clickable { onItemClick(fileItem) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                fileItem.isDirectory -> Icons.Default.Folder
                isImage -> Icons.Default.Image
                isText -> Icons.Default.Description
                else -> Icons.Default.InsertDriveFile
            },
            contentDescription = when {
                fileItem.isDirectory -> "Carpeta"
                isImage -> "Imagen"
                isText -> "Archivo de texto"
                else -> "Archivo"
            },
            tint = when {
                fileItem.isDirectory -> MaterialTheme.colorScheme.primary
                isImage -> MaterialTheme.colorScheme.tertiary
                isText -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = fileItem.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fileItem.lastModified,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!fileItem.isDirectory) {
                    Text(
                        text = FileItem.formatSize(fileItem.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    Divider()
}

@Composable
fun TextFileViewer(
    content: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar"
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (content.startsWith("Error") ||
                        content.startsWith("No se puede") ||
                        content.startsWith("El archivo"))
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.verticalScroll(scrollState)
                )
            }
        }
    }
}

@Composable
fun ImageViewer(
    image: androidx.compose.ui.graphics.ImageBitmap,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)

        // Ajustar el desplazamiento con los límites
        val maxX = (image.width * scale - image.width) / 2
        val maxY = (image.height * scale - image.height) / 2

        offset = Offset(
            x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { scale = 1f; offset = Offset.Zero }) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Resetear zoom"
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar"
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = image,
                contentDescription = "Imagen",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformableState),
                contentScale = ContentScale.Fit
            )
        }
    }
}