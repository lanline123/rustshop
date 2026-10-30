import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import CouponClient from '@/components/CouponClient';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '我的优惠券 · 万物新生', robots: { index: false, follow: false } };

export default async function CouponPage() {
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
            <span>我的优惠券</span>
          </nav>
          <h1 className="pc-cart-title">我的优惠券</h1>
          <CouponClient />
        </div>
      </main>
      <PcFooter />
    </>
  );
}
