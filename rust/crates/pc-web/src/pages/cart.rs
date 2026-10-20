use leptos::prelude::*;

#[component]
pub fn CartPage() -> impl IntoView {
    // 模拟购物车数据项
    let cart_items = vec![
        (1, 1001, "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G手机", "原色钛金属 / 256GB", "8999.00", 1, "/static/images/goods_empty.png", true),
        (2, 1005, "Sony WH-1000XM5 无线头戴式降噪耳机 铂金银", "铂金银 / 标配", "2299.00", 1, "/static/images/goods_empty.png", true),
    ];

    view! {
        <div class="cart-page" style="background: #f6f6f6; padding: 25px 0 60px 0;">
            <div class="container" style="max-width: 1200px; margin: 0 auto;">
                // 页面标题栏
                <div class="flex justify-between align-baseline" style="margin-bottom: 20px;">
                    <h2 style="font-size: 22px; color: #222; font-weight: bold; margin: 0;">"我的购物车"</h2>
                    <span style="font-size: 13px; color: #888;">"温馨提示：商品价格和库存以最终提交订单时为准"</span>
                </div>

                // 购物车表头
                <div class="cart-table-head flex align-center" style="background: #fff; padding: 15px 25px; border-radius: 6px; font-size: 13px; color: #666; margin-bottom: 15px; border: 1px solid #eee;">
                    <div style="width: 80px;" class="flex align-center">
                        <input type="checkbox" checked=true style="margin-right: 8px; cursor: pointer;"/>
                        <span>"全选"</span>
                    </div>
                    <div style="flex: 1;">"商品信息"</div>
                    <div style="width: 140px; text-align: center;">"单价"</div>
                    <div style="width: 140px; text-align: center;">"数量"</div>
                    <div style="width: 140px; text-align: center;">"小计"</div>
                    <div style="width: 100px; text-align: center;">"操作"</div>
                </div>

                // 购物车条目列表
                <div class="cart-items" style="display: flex; flex-direction: column; gap: 12px;">
                    {
                        cart_items.into_iter().map(|(_cart_id, _prod_id, name, sku, price, qty, pic, is_checked)| {
                            view! {
                                <div
                                    class="cart-item-row flex align-center"
                                    style="background: #fff; padding: 20px 25px; border-radius: 6px; border: 1px solid #eee; font-size: 13px;"
                                >
                                    // 复选框
                                    <div style="width: 40px;">
                                        <input type="checkbox" checked=is_checked style="cursor: pointer;"/>
                                    </div>

                                    // 商品信息（主图 + 标题 + SKU）
                                    <div class="flex" style="flex: 1; gap: 15px;">
                                        <div style="width: 80px; height: 80px; border: 1px solid #eee; border-radius: 4px; overflow: hidden; background: #fafafa; display: flex; align-items: center; justify-content: center;">
                                            <img src=pic style="max-width: 100%; max-height: 100%; object-fit: contain;"/>
                                        </div>
                                        <div class="flex flex-column justify-center" style="gap: 6px; max-width: 380px;">
                                            <a href=format!("/product/{}", _prod_id) style="color: #333; font-weight: bold; text-decoration: none; line-height: 1.4;">
                                                {name}
                                            </a>
                                            <span style="color: #999; font-size: 12px;">"规格: "{sku}</span>
                                        </div>
                                    </div>

                                    // 单价
                                    <div style="width: 140px; text-align: center; font-weight: bold; color: #333;">
                                        "¥"{price}
                                    </div>

                                    // 步进器
                                    <div style="width: 140px; display: flex; justify-content: center;">
                                        <div class="stepper flex align-center" style="border: 1px solid #ddd; border-radius: 3px; overflow: hidden;">
                                            <button style="width: 28px; height: 28px; border: none; background: #f5f5f5; cursor: pointer;">"-"</button>
                                            <input type="text" value=qty.to_string() style="width: 40px; height: 28px; border: none; text-align: center; font-size: 13px; outline: none;"/>
                                            <button style="width: 28px; height: 28px; border: none; background: #f5f5f5; cursor: pointer;">"+"</button>
                                        </div>
                                    </div>

                                    // 小计
                                    <div style="width: 140px; text-align: center; font-weight: bold; color: #ff3838; font-size: 15px;">
                                        "¥"{price}
                                    </div>

                                    // 操作按钮
                                    <div style="width: 100px; text-align: center;">
                                        <button
                                            style="background: transparent; border: none; color: #999; cursor: pointer; font-size: 12px;"
                                            onmouseover="this.style.color='#ff3838';"
                                            onmouseout="this.style.color='#999';"
                                        >
                                            "删除"
                                        </button>
                                    </div>
                                </div>
                            }
                        }).collect::<Vec<_>>()
                    }
                </div>

                // 底部固定结算条 (Sticky Bottom Bar)
                <div
                    class="checkout-bar flex justify-between align-center"
                    style="margin-top: 25px; background: #fff; padding: 15px 30px; border-radius: 6px; border: 1px solid #eee; box-shadow: 0 4px 15px rgba(0,0,0,0.05);"
                >
                    <div class="flex align-center" style="gap: 20px; font-size: 13px; color: #666;">
                        <label class="flex align-center" style="cursor: pointer;">
                            <input type="checkbox" checked=true style="margin-right: 8px;"/>
                            <span>"全选 (2)"</span>
                        </label>
                        <a href="#" style="color: #666; text-decoration: none;">"删除选中商品"</a>
                        <a href="#" style="color: #666; text-decoration: none;">"清理失效商品"</a>
                    </div>

                    <div class="flex align-center" style="gap: 25px;">
                        <div style="text-align: right;">
                            <div style="font-size: 14px; color: #444;">
                                "已选商品 "
                                <span style="color: #ff3838; font-weight: bold; font-size: 16px;">"2"</span>
                                " 件，合计："
                                <span style="color: #ff3838; font-size: 24px; font-weight: 900;">"¥ 11298.00"</span>
                            </div>
                            <div style="font-size: 11px; color: #999; margin-top: 3px;">"已优惠 ¥0.00 · 不含运费"</div>
                        </div>

                        <a
                            href="/checkout"
                            class="flex align-center justify-center"
                            style="background: #ff3838; color: #fff; width: 160px; height: 48px; border-radius: 4px; font-size: 16px; font-weight: bold; text-decoration: none; box-shadow: 0 4px 12px rgba(255, 56, 56, 0.35);"
                        >
                            "去结算"
                        </a>
                    </div>
                </div>
            </div>
        </div>
    }
}
