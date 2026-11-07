import { interpolate, useCurrentFrame, Easing } from 'remotion';
import { ACCENT, MUTED, SANS } from './TitleCard';

/**
 * 底部通栏解说：等宽小字 + 宽字距，入出场各带一次轻推。
 * 压在界面段上，用来把产品链路讲清楚。
 */
export const Caption: React.FC<{ duration: number; text: string; accent?: string }> = ({
  duration,
  text,
  accent,
}) => {
  const frame = useCurrentFrame();
  const IN = 10;
  const OUT = 8;

  const tIn = interpolate(frame, [0, IN], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.2, 0.8, 0.2, 1),
  });
  const tOut = interpolate(frame, [duration - OUT, duration], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const opacity = tIn * tOut;
  const y = (1 - tIn) * 12;

  return (
    <div
      style={{
        position: 'absolute',
        left: 0,
        right: 0,
        bottom: 108,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 18,
        opacity,
        transform: `translateY(${y}px)`,
      }}
    >
      <div style={{ width: 46, height: 1, background: accent ?? ACCENT, opacity: 0.5 }} />
      <div
        style={{
          fontFamily: SANS,
          fontSize: 24,
          letterSpacing: '0.2em',
          color: '#4a4844',
          fontWeight: 400,
        }}
      >
        {text}
      </div>
      <div style={{ width: 46, height: 1, background: accent ?? ACCENT, opacity: 0.5 }} />
    </div>
  );
};
