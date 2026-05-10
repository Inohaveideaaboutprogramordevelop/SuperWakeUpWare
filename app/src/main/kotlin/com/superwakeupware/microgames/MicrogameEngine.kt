package com.superwakeupware.microgames

import com.superwakeupware.microgames.games.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Every microgame must implement this contract. */
interface Microgame {
    val id: MicrogameId
    /** Instruction shown for ~1 s before the game starts. Keep ≤ 3 words. */
    val instruction: String
    /** Max play duration in milliseconds (≤ 5 000). */
    val durationMs: Long
    /**
     * Composable that renders the game board.
     * Must call [onResult] with true on win, false on time-out/loss.
     */
    @androidx.compose.runtime.Composable
    fun Content(onResult: (won: Boolean) -> Unit)
}

enum class MicrogameId {
    STOMP_GOOMBA,
    ENTER_PIPE,
    QUESTION_BLOCK,
    PLATFORM_BALANCE,
    CATCH_STAR,
    CARD_MEMORY,
    BLOW_FUSE,
    TURN_CRANK,
    FOLLOW_PATH,
    PRECISE_JUMP,
}

sealed class GameState {
    object Idle            : GameState()
    object ShowInstruction : GameState()
    data class Playing(val game: Microgame, val lives: Int, val volumePercent: Int) : GameState()
    data class Result(val won: Boolean, val lives: Int)  : GameState()
    object Dismissed       : GameState()
}

@Singleton
class MicrogameEngine @Inject constructor(
    stompGoomba     : StompGoomba,
    enterPipe       : EnterPipe,
    questionBlock   : QuestionBlock,
    platformBalance : PlatformBalance,
    catchStar       : CatchStar,
    cardMemory      : CardMemory,
    blowFuse        : BlowFuse,
    turnCrank       : TurnCrank,
    followPath      : FollowPath,
    preciseJump     : PreciseJump,
) {
    private val allGames: List<Microgame> = listOf(
        stompGoomba, enterPipe, questionBlock, platformBalance,
        catchStar, cardMemory, blowFuse, turnCrank, followPath, preciseJump,
    )

    private val _state = MutableStateFlow<GameState>(GameState.Idle)
    val state: StateFlow<GameState> = _state

    private var lives       = 3
    private var volumePct   = 40    // mirrors AlarmService starting volume

    /** Called by AlarmActivity when the service fires. */
    fun startSession() {
        lives     = 3
        volumePct = 40
        pickAndShowNext()
    }

    /** Called from the game Content composable. */
    fun reportResult(won: Boolean) {
        if (won) {
            _state.value = GameState.Result(won = true, lives = lives)
        } else {
            lives     = (lives - 1).coerceAtLeast(0)
            volumePct = (volumePct + 10).coerceAtMost(100)
            _state.value = GameState.Result(won = false, lives = lives)
        }
    }

    /** Called by UI after showing the result splash. */
    fun continueAfterResult() {
        val last = _state.value as? GameState.Result ?: return
        if (last.won) {
            _state.value = GameState.Dismissed
        } else {
            pickAndShowNext()
        }
    }

    private fun pickAndShowNext() {
        _state.value = GameState.ShowInstruction
        // Instruction is shown for 1.2 s by the UI before transitioning to Playing.
        val game = allGames.random()
        _state.value = GameState.Playing(game, lives, volumePct)
    }
}
