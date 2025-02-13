export const DEG_TO_RAD = Math.PI / 180.0;

export const RAD_TO_DEG = 180.0 / Math.PI;

export const TWO_PI = 2.0 * Math.PI;

export function normalizeAngle(angle: number): number {
  if (angle < 0.0 || angle >= TWO_PI) {
    angle -= Math.floor(angle / TWO_PI) * TWO_PI;
  }
  return angle;
}

export function normalizeAngleAroundZero(angle: number): number {
  angle = normalizeAngle(angle);
  if (angle > Math.PI) {
    angle -= TWO_PI;
  }
  return angle;
}

export function angleIsNearZero(angle: number, maxError?: number): boolean {
  return Math.abs(angle) < (maxError || 1e-6);
}
