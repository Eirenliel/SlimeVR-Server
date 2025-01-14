import { Button } from '@/components/commons/Button';
import { WarningBox } from '@/components/commons/TipBox';
import { Localized, useLocalization } from '@fluent/react';
import { BaseModal } from '@/components/commons/BaseModal';
import ReactModal from 'react-modal';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { useEffect, useState } from 'react';
import {
  RpcMessage,
  SettingsRequestT,
  SettingsResponseT,
} from 'solarxr-protocol';

export function ProportionsResetModal({
  isOpen = true,
  onClose,
  accept,
  ...props
}: {
  /**
   * Is the parent/sibling component opened?
   */
  isOpen: boolean;
  /**
   * Function to trigger when the warning hasn't been accepted
   */
  onClose: () => void;
  /**
   * Function when you press `Reset settings`
   */
  accept: () => void;
} & ReactModal.Props) {
  const { l10n } = useLocalization();
  const { useRPCPacket, sendRPCPacket } = useWebsocketAPI();
  const [usingDefaultHeight, setUsingDefaultHeight] = useState(true);

  useEffect(
    () => sendRPCPacket(RpcMessage.SettingsRequest, new SettingsRequestT()),
    []
  );
  useRPCPacket(RpcMessage.SettingsResponse, (res: SettingsResponseT) =>
    setUsingDefaultHeight(!res.modelSettings?.skeletonHeight?.hmdHeight)
  );

  return (
    <BaseModal
      isOpen={isOpen}
      shouldCloseOnOverlayClick
      onRequestClose={onClose}
      className={props.className}
      overlayClassName={props.overlayClassName}
    >
      <div className="flex w-full h-full flex-col ">
        <div className="flex flex-col flex-grow items-center gap-3">
          <Localized
            id={
              usingDefaultHeight
                ? 'reset-reset_all_warning_default'
                : 'reset-reset_all_warning'
            }
            elems={{ b: <b></b> }}
          >
            <WarningBox>
              <b>Warning:</b> This will reset your proportions to being just
              based on your height.
              <br />
              Are you sure you want to do this?
            </WarningBox>
          </Localized>

          <div className="flex flex-row gap-3 pt-5 place-content-center">
            <Button variant="primary" onClick={onClose}>
              {l10n.getString('reset-reset_all_warning-cancel')}
            </Button>
            <Button
              variant="tertiary"
              onClick={() => {
                accept();
              }}
            >
              {l10n.getString('reset-reset_all_warning-reset')}
            </Button>
          </div>
        </div>
      </div>
    </BaseModal>
  );
}
