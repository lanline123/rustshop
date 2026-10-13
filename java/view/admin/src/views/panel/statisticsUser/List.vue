<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="notice-warp">{{ $t('提示：查询的时间间隔上限默认为一年，如果数据量过大导至查询时间过久或者超时报错，请尝试缩小查询的时间间隔！') }}</div>
            <el-form :model="filterParams">
                <div class="filtrate-menu">
                    <el-form-item class="mr-10">
                        <SelectTimeInterval
                            type="date"
                            v-model:start-date="filterParams.startTime"
                            v-model:end-date="filterParams.endTime"
                        ></SelectTimeInterval>
                    </el-form-item>
                    <el-form-item class="mr-10">
                        <el-button plain @click="handleSearch">{{ $t('搜索') }}</el-button>
                    </el-form-item>
                    <el-form-item class="mr-10">
                        <el-button plain @click="handleExport" :loading="Exportloading">{{ $t('导出EXCEL') }}</el-button>
                    </el-form-item>
                </div>
            </el-form>

            <div class="main-panel">
                <ul>
                    <li class="main-panel-item">
                        <div class="main-panel-item-title">
                            <div class="flex flex-align-center"><div>{{ $t('商城访客数') }}</div>
                            <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover" :teleported="true">
                                <template #content>
                                    <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px">{{ $t('统计在一段时间内访问商城的客户数量') }}</div>
                                </template>
                                <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                            </el-tooltip>
                            </div>
                        </div>
                        <div class="main-panel-item-value">{{ filterState?.visitNum }}</div>
                        <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.visitGrowthRate }}% </span>

                            <i v-if="filterState.visitGrowthRate.toString() !== '--' && filterState.visitGrowthRate > 0" class="admin-iconfont red f12 up"
                                >&#xe61a;</i
                            >

                            <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                        </div>
                    </li>
                    <li class="main-panel-item">
                        <div class="main-panel-item-title">
                            <div class="flex flex-align-center"><div>{{ $t('浏览量') }}</div>
                            <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover" :teleported="true">
                                <template #content>
                                    <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('根据筛选时间，商城页面被浏览的次数。每打开一个页面或每刷新一次页面都记录1次 （按访问时间）') }} </div>
                                </template>
                                <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                            </el-tooltip>
                            </div>
                        </div>
                        <div class="main-panel-item-value">{{ filterState?.viewNum }}</div>
                        <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.viewGrowthRate }}%</span>
                            <i v-if="filterState.viewGrowthRate.toString() !== '--' && filterState.viewGrowthRate > 0" class="admin-iconfont red f12 up"
                                >&#xe61a;</i
                            >
                            <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                        </div>
                    </li>
                    <li class="main-panel-item">
                        <div class="main-panel-item-title">
                            <div class="flex flex-align-center"><div>{{ $t('新增用户数') }}</div>
                            <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover" :teleported="true">
                                <template #content>
                                    <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px">{{ $t('根据筛选时间，平台中的新增客户数') }}</div>
                                </template>
                                <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                            </el-tooltip>
                            </div>
                        </div>
                        <div class="main-panel-item-value">{{ filterState?.addUserNum }}</div>
                        <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.addUserGrowthRate }}% </span>
                            <i v-if="filterState.addUserGrowthRate.toString() !== '--' && filterState.addUserGrowthRate > 0" class="admin-iconfont red f12 up"
                                >&#xe61a;</i
                            >
                            <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                        </div>
                    </li>
                    <li class="main-panel-item">
                        <div class="main-panel-item-title">
                            <div class="flex flex-align-center"><div>{{ $t('成交用户数') }}</div>
                            <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover" :teleported="true">
                                <template #content>
                                    <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px">{{ $t('根据筛选时间，平台中的成交客户数') }}</div>
                                </template>
                                <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                            </el-tooltip>
                            </div>
                        </div>
                        <div class="main-panel-item-value">{{ filterState?.dealUserNum }}</div>
                        <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.dealUserGrowthRate }}% </span>
                            <i
                                v-if="(filterState.dealUserGrowthRate as string).toString() !== '--' && (filterState.dealUserGrowthRate as number) > 0"
                                class="admin-iconfont red f12 up"
                                >&#xe61a;</i
                            >
                            <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                        </div>
                    </li>
                    <li class="main-panel-item">
                        <div class="main-panel-item-title">
                            <div class="flex flex-align-center"><div>{{ $t('访客-支付转化率') }}</div>
                            <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover" :teleported="true">
                                <template #content>
                                    <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('访客支付转化率 =（支付买家数 ÷ 访客数）× 100%。例如，某商品当日访客100人，其中1人付款，转化率为1%') }} </div>
                                </template>
                                <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                            </el-tooltip>
                            </div>
                        </div>
                        <div class="main-panel-item-value">{{ filterState?.visitToUser }}</div>
                        <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.visitToUserRate }}%</span>
                            <i v-if="filterState.visitToUserRate.toString() !== '--' && filterState.visitToUserRate > 0" class="admin-iconfont red f12 up"
                                >&#xe61a;</i
                            >
                            <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                        </div>
                    </li>
                    <li class="main-panel-item">
                        <div class="main-panel-item-title">
                            <div class="flex flex-align-center"><div>{{ $t('充值用户数') }}</div>
                            <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover" :teleported="true">
                                <template #content>
                                    <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('根据筛选时间，统计在系统中充值的客户数量（多次充值记为一次）') }} </div>
                                </template>
                                <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                            </el-tooltip>
                            </div>
                        </div>
                        <div class="main-panel-item-value">{{ filterState?.rechargeUserNum }}</div>
                        <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.rechargeUserGrowthRate }}% </span>
                            <i
                                v-if="(filterState.rechargeUserGrowthRate as string).toString() !== '--' && (filterState.rechargeUserGrowthRate as number) > 0"
                                class="admin-iconfont red f12 up"
                                >&#xe61a;</i
                            >
                            <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { QuestionFilled } from "@element-plus/icons-vue";
import { onMounted, reactive, ref } from "vue";
import { SelectTimeInterval } from "@/components/select";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import { getUserStatisticsPanel, exportUserStatisticsPanel } from "@/api/panel/statisticsUser";
import { statisticsUserFilterParams, FilterResult } from "@/types/panel/statisticsUser.d";
import { message } from "ant-design-vue";
import requestExport from "@/utils/export";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref(false);
const filterParams = reactive<statisticsUserFilterParams>({
    startTime: formattedDate(getDays(30, "sub"), "YYYY-MM-DD"),
    endTime: formattedDate(new Date(), "YYYY-MM-DD")
});

const filterState = ref<FilterResult>({
    visitNum: 0,
    visitGrowthRate: 0,
    viewNum: 0,
    viewGrowthRate: 0,
    addUserNum: 0,
    addUserGrowthRate: 0,
    dealUserNum: 0,
    dealUserGrowthRate: "0",
    visitToUser: 0,
    visitToUserRate: 0,
    rechargeUserNum: 0,
    rechargeUserGrowthRate: "0"
});

const getData = async () => {
    try {
        const result = await getUserStatisticsPanel(filterParams);
        filterState.value = result;
    } catch (error: any) {
        message.error(error.message);
        console.error(error);
    }
};

const handleSearch = () => {
    getData();
};

const Exportloading = ref<boolean>(false);
const handleExport = async () => {
    Exportloading.value = true;
    try {
        const result = await exportUserStatisticsPanel({ ...filterParams, isExport: "1" });
        Exportloading.value = false;
        requestExport(result, t('用户统计导出'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        Exportloading.value = false;
    }
};

onMounted(() => {
    getData();
});
</script>

<style lang="less" scoped>
.filtrate-menu {
    display: flex;
    .mr-10 {
        margin-right: 5px;
    }
}

.main-panel {
    ul {
        display: grid;
        grid-template-columns: 1fr 1fr 1fr 1fr;
        grid-gap: 17px 28px;
    }

    .main-panel-item {
        padding: 15px;
        border-radius: 10px;
        background-color: #f7f8fa;

        .main-panel-item-title {
            font-size: 14px;
            color: #777;

            // 让提示组件只占用标题和图标的实际宽度，避免按整张卡片居中定位
            .box-item {
                display: inline-flex;
                width: fit-content;
            }

            .tooltip-icon {
                margin-left: 5px;
            }
        }

        .main-panel-item-value {
            font-size: 24px;
            color: #333;
            display: block;
            font-weight: bold;
            padding: 10px 0;
        }

        .main-panel-item-increase {
            font-size: 14px;
            color: #777;
            i {
                font-size: 16px;
            }

            .up {
                display: inline-block;
                transform: rotate(180deg);
            }
        }
    }
}
</style>
