<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('订单信息：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('订单号/会员名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('分销员：') }}</span></label>
                                    <div class="control-container">
                                        <el-select
                                            v-model="filterParams.salesmanId"
                                            filterable
                                            remote
                                            reserve-keyword
                                            :placeholder="$t('输入分销员昵称查询订单')"
                                            :remote-method="getSalesnamList"
                                            @change="onSearchSubmit"
                                            :loading="loadingSalesnam"
                                        >
                                            <el-option v-for="item in salesnamList" :key="item.key" :label="item.label" :value="item.value" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('下单时间：') }}</span></label>
                                    <SelectTimeInterval
                                        v-model:end-date="filterParams.endTime"
                                        v-model:start-date="filterParams.startTime"
                                        :clearable="false"
                                        type="date"
                                        value-format="YYYY-MM-DD"
                                    ></SelectTimeInterval>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <el-button @click="handleExport" :loading="Exportloading"> {{ $t('导出') }} </el-button>
                        </el-space>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <table class="custom-table">
                            <thead>
                                <tr>
                                    <th>
                                        <SortButton
                                            v-model:sortField="filterParams.sortField"
                                            v-model:sortOrder="filterParams.sortOrder"
                                            sortName="addTime"
                                            :text="$t('订单信息(下单时间排序)')"
                                            @loadData="onSearchSubmit()"
                                        ></SortButton>
                                    </th>
                                    <th>{{ $t('所属分销员') }}</th>
                                    <th>{{ $t('客户名称') }}</th>
                                    <th>{{ $t('收货人信息') }}</th>
                                    <th style="text-align: start">{{ $t('收货时间') }}</th>
                                    <th style="text-align: start">{{ $t('物流/支付信息') }}</th>
                                    <th style="text-align: start">{{ $t('订单状态') }}</th>
                                    <th>
                                        <SortButton
                                            v-model:sortField="filterParams.sortField"
                                            v-model:sortOrder="filterParams.sortOrder"
                                            sortName="totalAmount"
                                            :text="$t('订单金额')"
                                            @loadData="onSearchSubmit()"
                                        ></SortButton>
                                    </th>
                                </tr>
                            </thead>
                        </table>
                        <table v-for="item in filterState" class="custom-table">
                            <thead>
                                <tr class="data-tr">
                                    <th colspan="6">
                                        <span class="check-box">{{ $t('订单编号：{orderSn}', { orderSn: item.orderSn }) }}</span>
                                        <el-divider direction="vertical" />
                                        <span class="check-box">{{ $t('下单时间：{addTime}', { addTime: item.addTime }) }}</span>
                                        <el-tooltip v-if="item.orderSource" :content="$t('订单来自') + item.orderSource" effect="light" placement="top">
                                            <Tag :text="item.orderSource" :transparent="true" style="text-transform: uppercase"></Tag>
                                        </el-tooltip>
                                        <Tag v-if="item.isExchangeOrder" :transparent="true" color="red" :text="$t('积分兑换')"></Tag>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(product, index) in item.items">
                                    <td>
                                        <div class="displayRow flex flex-align-center flex-justify-between">
                                            <ProductCard
                                                :picThumb="product.picThumb"
                                                :productId="product.productId"
                                                :productName="product.productName"
                                                :productType="product.productType"
                                                :url="product.url"
                                            ></ProductCard>
                                            <div class="displayColumn textR whiteSN widthAuto">
                                                <div>{{ priceFormat(product.price) }}</div>
                                                <div>× {{ product.quantity }}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textL">
                                            <div>
                                                {{ item.salesmanName || "--" }}
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textL">
                                            <div v-if="item.user">
                                                <DialogForm
                                                    :params="{ act: 'detail', id: item.user?.userId }"
                                                    isDrawer
                                                    path="user/user/Detail"
                                                    :title="$t('用户信息')"
                                                    width="800px"
                                                    @okCallback="loadFilter"
                                                    :showOnOk="false"
                                                >
                                                    <a>{{ item.user?.nickname || item.user?.username || "--" }}</a>
                                                </DialogForm>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn fz12 textL">
                                            <div class="gray"> {{ $t('收货人：') }} <span class="black">
                                                    {{ maskString(item.consignee!, 1, 1) }}
                                                </span>
                                            </div>
                                            <div class="gray"> {{ $t('手机号：') }} <span class="black">
                                                    <span><MobileCard :mobile="item.mobile"></MobileCard></span>
                                                </span>
                                            </div>
                                            <div class="gray flex user-address">
                                                <span class="label"> {{ $t('收货地址：') }} </span>
                                                <span class="black line1 value">
                                                    {{ maskString(item.userAddress, 16, 0) }}
                                                </span>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textL">
                                            <div>
                                                <span>{{ item.receivedTime || "--" }}</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textL">
                                            <div>
                                                <span>{{ item.payTypeId === 1 ? $t('在线支付') : item.payTypeId === 2 ? $t('货到付款') : $t('线下支付') }}</span>
                                            </div>
                                            <div v-if="item.logisticsName">
                                                {{ item.shippingTypeName }}
                                                <span class="gray">{{ "" + item.logisticsName + "" }}</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <span
                                            :class="{
                                                orange: item.orderStatus === 0,
                                                black: item.orderStatus === 1 || item.orderStatus === 2 || item.orderStatus === 4,
                                                gray: item.orderStatus === 3,
                                                green: item.orderStatus === 5
                                            }"
                                        >
                                            {{ item.orderStatusName }}
                                        </span>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn fz14">
                                            <div>{{ priceFormat(item.totalAmount) }}</div>
                                            <div class="gray">{{ $t('(含{getOrderShippingFeeLabel}：{shippingFee})', { getOrderShippingFeeLabel: getOrderShippingFeeLabel(item), shippingFee: item.shippingFee }) }}</div>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <div v-if="total == 0" class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </a-spin>
                </div>
                <div v-if="total > 0" class="pagination-con">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { Pagination, ProductCard } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { customerFilterParams, customerFilterState } from "@/types/salesman/customerTransactions.d";
import { getcustomerTransactionList, exportCustomerTransactionList } from "@/api/salesman/customerTransactions";
import requestExport from "@/utils/export";
import { DialogForm } from "@/components/dialog";
import SortButton from "../../../components/list/src/SortButton.vue";
import { priceFormat } from "@/utils/format";
import { getOrderShippingFeeLabel } from "@/utils/orderShippingFee";
import { useRoute, useRouter } from "vue-router";
import { Tag } from "@/components/form";
import { SelectTimeInterval } from "@/components/select";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { maskString } from "@/utils/util";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const filterState = ref<customerFilterState[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const filterParams = reactive<customerFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "addTime",
    sortOrder: "desc",
    endTime: "",
    startTime: "",
    keyword: "",
    salesmanId: ""
});
const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = config.get("pageSize");
    filterParams.sortField = "addTime";
    filterParams.sortOrder = "desc";
    filterParams.keyword = "";
    filterParams.endTime = "";
    filterParams.startTime = "";
    filterParams.salesmanId = "";
    loadFilter();
};
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    selectedIds.value = [];
    try {
        const result = await getcustomerTransactionList({ ...filterParams });
        if (result.records !== null) {
            const newArr = result.records.map((item) => ({
                ...item // 保留原始数据项的属性
            }));
            filterState.value = newArr;
        } else {
            filterState.value = [];
        }
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const props = defineProps({ id: { type: Number, default: 0 }, act: { type: String, default: "" }, isDialog: Boolean });
const route = useRoute();
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
const openPage = (href: string) => {
    window.open(href, "_blank");
};
import { selectsalesmanList } from "@/api/salesman/promoter";
const salesnamList = ref<any[]>([]);
const loadingSalesnam = ref(false);
// 获取列表的查询结果
const getSalesnamList = async (query: string) => {
    try {
        if (query) {
            loadingSalesnam.value = true;
            const params = { nickname: query };
            const result = await selectsalesmanList(params);
            result.records.forEach((item: any) => {
                item.value = item.salesmanId;
                item.key = item.salesmanId;
                item.label = item.user?.nickname || item.user?.username || "--";
            });
            salesnamList.value = result.records;
        } else {
            salesnamList.value = [];
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loadingSalesnam.value = false;
    }
};

const Exportloading = ref<boolean>(false);
const handleExport = async () => {
    Exportloading.value = true;
    try {
        const result = await exportCustomerTransactionList({ ...filterParams, isExport: "1" });
        Exportloading.value = false;
        requestExport(result, t('客户成交订单导出'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        Exportloading.value = false;
    }
};
</script>
<style lang="less" scoped>
.table-container {
    .custom-table {
        width: 100%;
        margin-bottom: 20px;
        background-color: #f5f6fa;
        border: 1px solid #f0f0f0;
        border-collapse: collapse;

        height: 60px;

        thead,
        tbody {
            tr {
                height: 45px;
                border-top: 1px solid #f0f0f0; // 垂直分割线
                border-right: 1px solid #f0f0f0; // 垂直分割线
            }

            th,
            td {
                padding: 15px 12px;
                vertical-align: middle;
                border-bottom: 1px solid #f0f0f0; // 水平分割线

                &:first-child {
                    width: 24%; // 多选按钮的宽度
                    min-width: 230px;
                    border-left: 1px solid #f0f0f0; // 左侧分割线
                    text-align: start;
                }

                &:nth-child(2) {
                    width: 8%; // 多选按钮的宽度
                }

                &:nth-child(3) {
                    width: 8%; // 多选按钮的宽度
                }

                &:nth-child(4) {
                    width: 20%; // 多选按钮的宽度
                }

                &:nth-child(5) {
                    width: 10%; // 多选按钮的宽度
                }

                &:nth-child(6) {
                    width: 10%; // 多选按钮的宽度
                }

                &:nth-child(7) {
                    width: 10%; // 多选按钮的宽度
                }
                &:nth-child(8) {
                    width: 10%; // 多选按钮的宽度
                }
            }

            .data-tr {
                height: 45px;

                th {
                    font-weight: normal;
                    text-align: start;
                }
            }

            td {
                background-color: white;
                border-right: 1px solid #f0f0f0; // 垂直分割线
            }
        }

        .check-box {
            vertical-align: middle;

            &.tag {
                font-size: 14px;
                margin-right: 4px;
            }
        }

        .mr10 {
            margin-right: 10px;
        }

        .ml10 {
            margin-left: 10px;
        }

        .ml5 {
            margin-left: 5px;
        }

        .textR {
            text-align: right;
        }

        .textL {
            text-align: left;
        }

        .buttonColor {
            background-color: rgba(32, 96, 224, 0.03);
        }

        .displayRow {
            display: flex;
            flex-direction: row;
            gap: 8px;
        }

        .displayColumn {
            display: flex;
            flex-direction: column;
        }

        .orderImage {
            min-width: 50px;
            min-height: 50px;
            max-width: 50px;
            max-height: 50px;
            border: 1px solid #eee;
        }

        .productName {
            line-height: 1;
            word-wrap: break-word;
            font-size: 12px;
            color: #333333;
        }

        .whiteSN {
            white-space: nowrap;
        }

        .widthAuto {
            width: auto;
        }

        .buttonStyle {
            display: flex;
            flex-direction: row-reverse;
            flex-wrap: nowrap;
            /* 不允许换行 */
            gap: 8px;
        }
    }

    .fz12 {
        font-size: 12px;
    }

    .fz14 {
        font-size: 14px;
    }

    .black {
        color: #333333;
    }

    .orange {
        color: #ed6a0c;
    }

    .gray {
        color: #999999;
    }
}
.table-container {
    overflow-x: auto;
    > * {
        min-width: 1000px;
    }
    .table-container-con {
        min-width: 800px;
    }
}

@media only screen and (max-width: 767px) {
    .tabs {
        flex-wrap: wrap;
        gap: 10px !important;
        .item {
            margin: 0 !important;
        }
    }
    .table-container {
        overflow-x: auto;
        > * {
            width: 1000px;
        }
        .table-container-con {
            min-width: 800px;
        }
    }
}
@media only screen and (max-width: 1400px) {
    .list-table-tool-col {
        :deep(.el-space) {
            flex-wrap: wrap;
        }
    }
    .table-container {
        overflow-x: auto;
        > * {
            width: 1000px;
        }
        .table-container-con {
            min-width: 800px;
        }
    }
}
</style>
