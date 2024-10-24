import { useLocalization } from '@fluent/react';
import { useEffect, useMemo, useState } from 'react';
import { Typography } from '@/components/commons/Typography';
import {
  SettingsPageLayout,
  SettingsPagePaneLayout,
} from '@/components/settings/SettingsPageLayout';
import { WrenchIcon } from '@/components/commons/icon/WrenchIcons';
import { Button } from '@/components/commons/Button';

import { error, log } from '@/utils/logging';
import { useConfig } from '@/hooks/config';
import { CreateProfileModal } from '@/components/settings/CreateProfileModal';
import { ProfileCreateErrorModal } from '@/components/settings/ProfileCreateErrorModal';
import { DeleteProfileModal } from '@/components/settings/DeleteProfileModal';
import { ProfileDeleteErrorModal } from '@/components/settings/ProfileDeleteErrorModal';
import { Input } from '@/components/commons/Input';
import { useForm } from 'react-hook-form';
import { Dropdown } from '@/components/commons/Dropdown';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { RpcMessage, ChangeProfileRequestT } from 'solarxr-protocol';

export function ProfileSettings() {
  const { l10n } = useLocalization();
  const { config, getCurrentProfile, getProfiles, setProfile, deleteProfile } =
    useConfig();
  const [profiles, setProfiles] = useState<string[]>([]);
  const [showCreatePrompt, setShowCreatePrompt] = useState(false);
  const [showCreateError, setShowCreateError] = useState(false);
  const [showDeleteWarning, setShowDeleteWarning] = useState(false);
  const [showDeleteError, setShowDeleteError] = useState(false);

  const { sendRPCPacket } = useWebsocketAPI();

  const profileItems = useMemo(() => {
    // Add default profile to dropdown
    const defaultProfile = { label: 'Default profile', value: 'default' };
    const mappedProfiles = profiles.map((profile) => ({
      label: profile,
      value: profile,
    }));

    return [defaultProfile, ...mappedProfiles];
  }, [profiles]);

  // Fetch profiles on load
  useEffect(() => {
    const fetchProfiles = async () => {
      const profiles = await getProfiles();
      setProfiles(profiles);
    };

    fetchProfiles();
  }, []);

  // Set profile value on load, watch if profile switches
  useEffect(() => {
    const getProfile = async () => {
      const currentProfile = await getCurrentProfile();
      setProfileValue('profile', currentProfile);
    };
    getProfile();

    const subscription = watchProfileSubmit(() => {
      handleProfileSubmit(onSelectSubmit)();
    });
    return () => subscription.unsubscribe();
  }, [profiles]);

  // is there a better way to do this, theres a bunch of stuff here lol
  const {
    control: nameControl,
    watch: watchNameSubmit,
    handleSubmit: handleNameSubmit,
  } = useForm<{
    newName: string;
  }>({
    defaultValues: { newName: '' },
  });

  const {
    control: profileControl,
    watch: watchProfileSubmit,
    handleSubmit: handleProfileSubmit,
    setValue: setProfileValue,
  } = useForm<{
    profile: string;
  }>({
    defaultValues: { profile: config?.profile },
  });

  const {
    control: deleteControl,
    handleSubmit: handleDeleteControl,
    watch: watchDeleteControl,
  } = useForm<{
    profile: string;
  }>({
    defaultValues: { profile: config?.profile || 'default' },
  });

  const profileToCreate = watchNameSubmit('newName');
  const onNameSubmit = async (data: { newName: string }) => {
    if (!data.newName) return;

    const invalidCharsRegex = /[<>:"/\\|?*]/;
    if (data.newName === 'default' || invalidCharsRegex.test(data.newName)) {
      error('Invalid profile name');
      setShowCreateError(true);
      return;
    }

    setShowCreatePrompt(true);
  };

  const onSelectSubmit = (data: { profile: string }) => {
    setProfile(data.profile);
    sendRPCPacket(
      RpcMessage.ChangeProfileRequest,
      new ChangeProfileRequestT(data.profile)
    );
  };

  const profileToDelete = watchDeleteControl('profile');
  const onDeleteSelectSubmit = async (data: { profile: string }) => {
    if (data.profile === 'default') {
      error('Cannot delete default profile');
      setShowDeleteError(true);
      return;
    }

    log(`Deleting profile ${data.profile}`);

    try {
      await deleteProfile(data.profile);
    } catch (e) {
      error(e);
      setShowDeleteError(true);
      return;
    }

    // Update profiles list
    const profiles = await getProfiles();
    setProfiles(profiles);
  };

  const createProfile = async (name: string, useDefault: boolean) => {
    const profiles = await getProfiles();
    if (profiles.includes(name)) {
      error(`Profile with name ${name} already exists`);
      setShowCreateError(true);
      return;
    }

    log(`Creating new profile with name ${name} with defaults: ${useDefault}`);

    try {
      if (!useDefault) {
        const currentConfig = config;
        if (!currentConfig)
          throw new Error(
            'cannot copy current settings because.. current config does not exist?'
          );
        await setProfile(name, currentConfig);
      } else {
        // config.ts automatically uses default config if no config is passed
        await setProfile(name);
      }
    } catch (e) {
      error(e);
      setShowCreateError(true);
      return;
    }

    // Update profiles list
    setProfiles(profiles.concat(name));
  };

  return (
    <SettingsPageLayout>
      <SettingsPagePaneLayout icon={<WrenchIcon />} id="profiles">
        <>
          <Typography variant="main-title">
            {l10n.getString('settings-utils-profiles')}
          </Typography>
          <div className="flex flex-col pt-2 pb-4">
            <>
              {l10n
                .getString('settings-utils-profiles-description')
                .split('\n')
                .map((line, i) => (
                  <Typography color="secondary" key={i}>
                    {line}
                  </Typography>
                ))}
            </>
          </div>
          <div>
            <Typography bold>
              {l10n.getString('settings-utils-profiles-profile')}
            </Typography>
            <div className="flex flex-col pt-1 pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-utils-profiles-profile-description')}
              </Typography>
            </div>
            <div className="grid sm:grid-cols-1 pb-4">
              <Dropdown
                control={profileControl}
                name="profile"
                display="block"
                placeholder={l10n.getString('settings-utils-profiles-default')}
                direction="down"
                items={profileItems}
              ></Dropdown>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2 mobile:grid-cols-1">
            <div>
              <Typography bold>
                {l10n.getString('settings-utils-profiles-new')}
              </Typography>
              <div className="flex flex-col pt-1 pb-2">
                <Typography color="secondary">
                  {l10n.getString('settings-utils-profiles-new-description')}
                </Typography>
              </div>
              <form onSubmit={handleNameSubmit(onNameSubmit)}>
                <div className="flex gap-2 mobile:flex-col">
                  <div style={{ flexBasis: '65%' }}>
                    <Input
                      control={nameControl}
                      rules={{ required: true }}
                      name="newName"
                      type="text"
                      placeholder="Enter name"
                      variant="secondary"
                      className="flex-grow"
                    />
                  </div>

                  <Button
                    variant="secondary"
                    className="flex-grow"
                    style={{ flexBasis: '35%' }}
                    type="submit"
                  >
                    {l10n.getString('settings-utils-profiles-new-label')}
                  </Button>
                </div>
              </form>
              <CreateProfileModal
                primary={() => {
                  setShowCreatePrompt(false);
                  createProfile(profileToCreate, true);
                }}
                secondary={() => {
                  setShowCreatePrompt(false);
                  createProfile(profileToCreate, false);
                }}
                onClose={() => setShowCreatePrompt(false)}
                isOpen={showCreatePrompt}
              ></CreateProfileModal>
            </div>

            <div>
              <Typography bold>
                {l10n.getString('settings-utils-profiles-delete')}
              </Typography>
              <div className="flex flex-col pt-1 pb-2">
                <Typography color="secondary">
                  {l10n.getString('settings-utils-profiles-delete-description')}
                </Typography>
              </div>
              <div className="flex gap-2 mobile:flex-col">
                <div style={{ flexBasis: '65%' }}>
                  <Dropdown
                    control={deleteControl}
                    name="profile"
                    display="block"
                    placeholder={l10n.getString(
                      'settings-utils-profiles-default'
                    )}
                    direction="down"
                    items={profileItems}
                  ></Dropdown>
                </div>
                <Button
                  variant="secondary"
                  onClick={() => {
                    setShowDeleteWarning(true);
                  }}
                  style={{ flexBasis: '35%' }}
                >
                  {l10n.getString('settings-utils-profiles-delete-label')}
                </Button>
                <DeleteProfileModal
                  accept={() => {
                    handleDeleteControl(onDeleteSelectSubmit)();
                    setShowDeleteWarning(false);
                  }}
                  onClose={() => setShowDeleteWarning(false)}
                  isOpen={showDeleteWarning}
                  profile={profileToDelete}
                ></DeleteProfileModal>
                <ProfileCreateErrorModal
                  isOpen={showCreateError}
                  onClose={() => setShowCreateError(false)}
                ></ProfileCreateErrorModal>
                <ProfileDeleteErrorModal
                  isOpen={showDeleteError}
                  onClose={() => setShowDeleteError(false)}
                  profile={profileToDelete}
                ></ProfileDeleteErrorModal>
              </div>
            </div>
          </div>
        </>
      </SettingsPagePaneLayout>
    </SettingsPageLayout>
  );
}
