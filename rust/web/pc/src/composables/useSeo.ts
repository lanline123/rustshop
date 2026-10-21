/**
 * SEO 头管理（PC 商城端）。
 *
 * SPA 的 `<title>` / `<meta>` 不会随路由变化，必须在数据加载后手动更新，
 * 否则搜索引擎/社交媒体抓到的永远是首页信息。
 */
export interface SeoOptions {
  /** 页面标题（不含站点后缀） */
  title?: string;
  description?: string;
  keywords?: string;
  /** 分享图（绝对 URL 或以 / 开头） */
  image?: string;
  /** canonical URL；不传则用当前地址 */
  canonical?: string;
  /** og:type，默认 website */
  type?: string;
  /** 是否追加站点名后缀，默认 true */
  withSiteName?: boolean;
  /** 是否禁止索引（如结算页） */
  noindex?: boolean;
}

const SITE_NAME = 'TigShop 官方旗舰商城';

let managed: { key: string; el: Element }[] = [];

function upsertMeta(attr: 'name' | 'property', key: string, content: string) {
  let el = document.head.querySelector<HTMLMetaElement>(`meta[${attr}="${key}"]`);
  if (!el) {
    el = document.createElement('meta');
    el.setAttribute(attr, key);
    document.head.appendChild(el);
  }
  el.setAttribute('content', content);
  managed.push({ key: `${attr}:${key}`, el });
}

function upsertLink(rel: string, href: string) {
  let el = document.head.querySelector<HTMLLinkElement>(`link[rel="${rel}"]`);
  if (!el) {
    el = document.createElement('link');
    el.setAttribute('rel', rel);
    document.head.appendChild(el);
  }
  el.setAttribute('href', href);
  managed.push({ key: `link:${rel}`, el });
}

function clearManaged() {
  for (const { el } of managed) {
    el.parentNode?.removeChild(el);
  }
  managed = [];
}

/**
 * 应用 SEO 头。
 *
 * 注意：该函数不依赖 Vue 路由上下文，可在异步数据加载完成后调用。
 */
export function applySeo(options: SeoOptions = {}): void {
  const {
    title,
    description,
    keywords,
    image,
    canonical,
    type = 'website',
    withSiteName = true,
    noindex = false,
  } = options;

  clearManaged();

  const finalTitle = title
    ? withSiteName
      ? `${title} - ${SITE_NAME}`
      : title
    : SITE_NAME;
  document.title = finalTitle;

  if (description) {
    upsertMeta('name', 'description', description);
    upsertMeta('property', 'og:description', description);
  }
  if (keywords) upsertMeta('name', 'keywords', keywords);

  upsertMeta('property', 'og:title', finalTitle);
  upsertMeta('property', 'og:type', type);
  upsertMeta('property', 'og:url', canonical || window.location.href);
  if (image) {
    upsertMeta('property', 'og:image', image);
    upsertMeta('name', 'twitter:image', image);
  }
  upsertMeta('name', 'twitter:card', image ? 'summary_large_image' : 'summary');
  upsertMeta('name', 'twitter:title', finalTitle);
  if (description) upsertMeta('name', 'twitter:description', description);

  if (noindex) {
    upsertMeta('name', 'robots', 'noindex,nofollow');
  } else {
    upsertMeta('name', 'robots', 'index,follow');
  }

  upsertLink('canonical', canonical || window.location.href.split('?')[0]);
}

/** `useSeo` 的别名，语义更贴近 Vue 组合式 API 习惯。 */
export function useSeo(options: SeoOptions = {}) {
  applySeo(options);
}

/**
 * 把后端返回的相对图片路径解析为可直接展示的 URL。
 *
 * - `http(s)://`、`data:`、`blob:` 原样返回；
 * - 以 `/` 开头视为站点绝对路径；
 * - 其它相对路径拼到 `baseUrl` 上（默认 `/static`）。
 */
export function resolveImageUrl(value: string | undefined | null, baseUrl = '/static'): string {
  if (!value) return '';
  const url = value.trim();
  if (!url) return '';
  if (/^(https?:)?\/\//i.test(url) || /^(data|blob):/i.test(url)) return url;
  if (url.startsWith('/')) return url;
  return `${baseUrl.replace(/\/+$/, '')}/${url.replace(/^\/+/, '')}`;
}

export default useSeo;
