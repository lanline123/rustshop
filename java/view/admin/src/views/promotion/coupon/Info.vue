<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="120">
                    <el-form-item v-if="formState.isNewUser != 0" prop="couponType" :label="$t('优惠券类型')">
                        <RadioType
                            v-model:modelValue="formState.couponType"
                            :radioList="[
                                { key: 1, title: '满减券', desc: '例：满100减20' },
                                { key: 2, title: '折扣券', desc: '例：满100打8折' }
                            ]"
                        >
                        </RadioType>
                    </el-form-item>
                    <el-form-item prop="couponName" :label="$t('优惠券名称')">
                        <!-- <TigInput classType="tig-form-input" type="text" :maxlength="60" v-model="formState.couponName" /> -->
                        <BusinessData width="450px" v-model:modelValue="formState.couponName" :dataId="id" :dataType="10" :maxlength="60"></BusinessData>
                    </el-form-item>
                    <el-form-item prop="reduceType" :label="$t('优惠内容')">
                        <div class="tig-radio">
                            <el-radio-group v-model="formState.reduceType">
                                <div class="flex mb20">
                                    <el-radio :value="1"></el-radio>
                                    <div class="flex">
                                        <p class="mr10">{{ $t("满") }}</p>
                                        <div class="check-box">
                                            <TigInput
                                                :type="formState.couponUnit == 1 ? 'decimal' : 'integer'"
                                                v-model="formState.minOrderAmount"
                                                class="mr10"
                                                width="240px"
                                                :disabled="formState.reduceType == 2"
                                            >
                                                <template #append>
                                                    <div class="flex" v-if="formState.reduceType != 2">
                                                        <div class="ml10 item" :class="{ active: formState.couponUnit == 1 }" @click="formState.couponUnit = 1">
                                                            {{ config.config.amountUnit }}
                                                        </div>
                                                        <div class="ml10 item" :class="{ active: formState.couponUnit == 2 }" @click="formState.couponUnit = 2">
                                                            {{ $t("件") }}
                                                        </div>
                                                    </div>
                                                    <div class="flex disabled" v-else>
                                                        <div class="ml10 item" :class="{ active: formState.couponUnit == 1 }">{{ config.config.amountUnit }}</div>
                                                        <div class="ml10 item" :class="{ active: formState.couponUnit == 2 }">{{ $t("件") }}</div>
                                                    </div>
                                                </template>
                                            </TigInput>
                                        </div>
                                        <p class="mr10">, {{ formState.couponType == 1 ? $t("减") : $t("打") }}</p>
                                        <TigInput
                                            type="decimal"
                                            v-if="formState.couponType == 1"
                                            v-model="formState.couponMoney"
                                            width="180px"
                                            :disabled="formState.reduceType == 2"
                                        >
                                            <template #append>{{ config.config.amountUnit }}</template>
                                        </TigInput>
                                        <TigInput
                                            type="decimal"
                                            :decimalPlaces="1"
                                            v-if="formState.couponType == 2"
                                            v-model="formState.couponDiscount"
                                            width="180px"
                                            :disabled="formState.reduceType == 2"
                                        >
                                            <template #append>{{ $t("折") }}</template>
                                        </TigInput>
                                    </div>
                                </div>
                                <div class="flex mb20">
                                    <el-radio :value="2"></el-radio>
                                    <div class="flex">
                                        <p class="mr10">{{ $t("无门槛,{couponType1}", { couponType1: formState.couponType == 1 ? "减" : "打" }) }}</p>
                                        <TigInput
                                            type="decimal"
                                            v-if="formState.couponType == 1"
                                            v-model="formState.couponMoney"
                                            width="180px"
                                            :disabled="formState.reduceType == 1"
                                        >
                                            <template #append>{{ config.config.amountUnit }}</template>
                                        </TigInput>
                                        <TigInput
                                            type="decimal"
                                            :decimalPlaces="1"
                                            v-if="formState.couponType == 2"
                                            v-model="formState.couponDiscount"
                                            width="180px"
                                            :disabled="formState.reduceType == 1"
                                        >
                                            <template #append>{{ $t("折") }}</template>
                                        </TigInput>
                                    </div>
                                </div>
                            </el-radio-group>
                        </div>
                    </el-form-item>
                    <el-form-item prop="maxOrderAmount" :label="$t('优惠上限')" v-if="formState.couponType == 2">
                        <div>
                            <div>
                                <TigInput type="decimal" v-model="formState.maxOrderAmount" width="180px">
                                    <template #append>{{ config.config.amountUnit }}</template>
                                </TigInput>
                            </div>
                            <div class="extra">{{ $t("最多可打折的金额, 默认 0 表示无限制") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="sendType" :label="$t('用券时间')">
                        <div class="tig-radio">
                            <el-radio-group v-model="formState.sendType">
                                <div class="flex mb20">
                                    <el-radio :value="1"></el-radio>
                                    <div class="flex flex-align-center send-time-row">
                                        <SelectTimeInterval
                                            v-model:end-date="formState.useEndDate"
                                            v-model:start-date="formState.useStartDate"
                                            :clearable="true"
                                            width="200px"
                                            type="datetime"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            :disabled="formState.sendType == 0"
                                            @callback="changeDateType(-1)"
                                        ></SelectTimeInterval>
                                        <el-radio-group class="itemWidth" v-model="dateType" @change="changeDateType" :disabled="formState.sendType == 0">
                                            <el-radio-button :value="0">{{ $t("1天") }}</el-radio-button>
                                            <el-radio-button :value="1">{{ $t("7天") }}</el-radio-button>
                                            <el-radio-button :value="2">{{ $t("15天") }}</el-radio-button>
                                        </el-radio-group>
                                    </div>
                                </div>
                                <div class="flex mb20">
                                    <el-radio :value="0"></el-radio>
                                    <div class="flex">
                                        <p class="mr10">{{ $t("领券后") }}</p>
                                        <TigInput type="integer" v-model="formState.delayDay" width="100px" :disabled="formState.sendType == 1"> </TigInput>
                                        <p class="mr10 ml10">{{ $t(", 有效期") }}</p>
                                        <TigInput type="integer" v-model="formState.useDay" width="180px" :disabled="formState.sendType == 1">
                                            <template #append>{{ $t("天") }}</template>
                                        </TigInput>
                                    </div>
                                </div>
                            </el-radio-group>
                            <div class="extra" v-if="formState.sendType == 0">{{ $t("领券后默认0天生效, 表示立即生效") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="sendNum" :label="$t('发放总量')">
                        <div>
                            <div>
                                <TigInput type="integer" v-model="formState.sendNum" width="180px">
                                    <template #append>{{ $t("张") }}</template>
                                </TigInput>
                            </div>
                            <div class="extra">{{ $t("修改优惠券总量时只能增加不能减少，请谨慎设置。") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="limitNum" :label="$t('领取次数限制')">
                        <div>
                            <div>
                                <TigInput type="integer" :disabled="formState.isNewUser == 1" v-model="formState.limitNum" width="180px">
                                    <template #append>{{ $t("次") }}</template>
                                </TigInput>
                            </div>
                            <div class="extra">
                                {{ formState.isNewUser == 1 ? $t("新人专享券仅限领取一次") : $t("限制每个用户最多可领次数, 默认 0 表示无领取限制") }}
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item v-if="formState.isNewUser == 1" prop="isNewUser" :label="$t('领取客户限制')">
                        <div v-if="formState.isNewUser == 1">{{ $t("新人专享") }}</div>
                    </el-form-item>
                    <el-form-item v-if="formState.isNewUser == 2" prop="limitUserRank" :label="$t('领取客户限制')">
                        <el-select v-model="formState.limitUserRank" multiple :placeholder="$t('请选择')" style="width: 300px" clearable>
                            <el-option v-for="item in rankList" :key="item.rankId" :label="item.rankName" :value="item.rankId" />
                        </el-select>
                    </el-form-item>
                    <!-- 会员标签二期：领取人群圈选（全部会员 / 部分会员，部分会员下指定会员、会员标签、会员等级三者互斥） -->
                    <el-form-item prop="sendUserTarget" :label="$t('领取人群')">
                        <SendUserSelector ref="sendUserRef" v-model="formState.sendUserTarget" />
                    </el-form-item>
                    <el-form-item prop="sendRange" :label="$t('适用商品')">
                        <div>
                            <div>
                                <SelectGoodsRange v-model:rangeIds="formState.sendRangeData" v-model:rangeType="formState.sendRange" :isSelf="1" :otherParams="{ showVendorProduct: 1 }" />
                            </div>
                            <div v-if="Number(formState.sendRange) > 0 && !isProduct" class="extra red">{{ $t("请选择商品") }}</div>
                        </div>
                    </el-form-item>
                    <!-- <el-form-item prop="isGlobal" label="全店展示">
                        <div>
                            <el-radio-group v-model="formState.isGlobal" @change="onChangeIsGlobal">
                                <el-radio :value="1">是</el-radio>
                                <el-radio :value="0">否</el-radio>
                            </el-radio-group>
                        </div>
                    </el-form-item> -->
                    <el-form-item prop="isShow" :label="$t('是否公开展示')">
                        <div>
                            <el-radio-group v-model="formState.isShow">
                                <el-radio :value="1">{{ $t("是") }}</el-radio>
                                <el-radio :value="0">{{ $t("否") }}</el-radio>
                            </el-radio-group>
                        </div>
                    </el-form-item>
                    <el-form-item prop="couponDesc" :label="$t('券使用说明')">
                        <div class="flex" style="align-items: flex-end">
                            <div class="multilingual-textarea-overlay" style="position: relative; display: inline-block">
                                <TigInput classType="tig-form-input" type="textarea" :row="6" v-model="formState.couponDesc" />
                                <BusinessData v-model:modelValue="formState.couponDesc" :dataId="id" :dataType="11" type="textarea"></BusinessData>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t("提交") }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, reactive } from "vue";
import RadioType from "@/components/radio/src/RadioType.vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { CouponFormState, CouponRankList } from "@/types/promotion/coupon.d";
import { getCoupon, updateCoupon, getCouponConfig } from "@/api/promotion/coupon";
import type { FormInstance, FormRules } from "element-plus";
import { SelectGoodsRange } from "@/components/select";
import { SelectTimeInterval } from "@/components/select";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import SendUserSelector from "@/components/SendUserSelector/index.vue";
import type { SendUserTarget } from "@/types/user/memberAudience";
import { emptySendUserTarget, normalizeSendUserTarget } from "@/utils/sendUserTarget";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
import { useConfigStore } from "@/store/config";
const config: any = useConfigStore();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const rankList = ref<CouponRankList[]>([]); // 允许领取客户等级列表
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    couponType: {
        type: Number,
        default: 0
    },
    isNewUser: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: {
        type: Boolean,
        default: false
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const couponType = ref<number>(props.isDialog ? props.couponType : Number(query.couponType));
const isNewUser = ref<number>(props.isDialog ? props.isNewUser : Number(query.isNewUser));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const sendUserRef = ref();
const formState = ref<CouponFormState>({
    sendRange: 0,
    sendRangeData: [],
    isGlobal: 0,
    isShow: 1,
    couponType: couponType.value || 1,
    isNewUser: isNewUser.value || 0,
    couponUnit: 1,
    reduceType: 1,
    sendType: 1,
    limitUserRank: [],
    useStartDate: formattedDate(new Date(), "YYYY-MM-DD HH:mm:ss"),
    useEndDate: formattedDate(getDays(1, "add"), "YYYY-MM-DD HH:mm:ss"),
    delayDay: 0,
    sendNum: 1000,
    couponMoney: 20.0,
    couponDiscount: 8.0,
    minOrderAmount: 100.0,
    maxOrderAmount: 0.0,
    limitNum: isNewUser.value == 1 ? 1 : 0,
    memberRange: 0,
    memberMarks: [],
    sendUserTarget: emptySendUserTarget()
});
const isProduct = ref<boolean>(true);
const dateType = ref(0);
const changeDateType = (event: number) => {
    if (event === -1) {
        dateType.value = event;
        return;
    }
    formState.value.useStartDate = formattedDate(new Date(), "YYYY-MM-DD HH:mm:ss");
    if (event === 0) {
        //1
        formState.value.useEndDate = formattedDate(getDays(1, "add"), "YYYY-MM-DD HH:mm:ss");
    }
    if (event === 1) {
        //7
        formState.value.useEndDate = formattedDate(getDays(7, "add"), "YYYY-MM-DD HH:mm:ss");
    }
    if (event === 2) {
        //15
        formState.value.useEndDate = formattedDate(getDays(15, "add"), "YYYY-MM-DD HH:mm:ss");
    }
};
onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchCoupon();
    } else {
        loading.value = false;
    }
    fetchCouponConfig();
});
const fetchCoupon = async () => {
    try {
        const result = await getCoupon(action.value, {
            id: id.value,
            couponType: couponType.value,
            isNewUser: isNewUser.value
        });
        Object.assign(formState.value, result);
        // 回显容错
        formState.value.memberRange = Number((formState.value as any).memberRange ?? 0);
        (formState.value as any).memberMarks = Array.isArray((formState.value as any).memberMarks) ? (formState.value as any).memberMarks : [];
        (formState.value as any).sendUserTarget = normalizeSendUserTarget(
            (formState.value as any).sendUserTarget ?? (formState.value as any).memberAudience
        );
        delete (formState.value as any).memberAudience;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const fetchCouponConfig = async () => {
    try {
        const result = await getCouponConfig();
        rankList.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onChangeIsGlobal = (value: number) => {
    if (value == 1) {
        formState.value.sendRange = 0;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    if (!validateProduct()) {
        return;
    }
    await formRef.value.validate();
    const audienceResult = sendUserRef.value?.validate?.();
    if (audienceResult && !audienceResult.valid) {
        message.error(audienceResult.message);
        return;
    }
    try {
        emit("update:confirmLoading", true);
        // 旧会员标记功能保持禁用（统一传 0/[]）；领取人群以 sendUserTarget 为准
        (formState.value as any).memberRange = 0;
        (formState.value as any).memberMarks = [];
        const result = await updateCoupon(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};
const validateReduce = (rule: any, value: any, callback: any) => {
    if (formState.value.reduceType == 1) {
        if (formState.value.couponType == 1 && (Number(formState.value.minOrderAmount) <= 0 || Number(formState.value.couponMoney) <= 0)) {
            callback(new Error(t("请填写")));
            return;
        }
        if (formState.value.couponType == 2 && (Number(formState.value.minOrderAmount) <= 0 || Number(formState.value.couponDiscount) <= 0)) {
            callback(new Error(t("请填写")));
            return;
        }
        callback();
    } else if (formState.value.reduceType == 2) {
        if (formState.value.couponType == 1 && Number(formState.value.couponMoney) <= 0) {
            callback(new Error(t("请填写")));
            return;
        }
        if (formState.value.couponType == 2 && Number(formState.value.couponDiscount) <= 0) {
            callback(new Error(t("请填写")));
            return;
        }
        callback();
    }
};
const validateSendTime = (rule: any, value: any, callback: any) => {
    if (formState.value.sendType == 1) {
        if (!formState.value.useStartDate && !formState.value.useEndDate) {
            callback(new Error(t("请输入用券时间")));
            return;
        }
        callback();
    } else if (formState.value.sendType == 0) {
        if (!formState.value.delayDay && !formState.value.useDay) {
            callback(new Error(t("请输入用券时间")));
            return;
        }
        callback();
    } else {
        callback();
    }
};
const validateProduct = () => {
    if (Number(formState.value.sendRange) > 0 && formState.value.sendRangeData.length == 0) {
        isProduct.value = false;
    } else {
        isProduct.value = true;
    }
    return isProduct.value;
};

interface RuleForm {
    couponName: string;
    reduceType: number;
    sendType: number;
    sendNum: number;
    limitNum: number;
    limitUserRank: number[];
}
const rules = reactive<FormRules<RuleForm>>({
    couponName: [{ required: true, message: t("请输入优惠券名称"), trigger: "blur" }],
    reduceType: [{ required: true, validator: validateReduce, trigger: "blur" }],
    sendType: [{ required: true, validator: validateSendTime, trigger: "blur" }],
    sendNum: [{ required: true, message: t("请填写发放总量"), trigger: "blur" }],
    limitNum: [{ required: true, message: t("请设置领取次数"), trigger: "blur" }],
    limitUserRank: [{ required: true, message: t("请选择可用会员等级"), trigger: "blur" }]
});
defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.container {
    .tig-radio {
        :deep(.el-radio-group) {
            display: block;
            font-size: 12px;
        }
        :deep(.el-radio) {
            margin-right: 0px;
        }
        .check-box {
            cursor: pointer;
            :deep(.el-input-group__append) {
                background-color: #fff !important;
                padding: 0;
                box-shadow: none;
            }
            .item {
                width: 35px;
                height: 25px;
                line-height: 25px;
                text-align: center;
                border: 1px solid #b8b8b8;
            }
            .active {
                border: 1px solid var(--tig-primary);
                color: var(--tig-primary);
                background: var(--tig-item-active-bg);
            }
            .disabled {
                .item {
                    background-color: #f7f7f7;
                    color: #ccc;
                }
                .active {
                    border: 1px solid #b8b8b8;
                    background-color: #e0e0e0;
                }
            }
        }
    }
    .itemWidth {
        margin-bottom: 3px;
        margin-left: 10px;
        flex-shrink: 0;
        white-space: nowrap;
    }
    .send-time-row {
        flex-wrap: nowrap;
        min-width: 0;
        :deep(.lyecs-time-select-group) {
            flex-shrink: 0;
        }
        :deep(.time-con) {
            flex-wrap: nowrap;
        }
    }
    .red {
        color: var(--el-color-danger);
    }
}

/* 给右下角翻译按钮预留空间，避免遮挡输入文字 */
:deep(.multilingual-textarea-overlay .el-textarea__inner) {
    padding-bottom: 44px;
}
</style>
