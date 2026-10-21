<template>
    <div class="lyecs-popedit">
        <el-popover :visible="showPopover" ref="popoverRef" placement="left" :width="0">
            <template #reference>
                <div class="lyecs-popedit-con" @click="showPopover = true" v-click-outside="onClickOutside">
                    <slot></slot>
                    <span class="lyecs-popedit-btn iconfont icon-bianji1"></span>
                </div>
            </template>
            <template #default>
                <template v-if="skuList && skuList.length > 0">
                    <div class="table-product-box">
                        <el-form :model="Form" :rules="rules" ref="formRef" class="formDiv">
                            <el-table :data="pagedSkuList" style="width: 100%" border>
                                <el-table-column v-for="attr in skuTit" :label="attr.name" min-width="120px">
                                    <template #default="{ row, $index }">
                                        <ul>
                                            <li v-for="(data, index) in row.skuData" :key="index">
                                                {{ attr.name == data.name ? data.value : "" }}
                                            </li>
                                        </ul>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('库存')" width="100px">
                                    <template #default="{ row, $index }">
                                        <el-form-item :prop="'skuList.' + rowIndex(row) + '.skuStock'" :rules="rules.skuStock">
                                            <TigInput type="integer" v-model.trim="row.skuStock" :placeholder="$t('请输入商品库存')"></TigInput>
                                        </el-form-item>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </el-form>
                    </div>
                    <div v-if="paginationEnabled" class="table-product-page">
                        <Pagination
                            v-model:page="currentPage"
                            v-model:size="pageSize"
                            :total="enabledSkuList.length"
                            :page-sizes="[...SKU_PAGE_SIZES]"
                            :paginationText="false"
                        />
                    </div>
                </template>
                <div class="table-product-inp" v-else>
                    <el-form :model="Form" :rules="rules" ref="formRef" class="formDiv">
                        <el-form-item prop="productStock" :rules="rules.productStock">
                            <TigInput type="integer" v-model.trim="Form.productStock" :placeholder="$t('请输入商品库存')"></TigInput>
                        </el-form-item>
                    </el-form>
                </div>
                <div class="table-btn-box">
                    <el-button ref="buttonRef" @click="showPopover = false">{{ $t('取消') }}</el-button>
                    <el-button v-if="skuList && skuList.length > 0" type="primary" @click="onConfirm({ field: 'productSku' })" :loading="loading"
                        >{{ $t('保存') }}</el-button
                    >
                    <el-button v-else type="primary" @click="onConfirm({ field: 'productStock' })" :loading="loading">{{ $t('保存') }}</el-button>
                </div>
            </template>
        </el-popover>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, unref, watch, nextTick, computed } from "vue";
import request from "@/utils/request";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import { ClickOutside as vClickOutside } from "element-plus";
import { useSkuTablePagination, SKU_PAGE_SIZES } from "@/hooks/useSkuTablePagination";
import { Pagination } from "@/components/list";
import { getEnabledSkus } from "@/utils/productSkuStatus";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const showPopover = ref(false);
const popoverRef = ref();
const loading = ref(false);
const onClickOutside = (e: any) => {
    if (showPopover.value && !unref(popoverRef).popperRef.contentRef.contains(e.target)) {
        showPopover.value = false;
    }
};

const props = defineProps({
    params: {
        type: Object,
        default: { id: null, productInfo: {} }
    },
    requestApi: { type: Function, default: request }
});

// 监听props.params.productInfo.productStock的变化
watch(
    () => props.params.productInfo.productStock,
    (newVal) => {
        Form.value.productStock = newVal || 0;
    }
);

const Form = ref({
    skuList: props.params.productInfo.productSku || [],
    productStock: props.params.productInfo.productStock || 0
});
const enabledSkuList = computed(() => getEnabledSkus(Form.value.skuList));

// SKU 明细前端分页（大基数时避免弹窗一次性渲染全部行）
const {
    currentPage,
    pageSize,
    paginationEnabled,
    pagedList: pagedSkuList,
    rowIndex,
    goToIndex,
    resetPage
} = useSkuTablePagination<any>(() => enabledSkuList.value, { fullList: () => Form.value.skuList });

const skuTit = ref(enabledSkuList.value[0]?.skuData);

// 父组件 update 成功后会刷新列表并传入最新 productInfo，需要把本地 skuList/skuTit 一并同步
// 否则会出现「删除规格值后弹窗仍显示旧规格」的缓存问题
// 注意：这里必须用浅 watch —— Form.skuList 与 props.productSku 共享数组引用，
// 深 watch 会在单元格库存输入时触发，导致 resetPage() 强跳第一页
watch(
    () => props.params.productInfo.productSku,
    (newVal) => {
        const list = newVal || [];
        Form.value.skuList = list;
        skuList.value = getEnabledSkus(list);
        skuTit.value = skuList.value[0]?.skuData;
        resetPage();
    }
);

const rules = ref({
    skuStock: [
        {
            required: true,
            message: t('不能为空'),
            trigger: "blur"
        }
    ],
    productStock: [
        {
            required: true,
            message: t('不能为空'),
            trigger: "blur"
        }
    ]
});

const emit = defineEmits(["callback"]);
const formRef = ref(); //表单Ref
const visible = ref(false);
const skuList = ref(getEnabledSkus(props.params.productInfo.productSku || []));
/** 分页后隐藏页的 el-form 未挂载，validate() 覆盖不到 → 先全量扫描空库存 */
const findFirstEmptyStock = (): number => {
    const list = enabledSkuList.value;
    for (let i = 0; i < list.length; i++) {
        const s = list[i]?.skuStock;
        if (s === "" || s === null || s === undefined) return i;
    }
    return -1;
};

const onConfirm = async (data: any) => {
    // 多规格分页时：先全量校验库存非空，命中则跳到该行所在页再提示
    if (data.field === "productSku") {
        const bad = findFirstEmptyStock();
        if (bad > -1) {
            if (paginationEnabled.value) {
                goToIndex(bad);
                await nextTick();
            }
            formRef.value.validate();
            message.error(t('请填写库存'));
            return;
        }
    }
    loading.value = true;
    return new Promise((resolve, reject) => {
        formRef.value.validate().then(() => {
            let val = null;
            if (data.field == "productSku") {
                val = extractSkuInfo(skuList.value);
            } else {
                val = Form.value.productStock;
            }
            props
                .requestApi({
                    id: props.params.id,
                    field: data.field,
                    val: val
                })
                .then((result: any) => {
                    emit("callback");
                    message.success(t('操作成功'));
                    loading.value = false;
                    showPopover.value = false;
                    resolve(true);
                })
                .catch((error: any) => {
                    loading.value = false;
                    message.error(error.message);
                    reject();
                });
        });
    });
};

function extractSkuInfo(skus: any[]) {
    let skuInfoArray: any[] = [];
    skus.forEach((sku) => {
        skuInfoArray.push({ skuId: sku.skuId, skuStock: Number(sku.skuStock) });
    });
    return skuInfoArray;
}
</script>

<style lang="less" scoped>
.lyecs-popedit-con {
    cursor: pointer;
    display: flex;
    align-items: center;
    .lyecs-popedit-btn {
        cursor: pointer;
        color: #999;
        font-size: 16px;
        padding-left: 2px;
        line-height: 1;
    }
}
.table-btn-box {
    text-align: end;
    padding-top: 10px;
    padding-right: 10px;
}
.table-product-box {
    padding-right: 10px;
    height: 300px;
    overflow-y: scroll;
    overflow-x: hidden;
    &&::-webkit-scrollbar {
        //修改滚动条宽度或高度
        width: 8px;
    }
    &&::-webkit-scrollbar-thumb {
        border-radius: 20px;
        background: #c2c2c2;
    }
    &&::-webkit-scrollbar-track {
        border-radius: 8px;
        background: #fff;
    }
    :deep(.el-form-item) {
        margin-bottom: 0 !important;
        width: 70px !important;
    }
}
.table-product-inp {
    :deep(.el-form-item) {
        margin-bottom: 0 !important;
        width: 120px !important;
    }
}
.table-product-page {
    display: flex;
    justify-content: flex-end;
    padding: 10px 10px 0;
}
</style>
