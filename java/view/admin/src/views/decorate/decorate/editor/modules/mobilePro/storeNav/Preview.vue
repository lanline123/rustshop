<template>
    <div
        class="module-ad-con"
        :style="commonStyle.moduleStyle.boxPaddingTopBottom + commonStyle.moduleStyle.boxPadding + commonStyle.moduleStyle.backgroundColor"
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundColor +
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.paddingTopBottom +
                commonStyle.moduleContentStyle.paddingLeftRight
            "
        >
            <div class="store-nav-box" v-if="module.storeStyle !== StoreNavStyle.Two">
                <div class="store-header" :style="titleBoxStyle">
                    <div class="header-title" :style="titleStyle">附近门店</div>
                    <div class="header-more">更多<span class="iconfont-h5 icon-sanjiaoright"></span></div>
                </div>
                <div class="stores">
                    <div
                        class="stores-item"
                        v-for="(store, index) in previewStores"
                        :key="index"
                        :style="storeItemStyle(index)"
                    >
                        <div class="item-left">
                            <img :src="imageFormat(store.shopLogo || DEMO_STORE_LOGO)" />
                        </div>
                        <div class="item-right">
                            <div class="store-title" :style="storeTitleStyle">{{ store.shopTitle }}</div>
                            <template v-if="module.showRating == 1">
                                <div class="store-desc">
                                    <img src="@/style/images/decorate/storeNav/store-tag.png" alt="" srcset="" />
                                    5.00
                                </div>
                            </template>

                            <div class="store-address-info">
                                <div class="address line1">江西省南昌市青山湖区艾溪湖312123312</div>
                                <div class="distance">
                                    <span class="iconfont-h5 icon-daohang"></span>
                                    520m
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="store-card-box" v-else>
                <div
                    class="store-card"
                    v-for="(store, index) in previewStores"
                    :key="index"
                    :style="storeCardStyle(index)"
                >
                    <div class="card-header">
                        <div class="shop-logo">
                            <img :src="imageFormat(store.shopLogo || DEMO_STORE_LOGO)" />
                        </div>
                        <div class="shop-main">
                            <div class="title-row">
                                <img
                                    v-if="module.showRating == 1"
                                    class="store-tag"
                                    src="@/style/images/decorate/storeNav/store-tag.png"
                                    alt=""
                                />
                                <div class="shop-title line1" :style="storeTitleStyle">{{ store.shopTitle }}</div>
                                <div class="collect-btn" v-if="module.showFollow == 1">
                                    <span class="iconfont-h5 icon-guanzhu"></span>
                                    <span class="collect-text">关注</span>
                                </div>
                            </div>
                            <div class="meta-row">
                                <span class="shop-star" v-if="module.showRating == 1">5.00分</span>
                                <span class="meta-item" v-if="module.showMinOrder == 1">起送 ¥1</span>
                                <span class="meta-item" v-if="module.showDeliveryFee == 1">配送费 ¥1</span>
                                <span class="meta-item" v-if="module.showDistance == 1">距离 556.88km</span>
                            </div>
                        </div>
                    </div>
                    <div class="product-scroll">
                        <div class="product-row" :style="{ columnGap: `${module.productGap ?? 8}px` }">
                            <div class="product-item" v-for="product in DEMO_PRODUCTS" :key="product.productName">
                                <div class="product-image">
                                    <img :src="product.picThumb" />
                                </div>
                                <div
                                    v-if="module.showProductName == 1"
                                    class="product-name"
                                    :class="module.productNameRow == 2 ? 'line2' : 'line1'"
                                >
                                    {{ product.productName }}
                                </div>
                                <div class="product-price" v-if="module.showProductPrice == 1">¥{{ product.productPrice }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { imageFormat } from "@/utils/format";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { storeNavDefaults } from "./index";
import { StoreNavSource, StoreNavStyle, STORE_NAV_MIN_AUTO_NUM, STORE_NAV_MAX_AUTO_NUM, type StoreNavModule } from "./types";

const props = defineProps<{ module: StoreNavModule }>();
const module = props.module;

// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, storeNavDefaults);

/** 演示门店名称，自动获取时展示 */
const DEMO_STORE_TITLE = "门店名称门店名称";
const DEMO_STORE_LOGO = "https://oss.tigshop.com/img/gallery/202508/1756284426i6dOj6OQOWT7PEgTsu.jpeg";
const DEMO_PRODUCTS = [
    {
        productName: "婴儿连体衣 会员特价",
        productPrice: 19.8,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474y0XWFxO8GJZF0dg64.jpg"
    },
    {
        productName: "纯棉新生儿套装",
        productPrice: 12.9,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071axvkfUDkQNFbIzF1iY.jpg"
    },
    {
        productName: "可折叠婴儿浴盆",
        productPrice: 39.9,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071lOa0ZBx2mYlvq1lgjr.jpg"
    },
    {
        productName: "婴儿洗护礼盒 热卖中",
        productPrice: 12.9,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071xUnObq2LokKPFecwXq.jpg"
    }
];

/** 指定店铺时按所选顺序预览，自动获取时按配置数量铺演示门店 */
const previewStores = computed(() => {
    if (module.storeSource === StoreNavSource.Assign) {
        const assigned = module.storeList ?? [];
        if (assigned.length > 0) {
            return assigned.map((item) => ({
                shopTitle: item.shopTitle || DEMO_STORE_TITLE,
                shopLogo: item.shopLogo || DEMO_STORE_LOGO
            }));
        }
        return [{ shopTitle: DEMO_STORE_TITLE, shopLogo: DEMO_STORE_LOGO }];
    }
    const num = Math.min(Math.max(Math.round(Number(module.storeNum) || STORE_NAV_MIN_AUTO_NUM), STORE_NAV_MIN_AUTO_NUM), STORE_NAV_MAX_AUTO_NUM);
    return Array.from({ length: num }, () => ({ shopTitle: DEMO_STORE_TITLE, shopLogo: DEMO_STORE_LOGO }));
});

const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});

const titleStyle = computed(() => {
    return `
        font-size: ${module.subTitle?.titleFontSize}px;
        color: ${module.subTitle?.titleColor};
        font-weight: ${module.subTitle?.titleFontBold};
    `;
});

const titleBoxStyle = computed(() => {
    const padY = module.titlePaddingY ?? 0;
    const padX = module.titlePaddingX ?? 0;
    return {
        padding: `${padY}px ${padX}px`
    };
});

const storeTitleStyle = computed(() => {
    const style = module.storeTitleStyle ?? storeNavDefaults.storeTitleStyle;
    return {
        fontSize: `${style.titleFontSize ?? 14}px`,
        color: style.titleColor || "#1a1a1a",
        fontWeight: style.titleFontBold ?? 700
    };
});

const storeBgColor = computed(() => module.storeBackgroundColor || "#ffffff");

/** 门店项公共样式：背景、内边距、圆角 */
const storeBoxBaseStyle = () => {
    const padY = module.storePaddingY ?? 0;
    const padX = module.storePaddingX ?? 0;
    return {
        backgroundColor: storeBgColor.value,
        padding: `${padY}px ${padX}px`,
        borderRadius: `${module.storeRadius ?? 0}px`,
        overflow: "hidden"
    } as Record<string, string>;
};

/** 样式一门店项 */
const storeItemStyle = (index: number) => {
    const style = storeBoxBaseStyle();
    if (index > 0) style.marginTop = `${module.storeGap ?? 15}px`;
    return style;
};

/** 样式二门店卡片 */
const storeCardStyle = (index: number) => {
    const style = storeBoxBaseStyle();
    if (index > 0) style.marginTop = `${module.storeGap ?? 15}px`;
    return style;
};
</script>
<style lang="less" scoped>
.store-nav-box {
    .store-header {
        display: flex;
        align-items: center;
        border-bottom: 1px solid #f4f4f4;

        .header-title {
            font-weight: 600;
            font-size: 14px;
            color: #1a1a1a;
        }

        .header-more {
            margin-left: auto;
            display: flex;
            align-items: center;
            font-weight: 500;
            font-size: 12px;
            color: #2a2a2a;
            column-gap: 2px;

            .icon-sanjiaoright {
                font-size: 12px;
            }
        }
    }

    .stores {
        .stores-item {
            width: 100%;
            display: flex;
            align-items: center;
            column-gap: 12px;

            .item-left {
                width: 68px;
                height: 68px;
                border-radius: 8px;
                overflow: hidden;
                background-color: #f5f5f5;

                img {
                    display: block;
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                }
            }

            .item-right {
                flex: 1;
                min-height: 68px;
                overflow: hidden;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                padding-top: 2px;

                .store-desc {
                    font-weight: 400;
                    font-size: 14px;
                    color: #1a1a1a;
                    display: flex;
                    align-items: center;
                    column-gap: 2px;

                    img {
                        display: block;
                        width: 58px;
                        height: 14px;
                    }
                }

                .store-address-info {
                    display: flex;
                    align-items: center;
                    column-gap: 20px;

                    .address {
                        font-weight: 500;
                        font-size: 14px;
                        color: #1a1a1a;
                        flex: 1;
                        overflow: hidden;
                    }

                    .distance {
                        background: #f4f4f4;
                        font-size: 12px;
                        color: #666666;
                        display: flex;
                        align-items: center;
                        column-gap: 5px;
                        border-radius: 999px;
                        padding: 4px 6px;

                        .icon-daohang {
                            color: rgba(102, 102, 102, 1);
                            font-size: 12px;
                        }
                    }
                }
            }
        }
    }
}

// 与移动端 pages/search/src/storeSearchCard.vue 对齐，按 375 宽画布做 rpx -> px 折算
.store-card-box {
    .store-card {
        & + .store-card {
            border-top: 1px solid #f4f4f4;
        }

        .card-header {
            display: flex;
            align-items: flex-start;

            .shop-logo {
                flex: none;
                width: 40px;
                height: 40px;
                border-radius: 50%;
                overflow: hidden;
                background-color: #f5f5f5;

                img {
                    display: block;
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }

            .shop-main {
                flex: 1;
                width: 0;
                margin-left: 8px;

                .title-row {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    column-gap: 4px;

                    .store-tag {
                        flex-shrink: 0;
                        display: block;
                        width: 53px;
                        height: 13px;
                    }

                    .shop-title {
                        flex: 1;
                        width: 0;
                        line-height: 21px;
                    }

                    .collect-btn {
                        flex-shrink: 0;
                        display: flex;
                        align-items: center;
                        height: 24px;
                        padding: 0 10px;
                        border-radius: 12px;
                        background-color: var(--general, #ff4a1e);
                        color: #ffffff;

                        .icon-guanzhu {
                            font-size: 11px;
                            margin-right: 3px;
                        }

                        .collect-text {
                            font-size: 11px;
                            line-height: 1;
                        }
                    }
                }

                .meta-row {
                    display: flex;
                    flex-wrap: wrap;
                    align-items: center;
                    margin-top: 5px;
                    column-gap: 8px;
                    row-gap: 3px;

                    .shop-star {
                        font-weight: 600;
                        font-size: 11px;
                        color: var(--general, #ff4a1e);
                    }

                    .meta-item {
                        font-size: 11px;
                        color: #999999;
                    }
                }
            }
        }

        .product-scroll {
            width: 100%;
            margin-top: 10px;
            overflow-x: auto;

            &::-webkit-scrollbar {
                display: none;
            }

            .product-row {
                display: flex;
            }

            .product-item {
                flex-shrink: 0;
                width: 78px;

                .product-image {
                    width: 78px;
                    height: 78px;
                    border-radius: 6px;
                    overflow: hidden;
                    background-color: #f5f5f5;

                    img {
                        display: block;
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                    }
                }

                .product-name {
                    margin-top: 5px;
                    font-size: 12px;
                    line-height: 17px;
                    color: #333333;

                    &.line1 {
                        overflow: hidden;
                        white-space: nowrap;
                        text-overflow: ellipsis;
                    }

                    &.line2 {
                        display: -webkit-box;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        -webkit-line-clamp: 2;
                        -webkit-box-orient: vertical;
                        white-space: normal;
                    }
                }

                .product-price {
                    margin-top: 2px;
                    font-weight: bold;
                    font-size: 12px;
                    color: var(--general, #ff4a1e);
                }
            }
        }
    }
}
</style>
