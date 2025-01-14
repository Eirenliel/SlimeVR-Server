import { Localized, useLocalization } from '@fluent/react';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  NewSerialDeviceResponseT,
  RpcMessage,
  SerialDeviceT,
} from 'solarxr-protocol';
import { useConfig } from '@/hooks/config';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { useWifiForm, WifiFormData } from '@/hooks/wifi-form';
import { BaseModal } from './commons/BaseModal';
import { Button } from './commons/Button';
import { BulbIcon } from './commons/icon/BulbIcon';
import { USBIcon } from './commons/icon/UsbIcon';
import { Input } from './commons/Input';
import { Typography } from './commons/Typography';

export function SerialDetectionModal() {
  const { l10n } = useLocalization();
  const { config } = useConfig();
  const nav = useNavigate();
  const { pathname } = useLocation();
  const { useRPCPacket } = useWebsocketAPI();
  const [isOpen, setOpen] = useState<SerialDeviceT | null>(null);
  const [showWifiForm, setShowWifiForm] = useState(false);

  const { handleSubmit, submitWifiCreds, formState, control } = useWifiForm();

  const closeModal = () => {
    setOpen(null);
    setShowWifiForm(false);
  };

  const openSerial = () => {
    nav('/settings/serial', { state: { serialPort: isOpen?.port } });
    closeModal();
  };

  const openWifi = () => {
    setShowWifiForm(true);
  };

  const modalWifiSubmit = (form: WifiFormData) => {
    submitWifiCreds(form);
    closeModal();
    nav('/onboarding/connect-trackers', { state: { alonePage: true } });
  };

  useRPCPacket(
    RpcMessage.NewSerialDeviceResponse,
    ({ device }: NewSerialDeviceResponseT) => {
      if (
        config?.watchNewDevices &&
        ![
          '/settings/serial',
          '/onboarding/connect-trackers',
          '/settings/firmware-tool',
        ].includes(pathname)
      ) {
        setOpen(device);
      }
    }
  );

  return (
    <BaseModal isOpen={!!isOpen} onRequestClose={closeModal}>
      <div className="flex flex-col gap-3">
        {!showWifiForm && (
          <>
            <div className="flex flex-col items-center gap-3 fill-accent-background-20">
              <USBIcon></USBIcon>
              <div className="flex flex-col items-center gap-2">
                <Typography variant="main-title">
                  {l10n.getString('serial_detection-new_device-p0')}
                </Typography>
                <Typography variant="section-title">
                  {isOpen?.name || 'unknown'}
                </Typography>
                <Typography variant="standard">
                  {l10n.getString('serial_detection-new_device-p2')}
                </Typography>
              </div>
            </div>

            <Button variant="primary" onClick={openWifi}>
              {l10n.getString('serial_detection-open_wifi')}
            </Button>
            <Button variant="tertiary" onClick={openSerial}>
              {l10n.getString('serial_detection-open_serial')}
            </Button>
            <Button variant="tertiary" onClick={closeModal}>
              {l10n.getString('serial_detection-close')}
            </Button>
          </>
        )}
        {showWifiForm && (
          <form
            onSubmit={handleSubmit(modalWifiSubmit)}
            className="flex flex-col gap-3"
          >
            <div className="flex flex-col items-center gap-3">
              <div className="fill-background-10">
                <BulbIcon></BulbIcon>
              </div>
              <Typography variant="main-title">
                {l10n.getString('serial_detection-new_device-p0')}
              </Typography>
              <Typography variant="standard">
                {l10n.getString('serial_detection-new_device-p1')}
              </Typography>
            </div>
            <div className="flex flex-col gap-3 rounded-xl max-w-sm sentry-mask">
              <Localized
                id="onboarding-wifi_creds-ssid"
                attrs={{ placeholder: true, label: true }}
              >
                <Input
                  control={control}
                  rules={{ required: true }}
                  name="ssid"
                  type="text"
                  label="SSID"
                  placeholder="ssid"
                  variant="secondary"
                />
              </Localized>
              <Localized
                id="onboarding-wifi_creds-password"
                attrs={{ placeholder: true, label: true }}
              >
                <Input
                  control={control}
                  name="password"
                  type="password"
                  label="Password"
                  placeholder="password"
                  variant="secondary"
                />
              </Localized>
            </div>

            <Button
              type="submit"
              variant="primary"
              disabled={!formState.isValid}
            >
              {l10n.getString('serial_detection-submit')}
            </Button>
            <Button variant="tertiary" onClick={closeModal}>
              {l10n.getString('serial_detection-close')}
            </Button>
          </form>
        )}
      </div>
    </BaseModal>
  );
}
