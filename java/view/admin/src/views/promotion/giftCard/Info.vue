<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <div class="group">
                        <div class="group-header">
                            <div class="group__block"></div>
                            <div class="group__title">{{ $t('基础设置') }}</div>
                        </div>
                        <div class="group-content">
                            <el-form-item prop="cardType" required :label="cardLabel + $t('类型')">
                                <RadioType v-model:modelValue="formState.cardType" :radioList="cardTypeLisst">
                                    <template #tooltip="{ item }">
                                        <div class="tooltip-content" @click.stop>
                                            <div class="tooltip-text">{{ item.tips }}</div>
                                            <el-image
                                                :src="getCardExampleImg(item.key)"
                                                alt="示例图"
                                                class="tooltip-image"
                                                fit="contain"
                                                @click.stop="openImageViewer(item.key)"
                                            />
                                        </div>
                                    </template>
                                </RadioType>
                            </el-form-item>
                            <el-form-item prop="cardGroupName" :label="cardLabel + $t('名称')">
                                <div class="form-item-wrapper">
                                    <BusinessData
                                        width="300px"
                                        v-model:modelValue="formState.cardGroupName"
                                        :dataType="17"
                                        :placeholder="$t('请输入') + cardLabel + $t('名称')"
                                    ></BusinessData>
                                </div>
                            </el-form-item>
                            <el-form-item :label="cardLabel + $t('图片')" prop="imageUrl">
                                <FormAddGallery v-if="!loading" v-model:photo="formState.imageUrl" pageType="overseas" :dataType="17"> </FormAddGallery>
                            </el-form-item>
                            <el-form-item :label="$t('储值卡面额')" prop="faceAmount" v-if="groupType == 1">
                                <div class="form-item-wrapper">
                                    <TigInput type="decimal" v-model="formState.faceAmount" width="240px" :placeholder="$t('请输入金额')">
                                        <template #append>
                                            <div>{{ $t('元') }}</div>
                                        </template>
                                    </TigInput>
                                    <div class="tips">{{ $t('此金额代表可兑换至余额中的金额') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item :label="$t('有效期')" prop="validityType" required>
                                <div class="validity-type">
                                    <el-radio-group
                                        v-model="formState.validityType"
                                        style="flex-direction: column; align-items: flex-start"
                                        @change="handleLimitChange"
                                    >
                                        <el-radio :value="1">{{ $t('永久有效') }}</el-radio>
                                        <el-radio :value="2" style="margin-top: 10px" v-if="formState.cardType !== 2">{{ $t('天数') }}</el-radio>
                                        <el-form-item v-if="formState.validityType == 2 && formState.cardType !== 2" label="" prop="validityDays">
                                            <div style="display: flex; flex-wrap: wrap; gap: 5px; align-items: center; margin-left: 24px; width: 100%; margin-top: 10px">
                                                <span style="white-space: nowrap">{{ $t('购买后立即生效，') }}</span>
                                                <TigInput
                                                    v-model="formState.validityDays"
                                                    :placeholder="$t('请输入有效天数')"
                                                    type="integer"
                                                    width="150px"
                                                    :disabled="formState.validityType !== 2"
                                                >
                                                </TigInput>
                                                <span style="white-space: nowrap">{{ $t('天有效') }}</span>
                                            </div>
                                        </el-form-item>
                                        <el-radio :value="3" style="margin-top: 10px">{{ $t('固定时间') }}</el-radio>
                                        <el-form-item v-if="formState.validityType == 3" label="" prop="expireTime">
                                            <div style="display: flex; flex-wrap: wrap; gap: 5px; align-items: center; margin-left: 24px; width: 100%; margin-top: 10px">
                                                <span style="white-space: nowrap">{{ $t('购买后立即生效，使用时间截止为') }}</span>
                                                <el-date-picker
                                                    v-model="formState.expireTime"
                                                    type="date"
                                                    :placeholder="$t('请选择截止时间')"
                                                    value-format="YYYY-MM-DD HH:mm:ss"
                                                    :disabled-date="disabledDate"
                                                    style="width: 100%"
                                                    :disabled="formState.validityType !== 3"
                                                />
                                            </div>
                                        </el-form-item>
                                    </el-radio-group>
                                </div>
                            </el-form-item>
                            <el-form-item :label="cardLabel + $t('状态')" prop="status">
                                <div>
                                    <el-switch v-model="formState.status" :active-value="1" :inactive-value="0" :disabled="!formState.canDelete" />
                                    <div class="tips">{{ $t('禁用后此卡项，不可出售') }}<br />{{ $t('已绑定商品的卡状态无法禁用') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item
                                v-if="groupType == 2"
                                :label="$t('适用商品')"
                                prop="giftCardProducts"
                                :rules="[{ required: true, message: '请选择适用商品', trigger: 'change' }]"
                            >
                                <SelectGiftCardProduct v-model:ids="formState.giftCardProducts" :exclude-product-types="[7, 8]"></SelectGiftCardProduct>
                            </el-form-item>
                            <el-form-item :label="$t('备注')" prop="remark">
                                <TigInput classType="tig-form-input" v-model="formState.remark" :row="3" type="textarea" />
                            </el-form-item>
                            <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                                <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }}</el-button>
                            </el-form-item>
                        </div>
                    </div>
                </el-form>
            </div>
        </div>
        <el-image-viewer
            v-if="showImageViewer"
            :url-list="[currentPreviewImg]"
            :initial-index="0"
            @close="showImageViewer = false"
            :teleported="true"
        />
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, reactive, computed, watch } from "vue";
import RadioType from "@/components/radio/src/RadioType.vue";
import { FormAddGallery } from "@/components/gallery";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import { ElImageViewer } from "element-plus";
import SelectGiftCardProduct from "./src/SelectGiftCardProduct.vue";
import type { GiftCardGroupFormState, GiftCardProduct } from "@/types/promotion/giftCard.d";
import { getGiftCardGroupDetail, updateGiftCardGroup } from "@/api/promotion/giftCard";
import { getAdminType, getShopType } from "@/utils/storage";
import electronicCardImg from "@/style/images/promotion/giftCard/electronic.png";
import entityCardImg from "@/style/images/promotion/giftCard/entity.png";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    groupType: {
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
const showImageViewer = ref<boolean>(false);
const currentPreviewImg = ref<string>("");
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const groupType = ref<number>(props.isDialog ? props.groupType : Number(query.groupType));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const cardDefaultBg = "https://oss.tigshop.com/img/gallery/202511/1763106657gduKxujm9Fx7WZNjJZ.jpeg";
const formState = ref<GiftCardGroupFormState>({
    cardGroupName: "",
    cardType: 1,
    imageUrl: cardDefaultBg,
    status: 1,
    remark: "",
    validityType: 1,
    giftCardProducts: [],
    groupType: groupType.value,
    canDelete: true
});
// 是否是店铺
const isShop = computed(() => {
    return getAdminType() === "shop" && getShopType() === 1;
});
const cardTypeLisst = ref([
    {
        key: 1,
        title: t('电子卡'),
        desc: t('虚拟卡号，无实体卡片'),
        tips: t('电子礼品卡：购买后可在订单详情查看卡号及卡密，也可转增；根据礼品卡的类型可用于兑换商品{value}', { value: isShop.value ? "" : t('或充值余额') })
    },
    {
        key: 2,
        title: t('实体卡'),
        desc: t('快递发货实物卡片'),
        tips: t('实体礼品卡：购买后在收到货后刮开图层查看卡密进行兑换，根据礼品卡的类型可用于兑换商品{value}', { value: isShop.value ? "" : t('或充值余额') })
    }
]);

const cardLabel = computed(() => {
    return groupType.value === 1 ? t('储值卡') : t('兑换卡');
});

// 根据卡类型获取对应的示例图
const getCardExampleImg = (cardType: number) => {
    return cardType === 1 ? electronicCardImg : entityCardImg;
};

// 打开图片预览器
const openImageViewer = (cardType: number) => {
    currentPreviewImg.value = getCardExampleImg(cardType);
    showImageViewer.value = true;
};

// 记录原有的商品数据
const giftCardProducts = ref<GiftCardProduct[]>([]);

const getDetail = async () => {
    try {
        const result = await getGiftCardGroupDetail({
            cardGroupsId: id.value
        });
        // 过滤掉giftCardProducts里为空的数据
        result.giftCardProducts = result.giftCardProducts?.filter((item) => item?.productId);
        result.imageUrl = result.imageUrl || cardDefaultBg;
        Object.assign(formState.value, result);
        // 如果是编辑
        cardTypeLisst.value = cardTypeLisst.value.filter((item) => item.key == result.cardType);
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

// 处理有效期类型变更
const handleLimitChange = () => {
    // 清除相关字段的值
    if (formState.value.validityType !== 2) {
        formState.value.validityDays = undefined;
    }
    if (formState.value.validityType !== 3) {
        formState.value.expireTime = "";
    }
};

const validateCardName = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请输入') + cardLabel.value + t('名称')));
        return;
    }
    callback();
};

const validFaceAmount = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请输入') + cardLabel.value + t('面额')));
        return;
    }
    if (Number(value) <= 0) {
        callback(new Error(cardLabel.value + t('面额需大于0')));
        return;
    }
    callback();
};

const validateValidtyDays = (rule: any, value: any, callback: any) => {
    // 只有在选择"天数"选项时才需要验证
    if (formState.value.validityType === 2) {
        if (value != "0" && !value) {
            callback(new Error(t('请输入有效天数')));
            return;
        }
        if (Number(value) <= 0) {
            callback(new Error(t('有效天数需大于0')));
            return;
        }
    }
    callback();
};

const validateExpireTime = (rule: any, value: any, callback: any) => {
    // 只有在选择"固定时间"选项时才需要验证
    if (formState.value.validityType === 3) {
        if (!value) {
            callback(new Error(t('请选择截止时间')));
            return;
        }
    }
    callback();
};

interface RuleForm {
    cardGroupName: string;
    faceAmount: number;
    validityDays: number;
    expireTime: string;
}
const rules = reactive<FormRules<RuleForm>>({
    cardGroupName: [{ required: true, validator: validateCardName, trigger: "blur" }],
    faceAmount: [{ required: true, validator: validFaceAmount, trigger: "blur" }],
    validityDays: [{ validator: validateValidtyDays, trigger: "blur" }],
    expireTime: [{ validator: validateExpireTime, trigger: "change" }]
});

// 监听适用商品变更并重新校验当前字段
watch(
    () => formState.value.giftCardProducts,
    () => {
        if (groupType.value === 2) {
            formRef.value?.validateField?.("giftCardProducts");
        }
    },
    { deep: true }
);

// 监听卡类型变更，如果是实体卡且当前选择的是天数，则自动切换到永久有效
watch(
    () => formState.value.cardType,
    (newVal) => {
        if (newVal === 2 && formState.value.validityType === 2) {
            formState.value.validityType = 1;
            formState.value.validityDays = undefined;
        }
    }
);

// 禁止选择今天之前的日期
const disabledDate = (date: Date) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date.getTime() < today.getTime();
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        // 处理giftCardProducts数据
        if (groupType.value === 2) {
            // 兑换卡需要处理商品数据
            formState.value.giftCardProducts = formState.value.giftCardProducts?.map((item) => {
                // 看看原始数据是否有该商品
                const oldItem = giftCardProducts.value.find((source) => source.productId === item.productId && source.skuId === item.skuId);
                return {
                    giftProductId: oldItem?.giftProductId || 0,
                    cardGroupsId: id.value,
                    productId: item.productId,
                    skuId: item.skuId
                };
            });
        }
        const result = await updateGiftCardGroup(operation, { id: id.value, ...formState.value });
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
    // display: flex;
    align-items: center;
}

.tips {
    font-size: 12px;
    color: #999;
}

.form-submit-btn {
    width: 100px;
}
.validity-type {
    :deep(.el-form-item__error) {
        margin-left: 24px;
        margin-top: 4px;
        position: relative;
        top: 0;
    }
}

.tooltip-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 4px 0;

    .tooltip-text {
        width: 200px;
        text-align: left;
        font-size: 12px;
        line-height: 1.5;
        color: #333;
    }

    .tooltip-image {
        width: 200px;
        height: auto;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        cursor: pointer;

        :deep(.el-image__inner) {
            border-radius: 4px;
        }
    }
}
</style>
