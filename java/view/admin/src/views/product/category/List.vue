/**
 * @file 商品分类列表页面
 * @description 商品分类管理列表，支持树形结构展示和懒加载子分类。
 * @features
 * - 树形结构展示分类层级
 * - 懒加载子分类数据
 * - 行内编辑（分类名称、单位、排序）
 * - 显示状态和热门状态开关
 * - 添加分类/子分类
 * - 商品转移功能
 * - 批量删除
 * - 海外版支持批量翻译分类名称
 */
<template>
    <link :href="icoDefinedCss" rel="stylesheet" />
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-button v-if="filterParams.parentId > 0" @click="backToParentCat"><i
                                            class="iconfont-admin icon-diaotou"
                                            style="font-size: 14px; padding-right: 5px"></i> {{ $t('返回上一级') }} </el-button>
                                    <DialogForm :params="{ act: 'add', parentId: filterParams.parentId }" isDrawer
                                        path="product/category/Info" :title="$t('添加分类')" width="600px"
                                        @okCallback="loadFilter">
                                        <el-button type="primary">{{ $t('添加分类') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :expand-row-keys="expandIds" :indent="30" :load="loadData"
                            :total="total" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" lazy
                            row-key="categoryId" @selection-change="onSelectChange" @expand-change="handleExpandChange"
                            ref="tableRef">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="200" :label="$t('分类名称')">
                                <template #default="{ row }" row-key="categoryId">
                                    <el-space style="min-height: 30px;">
                                        <PopForm v-model:org-value="row.categoryName"
                                            :params="{ id: row.categoryId, field: 'categoryName' }"
                                            :requestApi="updateCategoryFiled" :label="$t('分类名称')" type="textarea"
                                            @callback="changeCallback">
                                            <a v-if="row.hasChildren" @click="childCat(row.categoryId)">{{
                                                row.categoryName }}</a>
                                            <span v-else>{{ row.categoryName }}</span>
                                        </PopForm>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" :label="$t('分类图片')">
                                <template #default="{ row }">
                                    <div v-if="row.categoryPic">
                                        <el-image :src="imageFormat(row.categoryPic)" fit="contain"
                                            style="height: 30px; width: 30px" />
                                    </div>
                                </template>
                            </el-table-column>
                            <!-- <el-table-column :width="120" label="分类图标">
                                <template #default="{ row }">
                                    <i :class="'lyecs-defined-iconfont ' + row.categoryIco"></i>
                                </template>
                            </el-table-column> -->
                            <el-table-column :label="$t('单位')">
                                <template #default="{ row }">
                                    <PopForm v-model:org-value="row.measureUnit"
                                        :params="{ id: row.categoryId, field: 'measureUnit' }"
                                        :requestApi="updateCategoryFiled" :label="$t('单位')" type="input"
                                        @callback="changeCallback">
                                        <div>{{ row.measureUnit || "-" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否热门')">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isHot" :params="{ id: row.categoryId, field: 'isHot' }"
                                        :requestApi="updateCategoryFiled" />
                                </template>
                            </el-table-column>
                            <el-table-column key="isShow" :sorter="true" :label="$t('是否显示')">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isShow"
                                        :params="{ id: row.categoryId, field: 'isShow' }"
                                        :requestApi="updateCategoryFiled" />
                                </template>
                            </el-table-column>
                            <el-table-column key="sortOrder" :sorter="true" :label="$t('排序')">
                                <template #default="{ row }">
                                    <PopForm v-model:org-value="row.sortOrder"
                                        :params="{ id: row.categoryId, field: 'sortOrder' }"
                                        :requestApi="updateCategoryFiled" :label="$t('排序')" type="integer"
                                        @callback="changeCallback">
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="operationColumnWidth" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm :params="{ act: 'detail', id: row.categoryId }"
                                            :data="{ parentId: row.parentId }" isDrawer path="product/category/Info"
                                            :title="$t('编辑分类')" width="600px" @okCallback="updateChildFilter">
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm :params="{ act: 'add', parentId: row.categoryId }"
                                            :data="{ parentId: row.parentId || row.categoryId }" isDrawer
                                            path="product/category/Info" :title="$t('添加子分类')" width="600px"
                                            @okCallback="updateChildFilter">
                                            <a class="btn-link">{{ $t('添加子分类') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <el-dropdown trigger="hover" :hide-on-click="false">
                                            <el-icon class="btn-link" size="12">
                                                <MoreFilled />
                                            </el-icon>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item>
                                                        <DialogForm :params="{ id: row.categoryId }" isDrawer
                                                            path="product/category/Move" :title="$t('商品转移')" width="600px"
                                                            @okCallback="loadFilter">
                                                            <a class="btn-link">{{ $t('商品转移') }}</a>
                                                        </DialogForm>
                                                    </el-dropdown-item>
                                                    <el-dropdown-item>
                                                        <DeleteRecord v-if="row.parentId"
                                                            :params="{ id: row.categoryId }" :requestApi="delCategory"
                                                            @afterDelete="updateChildFilter({}, { parentId: row.parentId })"> {{ $t('删除') }} </DeleteRecord>
                                                        <DeleteRecord v-if="!row.parentId"
                                                            :params="{ id: row.categoryId }" :requestApi="delCategory"
                                                            @afterDelete="loadFilter">{{ $t('删除') }}</DeleteRecord>
                                                    </el-dropdown-item>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
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
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref, computed } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { PopForm } from "@/components/pop-form";
import { useRoute, useRouter } from "vue-router";
import { imageFormat } from "@/utils/format";
import { CategoryFilterParams, CategoryFilterState } from "@/types/product/category";
import { batchSubmit, delCategory, getCategoryList, updateCategoryFiled } from "@/api/product/category";
import { useCategoryStore } from "@/store/category";
import { isOverseas } from "@/utils/version";
import { MoreFilled } from "@element-plus/icons-vue";
import { useI18n } from "vue-i18n"
const { t, locale } = useI18n()
const categoryStore = useCategoryStore();
/** 路由实例 */
const { currentRoute } = useRouter();
const config: any = useConfigStore();
/** 自定义图标CSS路径 */
const icoDefinedCss = ref(config.icoDefinedCss);
/** 操作列宽度：英文「Add subcategory」更长，单独加宽 */
const operationColumnWidth = computed(() => {
    const isEn = String(locale.value).toLowerCase().startsWith("en");
    return isEn ? 240 : 180;
});

/** 分类列表数据 */
const filterState = ref<CategoryFilterState[]>([]);
/** 加载状态 */
const loading = ref<boolean>(true);
/** 总数 */
const total = ref<number>(0);
/** 选中的分类ID列表 */
const selectedIds = ref<number[]>([]);
/** 展开的行ID列表 */
const expandIds = ref([]);
/**
 * @description 查询参数
 * @property {number} parentId - 父分类ID，0表示顶级分类
 */
const filterParams = reactive<CategoryFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    isShow: -1,
    parentId: 0
});
/** 父分类ID层级栈，用于返回上一级 */
const parentId = ref<Array<number>>([0]);
/** 父分类名称 */
const parentName = ref<string>();
const route = useRoute();
/** 树形表格刷新工具，存储resolve方法和展开状态 */
const tableTreeRefreshTool = reactive<any>({});
onMounted(() => {
    const query = <any>currentRoute.value.query;
    if (query.id > 0) {
        filterParams.parentId = query.id;
    }
    route.meta.title = "";
    loadFilter();
});

/**
 * @description 获取分类列表数据
 */
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getCategoryList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * @description 懒加载子分类数据
 * @param tree 当前行数据
 * @param treeNode 树节点
 * @param resolve 数据加载完成回调
 */
const loadData = async (tree: any, treeNode: any, resolve: any) => {
    // 保存父级id，在你需要刷新子级数据的地方可以用到
    // currentParentId.value = tree.categoryId
    // 在之前声明的全局变量中，增加一个key为 本条数据的id，id可替换为你数据中的任意唯一值
    tableTreeRefreshTool[tree.categoryId] = {};
    // 重要！保存resolve方法，以便后续使用
    tableTreeRefreshTool[tree.categoryId].resolve = resolve;
    // 记录展开次数，具体作用后续介绍
    tableTreeRefreshTool[tree.categoryId].expandCount = 0;
    // 请求api接口获取数据
    const result = await getCategoryList({
        page: 1,
        parentId: tree.categoryId,
        size: 1000
    });
    // 调用resolve方法，渲染子级数据
    resolve(result.records);
    tree.children = result.records;
};
/**
 * @description 行展开/折叠状态变更处理
 * 用于刷新已展开过的子节点数据
 */
const handleExpandChange = async (row: any, expanded: any) => {
    // 获取到之前保存的全局变量
    const curr = tableTreeRefreshTool[row.categoryId];
    // 展开次数 +1
    curr.expandCount++;
    // 如果是展开状态，且展开次数大于1，且上一次的状态为折叠，则请求api数据，更新子菜单
    if (expanded && curr.expandCount > 1 && !curr.prevStatus) {
        // api请求
        const result = await getCategoryList({
            page: 1,
            parentId: row.categoryId,
            size: 1000
        });
        // 调用resolve方法，渲染子级数据
        curr.resolve(result.records);
    }
    // 保存本次的展开或折叠状态，用于下次判断
    curr.prevStatus = expanded;
};
/** 表格组件引用 */
const tableRef = ref();
/**
 * @description 更新当前层级的所有子级数据
 * 用于添加/编辑子分类后刷新父级下的所有子节点
 */
const updateChildFilter = async (res: any, data: any) => {
    const curr = tableTreeRefreshTool[data.parentId];
    if (curr) {
        const result = await getCategoryList({
            page: 1,
            parentId: data.parentId,
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
/**
 * @description 分类编辑后的回调，清除分类缓存
 */
const changeCallback = () => {
    categoryStore.clearCategory();
};

/**
 * @description 进入子分类列表
 * @param id 分类ID
 */
const childCat = (id: number) => {
    parentId.value.push(id);
    filterParams.parentId = id;
    loadFilter();
};
/**
 * @description 返回上一级分类
 */
const backToParentCat = () => {
    parentId.value.pop();
    filterParams.parentId = parentId.value[parentId.value.length - 1];
    loadFilter();
};

/**
 * @description 批量操作处理
 * @param action 操作类型：'del'-批量删除
 */
const onBatchSubmit = async (action: string) => {
    try {
        const result = await batchSubmit(action, { ids: selectedIds.value });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};

/**
 * @description 选中状态变更处理
 */
const onSelectChange = (e: any) => {
    selectedIds.value = e.map((item: any) => item.categoryId);
};
</script>

<style lang="less">
.el-table__expand-icon {
    top: -1px;
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

.el-table .cell {
    display: flex;
    align-items: center;
}
</style>
