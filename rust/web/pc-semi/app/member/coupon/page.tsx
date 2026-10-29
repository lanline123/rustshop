import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import CouponClient from '@/components/CouponClient';
import { getPcHeader } from '@/lib/api';

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
      <footer className="pc-footer"><div className="pc-container pc-footer__inner"><span>万物新生 · 退役动力电池循环利用产业服务平台</span><span>安全合规 · 高效协同 · 绿色循环 · 数据可追溯</span></div></footer>
    </>
  );
}
