/**
 * @file 订单发货组件
 * @description 订单发货表单，支持选择商品和配送方式
 * @features
 * - 支持物流配送/商家配送/无需配送/电子面单
 * - 支持部分发货（自动拆分订单）
 * - 选择物流公司和填写快递单号
 * - 虚拟商品自动设置为无需配送
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <div class="button-card babyBlue pd15">{{ $t('提示：如果订单分开发货，系统会自动将订单拆分为不同订单！') }}</div>
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('配送方式')" prop="shippingMethod">
                        <div>
                            <el-radio-group v-model="formState.shippingMethod" :disabled="disabled">
                                <el-radio :value="1">{{ $t('物流配送') }}</el-radio>
                                <el-radio :value="2">{{ $t('商家配送') }}</el-radio>
                                <el-radio :value="3">{{ $t('无需配送') }}</el-radio>
                                <el-radio v-if="formState.wayBill" :value="4">{{ $t('电子面单') }}</el-radio>
                            </el-radio-group>
                            <div class="extra" v-if="formState.shippingMethod == 4">{{ $t('此功能需要配置快递相关接口，请前往 设置->配送管理->配送设置 进行配置') }}</div>
                        </div>
                    </el-form-item>
                    <div v-show="formState.shippingMethod === 1 || formState.shippingMethod === 4">
                        <!-- <el-form-item label="配送类型" prop="shippingTypeId">
                            <ShippingType v-model:id="formState.shippingTypeId" @change="onChangeShippingType"></ShippingType>
                        </el-form-item> -->
                        <el-form-item
                            :label="$t('物流公司')"
                            prop="logisticsId"
                            :rules="[{ required: formState.shippingMethod == 4 ? true : false, message: '请选择物流公司' }]"
                        >
                            <SelectLogisticsCompany
                                v-model:logisticsId="formState.logisticsId"
                                source="tenant-config"
                                :is-enabled="1"
                                auto-select-default
                            ></SelectLogisticsCompany>
                        </el-form-item>
                        <el-form-item v-if="formState.shippingMethod === 1" :label="$t('快递单号')" prop="trackingNo">
                            <TigInput width="100%" v-model="formState.trackingNo" />
                        </el-form-item>
                        <el-form-item v-if="formState.shippingMethod === 4" :label="$t('发货备注')" prop="billRemark">
                            <TigInput width="100%" v-model="formState.billRemark" type="textarea" :rows="3" />
                        </el-form-item>
                    </div>
                    <el-table :data="formState.items" style="width: 100%" @selection-change="handleSelectionChange">
                        <el-table-column type="selection" width="50" :selectable="selectable" />
                        <el-table-column :label="$t('商品名称')" prop="productId" width="250">
                            <template #default="{ row }">
                                <ProductCard
                                    :picThumb="row.picThumb"
                                    :productId="row.productId"
                                    :productName="row.productName"
                                    :productType="row.productType"
                                    :url="row.url"
                                >
                                    <template #name-suffix>
                                        <span v-if="getAftersalesNum(row) > 0 && getShippableNum(row) <= 0" class="aftersales-flag">售后中</span>
                                    </template>
                                    <template #name-append>
                                        <div v-if="getAftersalesNum(row) > 0 && getShippableNum(row) <= 0" class="aftersales-tip-row">此商品已申请售后，请先处理售后</div>
                                        <div v-else-if="getAftersalesNum(row) > 0" class="aftersales-tip-row">
                                            {{ getAftersalesNum(row) }}件商品申请售后，最多可发货{{ getShippableNum(row) }}件
                                        </div>
                                    </template>
                                </ProductCard>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('属性')" prop="productAttr">
                            <template #default="{ row }">
                                {{ row.skuValue || "-" }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('附加规格')" prop="extraSkuData">
                            <template #default="{ row }">
                                <div v-if="row.extraSkuData && row.extraSkuData.length > 0" class="extraskudata" v-for="item in row.extraSkuData" :key="item">
                                    {{ item.attrName + item.attrValue + priceFormat(item.totalAttrPrice) }}
                                </div>
                                <div v-else>-</div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('商品编号')" prop="productSn" />
                        <el-table-column :label="$t('库存')" prop="stock"></el-table-column>
                        <el-table-column :label="$t('数量')" prop="quantity"> </el-table-column>
                        <el-table-column :label="$t('待发货数量')" prop="toDeliveryQuantity" width="120">
                            <template #default="{ row }">
                                <el-input-number
                                    class="input-number"
                                    controls-position="right"
                                    v-model="row.toDeliveryQuantity"
                                    @change="updateDeliverData"
                                    type="number"
                                    :min="getShippableNum(row) <= 0 ? 0 : 1"
                                    :max="getShippableNum(row)"
                                    :disabled="getShippableNum(row) <= 0"
                                />
                            </template>
                        </el-table-column>
                    </el-table>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message, Modal } from "ant-design-vue";
import { DRAWER_NESTED_Z_INDEX } from "@/utils/overlayZIndex";
import type { OrderToShipFormState } from "@/types/order/order.d";
import { getOrder, orderDeliver } from "@/api/order/order";
import { ProductCard } from "@/components/list";
import { SelectLogisticsCompany, ShippingType } from "@/components/select";
import { imageFormat, priceFormat } from "@/utils/format";
import { isPlatformSelfOrder } from "@/utils/orderShipPermission";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "okType"]);

emit("okType", false);
const props = defineProps({
    id: { type: Number, default: 0 },
    act: { type: String, default: "" },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
// 这里不要用OrderFormState
const formState = ref<OrderToShipFormState>({
    items: [],
    shippingMethod: 1,
    shippingTypeId: 1
});
const warnSplitNeed = ref(false);
const selectedRows = ref<OrderToShipFormState["items"]>([]);
const deliverData = ref<{ itemId: number; toDeliveryQuantity: number }[]>();
const disabled = ref(false);
const adminType = localStorage.getItem("adminType");
onMounted(() => {
    // 获取详情数据
    fetchOrder();
});
// 该商品被售后占用的数量：新版后端返回累计的 aftersalesNumber，旧版只有最后一条 aftersalesItem.number
const getAftersalesNum = (row: any) => Number(row?.aftersalesNumber ?? row?.aftersalesItem?.number) || 0;
// 真正可发货的数量。allowDeliverNum 是否已扣除售后随后端版本而变，取两种口径的较小值，避免重复扣减
const getShippableNum = (row: any) => {
    const allowNum = Number(row?.allowDeliverNum) || 0;
    const quantity = Number(row?.quantity) || 0;
    const netNum = quantity > 0 ? quantity - getAftersalesNum(row) : allowNum;
    return Math.max(0, Math.min(allowNum, netNum));
};
// 动态控制选择功能：可发货数为 0（含全部售后中）的行禁用勾选
const selectable = (row: any, index: number) => {
    return getShippableNum(row) > 0 && row.toDeliveryQuantity > 0;
};
const fetchOrder = async () => {
    try {
        loading.value = true;
        const result = await getOrder(action.value, { id: id.value });
        if (adminType === "admin" && !isPlatformSelfOrder(result)) {
            message.error(t('平台端仅支持为平台自营订单发货'));
            emit("close");
            return;
        }
        Object.assign(formState.value, result);
        // 当前服务商凭证未配齐时不提供电子面单，避免残留选中态提交后被后端拒绝
        if (!formState.value.wayBill && formState.value.shippingMethod === 4) {
            formState.value.shippingMethod = 1;
        }
        if (formState.value.items) {
            formState.value.items.forEach((item) => {
                item.toDeliveryQuantity = getShippableNum(item);
            });
            if (formState.value.items.length === 1 && formState.value.items[0].productType !== 1 && formState.value.items[0].productType !== 7) {
                formState.value.shippingMethod = 3;
                disabled.value = true;
            }
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const updateDeliverData = () => {
    const arr = <{ itemId: number; toDeliveryQuantity: number }[]>[];
    warnSplitNeed.value = false;
    if (selectedRows.value.length > 0) {
        selectedRows.value.forEach((element) => {
            console.log(element);
            if (element.toDeliveryQuantity < element.allowDeliverNum) {
                warnSplitNeed.value = true;
            }
            arr.push({
                itemId: element.itemId,
                toDeliveryQuantity: element.toDeliveryQuantity
            });
        });
    }
    deliverData.value = arr;
    if (warnSplitNeed.value === false) {
        warnSplitNeed.value = selectedRows.value.length < formState.value.items.length;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    if (warnSplitNeed.value === false) {
        deliverDo();
    } else {
        Modal.confirm({
            zIndex: DRAWER_NESTED_Z_INDEX,
            title: t('您当前只选择了部份商品发货，系统将会自动将订单进行拆分，是否确认继续发货？'),
            onOk: async () => {
                await deliverDo();
            }
        });
    }
};
const deliverDo = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await orderDeliver({
            id: id.value,
            deliverData: deliverData.value,
            shippingMethod: formState.value.shippingMethod,
            logisticsId: formState.value.logisticsId,
            trackingNo: formState.value.trackingNo,
            billRemark: formState.value.billRemark
        });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
const onChangeShippingType = (defaultLogisticsId: number) => {
    formState.value.logisticsId = defaultLogisticsId;
};

const handleSelectionChange = (val: OrderToShipFormState["items"]) => {
    selectedRows.value = val;
    updateDeliverData();
    emit("okType", selectedRows.value.length > 0);
};

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.button-card {
    margin-top: 12px;
    margin-bottom: 12px;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap; /* 允许换行 */
    gap: 8px;
    line-height: 24px;
}

.babyBlue {
    background-color: rgba(61, 127, 255, 0.06);
    border-radius: 9px;
}

.pd15 {
    padding: 15px;
}
.input-number {
    width: 85px;

    // 售后占用导致禁用时，Element 默认的浅灰箭头几乎不可见，加深以便识别上下限
    &.is-disabled {
        :deep(.el-input-number__decrease),
        :deep(.el-input-number__increase) {
            color: #606266;
        }
    }
}
.aftersales-flag {
    flex-shrink: 0;
    margin-left: 8px;
    font-size: 13px;
    color: #3d7fff;
}
.aftersales-tip-row {
    margin-top: 4px;
    font-size: 12px;
    color: #ed6a0c;
    line-height: 18px;
}
</style>
