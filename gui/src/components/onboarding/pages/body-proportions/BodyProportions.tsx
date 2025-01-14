import { useLocalization } from '@fluent/react';
import classNames from 'classnames';
import {
  MouseEventHandler,
  ReactNode,
  useEffect,
  useRef,
  UIEvent,
  useMemo,
} from 'react';
import {
  LabelType,
  ProportionChangeType,
  useManualProportions,
} from '@/hooks/manual-proportions';
import { useLocaleConfig } from '@/i18n/config';
import { Typography } from '@/components/commons/Typography';
import {
  ArrowDownIcon,
  ArrowUpIcon,
} from '@/components/commons/icon/ArrowIcons';
import { useBreakpoint } from '@/hooks/breakpoint';
import { debounce } from '@/hooks/timeout';

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
        'p-3 rounded-lg xs:w-16 xs:h-16 mobile:w-10 flex flex-col justify-center items-center',
        'bg-background-60 hover:bg-opacity-50 active:bg-accent-background-30'
      )}
    >
      <Typography variant="mobile-title" bold>
        {children}
      </Typography>
    </div>
  );
}

export function BodyProportions({
  precise,
  type,
  variant: _variant = 'onboarding',
}: {
  precise: boolean;
  type: 'linear' | 'ratio';
  variant: 'onboarding' | 'alone';
}) {
  const { bodyParts, dispatch, state, setRatioMode } = useManualProportions();
  const { l10n } = useLocalization();
  const { currentLocales } = useLocaleConfig();
  const { isTall } = useBreakpoint('tall');

  const offsetItems = isTall ? 2 : 1;
  const itemsToDisplay = offsetItems * 2 + 1;
  const itemHeight = 80;
  const scrollHeight = itemHeight * itemsToDisplay;

  const scrollerRef = useRef<HTMLDivElement | null>(null);

  const cmFormat = Intl.NumberFormat(currentLocales, {
    style: 'unit',
    unit: 'centimeter',
    maximumFractionDigits: 1,
  });
  const percentageFormat = new Intl.NumberFormat(currentLocales, {
    style: 'percent',
    maximumFractionDigits: 1,
  });
  const configFormat = Intl.NumberFormat(currentLocales, {
    signDisplay: 'always',
    maximumFractionDigits: 1,
  });

  useEffect(() => {
    if (type === 'linear') {
      setRatioMode(false);
    } else {
      setRatioMode(true);
    }
  }, [type]);

  useEffect(() => {
    if (scrollerRef.current && bodyParts.length > 0) {
      selectId(bodyParts[offsetItems].label);
    }
  }, [scrollerRef, bodyParts.length]);

  const handleUIEvent = (e: UIEvent<HTMLDivElement>) => {
    const target = e.target as HTMLDivElement;
    const itemHeight = target.offsetHeight / itemsToDisplay;
    const atSnappingPoint = target.scrollTop % itemHeight === 0;
    const index = Math.round(target.scrollTop / itemHeight);
    const elem = scrollerRef.current?.childNodes[
      index + offsetItems
    ] as HTMLDivElement;

    elem.scrollIntoView({ behavior: 'smooth', block: 'center' });

    if (atSnappingPoint) {
      const elem = scrollerRef.current?.childNodes[
        index + offsetItems
      ] as HTMLDivElement;
      const id = elem.getAttribute('itemid');

      if (id) selectNew(id);
    }
  };

  const moveToId = (id: string) => {
    if (!scrollerRef.current) return;
    const index = bodyParts.findIndex(({ label }) => label === id);
    scrollerRef.current.scrollTo({
      top: index * itemHeight,
      behavior: 'smooth',
    });
  };

  const clickPart = (id: string) => () => {
    moveToId(id);
    selectNew(id);
  };

  const selectId = (id: string) => {
    moveToId(id);
    if (id) selectNew(id);
  };

  const selectNew = (id: string) => {
    const part = bodyParts.find(({ label }) => label === id);
    if (!part) return;

    const { value: originalValue, label, type, ...props } = part;

    const value =
      'index' in props && props.index !== undefined
        ? props.bones[props.index].value
        : originalValue;

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
          index: undefined,
          parentLabel: label,
        });
        break;
      }
      case LabelType.GroupPart: {
        if (!('index' in props)) throw 'unreachable';
        dispatch({
          ...props,
          label,
          // If this isn't done, we are replacing total
          // with percentage value
          value: originalValue,
          type: ProportionChangeType.Group,
          index: props.index,
        });
      }
    }
  };

  const seletedLabel = useMemo(() => {
    return bodyParts.find(({ label }) => label === state.currentLabel);
  }, [state]);

  const move = (action: 'next' | 'prev') => {
    const elem = scrollerRef.current?.querySelector(
      `div[itemid=${state.currentLabel}]`
    );

    const moveId = (id: string) => {
      moveToId(id);
      selectNew(id);
    };

    if (action === 'prev') {
      const prevElem = elem?.previousSibling as HTMLDivElement;
      const prevId = prevElem.getAttribute('itemid');
      if (!prevId) return;
      moveId(prevId);
    }

    if (action === 'next') {
      const nextElem = elem?.nextSibling as HTMLDivElement;
      const nextId = nextElem.getAttribute('itemid');
      if (!nextId) return;
      moveId(nextId);
    }
  };

  return (
    (bodyParts.length > 0 && (
      <div className="flex w-full gap-3">
        <div className="flex items-center mobile:justify-center mobile:flex-col gap-2 my-2">
          {!precise && (
            <div className="mobile:order-2">
              <IncrementButton
                onClick={() =>
                  seletedLabel?.type === LabelType.GroupPart
                    ? dispatch({
                        type: ProportionChangeType.Ratio,
                        value: -0.05,
                      })
                    : dispatch({
                        type: ProportionChangeType.Linear,
                        value: -5,
                      })
                }
              >
                {configFormat.format(-5)}
              </IncrementButton>
            </div>
          )}
          <div className="mobile:order-1">
            <IncrementButton
              onClick={() =>
                seletedLabel?.type === LabelType.GroupPart
                  ? dispatch({
                      type: ProportionChangeType.Ratio,
                      value: -0.01,
                    })
                  : dispatch({
                      type: ProportionChangeType.Linear,
                      value: -1,
                    })
              }
            >
              {configFormat.format(-1)}
            </IncrementButton>
          </div>
          {precise && (
            <div className="mobile:order-2">
              <IncrementButton
                onClick={() =>
                  seletedLabel?.type === LabelType.GroupPart
                    ? dispatch({
                        type: ProportionChangeType.Ratio,
                        value: -0.005,
                      })
                    : dispatch({
                        type: ProportionChangeType.Linear,
                        value: -0.5,
                      })
                }
              >
                {configFormat.format(-0.5)}
              </IncrementButton>
            </div>
          )}
        </div>
        <div className="flex flex-grow flex-col">
          <div className="flex justify-center">
            <div
              onClick={() => move('prev')}
              className={classNames(
                'h-12 w-32 rounded-lg bg-background-60 flex flex-col justify-center',
                'items-center fill-background-10',
                (scrollerRef?.current?.scrollTop ?? 0 > 0)
                  ? 'opacity-100 active:bg-accent-background-30'
                  : 'opacity-50'
              )}
            >
              <ArrowUpIcon size={32}></ArrowUpIcon>
            </div>
          </div>
          <div
            ref={scrollerRef}
            onScroll={debounce(handleUIEvent, 150)} // Debounce at 150ms to match the animation speed and prevent snaping between two animations
            className={classNames(
              'flex-grow flex-col overflow-y-auto',
              'no-scrollbar'
            )}
            style={{ height: scrollHeight }}
          >
            {Array.from({ length: offsetItems }).map((_, index) => (
              <div style={{ height: itemHeight }} key={index}></div>
            ))}
            {bodyParts.map((part) => {
              const { label, value: originalValue, type, ...props } = part;
              const value =
                'index' in props && props.index !== undefined
                  ? props.bones[props.index].value
                  : originalValue;

              const selected = state.currentLabel === label;

              return (
                <div
                  key={label}
                  itemID={label}
                  onClick={clickPart(label)}
                  style={{ height: itemHeight }}
                  className="flex-col flex justify-center"
                >
                  <div
                    className={classNames(
                      'h-16 p-3 rounded-lg flex w-full items-center justify-between px-6 transition-colors',
                      'duration-300 bg-background-60',
                      (selected && 'opacity-100') ||
                        'opacity-50 active:bg-accent-background-30'
                    )}
                  >
                    <Typography variant="section-title" bold>
                      {l10n.getString(label)}
                    </Typography>
                    <Typography variant="mobile-title" bold sentryMask>
                      {type === LabelType.GroupPart
                        ? /* Make number rounding so it's based on .5 decimals */
                          percentageFormat.format(Math.round(value * 200) / 200)
                        : cmFormat.format(value * 100)}
                      {type === LabelType.GroupPart && (
                        <p className="text-standard">{`(${cmFormat.format(
                          value * originalValue * 100
                        )})`}</p>
                      )}
                    </Typography>
                  </div>
                </div>
              );
            })}
            {Array.from({ length: offsetItems }).map((_, index) => (
              <div
                className="h-20"
                style={{ height: itemHeight }}
                key={index}
              ></div>
            ))}
          </div>
          <div className="flex justify-center">
            <div
              onClick={() => move('next')}
              className={classNames(
                'h-12 w-32 rounded-lg bg-background-60 flex flex-col justify-center',
                'items-center fill-background-10',
                scrollerRef?.current?.scrollTop !==
                  (scrollerRef?.current?.scrollHeight ?? 0) -
                    (scrollerRef?.current?.offsetHeight ?? 0)
                  ? 'opacity-100 active:bg-accent-background-30'
                  : 'opacity-50'
              )}
            >
              <ArrowDownIcon size={32}></ArrowDownIcon>
            </div>
          </div>
        </div>

        <div className="flex items-center mobile:justify-center mobile:flex-col gap-2">
          {precise && (
            <div className="mobile:order-2">
              <IncrementButton
                onClick={() =>
                  seletedLabel?.type === LabelType.GroupPart
                    ? dispatch({
                        type: ProportionChangeType.Ratio,
                        value: 0.005,
                      })
                    : dispatch({
                        type: ProportionChangeType.Linear,
                        value: 0.5,
                      })
                }
              >
                {configFormat.format(+0.5)}
              </IncrementButton>
            </div>
          )}

          <div className="mobile:order-1">
            <IncrementButton
              onClick={() =>
                seletedLabel?.type === LabelType.GroupPart
                  ? dispatch({
                      type: ProportionChangeType.Ratio,
                      value: 0.01,
                    })
                  : dispatch({
                      type: ProportionChangeType.Linear,
                      value: 1,
                    })
              }
            >
              {configFormat.format(+1)}
            </IncrementButton>
          </div>
          {!precise && (
            <div className="mobile:order-2">
              <IncrementButton
                onClick={() =>
                  seletedLabel?.type === LabelType.GroupPart
                    ? dispatch({
                        type: ProportionChangeType.Ratio,
                        value: 0.05,
                      })
                    : dispatch({
                        type: ProportionChangeType.Linear,
                        value: 5,
                      })
                }
              >
                {configFormat.format(+5)}
              </IncrementButton>
            </div>
          )}
        </div>
      </div>
    )) || <></>
  );
}
