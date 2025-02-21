import { Localized, useLocalization } from '@fluent/react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import {
  ChangeSettingsRequestT,
  OSCSettingsT,
  OSCTrackersSettingT,
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
  VRCOSCSettingsT,
} from 'solarxr-protocol';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { CheckBox } from '@/components/commons/Checkbox';
import { VRCIcon } from '@/components/commons/icon/VRCIcon';
import { Input } from '@/components/commons/Input';
import { Typography } from '@/components/commons/Typography';
import {
  SettingsPageLayout,
  SettingsPagePaneLayout,
} from '@/components/settings/SettingsPageLayout';

interface VRCOSCSettingsForm {
  vrchat: {
    oscSettings: {
      enabled: boolean;
      portIn: number;
      portOut: number;
      address: string;
    };
    trackers: {
      head: boolean;
      chest: boolean;
      elbows: boolean;
      feet: boolean;
      knees: boolean;
      hands: boolean;
      waist: boolean;
    };
  };
}

const defaultValues = {
  vrchat: {
    oscSettings: {
      enabled: false,
      portIn: 9001,
      portOut: 9000,
      address: '127.0.0.1',
    },
    trackers: {
      head: false,
      chest: false,
      elbows: false,
      feet: false,
      knees: false,
      hands: false,
      waist: false,
    },
  },
};

export function VRCOSCSettings() {
  const { l10n } = useLocalization();
  const { sendRPCPacket, useRPCPacket } = useWebsocketAPI();

  const { reset, control, watch, handleSubmit } = useForm<VRCOSCSettingsForm>({
    defaultValues,
  });

  const onSubmit = (values: VRCOSCSettingsForm) => {
    const settings = new ChangeSettingsRequestT();

    if (values.vrchat) {
      const vrcOsc = new VRCOSCSettingsT();

      vrcOsc.oscSettings = Object.assign(
        new OSCSettingsT(),
        values.vrchat.oscSettings
      );
      vrcOsc.trackers = Object.assign(
        new OSCTrackersSettingT(),
        values.vrchat.trackers
      );

      settings.vrcOsc = vrcOsc;
    }
    sendRPCPacket(RpcMessage.ChangeSettingsRequest, settings);
  };

  useEffect(() => {
    const subscription = watch(() => handleSubmit(onSubmit)());
    return () => subscription.unsubscribe();
  }, []);

  useEffect(() => {
    sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT());
  }, []);

  useRPCPacket(RpcMessage.SettingsResponse, (settings: SettingsResponseT) => {
    const formData: VRCOSCSettingsForm = defaultValues;
    if (settings.vrcOsc) {
      if (settings.vrcOsc.oscSettings) {
        formData.vrchat.oscSettings.enabled =
          settings.vrcOsc.oscSettings.enabled;
        if (settings.vrcOsc.oscSettings.portIn)
          formData.vrchat.oscSettings.portIn =
            settings.vrcOsc.oscSettings.portIn;
        if (settings.vrcOsc.oscSettings.portOut)
          formData.vrchat.oscSettings.portOut =
            settings.vrcOsc.oscSettings.portOut;
        if (settings.vrcOsc.oscSettings.address)
          formData.vrchat.oscSettings.address =
            settings.vrcOsc.oscSettings.address.toString();
      }
      if (settings.vrcOsc.trackers)
        formData.vrchat.trackers = settings.vrcOsc.trackers;
    }

    reset(formData);
  });

  return (
    <SettingsPageLayout>
      <form className="flex flex-col gap-2 w-full">
        <SettingsPagePaneLayout icon={<VRCIcon></VRCIcon>} id="vrchat">
          <>
            <Typography variant="main-title">
              {l10n.getString('settings-osc-vrchat')}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              <>
                {l10n
                  .getString('settings-osc-vrchat-description-v2')
                  .split('\n')
                  .concat(
                    l10n
                      .getString('settings-osc-vrchat-description-guide')
                      .split('\n')
                  )
                  .map((line, i) => (
                    <Typography color="secondary" key={i}>
                      {line}
                    </Typography>
                  ))}
              </>
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vrchat-enable')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-osc-vrchat-enable-description')}
              </Typography>
            </div>
            <div className="grid grid-cols-2 gap-3 pb-5">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vrchat.oscSettings.enabled"
                label={l10n.getString('settings-osc-vrchat-enable-label')}
              />
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vrchat-network')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-osc-vrchat-network-description-v1')}
              </Typography>
            </div>
            <div className="grid grid-cols-2 gap-3 pb-5">
              <Localized
                id="settings-osc-vrchat-network-port_in"
                attrs={{ placeholder: true, label: true }}
              >
                <Input
                  type="number"
                  control={control}
                  name="vrchat.oscSettings.portIn"
                  rules={{ required: true }}
                  placeholder="9001"
                  label=""
                ></Input>
              </Localized>
              <Localized
                id="settings-osc-vrchat-network-port_out"
                attrs={{ placeholder: true, label: true }}
              >
                <Input
                  type="number"
                  control={control}
                  name="vrchat.oscSettings.portOut"
                  rules={{ required: true }}
                  placeholder="9000"
                  label=""
                ></Input>
              </Localized>
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vrchat-network-address')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString(
                  'settings-osc-vrchat-network-address-description-v1'
                )}
              </Typography>
            </div>
            <div className="grid gap-3 pb-5">
              <Input
                type="text"
                control={control}
                name="vrchat.oscSettings.address"
                rules={{
                  required: true,
                  pattern:
                    /^(?!0)(?!.*\.$)((1?\d?\d|25[0-5]|2[0-4]\d)(\.|$)){4}$/i,
                }}
                placeholder={l10n.getString(
                  'settings-osc-vrchat-network-address-placeholder'
                )}
                label=""
              ></Input>
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vrchat-network-trackers')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString(
                  'settings-osc-vrchat-network-trackers-description'
                )}
              </Typography>
            </div>
            <div className="grid grid-cols-2 gap-3 pb-5">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vrchat.trackers.chest"
                label={l10n.getString(
                  'settings-osc-vrchat-network-trackers-chest'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vrchat.trackers.waist"
                label={l10n.getString(
                  'settings-osc-vrchat-network-trackers-hip'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vrchat.trackers.knees"
                label={l10n.getString(
                  'settings-osc-vrchat-network-trackers-knees'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vrchat.trackers.feet"
                label={l10n.getString(
                  'settings-osc-vrchat-network-trackers-feet'
                )}
              />
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vrchat.trackers.elbows"
                label={l10n.getString(
                  'settings-osc-vrchat-network-trackers-elbows'
                )}
              />
            </div>
          </>
        </SettingsPagePaneLayout>
      </form>
    </SettingsPageLayout>
  );
}
