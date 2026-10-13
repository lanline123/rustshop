<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>视频号</h3>
            <div class="dec-edit-group-con">
                <div class="video-number-tip">
                    视频号组件为视频号配套组件：通过该组件可以实现自定义进入视频号视频或直播，帮助商户提升视频号相关内容的曝光率，提升视频号热度。
                </div>
            </div>
        </div>

        <div class="setting-tabs">
            <div class="setting-tab" :class="{ active: activeTab === 'content' }" @click="activeTab = 'content'">内容设置</div>
            <div class="setting-tab" :class="{ active: activeTab === 'style' }" @click="activeTab = 'style'">样式设置</div>
        </div>

        <template v-if="activeTab === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">内容类型</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group video-type-group" v-model="module.contentType">
                        <el-radio-button value="video">视频</el-radio-button>
                        <el-radio-button value="live">直播</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group--block">
                <div class="dec-edit-group-title">
                    <div class="label">视频号ID</div>
                </div>
                <div class="dec-edit-group-con" style="flex-direction: column; align-items: flex-start">
                    <el-select v-model="module.videoNumberId" placeholder="请选择需要跳转的视频号ID" style="width: 100%" @change="handleVideoNumberChange">
                        <el-option v-for="item in videoNumberOptions" :key="item.videoNumberId" :label="`${item.videoNumberId}`" :value="item.videoNumberId" />
                    </el-select>
                    <div class="field-tip">
                        跳转的视频号需要与小程序为同主体或者关联主体
                        <a @click="openVideoNumberPage">去添加</a>
                    </div>
                </div>
            </div>

            <template v-if="module.contentType === 'video'">
                <div class="dec-edit-group dec-edit-group--block">
                    <div class="dec-edit-group-title">
                        <div class="label">视频ID</div>
                    </div>
                    <div class="dec-edit-group-con" style="flex-direction: column; align-items: flex-start">
                        <TigInput v-model="module.videoId" width="100%" placeholder="请输入需要跳转的视频ID" />
                        <div class="field-tip">
                            视频ID可在【视频号助手】获取
                            <a @click="openVideoHelper">去获取</a>
                        </div>
                    </div>
                </div>
            </template>

            <template v-else>
                <div class="dec-edit-group dec-edit-group--block">
                    <div class="dec-edit-group-title">
                        <div class="label">标题</div>
                        <div class="value">{{ `${module.liveTitle?.length || 0}/14` }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <TigInput v-model="module.liveTitle" width="100%" :maxlength="14" showWordLimit placeholder="请输入直播标题" />
                    </div>
                </div>

                <div class="dec-edit-group dec-edit-group--block">
                    <div class="dec-edit-group-con" style="justify-content: space-between">
                        <div class="dec-edit-group-title">
                            <div class="label">标题颜色</div>
                        </div>
                        <el-radio-group class="dec-radio-group" v-model="module.titleColorType">
                            <el-radio-button value="default">默认</el-radio-button>
                            <el-radio-button value="theme">主题色</el-radio-button>
                            <el-radio-button value="custom">自定义</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-group dec-edit-group--block" v-if="module.titleColorType === 'custom'">
                    <div class="dec-edit-group-con" style="justify-content: space-between">
                        <div class="dec-edit-group-title">
                            <div class="label">显示颜色</div>
                        </div>
                        <div class="dec-color-group" need-refresh="true">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.titleColor = '#2A2A2A'">重置</a>
                                <SelectColor v-model:color="module.titleColor" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group dec-edit-group--block">
                    <div class="dec-edit-group-title">
                        <div class="label">商品图片</div>
                    </div>
                    <div class="dec-edit-group-con" style="flex-direction: column; align-items: flex-start">
                        <div class="product-image-list" v-if="module.productImages?.length">
                            <draggable :list="module.productImages" item-key="picKey" animation="200" handle=".product-image-item__drag">
                                <template #item="{ element, index }">
                                    <div class="product-image-item">
                                        <div class="product-image-item__drag">⋮⋮</div>
                                        <img class="product-image-item__thumb" :src="productImage(element)" alt="商品图片" />
                                        <div class="product-image-item__content">
                                            <div class="product-image-item__name">{{ element.picName || `图片${index + 1}` }}</div>
                                            <div class="product-image-item__url">{{ element.picUrl || "-" }}</div>
                                        </div>
                                        <a class="product-image-item__delete" @click="removeProductImage(index)">删除</a>
                                    </div>
                                </template>
                            </draggable>
                        </div>
                        <DialogForm type="gallery" @okCallback="handleAddProductImages" :params="{ isMultiple: true }" :style="{ width: '100%' }">
                            <div class="product-image-add-btn">
                                <div class="product-image-add-btn__icon">+</div>
                                <div>添加图片</div>
                            </div>
                        </DialogForm>
                    </div>
                    <div class="field-tip">最多可添加三个图片，支持手动拖拽排序</div>
                </div>
            </template>
        </template>

        <template v-else>
            <CommonFrameEdit v-model="module.frame" type="live" />
        </template>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import draggable from "vuedraggable";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { SelectColor } from "@/components/select";
import { getWechatVideoAccountList } from "@/api/content/wechatVideoAccount";
import type { WechatVideoAccountState } from "@/types/content/wechatVideoAccount.d";
import { productImage } from "@/utils/format";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { defaultFrame, mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { VideoNumberModule } from "./types";

interface VideoNumberOption {
    videoNumberId: string;
    liveTitle: string;
}

const router = useRouter();
const activeTab = ref<"content" | "style">("content");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as VideoNumberModule);
const videoNumberOptions = ref<VideoNumberOption[]>([]);
const DEFAULT_LIVE_TITLE = "这场直播特别有料";

// 旧 Edit.vue 自带的烧录字面量与 View 侧不同（liveTitle/frame 子集），原样保留（契约）
mergeDefaultModule(module.value, {
    titleText: "视频号",
    contentType: "video",
    videoNumberId: "",
    videoId: "",
    liveTitle: DEFAULT_LIVE_TITLE,
    titleColorType: "default",
    titleColor: "#2A2A2A",
    productImages: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        innerPadding: 12,
        boxRadius: 8
    }
});

const currentVideoNumber = computed<VideoNumberOption | undefined>(() => {
    return videoNumberOptions.value.find((item) => item.videoNumberId === module.value.videoNumberId);
});

const loadVideoNumberOptions = async () => {
    try {
        const result = await getWechatVideoAccountList({
            page: 1,
            size: 100,
            keyword: "",
            finderId: "",
            accountStatus: undefined,
            relationTimeStart: "",
            relationTimeEnd: "",
            sortField: "",
            sortOrder: ""
        });
        videoNumberOptions.value = result.records.map((item: WechatVideoAccountState) => ({
            videoNumberId: item.finderId,
            liveTitle: DEFAULT_LIVE_TITLE
        }));

        if (!module.value.videoNumberId && videoNumberOptions.value.length > 0) {
            module.value.videoNumberId = videoNumberOptions.value[0].videoNumberId;
        }
        if (!module.value.liveTitle) {
            module.value.liveTitle = currentVideoNumber.value?.liveTitle || DEFAULT_LIVE_TITLE;
        }
    } catch (error: any) {
        message.error(error.message || "获取视频号列表失败");
    }
};

const normalizeProductImages = (list: any[] = []) => {
    return list.slice(0, 3).map((item, index) => ({
        ...item,
        picKey: String(item.picId || item.picUrl || `${Date.now()}-${index}`)
    }));
};

const openVideoNumberPage = () => {
    const route = router.resolve({ path: "/live/videoNumber/list" });
    window.open(route.href, "_blank");
};

const openVideoHelper = () => {
    window.open("https://channels.weixin.qq.com/platform", "_blank");
};

const handleVideoNumberChange = (videoNumberId: string) => {
    const selectedItem = videoNumberOptions.value.find((item) => item.videoNumberId === videoNumberId);
    if (!selectedItem) return;
    if (!module.value.liveTitle) {
        module.value.liveTitle = selectedItem.liveTitle;
    }
};

const handleAddProductImages = (result: any[]) => {
    const currentList = module.value.productImages || [];
    const remainCount = 3 - currentList.length;
    if (remainCount <= 0) {
        message.error("最多只能添加3张图片");
        return;
    }
    if (result.length > remainCount) {
        message.error("最多只能添加3张图片");
    }
    const nextList = [
        ...currentList,
        ...result.slice(0, remainCount).map((item, index) => ({
            ...item,
            picKey: String(item.picId || item.picUrl || `${Date.now()}-${index}`)
        }))
    ];
    module.value.productImages = normalizeProductImages(nextList);
};

const removeProductImage = (index: number) => {
    module.value.productImages = (module.value.productImages || []).filter((_, itemIndex) => itemIndex !== index);
};

watch(
    () => module.value.productImages,
    (value) => {
        const currentList = value || [];
        const normalizedList = normalizeProductImages(currentList);
        const hasChanged = normalizedList.length !== currentList.length || normalizedList.some((item, index) => item.picKey !== currentList[index]?.picKey);
        if (hasChanged) {
            module.value.productImages = normalizedList;
        }
    },
    { deep: true, immediate: true }
);

watch(
    () => module.value.contentType,
    (value) => {
        if (value === "live" && !module.value.liveTitle) {
            module.value.liveTitle = currentVideoNumber.value?.liveTitle || DEFAULT_LIVE_TITLE;
        }
    },
    { immediate: true }
);

watch(
    () => module.value.liveTitle,
    (value) => {
        if ((value || "").length > 14) {
            module.value.liveTitle = (value || "").slice(0, 14);
        }
    }
);

onMounted(() => {
    loadVideoNumberOptions();
});
</script>

<style lang="less" scoped>
.video-number-tip {
    color: #999;
    font-size: 12px;
    line-height: 20px;
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

.video-type-group {
    width: 100%;

    :deep(.el-radio-button) {
        flex: 1;
    }

    :deep(.el-radio-button__inner) {
        width: 100%;
    }
}

.dec-edit-group--block {
    flex-direction: column;
    align-items: stretch;

    .dec-edit-group-title {
        margin-bottom: 8px;
    }

    .dec-edit-group-con {
        width: 100%;
    }
}

.field-tip {
    margin-top: 8px;
    color: #999;
    font-size: 12px;
    line-height: 18px;
    text-align: left;

    a {
        margin-left: 4px;
        color: #155bd4;
        cursor: pointer;
    }
}

.product-image-list {
    margin-bottom: 12px;
    width: 100%;
}

.product-image-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    margin-bottom: 10px;
    background: #f7f8fa;
    border-radius: 4px;
}

.product-image-item__drag {
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

.product-image-item__thumb {
    width: 48px;
    height: 48px;
    border-radius: 4px;
    object-fit: cover;
    background: #e5e7eb;
}

.product-image-item__content {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.product-image-item__name {
    color: #333;
    font-size: 13px;
    line-height: 20px;
    overflow: hidden;
    text-overflow: ellipsis;
    line-clamp: 2;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.product-image-item__url {
    margin-top: 2px;
    color: #999;
    font-size: 12px;
    line-height: 18px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-image-item__delete {
    color: #f56c6c;
    cursor: pointer;
    white-space: nowrap;
}

.product-image-add-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    border: 1px solid #4f67ff;
    color: #4f67ff;
    font-size: 16px;
    cursor: pointer;
    gap: 6px;
}

.product-image-add-btn__icon {
    font-size: 22px;
    line-height: 1;
}
</style>
