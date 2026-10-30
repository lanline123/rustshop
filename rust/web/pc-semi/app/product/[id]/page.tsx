import type { Metadata } from 'next';
import { notFound } from 'next/navigation';
import Link from 'next/link';
import PcHeader from '@/components/PcHeader';
import ProductDetailClient from '@/components/ProductDetailClient';
import { getPcHeader, getProductDetail } from '@/lib/api';
import PcFooter from '@/components/PcFooter';

export const dynamic = 'force-dynamic';

interface PageProps {
  params: { id: string };
}

async function loadProduct(id: string) {
  const productId = Number(id);
  if (!Number.isFinite(productId) || productId <= 0) return null;
  try {
    return await getProductDetail(productId);
  } catch {
    return null;
  }
}

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const detail = await loadProduct(params.id);
  const item = detail?.item;
  if (!item) return { title: '商品不存在 · 万物新生' };
  return {
    title: `${item.productName} · 万物新生`,
    description: item.productBrief || item.keywords || item.productName,
    openGraph: {
      title: item.productName,
      description: item.productBrief || item.productName,
      images: item.picLarge ? [item.picLarge] : undefined,
    },
  };
}

export default async function ProductPage({ params }: PageProps) {
  const detail = await loadProduct(params.id);
  if (!detail) notFound();
  const header = await getPcHeader().catch(() => null);

  return (
    <>
      <PcHeader config={header as any} />
      <main className="pc-product-page">
        <div className="pc-container">
          <nav className="pc-breadcrumb">
            <Link href="/">首页</Link>
            <span>/</span>
            <span>{detail.item?.productName || '商品详情'}</span>
          </nav>
          <ProductDetailClient detail={detail} />
        </div>
      </main>
      <PcFooter />
    </>
  );
}
