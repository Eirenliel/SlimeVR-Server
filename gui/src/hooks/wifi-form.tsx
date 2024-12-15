import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useOnboarding } from './onboarding';
import { useWifiNetworks } from './wifi-scan';

export interface WifiFormData {
  ssid: string;
  ssidSelect: string;
  password?: string;
}

export function useWifiForm() {
  const navigate = useNavigate();
  const { state, setWifiCredentials } = useOnboarding();
  const { register, reset, handleSubmit, formState, control, watch, setValue } =
    useForm<WifiFormData>({
      defaultValues: {},
      reValidateMode: 'onSubmit',
    });

  const { wifiNetworks } = useWifiNetworks();

  const ssidSelect = watch('ssidSelect');
  useEffect(() => {
    if (ssidSelect === 'other') {
      setValue('ssid', '');
      setValue('password', '');
      return;
    }

    const network = wifiNetworks.find((network) => network.ssid === ssidSelect);

    if (!network) return;

    setValue('ssid', network.ssid);
    setValue('password', network.password);
  }, [ssidSelect]);

  useEffect(() => {
    if (state.wifi) {
      reset({
        ssid: state.wifi.ssid,
        password: state.wifi.password,
      });
    }
  }, []);

  const submitWifiCreds = (value: WifiFormData) => {
    setWifiCredentials(value.ssid, value.password ?? '');
    navigate('/onboarding/connect-trackers', {
      state: { alonePage: state.alonePage },
    });
  };

  return {
    submitWifiCreds,
    handleSubmit,
    register,
    formState,
    wifiNetworks,
    hasWifiCreds: !!state.wifi,
    control,
    watch,
  };
}
