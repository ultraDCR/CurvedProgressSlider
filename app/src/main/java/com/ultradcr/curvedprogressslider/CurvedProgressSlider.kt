package com.ultradcr.curvedprogressslider

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ultradcr.curvedprogressslider.ui.theme.CurvedProgressSliderTheme


@Composable
fun CurvedProgressSlider(
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
    progressColor: Color = MaterialTheme.colors.primary,
    progressSliderWidth: Dp = 6.dp,
    initialProgress: Int = 30,
    maxProgress: Int = 100,
    onProgressChangeListener: (Int) -> Unit
) {
    if(initialProgress > maxProgress ){
        Log.e("CurvedProgressSlider", "CurvedProgressSlider: Error: Slider Progress can not be greater than max progress. Progress : $initialProgress and MaxProgress = $maxProgress")
    }
    BoxWithConstraints(
        modifier = modifier, contentAlignment = Alignment.Center) {

       val height =  (constraints.maxHeight / 2)
        val progressValue = kotlin.math.max((initialProgress / maxProgress) * constraints.maxWidth, constraints.maxWidth)
        var offset by remember {
            mutableStateOf(Offset(progressValue.toFloat(), height.toFloat()))
        }

        val offsetAnim = animateOffsetAsState(targetValue = offset, animationSpec = spring(dampingRatio = 0.4f))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        offset = Offset(it.x, size.height / 2f)
                        onProgressChangeListener(
                            ((it.x / size.width) * maxProgress).toInt()
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(

                        onDragEnd = {
                            val offsetX = offset.x.coerceIn(0f, size.width.toFloat())
                            val offsetY = size.height / 2f
                            offset = Offset(offsetX, offsetY)
                            onProgressChangeListener(
                                ((offsetX / size.width) * maxProgress).toInt()
                            )
                        }
                    ) { change, dragAmount ->
                        change.consumeAllChanges()
                        val offsetX = change.position.x.coerceIn(0f, size.width.toFloat())
                        val offsetY = change.position.y.coerceIn(0f, size.height.toFloat())
                        offset = Offset(offsetX, offsetY)
                    }
                }
        ) {

            //Path for track and progress.
            val progressPath = Path()
            progressPath.moveTo(0f,size.height/2)
            progressPath.quadraticBezierTo(
                offsetAnim.value.x, offsetAnim.value.y,
                size.width, size.height/2
            )

            //Circle at progress end
//            val t = 0.4f
//
//            val x = (1f - t) * (1f - t) * 0f + 2f * (1f - t) * t * offsetAnim.value.x + t * t * size.width;
//
//            val y = ((1f - t) * (1f - t) * height.toFloat() )+ (2f * (1f - t) * t * offsetAnim.value.y )+ t * t * height.toFloat();
//
//
//            drawCircle(color = progressColor, center = Offset(offsetAnim.value.x,y), radius = 20f)

            //Track path drawing
            drawPath(
                path = progressPath,
                color = trackColor,
                style = Stroke(
                    width = 5.dp.toPx(),
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )


            val rect = Rect(
                -100f,-100f,
                offsetAnim.value.x,
                size.height + 150
            )

            //Clip path to clip progress value from track progress.
            val clipPath = Path()
            clipPath.addRect(
                rect
            )



            clipPath(path = clipPath){

                //Progress path draw
                drawPath(
                    path = progressPath,
                    color = progressColor,
                    style = Stroke(width = progressSliderWidth.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )
            }

        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CurvedProgressSliderPreview() {
    CurvedProgressSliderTheme() {
        Surface {

            CurvedProgressSlider(
                modifier = Modifier
                    .padding(30.dp)
                    .height(200.dp)
                    .fillMaxWidth(),
                initialProgress = 10
            ) {
                Log.d("TAG", "CurvedProgressSliderPreview: $it")
            }
        }
    }
}