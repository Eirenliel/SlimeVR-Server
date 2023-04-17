import { useOnboarding } from '../../../../hooks/onboarding';
import { useLocalization } from '@fluent/react';
import { useState } from 'react';
import { SkipSetupWarningModal } from '../../SkipSetupWarningModal';
import { SkipSetupButton } from '../../SkipSetupButton';
import classNames from 'classnames';
import { Typography } from '../../../commons/Typography';
import { Button } from '../../../commons/Button';
export function ProportionsChoose() {
  const { l10n } = useLocalization();
  const { applyProgress, skipSetup, state } = useOnboarding();
  const [skipWarning, setSkipWarning] = useState(false);
  const [animated, setAnimated] = useState(false);

  applyProgress(0.65);

  return (
    <>
      <div className="flex flex-col gap-5 h-full items-center w-full justify-center relative">
        <SkipSetupButton
          visible={!state.alonePage}
          modalVisible={skipWarning}
          onClick={() => setSkipWarning(true)}
        ></SkipSetupButton>
        <div className="flex flex-col gap-4 justify-center">
          <div className="w-[666px]">
            <Typography variant="main-title">
              {l10n.getString('onboarding-choose_proportions')}
            </Typography>
          </div>
          <div className={classNames('h-full w-[760px] min-w-[760px]')}>
            <div className="flex flex-row gap-4 [&>div]:grow">
              <div
                className={classNames(
                  'rounded-lg p-4 flex flex-row',
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
                    <div>
                      <img
                        onMouseEnter={() => setAnimated(() => true)}
                        onAnimationEnd={() => setAnimated(() => false)}
                        src="/images/slimetower.png"
                        className={classNames(
                          'absolute w-1/3 -right-2 -top-32',
                          animated && 'animate-[bounce_1s_1]'
                        )}
                      ></img>
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
                      <Typography color="secondary">
                        {l10n.getString(
                          'onboarding-choose_proportions-auto_proportions-description'
                        )}
                      </Typography>
                    </div>
                  </div>
                  <Button
                    variant="primary"
                    to="/onboarding/body-proportions/auto"
                    className="self-start mt-auto"
                    state={{ alonePage: state.alonePage }}
                  >
                    {l10n.getString('onboarding-manual_proportions-auto')}
                  </Button>
                </div>
              </div>
            </div>
          </div>
          {!state.alonePage && (
            <Button
              variant="secondary"
              className="self-start ml-4"
              to="/onboarding/reset-tutorial"
            >
              {l10n.getString('onboarding-previous_step')}
            </Button>
          )}
        </div>
      </div>
      <SkipSetupWarningModal
        accept={skipSetup}
        onClose={() => setSkipWarning(false)}
        isOpen={skipWarning}
      ></SkipSetupWarningModal>
    </>
  );
}
