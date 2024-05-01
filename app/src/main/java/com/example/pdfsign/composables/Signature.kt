package com.example.pdfsign.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.pdfsign.viewModels.PathInfo

@Composable
fun Signature(modifier: Modifier = Modifier, pathInfo: PathInfo, onClickDelete: ()-> Unit, isVisible : Boolean, changeVisibility: ()-> Unit) {
    val localDensity = LocalDensity.current
    val canvasHeight = with(localDensity) { pathInfo.height.toDp() }
    val canvasWidth = with(localDensity) { pathInfo.width.toDp() }
    var canvasScale by remember {
        mutableFloatStateOf(pathInfo.scale)
    }
    var canvasOffsetX by remember {
        mutableFloatStateOf(pathInfo.offset.x)
    }
    var canvasOffsetY by remember {
        mutableFloatStateOf(pathInfo.offset.y)
    }
    Box(
        modifier
            .offset { IntOffset(x = canvasOffsetX.toInt(), y = canvasOffsetY.toInt()) }
            .scale(canvasScale)
            .pointerInput(Unit){
                detectTapGestures {
                    changeVisibility()
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // with scale we controlling amount of drag. Therefore, I am multiplying scale
                    canvasOffsetX += dragAmount.x * canvasScale
                    canvasOffsetY += dragAmount.y * canvasScale
                    pathInfo.offset = Offset(x = canvasOffsetX, y = canvasOffsetY)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .padding(40.dp)
                .border(width = 2.dp, color = if(isVisible) Color.Red else Color.Transparent)
                .padding(16.dp)
                .width(width = canvasWidth)
                .height(height = canvasHeight)
                .align(Alignment.Center)
        ) {
            drawPath(
                path = pathInfo.path,
                color = Color.Black,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        if(isVisible){
            Box(
                modifier = modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterStart)
                    .clickable {
                        onClickDelete()
                    }
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    imageVector = Icons.Default.Clear,
                    contentDescription = "remove signature",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Box(modifier = modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newSize = canvasScale + scaleOnDrag(dragAmount)
                        canvasScale = minMaxScale(newSize)
                        pathInfo.scale = canvasScale
                    }
                }) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "resize signature",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

fun scaleOnDrag(offset: Offset): Float {
    return if (offset.x < 0 || offset.y < 0) minOf(offset.x, offset.y) / 200
    else maxOf(offset.x, offset.y) / 200
}

private fun minMaxScale(scale: Float): Float {
    return scale.coerceIn(0.1f, 1.5f)
}

@Preview(showBackground = true)
@Composable
private fun PreviewSignature() {
    val localDensity = LocalDensity.current
    val size = with(localDensity) {
        100.dp.toPx()
    }
    val path = Path()
    path.moveTo(0f, 0f)
    path.lineTo(size, 0f)
    path.lineTo(size, size)
    path.lineTo(0f, size)
    path.lineTo(0f, 0f)
    Signature(pathInfo = PathInfo(path, size, size),  onClickDelete = {}, isVisible = true, changeVisibility = {})
}