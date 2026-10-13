/**
 * @file 售后列表页面
 * @description 商城后台售后管理列表，支持退款/退货/换货申请处理
 * @features
 * - 多条件筛选（订单号/售后编号/申请类型/退换状态）
 * - 售后商品信息展示（商品详情/退货数量/金额）
 * - 售后状态追踪（待处理/处理中/已完成/已拒绝等）
 * - 售后类型区分（仅退款/退货退款/换货）
 * - 快捷操作按钮（处理退款/查看详情）
 * - 支持供应商商品售后流程
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('订单号：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('订单号')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('交易单号：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.outTradeNo"
                                                name="outTradeNo"
                                                :placeholder="$t('交易单号')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('售后编号：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.aftersalesSn"
                                                name="aftersalesSn"
                                                :placeholder="$t('售后编号')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('申请类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.aftersaleType" clearable @change="onSearchSubmit">
                                                <el-option v-for="(item, index) in typeList" :value="index" :label="item" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('退换状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable @change="onSearchSubmit">
                                                <el-option v-for="(item, index) in statusList" :value="index" :label="item" />
                                            </el-select>
                                        </div>
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
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <table class="custom-table">
                            <colgroup>
                                <col style="width: 30%" />
                                <col style="width: 20%" />
                                <col style="width: 15%" />
                                <col style="width: 15%" />
                                <col style="width: 20%" />
                            </colgroup>
                            <thead>
                                <tr>
                                    <th>
                                        <SortButton
                                            v-model:sortField="filterParams.sortField"
                                            v-model:sortOrder="filterParams.sortOrder"
                                            sortName="orderId"
                                            :text="$t('退换货商品')"
                                            @loadData="loadFilter()"
                                        ></SortButton>
                                    </th>
                                    <th>{{ $t('售后原因') }}</th>
                                    <th>{{ $t('服务类型') }}</th>
                                    <th>{{ $t('售后状态') }}</th>
                                    <th>{{ $t('操作') }}</th>
                                </tr>
                            </thead>
                        </table>
                        <table v-for="item in filterState" class="custom-table">
                            <colgroup>
                                <col style="width: 30%" />
                                <col style="width: 20%" />
                                <col style="width: 15%" />
                                <col style="width: 15%" />
                                <col style="width: 20%" />
                            </colgroup>
                            <thead>
                                <tr class="data-tr">
                                    <th colspan="4">
                                        <span class="check-box">{{ $t('订单编号：{orderSn}', { orderSn: item.orderSn }) }}</span>
                                        <el-divider direction="vertical" />
                                        <span class="check-box" v-if="item.outTradeNo">{{ $t('交易单号：{outTradeNo}', { outTradeNo: item.outTradeNo }) }}</span>
                                        <el-divider direction="vertical" v-if="item.outTradeNo" />
                                        <span class="check-box">{{ $t('售后编号：{aftersalesSn}', { aftersalesSn: item.aftersalesSn }) }}</span>
                                        <el-divider direction="vertical" />
                                        <span class="check-box">{{ $t('申请时间：{addTime}', { addTime: item.addTime }) }}</span>
                                        <el-divider direction="vertical" v-if="item.shippingTime" />
                                        <span class="check-box" v-if="item.userName">{{ $t('发货时间：{shippingTime}', { shippingTime: item.shippingTime }) }}</span>
                                        <el-divider direction="vertical" v-if="item.userName" />
                                        <span class="check-box" v-if="item.userName">{{ $t('申请人：{userName}', { userName: item.userName }) }}</span>
                                    </th>
                                    <th class="header-actions-th">
                                        <div class="header-actions">
                                            <DialogForm
                                                :params="{ act: 'detail', id: item.aftersaleId }"
                                                isDrawer
                                                path="order/aftersales/Info"
                                                :title="$t('售后详情 {aftersalesSn}', { aftersalesSn: item.aftersalesSn })"
                                                width="800px"
                                                @okCallback="loadFilter"
                                                :showClose="false"
                                                :showOnOk="false"
                                            >
                                                <el-button size="small" text type="primary">{{ $t('售后详情') }}</el-button>
                                            </DialogForm>
                                            <template v-if="item.orderId">
                                                <el-divider direction="vertical" />
                                                <DialogForm
                                                    :params="{ act: 'detail', id: item.orderId }"
                                                    isDrawer
                                                    path="order/order/Info"
                                                    :title="$t('订单详情 {orderSn}', { orderSn: item.orderSn })"
                                                    width="800px"
                                                    @okCallback="loadFilter"
                                                    :showClose="false"
                                                    :showOnOk="false"
                                                >
                                                    <el-button size="small" text type="primary">{{ $t('订单详情') }}</el-button>
                                                </DialogForm>
                                                <template v-if="adminType !== 'vendor'">
                                                    <el-divider direction="vertical" />
                                                    <EditSign :item="(item as any)" :title="$t('添加标记')" @call-back="loadFilter">
                                                        <el-button size="small" text type="primary">{{ $t('标记') }}</el-button>
                                                    </EditSign>
                                                </template>
                                            </template>
                                        </div>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(product, index) in item.aftersalesItems">
                                    <td>
                                        <div class="displayRow flex-justify-between">
                                            <ProductCard :picThumb="product.picThumb" :productId="product.productId" :productName="product.productName">
                                            </ProductCard>
                                            <div class="displayColumn textR whiteSN widthAuto">
                                                <div>{{ priceFormat(adminType === "vendor" ? getSupplierOrderItemPrice(product) : product.price) }}</div>
                                                <div>
                                                    × {{ product.quantity }}<span class="red ml10">{{ $t('(退货数量：{number})', { number: product.number }) }}</span>
                                                </div>
                                                <div></div>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.aftersalesItems.length">
                                        <p>{{ item.aftersaleReason || "-" }}</p>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.aftersalesItems.length">
                                        <span>
                                            {{ item.aftersalesTypeName }}
                                        </span>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.aftersalesItems.length">
                                        <span
                                            :class="{
                                                orange: item.status === 0,
                                                black: item.status === 1 || item.status === 2 || item.status === 4,
                                                gray: item.status === 3,
                                                green: item.status === 5
                                            }"
                                        >
                                            {{ item.statusName }}
                                        </span>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.aftersalesItems.length">
                                        <ListBtn :item="item" @callback="loadFilter"></ListBtn>
                                        <!-- <template v-if="item.vendorId && item.vendorId > 0">
                                            <DialogForm
                                                v-if="(item.status === 4 || item.status === 21 || item.status === 22) && adminType !== 'vendor'"
                                                :params="{ act: 'detail', id: item.aftersaleId }"
                                                isDrawer
                                                path="order/aftersales/Info"
                                                :title="'售后详情 ' + item.aftersalesSn"
                                                width="800px"
                                                @okCallback="loadFilter"
                                                :showClose="false"
                                                :showOnOk="false"
                                            >
                                                <el-button size="small" text type="primary"> 售后详情 </el-button>
                                            </DialogForm>
                                            <DialogForm
                                                v-else-if="item.status === 5 || item.status === 6 || item.status === 7"
                                                :params="{ act: 'detail', id: item.aftersaleId }"
                                                isDrawer
                                                path="order/aftersales/Info"
                                                :title="'售后详情 ' + item.aftersalesSn"
                                                width="800px"
                                                @okCallback="loadFilter"
                                                :showClose="false"
                                                :showOnOk="false"
                                            >
                                                <el-button size="small" text type="primary"> 售后详情 </el-button>
                                            </DialogForm>
                                            <DialogForm
                                                v-else-if="item.status !== 4"
                                                :params="{ act: 'detail', id: item.aftersaleId }"
                                                isDrawer
                                                path="order/aftersales/Info"
                                                :title="'处理售后申请 ' + item.aftersalesSn"
                                                width="800px"
                                                @okCallback="loadFilter"
                                                :showClose="false"
                                                :showOnOk="false"
                                            >
                                                <el-button size="small" text type="danger"> 处理退款 </el-button>
                                            </DialogForm>
                                        </template>

                                        <template v-else>
                                            <DialogForm
                                                v-if="item.status === 3 || item.status === 5 || item.status === 6 || item.status === 7"
                                                :params="{ act: 'detail', id: item.aftersaleId }"
                                                isDrawer
                                                path="order/aftersales/Info"
                                                :title="'售后详情 ' + item.aftersalesSn"
                                                width="800px"
                                                @okCallback="loadFilter"
                                                :showClose="false"
                                                :showOnOk="false"
                                            >
                                                <el-button size="small" text type="primary"> 售后详情 </el-button>
                                            </DialogForm>
                                            <DialogForm
                                                v-else
                                                :params="{ act: 'detail', id: item.aftersaleId }"
                                                isDrawer
                                                path="order/aftersales/Info"
                                                :title="'处理售后申请 ' + item.aftersalesSn"
                                                width="800px"
                                                @okCallback="loadFilter"
                                                :showClose="false"
                                                :showOnOk="false"
                                            >
                                                <el-button size="small" text type="danger"> 处理退款 </el-button>
                                            </DialogForm>
                                        </template> -->
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <div v-if="total == 0" class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import SortButton from "../../../components/list/src/SortButton.vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination, ProductCard, Switch } from "@/components/list";
import { Image } from "@/components/image";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { AftersalesFilterParams, AftersalesFilterState } from "@/types/order/aftersales";
import { batchSubmit, getAftersalesList, updateAftersalesField, getAftersalesApplyType, getAftersalesReturnStatus } from "@/api/order/aftersales";
import { priceFormat } from "@/utils/format";
import { getSupplierOrderItemPrice } from "@/utils/orderSupplierPrice";
import { useRoute, useRouter } from "vue-router";
import { useListRequest } from "@/hooks/useListRequest";
import ListBtn from "./src/ListBtn.vue"
import EditSign from "@/views/order/order/src/EditSign.vue"

const config: any = useConfigStore();
/** 当前管理员类型 */
const adminType = ref(localStorage.getItem("adminType"));
const query = useRouter().currentRoute.value.query;
/**
 * 使用列表请求Hook
 * @description 封装售后列表数据加载、筛选等通用逻辑
 */
const {
    /** 售后列表数据 */
    listData: filterState,
    /** 加载状态 */
    loading,
    /** 总数 */
    total,
    /** 筛选参数 */
    filterParams,
    /** 加载数据方法 */
    loadData: loadFilter,
    /** 搜索提交 */
    onSearchSubmit
} = useListRequest<AftersalesFilterState, AftersalesFilterParams>({
    apiFunction: getAftersalesList,
    idKey: "aftersaleId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: (query.keyword as any) || "",
        outTradeNo: "",
        aftersalesSn: "",
        aftersaleType: "",
        status: (query.status as any) || "",
        page: 1,
        size: config.get("pageSize")
    }
});
loadFilter();

/**
 * 重置筛选参数
 * @description 将所有筛选条件恢复为默认值并重新加载列表
 */
const resetParams = () => {
    filterParams.page = 1;
    filterParams.sortField = "";
    filterParams.sortOrder = "";
    filterParams.keyword = "";
    filterParams.outTradeNo = "";
    filterParams.aftersalesSn = "";
    filterParams.aftersaleType = "";
    filterParams.status = "";
    loadFilter();
};

useRouter().replace({ query: {} });
/** 售后状态列表（从后端获取） */
const statusList = ref<any>({});
/** 售后类型列表（仅退款/退货退款/换货） */
const typeList = ref<any>({});

/**
 * 加载筛选选项数据
 * @description 获取售后类型和售后状态的下拉选项
 */
const loadData = async () => {
    loading.value = true;
    try {
        const applyType = await getAftersalesApplyType();
        typeList.value = applyType;
        const returnStatusList = await getAftersalesReturnStatus();
        statusList.value = returnStatusList;
        if (query && query.status) {
            filterParams.status = query.status as any;
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    if (query && query.status) {
        filterParams.status = Number(query.status);
    }
    loadData();
});
</script>
<style lang="less" scoped>
.table-container {
    overflow: auto;
    .custom-table {
        width: 100%;
        // 表头与数据分属不同 table，必须靠 colgroup + 固定布局才能保证列宽一致
        min-width: 1000px;
        table-layout: fixed;
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
                    border-left: 1px solid #f0f0f0; // 左侧分割线
                    text-align: start;
                }

                &:nth-child(2),
                &:nth-child(3),
                &:nth-child(4) {
                    text-align: start;
                }
            }

            .data-tr {
                height: 45px;

                th {
                    font-weight: normal;
                }

                .header-actions-th {
                    text-align: right;
                    white-space: nowrap;
                }

                .header-actions {
                    display: flex;
                    align-items: center;
                    justify-content: flex-end;
                    flex-wrap: nowrap;
                }
            }

            td {
                background-color: white;
                border-right: 1px solid #f0f0f0; // 垂直分割线
            }
        }

        .check-box {
            vertical-align: middle;
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
</style>
