use leptos::prelude::*;
use crate::components::product_card::ProductCard;

#[component]
pub fn HomePage() -> impl IntoView {
    // 首页商品数据
    let recommend_products = vec![
        (1001, "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G手机", "8999.00", "9999.00", "/static/images/goods_empty.png", 2850, vec!["自营", "特惠", "免息"]),
        (1002, "联想 ThinkPad X1 Carbon 14英寸英特尔酷睿Ultra7 商务笔记本", "11999.00", "13999.00", "/static/images/goods_empty.png", 1420, vec!["企业采购", "限时特惠"]),
        (1003, "华为 Mate 60 Pro 12GB+512GB 雅丹黑 全网通 卫星通话", "6999.00", "6999.00", "/static/images/goods_empty.png", 5630, vec!["自营", "热销"]),
        (1004, "戴森 (Dyson) V12 Detect Slim 轻量手持无线吸尘器", "3499.00", "4199.00", "/static/images/goods_empty.png", 980, vec!["正品保证", "保价"]),
        (1005, "Sony WH-1000XM5 头戴式无线蓝牙降噪耳机 黑色", "2299.00", "2799.00", "/static/images/goods_empty.png", 1890, vec!["热卖榜", "Hi-Res"]),
        (1006, "三只松鼠 坚果礼盒 每日坚果 1428g/箱 休闲零食", "128.00", "168.00", "/static/images/goods_empty.png", 12500, vec!["包邮", "精选"]),
        (1007, "飞利浦 (Philips) 钻石牙刷 礼盒装 声波震动 粉色", "799.00", "999.00", "/static/images/goods_empty.png", 3420, vec!["热卖"]),
        (1008, "科沃斯 (ECOVACS) 地宝 T30 PRO 扫地机器人", "3299.00", "3999.00", "/static/images/goods_empty.png", 870, vec!["新品", "满减"]),
    ];

    let seckill_products = vec![
        (1001, "iPhone 16 Pro Max", "7999.00", "9999.00", "/static/images/goods_empty.png"),
        (1005, "Sony WH-1000XM5", "1999.00", "2799.00", "/static/images/goods_empty.png"),
        (1004, "戴森无线吸尘器 V12", "2999.00", "4199.00", "/static/images/goods_empty.png"),
        (1006, "每日坚果大礼盒", "99.00", "168.00", "/static/images/goods_empty.png"),
        (1007, "飞利浦声波震动牙刷", "599.00", "999.00", "/static/images/goods_empty.png"),
    ];

    let categories = vec![
        ("手机数码", "手机 / 数码 / 摄影"),
        ("电脑办公", "笔记本 / 平板 / 台式机"),
        ("家用电器", "电视 / 空调 / 洗衣机"),
        ("服饰鞋包", "男装 / 女装 / 箱包"),
        ("美妆个护", "护肤 / 彩妆 / 香水"),
        ("食品生鲜", "生鲜 / 零食 / 茗茶"),
        ("母婴玩具", "奶粉 / 纸尿裤 / 玩具"),
        ("家居生活", "家纺 / 厨具 / 家具"),
        ("运动户外", "运动鞋 / 健身器材"),
        ("企业采购", "礼品 / 办公耗材 / 定制"),
    ];

    view! {
        <div class="home-page" style="background: #f2f3f5; padding-bottom: 50px;">
            // 1. 焦点 Banner 与首页三栏布局 (Banner Section)
            <div class="banner container" style="margin-top: 10px;">
                <div class="inner" style="display: flex; height: 470px;">
                    // 左侧: 全部商品分类 (AllCate 190px)
                    <div class="left" style="width: 190px; height: 470px; background: #fff; box-shadow: 0 0 8px rgba(0,0,0,0.04); box-sizing: border-box; overflow: hidden;">
                        <div class="cate_menu" style="padding: 10px 0; height: 100%; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-around;">
                            {
                                categories.into_iter().map(|(name, sub)| {
                                    view! {
                                        <div class="cate_menu_item" style="padding: 0 16px; height: 38px; line-height: 38px; display: flex; align-items: center; justify-content: space-between; cursor: pointer; transition: background-color 0.2s ease;">
                                            <a href="/search" class="cate_menu_lk" style="font-size: 13px; color: #333; font-weight: 500;">
                                                {name}
                                            </a>
                                            <span style="font-size: 11px; color: #999; max-width: 90px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{sub}</span>
                                        </div>
                                    }
                                }).collect::<Vec<_>>()
                            }
                        </div>
                    </div>

                    // 中间: 轮播主图与推荐位 (Middle 790px: 590px + 190px)
                    <div class="middle" style="width: 790px; height: 470px; margin: 0 10px; display: flex; gap: 10px;">
                        // 主轮播图 (590px)
                        <div style="width: 590px; height: 470px; border-radius: 4px; overflow: hidden; position: relative; background: #2c3e50;">
                            <div style="width: 100%; height: 100%; background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); display: flex; flex-direction: column; justify-content: center; padding: 50px; box-sizing: border-box; color: #fff;">
                                <span style="background: rgba(255,255,255,0.2); padding: 4px 12px; border-radius: 20px; font-size: 13px; width: fit-content; margin-bottom: 16px;">
                                    "2026 年度重磅首发"
                                </span>
                                <h1 style="font-size: 38px; font-weight: 800; margin: 0 0 12px 0; letter-spacing: 1px;">
                                    "超凡科技 · 巅峰体验"
                                </h1>
                                <p style="font-size: 16px; opacity: 0.9; margin: 0 0 28px 0; line-height: 24px;">
                                    "全场大牌新品限时直降，专享最高 24 期免息"
                                </p>
                                <a
                                    href="/search"
                                    style="background: #fff; color: var(--general); font-weight: bold; padding: 12px 28px; border-radius: 25px; text-decoration: none; width: fit-content; font-size: 15px; box-shadow: 0 4px 14px rgba(0,0,0,0.2);"
                                >
                                    "立即探索 >"
                                </a>
                            </div>
                            // 轮播圆点
                            <div style="position: absolute; bottom: 15px; width: 100%; display: flex; justify-content: center; gap: 8px;">
                                <span style="width: 20px; height: 6px; background: #fff; border-radius: 3px;"></span>
                                <span style="width: 6px; height: 6px; background: rgba(255,255,255,0.5); border-radius: 50%;"></span>
                                <span style="width: 6px; height: 6px; background: rgba(255,255,255,0.5); border-radius: 50%;"></span>
                            </div>
                        </div>

                        // 右侧双图推荐 (190px)
                        <div style="width: 190px; height: 470px; display: flex; flex-direction: column; gap: 10px;">
                            <div style="height: 230px; background: #fff; border-radius: 4px; padding: 16px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between; box-shadow: 0 0 6px rgba(0,0,0,0.04);">
                                <div>
                                    <span style="font-size: 11px; background: #fee; color: var(--general); padding: 2px 6px; border-radius: 2px;">"大牌特卖"</span>
                                    <div style="font-size: 15px; font-weight: bold; color: #333; margin-top: 6px;">"数码狂欢季"</div>
                                    <div style="font-size: 12px; color: #999; margin-top: 2px;">"满 2000 减 300"</div>
                                </div>
                                <img src="/static/images/goods_empty.png" alt="数码" style="width: 100px; height: 100px; align-self: center; object-fit: contain;"/>
                            </div>
                            <div style="height: 230px; background: #fff; border-radius: 4px; padding: 16px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between; box-shadow: 0 0 6px rgba(0,0,0,0.04);">
                                <div>
                                    <span style="font-size: 11px; background: #e6f7ff; color: #1890ff; padding: 2px 6px; border-radius: 2px;">"居家精选"</span>
                                    <div style="font-size: 15px; font-weight: bold; color: #333; margin-top: 6px;">"智能生活馆"</div>
                                    <div style="font-size: 12px; color: #999; margin-top: 2px;">"爆款直降 5 折起"</div>
                                </div>
                                <img src="/static/images/goods_empty.png" alt="家居" style="width: 100px; height: 100px; align-self: center; object-fit: contain;"/>
                            </div>
                        </div>
                    </div>

                    // 右侧: 会员小中心卡片 (SmallPersonalCenter 190px)
                    <div class="right small-personal_center" style="width: 190px; height: 470px; background: #fff; border-radius: 4px; box-shadow: 0 0 8px rgba(0,0,0,0.04); padding: 20px 14px; box-sizing: border-box; text-align: center; display: flex; flex-direction: column; justify-content: space-between;">
                        <div class="uphoto-wrap">
                            <div style="width: 60px; height: 60px; border-radius: 50%; background: #f0f0f0; margin: 0 auto; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                <span class="iconfont-pc icon-wode" style="font-size: 32px; color: #bbb;"></span>
                            </div>
                            <div class="login-box" style="margin-top: 12px;">
                                <div style="font-size: 14px; font-weight: bold; color: #333;">"Hi，您好！"</div>
                                <div style="font-size: 12px; color: #999; margin-top: 4px;">"欢迎来到 TigShop 商城"</div>
                                <div style="margin-top: 14px; display: flex; gap: 8px; justify-content: center;">
                                    <a href="/login" class="login_btn" style="background: var(--general); color: #fff; padding: 5px 16px; border-radius: 15px; font-size: 12px; font-weight: bold;">"登录"</a>
                                    <a href="/login" class="login_btn" style="background: #fff; color: #333; border: 1px solid #ddd; padding: 5px 16px; border-radius: 15px; font-size: 12px;">"注册"</a>
                                </div>
                            </div>
                        </div>

                        // 会员特权与快速通道
                        <div class="vip-list" style="border-top: 1px solid #f2f2f2; border-bottom: 1px solid #f2f2f2; padding: 12px 0; display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; font-size: 11px; color: #666;">
                            <div style="display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer;">
                                <span class="iconfont-pc icon-youhuiquan" style="font-size: 20px; color: var(--general);"></span>
                                <span>"领券"</span>
                            </div>
                            <div style="display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer;">
                                <span class="iconfont-pc icon-dingdan" style="font-size: 20px; color: #1890ff;"></span>
                                <span>"订单"</span>
                            </div>
                            <div style="display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer;">
                                <span class="iconfont-pc icon-kefu" style="font-size: 20px; color: #52c41a;"></span>
                                <span>"客服"</span>
                            </div>
                        </div>

                        // 商城公告
                        <div class="notice-list" style="text-align: left; font-size: 12px; line-height: 22px;">
                            <div style="font-weight: bold; color: #333; margin-bottom: 4px; font-size: 12px;">"商城头条"</div>
                            <div style="color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                <span style="color: var(--general); font-weight: bold;">"【通知】"</span>"商城服务升级公告"
                            </div>
                            <div style="color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                <span style="color: var(--general); font-weight: bold;">"【特惠】"</span>"全场满 300 减 50 优惠券"
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            // 2. 限时秒杀专区楼层 (HomeSeckill 1:1)
            <div class="container" style="margin-top: 25px;">
                <div class="mod_seckill" style="height: 260px; display: flex; background: #fff; border-radius: 4px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
                    // 红色秒杀倒计时侧栏 (190px)
                    <div class="count_down" style="width: 190px; height: 260px; background-color: #e83632; background-image: url('/assets/images/index/qg-bg.png'); background-size: contain; background-repeat: no-repeat; color: #fff; text-align: center; padding: 25px 15px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <div class="count_down_tips" style="font-size: 28px; font-weight: bold; letter-spacing: 2px;">"限时秒杀"</div>
                            <div style="font-size: 12px; opacity: 0.85; margin-top: 4px;">"FLASH DEALS"</div>
                            <div style="font-size: 13px; margin-top: 30px; font-weight: 500;">"本场距离结束还剩"</div>
                            <div class="count_time" style="display: flex; justify-content: center; align-items: center; gap: 4px; margin-top: 8px;">
                                <em style="background: #2f2f2f; color: #fff; font-size: 15px; font-weight: bold; padding: 4px 6px; border-radius: 3px; font-style: normal;">"02"</em>
                                <span style="color: #fff; font-weight: bold;">":"</span>
                                <em style="background: #2f2f2f; color: #fff; font-size: 15px; font-weight: bold; padding: 4px 6px; border-radius: 3px; font-style: normal;">"45"</em>
                                <span style="color: #fff; font-weight: bold;">":"</span>
                                <em style="background: #2f2f2f; color: #fff; font-size: 15px; font-weight: bold; padding: 4px 6px; border-radius: 3px; font-style: normal;">"18"</em>
                            </div>
                        </div>
                        <div class="more-seckill">
                            <a href="/search" style="color: #fff; border: 1px solid rgba(255,255,255,0.7); border-radius: 20px; padding: 5px 0; font-size: 12px; text-decoration: none; display: block;">"查看更多 >"</a>
                        </div>
                    </div>

                    // 秒杀商品卡片清单 (5个等分)
                    <div class="seckill_list_wrap" style="flex: 1; display: grid; grid-template-columns: repeat(5, 1fr); background: #fff;">
                        {
                            seckill_products.into_iter().map(|(id, name, price, market, pic)| {
                                view! {
                                    <div class="proditem" style="padding: 20px 15px; border-right: 1px solid #f2f2f2; display: flex; flex-direction: column; justify-content: space-between; text-align: center; box-sizing: border-box;">
                                        <a href=format!("/product/{}", id) style="display: block; width: 100%; height: 100%; display: flex; flex-direction: column; justify-content: space-between;">
                                            <div class="seckill-item-image" style="width: 130px; height: 130px; margin: 0 auto; display: flex; align-items: center; justify-content: center;">
                                                <img src=pic alt=name style="max-width: 100%; max-height: 100%; object-fit: contain;"/>
                                            </div>
                                            <div class="seckill-item__name" style="font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 10px;">
                                                {name}
                                            </div>
                                            <div class="seckill-item__price" style="margin-top: 8px;">
                                                <div style="color: var(--price); font-size: 18px; font-weight: bold;">
                                                    <span style="font-size: 12px;">"¥"</span>{price}
                                                </div>
                                                <div style="font-size: 12px; color: #bbb; text-decoration: line-through; margin-top: 2px;">
                                                    "¥"{market}
                                                </div>
                                            </div>
                                        </a>
                                    </div>
                                }
                            }).collect::<Vec<_>>()
                        }
                    </div>
                </div>
            </div>

            // 3. 精选好物推荐楼层 (Recommend Floor)
            <div class="container" style="margin-top: 30px;">
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <div style="width: 4px; height: 22px; background: var(--general); border-radius: 2px;"></div>
                        <span style="font-size: 22px; font-weight: bold; color: #333;">"精选好物推荐"</span>
                        <span style="font-size: 13px; color: #999; margin-left: 10px;">"品质保障 · 严选爆款"</span>
                    </div>
                    <a href="/search" style="font-size: 13px; color: #666; text-decoration: none;">"查看全部商品 >"</a>
                </div>

                <div class="grid" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px;">
                    {
                        recommend_products.iter().map(|(id, name, price, market, pic, sales, item_tags)| {
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
        </div>
    }
}
