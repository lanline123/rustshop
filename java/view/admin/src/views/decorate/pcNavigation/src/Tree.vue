<template>
    <div class="container">
        <div class="lyecs-table-list-warp">
            <div class="list-table-tool lyecs-search-warp">
                <div class="advanced-search-warp list-table-tool-row">
                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <div class="form-group">
                                <div class="control-container">
                                    <TigInput
                                        v-model="filterParams.keyword"
                                        name="keyword"
                                        :placeholder="$t('输入导航栏名称')"
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
                                    :params="{ act: 'add', type: 2 }"
                                    :title="$t('添加顶部小导航')"
                                    isDrawer
                                    path="decorate/pcNavigation/Info"
                                    width="560px"
                                    @okCallback="loadFilter"
                                >
                                    <el-button type="primary">{{ $t('添加顶部小导航') }}</el-button>
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
                        :indent="30"
                        :load="loadData"
                        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                        lazy
                        :loading="loading"
                        :total="total"
                        row-key="id"
                        @selection-change="onSelectChange"
                        @expand-change="handleExpandChange"
                        @sort-change="onSortChange"
                        ref="tableRef"
                    >
                        <el-table-column type="selection" width="32" />
                        <el-table-column :width="200" :label="$t('名称')" prop="title"></el-table-column>
                        <el-table-column :width="100" :label="$t('位置')" prop="typeName"></el-table-column>
                        <el-table-column :label="$t('是否显示')" prop="isShow">
                            <template #default="{ row }">
                                <Switch v-model:checked="row.isShow" :params="{ id: row.id, field: 'isShow' }" :requestApi="updatePcNavigationField" />
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('是否新窗口')">
                            <template #default="{ row }">
                                <Switch v-model:checked="row.isBlank" :params="{ id: row.id, field: 'isBlank' }" :requestApi="updatePcNavigationField" />
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                            <template #default="{ row }">
                                <PopForm
                                    v-model:org-value="row.sortOrder"
                                    :params="{ id: row.id, field: 'sortOrder' }"
                                    :requestApi="updatePcNavigationField"
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
                                    :params="{ act: 'detail', type: 2, id: row.id }"
                                    :title="$t('编辑商城顶部小导航栏')"
                                    isDrawer
                                    path="decorate/pcNavigation/Info"
                                    width="560px"
                                    @okCallback="updateChildFilter"
                                >
                                    <a class="btn-link">{{ $t('编辑') }}</a>
                                </DialogForm>
                                <el-divider direction="vertical" />
                                <DeleteRecord :params="{ id: row.id }" :requestApi="delPcNavigation" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { computed, onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { PcNavigationFilterParams, PcNavigationFilterState } from "@/types/decorate/pcNavigation.d";
import { batchSubmit, delPcNavigation, getPcNavigationList, updatePcNavigationField } from "@/api/decorate/pcNavigation";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref(<PcNavigationFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const filterParams = reactive<PcNavigationFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    type: 2,
    parentId: 0
});
const tableTreeRefreshTool = reactive<any>({});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getPcNavigationList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const loadData = async (tree: any, treeNode: any, resolve: any) => {
    // 保存父级id，在你需要刷新子级数据的地方可以用到
    // currentParentId.value = tree.id
    // 在之前声明的全局变量中，增加一个key为 本条数据的id，id可替换为你数据中的任意唯一值
    tableTreeRefreshTool[tree.id] = {};
    // 重要！保存resolve方法，以便后续使用
    tableTreeRefreshTool[tree.id].resolve = resolve;
    // 记录展开次数，具体作用后续介绍
    tableTreeRefreshTool[tree.id].expandCount = 0;
    // 请求api接口获取数据
    const result = await getPcNavigationList({
        page: 1,
        parentId: tree.id,
        type: 2,
        size: 1000
    });
    // 调用resolve方法，渲染子级数据
    resolve(result.records);
};
const handleExpandChange = async (row: any, expanded: any) => {
    // 获取到之前保存的全局变量
    const curr = tableTreeRefreshTool[row.id];
    // 展开次数 +1
    curr.expandCount++;
    // 如果是展开状态，且展开次数大于1，且上一次的状态为折叠，则请求api数据，更新子菜单
    if (expanded && curr.expandCount > 1 && !curr.prevStatus) {
        // api请求
        const result = await getPcNavigationList({
            page: 1,
            parentId: row.id,
            type: 2,
            size: 1000
        });
        // 调用resolve方法，渲染子级数据
        curr.resolve(result.records);
    }
    // 保存本次的展开或折叠状态，用于下次判断
    curr.prevStatus = expanded;
};
const tableRef = ref(); // 用于引用表格组件
// 更新当前层级的所有子级
const updateChildFilter = async (data: any) => {
    const curr = tableTreeRefreshTool[data.parentId];
    if (curr) {
        const result = await getPcNavigationList({
            page: 1,
            parentId: data.parentId,
            type: 2,
            size: 1000
        });
        //通过ref获取table的子节点数
        if (tableRef.value.store.states.lazyTreeNodeMap.value[data.parentId].length > 1) {
            //说明该节点下有多个子节点
            tableRef.value.store.states.lazyTreeNodeMap[data.parentId] = [];
        } else {
            //说明该节点只有一个节点
            tableRef.value.store.states.lazyTreeNodeMap.value[data.parentId] = [];
        }
        curr.resolve(result.records);
    } else {
        loadFilter();
    }
};
onMounted(() => {
    loadFilter();
});
// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};
// 参数查询
const onSearchSubmit = () => {
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
const onSelectChange = (e: PcNavigationFilterState[]) => {
    selectedIds.value = e.map((item) => item.id);
};
</script>
<style lang="less">
.el-table td.el-table__cell div {
    display: flex;
}
.el-table__expand-icon {
    top: 2px;
    margin-right: 18px !important;
}

.el-table__expand-icon--expanded {
    transform: none;
}

.el-table__expand-icon svg {
    display: none;
}

.el-table__expand-icon .el-icon {
    color: inherit;
    text-decoration: none;
    outline: none;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;
    float: left;
    box-sizing: border-box;
    width: 17px;
    height: 17px;
    padding: 0;
    line-height: 17px;
    background: #ffffff;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    transform: scale(0.9411764705882353);
    user-select: none;
}

.el-table__expand-icon .el-icon.is-loading {
    animation: none;
}

.el-table__expand-icon .el-icon::before,
.el-table__expand-icon .el-icon::after {
    position: absolute;
    background: currentcolor;
    transition: transform 0.3s ease-out;
    content: "";
}

.el-table__expand-icon .el-icon::before {
    top: 7px;
    inset-inline-end: 3px;
    inset-inline-start: 3px;
    height: 1px;
}

.el-table__expand-icon .el-icon::after {
    top: 3px;
    bottom: 3px;
    inset-inline-start: 7px;
    width: 1px;
    transform: rotate(90deg);
}

.el-table__expand-icon .el-icon::after {
    position: absolute;
    background: currentcolor;
    transition: transform 0.3s ease-out;
    content: "";
}

.el-table__expand-icon .el-icon::before {
    transform: rotate(-180deg);
}

.el-table__expand-icon .el-icon::after {
    transform: rotate(0deg);
}

.el-table__expand-icon--expanded .el-icon::before {
    transform: rotate(0);
}

.el-table__expand-icon--expanded .el-icon::after {
    transform: rotate(90deg);
}
</style>
