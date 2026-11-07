import { interpolate, useCurrentFrame, Easing } from 'remotion';
import { PAPER } from './TitleCard';

/**
 * 页面坐标系里的「条带聚光」：把目标行上下的内容压白 + 轻微失焦，
 * 目标行整行保持原样清晰。
 *
 * 用横向条带而不是椭圆：一行引用文字横跨近千像素，椭圆聚光会把两端压掉，
 * 条带才能让整行都读得清。
 */
export const Spotlight: React.FC<{
  y: number; // 目标行的页面 y 坐标（行中心）
  ry: number; // 清晰带半高
  from: number;
  duration?: number;
  strength?: number;
  blur?: number;
  pageH?: number;
}> = ({ y, ry, from, duration = 22, strength = 0.8, blur = 5, pageH = 1080 }) => {
  const frame = useCurrentFrame();
  const t = interpolate(frame, [from, from + duration], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.25, 0, 0.2, 1),
  });
  if (t <= 0) return null;

  const pct = (v: number) => `${(v / pageH) * 100}%`;
  const top = y - ry;
  const bottom = y + ry;
  const fade = 26;
  const mask = `linear-gradient(to bottom,
    rgba(0,0,0,1) 0%,
    rgba(0,0,0,0.98) ${pct(Math.max(0, top - fade))},
    rgba(0,0,0,0) ${pct(Math.max(0, top))},
    rgba(0,0,0,0) ${pct(bottom)},
    rgba(0,0,0,0.98) ${pct(Math.min(pageH, bottom + fade))},
    rgba(0,0,0,1) 100%)`;

  const base: React.CSSProperties = {
    position: 'absolute',
    left: 0,
    top: 0,
    width: 1920,
    height: pageH,
    maskImage: mask,
    WebkitMaskImage: mask,
    pointerEvents: 'none',
  };

  return (
    <>
      <div style={{ ...base, backgroundColor: PAPER, opacity: strength * t }} />
      <div
        style={{
          ...base,
          backdropFilter: `blur(${blur * t}px)`,
          WebkitBackdropFilter: `blur(${blur * t}px)`,
        }}
      />
    </>
  );
};
