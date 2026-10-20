use leptos::prelude::*;

#[component]
pub fn Footer() -> impl IntoView {
    view! {
        <footer class="mall-footer" style="background: #2e2e2e; color: #999; margin-top: 50px; padding: 40px 0 20px 0; font-size: 13px;">
            // 四大服务承诺保障横条
            <div class="guarantee-bar" style="border-bottom: 1px solid #3d3d3d; padding-bottom: 30px;">
                <div class="container flex justify-between" style="max-width: 1200px; margin: 0 auto;">
                    <div class="item flex align-center" style="gap: 12px;">
                        <span style="font-size: 28px; color: #ff3838; font-weight: bold;">"品"</span>
                        <div>
                            <div style="color: #fff; font-size: 15px; font-weight: bold;">"正品保障"</div>
                            <div style="font-size: 12px; margin-top: 3px;">"正品货源，假一赔十"</div>
                        </div>
                    </div>
                    <div class="item flex align-center" style="gap: 12px;">
                        <span style="font-size: 28px; color: #ff3838; font-weight: bold;">"退"</span>
                        <div>
                            <div style="color: #fff; font-size: 15px; font-weight: bold;">"七天退换"</div>
                            <div style="font-size: 12px; margin-top: 3px;">"7天无理由退换货保障"</div>
                        </div>
                    </div>
                    <div class="item flex align-center" style="gap: 12px;">
                        <span style="font-size: 28px; color: #ff3838; font-weight: bold;">"快"</span>
                        <div>
                            <div style="color: #fff; font-size: 15px; font-weight: bold;">"极速配送"</div>
                            <div style="font-size: 12px; margin-top: 3px;">"多仓智能直发，次日直达"</div>
                        </div>
                    </div>
                    <div class="item flex align-center" style="gap: 12px;">
                        <span style="font-size: 28px; color: #ff3838; font-weight: bold;">"专"</span>
                        <div>
                            <div style="color: #fff; font-size: 15px; font-weight: bold;">"专业客服"</div>
                            <div style="font-size: 12px; margin-top: 3px;">"7x24小时全天候贴心咨询"</div>
                        </div>
                    </div>
                </div>
            </div>

            // 底部帮助链接栏
            <div class="help-links" style="padding: 35px 0; border-bottom: 1px solid #3d3d3d;">
                <div class="container flex justify-between" style="max-width: 1200px; margin: 0 auto;">
                    <div>
                        <div style="color: #eee; font-size: 15px; font-weight: bold; margin-bottom: 15px;">"购物指南"</div>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <a href="#" style="color: #999; text-decoration: none;">"购物流程"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"会员注册与权益"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"常见问题解答"</a>
                        </div>
                    </div>
                    <div>
                        <div style="color: #eee; font-size: 15px; font-weight: bold; margin-bottom: 15px;">"配送与支付"</div>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <a href="#" style="color: #999; text-decoration: none;">"在线支付与银联"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"物流费用与规则"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"验货与签收指南"</a>
                        </div>
                    </div>
                    <div>
                        <div style="color: #eee; font-size: 15px; font-weight: bold; margin-bottom: 15px;">"售后保障"</div>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <a href="#" style="color: #999; text-decoration: none;">"退换货政策与流程"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"退款时效说明"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"发票与售后维修"</a>
                        </div>
                    </div>
                    <div>
                        <div style="color: #eee; font-size: 15px; font-weight: bold; margin-bottom: 15px;">"企业与商家"</div>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <a href="#" style="color: #999; text-decoration: none;">"大宗批发采购"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"商户招商入驻"</a>
                            <a href="#" style="color: #999; text-decoration: none;">"关于我们与联系方式"</a>
                        </div>
                    </div>
                </div>
            </div>

            // 版权与备案信息
            <div class="copyright-info" style="text-align: center; padding-top: 25px; font-size: 12px; color: #777;">
                <p>"Copyright © 2026 TigShop 商业商城系统 · 全栈 Rust (Leptos + Axum) 架构驱动 · 保留所有权利"</p>
                <p style="margin-top: 8px;">"增值电信业务经营许可证：浙B2-20260001 · 浙ICP备20260001号-1 · 浙公网安备 33010002000001号"</p>
            </div>
        </footer>
    }
}
