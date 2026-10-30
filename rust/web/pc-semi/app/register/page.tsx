import type { Metadata } from 'next';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import { getPcHeader } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';
export const metadata: Metadata = { title: '注册 · 万物新生' };

export default async function RegisterPage() {
  const header = await getPcHeader().catch(() => null);
  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-static-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb"><Link href="/">首页</Link><span>/</span><span>注册</span></nav>
          <h1 className="pc-cart-title">注册账号</h1>
          <div className="pc-static-card">
            <b>手机号注册</b>
            <p>注册需要短信验证码。当前版本保留注册入口，短信注册流程将在后续版本接入。</p>
            <p>已有账号可直接登录。</p>
            <Link href="/login">前往登录 →</Link>
          </div>
        </div>
      </main>
      <PcFooter />
    </>
  );
}
