<template>
    <div class="pc-header-simple">
        <div
            class="pc-header-simple__header"
            :class="{
                'pc-header-simple__header--bottom-none': bottomDividerType === 0,
                'pc-header-simple__header--white': isWhiteHeader,
                'pc-header-simple__header--bottom-shadow': bottomDividerType === 1,
                'pc-header-simple__header--bottom-border': bottomDividerType === 2
            }"
        >
            <div class="pc-container">
                <div class="pc-header">
                    <div ref="wrapperRef" class="pc-header__wrapper">
                        <div class="pc-header-home__left">
                            <div class="pc-header__logo-slot" :class="{ 'pc-header__logo-slot--wide': !showCategory }">
                                <div class="pc-header__logo" :class="logoAlignClass">
                                    <div class="pc-header__logo-area">
                                        <div class="pc-header__logo-box">
                                            <img :src="logoSrc" :style="logoImgStyle" alt="" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="showCategory" class="category-nav" :style="{ marginTop: `${categoryMarginTop}px` }">
                                <div class="category-wrap">
                                    <div class="bar-wrap">
                                        <div class="content">
                                            <div class="icon">
                                                <span class="pc-icon pc-icon--menu"></span>
                                            </div>
                                            <div class="txt">分类</div>
                                            <div class="icon ml">
                                                <span class="pc-icon pc-icon--arrow arrow"></span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="pc-header-home__center">
                            <div class="pc-header__search-nav">
                                <div class="pc-header__search" :style="simpleSearchStyle">
                                    <div class="top-search">
                                        <div class="search-form">
                                            <div class="acea-row row-middle">
                                                <!-- 预览态：照搬 o2o DefSearchNew 搜门店/商品切换，常显且不可交互 -->
                                                <div class="search-type-select">
                                                    <div class="search-type-menu" data-longest-label="门店">
                                                        <div class="search-type-item">
                                                            <span class="search-type-label">商品</span>
                                                            <svg class="arrow" xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" viewBox="0 0 14 14">
                                                                <path
                                                                    stroke="currentColor"
                                                                    stroke-linecap="round"
                                                                    stroke-linejoin="round"
                                                                    stroke-width="1.313"
                                                                    d="M10.063 5.469 7 8.53 3.938 5.47"
                                                                />
                                                            </svg>
                                                        </div>
                                                    </div>
                                                </div>
                                                <input type="text" autocomplete="off" class="search-input" placeholder="请输入关键词" disabled />
                                                <div type="submit" class="search-submit search-submit--text">搜索</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <PcHeaderNavPreviewBar
                                    v-if="showMainNav"
                                    class="pc-header-nav"
                                    :padding-top="mainNavPaddingTop"
                                    :padding-bottom="mainNavPaddingBottom"
                                    :items="navItems"
                                />
                            </div>
                        </div>

                        <div class="pc-header-home__right" :class="{ 'pc-header-home__right--empty': !showRightCart }">
                            <div v-if="showRightCart" class="pc-header__cart" :class="{ 'pc-header__cart--pic': cartStyle === 2 }">
                                <div
                                    class="pc-cart"
                                    :class="{
                                        'pc-cart--pic': cartStyle === 2,
                                        'pc-cart--has-pic': cartStyle === 2 && !!cartPicThumb
                                    }"
                                >
                                    <template v-if="cartStyle === 2">
                                        <img v-if="cartPicThumb" class="pc-cart__pic" :src="cartPicThumb" alt="" />
                                        <div v-else class="pc-cart__pic-empty">购物车图</div>
                                    </template>
                                    <template v-else>
                                        <span class="pc-icon pc-icon--cart"></span>
                                        <span class="pc-cart__count">18</span>
                                        <span class="pc-cart__text">购物车</span>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import PcHeaderNavPreviewBar from "./PcHeaderNavPreviewBar.vue";

defineProps<{
    isWhiteHeader: boolean;
    logoSrc: string;
    logoImgStyle: Record<string, string>;
    logoAlignClass: string;
    showCategory: boolean;
    categoryMarginTop: number;
    simpleSearchStyle: Record<string, string>;
    showMainNav: boolean;
    mainNavPaddingTop: number;
    mainNavPaddingBottom: number;
    showRightCart: boolean;
    cartStyle: 1 | 2;
    cartPicThumb: string;
    bottomDividerType: 0 | 1 | 2;
    navItems?: string[];
}>();
</script>

<style lang="less" scoped>
@import "./_pcHeaderPreviewIcons.less";

.pc-container {
    margin: 0 auto;
    min-width: 0;
    width: 100%;
    box-sizing: border-box;
    padding: 0;
}

.pc-header-simple {
    background: #ffffff;
}

.pc-header-simple__header {
    background: transparent;
    border-bottom: 0;
    box-shadow: none;
}

.pc-header-simple__header--bottom-none {
    border-bottom: none !important;
    box-shadow: none !important;
}

.pc-header-simple__header--white {
    background: #ffffff;
}

.pc-header-simple__header--bottom-border {
    border-bottom: 1px solid #ededed;
}

.pc-header-simple__header--bottom-shadow {
    position: relative;
    z-index: 1;
    box-shadow:
        0 2px 12px 0 rgba(0, 0, 0, 0.0392),
        0 4px 6px 0 rgba(0, 0, 0, 0.0588);
}

.pc-header {
    padding: 16px 0 0;
}

.pc-header__wrapper {
    display: grid;
    grid-template-columns: max-content minmax(0, 1fr) max-content;
    column-gap: 16px;
    align-items: start;
    min-height: 60px;

    .pc-header-home__left {
        display: flex;
        align-items: center;
        justify-content: flex-start;
        gap: 16px;
        height: 60px;
        min-width: 0;
        overflow: visible;
    }

    .pc-header-home__center {
        min-width: 0;
        display: flex;
        align-items: flex-start;
    }

    .pc-header-home__right {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 60px;
        width: 252px;
        min-width: 252px;
        max-width: 252px;
        overflow: visible;
    }

    .pc-header-home__right--empty {
        pointer-events: none;
    }
}

.pc-header__logo-slot {
    width: 160px;
    min-width: 160px;
    max-width: 160px;
    flex-shrink: 0;
}

.pc-header__logo-slot--wide {
    width: 250px;
    min-width: 250px;
    max-width: 250px;
}

.pc-header__logo {
    flex-shrink: 0;
    width: 100%;
    display: flex;
    align-items: center;

    &.logo--left {
        justify-content: flex-start;
    }
    &.logo--center {
        justify-content: center;
    }
    &.logo--right {
        justify-content: flex-end;
    }
}

.pc-header__logo-area {
    height: 60px;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    overflow: hidden;
    transform: none;
}

.pc-header__logo-box {
    display: flex;
    align-items: center;
    min-width: 0;
    max-width: 100%;
    overflow: hidden;

    img {
        // 配置项可能把 logo 高度调得很大，这里保证不撑开其他区域，同时保持垂直居中
        max-height: 100%;
        // 配置项可能把 logo 宽度调得很大：用 max-width 限制在可用空间内
        max-width: 100%;
    }
}

.pc-header__search {
    width: 100%;
    min-width: 0;
    min-height: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    position: relative;
    overflow: visible;
}

.pc-header__search-nav {
    width: 100%;
    min-width: 0;
    display: flex;
    flex-direction: column;
}

.pc-header-nav {
    min-width: 0;
}

.category-nav {
    min-width: 74px;
    height: 100%;
    white-space: nowrap;
    flex-shrink: 0;
}

.pc-header-simple {
    .category-wrap {
        height: 100%;

        .bar-wrap {
            cursor: pointer;
            display: flex;
            align-items: center;
            height: 100%;

            .content {
                display: flex;
                height: 28px;

                &:hover {
                    .icon {
                        color: var(--primary-dark-2);
                    }

                    .txt {
                        color: var(--primary-dark-2);
                        font-weight: 500;
                        text-transform: capitalize;
                    }
                }

                .icon {
                    align-self: center;
                    color: #1a1a1a;

                    &.ml {
                        margin-left: 4px;
                        margin-bottom: 3px;
                        align-self: end;
                    }
                }

                .txt {
                    align-self: center;
                    margin-left: 8px;
                    font-size: 14px;
                    color: #1a1a1a;
                    font-weight: 500;
                    text-transform: capitalize;
                }
            }
        }
    }
}

.pc-header__cart {
    flex-shrink: 0;
    min-width: clamp(96px, 14vw, 120px);
    display: flex;
    justify-content: flex-end;
}

.pc-header__cart--pic {
    min-width: clamp(140px, 24vw, 180px);
}

.pc-header__right {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 14px;
}

.pc-cart {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    user-select: none;
    color: #1a1a1a;

    &--pic {
        width: 180px;
        height: 60px;
        border-radius: 6px;
        overflow: hidden;
        background: #f3f4f6;
        border: 1px solid #ededed;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    &--has-pic {
        background: transparent;
        border: 0;
    }

    &__pic {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    &__pic-empty {
        font-size: 12px;
        color: #9ca3af;
    }

    &__count {
        position: absolute;
        top: -6px;
        left: 10px;
        min-width: 16px;
        height: 16px;
        line-height: 16px;
        padding: 0 3px;
        border-radius: 100px;
        background: #cc4b11;
        border: 1px solid #fff;
        color: #fff;
        font-size: 10px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
    }

    &__text {
        font-size: 14px;
        font-weight: 500;
    }
}

/* DefSearchNew：top-search */
.top-search {
    position: relative;
    border-radius: 7px;
    width: 100%;
    border: 1px solid var(--pc-header-search-accent, #c1c1c1);
    padding: 1px;
    background: #fff;
    box-sizing: border-box;

    &:hover {
        border: 1px solid var(--pc-header-search-accent, #1a1a1a);
    }

    .search-form {
        display: flex;
        align-items: center;
        background-color: #fff;
        overflow: hidden;
        height: 37px;
        width: 100%;
        border-radius: 8px;

        .search-type-select {
            position: relative;
            height: 37px;
            display: inline-flex;
            align-items: center;
            border-radius: 8px;
            user-select: none;
            flex-shrink: 0;
            background-color: #fff;
            color: #333;

            .search-type-menu {
                width: max-content;
                height: 37px;
                position: relative;
                display: flex;
                flex-direction: column;

                &::after {
                    content: attr(data-longest-label);
                    display: block;
                    height: 0;
                    overflow: hidden;
                    visibility: hidden;
                    white-space: nowrap;
                    font-size: 14px;
                    padding: 0 calc(12px + 14px + 4px) 0 12px;
                }

                .search-type-item {
                    height: 37px;
                    font-size: 14px;
                    color: #333;
                    display: flex;
                    align-items: center;
                    justify-content: flex-start;
                    padding-left: 16px;
                    border-radius: 6px;
                    gap: 4px;
                    white-space: nowrap;
                    width: 100%;
                    box-sizing: border-box;
                    border: 1px solid transparent;
                    line-height: 1;

                    .arrow {
                        flex: 0 0 auto;
                        display: block;
                        width: 14px;
                        height: 14px;
                    }
                }
            }
        }

        .row-middle {
            flex: 1;
            display: flex;
            align-items: center;
            min-width: 0;
        }

        .search-input {
            flex: 1;
            min-width: 0;
            background-color: #fff;
            border: none;
            height: 26px;
            line-height: 26px;
            padding: 6px 10px;
            outline: none;

            &:disabled {
                opacity: 1;
                cursor: not-allowed;
                -webkit-text-fill-color: inherit;
            }

            &::placeholder {
                font-size: 14px;
                color: #9ca3af;
            }
        }

        .search-submit {
            width: 58px;
            height: 32px;
            margin-right: 3px;
            background: var(--pc-header-search-accent, #1a1a1a);
            border: none;
            border-radius: 4px;
            color: #ffffff;
            cursor: pointer;
            text-align: center;
            font-weight: normal;
            font-size: 22px;
            line-height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }
    }
}

.pc-header-simple {
    .top-search {
        .search-submit--text {
            font-size: 12px;
            font-weight: normal;
        }
    }
}

.pc-cart__pic {
    height: 100%;
    width: auto;
    object-fit: contain;
}
</style>
