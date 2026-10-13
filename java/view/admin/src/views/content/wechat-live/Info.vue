<!--
 * @file 直播间编辑/添加页面
 * @description 直播间信息编辑页面，支持以下功能：
 *   - 基本信息：直播间名称、房间号、主播信息等
 *   - 直播间设置：封面、直播配置等
 * @module views/content/wechat-live/info
-->
<template>
    <div class="live-room-container">
        <div class="anchor-container">
            <div class="anchor">
                <div class="tab-row">
                    <div
                        v-for="(item, index) in anchorTabs"
                        :key="item.id"
                        class="anchor-item"
                        :class="{ active: activeAnchor === item.id }"
                        @click="handleTabClick(item.id)"
                    >
                        <div :class="{ active: activeAnchor === item.id }" class="item">
                            {{ item.title }}
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-alert">
            <el-icon size="18" color="#ed6a18"><WarnTriangleFilled /></el-icon>
            <span> {{ $t('使用小程序直播组件必须在小程序中引入相关代码，具体配置流程请参考') }}<a
                    href="https://developers.weixin.qq.com/miniprogram/dev/platform-capabilities/industry/liveplayer/live-player-plugin.html"
                    target="_blank"
                    rel="noopener noreferrer"
                    >{{ $t('《开发文档》') }}</a
                >
            </span>
        </div>

        <div class="config-panel">
            <div v-show="activeAnchor === 'part-basic'" id="part-basic" class="section-item">
                <BasicInfo ref="basicInfoRef" :id="id" :act="act" :loading="loading" v-model:formState="formState" />
            </div>

            <div v-show="activeAnchor === 'part-settings'" id="part-settings" class="section-item">
                <LiveRoomSettings ref="liveRoomSettingsRef" :act="act" :loading="loading" v-model:formState="formState" />
            </div>
        </div>

        <!-- 自定义底部按钮 -->
        <div class="custom-footer">
            <el-button @click="handleCancel">{{ $t('取消') }}</el-button>
            <el-button v-if="activeAnchor !== 'part-basic'" @click="handlePrevStep">{{ $t('上一步') }}</el-button>
            <el-button v-if="activeAnchor !== 'part-settings'" type="primary" @click="handleNextStep">{{ $t('下一步') }}</el-button>
            <el-button v-if="activeAnchor === 'part-settings'" type="primary" :loading="confirmLoading" @click="handleSubmit">{{ $t('确认') }}</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { FormInstance } from "ant-design-vue";
import { BasicInfo, LiveRoomSettings } from "./src/components";
import { getWechatLiveRoomDetail, updateWechatLiveRoom } from "@/api/content/wechatLive";
import { WarnTriangleFilled } from "@element-plus/icons-vue";

/**
 * 组件事件定义
 * @event submitCallback - 提交成功回调
 * @event update:confirmLoading - 确认按钮加载状态更新
 * @event close - 关闭页面/弹窗
 * @event closeConfirm - 关闭确认状态
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "closeConfirm"]);

/**
 * 直播间表单数据类型
 */
interface LiveRoomFormState {
    liveType?: number; // 直播类型：0-手机直播，1-推流设备直播
    name?: string; // 直播间名称
    roomId?: number; // 房间ID
    anchorName?: string; // 主播昵称
    anchorWechat?: string; // 主播微信账号
    subAnchorWechat?: string; // 副主播微信账号
    coverImg?: string; // 封面图
    shareCardCover?: string; // 分享卡片封面
    liveCardCover?: string; // 直播卡片封面
    backgroundWall?: string; // 直播间背景墙
    liveStatus?: number; // 101直播中 102未开始 103已结束 104禁播 105暂停 106异常 107已过期
    isFeedsPublic?: number; // 官方收录：0-关闭，1-开启
    startTime?: string; // 开播时间
    endTime?: string; // 结束时间
}

/**
 * 组件属性定义
 * @property {number} id - 直播间ID，编辑时必传
 * @property {string} act - 操作类型：add/detail/copy/again
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    }
});

// 加载状态
const confirmLoading = defineModel("confirmLoading", {
    type: Boolean,
    default: false
});

/** Tab导航配置列表 */
const anchorTabs = [
    { id: "part-basic", title: t('基本信息') },
    { id: "part-settings", title: t('直播间设置') }
];

/** 当前激活的Tab */
const activeAnchor = ref("part-basic");

/** 各区块组件引用 */
const basicInfoRef = ref();
const liveRoomSettingsRef = ref();

/**
 * Tab点击处理
 * @param {string} targetId - 目标Tab ID
 */
const handleTabClick = async (targetId: string) => {
    if (targetId === activeAnchor.value) return;

    // 验证当前区块
    let isValid = true;
    if (activeAnchor.value === "part-basic") {
        isValid = await basicInfoRef.value?.validate?.();
    } else if (activeAnchor.value === "part-settings") {
        isValid = await liveRoomSettingsRef.value?.validate?.();
    }

    if (!isValid) return;

    activeAnchor.value = targetId;
};

// 取消按钮
const handleCancel = () => {
    emit("close");
};

// 上一步
const handlePrevStep = () => {
    const currentIndex = anchorTabs.findIndex((t) => t.id === activeAnchor.value);
    if (currentIndex > 0) {
        activeAnchor.value = anchorTabs[currentIndex - 1].id;
    }
};

// 下一步
const handleNextStep = async () => {
    // 验证当前区块
    let isValid = true;
    if (activeAnchor.value === "part-basic") {
        isValid = await basicInfoRef.value?.validate?.();
    } else if (activeAnchor.value === "part-settings") {
        isValid = await liveRoomSettingsRef.value?.validate?.();
    }

    if (!isValid) return;

    const currentIndex = anchorTabs.findIndex((t) => t.id === activeAnchor.value);
    if (currentIndex < anchorTabs.length - 1) {
        activeAnchor.value = anchorTabs[currentIndex + 1].id;
    }
};

// 提交表单
const handleSubmit = async () => {
    try {
        confirmLoading.value = true;

        // 提交前验证全部区块
        const basicValid = await basicInfoRef.value?.validate?.();
        const settingsValid = await liveRoomSettingsRef.value?.validate?.();
        if (!basicValid || !settingsValid) return;

        const result = await updateWechatLiveRoom(operation.value, buildSubmitData());
        message.success(operation.value === "create" ? t('创建成功') : t('保存成功'));
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message || t('提交失败'));
    } finally {
        confirmLoading.value = false;
    }
};

/** 加载状态 */
const loading = ref<boolean>(true);

/** 路由查询参数 */
const query = useRouter().currentRoute.value.query;

/** 直播间ID */
const id = ref<number>(Number(query.id) || props.id);

/** 提交操作类型 */
const operation = computed<"create" | "update">(() => (props.act === "add" ? "create" : "update"));
console.log(operation.value);

/** 表单引用 */
const formRef = shallowRef<FormInstance>();

/**
 * 直播间表单状态
 * @type {LiveRoomFormState}
 */
const formState = ref<LiveRoomFormState>({
    liveType: 0,
    name: "",
    roomId: undefined,
    anchorName: "",
    anchorWechat: "",
    subAnchorWechat: "",
    coverImg: "",
    shareCardCover: "",
    liveCardCover: "",
    backgroundWall: "",
    liveStatus: 102,
    isFeedsPublic: 1,
    startTime: "",
    endTime: ""
});

// 组装提交参数，兼容接口文档中的创建和更新字段
const buildSubmitData = () => {
    const payload: Record<string, any> = {
        name: formState.value.name,
        wechatLiveTitle: formState.value.name,
        liveType: Number(formState.value.liveType ?? 0),
        roomId: formState.value.roomId,
        coverImg: formState.value.backgroundWall || formState.value.coverImg || "",
        backgroundWall: formState.value.backgroundWall || formState.value.coverImg || "",
        shareImg: formState.value.shareCardCover || "",
        shareCardCover: formState.value.shareCardCover || "",
        feedsImg: formState.value.liveCardCover || "",
        liveCardCover: formState.value.liveCardCover || "",
        isFeedsPublic: Number(formState.value.isFeedsPublic ?? 0),
        anchorName: formState.value.anchorName || "",
        anchorWechat: formState.value.anchorWechat || "",
        anchorOpenid: formState.value.anchorWechat || "",
        subAnchorWechat: formState.value.subAnchorWechat || "",
        startTime: formState.value.startTime || "",
        endTime: formState.value.endTime || "",
        closeLike: 0,
        closeGoods: 0,
        closeComment: 0,
        closeKf: 0,
        closeReplay: 1
    };

    if (operation.value === "update") {
        payload.wechatLiveId = id.value;
        payload.id = id.value;
    }

    return payload;
};

/**
 * 获取直播间详情
 */
const loadData = async () => {
    if (!id.value) {
        loading.value = false;
        return;
    }
    try {
        loading.value = true;
        const data: any = await getWechatLiveRoomDetail({ id: id.value });
        console.log(data);
        Object.assign(formState.value, {
            liveType: Number(data.liveType ?? 0),
            name: data.name || data.wechatLiveTitle || "",
            roomId: data.roomId,
            anchorName: data.anchorName || "",
            anchorWechat: data.anchorWechat || data.anchorOpenid || "",
            subAnchorWechat: data.subAnchorWechat || "",
            coverImg: data.coverImg || "",
            shareCardCover: data.shareCardCover || data.shareImg || "",
            liveCardCover: data.liveCardCover || data.feedsImg || "",
            backgroundWall: data.backgroundWall || data.coverImg || "",
            liveStatus: Number(data.liveStatus ?? 102),
            isFeedsPublic: Number(data.isFeedsPublic),
            startTime: data.startTime || "",
            endTime: data.endTime || ""
        });
        console.log(formState.value);
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadData();
});

// 暴露方法供父组件调用
defineExpose({
    handleSubmit,
    handleCancel,
    handlePrevStep,
    handleNextStep,
    formRef
});
</script>

<style lang="less" scoped>
.live-room-container {
    padding: 20px;
    background: #fff;
    height: 100%;
    min-height: calc(100vh - 210px);
    box-sizing: border-box;
    display: flex;
    flex-direction: column;

    .anchor-container {
        width: 100%;
        margin-bottom: 20px;
        flex: 0 0 auto;

        .anchor {
            background-color: #fff;

            .tab-row {
                display: flex;
                width: 100%;
                height: 38px;
                box-sizing: border-box;

                .anchor-item {
                    height: 100%;
                    flex: 1;
                    background-color: var(--tig-item-active-bg);
                    color: #314f73;
                    margin-right: -10px;
                    padding-left: 10px;
                    clip-path: polygon(0 0, calc(100% - 15px) 0, 100% 50%, calc(100% - 15px) 100%, 0 100%, 15px 50%);
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;

                    &:first-child {
                        padding-left: 0;
                        clip-path: polygon(0 0, calc(100% - 15px) 0, 100% 50%, calc(100% - 15px) 100%, 0 100%);
                    }

                    &:last-child {
                        margin-right: 0 !important;
                        clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%, 15px 50%);
                    }

                    &.active {
                        background-color: var(--tig-primary);
                        color: #fff;
                    }

                    .item {
                        width: 100%;
                        text-align: center;
                        font-size: 14px;
                    }
                }
            }
        }
    }

    .config-panel {
        flex: 1;
        min-height: 500px;
        overflow-y: auto;
        overscroll-behavior: contain;

        .section-item {
            padding: 10px 0;
        }
    }

    .custom-footer {
        flex: 0 0 auto;
        padding: 16px 24px 0 24px;
        background: #fff;
        border-top: 1px solid #e8e8e8;
        display: flex;
        justify-content: flex-end;
        gap: 10px;
    }
}

.page-alert {
    margin-bottom: 16px;
    color: rgb(16, 16, 16);
    font-size: 14px;
    line-height: 20px;
    height: 50px;
    background-color: rgb(255, 239, 230);
    padding: 4px 20px;
    display: flex;
    align-items: center;
    gap: 8px;
}
</style>
