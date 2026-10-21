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
                <div class="table-product-box">
                    <el-form :model="form" :rules="rules" ref="formRef" class="formDiv">
                        <el-table :data="pagedSkuList" style="width: 100%" border>
                            <el-table-column v-for="attr in skuTitles" :key="attr.name" :label="attr.name" min-width="120px">
                                <template #default="{ row }">
                                    <ul>
                                        <li v-for="(data, index) in row.skuData || []" :key="index">
                                            {{ attr.name === data.name ? data.value : "" }}
                                        </li>
                                    </ul>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('售价')" width="120px">
                                <template #default="{ row }">
                                    <el-form-item :prop="'skuList.' + rowIndex(row) + '.skuPrice'" :rules="rules.skuPrice">
                                        <TigInput type="decimal" v-model.trim="row.skuPrice" :placeholder="$t('请输入商品价格')" />
                                    </el-form-item>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form>
                </div>
                <div v-if="paginationEnabled" class="table-product-page">
                    <Pagination v-model:page="currentPage" v-model:size="pageSize" :total="form.skuList.length" :page-sizes="[...SKU_PAGE_SIZES]" :paginationText="false" />
                </div>
                <div class="table-btn-box">
                    <el-button @click="showPopover = false">{{ $t('取消') }}</el-button>
                    <el-button type="primary" @click="onConfirm" :loading="loading">{{ $t('保存') }}</el-button>
                </div>
            </template>
        </el-popover>
    </div>
</template>

<script setup lang="ts">
import { ref, unref, watch, nextTick } from "vue";
import request from "@/utils/request";
import { message } from "ant-design-vue";
import { ClickOutside as vClickOutside } from "element-plus";
import { Pagination } from "@/components/list";
import { useSkuTablePagination, SKU_PAGE_SIZES } from "@/hooks/useSkuTablePagination";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const showPopover = ref(false);
const popoverRef = ref();
const formRef = ref();
const loading = ref(false);
const props = defineProps({
    params: { type: Object, default: () => ({ id: null, productInfo: {} }) },
    requestApi: { type: Function, default: request }
});
const emit = defineEmits(["callback"]);
const cloneSkuList = (list: any[]) => (list || []).map((sku) => ({ ...sku }));
const form = ref({ skuList: cloneSkuList(props.params.productInfo.productSku || []) });
const skuList = ref(form.value.skuList);
const skuTitles = ref(form.value.skuList[0]?.skuData || []);
const { currentPage, pageSize, paginationEnabled, pagedList: pagedSkuList, rowIndex, goToIndex, resetPage } = useSkuTablePagination<any>(() => form.value.skuList);

watch(() => props.params.productInfo.productSku, (value) => {
    const list = value || [];
    form.value.skuList = cloneSkuList(list);
    skuList.value = form.value.skuList;
    skuTitles.value = list[0]?.skuData || [];
    resetPage();
});

const rules = {
    skuPrice: [{ required: true, message: t("请输入商品价格"), trigger: "blur" }]
};

const onClickOutside = (event: any) => {
    const content = unref(popoverRef)?.popperRef?.contentRef;
    if (showPopover.value && content && !content.contains(event.target)) showPopover.value = false;
};

const findInvalidPrice = () => form.value.skuList.findIndex((sku: any) => {
    const price = Number(sku?.skuPrice);
    return sku?.skuPrice === "" || sku?.skuPrice === null || sku?.skuPrice === undefined || !Number.isFinite(price) || price < 0;
});

const onConfirm = async () => {
    const invalidIndex = findInvalidPrice();
    if (invalidIndex > -1) {
        if (paginationEnabled.value) {
            goToIndex(invalidIndex);
            await nextTick();
        }
        await formRef.value?.validate().catch(() => undefined);
        message.error(t("请输入正确的商品价格"));
        return;
    }
    loading.value = true;
    try {
        await formRef.value?.validate();
        await props.requestApi({
            id: props.params.id,
            // 多规格库存和售价统一通过 productSku 字段更新，后端会按实际传入的字段分别处理。
            field: "productSku",
            val: skuList.value.map((sku: any) => ({ skuId: sku.skuId, skuPrice: Number(sku.skuPrice) }))
        });
        emit("callback");
        message.success(t("操作成功"));
        showPopover.value = false;
    } catch (error: any) {
        message.error(error?.message || t("操作失败"));
    } finally {
        loading.value = false;
    }
};
</script>

<style lang="less" scoped>
.lyecs-popedit-con { cursor: pointer; display: flex; align-items: center;
    .lyecs-popedit-btn { color: #999; font-size: 16px; padding-left: 2px; line-height: 1; }
}
.table-btn-box { text-align: end; padding-top: 10px; padding-right: 10px; }
.table-product-box { padding-right: 10px; height: 300px; overflow-y: scroll; overflow-x: hidden;
    &::-webkit-scrollbar { width: 8px; }
    &::-webkit-scrollbar-thumb { border-radius: 20px; background: #c2c2c2; }
    &::-webkit-scrollbar-track { border-radius: 8px; background: #fff; }
    :deep(.el-form-item) { margin-bottom: 0 !important; width: 90px !important; }
}
.table-product-page { display: flex; justify-content: flex-end; padding: 10px 10px 0; }
</style>
