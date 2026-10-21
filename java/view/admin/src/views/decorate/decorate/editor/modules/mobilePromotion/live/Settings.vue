<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>小程序直播</h3>
            <div class="dec-edit-group-con">
                <div class="live-tip">
                    小程序直播组件为小程序直播插件的配套组件：通过该组件可以实现自定义进入小程序直播的直播间，帮助商户提升直播相关内容的曝光率，提升直播热度。
                </div>
            </div>
        </div>

        <div class="dec-edit-group" style="flex-direction: column; margin-bottom: 30px">
            <div class="dec-edit-group-title">
                <div class="label">排列方式</div>
                <!-- <div class="value">{{ displayModeText[module.displayMode || "list"] }}</div> -->
            </div>
            <div class="dec-edit-group-con" style="margin-top: 10px">
                <el-radio-group class="dec-radio-group live-mode-group" v-model="module.displayMode">
                    <el-radio-button value="list">列表模式</el-radio-button>
                    <el-radio-button value="bigImage">大图模式</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="setting-tabs">
            <div class="setting-tab" :class="{ active: activeTab === 'content' }" @click="activeTab = 'content'">内容设置</div>
            <div class="setting-tab" :class="{ active: activeTab === 'style' }" @click="activeTab = 'style'">样式设置</div>
        </div>

        <template v-if="activeTab === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">直播内容</div>
                    <div class="value">{{ sourceTypeText[module.sourceType || "auto"] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group live-source-group" v-model="module.sourceType">
                        <el-radio-button value="auto">自动</el-radio-button>
                        <el-radio-button value="manual">手动</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <template v-if="module.sourceType === 'manual'">
                <div class="selected-live-list" v-if="module.selectedLiveList?.length">
                    <draggable :list="module.selectedLiveList" item-key="wechatLiveId" handle=".selected-live-item__drag" animation="200">
                        <template #item="{ element, index }">
                            <div class="selected-live-item">
                                <div class="selected-live-item__drag">
                                    <span></span>
                                    <span></span>
                                    <span></span>
                                </div>
                                <img v-if="element.feedsImg" class="selected-live-item__cover" :src="element.feedsImg" alt="直播封面" />
                                <div v-else class="selected-live-item__cover" />
                                <div class="selected-live-item__content">
                                    <div class="selected-live-item__title">{{ element.wechatLiveTitle || "-" }}</div>
                                    <div class="selected-live-item__meta">房间号：{{ element.roomIdStr || element.roomId || "-" }}</div>
                                </div>
                                <el-button text type="danger" @click="handleRemove(index)">删除</el-button>
                            </div>
                        </template>
                    </draggable>
                </div>

                <DialogForm
                    isDrawer
                    title="直播间列表"
                    width="960px"
                    path="decorate/decorate/src/modules/mobilePromotion/live/SelectLive"
                    :params="{ selectedLiveList: module.selectedLiveList || [] }"
                    :style="{ width: '100%' }"
                    @okCallback="handleSelectLive"
                >
                    <div class="live-add-btn">
                        <div class="iconfont-pc icon-zengjia"></div>
                        <div>添加直播</div>
                    </div>
                </DialogForm>
            </template>
            <template v-else>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">直播数量</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <TigInput v-model="module.autoLiveNum" type="integer" :min="1" :max="10" width="275px" />
                    </div>
                </div>
            </template>

            <div class="live-empty-tip" v-if="!previewLives.length && module.sourceType === 'manual'">暂未选择直播间，请点击“添加直播”。</div>

            <div class="live-bottom-tip">最多10个，可拖拽排序直播组件目前仅支持小程序，预览页仅提供样式规范直播封面图会自动读取官方收录直播卡片封面图</div>
        </template>

        <template v-else>
            <CommonFrameEdit v-model="module.frame" type="live" />
        </template>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import draggable from "vuedraggable";
import { DialogForm } from "@/components/dialog";
import type { WechatLiveRoomState } from "@/types/content/wechatLive.d";
import { getWechatLiveRoomList } from "@/api/content/wechatLive";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { defaultFrame, mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { LiveModule } from "./types";

const defaultCover = "https://via.placeholder.com/120x80?text=LIVE";
const activeTab = ref<"content" | "style">("content");
const autoLiveList = ref<WechatLiveRoomState[]>([]);
const manualSelectedLiveList = ref<WechatLiveRoomState[]>([]);
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as LiveModule);

const displayModeText: Record<string, string> = {
    list: "列表模式",
    bigImage: "大图模式"
};

const sourceTypeText: Record<string, string> = {
    auto: "自动",
    manual: "手动"
};

mergeDefaultModule(module.value, {
    titleText: "小程序直播",
    displayMode: "list",
    sourceType: "auto",
    autoLiveNum: 10,
    selectedLiveList: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        innerPadding: 12,
        boxRadius: 8
    }
});

const previewLives = computed(() => {
    const autoLiveNum = Math.min(Math.max(Number(module.value.autoLiveNum || 10), 1), 10);
    const list = module.value.sourceType === "manual" ? module.value.selectedLiveList || [] : autoLiveList.value;
    return list.slice(0, module.value.sourceType === "manual" ? 10 : autoLiveNum);
});

const syncSelectedLiveList = () => {
    if (module.value.sourceType === "manual") {
        module.value.selectedLiveList = manualSelectedLiveList.value.slice(0, 10);
        return;
    }

    const autoLiveNum = Math.min(Math.max(Number(module.value.autoLiveNum || 10), 1), 10);
    module.value.selectedLiveList = autoLiveList.value.slice(0, autoLiveNum);
};

const loadAutoLives = async () => {
    try {
        const result = await getWechatLiveRoomList({
            keyword: "",
            page: 1,
            size: 10
        });
        autoLiveList.value = result.records.filter((item: WechatLiveRoomState) => Number(item.liveStatus) !== 4).slice(0, 10);
    } catch (error) {
        console.error(error);
        autoLiveList.value = [];
    }
};

const handleSelectLive = (selectedLiveList: WechatLiveRoomState[]) => {
    module.value.selectedLiveList = (selectedLiveList || []).slice(0, 10);
    manualSelectedLiveList.value = module.value.selectedLiveList.slice();
};

const handleRemove = (index: number) => {
    module.value.selectedLiveList = (module.value.selectedLiveList || []).filter((_, itemIndex) => itemIndex !== index);
    manualSelectedLiveList.value = module.value.selectedLiveList.slice();
};

const normalizeAutoLiveNum = () => {
    const value = Number(module.value.autoLiveNum || 10);
    if (!value || value < 1) {
        module.value.autoLiveNum = 1;
        return;
    }
    module.value.autoLiveNum = Math.min(value, 10);
};

onMounted(() => {
    normalizeAutoLiveNum();
    manualSelectedLiveList.value = (module.value.selectedLiveList || []).slice(0, 10);
    loadAutoLives();
    syncSelectedLiveList();
});

watch(
    [() => module.value.sourceType, () => module.value.autoLiveNum, autoLiveList],
    () => {
        syncSelectedLiveList();
    },
    {
        deep: true
    }
);
</script>

<style lang="less" scoped>
.live-tip {
    color: #999;
    font-size: 12px;
    line-height: 20px;
}

.live-mode-group,
.live-source-group {
    width: 100%;

    :deep(.el-radio-button) {
        flex: 1;
    }

    :deep(.el-radio-button__inner) {
        width: 100%;
    }
}

.setting-tabs {
    display: flex;
    margin: 10px 16px 16px;
    border-bottom: 1px solid #ebeef5;
}

.setting-tab {
    position: relative;
    flex: 1;
    padding: 0 4px 12px;
    color: #999;
    font-size: 14px;
    cursor: pointer;
    text-align: center;

    &.active {
        color: #155bd4;
        font-weight: 600;

        &::after {
            content: "";
            position: absolute;
            left: 0;
            right: 0;
            bottom: -1px;
            height: 3px;
            background: #155bd4;
        }
    }
}

.selected-live-list,
.live-preview-list {
    margin: 0 16px 16px;
}

.selected-live-item,
.live-preview-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    margin-bottom: 10px;
    background: #f7f8fa;
    border-radius: 4px;
}

.selected-live-item__drag {
    display: flex;
    flex-direction: column;
    gap: 4px;
    cursor: move;

    span {
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: #c0c4cc;
    }
}

.selected-live-item__cover,
.live-preview-item__cover {
    width: 72px;
    height: 72px;
    border-radius: 4px;
    object-fit: cover;
    background: #e5e7eb;
}

.selected-live-item__content,
.live-preview-item__content {
    min-width: 0;
    flex: 1;
}

.selected-live-item__title,
.live-preview-item__title {
    color: #333;
    font-size: 14px;
    line-height: 22px;
    font-weight: 600;
    word-break: break-all;
}

.selected-live-item__meta,
.live-preview-item__meta {
    margin-top: 4px;
    color: #999;
    font-size: 12px;
    line-height: 18px;
}

.live-add-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 16px 16px;
    height: 40px;
    border: 1px solid #4f67ff;
    color: #4f67ff;
    font-size: 16px;
    cursor: pointer;
    gap: 5px;

    span:first-child {
        font-size: 26px;
    }
}

.live-empty-tip,
.live-bottom-tip {
    margin: 0 16px 16px;
    color: #999;
    font-size: 12px;
    line-height: 20px;
}
</style>
