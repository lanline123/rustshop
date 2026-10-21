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
                                        <TigInput v-model="filterParams.keyword" name="keyword"
                                            :placeholder="$t('订单号/收货人姓名/商品名称')" @keyup.enter="onSearchSubmit" clearable
                                            @clear="onSearchSubmit">
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span
                                                        class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('交易单号：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.outTradeNo" name="outTradeNo"
                                            :placeholder="$t('请输入交易单号')" @keyup.enter="onSearchSubmit" clearable
                                            @clear="onSearchSubmit" />
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field" v-if="isMerchant() && adminType !== 'shop'">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('选择店铺：') }}</span></label>
                                    <div class="control-container">
                                        <SelectShop v-model:shopId="filterParams.shopId" @onChange="onSearchSubmit">
                                        </SelectShop>
                                    </div>
                                </div>
                            </div>
                            <!-- <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>支付状态：</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.payStatus" clearable @change="onSearchSubmit">
                                            <el-option v-for="item in payStatusList" :label="item.label" :value="item.value"></el-option>
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>发货状态：</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.shippingStatus" clearable @change="onSearchSubmit">
                                            <el-option v-for="item in shippingStatusList" :label="item.label" :value="item.value"></el-option>
                                        </el-select>
                                    </div>
                                </div>
                            </div> -->
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span> {{ $t('详细地址：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.address" @keyup.enter="onSearchSubmit" clearable
                                            @clear="onSearchSubmit" />
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>Email：</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.email" @keyup.enter="onSearchSubmit" clearable
                                            @clear="onSearchSubmit" />
                                    </div>
                                </div>
                            </div>

                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('手机号：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.mobile" @keyup.enter="onSearchSubmit" clearable
                                            @clear="onSearchSubmit" />
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field" v-if="adminType !== 'vendor'">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('订单标记：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.mark" clearable @change="onSearchSubmit">
                                            <template #label>
                                                <template v-if="filterParams.mark !== -1">
                                                    <SignTag :value="filterParams.mark!" />
                                                </template>
                                                <template v-else> {{ $t('全部标记') }} </template>
                                            </template>
                                            <template v-for="sign in signList" :key="sign.value">
                                                <template v-if="sign.value !== -1">
                                                    <el-option :value="sign.value">
                                                        <SignTag :value="sign.value" />
                                                    </el-option>
                                                </template>
                                                <template v-else>
                                                    <el-option :label="$t('全部标记')" :value="-1"></el-option>
                                                </template>
                                            </template>
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <!-- <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>配送物流：</span></label>
                                    <div class="control-container">
                                        <SelectLogisticsCompany v-model:id="filterParams.logisticsId" @onChange="onSearchSubmit"></SelectLogisticsCompany>
                                    </div>
                                </div>
                            </div> -->
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('下单时间：') }}</span></label>
                                    <SelectTimeInterval v-model:end-date="filterParams.addEndTime"
                                        v-model:start-date="filterParams.addStartTime" :clearable="false" type="date"
                                        value-format="YYYY-MM-DD"></SelectTimeInterval>
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
                    <div class="tabs-box">
                        <el-tabs v-model="filterParams.orderStatus" tab-position="top" @tab-change="onTabChange">
                            <el-tab-pane v-for="item in deliverOrderStatusList" :key="item.value" :name="item.value">
                                <template #label>
                                    <el-badge
                                        class="order-status-tab-badge"
                                        :class="{ 'is-count-visible': item.count > 0 }"
                                        :value="item.count"
                                        color="#e5484d"
                                        :offset="[0, 0]"
                                        :hidden="item.count === 0"
                                    >
                                        {{ item.label }}
                                    </el-badge>
                                </template>
                            </el-tab-pane>
                        </el-tabs>
                    </div>

                    <div class="list-table-tool-col">
                        <el-space>
                            <template v-if="adminType !== 'vendor'">
                                <EditSign v-if="selectedIds.length > 0" :title="$t('添加标记')" :ids="selectedIds"
                                    @callBack="loadFilter">
                                    <el-button> {{ $t('批量标记') }} </el-button>
                                </EditSign>
                                <el-button v-else :disabled="true"> {{ $t('批量标记') }} </el-button>
                            </template>
                            <DialogForm v-if="canBatchDeliverSelected && filterParams.orderStatus === 1"
                                :params="{ act: 'severalDetail', ids: selectedIds }" :title="$t('订单批量发货')" isDrawer
                                path="order/order/src/BatchShip" width="880px" @okCallback="loadFilter">
                                <el-button> {{ $t('批量发货') }} </el-button>
                            </DialogForm>
                            <el-button v-else-if="filterParams.orderStatus === 1" :disabled="true"> {{ $t('批量发货') }} </el-button>
                            <DialogForm v-if="canBatchDeliverSelected && filterParams.orderStatus === 1"
                                :params="{ ids: selectedIds }" :title="$t('Excel批量发货')" isDrawer
                                path="order/order/src/BatchDeliverImport" width="560px" :showOnOk="false"
                                @okCallback="loadFilter" @callback="loadFilter">
                                <el-button> {{ $t('Excel批量发货') }} </el-button>
                            </DialogForm>
                            <el-button v-else-if="filterParams.orderStatus === 1" :disabled="true"> {{ $t('Excel批量发货') }} </el-button>
                            <el-button v-if="selectedIds.length > 0"
                                @click="openPage(`/print/orderPrint?ids=${selectedIds}`)"> {{ $t('批量打印') }} </el-button>
                            <el-button v-else :disabled="true"> {{ $t('批量打印') }} </el-button>
                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                        </el-space>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <table class="custom-table">
                            <thead>
                                <tr>
                                    <th>
                                        <el-checkbox v-model="selectAll" :indeterminate="isIndeterminate"
                                            class="check-box mr10" :disabled="selectableOrders.length === 0"
                                            @change="onSelectChangeAll"></el-checkbox>
                                        <SortButton v-model:sortField="filterParams.sortField"
                                            v-model:sortOrder="filterParams.sortOrder" sortName="orderId"
                                            :text="$t('订单信息(下单时间排序)')" @loadData="onSearchSubmit()"></SortButton>
                                    </th>
                                    <th>
                                        <SortButton v-model:sortField="filterParams.sortField"
                                            v-model:sortOrder="filterParams.sortOrder" sortName="totalAmount"
                                            :text="$t('订单金额')" @loadData="onSearchSubmit()"></SortButton>
                                    </th>
                                    <th class="textL">{{ $t('会员名称') }}</th>
                                    <th class="textL">{{ $t('收货人信息') }}</th>
                                    <th class="textL">{{ $t('物流/支付信息') }}</th>
                                    <th class="textL">{{ $t('订单状态') }}</th>
                                    <th class="textR">{{ $t('操作') }}</th>
                                </tr>
                            </thead>
                        </table>
                        <table v-for="item in filterState" class="custom-table">
                            <thead>
                                <tr class="data-tr">
                                    <th colspan="6">
                                        <el-checkbox v-model="item.checkBox" class="check-box mr10"
                                            :disabled="isSelectionDisabled(item)"
                                            @change="onSelectChange(item)"></el-checkbox>
                                        <template v-if="item.mark && adminType !== 'vendor'">
                                            <el-tooltip :disabled="!item.adminNote" :content="item.adminNote"
                                                effect="light" placement="top">
                                                <SignTag class="check-box tag" :value="item.mark"></SignTag>
                                            </el-tooltip>
                                        </template>

                                        <span class="check-box">{{ $t('订单编号：{orderSn}', { orderSn: item.orderSn }) }}</span>
                                        <el-divider direction="vertical" />
                                        <span class="check-box" v-if="item.outTradeNo">{{ $t('交易单号：{outTradeNo}', { outTradeNo: item.outTradeNo }) }}</span>
                                        <el-divider direction="vertical" v-if="item.outTradeNo" />
                                        <span class="check-box">{{ $t('下单时间：{addTime}', { addTime: item.addTime }) }}</span>
                                        <DialogForm v-if="item.parentOrderId > 0"
                                            :params="{ act: 'parentDetail', id: item.orderId, isParent: true }"
                                            :showClose="false" :showOnOk="false" :title="$t('订单详情 ') + item.orderSn"
                                            isDrawer :style="{ display: 'inline-block' }" path="order/order/Info"
                                            width="880px" @callback="loadFilter">
                                            <el-tooltip v-if="item.parentOrderId > 0" :content="$t('该订单已拆分，点击可查看父订单') + ''"
                                                effect="light" placement="top">
                                                <Tag :text="$t('子订单')"></Tag>
                                            </el-tooltip>
                                        </DialogForm>

                                        <el-tooltip v-if="item.shopId > 0 && isMerchant()"
                                            :content="$t('订单来自店铺：') + item.shop.shopTitle" effect="light" placement="top">
                                            <Tag :transparent="false" :text="$t('店铺')"></Tag>
                                        </el-tooltip>
                                        <el-tooltip v-if="item.orderSource" :content="$t('订单来自') + item.orderSource"
                                            effect="light" placement="top">
                                            <Tag :text="item.orderSource" :transparent="true"
                                                style="text-transform: uppercase"></Tag>
                                        </el-tooltip>
                                        <!--<el-tag class="ml10" disable-transitions effect="plain" size="small">-->
                                        <!--团购订单-->
                                        <!--</el-tag>-->
                                        <Tag v-if="item.isExchangeOrder" :transparent="true" color="red" :text="$t('积分兑换')">
                                        </Tag>
                                    </th>
                                    <th class="textR">
                                        <div style="gap: 8px; display: flex; justify-content: right">
                                            <DialogForm :params="{ act: 'detail', id: item.orderId }" :showClose="false"
                                                :showOnOk="false" :title="$t('订单详情 ') + item.orderSn" isDrawer
                                                path="order/order/Info" width="880px" @callback="loadFilter">
                                                <el-button bg class="buttonColor mr10" size="small" text type="primary"> {{ $t('查看详情') }}</el-button>
                                            </DialogForm>

                                            <!-- <DialogForm
                                                :params="{ act: 'detail', id: item.orderId, valueName: 'adminNote' }"
                                                isDrawer
                                                path="order/order/src/EditRemark"
                                                title="编辑备注"
                                                width="600px"
                                                @okCallback="loadFilter"
                                            >
                                                <el-button bg class="buttonColor" size="small" text type="primary"> 备注 </el-button>
                                            </DialogForm> -->

                                            <EditSign :title="$t('添加标记')" :item="item" @callBack="loadFilter">
                                                <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('标记') }} </el-button>
                                            </EditSign>
                                        </div>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(product, index) in item.items">
                                    <td>
                                        <div class="displayRow flex flex-align-center flex-justify-between">
                                            <ProductCard :picThumb="product.picThumb" :productId="product.productId"
                                                :productName="product.productName" :productType="product.productType"
                                                :url="product.url" :skuData="(product as any).skuData"></ProductCard>
                                            <div class="displayColumn textR whiteSN widthAuto">
                                                <div>{{ priceFormat(adminType === 'vendor' ? getSupplierOrderItemPrice(product) : product.price) }}</div>
                                                <div>× {{ product.quantity }}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textR fz14">
                                            <div>{{ priceFormat(adminType === 'vendor' ? getSupplierOrderTotal(item) : item.totalAmount) }}</div>
                                            <div class="gray fz12">{{ $t('含运费：{shippingFee}', { shippingFee: priceFormat(Number(item.shippingFee ?? 0)) }) }}
                                            </div>
                                            <div class="gray fz12">{{ $t('税费：{priceFormat}', { priceFormat: priceFormat(getOrderTaxFee(item)) }) }}</div>

                                            <div>
                                                <DialogForm v-if="item.status === 6 || item.status === 7"
                                                    :params="{ act: 'detail', id: item.aftersaleId }" isDrawer
                                                    path="order/aftersales/Info" :title="$t('售后详情 ') + item.aftersalesSn"
                                                    width="800px" @okCallback="loadFilter" :showClose="false"
                                                    :showOnOk="false">
                                                    <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('售后详情') }} </el-button>
                                                </DialogForm>
                                                <!-- <el-button size="small" text type="primary"> 售后详情 </el-button> -->
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn fz12 textL">
                                            <div>
                                                <span>{{ item.user?.username || "--" }}</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn fz12 textL" v-if="!item.isPickup">
                                            <div class="gray"> {{ $t('收货人：') }} <span class="black">
                                                    {{ maskString(item.consignee!, 1, 1) }}
                                                </span>
                                            </div>
                                            <div class="gray"> {{ $t('手机号：') }} <span class="black">
                                                    <span>
                                                        <MobileCard :mobile="item.mobile"></MobileCard>
                                                    </span>
                                                </span>
                                            </div>
                                            <div class="gray user-address">
                                                <span class="label"> {{ $t('收货地址：') }} </span>
                                                <span class="black line1 value">
                                                    {{ maskString(item.userAddress, 16, 0) }}
                                                </span>
                                            </div>
                                        </div>
                                        <div class="displayColumn fz12 textL"
                                            v-if="item.isPickup && product.productType !== 5 && product.productType !== 6 && item.orderSource !== 'pos'">
                                            <span class="black"> {{ $t('自提订单') }} </span>
                                        </div>
                                        <div class="displayColumn fz12 textL"
                                            v-if="item.isPickup && product.productType === 5 && item.orderSource !== 'pos'">
                                            <span class="black"> {{ $t('预约上门订单') }} </span>
                                        </div>
                                        <div class="displayColumn fz12 textL"
                                            v-if="item.isPickup && product.productType === 6 && item.orderSource !== 'pos'">
                                            <span class="black"> {{ $t('预约到店订单') }} </span>
                                        </div>
                                        <div class="displayColumn fz12 textL" v-if="item.orderSource === 'pos'">
                                            <span class="black"> {{ $t('收银台订单') }} </span>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn fz12 textL">
                                            <div>
                                                <span>{{ item.payTypeId === 1 ? $t('在线支付') : item.payTypeId === 2 ? $t('货到付款') :
                                                    $t('线下支付') }}</span>
                                            </div>
                                            <div v-if="item.logisticsName">
                                                {{ item.shippingTypeName }}
                                                <span class="gray">{{ "" + item.logisticsName + "" }}</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <span :class="{
                                            orange: item.orderStatus === 0,
                                            black: item.orderStatus === 1 || item.orderStatus === 2 || item.orderStatus === 4,
                                            gray: item.orderStatus === 3,
                                            green: item.orderStatus === 5
                                        }">
                                            {{ item.orderStatusName }}
                                        </span>
                                        <div v-if="item.afterSalesIng" class="aftersales-tag">售后中</div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="buttonStyle">
                                            <DialogForm v-if="item.availableActions.cancelOrder"
                                                :params="{ act: 'cancelOrder', id: item.orderId }" isDrawer
                                                path="order/order/src/Operation" :title="$t('取消订单')" width="600px"
                                                @okCallback="loadFilter">
                                                <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('取消订单') }} </el-button>
                                            </DialogForm>
                                            <el-button v-if="item.availableActions.delOrder" bg size="small" text
                                                type="primary" @click="onDelClick(item.orderId)"> {{ $t('删除订单') }} </el-button>

                                            <DialogForm :params="{ act: 'detail', id: item.orderId }"
                                                :title="$t('订单发货 ') + item.orderSn" isDrawer path="order/order/src/ToShip"
                                                width="900px" @okCallback="loadFilter"
                                                v-if="canShowDeliverButton(item)">
                                                <el-button bg size="small" text type="danger"> {{ $t('去发货') }} </el-button>
                                            </DialogForm>
                                            <!-- 其他订单：使用原有备货功能 -->
                                            <DeleteRecord v-else-if="
                                                isStore() &&
                                                item.availableActions.deliver &&
                                                canCurrentAdminShipOrder(item) &&
                                                item.orderType !== 9 &&
                                                item.orderType !== 10 &&
                                                (item.isPickup || item.shippingTypeId === 3)
                                            " :params="{
                                                    id: item.orderId,
                                                    shippingMethod: 3,
                                                    deliverData: deliverDataFun(item.items),
                                                    LogisticsId: 0,
                                                    trackingNo: ''
                                                }" :requestApi="orderDeliver" :title="$t('该订单备货完成吗？')" :message="$t('备货完成')"
                                                @afterDelete="loadFilter">
                                                <el-button bg size="small" text type="danger"> {{ $t('去备货') }} </el-button>
                                            </DeleteRecord>
                                            <el-button v-if="!item.isPickup && item.availableActions.confirmReceipt && ORDER_RECEIPT_CONFIRM_ENABLED" bg
                                                size="small" text type="danger" @click="onReceiptClick(item.orderId)"> {{ $t('确认收货') }} </el-button>
                                        </div>
                                    </td>
                                </tr>
                                <!-- <tr v-if="item.adminNote">
                                    <td colspan="6">
                                        <span class="orange">商家备注：</span>{{ item.adminNote }}
                                        <DialogForm
                                            :params="{ act: 'detail', id: item.orderId, valueName: 'adminNote' }"
                                            isDrawer
                                            path="order/order/src/EditRemark"
                                            title="编辑备注"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button bg class="buttonColor" size="small" text type="primary"> 修改 </el-button>
                                        </DialogForm>
                                    </td>
                                </tr> -->
                                <!-- 可以添加更多的行 -->
                            </tbody>
                        </table>
                        <div v-if="total == 0" class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </a-spin>
                </div>
                <div v-if="total > 0" class="pagination-con">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                        @callback="loadFilter" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, reactive, ref } from "vue";
import { Pagination, ProductCard, DeleteRecord } from "@/components/list";
import { message, Modal } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { OrderFilterParams, OrderFilterState } from "@/types/order/order.d";
import { getOrderList, getOrderStatusCount, operationOrder, orderDeliver } from "@/api/order/order";
import { SelectShop } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import SortButton from "../../../components/list/src/SortButton.vue";
import { priceFormat } from "@/utils/format";
import { useRoute, useRouter } from "vue-router";
import { Tag } from "@/components/form";
import { SelectTimeInterval } from "@/components/select";
import { maskString } from "@/utils/util";
import MobileCard from "@/components/list/src/MobileCard.vue";
import EditSign from "@/views/order/order/src/EditSign.vue";
import SignTag from "@/views/order/order/src/SignTag.vue";
import { isMerchant, isS2b2c, isStore, isPro } from "@/utils/version";
import { getShopType } from "@/utils/storage";
import StockUpWithDispatch from "@/components/organize/StockUpWithDispatch.vue";
import { useServiceConfigStore } from "@/store/serviceStaffConfig";
import { getOrderShippingFeeLabel } from "@/utils/orderShippingFee";
import { canCurrentShopWriteOffOrder, isPlatformSelfOrder } from "@/utils/orderShipPermission";
import { getSupplierOrderItemPrice, getSupplierOrderItemSubtotal } from "@/utils/orderSupplierPrice";
import { ORDER_RECEIPT_CONFIRM_ENABLED } from "@/utils/orderReceiptGate";
import { applyOrderStatusCount, buildOrderStatusCountParams } from "@/utils/orderStatusCount";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = ref(localStorage.getItem("adminType"));
const config: any = useConfigStore();
// 基本参数定义
const orderStatusList = reactive([
    { value: -1, label: t('全部'), isShow: true },
    { value: 0, label: t('待支付'), isShow: true },
    { value: 1, label: !isStore() ? t('待发货') : t('待发货/待备货'), isShow: true, count: 0 },
    { value: 2, label: !isStore() ? t('已发货') : t('已发货/待核销'), isShow: true, count: 0 },
    { value: 3, label: t('已取消'), isShow: true },
    { value: 4, label: t('无效'), isShow: true },
    { value: 5, label: !isStore() ? t('已完成') : t('已完成/已核销'), isShow: true },
    { value: -2, label: t('已删除'), isShow: true }
]);
const deliverOrderStatusList = computed(() => orderStatusList.filter((item) => [1, 2, 5].includes(item.value)));

const shippingStatusList = reactive([
    { value: 0, label: !isStore() ? t('待发货') : t('待发货/待备货') },
    { value: 1, label: !isStore() ? t('已发货') : t('已发货/待核销') },
    { value: 2, label: t('已收货') },
    { value: 3, label: t('配送中') },
    { value: 4, label: t('配送失败') }
]);

const payStatusList = reactive([
    { value: 0, label: t('未付款') },
    { value: 1, label: t('支付中') },
    { value: 2, label: t('已付款') },
    { value: 3, label: t('退款中') },
    { value: 4, label: t('已退款') }
]);

const signList = [
    {
        value: -1
    },
    {
        value: 1
    },
    {
        value: 2
    },
    {
        value: 3
    },
    {
        value: 4
    },
    {
        value: 5
    }
];

const isPlatformAdmin = computed(() => adminType.value === "admin");

const getSupplierOrderTotal = (item: any) =>
    Array.isArray(item?.items) ? item.items.reduce((total: number, product: any) => total + getSupplierOrderItemSubtotal(product), 0) : 0;

const canCurrentAdminShipOrder = (item: any): boolean => {
    if (!isPlatformAdmin.value) return true;
    return isPlatformSelfOrder(item);
};

const canCurrentShopWriteOff = (item: any): boolean => {
    return canCurrentShopWriteOffOrder(item, adminType.value, localStorage.getItem("shopId"));
};

const canShowDeliverButton = (item: any): boolean => {
    return (
        (!item?.isPickup &&
            item?.availableActions?.deliver &&
            adminType.value !== "vendor" &&
            canCurrentAdminShipOrder(item) &&
            item?.shippingTypeId !== 3) ||
        (item?.availableActions?.deliver && adminType.value === "vendor")
    );
};
const getOrderTaxFee = (item: any) => Number(item?.taxRateAmount ?? item?.taxFee ?? item?.taxAmount ?? item?.orderTaxFee ?? 0);

const onTabChange = (value: number) => {
    filterParams.orderStatus = value;
    filterParams.page = 1;
    selectedIds.value = [];
    loadFilter();
};
const filterState = ref<OrderFilterState[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const selectAll = ref<boolean>(false);
const selectedOrders = computed(() => {
    return filterState.value.filter((item) => item.orderId && selectedIds.value.includes(item.orderId));
});
const canBatchDeliverSelected = computed(() => {
    return selectedOrders.value.length > 0 && selectedOrders.value.every((item) => canCurrentAdminShipOrder(item));
});
const shouldRestrictBatchSelection = computed(() => isPlatformAdmin.value && filterParams.orderStatus === 1);
const isSelectionDisabled = (item: any): boolean => {
    return shouldRestrictBatchSelection.value && !canCurrentAdminShipOrder(item);
};
const selectableOrders = computed(() => {
    return filterState.value.filter((item) => !isSelectionDisabled(item));
});
const filterParams = reactive<OrderFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "orderId",
    sortOrder: "desc",
    keyword: "",
    outTradeNo: "",
    orderStatus: 1,
    mark: -1,
    address: "",
    email: "",
    mobile: "",
    shopId: "",
    addTime: [],
    addEndTime: "",
    addStartTime: ""
});
const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = config.get("pageSize");
    filterParams.sortField = "orderId";
    filterParams.sortOrder = "desc";
    filterParams.keyword = "";
    filterParams.outTradeNo = "";
    // filterParams.orderStatus = 1;
    filterParams.mark = -1;
    filterParams.address = "";
    filterParams.email = "";
    filterParams.mobile = "";
    filterParams.shopId = "";
    filterParams.addTime = [];
    filterParams.addEndTime = "";
    filterParams.addStartTime = "";
    filterParams.shippingStatus = "";
    filterParams.payStatus = "";
    loadFilter();
};
// 获取列表的查询结果
const loadStatusCount = async () => {
    try {
        const result = await getOrderStatusCount(buildOrderStatusCountParams({ ...filterParams }));
        applyOrderStatusCount(orderStatusList, result);
    } catch (error) {
        applyOrderStatusCount(orderStatusList, null);
    }
};
const loadFilter = async () => {
    loading.value = true;
    selectedIds.value = [];
    loadStatusCount();
    try {
        const result = await getOrderList({ ...filterParams });
        if (result.records !== null) {
            const newArr = result.records.map((item) => ({
                ...item, // 保留原始数据项的属性
                checkBox: false // 添加 check 属性并设置为 false
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

// 服务配置 store（同城配送/上门服务 & 派单模式判断）
const serviceConfigStore = useServiceConfigStore();
onMounted(() => {
    if (route.path.includes("exchange")) {
        filterParams.isExchangeOrder = 1;
    }
    if (action.value && action.value !== "undefined") {
        filterParams.userId = id.value;
    }
    if (query && query.status) {
        filterParams.orderStatus = Number(query.status);
    }
    loadFilter();
    // 加载同城配送和上门服务配置（用于判断派单模式）
    serviceConfigStore.loadAllConfig();
});

// 参数查询
const onSearchSubmit = () => {
    filterParams.page = 1;
    loadFilter();
};
const router = useRouter();
const openPage = (href: string) => {
    let base = location.origin + router.options.history.base + href;
    window.open(base, "_blank");
};
const onDelClick = (id: any) => {
    Modal.confirm({
        title: t('确认删除订单吗？'),
        content: t('被删除的订单可以通过筛选查找并恢复'),
        onOk: async () => {
            try {
                const result = await operationOrder("delOrder", { id: id });
                message.success(t('操作成功'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message);
            } finally {
                loading.value = false;
            }
        }
    });
};
const onReceiptClick = (id: any) => {
    Modal.confirm({
        title: t('确认订单已收货吗？'),
        onOk: async () => {
            try {
                const result = await operationOrder("confirmReceipt", { id: id });
                message.success(t('操作成功'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message);
            } finally {
                loading.value = false;
            }
        }
    });
};

// 多选操作
const isIndeterminate = computed(() => {
    const selectableOrderIds = selectableOrders.value.map((item) => item.orderId).filter(Boolean);
    const selectedSelectableCount = selectedIds.value.filter((id) => selectableOrderIds.includes(id)).length;
    selectAll.value = selectableOrderIds.length > 0 && selectedSelectableCount === selectableOrderIds.length;
    return selectedSelectableCount > 0 && selectedSelectableCount < selectableOrderIds.length;
});
const onSelectChangeAll = (val: boolean) => {
    if (val) {
        selectedIds.value.length = 0;
        filterState.value?.forEach((item: any) => {
            if (isSelectionDisabled(item)) {
                item.checkBox = false;
                return;
            }
            item.checkBox = true;
            selectedIds.value.push(item.orderId);
        });
    } else {
        filterState.value?.forEach((item) => {
            item.checkBox = false;
            selectedIds.value.length = 0;
        });
    }
};
const onSelectChange = (e: any) => {
    if (isSelectionDisabled(e)) {
        e.checkBox = false;
        return;
    }
    if (e.checkBox) {
        if (selectedIds.value.indexOf(e.orderId) === -1) {
            selectedIds.value.push(e.orderId);
        }
    } else {
        let index = selectedIds.value.indexOf(e.orderId);
        if (index !== -1) {
            selectedIds.value.splice(index, 1);
        }
    }
};

/**
 * 判断订单是否为可派单订单（同城配送或上门服务，且对应服务开启了派单模式）
 */
const isDispatchableOrder = (item: any): boolean => {
    // 同城配送：shippingTypeId === 3
    if (item.shippingTypeId === 3) {
        // 需要同城配送开启且为派单模式
        return serviceConfigStore.isIntraCityDeliveryEnabled && serviceConfigStore.isIntraCityDeliveryDispatchMode;
    }

    // 上门服务：isPickup && (productType === 5 || productType === 6)
    if (item.isPickup && item.items && item.items.length > 0) {
        const productType = item.items[0].productType;
        if (productType === 5 || productType === 6) {
            // 需要上门服务开启且为派单模式
            return serviceConfigStore.isHomeServiceEnabled && serviceConfigStore.isHomeServiceDispatchMode;
        }
    }

    return false;
};

/**
 * 判断是否为同城配送且需要派单（派单模式且未派单）
 */
const isDeliveryNeedDispatch = (item: any): boolean => {
    if (!item.deliveryOrderInfo) return false;
    // 判断当前是否启用了派单模式
    if (!serviceConfigStore.isIntraCityDeliveryDispatchMode) return false;
    // 同城配送（type=1）且未派单（staffId为null）
    return item.deliveryOrderInfo.type === 1 && !item.deliveryOrderInfo.staffId;
};

/**
 * 判断是否为上门服务且需要派单（派单模式且未派单）
 */
const isHomeServiceNeedDispatch = (item: any): boolean => {
    if (!item.deliveryOrderInfo) return false;
    // 判断当前是否启用了派单模式
    if (!serviceConfigStore.isHomeServiceDispatchMode) return false;
    // 上门服务（type=2）且未派单（staffId为null）
    return item.deliveryOrderInfo.type === 2 && !item.deliveryOrderInfo.staffId;
};

/**
 * 获取配送/派单按钮文本
 * @param item 订单信息
 * @returns 配送/派单按钮文本
 */
const getDeliveryButtonText = (item: any): string => {
    if (!item.deliveryOrderInfo) return t('去备货');

    // 上门服务且未派单
    if (item.deliveryOrderInfo.type === 2 && !item.deliveryOrderInfo.staffId) {
        return t('派单');
    }

    // 同城配送
    return t('去备货');
};

const deliverDataFun = (items: any) => {
    let deliverData: any = [];
    items.forEach((item: any) => {
        let data = {
            itemId: item.itemId,
            toDeliveryQuantity: item.quantity
        };
        deliverData.push(data);
    });
    return deliverData;
};
</script>
<style lang="less" scoped>
.aftersales-tag {
    margin-top: 4px;
    font-size: 12px;
    color: #ed6a0c;
    line-height: 18px;
}
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
                    width: 25%; // 多选按钮的宽度
                    min-width: 230px;
                    border-left: 1px solid #f0f0f0; // 左侧分割线
                    text-align: start;
                }

                &:nth-child(2) {
                    //min-width: 90px; // “”列宽度
                    width: 10%; // 多选按钮的宽度
                }

                &:nth-child(3) {
                    //min-width: 230px; // “”列宽度
                    width: 10%; // 多选按钮的宽度
                }

                &:nth-child(4) {
                    //min-width: 230px; // “”列宽度
                    width: 25%; // 多选按钮的宽度
                }

                &:nth-child(5) {
                    //min-width: 140px; // “”列宽度
                    width: 10%; // 多选按钮的宽度
                }

                &:nth-child(6) {
                    //min-width: 110px; // “”列宽度
                    width: 10%; // 多选按钮的宽度
                }

                &:nth-child(7) {
                    //min-width: 140px; // “”列宽度
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

        .user-address {
            width: 100%;
            display: flex;

            .value {
                flex: 1;
            }
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

.tabs-box {
    padding-bottom: 2px;

    :deep(.el-tabs__item) {
        padding-right: 12px;
    }

    .order-status-tab-badge {
        // 角标绝对定位时预留右侧空间，避免盖住文案（含 99+）
        &.is-count-visible {
            padding-right: 26px;
        }

        :deep(.el-badge__content) {
            box-sizing: border-box;
            min-width: 16px;
            height: 16px;
            padding: 0 4px;
            border-radius: 8px;
            font-size: 10px;
            line-height: 14px;
        }

        :deep(.el-badge__content.is-fixed) {
            top: 0;
            right: 0;
            transform: translateY(-35%);
        }
    }
}

.table-container {
    overflow-x: auto;

    >* {
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

        >* {
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

        >* {
            width: 1000px;
        }

        .table-container-con {
            min-width: 800px;
        }
    }
}
</style>
