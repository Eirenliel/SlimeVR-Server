package dev.slimevr.autobone.errors;


import com.jme3.math.FastMath;
import dev.slimevr.autobone.AutoBoneTrainingStep;
import dev.slimevr.autobone.errors.proportions.ProportionLimiter;
import dev.slimevr.autobone.errors.proportions.RangeProportionLimiter;
import dev.slimevr.tracking.processor.HumanPoseManager;
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets;


// The distance from average human proportions
public class BodyProportionError implements IAutoBoneError {

	// TODO hip tracker stuff... Hip tracker should be around 3 to 5
	// centimeters.

	// The headset height is not the full height! This value compensates for the
	// offset from the headset height to the user height
	public static float eyeHeightToHeightRatio = 0.936f;

	// Default config
	// Height: 1.58
	// Full Height: 1.58 / 0.936 = 1.688034
	// Neck: 0.1 / 1.688034 = 0.059241
	// Torso: 0.56 / 1.688034 = 0.331747
	// Chest: 0.32 / 1.688034 = 0.18957
	// Waist: (0.56 - 0.32 - 0.04) / 1.688034 = 0.118481
	// Hip: 0.04 / 1.688034 = 0.023696
	// Hip Width: 0.26 / 1.688034 = 0.154025
	// Upper Leg: (0.92 - 0.50) / 1.688034 = 0.24881
	// Lower Leg: 0.50 / 1.688034 = 0.296203

	// "Expected" are values from Drillis and Contini (1966)
	// "Experimental" are values from experimentation by the SlimeVR community
	public static final ProportionLimiter[] proportionLimits = new ProportionLimiter[] {
		// Head
		// Experimental: 0.059
		new RangeProportionLimiter(
			0.059f,
			config -> config.getOffset(SkeletonConfigOffsets.HEAD),
			0.01f
		),

		// Neck
		// Expected: 0.052
		// Experimental: 0.059
		new RangeProportionLimiter(
			0.054f,
			config -> config.getOffset(SkeletonConfigOffsets.NECK),
			0.0015f
		),

		// Chest
		// Experimental: 0.189
		new RangeProportionLimiter(
			0.189f,
			config -> config.getOffset(SkeletonConfigOffsets.CHEST),
			0.02f
		),

		// Waist
		// Experimental: 0.118
		new RangeProportionLimiter(
			0.118f,
			config -> config.getOffset(SkeletonConfigOffsets.WAIST),
			0.05f
		),

		// Hip
		// Experimental: 0.0237
		new RangeProportionLimiter(
			0.0237f,
			config -> config.getOffset(SkeletonConfigOffsets.HIP),
			0.01f
		),

		// Hip Width
		// Expected: 0.191
		// Experimental: 0.154
		new RangeProportionLimiter(
			0.184f,
			config -> config.getOffset(SkeletonConfigOffsets.HIPS_WIDTH),
			0.04f
		),

		// Upper Leg
		// Expected: 0.245
		new RangeProportionLimiter(
			0.245f,
			config -> config.getOffset(SkeletonConfigOffsets.UPPER_LEG),
			0.015f
		),

		// Lower Leg
		// Expected: 0.246 (0.285 including below ankle, could use a separate
		// offset?)
		new RangeProportionLimiter(
			0.285f,
			config -> config.getOffset(SkeletonConfigOffsets.LOWER_LEG),
			0.02f
		),
	};

	public static ProportionLimiter getProportionLimitForOffset(SkeletonConfigOffsets offset) {
		ProportionLimiter result = null;
		switch (offset) {
			case HEAD -> result = proportionLimits[0];
			case NECK -> result = proportionLimits[1];
			case CHEST -> result = proportionLimits[2];
			case WAIST -> result = proportionLimits[3];
			case HIP -> result = proportionLimits[4];
			case HIPS_WIDTH -> result = proportionLimits[5];
			case UPPER_LEG -> result = proportionLimits[6];
			case LOWER_LEG -> result = proportionLimits[7];
		}
		return result;
	}

	@Override
	public float getStepError(AutoBoneTrainingStep trainingStep) throws AutoBoneException {
		return getBodyProportionError(
			trainingStep.getHumanPoseManager1(),
			trainingStep.getCurrentHeight()
		);
	}

	public float getBodyProportionError(HumanPoseManager humanPoseManager, float height) {
		float fullHeight = height / eyeHeightToHeightRatio;

		float sum = 0f;
		for (ProportionLimiter limiter : proportionLimits) {
			sum += FastMath.abs(limiter.getProportionError(humanPoseManager, fullHeight));
		}

		return sum;
	}
}
