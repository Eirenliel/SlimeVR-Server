import { useLocalization } from '@fluent/react';
import classNames from 'classnames';
import { MouseEventHandler, ReactNode, useEffect } from 'react';
import {
  LabelType,
  ProportionChangeType,
  useManualProportions,
} from '../../../../hooks/manual-proportions';
import { useLocaleConfig } from '../../../../i18n/config';
import { Typography } from '../../../commons/Typography';

function IncrementButton({
  children,
  onClick,
}: {
  children: ReactNode;
  onClick?: MouseEventHandler<HTMLDivElement>;
}) {
  return (
    <div
      onClick={onClick}
      className={classNames(
        'p-3  rounded-lg w-16 h-16 flex flex-col justify-center items-center bg-background-60 hover:bg-opacity-50'
      )}
    >
      <Typography variant="main-title" bold>
        {children}
      </Typography>
    </div>
  );
}

export function BodyProportions({
  precise,
  type,
  variant = 'onboarding',
}: {
  precise: boolean;
  type: 'linear' | 'ratio';
  variant: 'onboarding' | 'alone';
}) {
  const [bodyParts, ratioMode, currentSelection, dispatch, setRatioMode] =
    useManualProportions();
  const { currentLocales } = useLocaleConfig();
  const { l10n } = useLocalization();
  const cmFormat = Intl.NumberFormat(currentLocales, {
    style: 'unit',
    unit: 'centimeter',
    maximumFractionDigits: 1,
  });
  const configFormat = Intl.NumberFormat(currentLocales, {
    signDisplay: 'always',
    maximumFractionDigits: 1,
  });
  const percentageFormat = Intl.NumberFormat(currentLocales, {
    style: 'percent',
    maximumFractionDigits: 2,
  });

  useEffect(() => {
    if (type === 'linear') {
      setRatioMode(false);
    } else {
      setRatioMode(true);
    }
  }, [type]);

  return (
    <div className="relative w-full">
      <div
        className={classNames(
          'flex flex-col overflow-y-scroll overflow-x-hidden max-h-[450px] w-full px-1',
          'gap-3 pb-16',
          variant === 'onboarding' && 'gradient-mask-b-90',
          variant === 'alone' && 'gradient-mask-b-80'
        )}
      >
        <>
          {bodyParts.map(({ label, type, value, ...props }) => (
            <div className="flex" key={label}>
              <div
                className={classNames(
                  'flex gap-2 transition-opacity duration-300',
                  currentSelection.label !== label &&
                    'opacity-0 pointer-events-none'
                )}
              >
                {!precise && (
                  <IncrementButton
                    onClick={() =>
                      dispatch({
                        type:
                          type === LabelType.GroupPart
                            ? ProportionChangeType.Ratio
                            : ProportionChangeType.Linear,
                        value: -5,
                      })
                    }
                  >
                    {configFormat.format(-5)}
                  </IncrementButton>
                )}
                <IncrementButton
                  onClick={() =>
                    dispatch({
                      type:
                        type === LabelType.GroupPart
                          ? ProportionChangeType.Ratio
                          : ProportionChangeType.Linear,
                      value: -1,
                    })
                  }
                >
                  {configFormat.format(-1)}
                </IncrementButton>
                {precise && (
                  <IncrementButton
                    onClick={() =>
                      dispatch({
                        type:
                          type === LabelType.GroupPart
                            ? ProportionChangeType.Ratio
                            : ProportionChangeType.Linear,
                        value: -0.5,
                      })
                    }
                  >
                    {configFormat.format(-0.5)}
                  </IncrementButton>
                )}
              </div>
              <div
                className="flex flex-grow flex-col px-2"
                onClick={() => {
                  switch (type) {
                    case LabelType.Bone: {
                      if (!('bone' in props)) throw 'unreachable';
                      dispatch({
                        ...props,
                        label,
                        value,
                        type: ProportionChangeType.Bone,
                      });
                      break;
                    }
                    case LabelType.Group: {
                      if (!('bones' in props)) throw 'unreachable';
                      dispatch({
                        ...props,
                        label,
                        value,
                        type: ProportionChangeType.Group,
                      });
                      break;
                    }
                    case LabelType.GroupPart: {
                      if (!('index' in props)) throw 'unreachable';
                      dispatch({
                        ...props,
                        label,
                        value,
                        type: ProportionChangeType.Group,
                      });
                    }
                  }
                }}
              >
                <div
                  key={label}
                  className={classNames(
                    'p-3  rounded-lg h-16 flex w-full items-center justify-between px-6 transition-colors duration-300 bg-background-60',
                    (currentSelection.label === label && 'opacity-100') ||
                      'opacity-50'
                  )}
                >
                  <Typography variant="section-title" bold>
                    {l10n.getString(label)}
                  </Typography>
                  <Typography variant="main-title" bold>
                    {type === LabelType.GroupPart
                      ? percentageFormat.format(value)
                      : cmFormat.format(value * 100)}
                  </Typography>
                </div>
              </div>
              <div
                className={classNames(
                  'flex gap-2 transition-opacity duration-300',
                  currentSelection.label !== label &&
                    'opacity-0 pointer-events-none'
                )}
              >
                {precise && (
                  <IncrementButton
                    onClick={() =>
                      dispatch({
                        type:
                          type === LabelType.GroupPart
                            ? ProportionChangeType.Ratio
                            : ProportionChangeType.Linear,
                        value: 0.5,
                      })
                    }
                  >
                    {configFormat.format(+0.5)}
                  </IncrementButton>
                )}
                <IncrementButton
                  onClick={() =>
                    dispatch({
                      type:
                        type === LabelType.GroupPart
                          ? ProportionChangeType.Ratio
                          : ProportionChangeType.Linear,
                      value: 1,
                    })
                  }
                >
                  {configFormat.format(+1)}
                </IncrementButton>
                {!precise && (
                  <IncrementButton
                    onClick={() =>
                      dispatch({
                        type:
                          type === LabelType.GroupPart
                            ? ProportionChangeType.Ratio
                            : ProportionChangeType.Linear,
                        value: 5,
                      })
                    }
                  >
                    {configFormat.format(+5)}
                  </IncrementButton>
                )}
              </div>
            </div>
          ))}
        </>
      </div>
    </div>
  );
}
