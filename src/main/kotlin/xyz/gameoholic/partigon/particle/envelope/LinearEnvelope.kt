package xyz.gameoholic.partigon.particle.envelope

import xyz.gameoholic.partigon.particle.loop.Loop


/**
 * An envelope used for creating linear lines between 2 points.
 *
 * @param propertyType The property for the envelope to affect.
 * @param value1 The first value to interpolate.
 * @param value2 The second value to interpolate.
 * @param loop The loop to be used with the envelope.
 * @param completion How much of the animation to animate. 1.0 for its entirety, 0.5 for half, etc.
 */
class LinearEnvelope(
    override val propertyType: PropertyType,
    private val value1: Envelope,
    private val value2: Envelope,
    override val loop: Loop,
    override val completion: Double = 1.0): BasicEnvelope(propertyType, "", loop, completion, listOf())
{
    override val envelopeExpression: String
    override val nestedEnvelopes: List<Envelope>

    /**
     * An envelope used for creating linear lines between 2 points.
     * This secondary constructor provides the property type as NONE, and should only be used for
     * constructor-parameter envelopes (positionY = ...) or for nested Envelopes.
     *
     * @param value1 The first value to interpolate.
     * @param value2 The second value to interpolate.
     * @param loop The loop to be used with the envelope.
     * @param completion How much of the animation to animate. 1.0 for its entirety, 0.5 for half, etc.
     */
    constructor(
        value1: Envelope,
        value2: Envelope,
        loop: Loop,
        completion: Double = 1.0) : this(PropertyType.NONE, value1, value2, loop, completion)

    init {
        val nestedEnvelopesList = mutableListOf<Envelope>()

        // Since we don't know the actual nested envelope value initialization-time,
        // we give it a placeholder (@ENV_X@) and replace it with the nested envelope's
        // value every tick.

        val value1String = "@ENV_0@"
        nestedEnvelopesList.add(value1)

        val value2String = "@ENV_1@"
        nestedEnvelopesList.add(value2)

        envelopeExpression = "$value1String + frame_index * (($value2String - $value1String) / ${loop.envelopeDuration - 1}) * $completion"
        nestedEnvelopes = nestedEnvelopesList.toList()
    }

    override fun copyWithPropertyType(propertyType: PropertyType): LinearEnvelope {
        return LinearEnvelope(propertyType, value1, value2, loop, completion)
    }

}