import { interpolate, useCurrentFrame, Easing } from 'remotion';
import { ACCENT } from './TitleCard';

/**
 * 页面坐标系里的下划线标注：墨绿细线自左向右生长，左端落一个点。
 * 比"压白聚光"稳——不用 backdrop-filter，不会露出模糊层边界，
 * 也不动原图，只在要强调的那一行下面画一笔。
 */
export const Underline: React.FC<{
  x: number;
  y: number;
  w: number;
  from: number;
  duration?: number;
  thickness?: number;
  color?: string;
}> = ({ x, y, w, from, duration = 20, thickness = 2, color = ACCENT }) => {
  const frame = useCurrentFrame();
  const grow = interpolate(frame, [from, from + duration], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.22, 0.9, 0.24, 1),
  });
  const dot = interpolate(frame, [from - 4, from + 6], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  if (grow <= 0 && dot <= 0) return null;

  return (
    <>
      <div
        style={{
          position: 'absolute',
          left: x,
          top: y,
          width: w * grow,
          height: thickness,
          background: color,
          opacity: 0.9,
          borderRadius: thickness,
        }}
      />
      <div
        style={{
          position: 'absolute',
          left: x - 5,
          top: y + thickness / 2 - 5,
          width: 10,
          height: 10,
          borderRadius: 5,
          background: color,
          opacity: 0.9 * dot,
          transform: `scale(${0.4 + 0.6 * dot})`,
        }}
      />
    </>
  );
};
