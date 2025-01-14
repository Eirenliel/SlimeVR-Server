import classNames from 'classnames';
import { ProcessStatus, useAutobone } from '@/hooks/autobone';
import { Button } from '@/components/commons/Button';
import { Typography } from '@/components/commons/Typography';
import { useLocalization } from '@fluent/react';

export function VerifyResultsStep({
  nextStep,
  prevStep,
  variant,
}: {
  nextStep: () => void;
  prevStep: () => void;
  variant: 'onboarding' | 'alone';
}) {
  const { l10n } = useLocalization();
  const {
    startRecording,
    hasCalibration,
    bodyParts,
    hasRecording,
    applyProcessing,
  } = useAutobone();

  const apply = () => {
    applyProcessing();
    nextStep();
  };

  const redo = () => {
    startRecording();
    prevStep();
  };

  return (
    <>
      <div className="flex flex-col flex-grow justify-between gap-2">
        <div className="flex flex-col gap-1 max-w-sm">
          <Typography variant="main-title" bold>
            {l10n.getString(
              'onboarding-automatic_proportions-verify_results-title'
            )}
          </Typography>
          <div>
            <Typography color="secondary">
              {l10n.getString(
                'onboarding-automatic_proportions-verify_results-description'
              )}
            </Typography>
          </div>
        </div>
        <div className="flex w-full items-center flex-col">
          <div className="flex flex-col pt-1 gap-2 justify-center w-full max-w-xs">
            <Typography bold>
              {l10n.getString(
                'onboarding-automatic_proportions-verify_results-results'
              )}
            </Typography>
            <div
              className={classNames(
                'flex flex-col  w-full p-4 rounded-md gap-2',
                variant === 'onboarding' && 'bg-background-60',
                variant === 'alone' && 'bg-background-50'
              )}
            >
              {bodyParts?.map(({ bone, label, value }) => (
                <div className="flex justify-between" key={bone}>
                  <Typography color="secondary">{label}</Typography>
                  <Typography bold sentryMask>
                    {(value * 100).toFixed(2)} CM
                  </Typography>
                </div>
              ))}
              {hasCalibration === ProcessStatus.PENDING &&
                hasRecording === ProcessStatus.FULFILLED && (
                  <Typography>
                    {l10n.getString(
                      'onboarding-automatic-proportions-verify-results-processing'
                    )}
                  </Typography>
                )}
            </div>
          </div>
        </div>
        <div className="flex gap-2">
          <Button
            variant={variant === 'onboarding' ? 'secondary' : 'tertiary'}
            onClick={redo}
          >
            {l10n.getString(
              'onboarding-automatic_proportions-verify_results-redo'
            )}
          </Button>
          <Button variant="primary" onClick={apply}>
            {l10n.getString(
              'onboarding-automatic_proportions-verify_results-confirm'
            )}
          </Button>
        </div>
      </div>
    </>
  );
}
