<template>
    <div
        class="module-ad-con module-store-order-card"
        :class="['pic-type-' + module.picType, 'card-style-' + module.cardStyle, 'box-style-' + module.boxStyle]"
        :style="wrapStyle"
    >
        <!-- 左一右二：1 大 + 2 小 -->
        <template v-if="module.picType === 3">
            <div class="store-order-row layout-3">
                <div class="store-order-col store-order-col-left">
                    <div class="store-order-item item-large" :style="cardStyleObj">
                        <CardBox :item="module.cardList?.[0]" :card-style="module.cardStyle" size="medium"></CardBox>
                    </div>
                </div>
                <div class="store-order-col store-order-col-right">
                    <div class="store-order-item" :style="cardStyleObj">
                        <CardBox :item="module.cardList?.[1]" :card-style="module.cardStyle" size="small"></CardBox>
                    </div>
                    <div class="store-order-item" :style="cardStyleObj">
                        <CardBox :item="module.cardList?.[2]" :card-style="module.cardStyle" size="small"></CardBox>
                    </div>
                </div>
            </div>
        </template>
        <!-- 两行五个：上 2 大 + 下 3 小 -->
        <template v-else-if="module.picType === 6">
            <div class="store-order-row layout-6-top">
                <div v-for="(item, index) in (module.cardList || []).slice(0, 2)" :key="'t-' + index" class="store-order-item" :style="cardStyleObj">
                    <CardBox :item="item" :card-style="module.cardStyle" size="medium"></CardBox>
                </div>
            </div>
            <div class="store-order-row layout-6-bot">
                <div v-for="(item, index) in (module.cardList || []).slice(2, 5)" :key="'b-' + index" class="store-order-item" :style="cardStyleObj">
                    <CardBox :item="item" :card-style="module.cardStyle" size="small"></CardBox>
                </div>
            </div>
        </template>
        <!-- 其余：一行一个 / 一行两个 / 一行三个 / 一行四个 -->
        <template v-else>
            <div class="store-order-row" :class="'grid-cols-' + colsMap[module.picType || 2]">
                <div v-for="(item, index) in module.cardList" :key="index" class="store-order-item" :style="cardStyleObj">
                    <CardBox
                        :item="item"
                        :card-style="module.cardStyle"
                        :size="module.picType === 1 ? 'large' : module.picType === 2 ? 'medium' : 'small'"
                    ></CardBox>
                </div>
            </div>
        </template>
    </div>
</template>
<script lang="ts" setup>
import { computed, defineComponent, h } from "vue";
import { productImage } from "@/utils/format";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/";
import { storeOrderCardDefaults, moduleFrameDefault, LENGTH_MAP, buildDefaultCard } from "./index";
import type { StoreOrderCardModule } from "./types";

const props = defineProps<{ module: StoreOrderCardModule }>();
const module = props.module;

// 历史数据仅有单个 innerPadding 时，用它回填左右/上下内边距
const migrateInnerPadding = (frame: any) => {
    if (!frame || typeof frame !== "object") return;
    if (frame.innerPaddingLeftRight === undefined || frame.innerPaddingLeftRight === null) {
        frame.innerPaddingLeftRight = frame.innerPadding ?? 10;
    }
    if (frame.innerPaddingTopBottom === undefined || frame.innerPaddingTopBottom === null) {
        frame.innerPaddingTopBottom = frame.innerPadding ?? 10;
    }
};

mergeDefaultModule(module, storeOrderCardDefaults);
if (!module.frame || typeof module.frame !== "object") {
    module.frame = { ...defaultFrame, ...moduleFrameDefault };
} else {
    migrateInnerPadding(module.frame);
    mergeDefaultModule(module.frame, { ...defaultFrame, ...moduleFrameDefault });
}

// 保证 cardList 数量与 picType 对齐（历史脏数据兜底）
const expectLen = LENGTH_MAP[module.picType || 2] || 2;
if (!Array.isArray(module.cardList)) module.cardList = [];
while (module.cardList.length < expectLen) {
    module.cardList.push(buildDefaultCard());
}

const colsMap: Record<number, number> = {
    1: 1,
    2: 2,
    4: 3,
    5: 4
};

const wrapStyle = computed(() => {
    const frame = module.frame || {};
    const gap = module.cardGap ?? 8;
    return {
        backgroundColor: module.backgroundColor || "",
        paddingLeft: (frame.boxPadding ?? 10) + "px",
        paddingRight: (frame.boxPadding ?? 10) + "px",
        marginTop: (frame.boxPaddingTop ?? 5) + "px",
        marginBottom: (frame.boxPaddingBottom ?? 5) + "px",
        "--card-gap": gap + "px"
    } as Record<string, string>;
});

const cardStyleObj = computed(() => {
    const boxStyle = Number(module.boxStyle) || 1;
    const radius = module.frame?.boxRadius ?? 0;
    const style: Record<string, string> = {
        borderRadius: radius + "px"
    };
    if (boxStyle === 4) {
        style.backgroundColor = "transparent";
    } else if (boxStyle === 1) {
        style.backgroundColor = "#ffffff";
    } else {
        style.backgroundColor = module.cardBackground || "#ffffff";
    }
    if (boxStyle === 2) {
        style.boxShadow = "0 2px 8px rgba(0, 0, 0, 0.08)";
    }
    if (boxStyle === 3) {
        style.border = "1px solid #E5E5E5";
    }
    return style;
});

const CardBox = defineComponent({
    props: {
        item: { type: Object, default: () => ({}) },
        cardStyle: { type: Number, default: 1 },
        size: { type: String, default: "small" }
    },
    setup(props) {
        return () => {
            const isImageMode = props.cardStyle === 2;
            const hasPic = !!(props.item?.picUrl || props.item?.picThumb);
            const frame = module.frame || {};
            const paddingX = frame.innerPaddingLeftRight ?? frame.innerPadding ?? 0;
            const paddingY = frame.innerPaddingTopBottom ?? frame.innerPadding ?? 0;
            const padding = `${paddingY}px ${paddingX}px`;
            const imgTop = Number(frame.imagePaddingTop) || 0;
            const imgBottom = Number(frame.imagePaddingBottom) || 0;
            const imgLeftRight = Number(frame.imagePaddingLeftRight) || 0;
            if (isImageMode) {
                // 整卡即图片，图片边距叠加到内容容器的内边距上（border-box，不会溢出裁切）
                return h(
                    "div",
                    {
                        class: ["store-order-card-content", "mode-image", "size-" + props.size],
                        style: {
                            padding: `${paddingY + imgTop}px ${paddingX + imgLeftRight}px ${paddingY + imgBottom}px`
                        }
                    },
                    [
                        hasPic
                            ? h("img", { class: "store-order-full-img", src: productImage(props.item) })
                            : h("div", { class: "store-order-empty-img" }, "")
                    ]
                );
            }
            return h(
                "div",
                {
                    class: ["store-order-card-content", "mode-textimg", "size-" + props.size],
                    style: { padding: padding }
                },
                [
                    h("div", { class: "store-order-text" }, [
                        h("div", { class: "store-order-title" }, props.item?.title || ""),
                        h("div", { class: "store-order-desc" }, props.item?.desc || "")
                    ]),
                    h(
                        "div",
                        {
                            class: "store-order-img",
                            style: {
                                marginTop: imgTop + "px",
                                marginBottom: imgBottom + "px",
                                marginLeft: imgLeftRight + "px",
                                marginRight: imgLeftRight + "px"
                            }
                        },
                        [hasPic ? h("img", { src: productImage(props.item) }) : h("div", { class: "store-order-img-placeholder" })]
                    )
                ]
            );
        };
    }
});
</script>
<style lang="less" scoped>
.module-store-order-card {
    box-sizing: border-box;

    .store-order-row {
        display: flex;
        gap: var(--card-gap, 8px);

        & + .store-order-row {
            margin-top: var(--card-gap, 8px);
        }

        &.grid-cols-1 .store-order-item {
            flex: 1;
        }
        &.grid-cols-2 .store-order-item,
        &.grid-cols-3 .store-order-item,
        &.grid-cols-4 .store-order-item {
            flex: 1;
        }

        &.layout-6-top .store-order-item,
        &.layout-6-bot .store-order-item {
            flex: 1;
        }
    }

    .store-order-row.layout-3 {
        display: flex;
        gap: var(--card-gap, 8px);

        .store-order-col-left,
        .store-order-col-right {
            flex: 1;
            min-width: 0;
            display: flex;
            flex-direction: column;
            gap: var(--card-gap, 8px);
        }

        .store-order-col-left .store-order-item {
            flex: 1;
            min-height: 152px;
        }
    }

    .store-order-item {
        box-sizing: border-box;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        min-width: 0;
    }

    :deep(.store-order-card-content.mode-textimg) {
        display: flex;
        flex: 1;
        flex-direction: column-reverse;
        justify-content: center;
        align-items: center;
        gap: 4px;
        box-sizing: border-box;
        text-align: center;

        .store-order-text {
            min-width: 0;
            width: 100%;
        }
        .store-order-title {
            font-size: 14px;
            color: #333;
            font-weight: 500;
            line-height: 1.4;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .store-order-desc {
            font-size: 12px;
            color: #999;
            margin-top: 2px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        // align-self: stretch + width: auto 让图片宽度自动扣除左右外边距，不会溢出被裁切
        .store-order-img {
            width: auto;
            align-self: stretch;
            aspect-ratio: 1 / 1;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            overflow: hidden;
            img {
                width: 100%;
                height: 100%;
                object-fit: contain;
            }
            .store-order-img-placeholder {
                width: 100%;
                height: 100%;
                border: 1px solid #eee;
                border-radius: 2px;
                background: #f7f8fa;
            }
        }

        &.size-large {
            flex-direction: row;
            align-items: center;
            justify-content: space-between;
            text-align: left;
            gap: 8px;
            min-height: 90px;

            .store-order-text {
                flex: 1;
                width: auto;
            }
            .store-order-title {
                font-size: 16px;
            }
            .store-order-desc {
                font-size: 13px;
            }
            .store-order-img {
                width: 84px;
                height: 84px;
            }
        }

        &.size-medium {
            .store-order-title {
                font-size: 14px;
            }
        }

        &.size-small {
            .store-order-title {
                font-size: 13px;
            }
            .store-order-desc {
                font-size: 11px;
            }
        }
    }

    .store-order-row.layout-3 .store-order-col-left :deep(.store-order-card-content.mode-textimg) {
        gap: 6px;

        .store-order-img {
            width: 96px;
            height: 96px;
        }
        .store-order-title {
            font-size: 16px;
        }
        .store-order-desc {
            font-size: 13px;
        }
    }

    .store-order-row.layout-3 .store-order-col-left :deep(.store-order-card-content.mode-image) {
        flex: 1;
        height: 100%;

        .store-order-full-img {
            height: 100%;
            aspect-ratio: auto;
        }
        .store-order-empty-img {
            height: 100%;
            aspect-ratio: auto;
        }
    }

    :deep(.store-order-card-content.mode-image) {
        width: 100%;
        overflow: hidden;
        box-sizing: border-box;

        .store-order-full-img {
            display: block;
            width: 100%;
            aspect-ratio: 1 / 1;
            height: auto;
            object-fit: contain;
        }

        .store-order-empty-img {
            width: 100%;
            aspect-ratio: 1 / 1;
            height: auto;
            background: #f7f8fa;
        }
    }
}
</style>
