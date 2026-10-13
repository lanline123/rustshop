<template>
    <div class="phone-preview-wrap">
        <div class="phone" :class="`phone--${mode}`">
            <div v-if="showStatusBar" class="status-bar">
                <span class="time">1:21</span>
                <span class="signals">
                    <span class="sig-dot"></span>
                    <span class="sig-dot"></span>
                    <span class="sig-dot"></span>
                    <span class="sig-dot"></span>
                    <span class="sig-wifi">
                        <svg viewBox="0 0 16 12" width="14" height="11" aria-hidden="true">
                            <path
                                d="M8 2c2.5 0 4.8.9 6.6 2.4l-1 1.2A8.5 8.5 0 0 0 8 3.5 8.5 8.5 0 0 0 2.4 5.6l-1-1.2A10.5 10.5 0 0 1 8 2zm0 3c1.7 0 3.2.6 4.4 1.6L11.3 7.8A5.5 5.5 0 0 0 8 6.5a5.5 5.5 0 0 0-3.3 1.3L3.6 6.6A7 7 0 0 1 8 5zm0 3c.9 0 1.7.3 2.3.9L8 10.5 5.7 8.9A3.5 3.5 0 0 1 8 8z"
                                fill="currentColor"
                            />
                        </svg>
                    </span>
                    <span class="sig-battery"><span class="sig-battery-inner"></span></span>
                </span>
            </div>

            <template v-if="mode === 'menu'">
                <div class="nav-bar">
                    <span class="nav-back">
                        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                            <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                    <span class="nav-right">
                        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                            <circle cx="12" cy="8" r="3.5" fill="none" stroke="currentColor" stroke-width="1.5" />
                            <path d="M5 20c1-3.5 4-5.5 7-5.5s6 2 7 5.5" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                        </svg>
                    </span>
                </div>
            </template>
            <template v-else>
                <div v-if="showBanner" class="mp-banner">
                    <slot name="header">
                        <span class="mp-back">
                            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                                <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                            </svg>
                        </span>
                        <div class="mp-title">{{ title }}</div>
                    </slot>
                </div>
            </template>

            <div class="phone-body">
                <MenuPreview
                    v-if="mode === 'menu'"
                    ref="menuPreviewRef"
                    :tree="tree ?? { buttons: [] }"
                    :selected-id="selectedId ?? ''"
                    :error-id="errorId ?? ''"
                    @select="(id) => emit('select', id)"
                    @add-root="() => emit('add-root')"
                    @add-child="(parentId) => emit('add-child', parentId)"
                />
                <ChatPreview v-else :messages="messages ?? []" :nickname="nickname" :shop-name="shopName" :input-placeholder="inputPlaceholder" :shop-logo="shopLogo" />
            </div>

            <slot name="footer" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import MenuPreview from "./MenuPreview.vue";
import ChatPreview from "./ChatPreview.vue";
import type { WxMenuTree, PreviewMessage } from "@/types/setting/wechat.d";

const menuPreviewRef = ref<InstanceType<typeof MenuPreview> | null>(null);

/**
 * 透传 MenuPreview 暴露的 getNodeEl，供父组件（菜单编辑器）锚定浮动操作胶囊使用。
 * 仅 mode="menu" 时有效。
 */
function getNodeEl(id: string): HTMLElement | null {
    return menuPreviewRef.value?.getNodeEl(id) ?? null;
}

interface Props {
    mode: "menu" | "chat";
    title?: string;
    showStatusBar?: boolean;
    showBanner?: boolean;
    /** mode="menu" 时使用：菜单树 */
    tree?: WxMenuTree;
    /** mode="menu" 时使用：当前选中节点 id */
    selectedId?: string;
    /** mode="menu" 时使用：校验失败的节点 id，用于把选中框标红 */
    errorId?: string;
    /** mode="chat" 时使用：消息列表 */
    messages?: PreviewMessage[];
    /** mode="chat" 时使用：用户昵称（变量替换） */
    nickname?: string;
    /** mode="chat" 时使用：商城名（变量替换） */
    shopName?: string;
    /** mode="chat" 时使用：输入框占位文字 */
    inputPlaceholder?: string;
    /** mode="chat" 时使用：商城 Logo（公众号头像） */
    shopLogo?: string;
}

withDefaults(defineProps<Props>(), {
    title: "Tigshop",
    showStatusBar: true,
    showBanner: true,
    nickname: undefined,
    shopName: undefined,
    inputPlaceholder: undefined,
    shopLogo: ""
});

const emit = defineEmits<{
    /** mode="menu" 时由 MenuPreview 触发，用于父组件联动选中 */
    (e: "select", nodeId: string): void;
    /** mode="menu" 时由 MenuPreview 触发：新增一级菜单 */
    (e: "add-root"): void;
    /** mode="menu" 时由 MenuPreview 触发：新增二级子菜单 */
    (e: "add-child", parentId: string): void;
}>();

defineExpose({ getNodeEl });

defineOptions({ inheritAttrs: false });
</script>

<style lang="less" scoped>
.phone-preview-wrap {
    display: flex;
    justify-content: center;
    min-height: 0;
    padding: 4px 0;
}

.phone {
    width: 290px;
    border-radius: 8px;
    background: #ffffff;
    border: 1px solid #e8eaee;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.06);
    position: relative;

    &--menu {
        min-height: 520px;
        padding-bottom: 48px;
        background: #ffffff;
    }

    &--chat {
        height: 560px;
        padding-bottom: 0;
        display: flex;
        flex-direction: column;
        background: #f5f6f8;
    }
}

.status-bar {
    height: 26px;
    background: #ffffff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    font-size: 12px;
    color: #0a0a0a;
    font-weight: 600;
    flex: 0 0 auto;
    border-radius: 8px 8px 0 0;

    .time {
        font-variant-numeric: tabular-nums;
    }

    .signals {
        display: inline-flex;
        align-items: center;
        gap: 3px;
        color: #0a0a0a;
    }

    .sig-dot {
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: currentColor;
        display: inline-block;

        &:nth-child(1) {
            opacity: 1;
        }
        &:nth-child(2) {
            opacity: 1;
        }
        &:nth-child(3) {
            opacity: 1;
        }
        &:nth-child(4) {
            opacity: 0.5;
        }
    }

    .sig-wifi {
        margin-left: 2px;
        display: inline-flex;
        align-items: center;
    }

    .sig-battery {
        margin-left: 4px;
        width: 14px;
        height: 5px;
        border: 1px solid currentColor;
        border-radius: 2px;
        position: relative;
        padding: 1px;

        &::after {
            content: "";
            position: absolute;
            right: -3px;
            top: 1.5px;
            width: 2px;
            height: 4px;
            background: currentColor;
            border-radius: 0 1px 1px 0;
        }

        .sig-battery-inner {
            display: block;
            width: 100%;
            height: 100%;
            background: currentColor;
            border-radius: 1px;
        }
    }
}

.nav-bar {
    height: 42px;
    background: #ffffff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 14px;
    flex: 0 0 auto;

    .nav-back,
    .nav-right {
        color: #333;
        display: inline-flex;
        align-items: center;
    }
}

.mp-banner {
    height: 44px;
    background: #ffffff;
    display: flex;
    align-items: center;
    padding: 0 12px;
    gap: 8px;
    border-bottom: 1px solid #eef0f3;
    flex: 0 0 auto;

    .mp-back {
        color: #333;
        display: inline-flex;
        align-items: center;
        flex: 0 0 auto;
    }

    .mp-title {
        font-size: 14px;
        font-weight: 600;
        color: #222;
        flex: 1;
        text-align: center;
        padding-right: 26px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}

.phone-body {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}
</style>
