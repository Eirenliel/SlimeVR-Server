import classNames from 'classnames';
import { ReactNode } from 'react';
import ReactModal from 'react-modal';

export function BaseModal({
  children,
  important = false,
  closeable = true,
  ...props
}: {
  isOpen: boolean;
  children: ReactNode;
  important?: boolean;
  closeable?: boolean;
} & ReactModal.Props) {
  return (
    <ReactModal
      {...props}
      shouldCloseOnOverlayClick={closeable}
      shouldCloseOnEsc={closeable}
      overlayClassName={
        props.overlayClassName ||
        classNames(
          'fixed top-0 right-0 left-0 bottom-0 flex flex-col justify-center',
          'items-center w-full h-full bg-background-90 bg-opacity-60',
          important ? 'z-50' : 'z-40'
        )
      }
      className={
        props.className ||
        classNames(
          'items-center focus:ring-transparent focus:ring-offset-transparent',
          'focus:outline-transparent outline-none bg-background-60 p-6 rounded-lg m-2',
          'text-background-10'
        )
      }
    >
      {children}
    </ReactModal>
  );
}
