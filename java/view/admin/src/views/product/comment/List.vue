/** * @file 商品评论列表页面 * @description 商品评论管理列表，支持查看、编辑、删除商品评论。 * @features * - Tab切换：全部评价/有晒单评价 * - 评论内容搜索 * -
添加用户评论（仅平台管理员） * - 行内编辑（评论等级、排序） * - 置顶和推荐状态开关 * - 晒单图片展示 * - 批量删除 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                <el-tab-pane :name="-1" :label="$t('全部评价')"></el-tab-pane>
                <el-tab-pane :name="1" :label="$t('有晒单')"></el-tab-pane>
            </el-tabs>
            <div v-if="activeKey != null" class="container">
                <div class="lyecs-table-list-warp">
                    <div class="list-table-tool lyecs-search-warp">
                        <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                            <div class="list-table-tool-row">
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <div class="control-container">
                                                <TigInput
                                                    v-model="filterParams.keyword"
                                                    name="keyword"
                                                    :placeholder="$t('请输入评价内容')"
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
                                    <div class="simple-form-field" v-if="adminType === 'admin'">
                                        <div class="form-group">
                                            <DialogForm
                                                :params="{ act: 'add' }"
                                                isDrawer
                                                path="product/comment/Info"
                                                :title="$t('添加用户评论')"
                                                width="600px"
                                                @okCallback="loadFilter"
                                            >
                                                <el-button type="primary">{{ $t('添加用户评论') }}</el-button>
                                            </DialogForm>
                                        </div>
                                    </div>
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                                <template #reference>
                                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                                </template>
                                            </el-popconfirm>
                                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-form>
                    </div>
                    <div class="table-container">
                        <a-spin :spinning="loading">
                            <el-table
                                :data="filterState"
                                :loading="loading"
                                :total="total"
                                row-key="commentId"
                                @selection-change="onSelectChange"
                                @sort-change="onSortChange"
                            >
                                <el-table-column type="selection" width="32" />
                                <el-table-column :width="150" :label="$t('用户名称')" prop="username">
                                    <template #default="{ row }">
                                        <div class="display-col">
                                            <div v-if="row.avatar" class="image avatar flex">
                                                <Image :src="imageFormat(row.avatar)" class="image-style" fit="cover" />
                                            </div>
                                            <el-tooltip class="box-item" effect="light" :content="row.username" placement="bottom">
                                                <div class="text">
                                                    {{ row.username }}
                                                </div>
                                            </el-tooltip>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :width="260" :label="$t('评论对象')">
                                    <template #default="{ row }">
                                        <div class="display-col">
                                            <div class="image_comment">
                                                <Image :src="imageFormat(row.picThumb)" class="image-style shopPics" fit="contain" />
                                            </div>
                                            <div class="text_comment multiline_hiding">
                                                <a :href="urlFormat({ path: 'product', sn: row.productSn })" target="_blank"> {{ row.productName }} </a>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :width="260" :label="$t('评价内容')" prop="content">
                                    <template #default="{ row }">
                                        <div class="text_comment">
                                            {{ row.content }}
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :width="250" :label="$t('晒单')" prop="showPics">
                                    <template #default="{ row }">
                                        <div v-if="row.showPics" class="image">
                                            <template v-if="row.showPics.length > 0" v-for="(item, index) in row.showPics">
                                                <Image class="shopPics" v-if="index < 4" :src="imageFormat(item.picThumb)" width="50" style="" fit="contain" />
                                            </template>
                                        </div>
                                        <div v-else>--</div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('评论等级')" prop="commentRank" sortable="custom" width="120">
                                    <template #default="{ row }">
                                        <PopForm
                                            v-model:org-value="row.commentRank"
                                            :params="{ id: row.commentId, field: 'commentRank' }"
                                            type="integer"
                                            :minNum="1"
                                            :maxNum="5"
                                            :requestApi="updateCommentFiled"
                                            :label="$t('评论等级')"
                                        >
                                            <div>{{ $t('{commentRank}星', { commentRank: row.commentRank }) }}</div>
                                        </PopForm>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('评论日期')" prop="addTime" sortable="custom" width="160"></el-table-column>
                                <el-table-column :label="$t('是否置顶')" prop="isTop" sortable="custom" width="120">
                                    <template #default="{ row }">
                                        <Switch v-model:checked="row.isTop" :params="{ id: row.commentId, field: 'isTop' }" :requestApi="updateCommentFiled" />
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('是否推荐')" prop="isRecommend" sortable="custom" width="120">
                                    <template #default="{ row }">
                                        <Switch
                                            v-model:checked="row.isRecommend"
                                            :params="{ id: row.commentId, field: 'isRecommend' }"
                                            :requestApi="updateCommentFiled"
                                        />
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                    <template #default="{ row }">
                                        <PopForm
                                            v-model:org-value="row.sortOrder"
                                            :params="{ id: row.commentId, field: 'sortOrder' }"
                                            :requestApi="updateCommentFiled"
                                            type="integer"
                                            :label="$t('排序')"
                                        >
                                            <div>{{ row.sortOrder }}</div>
                                        </PopForm>
                                    </template>
                                </el-table-column>
                                <el-table-column width="100" fixed="right" :label="$t('操作')">
                                    <template #default="{ row }">
                                        <DialogForm
                                            :params="{ act: 'detail', id: row.commentId, examine: adminType === 'admin' ? 0 : 1 }"
                                            isDrawer
                                            :showClose="adminType === 'admin' ? true : false"
                                            :showOnOk="adminType === 'admin' ? true : false"
                                            path="product/comment/Info"
                                            :title="adminType === 'admin' ? $t('编辑') : $t('查看') + $t('用户评论')"
                                            width="700px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ adminType === "admin" ? $t('编辑') : $t('查看') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord :params="{ id: row.commentId }" :requestApi="delComment" @afterDelete="loadFilter">{{ $t('删除') }}</DeleteRecord>
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
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { Image } from "@/components/image";
import { useConfigStore } from "@/store/config";
import { CommentFilterParams, CommentFilterState } from "@/types/product/comment";
import { batchSubmit, delComment, getCommentList, updateCommentFiled } from "@/api/product/comment";
import { imageFormat, urlFormat } from "@/utils/format";
import { useListRequest } from "@/hooks/useListRequest";
import { getAdminType } from "@/utils/storage";
/** 当前管理员类型 */
const adminType = getAdminType();
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
} = useListRequest<CommentFilterState, CommentFilterParams>({
    apiFunction: getCommentList,
    idKey: "commentId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        isShowed: -1,
        page: 1,
        size: config.get("pageSize")
    }
});
/**
 * @description 批量操作处理
 * @param action 操作类型：'del'-批量删除
 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};
// 初始化加载
loadFilter();

/** 当前激活的Tab：-1-全部，1-有晒单 */
const activeKey = ref<number>(-1);
/**
 * @description Tab切换处理
 * @param val Tab值
 */
const onTabChange = (val: number) => {
    filterParams.isShowed = val;
    filterParams.page = 1;
    loadFilter();
};
</script>
<style lang="less" scoped>
.status-switch {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    width: 150px;
}

.status-switch > div {
    word-break: keep-all;
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.display-col {
    display: flex;
    align-items: center; /* 垂直居中 */
    gap: 8px;

    .image {
        width: 30px;
        height: 30px;

        .image-style {
            width: 100%;
            height: 100%;
            border-radius: 100%;
            object-fit: cover; /* 保持图片比例 */
        }
    }

    .image_comment {
        min-width: 50px;
        min-height: 50px;
        max-width: 50px;
        max-height: 50px;

        .image-style {
            width: 100%;
            height: 100%;
            object-fit: cover; /* 保持图片比例 */
        }
    }
    .text {
        max-width: 80px;
        flex-grow: 1; /* 占据剩余空间 */
        white-space: nowrap; /* 不换行 */
        overflow: hidden; /* 隐藏超出部分 */
        text-overflow: ellipsis; /* 超出显示省略号 */
    }
}
.text_comment {
    /*最多两行*/
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}
.shopPics {
    margin-right: 5px;
    margin-top: 5px;
    background: #fff;
    border: 1px solid #eee;
    border-radius: 3px;
}
.multiline_hiding {
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2; /* 限制在3行 */
    -webkit-box-orient: vertical;
    a {
        color: #343434;
    }
}
</style>
