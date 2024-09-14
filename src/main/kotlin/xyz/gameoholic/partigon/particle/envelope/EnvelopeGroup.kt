package xyz.gameoholic.partigon.particle.envelope

import xyz.gameoholic.partigon.util.rotation.RotationOptions
import java.lang.RuntimeException

/**
 * Used for grouping multiple position/offset envelopes together.
 * Sets the group of the envelopes provided to this one on init.
 * @param envelopeX The envelope used for the X component.
 * @param envelopeY The envelope used for the Y component.
 * @param envelopeZ The envelope used for the Z component.
 * @param rotationOptions List of rotation options to be used on the components.
 *
 * @throws RuntimeException If one of the envelopes already has a group assigned.
 * @throws IllegalArgumentException If the group doesn't support one of the envelopes' properties.
 */
class EnvelopeGroup(
    val envelopeX: Envelope, //todo: provide default values here, for offset AND position.
    val envelopeY: Envelope,
    val envelopeZ: Envelope,
    var rotationOptions: List<RotationOptions> = listOf() // Needs to be var, so Animation can add additional rotations on top
) {

    enum class EnvelopeGroupType { POSITION, OFFSET }

    init {
        if (envelopeX.envelopeGroup != null || envelopeY.envelopeGroup != null || envelopeZ.envelopeGroup != null)
            throw RuntimeException("Envelopes may only have one envelope group assigned to them.")
        if (envelopeX.propertyType != PropertyType.POS_X && envelopeX.propertyType != PropertyType.OFFSET_X &&
            envelopeY.propertyType != PropertyType.POS_Y && envelopeY.propertyType != PropertyType.OFFSET_Y &&
            envelopeZ.propertyType != PropertyType.POS_Z && envelopeZ.propertyType != PropertyType.OFFSET_Z)
            throw IllegalArgumentException("One of the envelopes' properties is invalid for group.")
        envelopeX.envelopeGroup = this
        envelopeY.envelopeGroup = this
        envelopeZ.envelopeGroup = this
    }
    /**
     * Returns a list of all envelopes.
     */
    fun getEnvelopes(): List<Envelope> {
        return listOf(envelopeX, envelopeY, envelopeZ)
    }
}