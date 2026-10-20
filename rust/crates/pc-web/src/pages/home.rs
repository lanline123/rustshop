use leptos::prelude::*;
use crate::components::product_card::ProductCard;

#[component]
pub fn HomePage() -> impl IntoView {
    // 模拟常用分类
    let categories = vec![
        ("手机 / 数码 / 充值", vec!["智能手机", "数码相机", "平板电脑", "智能手表"]),
        ("电脑 / 办公 / 硬件", vec!["轻薄本", "游戏本", "显示器", "机械键盘"]),
        ("家用电器 / 电视", vec!["冰箱", "洗衣机", "空调", "智能扫地机"]),
        ("美妆个护 / 宠物", vec!["面部护肤", "彩妆香氛", "洗发护发", "宠物食品"]),
        ("食品生鲜 / 熟食", vec!["休闲零食", "坚果炒货", "新鲜水果", "精品粮油"]),
        ("服饰内衣 / 鞋靴", vec!["潮流男装", "时尚女装", "运动鞋", "羽绒服"]),
        ("家居家装 / 厨具", vec!["实木家具", "厨房电器", "床上用品", "灯饰照明"]),
        ("运动户外 / 健身", vec!["跑步机", "动感单车", "户外装备", "钓具装备"]),
    ];

    // 精选推荐商品数据
    let recommend_products = vec![
        (1001, "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G手机", "8999.00", "9999.00", "/static/images/goods_empty.png", 2850, vec!["自营", "热卖", "分期免息"]),
        (1002, "联想 ThinkPad X1 Carbon 14英寸英特尔酷睿Ultra7 商务轻薄笔记本电脑", "11999.00", "13999.00", "/static/images/goods_empty.png", 1420, vec!["企业采购", "限时特惠"]),
        (1003, "华为 Mate 60 Pro 12GB+512GB 雅川青 卫星通话 超可靠玄武架构", "6999.00", "6999.00", "/static/images/goods_empty.png", 5630, vec!["自营", "现货即发"]),
        (1004, "戴森 (Dyson) V12 Detect Slim 智能无绳手持吸尘器 除螨吸尘一体", "3499.00", "4199.00", "/static/images/goods_empty.png", 980, vec!["正品保证", "赠配件包"]),
        (1005, "Sony WH-1000XM5 无线头戴式主动降噪耳机 铂金银", "2299.00", "2799.00", "/static/images/goods_empty.png", 1890, vec!["秒杀", "Hi-Res"]),
        (1006, "三只松鼠 坚果大礼包 每日坚果送礼尊享版 1428g/箱", "128.00", "168.00", "/static/images/goods_empty.png", 12500, vec!["满减", "送礼佳选"]),
        (1007, "飞利浦 (Philips) 钻石亮白智能声波电动牙刷 送旅行盒+4刷头", "799.00", "999.00", "/static/images/goods_empty.png", 3420, vec!["热销爆款"]),
        (1008, "科沃斯 (ECOVACS) 地宝 T30 PRO 扫拖一体洗地全自动基站清洁机器人", "3299.00", "3999.00", "/static/images/goods_empty.png", 870, vec!["新品上市", "保价618"]),
    ];

    view! {
        <div class="home-page" style="background: #f6f6f6; padding-bottom: 40px;">
            // 1. 首屏 Banner 与 分类导航区
            <div class="hero-section" style="background: #fff; margin-bottom: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
                <div class="container flex" style="max-width: 1200px; margin: 0 auto; min-height: 460px;">
                    // 左侧分类导航
                    <div class="category-menu" style="width: 200px; background: #2b2e33; color: #fff; padding: 10px 0; box-sizing: border-box;">
                        {
                            categories.into_iter().map(|(main_cat, subs)| {
                                view! {
                                    <div
                                        class="cat-item"
                                        style="padding: 10px 15px; cursor: pointer; transition: background 0.2s;"
                                        onmouseover="this.style.background='#ff3838';"
                                        onmouseout="this.style.background='transparent';"
                                    >
                                        <div style="font-size: 13px; font-weight: bold;">{main_cat}</div>
                                        <div style="font-size: 11px; color: #bbb; margin-top: 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                            {subs.join(" / ")}
                                        </div>
                                    </div>
                                }
                            }).collect::<Vec<_>>()
                        }
                    </div>

                    // 中间主轮播大图 (Hero Banner)
                    <div class="hero-banner" style="flex: 1; position: relative; overflow: hidden; background: linear-gradient(135deg, #fceceb 0%, #fae1dc 100%); display: flex; align-items: center; justify-content: center;">
                        <div style="text-align: center; padding: 40px;">
                            <div style="color: #ff3838; font-size: 14px; font-weight: bold; letter-spacing: 2px;">"2026 全球超级品牌盛典 · 全球甄选"</div>
                            <h1 style="font-size: 38px; color: #222; margin: 15px 0 10px 0; font-weight: 900;">"正品现货 · 闪电速达 · 跨店每满300减50"</h1>
                            <p style="font-size: 16px; color: #666; max-width: 500px; margin: 0 auto 25px auto;">
                                "大牌数码、智能家电、生鲜零食一站式购齐，全场支持企业集中批发与阳光采购。"
                            </p>
                            <a
                                href="/search"
                                style="background: #ff3838; color: #fff; padding: 12px 35px; border-radius: 25px; font-size: 16px; font-weight: bold; text-decoration: none; display: inline-block; box-shadow: 0 6px 16px rgba(255, 56, 56, 0.35);"
                            >
                                "立即探索热卖榜单"
                            </a>
                        </div>
                    </div>

                    // 右侧个人快捷中心与商城头条
                    <div class="user-quick-panel" style="width: 250px; background: #fff; border-left: 1px solid #f0f0f0; padding: 25px 20px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between;">
                        // 用户头像与登录状态
                        <div style="text-align: center;">
                            <div style="width: 64px; height: 64px; border-radius: 50%; background: #ffebeb; margin: 0 auto; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #ff3838; font-weight: bold;">
                                "Hi"
                            </div>
                            <div style="font-size: 15px; font-weight: bold; margin-top: 12px; color: #333;">"Hi，欢迎来到 TigShop"</div>
                            <div class="flex justify-center" style="gap: 10px; margin-top: 15px;">
                                <a href="/login" style="background: #ff3838; color: #fff; padding: 6px 18px; border-radius: 15px; font-size: 12px; text-decoration: none; font-weight: bold;">"登录"</a>
                                <a href="/login" style="background: #f5f5f5; color: #333; padding: 6px 18px; border-radius: 15px; font-size: 12px; text-decoration: none;">"注册"</a>
                            </div>
                        </div>

                        // 新人福利
                        <div style="background: #fff8f8; border: 1px dashed #ffb4b4; border-radius: 6px; padding: 12px; text-align: center; margin: 20px 0;">
                            <div style="color: #ff3838; font-weight: bold; font-size: 13px;">"新人专享 ¥188 礼包"</div>
                            <div style="font-size: 11px; color: #888; margin-top: 4px;">"注册即领，全场通用券"</div>
                        </div>

                        // 商城公告
                        <div>
                            <div class="flex justify-between" style="font-size: 13px; font-weight: bold; color: #333; margin-bottom: 10px;">
                                <span>"商城公告"</span>
                                <a href="#" style="font-size: 11px; color: #999; text-decoration: none;">"更多 >"</a>
                            </div>
                            <ul style="list-style: none; padding: 0; margin: 0; font-size: 12px; color: #666; display: flex; flex-direction: column; gap: 8px;">
                                <li style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">"· 关于2026年大促物流配送保障公告"</li>
                                <li style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">"· 企业采购专属发票与账期系统上线"</li>
                                <li style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">"· 防范网络电信网络诈骗安全温馨提示"</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>

            // 2. 秒杀专区楼层 (Seckill Floor)
            <div class="seckill-floor" style="max-width: 1200px; margin: 0 auto 30px auto; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.03);">
                // 秒杀头部
                <div class="flex justify-between align-center" style="background: linear-gradient(90deg, #ff3838 0%, #ff6b6b 100%); color: #fff; padding: 15px 25px;">
                    <div class="flex align-center" style="gap: 15px;">
                        <span style="font-size: 20px; font-weight: 900; letter-spacing: 1px;">"⚡ 限时秒杀 · FLASH SALE"</span>
                        <span style="font-size: 13px; background: rgba(0,0,0,0.2); padding: 3px 10px; border-radius: 12px;">"每日四场，正品低价"</span>
                    </div>
                    <div class="flex align-center" style="gap: 10px; font-size: 13px;">
                        <span>"本场距结束还剩："</span>
                        <span style="background: #222; padding: 3px 6px; border-radius: 4px; font-weight: bold; font-family: monospace;">"02"</span>":"
                        <span style="background: #222; padding: 3px 6px; border-radius: 4px; font-weight: bold; font-family: monospace;">"45"</span>":"
                        <span style="background: #222; padding: 3px 6px; border-radius: 4px; font-weight: bold; font-family: monospace;">"18"</span>
                    </div>
                </div>
                // 秒杀商品卡片行
                <div class="grid" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; padding: 20px;">
                    {
                        recommend_products.iter().take(4).map(|(id, name, price, market, pic, sales, item_tags)| {
                            view! {
                                <ProductCard
                                    id=*id
                                    name=(*name).to_string()
                                    price=(*price).to_string()
                                    market_price=(*market).to_string()
                                    pic_thumb=(*pic).to_string()
                                    sales=*sales
                                    badges={item_tags.iter().map(|t| (*t).to_string()).collect::<Vec<_>>()}
                                />
                            }
                        }).collect::<Vec<_>>()
                    }
                </div>
            </div>

            // 3. 精选好物推荐楼层 (Recommend Goods Grid)
            <div class="recommend-floor" style="max-width: 1200px; margin: 0 auto;">
                <div class="flex justify-between align-center" style="margin-bottom: 20px;">
                    <div style="font-size: 22px; font-weight: 900; color: #222; display: flex; align-items: center; gap: 10px;">
                        <span style="width: 4px; height: 22px; background: #ff3838; border-radius: 2px;"></span>
                        <span>"精选好物 · 猜你喜欢"</span>
                    </div>
                    <div class="flex" style="gap: 20px; font-size: 14px;">
                        <a href="#" style="color: #ff3838; font-weight: bold; text-decoration: none;">"热门推荐"</a>
                        <a href="#" style="color: #666; text-decoration: none;">"新品首发"</a>
                        <a href="#" style="color: #666; text-decoration: none;">"企业大宗"</a>
                        <a href="#" style="color: #666; text-decoration: none;">"品牌特卖"</a>
                    </div>
                </div>

                // 推荐商品瀑布流/网格 (8张卡片)
                <div class="grid" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px;">
                    {
                        recommend_products.into_iter().map(|(id, name, price, market, pic, sales, item_tags)| {
                            view! {
                                <ProductCard
                                    id=id
                                    name=name.to_string()
                                    price=price.to_string()
                                    market_price=market.to_string()
                                    pic_thumb=pic.to_string()
                                    sales=sales
                                    badges={item_tags.into_iter().map(|t| t.to_string()).collect::<Vec<_>>()}
                                />
                            }
                        }).collect::<Vec<_>>()
                    }
                </div>
            </div>
        </div>
    }
}
