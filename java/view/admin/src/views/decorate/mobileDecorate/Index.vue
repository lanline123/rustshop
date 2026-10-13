<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('新建页面')"
                                        width="600px"
                                        path="decorate/mobileDecorate/Info"
                                        :params="{ act: 'add' }">
                                        <el-button type="primary">{{ $t('新建页面') }}</el-button>
                                    </DialogForm>
                                    <TigInput name="keyword" v-model="filterParams.keyword" :placeholder="$t('输入页面名称')">
                                        <template #append>
                                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                </el-space>
                            </div>
                            <div class="list-table-tool-col"></div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="decorateId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('页面名称')" prop="decorateId">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <el-space>
                                            <el-tag v-if="row.status == 1" type="success" :hit="false" effect="light" size="small" disable-transitions
                                                >{{ $t('发布') }}</el-tag
                                            >
                                            <PopForm
                                                :label="$t('页面名称')"
                                                type="textarea"
                                                :requestApi="updateDecorateFiled"
                                                v-model:org-value="row.decorateTitle"
                                                :params="{ id: row.decorateId, field: 'decorateTitle' }"
                                                :max="50">
                                                <div>{{ row.decorateTitle }}</div>
                                            </PopForm>
                                        </el-space>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('添加时间')" prop="addTime" sortable="custom" :width="300">
                                <template #default="{ row }">
                                    {{ row.addTime }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="150">
                                <template #default="{ row }">
                                    <router-link :to="{ path: '/decorate/index', query: { id: 1 } }" target="_blank" class="btn-link">{{ $t('编辑') }}</router-link>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delDecorate" :params="{ id: row.decorateId }">{{ $t('删除') }}</DeleteRecord>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
                <div class="selected-action-warp selected-warp-left" v-if="selectedIds.length > 0">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                            >
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference><el-button>{{ $t('批量删除') }}</el-button></template>
                            </el-popconfirm>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { ref, reactive, onMounted } from "vue";
import { DeleteRecord, Switch, Pagination } from "@/components/list";
import { Image } from "@/components/image";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { FilterState, FilterParams } from "@/types/decorate/mobileDecorate.d";
import { getDecorateList, batchSubmit, updateDecorateFiled, delDecorate } from "@/api/decorate/mobileDecorate";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<FilterState[]>();
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const advancedSearch = ref<boolean>(false);
const filterParams = reactive<FilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getDecorateList({ ...filterParams });
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

// 批量操作
const onBatchSubmit = async (action: string) => {
    try {
        const result = await batchSubmit(action, { ids: selectedIds.value });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
// 多选操作
const onSelectChange = (e: FilterState[]) => {
    selectedIds.value = e.map((item) => item.decorateId);
};
</script>
