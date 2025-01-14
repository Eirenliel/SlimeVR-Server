import { useLocalization } from '@fluent/react';
import { AutoboneContextC, useProvideAutobone } from '@/hooks/autobone';
import { useOnboarding } from '@/hooks/onboarding';
import { Typography } from '@/components/commons/Typography';
import { StepperSlider } from '@/components/onboarding/StepperSlider';
import { DoneStep } from './autobone-steps/Done';
import { RequirementsStep } from './autobone-steps/Requirements';
import { PutTrackersOnStep } from './autobone-steps/PutTrackersOn';
import { Recording } from './autobone-steps/Recording';
import { StartRecording } from './autobone-steps/StartRecording';
import { VerifyResultsStep } from './autobone-steps/VerifyResults';
import { CheckHeightStep } from './autobone-steps/CheckHeight';
import { PreparationStep } from './autobone-steps/Preparation';
import { HeightContextC, useProvideHeightContext } from '@/hooks/height';
import { CheckFloorHeightStep } from './autobone-steps/CheckFloorHeight';

export function AutomaticProportionsPage() {
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const context = useProvideAutobone();
  const heightContext = useProvideHeightContext();

  applyProgress(0.9);

  return (
    <AutoboneContextC.Provider value={context}>
      <HeightContextC.Provider value={heightContext}>
        <div className="flex flex-col gap-5 h-full items-center w-full xs:justify-center overflow-y-auto overflow-x-hidden relative px-4 pb-4">
          <div className="flex flex-col w-full xs:h-full xs:justify-center max-w-3xl gap-5">
            <div className="flex flex-col max-w-lg gap-3">
              <Typography variant="main-title">
                {l10n.getString('onboarding-automatic_proportions-title')}
              </Typography>
              <div>
                <Typography color="secondary">
                  {l10n.getString(
                    'onboarding-automatic_proportions-description'
                  )}
                </Typography>
              </div>
            </div>
            <div className="flex">
              <StepperSlider
                variant={state.alonePage ? 'alone' : 'onboarding'}
                steps={[
                  { type: 'numbered', component: PutTrackersOnStep },
                  { type: 'numbered', component: RequirementsStep },
                  { type: 'numbered', component: CheckHeightStep },
                  { type: 'numbered', component: CheckFloorHeightStep },
                  { type: 'numbered', component: PreparationStep },
                  { type: 'numbered', component: StartRecording },
                  { type: 'fullsize', component: Recording },
                  { type: 'numbered', component: VerifyResultsStep },
                  { type: 'fullsize', component: DoneStep },
                ]}
              ></StepperSlider>
            </div>
          </div>
        </div>
      </HeightContextC.Provider>
    </AutoboneContextC.Provider>
  );
}
