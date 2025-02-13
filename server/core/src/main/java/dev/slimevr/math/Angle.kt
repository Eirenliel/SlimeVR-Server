package dev.slimevr.math

import com.jme3.math.FastMath
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

class Angle {

	private val rad: Float
	private val deg: Float?

	private constructor() {
		rad = 0.0f
		deg = null
	}

	private constructor(rad: Float) {
		this.rad = rad
		this.deg = null
	}

	/**
	 * Constructor that preserves the degrees, so that we don't have angles like
	 * 0.999999 deg.
	 */
	private constructor(rad: Float, deg: Float) {
		this.rad = rad
		this.deg = deg
	}

	fun toRad() = rad
	fun toDeg() = deg ?: (rad * FastMath.RAD_TO_DEG)

	/**
	 * Normalizes the angle to between [0, 2*PI)
	 */
	fun normalized() =
		if (rad < 0.0f || rad >= FastMath.TWO_PI) {
			Angle(rad - floor(rad * FastMath.INV_TWO_PI) * FastMath.TWO_PI)
		} else {
			this
		}

	/**
	 * Normalizes the angle to between [-PI, PI)
	 */
	fun normalizedAroundZero() =
		normalized().let {
			if (it.rad > FastMath.PI) {
				Angle(it.rad - FastMath.TWO_PI)
			} else {
				it
			}
		}

	fun nearZero(error: Angle = DEFAULT_NEAR_ANGLE) =
		abs(this) < error

	operator fun unaryPlus() = Angle(rad)
	operator fun unaryMinus() = Angle(-rad)
	operator fun plus(other: Angle) = Angle(rad + other.rad)
	operator fun minus(other: Angle) = Angle(rad - other.rad)
	operator fun times(scale: Float) = Angle(rad * scale)
	operator fun div(scale: Float) = Angle(rad / scale)

	operator fun compareTo(other: Angle): Int = rad.compareTo(other.rad)
	override fun toString(): String = "${toDeg()} deg"

	companion object {
		val ZERO = Angle()

		fun ofRad(rad: Float) = Angle(rad)
		fun ofDeg(deg: Float) = Angle(deg * FastMath.DEG_TO_RAD, deg)

		fun abs(angle: Angle) = ofRad(abs(angle.rad))

		// Signed angle between two angles
		fun signedBetween(a: Angle, b: Angle) =
			ofRad(a.rad - b.rad).normalizedAroundZero()

		// Angle between two vectors
		fun absBetween(a: Vector3, b: Vector3) =
			ofRad(a.angleTo(b))

		// Angle between two rotations in rotation space
		fun absBetween(a: Quaternion, b: Quaternion) =
			ofRad(a.angleToR(b))

		private val DEFAULT_NEAR_ANGLE = ofRad(1e-6f)
	}
}
