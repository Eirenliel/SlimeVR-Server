import { ReactNode } from 'react';
import { BulbIcon } from './icon/BulbIcon';
import { WarningIcon } from './icon/WarningIcon';
import { Typography } from './Typography';
import classNames from 'classnames';
import { QuestionIcon } from './icon/QuestionIcon';

/**
 * Enabling "whitespace" will respect the newlines and spacing given in the text.
 */

export function TipBox({
  children,
  hideIcon = false,
  whitespace = false,
  className,
}: {
  children: ReactNode;
  hideIcon?: boolean;
  whitespace?: boolean;
  className?: string;
}) {
  return (
    <div
      className={classNames(
        'flex flex-row gap-4 bg-accent-background-50 p-4 rounded-md',
        className
      )}
    >
      <div
        className={classNames(
          'fill-accent-background-20 flex flex-col justify-center',
          hideIcon && 'hidden'
        )}
      >
        <BulbIcon></BulbIcon>
      </div>
      <div className="flex flex-col justify-center">
        <Typography
          color="text-accent-background-10"
          whitespace={whitespace ? 'whitespace-pre' : undefined}
        >
          {children}
        </Typography>
      </div>
    </div>
  );
}

export function QuestionBox({
  children,
  hideIcon = false,
  whitespace = false,
  className,
}: {
  children: ReactNode;
  hideIcon?: boolean;
  whitespace?: boolean;
  className?: string;
}) {
  return (
    <div
      className={classNames(
        'flex flex-row gap-4 bg-trans-blue-400 p-4 rounded-md',
        className
      )}
    >
      <div
        className={classNames(
          'stroke-background-90 flex flex-col justify-center',
          hideIcon && 'hidden'
        )}
      >
        <QuestionIcon width={whitespace ? 36 : 24}></QuestionIcon>
      </div>
      <div className="flex flex-col justify-center">
        <Typography
          color="text-background-90"
          whitespace={whitespace ? 'whitespace-pre' : undefined}
        >
          {children}
        </Typography>
      </div>
    </div>
  );
}

export function ErrorBox({
  children,
  whitespace = true,
  hideIcon = false,
  className,
}: {
  children: ReactNode;
  whitespace?: boolean;
  hideIcon?: boolean;
  className?: string;
}) {
  return (
    <div
      className={classNames(
        'flex flex-row gap-4 bg-status-critical p-4 rounded-md',
        className
      )}
    >
      <div
        className={classNames(
          'text-background-90 flex flex-col justify-center',
          hideIcon && 'hidden'
        )}
      >
        <WarningIcon></WarningIcon>
      </div>
      <div className="flex flex-col justify-center">
        <Typography
          color="text-background-90"
          whitespace={whitespace ? 'whitespace-pre-line' : undefined}
        >
          {children}
        </Typography>
      </div>
    </div>
  );
}

export function WarningBox({
  children,
  whitespace = true,
  hideIcon = false,
  className,
}: {
  children: ReactNode;
  whitespace?: boolean;
  hideIcon?: boolean;
  className?: string;
}) {
  return (
    <div
      className={classNames(
        'flex flex-row gap-4 bg-status-warning p-4 rounded-md',
        className
      )}
    >
      <div
        className={classNames(
          'text-background-60 flex flex-col justify-center',
          hideIcon && 'hidden'
        )}
      >
        <WarningIcon></WarningIcon>
      </div>
      <div className="flex flex-col justify-center">
        <Typography
          color="text-background-60"
          whitespace={whitespace ? 'whitespace-pre-line' : undefined}
        >
          {children}
        </Typography>
      </div>
    </div>
  );
}
