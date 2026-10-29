import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import AfterSalesApplyClient from '@/components/AfterSalesApplyClient';
import { getPcHeader } from '@/lib/api';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '申请售后 · 万物新生', robots: { index: false, follow: false } };

export default async function AfterSalesApplyPage({ searchParams }: { searchParams?: { [key: string]: string | string[] | undefined } }) {
  const read = (value: string | string[] | undefined) => Number(Array.isArray(value) ? value[0] : value) || 0;
  const orderId = read(searchParams?.orderId);
  const itemId = read(searchParams?.itemId);
  const header = await getPcHeader().catch(() => null);
  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-member-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb"><Link href="/">首页</Link><span>/</span><Link href="/member">会员中心</Link><span>/</span><Link href="/member/aftersales">售后服务</Link><span>/</span><span>申请售后</span></nav>
          <h1 className="pc-cart-title">申请售后</h1>
          <AfterSalesApplyClient orderId={orderId} itemId={itemId} />
        </div>
      </main>
      <footer className="pc-footer"><div className="pc-container pc-footer__inner"><span>万物新生 · 退役动力电池循环利用产业服务平台</span><span>安全合规 · 高效协同 · 绿色循环 · 数据可追溯</span></div></footer>
    </>
  );
}
