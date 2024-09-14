package xyz.gameoholic.partigon.particle

import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Represents a [PartigonAnimation] composed of multiple [Animation]s that will be drawn in sync.
 * The methods [start], [stop], and [resume] control all [Animation] instances.
 */
class MultiAnimation(animationList: List<Animation>) : PartigonAnimation {

    private constructor(builder: Builder) : this(builder.animations)

    private val animations: MutableList<Animation>

    init {
        require(animationList.isNotEmpty()) { "MultiAnimation must contain at least one Animation." }
        this.animations = animationList.toMutableList()
    }

    /**
     * The coroutine scope that this MultiAnimation will run in.
     */
    private lateinit var scope: CoroutineScope

    /**
     * The job produced by launching the coroutine that draws the animations.
     */
    private var job: Job? = null


    companion object {
        inline fun multiAnimation(
            block: Builder.() -> Unit
        ) = Builder().apply(block).build()

        inline fun multiAnimationBuilder(
            block: Builder.() -> Unit
        ) = Builder().apply(block)
    }

    class Builder {
        /**
         * The list of singular particles that are part of this MultiAnimation.
         */
        var animations: MutableList<Animation> = mutableListOf()

        /**
         * Adds this particle to the MultiAnimation instance.
         */
        fun Animation.add() {
            animations += this
        }

        fun build() = MultiAnimation(this)
    }

    private fun beginMultiAnimation(scope: CoroutineScope, restartFrameIndex: Boolean = true) {
        this.scope = scope
        job?.cancel() // Cancel the job if it already exists
        job = null

        animations.forEach { it.beginAnimating(scope, restartFrameIndex, selfContained = false) }

        job = scope.launch {
            while (animations.isNotEmpty()) {
                val iterator = animations.iterator()

                iterator.forEach {
                    it.drawAnimationFrame()
                    if (it.isComplete) iterator.remove()
                }

                delay(1.ticks)
            }
        }
    }

    override fun start(scope: CoroutineScope) {
        beginMultiAnimation(scope)
    }

    override fun stop() {
        job?.cancel()
    }

    override fun resume(scope: CoroutineScope?) {
        if (job?.isCancelled == false) return //Do nothing if the job is still running
        require(this::scope.isInitialized || scope != null) { "Scope must be provided to resume the particle." }
        beginMultiAnimation(scope ?: this.scope, restartFrameIndex = false)
    }
}