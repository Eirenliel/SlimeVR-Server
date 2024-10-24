import { Button } from '@/components/commons/Button';
import { QuestionBox } from '@/components/commons/TipBox';
import { Localized, useLocalization } from '@fluent/react';
import { BaseModal } from '@/components/commons/BaseModal';
import ReactModal from 'react-modal';

export function CreateProfileModal({
  isOpen = true,
  onClose,
  primary,
  secondary,
  ...props
}: {
  /**
   * Is the parent/sibling component opened?
   */
  isOpen: boolean;
  /**
   * Function when closing/rejecting the modal
   */
  onClose: () => void;
  /**
   * Function for primary action
   */
  primary: () => void;
  /**
   * Function for secondary action
   */
  secondary: () => void;
} & ReactModal.Props) {
  const { l10n } = useLocalization();

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
          <Localized id="settings-utils-profiles-modal">
            <QuestionBox whitespace>
              Should the default settings or your current settings be used for
              the new profile?
            </QuestionBox>
          </Localized>

          <div className="flex flex-row gap-3 pt-5 place-content-center">
            <Button variant="tertiary" onClick={() => primary()}>
              {l10n.getString('settings-utils-profiles-modal-default')}
            </Button>
            <Button variant="tertiary" onClick={() => secondary()}>
              {l10n.getString('settings-utils-profiles-modal-copy')}
            </Button>
          </div>
          <Button variant="primary" onClick={onClose}>
            {l10n.getString('settings-utils-profiles-modal-cancel')}
          </Button>
        </div>
      </div>
    </BaseModal>
  );
}
