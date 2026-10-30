import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import BalanceClient from '@/components/BalanceClient';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '账户余额 · 万物新生', robots: { index: false, follow: false } };

export default async function BalancePage() {
  const header = await getPcHeader().catch(() => null);
  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-member-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb"><Link href="/">首页</Link><span>/</span><Link href="/member">会员中心</Link><span>/</span><span>账户余额</span></nav>
          <h1 className="pc-cart-title">账户余额</h1>
          <BalanceClient />
        </div>
      </main>
      <PcFooter />
    </>
  );
}
