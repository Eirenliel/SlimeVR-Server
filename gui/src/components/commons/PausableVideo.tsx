import { useRef, useState } from 'react';
import { PlayCircleIcon } from './icon/PlayIcon';
import { useDebouncedEffect } from '@/hooks/timeout';
import classNames from 'classnames';

export function PausableVideo({
  src,
  poster,
  restartOnPause = false,
  autoPlay = false,
}: {
  src?: string;
  poster?: string;
  restartOnPause?: boolean;
  autoPlay?: boolean;
}) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const [paused, setPaused] = useState(!autoPlay);
  const [atStart, setAtStart] = useState(true);

  function toggleVideo() {
    if (!videoRef.current) return;
    if (videoRef.current.paused) {
      videoRef.current.play();
    } else {
      videoRef.current.pause();
      if (restartOnPause) {
        videoRef.current.currentTime = 0;
      }
      setAtStart(videoRef.current.currentTime === 0);
    }
    setPaused(videoRef.current.paused);
  }

  useDebouncedEffect(
    () => {
      if (paused) videoRef.current?.pause();
    },
    [paused],
    250
  );

  return (
    <button className="relative appearance-none" onClick={toggleVideo}>
      <div
        className={classNames(
          'absolute w-[100px] h-[100px] top-0 bottom-0 left-0 right-0 m-auto',
          'fill-background-20',
          paused && !atStart && 'opacity-50'
        )}
        hidden={!paused}
      >
        <PlayCircleIcon width={100}></PlayCircleIcon>
      </div>

      <video
        preload="auto"
        ref={videoRef}
        src={src}
        poster={poster}
        className="min-w-[12rem] w-[30rem]"
        muted
        loop
        playsInline
        autoPlay={autoPlay}
        controls={false}
      ></video>
    </button>
  );
}
