import { useLocalization } from '@fluent/react';
import { useMemo } from 'react';
import {
  ResetRequestT,
  ResetType,
  RpcMessage,
  StatusData,
} from 'solarxr-protocol';
import { useConfig } from '@/hooks/config';
import { useCountdown } from '@/hooks/countdown';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { playSoundOnResetStarted } from '@/sounds/sounds';
import { BigButton } from '@/components/commons/BigButton';
import { Button } from '@/components/commons/Button';
import {
  MountingResetIcon,
  YawResetIcon,
  FullResetIcon,
} from '@/components/commons/icon/ResetIcon';
import { useStatusContext } from '@/hooks/status-system';

export function ResetButton({
  type,
  variant = 'big',
  onReseted,
}: {
  type: ResetType;
  variant: 'big' | 'small';
  onReseted?: () => void;
}) {
  const { l10n } = useLocalization();
  const { sendRPCPacket } = useWebsocketAPI();
  const { statuses } = useStatusContext();
  const { config } = useConfig();

  const needsFullReset = useMemo(
    () =>
      type === ResetType.Mounting &&
      Object.values(statuses).some(
        (status) => status.dataType === StatusData.StatusTrackerReset
      ),
    [statuses]
  );

  const reset = () => {
    const req = new ResetRequestT();
    req.resetType = type;
    sendRPCPacket(RpcMessage.ResetRequest, req);
  };

  const { isCounting, startCountdown, timer } = useCountdown({
    duration: type === ResetType.Yaw ? 0.2 : undefined,
    onCountdownEnd: () => {
      reset();
      if (onReseted) onReseted();
    },
  });

  const text = useMemo(() => {
    switch (type) {
      case ResetType.Yaw:
        return l10n.getString('reset-yaw');
      case ResetType.Mounting:
        return l10n.getString('reset-mounting');
      case ResetType.Full:
        return l10n.getString('reset-full');
    }
  }, [type]);

  const getIcon = () => {
    switch (type) {
      case ResetType.Yaw:
        return <YawResetIcon width={20} />;
      case ResetType.Mounting:
        return <MountingResetIcon width={20} />;
    }
    return <FullResetIcon width={20} />;
  };

  const maybePlaySoundOnResetStarted = (type: ResetType) => {
    if (!config?.feedbackSound) return;
    playSoundOnResetStarted(type, config?.feedbackSoundVolume);
  };

  return variant === 'small' ? (
    <Button
      icon={getIcon()}
      onClick={() => {
        startCountdown();
        maybePlaySoundOnResetStarted(type);
      }}
      variant="primary"
      disabled={isCounting || needsFullReset}
    >
      <div className="relative">
        <div className="opacity-0 h-0">{text}</div>
        {!isCounting || type === ResetType.Yaw ? text : String(timer)}
      </div>
    </Button>
  ) : (
    <BigButton
      text={!isCounting || type === ResetType.Yaw ? text : String(timer)}
      icon={getIcon()}
      onClick={() => {
        startCountdown();
        maybePlaySoundOnResetStarted(type);
      }}
      disabled={isCounting || needsFullReset}
    ></BigButton>
  );
}
