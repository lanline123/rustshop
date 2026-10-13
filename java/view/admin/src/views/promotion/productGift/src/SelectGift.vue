<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput name="keyword" v-model="filterParams.keyword" :placeholder="$t('输入赠品名称')" clearable @clear="onSearchSubmit">
                                        <template #append>
                                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            ref="tableRef"
                            :class="isMultiple ? '' : 'hide-checkbox'"
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            class="result-table"
                            style="width: 100%"
                            row-key="giftId"
                            @select="selectRow"
                            @selection-change="onSelectChange"
                        >
                            <el-table-column :selectable="isSelectable" type="selection" width="40" fixed="left" />
                            <el-table-column :label="$t('赠品名称')" prop="giftName" min-width="120"></el-table-column>
                            <el-table-column :label="$t('商品信息')" prop="productName" min-width="250">
                                <template #default="{ row }">
                                    <div class="product-info">
                                        <img :src="imageFormat(row.productInfo.picThumb)" height="50" width="50" />
                                        <p>{{ row.productInfo.productName }}</p>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品规格')" prop="productSku" min-width="150">
                                <template #default="{ row }">
                                    <div v-if="row.skuInfo && row.skuInfo.skuData.length > 0">
                                        <div class="product-info" v-for="sku in JSON.parse(row.skuInfo.skuData || '[]')">{{ sku.name }}: {{ sku.value }}</div>
                                    </div>
                                    <div v-else>--</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('赠品库存')" prop="giftStock" sortable="custom" min-width="110"></el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { ref, reactive, onMounted } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { ProductGiftFilterState, ProductGiftFilterParams } from "@/types/promotion/productGift.d";
import { getProductGiftList, delProductGift, batchSubmit } from "@/api/promotion/productGift";
import { useConfigStore } from "@/store/config";
import { imageFormat } from "@/utils/format";
import { truncate } from "lodash-es";
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    modelValue: {
        type: Object,
        default: {}
    },
    isMultiple: {
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["submitCallback", "okType", "update:modelValue"]);
emit("okType", false);
const selectedIds = ref<any[]>([]);
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<ProductGiftFilterState[]>();
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive<ProductGiftFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: ""
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getProductGiftList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
    // selectedIds.value = props.selectedIds;
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
const tableRef: any = ref();

const onSelectChange = (e: any) => {
    if (props.isMultiple) {
        e.forEach((item: any) => {
            // 创建一个 Set 包含当前 selectedIds.value 的所有项
            const idsSet = new Set(selectedIds.value);
            // 尝试向 Set 中添加新的
            idsSet.add(item);
            // 使用 Set 的唯一性质，将其转换回数组赋给 selectedIds.value
            selectedIds.value = Array.from(idsSet);
        });
        emit("okType", selectedIds.value.length > 0);
    }
};

const selectRow = (selection: any, val: any) => {
    //手动触发该事件
    if (!props.isMultiple) {
        // 单选
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val);
        emit("okType", selectedIds.value.length > 0);
    }
    // selectedIds.value = selection
    emit("update:modelValue", val);
};
const isSelectable = (row: any, rowIndex: number) => {
    // 排除重复项
    let ids: any[] = [];
    props.selectedIds.map((item: any) => {
        ids.push(item.giftId);
    });
    return !ids.includes(row.giftId);
};
// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    emit("submitCallback", selectedIds.value);
};

// 修改排序

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.product-info {
    display: flex;
    align-items: center;
    img {
        margin-right: 10px;
        flex-shrink: 0;
    }
}
.pagination-con {
    background: #fff;
}
:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}
.container {
    :deep(.el-table__header-wrapper .el-table-column--selection > .cell) {
        display: none;
    }
}

@media only screen and (max-width: 767px) {
    :deep(.el-table) {
        width: 100% !important;
    }
}
</style>
