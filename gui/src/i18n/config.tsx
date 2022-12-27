import { negotiateLanguages } from '@fluent/langneg';
import { FluentBundle, FluentResource } from '@fluent/bundle';
import { LocalizationProvider, ReactLocalization } from '@fluent/react';
import { Children, ReactNode, useEffect, useState, createContext } from 'react';

export const defaultNS = 'translation';
export const DEFAULT_LOCALE = 'en';

export const langs = [
  {
    name: '🇺🇸 English',
    key: 'en',
  },
  {
    name: '🧉 Español Latinoamericano',
    key: 'es-419',
  },
  {
    name: '🇫🇷 Français',
    key: 'fr',
  },
  {
    name: '🇮🇹 Italiano',
    key: 'it',
  },
  {
    name: '🇯🇵 日本語',
    key: 'ja',
  },
  {
    name: '🇰🇷 한국어',
    key: 'ko',
  },
  {
    name: '🇵🇱 Polski',
    key: 'pl',
  },
  {
    name: '🇧🇷 Português Brasileiro',
    key: 'pt-BR',
  },
  {
    name: '🇻🇳 Tiếng Việt',
    key: 'vi',
  },
  {
    name: '🇨🇳 简体中文',
    key: 'zh-Hans',
  },
  {
    name: '🥺 Engwish~ OwO',
    key: 'en-OwO',
  },
];

// Fetch translation file
async function fetchMessages(locale: string): Promise<[string, string]> {
  const response = await fetch(`/i18n/${locale}/translation.ftl`);
  const messages = await response.text();
  return [locale, messages];
}

// Generator function for making FluentBundles from the translation file
function* lazilyParsedBundles(fetchedMessages: [string, string][]) {
  for (const [locale, messages] of fetchedMessages) {
    const resource = new FluentResource(messages);
    const bundle = new FluentBundle(locale);
    bundle.addResource(resource);
    yield bundle;
  }
}

interface AppLocalizationProviderProps {
  children: ReactNode;
}
interface i18n {
  currentLocales: string[];
  changeLocales: (userLocales: readonly string[]) => Promise<void>;
}

export const LangContext = createContext<i18n>(undefined as never);
export function AppLocalizationProvider(props: AppLocalizationProviderProps) {
  const [currentLocales, setCurrentLocales] = useState([DEFAULT_LOCALE]);
  const [l10n, setL10n] = useState<ReactLocalization | null>(null);

  async function changeLocales(userLocales: readonly string[]) {
    const currentLocales = negotiateLanguages(
      userLocales,
      langs.map((x) => x.key),
      { defaultLocale: DEFAULT_LOCALE }
    );
    setCurrentLocales(currentLocales);

    const fetchedMessages = await Promise.all(
      currentLocales.map(fetchMessages)
    );

    const bundles = lazilyParsedBundles(fetchedMessages);
    localStorage.setItem('i18nextLng', currentLocales[0]);
    setL10n(new ReactLocalization(bundles));
  }

  useEffect(() => {
    const lang = localStorage.getItem('i18nextLng');
    const array = [];
    if (lang) array.push(lang);
    changeLocales([...array, ...navigator.languages]);
    // detect hot reload translation file changes
    if (import.meta.hot) {
      import.meta.hot.on('locales-update', () => changeLocales(currentLocales));
    }
  }, []);

  if (l10n === null) {
    return <></>;
  }

  return (
    <>
      <LocalizationProvider l10n={l10n}>
        <LangContext.Provider value={{ currentLocales, changeLocales }}>
          {Children.only(props.children)}
        </LangContext.Provider>
      </LocalizationProvider>
    </>
  );
}
