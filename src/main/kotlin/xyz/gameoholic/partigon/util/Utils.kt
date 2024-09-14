package xyz.gameoholic.partigon.util

import xyz.gameoholic.partigon.particle.envelope.ConstantEnvelope
import xyz.gameoholic.partigon.particle.envelope.Envelope
import xyz.gameoholic.partigon.particle.envelope.PropertyType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

typealias EnvelopeTriple = Triple<Envelope, Envelope, Envelope>
typealias EnvelopePair = Pair<Envelope, Envelope>
typealias DoubleTriple = Triple<Double, Double, Double>

val <T> Triple<T, *, *>.x
    get(): T = first
val <T> Triple<*, T, *>.y
    get(): T = second
val <T> Triple<*, *, T>.z
    get(): T = third

/**
 * Wraps a number in a ConstantEnvelope.
 */
val Number.envelope: ConstantEnvelope
    get() = ConstantEnvelope(PropertyType.NONE, this)

/**
 * Converts a Duration to minecraft ticks.
 */
val Duration.ticks: Int
    get() = (inWholeMilliseconds / 50).toInt() // 50 milliseconds per tick

/**
 * Converts a number of ticks to a Duration. The [Number] of ticks does not have to be a whole number.
 */
fun Number.ticksToDuration(): Duration = (this.toDouble() * 50).milliseconds // 50 milliseconds per tick