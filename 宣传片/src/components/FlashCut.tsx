import { AbsoluteFill, interpolate, useCurrentFrame } from 'remotion';

/**
 * 硬切点上的暖白闪：只盖切点两侧各 5 帧，不当装饰光效用。
 * from = 切点帧号 − 5。
 */
export const FlashCut: React.FC<{ from: number; color?: string }> = ({ from, color = '#fffdf8' }) => {
  const frame = useCurrentFrame();
  const opacity = interpolate(frame, [from, from + 5, from + 10], [0, 0.92, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  if (opacity <= 0) return null;
  return <AbsoluteFill style={{ backgroundColor: color, opacity, pointerEvents: 'none' }} />;
};
