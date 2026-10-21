/**
 * @file 店铺账单详情页
 * @description 展示店铺财务汇总详情和收支明细
 * @features
 * - 显示账单周期和店铺信息
 * - 期初/期末余额对比
 * - 收入/支出分类统计
 * - 下载财务报表
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <div class="download-info container-card">
                <div class="info-row">
                    <div class="title">{{ $t('2025年06月财务汇总详情') }}</div>
                    <div class="info">
                        <div class="item">
                            <div class="label">{{ $t('店铺名称：') }}</div>
                            <div class="value">{{ $t('xxxx店铺') }}</div>
                        </div>
                        <div class="item">
                            <div class="label">{{ $t('账单周期：') }}</div>
                            <div class="value">{{ $t('2025-06-01 00:00:00 至 2025-06-30 23:59:59') }}</div>
                        </div>
                    </div>
                </div>
                <el-button type="primary">{{ $t('下载报表') }}</el-button>
            </div>
            <div class="download-detail container-card">
                <div class="num-row flex align-center">
                    <div class="num-item">
                        <div class="tit">{{ $t('本期期初余额') }}({{ config.config.dollarSignCn }})</div>
                        <div class="num">255.55</div>
                    </div>
                    <div class="num-item">
                        <div class="tit">{{ $t('本期收入') }}({{ config.config.dollarSignCn }})</div>
                        <div class="num">232.00</div>
                    </div>
                    <div class="num-item">
                        <div class="tit">{{ $t('本期支出') }}({{ config.config.dollarSignCn }})</div>
                        <div class="num">2.55</div>
                    </div>
                    <div class="num-item">
                        <div class="tit">{{ $t('本期期末余额') }}({{ config.config.dollarSignCn }})</div>
                        <div class="num">34123132.00</div>
                    </div>
                </div>
                <div class="tit-box">
                    <div class="tit">
                        <span>{{ $t('本期收入') }}</span>
                    </div>
                </div>
                <div class="num-list">
                    <el-row>
                        <el-col :span="14">
                            <div class="head-col">{{ $t('收入类型') }}</div>
                        </el-col>
                        <el-col :span="5">
                            <div class="head-col text-end">{{ $t('金额') }}({{ config.config.dollarSignCn }})/笔数</div>
                        </el-col>
                        <el-col :span="5">
                            <div class="head-col text-center">{{ $t('明细') }}</div>
                        </el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="14">
                            <div class="info-col">{{ $t('合计') }}</div>
                        </el-col>
                        <el-col :span="5">
                            <div class="info-col text-end">
                                <p>0.00</p>
                                <p>{{ $t('0笔') }}</p>
                            </div>
                        </el-col>
                        <el-col :span="5">
                            <div class="info-col text-center">--</div>
                        </el-col>
                    </el-row>
                </div>
                <div class="tit-box">
                    <div class="tit">
                        <span>{{ $t('本期支出') }}</span>
                    </div>
                </div>
                <div class="num-list">
                    <el-row>
                        <el-col :span="14">
                            <div class="head-col">{{ $t('支出类型') }}</div>
                        </el-col>
                        <el-col :span="5">
                            <div class="head-col text-end">{{ $t('金额') }}({{ config.config.dollarSignCn }})/笔数</div>
                        </el-col>
                        <el-col :span="5">
                            <div class="head-col text-center">{{ $t('明细') }}</div>
                        </el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="14">
                            <div class="info-col">{{ $t('合计') }}</div>
                        </el-col>
                        <el-col :span="5">
                            <div class="info-col text-end">
                                <p>0.00</p>
                                <p>{{ $t('0笔') }}</p>
                            </div>
                        </el-col>
                        <el-col :span="5">
                            <div class="info-col text-center">--</div>
                        </el-col>
                    </el-row>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { ref } from "vue";
import { useConfigStore } from "@/store/config";
import type { StatementStatisticsRequest, StatementStatisticsResponse } from "@/types/finance/statementDownload.d";
import { getStatementStatisticsList } from "@/api/finance/statementDownload";
import { message } from "ant-design-vue";
import { formatDate } from "@/utils/util";
const config: any = useConfigStore();
const loading = ref(false);
const filterState = ref<StatementStatisticsResponse[]>([]);
const nowDate = formatDate(new Date());
const dates = ref(nowDate ? nowDate.split("-") : "");
const filterParams = ref<StatementStatisticsRequest>({
    statementDate: `${dates.value[0]}-${dates.value[1]}`,
    statementDateType: "day",
    accountType: ""
});
const loadFilter = async () => {
    try {
        const result = await getStatementStatisticsList({ ...filterParams.value });
        console.log(result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// loadFilter();
</script>
<style scoped lang="less">
.container-card {
    border: 1px solid #ececec;
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;
}
.tit-box {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 10px;
    margin-bottom: 20px;
    background-color: #f7f7f7;
    .tit {
        border-left: 3px solid #155bd4;
        padding-left: 10px;
        font-size: 14px;
    }
}
.download-info {
    .info-row {
        background-color: #f7f8fa;
        margin-bottom: 20px;
        padding: 20px 60px 20px 20px;
        .title {
            font-size: 18px;
            font-weight: bolder;
            line-height: 18px;
            margin-bottom: 20px;
        }
        .info {
            display: flex;
            align-items: center;
            justify-content: space-between;
            .item {
                display: flex;
                align-items: center;
                .label {
                    color: #969799;
                }
            }
        }
    }
}
.download-detail {
    .num-row {
        background-color: #f7f8fa;
        margin-bottom: 20px;
        padding: 20px 10%;
        .num-item {
            width: 25%;
            .tit {
                margin-bottom: 20px;
            }
            .num {
                font-size: 18px;
                font-weight: bolder;
            }
        }
    }
    .num-list {
        .head-col {
            background-color: #f7f7f7;
            padding: 15px 20px;
            border-bottom: 1px solid #e0e0e0;
        }
        .info-col {
            padding: 0 20px;
            border-bottom: 1px solid #e0e0e0;
            height: 70px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            p {
                line-height: 20px;
            }
        }
        .text-end {
            text-align: end;
        }
        .text-center {
            text-align: center;
        }
    }
}
</style>
