<template>
    <tig-layout :need-safe-top="true" safe-top-bg-color="#f5f5f5">
        <up-form ref="formRef" label-position="left" :model="personalData" :rules="rules">
            <view class="personal-title">{{ $t("个人信息") }}</view>
            <view class="personal-info">
                <up-form-item ref="itemRef" :label="$t('头像')" label-width="100" :border-bottom="true">
                    <view class="salesman-avatar">
                        <tig-image class="salesman-avatar__img salesman-avatar--round salesman-avatar__img--big" :src="personalData.avatar" />
                    </view>
                </up-form-item>
                <up-form-item ref="itemRef" :label="$t('昵称')" label-width="100" prop="nickname" :border-bottom="true">
                    <up-input v-model="personalData.nickname" border="none" :placeholder="$t('请输入昵称')" font-size="14" />
                </up-form-item>
                <!-- <up-form-item
                    label="名片名字"
                    labelWidth="100"
                    prop="cardname"
                    :borderBottom="true"
                    ref="itemRef"
                >
                    <up-input
                        v-model="personalData.cardname"
                        border="none"
                        placeholder="请输入名片名字"
                        fontSize="14"
                    ></up-input>
                </up-form-item>
                <up-form-item
                    label="微信号"
                    labelWidth="100"
                    :borderBottom="false"
                >
                    <up-input
                        v-model="personalData.wechatname"
                        border="none"
                        placeholder="请输入微信号"
                        fontSize="14"
                    ></up-input>
                </up-form-item> -->
            </view>
            <view class="personal-title">{{ $t("联系方式") }}</view>
            <view class="personal-info">
                <up-form-item ref="itemRef" :label="$t('手机号')" label-width="100" prop="mobile" :border-bottom="true">
                    <up-input v-model="personalData.mobile" border="none" :placeholder="$t('请输入手机号')" font-size="14" />
                </up-form-item>
                <!-- <up-form-item
                    label="微信二维码"
                    labelWidth="100"
                >
                    <view class="go-upload" @click="goPages('/pages/salesman/upQrcode')">
                        <view>去上传</view>
                        <view class="iconfont icon-xiangyou"></view>
                    </view>
                </up-form-item> -->
            </view>
            <!-- <view class="personal-title">销售权益</view>
            <view class="profit-item">
                <view class="profit-item__tag">分销员间绑定</view>
                <view class="profit-item__text">可与其他分销员绑定客户关系</view>
            </view> -->
        </up-form>
        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button class="salesman-btn" @click="onSubmit">{{ $t("保存") }}</tig-button>
            </view>
        </tig-fixed-placeholder>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 分销员个人信息页
 * - 编辑分销员名片信息
 * - 设置头像、昵称、联系方式
 */
import { ref, reactive } from "vue";
import { getSalesmanPersonal, updateSalesmanPersonal } from "@/api/salesman/salesmanPersonal";
import { onShow } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const personalData = reactive<any>({
    nickname: "",
    cardname: "",
    wechatname: "",
    mobile: ""
});

const rules = {
    mobile: {
        type: "string",
        required: true,
        message: t("请输入手机号"),
        trigger: ["blur", "change"]
    }
};

const _getSalesmanPersonal = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getSalesmanPersonal();
        Object.assign(personalData, result);
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const goPages = (url: string) => {
    uni.navigateTo({
        url
    });
};

const formRef = ref();
const onSubmit = () => {
    formRef.value
        .validate()
        .then(() => {
            save();
        })
        .catch(() => {
            uni.$u.toast(t("保存失败"));
        });
};

const save = async () => {
    try {
        await updateSalesmanPersonal({ ...personalData });
        uni.$u.toast(t("保存成功"));
        setTimeout(() => {
            uni.navigateBack({
                success: function (res) {
                    uni.$emit("refreshData");
                }
            });
        }, 1000);
    } catch (error: any) {
        console.error("表单错误信息：", error);
        uni.$u.toast(error.message);
    }
};

onShow(() => {
    _getSalesmanPersonal();
});
</script>

<style lang="scss" scoped>
.personal-title {
    height: 80rpx;
    box-sizing: border-box;
    padding: 32rpx 0 16rpx 32rpx;
    color: #969799;
    font-size: 24rpx;
    line-height: 32rpx;
}
.personal-info {
    background-color: #fff;
    padding: 0 32rpx;
}
.go-upload {
    display: flex;
    align-items: center;
    gap: 8rpx;
    color: #969799;
}
.profit-item {
    display: flex;
    align-items: center;
    padding: 24rpx 32rpx;
    gap: 32rpx;
    background-color: #fff;
    .profit-item__tag {
        position: relative;
        display: inline-flex;
        align-items: center;
        font-size: 24rpx;
        line-height: 32rpx;
        padding: 4rpx 12rpx;
        border-radius: 1998rpx;
        background: rgb(255, 240, 230);
        color: rgb(255, 114, 13);
    }
    .profit-item__text {
        line-height: 40rpx;
        vertical-align: middle;
        color: #969799;
        font-size: 26rpx;
    }
}
:deep(.u-line) {
    border-color: #ebedf0 !important;
}
:deep(.u-form-item__body__left__content__label) {
    font-size: 28rpx;
}
:deep(.u-form-item__body__right__content__slot) {
    justify-content: flex-end;
}
// 头像
.salesman-avatar {
    position: relative;
    display: inline-block;
    .salesman-avatar__img {
        display: block;
        margin: 0 auto;
    }
    .salesman-avatar--round {
        border-radius: 100%;
        overflow: hidden;
    }
    .salesman-avatar__img--default {
        height: 100rpx;
        width: 100rpx;
    }
    .salesman-avatar__img--big {
        height: 120rpx;
        width: 120rpx;
    }
}
.btn-box {
    padding: 25rpx;

    .salesman-btn {
        font-size: 28rpx;
        background: linear-gradient(45deg, rgb(255, 170, 30), rgb(255, 114, 13));
        border-color: #fff !important;
    }
}
</style>
