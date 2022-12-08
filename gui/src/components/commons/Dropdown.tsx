import classNames from 'classnames';
import { useState } from 'react';
import { Control, Controller } from 'react-hook-form';

export interface DropdownItem {
  label: string;
  value: string;
}

export type DropdownDirection = 'up' | 'down';

export function Dropdown({
  direction = 'up',
  variant = 'primary',
  placeholder,
  control,
  name,
  items = [],
}: {
  direction?: DropdownDirection;
  variant?: 'primary' | 'secondary';
  placeholder: string;
  control: Control<any>;
  name: string;
  items: DropdownItem[];
}) {
  const [isOpen, setOpen] = useState(false);

  return (
    <Controller
      control={control}
      name={name}
      render={({ field: { onChange, value } }) => (
        <>
          {isOpen && (
            <div
              className="absolute top-0 left-0 w-full h-full bg-transparent"
              onClick={() => setOpen(false)}
            ></div>
          )}
          <div className="relative w-fit">
            <div
              className={classNames(
                'min-h-[35px] text-white px-5 py-2.5 rounded-md focus:ring-4 text-center flex',
                variant == 'primary' &&
                  'bg-background-60 hover:bg-background-50',
                variant == 'secondary' &&
                  'bg-background-70 hover:bg-background-60'
              )}
              onClick={() => setOpen((open) => !open)}
            >
              <div className="flex-grow">
                {items.find((i) => i.value == value)?.label || placeholder}
              </div>
              <div
                className={classNames(
                  'ml-2',
                  direction == 'up' && 'rotate-180',
                  direction == 'down' && 'rotate-0'
                )}
              >
                <svg
                  className="justify-end w-4 h-4 "
                  aria-hidden="true"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M19 9l-7 7-7-7"
                  ></path>
                </svg>
              </div>
            </div>
            {isOpen && (
              <div
                className={classNames(
                  'absolute z-10 rounded shadow right-0',
                  direction === 'up' && 'bottom-[45px]',
                  direction === 'down' && 'top-[45px]',
                  variant == 'primary' && 'bg-background-60',
                  variant == 'secondary' && 'bg-background-70'
                )}
              >
                <ul className="py-1 text-sm text-gray-200 flex flex-col ">
                  {items.map((item) => (
                    <li
                      className={classNames(
                        'py-2 px-4 hover:text-white min-w-max',
                        variant == 'primary' && 'hover:bg-background-50',
                        variant == 'secondary' && 'hover:bg-background-60'
                      )}
                      onClick={() => {
                        onChange(item.value);
                        setOpen(false);
                      }}
                      key={item.value}
                    >
                      {item.label}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </>
      )}
    />
  );
}
