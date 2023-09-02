import { useMemo, useState } from 'react';
import { AssignTrackerRequestT, BodyPart, RpcMessage } from 'solarxr-protocol';
import { FlatDeviceTracker } from '../../../../hooks/app';
import { useOnboarding } from '../../../../hooks/onboarding';
import { useTrackers } from '../../../../hooks/tracker';
import { useWebsocketAPI } from '../../../../hooks/websocket-api';
import { MountingOrientationDegreesToQuatT } from '../../../../maths/quaternion';
import { Button } from '../../../commons/Button';
import { TipBox } from '../../../commons/TipBox';
import { Typography } from '../../../commons/Typography';
import { BodyAssignment } from '../../BodyAssignment';
import { MountingSelectionMenu } from './MountingSelectionMenu';
import { useLocalization } from '@fluent/react';
import { useBreakpoint } from '../../../../hooks/breakpoint';
import { Quaternion } from 'three';

export function ManualMountingPage() {
  const { isMobile } = useBreakpoint('mobile');
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const { sendRPCPacket } = useWebsocketAPI();

  const [selectedRole, setSelectRole] = useState<BodyPart>(BodyPart.NONE);

  applyProgress(0.7);

  const { useAssignedTrackers } = useTrackers();
  const assignedTrackers = useAssignedTrackers();

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

  const onDirectionSelected = (mountingOrientationDegrees: Quaternion) => {
    (trackerPartGrouped[selectedRole] || []).forEach((td) => {
      const assignreq = new AssignTrackerRequestT();

      assignreq.bodyPosition = td.tracker.info?.bodyPart || BodyPart.NONE;
      assignreq.mountingOrientation = MountingOrientationDegreesToQuatT(
        mountingOrientationDegrees
      );
      assignreq.trackerId = td.tracker.trackerId;
      assignreq.allowDriftCompensation =
        td.tracker.info?.allowDriftCompensation ?? true;
      assignreq.accessoryId = td.tracker.info?.accessoryId || 0;

      sendRPCPacket(RpcMessage.AssignTrackerRequest, assignreq);
    });

    setSelectRole(BodyPart.NONE);
  };

  return (
    <>
      <MountingSelectionMenu
        bodyPart={selectedRole}
        isOpen={selectedRole !== BodyPart.NONE}
        onClose={() => setSelectRole(BodyPart.NONE)}
        onDirectionSelected={onDirectionSelected}
      ></MountingSelectionMenu>
      <div className="flex flex-col gap-5 h-full items-center w-full xs:justify-center relative overflow-y-auto">
        <div className="flex xs:flex-row mobile:flex-col h-full px-8 xs:w-full xs:justify-center mobile:px-4 items-center">
          <div className="flex flex-col w-full xs:max-w-sm gap-3">
            <Typography variant="main-title">
              {l10n.getString('onboarding-manual_mounting')}
            </Typography>
            <Typography color="secondary">
              {l10n.getString('onboarding-manual_mounting-description')}
            </Typography>
            <TipBox>{l10n.getString('tips-find_tracker')}</TipBox>
            <div className="flex flex-row gap-3 mt-auto">
              <Button
                variant="secondary"
                to="/onboarding/mounting/choose"
                state={state}
              >
                {l10n.getString('onboarding-previous_step')}
              </Button>
              {!state.alonePage && (
                <Button variant="primary" to="/onboarding/reset-tutorial">
                  {l10n.getString('onboarding-manual_mounting-next')}
                </Button>
              )}
            </div>
          </div>
          <div className="flex flex-row justify-center">
            <BodyAssignment
              width={isMobile ? 160 : undefined}
              onlyAssigned={true}
              advanced={true}
              onRoleSelected={setSelectRole}
            ></BodyAssignment>
          </div>
        </div>
      </div>
    </>
  );
}
