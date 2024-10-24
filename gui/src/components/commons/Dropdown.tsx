import classNames from 'classnames';
import { ReactNode, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { Control, Controller, UseControllerProps } from 'react-hook-form';
import { a11yClick } from '@/utils/a11y';
import { createPortal } from 'react-dom';
import { ArrowDownIcon } from './icon/ArrowIcons';

interface DropdownProps {
  direction?: DropdownDirection;
  variant?: 'primary' | 'secondary' | 'tertiary';
  alignment?: 'right' | 'left';
  display?: 'fit' | 'block';
  placeholder: string;
  control: Control<any>;
  name: string;
  items: DropdownItem[];
  maxHeight?: string | number;
  rules?: UseControllerProps<any>['rules'];
}

type DropdownItemsProps = Pick<
  DropdownProps,
  'direction' | 'variant' | 'alignment' | 'display' | 'items' | 'maxHeight'
> & {
  onSelectItem: (item: DropdownItem) => void;
  onBackdropClick: () => void;
  value: string;
  dropdownBounds: DOMRect;
};

export interface DropdownItem {
  label?: string;
  component?: ReactNode;
  value: string;
  fontName?: string;
}

export type DropdownDirection = 'up' | 'down';

export function DropdownItems({
  display,
  direction,
  variant,
  alignment,
  items,
  maxHeight,
  value,
  dropdownBounds,
  onSelectItem,
  onBackdropClick,
}: DropdownItemsProps) {
  const ref = useRef<HTMLDivElement | null>(null);
  const [itemBounds, setItemBounds] = useState<DOMRect>();

  const updateBounds = () => {
    if (!ref.current) return;
    setItemBounds(ref.current?.getBoundingClientRect());
  };

  useLayoutEffect(() => {
    updateBounds();
  }, []);

  const GAP = 8;

  return (
    <>
      <div
        className="z-[999] fixed top-0 w-full h-full"
        onClick={onBackdropClick}
      ></div>
      <div
        ref={ref}
        className={classNames(
          'z-[1000] fixed rounded shadow',
          'overflow-y-auto dropdown-scroll overflow-x-hidden text-background-10',
          variant == 'primary' && 'bg-background-60',
          variant == 'secondary' && 'bg-background-70',
          variant == 'tertiary' && 'bg-accent-background-30',
          itemBounds?.height == 0 && 'opacity-0' // Avoid flicker while the component find its position
        )}
        style={{
          maxHeight: maxHeight,
          left:
            alignment === 'left'
              ? dropdownBounds.left
              : dropdownBounds.left +
                dropdownBounds.width -
                (itemBounds?.width ?? 0),
          top:
            direction == 'down'
              ? dropdownBounds.bottom + GAP
              : dropdownBounds.top - (itemBounds?.height ?? 0) - GAP,
          minWidth: display === 'block' ? dropdownBounds.width : 'inherit',
        }}
      >
        <ul className="py-1 text-sm flex flex-col">
          {items.map((item) => (
            <li
              style={item.fontName ? { fontFamily: item.fontName } : {}}
              className={classNames(
                'py-2 px-4 min-w-max cursor-pointer first-of-type:*:pointer-events-none',
                variant == 'primary' &&
                  'checked-hover:bg-background-50 text-background-20 ' +
                    'checked-hover:text-background-10',
                variant == 'secondary' &&
                  'checked-hover:bg-background-60 text-background-20 ' +
                    'checked-hover:text-background-10',
                variant == 'tertiary' &&
                  'bg-accent-background-30 checked-hover:bg-accent-background-20'
              )}
              onClick={() => {
                onSelectItem(item);
              }}
              onKeyDown={(ev) => {
                if (!a11yClick(ev)) return;
                onSelectItem(item);
              }}
              key={item.value}
              tabIndex={0}
              data-checked={item.value === value}
            >
              {item.component || item.label}
            </li>
          ))}
        </ul>
      </div>
    </>
  );
}

export function Dropdown({
  direction = 'up',
  variant = 'primary',
  alignment = 'right',
  display = 'fit',
  maxHeight = '50vh',
  placeholder,
  control,
  rules,
  name,
  items = [],
}: DropdownProps) {
  const ref = useRef<HTMLDivElement | null>(null);
  const [isOpen, setOpenState] = useState(false);
  const [dropdownBounds, setDropdownBounds] = useState<DOMRect>();

  const updateBounds = () => {
    if (!ref.current) return;
    setDropdownBounds(ref.current?.getBoundingClientRect());
  };

  const onResize = () => {
    // We could have two behaviours here:
    // 1 - We update the bounds of the dropdown so the size and position match.
    //    Works but have a slight delay when resizing, kinda looks laggy
    // 2 - We close the dropdown on resize.
    //     We could consider this as the same as clicking outside of the dropdown
    //     This is the approach chosen RN
    setOpen(false);
  };

  useEffect(() => {
    window.addEventListener('resize', onResize);
    return () => {
      window.removeEventListener('resize', onResize);
    };
  }, []);

  const setOpen = (open: boolean | ((prevState: boolean) => boolean)) => {
    updateBounds();
    setOpenState(open);
  };

  return (
    <Controller
      control={control}
      name={name}
      rules={rules}
      render={({ field: { onChange, value } }) => (
        <>
          <div
            ref={ref}
            className={classNames(
              'min-h-[42px] text-background-10 px-5 py-3 rounded-md focus:ring-4 text-center dropdown',
              'flex cursor-pointer',
              variant == 'primary' && 'bg-background-60 hover:bg-background-50',
              variant == 'secondary' &&
                'bg-background-70 hover:bg-background-60',
              variant == 'tertiary' &&
                'bg-accent-background-30 hover:bg-accent-background-20',
              display === 'fit' && 'w-fit',
              display === 'block' && 'w-full'
            )}
            onClick={() => setOpen((open) => !open)}
            onKeyDown={(ev) => a11yClick(ev) && setOpen((open) => !open)}
            tabIndex={0}
          >
            <div className="flex-grow text-standard first:pointer-events-none">
              {items.find((i) => i.value == value)?.component ||
                items.find((i) => i.value == value)?.label ||
                placeholder}
            </div>
            <div
              className={classNames(
                'ml-2 fill-background-10 flex items-center',
                direction == 'up' && 'rotate-180',
                direction == 'down' && 'rotate-0'
              )}
            >
              <ArrowDownIcon size={16}></ArrowDownIcon>
            </div>
          </div>
          {isOpen &&
            dropdownBounds &&
            createPortal(
              <DropdownItems
                items={items}
                dropdownBounds={dropdownBounds}
                direction={direction}
                display={display}
                alignment={alignment}
                maxHeight={maxHeight}
                variant={variant}
                value={value}
                onSelectItem={(item) => {
                  setOpen(false);
                  onChange(item.value);
                }}
                onBackdropClick={() => {
                  setOpen(false);
                }}
              ></DropdownItems>,
              document.body
            )}
        </>
      )}
    />
  );
}
