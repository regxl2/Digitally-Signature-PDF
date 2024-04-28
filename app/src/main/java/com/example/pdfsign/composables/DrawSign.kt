package com.example.pdfsign.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pdfsign.viewModels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DrawSign(
    modifier: Modifier = Modifier,
    onClickDone: (Path) -> Unit,
    onClickCancel: () -> Unit
) {
    val path = remember { Path() }
    var reset by remember {
        mutableStateOf(false)
    }
    Column(modifier = modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            SignButton(onClick = onClickCancel, text = "Cancel", enabled = true)
            Text(
                modifier = Modifier.weight(1f),
                text = "Place Signature",
                textAlign = TextAlign.Center
            )
            SignButton(onClick = {
                if (!path.isEmpty) {
                    onClickDone(path)
                }
            }, text = "Done")
        }
        Drawing(modifier = Modifier.weight(1f), path = path, reset = reset)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp)
        ) {
            SignButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    path.reset()
                    reset = !reset
                },
                text = "Clear",
                enabled = true
            )
        }
    }
}


enum class MotionEvent {
    Idle,
    Down,
    Move,
    Up
}

@Composable
fun Drawing(modifier: Modifier = Modifier, path: Path, reset: Boolean) {
    var prevOffset by remember {
        mutableStateOf(Offset.Unspecified)
    }
    var currOffset by remember {
        mutableStateOf(Offset.Unspecified)
    }
    var motionEvent by remember {
        mutableStateOf(MotionEvent.Idle)
    }
    val scope = rememberCoroutineScope()
    Canvas(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clipToBounds()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var waitAfterDown = false
                    currOffset = down.position
                    motionEvent = MotionEvent.Down
                    scope.launch {
                        delay(20)
                        waitAfterDown = true
                    }
                    down.consume()
                    do {
                        val event = awaitPointerEvent()
                        if (waitAfterDown) {
                            currOffset = event.changes.first().position
                            motionEvent = MotionEvent.Move
                        }
                    } while (event.changes.any { pointerInputChange ->
                            val pressed = pointerInputChange.pressed
                            if (pressed) {
                                pointerInputChange.consume()
                            }
                            pressed
                        }
                    )
                    motionEvent = MotionEvent.Up
                }
            }
    ) {
        when (motionEvent) {
            MotionEvent.Down -> {
                path.moveTo(currOffset.x, currOffset.y)
                prevOffset = currOffset
            }

            MotionEvent.Move -> {
                path.quadraticBezierTo(
                    prevOffset.x,
                    prevOffset.y,
                    (prevOffset.x + currOffset.x) / 2,
                    (prevOffset.y + currOffset.y) / 2
                )
                prevOffset = currOffset
            }

            MotionEvent.Up -> {}
            else -> return@Canvas
        }
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun SignButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(),
        contentPadding = PaddingValues(0.dp),
        enabled = enabled
    ) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDrawSign() {
    DrawSign(onClickDone = {}, onClickCancel = {})
}