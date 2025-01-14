import { useOnboarding } from '@/hooks/onboarding';
import { Localized, useLocalization } from '@fluent/react';
import { useMemo, useRef, useState } from 'react';
import classNames from 'classnames';
import { Typography } from '@/components/commons/Typography';
import { Button } from '@/components/commons/Button';
import {
  RpcMessage,
  SkeletonConfigRequestT,
  SkeletonBone,
  ChangeSkeletonConfigRequestT,
  SkeletonResetAllRequestT,
} from 'solarxr-protocol';
import { useWebsocketAPI } from '@/hooks/websocket-api';
import { save } from '@tauri-apps/plugin-dialog';
import { writeTextFile } from '@tauri-apps/plugin-fs';
import { useIsTauri } from '@/hooks/breakpoint';
import { useAppContext } from '@/hooks/app';
import { error } from '@/utils/logging';
import { fileOpen, fileSave } from 'browser-fs-access';
import { useDebouncedEffect } from '@/hooks/timeout';
import { ProportionsResetModal } from './ProportionsResetModal';

export const MIN_HEIGHT = 0.4;
export const MAX_HEIGHT = 4;
export const DEFAULT_HEIGHT = 1.5;
export const CURRENT_EXPORT_VERSION = 1;

enum ImportStatus {
  FAILED,
  SUCCESS,
  OK,
}

export function ProportionsChoose() {
  const isTauri = useIsTauri();
  const { l10n } = useLocalization();
  const { applyProgress, state } = useOnboarding();
  const { useRPCPacket, sendRPCPacket } = useWebsocketAPI();
  const [animated, setAnimated] = useState(false);
  const [showProportionWarning, setShowProportionWarning] = useState(false);
  const [importState, setImportState] = useState(ImportStatus.OK);
  const exporting = useRef(false);
  const { computedTrackers } = useAppContext();

  useDebouncedEffect(
    () => setImportState(ImportStatus.OK),
    [importState],
    2000
  );

  const hmdTracker = useMemo(
    () =>
      computedTrackers.find(
        (tracker) =>
          tracker.tracker.trackerId?.trackerNum === 1 &&
          tracker.tracker.trackerId.deviceId?.id === undefined
      ),
    [computedTrackers]
  );

  const beneathFloor = useMemo(
    () =>
      !(
        hmdTracker?.tracker.position &&
        hmdTracker.tracker.position.y >= MIN_HEIGHT
      ),
    [hmdTracker?.tracker.position?.y]
  );

  const importStatusKey = useMemo(() => {
    switch (importState) {
      case ImportStatus.FAILED:
        return 'onboarding-choose_proportions-import-failed';
      case ImportStatus.SUCCESS:
        return 'onboarding-choose_proportions-import-success';
      case ImportStatus.OK:
        return 'onboarding-choose_proportions-import';
    }
  }, [importState]);

  useRPCPacket(
    RpcMessage.SkeletonConfigResponse,
    (data: SkeletonConfigExport) => {
      if (!exporting.current) return;
      exporting.current = false;
      // Convert the skeleton part enums into a string
      data.skeletonParts.forEach((x) => {
        if (typeof x.bone === 'number')
          x.bone = SkeletonBone[x.bone] as SkeletonBoneKey;
      });
      data.version = CURRENT_EXPORT_VERSION;

      const blob = new Blob([JSON.stringify(data)], {
        type: 'application/json',
      });
      if (isTauri) {
        save({
          filters: [
            {
              name: l10n.getString('onboarding-choose_proportions-file_type'),
              extensions: ['json'],
            },
          ],
          defaultPath: 'body-proportions.json',
        })
          .then((path) =>
            path ? writeTextFile(path, JSON.stringify(data)) : undefined
          )
          .catch((err) => {
            error(err);
          });
      } else {
        fileSave(blob, {
          fileName: 'body-proportions.json',
          extensions: ['.json'],
        });
      }
    }
  );

  applyProgress(0.85);

  const onImport = async () => {
    const file = await fileOpen({
      mimeTypes: ['application/json'],
    });

    const text = await file.text();
    const config = JSON.parse(text) as SkeletonConfigExport;
    if (
      !config?.skeletonParts?.length ||
      !Array.isArray(config.skeletonParts)
    ) {
      error(
        'failed to import body proportions because skeletonParts is not an array/empty'
      );
      return setImportState(ImportStatus.FAILED);
    }

    for (const bone of [...config.skeletonParts]) {
      if (
        (typeof bone.bone === 'string' && !(bone.bone in SkeletonBone)) ||
        (typeof bone.bone === 'number' &&
          typeof SkeletonBone[bone.bone] !== 'string')
      ) {
        error(
          `failed to import body proportions because ${bone.bone} is not a valid bone`
        );
        return setImportState(ImportStatus.FAILED);
      }
    }

    parseConfigImport(config).forEach((req) =>
      sendRPCPacket(RpcMessage.ChangeSkeletonConfigRequest, req)
    );
    setImportState(ImportStatus.SUCCESS);
  };

  const resetAll = () => {
    sendRPCPacket(
      RpcMessage.SkeletonResetAllRequest,
      new SkeletonResetAllRequestT()
    );
  };

  return (
    <>
      <div className="flex flex-col gap-5 h-full items-center w-full xs:justify-center mobile:overflow-y-auto relative px-4 pb-4">
        <div className="flex flex-col gap-4 justify-center">
          <Typography variant="main-title">
            {l10n.getString('onboarding-choose_proportions')}
          </Typography>
          <div className="xs:w-10/12 xs:max-w-[666px]">
            <Localized
              id="onboarding-choose_proportions-description-v1"
              elems={{ b: <b className="text-base underline"></b> }}
            >
              <Typography
                variant="standard"
                color="secondary"
                whitespace="whitespace-pre-line"
              >
                How to measure your body!
              </Typography>
            </Localized>
          </div>
          <div
            className={classNames(
              'grid xs:grid-cols-2 w-full xs:flex-row mobile:flex-col gap-4 [&>div]:grow'
            )}
          >
            <div
              className={classNames(
                'rounded-lg p-4 flex flex-row flex-grow',
                !state.alonePage && 'bg-background-70',
                state.alonePage && 'bg-background-60'
              )}
            >
              <div className="flex flex-col gap-4">
                <div className="flex flex-grow flex-col gap-4 max-w-sm">
                  <div>
                    <Typography variant="main-title" bold>
                      {l10n.getString(
                        'onboarding-choose_proportions-manual_proportions'
                      )}
                    </Typography>
                    <Typography variant="vr-accessible" italic>
                      {l10n.getString(
                        'onboarding-choose_proportions-manual_proportions-subtitle'
                      )}
                    </Typography>
                  </div>
                  <div>
                    <Typography color="secondary">
                      {l10n.getString(
                        'onboarding-choose_proportions-manual_proportions-description'
                      )}
                    </Typography>
                  </div>
                </div>

                <Button
                  variant={!state.alonePage ? 'secondary' : 'tertiary'}
                  to="/onboarding/body-proportions/manual"
                  className="self-start mt-auto"
                  state={{ alonePage: state.alonePage }}
                >
                  {l10n.getString('onboarding-automatic_proportions-manual')}
                </Button>
              </div>
            </div>
            {state.alonePage && (
              <div
                className={classNames(
                  'rounded-lg p-4 flex flex-row relative',
                  !state.alonePage && 'bg-background-70',
                  state.alonePage && 'bg-background-60'
                )}
              >
                <div className="flex flex-col gap-4">
                  <div className="flex flex-grow flex-col gap-4 max-w-sm">
                    <img
                      onMouseEnter={() => setAnimated(() => true)}
                      onAnimationEnd={() => setAnimated(() => false)}
                      src="/images/slimetower.webp"
                      className={classNames(
                        'absolute w-[100px] -right-2 -top-24',
                        animated && 'animate-[bounce_1s_1]'
                      )}
                    ></img>
                    <div>
                      <Typography variant="main-title" bold>
                        {l10n.getString(
                          'onboarding-choose_proportions-auto_proportions'
                        )}
                      </Typography>
                      <Typography variant="vr-accessible" italic>
                        {l10n.getString(
                          'onboarding-choose_proportions-auto_proportions-subtitle'
                        )}
                      </Typography>
                    </div>
                    <div>
                      <Localized
                        id="onboarding-choose_proportions-auto_proportions-descriptionv3"
                        elems={{ b: <b></b> }}
                      >
                        <Typography
                          color="secondary"
                          whitespace="whitespace-pre-line"
                        >
                          Description for autobone
                        </Typography>
                      </Localized>
                    </div>
                  </div>
                  <Button
                    variant="primary"
                    // Check if we are in dev mode and just let it be used
                    disabled={beneathFloor && import.meta.env.PROD}
                    to="/onboarding/body-proportions/auto"
                    className="self-start mt-auto"
                    state={{ alonePage: state.alonePage }}
                  >
                    {l10n.getString('onboarding-manual_proportions-auto')}
                  </Button>
                </div>
              </div>
            )}
            {!state.alonePage && (
              <div
                className={classNames(
                  'rounded-lg p-4 flex flex-row relative',
                  !state.alonePage && 'bg-background-70',
                  state.alonePage && 'bg-background-60'
                )}
              >
                <div className="flex flex-col gap-4">
                  <div className="flex flex-grow flex-col gap-4 max-w-sm">
                    <img
                      onMouseEnter={() => setAnimated(() => true)}
                      onAnimationEnd={() => setAnimated(() => false)}
                      src="/images/slimetower.webp"
                      className={classNames(
                        'absolute w-[100px] -right-2 -top-24',
                        animated && 'animate-[bounce_1s_1]'
                      )}
                    ></img>
                    <div>
                      <Typography variant="main-title" bold>
                        {l10n.getString(
                          'onboarding-choose_proportions-scaled_proportions'
                        )}
                      </Typography>
                      <Typography variant="vr-accessible" italic>
                        {l10n.getString(
                          'onboarding-choose_proportions-scaled_proportions-subtitle'
                        )}
                      </Typography>
                    </div>
                    <div>
                      <Localized
                        id="onboarding-choose_proportions-scaled_proportions-description"
                        elems={{ b: <b></b> }}
                      >
                        <Typography
                          color="secondary"
                          whitespace="whitespace-pre-line"
                        >
                          Description for scaled proportions
                        </Typography>
                      </Localized>
                    </div>
                  </div>
                  <Button
                    variant="primary"
                    to="/onboarding/body-proportions/scaled"
                    className="self-start mt-auto"
                    state={{ alonePage: state.alonePage }}
                  >
                    {l10n.getString(
                      'onboarding-choose_proportions-scaled_proportions-button'
                    )}
                  </Button>
                </div>
              </div>
            )}
          </div>
          <div className="flex flex-row gap-3">
            {!state.alonePage && (
              <Button variant="secondary" to="/onboarding/reset-tutorial">
                {l10n.getString('onboarding-previous_step')}
              </Button>
            )}
            {state.alonePage && (
              <Button
                variant="secondary"
                onClick={() => setShowProportionWarning(true)}
              >
                {l10n.getString('reset-reset_all')}
              </Button>
            )}
            <ProportionsResetModal
              accept={() => {
                resetAll();
                setShowProportionWarning(false);
              }}
              onClose={() => setShowProportionWarning(false)}
              isOpen={showProportionWarning}
            ></ProportionsResetModal>
            <Button
              variant={!state.alonePage ? 'secondary' : 'tertiary'}
              className="ml-auto"
              onClick={() => {
                exporting.current = true;

                sendRPCPacket(
                  RpcMessage.SkeletonConfigRequest,
                  new SkeletonConfigRequestT()
                );
              }}
            >
              {l10n.getString('onboarding-choose_proportions-export')}
            </Button>
            <Button
              variant={!state.alonePage ? 'secondary' : 'tertiary'}
              className={classNames(
                'transition-colors',
                importState === ImportStatus.FAILED && 'bg-status-critical',
                importState === ImportStatus.SUCCESS && 'bg-status-success'
              )}
              onClick={onImport}
            >
              {l10n.getString(importStatusKey)}
            </Button>
          </div>
        </div>
      </div>
    </>
  );
}

function parseConfigImport(
  config: SkeletonConfigExport
): ChangeSkeletonConfigRequestT[] {
  if (!config.version) config.version = 1;
  if (config.version < 1) {
    // Add config migration stuff here, this one is just an example.
  }

  return config.skeletonParts.map((part) => {
    const bone =
      typeof part.bone === 'string' ? SkeletonBone[part.bone] : part.bone;

    return new ChangeSkeletonConfigRequestT(bone, part.value);
  });
}

type SkeletonBoneKey = keyof typeof SkeletonBone;

interface SkeletonConfigExport {
  version?: number;
  skeletonParts: {
    bone: SkeletonBoneKey | SkeletonBone;
    value: number;
  }[];
}
