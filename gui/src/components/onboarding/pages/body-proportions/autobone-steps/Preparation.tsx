import { ResetType } from 'solarxr-protocol';
import { Button } from '@/components/commons/Button';
import { Typography } from '@/components/commons/Typography';
import { ResetButton } from '@/components/home/ResetButton';
import { useLocalization } from '@fluent/react';
import { useBreakpoint } from '@/hooks/breakpoint';

export function PreparationStep({
  nextStep,
  prevStep,
  variant,
}: {
  nextStep: () => void;
  prevStep: () => void;
  variant: 'onboarding' | 'alone';
}) {
  const { isMobile } = useBreakpoint('mobile');
  const { l10n } = useLocalization();

  return (
    <div className="flex mobile:flex-col items-center w-full">
      <div className="flex flex-col flex-grow justify-between">
        <div className="flex flex-col gap-4 max-w-sm">
          <Typography variant="main-title" bold>
            {l10n.getString('onboarding-automatic_mounting-preparation-title')}
          </Typography>
          <div>
            <Typography color="secondary">
              {l10n.getString(
                'onboarding-automatic_mounting-preparation-step-0'
              )}
            </Typography>
            <Typography color="secondary">
              {l10n.getString(
                'onboarding-automatic_mounting-preparation-step-1'
              )}
            </Typography>
          </div>
        </div>
        {isMobile && (
          <div className="flex flex-col pt-1 items-center fill-background-50 justify-center px-12">
            <img
              src="/images/reset-pose.webp"
              width={50}
              alt="Reset position"
            />
          </div>
        )}
        <div className="flex gap-3 mobile:justify-between">
          <Button
            variant={variant === 'onboarding' ? 'secondary' : 'tertiary'}
            onClick={prevStep}
          >
            {l10n.getString('onboarding-automatic_mounting-prev_step')}
          </Button>
          <ResetButton
            variant="small"
            type={ResetType.Full}
            onReseted={nextStep}
          ></ResetButton>
        </div>
      </div>
      {!isMobile && (
        <div className="flex flex-col pt-1 items-center fill-background-50 justify-center px-12">
          <img src="/images/reset-pose.webp" width={90} alt="Reset position" />
        </div>
      )}
    </div>
  );
}
