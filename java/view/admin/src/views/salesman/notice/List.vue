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
                                        <label class="control-label"><span>{{ $t('标题：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.title"
                                                name="keyword"
                                                :placeholder="$t('请输入内容标题')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            ></TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field" v-if="noticeConfig">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" :placeholder="$t('请选择文章状态')" @change="onSearchSubmit" clearable>
                                                <el-option v-for="(item, index) in noticeConfig" :label="item" :value="index" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('推送时间：') }}</span></label>
                                        <div class="flex flex-align-center">
                                            <SelectTimeInterval
                                                v-model:start-date="filterParams.startTime"
                                                v-model:end-date="filterParams.endTime"
                                                value-format="YYYY-MM-DD HH:mm:ss"
                                                @callback="changeDateType(-1)"
                                            ></SelectTimeInterval>
                                            <div style="margin-left: 10px">
                                                <el-radio-group class="itemWidth" v-model="dateType" @change="changeDateType">
                                                    <el-radio-button :value="0">{{ $t('今天') }}</el-radio-button>
                                                    <el-radio-button :value="1">{{ $t('昨天') }}</el-radio-button>
                                                    <el-radio-button :value="2">{{ $t('近7天') }}</el-radio-button>
                                                    <el-radio-button :value="3">{{ $t('近30天') }}</el-radio-button>
                                                </el-radio-group>
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
                        </div>
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('添加活动通知')"
                                        width="700px"
                                        path="salesman/notice/Info"
                                        :params="{ act: 'add' }"
                                    >
                                        <el-button type="primary">{{ $t('立即新建') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :total="total" row-key="productId" @selection-change="onSelectChange" @sort-change="onSortChange">
                            <el-table-column :label="$t('标题')" prop="productId" :width="250" fixed="left" show-overflow-tooltip>
                                <template #default="{ row }">
                                    <div class="tit-row flex flex-align-center">
                                        <div class="label" v-if="row.isTop != 0">{{ $t('置顶') }}</div>
                                        <div class="txt ellipsis">{{ row.title }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('推送时间')" :width="200" sortable="custom">
                                <template #default="{ row }"> {{ row.startTime }} </template>
                            </el-table-column>
                            <el-table-column :label="$t('失效时间')" :width="200" sortable="custom">
                                <template #default="{ row }"> {{ row.endTime || "--" }} </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')">
                                <template #default="{ row }"> {{ row.statusText }} </template>
                            </el-table-column>
                            <el-table-column :width="200" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DeleteRecord
                                            v-if="row.isTop != 0"
                                            @afterDelete="loadFilter"
                                            :requestApi="updatenoticeFiled"
                                            :params="{ id: row.id, field: 'isTop', val: 0 }"
                                            :title="$t('确定取消置顶吗？')"
                                            >{{ $t('取消置顶') }}</DeleteRecord
                                        >
                                        <el-divider v-if="row.isTop != 0" direction="vertical" />
                                        <DialogForm
                                            :params="{ act: 'detail', id: row.id }"
                                            isDrawer
                                            path="salesman/notice/Info"
                                            :title="$t('编辑内容')"
                                            width="700px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord
                                            v-if="row.isAvailable == 1"
                                            @afterDelete="loadFilter"
                                            :requestApi="updatenoticeFiled"
                                            :params="{ id: row.id, field: 'isAvailable', val: 0 }"
                                            :title="$t('失效后将不可恢复，确认失效？')"
                                            :message="$t('操作成功')"
                                            >{{ $t('失效') }}</DeleteRecord
                                        >
                                        <DeleteRecord v-else @afterDelete="loadFilter" :requestApi="delnotice" :params="{ id: row.id }">{{ $t('删除') }}</DeleteRecord>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
                <div v-if="selectedIds.length > 0" class="selected-action-warp selected-warp-left">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                            >
                            <el-button>{{ $t('设置等级') }}</el-button>
                            <el-button>{{ $t('分组') }}</el-button>
                            <el-button>{{ $t('清退') }}</el-button>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { SelectTimeInterval } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { formattedDate } from "@/utils/time";
import { getDays } from "@/utils/util";
import dayjs from "dayjs";
import type { noticeFilterState, noticeFilterParams } from "@/types/salesman/notice.d";
import { getnoticeList, batchSubmit, delnotice, getnoticeConfig, updatenoticeFiled } from "@/api/salesman/notice";
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
    onBatchAction,
    resetParams
} = useListRequest<noticeFilterState, noticeFilterParams>({
    apiFunction: getnoticeList,
    idKey: "productId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        title: "",
        status: "",
        endTime: "",
        startTime: ""
    }
});

// 初始化加载
loadFilter();
const dateType = ref(-1);
const changeDateType = (event: number) => {
    if (event === -1) {
        dateType.value = event;
        return;
    }
    filterParams.startTime = dayjs().startOf("day").format("YYYY-MM-DD HH:mm:ss");
    if (event === 0) {
        //今天
        filterParams.endTime = dayjs().endOf("day").format("YYYY-MM-DD HH:mm:ss");
    }
    if (event === 1) {
        //昨天
        filterParams.startTime = dayjs().subtract(1, "day").startOf("day").format("YYYY-MM-DD HH:mm:ss");
        filterParams.endTime = dayjs().subtract(1, "day").endOf("day").format("YYYY-MM-DD HH:mm:ss");
    }
    if (event === 2) {
        //近七天
        filterParams.startTime = formattedDate(getDays(7, "sub"), "YYYY-MM-DD HH:mm:ss");
        filterParams.endTime = formattedDate(new Date(), "YYYY-MM-DD HH:mm:ss");
    }
    if (event === 3) {
        //近30天
        filterParams.startTime = formattedDate(getDays(30, "sub"), "YYYY-MM-DD HH:mm:ss");
        filterParams.endTime = formattedDate(new Date(), "YYYY-MM-DD HH:mm:ss");
    }
    loadFilter();
};
const noticeConfig = ref();
const loadConfig = async () => {
    loading.value = true;
    try {
        const result = await getnoticeConfig();
        noticeConfig.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
loadConfig();
</script>
<style lang="less" scoped>
.list-table-tool {
    margin-bottom: 0;
}
.tit-row {
    .label {
        display: inline-block;
        margin-right: 5px;
        color: #356fd4;
        border: 1px solid #356fd4;
        border-radius: 2px;
        height: 15px;
        line-height: 14px;
        padding: 0 3px;
    }
    .red {
        color: #ed6a18;
        border-color: #ed6a18;
    }
}
.bonus-wrap .card-wrap {
    display: -ms-flexbox;
    display: flex;
    margin-bottom: 24px;
    -ms-flex-pack: start;
    justify-content: flex-start;
    overflow: hidden;
    z-index: -1;
}

.bonus-wrap .card-wrap__container {
    transition: transform 0.6s ease-in-out;
    display: -ms-flexbox;
    display: flex;
    flex: 1;
}

.bonus-wrap .item-bg {
    position: relative;
    white-space: nowrap;
    padding: 20px 27px 17px;
    display: -ms-flexbox;
    display: flex;
    -ms-flex-direction: column;
    flex-direction: column;
    background: rgba(51, 136, 255, 0.07);
    margin-right: 15px;
    text-align: center;
    -ms-flex-align: center;
    align-items: center;
    -ms-flex-pack: justify;
    justify-content: space-between;
}

.bonus-wrap .coupon-cards-item {
    width: 20%;
}

.bonus-wrap .coupon-cards-item .title {
    font-size: 16px;
    color: #333;
}

.bonus-wrap .coupon-cards-item .desc {
    padding-top: 10px;
    line-height: 18px;
    font-size: 12px;
    color: #666;
}

.bonus-wrap .coupon-cards-item button {
    margin-top: 15px;
    padding: 0 16px;
}

.bonus-wrap .subtitle {
    font-weight: 500;
    font-size: 14px;
    color: #323233;
    line-height: 20px;
    margin-bottom: 16px;
}

.bonus-wrap .item-bg2 {
    background: #fff;
    border: 1px solid #ebedf0;
    box-sizing: border-box;
    width: 223px !important;
}

@media only screen and (max-width: 767px) {
    .bonus-wrap .card-wrap__container {
        flex-wrap: wrap;
    }
    .bonus-wrap .item-bg {
        width: 47% !important;
        box-sizing: border-box;
        margin: 0;
        margin-top: 10px;
    }
    .bonus-wrap .item-bg:nth-child(2n) {
        margin-left: 3%;
    }
}
.ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    width: 90%;
}
</style>
