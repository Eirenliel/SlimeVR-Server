import { createContext, useEffect, useState } from 'react';
import {
  BrowserRouter as Router,
  Outlet,
  Route,
  Routes,
} from 'react-router-dom';
import { Home } from './components/home/Home';
import { MainLayoutRoute } from './components/MainLayout';
import { AppContextProvider } from './components/providers/AppContext';
import { GeneralSettings } from './components/settings/pages/GeneralSettings';
import { Serial } from './components/settings/pages/Serial';
import { SettingsLayoutRoute } from './components/settings/SettingsLayout';
import {
  useProvideWebsocketApi,
  useWebsocketAPI,
  WebSocketApiContext,
} from './hooks/websocket-api';

import { Event, listen } from '@tauri-apps/api/event';
import { OnboardingContextProvider } from './components/onboarding/OnboardingContextProvicer';
import { OnboardingLayout } from './components/onboarding/OnboardingLayout';
import { AutomaticProportionsPage } from './components/onboarding/pages/body-proportions/AutomaticProportions';
import { ManualProportionsPage } from './components/onboarding/pages/body-proportions/ManualProportions';
import { ConnectTrackersPage } from './components/onboarding/pages/ConnectTracker';
import { DonePage } from './components/onboarding/pages/Done';
import { EnterVRPage } from './components/onboarding/pages/EnterVR';
import { HomePage } from './components/onboarding/pages/Home';
import { AutomaticMountingPage } from './components/onboarding/pages/mounting/AutomaticMounting';
import { ManualMountingPage } from './components/onboarding/pages/mounting/ManualMounting';
import { ResetTutorialPage } from './components/onboarding/pages/ResetTutorial';
import { TrackersAssignPage } from './components/onboarding/pages/trackers-assign/TrackerAssignment';
import { WifiCredsPage } from './components/onboarding/pages/WifiCreds';
import { ConfigContextProvider } from './components/providers/ConfigContext';
import { SerialDetectionModal } from './components/SerialDetectionModal';
import { VRCOSCSettings } from './components/settings/pages/VRCOSCSettings';
import { TopBar } from './components/TopBar';
import { TrackerSettingsPage } from './components/tracker/TrackerSettings';
import { useConfig } from './hooks/config';
import { OSCRouterSettings } from './components/settings/pages/OSCRouterSettings';
import { useLocalization } from '@fluent/react';
import { os } from '@tauri-apps/api';
import { VMCSettings } from './components/settings/pages/VMCSettings';
import { MountingChoose } from './components/onboarding/pages/mounting/MountingChoose';
import { ProportionsChoose } from './components/onboarding/pages/body-proportions/ProportionsChoose';
import { StatusProvider } from './components/providers/StatusSystemContext';
import { VersionUpdateModal } from './components/VersionUpdateModal';
import { CalibrationTutorialPage } from './components/onboarding/pages/CalibrationTutorial';
import { AssignmentTutorialPage } from './components/onboarding/pages/assignment-preparation/AssignmentTutorial';
import { open } from '@tauri-apps/api/shell';
import semver from 'semver';
import { useBreakpoint, useIsTauri } from './hooks/breakpoint';
import { VRModePage } from './components/vr-mode/VRModePage';
import { InterfaceSettings } from './components/settings/pages/InterfaceSettings';
import { error, log } from './utils/logging';
import {
  ComputerDirectory,
  RpcMessage,
  SaveFileNotificationT,
} from 'solarxr-protocol';
import mime from 'mime-types';
import { resolveDir, saveFile } from './utils/a11y';

export const GH_REPO = 'SlimeVR/SlimeVR-Server';
export const VersionContext = createContext('');
export const DOCS_SITE = 'https://docs.slimevr.dev';
export const SLIMEVR_DISCORD = 'https://discord.gg/slimevr';

function Layout() {
  const { loading } = useConfig();
  const { isMobile } = useBreakpoint('mobile');
  const isTauri = useIsTauri();
  const { useRPCPacket } = useWebsocketAPI();

  useRPCPacket(
    RpcMessage.SaveFileNotification,
    async (fileSave: SaveFileNotificationT) => {
      if (!fileSave.data) return;
      let filename =
        typeof fileSave.expectedFilename === 'string'
          ? fileSave.expectedFilename
          : 'data';

      let mimeType = 'application/octet-stream';
      if (typeof fileSave.fileExtension === 'string') {
        filename += fileSave.fileExtension;
      } else if (typeof fileSave.mimeType === 'string') {
        mimeType = fileSave.mimeType;
        filename += mime.extension(mimeType);
      }

      const file = new File([Uint8Array.from(fileSave.data)], filename, {
        type: mimeType,
      });

      saveFile({
        isTauri,
        file,
        defaultPath: await resolveDir(
          fileSave.expectedDir ?? ComputerDirectory.Documents
        ).catch(() => undefined),
      });
    }
  );

  if (loading) return <></>;

  return (
    <>
      <SerialDetectionModal></SerialDetectionModal>
      <VersionUpdateModal></VersionUpdateModal>
      <Routes>
        <Route
          path="/"
          element={
            <MainLayoutRoute isMobile={isMobile}>
              <Home />
            </MainLayoutRoute>
          }
        />
        <Route
          path="/vr-mode"
          element={
            <MainLayoutRoute isMobile={isMobile}>
              <VRModePage />
            </MainLayoutRoute>
          }
        />
        <Route
          path="/tracker/:trackernum/:deviceid"
          element={
            <MainLayoutRoute background={false} isMobile={isMobile}>
              <TrackerSettingsPage />
            </MainLayoutRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <SettingsLayoutRoute>
              <Outlet></Outlet>
            </SettingsLayoutRoute>
          }
        >
          <Route path="trackers" element={<GeneralSettings />} />
          <Route path="serial" element={<Serial />} />
          <Route path="osc/router" element={<OSCRouterSettings />} />
          <Route path="osc/vrchat" element={<VRCOSCSettings />} />
          <Route path="osc/vmc" element={<VMCSettings />} />
          <Route path="interface" element={<InterfaceSettings />} />
        </Route>
        <Route
          path="/onboarding"
          element={
            <OnboardingLayout>
              <Outlet></Outlet>
            </OnboardingLayout>
          }
        >
          <Route path="home" element={<HomePage />} />
          <Route path="wifi-creds" element={<WifiCredsPage />} />
          <Route path="connect-trackers" element={<ConnectTrackersPage />} />
          <Route
            path="calibration-tutorial"
            element={<CalibrationTutorialPage />}
          />
          <Route path="assign-tutorial" element={<AssignmentTutorialPage />} />
          <Route path="trackers-assign" element={<TrackersAssignPage />} />
          <Route path="enter-vr" element={<EnterVRPage />} />
          <Route path="mounting/choose" element={<MountingChoose />}></Route>
          <Route path="mounting/auto" element={<AutomaticMountingPage />} />
          <Route path="mounting/manual" element={<ManualMountingPage />} />
          <Route path="reset-tutorial" element={<ResetTutorialPage />} />
          <Route
            path="body-proportions/choose"
            element={<ProportionsChoose />}
          />
          <Route
            path="body-proportions/auto"
            element={<AutomaticProportionsPage />}
          />
          <Route
            path="body-proportions/manual"
            element={<ManualProportionsPage />}
          />
          <Route path="done" element={<DonePage />} />
        </Route>
        <Route path="*" element={<TopBar></TopBar>}></Route>
      </Routes>
    </>
  );
}

export default function App() {
  const websocketAPI = useProvideWebsocketApi();
  const { l10n } = useLocalization();
  const [updateFound, setUpdateFound] = useState('');
  useEffect(() => {
    async function fetchReleases() {
      const releases = await fetch(
        `https://api.github.com/repos/${GH_REPO}/releases`
      )
        .then((res) => res.json())
        .then((json: any[]) => json.filter((rl) => rl?.prerelease === false));

      if (
        __VERSION_TAG__ &&
        typeof releases[0].tag_name === 'string' &&
        semver.gt(releases[0].tag_name, __VERSION_TAG__)
      ) {
        setUpdateFound(releases[0].tag_name);
      }
    }
    fetchReleases().catch(() => error('failed to fetch releases'));
  }, []);

  if (window.__TAURI_METADATA__) {
    useEffect(() => {
      os.type()
        .then((type) => document.body.classList.add(type.toLowerCase()))
        .catch(error);

      return () => {
        os.type()
          .then((type) => document.body.classList.remove(type.toLowerCase()))
          .catch(error);
      };
    }, []);
  }

  if (window.__TAURI_METADATA__) {
    useEffect(() => {
      const unlisten = listen(
        'server-status',
        (event: Event<[string, string]>) => {
          const [eventType, s] = event.payload;
          if ('stderr' === eventType) {
            // This strange invocation is what lets us lose the line information in the console
            // See more here: https://stackoverflow.com/a/48994308
            // These two are fine to keep with console.log, they are server logs
            setTimeout(
              console.log.bind(
                console,
                `%c[SERVER] %c${s}`,
                'color:cyan',
                'color:red'
              )
            );
          } else if (eventType === 'stdout') {
            setTimeout(
              console.log.bind(
                console,
                `%c[SERVER] %c${s}`,
                'color:cyan',
                'color:green'
              )
            );
          } else if (eventType === 'error') {
            error('Error: %s', s);
          } else if (eventType === 'terminated') {
            error('Server Process Terminated: %s', s);
          } else if (eventType === 'other') {
            log('Other process event: %s', s);
          }
        }
      );
      return () => {
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        unlisten.then(() => {});
      };
    }, []);
  }

  useEffect(() => {
    function onKeyboard(ev: KeyboardEvent) {
      if (ev.key === 'F1') {
        return open(DOCS_SITE).catch(() => window.open(DOCS_SITE, '_blank'));
      }
    }

    document.addEventListener('keyup', onKeyboard);
    return () => document.removeEventListener('keyup', onKeyboard);
  }, []);

  return (
    <Router>
      <ConfigContextProvider>
        <WebSocketApiContext.Provider value={websocketAPI}>
          <AppContextProvider>
            <OnboardingContextProvider>
              <StatusProvider>
                <VersionContext.Provider value={updateFound}>
                  <div className="h-full w-full text-standard bg-background-80 text-background-10">
                    <div className="flex-col h-full">
                      {!websocketAPI.isConnected && (
                        <>
                          <TopBar></TopBar>
                          <div className="flex w-full h-full justify-center items-center p-2">
                            {websocketAPI.isFirstConnection
                              ? l10n.getString('websocket-connecting')
                              : l10n.getString('websocket-connection_lost')}
                          </div>
                        </>
                      )}
                      {websocketAPI.isConnected && <Layout></Layout>}
                    </div>
                  </div>
                </VersionContext.Provider>
              </StatusProvider>
            </OnboardingContextProvider>
          </AppContextProvider>
        </WebSocketApiContext.Provider>
      </ConfigContextProvider>
    </Router>
  );
}
