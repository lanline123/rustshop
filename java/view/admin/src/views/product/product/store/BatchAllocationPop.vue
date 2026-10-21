<template>
    <a-spin :spinning="pageLoading">
        <div class="batch-allocation-pop">
            <div class="tips-row">
                <el-icon size="16"><WarningFilled /></el-icon>
                <span>{{ $t('商品分配给组织后，状态为') }} <b>{{ storePostAllocationStatus ? $t('上架') : $t('下架') }}</b>{{ $t('（根据配置） 状态') }}</span>
                <router-link to="/organize/store-settings/info">
                    <el-button type="primary" link>{{ $t('修改配置') }}</el-button>
                </router-link>
            </div>

            <div class="section-row" style="margin-bottom: 12px;">
                <div class="label">{{ $t('选择商品') }}</div>
                <div class="value">{{ $t('已选择{length}个商品', { length: productIds.length }) }}<DialogForm
                    :maskClose="false"
                    :params="{ isMultiple: true, isSelf: 1, selectedIds: [], defaultSelectedIds: productIds }"
                    isDrawer
                    path="product/product/src/SelectProduct"
                    :title="$t('选择商品')"
                    width="900px"
                    @okCallback="onProductChange"
                >
                        <el-button link type="primary" style="margin-left: 8px;">{{ $t('修改') }}</el-button>
                    </DialogForm>
                </div>
            </div>

            <div class="product-grid-wrap">
                <div v-if="productList.length > 0" class="product-grid">
                    <div
                        v-for="item in productList"
                        :key="item.productId"
                        class="product-card"
                        :class="{ 'is-disabled': !canAllocate(item) }"
                    >
                        <el-button class="remove-btn" circle text @click="removeProduct(item.productId)">
                            <el-icon><Close /></el-icon>
                        </el-button>
                        <img class="thumb" :src="imageFormat(item.picThumb)" alt="" />
                        <div class="info">
                            <div class="name">{{ item.productName || "--" }}</div>
                            <div class="price">¥{{ item.productPrice || "0.00" }}</div>
                        </div>
                    </div>
                </div>
                <el-empty v-else-if="!pageLoading" :image-size="72" :description="$t('暂无已选商品')" />
                <div v-if="excludedCount > 0" class="excluded-tip"> {{ $t('灰色商品不可分配，提交时将自动忽略，可手动删除') }} </div>
            </div>

            <div class="section-row org-row">
                <div class="label">{{ $t('选择组织') }}</div>
                <el-radio-group v-model="allocationType" @change="onAllocationTypeChange" style="display: flex; align-items: center;">
                    <el-radio label="SHOPS" :disabled="disableShopsOption">{{ $t('指定组织') }}</el-radio>
                    <DialogForm
                        v-if="allocationType === 'SHOPS'"
                        dialogClass="noPadding"
                        :maskClose="false"
                        :params="{ selectedIds: selectedShops, type: 'SHOPS', isSelect: true }"
                        isDrawer
                        path="product/product/store/OrganizePop"
                        :title="$t('选择组织')"
                        width="1100px"
                        @okCallback="onShopChange"
                    >
                        <el-button link type="primary" :disabled="disableShopsOption" style="margin-left: -12px;">{{ $t('选择组织') }}</el-button>
                    </DialogForm>
                    <span v-if="allocationType === 'SHOPS'" class="org-count">{{ $t('已选择{length}个组织', { length: selectedShops.length }) }}</span>
                    <el-radio label="ALL">{{ $t('全部门店') }}</el-radio>
                    <el-radio label="AREA">{{ $t('全部区域') }}</el-radio>
                </el-radio-group>
            </div>

            <div class="tips-text"> {{ $t('指定商品数量100以内可支持选择组织方式可以指定组织，全部组织；商品数量101到500内则选择组织方式只能指定全部组织，不能选择指定组织。') }} </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { Close, WarningFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { getProductList, allocationProduct } from "@/api/product/product";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    productIds: {
        type: Array,
        default: () => []
    },
    storePostAllocationStatus: {
        type: Number,
        default: 0
    }
});

const emit = defineEmits(["submitCallback", "update:confirmLoading"]);

const pageLoading = ref<boolean>(true);
const productIds = ref<number[]>([]);
const productList = ref<any[]>([]);
const selectedShops = ref<any[]>([]);
const allocationType = ref<"SHOPS" | "ALL" | "AREA">("SHOPS");

const canAllocate = (item: any) => !item.shopId && item.groupType !== 1;
const excludedCount = computed(() => productList.value.filter((item) => !canAllocate(item)).length);
const allocatableProductIds = computed(() =>
    productList.value.filter(canAllocate).map((item) => item.productId)
);

const disableShopsOption = computed(() => productIds.value.length > 100);

const loadProducts = async () => {
    if (productIds.value.length === 0) {
        productList.value = [];
        pageLoading.value = false;
        return;
    }
    pageLoading.value = true;
    try {
        const result = await getProductList({
            page: 1,
            size: Math.min(500, productIds.value.length),
            ids: productIds.value.join(",")
        });
        const recordMap = new Map((result.records || []).map((item: any) => [item.productId, item]));
        productList.value = productIds.value.map((id) => recordMap.get(id)).filter(Boolean);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        pageLoading.value = false;
    }
};

watch(
    () => props.productIds,
    (ids: any[]) => {
        productIds.value = (ids || []).map((id) => Number(id)).filter((id) => !Number.isNaN(id));
        if (productIds.value.length > 100 && allocationType.value === "SHOPS") {
            allocationType.value = "ALL";
        }
        loadProducts();
    },
    { immediate: true }
);

const onProductChange = (ids: number[]) => {
    productIds.value = ids || [];
    if (productIds.value.length > 100 && allocationType.value === "SHOPS") {
        allocationType.value = "ALL";
        selectedShops.value = [];
    }
    loadProducts();
};

const onShopChange = (shops: any[]) => {
    selectedShops.value = shops || [];
};

const removeProduct = (productId: number) => {
    productIds.value = productIds.value.filter((id) => id !== productId);
    productList.value = productList.value.filter((item) => item.productId !== productId);
    if (productIds.value.length > 100 && allocationType.value === "SHOPS") {
        allocationType.value = "ALL";
        selectedShops.value = [];
    }
};

const onAllocationTypeChange = (type: "SHOPS" | "ALL" | "AREA") => {
    if (type !== "SHOPS") {
        return;
    }
    if (productIds.value.length > 100) {
        message.warning(t('当前商品数量大于100，不能选择指定组织'));
        allocationType.value = "ALL";
    }
};

const onSubmit = async () => {
    if (productIds.value.length === 0) {
        message.error(t('请选择商品'));
        return;
    }
    if (allocatableProductIds.value.length === 0) {
        message.error(t('当前所选商品均不可分配，请删除灰色商品后重试'));
        return;
    }
    if (productIds.value.length > 500) {
        message.error(t('最多支持500个商品进行批量分配'));
        return;
    }
    if (allocationType.value === "SHOPS" && selectedShops.value.length === 0) {
        message.error(t('请选择组织'));
        return;
    }

    const payload: any = {
        type: allocationType.value,
        productIds: allocatableProductIds.value
    };
    if (allocationType.value === "SHOPS") {
        payload.shopIds = selectedShops.value.map((item) => item.shopId);
    }

    try {
        emit("update:confirmLoading", true);
        const result = await allocationProduct(payload);
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({
    onFormSubmit
});
</script>

<style lang="less" scoped>
.batch-allocation-pop {
    padding: 24px;

    .tips-row {
        display: flex;
        align-items: center;
        gap: 8px;
        background: #f4f8ff;
        border: 1px solid #b3d4ff;
        padding: 10px 14px;
        margin-bottom: 24px;
        color: #333;
        font-size: 13px;

        :deep(.el-icon) {
            color: #333;
        }
    }

    .section-row {
        display: flex;
        align-items: center;
        margin-bottom: 20px;

        .label {
            width: 72px;
            color: #333;
            font-weight: 400;
            flex-shrink: 0;
            font-size: 14px;
        }

        .value {
            display: flex;
            align-items: center;
            gap: 8px;
            color: #333;
            font-size: 14px;
        }
        
        :deep(.el-radio) {
            margin-right: 24px;
        }
    }

    .product-grid-wrap {
        margin-bottom: 24px;
        padding-left: 72px;
        padding-right: 8px;
        padding-top: 8px;
        max-height: 400px;
        overflow-y: auto;
        overflow-x: hidden;
        
        /* 隐藏滚动条但保留功能 */
        &::-webkit-scrollbar {
            width: 6px;
        }
        &::-webkit-scrollbar-thumb {
            background: #dcdfe6;
            border-radius: 3px;
        }
    }

    .product-grid {
        display: grid;
        grid-template-columns: repeat(5, minmax(0, 1fr));
        gap: 12px;

        .product-card {
            border: 1px solid #e4e7ed;
            padding: 10px;
            display: flex;
            align-items: center;
            gap: 10px;
            overflow: visible;
            background: #fff;
            border-radius: 2px;
            position: relative;

            .thumb {
                width: 48px;
                height: 48px;
                border-radius: 2px;
                object-fit: cover;
                background: #f5f5f5;
                flex-shrink: 0;
            }

            .info {
                min-width: 0;
                display: flex;
                flex-direction: column;
                justify-content: center;
            }

            .name {
                font-size: 13px;
                color: #333;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                margin-bottom: 6px;
            }

            .price {
                color: #666;
                font-size: 13px;
            }

            .remove-btn {
                position: absolute;
                top: -8px;
                right: -8px;
                width: 16px;
                height: 16px;
                min-height: 16px;
                padding: 0;
                color: #fff;
                background-color: #dcdfe6;
                border: none;
                z-index: 10;

                &:hover {
                    background-color: #c0c4cc;
                    color: #fff;
                }

                :deep(.el-icon) {
                    font-size: 10px;
                }
            }
        }

        .product-card.is-disabled {
            background: #f5f5f5;
            border-color: #ebeef5;

            .thumb,
            .info {
                opacity: 0.45;
            }
        }
    }

    .org-row {
        margin-bottom: 12px;
        .org-count {
            margin: 0 16px 0 8px;
            color: #666;
            font-size: 13px;
        }
    }

    .tips-text {
        color: #999;
        font-size: 12px;
        line-height: 1.5;
        padding-left: 72px;
    }

    .excluded-tip {
        margin-top: 12px;
        color: #999;
        font-size: 12px;
    }
}
</style>
