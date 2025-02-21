import { DOCS_SITE } from '@/App';
import { A } from '@/components/commons/A';
import { Button } from '@/components/commons/Button';
import { WarningBox } from '@/components/commons/TipBox';
import { Typography } from '@/components/commons/Typography';
import { useOnboarding } from '@/hooks/onboarding';
import { useStatusContext } from '@/hooks/status-system';
import { Localized, useLocalization } from '@fluent/react';
import classNames from 'classnames';
import { useMemo, useState } from 'react';
import { StatusData, StatusSteamVRDisconnectedT } from 'solarxr-protocol';

export function VRUsageChoose() {
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const { statuses } = useStatusContext();
  const [animated, setAnimated] = useState(false);

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
            {l10n.getString('onboarding-usage-vr-choose')}
          </Typography>
          <Typography
            variant="standard"
            color="secondary"
            whitespace="whitespace-pre-line"
          >
            {l10n.getString('onboarding-usage-vr-choose-description')}
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
                    {l10n.getString('onboarding-usage-vr-choose-standalone')}
                  </Typography>
                  <Typography variant="vr-accessible" italic>
                    {l10n.getString(
                      'onboarding-usage-vr-choose-standalone-label'
                    )}
                  </Typography>
                </div>
                <div>
                  <Typography
                    color="secondary"
                    whitespace="whitespace-pre-line"
                  >
                    {l10n.getString(
                      'onboarding-usage-vr-choose-standalone-description'
                    )}
                  </Typography>
                </div>
              </div>
              <Button
                variant={!state.alonePage ? 'secondary' : 'tertiary'}
                to={'/onboarding/usage/vr/standalone'}
                className="self-start mt-auto"
                state={{ alonePage: state.alonePage }}
              >
                {l10n.getString('onboarding-usage-vr-choose-standalone')}
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
                    src="/images/vrslimes.webp"
                    className={classNames(
                      'absolute w-[150px] -right-8 -top-16',
                      animated && 'animate-[bounce_1s_1]'
                    )}
                  ></img>
                  <Typography variant="main-title" bold>
                    {l10n.getString('onboarding-usage-vr-choose-steamvr')}
                  </Typography>
                  <Typography variant="vr-accessible" italic>
                    {l10n.getString('onboarding-usage-vr-choose-steamvr-label')}
                  </Typography>
                </div>
                <div className="flex flex-col gap-3">
                  <Typography
                    color="secondary"
                    whitespace="whitespace-pre-line"
                  >
                    {l10n.getString(
                      'onboarding-usage-vr-choose-steamvr-description'
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
                to="/onboarding/mounting/manual"
                className="self-start mt-auto"
                state={{ alonePage: state.alonePage }}
                disabled={missingSteamVr}
              >
                {l10n.getString('onboarding-usage-vr-choose-steamvr')}
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
