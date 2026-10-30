import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import AfterSalesDetailClient from '@/components/AfterSalesDetailClient';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '售后详情 · 万物新生', robots: { index: false, follow: false } };

export default async function AfterSalesDetailPage({ searchParams }: { searchParams?: { [key: string]: string | string[] | undefined } }) {
  const raw = searchParams?.id;
  const id = Number(Array.isArray(raw) ? raw[0] : raw) || 0;
  const header = await getPcHeader().catch(() => null);
  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-member-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb"><Link href="/">首页</Link><span>/</span><Link href="/member">会员中心</Link><span>/</span><Link href="/member/aftersales">售后服务</Link><span>/</span><span>售后详情</span></nav>
          <h1 className="pc-cart-title">售后详情</h1>
          <AfterSalesDetailClient id={id} />
        </div>
      </main>
      <PcFooter />
    </>
  );
}
