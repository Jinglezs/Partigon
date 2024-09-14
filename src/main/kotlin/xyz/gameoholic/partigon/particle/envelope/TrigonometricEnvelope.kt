package xyz.gameoholic.partigon.particle.envelope

import xyz.gameoholic.partigon.particle.loop.Loop

//TODO: fix extra frames bug
/**
 * An envelope used for creating curves between two points and circles using trigonometric functions.
 *
 * @param propertyType The property for the envelope to affect. NONE should only be used for constructor-parameter envelopes (positionY = ...) or for nested Envelopes.
 * @param value1 The first value to interpolate.
 * @param value2 The second value to interpolate.
 * @param completion How much of the animation will be animated. If set to 1.0, half a wave length. If set to 2.0, one wavelength, etc.
 * @param trigFunc The trigonometric function to use to animate the curve.
 * @param loop The loop to be used with the envelope.
 */
open class TrigonometricEnvelope(
    override val propertyType: PropertyType = PropertyType.NONE,
    private val value1: Envelope,
    private val value2: Envelope,
    private val trigFunc: TrigFunc,
    override val loop: Loop,
    override val completion: Double = 1.0,
    override val envelopeExpression: String,
    override val nestedEnvelopes: List<Envelope>
) : BasicEnvelope(propertyType, "", loop, completion, listOf()) {

    companion object {
        /**
         * Creates a trigonometric envelope between two points.
         * Automatically creates the envelope expression and nested envelopes.
         */
        fun create(
            propertyType: PropertyType,
            value1: Envelope,
            value2: Envelope,
            trigFunc: TrigFunc,
            loop: Loop,
            completion: Double = 1.0
        ): TrigonometricEnvelope {
            val animProgress = "frame_index / ${(loop.envelopeDuration - 1)}" //The animation progress, from 0.0 to 1.0

            val nestedEnvelopesList = mutableListOf<Envelope>()
            // Since we don't know the actual nested envelope value initialization-time,
            // we give it a placeholder (@ENV_X@) and replace it with the nested envelope's
            // value every tick.

            val value1String = "@ENV_0@"
            nestedEnvelopesList.add(value1)

            val value2String = "@ENV_1@"
            nestedEnvelopesList.add(value2)

            // Cos starts at 1 and heads down until pi radians. Because we interpolate the value from down, to up, we must switch the values of the two values.
            val envelopeExpression = if (trigFunc == TrigFunc.COS) {
                "$value2String + ($value1String - $value2String) * ${trigFunc.value}(pi * $animProgress * $completion)"
            } else {
                "$value1String + ($value2String - $value1String) * ${trigFunc.value}(pi * $animProgress * $completion)"
            }

            return TrigonometricEnvelope(
                propertyType, value1, value2, trigFunc, loop, completion, envelopeExpression, nestedEnvelopesList.toList()
            )
        }
    }

    override fun copyWithPropertyType(propertyType: PropertyType): TrigonometricEnvelope = TrigonometricEnvelope(
        propertyType, value1, value2, trigFunc, loop, completion, envelopeExpression, nestedEnvelopes
    )
}

enum class TrigFunc(val value: String) { SIN("sin"), COS("cos"), TAN("tan"), COT("cot"), COSEC("cosec"), SEC("sec") }