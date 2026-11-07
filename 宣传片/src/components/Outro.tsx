import { AbsoluteFill, Img, interpolate, staticFile, useCurrentFrame, Easing } from 'remotion';
import { ACCENT, INK, MUTED, PAPER, SANS, SERIF } from './TitleCard';

/**
 * 收尾：界面退到虚焦远景，纸白层浮起，字标与副句落定后 hold。
 */
export const Outro: React.FC<{ duration: number; bg?: string }> = ({ duration, bg = 'textures/live/pages-answer.png' }) => {
  const frame = useCurrentFrame();

  const bgT = interpolate(frame, [0, 22], [0, 1], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
  const veil = interpolate(frame, [0, 26], [0, 0.7], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
  const markT = interpolate(frame, [10, 26], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.16, 0.9, 0.24, 1),
  });
  const lineT = interpolate(frame, [20, 34], [0, 1], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
  const tagT = interpolate(frame, [26, 40], [0, 1], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });

  return (
    <AbsoluteFill style={{ backgroundColor: PAPER, overflow: 'hidden' }}>
      {/* 虚焦背景 */}
      <Img
        src={staticFile(bg)}
        style={{
          position: 'absolute',
          width: 1920 * 1.12,
          height: 1080 * 1.12,
          left: -115,
          top: -65,
          filter: `blur(26px) saturate(0.75)`,
          opacity: 0.8 * bgT,
          transform: `scale(${1 + frame * 0.0006})`,
        }}
      />
      <AbsoluteFill style={{ backgroundColor: PAPER, opacity: veil }} />

      <AbsoluteFill style={{ justifyContent: 'center', alignItems: 'center' }}>
        <div style={{ textAlign: 'center' }}>
          <div
            style={{
              fontFamily: SERIF,
              fontSize: 104,
              fontWeight: 700,
              color: INK,
              letterSpacing: '0.04em',
              opacity: markT,
              transform: `translateY(${(1 - markT) * 12}px)`,
              filter: `blur(${(1 - markT) * 6}px)`,
            }}
          >
            小智医疗
          </div>

          <div
            style={{
              width: 180,
              height: 2,
              background: ACCENT,
              margin: '30px auto 26px',
              opacity: 0.8,
              transform: `scaleX(${lineT})`,
            }}
          />

          <div
            style={{
              fontFamily: SANS,
              fontSize: 28,
              letterSpacing: '0.2em',
              color: MUTED,
              opacity: tagT,
            }}
          >
            问题问清楚 · 答案有出处
          </div>
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
