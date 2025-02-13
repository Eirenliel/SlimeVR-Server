import { Typography } from '@/components/commons/Typography';
import { useLocaleConfig } from '@/i18n/config';
import { angleIsNearZero } from '@/maths/angle';
import { TrackerDataT } from 'solarxr-protocol';

export function StayAlignedInfo({
  color,
  tracker,
}: {
  color: 'primary' | 'secondary';
  tracker: TrackerDataT;
}) {
  const { currentLocales } = useLocaleConfig();
  const degreeFormat = new Intl.NumberFormat(currentLocales, {
    style: 'unit',
    unit: 'degree',
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });
  const errorFormat = new Intl.NumberFormat(currentLocales, {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });

  const locked = tracker.stayAlignedLocked ? '🔒' : '';

  const delta = `Δ=${degreeFormat.format(tracker.stayAlignedYawCorrectionInDeg)}`;

  const errors = [];
  const maxErrorToShow = 0.1;
  if (!angleIsNearZero(tracker.stayAlignedLockedErrorInDeg, maxErrorToShow)) {
    errors.push(`L=${errorFormat.format(tracker.stayAlignedLockedErrorInDeg)}`);
  }
  if (!angleIsNearZero(tracker.stayAlignedCenterErrorInDeg, maxErrorToShow)) {
    errors.push(`C=${errorFormat.format(tracker.stayAlignedCenterErrorInDeg)}`);
  }
  if (!angleIsNearZero(tracker.stayAlignedNeighborErrorInDeg, maxErrorToShow)) {
    errors.push(
      `N=${errorFormat.format(tracker.stayAlignedNeighborErrorInDeg)}`
    );
  }

  const error = errors.length > 0 ? `(${errors.join(', ')})` : '';

  return (
    <Typography color={color} whitespace="whitespace-nowrap">
      {locked} {delta} {error}
    </Typography>
  );
}
