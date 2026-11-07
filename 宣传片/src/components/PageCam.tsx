import { AbsoluteFill, Img, interpolate, staticFile, useCurrentFrame, Easing } from 'remotion';

export type CamKey = {
  frame: number;
  cx: number; // 页面 CSS 坐标：视口中心对准的页面点
  cy: number;
  zoom: number; // 1 = 1 CSS px 映射到 1 输出 px
  rotX?: number;
  rotY?: number;
  rotZ?: number;
  persp?: number;
};

const lerp = (a: number, b: number, t: number) => a + (b - a) * t;

/**
 * 2.5D 页面相机。底图是整页高清截图，keys 插值出 cx/cy/zoom/旋转。
 *
 * 关键：放大一律走 CSS `zoom`（布局级放大）而不是 transform: scale()。
 * scale 会让 Chromium 先按 1920 布局宽光栅化再 GPU 放大，文字会糊；
 * zoom 让整层按放大后的尺寸重新光栅化，从 3x 原图降采样，字始终锐利。
 */
export const PageCam: React.FC<{
  src: string;
  pageH: number;
  pageW?: number;
  keys: CamKey[];
  children?: React.ReactNode;
  blur?: number;
  saturate?: number;
  bg?: string;
  ease?: (t: number) => number;
  dof?: { focusY: number; strength: number };
}> = ({
  src,
  pageH,
  pageW = 1920,
  keys,
  children,
  blur = 0,
  saturate = 1,
  bg = '#f7f6f3',
  ease = Easing.bezier(0.33, 0, 0.15, 1),
  dof,
}) => {
  const frame = useCurrentFrame();
  let a = keys[0];
  let b = keys[keys.length - 1];
  for (let i = 0; i < keys.length - 1; i++) {
    if (frame >= keys[i].frame && frame <= keys[i + 1].frame) {
      a = keys[i];
      b = keys[i + 1];
      break;
    }
  }
  const t =
    a.frame === b.frame
      ? 1
      : interpolate(frame, [a.frame, b.frame], [0, 1], {
          extrapolateLeft: 'clamp',
          extrapolateRight: 'clamp',
          easing: ease,
        });

  const cx = lerp(a.cx, b.cx, t);
  const cy = lerp(a.cy, b.cy, t);
  const zoom = lerp(a.zoom, b.zoom, t);
  const rotX = lerp(a.rotX ?? 0, b.rotX ?? 0, t);
  const rotY = lerp(a.rotY ?? 0, b.rotY ?? 0, t);
  const rotZ = lerp(a.rotZ ?? 0, b.rotZ ?? 0, t);
  const persp = lerp(a.persp ?? 1600, b.persp ?? 1600, t);

  const filters: string[] = [];
  if (blur > 0) filters.push(`blur(${blur}px)`);
  if (saturate !== 1) filters.push(`saturate(${saturate})`);

  return (
    <AbsoluteFill style={{ overflow: 'hidden', backgroundColor: bg }}>
      <div
        style={{
          position: 'absolute',
          inset: 0,
          perspective: `${persp * zoom}px`,
          perspectiveOrigin: '960px 540px',
        }}
      >
        <div
          style={{
            position: 'absolute',
            width: pageW,
            height: pageH,
            zoom,
            transform: `translate(${960 / zoom - cx}px, ${540 / zoom - cy}px) rotateY(${rotY}deg) rotateX(${rotX}deg) rotateZ(${rotZ}deg)`,
            transformOrigin: `${cx}px ${cy}px`,
            transformStyle: 'preserve-3d',
            filter: filters.length ? filters.join(' ') : undefined,
          }}
        >
          <Img src={staticFile(src)} style={{ position: 'absolute', width: pageW, height: pageH }} />
          {children}
        </div>
      </div>

      {dof ? (
        <div
          style={{
            position: 'absolute',
            left: 0,
            right: 0,
            top: 0,
            height: Math.max(0, dof.focusY),
            backdropFilter: `blur(${dof.strength}px)`,
            WebkitBackdropFilter: `blur(${dof.strength}px)`,
            maskImage: 'linear-gradient(to bottom, rgba(0,0,0,1) 0%, rgba(0,0,0,1) 45%, rgba(0,0,0,0) 100%)',
            WebkitMaskImage: 'linear-gradient(to bottom, rgba(0,0,0,1) 0%, rgba(0,0,0,1) 45%, rgba(0,0,0,0) 100%)',
            pointerEvents: 'none',
          }}
        />
      ) : null}
    </AbsoluteFill>
  );
};
