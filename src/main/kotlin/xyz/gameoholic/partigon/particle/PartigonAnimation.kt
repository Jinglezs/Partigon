package xyz.gameoholic.partigon.particle

import kotlinx.coroutines.CoroutineScope

/**
 * Represents a particle animation, that's spawned every frame (the default is every tick).
 * The particle's properties are controlled via envelopes,
 * which interpolate them over time.
 */
interface PartigonAnimation {
    /**
     * Starts the particle animation from frame 0.
     */
    fun start(scope: CoroutineScope)
    /**
     * Stops the particle animation.
     */
    fun stop()
    /**
     * Resumes the particle animation from the frame it stopped.
     *
     * A scope can be provided to resume the animation in a different coroutine scope.
     */
    fun resume(scope: CoroutineScope? = null)
}