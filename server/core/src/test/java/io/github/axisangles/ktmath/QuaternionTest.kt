package io.github.axisangles.ktmath

import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuaternionTest {

	@Test
	fun plus() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(5f, 6f, 7f, 8f)
		val q3 = Quaternion(6f, 8f, 10f, 12f)
		assertEquals(q3, q1 + q2)
	}

	@Test
	fun times() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(5f, 6f, 7f, 8f)
		val q3 = Quaternion(-60f, 12f, 30f, 24f)
		assertEquals(q3, q1 * q2)
	}

	@Test
	fun timesScalarRhs() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(2f, 4f, 6f, 8f)
		assertEquals(q2, q1 * 2f)
	}

	@Test
	fun timesScalarLhs() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(2f, 4f, 6f, 8f)
		assertEquals(q2, 2f * q1)
	}

	@Test
	fun inverse() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(1f / 30f, -2f / 30f, -3f / 30f, -4f / 30f)
		assertEquals(q2, q1.inv())
	}

	@Test
	fun rightDiv() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(5f, 6f, 7f, 8f)
		val q3 = Quaternion(-60f, 12f, 30f, 24f)
		assertEquals(q1, q3 / q2)
	}

	@Test
	fun rightDivFloatRhs() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(2f, 4f, 6f, 8f)
		assertEquals(q1, q2 / 2f)
	}

	@Test
	fun rightDivFloatLhs() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(1f / 15f, -2f / 15f, -1f / 5f, -4f / 15f)

		assertEquals(q2, 2f / q1)
	}

	@Test
	fun pow() {
		val q = Quaternion(1f, 2f, 3f, 4f)
		assertEquals(q.pow(1f), q, 1e-5)
		assertEquals(q.pow(2f), q * q, 1e-5)
		assertEquals(q.pow(0f), Quaternion.IDENTITY, 1e-5)
		assertEquals(q.pow(-1f), q.inv(), 1e-5)
	}

	@Test
	fun interpQ() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(5f, 6f, 7f, 8f)
		val q3 = Quaternion(2.405691f, 3.5124686f, 4.619246f, 5.7260237f)
		assertEquals(q1.interpQ(q2, 0.5f), q3, 1e-7)
	}

	@Test
	fun interpR() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = -Quaternion(5f, 6f, 7f, 8f)
		val q3 = Quaternion(2.405691f, 3.5124686f, 4.619246f, 5.7260237f)
		assertEquals(q1.interpR(q2, 0.5f), q3, 1e-7)
	}

	@Test
	fun lerpQ() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(5f, 6f, 7f, 8f)
		val q3 = Quaternion(3f, 4f, 5f, 6f)
		assertEquals(q1.lerpQ(q2, 0.5f), q3, 1e-7)
	}

	@Test
	fun lerpR() {
		val q1 = Quaternion(1f, 2f, 3f, 4f)
		val q2 = Quaternion(-5f, -6f, -7f, -8f)
		val q3 = Quaternion(3f, 4f, 5f, 6f)
		assertEquals(q1.lerpR(q2, 0.5f), q3, 1e-7)
	}

	@Test
	fun angleToQ() {
		val q1 = Quaternion(1f, 0f, 0f, 0f)
		val q2 = Quaternion(0f, 1f, 0f, 0f)
		assertEquals(q1.angleToQ(q2), PI.toFloat() / 2f)
	}

	@Test
	fun angleToR() {
		val q1 = Quaternion(1f, 0f, 0f, 0f)
		val q2 = Quaternion(0f, 1f, 0f, 0f)
		assertEquals(q1.angleToR(q2), PI.toFloat())
	}

	@Test
	fun angleQ() {
		val q = Quaternion(0f, 1f, 0f, 0f)
		assertEquals(q.angleQ(), PI.toFloat() / 2f)
	}

	@Test
	fun angleR() {
		val q = Quaternion(0f, 1f, 0f, 0f)
		assertEquals(q.angleR(), PI.toFloat())
	}

	@Test
	fun angleAboutQ() {
		val q = Quaternion(1f, 1f, 1f, 0f)
		assertEquals(q.angleAboutQ(Vector3.POS_Y), PI.toFloat() / 4f)
	}

	@Test
	fun angleAboutR() {
		val q = Quaternion(1f, 1f, 1f, 0f)
		assertEquals(q.angleAboutR(Vector3.POS_Y), PI.toFloat() / 2f)
	}

	@Test
	fun project() {
		val q1 = Quaternion(1f, 1f, 1f, 0f)
		val q2 = Quaternion(1f, 0f, 1f, 0f)
		assertEquals(q1.project(Vector3.POS_Y), q2)
	}

	@Test
	fun reject() {
		val q1 = Quaternion(1f, 1f, 1f, 0f)
		val q2 = Quaternion(1f, 1f, 0f, 0f)
		assertEquals(q1.reject(Vector3.POS_Y), q2)
	}

	@Test
	fun align() {
		val q1 = Quaternion(0f, 1f, 0f, 0f)
		val q2 = Quaternion(0f, 0.5f, 0.5f, 0f)
		assertEquals(q1.align(Vector3.POS_X, Vector3.POS_Y), q2)
	}

	@Test
	fun fromTo() {
		val q1 = Quaternion(1f, 0f, 0f, 1f).unit()
		assertEquals(q1, Quaternion.fromTo(Vector3.POS_X, Vector3.POS_Y))
	}

	@Test
	fun sandwich() {
		val v1 = Quaternion(1f, 1f, 0f, 0f).sandwich(Vector3(1f, 1f, 0f))
		val v2 = Vector3(1f, 0f, 1f)
		assertEquals(v2, v1)
	}

	@Test
	fun sandwichUnitX() {
		val q = Quaternion(0.34f, 0.223f, -0.8f, -0.7f).unit()
		assertTrue(q.sandwichUnitX().isNear(q.sandwich(Vector3.POS_X)))
	}

	@Test
	fun sandwichUnitY() {
		val q = Quaternion(0.34f, 0.223f, -0.8f, -0.7f).unit()
		assertTrue(q.sandwichUnitY().isNear(q.sandwich(Vector3.POS_Y)))
	}

	@Test
	fun sandwichUnitZ() {
		val q = Quaternion(0.34f, 0.223f, -0.8f, -0.7f).unit()
		assertTrue(q.sandwichUnitZ().isNear(q.sandwich(Vector3.POS_Z)))
	}

	@Test
	fun axis() {
		val v1 = Quaternion(0f, Quaternion(1f, 2f, 3f, 4f).axis())
		val v2 = Quaternion(0f, Vector3(0.37139067f, 0.557086f, 0.74278134f))
		assertEquals(v2, v1, 1e-7)
	}

	@Test
	fun toRotationVector() {
		val v1 = Quaternion(1f, 2f, 3f, 4f).toRotationVector()
		val v2 = Vector3(1.0303806f, 1.5455709f, 2.0607612f)
		assertEquals(v2, v1)
	}

	@Test
	fun fromRotationVector() {
		val v1 = Quaternion.fromRotationVector(Vector3(1f, 2f, 3f))
		val v2 = Quaternion(-0.29555118f, 0.25532186f, 0.5106437f, 0.7659656f)
		assertEquals(v2, v1)
	}

	@Suppress("ktlint")
	@Test
	fun toMatrix() {
		val m1 = Matrix3(
			-1f,  0f,  0f,
			 0f, -1f,  0f,
			 0f,  0f,  1f)
		val m2 = Quaternion(0f, 0f, 0f, 2f).toMatrix()
		assertEquals(m1, m2)
	}

	private fun testEulerAngles(order: EulerOrder) {
		val inputQ = Quaternion(1f, 2f, 3f, 4f).unit()
		val outputQ = inputQ.toEulerAngles(order)
			.toQuaternion().twinNearest(Quaternion.IDENTITY)
		assertEquals(inputQ, outputQ, 1e-7)
	}

	@Test
	fun eulerAnglesXYZ() {
		testEulerAngles(EulerOrder.XYZ)
	}

	@Test
	fun eulerAnglesYZX() {
		testEulerAngles(EulerOrder.YZX)
	}

	@Test
	fun eulerAnglesZXY() {
		testEulerAngles(EulerOrder.ZXY)
	}

	@Test
	fun eulerAnglesZYX() {
		testEulerAngles(EulerOrder.ZYX)
	}

	@Test
	fun eulerAnglesYXZ() {
		testEulerAngles(EulerOrder.YXZ)
	}

	@Test
	fun eulerAnglesXZY() {
		testEulerAngles(EulerOrder.XZY)
	}

	companion object {
		private const val RELATIVE_TOLERANCE = 0.0

		fun assertEquals(
			expected: Quaternion,
			actual: Quaternion,
			tolerance: Double = RELATIVE_TOLERANCE,
		) {
			val len = (actual - expected).lenSq()
			val squareSum = expected.lenSq() + actual.lenSq()
			assertTrue(
				len <= tolerance * tolerance * squareSum,
				"Expected: $expected but got: $actual",
			)
		}
	}
}

var randSeed = 0
fun randInt(): Int {
	randSeed = (1103515245 * randSeed + 12345).mod(2147483648).toInt()
	return randSeed
}

fun randFloat(): Float = randInt().toFloat() / 2147483648

fun randGaussian(): Float {
	var thing = 1f - randFloat()
	while (thing == 0f) {
		// no 0s allowed
		thing = 1f - randFloat()
	}
	return sqrt(-2f * ln(thing)) * cos(PI.toFloat() * randFloat())
}

fun randMatrix(): Matrix3 = Matrix3(
	randGaussian(), randGaussian(), randGaussian(),
	randGaussian(), randGaussian(), randGaussian(),
	randGaussian(), randGaussian(), randGaussian(),
)

fun randQuaternion(): Quaternion = Quaternion(randGaussian(), randGaussian(), randGaussian(), randGaussian())

fun randRotMatrix(): Matrix3 = randQuaternion().toMatrix()

fun randVector(): Vector3 = Vector3(randGaussian(), randGaussian(), randGaussian())
