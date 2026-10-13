<template>
    <div
        class="module-ad-con module-shop-tips"
        :style="
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.boxMargin +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div class="shop-tips-empty" v-if="!module.picList || module.picList.length === 0">
                <div class="desc">{{ $t('点击添加商品提示') }}</div>
            </div>
            <div class="shop-tips-outer" v-else>
                <ul v-for="(row, rowIndex) in itemRows" :key="rowIndex" class="shop-tips-row" :style="rowStyle">
                    <li v-for="(item, colIndex) in row" :key="colIndex" class="shop-tips-li">
                        <a class="shop-tips-link" :style="linkStyle">
                            <div class="shop-tips-content" :style="textStyle">
                                <img v-if="item.picUrl || item.picThumb" class="shop-tips-icon" :src="productImage(item)" alt="" />
                                <span class="shop-tips-text">{{ item.picTitle }}</span>
                            </div>
                            <em v-if="module.showDivider === 1 && Number(colIndex) < row.length - 1" class="shop-tips-sx" :style="dividerStyle"></em>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, Ref } from "vue";
import { ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle } from "@/views/decorate/decorate/src/modules/";
import { productImage } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & Record<string, any>>("module") as Ref<ModuleType & Record<string, any>>;

const defaultPicList = [
    {
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779263739npDO706Su0cRzKK3ar.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779263739npDO706Su0cRzKK3ar.png",
        picTitle: t('最快30分钟送达'),
        picLink: {}
    },
    {
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779263739N0N6CeZ40H7KJY9vRb.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779263739N0N6CeZ40H7KJY9vRb.png",
        picTitle: t('满69元包邮'),
        picLink: {}
    },
    {
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779263739qL8q3AHR1CcdoNv4sD.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779263739qL8q3AHR1CcdoNv4sD.pngg",
        picTitle: t('品质保障'),
        picLink: {}
    }
];

const defaultModule = {
    picList: defaultPicList,
    rowNum: 3,
    itemGap: 0,
    showDivider: 0,
    titleColor: "#00C455",
    itemBackgroundColor: "",
    moduleStyle: {
        ...defaultModuleStyle,
        boxPaddingTop: 5,
        boxPaddingBottom: 5,
        boxPadding: 10
    },
    contentStyle: defaultContentStyle
};
mergeDefaultModule(module.value, defaultModule);

const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {});
});

const rowNum = computed(() => module.value.rowNum || 3);
const halfGap = computed(() => (module.value.itemGap ?? 0) / 2);

const itemRows = computed(() => {
    const list = module.value.picList || [];
    const n = rowNum.value;
    const rows: (typeof list)[] = [];
    for (let i = 0; i < list.length; i += n) {
        rows.push(list.slice(i, i + n));
    }
    return rows;
});

const rowStyle = computed(() => {
    const h = halfGap.value;
    return h > 0 ? `margin-left: -${h}px; margin-right: -${h}px;` : "";
});

const linkStyle = computed(() => {
    const h = halfGap.value;
    const bgColor = module.value.itemBackgroundColor;
    const bgStyle = bgColor ? `background-color: ${bgColor};` : "";
    const marginStyle = h > 0 ? `margin: ${h}px;` : "";
    return marginStyle + bgStyle;
});

const textStyle = computed(() => {
    const color = module.value.titleColor || "#333333";
    return `color: ${color};`;
});

const dividerStyle = computed(() => {
    const color = module.value.titleColor || "#333333";
    return `border-color: ${color};`;
});
</script>
<style lang="less" scoped>
.module-shop-tips {
    min-height: auto !important;

    .module-ad-content {
        overflow: hidden;
    }
}

.shop-tips-empty {
    padding: 20px 0;
    text-align: center;
    color: #999;
    font-size: 14px;
}

.shop-tips-outer {
    padding: 0;
    margin-left: 0;
    margin-right: 0;
}

.shop-tips-row {
    display: flex;
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
    list-style: none;
    margin: 0;
    padding: 0;
}

.shop-tips-li {
    white-space: nowrap;
    box-sizing: border-box;
    list-style: none;
    min-width: 20%;
    max-width: 100%;
}

.shop-tips-link {
    display: block;
    position: relative;
    background: transparent;
    padding: 0;
    min-height: 10px;
    text-decoration: none;
    color: inherit;
    border-radius: 0;
    cursor: pointer;
}

.shop-tips-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    overflow: hidden;
    width: 100%;
}

.shop-tips-icon {
    width: 14px;
    height: 14px;
    object-fit: contain;
    flex-shrink: 0;
}

.shop-tips-text {
    min-width: 0;
    flex: 0 1 auto;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 12px;
}

.shop-tips-sx {
    position: absolute;
    box-sizing: border-box;
    pointer-events: none;
    top: -30%;
    bottom: -30%;
    right: -10px;
    transform: scale(0.5);
    border-right-width: 1px;
    border-right-style: solid;
}
</style>
