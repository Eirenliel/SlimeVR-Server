import { Button } from '@/components/commons/Button';
import { WarningBox } from '@/components/commons/TipBox';
import { Localized, useLocalization } from '@fluent/react';
import { BaseModal } from '@/components/commons/BaseModal';
import ReactModal from 'react-modal';
import { useNavigate } from 'react-router-dom';

export function SkipSetupWarningModal({
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
   * Function when you press `i understand`
   */
  accept: () => void;
} & ReactModal.Props) {
  const { l10n } = useLocalization();
  const navigate = useNavigate();

  return (
    <BaseModal
      isOpen={isOpen}
      shouldCloseOnOverlayClick
      shouldCloseOnEsc
      onRequestClose={onClose}
      className={props.className}
      overlayClassName={props.overlayClassName}
    >
      <div className="flex w-full h-full flex-col ">
        <div className="flex flex-col flex-grow items-center gap-3">
          <Localized id="onboarding-setup_warning" elems={{ b: <b></b> }}>
            <WarningBox>
              <b>Warning:</b> The setup is needed for good tracking, this is
              required if this is your first time using SlimeVR.
            </WarningBox>
          </Localized>

          <div className="flex flex-col gap-3 pt-5 place-content-center">
            <Button variant="primary" onClick={onClose}>
              {l10n.getString('onboarding-setup_warning-cancel')}
            </Button>
            <Button
              variant="tertiary"
              onClick={() => {
                accept();
                navigate('/');
              }}
            >
              {l10n.getString('onboarding-setup_warning-skip')}
            </Button>
          </div>
        </div>
      </div>
    </BaseModal>
  );
}
