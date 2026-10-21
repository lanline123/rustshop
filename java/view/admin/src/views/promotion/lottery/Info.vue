<template>
    <div class="lucky-wheel-edit" :class="{ 'is-drawer': isDialog }">
        <div class="main-content">
            <div class="preview-container">
                <div class="preview-panel">
                    <div class="preview-wrapper">
                        <MobilePreview
                            :decorateConfig="formState.decorateConfig"
                            :gridConfig="formState.gridConfig"
                            :prizeList="formState.prizeList"
                            :noWinName="formState.noWinName"
                            :noWinImage="formState.noWinImage"
                            v-model="formState"
                        />
                    </div>
                </div>
            </div>

            <div class="config-container">
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
                <div class="config-panel">
                    <div v-show="activeAnchor === 'part-basic'" id="part-basic" class="section-item">
                        <Step1Basic ref="step1Ref" v-model="formState" />
                    </div>

                    <div v-show="activeAnchor === 'part-awards'" id="part-awards" class="section-item">
                        <Step2Awards ref="step2Ref" v-model="formState" />
                    </div>

                    <div v-show="activeAnchor === 'part-decorate'" id="part-decorate" class="section-item">
                        <Step3Decorate ref="step3Ref" v-model="formState" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 自定义底部按钮 -->
        <div v-if="isDialog" class="custom-footer">
            <el-button v-if="activeAnchor === 'part-basic'" @click="handleCancel">{{ $t('取消') }}</el-button>
            <el-button v-if="activeAnchor !== 'part-basic'" @click="handlePrevStep">{{ $t('上一步') }}</el-button>
            <el-button v-if="activeAnchor !== 'part-decorate'" type="primary" @click="handleNextStep">{{ $t('下一步') }}</el-button>
            <el-button v-if="activeAnchor === 'part-decorate'" type="primary" :loading="confirmLoading" @click="handleSubmit">{{ $t('确认') }}</el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { ref, onMounted, watch } from "vue";
import { message } from "ant-design-vue";
import Step1Basic from "./components/Step1Basic.vue";
import Step2Awards from "./components/Step2Awards.vue";
import Step3Decorate from "./components/Step3Decorate.vue";
import MobilePreview from "./components/MobilePreview.vue";
import { getLotteryActivityDetail, createLotteryActivity, updateLotteryActivity } from "@/api/promotion/lottery";
import { LotteryFormState, LotteryPrizeItemParam, GridPositionParam, PrizeItem } from "@/types/promotion/luckyWheel.d";

// Props 接收参数
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    act: {
        type: String,
        default: "add"
    },
    id: {
        type: [Number, String],
        default: 0
    },
    isDialog: {
        type: Boolean,
        default: false
    },
    visible: {
        type: Boolean,
        default: false
    }
});

// Emits 事件
const emit = defineEmits(["submitCallback", "callback", "close"]);

// 加载状态
const confirmLoading = defineModel("confirmLoading", {
    type: Boolean,
    default: false
});

const step1Ref = ref();
const step2Ref = ref();
const step3Ref = ref();
const activeAnchor = ref("part-basic");

const anchorTabs = [
    { id: "part-basic", title: t('创建活动'), stepIndex: 0 },
    { id: "part-awards", title: t('奖项设置'), stepIndex: 1 },
    { id: "part-decorate", title: t('页面装修'), stepIndex: 2 }
];

// 初始表单状态
const defaultPrizeEntryImage = "https://oss.tigshop.com/img/gallery/202601/1769405706W6G6mAb5dVkUxQIN7g.jpeg";

const getInitialFormState = (): LotteryFormState => ({
    activityName: "",
    timeRange: [],
    startTime: "",
    endTime: "",
    // 默认显示空：在 Step1Basic 选中对应限制项后再兜底为 1
    costPoints: "" as any,
    dailyLimit: "" as any,
    totalLimit: "" as any,
    winLimit: "" as any,
    winLimitType: 0,
    drawLimitType: 0,
    isConsumePoints: 1,
    prizeList: [],
    gridConfig: Array.from({ length: 8 }, (_, i) => ({
        position: i + 1,
        tempIndex: null,
        probability: 12.5
    })),
    noWinName: t('谢谢参与'),
    noWinImage: "https://oss.tigshop.com/img/gallery/202512/1766467150H93CwsgU07PQfSqwj4.jpeg",
    noWinTips: t('很遗憾没有抽中'),
    decorateConfig: {
        bgImage: "https://oss.tigshop.com/img/gallery/202512/1766469537jXrMsUPDXoszzk0KCC.jpeg",
        prizeEntryImage: defaultPrizeEntryImage,
        bgColor: "",
        showWinnerList: 1,
        activityRules: ""
    }
});

const formState = ref<LotteryFormState>(getInitialFormState());

const validateCurrentStep = async () => {
    let isValid = false;
    if (activeAnchor.value === "part-basic") {
        isValid = await step1Ref.value.validate();
    } else if (activeAnchor.value === "part-awards") {
        isValid = await step2Ref.value.validate();
    } else if (activeAnchor.value === "part-decorate") {
        isValid = await step3Ref.value.validate();
    }
    return isValid;
};

const handleTabClick = async (targetId: string) => {
    const currentIndex = anchorTabs.findIndex((t) => t.id === activeAnchor.value);
    const targetIndex = anchorTabs.findIndex((t) => t.id === targetId);

    if (targetIndex === currentIndex) return;

    const isValid = await validateCurrentStep();
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
    const isValid = await validateCurrentStep();
    if (!isValid) return;

    const currentIndex = anchorTabs.findIndex((t) => t.id === activeAnchor.value);
    if (currentIndex < anchorTabs.length - 1) {
        activeAnchor.value = anchorTabs[currentIndex + 1].id;
    }
};

// 确认提交
const handleSubmit = async () => {
    await onFormSubmit();
};

const loadData = async () => {
    const activityId = Number(props.id);
    if (activityId && props.act !== "add") {
        try {
            const res: any = await getLotteryActivityDetail(activityId);
            console.log(res, "res");

            if (res) {
                const data = res;
                // 后端返回 prizeId：
                // - tempIndex：前端用于九宫格关联
                // - prizeId：编辑时需要原样回传给后端用于更新
                const prizeList: PrizeItem[] = (data.prizeList || []).map((prize: any) => ({
                    tempIndex: prize.prizeId, // 把后端的 prizeId 赋值给 tempIndex
                    prizeId: prize.prizeId,
                    prizeName: prize.prizeName,
                    prizeType: prize.prizeType,
                    prizeValue: prize.prizeValue,
                    prizeImage: prize.prizeImage,
                    prizeStock: prize.prizeStock,
                    prizeStockUsed: prize.prizeStockUsed || 0
                }));

                // gridConfig 中的 prizeId 也转换为 tempIndex
                const gridConfig = (data.gridConfig || []).map((grid: any) => ({
                    position: grid.position,
                    tempIndex: grid.prizeId ?? null, // 把后端的 prizeId 赋值给 tempIndex
                    probability: grid.probability,
                    prizeName: grid.prizeName,
                    prizeImage: grid.prizeImage,
                    prizeType: grid.prizeType,
                    prizeValue: grid.prizeValue
                }));

                formState.value = {
                    activityId: data.activityId,
                    activityName: data.activityName,
                    startTime: data.startTime,
                    endTime: data.endTime,
                    timeRange: [data.startTime, data.endTime],
                    costPoints: data.costPoints || 0,
                    dailyLimit: data.dailyLimit || 0,
                    totalLimit: data.totalLimit || 0,
                    winLimit: data.winLimit || 0,
                    isConsumePoints: data.costType !== undefined && data.costType !== null ? (data.costType === 1 ? 1 : 0) : data.costPoints > 0 ? 1 : 0,
                    drawLimitType: data.dailyLimit > 0 || data.totalLimit > 0 ? 1 : 0,
                    winLimitType: data.winLimit > 0 ? 1 : 0,
                    prizeList,
                    gridConfig: gridConfig.length > 0 ? gridConfig : formState.value.gridConfig,
                    noWinName: data.noWinName || t('谢谢参与'),
                    noWinImage: data.noWinImage || "",
                    noWinTips: data.noWinTips || t('很遗憾，再接再厉！'),
                    decorateConfig: {
                        bgImage: data.decorateConfig?.bgImage || "",
                        prizeEntryImage: data.decorateConfig?.prizeEntryImage || "",
                        bgColor: data.decorateConfig?.bgColor || "",
                        showWinnerList: data.decorateConfig?.showWinnerList ?? 1,
                        activityRules: data.decorateConfig?.activityRules || ""
                    }
                };
                console.log(formState.value, "formState.value");
            }
        } catch (error) {
            console.error(error);
        }
    }
};

// 监听visible变化，打开时加载数据
watch(
    () => props.visible,
    (val) => {
        if (val) {
            activeAnchor.value = "part-basic";
            if (props.act === "add") {
                formState.value = getInitialFormState();
            } else {
                loadData();
            }
        }
    },
    { immediate: true }
);

onMounted(() => {
    if (!props.isDialog) {
        loadData();
    }
});

// 构建提交用的 prizeList（只传 tempIndex，不传 prizeId）
const buildPrizeList = (prizeList: PrizeItem[]): LotteryPrizeItemParam[] => {
    return prizeList.map((prize) => {
        // tempIndex 是前端关联九宫格用的必填字段；缺失会导致提交数据异常
        if (prize.tempIndex === undefined || prize.tempIndex === null) {
            throw new Error(t('奖品配置异常：缺少临时索引(tempIndex)'));
        }
        return {
            tempIndex: prize.tempIndex,
            // 编辑活动：保留 prizeId 回传给后端；新增奖品此字段为空
            prizeId: prize.prizeId,
            prizeName: prize.prizeName,
            prizeType: prize.prizeType,
            prizeValue: prize.prizeValue,
            prizeImage: prize.prizeImage,
            prizeStock: prize.prizeStock
        };
    });
};

// 构建提交用的 gridConfig（只传 tempIndex）
const buildGridConfig = (gridConfig: any[]): GridPositionParam[] => {
    return gridConfig.map((grid) => ({
        position: grid.position,
        tempIndex: grid.tempIndex ?? null,
        // 概率保留4位小数
        probability: grid.probability !== undefined && grid.probability !== null ? Number(Number(grid.probability).toFixed(4)) : grid.probability
    }));
};

// 表单提交方法
const onFormSubmit = async () => {
    // 验证所有步骤
    const valid1 = await step1Ref.value.validate();
    if (!valid1) {
        activeAnchor.value = "part-basic";
        return;
    }

    const valid2 = await step2Ref.value.validate();
    if (!valid2) {
        activeAnchor.value = "part-awards";
        return;
    }

    const valid3 = await step3Ref.value.validate();
    if (!valid3) {
        activeAnchor.value = "part-decorate";
        return;
    }

    confirmLoading.value = true;

    try {
        const form = formState.value;
        const submitData: any = {
            activityName: form.activityName,
            startTime: form.startTime,
            endTime: form.endTime,
            costType: form.isConsumePoints === 1 ? 1 : 0,
            costPoints: form.isConsumePoints === 1 ? form.costPoints : 0,
            dailyLimit: form.drawLimitType === 1 ? form.dailyLimit : 0,
            totalLimit: form.drawLimitType === 1 ? form.totalLimit : 0,
            winLimit: form.winLimitType === 1 ? form.winLimit : 0,
            decorateConfig: form.decorateConfig,
            prizeList: buildPrizeList(form.prizeList),
            gridConfig: buildGridConfig(form.gridConfig),
            noWinName: form.noWinName,
            noWinImage: form.noWinImage,
            noWinTips: form.noWinTips
        };

        if (props.act === "edit" && props.id) {
            submitData.activityId = Number(props.id);
            await updateLotteryActivity(submitData);
        } else {
            await createLotteryActivity(submitData);
        }

        message.success(t('保存成功'));
        emit("submitCallback", submitData);
    } catch (error: any) {
        message.error(error.message);
        console.error(error);
    } finally {
        confirmLoading.value = false;
    }
};

// 暴露方法给父组件
defineExpose({
    onFormSubmit
});
</script>

<style scoped lang="less">
.lucky-wheel-edit {
    padding: 20px;
    background: #fff;
    height: 100%;
    min-height: calc(100vh - 210px);
    box-sizing: border-box;
    // 只让右侧编辑区域滚动，左侧预览固定
    overflow: hidden;

    &.is-drawer {
        min-height: auto;
        padding: 20px;
        overflow: hidden;

        .main-content {
            max-width: 100%;
        }

        .preview-container {
            position: relative;
            top: 0;
        }
    }

    .main-content {
        display: flex;
        gap: 20px;
        align-items: flex-start;
        max-width: 1600px;
        margin: 0 auto;
        height: 100%;
    }

    .preview-container {
        width: 375px;
        flex-shrink: 0;
        // 外层不滚动时无需 sticky；保持固定不动即可
        position: relative;
        top: 0;

        .preview-panel {
            background: #fff;
            border-radius: 4px;
            padding: 20px;

            .preview-title {
                font-size: 16px;
                font-weight: bold;
                margin-bottom: 20px;
                text-align: center;
            }

            .preview-wrapper {
                display: flex;
                justify-content: center;
            }
        }
    }

    .config-container {
        flex: 1;
        min-width: 0;
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow: hidden;

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
            min-height: 500px;
            flex: 1 1 auto;
            overflow-y: auto;
            overscroll-behavior: contain;

            .section-item {
                padding: 10px 0;

                .section-header {
                    font-size: 18px;
                    font-weight: bold;
                    margin-bottom: 25px;
                    padding-left: 10px;
                    border-left: 4px solid var(--el-color-primary);
                }
            }
        }
    }
}

:deep(.formTitle) {
    font-size: 18px;
    font-weight: 500;
    margin: 0 0 20px;
    &::after {
        content: "";
        display: block;
        height: 1px;
        width: 100%;
        background: #efefef;
        margin-top: 10px;
    }
}

.custom-footer {
    position: sticky;
    bottom: -25px;
    left: 0;
    right: 0;
    padding: 16px 24px;
    background: #fff;
    border-top: 1px solid #e8e8e8;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    z-index: 10;
}
</style>
