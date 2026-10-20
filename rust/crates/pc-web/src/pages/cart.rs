use leptos::prelude::*;

#[component]
pub fn CartPage() -> impl IntoView {
    let cart_items = vec![
        (1, 1001, "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G手机", "原色钛金属 / 256GB", "8999.00", 1, "/static/images/goods_empty.png", true),
        (2, 1005, "Sony WH-1000XM5 头戴式无线蓝牙降噪耳机", "黑色 / 官方标配", "2299.00", 1, "/static/images/goods_empty.png", true),
    ];

    view! {
        <div class="cart-page" style="background: #f2f3f5; min-height: 80vh; padding: 25px 0 60px;">
            <div class="container">
                // 页面标题
                <div style="display: flex; align-items: baseline; gap: 12px; margin-bottom: 20px;">
                    <h2 style="font-size: 24px; font-weight: bold; color: #333; margin: 0;">"我的购物车"</h2>
                    <span style="font-size: 13px; color: #999;">"温馨提示：商品是否购买成功以最终下单为准，请尽快结算"</span>
                </div>

                // 购物车表格
                <div class="cart-table" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; overflow: hidden;">
                    // 表头
                    <div style="display: grid; grid-template-columns: 80px 1fr 140px 140px 140px 100px; background: #fafafa; border-bottom: 1px solid #eee; padding: 14px 20px; font-size: 13px; font-weight: bold; color: #666; text-align: center;">
                        <div style="text-align: left;">
                            <label style="cursor: pointer; display: flex; align-items: center; gap: 6px;">
                                <input type="checkbox" checked=true style="accent-color: var(--general); cursor: pointer;" />
                                <span>"全选"</span>
                            </label>
                        </div>
                        <div style="text-align: left; padding-left: 20px;">"商品信息"</div>
                        <div>"单价"</div>
                        <div>"数量"</div>
                        <div>"小计"</div>
                        <div>"操作"</div>
                    </div>

                    // 商品列表
                    <div>
                        {
                            cart_items.into_iter().map(|(_item_id, prod_id, name, spec, price, num, pic, checked)| {
                                view! {
                                    <div style="display: grid; grid-template-columns: 80px 1fr 140px 140px 140px 100px; align-items: center; padding: 20px; border-bottom: 1px solid #f2f2f2; text-align: center; font-size: 13px;">
                                        <div style="text-align: left;">
                                            <input type="checkbox" checked=checked style="accent-color: var(--general); cursor: pointer;" />
                                        </div>
                                        <div style="display: flex; gap: 16px; text-align: left; align-items: center;">
                                            <a href=format!("/product/{}", prod_id)>
                                                <img src=pic alt=name style="width: 72px; height: 72px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;" />
                                            </a>
                                            <div>
                                                <a href=format!("/product/{}", prod_id) style="font-weight: 500; color: #333; line-height: 20px; display: block; margin-bottom: 6px;">
                                                    {name}
                                                </a>
                                                <span style="font-size: 12px; color: #999; background: #f7f7f7; padding: 2px 8px; border-radius: 2px;">
                                                    {spec}
                                                </span>
                                            </div>
                                        </div>
                                        <div style="font-size: 14px; font-weight: bold; color: #333;">
                                            "¥"{price}
                                        </div>
                                        <div style="display: flex; justify-content: center;">
                                            <div style="display: flex; border: 1px solid #ddd; border-radius: 3px; overflow: hidden; width: fit-content;">
                                                <button style="border: none; background: #f7f7f7; width: 28px; height: 28px; cursor: pointer; font-size: 14px;">"-"</button>
                                                <input type="text" value=num style="width: 36px; height: 28px; border: none; border-left: 1px solid #ddd; border-right: 1px solid #ddd; text-align: center; font-size: 13px; outline: none;" />
                                                <button style="border: none; background: #f7f7f7; width: 28px; height: 28px; cursor: pointer; font-size: 14px;">"+"</button>
                                            </div>
                                        </div>
                                        <div style="color: var(--price); font-weight: bold; font-size: 15px;">
                                            "¥"{price}
                                        </div>
                                        <div>
                                            <button style="border: none; background: transparent; color: #999; cursor: pointer; font-size: 12px;">
                                                "删除"
                                            </button>
                                        </div>
                                    </div>
                                }
                            }).collect::<Vec<_>>()
                        }
                    </div>

                    // 结算吸底条
                    <div style="display: flex; justify-content: space-between; align-items: center; padding: 15px 25px; background: #fcfcfc;">
                        <div style="display: flex; gap: 20px; font-size: 13px; color: #666;">
                            <label style="cursor: pointer; display: flex; align-items: center; gap: 6px;">
                                <input type="checkbox" checked=true style="accent-color: var(--general); cursor: pointer;" />
                                <span>"全选 (2件)"</span>
                            </label>
                            <a href="#" style="color: #666;">"删除选中商品"</a>
                            <a href="#" style="color: #666;">"清理失效宝贝"</a>
                        </div>
                        <div style="display: flex; align-items: center; gap: 24px;">
                            <div style="text-align: right;">
                                <div style="font-size: 13px; color: #666;">
                                    "已选商品 "
                                    <span style="color: var(--general); font-weight: bold; font-size: 16px;">"2"</span>
                                    " 件，合计（不含运费）："
                                    <span style="color: var(--price); font-size: 24px; font-weight: bold; font-family: Arial;">"¥11,298.00"</span>
                                </div>
                                <div style="font-size: 12px; color: #999; margin-top: 2px;">
                                    "已享受优惠：-¥0.00"
                                </div>
                            </div>
                            <a
                                href="/checkout"
                                style="background: var(--general); color: #fff; font-size: 16px; font-weight: bold; padding: 12px 38px; border-radius: 4px; text-decoration: none; cursor: pointer; box-shadow: 0 4px 12px rgba(255, 68, 68, 0.3);"
                            >
                                "去结算"
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}
