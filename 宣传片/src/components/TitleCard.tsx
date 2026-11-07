import { AbsoluteFill, interpolate, useCurrentFrame, Easing } from 'remotion';

export const INK = '#1b1a17';
export const ACCENT = '#0f5c4d'; // 墨绿，取自产品发送按钮
export const MUTED = '#6b6a66';
export const PAPER = '#f7f6f3';
export const SERIF = '"Noto Serif SC", "Source Han Serif SC", "SimSun", serif';
export const SANS = '"Noto Sans SC", "Microsoft YaHei", "Segoe UI", sans-serif';

/**
 * 纸底字卡：衬线大字逐词压印，墨绿强调词 + 下划线生长。
 * words 里用空格分词，*词* 标强调（中文没空格时整句当一个词）。
 */
export const TitleCard: React.FC<{
  duration: number;
  words: { text: string; accent?: boolean }[];
  sub?: string;
  fontSize?: number;
  ink?: string;
  accent?: string;
  paper?: string;
}> = ({ duration, words, sub, fontSize = 104, ink = INK, accent = ACCENT, paper = PAPER }) => {
  const frame = useCurrentFrame();
  const fadeOut = interpolate(frame, [duration - 8, duration], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const underline = interpolate(frame, [16, 34], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.3, 0, 0.2, 1),
  });
  const subT = interpolate(frame, [14, 26], [0, 1], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: paper,
        justifyContent: 'center',
        alignItems: 'center',
        opacity: fadeOut,
        backgroundImage: 'radial-gradient(1100px 750px at 50% 42%, rgba(255,255,255,0.9), transparent 65%)',
      }}
    >
      <div style={{ textAlign: 'center', maxWidth: 1560 }}>
        <div
          style={{
            fontFamily: SERIF,
            fontSize,
            fontWeight: 500,
            lineHeight: 1.2,
            color: ink,
            letterSpacing: '0.02em',
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'center',
            columnGap: '0.22em',
          }}
        >
          {words.map((w, i) => {
            const delay = 4 + i * 5;
            const t = interpolate(frame, [delay, delay + 11], [0, 1], {
              extrapolateLeft: 'clamp',
              extrapolateRight: 'clamp',
              easing: Easing.bezier(0.2, 0.75, 0.3, 1),
            });
            return (
              <span
                key={i}
                style={{
                  opacity: t,
                  transform: `scale(${1.16 - 0.16 * t})`,
                  filter: `blur(${(1 - t) * 6}px)`,
                  display: 'inline-block',
                  color: w.accent ? accent : undefined,
                }}
              >
                {w.text}
              </span>
            );
          })}
        </div>

        <div
          style={{
            height: 5,
            width: 200,
            margin: '42px auto 0',
            borderRadius: 3,
            background: accent,
            transform: `scaleX(${underline})`,
          }}
        />

        {sub ? (
          <div
            style={{
              fontFamily: SANS,
              fontSize: 27,
              letterSpacing: '0.18em',
              color: MUTED,
              marginTop: 36,
              opacity: subT,
            }}
          >
            {sub}
          </div>
        ) : null}
      </div>
    </AbsoluteFill>
  );
};
