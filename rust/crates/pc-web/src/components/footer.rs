use leptos::prelude::*;

#[component]
pub fn Footer() -> impl IntoView {
    view! {
        <footer class="footer flex flex-column align-center" style="background: #fff; margin-top: 40px; border-top: 1px solid #e5e5e5; padding-top: 25px;">
            <div class="container">
                // 1. 四大服务承诺条 (Foot Service Banner)
                <div style="display: grid; grid-template-columns: repeat(4, 1fr); padding: 25px 0; border-bottom: 1px solid #eee; text-align: center;">
                    <div style="display: flex; align-items: center; justify-content: center; gap: 14px;">
                        <div style="width: 44px; height: 44px; border-radius: 50%; border: 2px solid var(--general); display: flex; align-items: center; justify-content: center; color: var(--general); font-size: 20px; font-weight: bold;">
                            "正"
                        </div>
                        <div style="text-align: left;">
                            <div style="font-size: 16px; font-weight: bold; color: #333;">"正品保障"</div>
                            <div style="font-size: 12px; color: #999; margin-top: 2px;">"正品行货 假一赔十"</div>
                        </div>
                    </div>

                    <div style="display: flex; align-items: center; justify-content: center; gap: 14px;">
                        <div style="width: 44px; height: 44px; border-radius: 50%; border: 2px solid var(--general); display: flex; align-items: center; justify-content: center; color: var(--general); font-size: 20px; font-weight: bold;">
                            "顺"
                        </div>
                        <div style="text-align: left;">
                            <div style="font-size: 16px; font-weight: bold; color: #333;">"顺丰速达"</div>
                            <div style="font-size: 12px; color: #999; margin-top: 2px;">"极速配送 准时送达"</div>
                        </div>
                    </div>

                    <div style="display: flex; align-items: center; justify-content: center; gap: 14px;">
                        <div style="width: 44px; height: 44px; border-radius: 50%; border: 2px solid var(--general); display: flex; align-items: center; justify-content: center; color: var(--general); font-size: 20px; font-weight: bold;">
                            "退"
                        </div>
                        <div style="text-align: left;">
                            <div style="font-size: 16px; font-weight: bold; color: #333;">"售后无忧"</div>
                            <div style="font-size: 12px; color: #999; margin-top: 2px;">"7天无理由 15天换货"</div>
                        </div>
                    </div>

                    <div style="display: flex; align-items: center; justify-content: center; gap: 14px;">
                        <div style="width: 44px; height: 44px; border-radius: 50%; border: 2px solid var(--general); display: flex; align-items: center; justify-content: center; color: var(--general); font-size: 20px; font-weight: bold;">
                            "优"
                        </div>
                        <div style="text-align: left;">
                            <div style="font-size: 16px; font-weight: bold; color: #333;">"尊享服务"</div>
                            <div style="font-size: 12px; color: #999; margin-top: 2px;">"专属客服 贴心答疑"</div>
                        </div>
                    </div>
                </div>

                // 2. 帮助与指引矩阵 (Help Columns)
                <div class="foot-help" style="display: flex; justify-content: space-between; padding: 35px 20px 30px; border-bottom: 1px solid #eee;">
                    <div>
                        <div style="font-size: 14px; font-weight: bold; color: #333; margin-bottom: 14px;">"购物指南"</div>
                        <div style="font-size: 12px; color: #666; line-height: 24px; display: flex; flex-direction: column;">
                            <a href="#">"购物流程"</a>
                            <a href="#">"会员介绍"</a>
                            <a href="#">"常见问题"</a>
                            <a href="#">"联系客服"</a>
                        </div>
                    </div>

                    <div>
                        <div style="font-size: 14px; font-weight: bold; color: #333; margin-bottom: 14px;">"配送方式"</div>
                        <div style="font-size: 12px; color: #666; line-height: 24px; display: flex; flex-direction: column;">
                            <a href="#">"上门自提"</a>
                            <a href="#">"配送服务查询"</a>
                            <a href="#">"配送费收取标准"</a>
                            <a href="#">"海外配送"</a>
                        </div>
                    </div>

                    <div>
                        <div style="font-size: 14px; font-weight: bold; color: #333; margin-bottom: 14px;">"支付方式"</div>
                        <div style="font-size: 12px; color: #666; line-height: 24px; display: flex; flex-direction: column;">
                            <a href="#">"在线支付"</a>
                            <a href="#">"分期付款"</a>
                            <a href="#">"邮局汇款"</a>
                            <a href="#">"公司转账"</a>
                        </div>
                    </div>

                    <div>
                        <div style="font-size: 14px; font-weight: bold; color: #333; margin-bottom: 14px;">"售后服务"</div>
                        <div style="font-size: 12px; color: #666; line-height: 24px; display: flex; flex-direction: column;">
                            <a href="#">"售后政策"</a>
                            <a href="#">"价格保护"</a>
                            <a href="#">"退款说明"</a>
                            <a href="#">"返修/退换货"</a>
                        </div>
                    </div>

                    // 客服咨询板块
                    <div style="text-align: right; border-left: 1px solid #eee; padding-left: 40px;">
                        <div style="font-size: 14px; font-weight: bold; color: #333; margin-bottom: 10px;">"客服热线"</div>
                        <div style="font-size: 22px; font-weight: bold; color: var(--general); margin-bottom: 6px;">"400-888-9999"</div>
                        <div style="font-size: 12px; color: #999; margin-bottom: 12px;">"周一至周日 09:00 - 22:00"</div>
                        <button style="background: var(--general); color: #fff; border: none; padding: 6px 18px; border-radius: 4px; font-size: 12px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px;">
                            <span class="iconfont-pc icon-kefu"></span>
                            "在线咨询"
                        </button>
                    </div>
                </div>

                // 3. ICP、网安与版权归属 (Copyright)
                <div class="copy" style="padding: 25px 0 40px; text-align: center; font-size: 12px; color: #999; line-height: 26px;">
                    <div>
                        <a href="/" style="color: #666; margin: 0 10px;">"商城首页"</a>"|"
                        <a href="/search" style="color: #666; margin: 0 10px;">"全部分类"</a>"|"
                        <a href="#" style="color: #666; margin: 0 10px;">"隐私政策"</a>"|"
                        <a href="#" style="color: #666; margin: 0 10px;">"服务条款"</a>"|"
                        <a href="#" style="color: #666; margin: 0 10px;">"商家入驻"</a>"|"
                        <a href="#" style="color: #666; margin: 0 10px;">"关于我们"</a>
                    </div>
                    <div style="margin-top: 8px;">
                        "Copyright © 2026 TigShop 商业电子商务系统. All Rights Reserved. 增值电信业务经营许可证：京ICP备20268888号"
                    </div>
                    <div style="display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 4px;">
                        <span class="iconfont-pc icon-anquan" style="color: #52c41a;"></span>
                        "京公网安备 11010802020268号 · 中国电子商务协会诚信认证"
                    </div>
                </div>
            </div>
        </footer>
    }
}
