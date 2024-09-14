package xyz.gameoholic.partigon.particle.loop

/**
 * Represents a loop that allows a single iteration, then halts the frame at the last index.
 */
class SingleIteration(override val duration: Int) : Loop {

    override val envelopeDuration: Int = duration
    private val haltIndex = duration

    override fun applyLoop(frameIndex: Int): Int {
        return frameIndex.coerceAtMost(haltIndex)
    }

}