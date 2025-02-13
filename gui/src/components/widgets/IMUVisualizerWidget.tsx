import { useEffect, useMemo, useState } from 'react';
import { TrackerDataT } from 'solarxr-protocol';
import { useTracker } from '@/hooks/tracker';
import { Typography } from '@/components/commons/Typography';
import { formatVector3 } from '@/utils/formatting';
import { Canvas } from '@react-three/fiber';
import * as THREE from 'three';
import { PerspectiveCamera, Vector3 } from 'three';
import { Button } from '@/components/commons/Button';
import { QuatObject } from '@/maths/quaternion';
import { useLocalization } from '@fluent/react';
import { Vector3Object } from '@/maths/vector3';
import { Gltf } from '@react-three/drei';
import { ErrorBoundary } from 'react-error-boundary';
import { StayAlignedInfo } from '@/components/tracker/StayAlignedInfo';

const groundColor = '#4444aa';

const scale = 6.5;

export function TrackerModel({ model }: { model: string }) {
  return (
    <group scale={scale} rotation={[Math.PI / 2, 0, 0]}>
      <Gltf src={model} />
    </group>
  );
}

function SceneRenderer({
  quat,
  vec,
  model,
}: {
  quat: QuatObject;
  vec: Vector3Object;
  model: string;
}) {
  return (
    <Canvas
      className="container"
      style={{ height: 200, background: 'transparent' }}
      onCreated={({ camera }) => {
        (camera as PerspectiveCamera).fov = 60;
      }}
    >
      <ambientLight intensity={0.5 * Math.PI} />
      <spotLight
        position={[20, 20, 20]}
        angle={0.09}
        penumbra={1}
        intensity={4000}
      />
      <group quaternion={[quat.x, quat.y, quat.z, quat.w]}>
        <TrackerModel model={model}></TrackerModel>
        <axesHelper args={[10]} />
      </group>

      <arrowHelper
        args={[
          new Vector3(-Math.sign(vec.x), 0, 0),
          new Vector3(0, 0, 0),
          Math.sqrt(Math.abs(vec.x)) * 2,
        ]}
      />
      <arrowHelper
        args={[
          new Vector3(0, Math.sign(vec.y), 0),
          new Vector3(0, 0, 0),
          Math.sqrt(Math.abs(vec.y)) * 2,
        ]}
      />
      <arrowHelper
        args={[
          new Vector3(0, 0, -Math.sign(vec.z)),
          new Vector3(0, 0, 0),
          Math.sqrt(Math.abs(vec.z)) * 2,
        ]}
      />

      <mesh position={[0, -3, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[50, 50, 10, 10]} />
        <meshBasicMaterial
          wireframe
          color={groundColor}
          transparent
          opacity={0.2}
          side={THREE.DoubleSide}
        />
      </mesh>
    </Canvas>
  );
}

export function IMUVisualizerWidget({ tracker }: { tracker: TrackerDataT }) {
  const { l10n } = useLocalization();
  const [enabled, setEnabled] = useState(false);
  const isExtension = useMemo(
    () => (tracker.trackerId?.trackerNum ?? 0) > 0,
    [tracker]
  );

  useEffect(() => {
    const state = localStorage.getItem('modelPreview');
    if (state) setEnabled(state === 'true');
  }, []);

  const { useRawRotationEulerDegrees, useIdentAdjRotationEulerDegrees } =
    useTracker(tracker);

  const rotationRaw = useRawRotationEulerDegrees();
  const rotationIdent = useIdentAdjRotationEulerDegrees() || rotationRaw;
  const quat =
    tracker?.rotationIdentityAdjusted ||
    tracker?.rotation ||
    new THREE.Quaternion();
  const vec =
    tracker?.linearAcceleration ||
    tracker?.rawAcceleration ||
    new THREE.Vector3();

  return (
    <div className="bg-background-70 flex flex-col p-3 rounded-lg gap-2">
      <Typography variant="section-title">
        {l10n.getString('widget-imu_visualizer')}
      </Typography>

      {tracker.position && (
        <div className="flex justify-between">
          <Typography color="secondary">
            {l10n.getString('widget-imu_visualizer-position')}
          </Typography>
          <Typography>{formatVector3(tracker.position, 2)}</Typography>
        </div>
      )}

      <div className="flex justify-between">
        <Typography color="secondary">
          {l10n.getString('widget-imu_visualizer-rotation_raw')}
        </Typography>
        <Typography>{formatVector3(rotationRaw, 2)}</Typography>
      </div>

      <div className="flex justify-between">
        <Typography color="secondary">
          {l10n.getString('widget-imu_visualizer-rotation_preview')}
        </Typography>
        <Typography>{formatVector3(rotationIdent, 2)}</Typography>
      </div>

      {tracker.linearAcceleration && (
        <div className="flex justify-between">
          <Typography color="secondary">
            {l10n.getString('widget-imu_visualizer-acceleration')}
          </Typography>
          <Typography>
            {formatVector3(tracker.linearAcceleration, 1)}
          </Typography>
        </div>
      )}

      {!!tracker.stayAlignedYawCorrectionInDeg && (
        <div className="flex justify-between">
          <Typography color="secondary">
            {l10n.getString('widget-imu_visualizer-stay_aligned')}
          </Typography>
          <StayAlignedInfo color="primary" tracker={tracker} />
        </div>
      )}

      {!enabled && (
        <Button
          variant="secondary"
          onClick={() => {
            setEnabled(true);
            localStorage.setItem('modelPreview', 'true');
          }}
        >
          {l10n.getString('widget-imu_visualizer-preview')}
        </Button>
      )}
      {enabled && (
        <>
          <Button
            variant="secondary"
            onClick={() => {
              setEnabled(false);
              localStorage.setItem('modelPreview', 'false');
            }}
          >
            {l10n.getString('widget-imu_visualizer-hide')}
          </Button>
          <ErrorBoundary
            fallback={
              <Typography color="primary" textAlign="text-center">
                {l10n.getString('tips-failed_webgl')}
              </Typography>
            }
          >
            <SceneRenderer
              quat={{ ...quat }}
              vec={{ ...vec }}
              model={
                isExtension ? '/models/extension.gltf' : '/models/tracker.gltf'
              }
            ></SceneRenderer>
          </ErrorBoundary>
        </>
      )}
    </div>
  );
}
