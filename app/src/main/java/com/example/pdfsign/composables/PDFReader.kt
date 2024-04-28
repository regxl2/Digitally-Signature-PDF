package com.example.pdfsign.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.pdfsign.utils.PdfRender

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PDFReader(pdfRender: PdfRender, navigateToPdfPageEdit: (ImageBitmap) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val pagerState = rememberPagerState {
            pdfRender.pageCount
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            BoxWithConstraints {
                val page = pdfRender.pageLists[index]
                DisposableEffect(key1 = Unit) {
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
                                    navigateToPdfPageEdit(imageBitmap)
                                })
                            },
                        contentScale = ContentScale.Fit
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