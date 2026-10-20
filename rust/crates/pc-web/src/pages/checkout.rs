use leptos::prelude::*;

#[component]
pub fn CheckoutPage() -> impl IntoView {
    view! {
        <div class="checkout-page" style="background: #f6f6f6; padding: 25px 0 60px 0;">
            <div class="container" style="max-width: 1200px; margin: 0 auto;">
                // 订单步骤条
                <div class="flex justify-between align-center" style="margin-bottom: 25px; padding-bottom: 15px; border-bottom: 2px solid #eee;">
                    <h2 style="font-size: 22px; color: #222; font-weight: bold; margin: 0;">"填写并核对订单信息"</h2>
                    <div class="steps flex align-center" style="font-size: 13px; gap: 15px;">
                        <span style="color: #999;">"1. 购物车"</span>
                        <span style="color: #ccc;">">"</span>
                        <span style="color: #ff3838; font-weight: bold;">"2. 填写核对订单"</span>
                        <span style="color: #ccc;">">"</span>
                        <span style="color: #999;">"3. 成功提交订单"</span>
                    </div>
                </div>

                // 1. 收货人地址选择块
                <div class="section-card" style="background: #fff; padding: 25px 30px; border-radius: 8px; border: 1px solid #eee; margin-bottom: 20px;">
                    <div class="flex justify-between align-center" style="margin-bottom: 18px;">
                        <span style="font-size: 16px; font-weight: bold; color: #333;">"收货人信息"</span>
                        <button style="border: 1px solid #ddd; background: #fff; color: #666; font-size: 12px; padding: 4px 12px; border-radius: 4px; cursor: pointer;">
                            "+ 新增收货地址"
                        </button>
                    </div>

                    // 默认选中的收货地址
                    <div style="border: 2px solid #ff3838; border-radius: 6px; padding: 15px 20px; width: 380px; position: relative; background: #fffcfc;">
                        <div class="flex align-center" style="gap: 15px; margin-bottom: 8px;">
                            <span style="font-size: 15px; font-weight: bold; color: #222;">"张三"</span>
                            <span style="color: #666; font-size: 13px;">"138****8888"</span>
                            <span style="background: #ff3838; color: #fff; font-size: 10px; padding: 1px 5px; border-radius: 2px;">"默认"</span>
                        </div>
                        <div style="font-size: 13px; color: #666; line-height: 1.5;">
                            "浙江省 杭州市 滨江区 江南大道 888号 科技园区 6号楼 801室"
                        </div>
                    </div>
                </div>

                // 2. 支付及配送方式
                <div class="section-card" style="background: #fff; padding: 25px 30px; border-radius: 8px; border: 1px solid #eee; margin-bottom: 20px; font-size: 13px;">
                    <div style="font-size: 16px; font-weight: bold; color: #333; margin-bottom: 18px;">"支付及配送方式"</div>
                    <div class="flex" style="gap: 15px; margin-bottom: 15px;">
                        <button style="border: 2px solid #ff3838; background: #fff; color: #ff3838; font-weight: bold; padding: 8px 24px; border-radius: 4px; cursor: pointer;">
                            "在线支付 (微信/支付宝/银行卡)"
                        </button>
                        <button style="border: 1px solid #ddd; background: #fff; color: #666; padding: 8px 24px; border-radius: 4px; cursor: pointer;">
                            "企业对公转账 / 账期月结"
                        </button>
                    </div>
                    <div style="color: #888; font-size: 12px;">"配送服务：顺丰特快直发 · 预计下单后次日即可送达"</div>
                </div>

                // 3. 商品清单核对
                <div class="section-card" style="background: #fff; padding: 25px 30px; border-radius: 8px; border: 1px solid #eee; margin-bottom: 20px;">
                    <div style="font-size: 16px; font-weight: bold; color: #333; margin-bottom: 18px;">"送货清单"</div>
                    <div class="item-list flex align-center" style="padding: 15px 0; border-bottom: 1px solid #f0f0f0; gap: 20px; font-size: 13px;">
                        <img src="/static/images/goods_empty.png" style="width: 60px; height: 60px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;"/>
                        <div style="flex: 1;">
                            <div style="font-weight: bold; color: #333;">"Apple iPhone 16 Pro Max 256GB 原色钛金属"</div>
                            <div style="color: #999; font-size: 12px; margin-top: 4px;">"原色钛金属 / 256GB"</div>
                        </div>
                        <div style="color: #ff3838; font-weight: bold; width: 120px; text-align: center;">"¥ 8999.00"</div>
                        <div style="color: #666; width: 80px; text-align: center;">"x 1"</div>
                        <div style="color: #ff3838; font-weight: bold; width: 120px; text-align: right;">"¥ 8999.00"</div>
                    </div>
                    <div class="item-list flex align-center" style="padding: 15px 0; gap: 20px; font-size: 13px;">
                        <img src="/static/images/goods_empty.png" style="width: 60px; height: 60px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;"/>
                        <div style="flex: 1;">
                            <div style="font-weight: bold; color: #333;">"Sony WH-1000XM5 无线头戴式降噪耳机 铂金银"</div>
                            <div style="color: #999; font-size: 12px; margin-top: 4px;">"铂金银 / 标配"</div>
                        </div>
                        <div style="color: #ff3838; font-weight: bold; width: 120px; text-align: center;">"¥ 2299.00"</div>
                        <div style="color: #666; width: 80px; text-align: center;">"x 1"</div>
                        <div style="color: #ff3838; font-weight: bold; width: 120px; text-align: right;">"¥ 2299.00"</div>
                    </div>

                    // 订单买家留言
                    <div style="margin-top: 20px; padding-top: 15px; border-top: 1px dashed #eee;">
                        <span style="font-size: 13px; color: #666;">"买家订单备注："</span>
                        <input
                            type="text"
                            placeholder="选填：如包装配送要求、发货时间说明等"
                            style="width: 450px; margin-left: 10px; padding: 6px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 12px; outline: none;"
                        />
                    </div>
                </div>

                // 4. 结算金额总计面板
                <div
                    class="settlement-summary flex flex-column align-end"
                    style="background: #fff; padding: 25px 30px; border-radius: 8px; border: 1px solid #eee; font-size: 14px; gap: 10px;"
                >
                    <div class="flex justify-between" style="width: 280px; color: #666;">
                        <span>"商品总金额："</span>
                        <span style="color: #333; font-weight: bold;">"¥ 11298.00"</span>
                    </div>
                    <div class="flex justify-between" style="width: 280px; color: #666;">
                        <span>"运费："</span>
                        <span style="color: #333; font-weight: bold;">"+ ¥ 0.00"</span>
                    </div>
                    <div class="flex justify-between" style="width: 280px; color: #666;">
                        <span>"活动立减优惠："</span>
                        <span style="color: #ff3838; font-weight: bold;">"- ¥ 0.00"</span>
                    </div>
                    <div style="border-top: 1px solid #eee; width: 100%; margin: 10px 0;"></div>
                    <div style="text-align: right;">
                        <span style="font-size: 15px; color: #333;">"应付总额："</span>
                        <span style="color: #ff3838; font-size: 30px; font-weight: 900;">"¥ 11298.00"</span>
                    </div>
                    <div style="color: #888; font-size: 12px; margin-top: 5px;">
                        "寄送至：浙江省 杭州市 滨江区 江南大道888号 801室 · 张三 (138****8888)"
                    </div>
                    <button
                        style="background: #ff3838; color: #fff; border: none; width: 180px; height: 50px; border-radius: 4px; font-size: 18px; font-weight: bold; cursor: pointer; margin-top: 15px; box-shadow: 0 4px 15px rgba(255, 56, 56, 0.4);"
                    >
                        "提交订单"
                    </button>
                </div>
            </div>
        </div>
    }
}
