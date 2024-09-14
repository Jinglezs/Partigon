package xyz.gameoholic.partigon.particle.loop

import xyz.gameoholic.partigon.util.ticks
import kotlin.time.Duration

/**
 * When loop reaches end, restarts the animation where it was on the first frame.
 *
 * @param duration The duration of the loop.
 *
 * @throws IllegalArgumentException If loop duration is not above 0.
 */
class RepeatLoop(override val duration: Int): Loop {

    constructor(duration: Duration) : this(duration.ticks)

    override val envelopeDuration = duration

    init {
        if (duration <= 0)
            throw IllegalArgumentException("Repeat loop duration must be above 0.")
    }
    override fun applyLoop(frameIndex: Int): Int {
        return frameIndex % duration
    }

}