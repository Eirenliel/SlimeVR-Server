import { Localized, useLocalization } from '@fluent/react';
import classNames from 'classnames';
import { IPv4 } from 'ip-num/IPNumber';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useParams } from 'react-router-dom';
import {
  AssignTrackerRequestT,
  BoardType,
  BodyPart,
  ForgetDeviceRequestT,
  ImuType,
  MagnetometerStatus,
  RpcMessage,
  TrackerDataType,
} from 'solarxr-protocol';
import { useDebouncedEffect } from '@/hooks/timeout';
import { useTrackerFromId } from '@/hooks/tracker';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import {
  MountingOrientationDegreesToQuatT,
  QuaternionFromQuatT,
  rotationToQuatMap,
  similarQuaternions,
} from '@/maths/quaternion';
import { ArrowLink } from '@/components/commons/ArrowLink';
import { BodyPartIcon } from '@/components/commons/BodyPartIcon';
import { Button } from '@/components/commons/Button';
import { CheckBox } from '@/components/commons/Checkbox';
import { WarningIcon } from '@/components/commons/icon/WarningIcon';
import { Input } from '@/components/commons/Input';
import { Typography } from '@/components/commons/Typography';
import { MountingSelectionMenu } from '@/components/onboarding/pages/mounting/MountingSelectionMenu';
import { IMUVisualizerWidget } from '@/components/widgets/IMUVisualizerWidget';
import { SingleTrackerBodyAssignmentMenu } from './SingleTrackerBodyAssignmentMenu';
import { TrackerCard } from './TrackerCard';
import { Quaternion } from 'three';
import { useAppContext } from '@/hooks/app';
import { MagnetometerToggleSetting } from '@/components/settings/pages/MagnetometerToggleSetting';
import semver from 'semver';
import { checkForUpdate } from '@/components/firmware-update/FirmwareUpdate';

const rotationsLabels: [Quaternion, string][] = [
  [rotationToQuatMap.BACK, 'tracker-rotation-back'],
  [rotationToQuatMap.FRONT, 'tracker-rotation-front'],
  [rotationToQuatMap.LEFT, 'tracker-rotation-left'],
  [rotationToQuatMap.RIGHT, 'tracker-rotation-right'],
  [rotationToQuatMap.BACK_LEFT, 'tracker-rotation-back_left'],
  [rotationToQuatMap.BACK_RIGHT, 'tracker-rotation-back_right'],
  [rotationToQuatMap.FRONT_LEFT, 'tracker-rotation-front_left'],
  [rotationToQuatMap.FRONT_RIGHT, 'tracker-rotation-front_right'],
];

export function TrackerSettingsPage() {
  const { l10n } = useLocalization();

  const { sendRPCPacket } = useWebsocketAPI();
  const [firstLoad, setFirstLoad] = useState(false);
  const [selectRotation, setSelectRotation] = useState<boolean>(false);
  const [selectBodypart, setSelectBodypart] = useState<boolean>(false);
  const { trackernum, deviceid } = useParams<{
    trackernum: string;
    deviceid: string;
  }>();
  const { control, watch, reset, handleSubmit } = useForm<{
    trackerName: string | null;
    allowDriftCompensation: boolean | null;
  }>({
    defaultValues: {
      trackerName: null,
      allowDriftCompensation: null,
    },
    reValidateMode: 'onSubmit',
  });
  const { dispatch } = useAppContext();
  const { trackerName, allowDriftCompensation } = watch();

  const tracker = useTrackerFromId(trackernum, deviceid);

  const onDirectionSelected = (mountingOrientationDegrees: Quaternion) => {
    if (!tracker) return;

    const assignreq = new AssignTrackerRequestT();

    assignreq.mountingOrientation = MountingOrientationDegreesToQuatT(
      mountingOrientationDegrees
    );
    assignreq.bodyPosition = tracker?.tracker.info?.bodyPart || BodyPart.NONE;
    assignreq.trackerId = tracker?.tracker.trackerId;
    if (allowDriftCompensation != null)
      assignreq.allowDriftCompensation = allowDriftCompensation;
    sendRPCPacket(RpcMessage.AssignTrackerRequest, assignreq);
    setSelectRotation(false);
  };

  const onRoleSelected = (role: BodyPart) => {
    if (!tracker) return;

    const assignreq = new AssignTrackerRequestT();
    assignreq.bodyPosition = role;
    assignreq.trackerId = tracker?.tracker.trackerId;
    if (allowDriftCompensation != null)
      assignreq.allowDriftCompensation = allowDriftCompensation;
    sendRPCPacket(RpcMessage.AssignTrackerRequest, assignreq);
    setSelectBodypart(false);
  };

  const currRotation = useMemo(() => {
    return QuaternionFromQuatT(tracker?.tracker.info?.mountingOrientation);
  }, [tracker?.tracker.info?.mountingOrientation]);

  const updateTrackerSettings = () => {
    if (!tracker) return;
    if (allowDriftCompensation == null) return;
    if (
      trackerName == tracker.tracker.info?.customName &&
      allowDriftCompensation == tracker.tracker.info?.allowDriftCompensation
    )
      return;
    const assignreq = new AssignTrackerRequestT();
    assignreq.bodyPosition = tracker?.tracker.info?.bodyPart || BodyPart.NONE;
    assignreq.mountingOrientation = currRotation
      ? MountingOrientationDegreesToQuatT(currRotation)
      : null;

    assignreq.displayName = trackerName ?? null;
    assignreq.trackerId = tracker?.tracker.trackerId;
    assignreq.allowDriftCompensation = allowDriftCompensation;
    sendRPCPacket(RpcMessage.AssignTrackerRequest, assignreq);
  };

  const onSettingsSubmit = () => {
    updateTrackerSettings();
  };

  useDebouncedEffect(() => updateTrackerSettings(), [trackerName], 1000);

  useEffect(() => {
    updateTrackerSettings();
  }, [allowDriftCompensation]);

  useEffect(() => {
    if (tracker && !firstLoad) setFirstLoad(true);
  }, [tracker, firstLoad]);

  useEffect(() => {
    if (firstLoad) {
      reset({
        trackerName: tracker?.tracker.info?.customName as string | null,
        allowDriftCompensation: tracker?.tracker.info?.allowDriftCompensation,
      });
    }
  }, [firstLoad]);

  const boardType = useMemo(() => {
    if (tracker?.device?.hardwareInfo?.officialBoardType) {
      return l10n.getString(
        'board_type-' +
          BoardType[
            tracker?.device?.hardwareInfo?.officialBoardType ??
              BoardType.UNKNOWN
          ]
      );
    } else if (tracker?.device?.hardwareInfo?.boardType) {
      return tracker?.device?.hardwareInfo?.boardType;
    } else {
      return '--';
    }
  }, [
    tracker?.device?.hardwareInfo?.officialBoardType,
    tracker?.device?.hardwareInfo?.boardType,
    l10n,
  ]);

  const macAddress = useMemo(() => {
    if (
      /(?:[a-zA-Z\d]{2}:){5}[a-zA-Z\d]{2}/.test(
        (tracker?.device?.hardwareInfo?.hardwareIdentifier as string | null) ??
          ''
      )
    ) {
      return tracker?.device?.hardwareInfo?.hardwareIdentifier as string;
    }
    return null;
  }, [tracker?.device?.hardwareInfo?.hardwareIdentifier]);

  const { currentFirmwareRelease } = useAppContext();

  const needUpdate =
    currentFirmwareRelease &&
    tracker?.device?.hardwareInfo &&
    checkForUpdate(currentFirmwareRelease, tracker?.device?.hardwareInfo);
  const updateUnavailable =
    tracker?.device?.hardwareInfo?.officialBoardType !== BoardType.SLIMEVR ||
    !semver.valid(
      tracker?.device?.hardwareInfo?.firmwareVersion?.toString() ?? 'none'
    );

  return (
    <form
      className="h-full overflow-y-auto"
      onSubmit={handleSubmit(onSettingsSubmit)}
    >
      <SingleTrackerBodyAssignmentMenu
        isOpen={selectBodypart}
        onClose={() => setSelectBodypart(false)}
        onRoleSelected={onRoleSelected}
      ></SingleTrackerBodyAssignmentMenu>
      <MountingSelectionMenu
        bodyPart={tracker?.tracker.info?.bodyPart}
        currRotation={currRotation}
        isOpen={selectRotation}
        onClose={() => setSelectRotation(false)}
        onDirectionSelected={onDirectionSelected}
      ></MountingSelectionMenu>
      <div className="flex gap-2 max-md:flex-wrap md:flex-row xs:flex-col mobile:flex-col">
        <div className="flex flex-col w-full md:max-w-xs gap-2">
          {tracker && (
            <TrackerCard
              bg="bg-background-70"
              device={tracker?.device}
              tracker={tracker?.tracker}
              shakeHighlight={false}
            ></TrackerCard>
          )}
          {
            <div className="flex flex-col bg-background-70 p-3 rounded-lg gap-2">
              <Localized id="tracker-settings-update-title">
                <Typography variant="section-title">
                  Firmware version
                </Typography>
              </Localized>
              <div className="flex gap-2">
                <Typography color="secondary">
                  v{tracker?.device?.hardwareInfo?.firmwareVersion}
                </Typography>
                <Typography color="secondary">-</Typography>
                {updateUnavailable && (
                  <Localized id="tracker-settings-update-unavailable">
                    <Typography>Cannot be updated (DIY)</Typography>
                  </Localized>
                )}
                {!updateUnavailable && (
                  <>
                    {!needUpdate && (
                      <Localized id="tracker-settings-update-up_to_date">
                        <Typography>Up to date</Typography>
                      </Localized>
                    )}
                    {needUpdate && (
                      <Localized
                        id="tracker-settings-update-available"
                        vars={{ versionName: currentFirmwareRelease?.name }}
                      >
                        <Typography color="text-accent-background-10">
                          New version available
                        </Typography>
                      </Localized>
                    )}
                  </>
                )}
              </div>
              <Localized id="tracker-settings-update">
                <Button
                  variant={needUpdate ? 'primary' : 'secondary'}
                  disabled={!needUpdate}
                  to="/firmware-update"
                >
                  Update now
                </Button>
              </Localized>
            </div>
          }

          <div className="flex flex-col bg-background-70 p-3 rounded-lg gap-2 overflow-x-auto">
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-manufacturer')}
              </Typography>
              <Typography>
                {tracker?.device?.hardwareInfo?.manufacturer}
              </Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-display_name')}
              </Typography>
              <Typography>{tracker?.tracker.info?.displayName}</Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-custom_name')}
              </Typography>
              <Typography sentry-mask>
                {tracker?.tracker.info?.customName || '--'}
              </Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-url')}
              </Typography>
              <Typography>
                udp://
                {IPv4.fromNumber(
                  tracker?.device?.hardwareInfo?.ipAddress?.addr || 0
                ).toString()}
              </Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-hardware_identifier')}
              </Typography>
              <Typography>
                {tracker?.device?.hardwareInfo?.hardwareIdentifier || '--'}
              </Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-data_support')}
              </Typography>
              <Typography>
                {tracker?.tracker.info?.dataSupport
                  ? TrackerDataType[tracker?.tracker.info?.dataSupport]
                  : '--'}
              </Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-imu')}
              </Typography>
              <Typography>
                {tracker?.tracker.info?.imuType
                  ? ImuType[tracker?.tracker.info?.imuType]
                  : '--'}
              </Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-board_type')}
              </Typography>
              <Typography>{boardType}</Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-magnetometer')}
              </Typography>
              <Typography>
                {tracker?.tracker.info?.magnetometer === undefined
                  ? '--'
                  : l10n.getString('tracker-infos-magnetometer-status-v1', {
                      status:
                        MagnetometerStatus[tracker.tracker.info.magnetometer],
                    })}
              </Typography>
            </div>
            <div className="flex justify-between">
              <Typography color="secondary">
                {l10n.getString('tracker-infos-network_version')}
              </Typography>
              <Typography>
                {tracker?.device?.hardwareInfo?.networkProtocolVersion || '--'}
              </Typography>
            </div>
          </div>
          {tracker?.tracker && (
            <IMUVisualizerWidget
              tracker={tracker?.tracker}
            ></IMUVisualizerWidget>
          )}
        </div>
        <div className="flex flex-col flex-grow  bg-background-70 rounded-lg p-5 gap-3">
          <ArrowLink to="/">
            {l10n.getString('tracker-settings-back')}
          </ArrowLink>
          <Typography variant="main-title">
            {l10n.getString('tracker-settings-title')}
          </Typography>
          <div className="flex flex-col gap-2 w-full mt-3">
            <Typography variant="section-title">
              {l10n.getString('tracker-settings-assignment_section')}
            </Typography>
            <Typography color="secondary">
              {l10n.getString(
                'tracker-settings-assignment_section-description'
              )}
            </Typography>
            <div className="flex justify-between bg-background-80 w-full p-3 rounded-lg">
              <div className="flex gap-3 items-center fill-background-10">
                {tracker?.tracker.info?.bodyPart !== BodyPart.NONE && (
                  <BodyPartIcon
                    bodyPart={tracker?.tracker.info?.bodyPart}
                  ></BodyPartIcon>
                )}
                {tracker?.tracker.info?.bodyPart === BodyPart.NONE && (
                  <WarningIcon className="fill-status-warning" />
                )}
                <Typography
                  color={classNames({
                    'text-status-warning':
                      tracker?.tracker.info?.bodyPart === BodyPart.NONE,
                  })}
                >
                  {l10n.getString(
                    'body_part-' +
                      BodyPart[tracker?.tracker.info?.bodyPart || BodyPart.NONE]
                  )}
                </Typography>
              </div>
              <div className="flex">
                <Button
                  variant="secondary"
                  onClick={() => setSelectBodypart(true)}
                >
                  {l10n.getString('tracker-settings-assignment_section-edit')}
                </Button>
              </div>
            </div>
          </div>
          {tracker?.tracker.info?.isImu && (
            <div className="flex flex-col gap-2 w-full mt-3">
              <Typography variant="section-title">
                {l10n.getString('tracker-settings-mounting_section')}
              </Typography>
              <Typography color="secondary">
                {l10n.getString(
                  'tracker-settings-mounting_section-description'
                )}
              </Typography>
              <div className="flex justify-between bg-background-80 w-full p-3 rounded-lg">
                <div className="flex gap-3 items-center">
                  <BodyPartIcon bodyPart={BodyPart.NONE}></BodyPartIcon>
                  <Typography>
                    {l10n.getString(
                      (rotationsLabels.find((q) =>
                        similarQuaternions(q[0], currRotation)
                      ) || [])[1] || 'tracker-rotation-custom'
                    ) +
                      (tracker?.tracker.info?.mountingResetOrientation &&
                      !similarQuaternions(
                        QuaternionFromQuatT(
                          tracker.tracker.info.mountingResetOrientation
                        ),
                        new Quaternion()
                      )
                        ? ` ${l10n.getString('tracker-rotation-overriden')}`
                        : '')}
                  </Typography>
                </div>
                <div className="flex">
                  <Button
                    variant="secondary"
                    onClick={() => setSelectRotation(true)}
                  >
                    {l10n.getString('tracker-settings-mounting_section-edit')}
                  </Button>
                </div>
              </div>
            </div>
          )}
          {tracker?.tracker.info?.isImu && (
            <div className="flex flex-col gap-2 w-full mt-3">
              <Typography variant="section-title">
                {l10n.getString('tracker-settings-drift_compensation_section')}
              </Typography>
              <Typography color="secondary">
                {l10n.getString(
                  'tracker-settings-drift_compensation_section-description'
                )}
              </Typography>
              <div className="flex">
                <CheckBox
                  variant="toggle"
                  outlined
                  name="allowDriftCompensation"
                  control={control}
                  label={l10n.getString(
                    'tracker-settings-drift_compensation_section-edit'
                  )}
                />
              </div>
            </div>
          )}
          {tracker?.tracker.info?.isImu &&
            tracker?.tracker.info?.magnetometer !==
              MagnetometerStatus.NOT_SUPPORTED && (
              <MagnetometerToggleSetting
                settingType="tracker"
                trackerNum={tracker.tracker.trackerId?.trackerNum}
                deviceId={tracker.tracker.trackerId?.deviceId?.id}
              />
            )}
          <div className="flex flex-col gap-2 w-full mt-3 sentry-mask">
            <Typography variant="section-title">
              {l10n.getString('tracker-settings-name_section')}
            </Typography>
            <Typography color="secondary">
              {l10n.getString('tracker-settings-name_section-description')}
            </Typography>
            <Input
              placeholder={l10n.getString(
                'tracker-settings-name_section-placeholder'
              )}
              type="text"
              name="trackerName"
              control={control}
              autocomplete="off"
              rules={undefined}
              label={l10n.getString('tracker-settings-name_section-label')}
            ></Input>
          </div>
          {macAddress && (
            <div className="flex flex-col gap-2 w-full mt-3">
              <Typography variant="section-title">
                {l10n.getString('tracker-settings-forget')}
              </Typography>
              <Typography color="secondary">
                {l10n.getString('tracker-settings-forget-description')}
              </Typography>
              <Button
                variant="secondary"
                className="!bg-status-critical self-start"
                onClick={() => {
                  sendRPCPacket(
                    RpcMessage.ForgetDeviceRequest,
                    new ForgetDeviceRequestT(macAddress)
                  );
                  dispatch({ type: 'ignoreTracker', value: macAddress });
                }}
              >
                {l10n.getString('tracker-settings-forget-label')}
              </Button>
            </div>
          )}
        </div>
      </div>
    </form>
  );
}
