package com.example.pdfsign

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Experiment() {
    var width = remember { 0 }
    var height = remember { 0 }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(color = Color.White)

        ) {
            Image(
                modifier = Modifier
                    .background(color = Color(red = 0, green = 255, blue = 255))
                    .onGloballyPositioned {
                        width = it.size.width
                        height = it.size.height
                    },
                painter = painterResource(id = R.drawable.world_map_without_antarctica),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
            var offsetX by remember {
                mutableFloatStateOf(0f)
            }
            var offsetY by remember {
                mutableFloatStateOf(0f)
            }
            val h = with(
                LocalDensity.current
            ) { 50.dp.toPx() }
            val w = with(LocalDensity.current) { 100.dp.toPx() }
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .width(100.dp)
                    .offset(
                        x = with(LocalDensity.current) { offsetX.toDp() },
                        y = with(LocalDensity.current) { offsetY.toDp() })
                    .background(color = Color.Green)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(0f, width.toFloat() - w)
                            offsetY = (offsetY + dragAmount.y).coerceIn(0f, height.toFloat() - h)
                        }
                    }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewExperiment() {
    Experiment()
}