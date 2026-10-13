<template>
    <div class="live-select-page">
        <div class="list-table-tool lyecs-search-warp">
            <el-form :model="filterParams" @submit.native.prevent="handleSearch">
                <div class="advanced-search-warp list-table-tool-row">
                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <div class="form-group">
                                <div class="control-container">
                                    <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入直播间名称')" clearable @clear="handleSearch">
                                        <template #append>
                                            <el-button @click="handleSearch"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <div class="control-container">
                                <el-button type="primary" plain @click="handleSearch">{{ $t('搜索') }}</el-button>
                                <el-button plain @click="handleReset">{{ $t('重置') }}</el-button>
                            </div>
                        </div>
                    </div>
                </div>
            </el-form>
        </div>

        <el-table ref="tableRef" :data="listData" :loading="loading" :row-key="getRowKey" @select="handleSelect" @select-all="handleSelectAll">
            <el-table-column type="selection" reserve-selection width="50" />
            <el-table-column :label="$t('直播间信息')" min-width="200">
                <template #default="{ row }">
                    <div class="live-info">
                        <img class="live-info__cover" :src="row.feedsImg || defaultCover" alt="直播封面" />
                        <div class="live-info__content">
                            <div class="live-info__title">{{ row.wechatLiveTitle || "-" }}</div>
                            <div class="live-info__meta">{{ row.anchorName || "-" }}</div>
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column :label="$t('房间号')" prop="roomId" width="150" />
            <el-table-column :label="$t('直播状态')" prop="liveStatusText" width="150" />
        </el-table>

        <div class="pagination-con" v-if="total > 0">
            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadData" />
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { nextTick, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { getWechatLiveRoomList } from "@/api/content/wechatLive";
import type { WechatLiveRoomFilterParams, WechatLiveRoomState } from "@/types/content/wechatLive.d";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    selectedLiveList?: WechatLiveRoomState[];
}>();

const emit = defineEmits(["submitCallback"]);
const config: any = useConfigStore();
const tableRef = ref();
const loading = ref(false);
const total = ref(0);
const listData = ref<WechatLiveRoomState[]>([]);
const selectedRows = ref<WechatLiveRoomState[]>(props.selectedLiveList ? [...props.selectedLiveList] : []);
const defaultCover = "https://via.placeholder.com/120x80?text=LIVE";

const filterParams = ref<WechatLiveRoomFilterParams>({
    keyword: "",
    liveStatus: undefined,
    startTime: "",
    endTime: "",
    sortField: "",
    sortOrder: "",
    page: 1,
    size: config.get("pageSize")
});

const getRowKey = (row: WechatLiveRoomState) => row.wechatLiveId || row.roomId;

const syncSelection = async () => {
    await nextTick();
    listData.value.forEach((row) => {
        const matched = selectedRows.value.some((item) => getRowKey(item) === getRowKey(row));
        tableRef.value?.toggleRowSelection(row, matched);
    });
};

const loadData = async () => {
    loading.value = true;
    try {
        const result = await getWechatLiveRoomList(filterParams.value);
        listData.value = result.records;
        total.value = result.total;
        await syncSelection();
    } catch (error: any) {
        message.warning(error.message || t('加载直播间列表失败'));
        listData.value = [];
        total.value = 0;
    } finally {
        loading.value = false;
    }
};

const handleSearch = () => {
    filterParams.value.page = 1;
    loadData();
};

const handleReset = () => {
    filterParams.value = {
        keyword: "",
        liveStatus: undefined,
        startTime: "",
        endTime: "",
        sortField: "",
        sortOrder: "",
        page: 1,
        size: config.get("pageSize")
    };
    loadData();
};

const updateSelectedRows = (selection: WechatLiveRoomState[], currentRow?: WechatLiveRoomState) => {
    const currentPageIds = listData.value.map((item) => getRowKey(item));
    const otherPageRows = selectedRows.value.filter((item) => !currentPageIds.includes(getRowKey(item)));
    const merged = [...otherPageRows, ...selection];

    if (merged.length > 10 && currentRow) {
        tableRef.value?.toggleRowSelection(currentRow, false);
        message.warning(t('最多只能选择10个直播间'));
        return;
    }

    selectedRows.value = merged.slice(0, 10);
};

const handleSelect = (selection: WechatLiveRoomState[], row: WechatLiveRoomState) => {
    updateSelectedRows(selection, row);
};

const handleSelectAll = (selection: WechatLiveRoomState[]) => {
    updateSelectedRows(selection);
    syncSelection();
};

const onFormSubmit = () => {
    emit("submitCallback", selectedRows.value.slice(0, 10));
};

defineExpose({ onFormSubmit });

onMounted(() => {
    loadData();
});
</script>

<style lang="less" scoped>
.live-select-page {
    padding: 20px;
}

.live-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.live-info__cover {
    width: 56px;
    height: 56px;
    border-radius: 4px;
    object-fit: cover;
    background: #f2f3f5;
}

.live-info__title {
    color: #333;
    font-weight: 600;
    line-height: 22px;
}

.live-info__meta {
    color: #999;
    font-size: 12px;
    line-height: 18px;
}
</style>
