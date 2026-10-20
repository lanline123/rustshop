use leptos::prelude::*;

#[component]
pub fn CheckoutPage() -> impl IntoView {
    view! {
        <div class="checkout-page" style="background: #f2f3f5; min-height: 80vh; padding: 25px 0 60px;">
            <div class="container">
                // 结算步骤指示器
                <div style="background: #fff; padding: 20px 30px; border-radius: 4px; border: 1px solid #e5e5e5; margin-bottom: 25px; display: flex; justify-content: space-between; align-items: center;">
                    <div style="font-size: 20px; font-weight: bold; color: #333;">"填写并核对订单信息"</div>
                    <div style="display: flex; align-items: center; gap: 12px; font-size: 13px;">
                        <span style="color: #999;">"1. 我的购物车"</span>
                        <span style="color: #ccc;">"——"</span>
                        <span style="color: var(--general); font-weight: bold;">"2. 填写核对订单信息"</span>
                        <span style="color: #ccc;">"——"</span>
                        <span style="color: #999;">"3. 成功提交订单"</span>
                    </div>
                </div>

                // 结算内容卡片
                <div style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 30px; box-sizing: border-box;">
                    // 收货人信息
                    <div style="margin-bottom: 30px;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                            <span style="font-size: 16px; font-weight: bold; color: #333;">"收货人信息"</span>
                            <a href="#" style="font-size: 13px; color: var(--general);">"+ 新增收货地址"</a>
                        </div>
                        <div style="border: 2px solid var(--general); border-radius: 4px; padding: 16px 20px; max-width: 480px; position: relative; background: #fffcfc;">
                            <div style="display: flex; justify-content: space-between; font-size: 14px; font-weight: bold; color: #333; margin-bottom: 8px;">
                                <span>"张三 (先生) 138****8888"</span>
                                <span style="font-size: 12px; background: var(--general); color: #fff; padding: 1px 8px; border-radius: 2px;">"默认地址"</span>
                            </div>
                            <div style="font-size: 13px; color: #666; line-height: 20px;">
                                "北京市 朝阳区 建国路88号 现代SOHO大厦 A座 1808室"
                            </div>
                        </div>
                    </div>

                    // 支付方式
                    <div style="margin-bottom: 30px; border-top: 1px solid #eee; padding-top: 25px;">
                        <div style="font-size: 16px; font-weight: bold; color: #333; margin-bottom: 16px;">"支付方式"</div>
                        <div style="display: flex; gap: 15px;">
                            <button style="border: 2px solid var(--general); background: #fff; color: var(--general); padding: 10px 24px; border-radius: 4px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                <span class="iconfont-pc icon-zhifubao" style="font-size: 18px; color: #1677ff;"></span>
                                "支付宝支付"
                            </button>
                            <button style="border: 1px solid #ddd; background: #fff; color: #333; padding: 10px 24px; border-radius: 4px; font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                <span class="iconfont-pc icon-weixin" style="font-size: 18px; color: #52c41a;"></span>
                                "微信支付"
                            </button>
                            <button style="border: 1px solid #ddd; background: #fff; color: #333; padding: 10px 24px; border-radius: 4px; font-size: 14px; cursor: pointer;">
                                "余额支付 (可用：¥0.00)"
                            </button>
                        </div>
                    </div>

                    // 送货清单
                    <div style="margin-bottom: 30px; border-top: 1px solid #eee; padding-top: 25px;">
                        <div style="font-size: 16px; font-weight: bold; color: #333; margin-bottom: 16px;">"送货清单"</div>
                        <div style="border: 1px solid #f0f0f0; border-radius: 4px; overflow: hidden;">
                            <div style="display: grid; grid-template-columns: 1fr 140px 140px 140px; padding: 12px 20px; background: #fafafa; font-size: 13px; font-weight: bold; color: #666;">
                                <div>"商品信息"</div>
                                <div style="text-align: center;">"单价"</div>
                                <div style="text-align: center;">"数量"</div>
                                <div style="text-align: right;">"小计"</div>
                            </div>
                            <div style="display: grid; grid-template-columns: 1fr 140px 140px 140px; padding: 18px 20px; align-items: center; font-size: 13px; border-bottom: 1px solid #f5f5f5;">
                                <div style="display: flex; gap: 15px; align-items: center;">
                                    <img src="/static/images/goods_empty.png" style="width: 60px; height: 60px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;"/>
                                    <div>
                                        <div style="font-weight: 500; color: #333;">"Apple iPhone 16 Pro Max 256GB 原色钛金属"</div>
                                        <div style="font-size: 12px; color: #999; margin-top: 4px;">"规格：原色钛金属 / 256GB"</div>
                                    </div>
                                </div>
                                <div style="text-align: center; color: #333;">"¥8999.00"</div>
                                <div style="text-align: center; color: #666;">"1"</div>
                                <div style="text-align: right; font-weight: bold; color: var(--price);">"¥8999.00"</div>
                            </div>
                            <div style="display: grid; grid-template-columns: 1fr 140px 140px 140px; padding: 18px 20px; align-items: center; font-size: 13px;">
                                <div style="display: flex; gap: 15px; align-items: center;">
                                    <img src="/static/images/goods_empty.png" style="width: 60px; height: 60px; border: 1px solid #eee; border-radius: 4px; object-fit: contain;"/>
                                    <div>
                                        <div style="font-weight: 500; color: #333;">"Sony WH-1000XM5 头戴式无线蓝牙降噪耳机"</div>
                                        <div style="font-size: 12px; color: #999; margin-top: 4px;">"规格：黑色 / 官方标配"</div>
                                    </div>
                                </div>
                                <div style="text-align: center; color: #333;">"¥2299.00"</div>
                                <div style="text-align: center; color: #666;">"1"</div>
                                <div style="text-align: right; font-weight: bold; color: var(--price);">"¥2299.00"</div>
                            </div>
                        </div>
                    </div>

                    // 费用明细与提交订单
                    <div style="border-top: 1px solid #eee; padding-top: 25px; display: flex; justify-content: flex-end;">
                        <div style="text-align: right; min-width: 300px;">
                            <div style="font-size: 13px; color: #666; line-height: 28px;">
                                <div style="display: flex; justify-content: space-between;">
                                    <span>"商品件数："</span>
                                    <span>"2 件"</span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span>"商品总额："</span>
                                    <span>"¥11,298.00"</span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span>"运费："</span>
                                    <span>"¥0.00 (满额包邮)"</span>
                                </div>
                                <div style="display: flex; justify-content: space-between; border-top: 1px solid #eee; margin-top: 10px; padding-top: 10px;">
                                    <span style="font-size: 15px; font-weight: bold; color: #333;">"应付总额："</span>
                                    <span style="font-size: 26px; font-weight: bold; color: var(--price); font-family: Arial;">"¥11,298.00"</span>
                                </div>
                            </div>
                            <div style="margin-top: 20px;">
                                <button
                                    style="background: var(--general); color: #fff; font-size: 18px; font-weight: bold; border: none; padding: 14px 50px; border-radius: 4px; cursor: pointer; box-shadow: 0 4px 14px rgba(255, 68, 68, 0.35);"
                                >
                                    "提交订单"
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}
