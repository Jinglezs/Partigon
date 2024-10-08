package xyz.gameoholic.partigon.particle.envelope.wrapper

import xyz.gameoholic.partigon.particle.envelope.EnvelopeGroup
import xyz.gameoholic.partigon.particle.envelope.LinearEnvelope
import xyz.gameoholic.partigon.particle.envelope.PropertyType
import xyz.gameoholic.partigon.particle.loop.Loop
import xyz.gameoholic.partigon.util.*
import xyz.gameoholic.partigon.util.rotation.RotationOptions

object LinearEnvelopeWrapper {
    /**
     * Envelope wrapper that creates a straight line between 2 points/offsets.
     *
     * @param envelopeGroupType The type of property (offset/position)
     * @param position1 The first position to interpolate (x,y,z).
     * @param position2 The second position to interpolate (x,y,z).
     * @param loop The loop to be used with the envelope.
     * @param rotationOptions The list of the rotations to apply to the curve.
     * @param completion How much of the line will be animated. If set to 1.0, the entire line would be drawn, if set to 0.5, only half, etc.
     *
     * @return The envelope group used to create the line.
     */
    fun linearEnvelopeGroup(
        envelopeGroupType: EnvelopeGroup.EnvelopeGroupType,
        position1: EnvelopeTriple,
        position2: EnvelopeTriple,
        loop: Loop,
        rotationOptions: List<RotationOptions> = listOf(),
        completion: Double = 1.0,
    ): EnvelopeGroup = EnvelopeGroup(
        LinearEnvelope(
            if (envelopeGroupType == EnvelopeGroup.EnvelopeGroupType.POSITION)
                PropertyType.POS_X
            else
                PropertyType.OFFSET_X,
            position1.x,
            position2.x,
            loop,
            completion
        ),
        LinearEnvelope(
            if (envelopeGroupType == EnvelopeGroup.EnvelopeGroupType.POSITION)
                PropertyType.POS_Y
            else
                PropertyType.OFFSET_Y,
            position1.y,
            position2.y,
            loop,
            completion
        ),
        LinearEnvelope(
            if (envelopeGroupType == EnvelopeGroup.EnvelopeGroupType.POSITION)
                PropertyType.POS_Z
            else
                PropertyType.OFFSET_Z,
            position1.z,
            position2.z,
            loop,
            completion
        ),
        rotationOptions
    )
}