package io.eiren.math

import com.jme3.math.FastMath
import com.jme3.math.FastMath.normalize
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

object FloatMath {
	const val PI: Float = Math.PI.toFloat()
	const val TWO_PI: Float = (Math.PI * 2).toFloat()
	const val ANGLE_EPSILON: Float = 0.028f // in degrees (float

	// epsilon for sin/cos)
	val ANGLE_EPSILON_RAD: Float = toRad(ANGLE_EPSILON)

	const val ZERO_TOLERANCE_F: Float = FastMath.ZERO_TOLERANCE
	const val ZERO_TOLERANCE_D: Double = 0.0001

	val SQRT_TWO: Float = kotlin.math.sqrt(2.0).toFloat()
	val INV_SQRT_TWO: Float = (1.0 / kotlin.math.sqrt(2.0)).toFloat()
	val SQRT_THREE: Float = kotlin.math.sqrt(3.0).toFloat()
	val INV_SQRT_THREE: Float = 1f / SQRT_THREE
	const val TWO_FPI: Float = PI * 2

	const val SIN_75_DEG: Float = 0.965926f
	const val SIN_60_DEG: Float = 0.866025f
	const val SIN_45_DEG: Float = 0.707107f
	const val SIN_30_DEG: Float = 0.5f
	const val SIN_15_DEG: Float = 0.258819f

	const val COS_75_DEG: Float = 0.258819f
	const val COS_60_DEG: Float = 0.5f
	const val COS_45_DEG: Float = 0.707107f
	const val COS_30_DEG: Float = 0.866025f
	const val COS_15_DEG: Float = 0.965926f

	const val TEN_BITS: Int = (0.inv() shl 10).inv()
	const val TENTH_BIT: Int = 1 shl 10
	const val TEN_BITS_MAX: Int = (0.inv() shl 9).inv()
	const val TEN_BITS_MAX_UNSIGNED: Int = (0.inv() shl 10).inv()
	const val TWO_BITS: Int = (0.inv() shl 2).inv()
	const val SECOND_BIT: Int = 1 shl 2
	const val TWO_BITS_MAX: Int = (0.inv() shl 1).inv()
	const val TWO_BITS_MAX_UNSIGNED: Int = (0.inv() shl 2).inv()

	fun roundIfZero(x: Float): Float = if (kotlin.math.abs(x.toDouble()) < ZERO_TOLERANCE_F) 0.0f else x

	fun equalsToZero(x: Float): Boolean = kotlin.math.abs(x.toDouble()) < ZERO_TOLERANCE_F

	fun lessThanZero(x: Float): Boolean = (x < -ZERO_TOLERANCE_F)

	fun lessOrEqualsToZero(x: Float): Boolean = (x < ZERO_TOLERANCE_F)

	fun greaterThanZero(x: Float): Boolean = (x > ZERO_TOLERANCE_F)

	fun greaterOrEqualsToZero(x: Float): Boolean = (x > -ZERO_TOLERANCE_F)

	fun equalsToZero(x: Float, epsilon: Float): Boolean = kotlin.math.abs(x.toDouble()) < epsilon

	fun equalsWithEpsilon(x: Float, y: Float): Boolean = kotlin.math.abs((x - y).toDouble()) < ZERO_TOLERANCE_F

	fun equalsWithEpsilon(x: Float, y: Float, epsilon: Float): Boolean = kotlin.math.abs((x - y).toDouble()) < epsilon

	fun lessWithEpsilon(x: Float, y: Float): Boolean = (x < y - ZERO_TOLERANCE_F)

	fun lessOrEqualsWithEpsilon(x: Float, y: Float): Boolean = (x < y + ZERO_TOLERANCE_F)

	fun lessWithEpsilon(x: Float, y: Float, epsilon: Float): Boolean = (x < y - epsilon)

	fun lessOrEqualsWithEpsilon(x: Float, y: Float, epsilon: Float): Boolean = (x < y + epsilon)

	fun greaterWithEpsilon(x: Float, y: Float): Boolean = (x > y + ZERO_TOLERANCE_F)

	fun greaterOrEqualsWithEpsilon(x: Float, y: Float): Boolean = (x > y - ZERO_TOLERANCE_F)

	fun greaterWithEpsilon(x: Float, y: Float, epsilon: Float): Boolean = (x > y + epsilon)

	fun greaterOrEqualsWithEpsilon(x: Float, y: Float, epsilon: Float): Boolean = (x > y - epsilon)

	fun roundIfZero(x: Double): Double = if (kotlin.math.abs(x) < ZERO_TOLERANCE_D) 0.0 else x

	fun equalsToZero(x: Double): Boolean = kotlin.math.abs(x) < ZERO_TOLERANCE_D

	fun equalsWithEpsilon(x: Double, y: Double): Boolean = kotlin.math.abs(x - y) < ZERO_TOLERANCE_D

	fun lessWithEpsilon(x: Double, y: Double): Boolean = (x < y - ZERO_TOLERANCE_D)

	fun lessOrEqualsWithEpsilon(x: Double, y: Double): Boolean = (x < y + ZERO_TOLERANCE_D)

	fun greaterWithEpsilon(x: Double, y: Double): Boolean = (x > y + ZERO_TOLERANCE_D)

	fun greaterOrEqualsWithEpsilon(x: Double, y: Double): Boolean = (x > y - ZERO_TOLERANCE_D)

	fun toDegrees(angrad: Float): Float = angrad * 180.0f / PI

	fun toRad(deg: Float): Float = deg / 180.0f * PI

	fun radEqual(angle1: Float, angle2: Float): Boolean {
		val diff = clampRad(angle1 - angle2)
		return kotlin.math.abs(diff.toDouble()) < ANGLE_EPSILON_RAD
	}

	fun degreesEqual(angle1: Float, angle2: Float): Boolean {
		val diff = clampDegrees(angle1 - angle2)
		return kotlin.math.abs(diff.toDouble()) < ANGLE_EPSILON
	}

	@Deprecated(
		"use {@link #normalizeRad(float)}",
		ReplaceWith("normalizeRad(angle)", "io.eiren.math.FloatMath.normalizeRad"),
	)
	fun clampRad(angle: Float): Float = normalizeRad(angle)

	fun normalizeRad(angle: Float): Float = normalize(angle, -FastMath.PI, FastMath.PI)

	@Deprecated(
		"use {@link #normalizeDegrees(float)}",
		ReplaceWith("normalizeDegrees(angle)", "io.eiren.math.FloatMath.normalizeDegrees"),
	)
	fun clampDegrees(angle: Float): Float = normalizeDegrees(angle)

	fun normalizeDegrees(angle: Float): Float = normalize(angle, -180f, 180f)

	fun animateEase(t: Float): Float {
		// Special case of Bezier interpolation (p0 = p1 = 0, p2 = p3 = 1)
		return (3.0f - 2.0f * t) * t * t
	}

	fun animateEaseIn(t: Float): Float = t * t

	/**
	 * Lineary remaps value from the source interval to the target interval.
	 * [details](https://en.wikipedia.org/wiki/Linear_interpolation)
	 */
	fun mapValue(
		value: Float,
		sourceStart: Float,
		sourceEnd: Float,
		targetStart: Float,
		targetEnd: Float,
	): Float = (
		targetStart +
			(value - sourceStart) * (targetEnd - targetStart) / (sourceEnd - sourceStart)
		)

	/**
	 * Clamps the given value and remaps to the target interval.
	 *
	 *
	 * Note the source interval values should be sorted.
	 */
	fun mapValueWithClampBefore(
		value: Float,
		sourceBottom: Float,
		sourceTop: Float,
		targetBottom: Float,
		targetTop: Float,
	): Float = mapValue(
		clamp(value, sourceBottom, sourceTop),
		sourceBottom,
		sourceTop,
		targetBottom,
		targetTop,
	)

	/**
	 * Remaps the given value to the target interval and clamps.
	 *
	 *
	 * Note the target interval values should be sorted.
	 */
	fun mapValueWithClampAfter(
		value: Float,
		sourceBottom: Float,
		sourceTop: Float,
		targetBottom: Float,
		targetTop: Float,
	): Float = clamp(
		mapValue(value, sourceBottom, sourceTop, targetBottom, targetTop),
		targetBottom,
		targetTop,
	)

	fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
		// Scale, bias and saturate x to 0..1 range
		var x = x
		x = FastMath.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f)
		// Evaluate polynomial
		return x * x * (3f - 2f * x)
	}

	fun smootherstep(edge0: Float, edge1: Float, x: Float): Float {
		// Scale, and clamp x to 0..1 range
		var x = x
		x = FastMath.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f)
		// Evaluate polynomial
		return x * x * x * (x * (x * 6f - 15f) + 10f)
	}

	/**
	 * Applies linear contrast (with clamping).
	 *
	 * @param t - input value in range (0..1)
	 * @param k - contrast factor in range (-1..1):
	 *
	 *  * 1.0 - maximal contrast
	 *  * **0.0** - bypass (returns input value)
	 *  * -1.0 - minimal contrast (returns 0.5f for any input)
	 *
	 * @return contrasted value in range (0..1)
	 */
	fun contrastLinear(t: Float, k: Float): Float {
		val x = 2f * t - 1f // -1..1
		val gamma = (1f + k) / (1f - k)
		val f = FastMath.clamp(gamma * x, -1f, 1f) // -1..1
		return 0.5f * (f + 1f) // 0..1
	}

	/**
	 * Applies non-linear contrast by power function.
	 *
	 * @param t - input value in range (0..1)
	 * @param k - contrast factor in range (-1..1) exclusive:
	 *
	 *  * 0.999 - maximal contrast
	 *  * 0.0 - bypass (returns input value)
	 *  * -0.999 - minimal contrast
	 *
	 * @return contrasted value in range (0..1)
	 */
	fun contrastPower(t: Float, k: Float): Float {
		val x = 2f * t - 1f // -1..1
		val gamma = (1f - k) / (1f + k)
		val f = sign(x) * abs(x).pow(gamma) // -1..1
		return 0.5f * (f + 1f) // 0..1
	}

	/**
	 * Applies non-linear contrast by square splines.
	 *
	 * @param t - input value in range (0..1)
	 * @param k - contrast factor in range (-1..1):
	 *
	 *  * 1.0 - maximal contrast
	 *  * 0.0 - bypass (returns input value)
	 *  * -1.0 - minimal contrast
	 *
	 * @return contrasted value in range (0..1)
	 */
	fun contrastQuadricSpline(t: Float, k: Float): Float {
		val x = 2f * t - 1f // -1..1
		val f = x * (1f + k * (1f - abs(x))) // -1..1
		return 0.5f * (f + 1f) // 0..1
	}

	/**
	 * Applies non-linear contrast by square splines inverted function.
	 *
	 * @param t - input value in range (0..1)
	 * @param k - contrast factor in range (-2..2):
	 *
	 *  * 2.0 - maximal contrast
	 *  * 0.0 - bypass (returns input value)
	 *  * -2.0 - minimal contrast
	 *
	 * @return contrasted value in range (0..1)
	 */
	fun contrastInvertQuadricSpline(t: Float, k: Float): Float {
		val x = 2f * t - 1f // -1..1
		val g = if (k > 0) {
			sign(x) * sqrt(abs(x)) - 2f * x
		} else {
			sign(x) * (sqrt(1f - abs(x)) - 1f)
		}
		val f = (1f + k) * x + k * g // -1..1
		return 0.5f * (f + 1f) // 0..1
	}

	/**
	 * Applies non-linear contrast by cubic splines.
	 *
	 * @param t - input value in range (0..1)
	 * @param k - contrast factor in range (-1..1):
	 *
	 *  * 1.0 - maximal contrast
	 *  * 0.0 - bypass (returns input value)
	 *  * -1.0 - minimal contrast
	 *
	 * @return contrasted value in range (0..1)
	 */
	fun contrastCubicSpline(t: Float, k: Float): Float {
		val x = 2f * t - 1f // -1..1
		var f = x * (1f + abs(k) * (x * x - 1f))
		if (k < 0) f -= x * 3f * k * (1f - abs(x))
		return 0.5f * (f + 1f) // 0..1
	}

	fun fraction(f: Float): Float = f - f.toInt()

	fun fraction(d: Double): Double = d - d.toLong()

	fun distance(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float): Float = distance(x1 - x0, y1 - y0, z1 - z0)

	fun distance(x: Float, y: Float, z: Float): Float = sqrt(sqrDistance(x, y, z))

	fun sqrDistance(x: Float, y: Float, z: Float): Float = x * x + y * y + z * z

	fun distance(x: Float, y: Float): Float = sqrt(sqrDistance(x, y))

	fun sqrDistance(x: Float, y: Float): Float = x * x + y * y

	fun sqrDistance(v: Vector3, x1: Float, y1: Float, z1: Float): Float = sqrDistance(x1 - v.x, y1 - v.y, z1 - v.z)

	fun sqrDistance(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float): Float = sqrDistance(x1 - x0, y1 - y0, z1 - z0)

	/**
	 * The same as FastMath.clamp
	 */
	fun clamp(value: Float, min: Float, max: Float): Float = kotlin.math.max(min.toDouble(), kotlin.math.min(max.toDouble(), value.toDouble())).toFloat()

	fun int2101010RevToFloats(packedValue: Int, source: Vector3): Vector3 {
		var x = source.x
		var y = source.y
		var z = source.z

		x = (packedValue and TEN_BITS_MAX).toFloat()
		if ((packedValue and TENTH_BIT) != 0) x *= -1f
		y = ((packedValue ushr 10) and TEN_BITS_MAX).toFloat()
		if ((packedValue and (TENTH_BIT shl 10)) != 0) y *= -1f
		z = ((packedValue ushr 20) and TEN_BITS_MAX).toFloat()
		if ((packedValue and (TENTH_BIT shl 20)) != 0) z *= -1f
		return Vector3(x, y, z)
	}

	fun floatToInt210101Rev(values: Vector3): Int {
		var store = 0
		store = store or ((values.x.toInt()) and TEN_BITS_MAX)
		if (values.x < 0) store = store or TENTH_BIT
		store = store or (((values.y.toInt()) and TEN_BITS_MAX) shl 10)
		if (values.y < 0) store = store or (TENTH_BIT shl 10)
		store = store or (((values.z.toInt()) and TEN_BITS_MAX) shl 20)
		if (values.z < 0) store = store or (TENTH_BIT shl 20)
		return store
	}

	fun floatToInt210101RevNormalized(values: Vector3): Int {
		var store = 0
		store = store or (((values.x * TEN_BITS).toInt()) and TEN_BITS_MAX)
		if (values.x < 0) store = store or TENTH_BIT
		store = store or ((((values.y * TEN_BITS).toInt()) and TEN_BITS_MAX) shl 10)
		if (values.y < 0) store = store or (TENTH_BIT shl 10)
		store = store or ((((values.z * TEN_BITS).toInt()) and TEN_BITS_MAX) shl 20)
		if (values.z < 0) store = store or (TENTH_BIT shl 20)
		return store
	}

	fun floatToUnsignedInt210101Rev(values: Vector3): Int {
		var store = 0
		store = store or ((values.x.toInt()) and TEN_BITS)
		store = store or (((values.y.toInt()) and TEN_BITS) shl 10)
		store = store or (((values.z.toInt()) and TEN_BITS) shl 20)
		return store
	}

	fun floatToUnsignedInt210101RevNormalized(values: Vector3): Int {
		var store = 0
		store = store or (((values.x * TEN_BITS).toInt()) and TEN_BITS)
		store = store or ((((values.y * TEN_BITS).toInt()) and TEN_BITS) shl 10)
		store = store or ((((values.z * TEN_BITS).toInt()) and TEN_BITS) shl 20)
		return store
	}

	fun floatToInt210101Rev(x: Float, y: Float, z: Float): Int {
		var store = 0
		store = store or ((x.toInt()) and TEN_BITS_MAX)
		if (x < 0) store = store or TENTH_BIT
		store = store or (((y.toInt()) and TEN_BITS_MAX) shl 10)
		if (y < 0) store = store or (TENTH_BIT shl 10)
		store = store or (((z.toInt()) and TEN_BITS_MAX) shl 20)
		if (z < 0) store = store or (TENTH_BIT shl 20)
		return store
	}

	fun floatToUnsignedInt210101Rev(x: Float, y: Float, z: Float): Int {
		var store = 0
		store = store or ((x.toInt()) and TEN_BITS)
		store = store or (((y.toInt()) and TEN_BITS) shl 10)
		store = store or (((z.toInt()) and TEN_BITS) shl 20)
		return store
	}

	fun unsignedInt2101010RevToFloats(packedValue: Int, source: Vector3): Vector3 {
		var x = source.x
		var y = source.y
		var z = source.z

		x = (packedValue and TEN_BITS).toFloat()
		y = ((packedValue ushr 10) and TEN_BITS).toFloat()
		z = ((packedValue ushr 20) and TEN_BITS).toFloat()

		return Vector3(x, y, z)
	}

	fun int2101010RevNormalizedToFloats(packedValue: Int, source: Vector3): Vector3 {
		var source = source
		source = int2101010RevToFloats(packedValue, source)
		var x = source.x
		var y = source.y
		var z = source.z

		x /= TEN_BITS_MAX.toFloat()
		y /= TEN_BITS_MAX.toFloat()
		z /= TEN_BITS_MAX.toFloat()
		return Vector3(x, y, z)
	}

	fun unsignedInt2101010RevNormalizedToFloats(
		packedValue: Int,
		source: Vector3,
	): Vector3 {
		var source = source
		source = unsignedInt2101010RevToFloats(packedValue, source)
		var x = source.x
		var y = source.y
		var z = source.z

		x /= TEN_BITS.toFloat()
		y /= TEN_BITS.toFloat()
		z /= TEN_BITS.toFloat()

		return Vector3(x, y, z)
	}
}
