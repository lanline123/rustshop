<template>
    <div class="container">
        <div class="content_wrapper">
            <RadioType
                v-if="!loading"
                width="330px"
                :radioType="2"
                v-model:modelValue="formState.settlementType"
                :radioList="[
                    {
                        key: 1,
                        title: t('系统自动结算'),
                        desc: t('佣金结算时，订单佣金将自动从店铺账户余额中扣除发放到分销员的零钱账户，分销员即可提现。'),
                        tips: t('效率高，适合分销员为外部人员的商家')
                    },
                    {
                        key: 2,
                        title: t('线下人工结算'),
                        desc: t('系统仅做业绩统计，商家需和分销员线下确认佣金结算时间和佣金支付方式（例如“微信转账”等）。'),
                        tips: t('更灵活，适合分销员为内部员工的商家')
                    }
                ]"
            >
            </RadioType>
            <div class="mt20">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item v-if="formState.settlementType === 1" prop="exampleName" :label="$t('自动结算时间：')">
                        <el-radio-group class="itemWidth" v-model="formState.dateType">
                            <el-radio :value="0" style="width: 100%">
                                <div class="flex flex-align-center warp"> {{ $t('订单确认收货后第15天结算') }} <p class="label">{{ $t('无亏损风险') }}</p>
                                </div>
                                <div class="tips">{{ $t('当前订单客户无法再发起退款，可避免订单退款但佣金已结算的亏损。自动结算时间一般为7+15天。') }}</div>
                            </el-radio>
                            <el-radio :value="1" style="width: 100%">
                                <div class="flex flex-align-center warp"> {{ $t('订单确认收货，立即结算') }} <p class="label">{{ $t('结算时间短') }}</p>
                                </div>
                                <div class="tips"> {{ $t('风险提示：可能会产生订单退款但佣金已发放的亏损 。若想进一步加快结算时间，可在订单处设置更短的自动确认收货时间。') }} </div>
                            </el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="formState.settlementType === 2" prop="exampleName" :label="$t('人工结算说明：')">
                        <div v-if="formState.settlementType === 2">
                            <TigInput v-model="formState.desc" width="350px" :rows="5" type="textarea" :placeholder="$t('请输入结算说明, 不超过200字')" />
                            <div class="extra">{{ $t('结算说明将展示在商品推广订单中，若为空则不展示') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item label=" ">
                        <el-button type="primary" @click="handleSubmit">{{ $t('保存') }}</el-button>
                    </el-form-item>
                    <!-- <el-form-item prop="isShow" label="销售额统计：">
                    <el-radio-group v-model="formState.isShow">
                        <el-radio :value="1">
                            <div class="flex flex-align-center">
                                <p>仅统计参与推广商品</p>
                                <el-tooltip placement="bottom-start" effect="light">
                                    <template #content>
                                        <div style="width:300px;">
                                        选择仅统计参与推广商品，在分销员销售额、团队业绩统计、团队管理-团队设置-升级条件、等级值-推广金额统计、等级值-推广订单数、排行榜-按销售额排名、试用期-销售额、任务奖励-销售额、任务奖励-订单数 会剔除不参与推广商品的业绩统计
                                        </div>
                                    </template>
                                    <el-icon size="16" color="#c8c9cc" style="margin-left: 5px;"><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                        </el-radio>
                        <el-radio :value="0">统计所有商品</el-radio>
                    </el-radio-group>
                </el-form-item> -->
                </el-form>
            </div>
            <!-- <div class="footer" v-if="!loading">
            <el-button type="primary" @click="handleSubmit">保存</el-button>
        </div> -->
        </div>
    </div>
</template>
<script lang="ts" setup>
import RadioType from "@/components/radio/src/RadioType.vue";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import { getSalesmanSettlementConfig, saveSalesmanSettlementConfig } from "@/api/salesman/performanceSettlement";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref<boolean>(false);
const formState = ref<any>({
    settlementType: 1,
    dateType: 0,
    desc: ""
});
const handleSubmit = async () => {
    try {
        const result = await saveSalesmanSettlementConfig({ ...formState.value });
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    }
};
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getSalesmanSettlementConfig();
        formState.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.container {
    padding: 20px;
    :deep(.radio-style .triangle) {
        &::after {
            right: 3px;
            top: 19px;
            font-size: 18px;
        }
    }
    .tit {
        margin-top: 40px;
        margin-bottom: 20px;
    }
    :deep(.el-radio) {
        height: auto;
        align-items: flex-start;
    }
    :deep(.el-radio__input) {
        padding-top: 8px;
    }
    .label {
        background-color: var(--tig-item-active-bg);
        color: var(--tig-primary);
        padding: 0 5px !important;
        height: 20px;
        line-height: 20px;
        margin-left: 10px;
        border-radius: 2px;
    }
    .tips {
        color: rgb(150, 151, 153);
        font-size: 12px;
        white-space: normal; /* 确保文本可以换行 */
        word-break: break-all; /* 允许在单词内换行 */
    }
}
.footer {
    width: 100%;
    position: absolute;
    bottom: 0;
    left: 0;
    justify-content: center;
    height: 30px;
    line-height: 30px;
    background-color: #fff;
    box-shadow: 0 -2px 8px 0 rgba(200, 201, 204, 0.2);
    z-index: 10;
}
@media (max-width: 768px) {
    .container {
        .warp {
            width: 40vw;
            flex-wrap: wrap;
        }
    }
}
</style>
