use leptos::prelude::*;
use crate::components::product_card::ProductCard;
use crate::loader::HomeData;

#[component]
pub fn HomePage() -> impl IntoView {
    let data_opt = use_context::<HomeData>();

    // 默认或数据库分类数据
    let categories = if let Some(ref d) = data_opt {
        d.categories.clone()
    } else {
        vec![
            crate::loader::CategoryItemVO { name: "手机数码".into(), sub: "手机 / 数码 / 摄影".into() },
            crate::loader::CategoryItemVO { name: "电脑办公".into(), sub: "笔记本 / 平板 / 台式机".into() },
            crate::loader::CategoryItemVO { name: "家用电器".into(), sub: "电视 / 空调 / 洗衣机".into() },
            crate::loader::CategoryItemVO { name: "服饰鞋包".into(), sub: "男装 / 女装 / 箱包".into() },
            crate::loader::CategoryItemVO { name: "美妆个护".into(), sub: "护肤 / 彩妆 / 香水".into() },
            crate::loader::CategoryItemVO { name: "食品生鲜".into(), sub: "生鲜 / 零食 / 茗茶".into() },
            crate::loader::CategoryItemVO { name: "母婴玩具".into(), sub: "奶粉 / 纸尿裤 / 玩具".into() },
            crate::loader::CategoryItemVO { name: "家居生活".into(), sub: "家纺 / 厨具 / 家具".into() },
            crate::loader::CategoryItemVO { name: "运动户外".into(), sub: "运动鞋 / 健身器材".into() },
            crate::loader::CategoryItemVO { name: "企业采购".into(), sub: "礼品 / 办公耗材 / 定制".into() },
        ]
    };

    let recommend_products = if let Some(ref d) = data_opt {
        d.recommend_products.clone()
    } else {
        vec![]
    };

    let seckill_products = if let Some(ref d) = data_opt {
        d.seckill_products.clone()
    } else {
        vec![]
    };

    view! {
        <div class="home-page" style="background: #f2f3f5; padding-bottom: 50px;">
            // 1. 焦点 Banner 与首页三栏布局 (Banner Section)
            <div class="banner container" style="margin-top: 10px;">
                <div class="inner" style="display: flex; height: 470px;">
                    // 左侧: 全部商品分类 (AllCate 190px)
                    <div class="left" style="width: 190px; height: 470px; background: #fff; box-shadow: 0 0 8px rgba(0,0,0,0.04); box-sizing: border-box; overflow: hidden;">
                        <div class="cate_menu" style="padding: 10px 0; height: 100%; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-around;">
                            {
                                categories.into_iter().map(|cat| {
                                    view! {
                                        <div class="cate_menu_item" style="padding: 0 16px; height: 38px; line-height: 38px; display: flex; align-items: center; justify-content: space-between; cursor: pointer; transition: background-color 0.2s ease;">
                                            <a href="/search" class="cate_menu_lk" style="font-size: 13px; color: #333; font-weight: 500;">
                                                {cat.name}
                                            </a>
                                            <span style="font-size: 11px; color: #999; max-width: 90px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{cat.sub}</span>
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
                                    "2026 官方旗舰精选"
                                </span>
                                <h1 style="font-size: 38px; font-weight: 800; margin: 0 0 12px 0; letter-spacing: 1px;">
                                    "全栈 Rust 驱动 · 极速响应"
                                </h1>
                                <p style="font-size: 16px; opacity: 0.9; margin: 0 0 28px 0; line-height: 24px;">
                                    "所有商品实时读取自 MySQL 真实数据仓储，享受极致购物体验"
                                </p>
                                <a
                                    href="/search"
                                    style="background: #fff; color: var(--general); font-weight: bold; padding: 12px 28px; border-radius: 25px; text-decoration: none; width: fit-content; font-size: 15px; box-shadow: 0 4px 14px rgba(0,0,0,0.2);"
                                >
                                    "立即探索 >"
                                </a>
                            </div>
                        </div>

                        // 轮播右侧双小推荐图 (190px)
                        <div style="width: 190px; height: 470px; display: flex; flex-direction: column; gap: 10px;">
                            <div style="flex: 1; background: #fff; border-radius: 4px; overflow: hidden; position: relative; border: 1px solid #f0f0f0;">
                                <a href="/search" style="display: block; width: 100%; height: 100%; background: linear-gradient(180deg, #ffefe9 0%, #ffffff 100%); padding: 20px; box-sizing: border-box; text-decoration: none;">
                                    <div style="font-size: 15px; font-weight: bold; color: #333;">"新品首发"</div>
                                    <div style="font-size: 12px; color: var(--general); margin-top: 4px;">"每日上新爆款"</div>
                                    <div style="width: 100px; height: 100px; margin: 15px auto 0; background: url('/static/images/goods_empty.png') center/contain no-repeat;"></div>
                                </a>
                            </div>
                            <div style="flex: 1; background: #fff; border-radius: 4px; overflow: hidden; position: relative; border: 1px solid #f0f0f0;">
                                <a href="/search" style="display: block; width: 100%; height: 100%; background: linear-gradient(180deg, #eaf6ff 0%, #ffffff 100%); padding: 20px; box-sizing: border-box; text-decoration: none;">
                                    <div style="font-size: 15px; font-weight: bold; color: #333;">"品质严选"</div>
                                    <div style="font-size: 12px; color: #1890ff; margin-top: 4px;">"大厂直供无忧"</div>
                                    <div style="width: 100px; height: 100px; margin: 15px auto 0; background: url('/static/images/goods_empty.png') center/contain no-repeat;"></div>
                                </a>
                            </div>
                        </div>
                    </div>

                    // 右侧: 用户信息与快捷通道 (Right 190px)
                    <div class="right" style="width: 190px; height: 470px; background: #fff; box-shadow: 0 0 8px rgba(0,0,0,0.04); box-sizing: border-box; padding: 15px; display: flex; flex-direction: column; justify-content: space-between;">
                        // 用户简况
                        <div class="user" style="text-align: center; padding-top: 10px;">
                            <div class="user_avatar" style="width: 54px; height: 54px; border-radius: 50%; margin: 0 auto; overflow: hidden; border: 2px solid #f0f0f0;">
                                <img src="/static/images/goods_empty.png" style="width: 100%; height: 100%; object-fit: cover;"/>
                            </div>
                            <div class="user_show" style="margin-top: 8px;">
                                <div style="font-size: 13px; font-weight: bold; color: #333;">"Hi, 尊贵客户"</div>
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
                                <span style="color: var(--general); font-weight: bold;">"【通知】"</span>"商城已接入真实数据库"
                            </div>
                            <div style="color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                <span style="color: var(--general); font-weight: bold;">"【特惠】"</span>"全场满 300 减 50 优惠券"
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            // 2. 限时秒杀专区楼层 (HomeSeckill 1:1)
            {
                if !seckill_products.is_empty() {
                    view! {
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
                                        seckill_products.into_iter().map(|p| {
                                            let name = p.name;
                                            let name_alt = name.clone();
                                            view! {
                                                <div class="proditem" style="padding: 20px 15px; border-right: 1px solid #f2f2f2; display: flex; flex-direction: column; justify-content: space-between; text-align: center; box-sizing: border-box;">
                                                    <a href=format!("/product/{}", p.id) style="display: block; width: 100%; height: 100%; display: flex; flex-direction: column; justify-content: space-between;">
                                                        <div class="seckill-item-image" style="width: 130px; height: 130px; margin: 0 auto; display: flex; align-items: center; justify-content: center;">
                                                            <img src=p.pic_thumb alt=name_alt style="max-width: 100%; max-height: 100%; object-fit: contain;"/>
                                                        </div>
                                                        <div class="seckill-item__name" style="font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 10px;">
                                                            {name}
                                                        </div>
                                                        <div class="seckill-item__price" style="margin-top: 8px;">
                                                            <div style="color: var(--price); font-size: 18px; font-weight: bold;">
                                                                <span style="font-size: 12px;">"¥"</span>{p.price}
                                                            </div>
                                                            <div style="font-size: 12px; color: #bbb; text-decoration: line-through; margin-top: 2px;">
                                                                "¥"{p.market_price}
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
                    }.into_any()
                } else {
                    view! { <div></div> }.into_any()
                }
            }

            // 3. 精选好物推荐楼层 (Recommend Floor - 真实 MySQL 数据直出)
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
                        recommend_products.iter().map(|p| {
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
