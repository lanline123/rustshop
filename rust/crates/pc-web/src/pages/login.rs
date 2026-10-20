use leptos::prelude::*;

#[component]
pub fn LoginPage() -> impl IntoView {
    view! {
        <div class="login-page" style="background: #f2f3f5; min-height: 80vh; display: flex; align-items: center; justify-content: center; padding: 50px 0;">
            <div class="container" style="display: flex; justify-content: center;">
                <div style="width: 880px; height: 500px; background: #fff; border-radius: 8px; box-shadow: 0 10px 30px rgba(0,0,0,0.08); overflow: hidden; display: grid; grid-template-columns: 380px 1fr;">
                    // 左侧海报区
                    <div style="background: linear-gradient(135deg, #ff4444 0%, #ff8855 100%); padding: 50px 40px; color: #fff; display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <div style="font-size: 28px; font-weight: 800; letter-spacing: 2px;">"TigShop"</div>
                            <div style="font-size: 14px; opacity: 0.9; margin-top: 4px;">"新一代高性能商业电商系统"</div>
                            <div style="margin-top: 50px; line-height: 28px; font-size: 14px; opacity: 0.95;">
                                <p>"• 100% 正品行货，售后无忧"</p>
                                <p>"• 极速物流顺丰直达，闪电发货"</p>
                                <p>"• 尊享会员专属折扣与大额优惠券"</p>
                            </div>
                        </div>
                        <div style="font-size: 12px; opacity: 0.7;">
                            "© 2026 TigShop Team · 全栈 Rust 驱动"
                        </div>
                    </div>

                    // 右侧登录面板
                    <div style="padding: 50px 50px; display: flex; flex-direction: column; justify-content: center;">
                        <div style="display: flex; border-bottom: 2px solid #eee; margin-bottom: 30px; gap: 30px;">
                            <span style="font-size: 18px; font-weight: bold; color: var(--general); padding-bottom: 10px; border-bottom: 2px solid var(--general); margin-bottom: -2px; cursor: pointer;">
                                "账号密码登录"
                            </span>
                            <span style="font-size: 18px; color: #999; padding-bottom: 10px; cursor: pointer;">
                                "验证码快捷登录"
                            </span>
                        </div>

                        <form action="/member" method="get" style="display: flex; flex-direction: column; gap: 18px;">
                            <div>
                                <input
                                    type="text"
                                    placeholder="请输入手机号 / 用户名 / 邮箱"
                                    style="width: 100%; height: 44px; border: 1px solid #ddd; border-radius: 4px; padding: 0 14px; box-sizing: border-box; font-size: 14px; outline: none;"
                                />
                            </div>
                            <div>
                                <input
                                    type="password"
                                    placeholder="请输入登录密码"
                                    style="width: 100%; height: 44px; border: 1px solid #ddd; border-radius: 4px; padding: 0 14px; box-sizing: border-box; font-size: 14px; outline: none;"
                                />
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 13px; color: #666;">
                                <label style="display: flex; align-items: center; gap: 6px; cursor: pointer;">
                                    <input type="checkbox" checked=true style="accent-color: var(--general);" />
                                    <span>"记住账号"</span>
                                </label>
                                <a href="#" style="color: #999;">"忘记密码？"</a>
                            </div>
                            <button
                                type="submit"
                                style="width: 100%; height: 46px; background: var(--general); color: #fff; font-size: 16px; font-weight: bold; border: none; border-radius: 4px; cursor: pointer; margin-top: 10px; box-shadow: 0 4px 14px rgba(255, 68, 68, 0.35);"
                            >
                                "立 即 登 录"
                            </button>
                        </form>

                        <div style="margin-top: 25px; text-align: center; font-size: 13px; color: #999;">
                            "还没有账号？"
                            <a href="/login" style="color: var(--general); font-weight: bold; margin-left: 4px;">"免费注册"</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}
