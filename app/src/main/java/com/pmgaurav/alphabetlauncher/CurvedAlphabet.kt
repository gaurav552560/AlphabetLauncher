package com.pmgaurav.alphabetlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

@Composable
fun CurvedAlphabet(
    selectedLetter: Char,
    selectedSpecial: Char?,
    onLetterSelected: (Char) -> Unit,
    onSpecialSelected: (Char?) -> Unit,
    onDragStateChanged: (Boolean) -> Unit
){
    var alphabetHeight by remember {
        mutableIntStateOf(0)
    }
    var touchY by remember {
        mutableFloatStateOf(-1f)
    }
    var isAlphabetDragging by remember {
        mutableStateOf(false)
    }
    fun updateLetterFromY(y: Float) {
        if (alphabetHeight <= 0) {
            return
        }
        val clampedY = y.coerceIn(0f, alphabetHeight.toFloat())
        val index = (clampedY / alphabetHeight * 29)
            .toInt()
            .coerceIn(0, 28)
        when(index){
            0 -> {onSpecialSelected('★')}
            28 -> {onSpecialSelected('•')}
            else -> {
                onSpecialSelected(null)
                val letterIndex = index -1
                val letter =
                    if (letterIndex == 26){
                        '#'
                    } else {
                        ('A'.code + letterIndex).toChar()
                    }

                onLetterSelected(letter)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(70.dp)
            .onSizeChanged{ size ->
                alphabetHeight = size.height
            }
            .pointerInput(Unit) {

                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isAlphabetDragging = true
                        onDragStateChanged(true)
                        touchY = offset.y
                        updateLetterFromY(
                            offset.y
                        )
                    },
                    onVerticalDrag = { change, _ ->
                        val y = change.position.y.coerceIn(0f, size.height.toFloat())
                        touchY = y
                        updateLetterFromY(y)

                        change.consume()
                    },
                    onDragEnd = {
                        isAlphabetDragging = false
                        touchY = -1f
                        onDragStateChanged(false)
                    },
                    onDragCancel = {
                        isAlphabetDragging = false
                        touchY = -1f
                        onDragStateChanged(false)
                    }
                )
            }
    ) {

        val items = listOf("★") + ('A'..'Z').map { it.toString() } + listOf("#", "•")

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->

                val normalizedFingerY = (touchY / alphabetHeight).coerceIn(0f, 1f)
                val normalizedItemY = (index + 0.5f) / items.size
                val curveAmount =
                    if (touchY >= 0f) {
                        val distance = (abs(normalizedItemY - normalizedFingerY) * 2f)
                            .coerceIn(0f, 1f)
                        val cosine = (cos(distance * PI) + 1.0)/2.0
                        cosine.toFloat()
                    } else {
                        0f
                    }
                val isSelected =
                    when {
                        item == "#" && selectedLetter == '#' -> true
                        item.length == 1 &&
                                item[0].isLetter() &&
                                item[0] == selectedLetter -> true
                        else -> false
                    }

                Text(
                    text = item,
                    color = Color.White,
                    fontSize = if (isSelected) {
                        18.sp
                    } else {
                        14.sp
                    },
                    fontWeight = if (isSelected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    modifier = Modifier
                        .offset(
                            x = (-curveAmount * 100f).dp
                        )
                        .clickable {
                            when {
                                item == "#" -> {
                                    onLetterSelected('#')
                                }

                                item.length == 1 &&
                                        item[0].isLetter() -> {
                                    onLetterSelected(item[0])
                                }
                            }
                        }
                        .padding(
                            vertical = 1.dp,
                            horizontal = 4.dp
                        )
                )
            }
        }
        val isTouchInsideAlphabet = touchY >= 0f && touchY <= alphabetHeight.toFloat()
        if (isAlphabetDragging && isTouchInsideAlphabet) {

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = -122.dp.roundToPx(),
                            y = touchY.roundToInt() - 28.dp.roundToPx()
                        )
                    }
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedSpecial?.toString() ?: selectedLetter.toString(),
                    color = Color.LightGray,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (isAlphabetDragging && isTouchInsideAlphabet) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset {
                        IntOffset(
                            x = -(8).dp.roundToPx(),
                            y = touchY.roundToInt() - alphabetHeight / 2
                        )
                    }
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        }
    }
}