<template>
    <!-- 根节点带 module-ad-con：复用全局 hover/选中边框(.module-ad-con:before) -->
    <div class="module-ad-con dec-mobile-login" :style="colorVars">
        <template v-if="overseas">
            <div class="overseas-login-page" :style="warpStyle">
                <div class="ol-tabs">
                    <span class="ol-tab ol-tab-active">{{ $t("账号登录") }}</span>
                    <span class="ol-tab">{{ $t("验证码登录") }}</span>
                </div>
                <div class="ol-form">
                    <div class="ol-item">
                        <span>{{ $t("用户名/手机号") }}</span>
                    </div>
                    <div class="ol-item ol-password">
                        <span>{{ $t("请输入密码") }}</span>
                        <span class="ml-eye"></span>
                    </div>
                </div>
                <div class="ol-btn">{{ $t("登录") }}</div>
                <div class="ol-other-row">
                    <span>{{ $t("忘记密码") }}</span>
                    <span
                        >{{ $t("新用户") }}<b>{{ $t("注册") }}</b></span
                    >
                </div>
                <div class="ol-footer">
                    <div class="ol-divider">
                        <span>{{ $t("其他登录方式") }}</span>
                    </div>
                    <div class="ol-socials">
                        <div>
                            <img :src="googleIcon" alt="Google" /><span>{{ $t("Google登录") }}</span>
                        </div>
                        <div>
                            <img :src="facebookIcon" alt="Facebook" /><span>{{ $t("Facebook登录") }}</span>
                        </div>
                        <div>
                            <img class="ol-twitter-icon" :src="twitterIcon" alt="Twitter/X" /><span>{{ $t("Twitter/X登录") }}</span>
                        </div>
                    </div>
                    <div class="ol-agreement">
                        <span class="ol-checkbox"></span>{{ $t("登录即为同意") }}<b>{{ $t("《商城用户服务协议》") }}</b>
                    </div>
                </div>
            </div>
        </template>
        <template v-else>
            <!-- .wechat-login-warp 内的 .plr-40：左右/顶部间距只作用在标题+表单，不影响底部 -->
            <div class="ml-warp" :style="warpStyle">
                <!-- 标题（对齐 .h1_tit：padding 20px / padding-left 0 / 18px 加粗） -->
                <div class="ml-title">{{ $t("账号登录") }}</div>

                <!-- 表单（账号登录：用户名 + 密码） -->
                <div class="ml-form">
                    <div class="ml-item">
                        <span class="ml-text ml-placeholder">{{ $t("用户名/手机号") }}</span>
                    </div>
                    <div class="ml-item ml-item-pwd">
                        <span class="ml-text ml-placeholder">{{ $t("请输入密码") }}</span>
                        <!-- 密码显隐（对齐 .password_show_hide） -->
                        <span class="ml-eye">
                            <svg viewBox="0 0 1024 1024" width="20" height="20">
                                <path
                                    fill="#666"
                                    d="M512 256C264 256 64 512 64 512s200 256 448 256 448-256 448-256-200-256-448-256zm0 416a160 160 0 1 1 0-320 160 160 0 0 1 0 320zm0-256a96 96 0 1 0 0 192 96 96 0 0 0 0-192z"
                                />
                            </svg>
                        </span>
                    </div>

                    <!-- 确认按钮（对齐 tig-button circle 形：胶囊圆角，色取 buttonColor） -->
                    <div class="ml-btn">{{ $t("确 认") }}</div>

                    <!-- 忘记密码（对齐 .desc-box.flex：右对齐） -->
                    <div class="ml-forgot">{{ $t("忘记密码") }}?</div>

                    <!-- 立即注册（对齐 .register + .special-color） -->
                    <div class="ml-register">
                        {{ $t("没有账号") }}？<span class="ml-link">{{ $t("立即注册") }}</span>
                    </div>
                </div>
            </div>

            <!-- 底部：其他登录方式 + 协议（对齐 .bottom-box .btns-box，吸附底部） -->
            <div class="ml-bottom">
                <div class="ml-other-tit">
                    <span class="ml-line ml-line1"></span>
                    <span class="ml-other-text">{{ $t("其他登录方式") }}</span>
                    <span class="ml-line ml-line2"></span>
                </div>
                <div class="ml-quick">
                    <span class="ml-quick-ico"></span>
                    <span class="ml-quick-text">{{ $t("手机号登录") }}</span>
                </div>
                <div class="ml-rule">
                    <span class="ml-checkbox"></span>
                    <span class="ml-rule-text"
                        >{{ $t("我已阅读并同意") }}<span class="ml-link">《{{ $t("服务协议") }}》</span>{{ $t("和")
                        }}<span class="ml-link">《{{ $t("隐私协议") }}》</span></span
                    >
                </div>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { isOverseas } from "@/utils/version";
import googleIcon from "@/assets/decorate/mobileLogin/google.png";
import facebookIcon from "@/assets/decorate/mobileLogin/facebook.png";
import twitterIcon from "@/assets/decorate/mobileLogin/twitter.svg";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { mobileLoginDefaults } from "./types";
import type { MobileLoginModule } from "./types";

const props = defineProps<{ module: MobileLoginModule }>();
const module = props.module;
const overseas = isOverseas();
// mergeDefaultModule 已把空值回填为默认（左右 20 / 顶部 0），此处按 px 直接使用
mergeDefaultModule(module, mobileLoginDefaults);

// 颜色变量挂在根节点，子元素继承（占位符空值继承输入框文字色 #252525，与 uniapp 一致）
const colorVars = computed(() => {
    const m = module || {};
    return {
        "--login-title": m.titleColor || "#333333",
        "--login-subtext": m.subTextColor || "#999999",
        "--login-input": m.inputTextColor || "#252525",
        "--login-placeholder": m.placeholderColor || "#252525",
        "--login-link": m.linkColor || "var(--tig-primary, #e93b3d)",
        "--login-button": m.buttonColor || "var(--tig-primary, #e93b3d)",
        "--login-divider": m.dividerColor || "#eeeeee"
    } as Record<string, string>;
});

// 左右/顶部间距只作用于 .plr-40（标题+表单包裹层），对齐 uniapp
// 顶部只设 padding-top（不动底部），左右设 padding-left/right；单位就是 px，直接用
const warpStyle = computed(() => {
    const px = module.contentPaddingX ?? 20;
    const pt = module.contentPaddingTop ?? 0;
    return {
        paddingTop: `${pt}px`,
        paddingLeft: `${px}px`,
        paddingRight: `${px}px`
    } as Record<string, string>;
});
</script>

<style lang="less" scoped>
.dec-mobile-login {
    box-sizing: border-box;
    // 固定视口高度 + 相对定位：对齐 uniapp 满屏页；底部区块绝对定位钉在底部，
    // 顶部间距只把标题+表单往下推，不会撑高容器把底部挤下去（对齐 uniapp .bottom-box fixed）
    height: 640px;
    background-color: #fff;
    position: relative;
    overflow: hidden;

    // 全局选中框默认向左右各扩 2px；登录页需要裁剪内部内容，需将边框收回容器内，避免被 overflow 隐藏
    &::before {
        left: 0 !important;
        right: 0 !important;
    }

    .overseas-login-page {
        box-sizing: border-box;
        height: 100%;
        color: var(--login-input);

        .ol-tabs {
            display: flex;
            align-items: flex-end;
            gap: 42px;
            padding-top: 20px;
            margin-bottom: 38px;
            color: var(--login-subtext);
            font-size: 17px;
        }

        .ol-tab-active {
            color: var(--login-title);
            font-size: 22px;
            font-weight: bold;
        }

        .ol-form {
            padding: 20px 0;
            margin-bottom: 20px;
        }

        .ol-item {
            display: flex;
            align-items: center;
            box-sizing: content-box;
            width: 100%;
            height: 44px;
            padding-top: 25px;
            border-bottom: 1px solid var(--login-divider);
            color: var(--login-placeholder);
            font-size: 14px;

            &:first-child {
                padding-top: 0;
            }
        }

        .ol-password {
            justify-content: space-between;

            .ml-eye {
                color: #333;
                font-family: "mobile-login-iconfont";
                font-size: 18px;

                &::before {
                    content: "\e619";
                }
            }
        }

        .ol-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 40px;
            color: #fff;
            font-size: 14px;
            border-radius: 100px;
            background: var(--login-button);
        }

        .ol-other-row {
            display: flex;
            justify-content: space-between;
            padding: 15px 0;
            // Uniapp 跨境端 other-row 使用固定正文色，不跟随输入框文字颜色
            color: #333;
            font-size: 14px;

            b {
                color: var(--login-link);
                font-weight: normal;
            }
        }

        .ol-footer {
            position: absolute;
            right: 0;
            bottom: 26px;
            left: 0;
            text-align: center;
        }

        .ol-divider {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 0 35px;
            color: var(--login-subtext);
            font-size: 14px;

            &::before,
            &::after {
                content: "";
                flex: 1;
                border-top: 1px solid var(--login-divider);
            }
        }

        .ol-socials {
            display: flex;
            align-items: center;
            justify-content: center;
            column-gap: 48px;
            // 对齐 Uniapp 跨境登录 .login-other 的 margin-bottom: 180rpx（90px）
            margin: 12px 0 90px;

            > div {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 8px;
                color: var(--login-subtext);
                font-size: 10px;
            }

            img {
                width: 45px;
                height: 45px;
                object-fit: contain;
            }

            .ol-twitter-icon {
                box-sizing: border-box;
                padding: 9px;
                border: 1px solid var(--login-divider);
                border-radius: 50%;
            }
        }

        .ol-agreement {
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--login-subtext);
            font-size: 10px;

            b {
                color: var(--login-link);
                font-weight: normal;
            }
        }

        .ol-checkbox {
            box-sizing: border-box;
            width: 14px;
            height: 14px;
            margin-right: 4px;
            border: 1px solid #ccc;
            border-radius: 50%;
        }
    }

    // 标题 .h1_tit: padding:40rpx(20px); padding-left:0; font-size:36rpx(18px); bold
    .ml-title {
        color: var(--login-title);
        font-size: 18px;
        font-weight: bold;
        padding: 20px;
        padding-left: 0;
    }

    .ml-form {
        // .item: margin-bottom:70rpx(35px); .text height:88rpx(44px); border-bottom 1px solid divider
        .ml-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: relative;
            height: 44px;
            margin-bottom: 35px;
            border-bottom: 1px solid var(--login-divider);
        }
        // .text: font-size:28rpx(14px); color var(--login-input)
        .ml-text {
            font-size: 14px;
            color: var(--login-input);
        }
        // .placeholder: font-size:28rpx(14px); font-weight:normal; color var(--login-placeholder)
        .ml-placeholder {
            font-weight: normal;
            color: var(--login-placeholder);
        }
        // .password_show_hide: right:8rpx(4px); top:10rpx(5px); font-size:40rpx(20px); color #666
        .ml-item-pwd .ml-eye {
            position: absolute;
            right: 4px;
            top: 5px;
            display: inline-flex;
        }
        // .btn2-css3 外层高 45px；内部 tig-button(normal) 最终高 40px、字号 14px，剩余 5px 保留为下方占位
        .ml-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 40px;
            margin-bottom: 5px;
            box-sizing: border-box;
            color: #fff;
            font-size: 14px;
            font-weight: normal;
            line-height: 1;
            border-radius: 100px;
            background-color: var(--login-button);
        }
        // .desc-box.flex: 右对齐; font-size:26rpx(13px); color subtext; margin-top:30rpx(15px)
        .ml-forgot {
            text-align: right;
            font-size: 13px;
            color: var(--login-subtext);
            margin-top: 15px;
        }
        // .register: 居中; padding:30rpx(15px)
        .ml-register {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 15px;
            font-size: 14px;
            color: var(--login-subtext);
        }
    }

    // .btns-box: position:absolute; bottom:max(60rpx,safe)≈30px; 钉在底部, 独立于表单高度
    .ml-bottom {
        position: absolute;
        left: 0;
        bottom: 30px;
        width: 100%;
        text-align: center;
        color: var(--login-subtext);
        font-size: 12px;

        // .login-dtit: width:200rpx(100px); margin:0 auto 20rpx(10px)
        .ml-other-tit {
            position: relative;
            width: 100px;
            height: 30px;
            line-height: 30px;
            margin: 0 auto 10px;

            .ml-other-text {
                position: relative;
                z-index: 10;
                font-size: 12px;
            }
            // .line: border-bottom 1px solid divider; height:30rpx(15px); width:250rpx(125px); top:0; left/right:-250rpx(-125px)
            .ml-line {
                position: absolute;
                top: 0;
                width: 125px;
                height: 15px;
                border-bottom: 1px solid var(--login-divider);
            }
            .ml-line1 {
                left: -125px;
            }
            .ml-line2 {
                right: -125px;
            }
        }

        // .quick-type .go-link: width:90rpx(45px); padding-top:90rpx→改用 height 45px; margin:0 60rpx(30px); icon-mobile 背景
        .ml-quick {
            .ml-quick-ico {
                display: inline-block;
                width: 45px;
                height: 45px;
                margin: 0 30px;
                background-repeat: no-repeat;
                background-size: 100% auto;
                background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMYAAADGCAYAAACJm/9dAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyhpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTQ1IDc5LjE2MzQ5OSwgMjAxOC8wOC8xMy0xNjo0MDoyMiAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTkgKE1hY2ludG9zaCkiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6N0ZDMTJBRjk1NzY2MTFFREExN0JDOEI5MDg3NjNEOTgiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6N0ZDMTJBRkE1NzY2MTFFREExN0JDOEI5MDg3NjNEOTgiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo3RkMxMkFGNzU3NjYxMUVEQTE3QkM4QjkwODc2M0Q5OCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo3RkMxMkFGODU3NjYxMUVEQTE3QkM4QjkwODc2M0Q5OCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pk6PpMYAABEwSURBVHja7J0LkFdlGcYflv8CK0lIxB0VkKvJAiWymQMIg3hLjMkGwwyycUora5IsI2ssMzWnURuyGJDwUhQOlCSSiobJSlyE4iLILS7LchNQ5LLg9r6dl1oX9+z/fr5zvuc38wwjAvud97zPdznn+97TpLa2FoSQ91PCEBByOqnTfmd6E0YlP3QWdRV1EXUSdRC1E7UVtRG1FrUStRSViZrVuR8nRMdFR0SHRYdEB0T7RXtFu0W7RDtF20XbRDsY8jwwobYBY5BM0WTvJ+oj6i3qJeoh6i4qzfLfLDW1NCM1Ro1ok2ijaL3oDdE60RozEcl5xCCNoSYYJBogKhf1N3NESamZUnVFnd9XU6wSrRS9LlpuZiE0Rs50FFWIhogGm8piNJqNNMGmZktMlaLFoireYhojXXqKhoouMXVLyHWV2XUNtf/eLFpkelm0gbeexqhPF+tZR4iG2+I56XQzfcEW7wtFL4iet0W9tzQ57T2Gf0+lRptGifqyf/gva0ULRPNN/uD5Uyl9jHqV6EoTeT99Td8QzTM9g+CxMEeMBKJPkcaIrhENZP5nxArRXNEcBE+5OGIkAH2iNNbUjTmeFQNNN4pmmyo5YsSTi0TXmbowt/OKLs5nmV7jiBEPzhddLxrHEaJgaEfzLdG1oqdET4pWJ+XikmaM9qIbRONtPUEKj3Y830PwEONx0UxRNY3hDuNs/nsZczUSyk36LmiGjSI0RoR8QjTR1Jz5GTnaMQ1DsGNgmmgpjVFcWohuMnHa5BbaQX1F9EnRVNNRGqPw6Ka+m23qRNyeXj1so/qjCDYtxoI4nuBTQ0yhKWLFjXbPbuaIkX90x+stoltFTZlrsRw9folgq4n+uoHGyJ3LRV9HsNmPxBft0HT/lR6oekj0LI2RPbqIuw3BkVGSDLSD06O/59oUi8bIgI8geLOqasFcShza0T2I4A26/rqPxmgcnYfeLprA/Ek02uHpW3M9Pnw/gnMgNEYDfEo0SXQ188YbtAPUaij3iV6hMU5H99vcYeYgfqEd4VmiexEcjKIxDN0afieCcjTET7RDvAdBPa1ZNEawI3YygncVxG+0Y/wxgm0lM6NqhAtvvnV+eTdNQerQ03IisocvKQdM8SMExQkIqcs5lhvKdJ9GDJ0+3UVTkBC6Wo7c4IsxrrM1xTm89ySNkWOy5UyijaGPZO/kmoJkuOa4E0WsAVZsY+jjuDvAR7Ikc/qjiO+4imkM3eYxCXx5R3LrWCehCKVUi/VUSjcE6t6neGzzaN4N6DwGaH8hcFYvoNXZQGlLidYZyUivmneA46JDW4D964BdlcCOp4GTe+LQes2hvZZPBdt4WKyCaz9BsFnMbdp/Fuj3JaDrpUDTUnhFzWFgywJg9RTgrb/GocX32Lojv1jBtWIYQ89T6LZid7eOtx4JDP6hjBIXc7ICyYdN84ClEo/Dy1xuqBZX0CMJUwphjEKvMfTk3W1Om+Jj9wGf/gtN8f9JBND9KuDahfLrN11uaAvLrcvjtvjWR2x6HNXNk3dNWwHDpGe88Hb/pk3pUHomMFQG+gt/43Ire1mO9YyTMbRwgZtntEtkET18LtDtChqg0RH1JqDiCZdbONpyLRbG0DIptzobyotnygJ7GJM+XfpcD/T/ucstvBV5Ls1TCGNU2ILbzRI3Pe8AzvsMkz1TBsl0vsPnXG1dU8u5CleN0cKc62bJzLILgMHfZZJntSaXVLn43mBt5ibllnt5edCT7xd8WkfW3QqB5d8BmkVwY48dBFY8BOxdDtSezDFBS4H2Q4ABXw1eOhaTVucCvScBa77v6h3W3NMi0o+4ZIxPmDHcpEVvoNdno/nZf/smsD2PRwr2PA3UHAI+eXfxr+UCucVrJ4vBa12905qDlcixyno+p1IT4XLV8e7SmTRtFsEPlgSqmp3/f7b61WjieEZ7oMuXXJ70lVsuOrHGGJePxhSUs4dHNTkHWhfg5eFZ50cXy64jXV8RTbScjHQq1d7mdu5+tKWJjBTtBkX384c+DLz6A+DAMhlATuTYlem1DAMumhzd9XS40HVjNLecfBFZfvYsH8bQY4duf96rVUVE0yjjwz2Ay59AYjjz7OAl6XvvutzKyyw3H4hiKqXj+Xjnb2RZZ5A8UiL9aYtY1NgebzladGNcjzh85qvYjzV9oPTDcWhlueVoUY1xUa4LnOKtMUqYyHmPaWw2Xo6zXC2aMbRqAz8uT1ynG7KoMJKtMYagyOVMCMmxEx9SDGOMRfDBD0LiQBfL2YIaozzTH0KIA4xFBg+KsjHGGK4tSEzXGmPS/cOZvuDTWqLXJDp8e/8FLL/fz9QZ9Rj+u4UluWjuThNty7cxrhINTHRyHDsA7Pitp53qY0m/wIGWw41WFsl0KnUlCIk3aeVwJsYYTWOQhBhjdL6NQUgSyJsx9DnwKMaTJIRRaOQ9XLrG0JMpfRlPkhD6Wk7nbIwRjCVJGCNyNYaWPxzOOJKEMRwhpT3TMcZQEU/6kKTR2XI7a2NcwhiShHJJtsboSGOQhBujYzbG0Fqg3DBIkko3NFDvtjFjDGHsSMIZko0xBjNuJOEMztQY/WgM4okx+mViDC3dV8a4kYRTZrmetjEGMGbEEwZkYoxyxot4Qnm6xmgn6s94EU/obznfqDH61f+DhCSYdvUX4A0Zow9jRTyjTzrG6M04Ec/onY4xejFOxDN6pWOMHowT8YwejRlD96l3Z5yIZ3RHnXNHH2QMrTZYyjgRzyi13G/QGKxiTnylS5gxOjE+xFM6hRmjA+NDPKVDmDH4xpv4SrswY7RlfIintA0zRhvGh3hKmzBjtGZ8iKe0DjNGK8aHeEqrMGO0ZHyIp7QMMwbPeRNfKQszRjPGh3hKszBjpBgf4impMGMQ4j0fZIwTDAvxlBNhxjjO+BBPOR5mjCOMD/GUI2HGOMz4EE85HGaMQ4wP8ZRDYcY4wPgQTzkQZoz9jA/xlP1hxtjL+BBP2RtmjN2MD/GU3WHG2MX4EE/ZFWaMnYwP8ZSdYcbYzvgQT9keZoxtohrGiHhGjeV+g8bYIdrEOBHP2GS536AxlI2ME/GM9+V8Q8ZYzzgRz1ifjjHeYJyIZ7yRjjHWMU7EM9alY4w14Btw4g+7LecbNYb+wVWMF/GEVfUHgrBiCCsZL+IJp+V6mDFeZ7yIJ7yeiTGWg+e/SfI5YrmetjF0MbKEcSMJZ0n9hXdjxgCNQTwxBjI1RiXjRhJOZTbGWCzazNiRhLLZcjxjY1SJFjF+JKEsshzP2BigMUjCjYFsjfEy6uxTJyQh7LDcztoYG0QLGUeSMBZabmdtDOUFxpEkjNCcTtcYz4vWMpYkIay1nM7ZGFo9YQHjSRLCAjRSDSeTT43NZzxJQmg0lzM1xjzGlMScefk2BmgMkhBjIN/GeEa0grElMWWF5XDejaGV2uYyviSmzEWdaoNhZPOx+zmiG0XdEhm6th8DLnvV07xpkuSL22y5i0IZQ8/HzhZ9O5Hha94a6FTBvjV5zEYGdQxKcvghrIpO4sJ2y1kU2hh6uGMW401iwixkeOguleMPuzaxa41icfQtYP3vgR0vAe9WSVclt+TM7kDXkcB5Y4CmzRmj3NcWGXfiuRjjNdFTou8x9lnypqwFK78G1NSblR54Edg2VWbEHweG/hpoP4ixyp6nLFczoiTHH/okWJgtO1Y/Biy69nRT1OXwMmD+pUAVa1JkyUrLURTbGKtFjzsfnveOu9We6uXAPyam2faDwEtfBI6/7VhMY1Fy7HHL0aIbQ5kpes7p8LiWVMsfAGprM1iHrAXWznQspvtdN8VzlpuIyhjVohmiY86G6G2HPvdxVBKq+neZ/71/O7S5ueadwKzucsxysjpKY5xa4ExzNkxH/ina60ZbDm7JbLT434J8mTvx3O/8d4WmWU4iamOcaoy7C/GdjmzzOJHl3PxklRjqPTeuocrpLTMr89FJ59MYS0VTnQ3XZkf2Pp7x0ez+Xlk50KTEgQuQ0W7TbJeNMdVy0RljnGrUDCfDtX26rDW2Rt+O1udJkl+Q+d/reKkbcdwho8XBl101xYx8dc75NsZR0aNOTql0Xr/ioejbob3++bdm/vf63ODGaLHiAZenUI9aDjpnDEVrgU7RWbFzodv4oMyPHahT3e+LQNtPZ2CKyUC7AdG3e/0fgD1zXDTFScu5xfn6Bws1aVXnPuJkv/LKN4Bjb0XbhqbNgJEy4n/k8sb/7Lm3ABdNjj5uBzcDSya5Olo8YjkH142h/BIuVhZ5ZwnwokxlTkb8NrxMFuFX/BG4QKYmqfYf8P/PByqeAIbJ9K+kNNq2akfywgSgZquLpphvuZbfGW9t/Wfq0/N6iku7xF+IejkXzq5fBoY/7Mbu1RMyLa5eCrxbLXdEd9d2DaZOLjyFUlM89wVg3zMummK96DbRs3n7FycEfkgVuOHa4HNFMrlHC6dCuu030rp9wIhfBb13lKQkNJ0/5V7aHdwIPH8jcOjvLpriqHW6zxbiHy9GlzTFjOEee54G5gwFtri91avo6IvEdU8CcytcNQUsp6YU6h8vKeJFTHcyvLrnZ+FomS6Ml+nMMhpi20LgT1cCiz8v67A9rrZ0eqE720KvMerSV/Qz0dVOJ4c+Rj1vrKxBRgAf6uyHIXTKtHUB8ObvXX55d4o/i76DQhUZtzVGMY2h6ET6p/ar+7T8ONBmoCyGzwFKzwSatUyGEY4dAo6L3t4C7F8CHIlNIftXRN+1X5EkYygyTuMeUX9O5kkGrEJwjLqwZWLNGFE8D9QL+wlCvmZDSD02WM4UrXZyVA/KtWrD3aKtvOekEbZarhS1XFOUb5D02OGPkGYtUeIl2yxHin6uN+pXq/rY7S6OHKSBkeIuRPSY34WTL3rhk7nmIPXWFJMR4buvlCOB0KFSD7DfCT6t8p1VttCOtARsyqGAaCAOi+5AXN5zkHyj7yfuhQNf7ko5FhgNyEGRbvy/mnniFfpG+z4U8uVdjI1xqtfYJ9J6NxOYL16ga4n74dC35FOOBkoDdLuoSvQtuLZlneQL3Tr+oGmfSw1LORy0fbYY16rHehilF/MoUeghIz1PMcXFxqViEEAN3BbR10WjmU+JQI+jasmWZ11tYComgdQAvim6RaS1Z5oyt2KJVvPQwgV6Rtvp91apGAV1g02pdP3xFVE58yxWrLTR/9E4NDYVwwBrYPUl0M0IPqtM3GeG3bfFcWlwKqaB1gCvQFCj9CaOHk6PElNNR+PU8FSMg37U5qtaWnCiiV9ydAPd3jPNtDSOF5BKwE1YalpkU6vLmJeR8pxNnZ6K80WkEnRD9Ea8KNLqx+M5vYpk2qTfvNMNodVxv5hUwm6O3hAtx62Pd68XjQO/Q15oNlunpF9HXZ2Ui0ol9GbpDdK35n8SXWfqwhzOK7ojYZbptaRdXCrhN+810x9EY00cQXIfIWabKpN6kSlPbmalSefAY0TXiAYyxzNCH4/r99rmwOVvLdIYWS8QT3288CoENa6uZM6HMs+k5c69KVwRRcE11xhtGoWgjCgJtt0sQLDZb75XV16kzwDEgVM3XxfnI0UjRMNFnT2Lww7RQtELoudtce0tHDE+mJ6ioaJLTEldsOtCepFJqzmzUgtHjFA2mHSPT0dRhWiIaLCpLKbXdUS0xKQPI3TPWRVvNxff2aCJ87RJ6ScaJNLPqOrbdS33087Rtu9GsBNZHzi8LlouWsNbSmMUgjWmx+2/25lZ+oh6IziC20PUXVSsr0rWiDaJNiI4MvqGaJ21czdvGY0RVa+seqne7+vivast6juJOpiJ2oraiFqLWola2tSsWZ37cUJ03KY+WmvrkOiAaD+C6in683aJdtoieZstnknBFt+EECdq1xJCYxBCYxASU/4jwABGanUujIXDQAAAAABJRU5ErkJggg==");
            }
            .ml-quick-text {
                display: block;
                margin-top: 4px;
                font-size: 12px;
                color: var(--login-subtext);
            }
        }

        // .rule-text: padding-top:58rpx(29px); 居中
        .ml-rule {
            display: flex;
            align-items: center;
            justify-content: center;
            padding-top: 29px;

            .ml-checkbox {
                flex-shrink: 0;
                width: 14px;
                height: 14px;
                border: 1px solid #ccc;
                border-radius: 50%;
                margin-right: 4px;
            }
            .ml-rule-text {
                color: var(--login-subtext);
            }
        }
    }

    // .special-color: color var(--login-link, var(--general))
    .ml-link {
        color: var(--login-link);
    }
}

@font-face {
    font-family: "mobile-login-iconfont";
    src: url("https://at.alicdn.com/t/c/font_2868362_tjezph784er.woff2?t=1754532292999") format("woff2");
}
</style>
