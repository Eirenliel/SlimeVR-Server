import { useEffect, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { useLocation } from 'react-router-dom';
import {
  ChangeSettingsRequestT,
  OSCTrackersSettingT,
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
  VRCOSCSettingsT,
} from 'solarxr-protocol';
import { useWebsocketAPI } from '../../../hooks/websocket-api';
import { CheckBox } from '../../commons/Checkbox';
import { VRCIcon } from '../../commons/icon/VRCIcon';
import { Input } from '../../commons/Input';
import { Typography } from '../../commons/Typography';
import { SettingsPageLayout } from '../SettingsPageLayout';

interface OSCSettingsForm {
  vrchat: {
    enabled: boolean;
    portIn: number;
    portOut: number;
    address: string;
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
    enabled: false,
    portIn: 9001,
    portOut: 9000,
    address: '127.0.0.1',
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

export function OSCSettings() {
  const { sendRPCPacket, useRPCPacket } = useWebsocketAPI();
  const { state } = useLocation();
  const pageRef = useRef<HTMLFormElement | null>(null);

  const { reset, control, watch, handleSubmit, register } =
    useForm<OSCSettingsForm>({
      defaultValues: defaultValues,
    });

  const onSubmit = (values: OSCSettingsForm) => {
    const settings = new ChangeSettingsRequestT();

    if (values.vrchat) {
      const vrcOsc = new VRCOSCSettingsT();
      vrcOsc.enabled = values.vrchat.enabled;
      vrcOsc.portIn = values.vrchat.portIn;
      vrcOsc.portOut = values.vrchat.portOut;
      vrcOsc.address = values.vrchat.address;
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
    const formData: OSCSettingsForm = defaultValues;
    if (settings.vrcOsc) {
      formData.vrchat.enabled = settings.vrcOsc.enabled;
      formData.vrchat.portIn =
        settings.vrcOsc.portIn || defaultValues.vrchat.portIn;
      formData.vrchat.portOut =
        settings.vrcOsc.portOut || defaultValues.vrchat.portOut;
      formData.vrchat.trackers =
        settings.vrcOsc.trackers || defaultValues.vrchat.trackers;
    }

    reset(formData);
  });

  // Handle scrolling to selected page
  useEffect(() => {
    const typedState: { scrollTo: string } = state as any;
    if (!pageRef.current || !typedState || !typedState.scrollTo) {
      return;
    }
    const elem = pageRef.current.querySelector(`#${typedState.scrollTo}`);
    if (elem) {
      elem.scrollIntoView({ behavior: 'smooth' });
    }
  }, [state]);

  return (
    <form className="flex flex-col gap-2 w-full" ref={pageRef}>
      <SettingsPageLayout icon={<VRCIcon></VRCIcon>} id="vrchat">
        <>
          <Typography variant="main-title">VRChat</Typography>
          <div className="flex flex-col pt-2 pb-4">
            <Typography color="secondary">
              Change VRChat-specific settings to receive HMD data and send
            </Typography>
            <Typography color="secondary">
              trackers data for FBT (works on Quest standalone).
            </Typography>
          </div>
          <Typography bold>Enable</Typography>
          <div className="flex flex-col pb-2">
            <Typography color="secondary">
              Toggle the sending and receiving of data
            </Typography>
          </div>
          <div className="grid grid-cols-2 gap-3 pb-5">
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="vrchat.enabled"
              label="Enable"
            />
          </div>
          <Typography bold>Network ports</Typography>
          <div className="flex flex-col pb-2">
            <Typography color="secondary">
              Set the ports for listening and sending data to VRChat
            </Typography>
          </div>
          <div className="grid grid-cols-2 gap-3 pb-5">
            <Input
              type="number"
              {...register('vrchat.portIn', { required: true })}
              placeholder="Port in (default: 9001)"
              label="Port In"
            ></Input>
            <Input
              type="number"
              {...register('vrchat.portOut', { required: true })}
              placeholder="Port out (default: 9000)"
              label="Port Out"
            ></Input>
          </div>
          <Typography bold>Network address</Typography>
          <div className="flex flex-col pb-2">
            <Typography color="secondary">
              Choose which address to send out data to VRChat (check your wifi
              settings on your device)
            </Typography>
          </div>
          <div className="grid gap-3 pb-5">
            <Input
              type="text"
              {...register('vrchat.address', {
                required: true,
                pattern:
                  /^(?!0)(?!.*\.$)((1?\d?\d|25[0-5]|2[0-4]\d)(\.|$)){4}$/i,
              })}
              placeholder="VRChat ip address"
            ></Input>
          </div>
          <Typography bold>Trackers</Typography>
          <div className="flex flex-col pb-2">
            <Typography color="secondary">
              Toggle the sending and receiving of data
            </Typography>
          </div>
          <div className="grid grid-cols-2 gap-3 pb-5">
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="vrchat.trackers.chest"
              label="Chest"
            />
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="vrchat.trackers.waist"
              label="Waist"
            />
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="vrchat.trackers.knees"
              label="Knees"
            />
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="vrchat.trackers.feet"
              label="Feet"
            />
            <CheckBox
              variant="toggle"
              outlined
              control={control}
              name="vrchat.trackers.elbows"
              label="Elbows"
            />
          </div>
        </>
      </SettingsPageLayout>
    </form>
  );
}
