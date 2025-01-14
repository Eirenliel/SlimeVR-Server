import { BaseModal } from '@/components/commons/BaseModal';
import { Button } from '@/components/commons/Button';
import { WarningBox } from '@/components/commons/TipBox';
import { useHeightContext } from '@/hooks/height';
import { useLocaleConfig } from '@/i18n/config';
import { Localized, useLocalization } from '@fluent/react';
import { useMemo } from 'react';
import { MIN_HEIGHT } from '@/components/onboarding/pages/body-proportions/ProportionsChoose';

export function TooSmolModal({
  isOpen = true,
  onClose,
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
} & ReactModal.Props) {
  const { l10n } = useLocalization();
  const { hmdHeight, floorHeight } = useHeightContext();
  const { currentLocales } = useLocaleConfig();

  const mFormat = useMemo(
    () =>
      new Intl.NumberFormat(currentLocales, {
        style: 'unit',
        unit: 'meter',
        maximumFractionDigits: 2,
      }),
    [currentLocales]
  );

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
          <Localized
            id="onboarding-automatic_proportions-smol_warning"
            elems={{ b: <b></b> }}
            vars={{
              height: mFormat.format(hmdHeight ?? 0 - (floorHeight ?? 0)),
              minHeight: mFormat.format(MIN_HEIGHT),
            }}
          >
            <WarningBox whitespace>
              <b>Warning:</b> You are too smol to continue
            </WarningBox>
          </Localized>

          <div className="flex flex-col gap-3 pt-5 place-content-center">
            <Button variant="primary" onClick={onClose}>
              {l10n.getString(
                'onboarding-automatic_proportions-smol_warning-cancel'
              )}
            </Button>
          </div>
        </div>
      </div>
    </BaseModal>
  );
}
