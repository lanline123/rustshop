use leptos::prelude::*;

#[component]
pub fn ProductDetailPage() -> impl IntoView {
    view! {
        <div class="product-detail-page" style="background: #f6f6f6; padding: 20px 0 50px 0;">
            <div class="container" style="max-width: 1200px; margin: 0 auto;">
                // 面包屑
                <div class="breadcrumb" style="font-size: 13px; color: #666; margin-bottom: 15px;">
                    <a href="/" style="color: #666; text-decoration: none;">"首页"</a>
                    <span style="margin: 0 8px;">">"</span>
                    <a href="/search?keyword=手机" style="color: #666; text-decoration: none;">"手机通讯"</a>
                    <span style="margin: 0 8px;">">"</span>
                    <span style="color: #333; font-weight: bold;">"Apple iPhone 16 Pro Max 256GB"</span>
                </div>

                // 商品主面板 (左图右文)
                <div class="detail-main flex" style="background: #fff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.03); gap: 40px;">
                    // 左侧大图相册
                    <div class="gallery" style="width: 440px;">
                        // 主图
                        <div class="big-pic flex justify-center align-center" style="width: 440px; height: 440px; border: 1px solid #eee; border-radius: 6px; overflow: hidden; background: #fafafa;">
                            <img src="/static/images/goods_empty.png" alt="主图" style="max-width: 90%; max-height: 90%; object-fit: contain;"/>
                        </div>
                        // 缩略图列表
                        <div class="thumbs flex" style="gap: 12px; margin-top: 15px;">
                            <div style="width: 68px; height: 68px; border: 2px solid #ff3838; border-radius: 4px; padding: 2px; cursor: pointer;">
                                <img src="/static/images/goods_empty.png" style="width: 100%; height: 100%; object-fit: contain;"/>
                            </div>
                            <div style="width: 68px; height: 68px; border: 1px solid #ddd; border-radius: 4px; padding: 2px; cursor: pointer;">
                                <img src="/static/images/goods_empty.png" style="width: 100%; height: 100%; object-fit: contain;"/>
                            </div>
                            <div style="width: 68px; height: 68px; border: 1px solid #ddd; border-radius: 4px; padding: 2px; cursor: pointer;">
                                <img src="/static/images/goods_empty.png" style="width: 100%; height: 100%; object-fit: contain;"/>
                            </div>
                        </div>
                    </div>

                    // 右侧购买决策信息
                    <div class="info-panel flex-1" style="flex: 1;">
                        <h1 style="font-size: 20px; font-weight: bold; color: #222; line-height: 1.4; margin: 0 0 10px 0;">
                            "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G全网通双卡双待手机"
                        </h1>
                        <p style="color: #ff3838; font-size: 13px; margin: 0 0 15px 0;">
                            "【现货特惠】搭载 A18 Pro 强劲芯片，超长续航，4800万融合式主摄！"
                        </p>

                        // 价格面板
                        <div class="price-banner" style="background: #fdf5f5; border-radius: 6px; padding: 15px 20px; margin-bottom: 20px;">
                            <div class="flex align-baseline" style="gap: 10px;">
                                <span style="color: #666; font-size: 13px;">"活动价："</span>
                                <span style="color: #ff3838; font-size: 16px; font-weight: bold;">"¥"</span>
                                <span style="color: #ff3838; font-size: 32px; font-weight: 900; line-height: 1;">"8999.00"</span>
                                <span style="color: #aaa; font-size: 13px; text-decoration: line-through; margin-left: 10px;">"专柜价 ¥9999.00"</span>
                            </div>
                            <div class="flex align-center" style="margin-top: 10px; font-size: 12px; gap: 15px;">
                                <span style="color: #ff3838; border: 1px solid #ff3838; background: #fff; padding: 1px 6px; border-radius: 2px;">"满减特惠"</span>
                                <span style="color: #666;">"每满300元减50元，可跨店凑单"</span>
                            </div>
                        </div>

                        // 规格属性选择 (SKU Spec)
                        <div class="specs" style="display: flex; flex-direction: column; gap: 18px; margin-bottom: 25px; font-size: 13px;">
                            // 颜色版本
                            <div class="flex align-center">
                                <span style="width: 80px; color: #888;">"机身颜色："</span>
                                <div class="flex" style="gap: 10px; flex-wrap: wrap;">
                                    <button style="border: 2px solid #ff3838; background: #fff; padding: 6px 16px; border-radius: 4px; font-size: 13px; cursor: pointer; color: #ff3838; font-weight: bold;">"原色钛金属"</button>
                                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 16px; border-radius: 4px; font-size: 13px; cursor: pointer; color: #444;">"黑色钛金属"</button>
                                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 16px; border-radius: 4px; font-size: 13px; cursor: pointer; color: #444;">"白色钛金属"</button>
                                </div>
                            </div>
                            // 存储容量
                            <div class="flex align-center">
                                <span style="width: 80px; color: #888;">"存储容量："</span>
                                <div class="flex" style="gap: 10px; flex-wrap: wrap;">
                                    <button style="border: 2px solid #ff3838; background: #fff; padding: 6px 16px; border-radius: 4px; font-size: 13px; cursor: pointer; color: #ff3838; font-weight: bold;">"256GB"</button>
                                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 16px; border-radius: 4px; font-size: 13px; cursor: pointer; color: #444;">"512GB"</button>
                                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 16px; border-radius: 4px; font-size: 13px; cursor: pointer; color: #444;">"1TB"</button>
                                </div>
                            </div>
                            // 购买数量
                            <div class="flex align-center">
                                <span style="width: 80px; color: #888;">"购买数量："</span>
                                <div class="flex align-center" style="border: 1px solid #ddd; border-radius: 4px; overflow: hidden; width: 110px;">
                                    <button style="width: 32px; height: 32px; border: none; background: #f7f7f7; cursor: pointer; font-size: 14px;">"-"</button>
                                    <input type="text" value="1" style="width: 46px; height: 32px; border: none; text-align: center; font-size: 14px; outline: none;"/>
                                    <button style="width: 32px; height: 32px; border: none; background: #f7f7f7; cursor: pointer; font-size: 14px;">"+"</button>
                                </div>
                                <span style="margin-left: 15px; color: #999; font-size: 12px;">"库存充裕（现货速发）"</span>
                            </div>
                        </div>

                        // 购买行动按钮
                        <div class="action-buttons flex" style="gap: 15px; margin-bottom: 25px;">
                            <a
                                href="/checkout"
                                class="flex justify-center align-center"
                                style="background: #ff3838; color: #fff; width: 180px; height: 48px; border-radius: 4px; font-size: 16px; font-weight: bold; text-decoration: none; box-shadow: 0 4px 12px rgba(255, 56, 56, 0.3);"
                            >
                                "立即购买"
                            </a>
                            <a
                                href="/cart"
                                class="flex justify-center align-center"
                                style="background: #ffeded; color: #ff3838; border: 1px solid #ff3838; width: 180px; height: 48px; border-radius: 4px; font-size: 16px; font-weight: bold; text-decoration: none;"
                            >
                                "加入购物车"
                            </a>
                        </div>

                        // 服务承诺
                        <div class="services flex align-center" style="font-size: 12px; color: #888; gap: 20px; border-top: 1px dashed #eee; padding-top: 15px;">
                            <span>"✓ 顺丰包邮直达"</span>
                            <span>"✓ 7天无理由退换"</span>
                            <span>"✓ 原厂正品保修"</span>
                            <span>"✓ 正规电子发票"</span>
                        </div>
                    </div>
                </div>

                // 下方商品详情图文介绍 Tabs
                <div class="detail-tabs" style="margin-top: 30px; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
                    <div class="tab-header flex" style="background: #fafafa; border-bottom: 1px solid #eee;">
                        <button style="background: #fff; color: #ff3838; font-weight: bold; border: none; border-top: 2px solid #ff3838; padding: 14px 30px; font-size: 15px; cursor: pointer;">"商品详情"</button>
                        <button style="background: transparent; color: #666; border: none; padding: 14px 30px; font-size: 15px; cursor: pointer;">"规格参数"</button>
                        <button style="background: transparent; color: #666; border: none; padding: 14px 30px; font-size: 15px; cursor: pointer;">"包装与售后"</button>
                        <button style="background: transparent; color: #666; border: none; padding: 14px 30px; font-size: 15px; cursor: pointer;">"用户评价 (99%好评)"</button>
                    </div>
                    <div class="tab-body" style="padding: 40px; text-align: center; color: #666; font-size: 14px; line-height: 2;">
                        <h3 style="color: #222; font-size: 22px; margin-bottom: 20px;">"强劲实力，超乎想象。"</h3>
                        <p>"精致的钛金属外观，超瓷晶面板坚固耐用，搭配新一代超视网膜 XDR 显示屏。"</p>
                        <div style="background: #fafafa; padding: 30px; border-radius: 6px; margin: 25px 0;">
                            <img src="/static/images/goods_empty.png" alt="图文详情" style="max-width: 600px; opacity: 0.7;"/>
                        </div>
                        <p>"全栈 Rust 极速响应 · 秒级注水加载"</p>
                    </div>
                </div>
            </div>
        </div>
    }
}
