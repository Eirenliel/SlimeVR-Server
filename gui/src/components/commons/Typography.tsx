import { useConfig } from '@/hooks/config';
import classNames from 'classnames';
import { createElement, ReactNode, useMemo } from 'react';

export function Typography({
  variant = 'standard',
  bold = false,
  color = 'primary',
  whitespace = 'whitespace-normal',
  children,
  italic = false,
  truncate = false,
  textAlign,
  sentryMask = false,
}: {
  variant?:
    | 'main-title'
    | 'section-title'
    | 'standard'
    | 'vr-accessible'
    | 'mobile-title';
  bold?: boolean;
  italic?: boolean;
  truncate?: boolean;
  block?: boolean;
  color?: 'primary' | 'secondary' | string;
  whitespace?:
    | 'whitespace-normal'
    | 'whitespace-nowrap'
    | 'whitespace-pre'
    | 'whitespace-pre-line'
    | 'whitespace-pre-wrap';
  textAlign?:
    | 'text-left'
    | 'text-center'
    | 'text-right'
    | 'text-justify'
    | 'text-start'
    | 'text-end';
  children?: ReactNode;
  sentryMask?: boolean;
}) {
  const tag = useMemo(() => {
    const tags = {
      'main-title': 'h1',
      'section-title': 'h2',
      'mobile-title': 'h1',
      standard: 'p',
      'vr-accessible': 'p',
    };
    return tags[variant];
  }, [variant]);
  const { config } = useConfig();

  return createElement(
    tag,
    {
      className: classNames([
        'transition-colors',
        variant === 'mobile-title' &&
          'xs:text-main-title mobile:text-section-title',
        variant === 'main-title' && 'text-main-title',
        variant === 'section-title' && 'text-section-title',
        variant === 'standard' &&
          (bold ? 'text-standard-bold' : 'text-standard'),
        variant === 'vr-accessible' &&
          (bold ? 'text-vr-accesible-bold' : 'text-vr-accesible'),
        color === 'primary' && 'text-background-10',
        color === 'secondary' && 'text-background-30',
        typeof color === 'string' && color,
        whitespace,
        textAlign,
        italic && 'italic',
        truncate && 'leading-3 text-ellipsis',
        truncate && (config?.textSize ?? 12) > 12 && 'line-clamp-1',
        truncate && (config?.textSize ?? 12) <= 12 && 'line-clamp-2',
        sentryMask && 'sentry-mask',
      ]),
    },
    children || []
  );
}
