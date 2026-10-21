/** * @file 保证金调整记录时间线 * @description 详情-调整记录 tab 与 调整弹窗-调整记录 tab 共用 */
<template>
    <a-spin class="adjust-log-timeline" :spinning="loading">
        <perfect-scrollbar
            v-if="logList.length"
            class="timeline-scroll"
            v-infinite-scroll="loadMore"
            :infinite-scroll-disabled="!hasMore || loadingMore || loading"
            :infinite-scroll-distance="80"
            @ps-y-reach-end="loadMore"
        >
            <div class="timeline">
                <div v-for="item in logList" :key="item.shopDepositAdjustId" class="timeline-item">
                    <div class="timeline-date">
                        <span class="date">{{ formatDay(item.addTime) }}</span>
                        <span class="week">{{ formatWeek(item.addTime) }}</span>
                    </div>
                    <div class="timeline-axis">
                        <span class="dot"></span>
                        <span class="line"></span>
                    </div>
                    <div class="timeline-content">
                        <div class="content-head">
                            <span class="time">{{ formatTime(item.addTime) }}</span>
                            <div class="operator-wrap">
                                <el-avatar
                                    :size="16"
                                    class="avatar"
                                    :class="{ 'is-system': !item.adminId }"
                                    :src="resolveAvatar(item)"
                                    fit="contain"
                                />
                                <el-tooltip :content="item.adminName || ''" placement="top" :disabled="!item.adminName" effect="light">
                                    <span class="operator">{{ item.adminName }}</span>
                                </el-tooltip>
                            </div>
                            <div class="action-wrap">
                                <div class="action-line">
                                    <span class="action">{{ item.sourceText }}</span>
                                    <span v-if="isCreate(item)" class="amount">{{ priceFormat(item.afterRequired) }}</span>
                                </div>
                                <div v-if="!isCreate(item)" class="content-detail">
                                    <div class="change">{{ $t('原额度{priceFormat}', { priceFormat: priceFormat(item.beforeRequired) }) }}<span class="trend">{{ Number(item.afterRequired) >= Number(item.beforeRequired) ? $t('上调至') : $t('下调至') }}</span>
                                        {{ priceFormat(item.afterRequired) }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div v-if="loadingMore" class="load-more-tip">{{ $t('加载中...') }}</div>
        </perfect-scrollbar>
        <div v-else-if="!loading" class="empty-warp"><div class="empty-bg">{{ $t('暂无调整记录') }}</div></div>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useLicensedStore } from "@/store/licensed";
import { avatarFormat, imageFormat, priceFormat } from "@/utils/format";
import { returnAvatar } from "@/utils/avatar";
import { formattedDate } from "@/utils/time";
import { getDepositAdjustLog } from "@/api/finance/deposit";
import type { AdjustLogState } from "@/types/finance/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    ownerType: { type: Number, default: undefined },
    ownerId: { type: Number, default: undefined }
});

const config: any = useConfigStore();
const licensedStore = useLicensedStore();
const platformLogo = computed(() => {
    const logo = licensedStore.licensedData?.adminLightLogo;
    return logo ? imageFormat(logo) : returnAvatar(0);
});
const loading = ref<boolean>(false);
const loadingMore = ref<boolean>(false);
const logList = ref<AdjustLogState[]>([]);
const total = ref<number>(0);
const page = ref<number>(1);
const size = ref<number>(config.get("pageSize"));

// 是否还有下一页
const hasMore = computed<boolean>(() => logList.value.length < total.value);

const WEEK_MAP = [t('星期日'), t('星期一'), t('星期二'), t('星期三'), t('星期四'), t('星期五'), t('星期六')];

const formatDay = (time?: string) => (time ? formattedDate(new Date(time), "MM-DD") : "");
const formatTime = (time?: string) => (time ? formattedDate(new Date(time), "HH:mm") : "");
const formatWeek = (time?: string) => (time ? WEEK_MAP[new Date(time).getDay()] : "");

// 无调整前金额视为创建店铺记录
const isCreate = (item: AdjustLogState) => item.beforeRequired === undefined || item.beforeRequired === null;

// 系统调整（无操作管理员，如类目变更自动触发）固定使用后台配置的平台 logo（adminLightLogo）
const resolveAvatar = (item: AdjustLogState) => {
    if (!item.adminId) return platformLogo.value;
    return item.adminAvatar ? avatarFormat(item.adminAvatar) : returnAvatar(0);
};

const fetchData = async () => {
    if (!props.ownerType || !props.ownerId) return;
    try {
        const result = await getDepositAdjustLog({
            ownerType: props.ownerType,
            ownerId: props.ownerId,
            page: page.value,
            size: size.value
        });
        // 第一页重置列表，后续页追加
        logList.value = page.value === 1 ? result.records : logList.value.concat(result.records);
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    }
};

// 重置并加载第一页（供父组件刷新调用）
const loadData = async () => {
    page.value = 1;
    total.value = 0;
    logList.value = [];
    loading.value = true;
    try {
        await fetchData();
    } finally {
        loading.value = false;
    }
};

// 上拉加载下一页
const loadMore = async () => {
    if (loading.value || loadingMore.value || !hasMore.value) return;
    loadingMore.value = true;
    try {
        page.value += 1;
        await fetchData();
    } finally {
        loadingMore.value = false;
    }
};

defineExpose({ loadData });

onMounted(() => {
    loadData();
});
</script>

<style lang="less" scoped>
@axis-color: #1456f0;

.adjust-log-timeline {
    padding: 10px 6px;
}

.timeline-scroll {
    height: calc(100vh - 340px);
    min-height: 280px;
    overflow-x: hidden;
}

.load-more-tip {
    padding: 8px 0;
    text-align: center;
    font-size: 12px;
    color: #909399;
}

.timeline {
    .timeline-item {
        display: grid;
        grid-template-columns: 90px 24px 1fr;
        column-gap: 10px;
        align-items: flex-start;

        // 末条记录不再向下延伸连线
        &:last-child .timeline-axis .line {
            display: none;
        }
    }

    .timeline-date {
        grid-column: 1;
        padding-top: 4px;
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #333;
        line-height: 22px;
    }

    .timeline-axis {
        grid-column: 2;
        align-self: stretch;
        display: flex;
        flex-direction: column;
        align-items: center;

        .dot {
            position: relative;
            flex-shrink: 0;
            width: 14px;
            height: 14px;
            margin-top: 8px;
            border-radius: 50%;
            border: 1px solid @axis-color;
            background-color: #fff;
            box-sizing: border-box;

            &::after {
                content: "";
                position: absolute;
                width: 6px;
                height: 6px;
                border-radius: 50%;
                background-color: @axis-color;
                left: 50%;
                top: 50%;
                transform: translate(-50%, -50%);
            }
        }

        .line {
            flex: 1;
            width: 2px;
            min-height: 24px;
            margin-top: 6px;
            background-color: @axis-color;
        }
    }

    .timeline-content {
        grid-column: 3;
        padding: 4px 0 22px 0;

        .content-head {
            display: grid;
            grid-template-columns: 45px max-content 1fr;
            column-gap: 10px;
            align-items: start;
            line-height: 22px;
            font-size: 13px;

            .time {
                grid-column: 1;
            }
            .operator-wrap {
                grid-column: 2;
                display: flex;
                align-items: center;
                gap: 4px;

                .avatar.is-system {
                    background-color: var(--el-color-primary);
                }
            }
            .operator {
                display: inline-block;
                max-width: 120px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                vertical-align: middle;
                color: #a4adb3;
            }
            .amount {
                color: @axis-color;
                font-weight: 600;
            }

            .action-wrap {
                grid-column: 3;
                min-width: 0;

                .action-line {
                    display: flex;
                    align-items: center;
                    flex-wrap: nowrap;
                    white-space: nowrap;
                    gap: 6px;
                }

                .content-detail {
                    margin-top: 6px;
                    background-color: #f5f7fa;
                    border-radius: 3px;
                    padding: 4px 5px;
                    font-size: 13px;
                    line-height: 1.6;
                    white-space: nowrap;

                    .trend {
                        margin: 0 4px;
                    }
                    .reason {
                        margin-top: 4px;
                    }
                }
            }
        }
    }
}
</style>
