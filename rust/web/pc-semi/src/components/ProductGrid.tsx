import Link from 'next/link';
import type { ProductListItem } from '@/lib/api';

function money(value: unknown): string {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export default function ProductGrid({ products }: { products: ProductListItem[] }) {
  return (
    <div className="pc-product-grid">
      {products.map((product) => (
        <Link key={product.productId} href={`/product/${product.productId}`} className="pc-product-grid__card">
          <div className="pc-product-grid__image">
            <img src={product.picThumb || product.picUrl || ''} alt={product.productName || ''} loading="lazy" />
          </div>
          <div className="pc-product-grid__body">
            <div className="pc-product-grid__name">{product.productName}</div>
            {product.brief ? <div className="pc-product-grid__brief">{product.brief}</div> : null}
            <div className="pc-product-grid__bottom">
              <b>
                <small>¥</small> {money(product.productPrice)}
              </b>
              <span>库存 {product.productStock ?? 0}</span>
            </div>
          </div>
        </Link>
      ))}
      {!products.length ? <div className="pc-list-empty">没有找到商品</div> : null}
    </div>
  );
}
