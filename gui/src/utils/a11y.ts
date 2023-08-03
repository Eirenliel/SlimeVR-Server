import { DialogFilter, save } from '@tauri-apps/api/dialog';
import { error } from './logging';
import saveAs from 'file-saver';
import { writeBinaryFile } from '@tauri-apps/api/fs';
import { ComputerDirectory } from 'solarxr-protocol';
import { documentDir } from '@tauri-apps/api/path';

export function a11yClick(event: React.KeyboardEvent | React.MouseEvent) {
  if (event.type === 'click') {
    return true;
  } else if (event.type === 'keydown') {
    const keyboard = event as React.KeyboardEvent;
    return keyboard.key === 'Enter' || keyboard.key === ' ';
  }
}

export async function saveFile({
  isTauri,
  file,
  filters,
  defaultPath,
}: {
  isTauri: boolean;
  file: File;
  filters?: DialogFilter[];
  defaultPath?: string;
}): Promise<void> {
  if (isTauri) {
    await save({
      filters,
      defaultPath: defaultPath ? `${defaultPath}/${file.name}` : file.name,
    })
      .then(async (path) =>
        path ? writeBinaryFile(path, await file.arrayBuffer()) : undefined
      )
      .catch((err) => {
        error(err);
      });
  } else {
    if ('share' in navigator) {
      let canShare: boolean | null = null;
      if ('canShare' in navigator) {
        canShare = navigator.canShare({ files: [file] });
      }
      if (canShare || canShare === null) {
        const shared = await navigator
          .share({ files: [file] })
          .then(() => true)
          .catch((err) => {
            error(err);
            return false;
          });

        if (shared) return;
      }
    }
    saveAs(file, file.name);
  }
}

export function resolveDir(dir: ComputerDirectory): Promise<string> {
  switch(dir) {
    case ComputerDirectory.Documents:
      return documentDir();
  }
}
