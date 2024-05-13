package com.example.pdfsign.viewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfsign.utils.PdfRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PathInfo(
    val path: Path,
    val width: Float,
    val height: Float,
    var offset: Offset = Offset.Zero,
    var scale: Float = 1f
)

data class SignaturesInfo(
    val signatures: SnapshotStateList<PathInfo> = mutableStateListOf<PathInfo>(),
    val isSignVisible: SnapshotStateList<Boolean> = mutableStateListOf<Boolean>()
)

data class ISignatureInfo(
    val bitmap: ImageBitmap,
    var offset: Offset = Offset.Zero,
    var scale: Float = 1f
)

data class ImageSignaturesInfo(
    val signatures: SnapshotStateList<ISignatureInfo> = mutableStateListOf<ISignatureInfo>(),
    val isSignVisible: SnapshotStateList<Boolean> = mutableStateListOf<Boolean>()
)

data class TSignatureInfo(
    val name: String,
    var offset: Offset = Offset.Zero,
    var scale: Float = 1f
)

data class TextSignaturesInfo(
    val signatures: SnapshotStateList<TSignatureInfo> = mutableStateListOf<TSignatureInfo>(),
    val isSignVisible: SnapshotStateList<Boolean> = mutableStateListOf<Boolean>()
)

data class ImageInfo(val imageBitmap: ImageBitmap, val index: Int)

class SharedViewModel : ViewModel() {
    var imageState = mutableStateOf<ImageInfo?>(null)
        private set

    var pdfRender = mutableStateOf<PdfRender?>(null)
        private set

    var exportPdfRender: PdfRender? = null
        private set

    var signaturesInfoHashMap = hashMapOf<Int, SignaturesInfo>()
        private set

    var imageSignaturesInfoHashMap = hashMapOf<Int, ImageSignaturesInfo>()
        private set

    var textSignaturesInfoHashMap = hashMapOf<Int, TextSignaturesInfo>()
        private set

    var pdfRenderAltHashMap = hashMapOf<Int, MutableState<ImageBitmap>>()
        private set

    fun setImageState(imageInfo: ImageInfo) {
        imageState.value = imageInfo
        if (signaturesInfoHashMap[imageInfo.index] == null) {
            signaturesInfoHashMap[imageInfo.index] = SignaturesInfo()
        }
        if (imageSignaturesInfoHashMap[imageInfo.index] == null) {
            imageSignaturesInfoHashMap[imageInfo.index] = ImageSignaturesInfo()
        }
        if (textSignaturesInfoHashMap[imageInfo.index] == null) {
            textSignaturesInfoHashMap[imageInfo.index] = TextSignaturesInfo()
        }
    }

    fun setPdfRender(pdfRender: PdfRender) {
        this.pdfRender.value = pdfRender
    }
    fun setExportPdfRender(pdfRender: PdfRender) {
        this.exportPdfRender = pdfRender
    }

    fun addSignature(pageIndex: Int, pathInfo: PathInfo) {
        signaturesInfoHashMap[pageIndex]?.signatures?.add(pathInfo)
        signaturesInfoHashMap[pageIndex]?.isSignVisible?.add(true)
    }

    fun addImageSignature(pageIndex: Int, bitmap: ImageBitmap) {
        imageSignaturesInfoHashMap[pageIndex]?.signatures?.add(ISignatureInfo(bitmap))
        imageSignaturesInfoHashMap[pageIndex]?.isSignVisible?.add(true)
    }

    fun addTextSignature(pageIndex: Int, name: String) {
        textSignaturesInfoHashMap[pageIndex]?.signatures?.add(TSignatureInfo(name))
        textSignaturesInfoHashMap[pageIndex]?.isSignVisible?.add(true)
    }

    fun addPdfRenderAltImage(index: Int, bitmap: ImageBitmap) {
        signaturesInfoHashMap[index]?.let { signaturesInfo ->
            if (signaturesInfo.signatures.size > 0) {
                pdfRenderAltHashMap[index] = mutableStateOf(bitmap)
                println("image saved")
            }
        }
        imageSignaturesInfoHashMap[index]?.let { imageSignaturesInfo ->
            if (imageSignaturesInfo.signatures.size > 0) {
                pdfRenderAltHashMap[index] = mutableStateOf(bitmap)
                println("image saved")
            }
        }
        textSignaturesInfoHashMap[index]?.let { textSignaturesInfo ->
            if (textSignaturesInfo.signatures.size > 0) {
                pdfRenderAltHashMap[index] = mutableStateOf(bitmap)
                println("image saved")
            }
        }
    }

    fun removeSignatureAtIndex(pageIndex: Int, signIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            signaturesInfoHashMap[pageIndex]?.signatures?.removeAt(signIndex)
            if (removeCheck(pageIndex)) {
                pdfRenderAltHashMap.remove(pageIndex)
                println("removed image")
            }
        }
    }

    fun removeImageSignatureAtIndex(pageIndex: Int, signIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            imageSignaturesInfoHashMap[pageIndex]?.signatures?.removeAt(signIndex)
            if (removeCheck(pageIndex)) {
                pdfRenderAltHashMap.remove(pageIndex)
                println("removed image")
            }
        }
    }

    fun removeTextSignatureAtIndex(pageIndex: Int, signIndex: Int) {
        textSignaturesInfoHashMap[pageIndex]?.signatures?.removeAt(signIndex)
        if (removeCheck(pageIndex)) {
            pdfRenderAltHashMap.remove(pageIndex)
            println("removed image")
        }
    }

    private fun removeCheck(pageIndex: Int): Boolean {
        return imageSignaturesInfoHashMap[pageIndex]?.signatures?.size == 0 && signaturesInfoHashMap[pageIndex]?.signatures?.size == 0 && textSignaturesInfoHashMap[pageIndex]?.signatures?.size == 0
    }

    fun resetIsSignAllVisibleList(pageIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            signaturesInfoHashMap[pageIndex]?.isSignVisible?.let { list ->
                for (i in 0 until list.size) {
                    list[i] = false
                }
            }
            imageSignaturesInfoHashMap[pageIndex]?.isSignVisible?.let { list ->
                for (i in 0 until list.size) {
                    list[i] = false
                }
            }
            textSignaturesInfoHashMap[pageIndex]?.isSignVisible?.let { list ->
                for (i in 0 until list.size) {
                    list[i] = false
                }
            }
        }
    }

    fun resetHashMaps() {
        viewModelScope.launch(Dispatchers.Default) {
            signaturesInfoHashMap.clear()
            pdfRenderAltHashMap.clear()
            imageSignaturesInfoHashMap.clear()
            textSignaturesInfoHashMap.clear()
        }
    }

    fun resetPdfRender() {
        viewModelScope.launch {
            pdfRender.value?.close()
            pdfRender.value = null
            exportPdfRender?.close()
            exportPdfRender = null
            println("clearing")
        }
    }
}