import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import PcFooter from '@/components/PcFooter';
import { getArticleIssueInfo, getPcHeader } from '@/lib/api';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '帮助详情 · 万物新生' };

export default async function ArticleIssueInfoPage({ searchParams }: { searchParams?: { [key: string]: string | string[] | undefined } }) {
  const raw = searchParams?.articleId;
  const articleId = Number(Array.isArray(raw) ? raw[0] : raw) || 0;
  const [header, data] = await Promise.all([getPcHeader().catch(() => null), articleId ? getArticleIssueInfo(articleId) : Promise.resolve(null)]);
  const item = data?.item || null;

  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-static-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb">
            <Link href="/">首页</Link><span>/</span><Link href="/article/issue">帮助中心</Link><span>/</span><span>{item?.articleTitle || '文章详情'}</span>
          </nav>
          {item ? (
            <article className="pc-article">
              <h1>{item.articleTitle}</h1>
              <div className="pc-article__meta">{item.description || ''}</div>
              <div className="pc-article__content" dangerouslySetInnerHTML={{ __html: item.content || '' }} />
              <div className="pc-article__nav">
                {data?.prev ? <Link href={`/article/issue/info?articleId=${data.prev.articleId}`}>上一篇：{data.prev.articleTitle}</Link> : <span />}
                {data?.next ? <Link href={`/article/issue/info?articleId=${data.next.articleId}`}>下一篇：{data.next.articleTitle}</Link> : null}
              </div>
            </article>
          ) : (
            <div className="pc-static-card"><b>文章不存在</b><Link href="/article/issue">返回帮助中心 →</Link></div>
          )}
        </div>
      </main>
      <PcFooter />
    </>
  );
}
