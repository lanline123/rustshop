import Link from 'next/link';
import { getFooterData } from '@/lib/api';

function articleHref(articleId: unknown): string {
  return `/article/issue/info?articleId=${encodeURIComponent(String(articleId ?? ''))}`;
}

export default async function PcFooter({ showFriendly = false }: { showFriendly?: boolean }) {
  const { config, bottomNav, friendLinks, helpCategories } = await getFooterData();
  const icp = config?.shopIcpNo as string | undefined;
  const icpUrl = (config?.shopIcpNoUrl as string | undefined) || 'https://beian.miit.gov.cn/';
  const phone = config?.kefuPhone as string | undefined;
  const serviceTime = config?.kefuTime as string | undefined;
  const address = (config?.companyAddress || config?.kefuAddress) as string | undefined;

  return (
    <footer className="pc-footer">
      {helpCategories.length ? (
        <div className="pc-footer__help">
          <div className="pc-container pc-footer__help-inner">
            {helpCategories.map((category) => (
              <div key={category.articleCategoryId || category.categorySn} className="pc-footer__help-col">
                <Link href={`/article/issue?categoryId=${encodeURIComponent(String(category.categorySn || ''))}`} className="pc-footer__help-title">
                  {category.articleCategoryName}
                </Link>
                {(category.articles || []).map((article: any) => (
                  <Link key={article.articleId} href={articleHref(article.articleId)} className="pc-footer__help-link">
                    {article.articleTitle}
                  </Link>
                ))}
              </div>
            ))}
          </div>
        </div>
      ) : null}

      <div className="pc-footer__bottom">
        <div className="pc-container pc-footer__inner">
          {bottomNav.length ? (
            <nav className="pc-footer__nav">
              {bottomNav.map((item, index) => {
                const href = item?.link?.link || (item?.link?.id ? articleHref(item.link.id) : '/');
                return (
                  <Link key={item.id || index} href={href}>
                    {item.title}
                  </Link>
                );
              })}
            </nav>
          ) : null}

          {showFriendly && friendLinks.length ? (
            <div className="pc-footer__friendly">
              <span>友情链接：</span>
              {friendLinks.map((item) => (
                <a key={item.linkId} href={item.linkUrl} target="_blank" rel="nofollow noreferrer">
                  {item.linkTitle}
                </a>
              ))}
            </div>
          ) : null}

          <div className="pc-footer__copy">
            <p>
              {icp ? (
                <a href={icpUrl} target="_blank" rel="nofollow noreferrer">
                  {icp}
                </a>
              ) : null}
              {config?.shop110No ? <span> {config.shop110No}</span> : null}
            </p>
            {phone || serviceTime || address ? (
              <p>
                {phone ? <span>客服热线：{phone}</span> : null}
                {serviceTime ? <span>客服时间：{serviceTime}</span> : null}
                {address ? <span>联系地址：{address}</span> : null}
              </p>
            ) : null}
            <p className="pc-footer__copyright">万物新生 · 退役动力电池循环利用产业服务平台</p>
          </div>
        </div>
      </div>
    </footer>
  );
}
