use leptos::prelude::*;

#[component]
pub fn ProductDetailPage() -> impl IntoView {
    view! {
        <div class="product-detail-page" style="background: #f2f3f5; min-height: 80vh; padding: 20px 0 60px;">
            <div class="container">
                // 面包屑
                <div class="breadcrumb acea-row row-middle" style="font-size: 13px; color: #666; margin-bottom: 16px; gap: 8px;">
                    <a href="/" style="color: #666;">"首页"</a>
                    <span>">"</span>
                    <a href="/search" style="color: #666;">"手机数码"</a>
                    <span>">"</span>
                    <a href="/search" style="color: #666;">"智能手机"</a>
                    <span>">"</span>
                    <span style="color: #333; font-weight: bold;">"Apple iPhone 16 Pro Max 256GB"</span>
                </div>

                // 商品主体卡片 (图集 + 信息)
                <div class="detail-main" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 30px; display: grid; grid-template-columns: 460px 1fr; gap: 40px; margin-bottom: 25px;">
                    // 左侧: 相册主图与缩略图
                    <div class="gallery">
                        <div class="main-image" style="width: 460px; height: 460px; border: 1px solid #f0f0f0; border-radius: 4px; display: flex; align-items: center; justify-content: center; background: #fafafa; overflow: hidden;">
                            <img src="/static/images/goods_empty.png" alt="主图" style="max-width: 90%; max-height: 90%; object-fit: contain;"/>
                        </div>
                        <div class="thumbnails flex" style="margin-top: 15px; gap: 12px; display: flex;">
                            <div style="width: 70px; height: 70px; border: 2px solid var(--general); border-radius: 4px; padding: 2px; cursor: pointer; display: flex; align-items: center; justify-content: center; background: #fff;">
                                <img src="/static/images/goods_empty.png" style="max-width: 100%; max-height: 100%; object-fit: contain;"/>
                            </div>
                            <div style="width: 70px; height: 70px; border: 1px solid #e5e5e5; border-radius: 4px; padding: 2px; cursor: pointer; display: flex; align-items: center; justify-content: center; background: #fff;">
                                <img src="/static/images/goods_empty.png" style="max-width: 100%; max-height: 100%; object-fit: contain;"/>
                            </div>
                            <div style="width: 70px; height: 70px; border: 1px solid #e5e5e5; border-radius: 4px; padding: 2px; cursor: pointer; display: flex; align-items: center; justify-content: center; background: #fff;">
                                <img src="/static/images/goods_empty.png" style="max-width: 100%; max-height: 100%; object-fit: contain;"/>
                            </div>
                        </div>
                    </div>

                    // 右侧: 商品信息与购买表单
                    <div class="info">
                        <h1 style="font-size: 22px; font-weight: bold; color: #222; margin: 0 0 10px 0; line-height: 30px;">
                            "Apple iPhone 16 Pro Max 256GB 原色钛金属 移动联通电信5G全网通手机"
                        </h1>
                        <p style="font-size: 13px; color: var(--general); margin: 0 0 16px 0; line-height: 20px;">
                            "【限时免息特惠】搭载 A18 Pro 仿生芯片，超强续航，4800万像素超广角，支持全焦段拍摄，现货速发！"
                        </p>

                        // 价格栏
                        <div style="background: #fafafa; border-radius: 4px; padding: 18px 20px; margin-bottom: 20px;">
                            <div style="display: flex; align-items: baseline; gap: 10px;">
                                <span style="font-size: 13px; color: #666;">"商城售价："</span>
                                <span style="font-size: 32px; font-weight: bold; color: var(--price); font-family: Arial;">
                                    <span style="font-size: 20px;">"¥"</span>"8999.00"
                                </span>
                                <span style="font-size: 13px; color: #999; text-decoration: line-through; margin-left: 10px;">
                                    "市场参考价：¥9999.00"
                                </span>
                            </div>
                            <div style="margin-top: 10px; font-size: 12px; color: #666; display: flex; gap: 24px;">
                                <span>"累计销量：2,850 件"</span>
                                <span>"用户评价：99% 好评"</span>
                                <span>"库存状态：有货 (北京仓闪电发货)"</span>
                            </div>
                        </div>

                        // 规格属性选择 (SKU)
                        <div style="display: flex; flex-direction: column; gap: 16px; margin-bottom: 25px; font-size: 13px;">
                            <div style="display: flex; align-items: center;">
                                <span style="width: 70px; color: #666;">"选择颜色："</span>
                                <div style="display: flex; gap: 10px;">
                                    <button style="border: 2px solid var(--general); background: #fff; color: var(--general); padding: 7px 18px; border-radius: 3px; cursor: pointer; font-weight: bold;">
                                        "原色钛金属"
                                    </button>
                                    <button style="border: 1px solid #ddd; background: #fff; color: #333; padding: 7px 18px; border-radius: 3px; cursor: pointer;">
                                        "沙漠钛金属"
                                    </button>
                                    <button style="border: 1px solid #ddd; background: #fff; color: #333; padding: 7px 18px; border-radius: 3px; cursor: pointer;">
                                        "白色钛金属"
                                    </button>
                                    <button style="border: 1px solid #ddd; background: #fff; color: #333; padding: 7px 18px; border-radius: 3px; cursor: pointer;">
                                        "黑色钛金属"
                                    </button>
                                </div>
                            </div>

                            <div style="display: flex; align-items: center;">
                                <span style="width: 70px; color: #666;">"存储容量："</span>
                                <div style="display: flex; gap: 10px;">
                                    <button style="border: 2px solid var(--general); background: #fff; color: var(--general); padding: 7px 18px; border-radius: 3px; cursor: pointer; font-weight: bold;">
                                        "256GB"
                                    </button>
                                    <button style="border: 1px solid #ddd; background: #fff; color: #333; padding: 7px 18px; border-radius: 3px; cursor: pointer;">
                                        "512GB"
                                    </button>
                                    <button style="border: 1px solid #ddd; background: #fff; color: #333; padding: 7px 18px; border-radius: 3px; cursor: pointer;">
                                        "1TB"
                                    </button>
                                </div>
                            </div>

                            <div style="display: flex; align-items: center;">
                                <span style="width: 70px; color: #666;">"购买数量："</span>
                                <div style="display: flex; border: 1px solid #ddd; border-radius: 3px; overflow: hidden; width: fit-content;">
                                    <button style="border: none; background: #f7f7f7; width: 32px; height: 32px; cursor: pointer; font-size: 15px;">"-"</button>
                                    <input type="text" value="1" style="width: 44px; height: 32px; border: none; border-left: 1px solid #ddd; border-right: 1px solid #ddd; text-align: center; font-size: 14px; outline: none;" />
                                    <button style="border: none; background: #f7f7f7; width: 32px; height: 32px; cursor: pointer; font-size: 15px;">"+"</button>
                                </div>
                                <span style="margin-left: 12px; color: #999; font-size: 12px;">"件 (库存充足)"</span>
                            </div>
                        </div>

                        // 购买动作按钮
                        <div style="display: flex; gap: 16px; margin-top: 25px;">
                            <a
                                href="/checkout"
                                style="background: var(--general); color: #fff; font-size: 16px; font-weight: bold; padding: 13px 45px; border-radius: 4px; text-decoration: none; cursor: pointer; box-shadow: 0 4px 14px rgba(255, 68, 68, 0.3);"
                            >
                                "立即购买"
                            </a>
                            <a
                                href="/cart"
                                style="background: #fff; color: var(--general); border: 2px solid var(--general); font-size: 16px; font-weight: bold; padding: 11px 40px; border-radius: 4px; text-decoration: none; cursor: pointer; display: flex; align-items: center; gap: 8px;"
                            >
                                <span class="iconfont-pc icon-shiwu-gouwuche" style="font-size: 18px;"></span>
                                "加入购物车"
                            </a>
                        </div>
                    </div>
                </div>

                // 底部详情与参数标签页 (Tabs)
                <div class="detail-tabs" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; overflow: hidden;">
                    <div style="display: flex; border-bottom: 1px solid #eee; background: #fafafa;">
                        <button style="padding: 15px 30px; font-size: 15px; font-weight: bold; color: var(--general); border: none; background: #fff; border-top: 2px solid var(--general); cursor: pointer;">
                            "商品详细介绍"
                        </button>
                        <button style="padding: 15px 30px; font-size: 15px; color: #666; border: none; background: transparent; cursor: pointer;">
                            "规格与包装"
                        </button>
                        <button style="padding: 15px 30px; font-size: 15px; color: #666; border: none; background: transparent; cursor: pointer;">
                            "售后与保障"
                        </button>
                        <button style="padding: 15px 30px; font-size: 15px; color: #666; border: none; background: transparent; cursor: pointer;">
                            "累计评价 (2,850)"
                        </button>
                    </div>

                    <div style="padding: 40px; text-align: center; color: #666; font-size: 14px; line-height: 28px;">
                        <div style="max-width: 800px; margin: 0 auto; text-align: left; margin-bottom: 30px;">
                            <h3 style="font-size: 18px; color: #333; margin-bottom: 15px;">"核心亮点"</h3>
                            <p>"• 强劲 A18 Pro 芯片，采用新一代 3 纳米工艺，带来顶尖性能与极致能效比。"</p>
                            <p>"• 钛金属设计配合超瓷晶面板，强度跃升，边框更窄，带来沉浸式 6.9 英寸超大视界。"</p>
                            <p>"• 4800 万像素双重超广角镜头系统，支持 5 倍光学变焦，专业电影级 4K 120 fps 杜比视界录制。"</p>
                        </div>
                        <img src="/static/images/goods_empty.png" alt="图文详情" style="max-width: 600px; opacity: 0.8;"/>
                    </div>
                </div>
            </div>
        </div>
    }
}
