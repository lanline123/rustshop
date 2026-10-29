import type { CSSProperties, ReactNode } from 'react';
import type { DecoratePic, DecorateTitle, ModuleFrameLike } from '../types';

export interface PicLike {
  picUrl?: string;
  picThumb?: string;
  picLink?: { link?: string; name?: string; label?: string };
  picTitle?: string;
  picDesc?: string;
}

export function imageUrl(pic?: PicLike | DecoratePic | null): string {
  return pic?.picUrl || pic?.picThumb || '';
}

export function linkUrl(link?: { link?: string } | null): string {
  return link?.link || '#';
}

export function SafeImage({
  src,
  alt = '',
  className,
  style,
}: {
  src?: string;
  alt?: string;
  className?: string;
  style?: CSSProperties;
}) {
  if (!src) {
    return <span className={`dec-image-placeholder ${className || ''}`} style={style} aria-hidden />;
  }
  // 装修图片来自 OSS/CDN，域名不固定；POC 先用原生 img，后续统一接 next/image。
  return <img src={src} alt={alt} className={className} style={style} loading="lazy" />;
}

export function Frame({
  frame,
  children,
  className = '',
}: {
  frame?: ModuleFrameLike | null;
  children: ReactNode;
  className?: string;
}) {
  const style: CSSProperties = {
    padding: frame?.boxPadding ?? 10,
    paddingTop: frame?.boxPaddingTop ?? frame?.boxPaddingTop ?? undefined,
    paddingBottom: frame?.boxPaddingBottom ?? undefined,
    borderRadius: frame?.boxRadius ?? 0,
    background: frame?.backgroundColor || 'transparent',
  };
  return (
    <section className={`dec-frame ${className}`} style={style}>
      {children}
    </section>
  );
}

export function ModuleTitle({ title }: { title?: DecorateTitle | null }) {
  if (!title || Number(title.showTitle) !== 1) return null;
  const align = Number(title.textAlign) === 2 ? 'center' : Number(title.textAlign) === 3 ? 'right' : 'left';
  return (
    <div className="dec-module-title" style={{ textAlign: align, color: title.titleColor }}>
      <div className="dec-module-title__text" style={{ fontSize: title.titleSize ? `${title.titleSize}px` : undefined }}>
        {title.title}
      </div>
      {title.subTitle ? <div className="dec-module-title__sub">{title.subTitle}</div> : null}
    </div>
  );
}

export function ModuleHeading({ title, subTitle }: { title?: string; subTitle?: string }) {
  if (!title) return null;
  return (
    <div className="dec-heading">
      <span className="dec-heading__bar" />
      <span className="dec-heading__text">{title}</span>
      {subTitle ? <span className="dec-heading__sub">{subTitle}</span> : null}
    </div>
  );
}
