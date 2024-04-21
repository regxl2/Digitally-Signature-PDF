package com.example.pdfsign.composables

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.example.pdfsign.utils.PdfRender
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PDFReader(file: File, navigateToPdfPageEdit: (ImageBitmap) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val pdfRender = PdfRender(
            fileDescriptor = ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
        DisposableEffect(key1 = Unit) {
            onDispose {
                pdfRender.close()
            }
        }
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
                page.pageContent.collectAsState().value?.asImageBitmap()?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Pdf page number: $index",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .clickable {
                                navigateToPdfPageEdit(it)
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