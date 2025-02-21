import { useLocalization } from '@fluent/react';
import classNames from 'classnames';
import { useMemo, useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import {
  AssignTrackerRequestT,
  BodyPart,
  QuatT,
  RpcMessage,
  TrackerIdT,
  SettingsRequestT,
  SettingsResponseT,
  TapDetectionSettingsT,
  ChangeSettingsRequestT,
  TapDetectionSetupNotificationT,
} from 'solarxr-protocol';
import { FlatDeviceTracker } from '@/hooks/app';
import { useChokerWarning } from '@/hooks/choker-warning';
import { useOnboarding } from '@/hooks/onboarding';
import { useTrackers } from '@/hooks/tracker';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { Button } from '@/components/commons/Button';
import { CheckBox } from '@/components/commons/Checkbox';
import { TipBox } from '@/components/commons/TipBox';
import { Typography } from '@/components/commons/Typography';
import {
  ASSIGNMENT_RULES,
  BodyAssignment,
  LOWER_BODY,
} from '@/components/onboarding/BodyAssignment';
import { NeckWarningModal } from '@/components/onboarding/NeckWarningModal';
import { TrackerSelectionMenu } from './TrackerSelectionMenu';
import { defaultConfig, useConfig } from '@/hooks/config';
import { playTapSetupSound } from '@/sounds/sounds';
import { useBreakpoint } from '@/hooks/breakpoint';
import { TrackerAssignOptions } from './TrackerAssignOptions';

export type BodyPartError = {
  label: string | undefined;
  affectedRoles: BodyPart[];
};

interface FlatDeviceTrackerDummy {
  tracker: {
    trackerId: TrackerIdT;
    info: undefined;
  };
}

export function TrackersAssignPage() {
  const { isMobile } = useBreakpoint('mobile');
  const { l10n } = useLocalization();
  const { config, setConfig } = useConfig();
  const { useAssignedTrackers, trackers } = useTrackers();
  const { applyProgress, state } = useOnboarding();
  const { sendRPCPacket, useRPCPacket } = useWebsocketAPI();
  const defaultValues = {
    mirrorView: config?.mirrorView ?? defaultConfig.mirrorView,
  };
  const { control, watch } = useForm<{
    mirrorView: boolean;
  }>({ defaultValues });
  const { mirrorView } = watch();
  const [selectedRole, setSelectRole] = useState<BodyPart>(BodyPart.NONE);
  const assignedTrackers = useAssignedTrackers();
  useEffect(() => {
    setConfig({ mirrorView });
  }, [mirrorView]);

  const [tapDetectionSettings, setTapDetectionSettings] = useState<Omit<
    TapDetectionSettingsT,
    'pack'
  > | null>(null);

  useEffect(() => {
    sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT());
  }, []);

  useRPCPacket(RpcMessage.SettingsResponse, (settings: SettingsResponseT) => {
    if (settings.tapDetectionSettings) {
      setTapDetectionSettings(settings.tapDetectionSettings);
    }
  });

  useEffect(() => {
    if (!tapDetectionSettings) return;
    const newTapSettings = new TapDetectionSettingsT(
      tapDetectionSettings.fullResetDelay,
      tapDetectionSettings.fullResetEnabled,
      tapDetectionSettings.fullResetTaps,
      tapDetectionSettings.yawResetDelay,
      tapDetectionSettings.yawResetEnabled,
      tapDetectionSettings.yawResetTaps,
      tapDetectionSettings.mountingResetDelay,
      tapDetectionSettings.mountingResetEnabled,
      tapDetectionSettings.mountingResetTaps,
      true
    );

    sendRPCPacket(
      RpcMessage.ChangeSettingsRequest,
      new ChangeSettingsRequestT(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        newTapSettings
      )
    );

    return () => {
      newTapSettings.setupMode = false;
      sendRPCPacket(
        RpcMessage.ChangeSettingsRequest,
        new ChangeSettingsRequestT(
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          newTapSettings
        )
      );
    };
  }, [tapDetectionSettings]);

  const trackerPartGrouped = useMemo(
    () =>
      assignedTrackers.reduce<{ [key: number]: FlatDeviceTracker[] }>(
        (curr, td) => {
          const key = td.tracker.info?.bodyPart || BodyPart.NONE;
          return {
            ...curr,
            [key]: [...(curr[key] || []), td],
          };
        },
        {}
      ),
    [assignedTrackers]
  );

  const rolesWithErrors = useMemo(() => {
    const trackerRoles = trackers.map(
      ({ tracker }) => tracker.info?.bodyPart || BodyPart.NONE
    );

    const message = (assignedRole: BodyPart) => {
      const unassignedRoles: [BodyPart | BodyPart[], boolean][] = (
        ASSIGNMENT_RULES[assignedRole] || []
      ).map((part) => [
        part,
        Array.isArray(part)
          ? trackerRoles.some((tr) => part.includes(tr))
          : trackerRoles.includes(part),
      ]);

      // Special exception for waist/hip: https://github.com/SlimeVR/SlimeVR-Server/issues/612
      if (
        (assignedRole === BodyPart.HIP || assignedRole === BodyPart.WAIST) &&
        !trackerRoles.some((t) => LOWER_BODY.has(t))
      ) {
        return;
      }

      if (unassignedRoles.every(([, state]) => state)) return;

      return {
        affectedRoles: unassignedRoles
          .filter(([, state]) => !state)
          .flatMap(([part]) => part),
        label: l10n.getString(
          `onboarding-assign_trackers-warning-${BodyPart[assignedRole]}`,
          {
            unassigned: unassignedRoles
              .map(([, state]) => state)
              .reduce((acc, cur, i) => acc + (Number(cur) << i), 0),
          }
        ),
      };
    };

    return Object.keys(BodyPart)
      .map<BodyPart>((key) => +key)
      .filter((key) => typeof key === 'number' && !Number.isNaN(key))
      .reduce<Record<BodyPart, BodyPartError>>((curr, role) => {
        return {
          ...curr,
          [role]: trackerRoles.find((tr) => tr === role)
            ? message(role)
            : undefined,
        };
      }, {} as any);
  }, [trackers]);

  const onTrackerSelected = (
    tracker: FlatDeviceTracker | FlatDeviceTrackerDummy | null
  ) => {
    const assign = (
      role: BodyPart,
      rotation: QuatT | null,
      trackerId: TrackerIdT | null
    ) => {
      const assignreq = new AssignTrackerRequestT();

      assignreq.bodyPosition = role;
      assignreq.mountingOrientation = rotation;
      assignreq.trackerId = trackerId;
      assignreq.allowDriftCompensation =
        tracker?.tracker?.info?.allowDriftCompensation ?? true;

      sendRPCPacket(RpcMessage.AssignTrackerRequest, assignreq);
    };

    (trackerPartGrouped[selectedRole] || []).forEach((td) =>
      assign(
        BodyPart.NONE,
        td.tracker.info?.mountingOrientation || null,
        td.tracker.trackerId
      )
    );

    if (!tracker) {
      setSelectRole(BodyPart.NONE);
      return;
    }
    assign(
      selectedRole,
      tracker.tracker.info?.mountingOrientation || null,
      tracker.tracker.trackerId
    );
    setSelectRole(BodyPart.NONE);
  };

  useRPCPacket(
    RpcMessage.TapDetectionSetupNotification,
    (tapSetup: TapDetectionSetupNotificationT) => {
      if (selectedRole === BodyPart.NONE || !tapSetup.trackerId) return;
      onTrackerSelected({
        tracker: { trackerId: tapSetup.trackerId, info: undefined },
      });
      playTapSetupSound(config?.feedbackSoundVolume);
    }
  );

  applyProgress(0.55);

  const { closeChokerWarning, tryOpenChokerWarning, shouldShowChokerWarn } =
    useChokerWarning({
      next: setSelectRole,
    });

  const firstError = Object.values(rolesWithErrors).find((r) => !!r);

  return (
    <>
      <TrackerSelectionMenu
        bodyPart={selectedRole}
        isOpen={selectedRole !== BodyPart.NONE}
        onClose={() => setSelectRole(BodyPart.NONE)}
        onTrackerSelected={onTrackerSelected}
      ></TrackerSelectionMenu>
      <NeckWarningModal
        isOpen={shouldShowChokerWarn}
        overlayClassName={classNames(
          'fixed top-0 right-0 left-0 bottom-0 flex flex-col items-center w-full h-full justify-center bg-background-90 bg-opacity-90 z-20'
        )}
        onClose={() => closeChokerWarning(true)}
        accept={() => closeChokerWarning(false)}
      ></NeckWarningModal>
      <div className="flex flex-col gap-5 h-full items-center w-full justify-center">
        <div className="flex flex-col w-full overflow-y-auto px-4 xs:items-center">
          <div className="flex mobile:flex-col md:gap-8 mobile:gap-4 mobile:pb-4">
            <div className="flex flex-col xs:max-w-sm gap-3">
              <Typography variant="main-title">
                {l10n.getString('onboarding-assign_trackers-title')}
              </Typography>
              <Typography color="secondary">
                {l10n.getString('onboarding-assign_trackers-description')}
              </Typography>
              <div className="flex gap-1">
                <Typography color="secondary">
                  {l10n.getString('onboarding-assign_trackers-assigned', {
                    assigned: assignedTrackers.length,
                    trackers: trackers.length,
                  })}
                </Typography>
              </div>
              <TipBox>{l10n.getString('tips-find_tracker')}</TipBox>
              {!!firstError && (
                <div className="bg-status-warning text-background-60 px-3 py-2 text-justify rounded-md">
                  <div className="flex flex-col gap-1 whitespace-normal">
                    <span>{firstError.label}</span>
                  </div>
                </div>
              )}
              <div className="flex flex-col md:gap-4 sm:gap-2 xs:gap-1 mobile:gap-4">
                <TrackerAssignOptions
                  variant={isMobile ? 'dropdown' : 'radio'}
                />
                <CheckBox
                  control={control}
                  label={l10n.getString(
                    'onboarding-assign_trackers-mirror_view'
                  )}
                  name="mirrorView"
                  variant="toggle"
                ></CheckBox>
              </div>
              <div className="flex flex-row">
                {!state.alonePage && (
                  <>
                    <Button
                      variant="secondary"
                      to="/onboarding/assign-tutorial"
                    >
                      {l10n.getString('onboarding-previous_step')}
                    </Button>
                    <Button
                      variant="primary"
                      to="/onboarding/mounting/choose"
                      disabled={
                        assignedTrackers.length === 0 && trackers.length > 0
                      }
                      className="ml-auto"
                    >
                      {l10n.getString('onboarding-enter_vr-ready')}
                    </Button>
                  </>
                )}
              </div>
            </div>
            <div className="flex flex-col rounded-xl fill-background-50 pt-1">
              <BodyAssignment
                width={isMobile ? 150 : undefined}
                dotSize={isMobile ? 10 : undefined}
                onlyAssigned={false}
                highlightedRoles={firstError?.affectedRoles || []}
                rolesWithErrors={rolesWithErrors}
                assignMode={config?.assignMode ?? defaultConfig.assignMode}
                mirror={mirrorView}
                onRoleSelected={tryOpenChokerWarning}
              ></BodyAssignment>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
