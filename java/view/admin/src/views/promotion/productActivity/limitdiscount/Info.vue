<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <el-form-item prop="promotionName" :label="$t('活动名称')">
                        <TigInput width="432px" :maxlength="60" v-model="formState.promotionName" />
                    </el-form-item>
                    <el-form-item prop="productTime" :label="$t('活动时间')">
                        <div>
                            <div>
                                <SelectTimeInterval
                                    v-model:end-date="formState.endTime"
                                    v-model:start-date="formState.startTime"
                                    :clearable="false"
                                    type="datetime"
                                    value-format="YYYY-MM-DD HH:mm:ss"
                                ></SelectTimeInterval>
                            </div>
                            <div class="extra">{{ $t('只有当前时间介于起始日期和截止日期之间时，此活动才可以生效') }}</div>
                        </div>
                    </el-form-item>
                    <!-- 会员标签二期：适用会员人群圈选（全部会员 / 部分会员，部分会员下指定会员、会员标签、会员等级三者互斥） -->
                    <el-form-item prop="sendUserTarget" :label="$t('适用会员')">
                        <SendUserSelector ref="sendUserRef" v-model="formState.sendUserTarget" />
                    </el-form-item>
                    <el-form-item prop="item" :label="$t('活动商品')">
                        <DiscountForm v-model:skuIds="formState.item" :isMultiple="true" :isSku="true"></DiscountForm>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { LimitdiscountFormState, discountItem } from "@/types/promotion/limitdiscount.d";
import { getLimitdiscount, updateLimitdiscount } from "@/api/promotion/limitdiscount";
import type { FormRules } from "element-plus";
import DiscountForm from "./src/DiscountForm.vue";
import { SelectTimeInterval } from "@/components/select";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import SendUserSelector from "@/components/SendUserSelector/index.vue";
import type { SendUserTarget } from "@/types/user/memberAudience";
import { emptySendUserTarget, normalizeSendUserTarget } from "@/utils/sendUserTarget";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
interface RuleForm {
    promotionName: string;
    item: Array<discountItem>;
    productTime: string[];
}
const gifts = ref<any>([]);
const validateTime = (rule: any, value: any, callback: any) => {
    if (!formState.value.startTime || !formState.value.endTime) {
        callback(new Error(t('活动日期不能为空')));
        return;
    } else {
        callback();
    }
};
const validateDiscount = (rule: any, value: any, callback: any) => {
    if (!formState.value.item || formState.value.item.length == 0) {
        callback(new Error(t('活动商品不能为空')));
        return;
    } else {
        callback();
    }
};
const rules = reactive<FormRules<RuleForm>>({
    promotionName: [{ required: true, message: t('请输入活动名称！'), trigger: "blur" }],
    item: [{ required: true, validator: validateDiscount, trigger: "blur" }],
    productTime: [{ required: true, validator: validateTime, trigger: "blur" }]
});
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    promotionType: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const promotionType = ref<number>(props.isDialog ? props.promotionType : Number(query.couponType) || 0);
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const sendUserRef = ref();
const formState = ref<LimitdiscountFormState>({
    item: [],
    memberRange: 0,
    memberMarks: [],
    sendUserTarget: emptySendUserTarget()
});
onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchProductActivity();
    } else {
        loading.value = false;
    }
});

const fetchProductActivity = async () => {
    try {
        const result = await getLimitdiscount(action.value, {
            discountId: id.value,
            promotionType: promotionType.value
        });
        console.log(result.discountInfo);
        Object.assign(formState.value, result.discountInfo);
        // 回显容错
        (formState.value as any).memberRange = Number((formState.value as any).memberRange ?? 0);
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
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    const audienceResult = sendUserRef.value?.validate?.();
    if (audienceResult && !audienceResult.valid) {
        message.error(audienceResult.message);
        return;
    }
    // 旧会员标记功能保持禁用（统一传 0/[]）；适用会员以 sendUserTarget 为准
    (formState.value as any).memberRange = 0;
    (formState.value as any).memberMarks = [];
    let discountItem: any[] = [];
    formState.value.item.forEach((item: any) => {
        if (!item.value) {
            message.error(t('【{productName}】: 请输入折扣值', { productName: item.product.productName }));
            throw new Error();
        }
        if (item.discountType == 1) {
            if (item.value <= 0 || item.value > 9.9) {
                message.error(t('【{productName}】: 请输入0.1-9.9之间折扣', { productName: item.product.productName }));
                throw new Error();
            }
        }
        if (item.discountType > 1) {
            if (item.value <= 0 || item.value > Number(item.productPrice)) {
                message.error(t('【{productName}】: 请输入大于0并且小于商品原价的折扣', { productName: item.product.productName }));
                throw new Error();
            }
        }
        // discountItem.push(item.skuInfo)
    });
    // formState.value.item = discountItem
    console.log(formState.value);
    try {
        emit("update:confirmLoading", true);
        const result = await updateLimitdiscount(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
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

defineExpose({ onFormSubmit });
</script>
