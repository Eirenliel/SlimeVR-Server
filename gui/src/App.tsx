import { createContext, useEffect, useState } from 'react';
import {
  BrowserRouter as Router,
  Outlet,
  Route,
  Routes,
} from 'react-router-dom';
import { Home } from './components/home/Home';
import { MainLayout } from './components/MainLayout';
import { AppContextProvider } from './components/providers/AppContext';
import { GeneralSettings } from './components/settings/pages/GeneralSettings';
import { Serial } from './components/settings/pages/Serial';
import { SettingsLayout } from './components/settings/SettingsLayout';
import {
  useProvideWebsocketApi,
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
import { OSCRouterSettings } from './components/settings/pages/OSCRouterSettings';
import { useLocalization } from '@fluent/react';
import * as os from '@tauri-apps/plugin-os';
import { VMCSettings } from './components/settings/pages/VMCSettings';
import { MountingChoose } from './components/onboarding/pages/mounting/MountingChoose';
import { ProportionsChoose } from './components/onboarding/pages/body-proportions/ProportionsChoose';
import { StatusProvider } from './components/providers/StatusSystemContext';
import { VersionUpdateModal } from './components/VersionUpdateModal';
import { CalibrationTutorialPage } from './components/onboarding/pages/CalibrationTutorial';
import { AssignmentTutorialPage } from './components/onboarding/pages/assignment-preparation/AssignmentTutorial';
import { open } from '@tauri-apps/plugin-shell';
import semver from 'semver';
import { useBreakpoint, useIsTauri } from './hooks/breakpoint';
import { VRModePage } from './components/vr-mode/VRModePage';
import { InterfaceSettings } from './components/settings/pages/InterfaceSettings';
import { error, log } from './utils/logging';
import { FirmwareToolSettings } from './components/firmware-tool/FirmwareTool';
import { AppLayout } from './AppLayout';
import { Preload } from './components/Preload';
import { UnknownDeviceModal } from './components/UnknownDeviceModal';
import { useDiscordPresence } from './hooks/discord-presence';
import { withSentryReactRouterV6Routing } from '@sentry/react';
import { EmptyLayout } from './components/EmptyLayout';
import { AdvancedSettings } from './components/settings/pages/AdvancedSettings';
import { FirmwareUpdate } from './components/firmware-update/FirmwareUpdate';

export const GH_REPO = 'SlimeVR/SlimeVR-Server';
export const VersionContext = createContext('');
export const DOCS_SITE = 'https://docs.slimevr.dev';
export const SLIMEVR_DISCORD = 'https://discord.gg/slimevr';

const SentryRoutes = withSentryReactRouterV6Routing(Routes);

function Layout() {
  const { isMobile } = useBreakpoint('mobile');
  useDiscordPresence();

  return (
    <>
      <SerialDetectionModal></SerialDetectionModal>
      <VersionUpdateModal></VersionUpdateModal>
      <UnknownDeviceModal></UnknownDeviceModal>
      <SentryRoutes>
        <Route element={<AppLayout />}>
          <Route
            path="/"
            element={
              <MainLayout isMobile={isMobile}>
                <Home />
              </MainLayout>
            }
          />
          <Route
            path="/firmware-update"
            element={
              <MainLayout isMobile={isMobile} widgets={false}>
                <FirmwareUpdate />
              </MainLayout>
            }
          />
          <Route
            path="/vr-mode"
            element={
              <MainLayout isMobile={isMobile}>
                <VRModePage />
              </MainLayout>
            }
          />
          <Route
            path="/tracker/:trackernum/:deviceid"
            element={
              <MainLayout background={false} isMobile={isMobile}>
                <TrackerSettingsPage />
              </MainLayout>
            }
          />
          <Route
            path="/settings"
            element={
              <SettingsLayout>
                <Outlet />
              </SettingsLayout>
            }
          >
            <Route path="firmware-tool" element={<FirmwareToolSettings />} />
            <Route path="trackers" element={<GeneralSettings />} />
            <Route path="serial" element={<Serial />} />
            <Route path="osc/router" element={<OSCRouterSettings />} />
            <Route path="osc/vrchat" element={<VRCOSCSettings />} />
            <Route path="osc/vmc" element={<VMCSettings />} />
            <Route path="interface" element={<InterfaceSettings />} />
            <Route path="advanced" element={<AdvancedSettings />} />
          </Route>
          <Route
            path="/onboarding"
            element={
              <OnboardingLayout>
                <Outlet />
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
            <Route
              path="assign-tutorial"
              element={<AssignmentTutorialPage />}
            />
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
        </Route>
      </SentryRoutes>
    </>
  );
}

export default function App() {
  const websocketAPI = useProvideWebsocketApi();
  const { l10n } = useLocalization();
  const [updateFound, setUpdateFound] = useState('');
  const isTauri = useIsTauri();

  useEffect(() => {
    const onKeydown: (arg0: KeyboardEvent) => void = function (event) {
      // prevent search bar keybind
      if (
        event.key === 'F3' ||
        (event.ctrlKey && event.key === 'f') ||
        (event.metaKey && event.key === 'f')
      ) {
        event.preventDefault();
      }
    };

    window.addEventListener('keydown', onKeydown);
    return () => window.removeEventListener('keydown', onKeydown);
  }, []);

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

  if (isTauri) {
    useEffect(() => {
      const type = os.type();
      document.body.classList.add(type.toLowerCase());

      return () => document.body.classList.remove(type.toLowerCase());
    }, []);
  }

  if (isTauri) {
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
        unlisten.then((fn) => fn());
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
                    <Preload />
                    {!websocketAPI.isConnected && (
                      <EmptyLayout>
                        <div className="flex w-full h-full justify-center items-center p-2">
                          {websocketAPI.isFirstConnection
                            ? l10n.getString('websocket-connecting')
                            : l10n.getString('websocket-connection_lost')}
                        </div>
                      </EmptyLayout>
                    )}
                    {websocketAPI.isConnected && <Layout></Layout>}
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
