package com.github.gameoholic.partigon.particle.envelope.wrapper

import com.github.gameoholic.partigon.Utils
import com.github.gameoholic.partigon.particle.envelope.Envelope
import com.github.gameoholic.partigon.particle.envelope.TrigonometricEnvelope
import com.github.gameoholic.partigon.particle.loop.Loop
import java.lang.IllegalArgumentException

object CircleEnvelopeWrapper {

    /**
     * Represents the orientation of the circle relative to a line connecting 2 points
     * in 2D/3D space.
     * This enum provides a way to specify the direction/orientation of a circle relative
     * to a line from point 1 to point 2.
     *
     * Visualize a line between point 1 and point 2. You're at point 1 and are facing point 2.
     * Setting the orientation to RIGHT will make the circle start at point 1, and
     * go around the line from the right, and finish at point 2.
     */
    enum class CircleOrientation {
        /**
         * 2D Circle to the right of the line.
         */
        RIGHT,
        /**
         * 2D Circle to the left of the line.
         */
        LEFT,
        /**
         * 3D Circle from the right side of the line, below it.
         */
        RIGHT_DOWN,
        /**
         * 3D Circle from the right side of the line, above it.
         */
        RIGHT_UP,
        /**
         * 3D Circle from the left side of the line, below it.
         */
        LEFT_DOWN,
        /**
         * 3D Circle from the left side of the line, above it.
         */
        LEFT_UP
    }

    /**
     * Represents the component (X,Y,Z) of a vector.
     * This is used in circle envelopes with circle orientations to automatically
     * determine the trigonometric function to use.
     */
    enum class VectorComponent { X, Y, Z }

    /**
     * Trigonometric envelope wrapper that when applied on multiple properties,
     * creates a circle between 2 points.
     *
     * @param propertyType The property for the envelope to affect.
     * @param value1 The first value to interpolate.
     * @param value2 The second value to interpolate.
     * @param circleOrientation The orientation/direction of the circle.
     * @param vectorComponent The vector component to be used for this circle property.
     * @param loop The loop to be used with the envelope.
     * @param completion How much of the circle will be animated. If set to 1.0, an entire circle would be drawn. If set to 0.5, only half of it, etc.
     * @param isAbsolute Whether the values are absolute, or relative to the original particle's values.
     *
     * @throws IllegalArgumentException If an invalid combination of circle orientation & vector component was provided.
     */
    fun circleEnvelope(
        propertyType: Envelope.PropertyType,
        value1: Any,
        value2: Any,
        circleOrientation: CircleOrientation,
        vectorComponent: VectorComponent,
        loop: Loop,
        completion: Double = 1.0,
        isAbsolute: Boolean = false,
        bonusTemp: Double = 1.0,
    ): TrigonometricEnvelope {
        val trigFunc =
            if ((circleOrientation == CircleOrientation.LEFT || circleOrientation == CircleOrientation.LEFT_UP || circleOrientation == CircleOrientation.LEFT_DOWN) && vectorComponent == VectorComponent.X)
                TrigonometricEnvelope.TrigFunc.SIN
            else if ((circleOrientation == CircleOrientation.LEFT || circleOrientation == CircleOrientation.LEFT_UP || circleOrientation == CircleOrientation.LEFT_DOWN) && vectorComponent == VectorComponent.Z)
                TrigonometricEnvelope.TrigFunc.COS
            else if ((circleOrientation == CircleOrientation.RIGHT || circleOrientation == CircleOrientation.RIGHT_UP || circleOrientation == CircleOrientation.RIGHT_DOWN) && vectorComponent == VectorComponent.X)
                TrigonometricEnvelope.TrigFunc.COS
            else if ((circleOrientation == CircleOrientation.RIGHT || circleOrientation == CircleOrientation.RIGHT_UP || circleOrientation == CircleOrientation.RIGHT_DOWN) && vectorComponent == VectorComponent.Z)
                TrigonometricEnvelope.TrigFunc.SIN
            else if ((circleOrientation == CircleOrientation.RIGHT_DOWN || circleOrientation == CircleOrientation.LEFT_DOWN) && vectorComponent == VectorComponent.Y)
                TrigonometricEnvelope.TrigFunc.SIN
            else if ((circleOrientation == CircleOrientation.RIGHT_UP || circleOrientation == CircleOrientation.LEFT_UP) && vectorComponent == VectorComponent.Y)
                TrigonometricEnvelope.TrigFunc.COS
            else
                throw IllegalArgumentException("Invalid combination of circle orientation & vector component")

        return TrigonometricEnvelope(
            propertyType,
            value1,
            value2,
            trigFunc,
            loop,
            completion * 4,
            isAbsolute,
            bonusTemp
        )
    }

    /**
     * Trigonometric envelope wrapper that when applied on multiple properties,
     * creates a circle between 2 points.
     * This automatically determines the trigonometric function to use based
     * on the circle orientation and the property type.
     * This method may only be used with vector property types (POS_X, POS_Y, POS_Z)
     *
     * @param propertyType The property for the envelope to affect.
     * @param value1 The first value to interpolate.
     * @param value2 The second value to interpolate.
     * @param circleOrientation The orientation/direction of the circle.
     * @param loop The loop to be used with the envelope.
     * @param completion How much of the circle will be animated. If set to 1.0, an entire circle would be drawn. If set to 0.5, only half of it, etc.
     * @param isAbsolute Whether the values are absolute, or relative to the original particle's values.
     *
     * @throws IllegalArgumentException If the method doesn't support the property type provided.
     */
    fun circleEnvelope(
        propertyType: Envelope.PropertyType,
        value1: Any,
        value2: Any,
        circleOrientation: CircleOrientation,
        loop: Loop,
        completion: Double = 1.0,
        isAbsolute: Boolean = false,
        bonusTemp: Double = 1.0,
    ): TrigonometricEnvelope {
        val vectorComponent =
            when (propertyType) {
                Envelope.PropertyType.POS_X -> VectorComponent.X
                Envelope.PropertyType.POS_Y -> VectorComponent.Y
                Envelope.PropertyType.POS_Z -> VectorComponent.Z
                else -> throw IllegalArgumentException()
            }

        return circleEnvelope(
            propertyType,
            value1,
            value2,
            circleOrientation,
            vectorComponent,
            loop,
            completion,
            isAbsolute,
            bonusTemp
        )
    }


    fun positionCircleEnvelopes(
        position1: Utils.Vector,
        position2: Utils.Vector,
        circleOrientation: CircleOrientation,
        loop: Loop,
        completion: Double = 1.0,
        isAbsolute: Boolean = false,
        bonusTemp: Double = 1.0
    ): List<TrigonometricEnvelope> = listOf(
            circleEnvelope(
                Envelope.PropertyType.POS_X,
                position1.x,
                position2.x,
                circleOrientation,
                loop,
                completion,
                isAbsolute,
                bonusTemp
            ),
            circleEnvelope(
                Envelope.PropertyType.POS_Y,
                position1.y,
                position2.y,
                circleOrientation,
                loop,
                completion,
                isAbsolute,
                bonusTemp
            ),
            circleEnvelope(
                Envelope.PropertyType.POS_Z,
                position1.z,
                position2.z,
                circleOrientation,
                loop,
                completion,
                isAbsolute,
                bonusTemp
            )
        )



}