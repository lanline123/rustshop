use leptos::prelude::*;

#[component]
pub fn MemberPage() -> impl IntoView {
    view! {
        <div class="member-page" style="background: #f2f3f5; min-height: 80vh; padding: 25px 0 60px;">
            <div class="container" style="display: grid; grid-template-columns: 210px 1fr; gap: 20px;">
                // 左侧导航树
                <div style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 20px 0; box-sizing: border-box; height: fit-content;">
                    <div style="padding: 0 20px 15px; font-size: 16px; font-weight: bold; color: #333; border-bottom: 1px solid #f2f2f2;">
                        "会员中心"
                    </div>

                    // 交易管理
                    <div style="padding: 15px 20px 8px; font-size: 14px; font-weight: bold; color: #666;">
                        "订单中心"
                    </div>
                    <div style="display: flex; flex-direction: column; font-size: 13px;">
                        <a href="/order/list" style="padding: 8px 20px; color: var(--general); background: #fff2f2; border-left: 3px solid var(--general); font-weight: bold;">"我的订单"</a>
                        <a href="#" style="padding: 8px 20px; color: #666;">"我的收藏"</a>
                        <a href="#" style="padding: 8px 20px; color: #666;">"评价晒单"</a>
                        <a href="#" style="padding: 8px 20px; color: #666;">"退款及售后"</a>
                    </div>

                    // 资产中心
                    <div style="padding: 18px 20px 8px; font-size: 14px; font-weight: bold; color: #666; border-top: 1px solid #f8f8f8; margin-top: 10px;">
                        "资产中心"
                    </div>
                    <div style="display: flex; flex-direction: column; font-size: 13px;">
                        <a href="#" style="padding: 8px 20px; color: #666;">"我的余额"</a>
                        <a href="#" style="padding: 8px 20px; color: #666;">"我的优惠券"</a>
                        <a href="#" style="padding: 8px 20px; color: #666;">"我的积分"</a>
                    </div>

                    // 个人设置
                    <div style="padding: 18px 20px 8px; font-size: 14px; font-weight: bold; color: #666; border-top: 1px solid #f8f8f8; margin-top: 10px;">
                        "个人设置"
                    </div>
                    <div style="display: flex; flex-direction: column; font-size: 13px;">
                        <a href="#" style="padding: 8px 20px; color: #666;">"个人资料"</a>
                        <a href="#" style="padding: 8px 20px; color: #666;">"收货地址管理"</a>
                        <a href="#" style="padding: 8px 20px; color: #666;">"账号安全中心"</a>
                    </div>
                </div>

                // 右侧主面板
                <div>
                    // 个人概览头图
                    <div style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 25px 30px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
                        <div style="display: flex; gap: 20px; align-items: center;">
                            <div style="width: 72px; height: 72px; border-radius: 50%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                <span class="iconfont-pc icon-wode" style="font-size: 38px; color: #bbb;"></span>
                            </div>
                            <div>
                                <div style="display: flex; align-items: center; gap: 10px;">
                                    <span style="font-size: 18px; font-weight: bold; color: #333;">"尊敬的用户 (tig_user_88)"</span>
                                    <span style="background: #ffec3d; color: #d46b08; font-size: 11px; font-weight: bold; padding: 2px 8px; border-radius: 10px;">"VIP 黄金会员"</span>
                                </div>
                                <div style="font-size: 13px; color: #999; margin-top: 6px;">
                                    "欢迎回到 TigShop 商城！享受极速响应与专属特权"
                                </div>
                            </div>
                        </div>

                        // 资产计数
                        <div style="display: flex; gap: 35px; text-align: center; border-left: 1px solid #eee; padding-left: 35px;">
                            <div>
                                <div style="font-size: 20px; font-weight: bold; color: var(--general); font-family: Arial;">"¥0.00"</div>
                                <div style="font-size: 12px; color: #999; margin-top: 4px;">"账户余额"</div>
                            </div>
                            <div>
                                <div style="font-size: 20px; font-weight: bold; color: #333; font-family: Arial;">"3"</div>
                                <div style="font-size: 12px; color: #999; margin-top: 4px;">"可用优惠券"</div>
                            </div>
                            <div>
                                <div style="font-size: 20px; font-weight: bold; color: #333; font-family: Arial;">"1,250"</div>
                                <div style="font-size: 12px; color: #999; margin-top: 4px;">"我的积分"</div>
                            </div>
                        </div>
                    </div>

                    // 订单中心主卡片
                    <div style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 20px 25px;">
                        // 订单过滤选项卡
                        <div style="display: flex; border-bottom: 2px solid #f2f2f2; margin-bottom: 20px; gap: 30px;">
                            <span style="font-size: 15px; font-weight: bold; color: var(--general); padding-bottom: 12px; border-bottom: 2px solid var(--general); margin-bottom: -2px; cursor: pointer;">
                                "全部订单"
                            </span>
                            <span style="font-size: 15px; color: #666; padding-bottom: 12px; cursor: pointer;">
                                "待付款 (1)"
                            </span>
                            <span style="font-size: 15px; color: #666; padding-bottom: 12px; cursor: pointer;">
                                "待发货 (0)"
                            </span>
                            <span style="font-size: 15px; color: #666; padding-bottom: 12px; cursor: pointer;">
                                "待收货 (1)"
                            </span>
                            <span style="font-size: 15px; color: #666; padding-bottom: 12px; cursor: pointer;">
                                "待评价 (0)"
                            </span>
                        </div>

                        // 订单列表
                        <div style="display: flex; flex-direction: column; gap: 16px;">
                            // 订单 1
                            <div style="border: 1px solid #e5e5e5; border-radius: 4px; overflow: hidden;">
                                <div style="background: #fafafa; padding: 10px 18px; font-size: 13px; color: #666; display: flex; justify-content: space-between; border-bottom: 1px solid #eee;">
                                    <div style="display: flex; gap: 20px;">
                                        <span>"下单时间：2026-10-20 10:35:12"</span>
                                        <span>"订单编号：TIG2026102008892"</span>
                                    </div>
                                    <span style="color: var(--general); font-weight: bold;">"等待买家付款"</span>
                                </div>
                                <div style="padding: 16px 18px; display: grid; grid-template-columns: 1fr 140px 140px 120px; align-items: center; font-size: 13px;">
                                    <div style="display: flex; gap: 14px; align-items: center;">
                                        <img src="/static/images/goods_empty.png" style="width: 55px; height: 55px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;"/>
                                        <div>
                                            <div style="font-weight: 500; color: #333;">"Apple iPhone 16 Pro Max 256GB 原色钛金属"</div>
                                            <div style="font-size: 12px; color: #999; margin-top: 4px;">"原色钛金属 / 256GB × 1"</div>
                                        </div>
                                    </div>
                                    <div style="text-align: center; color: #666;">"张三 (收)"</div>
                                    <div style="text-align: center;">
                                        <div style="font-size: 15px; font-weight: bold; color: #333; font-family: Arial;">"¥8,999.00"</div>
                                        <div style="font-size: 12px; color: #999; margin-top: 2px;">"在线支付"</div>
                                    </div>
                                    <div style="text-align: right; display: flex; flex-direction: column; gap: 6px;">
                                        <button style="background: var(--general); color: #fff; border: none; padding: 6px 14px; border-radius: 3px; cursor: pointer; font-size: 12px; font-weight: bold;">
                                            "立即付款"
                                        </button>
                                        <button style="background: transparent; color: #666; border: 1px solid #ddd; padding: 5px 14px; border-radius: 3px; cursor: pointer; font-size: 12px;">
                                            "取消订单"
                                        </button>
                                    </div>
                                </div>
                            </div>

                            // 订单 2
                            <div style="border: 1px solid #e5e5e5; border-radius: 4px; overflow: hidden;">
                                <div style="background: #fafafa; padding: 10px 18px; font-size: 13px; color: #666; display: flex; justify-content: space-between; border-bottom: 1px solid #eee;">
                                    <div style="display: flex; gap: 20px;">
                                        <span>"下单时间：2026-10-18 16:20:00"</span>
                                        <span>"订单编号：TIG2026101807716"</span>
                                    </div>
                                    <span style="color: #52c41a; font-weight: bold;">"已发货 · 运输中"</span>
                                </div>
                                <div style="padding: 16px 18px; display: grid; grid-template-columns: 1fr 140px 140px 120px; align-items: center; font-size: 13px;">
                                    <div style="display: flex; gap: 14px; align-items: center;">
                                        <img src="/static/images/goods_empty.png" style="width: 55px; height: 55px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;"/>
                                        <div>
                                            <div style="font-weight: 500; color: #333;">"Sony WH-1000XM5 头戴式无线蓝牙降噪耳机 黑色"</div>
                                            <div style="font-size: 12px; color: #999; margin-top: 4px;">"黑色 / 官方标配 × 1"</div>
                                        </div>
                                    </div>
                                    <div style="text-align: center; color: #666;">"张三 (收)"</div>
                                    <div style="text-align: center;">
                                        <div style="font-size: 15px; font-weight: bold; color: #333; font-family: Arial;">"¥2,299.00"</div>
                                        <div style="font-size: 12px; color: #999; margin-top: 2px;">"已微信支付"</div>
                                    </div>
                                    <div style="text-align: right; display: flex; flex-direction: column; gap: 6px;">
                                        <button style="background: var(--general); color: #fff; border: none; padding: 6px 14px; border-radius: 3px; cursor: pointer; font-size: 12px; font-weight: bold;">
                                            "确认收货"
                                        </button>
                                        <button style="background: transparent; color: #666; border: 1px solid #ddd; padding: 5px 14px; border-radius: 3px; cursor: pointer; font-size: 12px;">
                                            "查看物流"
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}
