<template>
    <!-- 单根包裹：避免多根时 tig-popup 被拍平进 num-card，多出一次 row-gap -->
    <view>
        <view class="points" @click="handlePoints">
            <view class="points-title"
                >{{ $t(configStore.integralName) }}
                <view class="points-text"
                    >{{ `${$t("当前账户")}${configStore.integralName}` }}：<text class="special-text">{{ points }}</text>
                </view>
            </view>
            <view class="points-content">
                <view v-if="pointsAmount > 0" class="points-text">-{{ isOverseas() ? currency + displayPointsAmount : currency + pointsAmount }}</view>
                <view v-else-if="points === 0 && pointsAmount === 0" class="points-text">{{ `${$t("暂无可用")}${configStore.integralName}` }}</view>
                <view class="iconfont-h5 icon-fanhui" />
            </view>
        </view>

        <tig-popup v-model:show="showPoints" background-color="#f5f5f5">
            <view class="points-popup">
                <view class="popup-title">{{ $t(configStore.integralName) }}</view>
                <view class="form-usePoints">
                    <uni-forms ref="pointsForm" :model-value="formData">
                        <uni-forms-item label-width="80" :label="$t('使用') + configStore.integralName" name="usePoints">
                            <uni-easyinput
                                v-model="formData.usePoints"
                                primary-color="rgb(192, 196, 204)"
                                :input-border="false"
                                type="number"
                                :placeholder="$t('请输入') + configStore.integralName"
                            />
                        </uni-forms-item>
                    </uni-forms>
                </view>
                <view class="points-popup-text">{{ $t("该订单最多可用") }} {{ props.availablePoints }}</view>
                <tig-fixed-placeholder background-color="#fff">
                    <view class="btn-box">
                        <tig-button2 style="height: 70rpx" theme @click="onSubmit"> {{ $t("确定") }}</tig-button2>
                    </view>
                </tig-fixed-placeholder>
            </view>
        </tig-popup>
    </view>
</template>

<script setup lang="ts">
/**
 * 积分抵扣卡片组件
 * - 展示当前账户积分与本单抵扣金额
 * - 弹层内输入本单使用的积分数量
 */
import { computed, reactive, ref, watch } from "vue";
import { useConfigStore } from "@/store/config";
import { useCurrencyStore } from "@/store/currency";
import { useCurrency } from "@/hooks/useCurrency";
import { useI18n } from "vue-i18n";
import { isOverseas, moneyFormat } from "@/utils";

const { t } = useI18n();

const configStore = useConfigStore();
const currencyStore = useCurrencyStore();

const { currency } = useCurrency();

interface Props {
    points: number;
    availablePoints: number;
    pointsAmount: number;
    usePoint: number;
}

const props = defineProps<Props>();
const emit = defineEmits(["update:usePoint", "change"]);

// 跨境积分减免金额：按当前币种汇率换算后展示；国内走裸 pointsAmount 分支，此计算不参与展示
const displayPointsAmount = computed(() => {
    let num = String(props.pointsAmount);
    if (isOverseas() && (currencyStore.currentCurrencyData as any)?.rate) {
        num = moneyFormat((Number(props.pointsAmount) * Number((currencyStore.currentCurrencyData as any).rate)).toString());
    }
    return num;
});

const showPoints = ref(false);
const formData = reactive({
    usePoints: 0
});
const pointsForm = ref();
const rules = {
    usePoints: {
        rules: [
            { required: true, errorMessage: `${t("请输入")}${configStore.integralName}` },
            {
                validateFunction: function (rule: any, value: number, data: any, callback: any) {
                    if (value > props.availablePoints) callback(`${configStore.integralName}${t("不能大于可用")}${configStore.integralName}`);
                    if (value < 0) callback(`${configStore.integralName}${t("不能小于0")}`);
                    return true;
                }
            }
        ]
    }
};

watch(showPoints, (value) => {
    if (value) {
        setTimeout(() => {
            pointsForm.value.setRules(rules);
        }, 1000);
    }
});

const handlePoints = () => {
    if (props.points === 0 && props.pointsAmount === 0) {
        return uni.showToast({
            title: `${t("暂无可用")}${configStore.integralName}`,
            duration: 1500,
            icon: "none"
        });
    }
    showPoints.value = true;
    formData.usePoints = props.usePoint;
};

const onSubmit = () => {
    pointsForm.value
        .validate()
        .then((_res: any) => {
            if (!formData.usePoints) {
                formData.usePoints = 0;
            }
            emit("update:usePoint", Number(formData.usePoints));
            emit("change");

            showPoints.value = false;
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};
</script>

<style lang="scss" scoped>
.btn-box {
    padding: 11rpx 28rpx;
}

.points {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 56rpx;
    background-color: #fff;
    font-size: 24.5rpx;

    .points-content {
        display: flex;
        align-items: center;
        justify-content: center;
        column-gap: 6rpx;

        .points-text {
            font-size: 24.5rpx;
            max-width: 300rpx;
            color: var(--general);
        }
    }

    .icon-fanhui {
        font-size: 17rpx;
        color: #888b94;
        transform: rotate(180deg);
    }
}

.isZh {
    .points-title {
        .points-text {
            max-width: 150rpx;
        }
    }
}

.points-title {
    display: flex;

    .points-text {
        font-size: 22rpx;
        padding-left: 15rpx;
        display: flex;
        align-items: flex-end;
    }
}

.special-text {
    color: var(--general);
}

.points-popup {
    height: 50vh;
    border-radius: 18rpx 18rpx 0 0;
    overflow: hidden;
    font-size: 24.5rpx;
    color: #1a1a1a;

    .points-popup-text {
        margin-left: 40rpx;
        position: relative;
        top: -10rpx;
        color: rgb(136, 139, 148);
        font-size: 24rpx;

        /* #ifdef MP-WEIXIN */
        top: 20rpx;
        /* #endif */
    }

    .form-usePoints {
        background-color: #fff;
        margin: 20rpx 20rpx 0;
        padding: 0 20rpx;
        :deep(.uni-forms-item__label) {
            height: 50px;
        }
        :deep(.uni-easyinput) {
            height: 100%;
            .uni-easyinput__content {
                height: 100%;
            }
        }
    }
}
</style>
