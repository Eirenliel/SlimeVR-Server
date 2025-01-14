import { useBreakpoint } from '@/hooks/breakpoint';
import classNames from 'classnames';
import {
  ReactNode,
  useRef,
  useState,
  ReactElement,
  useLayoutEffect,
  MutableRefObject,
  useMemo,
} from 'react';
import { createPortal } from 'react-dom';
import { Typography } from './Typography';
import { CloseIcon } from './icon/CloseIcon';

interface TooltipProps {
  content: ReactNode;
  children: ReactElement;
  preferedDirection: 'top' | 'left' | 'right' | 'bottom';
  mode?: 'corner' | 'center';
}

interface TooltipPos {
  left: number;
  top: number;
  width: number;
  height?: number;
}

type Rect = {
  left: number;
  top: number;
  width: number;
  height: number;
};

function overlapArea(rect1: Rect, rect2: Rect) {
  // Find the overlap in the x direction (width)
  const overlapWidth = Math.max(
    0,
    Math.min(rect1.left + rect1.width, rect2.left + rect2.width) -
      Math.max(rect1.left, rect2.left)
  );

  // Find the overlap in the y direction (height)
  const overlapHeight = Math.max(
    0,
    Math.min(rect1.top + rect1.height, rect2.top + rect2.height) -
      Math.max(rect1.top, rect2.top)
  );

  // If there is an overlap, return the area; otherwise, return 0
  return overlapWidth * overlapHeight;
}

function isNotInside(rect1: Rect, rect2: Rect) {
  // Check if rect1 is not inside rect2 or rect2 is not inside rect1
  const rect1InsideRect2 =
    rect1.left >= rect2.left &&
    rect1.left + rect1.width <= rect2.left + rect2.width &&
    rect1.top >= rect2.top &&
    rect1.top + rect1.height <= rect2.top + rect2.height;

  const rect2InsideRect1 =
    rect2.left >= rect1.left &&
    rect2.left + rect2.width <= rect1.left + rect1.width &&
    rect2.top >= rect1.top &&
    rect2.top + rect2.height <= rect1.top + rect1.height;

  // If neither is inside the other, return true
  return !(rect1InsideRect2 || rect2InsideRect1);
}

const clamp = (v: number, min: number, max: number) =>
  Math.min(max, Math.max(min, v));

const getFloatingTooltipPosition = (
  preferedDirection: TooltipProps['preferedDirection'],
  mode: TooltipProps['mode'],
  childrenRect: DOMRect,
  tooltipRect: DOMRect
) => {
  const spacing = 10;

  const getPosition = (
    direction: TooltipProps['preferedDirection']
  ): TooltipPos => {
    switch (direction) {
      case 'top':
        return {
          top: childrenRect.y - tooltipRect.height - spacing,
          left:
            childrenRect.x +
            (mode === 'center'
              ? childrenRect.width / 2 - tooltipRect.width / 2
              : 0),
          width: tooltipRect.width,
        };
      case 'left':
        return {
          top:
            childrenRect.y +
            (mode === 'center'
              ? childrenRect.height / 2 - tooltipRect.height / 2
              : 0),
          left: childrenRect.x - tooltipRect.width - spacing,
          width: tooltipRect.width,
        };
      case 'right':
        return {
          top:
            childrenRect.y +
            (mode === 'center'
              ? childrenRect.height / 2 - tooltipRect.height / 2
              : 0),
          left: childrenRect.x + childrenRect.width + spacing,
          width: tooltipRect.width,
        };
      case 'bottom':
        return {
          top: childrenRect.y + childrenRect.height + spacing,
          left:
            childrenRect.x +
            (mode === 'center'
              ? childrenRect.width / 2 - tooltipRect.width / 2
              : 0),
          width: tooltipRect.width,
        };
    }
  };

  const windowRect = document.body.getBoundingClientRect();

  const pos = getPosition(preferedDirection);
  if (isNotInside({ ...pos, height: tooltipRect.height }, windowRect)) {
    const [firstPos] = ['left', 'top', 'right', 'bottom']
      .map((dir) => ({
        dir,
        area: getPosition(dir as TooltipProps['preferedDirection']),
      }))
      .toSorted(
        (a, b) =>
          overlapArea({ ...b.area, height: tooltipRect.height }, windowRect) -
          overlapArea({ ...a.area, height: tooltipRect.height }, windowRect)
      );

    if (
      isNotInside({ ...firstPos.area, height: tooltipRect.height }, windowRect)
    ) {
      switch (firstPos.dir) {
        case 'top':
          return {
            top: clamp(firstPos.area.top, spacing, childrenRect.y - spacing),
            left: clamp(firstPos.area.left, spacing, childrenRect.x - spacing),
            width: clamp(
              firstPos.area.width,
              0,
              windowRect.width - spacing * 2
            ),
            height: clamp(
              tooltipRect.height,
              spacing,
              childrenRect.y - spacing * 2
            ),
          };
        case 'left':
          return {
            top: clamp(firstPos.area.top, spacing, windowRect.height - spacing),
            left: clamp(firstPos.area.left, spacing, childrenRect.x - spacing),
            width: clamp(firstPos.area.width, 0, childrenRect.x - spacing * 2),
            height: clamp(
              tooltipRect.height,
              spacing,
              windowRect.height - spacing * 2
            ),
          };
        case 'right':
          return {
            top: clamp(firstPos.area.top, spacing, windowRect.height - spacing),
            left: clamp(
              firstPos.area.left,
              childrenRect.x + childrenRect.width + spacing,
              windowRect.width - spacing
            ),
            width: clamp(
              firstPos.area.width,
              0,
              windowRect.width - spacing - firstPos.area.left
            ),
            height: clamp(
              tooltipRect.height,
              spacing,
              windowRect.height - spacing * 2
            ),
          };
        case 'bottom':
          return {
            top: clamp(
              firstPos.area.top,
              childrenRect.y + childrenRect.height + spacing,
              windowRect.height - childrenRect.y + childrenRect.height + spacing
            ),
            left: clamp(firstPos.area.left, spacing, windowRect.width),
            width: clamp(
              firstPos.area.width,
              0,
              windowRect.width - spacing * 2
            ),
            height: clamp(
              tooltipRect.height,
              firstPos.area.top + childrenRect.height + spacing,
              windowRect.height -
                (childrenRect.y + childrenRect.height) -
                spacing * 2
            ),
          };
      }
    }

    return firstPos.area;
  }
  return pos;
};

export function FloatingTooltip({
  childRef,
  preferedDirection,
  mode,
  children,
}: {
  childRef: MutableRefObject<HTMLDivElement | null>;
  children: ReactNode;
} & Pick<TooltipProps, 'mode' | 'preferedDirection'>) {
  const tooltipRef = useRef<HTMLDivElement | null>(null);
  const [tooltipStyle, setTooltipStyle] = useState<TooltipPos | undefined>();

  const onMouseEnter = () => {
    if (!childRef.current || !tooltipRef.current)
      throw new Error('invalid state');

    const childrenRect = childRef.current.children[0].getBoundingClientRect();
    const tooltipRect = tooltipRef.current.getBoundingClientRect();

    setTooltipStyle(
      getFloatingTooltipPosition(
        preferedDirection,
        mode,
        childrenRect,
        tooltipRect
      )
    );
  };

  const onMouseLeave = () => {
    if (!childRef.current || !tooltipRef.current)
      throw new Error('invalid state');
    setTooltipStyle(undefined);
  };

  const onResize = () => {
    setTooltipStyle(undefined);
  };

  useLayoutEffect(() => {
    if (childRef.current && childRef.current.children[0]) {
      const elem = childRef.current.children[0] as HTMLElement;
      elem.addEventListener('mouseenter', onMouseEnter);
      elem.addEventListener('mouseleave', onMouseLeave);

      return () => {
        elem.removeEventListener('mouseenter', onMouseEnter);
        elem.removeEventListener('mouseleave', onMouseLeave);
      };
    }
  }, []);

  useLayoutEffect(() => {
    window.addEventListener('resize', onResize);
    return () => {
      window.removeEventListener('resize', onResize);
    };
  }, []);

  const style = useMemo(() => {
    if (!tooltipStyle) return { opacity: 0, top: 0 };
    return tooltipStyle;
  }, [tooltipStyle]);

  return (
    <div
      className={classNames('fixed z-50 pointer-events-none')}
      ref={tooltipRef}
      style={style}
    >
      <div
        className="bg-background-90 rounded-md p-2 text-background-10 overflow-auto"
        style={style}
      >
        {children}
      </div>
    </div>
  );
}

const TOOLTIP_DELAY = 500;

interface DrawerStyle {
  bottom: number;
}

export function DrawerTooltip({
  children,
  childRef,
}: {
  children: ReactNode;
  childRef: MutableRefObject<HTMLDivElement | null>;
}) {
  const touchTimestamp = useRef<number>(0);
  const touchTimeout = useRef<number>(0);
  const drawerRef = useRef<HTMLDivElement | null>(null);
  const [drawerStyle, setDrawerStyle] = useState<DrawerStyle | undefined>(
    undefined
  );

  const touchStart = () => {
    if (childRef.current && childRef.current.children[0]) {
      touchTimestamp.current = Date.now();
      const elem = childRef.current.children[0] as HTMLElement;
      elem.classList.add(classNames('transition-all'));
      elem.classList.add(classNames('animate-pulse'));
      elem.classList.add(classNames('scale-[110%]'));
      elem.classList.add(classNames('duration-500'));
      touchTimeout.current = setTimeout(() => {
        open();
      }, TOOLTIP_DELAY);
    }
  };

  const clearEffect = () => {
    if (childRef.current && childRef.current.children[0]) {
      const elem = childRef.current.children[0] as HTMLElement;
      elem.classList.remove(classNames('animate-pulse'));
      elem.classList.remove(classNames('scale-[110%]'));
      elem.classList.remove(classNames('duration-500'));
    }
  };

  const touchEnd = (e: MouseEvent | TouchEvent) => {
    if (Date.now() - touchTimestamp.current > TOOLTIP_DELAY) {
      // open drawer
      e.preventDefault(); // cancel the click event
      clearTimeout(touchTimeout.current);

      open();
    }
  };

  const open = () => {
    if (drawerStyle) return;
    clearEffect();

    if (!drawerRef.current) throw new Error('invalid state');

    setDrawerStyle({ bottom: 0 });
  };

  const close = () => {
    setDrawerStyle(undefined);
  };

  useLayoutEffect(() => {
    if (childRef.current && childRef.current.children[0]) {
      const elem = childRef.current.children[0] as HTMLElement;

      elem.addEventListener('mousedown', touchStart); // for debug on desktop
      elem.addEventListener('mouseup', touchEnd); // for debug on desktop
      elem.addEventListener('click', touchEnd);
      elem.addEventListener('touchstart', touchStart);
      elem.addEventListener('touchend', touchEnd);

      return () => {
        elem.removeEventListener('mousedown', touchStart); // for debug on desktop
        elem.removeEventListener('mouseup', touchEnd); // for debug on desktop
        elem.removeEventListener('touchstart', touchStart);
        elem.removeEventListener('touchend', touchEnd);
        clearTimeout(touchTimeout.current);
      };
    }
  }, []);

  return (
    <>
      <div
        className="fixed top-[44px] rounded-t-lg h-screen z-50 w-full bg-background-90 opacity-50"
        onClick={() => close()}
        style={{
          opacity: drawerStyle ? 0.5 : 0,
          pointerEvents: drawerStyle ? 'all' : 'none',
        }}
      ></div>
      <div
        className={classNames(
          'fixed z-50 w-full text-background-10 max-h-full -bottom-full transition-all overflow-clip'
        )}
        style={drawerStyle}
      >
        <div
          className="bg-background-60 rounded-t-lg border-background-40 border-t-2"
          ref={drawerRef}
        >
          <div className="h-12 rounded-t-lg relative flex justify-center items-center">
            <Typography variant="section-title" textAlign="text-center">
              Pro tip
            </Typography>
            <button
              className="absolute right-4 top-3 h-6 w-6 bg-background-70 rounded-full flex justify-center items-center"
              onClick={() => close()}
            >
              <CloseIcon size={20} className="stroke-white"></CloseIcon>
            </button>
          </div>
          <div
            className="p-2 overflow-y-auto"
            style={{ maxHeight: 'calc(100vh - 49px - 44px)' }}
          >
            {children}
          </div>
        </div>
      </div>
    </>
  );
}

export function Tooltip({
  content,
  children,
  preferedDirection,
  mode = 'center',
}: TooltipProps) {
  const childRef = useRef<HTMLDivElement | null>(null);
  const { isMobile } = useBreakpoint('mobile');

  return (
    <>
      <div className="contents" ref={childRef}>
        {children}
      </div>
      {createPortal(
        isMobile ? (
          <DrawerTooltip childRef={childRef}>{content}</DrawerTooltip>
        ) : (
          <FloatingTooltip
            preferedDirection={preferedDirection}
            mode={mode}
            childRef={childRef}
          >
            {content}
          </FloatingTooltip>
        ),
        document.body
      )}
    </>
  );
}
