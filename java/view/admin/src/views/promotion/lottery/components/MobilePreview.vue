<template>
    <div class="mobile-preview" ref="previewRootRef" :style="previewScaleStyle">
        <div class="mobile-body">
            <div class="status-bar">
                <span class="time">12:00</span>
                <div class="icons">
                    <span class="signal"></span>
                    <span class="wifi"></span>
                    <span class="battery"></span>
                </div>
            </div>
            <div class="nav-bar">
                <span class="back-icon iconfont-admin icon-zuojiantou-copy"></span>
                <span class="title">{{ $t('抽奖大转盘') }}</span>
                <span class="more-icon" aria-hidden="true">
                    <span class="dots">
                        <i class="dot"></i>
                        <i class="dot"></i>
                        <i class="dot"></i>
                    </span>
                    <i class="divider"></i>
                    <i class="ring"></i>
                </span>
            </div>
            <div class="content" :style="contentStyle">
                <div class="content-top" v-if="decorateConfig.showWinnerList"> {{ $t('刘**抽中了苹果笔记本电脑24GB') }} </div>
                <!-- <div class="content-top" v-else style="background-color: transparent;"></div> -->
                <div class="content-prize">
                    <img src="@/assets/lottery/prize.png" />
                </div>
                <div class="grid-lottery-container">
                    <div class="grid-lottery">
                        <div
                            v-for="(cell, index) in gridCells"
                            :key="index"
                            class="grid-cell"
                            :class="{
                                'is-center': cell.isCenter,
                                // 修复：点击开始动画时 activeIndex 仍为 -1，会导致中间格(position=-1)短暂高亮
                                'is-active': !cell.isCenter && activeIndex === cell.position && isRunning
                            }"
                            @click="cell.isCenter && handleDraw()"
                        >
                            <template v-if="cell.isCenter">
                                <div class="draw-btn">
                                    <div class="draw-text">{{ $t('抽奖') }}</div>
                                    <div class="draw-point" v-if="formState.isConsumePoints">{{ $t('{costPoints}积分', { costPoints: formState.costPoints }) }}</div>
                                </div>
                            </template>
                            <template v-else>
                                <img v-if="getAwardImage(cell.tempIndex)" :src="getAwardImage(cell.tempIndex)" class="award-img" />
                                <div class="award-name">{{ getAwardName(cell.tempIndex) }}</div>
                            </template>
                        </div>
                    </div>
                </div>
                <!-- 剩余次数 / 不限制文案：对齐移动端展示逻辑 -->
                <div class="remaining-times" v-if="showRemainingInfo">
                    <template v-if="isUnlimitedAll"> {{ $t('每次抽奖消耗') }} <span class="remaining-times-num">{{ formState.costPoints || 0 }}</span> {{ $t('积分') }} </template>
                    <template v-else> {{ $t('您还有') }} <span class="remaining-times-num">{{ remainingTimes }}</span> {{ $t('次抽奖机会') }} </template>
                </div>
                <div class="rule-section">
                    <div class="rule-title">{{ $t('活动规则') }}</div>
                    <div class="rule-content" v-html="decorateConfig.activityRules"></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { DecorateConfig, GridPosition, PrizeItem } from '@/types/promotion/luckyWheel.d';
import { LotteryFormState } from '@/types/promotion/luckyWheel.d';

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    decorateConfig: DecorateConfig;
    gridConfig: GridPosition[];
    prizeList: PrizeItem[];
    noWinName: string;
    noWinImage: string;
}>();

const activeIndex = ref(-1);
const isRunning = ref(false);
const formState = defineModel<LotteryFormState>({ required: true });

// ==================== 预览自适应（窗口高度较小时缩放，避免被裁切） ====================
const previewRootRef = ref<HTMLElement | null>(null);
const previewScale = ref(1);
const DESIGN_HEIGHT = 740; // 对应 .mobile-preview 的设计高度

let rafId: number | null = null;
const updatePreviewScale = () => {
    if (rafId != null) cancelAnimationFrame(rafId);
    rafId = requestAnimationFrame(() => {
        const el = previewRootRef.value;
        if (!el) return;
        // 预留少量底部空间，避免贴底裁切
        const rect = el.getBoundingClientRect();
        const available = window.innerHeight - rect.top - 60;
        // 最小缩放兜底，避免过小导致不可读；可按需调整
        const scale = Math.min(1, Math.max(0.6, available / DESIGN_HEIGHT));
        previewScale.value = Number(scale.toFixed(3));
    });
};

onMounted(() => {
    nextTick(() => updatePreviewScale());
    window.addEventListener('resize', updatePreviewScale, { passive: true } as any);
    // 某些布局下（抽屉/页面滚动）顶部位置会变化，这里也做兜底监听
    window.addEventListener('scroll', updatePreviewScale, { passive: true } as any);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', updatePreviewScale as any);
    window.removeEventListener('scroll', updatePreviewScale as any);
    if (rafId != null) cancelAnimationFrame(rafId);
    rafId = null;
});

const previewScaleStyle = computed(() => {
    const s = previewScale.value;
    if (s >= 0.999) return {};
    return {
        transform: `scale(${s})`,
        transformOrigin: 'top center'
    } as any;
});

// ==================== 剩余次数展示（对齐 Uniapp 逻辑） ====================
// 后台编辑侧用 drawLimitType 控制“是否限制次数”：
// - drawLimitType !== 1：视为“不限制”（对应移动端 -1）
// - drawLimitType === 1：从 dailyLimit/totalLimit 计算剩余次数（只设置其中一个时取该值）
const isUnlimitedAll = computed(() => Number(formState.value.drawLimitType) !== 1);
const remainingTimes = computed(() => {
    const daily = Number((formState.value as any).dailyLimit ?? 0);
    const total = Number((formState.value as any).totalLimit ?? 0);
    if (daily > 0 && total > 0) return Math.min(daily, total);
    if (daily > 0) return daily;
    if (total > 0) return total;
    return 0;
});
const showRemainingInfo = computed(() => {
    // 不限制：展示“每次抽奖消耗”（若不消耗积分则不展示该条）
    if (isUnlimitedAll.value) return Boolean((formState.value as any).isConsumePoints);
    // 有限制：0 也需要展示
    return remainingTimes.value >= 0;
});

// 九宫格布局映射: 顺时针 1-8，中间为抽奖按钮
// 布局: 1 2 3
//       8 X 4
//       7 6 5
const gridOrder = [1, 2, 3, 8, -1, 4, 7, 6, 5]; // -1 表示中间抽奖按钮

const gridCells = computed(() => {
    return gridOrder.map((pos, index) => {
        if (pos === -1) {
            return { index, position: -1, isCenter: true, tempIndex: null };
        }
        const grid = props.gridConfig.find(g => g.position === pos);
        return {
            index,
            position: pos,
            isCenter: false,
            tempIndex: grid?.tempIndex ?? null
        };
    });
});

// 背景样式
const contentStyle = computed(() => {
    const styles: any = {};
    if (props.decorateConfig.bgImage) {
        styles.backgroundImage = `url(${props.decorateConfig.bgImage})`;
        // 背景图按宽度等比展示，不拉伸高度；图片高度不足时由背景色补齐
        styles.backgroundRepeat = "no-repeat";
        styles.backgroundPosition = "top center";
        styles.backgroundSize = "100% auto";
        // 预览容器内部滚动时，背景跟随内容一起滚动
        styles.backgroundAttachment = "local";
    }
    if (props.decorateConfig.bgColor) {
        styles.backgroundColor = props.decorateConfig.bgColor;
    }
    return styles;
});

// 抽奖动画顺序：顺时针 1->2->3->4->5->6->7->8
const animationOrder = [1, 2, 3, 4, 5, 6, 7, 8];

const handleDraw = () => {
    if (isRunning.value) return;
    isRunning.value = true;

    let step = 0;
    const totalSteps = animationOrder.length * 3 + Math.floor(Math.random() * 8);

    const interval = setInterval(() => {
        activeIndex.value = animationOrder[step % animationOrder.length];
        step++;

        if (step >= totalSteps) {
            clearInterval(interval);
            setTimeout(() => {
                isRunning.value = false;
                activeIndex.value = -1;
            }, 500);
        }
    }, 100);
};

const getAwardName = (tempIndex: number | null) => {
    if (tempIndex === null) return props.noWinName || t('谢谢参与');
    const prize = props.prizeList.find(item => item.tempIndex === tempIndex);
    return prize ? prize.prizeName : t('未知奖品');
};

const getAwardImage = (tempIndex: number | null) => {
    if (tempIndex === null) return props.noWinImage;
    const prize = props.prizeList.find(item => item.tempIndex === tempIndex);
    return prize ? prize.prizeImage : '';
};
</script>

<style scoped lang="less">
.mobile-preview {
    width: 375px;
    height: 740px;
    background: #f7f8fa;
    border: 10px solid #333;
    border-radius: 40px;
    overflow: hidden;
    position: relative;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
    box-sizing: border-box;

    .mobile-body {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        // 关键：允许内部可滚动区域在 flex 布局下正确收缩
        min-height: 0;
    }

    .status-bar {
        height: 20px;
        background: #333;
        color: #fff;
        display: flex;
        justify-content: space-between;
        padding: 0 15px;
        align-items: center;
        font-size: 12px;
    }

    .nav-bar {
        height: 44px;
        background: #fff;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 5px;
        border-bottom: 1px solid #eee;
        position: relative;

        .back-icon {
            position: absolute;
            left: 5px;
            font-size: 18px;
            line-height: 1;
            color: rgba(0, 0, 0, 0.85);
        }

        .title {
            font-weight: 500;
            font-size: 16px;
            color: rgba(0, 0, 0, 0.85);
            line-height: 22px;
        }

        // iOS 风格“更多”按钮：三个点 + 分隔线 + 右侧圆圈
        .more-icon {
            position: absolute;
            right: 5px;
            width: 87px;
            height: 32px;
            border: 1px solid rgba(0, 0, 0, 0.4);
            border-radius: 999px;
            box-sizing: border-box;
            background-color: #fff;
            display: inline-flex;
            align-items: center;
            justify-content: flex-start;
            padding: 0 10px;
            gap: 10px;

            .dots {
                display: inline-flex;
                align-items: center;
                gap: 4px;
            }

            .dot {
                display: inline-block;
                width: 4px;
                height: 4px;
                border-radius: 50%;
                background: rgba(0, 0, 0, 1);
                &:nth-child(2){
                    width: 7px;
                    height: 7px;
                }
            }

            .divider {
                display: inline-block;
                width: 1px;
                height: 14px;
                background: rgba(0, 0, 0, 0.35);
            }

            .ring {
                display: block;
                width: 19px;
                height: 19px;
                border-radius: 50%;
                border: 2px solid rgba(0, 0, 0, 1);
                box-sizing: border-box;
                // background: transparent;
                position: relative;
                &::after{
                    content: '';
                    display: block;
                    width: 7px;
                    height: 7px;
                    border-radius: 50%;
                    background: rgba(0, 0, 0, 1);
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                }
            }
        }
    }

    .content {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        // 关键：否则在部分浏览器/布局下会出现“滚动不到底/滚不动”
        min-height: 0;
        scrollbar-width: none; /* Firefox */
        -ms-overflow-style: none; /* IE/Edge */
        &::-webkit-scrollbar {
            width: 0; /* Chrome/Safari/Opera */
        }
        // 背景由 contentStyle 控制：bgImage 使用 100% auto 等比，bgColor 作为底色
        padding-top: 120px;
        padding-bottom: 20px;
        box-sizing: border-box;
        position: relative;
        .content-top {
            background: rgba(255, 255, 255, 0.24);
            border-radius: 11px;
            font-weight: 500;
            font-size: 14px;
            color: #FFFFFF;
            line-height: 22px;
            margin-bottom: 26px;
            width: 235px;
            height: 22px;
            text-align: center;
            margin: 0 auto 20px;
        }
        .content-prize{
            position: absolute;
            top: 80px;
            right: 5px;
            width: 40px;
            height: 40px;
            img{
                width: 100%;
                height: 100%;
            }
        }
    }

    // 九宫格抽奖样式
    .grid-lottery-container {
        width: 260px;
        height: 260px;
        margin: 0 auto;
        padding: 20px;
        background: url('@/assets/lottery/turntableBg.png') no-repeat center center;
        background-size: 100% 100%;
        border-radius: 10px;
        // box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .grid-lottery {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: repeat(3, 1fr);
        gap: 12px 10px;
        width: 220px;
        /*
         * 避免在部分环境中 aspect-ratio 兼容性不稳定时，
         * 奖品名称变长把格子撑高，导致九宫格“错乱”。
         *
         * 计算：cellW = (220 - 2*10) / 3 = 66.67
         * gridH = 3*cellW + 2*12 ≈ 224
         */
        height: 224px;
        margin: 5px auto;
    }

    .grid-cell {
        height: 100%;
        // background: #fff;
        border-radius: 6px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 6px;
        box-sizing: border-box;
        transition: all 0.15s ease;
        overflow: hidden;
        // box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);

        &.is-active {
            // 对齐移动端高亮效果（更偏金色发光）
            background: radial-gradient(circle at 50% 50%, #ffffff 0%, rgba(242, 209, 120, 0.35) 45%, rgba(241, 195, 103, 0.62) 78%, rgba(237, 165, 22, 0.72) 100%);
            transform: scale(1.02);
            box-shadow: 0 0 10px rgba(231, 177, 26, 0.28);

            .award-name {
                color: #5a2f00;
            }
        }

        &.is-center {
            // background: linear-gradient(135deg, #FF4757 0%, #FF6B6B 100%);
            cursor: pointer;
            padding-top: 15px;

            &:hover {
                transform: scale(1.02);
            }

            &:active {
                transform: scale(0.98);
            }
        }

        .award-img {
            width: 32px;
            height: 32px;
            object-fit: contain;
            margin-bottom: 2px;
        }

        .award-name {
            font-size: 10px;
            color: #333;
            text-align: center;
            display: block;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            width: 100%;
        }
    }

    .draw-btn {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 2px;

        .draw-text {
            color: #fff;
            font-size: 16px;
            font-weight: bold;
            line-height: 1.2;
        }
        .draw-point{
            color: #fff;
            font-size: 12px;
            line-height: 1.2;
        }
    }
    .remaining-times{
        background: url('@/assets/lottery/remaining-times.png') no-repeat center center;
        background-size: 250px 100px;
        height: 40px;
        text-align: center;
        line-height: 40px;
        margin: 5px auto 0;
        width: 300px;
        font-weight: 500;
        font-size: 14px;
        color: #FFFFFF;
        .remaining-times-num{
            color: #EC3364;
        }
    }

    .rule-section {
        margin: 20px;
        background: #FFFAE7;
        border-radius: 8px;
        padding: 15px;
        color: #F48900;

        .rule-title {
            text-align: center;
            font-weight: bold;
            margin-bottom: 10px;
            font-size: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            &::before{
                content: '';
                display: inline-block;
                width: 50px;
                height: 1px;
                background: #F48900;
                margin-right: 10px;
            }
            &::after{
                content: '';
                display: inline-block;
                width: 50px;
                height: 1px;
                background: #F48900;
                margin-left: 10px;
            }
        }

        .rule-content {
            font-weight: 500;
            font-size: 13px;
            color: #F48900;
            line-height: 21px;
            white-space: pre-wrap;
        }
    }
}
</style>
