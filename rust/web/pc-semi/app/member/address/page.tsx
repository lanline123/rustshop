import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import AddressClient from '@/components/AddressClient';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '收货地址 · 万物新生', robots: { index: false, follow: false } };

export default async function AddressPage() {
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
            <span>收货地址</span>
          </nav>
          <h1 className="pc-cart-title">收货地址</h1>
          <AddressClient />
        </div>
      </main>
      <PcFooter />
    </>
  );
}
