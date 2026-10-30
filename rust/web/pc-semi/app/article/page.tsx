import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '资讯中心 · 万物新生' };

export default async function ArticlePage() {
  const header = await getPcHeader().catch(() => null);
  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-static-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb"><Link href="/">首页</Link><span>/</span><span>资讯中心</span></nav>
          <h1 className="pc-cart-title">资讯中心</h1>
          <div className="pc-static-card">
            <b>资讯内容正在迁移</b>
            <p>原 PC 端文章列表与详情将在后续版本接入内容接口。</p>
            <Link href="/help">前往帮助中心 →</Link>
          </div>
        </div>
      </main>
      <PcFooter />
    </>
  );
}
