/** * @file 购物设置页 * @description 配置购物流程相关的全局参数 * @features * - 运费模板地区配送机制设置 * - 积分名称和使用规则配置 * - 购物车和结算流程设置 *
- 订单备注和发票设置 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="170px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t("购物全局设置") }}</div>
                    <el-form-item :label="$t('运费模板地区设置机制')" prop="childAreaNeedRegion">
                        <el-radio-group v-model="formState.childAreaNeedRegion" class="itemWidth">
                            <el-radio :value="1">{{ $t("仅设置的地区可配送") }}</el-radio>
                            <el-radio :value="0">{{ $t("未设置的地区皆可配送(使用默认运费设置)") }}</el-radio>
                        </el-radio-group>
                        <div>
                            <div class="extra">
                                {{
                                    $t(
                                        "运费模板一旦增加指定地区运费，如果该设置为“仅设置的地区可配送”，将不再默认全国都可配送，而是只能配送到增加的地区（如果收货地址不在设置范围，该配送类型不可用）；"
                                    )
                                }}
                            </div>
                            <div class="extra">
                                {{
                                    $t(
                                        "如果设置为“未设置的地区使用默认运费设置”，收货地址如果是设置的地区范围内，则按该地区的运费计费，如果不在这范围内，则按默认计费（相当于不会存在不能配送的区域）；"
                                    )
                                }}
                            </div>
                            <div class="extra">
                                {{
                                    $t(
                                        "如何选择？如果所有商品可全国配送，就选“未设置的地区使用默认运费设置”，如果有的商品只配送给部分区域，则选前者，该设置会对全站商品生效，慎重设置！"
                                    )
                                }}
                            </div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t("积分设置") }}</div>
                    <el-form-item :label="$t('积分自定义名称')" prop="integralName">
                        <div class="flex flex-align-center" style="gap: 5px">
                            {{ $t("将积分名称自定义为") }} <TigInput width="120px" v-model="formState.integralName" :maxlength="5" showWordLimit />
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('积分换算比例')" prop="integralScale">
                        <div class="flex flex-align-center" style="gap: 5px">
                            {{ $t("每 100 积分，可抵扣") }}
                            <TigInput type="decimal" width="90px" :decimalPlaces="1" v-model="formState.integralScale"> </TigInput
                            >{{ $t("{amountUnit}现金", { amountUnit: config.amountUnit }) }}
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('赠送积分')" prop="orderSendPoint">
                        <div class="flex flex-align-center" style="gap: 5px">
                            {{ $t("会员订单") }}
                            <el-select v-model="formState.orderSendPointNode" style="width: 180px" :placeholder="$t('选择下单送积分节点')" clearable>
                                <el-option :value="1" :label="$t('待发货')" />
                                <el-option :value="2" :label="$t('已发货')" />
                                <el-option :value="3" :label="$t('已完成')" />
                            </el-select>
                            {{ $t("订单，")
                            }}<template v-if="formState.orderSendPointNode === 3"
                                ><TigInput width="60px" type="integer" v-model="formState.orderCompleteSendPointDays" />{{ $t("天") }}</template
                            >{{ $t("赠送积分， 每消费 1{amountUnit}，可获得", { amountUnit: config.amountUnit })
                            }}<TigInput width="90px" type="integer" v-model="formState.orderSendPoint" /> {{ $t("积分") }}
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('积分支付比例')" prop="integralPercent">
                        <div class="flex flex-align-center" style="gap: 5px">
                            {{ $t("每") }}
                            <TigInput
                                width="90px"
                                type="decimal"
                                :decimalPlaces="2"
                                :fixedDecimal="false"
                                :min="0.01"
                                v-model="formState.integralPercentBaseAmount"
                            />
                            {{ $t("{amountUnit}商品，最多可使用", { amountUnit: config.amountUnit }) }}
                            <TigInput width="90px" type="decimal" :decimalPlaces="1" v-model="formState.integralPercent" />
                            {{ $t("积分") }}
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('评论商品赠送积分')" prop="commentSendPoint">
                        <div class="flex flex-align-center" style="gap: 5px">
                            {{ $t("每次有效评论，赠送") }} <TigInput width="90px" type="integer" v-model="formState.commentSendPoint" /> {{ $t("积分") }}
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('晒单赠送积分')" prop="showSendPoint">
                        <div class="flex flex-align-center" style="gap: 5px">
                            {{ $t("每次有效晒单，赠送") }} <TigInput width="90px" type="integer" v-model="formState.showSendPoint" /> {{ $t("积分") }}
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('签到赠送积分')" prop="useQiandaoPoint">
                        <el-radio-group v-model="formState.useQiandaoPoint" class="itemWidth">
                            <el-radio :value="1">{{ $t("开启") }}</el-radio>
                            <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                        </el-radio-group>
                        <div class="extra">
                            {{ $t("开启后，用户可通过签到获取积分。") }}
                            <el-popover :width="275" placement="right" trigger="click">
                                <template #reference>
                                    <a>{{ $t("查看示例") }}</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/useQiandaoPoint.png" style="width: 250px" />
                                </template>
                            </el-popover>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('连续签到')" prop="continuousSignIn">
                        <el-radio-group v-model="formState.continuousSignIn" class="itemWidth">
                            <el-radio :value="1">{{ $t("开启") }}</el-radio>
                            <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                        </el-radio-group>
                        <div class="extra">
                            {{ $t("开启后漏签将重置签到天数；关闭后漏签不会影响签到进度。同一天只能签到一次，达到最大签到天数后将重新从第 1 天开始。") }}
                        </div>
                    </el-form-item>
                </div>

                <div class="content_wrapper">
                    <div class="title">{{ $t("发票设置") }}</div>
                    <el-form-item :label="$t('是否能开发票')" prop="canInvoice">
                        <el-radio-group v-model="formState.canInvoice" class="itemWidth">
                            <el-radio :value="1">{{ $t("是") }}</el-radio>
                            <el-radio :value="0">{{ $t("否") }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t("如果为否，订单结算时将不显示发票") }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('是否支持增值税专用发票')" prop="invoiceAdded">
                        <el-radio-group v-model="formState.invoiceAdded" class="itemWidth">
                            <el-radio :value="1">{{ $t("是") }}</el-radio>
                            <el-radio :value="0">{{ $t("否") }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t("开启增值税专用发票会同步开启发票资质提交和审核，关闭则只显示“普通发票”") }}</div>
                    </el-form-item>
                </div>

                <div class="content_wrapper">
                    <div class="title">{{ $t("退换货设置") }}</div>
                    <el-form-item :label="$t('回寄联系人设置')" prop="returnConsignee">
                        <TigInput classType="tig-form-input" v-model="formState.returnConsignee" />
                    </el-form-item>
                    <el-form-item :label="$t('回寄电话设置')" prop="returnMobile">
                        <TigInput classType="tig-form-input" v-model="formState.returnMobile" />
                    </el-form-item>
                    <el-form-item :label="$t('回寄地址设置')" prop="returnAddress">
                        <TigInput classType="tig-form-input" v-model="formState.returnAddress" />
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
        </a-spin>
    </div>
    <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
        <div class="selected-action">
            <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t("提 交") }}</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, nextTick, watch } from "vue";
import { message } from "ant-design-vue";
import { BaseShoppingConfig, Regions } from "@/types/setting/config";
import { getConfigShopping, saveConfigShopping } from "@/api/setting/config";
import { useRoute } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useConfigStore } from "@/store/config";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const { themeInfo } = useThemeStore();
const config = useConfigStore().config;
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<BaseShoppingConfig>({
    childAreaNeedRegion: null,
    integralName: "",
    integralScale: "",
    orderSendPoint: "",
    orderSendPointNode: 3,
    orderCompleteSendPointDays: "",
    integralPercentBaseAmount: "100",
    integralPercent: "",
    commentSendPoint: "",
    showSendPoint: "",
    useQiandaoPoint: null,
    continuousSignIn: null,
    canInvoice: null,
    invoiceAdded: null,
    returnConsignee: "",
    returnMobile: "",
    returnAddress: ""
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigShopping();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        if (!formState.value.integralPercentBaseAmount) {
            formState.value.integralPercentBaseAmount = "100";
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await saveConfigShopping(formState.value);
        message.success(t("修改成功"));
        configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.content {
    padding: 0 20px;
}
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}

.itemWidth {
    width: 100%;
}
.error {
    color: red;
}
</style>
