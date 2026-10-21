<template>
    <view>
        <tig-layout>
            <view class="returnaddress">
                <view class="title">{{ $t("回寄信息") }}</view>
                <view class="returnaddress-content">{{ returnAddress }}</view>
            </view>
            <view class="leave-message">
                <uni-forms ref="formRef" :model-value="form" label-width="170rpx" :rules="rules">
                    <uni-forms-item :label="$t('快递公司')" name="logisticsName">
                        <uni-easyinput
                            v-model="form.logisticsName"
                            primary-color="rgb(192, 196, 204)"
                            :input-border="false"
                            :placeholder="$t('请您填写快递公司')"
                        />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('快递单号')" name="trackingNo">
                        <uni-easyinput
                            v-model="form.trackingNo"
                            primary-color="rgb(192, 196, 204)"
                            :input-border="false"
                            :placeholder="$t('请您填写快递单号')"
                        />
                    </uni-forms-item>
                </uni-forms>
            </view>
            <view class="leave-message-btn">
                <view class="btn-box" @click="handleSubmit">{{ $t("提交") }}</view>
            </view>
        </tig-layout>
    </view>
</template>

<script setup lang="ts">
/**
 * 售后寄货页
 * - 展示回寄地址信息
 * - 填写快递公司和快递单号
 * - 提交后跳转售后详情页
 */
import { ref, reactive } from "vue";
import { aftersalesFeedback } from "@/api/user/afterSale";
import { onLoad } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const form = reactive({
    logisticsName: "",
    trackingNo: "",
    id: 0
});
const returnAddress = ref("");

const rules = {
    logisticsName: {
        rules: [{ required: true, errorMessage: t("快递公司不能为空") }]
    },
    trackingNo: {
        rules: [{ required: true, errorMessage: t("快递单号不能为空") }]
    }
};

onLoad((options) => {
    if (options) {
        if (options.id) {
            form.id = options.id;
        }
        if (options.returnAddress) {
            returnAddress.value = options.returnAddress;
        }
    }
});

const formRef = ref();
const handleSubmit = () => {
    formRef.value
        .validate()
        .then(() => {
            __aftersalesFeedback();
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

const __aftersalesFeedback = async () => {
    try {
        const result = await aftersalesFeedback(form);

        uni.redirectTo({
            url: "/pages/user/afterSale/info",
            success: () => {
                uni.$emit("refreshInfo", form.id);
            }
        });
    } catch (error) {
        console.error(error);
    }
};
</script>

<style lang="scss" scoped>
.returnaddress {
    background-color: #fff;
    padding: 20rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
    width: 100%;
    display: flex;
    flex-direction: column;

    .title {
        width: 100%;
        color: #000;
        font-weight: 500;
        padding: 10rpx 0;
    }

    .returnaddress-content {
        width: 100%;
        font-size: 24rpx;
        line-height: 40rpx;
    }
}

.leave-message {
    background-color: #fff;
    height: 100%;
}
:deep(.special-item) {
    &.uni-forms-item {
        border-bottom: none;
        margin-bottom: 0;
    }
}
.uni-forms-item {
    margin: 10rpx 25rpx 10rpx 25rpx;
    padding: 20rpx 0;
    border-bottom: 1px solid #eee;
    &:last-child {
        border-bottom: none;
        padding-bottom: 20rpx;
    }
}
.leave-message-btn {
    margin-top: 30rpx;
    padding: 0 20rpx;

    .btn-box {
        width: 100%;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: var(--general);
        color: #fff;

        &:active {
            opacity: 0.7;
        }
    }
}

:deep(.uni-forms-item__error) {
    top: 70%;
    left: 3.5%;
}
</style>
