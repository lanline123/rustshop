import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import PcFooter from '@/components/PcFooter';
import { getFooterData, getPcHeader } from '@/lib/api';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '帮助中心 · 万物新生' };

export default async function ArticleIssuePage({ searchParams }: { searchParams?: { [key: string]: string | string[] | undefined } }) {
  const raw = searchParams?.categoryId;
  const categoryId = Array.isArray(raw) ? raw[0] || '' : raw || '';
  const [header, footer] = await Promise.all([getPcHeader().catch(() => null), getFooterData()]);
  const categories = categoryId ? footer.helpCategories.filter((item) => item.categorySn === categoryId || String(item.articleCategoryId) === categoryId) : footer.helpCategories;

  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-static-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb"><Link href="/">首页</Link><span>/</span><span>帮助中心</span></nav>
          <h1 className="pc-cart-title">{categoryId ? '帮助分类' : '帮助中心'}</h1>
          <div className="pc-article-categories">
            {(categories.length ? categories : footer.helpCategories).map((category) => (
              <div key={category.articleCategoryId} className="pc-article-category">
                <h2>{category.articleCategoryName}</h2>
                <div className="pc-article-links">
                  {(category.articles || []).map((article: any) => (
                    <Link key={article.articleId} href={`/article/issue/info?articleId=${article.articleId}`}>
                      {article.articleTitle}
                    </Link>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
      <PcFooter />
    </>
  );
}
