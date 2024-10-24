import { createContext, useContext, useEffect, useState } from 'react';
import { DeveloperModeWidgetForm } from '@/components/widgets/DeveloperModeWidget';
import { error, log } from '@/utils/logging';
import { useDebouncedEffect } from './timeout';
import { createStore, Store } from '@tauri-apps/plugin-store';
import { useIsTauri } from './breakpoint';
import { waitUntil } from '@/utils/a11y';
import { appConfigDir, resolve } from '@tauri-apps/api/path';
import { exists, mkdir, readDir, remove, writeTextFile } from '@tauri-apps/plugin-fs';
import { isTauri } from '@tauri-apps/api/core';

export interface WindowConfig {
  width: number;
  height: number;
  x: number;
  y: number;
}

export enum AssignMode {
  LowerBody = 'lower-body',
  Core = 'core',
  EnhancedCore = 'enhanced-core',
  FullBody = 'full-body',
  All = 'all',
}

export interface Config {
  debug: boolean;
  lang: string;
  doneOnboarding: boolean;
  watchNewDevices: boolean;
  devSettings: DeveloperModeWidgetForm;
  feedbackSound: boolean;
  feedbackSoundVolume: number;
  connectedTrackersWarning: boolean;
  theme: string;
  textSize: number;
  fonts: string[];
  useTray: boolean | null;
  mirrorView: boolean;
  assignMode: AssignMode;
  discordPresence: boolean;
  decorations: boolean;
  showNavbarOnboarding: boolean;
  profile: string;
}

export interface ConfigContext {
  config: Config | null;
  loading: boolean;
  setConfig: (config: Partial<Config>) => Promise<void>;
  loadConfig: () => Promise<Config | null>;
  saveConfig: () => Promise<void>;
  getCurrentProfile: () => Promise<string>;
  getProfiles: () => Promise<string[]>;
  setProfile: (profile: string, config?: Config) => Promise<void>;
  deleteProfile: (profile: string) => Promise<void>;
}

export const defaultConfig: Omit<Config, 'devSettings'> = {
  lang: 'en',
  debug: false,
  doneOnboarding: false,
  watchNewDevices: true,
  feedbackSound: true,
  feedbackSoundVolume: 0.5,
  connectedTrackersWarning: true,
  theme: 'slime',
  textSize: 12,
  fonts: ['poppins'],
  useTray: null,
  mirrorView: true,
  assignMode: AssignMode.Core,
  discordPresence: false,
  decorations: false,
  showNavbarOnboarding: true,
  profile: 'default',
};

interface CrossStorage {
  set(key: string, value: string): Promise<void>;
  get(key: string): Promise<string | null>;
}

let configToSet: Config | null | undefined = null;

const localStore: CrossStorage = {
  get: async (key) => localStorage.getItem(key),
  set: async (key, value) => localStorage.setItem(key, value),
};

let store: CrossStorage = isTauri()
  ? await createStore('gui-settings.dat')
  : localStore;

function fallbackToDefaults(loadedConfig: any): Config {
  return Object.assign({}, defaultConfig, loadedConfig);
}

export function useConfigProvider(): ConfigContext {
  const [currConfig, set] = useState<Config | null>(null);
  const [loading, setLoading] = useState(false);
  const [currentProfile, setCurrentProfile] = useState<string>('default');
  const tauri = useIsTauri();

  useDebouncedEffect(
    () => {
      if (!currConfig) return;

      store.set('config.json', JSON.stringify(currConfig));
    },
    [currConfig],
    100
  );

  // Load profile when changed
  useEffect(() => {
    loadProfileConfig(currentProfile);
  }, [currentProfile]);

  const getStore = async (profile: string) => {
    if (profile === 'default') return await createStore('gui-settings.dat');

    const appDirectory = await appConfigDir();
    const profileDir = await resolve(`${appDirectory}/profiles/${profile}`);
    const profileFile = await resolve(`${profileDir}/gui-settings.dat`);

    // Check if profile directory exists before creating it
    const profileDirExists = await exists(profileDir);
    if (!profileDirExists) {
      await mkdir(profileDir, { recursive: true });
    }

    // Check if profile file exists before writing defaults
    const profileFileExists = await exists(profileFile);
    if (!profileFileExists) {
      await writeTextFile(
        profileFile,
        JSON.stringify({ 'config.json': JSON.stringify(defaultConfig) })
      );
    }

    return createStore(profileFile);
  };

  const loadProfileConfig = async (profile: string) => {
    const profileStore = await getStore(profile);
    store = profileStore;
    const json = await store.get('config.json');
    let loadedConfig = fallbackToDefaults(JSON.parse(json ?? '{}'));
    if (configToSet) {
      // Used when user creates a profile in GUI and chose to copy their settings
      loadedConfig = { ...configToSet };
      configToSet = null;
    }
    set(loadedConfig);
    // FIXME for some reason, this function is apparently ran multiple times and so this log is printed multiple times
    // small, but kinda annoys me lol
    log(`Loaded profile: ${profile}`);
    return loadedConfig;
  };

  const setConfig = async (config: Partial<Config>) => {
    set((curr) =>
      config
        ? ({
            ...curr,
            ...config,
          } as Config)
        : null
    );
    if (tauri) {
      await waitUntil(
        async () => {
          const newConfig: Partial<Config> = JSON.parse(
            (await store.get('config.json')) ?? '{}'
          );

          return Object.entries(config).every(([key, value]) =>
            typeof value === 'object'
              ? JSON.stringify(newConfig[key as keyof Config]) === JSON.stringify(value)
              : newConfig[key as keyof Config] === value
          );
        },
        100,
        10
      );
    } else {
      await waitUntil(
        () => {
          const newConfig: Partial<Config> = JSON.parse(
            localStorage.getItem('config.json') ?? '{}'
          );
          return Object.entries(config).every(([key, value]) =>
            typeof value === 'object'
              ? JSON.stringify(newConfig[key as keyof Config]) === JSON.stringify(value)
              : newConfig[key as keyof Config] === value
          );
        },
        100,
        10
      );
    }
  };

  const getCurrentProfile = async () => {
    const defaultStore = await getStore('default');
    const defaultConfig = fallbackToDefaults(
      JSON.parse((await defaultStore.get('config.json')) ?? '{}')
    );
    return defaultConfig.profile ?? 'default';
  };

  const getProfiles = async () => {
    // artificial delay to let profile write to disk (when created) before we actually read it
    // fixes "default" being selected when new profile is created (because folder hasn't been made)
    // idk why this is necessary but it is
    await new Promise((resolve) => setTimeout(resolve, 100));
    const appDirectory = await appConfigDir();
    const profilesDir = await resolve(`${appDirectory}/profiles`);
    await mkdir(profilesDir, { recursive: true });
    const profiles = await readDir(profilesDir);

    return profiles.map((profile) => profile.name);
  };

  const setProfile = async (profile: string, config?: Config) => {
    // load default config, set profile to new profile, save config, then load new profile config
    // idk if this is the best way to do this lol
    const defaultStore = await getStore('default');
    const defaultConfig = fallbackToDefaults(
      JSON.parse((await defaultStore.get('config.json')) ?? '{}')
    );
    await defaultStore.set(
      'config.json',
      JSON.stringify({
        ...defaultConfig,
        profile,
      })
    );
    await defaultStore.save();

    // Used when user creates a profile in GUI and chose to copy their settings
    if (config) configToSet = config;
    setCurrentProfile(profile);
  };

  const deleteProfile = async (profile: string) => {
    if (profile === 'default') return;

    // Remove profile directory
    const appDirectory = await appConfigDir();
    const profileDir = await resolve(`${appDirectory}/profiles/${profile}`);
    await remove(profileDir, { recursive: true });

    // Set profile in default config
    const defaultStore = await getStore('default');
    const defaultConfig = fallbackToDefaults(
      JSON.parse((await defaultStore.get('config.json')) ?? '{}')
    );
    await defaultStore.set(
      'config.json',
      JSON.stringify({
        ...defaultConfig,
        profile: 'default',
      })
    );

    // Set to default if deleting current profile
    if (profile === currentProfile) {
      setCurrentProfile('default');
    }
  };

  return {
    config: currConfig,
    loading,
    setConfig,
    loadConfig: async () => {
      setLoading(true);
      try {
        const migrated = await store.get('configMigratedToTauri');
        if (!migrated) {
          const oldConfig = localStorage.getItem('config.json');

          if (oldConfig) await store.set('config.json', oldConfig);

          await store.set('configMigratedToTauri', 'true');
        }

        const json = await store.get('config.json');
        if (!json) throw new Error('Config has ceased existing for some reason');

        let loadedConfig = fallbackToDefaults(JSON.parse(json));
        const profile = loadedConfig.profile;
        if (profile && profile !== 'default') {
          loadedConfig = await loadProfileConfig(profile);
        }

        set(loadedConfig);
        setLoading(false);
        return loadedConfig;
      } catch (e) {
        error(e);
        setConfig(defaultConfig);
        setLoading(false);
        return null;
      }
    },
    saveConfig: async () => {
      if (!tauri) return;
      await (store as Store).save();
    },
    getCurrentProfile,
    getProfiles,
    setProfile,
    deleteProfile,
  };
}

export const ConfigContextC = createContext<ConfigContext>(undefined as never);

export function useConfig() {
  const context = useContext<ConfigContext>(ConfigContextC);
  if (!context) {
    throw new Error('useConfig must be within a ConfigContext Provider');
  }
  return context;
}
