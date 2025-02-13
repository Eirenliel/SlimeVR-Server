import { useLocalization } from '@fluent/react';
import classNames from 'classnames';
import { IPv4 } from 'ip-num/IPNumber';
import { MouseEventHandler, ReactNode, useMemo, useState } from 'react';
import {
  TrackerDataT,
  TrackerIdT,
  TrackerStatus as TrackerStatusEnum,
} from 'solarxr-protocol';
import { FlatDeviceTracker } from '@/hooks/app';
import { useConfig } from '@/hooks/config';
import { useTracker } from '@/hooks/tracker';
import { BodyPartIcon } from '@/components/commons/BodyPartIcon';
import { Typography } from '@/components/commons/Typography';
import { formatVector3 } from '@/utils/formatting';
import { TrackerBattery } from './TrackerBattery';
import { TrackerStatus } from './TrackerStatus';
import { TrackerWifi } from './TrackerWifi';
import { trackerStatusRelated, useStatusContext } from '@/hooks/status-system';
import { StayAlignedInfo } from './StayAlignedInfo';

enum DisplayColumn {
  NAME,
  TYPE,
  BATTERY,
  PING,
  TPS,
  ROTATION,
  TEMPERATURE,
  LINEAR_ACCELERATION,
  POSITION,
  STAY_ALIGNED,
  URL,
}

const displayColumns: { [k: string]: boolean } = {
  [DisplayColumn.NAME]: true,
  [DisplayColumn.TYPE]: true,
  [DisplayColumn.BATTERY]: true,
  [DisplayColumn.PING]: true,
  [DisplayColumn.TPS]: true,
  [DisplayColumn.ROTATION]: true,
  [DisplayColumn.TEMPERATURE]: true,
  [DisplayColumn.LINEAR_ACCELERATION]: true,
  [DisplayColumn.POSITION]: true,
  [DisplayColumn.STAY_ALIGNED]: true,
  [DisplayColumn.URL]: true,
};

const isSlime = ({ device }: FlatDeviceTracker) =>
  device?.hardwareInfo?.manufacturer === 'SlimeVR' ||
  device?.hardwareInfo?.manufacturer === 'HID Device';

const getDeviceName = ({ device }: FlatDeviceTracker) =>
  device?.customName?.toString() || '';

const getTrackerName = ({ tracker }: FlatDeviceTracker) =>
  tracker?.info?.customName?.toString() || '';

export function TrackerNameCell({ tracker }: { tracker: TrackerDataT }) {
  const { useName } = useTracker(tracker);

  const name = useName();

  return (
    <div className="flex flex-row gap-2">
      <div className="flex flex-col justify-center items-center fill-background-10">
        <BodyPartIcon bodyPart={tracker.info?.bodyPart}></BodyPartIcon>
      </div>
      <div className="flex flex-col flex-grow">
        <Typography bold whitespace="whitespace-nowrap">
          {name}
        </Typography>
        <TrackerStatus status={tracker.status}></TrackerStatus>
      </div>
    </div>
  );
}

export function TrackerRotCell({
  tracker,
  precise,
  color,
  referenceAdjusted,
}: {
  tracker: TrackerDataT;
  precise?: boolean;
  color?: string;
  referenceAdjusted?: boolean;
}) {
  const { useRawRotationEulerDegrees, useRefAdjRotationEulerDegrees } =
    useTracker(tracker);

  const rotationRaw = useRawRotationEulerDegrees();
  const rotationRef = useRefAdjRotationEulerDegrees() || rotationRaw;
  const rot = referenceAdjusted ? rotationRef : rotationRaw;

  return (
    <Typography color={color} whitespace="whitespace-nowrap">
      {formatVector3(rot, precise ? 2 : 0)}
    </Typography>
  );
}

export function RowContainer({
  children,
  rounded = 'none',
  hover,
  tracker,
  onClick,
  onMouseOver,
  onMouseOut,
  warning,
}: {
  children: ReactNode;
  rounded?: 'left' | 'right' | 'none';
  hover: boolean;
  tracker: TrackerDataT;
  onClick?: MouseEventHandler<HTMLDivElement>;
  onMouseOver?: MouseEventHandler<HTMLDivElement>;
  onMouseOut?: MouseEventHandler<HTMLDivElement>;
  warning: boolean;
}) {
  const { useVelocity } = useTracker(tracker);

  const velocity = useVelocity();

  return (
    <div
      className={classNames(
        'py-1',
        rounded === 'left' && 'pl-3',
        rounded === 'right' && 'pr-3',
        'overflow-hidden'
      )}
    >
      <div
        onClick={onClick}
        onMouseEnter={onMouseOver}
        onMouseLeave={onMouseOut}
        style={{
          boxShadow: `0px 0px ${Math.floor(velocity * 8)}px ${Math.floor(
            velocity * 8
          )}px rgb(var(--accent-background-30))`,
        }}
        className={classNames(
          'h-[50px]  flex flex-col justify-center px-3',
          rounded === 'left' && 'rounded-l-lg',
          rounded === 'right' && 'rounded-r-lg',
          hover ? 'bg-background-50 cursor-pointer' : 'bg-background-60',
          warning && 'border-status-warning border-solid border-t-2 border-b-2',
          rounded === 'left' && warning && 'border-l-2',
          rounded === 'right' && warning && 'border-r-2'
        )}
      >
        {children}
      </div>
    </div>
  );
}

export function TrackersTable({
  flatTrackers,
  clickedTracker,
}: {
  clickedTracker: (tracker: TrackerDataT) => void;
  flatTrackers: FlatDeviceTracker[];
}) {
  const { l10n } = useLocalization();
  const [hoverTracker, setHoverTracker] = useState<TrackerIdT | null>(null);
  const { config } = useConfig();
  const { statuses } = useStatusContext();

  const trackerEqual = (id: TrackerIdT | null) =>
    id?.trackerNum == hoverTracker?.trackerNum &&
    (!id?.deviceId || id.deviceId.id == hoverTracker?.deviceId?.id);

  const filteringEnabled =
    config?.debug && config?.devSettings?.filterSlimesAndHMD;
  const sortingEnabled = config?.debug && config?.devSettings?.sortByName;
  // TODO: fix memo
  const filteredSortedTrackers = useMemo(() => {
    const list = filteringEnabled
      ? flatTrackers.filter((t) => getDeviceName(t) === 'HMD' || isSlime(t))
      : flatTrackers;

    if (sortingEnabled) {
      list.sort((a, b) => getTrackerName(a).localeCompare(getTrackerName(b)));
    }
    return list;
  }, [flatTrackers, filteringEnabled, sortingEnabled]);

  const fontColor = config?.devSettings?.highContrast ? 'primary' : 'secondary';
  const moreInfo = config?.devSettings?.moreInfo;

  const hasTemperature = !!filteredSortedTrackers.find(
    ({ tracker }) => tracker?.temp && tracker?.temp?.temp != 0
  );
  displayColumns[DisplayColumn.TEMPERATURE] = hasTemperature || false;
  displayColumns[DisplayColumn.POSITION] = moreInfo || false;
  displayColumns[DisplayColumn.LINEAR_ACCELERATION] = moreInfo || false;
  displayColumns[DisplayColumn.STAY_ALIGNED] = moreInfo || false;
  displayColumns[DisplayColumn.URL] = moreInfo || false;
  const displayColumnsKeys = Object.keys(displayColumns).filter(
    (k) => displayColumns[k]
  );
  const firstColumnId = +displayColumnsKeys[0];
  const lastColumnId = +displayColumnsKeys[displayColumnsKeys.length - 1];

  function column({
    id,
    label,
    labelClassName,
    row,
  }: {
    id: DisplayColumn;
    label: string;
    labelClassName?: string;
    row: (data: FlatDeviceTracker) => ReactNode | null;
  }) {
    let rounded: 'left' | 'right' | 'none' = 'none';
    if (firstColumnId === id) rounded = 'left';
    else if (lastColumnId === id) rounded = 'right';

    if (!displayColumns[id]) return <></>;

    return (
      <div
        className={classNames('flex flex-col gap-1', {
          'flex-grow': lastColumnId === id,
        })}
      >
        <div className={`flex px-3 whitespace-nowrap ${labelClassName}`}>
          {label}
        </div>
        {filteredSortedTrackers.map((data, index) => (
          <RowContainer
            rounded={rounded}
            key={index}
            tracker={data.tracker}
            onClick={() => clickedTracker(data.tracker)}
            hover={trackerEqual(data.tracker.trackerId)}
            onMouseOver={() => setHoverTracker(data.tracker.trackerId)}
            onMouseOut={() => setHoverTracker(null)}
            warning={Object.values(statuses).some((status) =>
              trackerStatusRelated(data.tracker, status)
            )}
          >
            {row(data) || <></>}
          </RowContainer>
        ))}
      </div>
    );
  }

  return (
    <div className="flex w-full overflow-x-auto py-2">
      {column({
        id: DisplayColumn.NAME,
        label: l10n.getString('tracker-table-column-name'),
        row: ({ tracker }) => (
          <TrackerNameCell tracker={tracker}></TrackerNameCell>
        ),
      })}

      {column({
        id: DisplayColumn.TYPE,
        label: l10n.getString('tracker-table-column-type'),
        row: ({ device }) => (
          <Typography color={fontColor}>
            {device?.hardwareInfo?.manufacturer || '--'}
          </Typography>
        ),
      })}

      {column({
        id: DisplayColumn.BATTERY,
        label: l10n.getString('tracker-table-column-battery'),
        row: ({ device, tracker }) =>
          device?.hardwareStatus?.batteryPctEstimate && (
            <TrackerBattery
              value={device.hardwareStatus.batteryPctEstimate / 100}
              voltage={device.hardwareStatus?.batteryVoltage}
              disabled={tracker.status === TrackerStatusEnum.DISCONNECTED}
              textColor={fontColor}
            />
          ),
      })}

      {column({
        id: DisplayColumn.PING,
        label: l10n.getString('tracker-table-column-ping'),
        row: ({ device, tracker }) =>
          (device?.hardwareStatus?.rssi != null ||
            device?.hardwareStatus?.ping != null) && (
            <TrackerWifi
              rssi={device?.hardwareStatus?.rssi || 0}
              rssiShowNumeric
              ping={device?.hardwareStatus?.ping || 0}
              disabled={tracker.status === TrackerStatusEnum.DISCONNECTED}
              textColor={fontColor}
            ></TrackerWifi>
          ),
      })}

      {column({
        id: DisplayColumn.TPS,
        label: l10n.getString('tracker-table-column-tps'),
        row: ({ tracker }) => (
          <Typography color={fontColor}>
            {tracker?.tps != null ? <>{tracker.tps}</> : <></>}
          </Typography>
        ),
      })}

      {column({
        id: DisplayColumn.ROTATION,
        label: l10n.getString('tracker-table-column-rotation'),
        labelClassName: classNames({
          'w-44': config?.devSettings?.preciseRotation,
          'w-32': !config?.devSettings?.preciseRotation,
        }),
        row: ({ tracker }) => (
          <TrackerRotCell
            tracker={tracker}
            precise={config?.devSettings?.preciseRotation}
            referenceAdjusted={!config?.devSettings?.rawSlimeRotation}
            color={fontColor}
          />
        ),
      })}

      {column({
        id: DisplayColumn.TEMPERATURE,
        label: l10n.getString('tracker-table-column-temperature'),
        row: ({ tracker }) =>
          tracker?.temp &&
          tracker?.temp?.temp != 0 && (
            <Typography color={fontColor} whitespace="whitespace-nowrap">
              {`${tracker.temp.temp.toFixed(2)}`}
            </Typography>
          ),
      })}

      {column({
        id: DisplayColumn.LINEAR_ACCELERATION,
        label: l10n.getString('tracker-table-column-linear-acceleration'),
        labelClassName: 'w-36',
        row: ({ tracker }) =>
          tracker.linearAcceleration && (
            <Typography color={fontColor} whitespace="whitespace-nowrap">
              {formatVector3(tracker.linearAcceleration, 1)}
            </Typography>
          ),
      })}

      {column({
        id: DisplayColumn.POSITION,
        label: l10n.getString('tracker-table-column-position'),
        labelClassName: 'w-36',
        row: ({ tracker }) =>
          tracker.position && (
            <Typography color={fontColor} whitespace="whitespace-nowrap">
              {formatVector3(tracker.position, 2)}
            </Typography>
          ),
      })}

      {column({
        id: DisplayColumn.STAY_ALIGNED,
        label: l10n.getString('tracker-table-column-stay_aligned'),
        labelClassName: 'w-36',
        row: ({ tracker }) => (
          <StayAlignedInfo color={fontColor} tracker={tracker} />
        ),
      })}

      {column({
        id: DisplayColumn.URL,
        label: l10n.getString('tracker-table-column-url'),
        row: ({ device }) => (
          <Typography color={fontColor} whitespace="whitespace-nowrap">
            udp://
            {IPv4.fromNumber(
              device?.hardwareInfo?.ipAddress?.addr || 0
            ).toString()}
          </Typography>
        ),
      })}
    </div>
  );
}
