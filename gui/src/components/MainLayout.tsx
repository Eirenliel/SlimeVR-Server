import classNames from 'classnames';
import { ReactNode, useEffect, useMemo, useState } from 'react';
import {
  LegTweaksTmpChangeT,
  LegTweaksTmpClearT,
  ResetType,
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
  StatusData,
} from 'solarxr-protocol';
import { useConfig } from '../hooks/config';
import { useElemSize, useLayout } from '../hooks/layout';
import { BVHButton } from './BVHButton';
import { ResetButton } from './home/ResetButton';
import { Navbar } from './Navbar';
import { TopBar } from './TopBar';
import { DeveloperModeWidget } from './widgets/DeveloperModeWidget';
import { OverlayWidget } from './widgets/OverlayWidget';
import { ClearDriftCompensationButton } from './ClearDriftCompensationButton';
import { useWebsocketAPI } from '../hooks/websocket-api';
import { useStatusContext, parseStatusToLocale } from '../hooks/status-system';
import { Localized } from '@fluent/react';
import { TipBox } from './commons/TipBox';
import { useAppContext } from '../hooks/app';
import { TrackingPauseButton } from './TrackingPauseButton';
import { WidgetsComponent } from './WidgetsComponent';

export function MainLayoutRoute({
  children,
  background = true,
  widgets = true,
  isMobile = undefined,
}: {
  children: ReactNode;
  background?: boolean;
  isMobile?: boolean;
  widgets?: boolean;
}) {
  const { height, ref: navRef } = useElemSize<HTMLDivElement>();
  const { layoutHeight, ref } = useLayout<HTMLDivElement>();
  const { layoutWidth, ref: refw } = useLayout<HTMLDivElement>();
  const { sendRPCPacket } = useWebsocketAPI();
  const [ProportionsLastPageOpen, setProportionsLastPageOpen] = useState(true);

  useEffect(() => {
    sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT());
  }, []);

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
      <div
        ref={ref}
        className="flex-grow"
        style={{ height: layoutHeight - height }}
      >
        <div className="flex h-full xs:pb-3">
          {!isMobile && <Navbar></Navbar>}
          <div
            className="flex gap-2 xs:pr-3  w-full"
            ref={refw}
            style={{ minWidth: layoutWidth }}
          >
            <div
              className={classNames(
                'flex flex-col rounded-xl w-full overflow-hidden mobile:overflow-y-auto',
                background && 'bg-background-70'
              )}
            >
              {children}
            </div>
            {!isMobile && widgets && (
              <div className="flex flex-col px-2 min-w-[274px] w-[274px] gap-2 pt-2 rounded-xl overflow-y-auto bg-background-70">
                <WidgetsComponent></WidgetsComponent>
              </div>
            )}
          </div>
        </div>
        <div ref={navRef}>{isMobile && <Navbar></Navbar>}</div>
      </div>
    </>
  );
}
