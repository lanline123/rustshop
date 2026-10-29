import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import CouponCenterClient from '@/components/CouponCenterClient';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '领券中心 · 万物新生' };

export default async function CouponCenterPage() {
  const header = await getPcHeader().catch(() => null);
  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-member-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb">
            <Link href="/">首页</Link>
            <span>/</span>
            <span>领券中心</span>
          </nav>
          <h1 className="pc-cart-title">领券中心</h1>
          <CouponCenterClient />
        </div>
      </main>
      <PcFooter />
    </>
  );
}
