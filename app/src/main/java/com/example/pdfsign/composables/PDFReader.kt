package com.example.pdfsign.composables

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.example.pdfsign.utils.PdfRender
import com.example.pdfsign.viewModels.ImageInfo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PDFReader(
    pdfImageAlt: HashMap<Int, MutableState<ImageBitmap>>,
    pdfRender: PdfRender,
    navigateToPdfPageEdit: (ImageInfo) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val pagerState = rememberPagerState {
            pdfRender.pageCount
        }
        HorizontalPager(state = pagerState) { index ->
            BoxWithConstraints {
                val page = pdfRender.pageLists[index]
                DisposableEffect(Unit){
                    page.load()
                    onDispose {
                        page.recycle()
                    }
                }
                page.pageContent.collectAsState().value?.asImageBitmap()?.let { imageBitmap ->
                    val image = Bitmap.createBitmap(imageBitmap.asAndroidBitmap()).asImageBitmap()
                    var newImageBitmap = imageBitmap
                    pdfImageAlt[index]?.let { newImageBitmap = it.value }
                    Image(
                        bitmap = newImageBitmap,
                        contentDescription = "Pdf page number: $index",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .pointerInput(Unit) {
                                detectTapGestures(onDoubleTap = {
                                    navigateToPdfPageEdit(ImageInfo(imageBitmap = image, index = index))
                                })
                            },
                        contentScale = ContentScale.FillWidth
                    )
                } ?: Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            with(LocalDensity.current) {
                                page
                                    .heightByWidth(constraints.maxWidth)
                                    .toDp()
                            }
                        )
                )
            }
        }
    }
}