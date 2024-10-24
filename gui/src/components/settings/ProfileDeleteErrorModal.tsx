import { Button } from '@/components/commons/Button';
import { ErrorBox } from '@/components/commons/TipBox';
import { Localized, useLocalization } from '@fluent/react';
import { BaseModal } from '@/components/commons/BaseModal';
import ReactModal from 'react-modal';

export function ProfileDeleteErrorModal({
  isOpen = true,
  onClose,
  profile,
  ...props
}: {
  /**
   * Is the parent/sibling component opened?
   */
  isOpen: boolean;
  /**
   * Function to run to dismiss the modal
   */
  onClose: () => void;
  profile: string;
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
      <div className="flex w-full h-full flex-col">
        <div className="flex flex-col flex-grow items-center gap-3">
          <Localized
            id="settings-utils-profiles-delete-cant"
            elems={{ b: <b></b> }}
            vars={{ name: profile }}
          >
            <ErrorBox>
              You cannot delete this profile ({profile}). Please select another
              profile to delete.
            </ErrorBox>
          </Localized>

          <div className="flex flex-row gap-3 pt-5 place-content-center">
            <Button variant="primary" onClick={onClose}>
              {l10n.getString('settings-utils-profiles-delete-cant-ok')}
            </Button>
          </div>
        </div>
      </div>
    </BaseModal>
  );
}
