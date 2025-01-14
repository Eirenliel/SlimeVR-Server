import { createContext, useContext, useEffect, useState } from 'react';
import { useWebsocketAPI } from './websocket-api';
import { RpcMessage, SettingsRequestT, SettingsResponseT } from 'solarxr-protocol';
import { MIN_HEIGHT } from '@/components/onboarding/pages/body-proportions/ProportionsChoose';

export interface HeightContext {
  hmdHeight: number | null;
  setHmdHeight: React.Dispatch<React.SetStateAction<number | null>>;
  floorHeight: number | null;
  setFloorHeight: React.Dispatch<React.SetStateAction<number | null>>;
  validateHeight: (
    hmdHeight: number | null | undefined,
    floorHeight: number | null | undefined
  ) => boolean;
}

export function useProvideHeightContext(): HeightContext {
  const [hmdHeight, setHmdHeight] = useState<number | null>(null);
  const [floorHeight, setFloorHeight] = useState<number | null>(null);
  const { sendRPCPacket, useRPCPacket } = useWebsocketAPI();

  function validateHeight(
    hmdHeight: number | null | undefined,
    floorHeight: number | null | undefined
  ) {
    return (
      hmdHeight !== undefined &&
      hmdHeight !== null &&
      hmdHeight - (floorHeight ?? 0) > MIN_HEIGHT
    );
  }

  useEffect(
    () => sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT()),
    []
  );
  useRPCPacket(RpcMessage.SettingsResponse, (res: SettingsResponseT) => {
    const hmd = res.modelSettings?.skeletonHeight?.hmdHeight;
    const floor = res.modelSettings?.skeletonHeight?.floorHeight;

    if (validateHeight(hmd, floor)) {
      setHmdHeight(hmd ?? null);
      setFloorHeight(floor ?? null);
    }
  });

  return { hmdHeight, setHmdHeight, floorHeight, setFloorHeight, validateHeight };
}

export const HeightContextC = createContext<HeightContext>(undefined as never);

export function useHeightContext() {
  const context = useContext(HeightContextC);
  if (!context) {
    throw new Error('useHeightContext must be within a HeightContext Provider');
  }
  return context;
}
