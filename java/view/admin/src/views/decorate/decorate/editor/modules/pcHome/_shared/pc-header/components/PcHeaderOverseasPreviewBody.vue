<template>
    <div
        class="page-container"
        :class="{
            'page-container--bottom-none': bottomDividerType === 0,
            'page-container--bottom-shadow': bottomDividerType === 1,
            'page-container--bottom-border': bottomDividerType === 2
        }"
    >
        <div class="page-c-warap">
            <div class="page-header-home__left">
                <div class="logo-slot" :class="{ 'logo-slot--wide': !showCategory }">
                    <div class="logo" :class="logoAlignClass">
                        <div class="logo-area">
                            <img :src="logoSrc" :style="logoImgStyle" alt="" />
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
            <div class="page-header-home__center">
                <div class="search-nav-box">
                    <div class="search-bar" :style="topSearchStyle">
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
                                    <div type="submit" class="search-submit">
                                        <span class="pc-icon pc-icon--search"></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <PcHeaderNavPreviewBar
                        v-if="showMainNav"
                        :padding-top="mainNavPaddingTop"
                        :padding-bottom="mainNavPaddingBottom"
                        :items="navItems"
                    />
                </div>
            </div>
            <div class="page-header-home__right">
                <div class="language-bar">
                    <div class="lang-box">
                        <span class="pc-icon pc-icon--language language"></span>
                        <div class="lang-text">EN</div>
                        <span class="pc-icon pc-icon--arrow arrow"></span>
                    </div>
                </div>
                <div class="mine-bar">
                    <div class="category-wrap">
                        <div class="bar-wrap">
                            <div class="content">
                                <div class="icon">
                                    <span class="pc-icon pc-icon--mine"></span>
                                </div>
                                <div class="txt">我的</div>
                                <div class="icon ml">
                                    <span class="pc-icon pc-icon--arrow arrow"></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div v-if="showRightCart" class="car-bar">
                    <div class="content">
                        <template v-if="cartStyle === 2">
                            <img v-if="cartPicThumb" class="cart-img" :src="cartPicThumb" alt="" />
                            <div v-else class="cart-img-empty">购物车图</div>
                        </template>
                        <template v-else>
                            <div class="icon">
                                <span class="pc-icon pc-icon--cart"></span>
                            </div>
                            <div class="count">18</div>
                            <div class="txt">购物车</div>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import PcHeaderNavPreviewBar from "./PcHeaderNavPreviewBar.vue";

defineProps<{
    logoSrc: string;
    logoImgStyle: Record<string, string>;
    logoAlignClass: string;
    showCategory: boolean;
    categoryMarginTop: number;
    showMainNav: boolean;
    mainNavPaddingTop: number;
    mainNavPaddingBottom: number;
    showRightCart: boolean;
    cartStyle: 1 | 2;
    cartPicThumb: string;
    topSearchStyle: Record<string, string>;
    bottomDividerType: 0 | 1 | 2;
    navItems?: string[];
}>();
</script>

<style lang="less" scoped>
@import "./_pcHeaderPreviewIcons.less";

.page-container {
    box-sizing: border-box;
    width: 100%;
    min-height: 100px;
    height: auto;
    background: #fff;
    position: relative;
    /* 低于 benefit；整块预览抬层交给 .dec-pc-header-preview */
    z-index: 1;
    box-shadow: none;

    .page-c-warap {
        display: grid;
        align-items: start;
        grid-template-columns: max-content minmax(0, 1fr) max-content;
        column-gap: 16px;
        margin: 0 auto;
        min-height: 60px;
        min-width: 0;
        width: 100%;
        box-sizing: border-box;
        padding: 16px 0 0;

        .page-header-home__left {
            display: flex;
            align-items: center;
            justify-content: flex-start;
            gap: 16px;
            height: 60px;
            min-width: 0;
            overflow: visible;
        }

        .page-header-home__center {
            min-width: 0;
            display: flex;
            align-items: flex-start;
        }

        .search-nav-box {
            width: 100%;
            min-width: 0;
            display: flex;
            flex-direction: column;
        }

        .page-header-home__right {
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 14px;
            height: 60px;
            min-width: 0;
            overflow: visible;
        }

        .logo-slot {
            flex-shrink: 0;
            width: 160px;
            min-width: 160px;
            max-width: 160px;
        }

        .logo-slot--wide {
            width: 250px;
            min-width: 250px;
            max-width: 250px;
        }

        .logo {
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

        .logo-area {
            height: 60px;
            display: flex;
            align-items: center;
            flex-shrink: 0;
            overflow: hidden;
            transform: none;
        }

        .logo img {
            max-width: 100%;
            max-height: 100%;
        }

        .category-nav {
            min-width: 74px;
            height: 100%;
            white-space: nowrap;
            flex-shrink: 0;
        }

        .search-bar {
            width: 100%;
            min-width: 0;
            min-height: 60px;
            height: 60px;
            display: flex;
            align-items: center;
            position: relative;
            overflow: visible;
        }

        .language-bar {
            display: flex;
            align-items: center;
            min-width: 70px;
            height: 100%;
        }

        .mine-bar {
            min-width: 72px;
            height: 100%;
            white-space: nowrap;
        }

        .car-bar {
            display: flex;
            align-items: center;
            justify-content: flex-end;
            min-width: 96px;
            height: 100%;
            white-space: nowrap;

            .content {
                position: relative;
                cursor: pointer;
                display: flex;
                height: 40px;
                width: 100%;

                .cart-img {
                    align-self: center;
                    width: 40px;
                    height: 40px;
                    object-fit: contain;
                    border-radius: 4px;
                }

                .cart-img-empty {
                    align-self: center;
                    width: 100%;
                    height: 40px;
                    border-radius: 4px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 12px;
                    color: #9ca3af;
                    background: #f3f4f6;
                    border: 1px solid #ededed;
                    box-sizing: border-box;
                    user-select: none;
                }

                &:hover {
                    .icon {
                        color: var(--primary-dark-2);
                    }

                    .txt {
                        color: var(--primary-dark-2);
                    }
                }

                .icon {
                    align-self: center;
                    width: 20px;
                    height: 20px;
                    border-radius: 4px;
                    background: transparent;
                    color: #1a1a1a;
                }

                .txt {
                    align-self: center;
                    margin-left: 6px;
                    font-size: 14px;
                    font-weight: 500;
                    color: #1a1a1a;
                }

                .count {
                    box-sizing: border-box;
                    align-items: center;
                    background: #cc4b11;
                    border-radius: 100px;
                    border: 1px solid #fff;
                    color: #fff;
                    font-size: 10px;
                    justify-content: center;
                    top: 3px;
                    left: 8px;
                    min-width: 16px;
                    padding: 0 3px;
                    position: absolute;
                    text-align: center;
                    display: flex;
                    height: 16px;
                    line-height: 16px;
                }
            }
        }
    }
}

.page-container--bottom-shadow {
    box-shadow:
        0 2px 12px 0 rgba(0, 0, 0, 0.0392),
        0 4px 6px 0 rgba(0, 0, 0, 0.0588);
}

.page-container--bottom-none {
    border-bottom: none !important;
    box-shadow: none !important;
}

.page-container--bottom-border {
    border-bottom: 1px solid #ededed;
}

/* AllCategoryNew / Mine：bar-wrap */
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

/* LanguageChangeNew：lang-box */
.lang-box {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: #1a1a1a;

    .language {
        margin-right: 8px;
    }

    .lang-text {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-left: 0;
        font-size: 14px;
        font-weight: 500;
    }

    .arrow {
        margin-left: 6px;
        transition: all 0.2s;
        display: inline-flex;
        align-self: end;
    }

    &:hover {
        color: var(--primary-dark-2);

        .arrow {
            color: var(--primary-dark-2);
        }
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
</style>
