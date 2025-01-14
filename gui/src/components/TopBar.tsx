import { getCurrentWebviewWindow } from '@tauri-apps/api/webviewWindow';
import { ReactNode, useContext, useEffect, useState } from 'react';
import { NavLink, useMatch } from 'react-router-dom';
import {
  RpcMessage,
  ServerInfosRequestT,
  ServerInfosResponseT,
  TrackerStatus,
} from 'solarxr-protocol';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { CloseIcon } from './commons/icon/CloseIcon';
import { MaximiseIcon } from './commons/icon/MaximiseIcon';
import { MinimiseIcon } from './commons/icon/MinimiseIcon';
import { SlimeVRIcon } from './commons/icon/SimevrIcon';
import { ProgressBar } from './commons/ProgressBar';
import { Typography } from './commons/Typography';
import { DownloadIcon } from './commons/icon/DownloadIcon';
import { open } from '@tauri-apps/plugin-shell';
import { DOCS_SITE, GH_REPO, VersionContext } from '@/App';
import classNames from 'classnames';
import { QuestionIcon } from './commons/icon/QuestionIcon';
import { useBreakpoint, useIsTauri } from '@/hooks/breakpoint';
import { GearIcon } from './commons/icon/GearIcon';
import { invoke } from '@tauri-apps/api/core';
import { useTrackers } from '@/hooks/tracker';
import { TrackersStillOnModal } from './TrackersStillOnModal';
import { useConfig } from '@/hooks/config';
import { listen } from '@tauri-apps/api/event';
import { TrayOrExitModal } from './TrayOrExitModal';
import { error } from '@/utils/logging';
import { useDoubleTap } from 'use-double-tap';
import { isTrayAvailable } from '@/utils/tauri';
import { ErrorConsentModal } from './ErrorConsentModal';

export function VersionTag() {
  return (
    <div
      className={classNames(
        'flex justify-around flex-col text-standard-bold',
        'text-status-success bg-status-success bg-opacity-20 rounded-lg',
        'px-3 select-text cursor-pointer'
      )}
      onClick={() => {
        const url = `https://github.com/${GH_REPO}/releases`;
        open(url).catch(() => window.open(url, '_blank'));
      }}
    >
      {(__VERSION_TAG__ || __COMMIT_HASH__) + (__GIT_CLEAN__ ? '' : '-dirty')}
    </div>
  );
}

export function TopBar({
  progress,
}: {
  children?: ReactNode;
  progress?: number;
}) {
  const isTauri = useIsTauri();
  const { isMobile } = useBreakpoint('mobile');
  const { useRPCPacket, sendRPCPacket } = useWebsocketAPI();
  const { useConnectedIMUTrackers } = useTrackers();
  const connectedIMUTrackers = useConnectedIMUTrackers();
  const { config, setConfig, saveConfig } = useConfig();
  const version = useContext(VersionContext);
  const [localIp, setLocalIp] = useState<string | null>(null);
  const [showConnectedTrackersWarning, setConnectedTrackerWarning] =
    useState(false);
  const [showVersionMobile, setShowVersionMobile] = useState(false);
  const [showTrayOrExitModal, setShowTrayOrExitModal] = useState(false);
  const doesMatchSettings = useMatch({
    path: '/settings/*',
  });
  const closeApp = async () => {
    await saveConfig();
    await invoke('update_window_state');
    await getCurrentWebviewWindow().close();
  };
  const tryCloseApp = async (dontTray = false) => {
    if (isTrayAvailable && config?.useTray === null) {
      setShowTrayOrExitModal(true);
      return;
    }

    if (config?.useTray && !dontTray) {
      await getCurrentWebviewWindow().hide();
      await invoke('update_tray_text');
    } else if (
      config?.connectedTrackersWarning &&
      connectedIMUTrackers.filter(
        (t) => t.tracker.status !== TrackerStatus.TIMED_OUT
      ).length > 0
    ) {
      setConnectedTrackerWarning(true);
    } else {
      await closeApp();
    }
  };
  const showVersionBind = useDoubleTap(() => setShowVersionMobile(true));
  const unshowVersionBind = useDoubleTap(() => setShowVersionMobile(false));

  useEffect(() => {
    const unlisten = listen('try-close', async () => {
      const window = getCurrentWebviewWindow();
      await window.show();
      await window.setFocus();
      if (isTrayAvailable) await invoke('update_tray_text');
      await tryCloseApp(true);
    });
    return () => {
      unlisten.then((fn) => fn());
    };
  }, [config?.useTray, config?.connectedTrackersWarning]);

  useEffect(() => {
    if (config === null || !isTauri) return;
    getCurrentWebviewWindow().setDecorations(config?.decorations).catch(error);
  }, [config?.decorations]);

  useEffect(() => {
    sendRPCPacket(RpcMessage.ServerInfosRequest, new ServerInfosRequestT());
  }, []);

  useRPCPacket(
    RpcMessage.ServerInfosResponse,
    ({ localIp }: ServerInfosResponseT) => {
      if (localIp) setLocalIp(localIp.toString());
    }
  );

  return (
    <>
      <div className="flex gap-0 flex-col">
        <div className="h-[3px]"></div>
        <div data-tauri-drag-region className="flex gap-2 h-[38px] z-50">
          <div
            className="flex px-2 py-2 justify-around z-50"
            data-tauri-drag-region
          >
            <div className="flex gap-2" data-tauri-drag-region>
              {!config?.decorations && (
                <NavLink
                  to="/"
                  className="flex justify-around flex-col select-all"
                  data-tauri-drag-region
                >
                  <SlimeVRIcon></SlimeVRIcon>
                </NavLink>
              )}
              {(isTauri || !isMobile) && !config?.decorations && (
                <div
                  className={classNames('flex justify-around flex-col')}
                  data-tauri-drag-region
                >
                  <Typography>SlimeVR</Typography>
                </div>
              )}
              {(!(isMobile && !config?.decorations) || showVersionMobile) && (
                <>
                  <VersionTag></VersionTag>
                  {doesMatchSettings && (
                    <div
                      className={classNames(
                        'flex justify-around flex-col text-standard-bold text-status-special',
                        'bg-status-special bg-opacity-20 rounded-lg px-3 select-text'
                      )}
                      {...unshowVersionBind}
                    >
                      {localIp || 'unknown local ip'}
                    </div>
                  )}
                </>
              )}

              {version && (
                <div
                  className="cursor-pointer"
                  onClick={() => {
                    const url = document.body.classList.contains('windows')
                      ? 'https://slimevr.dev/download'
                      : `https://github.com/${GH_REPO}/releases/latest`;
                    open(url).catch(() => window.open(url, '_blank'));
                  }}
                >
                  <DownloadIcon></DownloadIcon>
                </div>
              )}
            </div>
          </div>
          <div
            className="flex flex-grow items-center h-full justify-center z-50"
            data-tauri-drag-region
          >
            {!isMobile && (
              <>
                <div
                  className="flex max-w-xl h-full items-center w-full"
                  data-tauri-drag-region
                >
                  {progress !== undefined && (
                    <ProgressBar
                      progress={progress}
                      height={3}
                      parts={3}
                    ></ProgressBar>
                  )}
                </div>
              </>
            )}

            {!isTauri && !showVersionMobile && !config?.decorations && (
              <div
                className="flex flex-row gap-2"
                {...(doesMatchSettings ? showVersionBind : {})}
              >
                <div
                  className="flex justify-around flex-col xs:hidden"
                  data-tauri-drag-region
                >
                  <Typography variant="section-title">SlimeVR</Typography>
                </div>
              </div>
            )}
          </div>
          <div
            className="flex justify-end items-center px-2 gap-2 z-50"
            data-tauri-drag-region
          >
            <NavLink
              to="/settings/trackers"
              className="flex justify-around flex-col select-all fill-background-50"
              data-tauri-drag-region
              state={{ scrollTo: 'steamvr' }}
            >
              <GearIcon></GearIcon>
            </NavLink>

            {!isMobile && (
              <div
                className={classNames(
                  'flex items-center justify-center stroke-window-icon',
                  'hover:bg-background-60 rounded-full w-7 h-7 cursor-pointer'
                )}
                onClick={() =>
                  open(DOCS_SITE).catch(() => window.open(DOCS_SITE, '_blank'))
                }
              >
                <QuestionIcon></QuestionIcon>
              </div>
            )}

            {isTauri && !config?.decorations && (
              <>
                <div
                  className="flex items-center justify-center hover:bg-background-60 rounded-full w-7 h-7"
                  onClick={() => getCurrentWebviewWindow().minimize()}
                >
                  <MinimiseIcon></MinimiseIcon>
                </div>
                <div
                  className="flex items-center justify-center hover:bg-background-60 rounded-full w-7 h-7"
                  onClick={() => getCurrentWebviewWindow().toggleMaximize()}
                >
                  <MaximiseIcon></MaximiseIcon>
                </div>
                <div
                  className="flex items-center justify-center hover:bg-background-60 rounded-full w-7 h-7"
                  onClick={() => tryCloseApp()}
                >
                  <CloseIcon></CloseIcon>
                </div>
              </>
            )}
          </div>
        </div>
        {isMobile && progress !== undefined && (
          <div className="flex gap-2 px-2 h-6 mb-2 justify-center flex-col border-b border-accent-background-30">
            <ProgressBar progress={progress} height={3} parts={3}></ProgressBar>
          </div>
        )}
      </div>
      <TrayOrExitModal
        isOpen={showTrayOrExitModal}
        accept={async (useTray) => {
          await setConfig({ useTray });
          setShowTrayOrExitModal(false);

          // Doing this in here just in case config doesn't get updated in time
          if (useTray) {
            await getCurrentWebviewWindow().hide();
            await invoke('update_tray_text');
          } else if (
            config?.connectedTrackersWarning &&
            connectedIMUTrackers.filter(
              (t) => t.tracker.status !== TrackerStatus.TIMED_OUT
            ).length > 0
          ) {
            setConnectedTrackerWarning(true);
          } else {
            await closeApp();
          }
        }}
        cancel={() => setShowTrayOrExitModal(false)}
      />
      <TrackersStillOnModal
        isOpen={showConnectedTrackersWarning}
        accept={() => closeApp()}
        cancel={() => setConnectedTrackerWarning(false)}
      ></TrackersStillOnModal>
      <ErrorConsentModal
        isOpen={config?.errorTracking === null}
        accept={() => setConfig({ errorTracking: true })}
        cancel={() => setConfig({ errorTracking: false })}
      />
    </>
  );
}
