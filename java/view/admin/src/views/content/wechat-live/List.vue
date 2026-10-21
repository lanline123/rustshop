<template>
    <div class="page-alert">
        <el-icon size="18" color="#ed6a18"><WarnTriangleFilled /></el-icon>
        <span>
            {{ $t('初次开通，需提交小程序审核，通过后方可正常访问直播间，请先前往“商城设置-->小程序设置"配置相关信息。')
            }}<router-link :to="{ path: '/setting/weChat' }">{{ $t("去配置") }}</router-link>
        </span>
    </div>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入直播间名称')"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("直播状态：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select v-model="filterParams.liveStatus" clearable :placeholder="$t('请选择')" @change="onSearchSubmit">
                                                <el-option :value="101" :label="$t('直播中')" />
                                                <el-option :value="102" :label="$t('未开始')" />
                                                <el-option :value="103" :label="$t('已结束')" />
                                                <el-option :value="104" :label="$t('禁播')" />
                                                <el-option :value="105" :label="$t('暂停')" />
                                                <el-option :value="106" :label="$t('异常')" />
                                                <el-option :value="107" :label="$t('已过期')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("创建时间：") }}</span></label
                                        >
                                        <SelectTimeInterval
                                            v-model:end-date="filterParams.endTime"
                                            v-model:start-date="filterParams.startTime"
                                            :clearable="false"
                                            type="date"
                                            value-format="YYYY-MM-DD"
                                        ></SelectTimeInterval>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t("搜索") }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t("重置") }}</el-button>
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
                                        :title="$t('新建直播间')"
                                        width="820px"
                                        path="content/wechat-live/Info"
                                        :params="{ act: 'add' }"
                                        :bodyStyle="{ padding: '0' }"
                                        :showClose="false"
                                        :showOnOk="false"
                                        :footerStyle="{ display: 'none' }"
                                    >
                                        <el-button type="primary">{{ $t("新建直播间") }}</el-button>
                                    </DialogForm>
                                    <el-button @click="handleSync">{{ $t("同步直播间") }}</el-button>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :row-key="(row: WechatLiveRoomState) => row.wechatLiveId || row.roomId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('直播间信息')" :min-width="240">
                                <template #default="{ row }">
                                    <div class="live-room-info">
                                        <Image :src="row.feedsImg" fit="cover" style="width: 60px; height: 40px" />
                                        <div class="live-room-info__text">
                                            <div class="live-name">{{ row.wechatLiveTitle || "-" }}</div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('房间号')" prop="roomId" :width="120"></el-table-column>
                            <el-table-column :label="$t('主播昵称')" prop="anchorName" :width="120"></el-table-column>
                            <el-table-column :label="$t('官方收录')" :width="100">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.isFeedsPublic"
                                        :requestApi="updateWechatLiveRoomField"
                                        :params="{ id: row.wechatLiveId || row.roomId, field: 'isFeedsPublic' }"
                                        :switching-code="[0, 1]"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('审核状态')" prop="auditStatusText" :width="120">
                                <template #default="{ row }">
                                    <span>{{ row.auditStatusText ? $t(row.auditStatusText) : "-" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('直播状态')" prop="liveStatusText" :width="120">
                                <template #default="{ row }">
                                    <span>{{ row.liveStatusText ? $t(row.liveStatusText) : "-" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('最近开播')" :width="160">
                                <template #default="{ row }">
                                    <div>{{ row.startTime || "-" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="locale === 'zh' ? 200 : 300">
                                <template #default="{ row }">
                                    <el-space :size="4">
                                        <DialogForm
                                            isDrawer
                                            :title="$t('管理商品')"
                                            width="900px"
                                            path="content/wechat-live/manageProduct"
                                            :params="{ id: row.roomId, roomData: row }"
                                            :bodyStyle="{ padding: '0' }"
                                            :showClose="false"
                                            :showOnOk="false"
                                            :footerStyle="{ display: 'none' }"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button type="primary" link>{{ $t("管理商品") }}</el-button>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <Advertise :title="$t('推广')" :item="row" @callBack="loadFilter">
                                            <div style="line-height: 18px">
                                                <el-button type="primary" link>{{ $t("投放") }}</el-button>
                                            </div>
                                        </Advertise>
                                        <el-divider direction="vertical" />
                                        <el-dropdown>
                                            <span class="more-action">
                                                {{ $t("更多") }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                                            </span>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item v-if="Number(row.liveType) === 0">
                                                        <el-button style="width: 100%" link type="primary" @click="handleViewBroadcastCode(row)">{{
                                                            $t("开播码")
                                                        }}</el-button>
                                                    </el-dropdown-item>
                                                    <el-dropdown-item v-if="Number(row.liveType) === 1">
                                                        <el-button style="width: 100%" link type="primary" @click="handleViewPushInfo(row)">{{
                                                            $t("推流地址")
                                                        }}</el-button>
                                                    </el-dropdown-item>
                                                    <el-dropdown-item>
                                                        <DialogForm
                                                            style="width: 100%"
                                                            isDrawer
                                                            @okCallback="loadFilter"
                                                            :title="$t('编辑直播间')"
                                                            width="820px"
                                                            path="content/wechat-live/Info"
                                                            :params="{ act: 'edit', id: row.wechatLiveId || row.roomId }"
                                                            :bodyStyle="{ padding: '0' }"
                                                            :showClose="false"
                                                            :showOnOk="false"
                                                            :footerStyle="{ display: 'none' }"
                                                        >
                                                            <el-button style="width: 100%" link type="primary">{{ $t("编辑") }}</el-button>
                                                        </DialogForm>
                                                    </el-dropdown-item>
                                                    <el-dropdown-item @click="handleDelete(row)">
                                                        <el-button style="width: 100%" link type="primary">{{ $t("删除") }}</el-button>
                                                    </el-dropdown-item>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
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
        <pushTheFlow
            v-model:show-dialog="pushTheFlowDialog"
            :title="$t('推流地址')"
            header-class="dailogheader"
            :loading="pushInfoLoading"
            :push-info="currentPushInfo"
            :room-name="currentRoomName"
        ></pushTheFlow>
        <startTheBroadcastCode
            v-model:show-dialog="startTheBroadcastCodeDialog"
            :title="$t('开播码')"
            header-class="dailogheader"
            :loading="pushInfoLoading"
            :push-info="currentPushInfo"
            :room-name="currentRoomName"
        ></startTheBroadcastCode>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { ArrowDown } from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";
import { Image } from "@/components/image";
import { DialogForm } from "@/components/dialog";
import { Switch, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import type { WechatLivePushUrlState, WechatLiveRoomState, WechatLiveRoomFilterParams } from "@/types/content/wechatLive.d";
import { delWechatLiveRoom, getWechatLivePushUrl, getWechatLiveRoomList, syncWechatLiveRoom, updateWechatLiveRoomField } from "@/api/content/wechatLive";
import { SelectTimeInterval } from "@/components/select";
import Advertise from "@/views/content/wechat-live/src/Advertise.vue";
import pushTheFlow from "@/views/content/wechat-live/src/components/pushTheFlow.vue";
import startTheBroadcastCode from "@/views/content/wechat-live/src/components/startTheBroadcastCode.vue";
import { ref } from "vue";
import { WarnTriangleFilled } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n";
const { t, locale } = useI18n();
const config: any = useConfigStore();

const pushTheFlowDialog = ref(false);
const startTheBroadcastCodeDialog = ref(false);
const pushInfoLoading = ref(false);
const currentPushInfo = ref<WechatLivePushUrlState | null>(null);
const currentRoomName = ref("");

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    resetParams
} = useListRequest<WechatLiveRoomState, WechatLiveRoomFilterParams>({
    apiFunction: getWechatLiveRoomList,
    idKey: "wechatLiveId",
    defaultParams: {
        keyword: "",
        liveStatus: undefined,
        startTime: "",
        endTime: "",
        sortField: "",
        sortOrder: "",
        page: 1,
        size: config.get("pageSize")
    }
});

// 初始化加载
loadFilter();

// 同步直播间
const handleSync = async () => {
    try {
        await syncWechatLiveRoom();
        message.success(t("同步成功"));
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t("同步失败"));
    }
};

// 获取直播间推流信息，开播码与推流地址共用同一个接口
const loadPushInfo = async (roomId: number, roomName: string) => {
    currentRoomName.value = roomName || String(roomId || "-");
    currentPushInfo.value = null;
    try {
        pushInfoLoading.value = true;
        const result = await getWechatLivePushUrl(roomId);
        currentPushInfo.value = result;
        return result;
    } catch (error: any) {
        currentPushInfo.value = null;
        return null;
    } finally {
        pushInfoLoading.value = false;
    }
};

const openPushInfoDialog = async (dialogVisible: typeof pushTheFlowDialog, row: WechatLiveRoomState) => {
    const roomId = Number(row.roomId || 0);
    if (!roomId) {
        message.warning(t("直播间房间号不存在"));
        return;
    }
    dialogVisible.value = true;
    await loadPushInfo(roomId, row.wechatLiveTitle || "");
};

const handleViewPushInfo = async (row: WechatLiveRoomState) => {
    await openPushInfoDialog(pushTheFlowDialog, row);
};

const handleViewBroadcastCode = async (row: WechatLiveRoomState) => {
    await openPushInfoDialog(startTheBroadcastCodeDialog, row);
};

// 删除
const handleDelete = async (row: WechatLiveRoomState) => {
    try {
        await ElMessageBox.confirm(t("确认删除直播间【{wechatLiveTitle}】吗？", { wechatLiveTitle: row.wechatLiveTitle || row.roomId }), t("提示"), {
            confirmButtonText: t("确认"),
            cancelButtonText: t("取消"),
            type: "warning"
        });
        await delWechatLiveRoom({ id: Number(row.wechatLiveId || row.roomId) });
        message.success(t("删除成功"));
        loadFilter();
    } catch (error: any) {
        if (error === "cancel" || error === "close") return;
        message.error(error.message || t("删除失败"));
    }
};
</script>

<style lang="less" scoped>
.live-name {
    color: inherit;
    font-size: inherit;
    font-weight: 400;
    line-height: 22px;
}

.live-room-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.live-room-info__text {
    min-width: 0;
}

.more-action {
    color: var(--tig-primary);
    cursor: pointer;
    display: inline-flex;
    align-items: center;
}
:deep(.el-divider) {
    margin: 0;
}
</style>

<style lang="less">
.dailogheader {
    padding: 0 0 16px 0 !important;
    .el-dialog__title {
        font-weight: 600;
    }
}

.page-alert {
    margin-bottom: 16px;
    color: rgb(16, 16, 16);
    font-size: 14px;
    line-height: 20px;
    min-height: 34px;
    background-color: rgb(255, 239, 230);
    padding: 4px 20px;
    display: flex;
    align-items: center;
    gap: 8px;
}
</style>
