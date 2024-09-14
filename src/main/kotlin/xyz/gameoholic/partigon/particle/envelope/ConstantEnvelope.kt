package xyz.gameoholic.partigon.particle.envelope

import xyz.gameoholic.partigon.particle.loop.ContinueLoop

/**
 * An envelope used for holding a constant Numeric value.
 *
 * @param propertyType The property for the envelope to affect.
 * @param value The value.
 */
class ConstantEnvelope(
    propertyType: PropertyType,
    private val value: Number
) :
    BasicEnvelope(
        propertyType,
        "",
        ContinueLoop(0),
        1.0,
        listOf()
    ) {

    // todo: add doc here
    constructor(value: Number) : this(PropertyType.NONE, value)

    override val envelopeExpression: String = value.toString()
    override val nestedEnvelopes: List<Envelope> = listOf()

    override fun copyWithPropertyType(propertyType: PropertyType): ConstantEnvelope {
        return ConstantEnvelope(propertyType, value)
    }

}