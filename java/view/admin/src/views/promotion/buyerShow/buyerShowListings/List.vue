<template>
    <div class="lyecs-table-list-warp">
        <div class="simple-form-field">
            <div class="form-group">
                <DialogForm
                    :params="{ act: 'add' }"
                    isDrawer
                    :delayed="20000"
                    path="promotion/buyerShow/buyerShowListings/Info"
                    :title="$t('新建买家秀')"
                    width="800px"
                    @okCallback="loadFilter"
                >
                    <el-button type="primary">{{ $t('新建买家秀') }}</el-button>
                </DialogForm>
            </div>
        </div>
        <div class="list-table-tool lyecs-search-warp">
            <el-form :model="filterParams">
                <div class="advanced-search-warp list-table-tool-row">
                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <div class="form-group">
                                <label class="control-label" style="width: 100px;"><span>{{ $t('买家秀状态：') }}</span></label>
                                <div class="control-container">
                                    <el-select :empty-values="[null, undefined]" v-model="filterParams.status" clearable @clear="onSearchSubmit" @change="onSearchSubmit">
                                        <el-option v-for="(item, key) in buyShowStatus" :key="key" :value="item.value" :label="item.label" />
                                    </el-select>
                                </div>
                            </div>
                        </div>
                        <div class="simple-form-field">
                            <div class="form-group">
                                <label class="control-label" style="width: 100px;"><span>{{ $t('买家秀分组：') }}</span></label>
                                <div class="control-container">
                                    <el-select v-model="filterParams.goodsId" clearable @clear="onSearchSubmit" @change="onSearchSubmit">
                                        <el-option v-for="(item, key) in buyerShowGroupingList" :key="key" :value="item.buyerShowGroupId" :label="item.groupName" />
                                    </el-select>
                                </div>
                            </div>
                        </div>
                        <div class="simple-form-field">
                            <div class="form-group">
                                <label class="control-label" style="width: 100px;"><span>{{ $t('下单时间：') }}</span></label>
                                <SelectTimeInterval
                                    v-model:end-date="filterParams.endTime"
                                    v-model:start-date="filterParams.startTime"
                                    :clearable="false"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                ></SelectTimeInterval>
                            </div>
                        </div>
                        <div class="simple-form-field">
                            <div class="form-group">
                                <label class="control-label" style="width: 100px;"><span>{{ $t('买家秀内容：') }}</span></label>
                                <div class="control-container">
                                    <el-input :placeholder="$t('请输入买家秀内容')" v-model="filterParams.keyword" style="width: 240px" />
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
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <slot></slot>
                            <el-popconfirm :title="$t('您确认要批量通过所选数据吗？')" @confirm="onBatchSubmit('audit', { value: 1 })">
                                <template #reference>
                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量通过') }}</el-button>
                                </template>
                            </el-popconfirm>
                            <el-popconfirm :title="$t('您确认要批量拒绝所选数据吗？')" @confirm="onBatchSubmit('audit', { value: 2 })">
                                <template #reference>
                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量拒绝') }}</el-button>
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
                <el-table  :data="filterState"  row-key="buyerShowId" @selection-change="onSelectChange" :total="total" @sort-change="onSortChange" :loading="loading">
                    <el-table-column type="selection" width="32" :selectable="isSelectable" />
                    <el-table-column :label="$t('买家秀内容')" prop="content" width="300" class-name="myTooltips">
                        <template #default="{ row }">
                            <div class="contentBox">
                                <el-popover
                                    v-if="getImage(row)"
                                    class="box-item"
                                    placement="right"
                                    width="auto"
                                >
                                    <div class="contentImgs">
                                        <img v-for="(item, index) in getImageList(row)" :key="index" :src="item" height="80" width="80" />
                                    </div>
                                    <template #reference>
                                        <div class="imgBox">
                                            <img :src="getImage(row)" height="50" width="50" />
                                            <div class="imgCount">+{{ getImageList(row)?.length }}</div>
                                        </div>
                                    </template>
                                </el-popover>
                                <el-popover
                                    v-if="row.content"
                                    placement="top"
                                    :width="500"
                                    trigger="hover"
                                >
                                    <div style="word-break: break-all;">{{ row.content }}</div>
                                    <template #reference>
                                        <p class="ml5 content" style="overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">{{ row.content }}</p>
                                    </template>
                                </el-popover>
                                <p v-else class="ml5 content">{{ row.content }}</p>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('分组')" prop="groupList"></el-table-column>
                    <el-table-column :label="$t('商品数')" prop="productCount" width="70"></el-table-column>
                    <el-table-column :label="$t('点赞数')" prop="likeCount" width="70"></el-table-column>
                    <el-table-column :label="$t('评论数')" prop="commentCount" width="70">
                        <template #default="{ row }">
                            <DialogForm
                                v-if="row.commentCount"
                                :params="{ buyerShowId: row.buyerShowId }"
                                isDrawer
                                :delayed="20000"
                                path="promotion/buyerShow/buyerShowComments/commentManagementList"
                                :title="$t('相关评论')"
                                width="900px"
                                @okCallback="loadFilter"
                                @closeCallback="loadFilter"
                                :showClose="false"
                                :showOnOk="false"
                            >
                                <a class="btn-link mr10">{{ row.commentCount }}</a>
                            </DialogForm>
                            <span v-else>{{ row.commentCount }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('创建时间')" prop="addTime"></el-table-column>
                    <el-table-column :label="$t('发布人')" prop="buyerName"></el-table-column>
                    <el-table-column :label="$t('状态')" prop="statusText" width="70"></el-table-column>
                    <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom" width="90">
                        <template #default="{ row }">
                            <PopForm
                                :label="$t('排序')"
                                type="integer"
                                :requestApi="updateBuyerShowField"
                                v-model:org-value="row.sortOrder"
                                :params="{ id: row.buyerShowId, field: 'sortOrder' }"
                                :extra="$t('默认值为50，数值越小，排序越靠前')"
                            >
                                <div>{{ row.sortOrder }}</div>
                            </PopForm>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('操作')" fixed="right" :width="200">
                        <template #default="{ row }">
                            <div class="flex flex-align-center">
                                <div v-if="row.statusText === '待审核'">
                                    <a class="btn-link mr10" @click="changeBuyerShowPassOrReject({ buyerShowId: row.buyerShowId, status: 1})"> {{ $t('通过') }} </a>
                                    <a class="btn-link mr10" v-if="row.statusText === '待审核'" @click="changeBuyerShowPassOrReject({ buyerShowId: row.buyerShowId, status: 2})"> {{ $t('拒绝') }} </a>
                                </div>
                                <a class="btn-link mr10" v-if="['公开', '隐藏'].includes(row.statusText)" @click="changeBuyerShowShowOrHide({ buyerShowId: row.buyerShowId, isShow: row.statusText === '隐藏' ? 1 : 0})">
                                    {{ row.statusText === $t('隐藏') ? $t('公开') : $t('隐藏') }}
                                </a>
                                <DialogForm isDrawer @okCallback="loadFilter" :title="$t('买家秀详情')" width="800px" path="promotion/buyerShow/buyerShowListings/Info"
                                            :params="{ act: 'detail', id: row.buyerShowId }" :showClose="false" :showOnOk="false">
                                    <a class="btn-link mr10">{{ $t('查看详情') }}</a>
                                </DialogForm>
                                <el-dropdown v-if="row.status === 1 || row.status === 2">
                                    <span class="btn-link"> {{ $t('更多') }} </span>
                                    <template #dropdown>
                                        <el-dropdown-menu>
                                            <el-dropdown-item v-if="row.statusText === '公开'">
                                                <DialogForm isDrawer @okCallback="loadFilter" :title="$t('新建评论')" width="600px" path="promotion/buyerShow/buyerShowListings/addComments"
                                                            :params="{ act: 'add', id: row.buyerShowId }">
                                                    <span class="btn-link">{{ $t('评论') }}</span>
                                                </DialogForm>
                                            </el-dropdown-item>
                                            <el-dropdown-item>
                                                <DialogForm isDrawer @okCallback="loadFilter" :title="$t('编辑买家秀')" width="800px" path="promotion/buyerShow/buyerShowListings/Info"
                                                            :params="{ act: 'edit', id: row.buyerShowId }">
                                                    <span class="btn-link">{{ $t('编辑') }}</span>
                                                </DialogForm>
                                            </el-dropdown-item>
                                            <el-dropdown-item>
                                                <DeleteRecord
                                                    class="btn-link"
                                                    @afterDelete="loadFilter"
                                                    :requestApi="deleteBuyerShow"
                                                    :params="{ id: row.buyerShowId }"
                                                ><span class="btn-link">{{ $t('删除') }}</span></DeleteRecord>
                                            </el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                            </div>
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
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { DeleteRecord, Pagination } from "@/components/list";
import { onMounted, ref, markRaw  } from "vue";
import { useListRequest } from "@/hooks/useListRequest";
import { buyerShowListFilterParams, buyerShowListFilterState } from "@/types/promotion/buyerShowListings";
import {
    getBuyerShowList,
    getBuyerShowGroupList,
    updateBuyerShowField,
    batchBuyerShow,
    buyerShowShowOrHide, buyerShowAudit, deleteBuyerShow
} from "@/api/promotion/buyShow";
import { useConfigStore } from "@/store/config";
import { DialogForm } from "@/components/dialog";
import { SelectTimeInterval } from "@/components/select";
import { ElMessageBox } from "element-plus";
import { WarningFilled } from '@element-plus/icons-vue'
import { message } from "ant-design-vue";
import { PopForm } from "@/components/pop-form";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
} = useListRequest<buyerShowListFilterState, buyerShowListFilterParams>({
    apiFunction: getBuyerShowList,
    idKey: "buyerShowId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        content: "",
        startTime: '',
        endTime: '',
        goodsId: "",
        status: "",
    }
});

// 批量操作
const onBatchSubmit = async (action: string, data: object) => {
    await onBatchAction(action, batchBuyerShow, data);
};

const buyShowStatus = ref([
    {
        value: '',
        label: t('全部状态')
    },
    {
        value: -1,
        label: t('待审核')
    },
    {
        value: 0,
        label: t('隐藏')
    },
    {
        value: 1,
        label: t('公开')
    }
]);

const buyerShowGroupingList: any = ref([])

// 获取买家秀分组列表
const getBuyerShowGroup = async () => {
    const res = await getBuyerShowGroupList({page: 1, size: 1000});
    buyerShowGroupingList.value = res.records || []
};

// 买家秀显示隐藏
const changeBuyerShowShowOrHide = async (data: any) => {
    ElMessageBox.confirm(
        data.isShow == 0 ? t('隐藏后，该买家秀仅创建人自己可见') : t('公开后，该买家秀将变为公开状态，所有客户可见'),
        {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            icon: markRaw(WarningFilled),
            customClass: 'buyShowHide',
        }
    ).then(async () => {
            await buyerShowShowOrHide(data).then(()=>{
                loadFilter();
                message.success(t('操作成功'));
            }).catch((error: any) => {
                message.error(error.message);
            })
        })
        .catch((error: any) => {
            // message.error(error.message);
            console.log(error.message);
        })
}

// 买家秀通过或拒绝
const changeBuyerShowPassOrReject = async (data: any) => {
    ElMessageBox.confirm(
        data.status == 1 ? t('通过后，该买家秀将变为公开状态，所有客户可见') : t('拒绝后，该买家秀将变为隐藏状态，所有客户不可见'),
        {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            icon: markRaw(WarningFilled),
            customClass: 'buyShowHide',
        }
    ).then(async () => {
            await buyerShowAudit(data).then(()=>{
                loadFilter();
                message.success(t('操作成功'));
            }).catch((error: any) => {
                message.error(error.message);
            })
        })
        .catch((error: any) => {
            // message.error(error.message);
            console.log(error.message);
        })
}

// 判断是否可选 (待审核才能勾选)
const isSelectable = (row: any, index: number) => {
    return row.status === 0
};

const getImage = (row: buyerShowListFilterState) => {
    return row.videoList?.[0]?.videoCover || row.imgList?.[0]?.picUrl
}

const getImageList = (row: buyerShowListFilterState) => {
    return row.videoList?.concat(row.imgList || []).map((item: any) => item.videoCover || item.picUrl).filter(Boolean)
}

onMounted(()=>{
    getBuyerShowGroup();
})

// 初始化加载
loadFilter();
</script>

<style lang="less" scoped>
.buyShowHide{
    //background: #f00;
    .el-message-box__content{
         .el-icon{
            svg{
                color: #006aff;
            }
        }
    }

}

.contentBox{
    display: flex;
    align-items: center;
    img{
        flex-shrink: 0;
        margin-right: 5px;
    }
    .content{
        word-break: break-all;
    }
}

.imgBox{
    position: relative;
    .imgCount{
        position: absolute;
        top: 0;
        right: 5px;
        padding: 0 5px;
        color: #fff;
        background-color: rgba(0, 0, 0, .5);
    }
}
.el-table{
    :deep(.el-popper.is-dark) {
        max-width: 50%;
    }
}

</style>
