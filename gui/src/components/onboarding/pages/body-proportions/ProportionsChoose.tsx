import { useOnboarding } from '../../../../hooks/onboarding';
import { Localized, useLocalization } from '@fluent/react';
import { useMemo, useState } from 'react';
import classNames from 'classnames';
import { Typography } from '../../../commons/Typography';
import { Button } from '../../../commons/Button';
import {
  SkeletonConfigResponseT,
  RpcMessage,
  SkeletonConfigRequestT,
} from 'solarxr-protocol';
import { useWebsocketAPI } from '../../../../hooks/websocket-api';
import { useIsTauri } from '../../../../hooks/breakpoint';
import { useAppContext } from '../../../../hooks/app';
import { saveFile } from '../../../../utils/a11y';

export const MIN_HEIGHT = 0.4;
export const MAX_HEIGHT = 4;
export const DEFAULT_HEIGHT = 1.5;

export function ProportionsChoose() {
  const isTauri = useIsTauri();
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const { useRPCPacket, sendRPCPacket } = useWebsocketAPI();
  const [animated, setAnimated] = useState(false);
  const { computedTrackers } = useAppContext();

  const hmdTracker = useMemo(
    () =>
      computedTrackers.find(
        (tracker) =>
          tracker.tracker.trackerId?.trackerNum === 1 &&
          tracker.tracker.trackerId.deviceId?.id === undefined
      ),
    [computedTrackers]
  );

  const beneathFloor = useMemo(
    () =>
      !(
        hmdTracker?.tracker.position &&
        hmdTracker.tracker.position.y >= MIN_HEIGHT
      ),
    [hmdTracker?.tracker.position?.y]
  );

  useRPCPacket(
    RpcMessage.SkeletonConfigResponse,
    (data: SkeletonConfigResponseT) => {
      const file = new File([JSON.stringify(data)], 'body-proportions.json', {
        type: 'application/json',
      });
      saveFile({
        isTauri,
        file,
        filters: [
          {
            name: l10n.getString('onboarding-choose_proportions-file_type'),
            extensions: ['json'],
          },
        ],
      });
    }
  );

  applyProgress(0.85);

  return (
    <>
      <div className="flex flex-col gap-5 h-full items-center w-full xs:justify-center mobile:overflow-y-auto relative px-4 pb-4">
        <div className="flex flex-col gap-4 justify-center">
          <Typography variant="main-title">
            {l10n.getString('onboarding-choose_proportions')}
          </Typography>
          <div className="xs:w-10/12 xs:max-w-[666px]">
            <Typography
              variant="standard"
              color="secondary"
              whitespace="whitespace-pre-line"
            >
              {l10n.getString('onboarding-choose_proportions-description')}
            </Typography>
          </div>
          <div
            className={classNames(
              'grid xs:grid-cols-2 w-full xs:flex-row mobile:flex-col gap-4 [&>div]:grow'
            )}
          >
            <div
              className={classNames(
                'rounded-lg p-4 flex flex-row flex-grow',
                !state.alonePage && 'bg-background-70',
                state.alonePage && 'bg-background-60'
              )}
            >
              <div className="flex flex-col gap-4">
                <div className="flex flex-grow flex-col gap-4 max-w-sm">
                  <div>
                    <Typography variant="main-title" bold>
                      {l10n.getString(
                        'onboarding-choose_proportions-manual_proportions'
                      )}
                    </Typography>
                    <Typography variant="vr-accessible" italic>
                      {l10n.getString(
                        'onboarding-choose_proportions-manual_proportions-subtitle'
                      )}
                    </Typography>
                  </div>
                  <div>
                    <Typography color="secondary">
                      {l10n.getString(
                        'onboarding-choose_proportions-manual_proportions-description'
                      )}
                    </Typography>
                  </div>
                </div>

                <Button
                  variant={!state.alonePage ? 'secondary' : 'tertiary'}
                  to="/onboarding/body-proportions/manual"
                  className="self-start mt-auto"
                  state={{ alonePage: state.alonePage }}
                >
                  {l10n.getString('onboarding-automatic_proportions-manual')}
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
                  <img
                    onMouseEnter={() => setAnimated(() => true)}
                    onAnimationEnd={() => setAnimated(() => false)}
                    src="/images/slimetower.webp"
                    className={classNames(
                      'absolute w-[100px] -right-2 -top-24',
                      animated && 'animate-[bounce_1s_1]'
                    )}
                  ></img>
                  <div>
                    <Typography variant="main-title" bold>
                      {l10n.getString(
                        'onboarding-choose_proportions-auto_proportions'
                      )}
                    </Typography>
                    <Typography variant="vr-accessible" italic>
                      {l10n.getString(
                        'onboarding-choose_proportions-auto_proportions-subtitle'
                      )}
                    </Typography>
                  </div>
                  <div>
                    <Localized
                      id="onboarding-choose_proportions-auto_proportions-descriptionv2"
                      elems={{ b: <b></b> }}
                    >
                      <Typography
                        color="secondary"
                        whitespace="whitespace-pre-line"
                      >
                        Description for autobone
                      </Typography>
                    </Localized>
                  </div>
                </div>
                <Button
                  variant="primary"
                  // Check if we are in dev mode and just let it be used
                  disabled={beneathFloor && import.meta.env.PROD}
                  to="/onboarding/body-proportions/auto"
                  className="self-start mt-auto"
                  state={{ alonePage: state.alonePage }}
                >
                  {l10n.getString('onboarding-manual_proportions-auto')}
                </Button>
              </div>
            </div>
          </div>
          <div className="flex flex-row">
            {!state.alonePage && (
              <Button variant="secondary" to="/onboarding/reset-tutorial">
                {l10n.getString('onboarding-previous_step')}
              </Button>
            )}
            <Button
              variant={!state.alonePage ? 'secondary' : 'tertiary'}
              className="ml-auto"
              onClick={() =>
                sendRPCPacket(
                  RpcMessage.SkeletonConfigRequest,
                  new SkeletonConfigRequestT()
                )
              }
            >
              {l10n.getString('onboarding-choose_proportions-export')}
            </Button>
          </div>
        </div>
      </div>
    </>
  );
}
