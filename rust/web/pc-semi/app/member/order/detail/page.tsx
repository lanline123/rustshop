import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import OrderDetailClient from '@/components/OrderDetailClient';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = {
  title: '订单详情 · 万物新生',
  robots: { index: false, follow: false },
};

export default async function MemberOrderDetailPage({
  searchParams,
}: {
  searchParams?: { [key: string]: string | string[] | undefined };
}) {
  const raw = searchParams?.id;
  const orderId = Number(Array.isArray(raw) ? raw[0] : raw) || 0;
  const header = await getPcHeader().catch(() => null);

  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-member-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb">
            <Link href="/">首页</Link>
            <span>/</span>
            <Link href="/member">会员中心</Link>
            <span>/</span>
            <Link href="/member/order/list">我的订单</Link>
            <span>/</span>
            <span>订单详情</span>
          </nav>
          <h1 className="pc-cart-title">订单详情</h1>
          <OrderDetailClient orderId={orderId} />
        </div>
      </main>
      <PcFooter />
    </>
  );
}
