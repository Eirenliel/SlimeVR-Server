import { FlatDeviceTracker } from '@/hooks/app';
import { normalizeAngleAroundZero, RAD_TO_DEG } from '@/maths/angle';
import { QuaternionFromQuatT } from '@/maths/quaternion';
import { Control, FieldPath, UseFormSetValue } from 'react-hook-form';
import { BodyPart } from 'solarxr-protocol';
import { SettingsForm } from '@/components/settings/pages/GeneralSettings';
import { Button } from '@/components/commons/Button';
import { CheckBox } from '@/components/commons/Checkbox';
import { WrenchIcon } from '@/components/commons/icon/WrenchIcons';
import { NumberSelector } from '@/components/commons/NumberSelector';
import { Typography } from '@/components/commons/Typography';
import { SettingsPagePaneLayout } from '@/components/settings/SettingsPageLayout';
import { useLocalization } from '@fluent/react';
import { useTrackers } from '@/hooks/tracker';
import { useLocaleConfig } from '@/i18n/config';
import { Euler } from 'three';

export function StayAlignedSettings({
  getValues,
  setValue,
  control,
}: {
  getValues: () => SettingsForm;
  setValue: UseFormSetValue<SettingsForm>;
  control: Control<SettingsForm, any>;
}) {
  const { l10n } = useLocalization();
  const { currentLocales } = useLocaleConfig();
  const degreePerSecFormat = new Intl.NumberFormat(currentLocales, {
    style: 'unit',
    unit: 'degree-per-second',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  const degreeFormat = new Intl.NumberFormat(currentLocales, {
    style: 'unit',
    unit: 'degree',
    maximumFractionDigits: 0,
  });

  const { useConnectedIMUTrackers } = useTrackers();
  const trackers = useConnectedIMUTrackers();

  const values = getValues();

  const yawBetweenInDeg = (
    leftTracker: FlatDeviceTracker,
    rightTracker: FlatDeviceTracker
  ) => {
    const leftTrackerYaw = new Euler().setFromQuaternion(
      QuaternionFromQuatT(leftTracker.tracker.rotationReferenceAdjusted),
      'YZX'
    ).y;
    const rightTrackerYaw = new Euler().setFromQuaternion(
      QuaternionFromQuatT(rightTracker.tracker.rotationReferenceAdjusted),
      'YZX'
    ).y;
    const yawDelta = normalizeAngleAroundZero(leftTrackerYaw - rightTrackerYaw);
    return yawDelta * RAD_TO_DEG;
  };

  function findTracker(bodyPart: BodyPart): FlatDeviceTracker | undefined {
    return trackers.find((t) => t.tracker.info?.bodyPart === bodyPart);
  }

  const detectAngles = (
    upperLegKey: FieldPath<SettingsForm>,
    lowerLegKey: FieldPath<SettingsForm>,
    footKey?: FieldPath<SettingsForm>
  ) => {
    const leftUpperLegTracker = findTracker(BodyPart.LEFT_UPPER_LEG);
    const rightUpperLegTracker = findTracker(BodyPart.RIGHT_UPPER_LEG);
    if (leftUpperLegTracker && rightUpperLegTracker) {
      const upperLegToBodyAngleInDeg =
        yawBetweenInDeg(leftUpperLegTracker, rightUpperLegTracker) / 2.0;
      setValue(upperLegKey, Math.round(upperLegToBodyAngleInDeg));
    }

    const leftLowerLegTracker = findTracker(BodyPart.LEFT_LOWER_LEG);
    const rightLowerLegTracker = findTracker(BodyPart.RIGHT_LOWER_LEG);
    if (leftLowerLegTracker && rightLowerLegTracker) {
      const footToBodyAngleInDeg =
        yawBetweenInDeg(leftLowerLegTracker, rightLowerLegTracker) / 2.0;
      setValue(lowerLegKey, Math.round(footToBodyAngleInDeg));
    }

    if (footKey) {
      const leftFootTracker = findTracker(BodyPart.LEFT_FOOT);
      const rightFootTracker = findTracker(BodyPart.RIGHT_FOOT);
      if (leftFootTracker && rightFootTracker) {
        const footToBodyAngleInDeg =
          yawBetweenInDeg(leftFootTracker, rightFootTracker) / 2.0;
        setValue(footKey, Math.round(footToBodyAngleInDeg));
      }
    }
  };

  const resetAngles = (
    upperLegKey: FieldPath<SettingsForm>,
    lowerLegKey: FieldPath<SettingsForm>,
    footKey?: FieldPath<SettingsForm>
  ) => {
    setValue(upperLegKey, 0.0);
    setValue(lowerLegKey, 0.0);
    if (footKey) {
      setValue(footKey, 0.0);
    }
  };

  return (
    <SettingsPagePaneLayout icon={<WrenchIcon />} id="stayaligned">
      <Typography variant="main-title">
        {l10n.getString('settings-general-stay_aligned')}
      </Typography>
      <div className="flex flex-col pt-2 pb-4">
        {l10n
          .getString('settings-general-stay_aligned-description')
          .split('\n')
          .map((line, i) => (
            <Typography color="secondary" key={i}>
              {line}
            </Typography>
          ))}
        {values.yawCorrectionSettings.enabled && (
          <>
            {!!values.driftCompensation.enabled && (
              <div className="pt-2">
                {l10n.getString(
                  'settings-general-stay_aligned-warnings-drift_compensation'
                )}
              </div>
            )}
          </>
        )}
      </div>
      <div className="grid sm:grid-cols-1 gap-3 pb-4">
        <CheckBox
          variant="toggle"
          outlined
          control={control}
          name="yawCorrectionSettings.enabled"
          label={l10n.getString('settings-general-stay_aligned-enabled-label')}
        />
        <div className="flex flex-col pt-2 pb-4">
          <Typography bold>
            {l10n.getString('settings-general-stay_aligned-amount-label')}
          </Typography>
          <Typography color="secondary">
            {l10n.getString('settings-general-stay_aligned-amount-description')}
          </Typography>
          <NumberSelector
            control={control}
            name="yawCorrectionSettings.amountInDegPerSec"
            valueLabelFormat={(value) => degreePerSecFormat.format(value)}
            min={0.02}
            max={2.0}
            step={0.02}
          />
        </div>
        <div className="flex flex-col pt-2">
          <Typography bold>
            {l10n.getString(
              'settings-general-stay_aligned-relaxed_body_angles-label'
            )}
          </Typography>
          <Typography color="secondary">
            {l10n.getString(
              'settings-general-stay_aligned-relaxed_body_angles-description'
            )}
          </Typography>
        </div>
        <div>
          <Typography color="secondary">
            {l10n.getString(
              'settings-general-stay_aligned-relaxed_body_angles-standing-label'
            )}
          </Typography>
          <div className="grid sm:grid-cols-5 gap-3 pb-3">
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.standingUpperLegAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-upper_leg_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.standingLowerLegAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-lower_leg_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.standingFootAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-foot_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <Button
              variant="primary"
              onClick={() =>
                detectAngles(
                  'yawCorrectionSettings.standingUpperLegAngle',
                  'yawCorrectionSettings.standingLowerLegAngle',
                  'yawCorrectionSettings.standingFootAngle'
                )
              }
            >
              {l10n.getString(
                'settings-general-stay_aligned-relaxed_body_angles-auto_detect'
              )}
            </Button>
            <Button
              variant="primary"
              onClick={() =>
                resetAngles(
                  'yawCorrectionSettings.standingUpperLegAngle',
                  'yawCorrectionSettings.standingLowerLegAngle',
                  'yawCorrectionSettings.standingFootAngle'
                )
              }
            >
              {l10n.getString(
                'settings-general-stay_aligned-relaxed_body_angles-reset'
              )}
            </Button>
          </div>
        </div>
        <div>
          <Typography color="secondary">
            {l10n.getString(
              'settings-general-stay_aligned-relaxed_body_angles-sitting-label'
            )}
          </Typography>
          <div className="grid sm:grid-cols-5 gap-3 pb-3">
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.sittingUpperLegAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-upper_leg_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.sittingLowerLegAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-lower_leg_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.sittingFootAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-foot_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <Button
              variant="primary"
              onClick={() =>
                detectAngles(
                  'yawCorrectionSettings.sittingUpperLegAngle',
                  'yawCorrectionSettings.sittingLowerLegAngle',
                  'yawCorrectionSettings.sittingFootAngle'
                )
              }
            >
              {l10n.getString(
                'settings-general-stay_aligned-relaxed_body_angles-auto_detect'
              )}
            </Button>
            <Button
              variant="primary"
              onClick={() =>
                resetAngles(
                  'yawCorrectionSettings.sittingUpperLegAngle',
                  'yawCorrectionSettings.sittingLowerLegAngle',
                  'yawCorrectionSettings.sittingFootAngle'
                )
              }
            >
              {l10n.getString(
                'settings-general-stay_aligned-relaxed_body_angles-reset'
              )}
            </Button>
          </div>
        </div>
        <div>
          <Typography color="secondary">
            {l10n.getString(
              'settings-general-stay_aligned-relaxed_body_angles-lying_on_back-label'
            )}
          </Typography>
          <div className="grid sm:grid-cols-5 gap-3 pb-3">
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.lyingOnBackUpperLegAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-upper_leg_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <NumberSelector
              control={control}
              name="yawCorrectionSettings.lyingOnBackLowerLegAngle"
              valueLabelFormat={(value) =>
                `${l10n.getString(
                  'settings-general-stay_aligned-relaxed_body_angles-lower_leg_angle'
                )}: ${degreeFormat.format(value)}`
              }
              min={-90.0}
              max={90.0}
              step={1.0}
            />
            <div></div>
            <Button
              variant="primary"
              onClick={() =>
                detectAngles(
                  'yawCorrectionSettings.lyingOnBackUpperLegAngle',
                  'yawCorrectionSettings.lyingOnBackLowerLegAngle'
                )
              }
            >
              {l10n.getString(
                'settings-general-stay_aligned-relaxed_body_angles-auto_detect'
              )}
            </Button>
            <Button
              variant="primary"
              onClick={() =>
                resetAngles(
                  'yawCorrectionSettings.lyingOnBackUpperLegAngle',
                  'yawCorrectionSettings.lyingOnBackLowerLegAngle'
                )
              }
            >
              {l10n.getString(
                'settings-general-stay_aligned-relaxed_body_angles-reset'
              )}
            </Button>
          </div>
        </div>
      </div>
    </SettingsPagePaneLayout>
  );
}
