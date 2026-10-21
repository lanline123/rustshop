<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('输入楼层名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="decorate/pcCatFloor/Info"
                                        :title="$t('添加抽屉楼层')"
                                        width="800px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加抽屉楼层') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-space>
                                        <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                            <template #reference>
                                                <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                            </template>
                                        </el-popconfirm>
                                        <!--                                    <el-button :loading="false"  @click="onClearCache">清除分类抽屉缓存 </el-button>-->
                                        <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                    </el-space>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="catFloorId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="100" :label="$t('楼层图标')">
                                <template #default="{ row }">
                                    <em :class="row.floorIcoFont" class="main_pel_m"></em>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('楼层名称')" prop="catFloorName">
                                <template #default="{ row }">
                                    <span>{{ row.catFloorName ? row.catFloorName : "--" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" :label="$t('是否显示')" sortable="custom">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.isShow"
                                        :params="{ id: row.catFloorId, field: 'isShow' }"
                                        :requestApi="updatePcCatFloorField"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.catFloorId, field: 'sortOrder' }"
                                        :requestApi="updatePcCatFloorField"
                                        :extra="$t('默认值为50，数值越小，排序越靠前')"
                                        :label="$t('排序')"
                                        type="integer"
                                    >
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.catFloorId }"
                                        isDrawer
                                        path="decorate/pcCatFloor/Info"
                                        :title="$t('编辑抽屉楼层')"
                                        width="800px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.catFloorId }" :requestApi="delPcCatFloor" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
                                </template>
                            </el-table-column>
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
                <div v-if="selectedIds.length > 0" class="selected-action-warp selected-warp-left">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                            >
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference>
                                    <el-button>{{ $t('批量删除') }}</el-button>
                                </template>
                            </el-popconfirm>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { PcCatFloorFilterParams, PcCatFloorFilterState } from "@/types/decorate/pcCatFloor.d";
import { batchSubmit, clearPcCatFloorCache, delPcCatFloor, getPcCatFloorList, updatePcCatFloorField } from "@/api/decorate/pcCatFloor";
import { useLoadCss } from "@/utils/domUtils";
import { useListRequest } from "@/hooks/useListRequest";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
if (configStore.config.icoDefinedCss) {
    useLoadCss(configStore.config.icoDefinedCss);
}

const config: any = useConfigStore();

const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<PcCatFloorFilterState, PcCatFloorFilterParams>({
    apiFunction: getPcCatFloorList,
    idKey: "catFloorId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize")
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();

const onClearCache = async () => {
    try {
        const result = await clearPcCatFloorCache();
        message.success(t('操作成功'));
        await loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>
