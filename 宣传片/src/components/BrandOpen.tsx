import { AbsoluteFill, interpolate, useCurrentFrame, Easing } from 'remotion';
import { ACCENT, INK, MUTED, PAPER, SANS, SERIF } from './TitleCard';

/**
 * 品牌开场：一条墨线自中心向两侧拉开，字标逐字压印落定，
 * 副标随后淡入，落定后留呼吸（≥30f hold）。
 */
export const BrandOpen: React.FC<{ duration: number }> = ({ duration }) => {
  const frame = useCurrentFrame();

  const line = interpolate(frame, [0, 16], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.2, 0.8, 0.2, 1),
  });

  const chars = [...'小智医疗'];
  const subtitle = 'AI 医疗问答 · 分导诊 · 预约挂号';

  const subT = interpolate(frame, [30, 44], [0, 1], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
  const subY = interpolate(frame, [30, 44], [14, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
  const exit = interpolate(frame, [duration - 6, duration], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <AbsoluteFill style={{ backgroundColor: PAPER, justifyContent: 'center', alignItems: 'center', opacity: exit }}>
      <div
        style={{
          position: 'absolute',
          inset: 0,
          backgroundImage: 'radial-gradient(900px 620px at 50% 46%, rgba(255,255,255,0.95), transparent 70%)',
        }}
      />

      <div style={{ position: 'relative', textAlign: 'center' }}>
        {/* 墨线 */}
        <div
          style={{
            width: 420,
            height: 3,
            margin: '0 auto 48px',
            background: ACCENT,
            opacity: 0.9,
            transform: `scaleX(${line})`,
          }}
        />

        {/* 字标：逐字压印 */}
        <div style={{ display: 'flex', justifyContent: 'center', columnGap: '0.06em' }}>
          {chars.map((c, i) => {
            const d = 8 + i * 6;
            const t = interpolate(frame, [d, d + 14], [0, 1], {
              extrapolateLeft: 'clamp',
              extrapolateRight: 'clamp',
              easing: Easing.bezier(0.16, 0.9, 0.24, 1),
            });
            return (
              <span
                key={i}
                style={{
                  fontFamily: SERIF,
                  fontSize: 150,
                  fontWeight: 700,
                  color: INK,
                  letterSpacing: '0.02em',
                  display: 'inline-block',
                  opacity: t,
                  transform: `translateY(${(1 - t) * 10}px) scale(${1.14 - 0.14 * t})`,
                  filter: `blur(${(1 - t) * 9}px)`,
                }}
              >
                {c}
              </span>
            );
          })}
        </div>

        {/* 副标逐字淡入 */}
        <div
          style={{
            fontFamily: SANS,
            fontSize: 30,
            letterSpacing: '0.26em',
            color: MUTED,
            marginTop: 40,
            transform: `translateY(${subY}px)`,
          }}
        >
          {[...subtitle].map((c, i) => {
            const d = 30 + i * 1.1;
            const t = interpolate(frame, [d, d + 12], [0, 1], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
            return (
              <span key={i} style={{ opacity: t }}>
                {c}
              </span>
            );
          })}
        </div>
      </div>
    </AbsoluteFill>
  );
};
