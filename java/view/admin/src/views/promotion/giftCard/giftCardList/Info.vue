<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <el-form-item prop="cardType" :label="cardLabel + $t('类型')">
                        <RadioType :model-value="cardType" :radioList="cardTypeList"> </RadioType>
                    </el-form-item>
                    <el-form-item prop="cardNumber" :label="cardLabel + $t('卡号')">
                        <div>
                            <TigInput
                                width="300px"
                                type="text"
                                v-model="formState.cardNumber"
                                :disabled="id > 0"
                                :placeholder="$t('请输入') + cardLabel + $t('卡号')"
                            />
                            <div class="tips">{{ $t('卡号不可重复，卡号长度需要在8~16位之间') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="cardPassword" :label="cardLabel + $t('密码')">
                        <div>
                            <TigInput width="300px" type="text" v-model="formState.cardPassword" :placeholder="$t('请设置') + cardLabel + $t('密码')" />
                            <div class="tips">{{ $t('密码长度需要在6~16位之间') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="cardLabel + $t('状态')" prop="status">
                        <div>
                            <el-switch v-model="formState.isAvailable" :active-value="1" :inactive-value="0"></el-switch>
                            <div class="tips">{{ $t('禁用后此卡项，不可出售') }}</div>
                        </div>
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
import { ref, shallowRef, onMounted, reactive, computed } from "vue";
import RadioType from "@/components/radio/src/RadioType.vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import type { GiftCardFormState } from "@/types/promotion/giftCard.d";
import { getGiftCard, updateGiftCard } from "@/api/promotion/giftCard";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    cardGroupsId: {
        type: Number,
        default: 0
    },
    groupType: {
        type: Number,
        default: 0
    },
    cardType: {
        type: Number,
        default: 1
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
const cardGroupsId = ref<number>(props.isDialog ? props.cardGroupsId : Number(query.cardGroupsId));
const groupType = ref<number>(props.isDialog ? props.groupType : Number(query.groupType));
const cardType = ref<number>(props.isDialog ? props.cardType : Number(query.cardType));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<GiftCardFormState>({
    cardGroupsId: cardGroupsId.value,
    cardNumber: "",
    cardPassword: "",
    isAvailable: 1
});
const cardTypes = reactive([
    {
        key: 1,
        title: t('电子卡'),
        desc: t('虚拟卡号，无实体卡片'),
        tips: t('电子礼品卡，是一种预付式电子购物卡无需实体卡片即可绑定使用。储值电子卡在购买完成后，可直接输入卡号，卡密兑换至余额中。兑换电子卡在购买完成后，直接输入卡号，卡密后进行兑换该卡绑定的商品，无需额外付费')
    },
    {
        key: 2,
        title: t('实体卡'),
        desc: t('快递发货实物卡片'),
        tips: t('实体礼品卡，可通过批量导出卡号、卡密，发给制卡商进行实体卡制作。更适合作为礼品赠送，收礼人可直接绑定线上使用，收到货后根据卡面中的卡号，卡密进行线上商品兑换或者充值')
    }
]);
const cardLabel = computed(() => {
    return groupType.value === 1 ? t('储值卡') : t('兑换卡');
});
const cardTypeList = computed(() => {
    return cardTypes.filter((item) => item.key === props.cardType);
});
const getDetail = async () => {
    try {
        const result = await getGiftCard({
            giftCardsId: id.value
        });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        getDetail();
    } else {
        loading.value = false;
    }
});

interface RuleForm {
    cardNumber: string;
    cardPassword: string;
}
const validatePassword = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请输入') + cardLabel.value + t('密码')));
        return;
    }
    if (value.length < 6 || value.length > 16) {
        callback(new Error(cardLabel.value + t('密码长度需要在6~16位之间')));
        return;
    }
    callback();
};

const validateCardNumber = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请输入') + cardLabel.value + t('卡号')));
        return;
    }
    if (value.length < 8 || value.length > 16) {
        callback(new Error(cardLabel.value + t('卡号长度需要在8~16位之间')));
        return;
    }
    callback();
};
const rules = reactive<FormRules<RuleForm>>({
    cardNumber: [
        { required: true, message: t('请输入') + cardLabel.value + t('卡号'), trigger: "blur" },
        { validator: validateCardNumber, trigger: "blur" }
    ],
    cardPassword: [{ required: true, validator: validatePassword, trigger: "blur" }]
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateGiftCard(operation, { cardId: id.value, ...formState.value });
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
<style lang="less" scoped>
.group {
    .group-header {
        display: flex;
        align-items: center;
        border-radius: 3px;
        background-color: rgba(245, 247, 250, 1);
        padding: 6px 0 6px 14px;
    }

    .group__block {
        width: 4px;
        height: 16px;
        border-radius: 22px;
        background-color: rgba(20, 86, 240, 1);
    }

    .group__title {
        color: var(--color-title);
        padding-left: 6px;
    }

    .extra {
        padding-top: 11px;
    }

    .group-content {
        padding: 26px 0 18px 5px;
    }
}

.form-item-wrapper {
    display: flex;
    align-items: center;
}

.tips {
    font-size: 12px;
    color: #999;
}

.form-submit-btn {
    width: 100px;
}
</style>
