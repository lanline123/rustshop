'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
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
        <span className="dec-notice__tag">
          {module.noticeTitle || '公告'}
        </span>
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
      <div className="dec-category-card">
        <div className="dec-category-nav">
          {(categories.length ? categories : Array.from({ length: 8 })).map((item: any, index: number) => (
            <a key={index} href={linkUrl(item?.picLink)} className="dec-category-nav__item">
              <span>{item?.picTitle || item?.title || '商品分类'}</span>
              <span className="dec-arrow">›</span>
            </a>
          ))}
        </div>
      </div>
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

  const formatHeroPrice = (value: any) => {
    if (value === undefined || value === null || value === '') return '-';
    const text = String(value);
    if (text.includes('¥')) return text;
    const amount = Number(value);
    return Number.isFinite(amount) ? `¥ ${amount.toLocaleString('zh-CN')}` : text;
  };

  const renderMiniProducts = (card: any) => (
    <div className="dec-hero__card-products">
      {(card.products || []).slice(0, 2).map((product: any, index: number) => (
        <div key={index} className="dec-hero__card-product">
          <div className="dec-hero__card-img-box">
            <SafeImage src={imageUrl(product.pic)} alt={product.productName || ''} />
            {product.label ? <span className="dec-hero__card-label">{product.label}</span> : null}
          </div>
          {card.showPrice !== 0 ? (
            <div className="dec-hero__card-price">{formatHeroPrice(product.price)}</div>
          ) : null}
        </div>
      ))}
    </div>
  );

  return (
    <Frame frame={module.frame} className="dec-hero-frame">
      <div
        className="dec-hero"
        style={{
          ['--dec-hero-height' as string]: `${style.height ?? 336}px`,
          ['--dec-hero-gap' as string]: `${style.gap ?? 16}px`,
          ['--dec-hero-left' as string]: `${style.leftWidth ?? 252}px`,
          ['--dec-hero-right' as string]: `${style.rightWidth ?? 252}px`,
          ['--dec-hero-panel' as string]: style.panelBackgroundColor || '#ffffff',
          ['--dec-hero-radius' as string]: `${style.panelRadius ?? 8}px`,
        }}
      >
        <aside className="dec-hero__left dec-hero__panel">
          <div className="dec-hero__cate-list" style={{ background: left.backgroundColor, color: left.textColor }}>
            {left.title ? <div className="dec-hero__cate-title-head">{left.title}</div> : null}
            {categories.slice(0, left.showCount || 12).map((item: any, index: number) => (
              <a key={index} href={linkUrl(item.picLink)} className="dec-hero__cate-item">
                <span>{item.picTitle || item.picName}</span>
                <span className="dec-arrow">›</span>
              </a>
            ))}
          </div>
        </aside>

        <section className="dec-hero__center">
          <div className="dec-hero__center-top">
            <div className="dec-hero__banner">
              {banners.length ? (
                <a href={linkUrl(banners[0].picLink)} className="dec-hero__banner-link">
                  <SafeImage src={imageUrl(banners[0])} alt={banners[0].picTitle || ''} />
                </a>
              ) : <div className="dec-hero__banner-empty" />}
            </div>
            <div className="dec-hero__promo-group">
              {cards.slice(0, 2).map((card: any, index: number) => (
                <a key={index} href={linkUrl(card.link)} className="dec-hero__panel dec-hero__promo-card">
                  <div className="dec-hero__card-header">
                    <span className="dec-hero__card-title">{card.moduleTitle || card.title || '促销'}</span>
                    {card.moduleSubTitle ? <span className="dec-hero__card-badge" style={card.moduleSubTitleBgColorA ? { background: `linear-gradient(90deg, ${card.moduleSubTitleBgColorA}, ${card.moduleSubTitleBgColorB || card.moduleSubTitleBgColorA})` } : undefined}>{card.moduleSubTitle}</span> : null}
                  </div>
                  {renderMiniProducts(card)}
                </a>
              ))}
            </div>
          </div>
          <div className="dec-hero__center-bottom">
            <div className="dec-hero__product-grid">
              {cards.slice(2, 6).map((card: any, index: number) => (
                <a key={index} href={linkUrl(card.link)} className="dec-hero__channel-card">
                  <div className="dec-hero__card-header">
                    <span className="dec-hero__card-title">{card.moduleTitle || card.title || '推荐'}</span>
                    {card.moduleSubTitle ? <span className="dec-hero__card-badge" style={card.moduleSubTitleBgColorA ? { background: `linear-gradient(90deg, ${card.moduleSubTitleBgColorA}, ${card.moduleSubTitleBgColorB || card.moduleSubTitleBgColorA})` } : undefined}>{card.moduleSubTitle}</span> : null}
                  </div>
                  {renderMiniProducts(card)}
                </a>
              ))}
            </div>
          </div>
        </section>

        <aside className="dec-hero__right">
          <div className="dec-hero__panel dec-hero__user-card">
            <div className="dec-hero__user-top">
              <div className="dec-hero__avatar">
                <SafeImage src={imageUrl(right.user?.avatar)} alt="" />
              </div>
              <div className="dec-hero__user-text">
                <div className="dec-hero__welcome">{right.user?.welcomeText || 'Hi，欢迎来到万物新生'}</div>
                <button type="button" className="dec-hero__login">登录 / 注册</button>
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
            <div className="dec-hero__panel dec-hero__shops">
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
            <div key={index} className="dec-product-card">
              <span className="dec-skeleton-line" />
              <span className="dec-skeleton-line" />
            </div>
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
  const [coupons, setCoupons] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    fetch('/api/home/home/getCoupon')
      .then((response) => response.json())
      .then((json) => {
        const list = json?.data || json?.records || [];
        setCoupons(Array.isArray(list) ? list.slice(0, 4) : []);
      })
      .catch(() => undefined)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const claim = async (coupon: any) => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/user/coupon/claim', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        body: JSON.stringify({ couponId: coupon.couponId }),
      });
      const json = await response.json();
      if (!response.ok || (json.code !== undefined && json.code !== 0)) throw new Error(json.msg || '领取失败');
      load();
    } catch (error) {
      alert(error instanceof Error ? error.message : '领取失败');
    }
  };

  if (loading || !coupons.length) return null;

  return (
    <Frame frame={module.frame}>
      <div className="dec-coupon-head">
        <b>{module.title || '领券中心'}</b>
        {module.desc ? <span>{module.desc}</span> : null}
        <Link href="/coupon/list">查看更多 &gt;</Link>
      </div>
      <div className="dec-coupon-grid">
        {coupons.map((coupon) => (
          <div key={coupon.couponId} className="dec-coupon-card">
            <div className="dec-coupon-card__value">
              {coupon.couponType === 2 ? <b>{coupon.couponDiscount}<small>折</small></b> : <b><small>¥</small>{Number(coupon.couponMoney || 0).toFixed(0)}</b>}
              <span>满 {Number(coupon.minOrderAmount || 0).toFixed(0)} 可用</span>
            </div>
            <div className="dec-coupon-card__info">
              <b>{coupon.couponName || '优惠券'}</b>
              <span>{coupon.couponDesc || '全场通用'}</span>
            </div>
            <button type="button" onClick={() => void claim(coupon)}>
              {coupon.isReceive ? '已领取' : '立即领取'}
            </button>
          </div>
        ))}
      </div>
    </Frame>
  );
}

function Seckill({ module }: ModuleProps) {
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [now, setNow] = useState(0);

  useEffect(() => {
    fetch('/api/home/home/getSeckill?page=1&size=6')
      .then((response) => response.json())
      .then((json) => {
        const list = json?.data?.records || json?.records || [];
        setProducts(Array.isArray(list) ? list : []);
      })
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, []);

  const endTime = (() => {
    const first = products[0];
    const raw = first?.seckkillData?.seckillEndTime || first?.seckillEndTime;
    const time = raw ? new Date(raw).getTime() : 0;
    return Number.isFinite(time) ? time : 0;
  })();

  useEffect(() => {
    if (!endTime) return;
    setNow(Date.now());
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, [endTime]);

  if (loading || !products.length) return null;

  const remaining = endTime && now ? Math.max(0, endTime - now) : 0;
  const countdown = now && remaining > 0
    ? `${String(Math.floor(remaining / 3600000)).padStart(2, '0')}:${String(Math.floor((remaining % 3600000) / 60000)).padStart(2, '0')}:${String(Math.floor((remaining % 60000) / 1000)).padStart(2, '0')}`
    : '';

  return (
    <Frame frame={module.frame}>
      <div className="dec-promo-head">
        <div>
          <b>{module.title?.titleText || '限时秒杀'}</b>
          <span>{module.title?.descText || '每日限时低价'}</span>
        </div>
        <div className="dec-promo-head__right">
          {countdown ? <span className="dec-promo-countdown">距结束 {countdown}</span> : null}
          <Link href="/search?tag=seckill">更多秒杀 &gt;</Link>
        </div>
      </div>
      <div className="dec-promo-grid">
        {products.map((product) => (
          <Link key={product.productId} href={`/product/${product.productId}`} className="dec-promo-card">
            <div className="dec-promo-card__image"><SafeImage src={product.picThumb || product.picUrl || ''} alt={product.productName || ''} /></div>
            <div className="dec-promo-card__name">{product.productName}</div>
            <div className="dec-promo-card__price">
              <b>¥ {Number(product.seckillPrice || product.productPrice || 0).toFixed(2)}</b>
              <s>¥ {Number(product.productPrice || 0).toFixed(2)}</s>
            </div>
            <div className="dec-promo-card__meta">已抢 {product.seckillSales || 0} 件</div>
          </Link>
        ))}
      </div>
    </Frame>
  );
}

function Groupon({ module }: ModuleProps) {
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/promotion/groupon/itemList?page=1&size=6')
      .then((response) => response.json())
      .then((json) => {
        const list = json?.data?.records || json?.records || [];
        setProducts(Array.isArray(list) ? list : []);
      })
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, []);

  if (loading || !products.length) return null;

  return (
    <Frame frame={module.frame}>
      <div className="dec-promo-head">
        <div>
          <b>{module.title?.titleText || '拼团活动'}</b>
          <span>{module.title?.descText || '邀请好友一起拼'}</span>
        </div>
        <Link href="/search?tag=groupon">更多拼团 &gt;</Link>
      </div>
      <div className="dec-promo-grid">
        {products.map((product) => (
          <Link key={product.productId || product.id} href={`/product/${product.productId || product.id}`} className="dec-promo-card">
            <div className="dec-promo-card__image"><SafeImage src={product.picThumb || product.picUrl || ''} alt={product.productName || ''} /></div>
            <div className="dec-promo-card__name">{product.productName}</div>
            <div className="dec-promo-card__price">
              <b>¥ {Number(product.grouponPrice || product.productPrice || 0).toFixed(2)}</b>
              <s>¥ {Number(product.productPrice || 0).toFixed(2)}</s>
            </div>
            <div className="dec-promo-card__meta">{product.grouponNum || 2} 人成团</div>
          </Link>
        ))}
      </div>
    </Frame>
  );
}

function ProductGroup({ module }: ModuleProps) {
  const groups: Array<Record<string, any>> = Array.isArray(module.productGroups) ? module.productGroups : [];
  const [active, setActive] = useState(0);
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    const group = groups[active];
    if (!group) {
      setProducts([]);
      setLoading(false);
      return;
    }
    const params = new URLSearchParams({ page: '1', size: '12' });
    if (group.sourceType === 'productGroup' && group.productGroupId) {
      params.set('productGroupId', String(group.productGroupId));
    } else {
      const ids = (Array.isArray(group.productList) ? group.productList : [])
        .map((item: any) => item.productId || item.id)
        .filter(Boolean);
      if (!ids.length) {
        setProducts([]);
        setLoading(false);
        return;
      }
      params.set('ids', ids.join(','));
      params.set('size', String(ids.length));
    }
    setLoading(true);
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
  }, [JSON.stringify(groups), active]);

  if (!groups.length) return null;
  const showTitle = Number(module.groupTitle?.showTitle ?? 1) !== 0;
  const showName = Number(module.showProductName ?? 1) !== 0;
  const showPrice = Number(module.showProductPrice ?? 1) !== 0;

  return (
    <Frame frame={module.frame}>
      {showTitle ? (
        <div className="dec-product-group__tabs">
          {groups.map((group, index) => (
            <button
              key={index}
              type="button"
              className={index === active ? 'is-active' : ''}
              onClick={() => setActive(index)}
            >
              {group.titlePic ? <SafeImage src={imageUrl(group.titlePic)} alt="" /> : null}
              <span>{group.title || `分组 ${index + 1}`}</span>
            </button>
          ))}
        </div>
      ) : null}
      {loading ? (
        <div className="dec-product-grid">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="dec-product-card"><span className="dec-skeleton-line" /></div>
          ))}
        </div>
      ) : (
        <div className="dec-product-grid">
          {products.map((product) => (
            <Link key={product.productSn || product.productId} href={`/product/${product.productId || ''}`} className="dec-product-card">
              <div className="dec-product-card__image">
                <SafeImage src={product.picThumb || product.picUrl || ''} alt={product.productName || ''} />
              </div>
              {showName ? <div className="dec-product-card__name">{product.productName || '商品'}</div> : null}
              {showPrice ? <div className="dec-product-card__price"><b>¥</b> {Number(product.productPrice || 0).toFixed(2)}</div> : null}
            </Link>
          ))}
          {!products.length ? <div className="dec-empty">该分组暂无商品</div> : null}
        </div>
      )}
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
      <div className="dec-fallback">
        <span>{module.label || '装修模块'}</span>
        <span className="dec-fallback__hint">
          该模块已注册，React 版渲染待补齐
        </span>
      </div>
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
