<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入商户名称')">
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
                            row-key="merchantId"
                            @select="selectRow"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column :selectable="isSelectable" type="selection" width="32" />
                            <el-table-column :label="$t('商户所属单位')">
                                <template #default="{ row }">
                                    <div class="ssdw">
                                        <template v-if="row.type == 1">
                                            <Tag :transparent="true" :text="$t('个人')"></Tag>
                                        </template>
                                        <template v-else>
                                            <Tag color="#409EFF" :transparent="false" :text="$t('企业')"></Tag>
                                        </template>
                                        {{ row.type == 1 ? ("" + row.corporateName) : ("" + row.companyName) }}
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商户所属会员(ID)')">
                                <template #default="{ row }">
                                    {{ row.user?.username }}{{ (row.user?.userId)?'('+(row.user?.userId)+')':'' }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商户类型')" prop="typeText"></el-table-column>
                            <el-table-column :label="$t('管理员')" >
                                <template #default="{ row }">
                                    {{ row.admin?.username }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('已绑定店铺')" prop="shopCount" :width="100" align="center">
                                <template #default="{ row }">
                                    <span class="font-color">{{ row.shopCount }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('认证状态')" prop="statusText">
                                <template #default="{ row }">
                                    <template v-if="row.status==1">
                                        <StatusDot color="#52c41a" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status==2">
                                        <StatusDot color="#f5222d" :flicker="false"></StatusDot>
                                    </template>
                                    {{ row.statusText }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('认证日期')" prop="addTime" sortable="custom" :width="200"></el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { MerchantFilterParams, MerchantFilterState } from "@/types/adminMerchant/merchant";
import { getMerchantList } from "@/api/adminMerchant/merchant";
import { Tag } from "@/components/form";
import StatusDot from "@/components/form/src/StatusDot.vue";
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    merchant: { type: Object, default: {} },
    isMultiple: {
        type: Boolean,
        default: true
    }
});
const emit = defineEmits(["submitCallback", "okType"]);
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<MerchantFilterState[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const merchant = ref<any>(props.merchant);
const filterParams = reactive<MerchantFilterParams>({
    //初使化用于查询的参数
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
        const result = await getMerchantList({ ...filterParams });
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
});
// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};
const tableRef: any = ref();
const onSelectChange = (e: any) => {
    if (props.isMultiple) {
        e.forEach((item: any) => {
            // 创建一个 Set 包含当前 selectedIds.value 的所有项
            const idsSet = new Set(selectedIds.value);
            // 尝试向 Set 中添加新的 merchantId
            idsSet.add(item.merchantId);
            // 使用 Set 的唯一性质，将其转换回数组赋给 selectedIds.value
            selectedIds.value = Array.from(idsSet);
        });
        console.log(selectedIds.value);
    }
};

const selectRow = (selection: any, val: any) => {
    //手动触发该事件
    if (!props.isMultiple) {
        // 单选
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            merchant.value = {}; // 清空data中绑定的merchant
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val.merchantId);
        merchant.value = val;
    }
};
const isSelectable = (row: any, index: number) => {
    // 排除重复项
    return !props.selectedIds?.includes(row.merchantId); // Column configuration not to be checked
};
// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    let obj = {
        merchantId: selectedIds.value[0],
        merchant: [merchant.value]
    }
    emit("submitCallback", obj);
};
defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.ssdw {
    display: flex;
    align-items: center;
    gap: 4px
}
.font-color {
    color: var(--tig-primary);
    cursor: pointer;
}
:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}
</style>
