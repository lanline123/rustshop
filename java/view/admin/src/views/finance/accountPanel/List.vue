/**
 * @file 账户面板统计页
 * @description 展示会员账户资金和积分统计信息
 * @features
 * - 会员账户总余额/待结算余额/可用余额统计
 * - 会员积分总量/消费积分/可用积分统计
 * - 支持按时间范围筛选
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <SelectTimeInterval v-model:end-date="filterParams.searchEndDate"
                                                v-model:start-date="filterParams.searchStartDate" :clearable="false"
                                                type="date" value-format="YYYY-MM-DD"></SelectTimeInterval>
                                        </div>
                                    </div>
                                    <div class="simple-form-field">
                                        <el-button type="primary" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                    </div>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <div class="tit-box">
                            <div class="xian"></div>
                            <div class="title">{{ $t('会员账户信息') }}</div>
                        </div>
                        <div class="content">
                            <div class="label">
                                <div class="txt">{{ $t('会员充值总额') }}</div>
                                <div class="num">
                                    {{ priceFormat(filterState.voucherAmount) }}
                                </div>
                            </div>
                            <div class="label">
                                <div class="txt">{{ $t('提现金额') }}</div>
                                <div class="num">
                                    {{ priceFormat(filterState.toCashAmount) }}
                                </div>
                            </div>
                            <div class="label">
                                <div class="txt">{{ $t('会员余额变化金额') }}</div>
                                <div class="num">
                                    {{ priceFormat(filterState.balance) }}
                                </div>
                            </div>
                            <div class="label">
                                <div class="txt">{{ $t('会员冻结变化金额') }}</div>
                                <div class="num">
                                    {{ priceFormat(filterState.frozenMoney) }}
                                </div>
                            </div>
                        </div>
                        <div class="tit-box">
                            <div class="xian"></div>
                            <div class="title">{{ $t('余额使用信息') }}</div>
                        </div>
                        <div class="content">
                            <div class="label">
                                <div class="txt">{{ $t('交易使用余额') }}</div>
                                <div class="num">
                                    {{ priceFormat(filterState.surplus) }}
                                </div>
                            </div>
                            <div class="label">
                                <div class="txt">{{ $t('交易使用积分') }}</div>
                                <div class="num">
                                    {{ priceFormat(filterState.usePoints) }}
                                </div>
                            </div>
                        </div>
                    </a-spin>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import type { AccountPanelFilterParams, AccountPanelFilterState } from "@/types/finance/accountPanel.d";
import { getAccountPanelList } from "@/api/finance/accountPanel";
import dayjs from "dayjs";
import { priceFormat } from "@/utils/format";
import { SelectTimeInterval } from "@/components/select";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";

const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<AccountPanelFilterState>({
    voucherAmount: 0,
    toCashAmount: 0,
    balance: 0,
    frozenMoney: 0,
    surplus: 0,
    usePoints: 0
});
const loading = ref<boolean>(true);
const filterParams = reactive<AccountPanelFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    searchStartDate: formattedDate(getDays(7, "sub"), "YYYY-MM-DD"),
    searchEndDate: formattedDate(new Date(), "YYYY-MM-DD")
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAccountPanelList({ ...filterParams });
        filterState.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
</script>
<style lang="less" scoped>
.list-table-tool .list-table-tool-row,
.list-table-tool {
    margin-bottom: 0;
}

.table-container {
    .tit-box {
        padding: 10px 0;
        display: flex;
        align-items: center;
        margin-bottom: 15px;

        .xian {
            width: 3px;
            height: 13px;
            background-color: #165bd4;
            margin-right: 5px;
        }
    }

    .content {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        background-color: #f7f8fa;
        padding: 20px;
        margin-bottom: 30px;
        gap: 40px;

        .label {
            min-width: 260px;

            .txt {
                font-size: 15px;
                margin-bottom: 15px;
            }

            .num {
                font-size: 30px;
            }
        }
    }
}
</style>
