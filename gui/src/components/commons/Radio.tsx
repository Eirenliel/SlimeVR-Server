import classNames from 'classnames';
import { Control, Controller } from 'react-hook-form';
import { Typography } from './Typography';
import { ReactNode, useMemo } from 'react';

export function Radio({
  control,
  name,
  label,
  value,
  description,
  children,
  // input props
  disabled,
  variant = 'secondary',
  ...props
}: {
  control: Control<any>;
  name: string;
  label?: string;
  value: string;
  description?: string | null;
  children?: ReactNode;
  variant?: 'secondary' | 'none';
} & React.HTMLProps<HTMLInputElement>) {
  const variantClasses = useMemo(() => {
    const variantsMap = {
      secondary: classNames({
        'bg-background-60 hover:bg-background-50': !disabled,
        'bg-background-80': disabled,
      }),
      none: '',
    };
    return variantsMap[variant];
  }, [variant, disabled]);

  return (
    <Controller
      control={control}
      name={name}
      render={({ field: { onChange, ref, name, value: checked } }) => (
        <label
          className={classNames(
            'w-full rounded-md flex gap-3 border-2 group/radio',
            variantClasses,
            {
              'border-accent-background-30': value == checked,
              'border-transparent': value != checked,
              'cursor-pointer': !disabled,
              'cursor-not-allowed': disabled,
              'p-3': variant !== 'none',
            }
          )}
        >
          <input
            type="radio"
            className={classNames(
              'text-accent-background-30 focus:ring-transparent',
              'focus:ring-offset-transparent focus:outline-transparent'
            )}
            name={name}
            ref={ref}
            onChange={onChange}
            value={value}
            disabled={disabled}
            checked={value == checked}
            {...props}
          />
          <div className="flex flex-col gap-2 pointer-events-none w-full">
            {children ? children : <Typography bold>{label}</Typography>}
            {description && (
              <Typography variant="standard" color="secondary">
                {description}
              </Typography>
            )}
          </div>
        </label>
      )}
    />
  );
}
