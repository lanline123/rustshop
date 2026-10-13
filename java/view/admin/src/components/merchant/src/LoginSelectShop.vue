<template>
    <div class="select-shop-warp">
        <div class="warp-info">
            <div class="back-btn">
                <div class="bark" @click="back()"><i class="iconfont-admin icon-zuojiantou-copy"></i>{{ $t('返回') }}</div>
            </div>
            <div class="title-name">
                <div class="left-title">{{ $t('您可以进入以下管理后台') }}</div>
            </div>
            <div class="sub-title-name" v-if="encryptedUsername">
                <span>{{ encryptedUsername }}</span> {{ $t('已绑定了以下组织，您可以进入以下任一组织') }} </div>
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px; height: 200px" />
            <div class="shop-list-warp">
                <!-- 管理后台 -->
                <div v-if="!loading && adminType === 'admin'" class="shop-list">
                    <div @click="toIndex(0, 'admin')" class="shop-item admin-item">
                        <div class="left-item">
                            <img class="left-image" src="@/assets/logo/shop_logo_b.png" alt="" />
                            <div class="left-info">
                                <div class="left-title"> {{ $t('管理后台') }} <span v-if="encryptedUsername">（{{ encryptedUsername }}）</span>
                                </div>
                            </div>
                        </div>
                        <div class="right-item">
                            <span class="iconfont-admin icon-login-youjiantou"></span>
                        </div>
                    </div>
                </div>
                <!-- 店铺/门店/自提点列表 -->
                <div v-if="myShopList.length > 0" class="shop-list" :class="{ 'shop-list-min-height': isOtherListsEmpty('shop') }">
                    <div v-if="!loading" @click="toIndex(item.shopId, 'shop')" v-for="item in myShopList" :key="item.shopId" class="shop-item">
                        <div class="left-item">
                            <el-image class="left-image" :src="getShopLogo(item.shopType)"></el-image>
                            <div class="left-info">
                                <div class="left-title">
                                    <p class="label" :class="shopTypeConfig[item.shopType]?.class">{{ shopTypeConfig[item.shopType]?.label }}</p>
                                    <p class="line1">{{ item.shopTitle }}</p>
                                </div>
                            </div>
                        </div>
                        <div class="right-item">
                            <span class="iconfont-admin icon-login-youjiantou"></span>
                        </div>
                    </div>
                </div>
                <!-- 供应商列表 -->
                <div v-if="vendorList.length > 0" class="shop-list" :class="{ 'top': isOtherListsEmpty('vendor'), 'shop-list-min-height': isOtherListsEmpty('vendor') }">
                    <div v-if="!loading" @click="toIndex(item.vendorId, 'vendor')" v-for="item in vendorList" :key="item.vendorId" class="shop-item">
                        <div class="left-item">
                            <el-image class="left-image" :src="logoMap.vendor"></el-image>
                            <div class="left-info">
                                <div class="left-title">
                                    <p class="label vendor">{{ $t('供应商') }}</p>
                                    <p class="line1">{{ item.vendorName }}</p>
                                </div>
                            </div>
                        </div>
                        <div class="right-item">
                            <span class="iconfont-admin icon-login-youjiantou"></span>
                        </div>
                    </div>
                </div>
                <!-- 空状态 -->
                <div v-if="showEmptyState" class="shop-list shop-list-min-height">
                    <el-empty :description="$t('您还没有已绑定的组织哦～')" />
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { computed } from "vue";
import shopLogo from "@/assets/logo/shop_logo.png";
import gysLogo from "@/assets/logo/gys.png";
import ztdLogo from "@/assets/logo/ztd.png";
import posLogo from "@/assets/logo/pos_logo_a.png";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    selectShopFlag: {
        type: Boolean,
        default: false
    },
    myShopList: {
        type: Array as () => any[],
        default: () => []
    },
    vendorList: {
        type: Array as () => any[],
        default: () => []
    },
    posList: {
        type: Array as () => any[],
        default: () => []
    },
    loading: {
        type: Boolean,
        default: false
    },
    userinfo: {
        type: Object,
        default: () => ({})
    },
    adminType: {
        type: String,
        default: ""
    }
});

// Logo 映射
const logoMap = {
    shop: shopLogo,
    vendor: gysLogo,
    pickup: ztdLogo,
    pos: posLogo
};

// 店铺类型配置
const shopTypeConfig: Record<number, { label: string; class: string }> = {
    1: { label: t('店铺'), class: "shop" },
    2: { label: t('门店'), class: "store" },
    3: { label: t('自提点'), class: "pickup" }
};

// 根据店铺类型获取 Logo
const getShopLogo = (shopType: number) => shopType === 3 ? ztdLogo : shopLogo;

// 判断除指定列表外其他列表是否都为空
const isOtherListsEmpty = (exclude: 'pos' | 'shop' | 'vendor') => {
    const lengths: Record<string, number> = {
        shop: props.myShopList.length,
        vendor: props.vendorList.length,
        pos: props.posList.length
    };
    return Object.entries(lengths)
        .filter(([key]) => key !== exclude)
        .every(([, len]) => len === 0);
};

// 是否显示空状态
const showEmptyState = computed(() => 
    !props.loading && 
    props.myShopList.length === 0 && 
    props.vendorList.length === 0 && 
    props.posList.length === 0 && 
    props.adminType !== "admin"
);

// 计算属性：加密后的用户名
const encryptedUsername = computed(() => {
    const username = props.userinfo?.username;
    return username ? encryptPhoneNumber(username) : "";
});

const emit = defineEmits(["closePopup", "update:selectShopFlag", "callBack"]);

const configStore = useConfigStore();
const toIndex = (id: number, type: string) => {
    emit("callBack", { id, type });
    configStore.updateConfig();
};

const { logout } = useUserStore();

const back = () => {
    emit("update:selectShopFlag", false);
    logout();
};

const encryptPhoneNumber = (username: string): string => {
    // 手机号正则
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (phoneRegex.test(username)) {
        return username.slice(0, 3) + "****" + username.slice(7);
    }
    return username;
};
</script>
<style scoped lang="less">
.select-shop-warp {
    background-color: #ffffff;
    padding: 20px 30px;
    box-sizing: border-box;

    .warp-info {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .back-btn {
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;

            .bark {
                display: flex;
                align-items: center;
                font-size: 16px;
                font-weight: 600;
                gap: 2px;
                cursor: pointer;

                i {
                    font-size: 14px;
                    padding-right: 5px;
                }
            }
        }

        .title-name {
            display: flex;
            justify-content: space-between;

            .bark {
                display: flex;
                align-items: center;
                font-size: 14px;
                font-weight: 600;
                gap: 2px;
                cursor: pointer;
            }

            .left-title {
                font-size: 22px;
                font-weight: bold;
            }

            .right-item {
                font-size: 14px;
            }
        }

        .sub-title-name {
            font-size: 14px;
            line-height: 1.5;

            & > span {
                font-weight: bold;
            }
        }

        .shop-list-warp {
            max-height: 500px;
            overflow: auto;

            &::-webkit-scrollbar {
                display: none;
            }
        }

        .shop-list {
            display: flex;
            flex-direction: column;
            gap: 8px;
            margin-bottom: 8px;

            .shop-item {
                cursor: pointer;
                border: 0.6px solid #e0e0e0;
                padding: 10px;
                display: flex;
                flex-direction: row;
                align-items: center;
                justify-content: space-between;
                border-radius: 10px;

                &.admin-item {
                    border-color: #ffeac7;
                    background-color: #fff4e6;
                    color: #4e4e4e;
                }

                .left-item {
                    flex: 1;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    gap: 14px;

                    .left-image {
                        width: 60px;
                        height: 60px;
                        border-radius: 10px;
                    }

                    .left-info {
                        display: flex;
                        flex-direction: column;
                        gap: 14px;

                        .left-title {
                            font-size: 16px;
                            line-height: 18px;
                            font-weight: bold;
                            display: flex;
                            align-items: center;

                            .line1 {
                                width: 200px;
                            }

                            .label {
                                height: 15px;
                                line-height: 15px;
                                padding: 0 2px;
                                font-size: 12px;
                                margin-right: 5px;
                                border-radius: 2px;
                                margin-top: 1px;
                                text-align: center;
                                border: 1px solid;

                                &.shop {
                                    min-width: 25px;
                                    color: #1456f0;
                                    border-color: #1456f0;
                                }

                                &.vendor {
                                    min-width: 35px;
                                    color: #a203fc;
                                    border-color: #a203fc;
                                }

                                &.pickup {
                                    min-width: 45px;
                                    color: #ff6104;
                                    border-color: #ff6104;
                                }

                                &.store {
                                    min-width: 25px;
                                    color: #52c41a;
                                    border-color: #52c41a;
                                }

                                &.pos {
                                    min-width: 35px;
                                    color: #fd2645;
                                    border-color: #fd2645;
                                }
                            }
                        }

                        .left-name {
                            display: flex;
                            align-items: center;
                            font-size: 14px;
                            color: #5b5b5b;
                            font-weight: 500;
                        }
                    }
                }

                .right-item {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
            }

            &.top {
                margin-top: -8px;
            }
        }

        .shop-list-min-height {
            min-height: 200px;
        }
    }
}

@media only screen and (max-width: 767px) {
    .select-shop-warp {
        padding: 0;
    }
    .line1 {
        width: 150px !important;
    }
}
</style>
