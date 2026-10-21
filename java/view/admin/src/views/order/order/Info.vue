/** * @file 订单详情页面 * @description 商城后台订单详情展示与操作 * @features * - 订单状态步骤条展示 * - 订单操作按钮（取消/删除/发货/备货/收货/核销/拆分） * -
收货人信息展示与编辑 * - 配送/自提/预约信息展示 * - 商品列表与金额明细 * - 物流追踪信息 * - 订单操作日志 * - 支持打印配送单/面单/小票 * - 同城配送送达照片展示
*/
<template>
    <div v-if="!loading">
        <div class="status-card" v-if="!isParent">
            <div class="status-card__header">
                {{ $t('订单编号：{orderSn}', { orderSn: formState.orderSn }) }}
                <el-divider direction="vertical" />
                {{ $t('下单时间：{addTime}', { addTime: formState.addTime }) }}
                <template v-if="formState.storeTitle">
                    <el-divider direction="vertical" />
                    <span>{{ $t('下单店铺：{storeTitle}', { storeTitle: formState.storeTitle }) }}</span>
                </template>
            </div>
            <div class="status-card__divider"></div>
            <div class="status-card__title">{{ orderStatusTitle }}</div>
            <div v-if="statusTipText" class="status-card__tip">{{ statusTipText }}</div>
            <el-space :size="10" wrap class="button-card status-card__actions">
                <div v-if="formState.isChangeOrderStatus">
                    <DialogForm
                        v-if="formState.availableActions.deliver"
                        :params="{ id: formState.orderId, preOrderStatus: formState.preOrderStatus }"
                        isDrawer
                        path="order/order/src/ChangeStatus"
                        :title="$t('取消订单')"
                        width="600px"
                        @okCallback="updateDataWithList"
                    >
                        <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('取消订单') }} </el-button>
                    </DialogForm>
                    <DialogForm
                        v-if="formState.availableActions.confirmReceipt"
                        :params="{ id: formState.orderId, preOrderStatus: formState.preOrderStatus }"
                        isDrawer
                        path="order/order/src/ChangeStatus"
                        :title="cancelDeliverText"
                        width="600px"
                        @okCallback="updateDataWithList"
                    >
                        <el-button bg class="buttonColor" size="small" text type="primary">
                            {{ cancelDeliverText }}
                        </el-button>
                    </DialogForm>
                </div>
                <!--                <el-button v-if="formState.availableActions.setConfirm" bg class="buttonColor" size="small" text type="primary" @click="setConfirm">-->
                <!--                    确认订单<Tooltip ico content="确认订单可允许在未支付的情况下发货" />-->
                <!--                </el-button>-->
                <DialogForm
                    v-if="formState.availableActions.setPaid"
                    :params="{ act: 'setPaid', id: formState.orderId }"
                    isDrawer
                    path="order/order/src/Operation"
                    :title="$t('设置为已支付')"
                    width="600px"
                    @okCallback="updateDataWithList"
                >
                    <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('设置为已支付') }} </el-button>
                </DialogForm>
                <DialogForm
                    v-if="formState.availableActions.cancelOrder"
                    :params="{ act: 'cancelOrder', id: formState.orderId }"
                    isDrawer
                    path="order/order/src/Operation"
                    :title="$t('取消订单')"
                    width="600px"
                    @okCallback="updateDataWithList"
                >
                    <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('取消订单') }} </el-button>
                </DialogForm>
                <el-button v-if="formState.availableActions.delOrder" bg size="small" text type="primary" @click="onDelClick"> {{ $t('删除订单') }} </el-button>
                <!-- formState.availableActions.deliver -->
                <!-- <DialogForm
                    v-if="(formState.availableActions.deliver && adminType == 'admin' && !formState.vendorId) || (formState.availableActions.deliver  && adminType == 'vendor')"
                    :params="{ act: 'detail', id: formState.orderId }"
                    :title="'订单发货 ' + formState.orderSn"
                    isDrawer
                    path="order/order/src/ToShip"
                    width="900px"
                    @okCallback="updateDataWithList"
                >
                    <el-button bg size="small" text type="danger"> 去发货 </el-button>
                </DialogForm> -->
                <!-- [修改] 使用计算属性 showDeliverButton 简化条件判断 -->
                <DialogForm
                    v-if="showDeliverButton"
                    :params="{ act: 'detail', id: formState.orderId }"
                    :title="$t('订单发货 ') + formState.orderSn"
                    isDrawer
                    path="order/order/src/ToShip"
                    width="900px"
                    @okCallback="updateDataWithList"
                >
                    <el-button bg size="small" text type="danger"> {{ $t('去发货') }} </el-button>
                </DialogForm>

                <!-- [修改] 使用计算属性 showPrepareButton 简化条件判断 -->
                <DeleteRecord
                    v-if="showPrepareButton"
                    :params="{
                        id: formState.orderId,
                        shippingMethod: 3,
                        deliverData: deliverDataFun(formState.items),
                        LogisticsId: 0,
                        trackingNo: ''
                    }"
                    :requestApi="orderDeliver"
                    :title="$t('该订单备货完成吗？')"
                    :message="$t('备货完成')"
                    @afterDelete="updateDataWithList"
                >
                    <el-button bg size="small" text type="danger"> {{ $t('去备货') }} </el-button>
                </DeleteRecord>
                <!-- [修改] 使用计算属性 showConfirmReceiptButton 简化条件判断 -->
                <el-button v-if="showConfirmReceiptButton" bg size="small" text type="primary" @click="onReceiptClick"> {{ $t('确认已收货') }} </el-button>
                <!-- [修改] 使用计算属性 showWriteOffButton 简化条件判断 -->
                <DialogForm
                    v-if="showWriteOffButton"
                    :params="{ act: 'detail', id: formState.orderId }"
                    :title="$t('订单核销 ') + formState.orderSn"
                    isDrawer
                    path="order/order/src/ToWriteOff"
                    width="900px"
                    @okCallback="updateDataWithList"
                >
                    <el-button bg size="small" text type="danger"> {{ $t('去核销') }} </el-button>
                </DialogForm>
                <el-button v-if="formState.availableActions.splitOrder" bg size="small" text type="danger" @click="onSplitClick"> {{ $t('拆分店铺订单') }} <Tooltip ico :content="$t('该订单来自多个店铺且还未拆分，在做任何操作前，您需要先拆分该订单')" />
                </el-button>
                <!-- <DialogForm
                    :params="{ act: 'detail', id: formState.orderId, valueName: 'adminNote' }"
                    isDrawer
                    path="order/order/src/EditRemark"
                    title="编辑备注"
                    width="600px"
                    @okCallback="updateDataWithList"
                >
                    <el-button bg class="buttonColor" size="small" text type="primary"> 商家备注 </el-button>
                </DialogForm> -->
                <el-button bg size="small" text type="primary" @click="openPage(`/print/orderPrint?ids=${formState.orderId}`, 'order')"> {{ $t('打印配送单') }} </el-button>
                <el-button v-if="formState.wayBill" bg size="small" text type="primary" @click="openPage(`/print/waybill?id=${formState.orderId}`, 'waybill')"> {{ $t('打印面单') }} </el-button>
                <el-button v-if="isPro() && hasPrint" bg size="small" text type="primary" :loading="loadingPrint" @click="handlePrint"> {{ $t('打印小票') }} </el-button>
            </el-space>
            <div v-if="formState.mark" style="margin-left: 10px">
                <EditSign :title="$t('编辑标记')" :item="formState" @callBack="fetchOrder">
                    <el-button link type="primary" style="font-size: 12px">{{ $t('标记订单') }}</el-button>
                    <div class="mt10">
                        <SignTag class="check-box tag" :value="formState.mark"></SignTag>
                        {{ formState.adminNote }}
                    </div>
                </EditSign>
            </div>
            <!-- <div v-if="formState.adminNote">
                <span class="orange">商家备注：</span>{{ formState.adminNote }}
                <DialogForm
                    :params="{ act: 'detail', id: formState.orderId, valueName: 'adminNote' }"
                    isDrawer
                    path="order/order/src/EditRemark"
                    title="编辑备注"
                    width="600px"
                    @okCallback="updateDataWithList"
                >
                    <el-button bg class="buttonColor" size="small" text type="primary"> 修改 </el-button>
                </DialogForm>
            </div> -->
        </div>
        <div class="container-card">
            <div class="title">{{ $t('订单明细') }}</div>
            <div class="info-card">
                <!-- [修改] 使用计算属性 showConsigneeInfo 简化条件判断 -->
                <div class="card-title" v-if="showConsigneeInfo">
                    <div class="min-title"> {{ $t('收货人信息') }} <!-- [修改] 使用计算属性 isEditConsigneeDisabled 简化 disabled 判断 -->
                        <DialogForm
                            v-if="!isParent"
                            :params="{ act: 'detail', id: formState.orderId, form: formState }"
                            isDrawer
                            path="order/order/src/EditConsignee"
                            :title="$t('编辑收货人信息')"
                            width="550px"
                            @okCallback="updateDataWithList"
                            :disabled="isEditConsigneeDisabled"
                        >
                            <!-- 供应商商品订单或订单收货地址为空不可点击编辑收货人信息 -->
                            <el-button bg class="ml10" size="small" text type="primary" :disabled="isEditConsigneeDisabled"> {{ $t('编辑') }} </el-button>
                        </DialogForm>
                    </div>
                    <ul>
                        <li class="card-li"> {{ $t('会员名称：') }}<span class="li-info">{{ formState.user?.nickname || formState.user?.username || "--" }}</span>
                        </li>
                        <li class="card-li">     {{ $t('收货人：') }}<span class="li-info">{{ formState.consignee ? formState.consignee : "--" }}</span>
                        </li>
                        <li class="card-li"> {{ $t('联系电话：') }}<span class="li-info">
                                <MobileCard :mobile="formState.mobile"></MobileCard>
                            </span>
                        </li>
                        <li class="card-li" v-if="formState.telephone"> {{ $t('手机：') }}<span class="li-info">{{ formState.telephone ? formState.telephone : "--" }}</span>
                        </li>
                        <li class="card-li" v-if="formState.email"> {{ $t('电子邮件：') }}<span class="li-info">{{ formState.email }}</span>
                        </li>
                        <li class="card-li"> {{ $t('邮政编码：') }}<span class="li-info">{{ formState.postcode || "--" }}</span>
                        </li>
                        <li class="card-li"> {{ $t('收货地址：') }} <span class="li-info">
                                {{ formState.userAddress ? formState.userAddress : "--" }}
                            </span>
                        </li>
                        <li class="card-li"> {{ $t('订单备注：') }} <span class="li-info">
                                {{ formState.buyerNote ? formState.buyerNote : "--" }}
                            </span>
                        </li>
                    </ul>
                </div>
                <!-- [修改] 使用计算属性 showConsigneeInfo 简化条件判断 -->
                <div class="card-title" v-if="showConsigneeInfo">
                    <div class="min-title"> {{ $t('配送信息') }} <!-- [修改] 使用计算属性 isEditShippingDisabled 简化 disabled 判断 -->
                        <DialogForm
                            v-if="!isParent"
                            :params="{ act: 'detail', id: formState.orderId }"
                            isDrawer
                            path="order/order/src/EditShipping"
                            :title="$t('编辑配送方式')"
                            width="600px"
                            :disabled="isEditShippingDisabled"
                            @okCallback="updateDataWithList"
                        >
                            <el-button :disabled="isEditShippingDisabled" bg class="buttonColor" size="small" text type="primary"> {{ $t('编辑') }} </el-button>
                        </DialogForm>
                    </div>
                    <ul>
                        <li class="card-li"> {{ $t('配送方式：') }}<span class="li-info">{{ formState.shippingTypeName }}</span>
                        </li>
                        <li class="card-li"> {{ $t('发货时间：') }}<span class="li-info">{{ formState.shippingTime ? formState.shippingTime : "--" }}</span>
                        </li>
                        <li class="card-li"> {{ $t('物流名称：') }}<span class="li-info">{{
                                formState.logisticsName && formState.shippingTypeId !== 3 ? formState.logisticsName : "--"
                            }}</span>
                        </li>
                        <li class="card-li"> {{ $t('发货单号：') }}<span class="li-info">{{ formState.trackingNo ? formState.trackingNo : "--" }}</span>
                        </li>
                    </ul>
                </div>
                <!-- [修改] 使用计算属性 showPickupInfo 简化条件判断 -->
                <div class="card-title" v-if="showPickupInfo">
                    <div class="min-title">{{ $t('自提信息') }}</div>
                    <ul>
                        <li class="card-li">{{ $t('配送方式：') }}<span class="li-info">{{ $t('到店自提') }}</span></li>
                        <li class="card-li" v-if="formState.pickupDetail"> {{ $t('自提点：') }}    <span class="li-info">{{ formState.pickupDetail.shopTitle }}</span>
                        </li>
                        <li class="card-li" v-if="formState.pickupDetail"> {{ $t('自提地址：') }}<span class="li-info">
                                {{ formState.pickupDetail.shopRegionNames.join("") }}
                                {{ formState.pickupDetail.address }}
                            </span>
                        </li>
                        <li class="card-li" v-if="formState.isPickup"> {{ $t('自提时间：') }}<span class="li-info">{{ formState.expectPickupTime }}</span>
                        </li>
                        <li class="card-li"> {{ $t('订单备注：') }}<span class="li-info">{{ formState.buyerNote ? formState.buyerNote : "--" }}</span>
                        </li>
                    </ul>
                </div>
                <!-- [修改] 使用计算属性 showReservationInfo、isReservationOrder、reservationConsignee、reservationMobile 简化条件判断 -->
                <div class="card-title" v-if="showReservationInfo">
                    <div class="min-title">{{ $t('预约信息') }}</div>
                    <ul>
                        <li class="card-li"> {{ $t('预约人：') }} <span class="li-info">{{ reservationConsignee }}</span>
                        </li>
                        <li class="card-li"> {{ $t('手机号：') }}<span class="li-info">
                                <MobileCard v-if="reservationMobile" :mobile="reservationMobile"></MobileCard>
                                <span v-else>--</span>
                            </span>
                        </li>
                        <li class="card-li" v-if="firstProductType === 5"> {{ $t('地址：') }}<span class="li-info">{{ formState.userAddress ? formState.userAddress : "--" }}</span>
                        </li>
                        <li class="card-li" v-if="isAppointmentOrderType"> {{ $t('预约时间：') }}<span class="li-info">{{ pickupOrReservationTimeText }}</span>
                        </li>
                        <li class="card-li" v-if="isAppointmentOrderType"> {{ $t('服务开始时间：') }}<span class="li-info">{{ formState.deliveryOrderInfo?.pickupTime || "--" }}</span>
                        </li>
                        <li class="card-li" v-if="isAppointmentOrderType"> {{ $t('服务截止时间：') }}<span class="li-info">{{ formState.deliveryOrderInfo?.completeTime || "--" }}</span>
                        </li>
                        <li class="card-li"> {{ $t('订单备注：') }}<span class="li-info">{{ formState.buyerNote ? formState.buyerNote : "--" }}</span>
                        </li>
                    </ul>
                </div>
                <div class="card-title">
                    <div class="min-title">{{ $t('支付信息') }}</div>
                    <ul>
                        <li class="card-li">
                            {{ isVendorAdmin ? $t('小计：') : $t('已支付金额：') }}<span class="li-info">{{ isVendorAdmin ? priceFormat(supplierProductAmount) : formState.paidAmount ? priceFormat(formState.paidAmount) : "--" }}</span>
                        </li>
                        <li class="card-li"> {{ $t('支付方式：') }}<span class="li-info">{{ formState.payTypeId === 1
                                    ? $t('在线支付') + `-${formState.isCreditOrder === 1 ? $t('账期支付') : ""}`
                                    : formState.payTypeId === 2
                                      ? $t('货到付款')
                                      : $t('线下支付') }}</span>

                            <!-- <DialogForm :params="{ act: 'info', id: 0 }" :showClose="false" :showOnOk="false" isDrawer path="order/src/PaymentLog" title="支付日志" width="860px"
                                @okCallback="fetchOrder">
                            </DialogForm> -->
                        </li>
                        <li class="card-li"> {{ $t('支付时间：') }}<span class="li-info">{{ formState.payTime ? formState.payTime : "--" }}</span>
                        </li>
                        <li class="card-li"> {{ $t('支付单号：') }}<span class="li-info">{{ formState.paySn ? formState.paySn : "--" }}</span>
                        </li>
                        <li v-if="formState.deliveryOrderInfo?.staffEarnings" class="card-li"> {{ $t('员工预计收益：') }}<span class="li-info">{{ priceFormat(formState.deliveryOrderInfo.staffEarnings) }}</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="container-card">
            <div class="title"> {{ $t('商品信息') }} <!-- [修改] 使用计算属性 isEditProductDisabled 简化条件判断，修复 TS 类型错误 -->
                <DialogForm
                    v-if="!isParent"
                    :params="{ act: 'detail', id: formState.orderId }"
                    isDrawer
                    path="order/order/src/EditProduct"
                    :title="$t('编辑商品信息')"
                    width="1050px"
                    :disabled="isEditProductDisabled"
                    @okCallback="updateDataWithList"
                >
                    <el-button :disabled="isEditProductDisabled" bg class="buttonColor" size="small" text type="primary"> {{ $t('编辑') }} </el-button>
                </DialogForm>
            </div>
            <el-table :data="formState.items">
                <el-table-column :label="$t('商品名称')" prop="productId" width="220">
                    <template #default="{ row }">
                        <div class="product-card-tp">
                            <div class="gift" v-if="row.isGift">{{ $t('赠') }}</div>
                            <ProductCard
                                :picThumb="row.picThumb"
                                :productId="row.productId"
                                :productName="row.productName"
                                :productType="row.productType"
                                :url="row.url"
                            >
                            </ProductCard>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('属性')" prop="productAttr">
                    <template #default="{ row }">
                        {{ row.skuValue || "-" }}
                    </template>
                </el-table-column>
                <el-table-column :label="$t('附加规格')" prop="extraSkuData">
                    <template #default="{ row }">
                        <div v-if="row.extraSkuData && row.extraSkuData.length > 0" class="extraskudata" v-for="item in row.extraSkuData" :key="item">
                            {{ item.attrName + item.attrValue + priceFormat(item.totalAttrPrice) }}
                        </div>
                        <div v-else>-</div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('商品编号')" width="150">
                    <template #default="{ row }">
                        {{ row.productSn }}
                    </template>
                </el-table-column>
                <el-table-column :label="isVendorAdmin ? $t('供货价') : $t('价格')" prop="price">
                    <template #default="{ row }">
                        {{ priceFormat(isVendorAdmin ? getSupplierOrderItemPrice(row) : row.price) }}
                    </template>
                </el-table-column>
                <!-- 预售订单才显示定金和尾款 -->
                <el-table-column :label="$t('定金')" prop="depositAmount" v-if="!isStore() && !isOverseas() && formState.isPresale">
                    <template #default="{ row }">
                        {{ priceFormat(row.depositAmount) }}
                    </template>
                </el-table-column>
                <el-table-column :label="$t('尾款')" prop="finalPaymentAmount" v-if="!isStore() && !isOverseas() && formState.isPresale">
                    <template #default="{ row }">
                        {{ priceFormat(row.finalPaymentAmount) }}
                    </template>
                </el-table-column>

                <el-table-column :label="$t('数量')" prop="quantity"></el-table-column>
                <!-- <el-table-column label="税费总计" prop="taxRateAmount">
                    <template #default="{ row }">
                        {{ priceFormat(getItemTaxTotal(row)) }}
                    </template>
                </el-table-column> -->
                <el-table-column :label="$t('库存')" prop="stock">
                    <template #default="{ row }">
                        {{ row.stock || "-" }}
                    </template>
                </el-table-column>

                <el-table-column :label="$t('小计')" prop="subtotal">
                    <template #default="{ row }">
                        {{ priceFormat(isVendorAdmin ? getSupplierOrderItemSubtotal(row) : row.subtotal) }}
                    </template>
                </el-table-column>
            </el-table>
            <div class="total">
                <span class="gray" style="margin-right: 16px">{{ $t('商品总重量：{totalProductWeight}kg', { totalProductWeight: formState.totalProductWeight }) }}</span>
                <span
                    >{{ $t('合计：{priceFormatNumberNumbergetOrderTaxFee}', { priceFormatNumberNumbergetOrderTaxFee: priceFormat(
                            isVendorAdmin
                                ? supplierProductAmount
                                : Number(formState.productAmount) - Number(formState.couponAmount) - Number(formState.discountAmount) + getOrderTaxFee(formState)
                        ) }) }}</span
                >
                <span v-if="isOverseas()" class="gray" style="margin-left: 16px">{{ $t('税费：{priceFormat}', { priceFormat: priceFormat(getOrderTaxFee(formState)) }) }}</span>
            </div>
            <div v-if="!isVendorAdmin" class="title"> {{ $t('订单金额') }} <!-- [修改] 使用计算属性 isEditMoneyDisabled 简化条件判断，修复 TS 类型错误 -->
                <DialogForm
                    v-if="!isParent"
                    :params="{ act: 'detail', id: formState.orderId }"
                    isDrawer
                    path="order/order/src/EditMoney"
                    :title="$t('编辑金额')"
                    width="600px"
                    :disabled="isEditMoneyDisabled"
                    @okCallback="updateDataWithList"
                >
                    <el-button :disabled="isEditMoneyDisabled" bg class="buttonColor" size="small" text type="primary"> {{ $t('编辑') }} </el-button>
                </DialogForm>
            </div>
            <OrderMoney v-if="!isVendorAdmin" v-model="formState"></OrderMoney>
        </div>
        <!-- <div class="container-card">
            <div class="title">其他信息
                <el-button bg class="ml10" size="small" text type="primary">
                    编辑
                </el-button>
            </div>
            <div class="info-card">
                <div class="card-title">
                    <div class="min-title">发票信息</div>
                    <ul>
                        <li class="card-li">发票类型：<span class="li-info"></span></li>
                        <li class="card-li">发票抬头：<span class="li-info"></span></li>
                        <li class="card-li">发票内容：<span class="li-info"></span></li>
                    </ul>
                </div>
                <div class="card-title">
                    <div class="min-title">备注信息</div>
                    <ul>
                        <li class="card-li">订单备注：<span class="li-info">{{ formState.buyerNote }}</span></li>
                        <li class="card-li">商家备注：<span class="li-info">{{ formState.adminNote }}</span></li>
                    </ul>
                </div>
            </div>
        </div> -->
        <!-- [修改] 使用计算属性 showLogisticsTracking 简化条件判断 -->
        <div class="container-card" v-if="showLogisticsTracking">
            <div class="title">{{ $t('物流追踪') }}</div>
            <LogisticsTracking
                :trackingNo="formState.trackingNo"
                :logisticsName="formState.logisticsName"
                :shippingMethod="formState.shippingMethod"
                v-model:id="formState.orderId"
                :shippingTypeId="formState.shippingTypeId"
            ></LogisticsTracking>
        </div>
        <DeliveryPhoto v-if="!isParent && formState.deliveryOrderInfo" :deliveryInfo="formState.deliveryOrderInfo" :orderSn="formState.orderSn"></DeliveryPhoto>
        <OperationLog v-if="!isParent" ref="operationLogRef" :orderId="formState.orderId"></OperationLog>
        <SubOrder v-if="action === 'parentDetail' && formState.subOrderIds && formState.subOrderIds.length > 0" :order-ids="formState.subOrderIds" />
    </div>
    <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, reactive, computed } from "vue";
import { useRouter } from "vue-router";
import { message, Modal } from "ant-design-vue";
import { OrderFormState } from "@/types/order/order.d";
import {
    getOrder,
    updateOrder,
    getOrderLogList,
    setConfirmOrder,
    splitStoreOrder,
    operationOrder,
    getOrderPrint,
    getOrderPrintBill,
    orderDeliver
} from "@/api/order/order";
import { DialogForm } from "@/components/dialog";
import { OrderMoney } from "@/components/order";
import { imageFormat, priceFormat } from "@/utils/format";
import { Tooltip } from "@/components/form";
import { ProductCard, Pagination, DeleteRecord } from "@/components/list";
import { useConfigStore } from "@/store/config";
import OperationLog from "@/views/order/order/src/OperationLog.vue";
import LogisticsTracking from "@/views/order/order/src/LogisticsTracking.vue";
import DeliveryPhoto from "@/views/order/order/src/DeliveryPhoto.vue";
import MobileCard from "@/components/list/src/MobileCard.vue";
import EditSign from "@/views/order/order/src/EditSign.vue";
import SignTag from "@/views/order/order/src/SignTag.vue";
import { hasEnabledPrint, triggerPrint } from "@/api/setting/receiptPrint";
import { isPro, isStore, isOverseas } from "@/utils/version";
import SubOrder from "@/views/order/order/src/SubOrder.vue";
import { canCurrentShopWriteOffOrder, isPlatformSelfOrder } from "@/utils/orderShipPermission";
import { ORDER_RECEIPT_CONFIRM_ENABLED } from "@/utils/orderReceiptGate";
import { getSupplierOrderItemPrice, getSupplierOrderItemSubtotal } from "@/utils/orderSupplierPrice";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
/** 当前管理员类型：admin-平台管理员 / shop-店铺管理员 / vendor-供应商 */
const adminType = ref(localStorage.getItem("adminType"));
const emit = defineEmits(["submitCallback", "callback", "update:confirmLoading", "close"]);

/**
 * 组件属性定义
 * @property {number} id - 订单ID
 * @property {string} act - 操作类型
 * @property {boolean} isParent - 是否为父订单视图
 * @property {boolean} isDialog - 是否以弹窗形式展示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isParent: {
        type: Boolean,
        default: false
    },
    isDialog: Boolean
});

/** 页面加载状态 */
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
/** 当前操作类型 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
/** 订单ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** 是否为父订单（拆分前的订单） */
const isParent = ref<boolean>(props.isDialog ? props.isParent : Boolean(query.isParent));
/** 操作类型：create/update */
const operation = action.value === "add" ? "create" : "update";
/** 表单引用 */
const formRef = shallowRef();
/** 操作日志组件引用 */
const operationLogRef = ref<InstanceType<typeof OperationLog>>();
/** 订单表单数据 */
const formState = ref<OrderFormState>({
    balance: 0,
    couponAmount: 0,
    pointsAmount: 0,
    productAmount: 0,
    totalProductWeight: 0,
    orderId: 0,
    items: [],
    payType: 0,
    availableActions: {},
    totalAmount: 0,
    paidAmount: 0,
    updateOrderSn: 0,
    unpaidAmount: 0,
    discountAmount: 0,
    expectPickupTime: "",
    isPresale: 0
});
/** 是否启用小票打印功能 */
const hasPrint = ref(false);

// ==================== 计算属性区域 - 简化模板条件判断 ====================

/**
 * 顶部状态卡标题
 * 优先后端 orderStatusName；缺失时从 stepStatus 当前步骤兜底取标题。
 */
const orderStatusTitle = computed(() => {
    if (formState.value.orderStatusName) {
        return formState.value.orderStatusName;
    }
    const step = formState.value.stepStatus;
    if (step && Array.isArray(step.steps) && step.steps[step.current]) {
        return step.steps[step.current].title || "--";
    }
    return "--";
});

/** 顶部状态卡副文案：后端 orderStatusTip（可选），缺失则不展示 */
const statusTipText = computed<string>(() => formState.value.orderStatusTip || "");

/**
 * 供应商视角：只由「查看者身份」决定，与「这张订单是否含供应商」无关。
 * 供应商端需要只看供货价、不暴露平台价格；平台端/店铺端即使查看供应商订单，
 * 也应按平台价格正常展示，因此这里不能再叠加 hasVendorOrderIdentity 判断。
 */
const isVendorAdmin = computed(() => adminType.value === "vendor");

const supplierProductAmount = computed(() =>
    formState.value.items.reduce((total, item) => total + getSupplierOrderItemSubtotal(item), 0)
);

const isPlatformAdmin = computed(() => adminType.value === "admin");
const canCurrentAdminShipCurrentOrder = computed(() => {
    if (!isPlatformAdmin.value) return true;
    return isPlatformSelfOrder(formState.value);
});

/** [新增] 获取首个商品的类型，若无商品返回 undefined */
const firstProductType = computed(() => formState.value.items?.[0]?.productType);

/** [新增] 判断是否为服务类商品（productType 5 或 6） */
const isServiceProduct = computed(() => firstProductType.value === 5 || firstProductType.value === 6);

/** 预约上门订单待核销：取消发货按钮文案改为“取消备货” */
const cancelDeliverText = computed(() => {
    const isAppointmentHomeOrder = !!formState.value.isPickup && firstProductType.value === 5;
    if (isAppointmentHomeOrder && formState.value.availableActions.confirmReceipt) return t('取消备货');
    return t('取消发货');
});

/** [新增] 判断是否为预约类订单（orderType 9 或 10，或到店自提无自提点详情） */
const isReservationOrder = computed(() => {
    const { orderType, isPickup, pickupDetail } = formState.value;
    // return orderType === 9 || orderType === 10 || (orderType === 2 && isPickup && !pickupDetail);
    return orderType === 9 || (orderType === 2 && isPickup && !pickupDetail);
});

/** [新增] 判断是否禁用编辑收货人信息 */
const isEditConsigneeDisabled = computed(() => {
    return isVendorAdmin.value || !formState.value.userAddress || !formState.value.availableActions.modifyOrderConsignee;
});

/** [新增] 判断是否禁用编辑配送方式 */
const isEditShippingDisabled = computed(() => {
    return !formState.value.availableActions.modifyShippingInfo || isVendorAdmin.value;
});

/** [新增] 判断是否禁用编辑商品信息 */
const isEditProductDisabled = computed(() => {
    return !formState.value.availableActions.modifyOrder || isVendorAdmin.value;
});

/** [新增] 判断是否禁用编辑金额 */
const isEditMoneyDisabled = computed(() => {
    return !formState.value.availableActions.modifyOrderMoney || isVendorAdmin.value;
});

/** [新增] 判断是否显示发货按钮 */
const showDeliverButton = computed(() => {
    const { isPickup, availableActions, shippingTypeId, vendorId } = formState.value;
    // 平台管理员仅允许为平台自营订单发货，店铺/供应商订单在详情页不再展示发货入口
    return (
        (!isPickup && availableActions.deliver && !isVendorAdmin.value && !vendorId && canCurrentAdminShipCurrentOrder.value && shippingTypeId !== 3) ||
        (availableActions.deliver && isVendorAdmin.value)
    );
});

/** [新增] 判断是否显示备货按钮 */
const showPrepareButton = computed(() => {
    const { availableActions, isPickup, shippingTypeId } = formState.value;
    return isStore() && availableActions.deliver && canCurrentAdminShipCurrentOrder.value && !isServiceProduct.value && (isPickup || shippingTypeId === 3);
});

/** [新增] 判断是否显示收货人信息区域（非自提或同城配送） */
const showConsigneeInfo = computed(() => {
    return !formState.value.isPickup || formState.value.shippingTypeId === 3;
});

/** [新增] 判断是否显示自提信息区域 */
const showPickupInfo = computed(() => {
    return formState.value.isPickup && !isServiceProduct.value;
});

/** [新增] 判断是否显示预约信息区域 */
const showReservationInfo = computed(() => {
    return formState.value.isPickup && isServiceProduct.value;
});

/** [新增] 判断是否显示物流追踪区域 */
const showLogisticsTracking = computed(() => {
    const { orderId, orderStatus, shippingMethod, orderType } = formState.value;
    console.log(formState.value, "formState.value");
    // 有订单ID + 已发货(2)或已完成(5) + 快递配送(1)或电子面单(4) + 非服务类订单(9,10)
    return (
        orderId &&
        (orderStatus === 2 || orderStatus === 5) &&
        (shippingMethod === 1 || shippingMethod === 4) &&
        orderType !== 9 &&
        orderType !== 10 &&
        !(formState.value.cycleInfo && Object.keys(formState.value.cycleInfo).length > 0)
    );
});

/** [新增] 判断是否显示确认已收货按钮（非自提 + 可确认收货） */
const showConfirmReceiptButton = computed(() => {
    return ORDER_RECEIPT_CONFIRM_ENABLED && !formState.value.isPickup && formState.value.availableActions.confirmReceipt;
});

/** [新增] 判断是否显示核销按钮（门店版 + 自提 + 可确认收货） */
const showWriteOffButton = computed(() => {
    const anyState = formState.value as any;
    // 周期购主订单不显示去核销
    if (+anyState.isCycleOrder === 1 && !anyState.parentOrderId) return false;
    return (
        isStore() &&
        formState.value.isPickup &&
        formState.value.availableActions.confirmReceipt &&
        canCurrentShopWriteOffOrder(formState.value, adminType.value, localStorage.getItem("shopId"))
    );
});

/** [新增] 获取预约人名称 */
const reservationConsignee = computed(() => {
    if (isReservationOrder.value) {
        return formState.value.userPickupInfo?.consignee || "--";
    }
    return formState.value.consignee || "--";
});

/** [新增] 获取预约人手机号 */
const reservationMobile = computed(() => {
    if (isReservationOrder.value) {
        return formState.value.userPickupInfo?.mobile;
    }
    return formState.value.mobile;
});

const isAppointmentOrderType = computed(() => [9, 10].includes((formState.value.orderType ?? -1) as number));
const pickupOrReservationTimeText = computed(() => {
    if (isAppointmentOrderType.value) {
        const date = formState.value.orderReservation?.reservationDate;
        const slot = formState.value.orderReservation?.timeSlotDescription;
        return date && slot ? `${date} ${slot}` : "--";
    }
    return "--";
});

const getItemTaxTotal = (row: any) => Number(row?.taxRateAmount ?? row?.taxFee ?? row?.taxAmount ?? 0) * Number(row?.quantity ?? 0);
const getOrderTaxFee = (order: any) => Number(order?.taxRateAmount ?? order?.taxFee ?? order?.taxAmount ?? order?.orderTaxFee ?? 0);

onMounted(() => {
    // 获取详情数据
    loading.value = true;
    fetchOrder();
    _hasEnabledPrint();
});
/**
 * @description 获取订单详情数据
 * 包含订单基本信息、商品列表、收货人信息、物流信息等
 */
const fetchOrder = async () => {
    try {
        const result = await getOrder(action.value, { id: id.value });
        Object.assign(formState.value, result);
        formState.value.postcode = result.addressData?.postcode || result.postcode || "";
        if (props.isParent) {
            emit("callback", { orderSn: formState.value.orderSn, changeTitle: true });
        }
    } catch (error: any) {
        // 多店铺订单被拆分为多个子订单时（code 5004），原订单已不存在，
        // 这是设置已支付后的正常流转结果，提示成功而非错误
        if (error?.code === 5004) {
            message.success(error.message);
        } else {
            message.error(error.message);
        }
        emit("close");
    } finally {
        loading.value = false;
    }
};

/**
 * @description 构建发货数据
 * 将订单商品列表转换为发货接口所需的数据格式
 * @param items 订单商品列表
 * @returns 发货数据数组
 */
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

/**
 * 确认订单
 * @description 允许在未支付的情况下进行发货操作
 */
const setConfirm = async () => {
    try {
        const result = await setConfirmOrder({ id: id.value });
        fetchOrder();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
/**
 * @description 拆分多店铺订单
 * 将包含多个店铺商品的订单拆分为独立的子订单
 */
const onSplitClick = () => {
    Modal.confirm({
        title: t('确认拆分订单吗？'),
        onOk: async () => {
            try {
                const result = await splitStoreOrder({ id: id.value });
                message.success(t('操作成功'));
                updateDataWithList();
            } catch (error: any) {
                message.error(error.message);
            } finally {
                loading.value = false;
            }
        }
    });
};
/** 删除订单（软删除，可恢复） */
const onDelClick = () => {
    Modal.confirm({
        title: t('确认删除订单吗？'),
        content: t('被删除的订单可以通过筛选查找并恢复'),
        onOk: async () => {
            try {
                const result = await operationOrder("delOrder", { id: id.value });
                message.success(t('操作成功'));
                updateDataWithList();
            } catch (error: any) {
                message.error(error.message);
            } finally {
                loading.value = false;
            }
        }
    });
};
/** 确认收货操作 */
const onReceiptClick = () => {
    Modal.confirm({
        title: t('确认订单已收货吗？'),
        onOk: async () => {
            try {
                const result = await operationOrder("confirmReceipt", { id: id.value });
                message.success(t('操作成功'));
                updateDataWithList();
            } catch (error: any) {
                message.error(error.message);
            } finally {
                loading.value = false;
            }
        }
    });
};
/** 更新订单详情并通知列表刷新 */
const updateDataWithList = (result: any = {}) => {
    operationLogRef.value?.loadFilter();
    if (!result) {
        fetchOrder();
        emit("callback", result);
    } else {
        emit("submitCallback", result);
        emit("callback", result);
    }
};

/**
 * 提交表单
 * @description 验证表单后提交订单更新
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateOrder(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
/** 表单提交入口 */
const onFormSubmit = () => {
    onSubmit();
};
const router = useRouter();
/**
 * 新窗口打开打印页面
 * @param {string} href - 相对路径
 * @param {string} type - 打印类型：order-配送单 / waybill-面单
 */
const openPage = async (href: string, type: string) => {
    let base = location.origin + router.options.history.base + href;
    try {
        if (type === "order") {
            await getOrderPrint({ ids: formState.value.orderId });
        }
        if (type === "waybill") {
            await getOrderPrintBill({ id: formState.value.orderId });
        }
        window.open(base, "_blank");
    } catch (error: any) {
        message.error(error.message);
    }
};

/**
 * 检查是否启用小票打印
 * @description 检查系统是否配置了小票打印机
 */
const _hasEnabledPrint = async () => {
    try {
        let res = await hasEnabledPrint();
        hasPrint.value = true;
    } catch (error) {
        console.log(error);
    }
};
/** 打印按钮加载状态 */
const loadingPrint = ref(false);
/**
 * 打印小票
 * @description 触发小票打印机打印当前订单
 */
const handlePrint = async () => {
    loadingPrint.value = true;
    try {
        let res = await triggerPrint({ ids: [id.value] });
        message.success(t('正在打印中~'));
    } catch (error) {
        console.log(error);
    } finally {
        loadingPrint.value = false;
    }
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.status-card {
    border: 1px solid #ececec;
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;

    .status-card__header {
        color: #646566;
        font-size: 13px;
    }

    .status-card__divider {
        border-bottom: 1px solid #ececec;
        padding-bottom: 16px;
        margin-bottom: 16px;
    }

    .status-card__title {
        font-size: 16px;
        line-height: 25px;
        color: #323233;
        font-weight: 700;
    }

    .status-card__tip {
        margin-top: 6px;
        font-size: 13px;
        line-height: 20px;
        color: #86909c;
    }

    .status-card__actions {
        margin-top: 16px;
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        width: 100%;
    }

    .buttonColor {
        background-color: rgba(32, 96, 224, 0.03);
    }
}
.container-card {
    border: 1px solid #ececec;
    /* 添加淡淡的边框 */
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;

    .border-bottom {
        border-bottom: 1px solid #ececec;
        padding-bottom: 16px;
        margin-bottom: 16px;
    }

    .button-card {
        margin-bottom: 12px;
        margin-top: 20px;
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        /* 允许换行 */
        gap: 8px;
    }

    .babyBlue {
        background-color: rgba(61, 127, 255, 0.06);
        border-radius: 6px;
    }

    .pd20 {
        padding: 20px;
    }

    .orange {
        color: #ed6a0c;
    }

    .title {
        font-size: 16px;
        line-height: 25px;
        color: #323233;
        margin-bottom: 15px;
        font-weight: 700;
    }

    .info-card {
        display: flex;
        flex-direction: row;
        gap: 20px;
        white-space: nowrap;

        .card-title {
            flex: 1;
            display: flex;
            flex-direction: column;
            font-size: 14px;
            line-height: 30px;
            font-weight: 500;
            color: #646566;
        }

        .min-title {
            margin-bottom: 10px;
        }

        .card-li {
            font-weight: normal;
            display: flex;
            font-size: 12px;
            line-height: 28px;
            display: flex;
            align-items: flex-start;
        }

        .li-info {
            display: inline-block;
            color: black;
            font-size: 12px;
            white-space: pre-wrap;
            overflow-wrap: break-word;
        }
    }

    .total {
        margin: 24px 0;
        width: 100%;
        text-align: right;
    }
}

.product-card-tp {
    position: relative;

    .gift {
        z-index: 99;
        color: white;
        position: absolute;
        top: 0;
        left: -10px;
        background-color: #ff3838;
        border-radius: 4px;
        padding: 0 5px;
        font-size: 12px;
        box-sizing: border-box;
    }
}

.extraskudata {
    padding-bottom: 5px;

    &:last-child {
        padding-bottom: 0;
    }
}
</style>
