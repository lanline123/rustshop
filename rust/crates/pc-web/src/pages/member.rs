use leptos::prelude::*;

#[component]
pub fn MemberPage() -> impl IntoView {
    // 模拟订单数据
    let orders = vec![
        ("202610200001", "Apple iPhone 16 Pro Max 256GB 原色钛金属", "¥ 8999.00", "待付款", "2026-10-20 10:15:30", true),
        ("202610180023", "Sony WH-1000XM5 无线头戴式降噪耳机", "¥ 2299.00", "已发货 (顺丰速运)", "2026-10-18 14:22:10", false),
        ("202610100089", "三只松鼠 坚果大礼包 每日坚果送礼版 1428g", "¥ 128.00", "已完成", "2026-10-10 09:30:00", false),
    ];

    view! {
        <div class="member-page" style="background: #f6f6f6; padding: 25px 0 60px 0;">
            <div class="container flex" style="max-width: 1200px; margin: 0 auto; gap: 20px;">
                // 左侧会员导航菜单
                <div class="member-sidebar" style="width: 200px; background: #fff; border-radius: 8px; border: 1px solid #eee; padding: 20px 0; box-sizing: border-box;">
                    <div style="padding: 0 20px 15px 20px; font-size: 15px; font-weight: bold; color: #222; border-bottom: 1px solid #f0f0f0;">
                        "个人中心"
                    </div>

                    <div style="padding: 15px 0;">
                        <div style="padding: 6px 20px; font-size: 13px; font-weight: bold; color: #888;">"订单中心"</div>
                        <a href="/member" style="display: block; padding: 8px 20px; font-size: 13px; color: #ff3838; background: #fff2f2; font-weight: bold; text-decoration: none; border-left: 3px solid #ff3838;">
                            "我的订单"
                        </a>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"评价中心"</a>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"退换货售后"</a>
                    </div>

                    <div style="padding: 10px 0; border-top: 1px solid #f5f5f5;">
                        <div style="padding: 6px 20px; font-size: 13px; font-weight: bold; color: #888;">"我的资产"</div>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"账户余额"</a>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"优惠券"</a>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"我的积分"</a>
                    </div>

                    <div style="padding: 10px 0; border-top: 1px solid #f5f5f5;">
                        <div style="padding: 6px 20px; font-size: 13px; font-weight: bold; color: #888;">"账户设置"</div>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"个人信息"</a>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"收货地址"</a>
                        <a href="#" style="display: block; padding: 8px 20px; font-size: 13px; color: #555; text-decoration: none;">"安全中心"</a>
                    </div>
                </div>

                // 右侧主体内容
                <div class="member-content flex-1" style="flex: 1; display: flex; flex-direction: column; gap: 20px;">
                    // 顶部个人信息卡片
                    <div class="profile-card flex justify-between align-center" style="background: #fff; padding: 25px 30px; border-radius: 8px; border: 1px solid #eee;">
                        <div class="flex align-center" style="gap: 20px;">
                            <div style="width: 70px; height: 70px; border-radius: 50%; background: #ffdada; display: flex; align-items: center; justify-content: center; font-size: 26px; color: #ff3838; font-weight: bold;">
                                "张"
                            </div>
                            <div>
                                <div style="font-size: 18px; font-weight: bold; color: #222;">"张三 (VIP会员)"</div>
                                <div style="font-size: 12px; color: #888; margin-top: 5px;">"账号绑定手机号：138****8888"</div>
                            </div>
                        </div>

                        <div class="flex" style="gap: 40px; text-align: center;">
                            <div>
                                <div style="font-size: 12px; color: #888;">"账户余额"</div>
                                <div style="font-size: 20px; font-weight: bold; color: #ff3838; margin-top: 5px;">"¥ 950.00"</div>
                            </div>
                            <div>
                                <div style="font-size: 12px; color: #888;">"优惠券"</div>
                                <div style="font-size: 20px; font-weight: bold; color: #333; margin-top: 5px;">"3 张"</div>
                            </div>
                            <div>
                                <div style="font-size: 12px; color: #888;">"会员积分"</div>
                                <div style="font-size: 20px; font-weight: bold; color: #333; margin-top: 5px;">"1,250"</div>
                            </div>
                        </div>
                    </div>

                    // 订单快捷状态栏
                    <div class="order-status-bar flex justify-around align-center" style="background: #fff; padding: 20px; border-radius: 8px; border: 1px solid #eee; text-align: center;">
                        <a href="#" style="color: #444; text-decoration: none; display: flex; flex-direction: column; gap: 6px;">
                            <span style="font-size: 20px; color: #ff3838; font-weight: bold;">"1"</span>
                            <span style="font-size: 13px;">"待付款"</span>
                        </a>
                        <span style="color: #eee;">"|"</span>
                        <a href="#" style="color: #444; text-decoration: none; display: flex; flex-direction: column; gap: 6px;">
                            <span style="font-size: 20px; color: #333; font-weight: bold;">"0"</span>
                            <span style="font-size: 13px;">"待发货"</span>
                        </a>
                        <span style="color: #eee;">"|"</span>
                        <a href="#" style="color: #444; text-decoration: none; display: flex; flex-direction: column; gap: 6px;">
                            <span style="font-size: 20px; color: #ff3838; font-weight: bold;">"2"</span>
                            <span style="font-size: 13px;">"待收货"</span>
                        </a>
                        <span style="color: #eee;">"|"</span>
                        <a href="#" style="color: #444; text-decoration: none; display: flex; flex-direction: column; gap: 6px;">
                            <span style="font-size: 20px; color: #333; font-weight: bold;">"3"</span>
                            <span style="font-size: 13px;">"待评价"</span>
                        </a>
                    </div>

                    // 最近订单列表
                    <div class="orders-section" style="background: #fff; border-radius: 8px; border: 1px solid #eee; padding: 25px 30px;">
                        <div class="flex justify-between align-center" style="margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0;">
                            <span style="font-size: 16px; font-weight: bold; color: #222;">"近期订单"</span>
                            <a href="#" style="font-size: 12px; color: #888; text-decoration: none;">"查看全部订单 >"</a>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 15px;">
                            {
                                orders.into_iter().map(|(sn, prod_name, total, status, time, can_pay)| {
                                    view! {
                                        <div class="order-box" style="border: 1px solid #eee; border-radius: 6px; overflow: hidden; font-size: 13px;">
                                            <div class="order-head flex justify-between" style="background: #fafafa; padding: 10px 18px; color: #888; font-size: 12px;">
                                                <span>"订单号：" {sn} " · 下单时间：" {time}</span>
                                                <span style="color: #ff3838; font-weight: bold;">{status}</span>
                                            </div>
                                            <div class="order-body flex justify-between align-center" style="padding: 15px 18px;">
                                                <div class="flex align-center" style="gap: 15px;">
                                                    <img src="/static/images/goods_empty.png" style="width: 50px; height: 50px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;"/>
                                                    <div>
                                                        <div style="font-weight: bold; color: #333;">{prod_name}</div>
                                                        <div style="color: #999; font-size: 12px; margin-top: 4px;">"件数: 1"</div>
                                                    </div>
                                                </div>
                                                <div style="text-align: right;">
                                                    <div style="font-size: 16px; font-weight: bold; color: #222;">{total}</div>
                                                    <div style="margin-top: 6px;">
                                                        {
                                                            if can_pay {
                                                                view! {
                                                                    <a href="/checkout" style="background: #ff3838; color: #fff; padding: 4px 12px; border-radius: 3px; text-decoration: none; font-size: 12px; font-weight: bold;">
                                                                        "立即付款"
                                                                    </a>
                                                                }.into_any()
                                                            } else {
                                                                view! {
                                                                    <a href="#" style="color: #666; border: 1px solid #ddd; padding: 4px 10px; border-radius: 3px; text-decoration: none; font-size: 12px;">
                                                                        "查看详情"
                                                                    </a>
                                                                }.into_any()
                                                            }
                                                        }
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    }
                                }).collect::<Vec<_>>()
                            }
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}
