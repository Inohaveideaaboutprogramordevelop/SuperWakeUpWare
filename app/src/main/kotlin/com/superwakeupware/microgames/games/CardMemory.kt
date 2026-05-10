package com.superwakeupware.microgames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.superwakeupware.R
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import com.superwakeupware.ui.theme.SurfaceCard
import kotlinx.coroutines.delay
import javax.inject.Inject

data class MemoryCard(val id: Int, val spriteRes: Int, var flipped: Boolean = false, var matched: Boolean = false)

/**
 * CARD MEMORY
 * 2×2 grid with two pairs. Flip and match both pairs to win.
 * Users have the full 5 s — finding both pairs on first tries is a guaranteed win.
 */
class CardMemory @Inject constructor() : Microgame {
    override val id          = MicrogameId.CARD_MEMORY
    override val instruction = "MATCH 'EM!"
    override val durationMs  = 5_000L

    private val CARD_SPRITES = listOf(
        R.drawable.sprite_mushroom,
        R.drawable.sprite_fire_flower,
    )

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        val initialCards = remember {
            val pool = (CARD_SPRITES + CARD_SPRITES).shuffled()
            pool.mapIndexed { i, res -> MemoryCard(id = i, spriteRes = res) }.toMutableStateList()
        }
        var firstFlip by remember { mutableStateOf<Int?>(null) }
        var locked    by remember { mutableStateOf(false) }
        var done      by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(durationMs)
            if (!done) onResult(false)
        }

        fun onCardClick(index: Int) {
            if (locked || done || initialCards[index].flipped || initialCards[index].matched) return
            initialCards[index] = initialCards[index].copy(flipped = true)

            val first = firstFlip
            if (first == null) {
                firstFlip = index
            } else {
                firstFlip = null
                if (initialCards[first].spriteRes == initialCards[index].spriteRes) {
                    initialCards[first] = initialCards[first].copy(matched = true)
                    initialCards[index] = initialCards[index].copy(matched = true)
                    if (initialCards.all { it.matched }) { done = true; onResult(true) }
                } else {
                    locked = true
                    // Flip back after brief pause
                    kotlinx.coroutines.GlobalScope.kotlinx.coroutines.launch {
                        delay(700)
                        initialCards[first] = initialCards[first].copy(flipped = false)
                        initialCards[index] = initialCards[index].copy(flipped = false)
                        locked = false
                    }
                }
            }
        }

        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val chunked = initialCards.chunked(2)
            chunked.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { card ->
                        FlipCard(card = card, onClick = { onCardClick(initialCards.indexOf(card)) })
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FlipCard(card: MemoryCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue  = if (card.flipped || card.matched) 180f else 0f,
        animationSpec = tween(300),
        label        = "card_flip_${card.id}",
    )
    Card(
        modifier = Modifier
            .size(112.dp)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (rotation <= 90f) {
                // Card back
                Image(painterResource(R.drawable.sprite_card_back), contentDescription = null, modifier = Modifier.size(64.dp))
            } else {
                // Card front (mirrored because the view is rotated 180)
                Image(
                    painter            = painterResource(card.spriteRes),
                    contentDescription = null,
                    modifier           = Modifier
                        .size(64.dp)
                        .graphicsLayer { rotationY = 180f },
                )
            }
        }
    }
}
