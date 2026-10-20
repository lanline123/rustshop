use leptos::prelude::*;

#[component]
pub fn LoginPage() -> impl IntoView {
    view! {
        <div class="login-page" style="background: #f6f6f6; min-height: 80vh; display: flex; align-items: center; justify-content: center; padding: 40px 0;">
            <div class="login-box flex" style="background: #fff; width: 880px; height: 500px; border-radius: 10px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.08);">
                // 左侧品牌形象插画区
                <div class="left-poster flex flex-column justify-center align-center" style="width: 420px; background: linear-gradient(135deg, #ff4d4d 0%, #ff1a1a 100%); color: #fff; padding: 40px; box-sizing: border-box; text-align: center;">
                    <img src="/static/images/logo_blue.png" alt="Logo" style="height: 40px; filter: brightness(0) invert(1); margin-bottom: 25px;"/>
                    <h2 style="font-size: 26px; font-weight: 900; margin: 0 0 15px 0;">"欢迎登录 TigShop"</h2>
                    <p style="font-size: 14px; opacity: 0.9; line-height: 1.6; max-width: 280px; margin: 0 auto;">
                        "全栈 Rust 高性能企业级商城 · 极速同构加载 · 安全可靠"
                    </p>
                </div>

                // 右侧表单交互区
                <div class="right-form flex flex-column justify-center" style="flex: 1; padding: 50px; box-sizing: border-box;">
                    <div class="flex" style="gap: 25px; margin-bottom: 30px; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;">
                        <span style="font-size: 18px; font-weight: bold; color: #ff3838; border-bottom: 2px solid #ff3838; padding-bottom: 10px; margin-bottom: -12px; cursor: pointer;">
                            "密码登录"
                        </span>
                        <span style="font-size: 18px; color: #888; cursor: pointer;">
                            "手机验证码登录"
                        </span>
                    </div>

                    // 表单输入
                    <form action="/login" method="post" style="display: flex; flex-direction: column; gap: 20px;">
                        <div>
                            <input
                                type="text"
                                name="username"
                                placeholder="请输入手机号 / 会员用户名 / 邮箱"
                                style="width: 100%; padding: 12px 15px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; outline: none; box-sizing: border-box;"
                            />
                        </div>
                        <div>
                            <input
                                type="password"
                                name="password"
                                placeholder="请输入登录密码"
                                style="width: 100%; padding: 12px 15px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; outline: none; box-sizing: border-box;"
                            />
                        </div>

                        <div class="flex justify-between align-center" style="font-size: 12px; color: #666;">
                            <label class="flex align-center" style="cursor: pointer;">
                                <input type="checkbox" style="margin-right: 6px;"/>
                                <span>"记住账号"</span>
                            </label>
                            <a href="#" style="color: #888; text-decoration: none;">"忘记密码？"</a>
                        </div>

                        <button
                            type="submit"
                            style="background: #ff3838; color: #fff; border: none; padding: 14px; border-radius: 4px; font-size: 16px; font-weight: bold; cursor: pointer; margin-top: 10px; box-shadow: 0 4px 12px rgba(255, 56, 56, 0.35);"
                        >
                            "登 录"
                        </button>
                    </form>

                    <div style="margin-top: 25px; text-align: center; font-size: 13px; color: #888;">
                        "还没有账号？ "
                        <a href="/login" style="color: #ff3838; font-weight: bold; text-decoration: none;">"免费注册"</a>
                    </div>
                </div>
            </div>
        </div>
    }
}
