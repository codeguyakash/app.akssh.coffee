package app.akssh.coffee

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

@Composable
fun GameScreen() {
    var dinoLane by remember { mutableStateOf(1) }   // lane index
    var dinoOffsetX by remember { mutableStateOf(0f) }
    val laneCount = 3

    var necks by remember { mutableStateOf(listOf<Neck>()) }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    var gameOver by remember { mutableStateOf(false) }
    var dinoHeight by remember { mutableStateOf(100f) } // Dino height (grow after catch)

    // Game loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)

            if (!gameOver) {
                necks = necks.map { it.copy(y = it.y + 10f) }
                    .filter { it.y < screenHeight + 100f }

                if (Random.nextFloat() < 0.02f) {
                    val lane = Random.nextInt(laneCount)
                    necks = necks + Neck(lane, -50f)
                }

                // Collision check
                val laneWidth = screenWidth / laneCount
                val dinoX = laneWidth * (dinoLane + 0.5f)
                val dinoY = screenHeight - 150f

                val dinoRect = Rect(
                    dinoX - 60f, dinoY - dinoHeight,
                    dinoX + 60f, dinoY
                )

                val caught = mutableListOf<Neck>()
                for (neck in necks) {
                    val neckX = laneWidth * (neck.lane + 0.5f)
                    val neckRect = Rect(
                        neckX - 40f, neck.y,
                        neckX + 40f, neck.y + 120f
                    )

                    if (dinoRect.overlaps(neckRect)) {
                        // Caught ✅
                        dinoHeight += 20f
                        caught.add(neck)
                    } else if (neck.y + 120f >= screenHeight) {
                        // Missed ❌
                        gameOver = true
                    }
                }

                // remove caught necks
                necks = necks - caught.toSet()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBEE3F8))
            .pointerInput(Unit) {
                if (!gameOver) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            dinoOffsetX += dragAmount.x
                            change.consumeAllChanges()
                        },
                        onDragEnd = {
                            val laneWidth = screenWidth / laneCount
                            val newX = laneWidth * (dinoLane + 0.5f) + dinoOffsetX
                            val newLane = (newX / laneWidth).toInt().coerceIn(0, laneCount - 1)
                            dinoLane = newLane
                            dinoOffsetX = 0f
                        }
                    )
                }
            }
    ) {
        if (gameOver) {
            Text(
                text = "GAME OVER",
                color = Color.Red,
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                screenWidth = size.width
                screenHeight = size.height
                val laneWidth = screenWidth / laneCount

                // Dino
                val dinoX = laneWidth * (dinoLane + 0.5f) + dinoOffsetX
                val dinoY = screenHeight - 150f
                drawRect(
                    color = Color.Magenta,
                    topLeft = Offset(dinoX - 60f, dinoY - dinoHeight),
                    size = Size(120f, dinoHeight)
                )

                // Necks
                for (n in necks) {
                    val neckX = laneWidth * (n.lane + 0.5f)
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(neckX - 40f, n.y),
                        size = Size(80f, 120f)
                    )
                }
            }
        }
    }
}