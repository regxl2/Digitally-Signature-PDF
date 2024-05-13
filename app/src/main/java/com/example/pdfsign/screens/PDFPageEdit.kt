package com.example.pdfsign.screens

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.pdfsign.R
import com.example.pdfsign.composables.DrawSign
import com.example.pdfsign.composables.ImageSignature
import com.example.pdfsign.composables.Signature
import com.example.pdfsign.composables.TextSign
import com.example.pdfsign.composables.TextSignature
import com.example.pdfsign.utils.calculateDoubleTapOffset
import com.example.pdfsign.utils.calculateNewOffset
import com.example.pdfsign.utils.getBitmapFromUri
import com.example.pdfsign.viewModels.SharedViewModel
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalComposeApi::class
)
@Composable
fun PDFPageEdit(viewModel: SharedViewModel, navigateBackToPdfPicker: () -> Unit) {
    var openDrawSign by remember {
        mutableStateOf(false)
    }
    var openTextSign by remember {
        mutableStateOf(false)
    }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    val imageIndex = viewModel.imageState.value?.index
    val localContext = LocalContext.current
    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia())
        { uri ->
            uri?.let {
                getBitmapFromUri(localContext, it)?.asImageBitmap()
                    ?.let { it1 ->
                        if (imageIndex != null) {
                            viewModel.addImageSignature(imageIndex, it1)
                        }
                    }
            }
        }
    if (openDrawSign) {
        BasicAlertDialog(onDismissRequest = { openDrawSign = false }) {
            DrawSign(modifier = Modifier
                .fillMaxHeight(0.5F)
                .fillMaxWidth(), onClickDone = { pathInfo ->
                openDrawSign = false
                imageIndex?.let {
                    viewModel.addSignature(it, pathInfo)
                }
            }) {
                openDrawSign = false
            }
        }
    }
    if (openTextSign) {
        BasicAlertDialog(onDismissRequest = { openDrawSign = false }) {
            TextSign(modifier = Modifier
                .fillMaxWidth(), onClickDone = { name ->
                openTextSign = false
                imageIndex?.let {
                    viewModel.addTextSignature(it, name)
                }
            }) {
                openTextSign = false
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = navigateBackToPdfPicker) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "up button",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ), actions = {
                    IconButton(onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.image),
                            contentDescription = "add image signature",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { openTextSign = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.text),
                            contentDescription = "add text signature",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { openDrawSign = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.draw),
                            contentDescription = "draw signature",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = {
                        zoom = 1f
                        offset = Offset.Zero
                        scope.launch(Dispatchers.Default) {
                            imageIndex?.let {
                                viewModel.resetIsSignAllVisibleList(imageIndex)
                            }
                            val bitmapAsync = captureController.captureAsync(config = Bitmap.Config.ARGB_8888)
                            try {
                                val bitmap = bitmapAsync.await()
                                imageIndex?.let {
                                    viewModel.addPdfRenderAltImage(it, bitmap)
                                }
                            } catch (error: Throwable) {
                                Log.e("PDFPageEdit", "PDFPageEdit: ", error)
                            }
                        }
                        Toast.makeText(localContext, "Saved the changes", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "save changes",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val image = viewModel.imageState.value?.imageBitmap
            if (image != null) {
                val signatures = viewModel.signaturesInfoHashMap[imageIndex]!!.signatures
                val isSignVisibleList = viewModel.signaturesInfoHashMap[imageIndex]!!.isSignVisible
                val imageSignature = viewModel.imageSignaturesInfoHashMap[imageIndex]!!.signatures
                val isISignVisibleList =
                    viewModel.imageSignaturesInfoHashMap[imageIndex]!!.isSignVisible
                val textSignature = viewModel.textSignaturesInfoHashMap[imageIndex]!!.signatures
                val isTSignVisibleList =
                    viewModel.textSignaturesInfoHashMap[imageIndex]!!.isSignVisible
                Box(modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                zoom = if (zoom > 1f) 1f else 2f
                                offset = calculateDoubleTapOffset(zoom, size, tapOffset)
                            },
                            onTap = {
                                imageIndex?.let { index ->
                                    viewModel.resetIsSignAllVisibleList(index)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures(
                            onGesture = { centroid, pan, gestureZoom, _ ->
                                offset = offset.calculateNewOffset(
                                    centroid, pan, zoom, gestureZoom, size
                                )
                                zoom = maxOf(1f, zoom * gestureZoom)
                            }
                        )
                    }
                    .graphicsLayer {
                        translationX = -offset.x * zoom
                        translationY = -offset.y * zoom
                        scaleX = zoom; scaleY = zoom
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .capturable(captureController), contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds(),
                        bitmap = image,
                        contentDescription = "pdf page",
                        contentScale = ContentScale.FillWidth
                    )
                    imageSignature.forEachIndexed { index, iSignatureInfo ->
                        ImageSignature(
                            iSignatureInfo = iSignatureInfo,
                            onClickDelete = {
                                imageIndex?.let {
                                    viewModel.removeImageSignatureAtIndex(imageIndex, index)
                                }
                            },
                            isVisible = isISignVisibleList[index],
                            changeVisibility = { isISignVisibleList[index] = true }
                        )
                    }
                    signatures.forEachIndexed { index, pathInfo ->
                        Signature(
                            pathInfo = pathInfo,
                            onClickDelete = {
                                imageIndex?.let {
                                    viewModel.removeSignatureAtIndex(imageIndex, index)
                                }
                            },
                            isVisible = isSignVisibleList[index],
                            changeVisibility = { isSignVisibleList[index] = true }
                        )
                    }
                    textSignature.forEachIndexed { index, tSignatureInfo ->
                        TextSignature(
                            tSignatureInfo = tSignatureInfo,
                            onClickDelete = {
                                imageIndex?.let {
                                    viewModel.removeTextSignatureAtIndex(imageIndex, index)
                                }
                            },
                            isVisible = isTSignVisibleList[index],
                            changeVisibility = { isTSignVisibleList[index] = true }
                        )
                    }
                }
            } else {
                Text(text = "Error in loading page", fontSize = 24.sp)
            }
        }
    }
}


