package app.akssh.coffee

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FallingNeck(val lane: Int, val y: Float)

@Composable
fun GameScreen() {
    var dinoLane by remember { mutableStateOf(1) }   // lane index
    var dinoOffsetX by remember { mutableStateOf(0f) }
    val laneCount = 3

    var necks by remember { mutableStateOf(listOf<FallingNeck>()) }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    var gameOver by remember { mutableStateOf(false) }
    var dinoHeight by remember { mutableStateOf(100f) } // Dino height (grow after catch)

    // 🔄 Restart function
    fun restartGame() {
        dinoLane = 1
        dinoOffsetX = 0f
        necks = emptyList()
        gameOver = false
        dinoHeight = 100f
    }

    // Game loop
    LaunchedEffect(gameOver) {
        while (!gameOver) {
            delay(16)

            // Move necks downward
            necks = necks.map { it.copy(y = it.y + 10f) }
                .filter { it.y < screenHeight + 150f }

            // Randomly add new necks
            if (Random.nextFloat() < 0.02f) {
                val lane = Random.nextInt(laneCount)
                necks = necks + FallingNeck(lane, -50f)
            }

            // Collision check
            val laneWidth = screenWidth / laneCount
            val dinoX = laneWidth * (dinoLane + 0.5f)
            val dinoY = screenHeight - 150f

            val dinoRect = Rect(
                dinoX - 60f, dinoY - dinoHeight,
                dinoX + 60f, dinoY
            )

            val caught = mutableListOf<FallingNeck>()
            for (neck in necks) {
                val neckX = laneWidth * (neck.lane + 0.5f)
                val neckRect = Rect(
                    neckX - 40f, neck.y,
                    neckX + 40f, neck.y + 120f
                )

                if (dinoRect.overlaps(neckRect)) {
                    // ✅ Dino caught the neck
                    dinoHeight += 20f
                    caught.add(neck)
                }
            }

            // 🔥 Only check game over for uncaught necks
            for (neck in necks - caught.toSet()) {
                if (neck.y + 120f >= screenHeight) {
                    gameOver = true
                }
            }

            // Remove caught necks
            necks = necks - caught.toSet()
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
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME OVER",
                    color = Color.DarkGray,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { restartGame() }) {
                    Text("Restart Game")
                }
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                screenWidth = size.width
                screenHeight = size.height
                val laneWidth = screenWidth / laneCount

                // Dino
                val dinoX = laneWidth * (dinoLane + 0.5f) + dinoOffsetX
                val dinoY = screenHeight - 150f
                drawRect(
                    color = Color.Red, // cating box color
                    topLeft = Offset(dinoX - 60f, dinoY - dinoHeight),
                    size = Size(120f, dinoHeight)
                )

                // Necks
                for (n in necks) {
                    val neckX = laneWidth * (n.lane + 0.5f)
                    drawRect(
                        color = Color.Black, // falling box color
                        topLeft = Offset(neckX - 40f, n.y),
                        size = Size(80f, 120f)
                    )
                }
            }
        }
    }
}