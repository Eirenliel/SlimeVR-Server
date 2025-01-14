import { useWebsocketAPI } from '@/hooks/websocket-api';
import { Button } from '@/components/commons/Button';
import { Typography } from '@/components/commons/Typography';
import { Localized, useLocalization } from '@fluent/react';
import { useMemo } from 'react';
import { useLocaleConfig } from '@/i18n/config';
import { useHeightContext } from '@/hooks/height';
import { useForm } from 'react-hook-form';
import {
  ChangeSettingsRequestT,
  ModelSettingsT,
  RpcMessage,
  SkeletonHeightT,
  StatusData,
  StatusSteamVRDisconnectedT,
} from 'solarxr-protocol';
import { NumberSelector } from '@/components/commons/NumberSelector';
import { MIN_HEIGHT } from '@/components/onboarding/pages/body-proportions/ProportionsChoose';
import { WarningBox } from '@/components/commons/TipBox';
import { useStatusContext } from '@/hooks/status-system';

interface HeightForm {
  height: number;
}

export function ManualHeightStep({
  nextStep,
  prevStep,
  variant,
}: {
  nextStep: () => void;
  prevStep: () => void;
  variant: 'onboarding' | 'alone';
}) {
  const { l10n } = useLocalization();
  const { hmdHeight, setHmdHeight } = useHeightContext();
  const { control, handleSubmit } = useForm<HeightForm>({
    defaultValues: { height: 1.5 },
  });
  const { sendRPCPacket } = useWebsocketAPI();
  const { currentLocales } = useLocaleConfig();
  const { statuses } = useStatusContext();

  const missingSteamConnection = useMemo(
    () =>
      Object.values(statuses).some(
        (x) =>
          x.dataType === StatusData.StatusSteamVRDisconnected &&
          (x.data as StatusSteamVRDisconnectedT).bridgeSettingsName ===
            'steamvr'
      ),
    [statuses]
  );

  const mFormat = useMemo(
    () =>
      new Intl.NumberFormat(currentLocales, {
        style: 'unit',
        unit: 'meter',
        maximumFractionDigits: 2,
      }),
    [currentLocales]
  );

  handleSubmit((values) => {
    setHmdHeight(values.height);
  });

  return (
    <>
      <div className="flex flex-col flex-grow">
        <div className="flex gap-2 flex-grow">
          <div className="flex flex-grow flex-col gap-4">
            <Typography variant="main-title" bold>
              {l10n.getString(
                'onboarding-scaled_proportions-manual_height-title'
              )}
            </Typography>
            <div>
              <Typography color="secondary">
                {l10n.getString(
                  'onboarding-scaled_proportions-manual_height-description'
                )}
              </Typography>
              {/* <Localized
                id="onboarding-scaled_proportions-manual_height-warning"
                elems={{ u: <span className="underline"></span> }}
              >
                <Typography color="secondary" bold>
                  Input your height manually!
                </Typography>
              </Localized> */}
              {missingSteamConnection && (
                <div className="flex flex-row items-center mt-2 gap-2 mobile:flex-col">
                  <Localized
                    id="onboarding-scaled_proportions-manual_height-missing_steamvr"
                    elems={{ b: <b></b> }}
                    // TODO: Add link to docs!
                  >
                    <WarningBox>You don't have SteamVR connected!</WarningBox>
                  </Localized>
                </div>
              )}
            </div>
            <form className="flex flex-col self-center items-center justify-center">
              <NumberSelector
                control={control}
                name="height"
                label={l10n.getString(
                  'onboarding-scaled_proportions-manual_height-height'
                )}
                valueLabelFormat={(value) =>
                  isNaN(value)
                    ? l10n.getString(
                        'onboarding-scaled_proportions-manual_height-unknown'
                      )
                    : mFormat.format(value)
                }
                min={MIN_HEIGHT}
                max={4}
                step={0.01}
                showButtonWithNumber
                doubleStep={0.1}
              />
            </form>
          </div>
        </div>

        <div className="flex gap-3 mobile:justify-between">
          <Button
            variant={variant === 'onboarding' ? 'secondary' : 'tertiary'}
            onClick={prevStep}
          >
            {l10n.getString('onboarding-automatic_proportions-prev_step')}
          </Button>
          <Button
            variant="primary"
            onClick={() => {
              const settingsRequest = new ChangeSettingsRequestT();
              settingsRequest.modelSettings = new ModelSettingsT(
                null,
                null,
                null,
                new SkeletonHeightT(hmdHeight, 0)
              );
              sendRPCPacket(RpcMessage.ChangeSettingsRequest, settingsRequest);
              nextStep();
            }}
          >
            {l10n.getString(
              'onboarding-scaled_proportions-manual_height-next_step'
            )}
          </Button>
        </div>
      </div>
    </>
  );
}
