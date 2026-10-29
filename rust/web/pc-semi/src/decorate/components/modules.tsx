'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Skeleton, Tag, Typography } from '@douyinfe/semi-ui';
import type { DecorateModule, PageModule } from '../types';
import { Frame, imageUrl, linkUrl, ModuleHeading, ModuleTitle, SafeImage } from './common';

type ModuleProps<T = Record<string, any>> = { module: T };

function ImageAd({ module }: ModuleProps) {
  const pics = module.picList || [];
  if (!pics.length) return null;
  return (
    <Frame frame={module.frame}>
      <ModuleTitle title={module.title} />
      <div className={`dec-image-ad dec-image-ad--${module.picType === 2 ? 'carousel' : 'grid'}`}>
        {pics.map((pic: any, index: number) => (
          <a key={index} href={linkUrl(pic.picLink)} className="dec-image-ad__item">
            <SafeImage src={imageUrl(pic)} alt={pic.picTitle || ''} />
          </a>
        ))}
      </div>
    </Frame>
  );
}

function ImageNav({ module }: ModuleProps) {
  const items = module.items || module.picList || [];
  if (!items.length) return null;
  return (
    <Frame frame={module.frame}>
      <div className="dec-image-nav">
        {items.map((item: any, index: number) => (
          <a key={index} href={linkUrl(item.picLink)} className="dec-image-nav__item">
            <SafeImage src={imageUrl(item)} alt={item.picTitle || ''} />
            {item.picTitle ? <span>{item.picTitle}</span> : null}
          </a>
        ))}
      </div>
    </Frame>
  );
}

function ImageSquareAd({ module }: ModuleProps) {
  const pics = module.picList || module.items || [];
  if (!pics.length) return null;
  return (
    <Frame frame={module.frame}>
      <div className="dec-square-ad">
        {pics.map((pic: any, index: number) => (
          <a key={index} href={linkUrl(pic.picLink)} className="dec-square-ad__item">
            <SafeImage src={imageUrl(pic)} alt={pic.picTitle || ''} />
            {pic.picTitle ? <span>{pic.picTitle}</span> : null}
          </a>
        ))}
      </div>
    </Frame>
  );
}

function ImageHotarea({ module }: ModuleProps) {
  const pic = module.pic || module.backgroundPic || {};
  return (
    <Frame frame={module.frame}>
      <div className="dec-hotarea">
        <SafeImage src={imageUrl(pic)} alt="" />
        {(module.hotareas || []).map((area: any, index: number) => (
          <a
            key={index}
            href={linkUrl(area.link || area.picLink)}
            className="dec-hotarea__area"
            style={{
              left: `${area.left ?? 0}%`,
              top: `${area.top ?? 0}%`,
              width: `${area.width ?? 10}%`,
              height: `${area.height ?? 10}%`,
            }}
          />
        ))}
      </div>
    </Frame>
  );
}

function TitleAd({ module }: ModuleProps) {
  const title = module.title || {};
  return (
    <Frame frame={module.frame}>
      <ModuleTitle title={{ ...title, showTitle: 1 }} />
    </Frame>
  );
}

function Notice({ module }: ModuleProps) {
  const notices = module.noticeList || module.items || [];
  return (
    <Frame frame={module.frame}>
      <div className="dec-notice">
        <Tag color="orange" size="large">
          {module.noticeTitle || '公告'}
        </Tag>
        <div className="dec-notice__list">
          {notices.slice(0, 3).map((item: any, index: number) => (
            <a key={index} href={linkUrl(item.picLink || item.link)}>
              {item.picTitle || item.title || item.noticeTitle}
            </a>
          ))}
        </div>
      </div>
    </Frame>
  );
}

function Divider({ module }: ModuleProps) {
  return (
    <Frame frame={module.frame}>
      <div
        className="dec-divider"
        style={{
          height: `${module.lineHeight ?? 1}px`,
          background: module.lineColor || '#e8ebf0',
          marginTop: `${module.marginTop ?? 10}px`,
          marginBottom: `${module.marginBottom ?? 10}px`,
        }}
      />
    </Frame>
  );
}

function Spacer({ module }: ModuleProps) {
  return <div style={{ height: `${module.blankHeight ?? 20}px` }} aria-hidden />;
}

function QuickNav({ module }: ModuleProps) {
  const items = module.items || [];
  const style = module.style || {};
  return (
    <Frame frame={module.frame}>
      <nav
        className="dec-quick-nav"
        style={{
          justifyContent: style.containerAlign === 2 ? 'center' : style.containerAlign === 3 ? 'flex-end' : 'flex-start',
          gap: `${style.itemGap ?? 24}px`,
          color: style.textColor || '#344054',
          fontSize: `${style.fontSize ?? 14}px`,
        }}
      >
        {items.map((item: any, index: number) => (
          <a key={index} href={linkUrl(item.picLink)} className="dec-quick-nav__item">
            <SafeImage src={item.picThumb || item.picUrl} alt={item.picTitle || ''} />
            <span style={{ fontWeight: style.fontWeight || 500 }}>{item.picTitle || item.picLink?.name}</span>
          </a>
        ))}
      </nav>
    </Frame>
  );
}

function CategoryNav({ module }: ModuleProps) {
  const categories = module.categories || module.items || [];
  return (
    <Frame frame={module.frame}>
      <Card className="dec-category-card" bodyStyle={{ padding: 0 }}>
        <div className="dec-category-nav">
          {(categories.length ? categories : Array.from({ length: 8 })).map((item: any, index: number) => (
            <a key={index} href={linkUrl(item?.picLink)} className="dec-category-nav__item">
              <span>{item?.picTitle || item?.title || '商品分类'}</span>
              <span className="dec-arrow">›</span>
            </a>
          ))}
        </div>
      </Card>
    </Frame>
  );
}

function HomeHero({ module }: ModuleProps) {
  const left = module.left || {};
  const center = module.center || {};
  const right = module.right || {};
  const style = module.style || {};
  const banners = center.banners || [];
  const categories = left.categories || [];
  const cards = center.modules || [];
  return (
    <Frame frame={module.frame} className="dec-hero-frame">
      <div
        className="dec-hero"
        style={{
          gap: `${style.gap ?? 16}px`,
          minHeight: `${style.height ?? 460}px`,
          ['--dec-hero-left' as string]: `${style.leftWidth ?? 220}px`,
          ['--dec-hero-right' as string]: `${style.rightWidth ?? 280}px`,
          ['--dec-hero-panel' as string]: style.panelBackgroundColor || '#ffffff',
          ['--dec-hero-title' as string]: style.titleColor || '#101828',
          ['--dec-hero-muted' as string]: style.mutedColor || '#98a2b3',
        }}
      >
        <aside className="dec-hero__left">
          <div className="dec-hero__left-title" style={{ color: left.textColor }}>
            {left.title || '全部分类'}
          </div>
          {categories.slice(0, left.showCount || 12).map((item: any, index: number) => (
            <a key={index} href={linkUrl(item.picLink)} className="dec-hero__category">
              <span>{item.picTitle || item.picName}</span>
              <span className="dec-arrow">›</span>
            </a>
          ))}
        </aside>

        <main className="dec-hero__center">
          <div className="dec-hero__banners">
            {banners.length ? (
              banners.slice(0, 1).map((banner: any, index: number) => (
                <a key={index} href={linkUrl(banner.picLink)} className="dec-hero__banner">
                  <SafeImage src={imageUrl(banner)} alt={banner.picTitle || ''} />
                  {banner.picTitle ? <span className="dec-hero__banner-title">{banner.picTitle}</span> : null}
                </a>
              ))
            ) : (
              <div className="dec-hero__banner dec-hero__banner--empty" />
            )}
          </div>
          <div className="dec-hero__cards">
            {cards.slice(0, 5).map((card: any, index: number) => (
              <a key={index} href={linkUrl(card.link)} className="dec-hero__card">
                <SafeImage src={imageUrl(card.pic)} alt={card.moduleTitle || card.title || ''} />
                <div className="dec-hero__card-body">
                  <span className="dec-hero__card-title">{card.moduleTitle || card.title}</span>
                  <span className="dec-hero__card-desc">{card.moduleSubTitle || card.badgeText}</span>
                </div>
              </a>
            ))}
          </div>
        </main>

        <aside className="dec-hero__right">
          <div className="dec-hero__user">
            <div className="dec-hero__avatar">
              <SafeImage src={imageUrl(right.user?.avatar)} alt="" />
            </div>
            <div>
              <div className="dec-hero__welcome">{right.user?.welcomeText || 'Hi，欢迎来到万物新生'}</div>
              <Button theme="solid" type="primary" size="small">
                登录 / 注册
              </Button>
            </div>
          </div>
          <div className="dec-hero__stats">
            {(right.stats || []).slice(0, 3).map((stat: any, index: number) => (
              <div key={index} className="dec-hero__stat">
                <b>{stat.value || '-'}</b>
                <span>{stat.label || ''}</span>
              </div>
            ))}
          </div>
          <div className="dec-hero__shortcuts">
            {(right.shortcuts || []).slice(0, 9).map((item: any, index: number) => (
              <a key={index} href={linkUrl(item.picLink)} className="dec-hero__shortcut">
                <SafeImage src={imageUrl(item)} alt="" />
                <span>{item.picTitle}</span>
              </a>
            ))}
          </div>
          {right.shopsTitle ? (
            <div className="dec-hero__shops">
              <div className="dec-hero__shops-head">
                <b>{right.shopsTitle}</b>
                <a href={linkUrl(right.shopsMoreLink)}>{right.shopsMoreText || '查看更多'} ›</a>
              </div>
            </div>
          ) : null}
        </aside>
      </div>
    </Frame>
  );
}

interface ProductItem {
  productId?: number;
  productSn?: string;
  seoLink?: string;
  productName?: string;
  productPrice?: number | string;
  picThumb?: string;
  picUrl?: string;
  brief?: string;
  productStock?: number;
}

function Product({ module }: ModuleProps) {
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [loading, setLoading] = useState(true);
  const config = module.products || {};
  const columns = Number(module.style) === 2 ? 5 : 4;

  useEffect(() => {
    let cancelled = false;
    const params = new URLSearchParams({ page: '1' });
    if (config.productSelectType === 1 && Array.isArray(config.productIds) && config.productIds.length) {
      params.set('ids', config.productIds.join(','));
      params.set('size', String(config.productIds.length));
    } else {
      params.set('size', String(config.productNumber || (columns === 5 ? 10 : 8)));
      if (config.productCategoryId) params.set('categoryId', String(config.productCategoryId));
      if (config.productTag) params.set('tag', String(config.productTag));
    }
    fetch(`/api/product/product/list?${params.toString()}`)
      .then((response) => response.json())
      .then((json) => {
        if (cancelled) return;
        const records = json?.data?.records || json?.data || json?.records || [];
        setProducts(Array.isArray(records) ? records : []);
      })
      .catch(() => undefined)
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [JSON.stringify(config), columns]);

  return (
    <Frame frame={module.frame}>
      <ModuleHeading title={module.title?.titleText} subTitle={module.title?.descText} />
      {loading ? (
        <div className="dec-product-grid" style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}>
          {Array.from({ length: columns }).map((_, index) => (
            <Card key={index} className="dec-product-card">
              <Skeleton.Title />
              <Skeleton.Paragraph rows={2} />
            </Card>
          ))}
        </div>
      ) : (
        <div className="dec-product-grid" style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}>
          {products.map((product) => (
            <Link
              key={product.productSn || product.productId}
              href={`/product/${product.productId || ''}`}
              className="dec-product-card"
            >
              <div className="dec-product-card__image">
                <SafeImage src={product.picThumb || product.picUrl || ''} alt={product.productName || ''} />
              </div>
              <div className="dec-product-card__name">{product.productName || '商品'}</div>
              {module.showBrief ? <div className="dec-product-card__brief">{product.brief || ''}</div> : null}
              <div className="dec-product-card__price">
                <b>¥</b> {Number(product.productPrice || 0).toFixed(2)}
              </div>
            </Link>
          ))}
          {!products.length ? <div className="dec-empty">暂未获取到商品数据</div> : null}
        </div>
      )}
    </Frame>
  );
}

function Coupon({ module }: ModuleProps) {
  return (
    <Frame frame={module.frame}>
      <ModuleHeading title={module.title?.titleText || '领券中心'} />
      <div className="dec-generic-grid">
        {Array.from({ length: 4 }).map((_, index) => (
          <Card key={index} className="dec-generic-card">
            <b>¥ 10</b>
            <span>满 100 可用</span>
            <Button size="small" theme="solid" type="primary">
              立即领取
            </Button>
          </Card>
        ))}
      </div>
    </Frame>
  );
}

function Seckill({ module }: ModuleProps) {
  return (
    <Frame frame={module.frame}>
      <ModuleHeading title={module.title?.titleText || '限时秒杀'} subTitle="每日限时低价" />
      <div className="dec-generic-grid">
        {Array.from({ length: 5 }).map((_, index) => (
          <Card key={index} className="dec-generic-card">
            <div className="dec-generic-card__image" />
            <span>秒杀商品</span>
          </Card>
        ))}
      </div>
    </Frame>
  );
}

function Groupon({ module }: ModuleProps) {
  return (
    <Frame frame={module.frame}>
      <ModuleHeading title={module.title?.titleText || '拼团活动'} />
      <div className="dec-generic-grid">
        {Array.from({ length: 4 }).map((_, index) => (
          <Card key={index} className="dec-generic-card">
            <span>拼团商品</span>
          </Card>
        ))}
      </div>
    </Frame>
  );
}

function ProductGroup({ module }: ModuleProps) {
  return (
    <Frame frame={module.frame}>
      <ModuleHeading title={module.title?.titleText || '商品分组'} />
      <div className="dec-generic-grid">
        {Array.from({ length: 4 }).map((_, index) => (
          <Card key={index} className="dec-generic-card">
            <span>商品分组</span>
          </Card>
        ))}
      </div>
    </Frame>
  );
}

function RecommendProduct({ module }: ModuleProps) {
  return <Product module={{ ...module, products: { ...(module.products || {}), productSelectType: 3 } }} />;
}

function GroupBanner({ module }: ModuleProps) {
  const pics = module.banners || module.picList || [];
  return (
    <Frame frame={module.frame}>
      <div className="dec-group-banner">
        {pics.map((pic: any, index: number) => (
          <a key={index} href={linkUrl(pic.picLink)}>
            <SafeImage src={imageUrl(pic)} alt={pic.picTitle || ''} />
          </a>
        ))}
      </div>
    </Frame>
  );
}

function Fallback({ module }: ModuleProps) {
  return (
    <Frame frame={module.frame}>
      <Card className="dec-fallback">
        <Typography.Text type="tertiary">{module.label || '装修模块'}</Typography.Text>
        <Typography.Text type="tertiary" size="small">
          该模块已注册，React 版渲染待补齐
        </Typography.Text>
      </Card>
    </Frame>
  );
}

export const PC_MODULE_REGISTRY: Record<string, (props: ModuleProps) => JSX.Element | null> = {
  'pc-image-ad': ImageAd,
  'pc-image-nav': ImageNav,
  'pc-image-square-ad': ImageSquareAd,
  'pc-image-hotarea': ImageHotarea,
  'pc-title-ad': TitleAd,
  'pc-notice': Notice,
  'pc-divider': Divider,
  'pc-spacer': Spacer,
  'pc-home-quick-nav': QuickNav,
  'pc-category-nav': CategoryNav,
  'pc-home-hero': HomeHero,
  'pc-product': Product,
  'pc-recommend-product': RecommendProduct,
  'pc-product-group': ProductGroup,
  'pc-home-coupon': Coupon,
  'pc-home-seckill': Seckill,
  'pc-home-groupon': Groupon,
  'pc-group-banner': GroupBanner,
};

export function DecorateRenderer({ modules }: { modules: DecorateModule[] }) {
  return (
    <div className="decorate-renderer">
      {modules
        .filter((item) => item.isShow !== false)
        .map((item, index) => {
          const Component = PC_MODULE_REGISTRY[item.type] || Fallback;
          return (
            <div className="decorate-renderer__module" key={item.moduleIndex ?? `${item.type}-${index}`}>
              <Component module={{ ...item.module, label: item.label, frame: item.module?.frame }} />
            </div>
          );
        })}
    </div>
  );
}

export type { PageModule };
