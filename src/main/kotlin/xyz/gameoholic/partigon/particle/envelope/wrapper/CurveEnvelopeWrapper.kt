package xyz.gameoholic.partigon.particle.envelope.wrapper

import xyz.gameoholic.partigon.particle.envelope.*
import xyz.gameoholic.partigon.particle.loop.Loop
import xyz.gameoholic.partigon.util.*
import xyz.gameoholic.partigon.util.rotation.RotationOptions
import java.lang.IllegalArgumentException

object CurveEnvelopeWrapper {

    /**
     * Represents the interpolation type of the curve relative to a line connecting 2 points
     * in 3D space.
     * This enum provides a way to specify the direction/orientation of a curve relative
     * to a line from point 1 to point 2.
     *
     * Visualize a line between point 1 and point 2. You're at point 1 and are facing point 2.
     * Setting the orientation to RIGHT will make the curve start at point 1, and
     * go around the line from the right, and finish at point 2.
     */
    enum class CurveOrientation {
        /**
         * 2D curve to the right of the line.
         */
        RIGHT,

        /**
         * 2D curve to the left of the line.
         */
        LEFT,

        /**
         * 2D curve above the line.
         */
        ABOVE,

        /**
         * 2D curve below the line.
         */
        BELOW,

        /**
         * 3D curve from the right side of the line, below it.
         */
        RIGHT_BELOW,

        /**
         * 3D curve from the right side of the line, above it.
         */
        RIGHT_ABOVE,

        /**
         * 3D curve from the left side of the line, below it.
         */
        LEFT_BELOW,

        /**
         * 3D curve from the left side of the line, above it.
         */
        LEFT_ABOVE
    }

    /**
     * Represents the component (X, Y, Z) of a vector.
     * This is used in curve envelopes with curve orientations to automatically
     * determine the trigonometric function to use.
     */
    enum class VectorComponent { X, Y, Z }

    /**
     * Trigonometric envelope wrapper that when applied on multiple properties,
     * creates a curve between 2 points.
     *
     * @param propertyType The property for the envelope to affect.
     * @param value1 The first value to interpolate.
     * @param value2 The second value to interpolate.
     * @param curveOrientation The orientation of the curve.
     * @param vectorComponent The vector component to be used for this curve property.
     * @param loop The loop to be used with the envelope.
     * @param completion How much of the curve will be animated. If set to 1.0, the entire curve would be drawn. If set to 2.0, it'll draw a half-ellipse. If set to 4.0, it'll draw an entire ellipse, etc.
     *
     * @throws IllegalArgumentException If an invalid combination of curve orientation & vector component was provided.
     * @return The trigonometric envelope to be used on this property to create the curve.
     */
    fun curveEnvelope(
        propertyType: PropertyType,
        value1: Envelope,
        value2: Envelope,
        curveOrientation: CurveOrientation,
        vectorComponent: VectorComponent,
        loop: Loop,
        completion: Double = 1.0,
    ): TrigonometricEnvelope {
        //todo: clean up this shitty code
        val trigFunc =
            if ((curveOrientation == CurveOrientation.LEFT || curveOrientation == CurveOrientation.LEFT_ABOVE || curveOrientation == CurveOrientation.LEFT_BELOW) && vectorComponent == VectorComponent.X)
                TrigFunc.SIN
            else if ((curveOrientation == CurveOrientation.LEFT || curveOrientation == CurveOrientation.LEFT_ABOVE || curveOrientation == CurveOrientation.LEFT_BELOW) && vectorComponent == VectorComponent.Z)
                TrigFunc.COS
            else if ((curveOrientation == CurveOrientation.RIGHT || curveOrientation == CurveOrientation.RIGHT_ABOVE || curveOrientation == CurveOrientation.RIGHT_BELOW) && vectorComponent == VectorComponent.X)
                TrigFunc.COS
            else if ((curveOrientation == CurveOrientation.RIGHT || curveOrientation == CurveOrientation.RIGHT_ABOVE || curveOrientation == CurveOrientation.RIGHT_BELOW) && vectorComponent == VectorComponent.Z)
                TrigFunc.SIN
            else if ((curveOrientation == CurveOrientation.RIGHT_BELOW || curveOrientation == CurveOrientation.LEFT_BELOW) && vectorComponent == VectorComponent.Y)
                TrigFunc.SIN
            else if ((curveOrientation == CurveOrientation.RIGHT_ABOVE || curveOrientation == CurveOrientation.LEFT_ABOVE) && vectorComponent == VectorComponent.Y)
                TrigFunc.COS
            else if ((curveOrientation == CurveOrientation.ABOVE) && vectorComponent == VectorComponent.Y)
                TrigFunc.SIN
            else if ((curveOrientation == CurveOrientation.ABOVE) && vectorComponent == VectorComponent.X)
                TrigFunc.COS
            else if ((curveOrientation == CurveOrientation.ABOVE) && vectorComponent == VectorComponent.Z)
                TrigFunc.COS
            else if ((curveOrientation == CurveOrientation.BELOW) && vectorComponent == VectorComponent.Y)
                TrigFunc.COS
            else if ((curveOrientation == CurveOrientation.BELOW) && vectorComponent == VectorComponent.X)
                TrigFunc.SIN
            else if ((curveOrientation == CurveOrientation.BELOW) && vectorComponent == VectorComponent.Z)
                TrigFunc.SIN
            else
                throw IllegalArgumentException("Invalid combination of curve orientation & vector component")

        return TrigonometricEnvelope.create(
            propertyType,
            value1,
            value2,
            trigFunc,
            loop,
            completion,
        )
    }

    /**
     * Trigonometric envelope wrapper that when applied on multiple properties,
     * creates a curve between 2 points/offsets.
     * This automatically determines the trigonometric function to use based
     * on the curve orientation and the property type.
     * This method may only be used with the follwing vector property
     * types: POS_X, POS_Y, POS_Z, OFFSET_X, OFFSET_Y, OFFSET_Z, and is preferred
     * if you are dealing with position/offset envelopes.
     *
     * @param propertyType The property for the envelope to affect.
     * @param value1 The first value to interpolate.
     * @param value2 The second value to interpolate.
     * @param curveOrientation The orientation of the curve.
     * @param loop The loop to be used with the envelope.
     * @param completion How much of the curve will be animated. If set to 1.0, the entire curve would be drawn. If set to 2.0, it'll draw a half-ellipse. If set to 4.0, it'll draw an entire ellipse, etc.
     *
     * @throws IllegalArgumentException If the method doesn't support the property type provided.
     */
    fun curveEnvelope(
        propertyType: PropertyType,
        value1: Envelope,
        value2: Envelope,
        curveOrientation: CurveOrientation,
        loop: Loop,
        completion: Double = 1.0,
    ): TrigonometricEnvelope {
        val vectorComponent =
            when (propertyType) {
                PropertyType.POS_X -> VectorComponent.X
                PropertyType.POS_Y -> VectorComponent.Y
                PropertyType.POS_Z -> VectorComponent.Z
                PropertyType.OFFSET_X -> VectorComponent.X
                PropertyType.OFFSET_Y -> VectorComponent.Y
                PropertyType.OFFSET_Z -> VectorComponent.Z
                else -> throw IllegalArgumentException("This method doesn't support this property type, see method docs for more info.")
            }

        return curveEnvelope(
            propertyType,
            value1,
            value2,
            curveOrientation,
            vectorComponent,
            loop,
            completion,
        )
    }


    /**
     * Envelope wrapper that creates a curve between 2 points/offsets
     * in 3D space, with rotations.
     *
     * @param envelopeGroupType The type of property (offset/position)
     * @param position1 The first position to interpolate (x,y,z).
     * @param position2 The second position to interpolate (x,y,z).
     * @param curveOrientation The orientation of the curve.
     * @param loop The loop to be used with the envelope.
     * @param rotationOptions The list of the rotations to apply to the curve.
     * @param completion How much of the curve will be animated. If set to 1.0, the entire curve would be drawn. If set to 2.0, it'll draw a half-ellipse. If set to 4.0, it'll draw an entire ellipse, etc.
     *
     * @return The envelope group used to create the curve.
     */
    fun curveEnvelopeGroup(
        envelopeGroupType: EnvelopeGroup.EnvelopeGroupType,
        position1: EnvelopeTriple,
        position2: EnvelopeTriple,
        curveOrientation: CurveOrientation,
        loop: Loop,
        rotationOptions: List<RotationOptions> = listOf(),
        completion: Double = 1.0,
    ): EnvelopeGroup = EnvelopeGroup(
        curveEnvelope(
            if (envelopeGroupType == EnvelopeGroup.EnvelopeGroupType.POSITION)
                PropertyType.POS_X
            else
                PropertyType.OFFSET_X,
            position1.x,
            position2.x,
            curveOrientation,
            loop,
            completion,
        ),
        curveEnvelope(
            if (envelopeGroupType == EnvelopeGroup.EnvelopeGroupType.POSITION)
                PropertyType.POS_Y
            else
                PropertyType.OFFSET_Y,
            position1.y,
            position2.y,
            curveOrientation,
            loop,
            completion,
        ),
        curveEnvelope(
            if (envelopeGroupType == EnvelopeGroup.EnvelopeGroupType.POSITION)
                PropertyType.POS_Z
            else
                PropertyType.OFFSET_Z,
            position1.z,
            position2.z,
            curveOrientation,
            loop,
            completion,
        ),
        rotationOptions
    )

}