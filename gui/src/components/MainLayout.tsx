import classNames from 'classnames';
import { ReactNode, useEffect, useState } from 'react';
import {
  LegTweaksTmpChangeT,
  LegTweaksTmpClearT,
  ResetType,
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
} from 'solarxr-protocol';
import { useConfig } from '../hooks/config';
import { useLayout } from '../hooks/layout';
import { BVHButton } from './BVHButton';
import { ResetButton } from './home/ResetButton';
import { Navbar } from './Navbar';
import { TopBar } from './TopBar';
import { DeveloperModeWidget } from './widgets/DeveloperModeWidget';
import { OverlayWidget } from './widgets/OverlayWidget';
import { ClearDriftCompensationButton } from './ClearDriftCompensationButton';
import { useWebsocketAPI } from '../hooks/websocket-api';

export function MainLayoutRoute({
  children,
  background = true,
  widgets = true,
}: {
  children: ReactNode;
  background?: boolean;
  widgets?: boolean;
}) {
  const { layoutHeight, ref } = useLayout<HTMLDivElement>();
  const { layoutWidth, ref: refw } = useLayout<HTMLDivElement>();
  const { config } = useConfig();
  const { useRPCPacket, sendRPCPacket } = useWebsocketAPI();
  const [driftCompensationEnabled, setDriftCompensationEnabled] =
    useState(false);
  const [ProportionsLastPageOpen, setProportionsLastPageOpen] = useState(true);

  useEffect(() => {
    sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT());
  }, []);

  useRPCPacket(RpcMessage.SettingsResponse, (settings: SettingsResponseT) => {
    if (settings.driftCompensation != null)
      setDriftCompensationEnabled(settings.driftCompensation.enabled);
  });

  function usePageChanged(callback: () => void) {
    useEffect(() => {
      callback();
    }, [location.pathname]);
  }

  usePageChanged(() => {
    if (location.pathname.includes('body-proportions')) {
      const tempSettings = new LegTweaksTmpChangeT();
      tempSettings.skatingCorrection = false;
      tempSettings.floorClip = false;
      tempSettings.toeSnap = false;
      tempSettings.footPlant = false;

      sendRPCPacket(RpcMessage.LegTweaksTmpChange, tempSettings);
    } else if (ProportionsLastPageOpen) {
      const resetSettings = new LegTweaksTmpClearT();
      resetSettings.skatingCorrection = true;
      resetSettings.floorClip = true;
      resetSettings.toeSnap = true;
      resetSettings.footPlant = true;

      sendRPCPacket(RpcMessage.LegTweaksTmpClear, resetSettings);
    }
    setProportionsLastPageOpen(location.pathname.includes('body-proportions'));
  });

  return (
    <>
      <TopBar></TopBar>
      <div ref={ref} className="flex-grow" style={{ height: layoutHeight }}>
        <div className="flex h-full pb-3">
          <Navbar></Navbar>
          <div
            className="flex gap-2 pr-3 w-full"
            ref={refw}
            style={{ minWidth: layoutWidth }}
          >
            <div
              className={classNames(
                'flex flex-col rounded-xl w-full overflow-hidden',
                background && 'bg-background-70'
              )}
            >
              {children}
            </div>
            {widgets && (
              <div className="flex flex-col px-2 min-w-[274px] w-[274px] gap-2 pt-2 rounded-xl overflow-y-auto bg-background-70">
                <div className="grid grid-cols-2 gap-2 w-full [&>*:nth-child(odd):last-of-type]:col-span-full">
                  <ResetButton type={ResetType.Yaw} variant="big"></ResetButton>
                  <ResetButton
                    type={ResetType.Full}
                    variant="big"
                  ></ResetButton>
                  {config?.debug && (
                    <ResetButton
                      type={ResetType.Mounting}
                      variant="big"
                    ></ResetButton>
                  )}
                  <BVHButton></BVHButton>
                  {driftCompensationEnabled && (
                    <ClearDriftCompensationButton></ClearDriftCompensationButton>
                  )}
                </div>
                <div className="w-full">
                  <OverlayWidget></OverlayWidget>
                </div>
                {config?.debug && (
                  <div className="w-full">
                    <DeveloperModeWidget></DeveloperModeWidget>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
