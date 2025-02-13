import { Localized, useLocalization } from '@fluent/react';
import { useEffect, useState } from 'react';
import { DefaultValues, useForm } from 'react-hook-form';
import {
  ChangeSettingsRequestT,
  DriftCompensationSettingsT,
  FilteringSettingsT,
  FilteringType,
  LegTweaksSettingsT,
  ModelRatiosT,
  ModelSettingsT,
  ModelTogglesT,
  ResetsSettingsT,
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
  SteamVRTrackersSettingT,
  TapDetectionSettingsT,
  YawCorrectionSettingsT,
} from 'solarxr-protocol';
import { useConfig } from '@/hooks/config';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { useLocaleConfig } from '@/i18n/config';
import { CheckBox } from '@/components/commons/Checkbox';
import { SteamIcon } from '@/components/commons/icon/SteamIcon';
import { WrenchIcon } from '@/components/commons/icon/WrenchIcons';
import { NumberSelector } from '@/components/commons/NumberSelector';
import { Radio } from '@/components/commons/Radio';
import { Typography } from '@/components/commons/Typography';
import {
  SettingsPageLayout,
  SettingsPagePaneLayout,
} from '@/components/settings/SettingsPageLayout';
import { HandsWarningModal } from '@/components/settings/HandsWarningModal';
import { MagnetometerToggleSetting } from './MagnetometerToggleSetting';
import { DriftCompensationModal } from '@/components/settings/DriftCompensationModal';
import { StayAlignedSettings } from './components/StayAlignedSettings';

export interface SettingsForm {
  trackers: {
    waist: boolean;
    chest: boolean;
    automaticTrackerToggle: boolean;
    leftFoot: boolean;
    rightFoot: boolean;
    leftKnee: boolean;
    rightKnee: boolean;
    leftElbow: boolean;
    rightElbow: boolean;
    leftHand: boolean;
    rightHand: boolean;
  };
  filtering: {
    type: number;
    amount: number;
  };
  driftCompensation: {
    enabled: boolean;
    prediction: boolean;
    amount: number;
    maxResets: number;
  };
  toggles: {
    extendedSpine: boolean;
    extendedPelvis: boolean;
    extendedKnee: boolean;
    forceArmsFromHmd: boolean;
    floorClip: boolean;
    skatingCorrection: boolean;
    viveEmulation: boolean;
    toeSnap: boolean;
    footPlant: boolean;
    selfLocalization: boolean;
    usePosition: boolean;
    enforceConstraints: boolean;
    correctConstraints: boolean;
  };
  ratios: {
    imputeWaistFromChestHip: number;
    imputeWaistFromChestLegs: number;
    imputeHipFromChestLegs: number;
    imputeHipFromWaistLegs: number;
    interpHipLegs: number;
    interpKneeTrackerAnkle: number;
    interpKneeAnkle: number;
  };
  tapDetection: {
    mountingResetEnabled: boolean;
    yawResetEnabled: boolean;
    fullResetEnabled: boolean;
    yawResetDelay: number;
    fullResetDelay: number;
    mountingResetDelay: number;
    yawResetTaps: number;
    fullResetTaps: number;
    mountingResetTaps: number;
    numberTrackersOverThreshold: number;
  };
  legTweaks: {
    correctionStrength: number;
  };
  resetsSettings: {
    resetMountingFeet: boolean;
    armsMountingResetMode: number;
    yawResetSmoothTime: number;
    saveMountingReset: boolean;
    resetHmdPitch: boolean;
  };
  yawCorrectionSettings: {
    enabled: boolean;
    amountInDegPerSec: number;
    standingUpperLegAngle: number;
    standingLowerLegAngle: number;
    standingFootAngle: number;
    sittingUpperLegAngle: number;
    sittingLowerLegAngle: number;
    sittingFootAngle: number;
    lyingOnBackUpperLegAngle: number;
    lyingOnBackLowerLegAngle: number;
  };
}

const defaultValues: SettingsForm = {
  trackers: {
    waist: false,
    chest: false,
    automaticTrackerToggle: true,
    leftFoot: false,
    rightFoot: false,
    leftElbow: false,
    rightElbow: false,
    leftHand: false,
    rightHand: false,
    leftKnee: false,
    rightKnee: false,
  },
  toggles: {
    extendedSpine: true,
    extendedPelvis: true,
    extendedKnee: true,
    forceArmsFromHmd: false,
    floorClip: false,
    skatingCorrection: false,
    viveEmulation: false,
    toeSnap: false,
    footPlant: true,
    selfLocalization: false,
    usePosition: true,
    enforceConstraints: true,
    correctConstraints: true,
  },
  ratios: {
    imputeWaistFromChestHip: 0.3,
    imputeWaistFromChestLegs: 0.2,
    imputeHipFromChestLegs: 0.45,
    imputeHipFromWaistLegs: 0.4,
    interpHipLegs: 0.25,
    interpKneeTrackerAnkle: 0.85,
    interpKneeAnkle: 0.2,
  },
  filtering: { amount: 0.1, type: FilteringType.NONE },
  driftCompensation: {
    enabled: false,
    prediction: false,
    amount: 0.1,
    maxResets: 1,
  },
  tapDetection: {
    mountingResetEnabled: false,
    yawResetEnabled: false,
    fullResetEnabled: false,
    yawResetDelay: 0.2,
    fullResetDelay: 1.0,
    mountingResetDelay: 1.0,
    yawResetTaps: 2,
    fullResetTaps: 3,
    mountingResetTaps: 3,
    numberTrackersOverThreshold: 1,
  },
  legTweaks: { correctionStrength: 0.3 },
  resetsSettings: {
    resetMountingFeet: false,
    armsMountingResetMode: 0,
    yawResetSmoothTime: 0.0,
    saveMountingReset: false,
    resetHmdPitch: false,
  },
  yawCorrectionSettings: {
    enabled: true,
    amountInDegPerSec: 0.2,
    standingUpperLegAngle: 0.0,
    standingLowerLegAngle: 0.0,
    standingFootAngle: 0.0,
    sittingUpperLegAngle: 0.0,
    sittingLowerLegAngle: 0.0,
    sittingFootAngle: 0.0,
    lyingOnBackUpperLegAngle: 0.0,
    lyingOnBackLowerLegAngle: 0.0,
  },
};

export function GeneralSettings() {
  const { l10n } = useLocalization();
  const { config } = useConfig();
  // const { state } = useLocation();
  const { currentLocales } = useLocaleConfig();
  // const pageRef = useRef<HTMLFormElement | null>(null);

  const percentageFormat = new Intl.NumberFormat(currentLocales, {
    style: 'percent',
    maximumFractionDigits: 0,
  });
  const secondsFormat = new Intl.NumberFormat(currentLocales, {
    style: 'unit',
    unit: 'second',
    unitDisplay: 'narrow',
    maximumFractionDigits: 2,
  });

  const { sendRPCPacket, useRPCPacket } = useWebsocketAPI();
  const { reset, control, watch, handleSubmit, getValues, setValue } =
    useForm<SettingsForm>({
      defaultValues,
    });
  const {
    trackers: {
      automaticTrackerToggle,
      leftHand: steamVrLeftHand,
      rightHand: steamVrRightHand,
    },
  } = watch();

  const onSubmit = (values: SettingsForm) => {
    const settings = new ChangeSettingsRequestT();

    if (values.trackers) {
      const trackers = new SteamVRTrackersSettingT();
      trackers.waist = values.trackers.waist;
      trackers.chest = values.trackers.chest;
      trackers.leftFoot = values.trackers.leftFoot;
      trackers.rightFoot = values.trackers.rightFoot;

      trackers.leftKnee = values.trackers.leftKnee;
      trackers.rightKnee = values.trackers.rightKnee;

      trackers.leftElbow = values.trackers.leftElbow;
      trackers.rightElbow = values.trackers.rightElbow;

      trackers.leftHand = values.trackers.leftHand;
      trackers.rightHand = values.trackers.rightHand;

      trackers.automaticTrackerToggle = values.trackers.automaticTrackerToggle;
      settings.steamVrTrackers = trackers;
    }

    const modelSettings = new ModelSettingsT();

    if (values.toggles) {
      const toggles = new ModelTogglesT();
      toggles.floorClip = values.toggles.floorClip;
      toggles.skatingCorrection = values.toggles.skatingCorrection;
      toggles.extendedKnee = values.toggles.extendedKnee;
      toggles.extendedPelvis = values.toggles.extendedPelvis;
      toggles.extendedSpine = values.toggles.extendedSpine;
      toggles.forceArmsFromHmd = values.toggles.forceArmsFromHmd;
      toggles.viveEmulation = values.toggles.viveEmulation;
      toggles.toeSnap = values.toggles.toeSnap;
      toggles.footPlant = values.toggles.footPlant;
      toggles.selfLocalization = values.toggles.selfLocalization;
      toggles.usePosition = values.toggles.usePosition;
      toggles.enforceConstraints = values.toggles.enforceConstraints;
      toggles.correctConstraints = values.toggles.correctConstraints;
      modelSettings.toggles = toggles;
    }

    if (values.ratios) {
      const ratios = new ModelRatiosT();
      ratios.imputeWaistFromChestHip =
        values.ratios.imputeWaistFromChestHip || -1;
      ratios.imputeWaistFromChestLegs =
        values.ratios.imputeWaistFromChestLegs || -1;
      ratios.imputeHipFromChestLegs =
        values.ratios.imputeHipFromChestLegs || -1;
      ratios.imputeHipFromWaistLegs =
        values.ratios.imputeHipFromWaistLegs || -1;
      ratios.interpHipLegs = values.ratios.interpHipLegs || -1;
      ratios.interpKneeTrackerAnkle =
        values.ratios.interpKneeTrackerAnkle || -1;
      ratios.interpKneeAnkle = values.ratios.interpKneeAnkle || -1;
      modelSettings.ratios = ratios;
    }

    if (values.legTweaks) {
      const legTweaks = new LegTweaksSettingsT();
      legTweaks.correctionStrength = values.legTweaks.correctionStrength;
      modelSettings.legTweaks = legTweaks;
    }

    settings.modelSettings = modelSettings;

    const tapDetection = new TapDetectionSettingsT();
    tapDetection.fullResetDelay = values.tapDetection.fullResetDelay;
    tapDetection.fullResetEnabled = values.tapDetection.fullResetEnabled;
    tapDetection.fullResetTaps = values.tapDetection.fullResetTaps;
    tapDetection.yawResetDelay = values.tapDetection.yawResetDelay;
    tapDetection.yawResetEnabled = values.tapDetection.yawResetEnabled;
    tapDetection.yawResetTaps = values.tapDetection.yawResetTaps;
    tapDetection.mountingResetEnabled =
      values.tapDetection.mountingResetEnabled;
    tapDetection.mountingResetDelay = values.tapDetection.mountingResetDelay;
    tapDetection.mountingResetTaps = values.tapDetection.mountingResetTaps;
    tapDetection.numberTrackersOverThreshold =
      values.tapDetection.numberTrackersOverThreshold;
    tapDetection.setupMode = false;
    settings.tapDetectionSettings = tapDetection;

    const filtering = new FilteringSettingsT();
    filtering.type = values.filtering.type;
    filtering.amount = values.filtering.amount;
    settings.filtering = filtering;

    const driftCompensation = new DriftCompensationSettingsT();
    driftCompensation.enabled = values.driftCompensation.enabled;
    driftCompensation.prediction = values.driftCompensation.prediction;
    driftCompensation.amount = values.driftCompensation.amount;
    driftCompensation.maxResets = values.driftCompensation.maxResets;
    settings.driftCompensation = driftCompensation;

    const yawCorrectionSettings = new YawCorrectionSettingsT();
    yawCorrectionSettings.enabled = values.yawCorrectionSettings.enabled;
    yawCorrectionSettings.amountInDegPerSec =
      values.yawCorrectionSettings.amountInDegPerSec;
    yawCorrectionSettings.standingUpperLegAngle =
      values.yawCorrectionSettings.standingUpperLegAngle;
    yawCorrectionSettings.standingLowerLegAngle =
      values.yawCorrectionSettings.standingLowerLegAngle;
    yawCorrectionSettings.standingFootAngle =
      values.yawCorrectionSettings.standingFootAngle;
    yawCorrectionSettings.sittingUpperLegAngle =
      values.yawCorrectionSettings.sittingUpperLegAngle;
    yawCorrectionSettings.sittingLowerLegAngle =
      values.yawCorrectionSettings.sittingLowerLegAngle;
    yawCorrectionSettings.sittingFootAngle =
      values.yawCorrectionSettings.sittingFootAngle;
    yawCorrectionSettings.lyingOnBackUpperLegAngle =
      values.yawCorrectionSettings.lyingOnBackUpperLegAngle;
    yawCorrectionSettings.lyingOnBackLowerLegAngle =
      values.yawCorrectionSettings.lyingOnBackLowerLegAngle;
    settings.yawCorrectionSettings = yawCorrectionSettings;

    if (values.resetsSettings) {
      const resetsSettings = new ResetsSettingsT();
      resetsSettings.resetMountingFeet =
        values.resetsSettings.resetMountingFeet;
      resetsSettings.armsMountingResetMode =
        values.resetsSettings.armsMountingResetMode;
      resetsSettings.yawResetSmoothTime =
        values.resetsSettings.yawResetSmoothTime;
      resetsSettings.saveMountingReset =
        values.resetsSettings.saveMountingReset;
      resetsSettings.resetHmdPitch = values.resetsSettings.resetHmdPitch;
      settings.resetsSettings = resetsSettings;
    }

    sendRPCPacket(RpcMessage.ChangeSettingsRequest, settings);
  };

  useEffect(() => {
    const subscription = watch(() => handleSubmit(onSubmit)());
    return () => subscription.unsubscribe();
  }, []);

  useEffect(() => {
    sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT());
  }, []);

  // If null, we still haven't shown the hands warning
  // if false then initially the hands warning was disabled
  const [handsWarning, setHandsWarning] = useState<boolean | null>(null);
  useRPCPacket(RpcMessage.SettingsResponse, (settings: SettingsResponseT) => {
    const formData: DefaultValues<SettingsForm> = {};

    if (settings.filtering) {
      formData.filtering = settings.filtering;
    }

    if (settings.driftCompensation) {
      formData.driftCompensation = settings.driftCompensation;
    }

    if (settings.steamVrTrackers) {
      formData.trackers = settings.steamVrTrackers;
      if (
        settings.steamVrTrackers.leftHand ||
        settings.steamVrTrackers.rightHand
      ) {
        setHandsWarning(false);
      }
    }

    if (settings.modelSettings?.toggles) {
      formData.toggles = Object.keys(settings.modelSettings.toggles).reduce(
        (curr, key: string) => ({
          ...curr,
          [key]:
            (settings.modelSettings?.toggles &&
              (settings.modelSettings.toggles as any)[key]) ||
            false,
        }),
        {}
      );
    }

    if (settings.modelSettings?.ratios) {
      formData.ratios = Object.keys(settings.modelSettings.ratios).reduce(
        (curr, key: string) => ({
          ...curr,
          [key]:
            (settings.modelSettings?.ratios &&
              (settings.modelSettings.ratios as any)[key]) ||
            0.0,
        }),
        {}
      );
    }

    if (settings.tapDetectionSettings) {
      formData.tapDetection = {
        yawResetEnabled:
          settings.tapDetectionSettings.yawResetEnabled ||
          defaultValues.tapDetection.yawResetEnabled,
        fullResetEnabled:
          settings.tapDetectionSettings.fullResetEnabled ||
          defaultValues.tapDetection.fullResetEnabled,
        mountingResetEnabled:
          settings.tapDetectionSettings.mountingResetEnabled ||
          defaultValues.tapDetection.mountingResetEnabled,
        yawResetDelay:
          settings.tapDetectionSettings.yawResetDelay ||
          defaultValues.tapDetection.yawResetDelay,
        fullResetDelay:
          settings.tapDetectionSettings.fullResetDelay ||
          defaultValues.tapDetection.fullResetDelay,
        mountingResetDelay:
          settings.tapDetectionSettings.mountingResetDelay ||
          defaultValues.tapDetection.mountingResetDelay,
        yawResetTaps:
          settings.tapDetectionSettings.yawResetTaps ||
          defaultValues.tapDetection.yawResetTaps,
        fullResetTaps:
          settings.tapDetectionSettings.fullResetTaps ||
          defaultValues.tapDetection.fullResetTaps,
        mountingResetTaps:
          settings.tapDetectionSettings.mountingResetTaps ||
          defaultValues.tapDetection.mountingResetTaps,
        numberTrackersOverThreshold:
          settings.tapDetectionSettings.numberTrackersOverThreshold ||
          defaultValues.tapDetection.numberTrackersOverThreshold,
      };
    }

    if (settings.modelSettings?.legTweaks) {
      formData.legTweaks = {
        correctionStrength:
          settings.modelSettings.legTweaks.correctionStrength ||
          defaultValues.legTweaks.correctionStrength,
      };
    }

    if (settings.resetsSettings) {
      formData.resetsSettings = settings.resetsSettings;
    }

    if (settings.yawCorrectionSettings) {
      formData.yawCorrectionSettings = settings.yawCorrectionSettings;
    }

    reset({ ...getValues(), ...formData });
  });

  useEffect(() => {
    if ((steamVrLeftHand || steamVrRightHand) && handsWarning === null) {
      setHandsWarning(true);
    } else if (
      !(steamVrLeftHand || steamVrRightHand) &&
      handsWarning === false
    ) {
      setHandsWarning(null);
    }
  }, [steamVrLeftHand, steamVrRightHand, handsWarning]);

  // Handle scrolling to selected page
  // useEffect(() => {
  //   const typedState: { scrollTo: string } = state as any;
  //   if (!pageRef.current || !typedState || !typedState.scrollTo) {
  //     return;
  //   }
  //   const elem = pageRef.current.querySelector(`#${typedState.scrollTo}`);
  //   if (elem) {
  //     elem.scrollIntoView({ behavior: 'smooth' });
  //   }
  // }, [state]);

  const [showDriftCompWarning, setShowDriftCompWarning] = useState(false);

  return (
    <SettingsPageLayout>
      <HandsWarningModal
        isOpen={!!handsWarning}
        onClose={() => {
          setValue('trackers.leftHand', false);
          setValue('trackers.rightHand', false);
          setHandsWarning(null);
        }}
        accept={() => {
          setValue('trackers.leftHand', true);
          setValue('trackers.rightHand', true);
          setHandsWarning(false);
        }}
      />
      <form className="flex flex-col gap-2 w-full">
        <SettingsPagePaneLayout icon={<SteamIcon></SteamIcon>} id="steamvr">
          <>
            <Typography variant="main-title">
              {l10n.getString('settings-general-steamvr')}
            </Typography>
            <Typography bold>
              {l10n.getString('settings-general-steamvr-subtitle')}
            </Typography>
            <div className="flex flex-col py-2">
              {l10n
                .getString('settings-general-steamvr-description')
                .split('\n')
                .map((line, i) => (
                  <Typography color="secondary" key={i}>
                    {line}
                  </Typography>
                ))}
            </div>
            <div className="flex flex-col pt-4"></div>
            <Typography bold>
              {l10n.getString(
                'settings-general-steamvr-trackers-tracker_toggling'
              )}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              {l10n
                .getString(
                  'settings-general-steamvr-trackers-tracker_toggling-description'
                )
                .split('\n')
                .map((line, i) => (
                  <Typography color="secondary" key={i}>
                    {line}
                  </Typography>
                ))}
            </div>
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="trackers.automaticTrackerToggle"
              label={l10n.getString(
                'settings-general-steamvr-trackers-tracker_toggling-label'
              )}
            />
            <div className="flex flex-col pt-4"></div>
            <div className="grid grid-cols-2 gap-3">
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.chest"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-chest'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.waist"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-waist'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.leftKnee"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-left_knee'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.rightKnee"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-right_knee'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.leftFoot"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-left_foot'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.rightFoot"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-right_foot'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.leftElbow"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-left_elbow'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                disabled={automaticTrackerToggle}
                control={control}
                name="trackers.rightElbow"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-right_elbow'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="trackers.leftHand"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-left_hand'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="trackers.rightHand"
                label={l10n.getString(
                  'settings-general-steamvr-trackers-right_hand'
                )}
              />
            </div>
          </>
        </SettingsPagePaneLayout>
        <SettingsPagePaneLayout icon={<WrenchIcon></WrenchIcon>} id="mechanics">
          <>
            <Typography variant="main-title">
              {l10n.getString('settings-general-tracker_mechanics')}
            </Typography>
            <Typography bold>
              {l10n.getString('settings-general-tracker_mechanics-filtering')}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              {l10n
                .getString(
                  'settings-general-tracker_mechanics-filtering-description'
                )
                .split('\n')
                .map((line, i) => (
                  <Typography color="secondary" key={i}>
                    {line}
                  </Typography>
                ))}
            </div>
            <Typography>
              {l10n.getString(
                'settings-general-tracker_mechanics-filtering-type'
              )}
            </Typography>
            <div className="flex md:flex-row flex-col gap-3 pt-2">
              <Radio
                control={control}
                name="filtering.type"
                label={l10n.getString(
                  'settings-general-tracker_mechanics-filtering-type-none'
                )}
                description={l10n.getString(
                  'settings-general-tracker_mechanics-filtering-type-none-description'
                )}
                value={FilteringType.NONE.toString()}
              ></Radio>
              <Radio
                control={control}
                name="filtering.type"
                label={l10n.getString(
                  'settings-general-tracker_mechanics-filtering-type-smoothing'
                )}
                description={l10n.getString(
                  'settings-general-tracker_mechanics-filtering-type-smoothing-description'
                )}
                value={FilteringType.SMOOTHING.toString()}
              ></Radio>
              <Radio
                control={control}
                name="filtering.type"
                label={l10n.getString(
                  'settings-general-tracker_mechanics-filtering-type-prediction'
                )}
                description={l10n.getString(
                  'settings-general-tracker_mechanics-filtering-type-prediction-description'
                )}
                value={FilteringType.PREDICTION.toString()}
              ></Radio>
            </div>
            <div className="flex gap-5 pt-5 md:flex-row flex-col">
              <NumberSelector
                control={control}
                name="filtering.amount"
                label={l10n.getString(
                  'settings-general-tracker_mechanics-filtering-amount'
                )}
                valueLabelFormat={(value) => percentageFormat.format(value)}
                min={0.1}
                max={1.0}
                step={0.1}
              />
            </div>
            <div className="flex flex-col pt-4 pb-4"></div>
            <Typography bold>
              {l10n.getString(
                'settings-general-tracker_mechanics-drift_compensation'
              )}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              {l10n
                .getString(
                  'settings-general-tracker_mechanics-drift_compensation-description'
                )
                .split('\n')
                .map((line, i) => (
                  <Typography color="secondary" key={i}>
                    {line}
                  </Typography>
                ))}
            </div>
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="driftCompensation.enabled"
              label={l10n.getString(
                'settings-general-tracker_mechanics-drift_compensation-enabled-label'
              )}
              onClick={() => {
                if (getValues('driftCompensation.enabled')) {
                  return;
                }

                setShowDriftCompWarning(true);
              }}
            />
            <div className="flex flex-col pt-2 pb-4"></div>
            <Typography bold>
              {l10n.getString(
                'settings-general-tracker_mechanics-drift_compensation-prediction'
              )}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              {l10n
                .getString(
                  'settings-general-tracker_mechanics-drift_compensation-prediction-description'
                )
                .split('\n')
                .map((line, i) => (
                  <Typography color="secondary" key={i}>
                    {line}
                  </Typography>
                ))}
            </div>
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="driftCompensation.prediction"
              label={l10n.getString(
                'settings-general-tracker_mechanics-drift_compensation-prediction-label'
              )}
            />
            <DriftCompensationModal
              accept={() => {
                setShowDriftCompWarning(false);
              }}
              onClose={() => {
                setShowDriftCompWarning(false);
                setValue('driftCompensation.enabled', false);
              }}
              isOpen={showDriftCompWarning}
            ></DriftCompensationModal>
            <div className="flex gap-5 pt-5 md:flex-row flex-col">
              <NumberSelector
                control={control}
                name="driftCompensation.amount"
                label={l10n.getString(
                  'settings-general-tracker_mechanics-drift_compensation-amount-label'
                )}
                valueLabelFormat={(value) => percentageFormat.format(value)}
                min={0.1}
                max={1.0}
                step={0.1}
              />
            </div>
            <div className="flex gap-5 pt-5 md:flex-row flex-col">
              <NumberSelector
                control={control}
                name="driftCompensation.maxResets"
                label={l10n.getString(
                  'settings-general-tracker_mechanics-drift_compensation-max_resets-label'
                )}
                min={1}
                max={25}
                step={1}
              />
            </div>
            <div className="flex gap-5 pt-5 md:flex-row flex-col">
              <NumberSelector
                control={control}
                name="resetsSettings.yawResetSmoothTime"
                label={l10n.getString(
                  'settings-general-tracker_mechanics-yaw-reset-smooth-time'
                )}
                valueLabelFormat={(value) => secondsFormat.format(value)}
                min={0.0}
                max={0.5}
                step={0.05}
              />
            </div>
            <div className="flex flex-col pt-5 pb-3">
              <Typography bold>
                {l10n.getString(
                  'settings-general-tracker_mechanics-save_mounting_reset'
                )}
              </Typography>
              <Localized
                id="settings-general-tracker_mechanics-save_mounting_reset-description"
                elems={{ b: <b></b> }}
              >
                <Typography color="secondary"></Typography>
              </Localized>
            </div>
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="resetsSettings.saveMountingReset"
              label={l10n.getString(
                'settings-general-tracker_mechanics-save_mounting_reset-enabled-label'
              )}
            />
            <MagnetometerToggleSetting
              settingType="general"
              id="mechanics-magnetometer"
            />
          </>
        </SettingsPagePaneLayout>
        <StayAlignedSettings
          getValues={getValues}
          setValue={setValue}
          control={control}
        />
        <SettingsPagePaneLayout
          icon={<WrenchIcon></WrenchIcon>}
          id="fksettings"
        >
          <>
            <Typography variant="main-title">
              {l10n.getString('settings-general-fk_settings')}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              <Typography bold>
                {l10n.getString(
                  'settings-general-fk_settings-leg_tweak-skating_correction'
                )}
              </Typography>
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-leg_tweak-skating_correction-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 gap-3 pb-4">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="toggles.skatingCorrection"
                label={l10n.getString(
                  'settings-general-fk_settings-leg_tweak-skating_correction'
                )}
              />
              <NumberSelector
                control={control}
                name="legTweaks.correctionStrength"
                label={l10n.getString(
                  'settings-general-fk_settings-leg_tweak-skating_correction-amount'
                )}
                valueLabelFormat={(value) => percentageFormat.format(value)}
                min={0.1}
                max={1.0}
                step={0.1}
              />
            </div>

            <div className="flex flex-col pt-2 pb-2">
              <Typography bold>
                {l10n.getString('settings-general-fk_settings-leg_fk')}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 gap-3 pb-3">
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-leg_tweak-floor_clip-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 gap-3 pb-3">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="toggles.floorClip"
                label={l10n.getString(
                  'settings-general-fk_settings-leg_tweak-floor_clip'
                )}
              />
            </div>
            <div className="flex flex-col pt-2 pb-3">
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-leg_tweak-foot_plant-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 gap-3 pb-3">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="toggles.footPlant"
                label={l10n.getString(
                  'settings-general-fk_settings-leg_tweak-foot_plant'
                )}
              />
            </div>
            <div className="flex flex-col pt-2 pb-3">
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-leg_tweak-toe_snap-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 gap-3 pb-3">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="toggles.toeSnap"
                label={l10n.getString(
                  'settings-general-fk_settings-leg_tweak-toe_snap'
                )}
              />
            </div>

            <div className="flex flex-col pt-2 pb-3">
              <Typography bold>
                {l10n.getString('settings-general-fk_settings-arm_fk')}
              </Typography>
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-arm_fk-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 pb-3">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="toggles.forceArmsFromHmd"
                label={l10n.getString(
                  'settings-general-fk_settings-arm_fk-force_arms'
                )}
              />
            </div>

            <div className="flex flex-col pt-2">
              <Typography bold>
                {l10n.getString('settings-general-fk_settings-reset_settings')}
              </Typography>
            </div>
            <div className="flex flex-col pt-2 pb-3">
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-reset_settings-reset_hmd_pitch-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 gap-3 pb-3">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="resetsSettings.resetHmdPitch"
                label={l10n.getString(
                  'settings-general-fk_settings-reset_settings-reset_hmd_pitch'
                )}
              />
            </div>
            <div className="flex flex-col pt-2 pb-3">
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-leg_fk-reset_mounting_feet-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 gap-3 pb-3">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="resetsSettings.resetMountingFeet"
                label={l10n.getString(
                  'settings-general-fk_settings-leg_fk-reset_mounting_feet'
                )}
              />
            </div>

            <Typography color="secondary">
              {l10n.getString(
                'settings-general-fk_settings-arm_fk-reset_mode-description'
              )}
            </Typography>
            <div className="grid md:grid-cols-2 flex-col gap-3 pt-2 pb-3">
              <Radio
                control={control}
                name="resetsSettings.armsMountingResetMode"
                label={l10n.getString(
                  'settings-general-fk_settings-arm_fk-back'
                )}
                description={l10n.getString(
                  'settings-general-fk_settings-arm_fk-back-description'
                )}
                value={'0'}
              ></Radio>
              <Radio
                control={control}
                name="resetsSettings.armsMountingResetMode"
                label={l10n.getString(
                  'settings-general-fk_settings-arm_fk-forward'
                )}
                description={l10n.getString(
                  'settings-general-fk_settings-arm_fk-forward-description'
                )}
                value={'1'}
              ></Radio>
              <Radio
                control={control}
                name="resetsSettings.armsMountingResetMode"
                label={l10n.getString(
                  'settings-general-fk_settings-arm_fk-tpose_up'
                )}
                description={l10n.getString(
                  'settings-general-fk_settings-arm_fk-tpose_up-description'
                )}
                value={'2'}
              ></Radio>
              <Radio
                control={control}
                name="resetsSettings.armsMountingResetMode"
                label={l10n.getString(
                  'settings-general-fk_settings-arm_fk-tpose_down'
                )}
                description={l10n.getString(
                  'settings-general-fk_settings-arm_fk-tpose_down-description'
                )}
                value={'3'}
              ></Radio>
            </div>

            <div className="flex flex-col pt-2 pb-3">
              <Typography bold>
                {l10n.getString(
                  'settings-general-fk_settings-enforce_joint_constraints'
                )}
              </Typography>
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-fk_settings-enforce_joint_constraints-enforce_constraints-description'
                )}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 pb-3">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="toggles.enforceConstraints"
                label={l10n.getString(
                  'settings-general-fk_settings-enforce_joint_constraints-enforce_constraints'
                )}
              />
            </div>

            {config?.debug && (
              <>
                <div className="flex flex-col pt-2 pb-3">
                  <Typography bold>
                    {l10n.getString(
                      'settings-general-fk_settings-skeleton_settings-toggles'
                    )}
                  </Typography>
                  <Typography color="secondary">
                    {l10n.getString(
                      'settings-general-fk_settings-skeleton_settings-description'
                    )}
                  </Typography>
                </div>
                <div className="grid sm:grid-cols-2 gap-3 pb-3">
                  <CheckBox
                    variant="toggle"
                    outlined
                    control={control}
                    name="toggles.extendedSpine"
                    label={l10n.getString(
                      'settings-general-fk_settings-skeleton_settings-extended_spine_model'
                    )}
                  />
                  <CheckBox
                    variant="toggle"
                    outlined
                    control={control}
                    name="toggles.extendedPelvis"
                    label={l10n.getString(
                      'settings-general-fk_settings-skeleton_settings-extended_pelvis_model'
                    )}
                  />
                  <CheckBox
                    variant="toggle"
                    outlined
                    control={control}
                    name="toggles.extendedKnee"
                    label={l10n.getString(
                      'settings-general-fk_settings-skeleton_settings-extended_knees_model'
                    )}
                  />
                </div>
                <div className="flex flex-col pt-2 pb-3">
                  <div className="flex flex-col pt-2 pb-3">
                    <Typography bold>
                      {l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-ratios'
                      )}
                    </Typography>
                    <Typography color="secondary">
                      {l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-ratios-description'
                      )}
                    </Typography>
                  </div>
                  <div className="grid sm:grid-cols-2 gap-3 pb-3">
                    <NumberSelector
                      control={control}
                      name="ratios.imputeWaistFromChestHip"
                      label={l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_hip'
                      )}
                      valueLabelFormat={(value) =>
                        percentageFormat.format(value)
                      }
                      min={0.0}
                      max={1.0}
                      step={0.05}
                    />
                    <NumberSelector
                      control={control}
                      name="ratios.imputeWaistFromChestLegs"
                      label={l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_legs'
                      )}
                      valueLabelFormat={(value) =>
                        percentageFormat.format(value)
                      }
                      min={0.0}
                      max={1.0}
                      step={0.05}
                    />
                    <NumberSelector
                      control={control}
                      name="ratios.imputeHipFromChestLegs"
                      label={l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-impute_hip_from_chest_legs'
                      )}
                      valueLabelFormat={(value) =>
                        percentageFormat.format(value)
                      }
                      min={0.0}
                      max={1.0}
                      step={0.05}
                    />
                    <NumberSelector
                      control={control}
                      name="ratios.imputeHipFromWaistLegs"
                      label={l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-impute_hip_from_waist_legs'
                      )}
                      valueLabelFormat={(value) =>
                        percentageFormat.format(value)
                      }
                      min={0.0}
                      max={1.0}
                      step={0.05}
                    />
                    <NumberSelector
                      control={control}
                      name="ratios.interpHipLegs"
                      label={l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-interp_hip_legs'
                      )}
                      valueLabelFormat={(value) =>
                        percentageFormat.format(value)
                      }
                      min={0.0}
                      max={1.0}
                      step={0.05}
                    />
                    <NumberSelector
                      control={control}
                      name="ratios.interpKneeTrackerAnkle"
                      label={l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-interp_knee_tracker_ankle'
                      )}
                      valueLabelFormat={(value) =>
                        percentageFormat.format(value)
                      }
                      min={0.0}
                      max={1.0}
                      step={0.05}
                    />
                    <NumberSelector
                      control={control}
                      name="ratios.interpKneeAnkle"
                      label={l10n.getString(
                        'settings-general-fk_settings-skeleton_settings-interp_knee_ankle'
                      )}
                      valueLabelFormat={(value) =>
                        percentageFormat.format(value)
                      }
                      min={0.0}
                      max={1.0}
                      step={0.05}
                    />
                  </div>
                </div>

                <div className="flex flex-col pt-2 pb-3">
                  <Typography bold>
                    {l10n.getString(
                      'settings-general-fk_settings-vive_emulation-title'
                    )}
                  </Typography>
                  <Typography color="secondary">
                    {l10n.getString(
                      'settings-general-fk_settings-vive_emulation-description'
                    )}
                  </Typography>
                </div>
                <div className="grid sm:grid-cols-1 gap-3 pb-5">
                  <CheckBox
                    variant="toggle"
                    outlined
                    control={control}
                    name="toggles.viveEmulation"
                    label={l10n.getString(
                      'settings-general-fk_settings-vive_emulation-label'
                    )}
                  />
                </div>
                <div className="flex flex-col pt-2 pb-3">
                  <Typography bold>
                    {l10n.getString(
                      'settings-general-fk_settings-self_localization-title'
                    )}
                  </Typography>
                  <Typography color="secondary">
                    {l10n.getString(
                      'settings-general-fk_settings-self_localization-description'
                    )}
                  </Typography>
                </div>
                <div className="grid sm:grid-cols-1 gap3 pb5">
                  <CheckBox
                    variant="toggle"
                    outlined
                    control={control}
                    name="toggles.selfLocalization"
                    label={l10n.getString(
                      'settings-general-fk_settings-self_localization-title'
                    )}
                  />
                </div>
              </>
            )}
          </>
        </SettingsPagePaneLayout>

        <SettingsPagePaneLayout
          icon={<WrenchIcon></WrenchIcon>}
          id="gestureControl"
        >
          <>
            <Typography variant="main-title">
              {l10n.getString('settings-general-gesture_control')}
            </Typography>
            <Typography bold>
              {l10n.getString('settings-general-gesture_control-subtitle')}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              <Typography color="secondary">
                {l10n.getString('settings-general-gesture_control-description')}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-3 gap-5 pb-2">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="tapDetection.yawResetEnabled"
                label={l10n.getString(
                  'settings-general-gesture_control-yawResetEnabled'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="tapDetection.fullResetEnabled"
                label={l10n.getString(
                  'settings-general-gesture_control-fullResetEnabled'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="tapDetection.mountingResetEnabled"
                label={l10n.getString(
                  'settings-general-gesture_control-mountingResetEnabled'
                )}
              />
            </div>
            <div className="grid sm:grid-cols-3 gap-5 pb-2">
              <NumberSelector
                control={control}
                name="tapDetection.yawResetDelay"
                label={l10n.getString(
                  'settings-general-gesture_control-yawResetDelay'
                )}
                valueLabelFormat={(value) => secondsFormat.format(value)}
                min={0.2}
                max={3.0}
                step={0.2}
              />
              <NumberSelector
                control={control}
                name="tapDetection.fullResetDelay"
                label={l10n.getString(
                  'settings-general-gesture_control-fullResetDelay'
                )}
                valueLabelFormat={(value) => secondsFormat.format(value)}
                min={0.2}
                max={3.0}
                step={0.2}
              />
              <NumberSelector
                control={control}
                name="tapDetection.mountingResetDelay"
                label={l10n.getString(
                  'settings-general-gesture_control-mountingResetDelay'
                )}
                valueLabelFormat={(value) => secondsFormat.format(value)}
                min={0.2}
                max={3.0}
                step={0.2}
              />
            </div>
            <div className="grid sm:grid-cols-3 gap-5 pb-2">
              <NumberSelector
                control={control}
                name="tapDetection.yawResetTaps"
                label={l10n.getString(
                  'settings-general-gesture_control-yawResetTaps'
                )}
                valueLabelFormat={(value) =>
                  l10n.getString('settings-general-gesture_control-taps', {
                    amount: Math.round(value),
                  })
                }
                min={2}
                max={10}
                step={1}
              />
              <NumberSelector
                control={control}
                name="tapDetection.fullResetTaps"
                label={l10n.getString(
                  'settings-general-gesture_control-fullResetTaps'
                )}
                valueLabelFormat={(value) =>
                  l10n.getString('settings-general-gesture_control-taps', {
                    amount: Math.round(value),
                  })
                }
                min={2}
                max={10}
                step={1}
              />
              <NumberSelector
                control={control}
                name="tapDetection.mountingResetTaps"
                label={l10n.getString(
                  'settings-general-gesture_control-mountingResetTaps'
                )}
                valueLabelFormat={(value) =>
                  l10n.getString('settings-general-gesture_control-taps', {
                    amount: Math.round(value),
                  })
                }
                min={2}
                max={10}
                step={1}
              />
            </div>
            <div className="grid sm:grid-cols-1 gap-2 pt-2">
              <Typography bold>
                {l10n.getString(
                  'settings-general-gesture_control-numberTrackersOverThreshold'
                )}
              </Typography>
              <Typography color="secondary">
                {l10n.getString(
                  'settings-general-gesture_control-numberTrackersOverThreshold-description'
                )}
              </Typography>
              <NumberSelector
                control={control}
                name="tapDetection.numberTrackersOverThreshold"
                label={l10n.getString(
                  'settings-general-gesture_control-numberTrackersOverThreshold'
                )}
                valueLabelFormat={(value) =>
                  l10n.getString('settings-general-gesture_control-trackers', {
                    amount: Math.round(value),
                  })
                }
                min={1}
                max={20}
                step={1}
              />
            </div>
          </>
        </SettingsPagePaneLayout>
      </form>
    </SettingsPageLayout>
  );
}
