import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';
import en from './en/translation.json';
import fr from './fr/translation.json';
import owo from './owo/translation.json';
import ja from './ja/translation.json';

export const defaultNS = 'tanslations';

export const langs = [
  {
    name: 'English',
    key: 'en',
  },
  {
    name: 'Français',
    key: 'fr',
  },
  {
    name: 'Engwish~ (OwO)',
    key: 'owo',
  },
  {
    name: '日本語',
    key: 'ja',
  },
];

export const resources = {
  en: {
    tanslations: en,
  },
  fr: {
    tanslations: fr,
  },
  owo: {
    tanslations: owo,
  },
  ja: {
    tanslations: ja,
  },
};

i18next.use(initReactI18next).init({
  lng: 'en', // if you're using a language detector, do not define the lng option
  resources,
  fallbackLng: 'en',
  defaultNS,
});

export default i18next;
