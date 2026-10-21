<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('文章名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入文章名称')"
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
                                        <label class="control-label"><span>{{ $t('全部分类：') }}</span></label>
                                        <div class="control-container">
                                            <SelectArticleCategory v-model:articeCategoryId="articeCategoryId" @onChange="onChange"></SelectArticleCategory>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('是否显示：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.isShow" clearable @change="onSearchSubmit">
                                                <el-option :value="0" :label="$t('不显示')" />
                                                <el-option :value="1" :label="$t('显示')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('是否热门：') }}</span></label>
                                        <div class="control-container">
                                            <el-select :fit-input-width="false" v-model="filterParams.isHot" clearable @change="onSearchSubmit">
                                                <el-option :value="0" :label="$t('不热门')" />
                                                <el-option :value="1" :label="$t('热门')" />
                                            </el-select>
                                        </div>
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
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('添加文章')"
                                        width="580px"
                                        path="content/article/Info"
                                        :params="{ act: 'add' }"
                                    >
                                        <el-button type="primary">{{ $t('添加文章') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm :title="$t('您确认要批量显示所选数据吗？')" @confirm="onBatchSubmit('show')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量显示') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm :title="$t('您确认要批量隐藏所选数据吗？')" @confirm="onBatchSubmit('hide')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量隐藏') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-button :disabled="selectedIds.length === 0" v-if="selectedIds.length == 0">{{ $t('批量转移分类') }}</el-button>
                                    <DialogForm
                                        v-else
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('批量转移分类')"
                                        width="600px"
                                        path="content/article/Move"
                                        :params="{ ids: selectedIds }"
                                    >
                                        <el-button>{{ $t('批量转移分类') }}</el-button>
                                    </DialogForm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="articleId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('文章标题')" prop="articleId" :width="300">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <PopForm
                                            :label="$t('文章标题')"
                                            type="textarea"
                                            :requestApi="updateArticleFiled"
                                            v-model:org-value="row.articleTitle"
                                            :params="{ id: row.articleId, field: 'articleTitle' }"
                                            :max="50"
                                        >
                                            <div>{{ row.articleTitle }}</div>
                                        </PopForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('文章编号')">
                                <template #default="{ row }">
                                    <PopForm
                                        :label="$t('文章编号')"
                                        type="input"
                                        :requestApi="updateArticleFiled"
                                        v-model:org-value="row.articleSn"
                                        :params="{ id: row.articleId, field: 'articleSn' }"
                                        :max="10"
                                    >
                                        <div>{{ row.articleSn || "-" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('文章分类')" prop="articleCategoryName"></el-table-column>
                            <el-table-column :label="$t('文章类型')" prop="articleTypeText"> </el-table-column>
                            <el-table-column width="100" :label="$t('文章重要性')">
                                <template #default="{ row }">
                                    {{ row.isTop == 0 ? $t('普通') : $t('置顶') }}
                                </template>
                            </el-table-column>
                            <el-table-column width="160" :label="$t('发布时间')">
                                <template #default="{ row }">
                                    {{ row.addTime }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否热门')" prop="isHot" sortable="custom" :width="120">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isHot" :requestApi="updateArticleFiled" :params="{ id: row.articleId, field: 'isHot' }" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否显示')" prop="isShow" sortable="custom" :width="120">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isShow" :requestApi="updateArticleFiled" :params="{ id: row.articleId, field: 'isShow' }" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="110">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑文章')"
                                        width="580px"
                                        path="content/article/Info"
                                        :params="{ act: 'detail', id: row.articleId }"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delArticle" :params="{ id: row.articleId }">{{ $t('删除') }}</DeleteRecord>
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
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { ref, watch } from "vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Switch, Pagination } from "@/components/list";
import { getArticleList, batchSubmit, updateArticleFiled, delArticle } from "@/api/content/article";
import type { ArticleFilterState, ArticleFilterParams } from "@/types/content/article.d";
import SelectArticleCategory from "@/components/select/src/SelectArticleCategory.vue";
import { useConfigStore } from "@/store/config";
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
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
    onBatchAction
} = useListRequest<ArticleFilterState, ArticleFilterParams>({
    apiFunction: getArticleList,
    idKey: "articleId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        articleCategoryId: "",
        isHot: "",
        isShow: "",
        page: 1,
        size: config.get("pageSize")
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit, {});
};

// 初始化加载
loadFilter();
const articeCategoryId = ref<number[]>([]);
const onChange = (ids: number[]) => {
    filterParams.articleCategoryId = Array.isArray(ids) ? ids.join(",") : "";
    onSearchSubmit();
};

// 自定义重置参数函数
const resetParams = () => {
    const params = {
        sortField: "",
        sortOrder: "",
        keyword: "",
        articleCategoryId: "",
        isHot: "",
        isShow: "",
        page: 1,
        size: config.get("pageSize")
    };
    Object.assign(filterParams, params);
    loadFilter();
    articeCategoryId.value = [];
};

// 分类选择处理
// watch(articeCategoryId, (newVal) => {
// filterParams.articleCategoryId = Array.isArray(newVal) ? newVal.join(",") : "";
// onSearchSubmit();
// });

// watch(filterParams, (newVal) => {
//     if (!newVal.articleCategoryId) {
//         articeCategoryId.value = [];
//     }
// });
</script>
