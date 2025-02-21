import { DOCS_SITE } from '@/App';
import { A } from '@/components/commons/A';
import { Button } from '@/components/commons/Button';
import { WarningBox } from '@/components/commons/TipBox';
import { Typography } from '@/components/commons/Typography';
import { useOnboarding } from '@/hooks/onboarding';
import { useStatusContext } from '@/hooks/status-system';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { Localized, useLocalization } from '@fluent/react';
import classNames from 'classnames';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ChangeSettingsRequestT,
  ModelSettingsT,
  ModelTogglesT,
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
  StatusData,
  StatusSteamVRDisconnectedT,
} from 'solarxr-protocol';

export function HeadTrackingChoose() {
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const { statuses } = useStatusContext();
  const [animated, setAnimated] = useState(false);
  const { sendRPCPacket, useRPCPacket } = useWebsocketAPI();
  const fetchedSettings = useRef<ModelTogglesT | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT());
  }, []);

  const toggleMocap = (bool: boolean) => {
    const settings = new ChangeSettingsRequestT();
    const modelSettings = new ModelSettingsT();

    modelSettings.toggles = fetchedSettings.current ?? new ModelTogglesT();
    modelSettings.toggles.selfLocalization = bool;

    settings.modelSettings = modelSettings;
    sendRPCPacket(RpcMessage.ChangeSettingsRequest, settings);
  };

  useRPCPacket(
    RpcMessage.SettingsResponse,
    (oldSettings: SettingsResponseT) => {
      fetchedSettings.current = oldSettings.modelSettings?.toggles ?? null;

      toggleMocap(false);
    }
  );

  const missingSteamVr = useMemo(
    () =>
      Object.values(statuses).some(
        (x) =>
          x.dataType === StatusData.StatusSteamVRDisconnected &&
          (x.data as StatusSteamVRDisconnectedT).bridgeSettingsName ===
            'steamvr'
      ),
    [statuses]
  );

  applyProgress(0.55);

  return (
    <div className="flex flex-col gap-5 h-full items-center w-full xs:justify-center relative overflow-y-auto px-4 pb-4">
      <div className="flex flex-col gap-4 justify-center">
        <div className="xs:w-10/12 xs:max-w-[666px]">
          <Typography variant="main-title">
            {l10n.getString('onboarding-usage-mocap-head_choose')}
          </Typography>
          <Typography
            variant="standard"
            color="secondary"
            whitespace="whitespace-pre-line"
          >
            {l10n.getString('onboarding-usage-mocap-head_choose-description')}
          </Typography>
        </div>
        <div
          className={classNames(
            'grid xs:grid-cols-2 w-full xs:flex-row mobile:flex-col gap-4 [&>div]:grow'
          )}
        >
          <div
            className={classNames(
              'rounded-lg p-4 flex',
              !state.alonePage && 'bg-background-70',
              state.alonePage && 'bg-background-60'
            )}
          >
            <div className="flex flex-col gap-4">
              <div className="flex flex-grow flex-col gap-4 max-w-sm">
                <div>
                  <Typography variant="main-title" bold>
                    {l10n.getString(
                      'onboarding-usage-mocap-head_choose-standalone'
                    )}
                  </Typography>
                  <Typography variant="vr-accessible" italic>
                    {l10n.getString(
                      'onboarding-usage-mocap-head_choose-standalone-label'
                    )}
                  </Typography>
                </div>
                <div>
                  <Typography
                    color="secondary"
                    whitespace="whitespace-pre-line"
                  >
                    {l10n.getString(
                      'onboarding-usage-mocap-head_choose-standalone-description'
                    )}
                  </Typography>
                </div>
              </div>
              <Button
                variant={!state.alonePage ? 'secondary' : 'tertiary'}
                className="self-start mt-auto"
                onClick={() => {
                  toggleMocap(true);

                  navigate('/onboarding/usage/mocap/data/choose', {
                    state: { alonePage: state.alonePage },
                  });
                }}
              >
                {l10n.getString(
                  'onboarding-usage-mocap-head_choose-standalone-button'
                )}
              </Button>
            </div>
          </div>
          <div
            className={classNames(
              'rounded-lg p-4 flex flex-row relative',
              !state.alonePage && 'bg-background-70',
              state.alonePage && 'bg-background-60'
            )}
          >
            <div className="flex flex-col gap-4">
              <div className="flex flex-grow flex-col gap-4 max-w-sm">
                <div>
                  <img
                    onMouseEnter={() => setAnimated(() => true)}
                    onAnimationEnd={() => setAnimated(() => false)}
                    src="/images/nighty-vr-sitting.webp"
                    className={classNames(
                      'absolute w-[150px] -right-8 -top-36',
                      animated && 'animate-[bounce_1s_1]'
                    )}
                  ></img>
                  <Typography variant="main-title" bold>
                    {l10n.getString(
                      'onboarding-usage-mocap-head_choose-steamvr'
                    )}
                  </Typography>
                  <Typography variant="vr-accessible" italic>
                    {l10n.getString(
                      'onboarding-usage-mocap-head_choose-steamvr-label'
                    )}
                  </Typography>
                </div>
                <div className="flex flex-col gap-3">
                  <Typography
                    color="secondary"
                    whitespace="whitespace-pre-line"
                  >
                    {l10n.getString(
                      'onboarding-usage-mocap-head_choose-steamvr-description'
                    )}
                  </Typography>
                  {
                    // TODO: Add a button to open SteamVR via tauri's open()
                    missingSteamVr && (
                      <Localized
                        id="onboarding-usage-vr-choose-steamvr-warning"
                        elems={{
                          docs: (
                            <A
                              href={`${DOCS_SITE}/common-issues.html#the-trackers-are-connected-to-the-slimevr-server-but-arent-turning-up-on-steam`}
                            ></A>
                          ),
                          b: <b></b>,
                        }}
                      >
                        <WarningBox>SteamVR driver not connected</WarningBox>
                      </Localized>
                    )
                  }
                </div>
              </div>

              <Button
                variant={'primary'}
                to="/onboarding/usage/mocap/data/choose"
                className="self-start mt-auto"
                state={{ alonePage: state.alonePage }}
                disabled={missingSteamVr}
              >
                {l10n.getString(
                  'onboarding-usage-mocap-head_choose-steamvr-button'
                )}
              </Button>
            </div>
          </div>
        </div>

        <Button
          variant="secondary"
          className="self-start"
          to="/onboarding/usage/choose"
        >
          {l10n.getString('onboarding-previous_step')}
        </Button>
      </div>
    </div>
  );
}
