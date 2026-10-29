import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import CouponCenterClient from '@/components/CouponCenterClient';
import { getPcHeader } from '@/lib/api';

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
      <footer className="pc-footer">
        <div className="pc-container pc-footer__inner">
          <span>万物新生 · 退役动力电池循环利用产业服务平台</span>
          <span>安全合规 · 高效协同 · 绿色循环 · 数据可追溯</span>
        </div>
      </footer>
    </>
  );
}
