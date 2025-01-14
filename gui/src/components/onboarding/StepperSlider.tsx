import classNames from 'classnames';
import {
  FC,
  MouseEventHandler,
  ReactNode,
  useEffect,
  useRef,
  useState,
} from 'react';
import { useElemSize } from '@/hooks/layout';
import { CheckIcon } from '@/components/commons/icon/CheckIcon';
import { Typography } from '@/components/commons/Typography';
import { useDebouncedEffect } from '@/hooks/timeout';

type StepComponentType = FC<{
  nextStep: () => void;
  prevStep: () => void;
  resetSteps: () => void;
  variant: 'alone' | 'onboarding';
  active: boolean;
}>;
export type Step = {
  type: 'numbered' | 'fullsize';
  component: StepComponentType;
};

export function StepContainer({
  children,
  width,
  active,
  type,
  step,
  variant,
}: {
  type: 'numbered' | 'fullsize';
  variant: 'alone' | 'onboarding';
  children: ReactNode;
  width: number;
  active: boolean;
  step: number;
}) {
  return (
    <div
      className={classNames(
        'step-container transition-transform duration-500 relative w-full xs:p-8 mobile:p-2 rounded-lg flex gap-4 h-full',
        !active && 'opacity-40 pointer-events-none',
        variant === 'onboarding' && 'bg-background-70',
        variant === 'alone' && 'bg-background-60'
      )}
      style={{
        minWidth: width,
        width,
      }}
    >
      {type === 'numbered' && (
        <div className="xs:flex xs:flex-col mobile:absolute mobile:-top-3 mobile:-right-4">
          <div className="bg-accent-background-40 rounded-full h-8 w-8 flex flex-col items-center justify-center">
            <Typography variant="section-title" bold>
              {step + 1}
            </Typography>
          </div>
        </div>
      )}
      {children}
    </div>
  );
}

export function StepDot({
  active,
  done,
  onClick,
}: {
  active?: boolean;
  done?: boolean;
  onClick?: MouseEventHandler<HTMLDivElement>;
}) {
  return (
    <div
      className={classNames(
        'flex h-4 w-4 rounded-full justify-center items-center fill-background-10 transition-all',
        active || done ? 'bg-accent-background-20 ' : 'bg-background-60'
      )}
      onClick={onClick}
    >
      {active && (
        <div className="flex h-2 w-2 rounded-full bg-background-10"></div>
      )}
      {done && <CheckIcon />}
    </div>
  );
}

export function StepperSlider({
  variant,
  steps,
  back,
  forward,
}: {
  variant: 'alone' | 'onboarding';
  steps: Step[];
  /**
   * Ran when step is 0 and `prevStep` is executed
   */
  back?: () => void;
  /**
   * Ran when step is `steps.length - 1` and nextStep is executed
   */
  forward?: () => void;
}) {
  const ref = useRef<HTMLDivElement | null>(null);
  const { width } = useElemSize(ref);
  const [shouldAnimate, setShouldAnimate] = useState(true);
  const [step, setStep] = useState(0);

  useEffect(() => {
    setStep((x) => Math.min(x, steps.length - 1));
  }, [steps.length]);

  const nextStep = () => {
    if (step + 1 === steps.length) {
      forward?.();
      return;
    }
    setStep(step + 1);
  };

  const prevStep = () => {
    if (step - 1 < 0) {
      back?.();
      return;
    }
    setStep(step - 1);
  };

  const resetSteps = () => {
    setStep(0);
  };

  useEffect(() => {
    setShouldAnimate(false);
  }, [width]);

  // Make it so if you resize the window it wont try to move the slide with an animation
  useDebouncedEffect(
    () => {
      setShouldAnimate(true);
    },
    [width],
    500
  );

  return (
    <div className="w-full flex flex-col gap-4">
      <div className="w-full flex" ref={ref}>
        <div
          className={classNames('flex gap-8', {
            'transition-transform duration-500 ': shouldAnimate,
          })}
          style={{ transform: `translateX(-${(width + 32) * step}px)` }}
        >
          {steps.map(({ type, component: StepComponent }, index) => (
            <StepContainer
              variant={variant}
              key={index}
              type={type}
              width={width}
              active={index === step}
              step={index}
            >
              <StepComponent
                variant={variant}
                nextStep={nextStep}
                prevStep={prevStep}
                resetSteps={resetSteps}
                active={index === step}
              />
            </StepContainer>
          ))}
        </div>
      </div>
      <div className="flex justify-center items-center gap-2">
        {Array.from({ length: steps.length }).map((_, index) => (
          <div key={index} className="flex items-center gap-2">
            {index !== 0 && (
              <div className="w-5 h-1 bg-background-50 rounded-full"></div>
            )}
            <StepDot
              active={index === step}
              done={index < step}
              // onClick={() => setStep(index)}
            />
          </div>
        ))}
      </div>
    </div>
  );
}
