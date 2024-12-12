import plugin from 'tailwindcss/plugin';
import forms from '@tailwindcss/forms';
import gradient from 'tailwind-gradient-mask-image';
import type { Config } from 'tailwindcss';

const colors = {
  'blue-gray': {
    100: '#ffffff',
    200: '#78A4C6',
    300: '#608AAB',
    400: '#3D6381',
    500: '#1A3D59',
    600: '#112D43',
    700: '#081E30',
    800: '#00101C',
    900: '#000509',
  },
  purple: {
    100: '#BB8AE5',
    200: '#9D5CD4',
    500: '#65459A',
    700: '#623B83',
    900: '#2E2145',
  },
  'trans-blue': {
    100: '#4D222B', // Dark text
    200: '#1A6682', // Some lighter-ish text
    300: '#095470', // Light-ish text
    400: '#4F9FBD', // Button hover in some places
    500: '#EEEEEE', // Darker trans white tracker list background
    600: '#FFFFFF', // Trans white tracker list background and some buttons
    700: '#F5A9B8', // Trans pink home background
    800: '#5BCEFA', // Trans blue trans outer background
    900: '#000509',
  },
  'trans-pink': {
    100: '#B53A52', // Accent text
    200: '#FCCAD4', // Lighter trans pink button
    300: '#F7B7C4', // Somewhat lighter trans pink button
    400: '#F7B7C4', // Somewhat lighter trans pink button
    500: '#F5A9B8', // Trans pink buttons
    700: '#FA91A6', // Darker trans pink button
    900: '#F77C94', // Even darker trans pink home bottom
  },
  'green-background': {
    100: '#ffffff',
    200: '#6bce6b',
    300: '#44c145',
    400: '#2e8b2f',
    500: '#1b521c',
    600: '#143c14',
    700: '#456d45',
    800: '#071407',
    900: '#020602',
  },
  'green-accent': {
    100: '#68cd69',
    200: '#39ab3a',
    500: '#297a29',
    700: '#246c24',
    900: '#133913',
  },
  'yellow-background': {
    100: '#ffffff',
    200: '#cecc6b',
    300: '#c1bf44',
    400: '#8b892e',
    500: '#52521b',
    600: '#3c3c14',
    700: '#27270d',
    800: '#141407',
    900: '#060602',
  },
  'yellow-accent': {
    100: '#cdcb68',
    200: '#aba939',
    500: '#7a7929',
    700: '#6c6b24',
    900: '#393913',
  },
  'orange-background': {
    100: '#ffffff',
    200: '#ce916b',
    300: '#c17444',
    400: '#8b522e',
    500: '#52311b',
    600: '#3c2314',
    700: '#27170d',
    800: '#140c07',
    900: '#060402',
  },
  'orange-accent': {
    100: '#cd8f68',
    200: '#ab6539',
    500: '#7a4829',
    700: '#6c4024',
    900: '#392213',
  },
  'red-background': {
    100: '#ffffff',
    200: '#ce6b6b',
    300: '#c14444',
    400: '#8b2e2e',
    500: '#521b1b',
    600: '#3c1414',
    700: '#270d0d',
    800: '#140707',
    900: '#060202',
  },
  'red-accent': {
    100: '#cd6868',
    200: '#ab3939',
    500: '#7a2929',
    700: '#6c2424',
    900: '#391313',
  },
  'dark-background': {
    100: '#ffffff',
    200: '#9e9e9e',
    300: '#858585',
    400: '#5e5e5e',
    500: '#3a3a3a',
    600: '#2a2a2a',
    700: '#1c1c1c',
    800: '#0e0e0e',
    900: '#040404',
  },
  'dark-accent': {
    100: '#9e9e9e',
    200: '#797979',
    500: '#555555',
    700: '#4c4c4c',
    900: '#272727',
  },
  'light-background': {
    100: '#000000',
    200: '#616161',
    300: '#7a7a7a',
    400: '#a1a1a1',
    500: '#c5c5c5',
    600: '#d5d5d5',
    700: '#e3e3e3',
    800: '#f1f1f1',
    900: '#fbfbfb',
  },
  'light-accent': {
    100: '#616161',
    200: '#868686',
    500: '#aaaaaa',
    700: '#b3b3b3',
    900: '#d8d8d8',
  },
  asexual: {
    100: '#000000',
    200: '#A3A3A3',
    300: '#FFFFFF',
    400: '#800080',
  },
};

const config = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    screens: {
      nsmol: { raw: 'not (min-width: 525px)' },
      smol: '525px',
      mobile: { raw: 'not (min-width: 800px)' },
      xs: '800px',
      nsm: { raw: 'not (min-width: 900px)' },
      sm: '900px',
      md: '1100px',
      'md-max': { raw: 'not (min-width: 1100px)' },
      lg: '1300px',
      xl: '1600px',
      tall: { raw: '(min-height: 800px)' },
    },
    extend: {
      colors: {
        status: {
          success: 'rgb(var(--success), <alpha-value>)',
          warning: 'rgb(var(--warning), <alpha-value>)',
          critical: 'rgb(var(--critical), <alpha-value>)',
          special: 'rgb(var(--special), <alpha-value>)',
        },
        window: {
          icon: 'rgb(var(--window-icon-stroke), <alpha-value>)',
        },
        ...colors,
        background: {
          10: 'rgb(var(--background-10), <alpha-value>)',
          20: 'rgb(var(--background-20), <alpha-value>)',
          30: 'rgb(var(--background-30), <alpha-value>)',
          40: 'rgb(var(--background-40), <alpha-value>)',
          50: 'rgb(var(--background-50), <alpha-value>)',
          60: 'rgb(var(--background-60), <alpha-value>)',
          70: 'rgb(var(--background-70), <alpha-value>)',
          80: 'rgb(var(--background-80), <alpha-value>)',
          90: 'rgb(var(--background-90), <alpha-value>)',
        },
        'accent-background': {
          10: 'rgb(var(--accent-background-10), <alpha-value>)',
          20: 'rgb(var(--accent-background-20), <alpha-value>)',
          30: 'rgb(var(--accent-background-30), <alpha-value>)',
          40: 'rgb(var(--accent-background-40), <alpha-value>)',
          50: 'rgb(var(--accent-background-50), <alpha-value>)',
        },
      },
      fontSize: {
        DEFAULT: 'calc(var(--font-size-standard) / 16)',
      },
      fontWeight: {
        DEFAULT: '500',
      },
      color: {
        DEFAULT: 'rgb(var(--default-color), <alpha-value>)',
      },
      keyframes: {
        bounce: {
          '0%, 100%': {
            transform: 'translateY(0)',
            'animation-timing-function': 'cubic-bezier(0, 0, 0.2, 1)',
          },
          '50%': {
            transform: 'translateY(-25%)',
            'animation-timing-function': 'cubic-bezier(0.8, 0, 1, 1)',
          },
        },
        'background-scroll': {
          '0%': {
            'background-position': 'calc(128px / sin(135deg)) 0%', // size / sin(135deg)
          },
        },
      },
      animation: {
        scroll: 'background-scroll 4s linear infinite reverse',
      },
      backgroundImage: {
        slime: `linear-gradient(135deg, ${colors.purple[100]} 50%, ${colors['blue-gray'][700]} 50% 100%)`,
        'slime-green': `linear-gradient(135deg, ${colors['green-accent'][100]} 50%, ${colors['green-background'][700]} 50% 100%)`,
        'slime-yellow': `linear-gradient(135deg, ${colors['yellow-accent'][100]} 50%, ${colors['yellow-background'][700]} 50% 100%)`,
        'slime-orange': `linear-gradient(135deg, ${colors['orange-accent'][100]} 50%, ${colors['orange-background'][700]} 50% 100%)`,
        'slime-red': `linear-gradient(135deg, ${colors['red-accent'][100]} 50%, ${colors['red-background'][700]} 50% 100%)`,
        dark: `linear-gradient(135deg, ${colors['dark-accent'][100]} 50%, ${colors['dark-background'][700]} 50% 100%)`,
        light: `linear-gradient(135deg, ${colors['light-accent'][100]} 50%, ${colors['light-background'][700]} 50% 100%)`,
        'trans-flag': `linear-gradient(135deg, ${colors['trans-blue'][800]} 40%, ${colors['trans-blue'][700]} 40% 70%, ${colors['trans-blue'][600]} 70% 100%)`,
        'asexual-flag': `linear-gradient(135deg, ${colors['asexual'][100]} 30%, ${colors['asexual'][200]} 30% 50%, ${colors['asexual'][300]} 50% 70%, ${colors['asexual'][400]} 70% 100%)`,
        random:
          'repeating-linear-gradient(135deg, #ff0000 0 calc(128px / 6 * 1), #ffa500 calc(128px / 6 * 1) calc(128px / 6 * 2), #ffff00 calc(128px / 6 * 2) calc(128px / 6 * 3), #008000 calc(128px / 6 * 3) calc(128px / 6 * 4), #0000ff calc(128px / 6 * 4) calc(128px / 6 * 5), #800080 calc(128px / 6 * 5) calc(128px / 6 * 6))',
      },
    },
    data: {
      checked: 'checked=true',
    },
  },
  plugins: [
    forms,
    gradient,
    plugin(function ({ addUtilities }) {
      const textConfig = (fontSize: any, fontWeight: any) => ({
        fontSize,
        fontWeight,
      });

      addUtilities({
        '.text-main-title': textConfig('calc(var(--font-size-title) / 16)', 700),
        '.text-section-title': textConfig('calc(var(--font-size-vr) / 16)', 700),
        '.text-standard': textConfig('calc(var(--font-size-standard) / 16)', 500),
        '.text-vr-accesible': textConfig('calc(var(--font-size-vr) / 16)', 500),
        '.text-vr-accesible-bold': textConfig('calc(var(--font-size-vr) / 16)', 700),
        '.text-standard-bold': textConfig('calc(var(--font-size-standard) / 16)', 700),
      });
    }),
    plugin(function ({ addVariant }) {
      addVariant('checked-hover', ['&:hover', '&[data-checked=true]']);
    }),
  ],
} satisfies Config;

export default config;
