package com.github.gameoholic.partigon.particle

import com.github.gameoholic.partigon.Partigon
import com.github.gameoholic.partigon.particle.envelope.Envelope
import com.github.gameoholic.partigon.util.*
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*

//todo: add link to the documentation here, also, write the documentation you lazy shit
/**
 * Represents a particle animation, that's spawned every tick.
 * The particle's properties are controlled via envelopes,
 * which interpolate them over time.
 */
class PartigonParticle(
    val location: Location,
    val particleType: Particle,
    val envelopes: List<Envelope>,
    val count: Int,
    val offset: Vector,
    val animationFrameAmount: Int,
    val animationInterval: Int,
    val extra: Double,
    val rotationOptions: List<MatrixUtils.RotationOptions>
) {

    private constructor(
        builder: Builder
    ) :
        this(
            builder.location,
            builder.particleType,
            builder.envelopes,
            builder.count,
            builder.offset,
            builder.animationFrameAmount,
            builder.animationInterval,
            builder.extra,
            builder.rotationOptions
        )

    companion object {
        inline fun partigonParticle(
            location: Location,
            particleType: Particle,
            block: Builder.() -> Unit
        ) = Builder(location, particleType).apply(block).build()

        /**
         * Copies the constructor properties from an existing particle,
         * and allows you to provide new ones.
         */
        inline fun partigonParticle(
            particle: PartigonParticle,
            block: Builder.() -> Unit
        ) = Builder(particle.location, particle.particleType).apply {
            // We don't want to copy references


            envelopes = copiedParticle.envelopes
            count = copiedParticle.count
            offset = copiedParticle.offset
            animationFrameAmount = copiedParticle.animationFrameAmount
            animationInterval = copiedParticle.animationInterval
            extra = copiedParticle.extra
            rotationOptions = copiedParticle.rotationOptions

        }.apply(block).build()
    }

    class Builder(
        val location: Location,
        val particleType: Particle
    ) {
        var envelopes: List<Envelope> = listOf()
        var count: Int = 1
        var offset: Vector = Vector(0.0, 0.0, 0.0)
        var animationFrameAmount: Int = 1
        var animationInterval: Int = 1
        var extra: Double = 0.0
        var rotationOptions: List<MatrixUtils.RotationOptions> = listOf()

        fun build() = PartigonParticle(this)
    }

    val id = UUID.randomUUID()!!
    var frameIndex = -1
        private set
    private var task: BukkitTask? = null
    private var delay = animationInterval

    init {
        //Add rotation for every group, on top of whatever rotations they already have
        val groups = envelopes.mapNotNull { it.envelopeGroup }.distinct()
        groups.forEach {
            it.rotationOptions = rotationOptions.toMutableList().apply { this.addAll(rotationOptions) }
        }
    }

    /**
     * Resets and starts the particle animation.
     */
    fun start() {
        LoggerUtil.info("Starting PartigonParticleImpl", id)

        frameIndex = -1
        task?.cancel()
        task = object : BukkitRunnable() {
            override fun run() {
                onTimerTickPassed()
            }
        }.runTaskTimer(Partigon.plugin, 0L, 1L)
    }

    //todo: check edge casdsees for htese mthods
    /**
     * Pauses the particle animation.
     */
    fun pause() {
        LoggerUtil.info("Pausing PartigonParticleImpl", id)
        task?.cancel()
    }

    /**
     * Resumes the particle animation from the frame it stopped.
     */

    fun resume() {
        LoggerUtil.info("Resuming PartigonParticleImpl", id)
        if (task?.isCancelled == false) return
        task = object : BukkitRunnable() {
            override fun run() {
                onTimerTickPassed()
            }
        }.runTaskTimer(Partigon.plugin, 0L, 1L)
    }

    /**
     * Stops the particle animation.
     */

    fun stop() {
        LoggerUtil.info("Stopping PartigonParticleImpl", id)
        task?.cancel()
    }

    private fun onTimerTickPassed() {
        LoggerUtil.debug("Timer tick passed", id)

        //Animate animationFrameAmount frames, every animationInterval ticks
        delay += 1
        if (delay >= animationInterval) {
            for (i in 0 until animationFrameAmount) {
                frameIndex++
                applyEnvelopes()
            }
            delay = 0
        }
    }

    /**
     * Applies the envelopes and spawns the particle.
     */
    private fun applyEnvelopes() {
        LoggerUtil.debug("Applying envelopes", id)

        // We add envelope values, to the initial values provided in the constructor (envelopes are relative, not absolute)
        var newLocation = location.clone()
        var newCount = count
        var newOffset = offset.clone()

        for (envelope in envelopes.filter { !it.disabled }) {
            val envelopePropertyType = envelope.propertyType
            val envelopeValue = envelope.getValueAt(frameIndex)
            LoggerUtil.debug("Applying envelope $envelope. Envelope value is $envelopeValue", id)

            if (envelopeValue == null) {
                LoggerUtil.debug("Envelop disabled, not applying it.", id)
                continue
            }

            when (envelopePropertyType) {
                Envelope.PropertyType.POS_X -> {
                        newLocation.x += envelopeValue
                }

                Envelope.PropertyType.POS_Y -> {
                        newLocation.y += envelopeValue
                }

                Envelope.PropertyType.POS_Z -> {
                        newLocation.z += envelopeValue
                }

                Envelope.PropertyType.COUNT -> {
                        newCount += envelopeValue.toInt()
                }

                Envelope.PropertyType.OFFSET_X -> {
                        newOffset.x += envelopeValue
                }

                Envelope.PropertyType.OFFSET_Y -> {
                        newOffset.y += envelopeValue
                }

                Envelope.PropertyType.OFFSET_Z -> {
                        newOffset.z += envelopeValue
                }

                Envelope.PropertyType.NONE -> {
                    throw IllegalArgumentException("Property type NONE may only be used for nested envelopes, not for the parent one.")
                }
            }
        }

        LoggerUtil.debug("Current properties are: {location: $newLocation, count: $newCount, offset: $newOffset}", id)

        spawnParticle(newLocation, newCount, newOffset)
    }

    /**
     * Spawns the particle with the new provided properties.
     */
    private fun spawnParticle(newLocation: Location, newCount: Int, newOffset: Vector) {
        LoggerUtil.debug("Spawning particle", id)
        newLocation.world.spawnParticle(
            particleType,
            newLocation,
            newCount,
            newOffset.x,
            newOffset.y,
            newOffset.z,
            extra
        )
    }

}