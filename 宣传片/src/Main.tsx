import { AbsoluteFill, Audio, Sequence, interpolate, staticFile, useCurrentFrame, Easing } from 'remotion';
import { PageCam, type CamKey } from './components/PageCam';
import { BrandOpen } from './components/BrandOpen';
import { TitleCard } from './components/TitleCard';
import { Outro } from './components/Outro';
import { Underline } from './components/Underline';
import { Caption } from './components/Caption';
import { FlashCut } from './components/FlashCut';
import { PAPER } from './components/TitleCard';

// 15s @ 30fps = 450f。品牌开场（低）→ 界面段（中高）→ 字卡呼吸（低）→ 收尾峰值。
export const SHOTS = {
  brand: { from: 0, duration: 75 },
  hero: { from: 75, duration: 240 }, // 75–315
  card: { from: 315, duration: 75 },
  outro: { from: 390, duration: 60 },
} as const;

export const TOTAL = 450;

const FLASH_CUTS = [SHOTS.hero.from, SHOTS.card.from, SHOTS.outro.from];

export const SFX: { from: number; src: string; volume: number }[] = [
  { from: 2, src: 'transition-soft.mp3', volume: 0.35 }, // 墨线拉开
  { from: 10, src: 'sparkle.mp3', volume: 0.2 }, // 字标压印
  { from: 68, src: 'whoosh-fast.mp3', volume: 0.4 }, // 品牌 → 界面
  { from: 148, src: 'transition-soft.mp3', volume: 0.38 }, // 推近回答
  { from: 186, src: 'whoosh-big.mp3', volume: 0.44 }, // 下移依据行
  { from: 198, src: 'sparkle.mp3', volume: 0.3 }, // 聚光落定
  { from: 312, src: 'swoosh-quick.mp3', volume: 0.36 }, // 进字卡
  { from: 386, src: 'swoosh-quick.mp3', volume: 0.33 }, // 进收尾
  { from: 392, src: 'riser-cine.mp3', volume: 0.34 }, // 收尾起势
  { from: 420, src: 'impact-cine.mp3', volume: 0.42 }, // 字标落定
  { from: 424, src: 'sparkle.mp3', volume: 0.26 },
];

// 相机：推近回答块 → 下移到"依据"行（页面坐标，1920×1080 截图坐标系）
const HERO_KEYS: CamKey[] = [
  { frame: 0, cx: 960, cy: 535, zoom: 1.08 },
  { frame: 30, cx: 960, cy: 540, zoom: 1.0 },
  { frame: 64, cx: 962, cy: 536, zoom: 1.03 },
  { frame: 116, cx: 1068, cy: 470, zoom: 1.56 },
  { frame: 156, cx: 1072, cy: 522, zoom: 1.72 },
  { frame: 196, cx: 1072, cy: 530, zoom: 1.75 },
  { frame: 240, cx: 1070, cy: 532, zoom: 1.78 },
];

const HERO_CAPTIONS = [
  { from: 52, duration: 46, text: '自然语言提问' },
  { from: 104, duration: 50, text: '检索知识库 · 命中相关段落' },
  { from: 160, duration: 80, text: '答案附原文出处' },
];

const Hero: React.FC = () => {
  const frame = useCurrentFrame();
  const fadeIn = interpolate(frame, [0, 14], [0, 1], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
  const ease = Easing.bezier(0.4, 0, 0.18, 1);
  // 全景时界面本就铺满、无需柔化；推近后才渐入，用来吃掉换行留下的空白
  const topH = interpolate(frame, [60, 130], [0, 130], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });
  const botH = interpolate(frame, [60, 130], [40, 330], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp' });

  return (
    <AbsoluteFill style={{ backgroundColor: PAPER, opacity: fadeIn }}>
      <PageCam
        src="textures/live/pages-answer.png"
        pageH={1080}
        keys={HERO_KEYS}
        ease={ease}
        bg={PAPER}
      >
        <Underline x={548} y={574} w={46} from={160} duration={18} thickness={3} />
      </PageCam>

      {/* 边缘柔化：内容从纸面浮出，顺带吃掉界面底部的大片留白 */}
      <div
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          top: 0,
          height: topH,
          background: `linear-gradient(to bottom, ${PAPER} 0%, ${PAPER} 42%, rgba(247,246,243,0) 100%)`,
          pointerEvents: 'none',
        }}
      />
      <div
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          bottom: 0,
          height: botH,
          background: `linear-gradient(to top, ${PAPER} 8%, rgba(247,246,243,0.85) 48%, rgba(247,246,243,0) 100%)`,
          pointerEvents: 'none',
        }}
      />

      {HERO_CAPTIONS.map((c) => (
        <Sequence key={c.from} from={c.from} durationInFrames={c.duration}>
          <Caption duration={c.duration} text={c.text} />
        </Sequence>
      ))}
    </AbsoluteFill>
  );
};

export const Main: React.FC = () => {
  return (
    <AbsoluteFill style={{ backgroundColor: PAPER }}>
      <Sequence from={SHOTS.brand.from} durationInFrames={SHOTS.brand.duration}>
        <BrandOpen duration={SHOTS.brand.duration} />
      </Sequence>

      <Sequence from={SHOTS.hero.from} durationInFrames={SHOTS.hero.duration}>
        <Hero />
      </Sequence>

      <Sequence from={SHOTS.card.from} durationInFrames={SHOTS.card.duration}>
        <TitleCard
          duration={SHOTS.card.duration}
          words={[{ text: '让每个答案，' }, { text: '都有出处', accent: true }]}
          sub="知识库检索 · 原文引用"
          fontSize={100}
        />
      </Sequence>

      <Sequence from={SHOTS.outro.from} durationInFrames={SHOTS.outro.duration}>
        <Outro duration={SHOTS.outro.duration} />
      </Sequence>

      {FLASH_CUTS.map((f) => (
        <FlashCut key={f} from={f - 5} />
      ))}

      {SFX.map((s, i) => (
        <Sequence key={i} from={s.from} durationInFrames={90}>
          <Audio src={staticFile(`audio/${s.src}`)} volume={s.volume} />
        </Sequence>
      ))}
    </AbsoluteFill>
  );
};
