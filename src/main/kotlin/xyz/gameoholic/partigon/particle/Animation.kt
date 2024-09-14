package xyz.gameoholic.partigon.particle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.util.Vector
import xyz.gameoholic.partigon.particle.envelope.Envelope
import xyz.gameoholic.partigon.particle.envelope.EnvelopeGroup
import xyz.gameoholic.partigon.particle.envelope.PropertyType
import xyz.gameoholic.partigon.particle.location.PartigonLocation
import xyz.gameoholic.partigon.util.*
import xyz.gameoholic.partigon.util.envelope
import xyz.gameoholic.partigon.util.rotation.RotationOptions
import xyz.gameoholic.partigon.util.rotation.RotationUtil
import kotlin.time.Duration

/**
 * Represents a single Partigon animation.
 */
class Animation(
    private var originLocation: PartigonLocation,
    private val particleType: Particle,
    envelopes: List<Envelope>,
    positionX: Envelope,
    positionY: Envelope,
    positionZ: Envelope,
    offsetX: Envelope,
    offsetY: Envelope,
    offsetZ: Envelope,
    count: Envelope,
    extra: Envelope,
    private val dustOptions: DustOptions?,
    private var maximumFrames: Int,
    private val framesPerAnimationDraw: Int,
    private val animationInterval: Int,
    private val rotationOptions: List<RotationOptions>,
    private val envelopeGroupsRotationOptions: List<RotationOptions>
) : PartigonAnimation {

    private lateinit var scope: CoroutineScope

    private constructor(builder: Builder) : this(
        builder.originLocation!!,
        builder.particleType,
        builder.envelopes,
        builder.positionX,
        builder.positionY,
        builder.positionZ,
        builder.offsetX,
        builder.offsetY,
        builder.offsetZ,
        builder.count,
        builder.extra,
        builder.dustOptions,
        if (builder.maximumDuration.isInfinite()) Int.MAX_VALUE else builder.maximumDuration.ticks,
        builder.framesPerAnimationDraw,
        builder.animationInterval,
        builder.rotationOptions,
        builder.envelopeGroupsRotationOptions
    )

    companion object {
        inline fun singleAnimation(
            block: Builder.() -> Unit
        ) = Builder().apply(block).build()

        inline fun singleAnimationBuilder(
            block: Builder.() -> Unit
        ) = Builder().apply(block)
    }

    var frameIndex = -1
        private set

    private var job: Job? = null
    private var delay = animationInterval
    private val envelopes: List<Envelope>

    internal val isComplete: Boolean
        get() = frameIndex >= maximumFrames

    init {
        val newEnvelopes = envelopes.toMutableList()

        // Add all constructor-parameter envelopes to the envelopes list
        newEnvelopes += count.copyWithPropertyType(PropertyType.COUNT)
        newEnvelopes += positionX.copyWithPropertyType(PropertyType.POS_X)
        newEnvelopes += positionY.copyWithPropertyType(PropertyType.POS_Y)
        newEnvelopes += positionZ.copyWithPropertyType(PropertyType.POS_Z)
        newEnvelopes += offsetX.copyWithPropertyType(PropertyType.OFFSET_X)
        newEnvelopes += offsetY.copyWithPropertyType(PropertyType.OFFSET_Y)
        newEnvelopes += offsetZ.copyWithPropertyType(PropertyType.OFFSET_Z)
        newEnvelopes += extra.copyWithPropertyType(PropertyType.EXTRA)
        this.envelopes = newEnvelopes

        // Add rotation for every group, on top of whatever rotations they already have
        this.envelopes.mapNotNull { it.envelopeGroup }.distinct().forEach {
            it.rotationOptions = it.rotationOptions.toMutableList().apply { this.addAll(envelopeGroupsRotationOptions) }
        }
    }

    override fun start(scope: CoroutineScope) {
        beginAnimating(scope)
    }

    override fun stop() {
        job?.cancel()
    }

    override fun resume(scope: CoroutineScope?) {
        if (job?.isCancelled == false) return
        require(this::scope.isInitialized || scope != null) { "Scope must be provided to resume the particle." }
        beginAnimating(scope ?: this.scope, restartFrameIndex = false)
    }

    /**
     * Begins the animation. If [restartFrameIndex] is true, the frame index will be reset to -1. When the animation
     * is [selfContained], it handles its own coroutine creation. If [selfContained] is `false`, the animation frames
     * must be externally managed and drawn by calling [drawAnimationFrame].
     */
    internal fun beginAnimating(scope: CoroutineScope, restartFrameIndex: Boolean = true, selfContained: Boolean = true) {
        this.scope = scope
        job?.cancel() // Cancel the job if it already exists
        job = null

        if (restartFrameIndex) frameIndex = -1

        if (selfContained) {
            job = scope.launch {
                while (frameIndex <= maximumFrames) {
                    drawAnimationFrame()
                    delay(1.ticksToDuration())
                }
            }
        }
    }

    internal fun drawAnimationFrame() {
        //Animate animationFrameAmount frames, every animationInterval ticks
        delay += 1
        if (delay >= animationInterval) {
            var tempFrameIndex = ++frameIndex
            for (i in 0 until framesPerAnimationDraw) {
                tempFrameIndex++
                applyEnvelopes(tempFrameIndex)
            }
            delay = 0
        }
    }

    /**
     * Applies the envelopes and spawns the particle.
     */
    private fun applyEnvelopes(currentFrame: Int = frameIndex) {
        var newLocation = originLocation.getLocation().clone()
        var newOffset = Vector(0.0, 0.0, 0.0)
        var newCount = 0
        var newExtra = 0.0

        envelopes.forEach {
            val envelopePropertyType = it.propertyType
            val envelopeValue = it.getValueAt(currentFrame)

            when (envelopePropertyType) {
                PropertyType.POS_X -> {
                    newLocation.x += envelopeValue
                }

                PropertyType.POS_Y -> {
                    newLocation.y += envelopeValue
                }

                PropertyType.POS_Z -> {
                    newLocation.z += envelopeValue
                }

                PropertyType.OFFSET_X -> {
                    newOffset.x += envelopeValue
                }

                PropertyType.OFFSET_Y -> {
                    newOffset.y += envelopeValue
                }

                PropertyType.OFFSET_Z -> {
                    newOffset.z += envelopeValue
                }

                PropertyType.COUNT -> {
                    newCount += envelopeValue.toInt()
                }

                PropertyType.EXTRA -> {
                    newExtra += envelopeValue
                }

                PropertyType.NONE -> {
                    throw IllegalArgumentException("Property type NONE may not be used for top-level envelopes.")
                }
            }
        }

        spawnParticle(newLocation, newOffset, newCount, newExtra)
    }

    /**
     * Spawns the particle with the new provided properties.
     */
    private fun spawnParticle(newLocation: Location, newOffset: Vector, newCount: Int, newExtra: Double) {
        // Apply final rotations
        val newOffsetAfterRot = RotationUtil.applyRotationsForPoint(
            DoubleTriple(newOffset.x, newOffset.y, newOffset.z),
            rotationOptions,
            frameIndex
        )

        val newPositionAfterRot = RotationUtil.applyRotationsForPoint(
            DoubleTriple(newLocation.x, newLocation.y, newLocation.z),
            rotationOptions,
            frameIndex
        )

        newLocation.x = newPositionAfterRot.x
        newLocation.y = newPositionAfterRot.y
        newLocation.z = newPositionAfterRot.z

        newOffset.x = newOffsetAfterRot.x
        newOffset.y = newOffsetAfterRot.y
        newOffset.z = newOffsetAfterRot.z

        newLocation.world.spawnParticle(
            particleType,
            newLocation,
            newCount,
            newOffset.x,
            newOffset.y,
            newOffset.z,
            newExtra,
            dustOptions
        )
    }

    class Builder {

        /**
         * The location to display the animation at
         */
        var originLocation: PartigonLocation? = null

        /**
         * The Minecraft particle type.
         */
        var particleType = Particle.END_ROD

        /**
         * The envelopes that will affect this animation
         */
        var envelopes = listOf<Envelope>()

        /**
         * How many Minecraft particles to spawn on every frame. This will be rounded to an Int value.
         * If set to 0, the offset will control the particle's velocity.
         */
        var count: Envelope = 0.0.envelope

        /**
         * The X position of the particle.
         */
        var positionX: Envelope = 0.0.envelope

        /**
         * The Y position of the particle.
         */
        var positionY: Envelope = 0.0.envelope

        /**
         * The Z position of the particle.
         */
        var positionZ: Envelope = 0.0.envelope

        /**
         * The X offset of the particle. When count is set to 0, will control particle's X velocity.
         */
        var offsetX: Envelope = 0.0.envelope

        /**
         * The Y offset of the particle. When count is set to 0, will control particle's Y velocity.
         */
        var offsetY: Envelope = 0.0.envelope

        /**
         * The Z offset of the particle. When count is set to 0, will control particle's Z velocity.
         */
        var offsetZ: Envelope = 0.0.envelope

        /**
         * The extra value of the Minecraft particle. Controls its speed.
         */
        var extra: Envelope = 0.0.envelope

        /**
         * Options which can be applied to dust or dust transitions particles - a particle color and size.
         */
        var dustOptions: DustOptions? = null

        /**
         * The maximum amount of frames to animate. When passed, will stop the particle.
         * If set to null, will be ignored.
         */
        var maximumDuration: Duration = Duration.INFINITE

        /**
         * How many frames to animate everytime the animation is drawn.
         */
        //@Deprecated("You may use this field to generate shapes, however, this will be removed in the future and replaced with a better system.")
        var framesPerAnimationDraw: Int = 1

        /**
         * How often to draw a frame of the animation, in ticks.
         */
        var animationInterval: Int = 1

        /**
         * The rotation options to apply on the final particle's position and offset.
         * Remember that this is applied AFTER the envelopes, and on top of originLocation.
         */
        var rotationOptions: List<RotationOptions> = listOf()

        /**
         * Rotations to add to all Envelope groups.
         */
        var envelopeGroupsRotationOptions: List<RotationOptions> = listOf()

        /**
         * Adds this Envelope to the list of Envelopes of the particle.
         */
        fun Envelope.add() {
            envelopes += this
        }

        /**
         * Adds this Envelope Group to the list of Envelopes of the particle.
         */
        fun EnvelopeGroup.add() {
            envelopes += this.getEnvelopes()
        }

        /**
         * Adds this RotationOptions to the list of final Rotation Options of the particle to be applied last.
         */
        fun RotationOptions.add() {
            rotationOptions += this
        }

        /**
         * Adds this RotationOptions to all envelope groups.
         */
        fun RotationOptions.addToGroups() {
            envelopeGroupsRotationOptions += this
        }

        fun build(): Animation {
            requireNotNull(originLocation) { "Origin location must be set." }
            return Animation(this)
        }
    }
}