<template>
    <view class="termDetail" :style="pageLockStyle">
        <template v-if="showTermPay">
            <tig-layout class="term-detail-layout" :title="$t('账期详情')">
                <view class="header">
                    <view class="chart">
                        <canvas
                            id="creditChart"
                            canvas-id="creditChart"
                            class="chart-canvas"
                            :width="canvasSizePx"
                            :height="canvasSizePx"
                            :style="{ width: canvasSizeRpx + 'rpx', height: canvasSizeRpx + 'rpx' }"
                        />
                        <view class="chart-info">
                            <view class="chart-text creditLimit">{{ $t("信用额度") }}: {{ creditLimitText }}</view>
                            <view class="chart-text">{{ $t("剩余额度") }}: {{ availableAmountText }}</view>
                        </view>
                    </view>
                    <view class="termPayDays">
                        <view class="totalDays days">
                            <view class="text accountingPeriod">{{ $t("账期") }}</view>
                            <view class="num">{{ settlementDaysText }}</view>
                        </view>
                        <view class="hasDays days">
                            <view class="text">{{ $t("账期剩余") }}</view>
                            <view class="num">{{ remainingDaysText }}</view>
                        </view>
                    </view>
                </view>

                <!-- 结算状态切换（未结算/已结算） -->
                <view class="term-menu badgecolor">
                    <up-tabs
                        v-model:current="currentIndex"
                        line-color="var(--general)"
                        :list="menuTypeList"
                        :active-style="{ padding: '0 30rpx', color: '#1a1a1a' }"
                        :inactive-style="{ padding: '0 30rpx', color: '#9a9a9a' }"
                        :item-style="{ fontSize: '28px', height: '70rpx', width: '100%' }"
                        :scrollable="true"
                        key-name="value"
                        @change="changeMenu"
                    />
                </view>

                <!-- 订单列表（参考 user/order/list.vue） -->
                <view class="term-order-list">
                    <template v-for="order in currentOrders" :key="order.orderId">
                        <template v-if="order.items?.length">
                            <view class="term-order-card" hover-class="term-order-card--hover" hover-stay-time="150" @click="goOrderDetail(order.orderId)">
                                <view class="term-order-card-header">
                                    <text class="time">{{ $t("下单时间") }}：{{ order.orderTime }}</text>
                                    <text class="status" :style="getSettlementStatusStyle(order)">{{ $t(order.riskStatusText) }}</text>
                                </view>
                                <view class="term-order-card-goods">
                                    <view v-for="good in order.items" :key="good.productId" class="good-item">
                                        <view class="good-img">
                                            <tig-image :src="pickPic(good)" />
                                        </view>
                                        <view class="good-info">
                                            <view class="good-name">
                                                <view class="line2">{{ $t(good.productName) }}</view>
                                                <format-price
                                                    :font-style="{ color: '#1A1A1A', fontSize: '28rpx' }"
                                                    :decimals-style="{ fontSize: '20rpx' }"
                                                    :currency-style="{ fontSize: '20rpx' }"
                                                    class="goodPrice"
                                                    :show-text="false"
                                                    :is-bottom="false"
                                                    :price-data="good.price"
                                                />
                                            </view>
                                            <view class="good-qty">
                                                <view v-if="good.skuValue" class="sku-value">{{ formatSkuValue(good.skuValue) }}</view>
                                                <view class="qty">x{{ good.quantity }}</view>
                                            </view>
                                        </view>
                                    </view>
                                </view>

                                <view class="term-order-card-footer">
                                    <view class="summary">{{ $t("共") }}{{ order.totalQuantity }}{{ $t("件商品") }}</view>
                                    <view class="amount">
                                        {{ $t("账期付款") }}：
                                        <format-price :show-text="false" :is-bottom="false" :price-data="order.creditAmount" />
                                    </view>
                                </view>
                            </view>
                        </template>
                    </template>

                    <view v-if="!isLoading && currentOrders.length === 0" class="term-order-empty">
                        <view class="empty-imgBox">
                            <image :src="staticResource('termPay/emptyOrder.png')" style="width: 380rpx; height: 300rpx" />
                            <view class="empty-text">{{ $t("暂无订单") }}</view>
                        </view>
                        <!--                        <emptyBox mode="list" background="#f5f5f5" :text="$t('暂无订单')" />-->
                    </view>
                </view>

                <tig-fixed-placeholder :height="120" background-color="#fff" :border-style="{ borderTop: '1px solid #f0f1f2' }">
                    <view class="term-footer">
                        <tig-button :custom-style="{ height: '70rpx', width: '100%' }" @click="handleOpenRemittance">{{ $t("查看结算账号") }}</tig-button>
                    </view>
                </tig-fixed-placeholder>
            </tig-layout>
        </template>
        <tig-loadingpage v-model="isLoading" />

        <!-- 汇款方式弹窗（tig-popup 为全局组件，无需引入） -->
        <tig-popup v-model:show="showRemittancePopup" position="bottom" :round="20" :custom-style="{ overflow: 'hidden' }">
            <view class="remit-popup" @touchmove.stop.prevent>
                <view class="remit-popup__header">
                    <view class="remit-popup__title">{{ $t("汇款方式") }}</view>
                </view>
                <view class="remit-popup__content">
                    <OfflineRemit :offline-payment-list="offlinePaymentList" />
                </view>
            </view>
        </tig-popup>
    </view>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
import { computed, watch, onMounted, nextTick, ref } from "vue";
import { staticResource } from "@/utils";
import { useUserStore } from "@/store/user";
import { useThemeStore } from "@/store/theme";
import { onLoad, onPageScroll } from "@dcloudio/uni-app";
import { getUserCreditDetail } from "@/api/user/user";
import type { UserCreditDetailVO, UserCreditOrderItem } from "@/types/user/termPay";
import OfflineRemit from "@/pages/order/src/offline.vue";
import type { OfflinePaymentList } from "@/types/order/pay";
import { orderPayInfoTerm } from "@/api/order/pay";
import { useI18n } from "vue-i18n";

const userStore = useUserStore();
const themeStore = useThemeStore();

const { t } = useI18n();

const isLoading = ref(false);
const creditDetail = ref<UserCreditDetailVO | null>(null);

const showRemittancePopup = ref(false);
const offlinePaymentList = ref<OfflinePaymentList>({} as OfflinePaymentList);

// 页面滚动锁定：弹窗打开时禁止外层列表滚动（仅本页生效）
const currentScrollTop = ref(0);
const lockedScrollTop = ref(0);
const pageLockStyle = computed(() => {
    if (!showRemittancePopup.value) return {};
    // 将页面容器固定在当前 scrollTop，达到“背景不动”的效果
    return {
        position: "fixed",
        left: "0",
        right: "0",
        width: "100%",
        top: `-${lockedScrollTop.value}px`,
        height: "100vh",
        overflow: "hidden"
    } as Record<string, string>;
});

/** tabs：未结算/已结算（展示用 value 字段，和 order/list.vue 保持一致） */
const currentIndex = ref(0);
const menuTypeList = ref([
    { value: t("未结算"), status: 0 },
    { value: t("已结算"), status: 1 }
]);

const changeMenu = (e: any) => {
    currentIndex.value = e?.index ?? 0;
};

const showTermPay = computed(() => {
    if (creditDetail.value) return true;
    return userStore.userInfo?.creditPaymentInfo && Object.keys(userStore.userInfo?.creditPaymentInfo)?.length > 0;
});

const currentOrders = computed<UserCreditOrderItem[]>(() => {
    if (!creditDetail.value) return [];
    const status = menuTypeList.value[currentIndex.value]?.status ?? 0;
    return status === 0 ? creditDetail.value.pendingOrders || [] : creditDetail.value.settledOrders || [];
});

const creditLimitText = computed(() => {
    return creditDetail.value?.creditLimit ?? userStore.userInfo?.creditPaymentInfo?.creditLimit ?? "-";
});
const availableAmountText = computed(() => {
    return creditDetail.value?.availableAmount ?? userStore.userInfo?.creditPaymentInfo?.availableAmount ?? "-";
});
const settlementDaysText = computed(() => {
    const days = creditDetail.value?.settlementDays;
    return typeof days === "number" ? `${days}${t("天")}` : days ? `${days}${t("天")}` : "-";
});
const remainingDaysText = computed(() => {
    const days = creditDetail.value?.remainingDays;
    return typeof days === "number" ? `${days}${t("天")}` : days ? `${days}${t("天")}` : "-";
});

const getSettlementStatusStyle = (order: UserCreditOrderItem) => {
    // 0-未结算 1-已结算 2-已取消 3-退款中；riskStatus=1(逾期) 标红
    if (order.riskStatus === 1) return { color: "#ee0a24" };
};

const formatSkuValue = (val?: string) => {
    if (!val) return "";
    return val.replaceAll("|", " ");
};

const normalizeOfflinePaymentList = (cfg: any): OfflinePaymentList => {
    const source = cfg?.offlinePaymentList ?? cfg ?? {};
    return {
        thirdPartyPaymentTransfer: source?.thirdPartyPaymentTransfer ?? "",
        offlinePayBank: source?.offlinePayBank ?? "",
        offlinePayCompany: source?.offlinePayCompany ?? "",
        otherPaymentRemittance: source?.otherPaymentRemittance ?? ""
    };
};

const loadOfflinePaymentList = async () => {
    // 已经有内容就不重复拉取
    const hasAny =
        !!offlinePaymentList.value?.thirdPartyPaymentTransfer ||
        !!offlinePaymentList.value?.offlinePayBank ||
        !!offlinePaymentList.value?.offlinePayCompany ||
        !!offlinePaymentList.value?.otherPaymentRemittance;
    if (hasAny) return;

    try {
        const termResult = await orderPayInfoTerm();
        offlinePaymentList.value = normalizeOfflinePaymentList(termResult.offlinePaymentList);
    } catch {
        // 静默失败：仍然打开弹窗（内容可能为空）
        offlinePaymentList.value = normalizeOfflinePaymentList({});
    }
};

const handleOpenRemittance = async () => {
    lockedScrollTop.value = currentScrollTop.value;
    showRemittancePopup.value = true;
    await loadOfflinePaymentList();
};

// H5：弹窗打开时锁定 body 滚动，避免外层列表滚动（仅本页生效）
watch(
    () => showRemittancePopup.value,
    (val) => {
        // #ifdef H5
        if (typeof document !== "undefined") {
            document.body.style.overflow = val ? "hidden" : "";
        }
        // #endif
        // 弹窗关闭后恢复页面滚动位置
        if (!val) {
            nextTick(() => {
                uni.pageScrollTo({ scrollTop: lockedScrollTop.value, duration: 0 });
            });
        }
    }
);

onPageScroll((e) => {
    // 仅在未锁定时记录 scrollTop（锁定后不更新，避免跳动）
    if (!showRemittancePopup.value) {
        currentScrollTop.value = e.scrollTop || 0;
    }
});

// 获取设备信息，用于计算实际像素
const systemInfo = uni.getSystemInfoSync();
const windowWidth = systemInfo.windowWidth || 375;

// rpx 转 px 的转换比例（750rpx = 屏幕宽度）
const rpxToPx = windowWidth / 750;

// Canvas 尺寸（rpx）
const canvasSizeRpx = 200;
// Canvas 实际像素尺寸
const canvasSizePx = Math.round(canvasSizeRpx * rpxToPx);

// 获取主题色（从 themeStore 中获取）
const getGeneralColor = (name: string): string => {
    // 从 themeStore 中获取 --general 变量值
    const generalColor = themeStore.themeStyle[name];
    if (generalColor) {
        return generalColor;
    }
    // 如果 themeStore 中没有，使用默认主题色
    return "#007aff";
};

// 绘制圆环图表
const drawCircle = () => {
    const creditInfo = creditDetail.value || userStore.userInfo?.creditPaymentInfo;
    if (!creditInfo) return;

    // 延迟执行，确保 canvas 已渲染
    setTimeout(() => {
        const ctx = uni.createCanvasContext("creditChart");
        // 使用实际像素尺寸的一半作为中心点
        const centerX = canvasSizePx / 2; // 画布中心 X 坐标
        const centerY = canvasSizePx / 2; // 画布中心 Y 坐标
        const radius = 80 * rpxToPx; // 圆环半径（转换为实际像素）
        const lineWidth = 25 * rpxToPx; // 圆环宽度（转换为实际像素）

        // 获取主题色
        const generalColor = getGeneralColor("--general");
        // 获取背景色
        const mainBgColor = getGeneralColor("--primary-light-9");

        // 计算比例（百分比转小数）
        const creditLimit = Number((creditInfo as any).creditLimit) || 0;
        const usedAmount = Number((creditInfo as any).usedAmount) || 0;
        const availableAmount = Number((creditInfo as any).availableAmount) || 0;

        // 已使用比例（0-1之间）
        const usedPercent = creditLimit > 0 ? usedAmount / creditLimit : 0;
        // 剩余比例（0-1之间）
        const availablePercent = creditLimit > 0 ? availableAmount / creditLimit : 0;
        // 确保总和不超过 1
        const totalPercent = Math.min(usedPercent + availablePercent, 1);

        console.log("数据:", { creditLimit, usedAmount, availableAmount, usedPercent, availablePercent, totalPercent });

        // 起始角度（从顶部开始，12点钟方向）
        const startAngle = -Math.PI / 2;
        // 已使用角度（从顶部开始，顺时针）
        const usedAngle = startAngle + usedPercent * Math.PI * 2;

        // 绘制背景圆环（灰色，完整圆形）
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, 0, Math.PI * 2, false);
        ctx.setStrokeStyle(mainBgColor);
        ctx.setLineWidth(lineWidth);
        ctx.stroke();

        // 绘制已使用部分（使用主题色），从顶部开始
        if (usedPercent > 0) {
            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, startAngle, usedAngle, false);
            ctx.setStrokeStyle(generalColor);
            ctx.setLineWidth(lineWidth);
            // ctx.setLineCap("round");
            ctx.stroke();
        }

        // 绘制到 canvas，false 表示清除之前的绘制
        ctx.draw(false, () => {
            // 绘制完成后再次确认绘制，确保显示
            setTimeout(() => {
                ctx.draw(true);
            }, 50);
        });
    }, 300);
};

// 监听 creditPaymentInfo 变化，重新绘制
watch(
    () => [userStore.userInfo?.creditPaymentInfo, creditDetail.value],
    () => {
        if (showTermPay.value) {
            nextTick(() => {
                drawCircle();
            });
        }
    },
    { deep: true, immediate: false }
);

const fetchCreditDetail = async () => {
    isLoading.value = true;
    try {
        creditDetail.value = await getUserCreditDetail();
        nextTick(() => drawCircle());
    } catch (error: any) {
        uni.showToast({
            title: error?.message || t("加载失败"),
            icon: "none"
        });
    } finally {
        isLoading.value = false;
    }
};

const goOrderDetail = (orderId: string | number) => {
    if (orderId === undefined || orderId === null || orderId === "") return;
    uni.navigateTo({
        url: `/pages/user/order/info?id=${orderId}`
    });
};

onLoad(() => {
    fetchCreditDetail();
});

onMounted(() => {
    if (showTermPay.value) {
        nextTick(() => {
            drawCircle();
        });
    }
});
</script>

<style scoped lang="scss">
uni-page-body {
    height: 100%;
}
.termDetail {
    padding: 12rpx 20rpx;
    height: 100%;

    .header {
        background: #fff;
        display: flex;
        flex-direction: column;
        align-items: center;
        padding-bottom: 40rpx;

        .chart {
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 40rpx 0;

            .chart-canvas {
                display: block;
            }

            .chart-info {
                margin-top: 20rpx;
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 20rpx;

                .chart-text {
                    font-size: 28rpx;
                    color: #f1be00;
                }

                .creditLimit {
                    color: var(--general);
                }
            }
        }

        .termPayDays {
            width: 100%;
            padding: 0 70rpx;
            display: flex;
            justify-content: space-between;

            .days {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 20rpx;
                color: #f1be00;
            }

            .totalDays {
                color: var(--general);
            }
        }
    }

    .term-menu {
        margin-top: 20rpx;
        border-radius: 12rpx;
    }

    .term-order-list {
        padding: 20rpx 0 30rpx;
        // min-height: 60vh;
        display: flex;
        flex-direction: column;
        flex: 1;

        .term-order-empty {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            .empty-imgBox {
                display: flex;
                flex-direction: column;
                align-items: center;
                .empty-text {
                    font-size: 24rpx;
                    color: #9a9a9a;
                    transform: translateY(-20rpx);
                }
            }
        }

        .term-order-card {
            background: #fff;
            border-radius: 12rpx;
            padding: 20rpx;
            margin-bottom: 20rpx;

            &.term-order-card--hover {
                background: #fafafa;
            }

            &:last-child {
                margin-bottom: 0;
            }

            .term-order-card-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding-bottom: 15rpx;
                font-size: 24rpx;
                color: #8c8c8c;

                .status {
                    font-weight: 500;
                }
            }

            .term-order-card-goods {
                .good-item {
                    display: flex;
                    padding: 15rpx 0;

                    &:first-child {
                        padding-top: 10rpx;
                    }

                    &:last-child {
                        padding-bottom: 10rpx;
                    }

                    .good-img {
                        width: 140rpx;
                        height: 140rpx;
                        overflow: hidden;
                    }

                    .good-info {
                        flex: 1;
                        padding-left: 20rpx;
                        display: flex;
                        flex-direction: column;
                        //justify-content: space-between;
                        overflow: hidden;
                        gap: 20rpx;

                        .good-name {
                            font-size: 28rpx;
                            color: #262626;
                            display: flex;
                            justify-content: space-between;
                            gap: 15rpx;
                            .goodPrice {
                                flex-shrink: 0;
                            }
                        }

                        .good-qty {
                            font-size: 24rpx;
                            color: #8c8c8c;
                            display: flex;
                            justify-content: space-between;
                            //text-align: right;

                            .sku-value {
                                flex: 1;
                                min-width: 0;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                            }

                            .qty {
                                margin-left: auto;
                                text-align: right;
                                flex-shrink: 0;
                            }
                        }
                    }
                }
            }

            .term-order-card-footer {
                display: flex;
                justify-content: flex-end;
                align-items: center;
                gap: 18rpx;
                padding-top: 10rpx;
                font-size: 24rpx;
                color: #8c8c8c;

                .amount {
                    :deep(.price-content) {
                        color: var(--general);
                    }
                }
            }
        }
    }

    .remit-popup {
        background: #fff;
        height: 80vh;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .remit-popup__header {
            padding: 26rpx 24rpx 12rpx;
            border-bottom: 1px solid #f5f5f5;
        }

        .remit-popup__title {
            text-align: center;
            font-size: 30rpx;
            font-weight: 600;
            color: #1a1a1a;
        }

        .remit-popup__content {
            flex: 1;
            overflow: hidden;
            min-height: 0; // 关键：flex 子元素允许滚动（否则部分端会撑开不滚动）
            box-sizing: border-box;
        }
    }

    /* 只滚动 offline.vue 内部的内容区（33-54 的 .offline-content），且超出才滚动 */
    :deep(.offline-warp) {
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        margin: 0; /* 弹窗内不需要外边距，避免撑高 */
        padding: 0;
        background-color: transparent;
    }
    :deep(.offline-menu) {
        flex: 0 0 auto;
    }
    :deep(.offline-content) {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        -webkit-overflow-scrolling: touch;
    }
}

.term-detail-layout {
    display: flex;
    flex-direction: column;
    min-height: 100%;
}

.term-footer {
    padding: 21rpx 28rpx;
}
</style>
