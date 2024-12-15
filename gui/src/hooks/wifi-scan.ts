import { Command } from '@tauri-apps/plugin-shell';
import * as os from '@tauri-apps/plugin-os';
import { useWebsocketAPI } from './websocket-api';
import {
  RpcMessage,
  SerialTrackerGetWifiScanRequestT,
  SerialUpdateResponseT,
  OpenSerialRequestT,
} from 'solarxr-protocol';
import { useEffect, useState, useMemo } from 'react';

export interface WifiNetwork {
  ssid: string;
  source: string;
  password: string;
  connected: boolean;
  signalStrength: number | null;
}

async function getWifiNetworksLinux(): Promise<WifiNetwork[]> {
  // TODO
  return [];
}

async function getWifiNetworksMac(): Promise<WifiNetwork[]> {
  // TODO
  return [];
}

async function getWifiNetworksWindowsSaved(): Promise<WifiNetwork[]> {
  const ret: WifiNetwork[] = [];

  const networksResponse = await Command.create('netsh-list', [
    'wlan',
    'show',
    'profile'
  ]).execute();

  networksResponse.stdout.split('\n').forEach(line => {
    const ssidMatch = line.match(/All User Profile\s+:\s+(.+)/);
    if(!ssidMatch)
      return;
    const ssid = ssidMatch[1];
    ret.push({
      ssid: ssid,
      source: 'windows',
      password: '',
      connected: false,
      signalStrength: null,
    });
  });

  ret.forEach(async (network) => {
    const profileResponse = await Command.create('netsh-details', [
      'wlan',
      'show',
      'profile',
      `name=${network.ssid}`,
      'key=clear'
    ]).execute();
    const passwordMatch = profileResponse.stdout.match(/Key Content\s+:\s+(.+)/);
    network.password = passwordMatch ? passwordMatch[1] : '';
  });

  return ret;
}

async function getWifiNetworksWindowsScan(): Promise<WifiNetwork[]> {
  const ret: WifiNetwork[] = [];

  const scanResponse = await Command.create('netsh-scan', [
    'wlan',
    'show',
    'network',
    'mode=Bssid',
  ]).execute();

  let lastSsid: string | null = null;

  scanResponse.stdout.split('\n').forEach((line) => {
    const ssidMatch = line.match(/SSID\s+:\s+(.+)/);
    if (ssidMatch) {
      lastSsid = ssidMatch[1];
    }

    const signalMatch = line.match(/Signal\s+:\s+(.+)/);
    if (!signalMatch) return;

    if (lastSsid) {
      let network = ret.find((network) => network.ssid === lastSsid);
      if (network === undefined) {
        network = {
          ssid: lastSsid,
          source: 'windows',
          password: '',
          connected: false,
          signalStrength: null,
        };

        ret.push(network);
      }

      network.signalStrength = parseInt(signalMatch[1]) / 100;
    }
  });

  const connectedResponse = await Command.create('netsh-connected', [
    'wlan',
    'show',
    'interfaces',
  ]).execute();

  const connectedMatch = connectedResponse.stdout.match(/SSID\s+:\s+(.+)/);
  if (connectedMatch) {
    const connectedNetwork = ret.find((network) => network.ssid === connectedMatch[1]);
    if (connectedNetwork) {
      connectedNetwork.connected = true;
    }
  }

  return ret;
}

function useWifiNetworksSlimes() {
  const [slimeWifiNetworks, setSlimeWifiNetworks] = useState<WifiNetwork[]>([]);
  const { useRPCPacket, sendRPCPacket } = useWebsocketAPI();
  const [isSerialOpen, setSerialOpen] = useState<boolean>(false);
  const [isSerialOpening, setSerialOpening] = useState<boolean>(false);
  const [scanStarted, setScanStarted] = useState(false);

  useEffect(() => {
    if(isSerialOpening === false) {
      setSerialOpening(true);
      // sendRPCPacket(RpcMessage.CloseSerialRequest, new CloseSerialRequestT());
      const req = new OpenSerialRequestT();
      req.port = 'Auto';
      req.auto = true;
      sendRPCPacket(RpcMessage.OpenSerialRequest, req);
    }

    if (isSerialOpen === true) {
      if (scanStarted === false) {
        setScanStarted(true);
        setTimeout(() => {
          sendRPCPacket(
            RpcMessage.SerialTrackerGetWifiScanRequest,
            new SerialTrackerGetWifiScanRequestT()
          );
        }, 300);
      }
    }
  }, [isSerialOpen]);

  useRPCPacket(RpcMessage.SerialUpdateResponse, (data: SerialUpdateResponseT) => {
    if (data.closed) {
      if (isSerialOpen !== false) setSerialOpen(false);
      return;
    } else {
      if (isSerialOpen !== true) setSerialOpen(true);
    }

    const logString: string = <string>data.log;

    const regex = /\d+:\s+\d+\s+(.+)\t\(-\d+\)/gm;
    const match = regex.exec(logString);
    if (!match) return;

    const exists = slimeWifiNetworks.find((network) => network.ssid === match[1]);
    if (!exists) {
      setSlimeWifiNetworks([
        ...slimeWifiNetworks,
        {
          ssid: match[1],
          source: 'slime',
          password: '',
          connected: false,
          signalStrength: null,
        },
      ]);
    }
  });

  return slimeWifiNetworks;
}

function populateNetworksWindows(setWifiNetworks: (networks: WifiNetwork[]) => void)
{
  let networksSaved: WifiNetwork[] = [];
  let networksScanned: WifiNetwork[] = [];

  getWifiNetworksWindowsSaved().then((networks) => {
    networksSaved = networks;
    setWifiNetworks([...networksSaved, ...networksScanned]);
  });

  getWifiNetworksWindowsScan().then((networks) => {
    networksScanned = networks;
    setWifiNetworks([...networksSaved, ...networksScanned]);
  });
}

function useWifiNetworksInternal() {
  const [wifiNetworks, setWifiNetworks] = useState<WifiNetwork[]>([]);

  useEffect(() => {
    os.type().then(async (platformName) => {
      switch (platformName) {
        case 'windows':
          populateNetworksWindows(setWifiNetworks);
          break;
        case 'linux':
          getWifiNetworksLinux().then((networks) => {
            setWifiNetworks(networks);
          });
          break;
        case 'macos':
          getWifiNetworksMac().then((networks) => {
            setWifiNetworks(networks);
          });
          break;
        default:
          console.log('Unsupported platform: ', platformName);
      }
    });
  }, []);

  return wifiNetworks;
}

export function useWifiNetworks() {
  const wifiNetworksSlimes = useWifiNetworksSlimes();
  const wifiNetworksInternal = useWifiNetworksInternal();

  const wifiNetworks = useMemo(() => {
    const networksConcat = [...wifiNetworksInternal, ...wifiNetworksSlimes];

    const ret = networksConcat.reduce((acc, network) => {
      const exists = acc.find((accNetwork) => accNetwork.ssid === network.ssid);

      if (!exists) {
        acc.push({
          ...network,
        });
      } else {
        if (!exists.password && network.password) {
          exists.password = network.password;
        }
      }

      return acc;
    }, [] as WifiNetwork[]);

    return ret.sort((a, b) => {
      return a.ssid.localeCompare(b.ssid);
    });
  }, [wifiNetworksInternal, wifiNetworksSlimes]);

  return {
    wifiNetworks,
  };
}
