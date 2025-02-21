import { Localized, useLocalization } from '@fluent/react';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  ChangeSettingsRequestT,
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
  OSCSettingsT,
  VMCOSCSettingsT,
} from 'solarxr-protocol';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { CheckBox } from '@/components/commons/Checkbox';
import { FileInput } from '@/components/commons/FileInput';
import { VMCIcon } from '@/components/commons/icon/VMCIcon';
import { Input } from '@/components/commons/Input';
import { Typography } from '@/components/commons/Typography';
import { magic } from '@/utils/formatting';
import {
  SettingsPageLayout,
  SettingsPagePaneLayout,
} from '@/components/settings/SettingsPageLayout';
import { error } from '@/utils/logging';

export interface VMCSettingsForm {
  vmc: {
    oscSettings: {
      enabled: boolean;
      portIn: number;
      portOut: number;
      address: string;
    };
    vrmJson?: FileList;
    anchorHip: boolean;
    mirrorTracking: boolean;
  };
}

export const DEFAULT_VMC_VALUES = {
  vmc: {
    oscSettings: {
      enabled: false,
      portIn: 39540,
      portOut: 39539,
      address: '127.0.0.1',
    },
    anchorHip: true,
    mirrorTracking: true,
  },
};

export function VMCSettings() {
  const { l10n } = useLocalization();
  const { sendRPCPacket, useRPCPacket } = useWebsocketAPI();
  const [modelName, setModelName] = useState<string | null>(null);

  const { reset, control, watch, handleSubmit } = useForm<VMCSettingsForm>({
    defaultValues: DEFAULT_VMC_VALUES,
  });

  const onSubmit = async (values: VMCSettingsForm) => {
    const settings = new ChangeSettingsRequestT();

    if (values.vmc) {
      const vmcOsc = new VMCOSCSettingsT();

      vmcOsc.oscSettings = Object.assign(
        new OSCSettingsT(),
        values.vmc.oscSettings
      );
      if (values.vmc.vrmJson !== undefined) {
        if (values.vmc.vrmJson.length > 0) {
          vmcOsc.vrmJson = await parseVRMFile(values.vmc.vrmJson[0]);
          if (vmcOsc.vrmJson) {
            setModelName(
              JSON.parse(vmcOsc.vrmJson)?.extensions?.VRM?.meta?.title || ''
            );
          }
        } else {
          vmcOsc.vrmJson = '';
          setModelName(null);
        }
      }
      vmcOsc.anchorHip = values.vmc.anchorHip;
      vmcOsc.mirrorTracking = values.vmc.mirrorTracking;

      settings.vmcOsc = vmcOsc;
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
    const formData: VMCSettingsForm = DEFAULT_VMC_VALUES;
    if (settings.vmcOsc) {
      if (settings.vmcOsc.oscSettings) {
        formData.vmc.oscSettings.enabled = settings.vmcOsc.oscSettings.enabled;
        if (settings.vmcOsc.oscSettings.portIn)
          formData.vmc.oscSettings.portIn = settings.vmcOsc.oscSettings.portIn;
        if (settings.vmcOsc.oscSettings.portOut)
          formData.vmc.oscSettings.portOut =
            settings.vmcOsc.oscSettings.portOut;
        if (settings.vmcOsc.oscSettings.address)
          formData.vmc.oscSettings.address =
            settings.vmcOsc.oscSettings.address.toString();
      }
      const vrmJson = settings.vmcOsc.vrmJson?.toString();
      if (vrmJson) {
        setModelName(JSON.parse(vrmJson)?.extensions?.VRM?.meta?.title || '');
      }

      formData.vmc.anchorHip = settings.vmcOsc.anchorHip;
      formData.vmc.mirrorTracking = settings.vmcOsc.mirrorTracking;
    }

    reset(formData);
  });

  return (
    <SettingsPageLayout>
      <form className="flex flex-col gap-2 w-full">
        <SettingsPagePaneLayout icon={<VMCIcon></VMCIcon>} id="vmc">
          <>
            <Typography variant="main-title">
              {l10n.getString('settings-osc-vmc')}
            </Typography>
            <div className="flex flex-col pt-2 pb-4">
              <>
                {l10n
                  .getString('settings-osc-vmc-description')
                  .split('\n')
                  .map((line, i) => (
                    <Typography color="secondary" key={i}>
                      {line}
                    </Typography>
                  ))}
              </>
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vmc-enable')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-osc-vmc-enable-description')}
              </Typography>
            </div>
            <div className="grid grid-cols-2 gap-3 pb-5">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vmc.oscSettings.enabled"
                label={l10n.getString('settings-osc-vmc-enable-label')}
              />
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vmc-network')}
            </Typography>
            <div className="flex flex-col pb-2">
              <>
                {l10n
                  .getString('settings-osc-vmc-network-description')
                  .split('\n')
                  .map((line, i) => (
                    <Typography color="secondary" key={i}>
                      {line}
                    </Typography>
                  ))}
              </>
            </div>
            <div className="grid grid-cols-2 gap-3 pb-5">
              <Localized
                id="settings-osc-vmc-network-port_in"
                attrs={{ placeholder: true, label: true }}
              >
                <Input
                  type="number"
                  control={control}
                  name="vmc.oscSettings.portIn"
                  rules={{ required: true }}
                  placeholder="9002"
                  label=""
                ></Input>
              </Localized>
              <Localized
                id="settings-osc-vmc-network-port_out"
                attrs={{ placeholder: true, label: true }}
              >
                <Input
                  type="number"
                  control={control}
                  name="vmc.oscSettings.portOut"
                  rules={{ required: true }}
                  placeholder="9000"
                  label=""
                ></Input>
              </Localized>
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vmc-network-address')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-osc-vmc-network-address-description')}
              </Typography>
            </div>
            <div className="grid gap-3 pb-5">
              <Input
                type="text"
                control={control}
                name="vmc.oscSettings.address"
                rules={{
                  required: true,
                  pattern:
                    /^(?!0)(?!.*\.$)((1?\d?\d|25[0-5]|2[0-4]\d)(\.|$)){4}$/i,
                }}
                placeholder={l10n.getString(
                  'settings-osc-vmc-network-address-placeholder'
                )}
                label=""
              ></Input>
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vmc-vrm')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-osc-vmc-vrm-description')}
              </Typography>
            </div>
            <div className="grid gap-3 pb-5">
              <FileInput
                control={control}
                name="vmc.vrmJson"
                rules={{
                  required: false,
                }}
                value="help"
                importedFileName={
                  // if modelname is an empty string, it's an untitled model
                  modelName === ''
                    ? l10n.getString('settings-osc-vmc-vrm-untitled_model')
                    : modelName
                }
                label="settings-osc-vmc-vrm-file_select"
                accept="model/gltf-binary, model/gltf+json, model/vrml, .vrm, .glb, .gltf"
              ></FileInput>
              {/* For some reason, linux (GNOME) is detecting the VRM file is a VRML */}
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vmc-anchor_hip')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-osc-vmc-anchor_hip-description')}
              </Typography>
            </div>
            <div className="grid grid-cols-2 gap-3 pb-5">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vmc.anchorHip"
                label={l10n.getString('settings-osc-vmc-anchor_hip-label')}
              />
            </div>
            <Typography bold>
              {l10n.getString('settings-osc-vmc-mirror_tracking')}
            </Typography>
            <div className="flex flex-col pb-2">
              <Typography color="secondary">
                {l10n.getString('settings-osc-vmc-mirror_tracking-description')}
              </Typography>
            </div>
            <div className="grid grid-cols-2 gap-3 pb-5">
              <CheckBox
                variant="toggle"
                outlined
                control={control}
                name="vmc.mirrorTracking"
                label={l10n.getString('settings-osc-vmc-mirror_tracking-label')}
              />
            </div>
          </>
        </SettingsPagePaneLayout>
      </form>
    </SettingsPageLayout>
  );
}

const gltfHeaderStart = 0;
const gltfHeaderEnd = 20;

export async function parseVRMFile(vrm: File): Promise<string | null> {
  const headerView = new DataView(
    await vrm.slice(gltfHeaderStart, gltfHeaderEnd).arrayBuffer()
  );
  let cursor = 0;
  const magicBytes = headerView.getUint32(cursor, true);
  if (magicBytes !== magic`glTF`) {
    error(
      `.vrm file starts with ${magicBytes.toString(
        16
      )} instead of ${magic`glTF`.toString(16)}`
    );
    return null;
  }
  cursor += 4;

  const versionNumber = headerView.getUint32(cursor, true);
  if (versionNumber !== 2) {
    error('unsupported glTF version');
    return null;
  }
  cursor += 4;

  // const fileLength = headerView.getUint32(8, true);
  cursor += 4;

  const jsonLength = headerView.getUint32(cursor, true);
  cursor += 4;
  const jsonMagicBytes = headerView.getUint32(cursor, true);
  if (jsonMagicBytes !== magic`JSON`) {
    error(
      `first chunk contains ${jsonMagicBytes.toString(
        16
      )} instead of ${magic`JSON`.toString(16)}`
    );
    return null;
  }

  return vrm
    .slice(gltfHeaderEnd, gltfHeaderEnd + jsonLength, 'application/json')
    .text();
}
