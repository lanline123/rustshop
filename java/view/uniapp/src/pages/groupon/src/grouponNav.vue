<template>
    <view class="groupon-nav">
        <tig-navbar :title="$t('我的拼团')" :custom-back="true" @back="handleBack">
            <template #right>
                <view class="groupon-nav__rigth-icon">
                    <view class="click-placehoder" @click="handleSearch" />
                    <text class="iconfont-h5 icon-sousuo" />
                </view>
            </template>
        </tig-navbar>
    </view>
</template>

<script lang="ts" setup>
/**
 * 拼团导航栏组件
 * - 拼团页面顶部导航
 * - 提供返回按钮
 */
import { redirect } from "@/utils";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

/**
 * 处理返回按钮点击，跳转到个人中心页面
 */
const handleBack = () => {
    redirect({
        url: "/pages/member/index",
        mode: "redirectTo"
    });
};

/**
 * 处理跳转搜索页面
 */
const handleSearch = () => {
    redirect({
        url: "/pages/search/index",
        param: {
            redirectPath: "/pages/groupon/mylist",
            searchHistoryKey: "mygrouponSerchHistory",
            navbarTitle: t("搜索我的拼团"),
            placeholder: t("点击搜索"),
            isSwitchTab: true
        }
    });
};
</script>

<style lang="scss" scoped>
.groupon-nav {
    .groupon-nav__rigth-icon {
        .click-placehoder {
            width: 84rpx;
            position: absolute;
            top: 0;
            right: 16rpx;
            bottom: 0;
        }

        .icon-sousuo {
            font-size: 40rpx;
        }
    }
}
</style>
