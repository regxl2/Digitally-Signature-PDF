package com.example.pdfsign.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.pdfsign.R
import com.example.pdfsign.viewModels.ISignatureInfo

@Composable
fun ImageSignature(
    modifier: Modifier = Modifier,
    iSignatureInfo: ISignatureInfo,
    onClickDelete: () -> Unit,
    isVisible: Boolean,
    changeVisibility: () -> Unit
) {
    val localDensity = LocalDensity.current
    var canvasScale by remember {
        mutableFloatStateOf(iSignatureInfo.scale)
    }
    var canvasOffsetX by remember {
        mutableFloatStateOf(iSignatureInfo.offset.x)
    }
    var canvasOffsetY by remember {
        mutableFloatStateOf(iSignatureInfo.offset.y)
    }
    Box(
        modifier
            .offset { IntOffset(x = canvasOffsetX.toInt(), y = canvasOffsetY.toInt()) }
            .scale(canvasScale)
            .pointerInput(Unit) {
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
                    iSignatureInfo.offset = Offset(x = canvasOffsetX, y = canvasOffsetY)
                }
            }
    ) {
        Image(
            modifier = Modifier
                .padding(40.dp)
                .border(width = 2.dp, color = if (isVisible) Color.Red else Color.Transparent)
                .width(width = 200.dp)
                .height(height = 200.dp)
                .align(Alignment.Center),
            bitmap = iSignatureInfo.bitmap,
            contentDescription = "image Signature"
        )
        if (isVisible) {
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
                        iSignatureInfo.scale = canvasScale
                    }
                }) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.width),
                    contentDescription = "resize signature",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
