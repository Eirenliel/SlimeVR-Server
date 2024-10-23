import { Button } from '@/components/commons/Button';
import { Typography } from '@/components/commons/Typography';
import { useOnboarding } from '@/hooks/onboarding';
import { useLocalization } from '@fluent/react';
import classNames from 'classnames';
import { useState } from 'react';

export function VRUsageChoose() {
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const [animated, setAnimated] = useState(false);

  applyProgress(0.7);

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
                  <Typography color="secondary">
                    {l10n.getString(
                      'onboarding-usage-vr-choose-standalone-description'
                    )}
                  </Typography>
                </div>
              </div>
              <Button
                variant={!state.alonePage ? 'secondary' : 'tertiary'}
                to={'/onboarding/mounting/auto'}
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
                <div>
                  <Typography color="secondary">
                    {l10n.getString(
                      'onboarding-usage-vr-choose-steamvr-description'
                    )}
                  </Typography>
                </div>
              </div>

              <Button
                variant={'primary'}
                to="/onboarding/mounting/manual"
                className="self-start mt-auto"
                state={{ alonePage: state.alonePage }}
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
