<template>

    <!-- 配送方式选择（非实物/虚拟/卡券/特殊商品） -->
    <el-form-item v-if="showBasicShippingSelect" :rules="[{ required: true, message: '请选择配送方式!' }]" :label="$t('配送方式')" prop="noShipping">
        <el-radio-group v-model="formState.noShipping" :disabled="isReadOnly" style="width: 100%; margin-bottom: 10px">
            <el-radio :value="1">{{ $t('无需配送') }}</el-radio>
        </el-radio-group>
        <el-radio-group v-model="formState.noShipping" :disabled="isReadOnly || !isPhysicalProduct" style="width: 100%">
            <el-radio :value="0">
                <div class="flex">
                    <div class="mr10">{{ $t('运费模板') }}</div>
                    <el-form-item label="" prop="shippingTplId">
                        <div class="flex flex-justify-between">
                            <el-select
                                v-model="formState.shippingTplId"
                                :disabled="isReadOnly || !isPhysicalProduct || formState.noShipping === 1"
                                :placeholder="$t('请选择')"
                                style="width: 100%"
                            >
                                <el-option
                                    v-for="item in formState.shippingTplList"
                                    :key="item.shippingTplId"
                                    :label="item.shippingTplName"
                                    :value="item.shippingTplId"
                                />
                            </el-select>
                            <div v-if="!isReadOnly" class="con-btn">
                                <DialogForm
                                    :params="{ act: 'add' }"
                                    isDrawer
                                    path="setting/shippingTpl/Info"
                                    :title="$t('添加运费模板')"
                                    width="900px"
                                    @okCallback="fetchProductConfig"
                                >
                                    <el-button link type="primary">{{ $t('新建') }}</el-button>
                                </DialogForm>
                                <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                                <el-button :loading="configLoading" link type="primary" @click="fetchProductConfig">{{ $t('刷新') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                </div>
            </el-radio>
        </el-radio-group>
    </el-form-item>

    <!-- 快递运费（实物商品 + 非门店模式） -->
    <el-form-item v-if="showExpressShippingFee" :rules="[{ required: true }]" :label="$t('快递运费')" prop="fixedShippingType">
        <!-- 固定运费选项 -->
        <el-radio-group v-model="formState.fixedShippingType" :disabled="isReadOnly" style="width: 100%; margin-bottom: 25px">
            <div>
                <el-radio :value="1">
                    <div class="fixed-shipping-type">
                        <div class="flex flex-align-center">
                            <div class="mr10">{{ $t('固定运费') }}</div>
                            <el-form-item :rules="[{ required: true, validator: validateShippinFee }]" class="inner-item" label="" prop="fixedShippingFee">
                                <PriceInput
                                    v-model:modelValue="formState.fixedShippingFee"
                                    :disabled="isReadOnly || formState.fixedShippingType === 2"
                                    :placeholder="$t('请输入运费')"
                                    width="200px"
                                />
                            </el-form-item>
                        </div>
                    </div>
                </el-radio>
                <div class="extra" style="margin-top: 5px; margin-left: 20px"> {{ $t('设置固定运费为0时，前台展示为包邮。') }} <el-popover :width="400" placement="right-end" trigger="click">
                        <template #reference><a>{{ $t('查看示例') }}</a></template>
                        <template #default>
                            <img src="@/style/images/fixedShippingFee.png" style="width: 380px" />
                        </template>
                    </el-popover>
                </div>
            </div>
        </el-radio-group>

        <!-- 运费模板选项 -->
        <el-radio-group v-model="formState.fixedShippingType" :disabled="isReadOnly || !canUseShippingTemplate" style="width: 100%">
            <el-radio :value="2">
                <div class="flex">
                    <div class="mr10">{{ $t('运费模板') }}</div>
                    <el-form-item label="" prop="shippingTplId">
                        <div class="flex flex-justify-between">
                            <el-select
                                v-model="formState.shippingTplId"
                                :disabled="isReadOnly || !canUseShippingTemplate || formState.fixedShippingType === 1"
                                :placeholder="$t('请选择')"
                                style="width: 100%"
                            >
                                <el-option
                                    v-for="item in formState.shippingTplList"
                                    :key="item.shippingTplId"
                                    :label="item.shippingTplName"
                                    :value="item.shippingTplId"
                                />
                            </el-select>
                            <div v-if="!isReadOnly" class="con-btn">
                                <DialogForm
                                    :params="{ act: 'add' }"
                                    isDrawer
                                    path="setting/shippingTpl/Info"
                                    :title="$t('添加运费模板')"
                                    width="900px"
                                    @okCallback="fetchProductConfig"
                                >
                                    <el-button link type="primary">{{ $t('新建') }}</el-button>
                                </DialogForm>
                                <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                                <el-button :loading="configLoading" link type="primary" @click="fetchProductConfig">{{ $t('刷新') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                </div>
            </el-radio>
        </el-radio-group>
    </el-form-item>

    <!-- 配送方式（门店模式 或 S2B2C特殊商品） -->
    <el-form-item v-if="showStoreDeliveryOptions" :rules="[{ required: true, validator: deliveryMethod }]" :label="$t('配送方式')" prop="isLogistics">
        <!-- 快递发货选项 -->
        <div>
            <el-checkbox v-model="formState.isLogistics" :label="$t('快递发货')" :true-label="1" :false-label="0" />
            <div v-if="formState.isLogistics === 1">
                <!-- 固定运费 -->
                <el-radio-group v-model="formState.fixedShippingType" :disabled="isStoreReadOnly" style="width: 100%; margin-bottom: 25px">
                    <div>
                        <el-radio :value="1">
                            <div class="fixed-shipping-type">
                                <div class="flex flex-align-center">
                                    <div class="mr10">{{ $t('固定运费') }}</div>
                                    <el-form-item
                                        :rules="[{ required: true, validator: validateShippinFee }]"
                                        class="inner-item"
                                        label=""
                                        prop="fixedShippingFee"
                                    >
                                        <PriceInput
                                            v-model:modelValue="formState.fixedShippingFee"
                                            :disabled="isStoreReadOnly || formState.fixedShippingType === 2"
                                            :placeholder="$t('请输入运费')"
                                            width="200px"
                                        />
                                    </el-form-item>
                                </div>
                            </div>
                        </el-radio>
                        <div class="extra" style="margin-top: 5px; margin-left: 20px"> {{ $t('设置固定运费为0时，前台展示为包邮。') }} <el-popover :width="400" placement="right-end" trigger="click">
                                <template #reference><a>{{ $t('查看示例') }}</a></template>
                                <template #default>
                                    <img src="@/style/images/fixedShippingFee.png" style="width: 380px" />
                                </template>
                            </el-popover>
                        </div>
                    </div>
                </el-radio-group>

                <!-- 运费模板 -->
                <el-radio-group v-model="formState.fixedShippingType" :disabled="isStoreReadOnly || !canUseShippingTemplate" style="width: 100%">
                    <el-radio :value="2">
                        <div class="flex">
                            <div class="mr10">{{ $t('运费模板') }}</div>
                            <el-form-item label="" prop="shippingTplId">
                                <div class="flex flex-justify-between">
                                    <el-select
                                        v-model="formState.shippingTplId"
                                        :disabled="isStoreReadOnly || !canUseShippingTemplate || formState.fixedShippingType === 1"
                                        :placeholder="$t('请选择')"
                                        style="width: 100%"
                                    >
                                        <el-option
                                            v-for="item in formState.shippingTplList"
                                            :key="item.shippingTplId"
                                            :label="item.shippingTplName"
                                            :value="item.shippingTplId"
                                        />
                                    </el-select>
                                    <div v-if="!isStoreReadOnly" class="con-btn">
                                        <DialogForm
                                            :params="{ act: 'add' }"
                                            isDrawer
                                            path="setting/shippingTpl/Info"
                                            :title="$t('添加运费模板')"
                                            width="900px"
                                            @okCallback="fetchProductConfig"
                                        >
                                            <el-button link type="primary">{{ $t('新建') }}</el-button>
                                        </DialogForm>
                                        <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                                        <el-button :loading="configLoading" link type="primary" @click="fetchProductConfig">{{ $t('刷新') }}</el-button>
                                    </div>
                                </div>
                            </el-form-item>
                        </div>
                    </el-radio>
                </el-radio-group>
            </div>
        </div>
        <!-- 到店自提选项（S2B2C 非特殊商品） -->
        <div v-if="showSelfPickupOption" style="width: 100%">
            <div class="flex flex-align-center">
                <el-checkbox v-model="formState.isShopPickup" :label="$t('到店自提')" :true-label="1" :false-label="0" :disabled="!pickupTplData.status" />
                <div v-if="!pickupTplData.status" class="con-btn">
                    <el-button link type="primary" @click="goSettingPickup">{{ $t('去开启') }}</el-button>
                    <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                    <el-button :loading="configLoading" link type="primary" @click="_getShopPickupTpl">{{ $t('刷新') }}</el-button>
                </div>
            </div>
            <!-- 自提配置面板 -->
            <div v-if="formState.isShopPickup === 1" class="wechat-config">
                <div class="table-container">
                    <el-row>
                        <el-col :span="7"><div class="table-tr">{{ $t('系统自动完成备货') }}</div></el-col>
                        <el-col :span="10"><div class="table-tr">{{ $t('可自提时间') }}</div></el-col>
                        <el-col :span="7"><div class="table-tr">{{ $t('提货有效期') }}</div></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="7">
                            <div class="table-td">
                                <div class="info">{{ pickupTplData.stockingStatus ? $t('已开启') : $t('已关闭') }}</div>
                                <div class="btn">
                                    <DialogForm
                                        :maskClose="false"
                                        isDrawer
                                        :params="{ act: 'deawer' }"
                                        path="setting/pickupSetting/Index"
                                        :title="$t('自提设置')"
                                        width="1010px"
                                        @okCallback="_getShopPickupTpl"
                                    >
                                        <el-button type="primary" link>{{ $t('去设置') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                        </el-col>
                        <el-col :span="10">
                            <div class="table-td">
                                <div class="flex table-info">
                                    <div class="info" v-if="pickupTplData.pickupTimeStatus === 1">
                                        <template v-if="pickupTimeList.length > 0">
                                            <div class="info-conetnt">
                                                <div class="label">{{ $t('可自提时段：') }}</div>
                                                <div class="val">
                                                    <div v-for="item in pickupTimeList" :key="item.startTime">{{ item.startTime }}-{{ item.endTime }}</div>
                                                </div>
                                            </div>
                                        </template>
                                        <p v-if="pickupTplData.stockingStatus === 0">{{ $t('预约自提：需提前{appointmentTimeNum}{appointmentTimeUnit}预约自提', { appointmentTimeNum, appointmentTimeUnit }) }}</p>
                                        <p v-else>{{ $t('已开启自动完成备货') }}</p>
                                    </div>
                                    <div class="info" v-else>{{ $t('已关闭') }}</div>
                                    <div class="btn">
                                        <DialogForm
                                            :maskClose="false"
                                            isDrawer
                                            :params="{ act: 'deawer' }"
                                            path="setting/pickupSetting/Index"
                                            :title="$t('自提设置')"
                                            width="1010px"
                                            @okCallback="_getShopPickupTpl"
                                        >
                                            <el-button type="primary" link>{{ $t('去设置') }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                            </div>
                        </el-col>
                        <el-col :span="7">
                            <div class="table-td">
                                <div class="info">{{ pickupTplData.pickupEndStatus ? $t('已开启') : $t('已关闭') }}</div>
                                <div class="btn">
                                    <DialogForm
                                        :maskClose="false"
                                        isDrawer
                                        :params="{ act: 'deawer' }"
                                        path="setting/pickupSetting/Index"
                                        :title="$t('自提设置')"
                                        width="1010px"
                                        @okCallback="_getShopPickupTpl"
                                    >
                                        <el-button type="primary" link>{{ $t('去设置') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                </div>
            </div>
        </div>

    </el-form-item>
</template>

<script lang="ts" setup>
import { ref, watch, nextTick, computed } from "vue";
import { message } from "ant-design-vue";
import { ProductFormState } from "@/types/product/product.d";
import { getProductConfig } from "@/api/product/product";
import PriceInput from "@/views/product/product/src/PriceInput.vue";
import { DialogForm } from "@/components/dialog";
import { getShopPickupTpl } from "@/api/setting/pickupSetting";
import { pickupSettingData } from "@/types/setting/pickupSetting";
import { isS2b2c, isStore } from "@/utils/version";
import { getShopType } from "@/utils/storage";
import { useRouter } from "vue-router";
import { QuestionFilled } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const router = useRouter();

// ==================== Props ====================
const props = defineProps({
    id: { type: Number, default: 0 },
    shopId: { type: Number, default: 0 },
    examine: { type: Number, default: 0 },
    loading: { type: Boolean, default: false },
    act: { type: String, default: "" },
    storeSettingInfo: { type: Object, default: {} }
});

const formState = defineModel<ProductFormState>("formState", { default: {} });

// ==================== 商品类型常量 ====================
const ProductType = {
    PHYSICAL: 1, // 实物商品
    VIRTUAL: 5, // 虚拟商品
    CARD: 6, // 卡券商品
    SPECIAL: 7 // 特殊商品
} as const;

// ==================== 商品类型计算属性 ====================
/** 是否实物商品 */
const isPhysicalProduct = computed(() => formState.value.productType === ProductType.PHYSICAL);

/** 是否特殊商品 */
const isSpecialProduct = computed(() => formState.value.productType === ProductType.SPECIAL);

/** 是否可使用运费模板的商品类型（实物或特殊商品） */
const canUseShippingTemplate = computed(() => isPhysicalProduct.value || isSpecialProduct.value);

// ==================== 权限/状态计算属性 ====================
/** 是否只读状态（审核中） */
const isReadOnly = computed(() => props.examine === 1);

/** 门店模式下的只读状态（审核中但自营店铺可编辑） */
const isStoreReadOnly = computed(() => isReadOnly.value && getShopType() !== 2);

// ==================== 显示条件计算属性 ====================
/** 显示基础配送方式选择（非实物/虚拟/卡券/特殊商品） */
const showBasicShippingSelect = computed(() => {
    const productType = formState.value.productType;
    const excludeTypes: number[] = [ProductType.PHYSICAL, ProductType.VIRTUAL, ProductType.CARD, ProductType.SPECIAL];
    return productType !== undefined && !excludeTypes.includes(productType);
});

/** 显示快递运费设置（实物商品 + 非门店模式） */
const showExpressShippingFee = computed(() => isPhysicalProduct.value && !isStore());

/** 显示门店配送选项（门店模式实物商品 或 S2B2C特殊商品） */
const showStoreDeliveryOptions = computed(() => {
    const isStorePhysical = (isPhysicalProduct.value || [7].includes(formState.value.productType as number)) && isStore();
    const isS2b2cSpecial = isS2b2c() && isSpecialProduct.value;
    return isStorePhysical || isS2b2cSpecial;
});

/** 导入的供应商商品 */
const isVendorProduct = computed(() => formState.value.vendorId && formState.value.vendorProductId);

/** 显示到店自提选项（S2B2C模式 + 非特殊商品） */
const showSelfPickupOption = computed(
    () => isS2b2c() && (!isSpecialProduct.value || [7].includes(formState.value.productType as number)) && !isVendorProduct.value
);


/** 是否处于预售模式 */
const isPresaleMode = computed(() => [1, 2].includes(formState.value.saleMode ?? 0));

/** 是否可使用多次交付（仅非预售的指定商品类型） */
const canUseMultipleDelivery = computed(() => !isPresaleMode.value && [1, 5, 6].includes(formState.value.productType ?? 0));

/** 交付模式（1-按计划配送，2-按需兑换），读写 cycleConfig.deliveryType */
const cycleDeliveryType = computed<1 | 2>({
    get() {
        const t = formState.value.cycleConfig?.deliveryType;
        return t === 2 ? 2 : 1;
    },
    set(val) {
        if (!formState.value.cycleConfig) {
            formState.value.cycleConfig = {
                deliveryType: val,
                settingMode: 1,
                timesPerPeriod: 1,
                cycleType: 1,
                specs: []
            } as any;
        } else {
            formState.value.cycleConfig.deliveryType = val;
        }
    }
});

// ==================== 自提相关计算属性 ====================
/** 自提时间列表 */
const pickupTimeList = computed(() => pickupTplData.value.pickupTimeJson?.timeList || []);

/** 预约时间数值 */
const appointmentTimeNum = computed(() => pickupTplData.value.pickupTimeJson?.appointmentTime?.timeNum || 0);

/** 预约时间单位（格式化后） */
const appointmentTimeUnit = computed(() => {
    const unit = pickupTplData.value.pickupTimeJson?.appointmentTime?.timeUnit;
    const unitMap: Record<string, string> = { m: t('分钟'), h: t('小时'), d: t('天') };
    return unitMap[unit || ""] || "";
});

// ==================== 状态数据 ====================
const configLoading = ref(false);

const pickupTplData = ref<pickupSettingData>({
    pickupTimeJson: {
        type: 1,
        timeList: [{ startTime: "", endTime: "" }],
        timeType: 1
    },
    pickupEndJson: {
        type: 1,
        day: undefined,
        hour: undefined
    }
});
// 默认运费模板ID
const defaultShippingTplId = computed<number | undefined>(() => {
    const list = formState.value.shippingTplList || [];
    return list.find((i: any) => i?.isDefault === 1)?.shippingTplId;
});

const isShippingTplModeActive = computed(() => {
    // 基础配送方式：运费模板
    if (showBasicShippingSelect.value) return formState.value.noShipping === 0;
    // 非门店模式：快递运费 -> 运费模板
    if (showExpressShippingFee.value) return formState.value.fixedShippingType === 2;
    // 门店模式：快递发货 + 运费模板
    if (showStoreDeliveryOptions.value) return formState.value.isLogistics === 1 && formState.value.fixedShippingType === 2;
    return false;
});

const ensureDefaultShippingTplSelected = () => {
    if (!canUseShippingTemplate.value) return;
    if (!isShippingTplModeActive.value) return;

    const defId = defaultShippingTplId.value;
    if (!defId) return;

    const current = formState.value.shippingTplId as any;
    // 不覆盖用户手动选择；仅在未选择时自动填充
    if (current && Number(current) > 0) return;
    formState.value.shippingTplId = defId;
};

// ==================== 验证器 ====================
const validateShippinFee = (rule: any, value: any, callback: any) => {
    // 非固定运费模式，直接通过
    if (formState.value.fixedShippingType !== 1) {
        callback();
        return;
    }

    // 固定运费模式下的验证
    if (value !== 0 && !value) {
        callback(new Error(t('运费不能为空')));
        return;
    }

    if (Number(value) > 999999) {
        callback(new Error(t('运费不能超过999999')));
        return;
    }

    callback();
};

const deliveryMethod = (rule: any, value: any, callback: any) => {
    const { isShopPickup, isLogistics, isShopDelivery } = formState.value;
    const hasDeliveryMethod = isShopPickup || isLogistics || isShopDelivery;

    if (!hasDeliveryMethod) {
        callback(new Error(t('请选择快递配送方式')));
        return;
    }
    callback();
};

// ==================== API 方法 ====================
const fetchProductConfig = async () => {
    configLoading.value = true;
    try {
        const shopId = props.shopId || Number(localStorage.getItem("shopId")) || 0;
        const result = await getProductConfig({ shopId });
        formState.value.shippingTplList = result.shippingTplList;
        ensureDefaultShippingTplSelected();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        setTimeout(() => {
            configLoading.value = false;
        }, 200);
    }
};

const _getShopPickupTpl = async () => {
    try {
        const result = await getShopPickupTpl({ shopId: props.shopId });
        pickupTplData.value = result;
        nextTick(() => {
            if (!pickupTplData.value.status) {
                formState.value.isShopPickup = 0;
            }
        });
    } catch (error: any) {
        message.error(error.message);
    }
};



// ==================== 路由跳转 ====================
const goSettingPickup = () => router.push("/setting/pickup-setting");

// ==================== 监听器 ====================
watch(
    () => formState.value,
    (newVal) => {
        if ((newVal.productType === ProductType.PHYSICAL || [7].includes(newVal.productType as number)) && isStore()) {
            _getShopPickupTpl();
        }
    },
    { immediate: true }
);

watch(
    () => [
        props.act,
        defaultShippingTplId.value,
        formState.value.shippingTplId,
        formState.value.noShipping,
        formState.value.fixedShippingType,
        formState.value.isLogistics,
        showBasicShippingSelect.value,
        showExpressShippingFee.value,
        showStoreDeliveryOptions.value
    ],
    () => ensureDefaultShippingTplSelected(),
    { immediate: true }
);

watch(
    () => canUseMultipleDelivery.value,
    (enabled) => {
        if (!enabled && formState.value.deliveryMode === 2) {
            formState.value.deliveryMode = 1;
        }
    },
    { immediate: true }
);

</script>

<style lang="less" scoped>
.con-btn {
    display: flex;
    align-content: center;
    padding-left: 10px;

    span {
        color: #999;
    }
    a {
        display: block;
        word-break: keep-all;
        padding: 0 5px;
    }
}

.wechat-config {
    background-color: #f6f7fa;
    padding: 20px;
    margin-top: 10px;

    .table-container {
        border: 1px solid #ddd;
        border-right: none;

        .table-tr {
            padding: 15px;
            border-right: 1px solid #ddd;
        }

        .table-td {
            background-color: #fff;
            padding: 15px;
            border-right: 1px solid #ddd;
            border-top: 1px solid #ddd;
            display: flex;
            align-items: center;
            height: 100%;
            box-sizing: border-box;

            .table-info {
                align-items: flex-end;

                .info-conetnt {
                    display: flex;
                    align-items: flex-start;
                    flex-wrap: wrap;
                    line-height: 20px;

                    .val {
                        display: flex;
                        gap: 0 10px;
                        flex-wrap: wrap;
                    }
                }
            }

            .btn {
                margin-left: 10px;
            }
        }
    }
}

:deep(.el-table .custom-header-row th) {
    font-weight: normal;
}

.setting-btn {
    display: flex;
    align-items: center;
    padding-top: 4px;
}

@media (max-width: 768px) {
    .wechat-config {
        padding: 10px;

        .table-container {
            overflow-x: auto;
            -webkit-overflow-scrolling: touch;

            .el-row {
                min-width: 600px;
                flex-wrap: nowrap;
            }

            .table-tr {
                padding: 10px 5px;
                font-size: 13px;
                line-height: 1.4;
                text-align: center;
            }
            .table-td {
                padding: 10px 5px;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-align: center;

                .info {
                    font-size: 13px;
                }

                .btn {
                    margin-left: 0;
                    margin-top: 5px;
                }

                .table-info {
                    align-items: center;

                    .info-conetnt {
                        flex-direction: column;
                        align-items: center;

                        .val {
                            justify-content: center;
                            text-align: center;
                        }
                    }
                }
            }
        }
    }
}
</style>
