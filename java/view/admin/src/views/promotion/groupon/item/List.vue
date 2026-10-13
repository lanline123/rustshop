<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('商品名称：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('请输入商品名称')"
                                            @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit">
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span
                                                        class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('商品品牌：') }}</span></label>
                                    <div class="control-container">
                                        <SelectBrand v-model:brandId="filterParams.brandId" @onChange="onSearchSubmit">
                                        </SelectBrand>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space class="action-bar">
                            <DialogForm isDrawer @close-callback="loadFilter" :title="$t('添加商品')" width="900px"
                                path="promotion/groupon/item/Info" :show-on-ok="false"
                                :params="{ act: 'add', grouponPromotionId: grouponPromotionId, groupType: groupType }">
                                <el-button type="primary">{{ $t('添加商品') }}</el-button>
                            </DialogForm>
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference>
                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量移除') }}</el-button>
                                </template>
                            </el-popconfirm>
                            <span class="selected-count" v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                        </el-space>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="grouponPromotionItemId"
                            @selection-change="onSelectChange" :total="total" :loading="loading">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('商品信息')" prop="productName" :width="150">
                                <template #default="{ row }">
                                    {{ row.productName || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('销售价')" prop="productPrice" align="center"></el-table-column>
                            <el-table-column :label="$t('拼团价')" prop="minGroupPrice" align="center"></el-table-column>
                            <el-table-column :label="$t('团规模')" prop="minGroupNum" align="center"></el-table-column>
                            <el-table-column :label="$t('可售数量')" prop="maxLimitNum" align="center"></el-table-column>
                            <el-table-column :label="$t('活动排序')" prop="sort" align="center"></el-table-column>
                            <el-table-column :width="168" :label="$t('操作')" fixed="right">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm isDrawer @okCallback="loadFilter" :title="$t('编辑')" :width="groupType == 2 ? '750px' : '660px'"
                                            path="promotion/groupon/item/setup/Info" :params="{
                                                act: 'detail',
                                                product: {
                                                    ...row,
                                                    productSkus: []
                                                }, groupType: groupType,
                                                id: row.grouponPromotionItemId, grouponPromotionId: row.grouponPromotionId
                                            }">
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <!-- 下个版本再加 -->
                                        <!-- <el-divider direction="vertical" />
                                        <DialogForm isDrawer width="900px">
                                            <a class="btn-link">投放</a>
                                        </DialogForm> -->
                                        <el-divider direction="vertical" />
                                        <DeleteRecord @afterDelete="loadFilter" :requestApi="delGrouponItem"
                                            :params="{ id: row.grouponPromotionItemId }">{{ $t('移除') }}</DeleteRecord>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { delGrouponItem, getGrouponItemList, groupItemBatch } from "@/api/promotion/groupon";
import { GrouponItemFilterParams, GrouponItemFilterState } from "@/types/promotion/groupon";
import { useConfigStore } from "@/store/config";
import { reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { SelectBrand } from "@/components/select";
import { message } from "ant-design-vue";
// import SelectProductGroup from "@/components/select/src/SelectProductGroup.vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const filterState = ref<GrouponItemFilterState[]>([]);
const total = ref<number>(0);
const loading = ref(true);
const selectedIds = ref<number[]>([]);
const props = defineProps({
    grouponPromotionId: {
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
    isDialog: Boolean
});
const emit = defineEmits(["close"]);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const groupType = ref<number>(props.isDialog ? props.groupType : Number(query.groupType));

const filterParams = reactive<GrouponItemFilterParams>({
    sortField: "",
    sortOrder: "",
    keyword: "",
    page: 1,
    grouponPromotionId: props.grouponPromotionId,
    size: config.get("pageSize"),
});
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getGrouponItemList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

// 初始化加载
loadFilter();

// 批量操作
const onBatchSubmit = async (action: string) => {
    if (selectedIds.value.length === 0) {
        message.warning(t('请至少选择一项'));
        return;
    }
    await groupItemBatch({
        ids: selectedIds.value,
        type: action
    });
    loadFilter();
};

const onSelectChange = (e: GrouponItemFilterState[]) => {
    selectedIds.value = e.map((item) => item.grouponPromotionItemId);
};

const onSearchSubmit = () => {
    loadFilter();
};

const resetParams = () => {
    filterParams.keyword = "";
    filterParams.page = 1;
    filterParams.sortField = "";
    filterParams.sortOrder = "";
    filterParams.brandId = undefined;
    filterParams.productGroupId = undefined;
    loadFilter();
};
// 表单提交
const onFormSubmit = () => {
    emit("close");
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.action-bar {
    .selected-count {
        font-size: 12px;
    }
}
</style>
