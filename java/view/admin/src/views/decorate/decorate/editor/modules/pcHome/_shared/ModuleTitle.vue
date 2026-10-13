<template>
    <div v-if="title?.showTitle" class="dec-module-title" :class="titleClasses" :style="titleContainerStyle">
        <!-- 左侧：图标 + 标题 + 描述 -->
        <div class="dec-module-title__left">
            <img v-if="titleIcon" class="dec-module-title__icon" :src="titleIcon" alt="" />
            <div class="dec-module-title__text" :style="titleTextStyle">
                {{ title.titleText }}
            </div>
            <div v-if="title.descText" class="dec-module-title__desc" :style="descTextStyle">
                {{ title.descText }}
            </div>
        </div>

        <!-- 右侧：更多链接 -->
        <template v-if="title.showMore">
            <slot name="more">
                <div class="dec-module-title__right">
                    <a class="dec-module-title__more" :href="title.moreLink?.link || 'javascript:;'" :style="moreTextStyle">
                        <span>{{ title.moreLink?.title || "更多" }}</span>
                        <el-icon :size="12"><ArrowRight /></el-icon>
                    </a>
                </div>
            </slot>
        </template>
    </div>
</template>

<script setup lang="ts">
/**
 * 模块标题组件 - 预览（移植自 Vort-Admin）
 * 支持：标题文字、描述、图标、更多链接、多种样式、对齐方式
 */
import { computed } from "vue";
import { ArrowRight } from "@element-plus/icons-vue";
import { productImage } from "@/utils/format";
import type { ModuleTitle } from "./types";

const props = defineProps<{
    title?: ModuleTitle;
}>();

const titleIcon = computed(() => productImage(props.title?.titleBackgroundPic));

// 标题类名（样式 + 对齐方式）
const titleClasses = computed(() => {
    const classes: string[] = [];
    const title = props.title;

    const style = title?.titleStyle ?? 1;
    classes.push(`dec-module-title--style-${style}`);

    const align = title?.titleAlign ?? 1;
    classes.push(`dec-module-title--align-${align}`);

    return classes;
});

// 标题容器内联样式
const titleContainerStyle = computed(() => {
    const title = props.title;
    if (!title) return {};

    const styles: Record<string, string> = {};

    if (title.titleBackground2 && title.titleBackground) {
        styles.background = `linear-gradient(to right, ${title.titleBackground}, ${title.titleBackground2})`;
    } else if (title.titleBackground) {
        styles.backgroundColor = title.titleBackground;
    }

    if (title.titleRadius) {
        styles.borderRadius = `${title.titleRadius}px ${title.titleRadius}px 0 0`;
    }

    return styles;
});

const titleTextStyle = computed(() => {
    const title = props.title;
    if (!title) return {};
    return {
        color: title.titleColor || "#2a3145"
    };
});

const descTextStyle = computed(() => {
    const title = props.title;
    if (!title) return {};
    return {
        color: title.descColor || "#aaaaaa"
    };
});

const moreTextStyle = computed(() => {
    const title = props.title;
    if (!title) return {};
    return {
        color: title.moreColor || "#969799"
    };
});
</script>

<style lang="less" scoped>
.dec-module-title {
    display: flex;
    justify-content: space-between;
    line-height: 30px;
    align-items: center;
    padding: 5px 10px 3px;
    position: relative;

    // ========== 左侧内容 ==========
    &__left {
        display: flex;
        flex-wrap: nowrap;
        align-items: center;
    }

    &__icon {
        width: 30px;
        height: 30px;
        margin-right: 5px;
        object-fit: contain;
        flex-shrink: 0;
    }

    &__text {
        font-weight: 700;
        color: #2a3145;
        font-size: 16px;
        position: relative;
    }

    &__desc {
        color: #aaa;
        font-size: 12px;
        margin-left: 5px;
        position: relative;
        top: 1px;
    }

    // ========== 右侧内容 ==========
    &__right {
        display: flex;
        font-size: 12px;
    }

    &__more {
        color: #969799;
        display: flex;
        align-items: center;
        text-decoration: none;
        line-height: 10px;

        i {
            margin-left: 2px;
            font-size: 12px;
        }
    }

    // ========== 对齐方式 ==========
    // 使用 &.dec-module-title--align-X 提升 specificity，避免被全局样式覆盖

    // 左对齐（默认）
    &.dec-module-title--align-1 {
        justify-content: space-between;
    }

    // 居中
    &.dec-module-title--align-2 {
        justify-content: center;

        .dec-module-title__left {
            flex-direction: column;
            align-items: center;
            text-align: center;
        }

        .dec-module-title__desc {
            margin-left: 0;
            margin-top: 2px;
        }

        .dec-module-title__right {
            position: absolute;
            right: 10px;
        }
    }

    // 右对齐
    &.dec-module-title--align-3 {
        flex-direction: row-reverse;

        .dec-module-title__left {
            flex-direction: row-reverse;
        }

        .dec-module-title__icon {
            margin-right: 0;
            margin-left: 5px;
        }

        .dec-module-title__desc {
            margin-left: 0;
            margin-right: 5px;
        }

        .dec-module-title__right {
            margin-right: auto;
        }
    }

    // ========== 标题样式变体 ==========

    // 样式二：带左侧装饰线
    &.dec-module-title--style-2 {
        .dec-module-title__text {
            padding-left: 10px;

            &::before {
                content: "";
                position: absolute;
                left: 0;
                top: 50%;
                transform: translateY(-50%);
                width: 3px;
                height: 16px;
                background: linear-gradient(180deg, #155bd4 0%, #64b5f6 100%);
                border-radius: 2px;
            }
        }
    }

    // 样式三：带下划线装饰
    &.dec-module-title--style-3 {
        .dec-module-title__text {
            &::after {
                content: "";
                position: absolute;
                left: 0;
                bottom: -2px;
                width: 100%;
                height: 2px;
                background: linear-gradient(90deg, #155bd4 0%, transparent 100%);
                border-radius: 1px;
            }
        }
    }
}
</style>
