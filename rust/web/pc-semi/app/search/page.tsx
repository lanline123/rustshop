import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import ProductGrid from '@/components/ProductGrid';
import { getPcHeader, getProducts } from '@/lib/api';

export const dynamic = 'force-dynamic';

interface PageProps {
  searchParams?: { [key: string]: string | string[] | undefined };
}

function readParam(value: string | string[] | undefined): string {
  return Array.isArray(value) ? value[0] || '' : value || '';
}

export async function generateMetadata({ searchParams }: PageProps): Promise<Metadata> {
  const keyword = readParam(searchParams?.keyword);
  return { title: keyword ? `${keyword} · 搜索 · 万物新生` : '搜索商品 · 万物新生' };
}

const SORTS = [
  { key: 'default', label: '综合' },
  { key: 'price-asc', label: '价格升序' },
  { key: 'price-desc', label: '价格降序' },
  { key: 'new', label: '最新' },
];

export default async function SearchPage({ searchParams }: PageProps) {
  const keyword = readParam(searchParams?.keyword);
  const categoryId = readParam(searchParams?.categoryId);
  const sort = readParam(searchParams?.sort) || 'default';
  const page = Math.max(1, Number(readParam(searchParams?.page)) || 1);
  const size = 24;

  const sortMap: Record<string, { sortField?: string; sortOrder?: 'asc' | 'desc' }> = {
    'price-asc': { sortField: 'productPrice', sortOrder: 'asc' },
    'price-desc': { sortField: 'productPrice', sortOrder: 'desc' },
    new: { sortField: 'add_time', sortOrder: 'desc' },
  };

  const [products, header] = await Promise.all([
    getProducts({ page, size, keyword, categoryId, ...(sortMap[sort] || {}) }).catch(() => ({ records: [], total: 0 })),
    getPcHeader().catch(() => null),
  ]);
  const pages = Math.ceil(products.total / size);

  const queryFor = (nextSort: string, nextPage = 1) => {
    const query = new URLSearchParams();
    if (keyword) query.set('keyword', keyword);
    if (categoryId) query.set('categoryId', categoryId);
    if (nextSort !== 'default') query.set('sort', nextSort);
    if (nextPage > 1) query.set('page', String(nextPage));
    return query.toString();
  };

  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-list-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb">
            <Link href="/">首页</Link>
            <span>/</span>
            <span>{keyword ? `搜索：${keyword}` : categoryId ? '分类商品' : '全部商品'}</span>
          </nav>
          <div className="pc-list-head">
            <h1>{keyword ? `“${keyword}”的搜索结果` : '全部商品'}</h1>
            <span>共 {products.total} 件商品</span>
          </div>

          <div className="pc-list-toolbar">
            <div className="pc-list-sorts">
              {SORTS.map((item) => (
                <Link key={item.key} href={`/search?${queryFor(item.key)}`} className={sort === item.key ? 'is-active' : ''}>
                  {item.label}
                </Link>
              ))}
            </div>
            {categoryId ? <span className="pc-list-filter-tip">分类 ID：{categoryId}</span> : null}
          </div>

          <ProductGrid products={products.records} />

          {pages > 1 ? (
            <nav className="pc-pagination">
              {Array.from({ length: pages }).map((_, index) => {
                const next = index + 1;
                return (
                  <Link key={next} href={`/search?${queryFor(sort, next)}`} className={next === page ? 'is-active' : ''}>
                    {next}
                  </Link>
                );
              })}
            </nav>
          ) : null}
        </div>
      </main>
      <footer className="pc-footer">
        <div className="pc-container pc-footer__inner">
          <span>万物新生 · 退役动力电池循环利用产业服务平台</span>
          <span>安全合规 · 高效协同 · 绿色循环 · 数据可追溯</span>
        </div>
      </footer>
    </>
  );
}
