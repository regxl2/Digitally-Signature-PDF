package com.example.pdfsign.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.pdfsign.utils.PdfRender
import com.example.pdfsign.viewModels.ImageInfo
import com.example.pdfsign.viewModels.SharedViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PDFReader(
    pdfRender: PdfRender,
    navigateToPdfPageEdit: (ImageInfo) -> Unit
) {
    val pagerState = rememberPagerState {
        pdfRender.pageCount
    }
    HorizontalPager(state = pagerState) { index ->
        Box(contentAlignment = Alignment.Center){
            val page = pdfRender.pageLists[index]
            DisposableEffect(Unit) {
                page.load()
                onDispose {
                    page.recycle()
                }
            }
            page.pageContent.collectAsState().value?.asImageBitmap()?.let { imageBitmap ->
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Pdf page number: $index",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = {
                                navigateToPdfPageEdit(
                                    ImageInfo(
                                        imageBitmap = imageBitmap,
                                        index = index
                                    )
                                )
                            })
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}