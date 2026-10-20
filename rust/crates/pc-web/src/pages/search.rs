use leptos::prelude::*;
use crate::components::product_card::ProductCard;
use crate::loader::SearchData;

#[component]
pub fn SearchPage() -> impl IntoView {
    let data_opt = use_context::<SearchData>();

    let total = data_opt.as_ref().map(|d| d.total).unwrap_or(0);
    let keyword = data_opt.as_ref().map(|d| d.keyword.clone()).unwrap_or_default();
    let products = data_opt.as_ref().map(|d| d.products.clone()).unwrap_or_default();

    let display_title = if keyword.is_empty() {
        "全部商品列表".to_string()
    } else {
        format!("\"{}\" 的搜索结果", keyword)
    };

    view! {
        <div class="search-page" style="background: #f2f3f5; min-height: 80vh; padding: 20px 0 50px;">
            <div class="container">
                // 面包屑
                <div class="breadcrumb acea-row row-middle" style="font-size: 13px; color: #666; margin-bottom: 15px; gap: 8px;">
                    <a href="/" style="color: #666;">"首页"</a>
                    <span>">"</span>
                    <span style="color: #333; font-weight: bold;">{display_title}</span>
                    <span style="color: #999;">{format!("(共 {} 件相关商品)", total)}</span>
                </div>

                // 筛选面板
                <div class="filter-panel" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 15px 20px; margin-bottom: 20px;">
                    <div style="display: flex; align-items: center; padding: 8px 0; border-bottom: 1px dashed #eee;">
                        <span style="width: 80px; font-weight: bold; color: #666; font-size: 13px;">"品牌："</span>
                        <div style="display: flex; gap: 16px; flex-wrap: wrap; font-size: 13px; color: #333;">
                            <a href="#" style="color: var(--general); font-weight: bold;">"全部"</a>
                            <a href="/search?keyword=Apple">"Apple (苹果)"</a>
                            <a href="/search?keyword=华为">"HUAWEI (华为)"</a>
                            <a href="/search?keyword=小米">"Xiaomi (小米)"</a>
                            <a href="/search?keyword=Sony">"Sony (索尼)"</a>
                            <a href="/search?keyword=ThinkPad">"ThinkPad (联想)"</a>
                        </div>
                    </div>
                    <div style="display: flex; align-items: center; padding: 8px 0;">
                        <span style="width: 80px; font-weight: bold; color: #666; font-size: 13px;">"价格区间："</span>
                        <div style="display: flex; gap: 16px; font-size: 13px; color: #333;">
                            <a href="#" style="color: var(--general); font-weight: bold;">"全部"</a>
                            <a href="#">"0-500元"</a>
                            <a href="#">"500-2000元"</a>
                            <a href="#">"2000-5000元"</a>
                            <a href="#">"5000元以上"</a>
                        </div>
                    </div>
                </div>

                // 排序工具条 (sort-filter-wrap)
                <div class="sort-filter-wrap" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 8px 15px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between;">
                    <div class="sort-filter-left" style="display: flex; gap: 4px;">
                        <button class="sort-item active" style="border: 1px solid var(--general); background: var(--general); color: #fff; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer; font-weight: 500;">
                            "综合排序"
                        </button>
                        <button class="sort-item" style="border: 1px solid #ddd; background: #fff; color: #666; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer;">
                            "销量 ⬇"
                        </button>
                        <button class="sort-item" style="border: 1px solid #ddd; background: #fff; color: #666; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer;">
                            "价格 ⬆"
                        </button>
                        <button class="sort-item" style="border: 1px solid #ddd; background: #fff; color: #666; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer;">
                            "新品"
                        </button>
                    </div>
                    <div class="sort-filter-right" style="font-size: 13px; color: #999;">
                        <span>"第 1 页 · 共 "{total}" 个商品"</span>
                    </div>
                </div>

                // 商品列表网格 (4列布局)
                <div class="product-grid" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px;">
                    {
                        products.iter().map(|p| {
                            view! {
                                <ProductCard
                                    id=p.id
                                    name=p.name.clone()
                                    price=p.price.clone()
                                    market_price=p.market_price.clone()
                                    pic_thumb=p.pic_thumb.clone()
                                    sales=p.sales
                                    badges=p.tags.clone()
                                />
                            }
                        }).collect::<Vec<_>>()
                    }
                </div>
            </div>
        </div>
    }
}
