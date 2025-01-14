import { Localized, useLocalization } from '@fluent/react';
import { useOnboarding } from '@/hooks/onboarding';
import { useWifiForm } from '@/hooks/wifi-form';
import { Button } from '@/components/commons/Button';
import { Input } from '@/components/commons/Input';
import { Typography } from '@/components/commons/Typography';
import classNames from 'classnames';
import { useTrackers } from '@/hooks/tracker';
import { useIsRestCalibrationTrackers } from '@/hooks/imu-logic';

export function WifiCredsPage() {
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const { control, handleSubmit, submitWifiCreds, formState } = useWifiForm();
  const { useConnectedIMUTrackers } = useTrackers();
  const connectedIMUTrackers = useConnectedIMUTrackers();

  applyProgress(0.2);

  const isRestCalibration = useIsRestCalibrationTrackers(connectedIMUTrackers);

  return (
    <form
      className="flex flex-col w-full h-full"
      onSubmit={handleSubmit(submitWifiCreds)}
    >
      <div className="flex flex-col w-full h-full xs:justify-center items-center relative ">
        <div className="flex mobile:flex-col xs:gap-10 px-4">
          <div className="flex flex-col max-w-sm">
            <Typography variant="main-title">
              {l10n.getString('onboarding-wifi_creds')}
            </Typography>
            <>
              {l10n
                .getString('onboarding-wifi_creds-description')
                .split('\n')
                .map((line, i) => (
                  <Typography color="secondary" key={i}>
                    {line}
                  </Typography>
                ))}
            </>
            {!state.alonePage && (
              <Button
                variant="secondary"
                to="/onboarding/home"
                className="mt-auto mb-10 self-start"
              >
                {l10n.getString('onboarding-previous_step')}
              </Button>
            )}
          </div>
          <div
            className={classNames(
              'flex flex-col gap-3 p-10 rounded-xl max-w-sm sentry-mask',
              !state.alonePage && 'bg-background-70',
              state.alonePage && 'bg-background-60'
            )}
          >
            <Localized
              id="onboarding-wifi_creds-ssid"
              attrs={{ placeholder: true, label: true }}
            >
              <Input
                control={control}
                rules={{ required: true }}
                name="ssid"
                type="text"
                label="SSID"
                placeholder="ssid"
                variant="secondary"
              />
            </Localized>
            <Localized
              id="onboarding-wifi_creds-password"
              attrs={{ placeholder: true, label: true }}
            >
              <Input
                control={control}
                rules={{
                  validate: {
                    validPassword: (v: string | undefined) =>
                      v === undefined ||
                      v.length === 0 ||
                      new Blob([v]).size >= 8,
                  },
                }}
                name="password"
                type="password"
                label="Password"
                placeholder="password"
                variant="secondary"
              />
            </Localized>
            <div className="flex flex-row gap-3">
              <Button
                variant="secondary"
                className={state.alonePage ? 'opacity-0' : ''}
                to={
                  isRestCalibration
                    ? '/onboarding/calibration-tutorial'
                    : '/onboarding/assign-tutorial'
                }
              >
                {l10n.getString('onboarding-wifi_creds-skip')}
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={!formState.isValid}
              >
                {l10n.getString('onboarding-wifi_creds-submit')}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </form>
  );
}
