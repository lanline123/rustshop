import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import CheckoutClient from '@/components/CheckoutClient';
import { getPcHeader } from '@/lib/api';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = {
  title: '确认订单 · 万物新生',
  robots: { index: false, follow: false },
};

export default async function CheckoutPage({
  searchParams,
}: {
  searchParams?: { [key: string]: string | string[] | undefined };
}) {
  const raw = searchParams?.cartIds;
  const text = Array.isArray(raw) ? raw[0] || '' : raw || '';
  const cartIds = text
    .split(',')
    .map((item) => Number(item))
    .filter((item) => Number.isFinite(item) && item > 0);
  const header = await getPcHeader().catch(() => null);

  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-checkout-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb">
            <Link href="/">首页</Link>
            <span>/</span>
            <Link href="/cart">购物车</Link>
            <span>/</span>
            <span>确认订单</span>
          </nav>
          <h1 className="pc-cart-title">确认订单</h1>
          <CheckoutClient cartIds={cartIds} />
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
