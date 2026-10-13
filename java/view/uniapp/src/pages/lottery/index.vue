<template>
    <tig-layout :title="$t('抽奖大转盘')">
        <view class="lottery-page" :style="contentStyle">
            <!-- 中奖滚动通知 -->
            <view v-if="lotteryInfo?.decorateConfig.showWinnerList === 1 && winnerList.length > 0" class="winner-notice">
                <view class="winner-carousel">
                    <transition name="winner-slide" mode="out-in">
                        <view :key="currentWinnerIndex" class="winner-item">
                            <text>{{ currentWinner?.nickname }} {{ $t("抽中了") }} {{ currentWinner?.prizeName }}</text>
                        </view>
                    </transition>
                </view>
            </view>

            <!-- 奖品按钮 -->
            <view class="prize-btn" @click="openRecordPopup">
                <image :src="prizeEntryImage" mode="aspectFit" />
                <!--                <tig-image :src="staticResource('/images/lottery/prize.png')" mode="aspectFit" />-->
            </view>

            <!-- 九宫格抽奖区域 -->
            <view class="lottery-container" :style="lotteryContainerStyle">
                <!-- 无活动常驻提示：放在抽奖区域内，绝对定位居中 -->
                <view v-if="noActivityVisible" class="no-activity-banner">
                    {{ $t("暂无抽奖活动进行") }}
                </view>
                <view class="grid-lottery">
                    <view
                        v-for="cell in gridCells"
                        :key="cell.index"
                        class="grid-cell"
                        :class="{
                            'is-center': cell.isCenter,
                            'is-active': !cell.isCenter && activeIndex === cell.position
                        }"
                        @click="cell.isCenter && handleDraw()"
                    >
                        <template v-if="cell.isCenter">
                            <view class="draw-btn">
                                <text class="draw-text">{{ $t("抽奖") }}</text>
                                <text v-if="lotteryInfo?.costPoints" class="draw-point">{{ lotteryInfo.costPoints }}{{ $t("积分") }}</text>
                            </view>
                        </template>
                        <template v-else>
                            <tig-image v-if="cell.prizeImage" :src="cell.prizeImage" class="award-img" mode="aspectFit" />
                            <text class="award-name">{{ cell.prizeName }}</text>
                        </template>
                    </view>
                </view>
            </view>

            <!-- 剩余次数 / 不限制文案 -->
            <view v-if="lotteryInfo && showRemainingInfo" class="remaining-times" :style="remainingTimesStyle">
                <template v-if="isUnlimitedAll">
                    {{ $t("每次抽奖消耗") }}
                    <text class="remaining-times-num">{{ lotteryInfo.costPoints || 0 }}</text>
                    {{ $t("积分") }}
                </template>
                <template v-else>
                    {{ $t("您还有") }} <text class="remaining-times-num">{{ remainingTimes }}</text> {{ $t("次抽奖机会") }}
                </template>
            </view>

            <!-- 活动规则 -->
            <view class="rule-section">
                <view class="rule-title">{{ $t("活动规则") }}</view>
                <rich-text class="rule-content" :nodes="activityRulesNodes" />
            </view>
        </view>

        <!-- 抽奖结果弹窗 -->
        <uni-popup ref="resultPopupRef" type="center" :mask-click="true" class="result-popup-container">
            <view v-if="drawResult" class="result-popup" :style="resultPopupStyle">
                <view class="result-title">
                    <template v-if="drawResult.isWin">
                        <view class="result-win">{{ $t("恭喜你") }}</view>
                        <view class="result-win-big">{{ $t("中奖了") }}</view>
                    </template>
                    <template v-else>
                        <view class="result-lose">{{ loseLine1 }}</view>
                        <view class="result-lose-big">{{ loseLine2 }}</view>
                    </template>
                </view>
                <tig-image :src="drawResult.prizeImage" class="result-image" mode="aspectFit" />
                <text class="result-prize-name">{{ drawResult.prizeName }}</text>
                <template v-if="drawResult.prizeType === 2">
                    <buy
                        :id="JSON.parse(drawResult.prizeValue || '{}')?.productId"
                        :sku-id="JSON.parse(drawResult.prizeValue || '{}')?.skuId"
                        :lottery-record-id="drawResult.recordId"
                        :is-quick="true"
                        @callback="resultPopupRef?.close()"
                    >
                        <view class="result-btn">{{ resultActionText }}</view>
                    </buy>
                </template>
                <template v-else>
                    <view class="result-btn" @click="handleResultAction">{{ resultActionText }}</view>
                </template>
            </view>
            <view class="iconfont-h5 icon-jiaochacross80 close-btn" @click="resultPopupRef?.close()" />
        </uni-popup>

        <!-- 提示弹窗：用于支持换行的提示文案（showToast 在部分端不支持 \n） -->
        <uni-popup ref="tipPopupRef" type="center" :mask-click="false" class="tip-popup-container">
            <view class="tip-popup">
                <text class="tip-popup-text">{{ tipMessage }}</text>
            </view>
        </uni-popup>

        <!-- 中奖记录弹窗 -->
        <tig-popup v-model:show="showRecordPopup" position="bottom" :round="20">
            <view class="record-popup">
                <view class="record-header">
                    <text class="record-title">{{ $t("奖品") }}</text>
                </view>
                <scroll-view class="record-list" scroll-y @scrolltolower="loadMoreRecords">
                    <view v-for="record in recordList" :key="record.recordId" class="record-item">
                        <tig-image :src="record.prizeImage" class="record-image" mode="aspectFit" />
                        <text class="record-name">{{ record.prizeName }}</text>
                        <template v-if="record.prizeType === 2">
                            <buy
                                :id="JSON.parse(record.prizeValue || '{}')?.productId"
                                :sku-id="JSON.parse(record.prizeValue || '{}')?.skuId"
                                :is-quick="true"
                                :lottery-record-id="record.recordId"
                                @callback="showRecordPopup = false"
                            >
                                <view :class="['record-btn', { disabled: record.status !== 0 }]">
                                    {{ getRecordActionText(record) }}
                                </view>
                            </buy>
                        </template>
                        <template v-else>
                            <view
                                :class="['record-btn', { disabled: [1, 3].includes(record.prizeType) ? true : record.status !== 0 }]"
                                @click="handleRecordAction(record)"
                            >
                                {{ getRecordActionText(record) }}
                            </view>
                        </template>
                    </view>
                    <loading-box v-model="recordLoading" :page="recordPage" :length="recordList.length" />
                    <empty-box v-if="recordList.length === 0 && !recordLoading" style="margin-top: 200rpx" :text="$t('暂无中奖记录')" />
                </scroll-view>
            </view>
        </tig-popup>

        <tig-loadingpage v-model="isLoading" />
    </tig-layout>
</template>

<script lang="ts" setup>
import { ref, computed, shallowRef } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import { staticResource, redirect } from "@/utils";
import { imageFormat } from "@/utils/format";
import { getLotteryInfo, drawLottery, getMyRecords, getWinners } from "@/api/lottery/lottery";
import type { LotteryInfo, DrawResult, PrizeRecord, GridCell, GridPosition, LotteryWinner } from "@/types/lottery/lottery";
import TigPopup from "@/components/tig/tig-popup/tig-popup.vue";
import TigImage from "@/components/tig/tig-image/tig-image.vue";
import buy from "@/components/product/buy.vue";

const { t } = useI18n();

// ==================== 数据定义 ====================

/** 活动信息 */
const lotteryInfo = ref<LotteryInfo | null>(null);

/** 无活动常驻提示 */
const noActivityVisible = ref(false);

/** 页面加载状态 */
const isLoading = ref(true);

/** 是否正在抽奖 */
const isDrawing = ref(false);

/** 当前高亮位置 */
const activeIndex = ref(-1);

/** 抽奖结果 */
const drawResult = ref<DrawResult | null>(null);

/** 结果弹窗引用 */
const resultPopupRef = shallowRef();

/** 提示弹窗引用（支持换行） */
const tipPopupRef = shallowRef<any>();
const tipMessage = ref("");
let tipTimer: ReturnType<typeof setTimeout> | null = null;
const showTip = (msg: string, duration = 2000) => {
    tipMessage.value = msg;
    tipPopupRef.value?.open?.();
    if (tipTimer) clearTimeout(tipTimer);
    tipTimer = setTimeout(() => {
        tipPopupRef.value?.close?.();
        tipTimer = null;
    }, duration);
};

/** 记录弹窗显示 */
const showRecordPopup = ref(false);

/** 中奖记录列表 */
const recordList = ref<PrizeRecord[]>([]);

/** 记录加载状态 */
const recordLoading = ref(false);

/** 记录分页 */
const recordPage = ref(1);

/** 记录总数 */
const recordTotal = ref(0);

/** 中奖名单列表 */
const winnerList = ref<LotteryWinner[]>([]);

/** 当前显示的中奖名单索引 */
const currentWinnerIndex = ref(0);

/** 中奖名单轮播定时器 */
let winnerCarouselTimer: ReturnType<typeof setInterval> | null = null;

/** 当前展示的中奖信息 */
const currentWinner = computed(() => winnerList.value[currentWinnerIndex.value]);

// ==================== 九宫格布局 ====================

// 布局映射: 1 2 3 / 8 X 4 / 7 6 5
const gridOrder = [1, 2, 3, 8, -1, 4, 7, 6, 5]; // -1 表示中间抽奖按钮

// 动画顺序：顺时针 1->2->3->4->5->6->7->8
const animationOrder = [1, 2, 3, 4, 5, 6, 7, 8];

/** 上一次稳定停留的位置（用于抽奖失败时回退/以及下次从此位置开始） */
let lastStableIndex = -1;

/** 九宫格单元格 */
const gridCells = computed<GridCell[]>(() => {
    if (!lotteryInfo.value) return [];

    return gridOrder.map((pos, index) => {
        if (pos === -1) {
            return { index, position: -1, isCenter: true, prizeId: null };
        }
        const grid = lotteryInfo.value!.gridConfig.find((g: GridPosition) => g.position === pos);
        return {
            index,
            position: pos,
            isCenter: false,
            prizeId: grid?.prizeId ?? null,
            prizeName: grid?.prizeName ?? lotteryInfo.value!.noWinName,
            prizeImage: grid?.prizeImage ?? lotteryInfo.value!.noWinImage,
            prizeType: grid?.prizeType ?? 0
        };
    });
});

/** 背景样式 */
const contentStyle = computed(() => {
    const styles: Record<string, string> = {};
    if (lotteryInfo.value?.decorateConfig.bgImage) {
        styles.backgroundImage = `url(${lotteryInfo.value.decorateConfig.bgImage})`;
        // 页面高度超过 100vh 时，避免背景图按高度被“拉伸”
        // 按宽度等比缩放，图片高度不足时由背景色补齐（不重复平铺）
        styles.backgroundSize = "100% auto";
        styles.backgroundRepeat = "no-repeat";
        styles.backgroundPosition = "top center";
    }
    if (lotteryInfo.value?.decorateConfig.bgColor) {
        styles.backgroundColor = lotteryInfo.value.decorateConfig.bgColor;
    }
    return styles;
});

/** 奖品入口图（左上角奖品按钮图），优先读取装修配置，缺省回退默认图 */
const prizeEntryImage = computed(() => imageFormat(lotteryInfo.value?.decorateConfig?.prizeEntryImage ?? "") || staticResource("lottery/prize.png"));

/** 九宫格背景样式 */
const lotteryContainerStyle = computed(() => ({
    background: `url(${staticResource("lottery/turntableBg.png")}) no-repeat center center`,
    backgroundSize: "100% 100%"
}));

/** 剩余次数背景样式 */
const remainingTimesStyle = computed(() => ({
    background: `url(${staticResource("lottery/remaining-times.png")}) no-repeat center center`,
    backgroundSize: "500rpx 200rpx"
}));

/** 剩余次数展示：-1 表示不限制 */
const isUnlimitedToday = computed(() => lotteryInfo.value?.todayRemaining === -1);
const isUnlimitedTotal = computed(() => lotteryInfo.value?.totalRemaining === -1);
const isUnlimitedAll = computed(() => isUnlimitedToday.value && isUnlimitedTotal.value);

/** 剩余抽奖次数（不限制时返回 -1；其中一个不限制则取另一个；都有限制取最小值） */
const remainingTimes = computed(() => {
    if (!lotteryInfo.value) return 0;
    const today = lotteryInfo.value.todayRemaining;
    const total = lotteryInfo.value.totalRemaining;
    // 由积分可抽次数
    const userPoints = Number(lotteryInfo.value.userPoints ?? 0);
    const costPoints = Number(lotteryInfo.value.costPoints ?? 0);
    const canUsePoints = costPoints > 0 ? Math.floor(userPoints / costPoints) : Number.POSITIVE_INFINITY;
    if (today === -1 && total === -1) return -1;
    if (today === -1) return total;
    if (total === -1) return today;
    return Math.min(today, total, canUsePoints);
});

/** 是否显示剩余信息：不限制时显示“消耗积分”；有限制时仅在剩余次数 > 0 显示 */
const showRemainingInfo = computed(() => {
    if (!lotteryInfo.value) return false;
    if (isUnlimitedAll.value) return true;
    // 次数用完（0）也需要展示
    return remainingTimes.value >= 0;
});

/** 结果弹窗背景样式 */
const resultPopupStyle = computed(() => ({
    background: `url(${staticResource("lottery/lotteryPop.png")}) no-repeat`,
    backgroundSize: "100% 100%"
}));

/** 结果弹窗按钮文字 */
const resultActionText = computed(() => {
    if (!drawResult.value) return "";
    if (!drawResult.value.isWin) return t("再抽一次");
    return drawResult.value.prizeType === 2 ? t("立即领取") : t("立即查看");
});

/** 活动规则：把纯文本里的换行转成 <br/>，保证 rich-text 能正常换行 */
const activityRulesNodes = computed(() => {
    const rules = (lotteryInfo.value?.decorateConfig?.activityRules ?? "") as unknown as string;
    if (!rules) return "";
    return String(rules).replace(/\r\n|\r|\n/g, "<br/>");
});

/** 未中奖文案：默认“很遗憾 / 没有抽中”；若接口返回 noWinTips，则按前3个字换行，只显示两行 */
const noWinTipsText = computed(() => String(lotteryInfo.value?.noWinTips ?? "").trim());
const loseLine1 = computed(() => {
    const tip = noWinTipsText.value;
    if (!tip) return t("很遗憾");
    return tip.slice(0, 3) || t("很遗憾");
});
const loseLine2 = computed(() => {
    const tip = noWinTipsText.value;
    if (!tip) return t("没有抽中");
    return tip.slice(3) || t("没有抽中");
});

/** 活动信息默认值（接口未开启/无数据时兜底展示） */
const buildDefaultLotteryInfo = (): LotteryInfo => {
    const noWinName = t("谢谢参与");
    const noWinImage = "https://oss.tigshop.com/img/gallery/202512/1766467150H93CwsgU07PQfSqwj4.jpeg";
    return {
        activityId: 0,
        activityName: "",
        startTime: "",
        endTime: "",
        costPoints: 1,
        decorateConfig: {
            bgImage: "https://oss.tigshop.com/img/gallery/202512/1766469537jXrMsUPDXoszzk0KCC.jpeg",
            bgColor: "",
            prizeEntryImage: "",
            showWinnerList: 0,
            activityRules: ""
        },
        noWinName,
        noWinImage,
        noWinTips: t("很遗憾没有抽中"),
        gridConfig: Array.from({ length: 8 }, (_, i) => ({
            position: i + 1,
            prizeId: null,
            probability: 0,
            prizeName: noWinName,
            prizeImage: noWinImage,
            prizeType: 0,
            prizeValue: ""
        })),
        userPoints: 0,
        todayRemaining: 0,
        totalRemaining: 0,
        winRemaining: 0
    };
};

// ==================== 接口调用 ====================

/** 获取活动信息 */
const fetchLotteryInfo = async () => {
    try {
        const res = await getLotteryInfo();
        if (!res) {
            // 接口无活动：使用默认值兜底渲染（背景、九宫格占位、未中奖文案等）
            lotteryInfo.value = buildDefaultLotteryInfo();
            noActivityVisible.value = true;
        } else {
            lotteryInfo.value = res;
            noActivityVisible.value = false;
        }
        // 如果开启了中奖名单显示，获取中奖名单
        if (lotteryInfo.value.decorateConfig.showWinnerList === 1) {
            fetchWinners();
        }
    } catch (error: any) {
        uni.showToast({ title: error.message || t("获取活动信息失败"), icon: "none" });
    } finally {
        isLoading.value = false;
    }
};

/** 获取中奖名单 */
const fetchWinners = async () => {
    try {
        const res = await getWinners(lotteryInfo.value?.activityId);
        winnerList.value = res || [];
        // 开始轮播动画
        if (winnerList.value.length > 0) {
            startWinnerCarousel();
        }
    } catch (error: any) {
        console.error("获取中奖名单失败:", error);
    }
};

/** 开始中奖名单轮播 */
const startWinnerCarousel = () => {
    // 清除之前的定时器
    if (winnerCarouselTimer) {
        clearInterval(winnerCarouselTimer);
    }
    // 每3秒切换一条
    winnerCarouselTimer = setInterval(() => {
        if (winnerList.value.length > 0) {
            currentWinnerIndex.value = (currentWinnerIndex.value + 1) % winnerList.value.length;
        }
    }, 3000);
};

/** 停止中奖名单轮播 */
const stopWinnerCarousel = () => {
    if (winnerCarouselTimer) {
        clearInterval(winnerCarouselTimer);
        winnerCarouselTimer = null;
    }
};

/** 执行抽奖 */
const handleDraw = async () => {
    if (isDrawing.value) return;
    if (!lotteryInfo.value) return;
    // 活动未开启：兜底数据只用于展示，不允许抽奖
    if (lotteryInfo.value.activityId <= 0) {
        // uni.showToast({ title: t("暂无抽奖活动进行"), icon: "none" });
        return;
    }
    // todayRemaining / totalRemaining 为 -1 时表示不限制
    if (lotteryInfo.value.todayRemaining !== -1 && lotteryInfo.value.todayRemaining <= 0) {
        showTip(`${t("今日次数已用完，")}\n${t("明天再来")}`);
        return;
    }
    // 检查抽奖次数
    if (lotteryInfo.value.totalRemaining !== -1 && lotteryInfo.value.totalRemaining <= 0) {
        showTip(`${t("您的抽奖次数已用完，")}\n${t("感谢您的参与")}`);
        return;
    }

    // 检查积分
    if (lotteryInfo.value.userPoints < lotteryInfo.value.costPoints) {
        uni.showToast({ title: t("积分不足"), icon: "none" });
        return;
    }

    isDrawing.value = true;
    // 记录开始抽奖前的位置，接口失败时回退
    lastStableIndex = activeIndex.value;

    try {
        // 开始动画
        startAnimation();

        // 调用抽奖接口
        const res = await drawLottery();
        drawResult.value = res;

        // 更新剩余次数和积分
        lotteryInfo.value.todayRemaining = res.todayRemaining;
        lotteryInfo.value.totalRemaining = res.totalRemaining;
        lotteryInfo.value.userPoints -= lotteryInfo.value.costPoints;

        // 动画停止在中奖位置
        await stopAnimation(res.position);
        lastStableIndex = res.position;

        // 只有“中奖”才刷新列表接口（中奖名单/我的中奖记录）
        if (res?.isWin) {
            if (lotteryInfo.value?.decorateConfig?.showWinnerList === 1) {
                fetchWinners();
            }
        }

        // 显示结果弹窗
        resultPopupRef.value?.open();
    } catch (error: any) {
        uni.showToast({ title: error.message || t("抽奖失败"), icon: "none" });
        // 抽奖失败：停止动画并回到上一次停留的位置
        if (animationTimer) {
            clearInterval(animationTimer);
            animationTimer = null;
        }
        if (Number.isFinite(lastStableIndex) && lastStableIndex !== -1) {
            activeIndex.value = lastStableIndex;
        }
    } finally {
        isDrawing.value = false;
    }
};

/** 开始抽奖动画 */
let animationTimer: ReturnType<typeof setInterval> | null = null;
let animationStep = 0;

const startAnimation = () => {
    if (animationTimer) {
        clearInterval(animationTimer);
        animationTimer = null;
    }
    animationStep = 0;
    // 从当前高亮位置开始转（没有则从第一个开始）
    const startIdx = Math.max(0, animationOrder.indexOf(activeIndex.value));
    activeIndex.value = animationOrder[startIdx];
    animationTimer = setInterval(() => {
        activeIndex.value = animationOrder[(startIdx + animationStep) % animationOrder.length];
        animationStep++;
    }, 100);
};

/** 停止动画并定位到指定位置 */
const stopAnimation = (targetPosition: number): Promise<void> => {
    return new Promise((resolve) => {
        if (animationTimer) {
            clearInterval(animationTimer);
            animationTimer = null;
        }

        // 计算剩余步数，确保至少转2圈后停在目标位置
        const currentIndex = Math.max(0, animationOrder.indexOf(activeIndex.value));
        const targetIndex = animationOrder.indexOf(targetPosition);
        if (targetIndex < 0) {
            activeIndex.value = targetPosition;
            setTimeout(resolve, 300);
            return;
        }
        let remainingSteps = (targetIndex - currentIndex + animationOrder.length) % animationOrder.length;
        remainingSteps += animationOrder.length * 2; // 额外转2圈

        let step = 0;
        const tick = () => {
            activeIndex.value = animationOrder[(currentIndex + step + 1) % animationOrder.length];
            step++;
            if (step >= remainingSteps) {
                activeIndex.value = targetPosition;
                setTimeout(resolve, 300);
                return;
            }
            setTimeout(tick, 100 + step * 10); // 逐渐减速
        };
        setTimeout(tick, 100);
    });
};

/** 获取中奖记录 */
const fetchRecords = async (reset = true) => {
    if (reset) {
        recordPage.value = 1;
        recordList.value = [];
    }

    recordLoading.value = true;
    try {
        const res = await getMyRecords({ page: recordPage.value, size: 20 });
        if (reset) {
            recordList.value = res.records;
        } else {
            recordList.value = [...recordList.value, ...res.records];
        }
        recordTotal.value = res.total;
    } catch (error: any) {
        uni.showToast({ title: error.message || t("获取记录失败"), icon: "none" });
    } finally {
        recordLoading.value = false;
    }
};

/** 加载更多记录 */
const loadMoreRecords = () => {
    if (recordLoading.value) return;
    if (recordList.value.length >= recordTotal.value) return;
    recordPage.value++;
    fetchRecords(false);
};

/** 打开记录弹窗 */
const openRecordPopup = () => {
    showRecordPopup.value = true;
    fetchRecords();
};

// ==================== 操作处理 ====================

/** 处理结果弹窗按钮点击 */
const handleResultAction = () => {
    if (!drawResult.value) return;

    resultPopupRef.value?.close();

    if (!drawResult.value.isWin) {
        // 未中奖 - 再抽一次
        setTimeout(() => {
            handleDraw();
        }, 300);
        return;
    }

    // 根据奖品类型跳转
    switch (drawResult.value.prizeType) {
        case 1: // 积分
            redirect({ url: "/pages/user/point/detail" });
            break;
        case 2: // 商品 - 需要领取
            // 跳转到订单确认页
            redirect({
                url: "/pages/order/confirm/index",
                param: {
                    prizeId: drawResult.value.prizeId,
                    productId: drawResult.value.prizeValue,
                    fromLottery: 1
                }
            });
            break;
        case 3: // 优惠券
            redirect({ url: "/pages/coupon/index?type=2" });
            break;
    }
};

/** 处理记录项操作 */
const handleRecordAction = (record: PrizeRecord) => {
    console.log("record", record);
    // 积分/优惠券：始终可查看；其他类型：非未领取状态不操作
    if (![1, 3].includes(record.prizeType) && record.status !== 0) return;
    showRecordPopup.value = false;

    switch (record.prizeType) {
        case 1: // 积分
            redirect({ url: "/pages/user/point/detail" });
            break;
        case 2: // 商品
            // 跳转到订单确认页进行领取
            redirect({
                url: "/pages/order/confirm/index",
                param: {
                    recordId: record.recordId,
                    fromLottery: 1
                }
            });
            break;
        case 3: // 优惠券
            redirect({ url: "/pages/coupon/index?type=2" });
            break;
    }
};

/** 获取记录操作按钮文字 */
const getRecordActionText = (record: PrizeRecord): string => {
    // 积分/优惠券：显示“已发放”
    if ([1, 3].includes(record.prizeType)) return t("已发放");
    if (record.status === 1) return t("已领取");
    if (record.status === 2) return t("已过期");
    return record.prizeType === 2 ? t("立即领取") : t("立即查看");
};

// ==================== 生命周期 ====================

onLoad(() => {
    fetchLotteryInfo();
});

onUnload(() => {
    // 清除中奖名单轮播定时器
    stopWinnerCarousel();
    // 清除抽奖动画定时器
    if (animationTimer) {
        clearInterval(animationTimer);
        animationTimer = null;
    }
});
</script>

<style scoped lang="scss">
.lottery-page {
    // 注意：外层 tig-layout 还包含 navbar + safe-bottom，占用高度。
    // 这里如果直接 min-height:100vh，会导致“内容一屏能放下仍出现滚动条”。
    min-height: 100vh;
    min-height: calc(100vh - var(--nav-height, 0px) - var(--safe-bottom, 0px));
    background-color: transparent;
    background-size: cover;
    background-position: center;
    padding: 280rpx 30rpx 30rpx;
    box-sizing: border-box;
    position: relative;
}

// 无活动常驻提示
.no-activity-banner {
    position: absolute;
    top: calc(50% - 20rpx);
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 20;
    margin: 0;
    padding: 18rpx 50rpx;
    max-width: calc(100% - 60rpx);
    box-sizing: border-box;
    border-radius: 8rpx;
    background: rgba(0, 0, 0, 0.65);
    color: #fff;
    font-size: 24rpx;
    line-height: 36rpx;
    text-align: center;
    pointer-events: none;
}

// 中奖滚动通知
.winner-notice {
    background: rgba(255, 255, 255, 0.24);
    border-radius: 64rpx;
    font-weight: 500;
    font-size: 28rpx;
    color: #ffffff;
    line-height: 44rpx;
    width: 470rpx;
    text-align: center;
    margin: 0 auto 40rpx;
    padding: 10rpx 20rpx;
    height: 64rpx;
    overflow: hidden;
    position: relative;

    .winner-carousel {
        position: relative;
        height: 100%;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .winner-item {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        will-change: transform, opacity;

        text {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 100%;
        }
    }
}

// 中奖名单切换动画：上一条下滑淡出，新一条从上方滑入
.winner-slide-enter-active,
.winner-slide-leave-active {
    transition:
        transform 0.45s ease,
        opacity 0.45s ease;
}
.winner-slide-enter-from {
    transform: translateY(-20rpx);
    opacity: 0;
}
.winner-slide-enter-to {
    transform: translateY(0);
    opacity: 1;
}
.winner-slide-leave-from {
    transform: translateY(0);
    opacity: 1;
}
.winner-slide-leave-to {
    transform: translateY(20rpx);
    opacity: 0;
}

// 奖品按钮
.prize-btn {
    position: absolute;
    top: 200rpx;
    right: 20rpx;
    width: 90rpx;
    height: 90rpx;
    z-index: 10;

    image {
        width: 100%;
        height: 100%;
    }
}

// 九宫格容器
.lottery-container {
    width: 710rpx;
    height: 710rpx;
    margin: 0 auto;
    padding: 40rpx;
    border-radius: 20rpx;
    box-sizing: border-box;
    position: relative;
}

.grid-lottery {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(3, 1fr);
    gap: 25rpx 20rpx;
    width: 520rpx;
    // 固定高度，避免某些端不支持 aspect-ratio 导致内容把格子撑高、布局错乱
    // 计算：cellW = (500 - 2*20) / 3 = 153.33
    // gridH = 3*cellW + 2*30 ≈ 520
    height: 540rpx;
    margin: 20rpx auto;
}

.grid-cell {
    height: 100%;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12rpx;
    box-sizing: border-box;
    transition: all 0.15s ease;
    overflow: hidden;
    //background: #fff;

    &.is-active {
        background: radial-gradient(
            circle at 50% 50%,
            rgba(255, 255, 255, 1) 0%,
            rgba(242, 209, 120, 0.35) 45%,
            rgba(241, 195, 103, 0.62) 78%,
            rgba(237, 165, 22, 0.72) 100%
        );
        box-shadow: 0 0 14rpx rgba(231, 177, 26, 0.28);

        .award-name {
            color: #5a2f00;
        }
    }

    &.is-center {
        //background: linear-gradient(135deg, #ff4757 0%, #ff6b6b 100%);
        cursor: pointer;

        &:active {
            transform: scale(0.98);
        }
    }
}

.award-img {
    width: 80rpx !important;
    height: 80rpx !important;
    margin-bottom: 4rpx;
}

.award-name {
    text-align: center;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    width: 100%;
    font-weight: 500;
    font-size: 24rpx;
    color: #e80053;
    line-height: 30rpx;
}

.draw-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4rpx;

    .draw-text {
        color: #fff;
        font-size: 32rpx;
        font-weight: bold;
    }

    .draw-point {
        color: #fff;
        font-size: 24rpx;
    }
}

// 剩余次数
.remaining-times {
    height: 80rpx;
    text-align: center;
    line-height: 80rpx;
    margin: 20rpx auto 0;
    width: 600rpx;
    font-weight: 500;
    font-size: 28rpx;
    color: #ffffff;

    .remaining-times-num {
        color: #ec3364;
    }
}

// 活动规则
.rule-section {
    margin: 40rpx 20rpx;
    background: #fffae7;
    border-radius: 16rpx;
    padding: 30rpx;
    color: #f48900;

    .rule-title {
        text-align: center;
        font-weight: bold;
        margin-bottom: 20rpx;
        font-size: 32rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        &::before,
        &::after {
            content: "";
            display: inline-block;
            width: 100rpx;
            height: 2rpx;
            background: #f48900;
        }

        &::before {
            margin-right: 20rpx;
        }

        &::after {
            margin-left: 20rpx;
        }
    }

    .rule-content {
        font-weight: 500;
        font-size: 26rpx;
        color: #f48900;
        line-height: 42rpx;
    }

    // rich-text 内部节点的段落/换行兼容
    .rule-content :deep(p),
    .rule-content :deep(div) {
        margin: 0 0 16rpx;
    }
    .rule-content :deep(p:last-child),
    .rule-content :deep(div:last-child) {
        margin-bottom: 0;
    }
}

.result-popup-container {
    position: relative;
    .close-btn {
        position: absolute;
        bottom: -80rpx;
        left: 50%;
        transform: translateX(-50%);
        font-size: 40rpx;
        color: #fff;
        z-index: 999;
    }
}

// 换行提示弹窗
.tip-popup {
    max-width: 600rpx;
    min-width: 400rpx;
    padding: 22rpx 28rpx;
    background: rgba(0, 0, 0, 0.65);
    border-radius: 8rpx;
    text-align: center;
}
.tip-popup-text {
    color: #fff;
    font-size: 24rpx;
    line-height: 40rpx;
    text-align: center;
    white-space: pre-line;
}

// 抽奖结果弹窗
.result-popup {
    width: 710rpx;
    height: 738rpx;
    border-radius: 24rpx;
    padding: 147rpx 60rpx 30rpx;
    display: flex;
    flex-direction: column;
    align-items: center;

    .result-title {
        font-size: 36rpx;
        font-weight: bold;
        color: #262626;
        margin-bottom: 27rpx;
        text-align: center;
        .result-win,
        .result-lose {
            font-weight: bold;
            font-size: 36rpx;
            color: #c05e32;
        }
        .result-win-big,
        .result-lose-big {
            font-weight: 800;
            font-size: 48rpx;
            color: #c05e32;
            // 控制只显示两行中的第二行：第二行超出省略，避免出现第三行
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 100%;
        }
    }

    .result-image {
        width: 200rpx;
        height: 200rpx;
        margin-bottom: 20rpx;
    }

    .result-prize-name {
        padding: 0 60rpx;
        font-size: 28rpx;
        margin-bottom: 40rpx;
        text-align: center;
        font-weight: 500;
        color: #c05e32;
        line-height: 42rpx;
        line-clamp: 2;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
    }

    .result-btn {
        position: absolute;
        bottom: 70rpx;
        left: 50%;
        transform: translateX(-50%);
        width: 404rpx;
        height: 71rpx;
        background: linear-gradient(0deg, #ffbe6f, #ffa532);
        border-radius: 36rpx;
        line-height: 71rpx;
        font-weight: bold;
        font-size: 32rpx;
        color: #ffffff;
        text-align: center;
    }
}

// 中奖记录弹窗
.record-popup {
    height: 60vh;
    display: flex;
    flex-direction: column;

    .record-header {
        padding: 30rpx;
        text-align: center;
        border-bottom: 1rpx solid #eee;
        background: linear-gradient(to bottom, rgba(255, 188, 103, 0.52), rgba(255, 255, 255, 0.52) 80%);

        .record-title {
            font-weight: 600;
            font-size: 36rpx;
            color: #262626;
            line-height: 30rpx;
        }
    }

    .record-list {
        /*
         * scroll-view 在 uni-app 里对 flex 布局的支持不稳定：
         * 仅写 flex: 1 有概率出现“弹窗打开后列表滚不动”的问题。
         * 这里给一个明确高度，保证滚动区域可用。
         */
        height: calc(60vh - 140rpx);
        min-height: 0;
        padding: 10rpx;
        box-sizing: border-box;
    }

    .record-item {
        display: flex;
        align-items: center;
        padding: 20rpx;
        border-bottom: 1rpx solid #f5f5f5;

        .record-image {
            width: 100rpx;
            height: 100rpx;
            border-radius: 8rpx;
            flex-shrink: 0;
        }

        .record-name {
            flex: 1;
            margin: 0 20rpx;
            font-size: 28rpx;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .record-btn {
            width: 147rpx;
            height: 64rpx;
            flex-shrink: 0;
            border-radius: 8rpx;
            background: var(--general);
            font-weight: 500;
            font-size: 24rpx;
            color: #ffffff;
            line-height: 64rpx;
            text-align: center;
        }
        .disabled {
            background: #e5e5e5;
            color: #8f8f8f;
        }
    }

    .record-empty {
        text-align: center;
        padding: 100rpx 0;
        color: #999;
        font-size: 28rpx;
    }
}
</style>
