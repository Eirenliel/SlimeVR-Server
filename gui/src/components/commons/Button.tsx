import classNames from 'classnames';
import React, { ReactNode, useMemo } from 'react';
import { NavLink } from 'react-router-dom';
import { LoaderIcon, SlimeState } from './icon/LoaderIcon';
import { XOR } from 'ts-xor';

function ButtonContent({
  loading,
  icon,
  children,
}: {
  loading: boolean;
  icon?: ReactNode;
  children: ReactNode;
}) {
  return (
    <>
      <div
        className={classNames(
          { 'opacity-0': loading },
          'flex flex-row gap-2 justify-center'
        )}
      >
        {icon && (
          <div className="flex justify-center items-center fill-background-10 w-5 h-5">
            {icon}
          </div>
        )}
        {children}
      </div>
      {loading && (
        <div className="absolute top-0 left-0 w-full h-full flex justify-center items-center fill-background-10">
          <LoaderIcon slimeState={SlimeState.JUMPY}></LoaderIcon>
        </div>
      )}
    </>
  );
}

type ButtonBaseParams = {
  children?: ReactNode;
  icon?: ReactNode;
  variant: 'primary' | 'secondary' | 'tertiary' | 'quaternary';
  loading?: boolean;
  rounded?: boolean;
} & Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, 'onClick'>;

type ButtonNavigateParams = { to: string; state?: any } & ButtonBaseParams;
type ButtonScriptParams = {
  onClick?: React.MouseEventHandler<HTMLButtonElement>;
} & ButtonBaseParams;
type ButtonSubmitParams = { type: 'submit' } & ButtonBaseParams;
type ButtonParams = XOR<
  ButtonNavigateParams,
  ButtonScriptParams,
  ButtonSubmitParams
>;

export function Button({
  children,
  variant,
  disabled,
  to,
  loading = false,
  state = {},
  icon,
  rounded = false,
  ...props
}: ButtonParams) {
  const classes = useMemo(() => {
    const variantsMap = {
      primary: classNames({
        'bg-accent-background-30 hover:bg-accent-background-20 text-standard text-background-10':
          !disabled,
        'bg-accent-background-40 hover:bg-accent-background-40 cursor-not-allowed text-accent-background-10':
          disabled,
      }),
      secondary: classNames({
        'bg-background-60 hover:bg-background-50 text-standard text-background-10':
          !disabled,
        'bg-background-60 hover:bg-background-60 cursor-not-allowed text-background-40':
          disabled,
      }),
      tertiary: classNames({
        'bg-background-50 hover:bg-background-40 text-standard text-background-10':
          !disabled,
        'bg-background-50 hover:bg-background-50 cursor-not-allowed text-background-40':
          disabled,
      }),
      quaternary: classNames({
        'bg-background-70 hover:bg-background-60 text-standard text-background-10':
          !disabled,
        'bg-background-70 hover:bg-background-70 cursor-not-allowed text-background-40':
          disabled,
      }),
    };
    return classNames(
      variantsMap[variant],
      'focus:ring-4 text-center relative flex items-center justify-center',
      {
        'rounded-full p-2 text-center min-h-[35px] min-w-[35px]': rounded,
        'rounded-md px-5 py-2.5': !rounded,
      },
      props.className
    );
  }, [variant, disabled, rounded, props.className]);

  return to ? (
    <NavLink
      to={to}
      className={classes}
      state={state}
      onClick={(ev) => disabled && ev.preventDefault()}
    >
      <ButtonContent icon={icon} loading={loading}>
        {children}
      </ButtonContent>
    </NavLink>
  ) : (
    <button type="button" {...props} className={classes} disabled={disabled}>
      <ButtonContent icon={icon} loading={loading}>
        {children}
      </ButtonContent>
    </button>
  );
}
