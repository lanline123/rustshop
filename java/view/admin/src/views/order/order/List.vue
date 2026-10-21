/** * @file 订单列表页面 * @description 商城后台订单管理列表，支持多维度筛选和批量操作 * @features * -
订单状态Tab切换（全部/待支付/待发货/已发货/已取消/无效/已完成/已删除） * - 多条件筛选（订单号/商品名/支付方式/发货状态/物流公司/订单标记等） * -
批量操作（标记/批量打印配送单/批量打印小票） * - 单订单操作（查看详情/发货/取消/删除/核销/确认收货等） * - 支持多商户/多供应商订单管理 * -
O2O门店订单支持（自提/同城配送/上门服务/预约到店） * - 拼团订单状态显示 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <TigTabs v-model="filterParams.orderStatus" :tabs="orderStatusList" @onTabChange="onTabChange"> </TigTabs>
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
                                            :placeholder="$t('订单号/收货人姓名/商品名称')"
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
                                    <label class="control-label"><span>{{ $t('交易单号：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.outTradeNo"
                                            name="outTradeNo"
                                            :placeholder="$t('请输入交易单号')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        />
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field" v-if="isMerchant() && adminType !== 'shop'">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('选择店铺：') }}</span></label>
                                    <div class="control-container">
                                        <SelectShop v-model:shopId="filterParams.shopId" @onChange="onSearchSubmit"> </SelectShop>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('支付方式：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.payCode" clearable @change="onSearchSubmit">
                                            <el-option v-for="item in payTypeList" :label="item.label" :value="item.value"></el-option>
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('支付状态：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.payStatus" clearable @change="onSearchSubmit">
                                            <el-option v-for="item in payStatusList" :label="item.label" :value="item.value"></el-option>
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('发货状态：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.shippingStatus" clearable @change="onSearchSubmit">
                                            <el-option v-for="item in shippingStatusList" :label="item.label" :value="item.value"></el-option>
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span> {{ $t('详细地址：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.address" @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit" />
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>Email：</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.email" @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit" />
                                    </div>
                                </div>
                            </div>

                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('手机号：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.mobile" @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit" />
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('配送物流：') }}</span></label>
                                    <div class="control-container">
                                        <SelectLogisticsCompany v-model:logisticsId="filterParams.logisticsId" @onChange="onSearchSubmit">
                                        </SelectLogisticsCompany>
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
                                                <template v-else> {{ $t('全部订单') }} </template>
                                            </template>
                                            <template v-for="sign in signList" :key="sign.value">
                                                <template v-if="sign.value !== -1">
                                                    <el-option :value="sign.value">
                                                        <SignTag :value="sign.value" />
                                                    </el-option>
                                                </template>
                                                <template v-else>
                                                    <el-option :label="$t('全部订单')" :value="-1"></el-option>
                                                </template>
                                            </template>
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('下单时间：') }}</span></label>
                                    <SelectTimeInterval
                                        v-model:end-date="filterParams.addEndTime"
                                        v-model:start-date="filterParams.addStartTime"
                                        :clearable="false"
                                        type="date"
                                        value-format="YYYY-MM-DD"
                                    ></SelectTimeInterval>
                                </div>
                            </div>
                            <div v-if="MEMBER_TAG_AUDIENCE_READY" class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>会员标签：</span></label>
                                    <div class="control-container">
                                        <el-select v-model="memberTagFilterType" @change="onMemberTagFilterChange"
                                            style="min-width: 200px">
                                            <el-option label="全部" value="all" />
                                            <el-option :label="memberTagFilterDisplayText" value="specify">
                                                <span @click="openMemberTagDialog">{{ memberTagFilterDisplayText }}</span>
                                            </el-option>
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
                </div>
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <template v-if="adminType !== 'vendor'">
                                <EditSign v-if="selectedIds.length > 0" :title="$t('添加标记')" :ids="selectedIds" @callBack="loadFilter">
                                    <el-button> {{ $t('标记') }} </el-button>
                                </EditSign>
                                <el-button v-else :disabled="true"> {{ $t('标记') }} </el-button>
                            </template>
                            <!-- <DialogForm
                                v-if="selectedIds.length > 0"
                                :params="{ act: 'severalDetail', ids: selectedIds }"
                                title="订单批量发货"
                                isDrawer
                                path="order/order/src/BatchShip"
                                width="880px"
                                @okCallback="loadFilter"
                            >
                                <el-button> 批量发货 </el-button>
                            </DialogForm>
                            <el-button v-else :disabled="true"> 批量发货 </el-button> -->
                            <DialogForm
                                v-if="canBatchDeliverSelected"
                                :params="{ ids: selectedIds }"
                                :title="$t('Excel批量发货')"
                                isDrawer
                                path="order/order/src/BatchDeliverImport"
                                width="560px"
                                :showOnOk="false"
                                @okCallback="loadFilter"
                                @callback="loadFilter"
                            >
                                <el-button> {{ $t('Excel批量发货') }} </el-button>
                            </DialogForm>
                            <el-button v-else :disabled="true"> {{ $t('Excel批量发货') }} </el-button>
                            <el-button v-if="selectedIds.length > 0" @click="openPage(`/print/orderPrint?ids=${selectedIds}`)"> {{ $t('批量打印配送单') }} </el-button>
                            <el-button v-else :disabled="true"> {{ $t('批量打印配送单') }} </el-button>

                            <template v-if="isPro() && hasPrint">
                                <el-button v-if="selectedIds.length > 0" @click="handlePrint(selectedIds)"> {{ $t('批量打印小票') }} </el-button>
                                <el-button v-else :disabled="true"> {{ $t('批量打印小票') }} </el-button>
                            </template>

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
                                        <el-checkbox
                                            v-model="selectAll"
                                            :indeterminate="isIndeterminate"
                                            class="check-box mr10"
                                            :disabled="selectableOrders.length === 0"
                                            @change="onSelectChangeAll"
                                        ></el-checkbox>
                                        <SortButton
                                            v-model:sortField="filterParams.sortField"
                                            v-model:sortOrder="filterParams.sortOrder"
                                            sortName="orderId"
                                            :text="$t('订单信息(下单时间排序)')"
                                            @loadData="onSearchSubmit()"
                                        ></SortButton>
                                    </th>
                                    <th>
                                        <SortButton
                                            v-model:sortField="filterParams.sortField"
                                            v-model:sortOrder="filterParams.sortOrder"
                                            sortName="totalAmount"
                                            :text="adminType === 'vendor' ? $t('小计') : $t('订单金额')"
                                            @loadData="onSearchSubmit()"
                                        ></SortButton>
                                    </th>
                                    <th>{{ $t('会员名称') }}</th>
                                    <th>{{ $t('联系人') }}</th>
                                    <th style="text-align: start">{{ $t('物流/支付信息') }}</th>
                                    <th style="text-align: start">{{ $t('订单状态') }}</th>
                                    <th class="textR">{{ $t('操作') }}</th>
                                </tr>
                            </thead>
                        </table>
                        <table v-for="item in filterState" class="custom-table">
                            <thead>
                                <tr class="data-tr">
                                    <th colspan="6">
                                        <el-checkbox
                                            v-model="item.checkBox"
                                            class="check-box mr10"
                                            :disabled="isSelectionDisabled(item)"
                                            @change="onSelectChange(item)"
                                        ></el-checkbox>
                                        <!-- && adminType !== 'vendor' -->
                                        <template v-if="item.mark">
                                            <el-tooltip :disabled="!item.adminNote" :content="item.adminNote" effect="light" placement="top">
                                                <SignTag class="check-box tag" :value="item.mark"></SignTag>
                                            </el-tooltip>
                                        </template>

                                        <span class="check-box">{{ $t('订单编号：{orderSn}', { orderSn: item.orderSn }) }}</span>
                                        <el-divider direction="vertical" />
                                        <span class="check-box" v-if="item.outTradeNo">{{ $t('交易单号：{outTradeNo}', { outTradeNo: item.outTradeNo }) }}</span>
                                        <el-divider direction="vertical" v-if="item.outTradeNo" />
                                        <span class="check-box">{{ $t('下单时间：{addTime}', { addTime: item.addTime }) }}</span>
                                        <!--                                    <el-tooltip :content="'操作人：' + item.opAdminName" effect="light" placement="top">-->
                                        <!--                                        <el-tag class="ml10" disable-transitions effect="plain" size="small">代下单</el-tag>-->
                                        <!--                                    </el-tooltip>-->
                                        <DialogForm
                                            v-if="item.isCycleOrder || item.isCardOrder"
                                            :params="{ act: 'detail', id: item.orderId }"
                                            :showClose="false"
                                            :showOnOk="false"
                                            :title="item.items[0].productName"
                                            isDrawer
                                            path="order/cycleBuy/src/periodsList"
                                            :style="{ display: 'inline-block' }"
                                            width="1200px"
                                        >
                                            <template #title>
                                                <span class="cycleBuyTag">{{ item.isCycleOrder ? $t('周期购') : $t('次卡') }}</span
                                                >{{ item.items[0].productName }}
                                            </template>
                                            <Tag :text="item.isCycleOrder ? $t('周期购') : $t('次卡')"></Tag>
                                        </DialogForm>

                                        <DialogForm
                                            v-if="item.parentOrderId > 0 && adminType !== 'shop'"
                                            :params="{ act: 'parentDetail', id: item.orderId, isParent: true }"
                                            :showClose="false"
                                            :showOnOk="false"
                                            :title="$t('订单详情 ') + (item.parentOrderSn > 0 ? item.parentOrderSn : +item.parentOrderSn === 0 ? '' : item.orderSn)"
                                            isDrawer
                                            :style="{ display: 'inline-block' }"
                                            path="order/order/Info"
                                            width="1000px"
                                            @callback="(e) => subOrderCallback(e, item)"
                                        >
                                            <el-tooltip
                                                v-if="item.parentOrderId > 0"
                                                :content="$t('该订单已拆分，点击可查看父订单') + ''"
                                                effect="light"
                                                placement="top"
                                            >
                                                <Tag :text="$t('子订单')"></Tag>
                                            </el-tooltip>
                                        </DialogForm>

                                        <el-tooltip
                                            v-if="item.shopId > 0 && item.shop && isMerchant()"
                                            :content="$t('订单来自店铺：') + item.shop.shopTitle"
                                            effect="light"
                                            placement="top"
                                        >
                                            <Tag :transparent="false" :text="$t('店铺')"></Tag>
                                        </el-tooltip>
                                        <el-tooltip v-if="item.orderSource" :content="$t('订单来自') + item.orderSource" effect="light" placement="top">
                                            <Tag :text="item.orderSource" :transparent="true" style="text-transform: uppercase"></Tag>
                                        </el-tooltip>
                                        <el-tooltip v-if="isCycleMainOrder(item)" :content="$t('周期购主订单')" effect="light" placement="top">
                                            <Tag :text="$t('主')" :transparent="true"></Tag>
                                        </el-tooltip>
                                        <!--<el-tag class="ml10" disable-transitions effect="plain" size="small">-->
                                        <!--团购订单-->
                                        <!--</el-tag>-->
                                        <Tag v-if="item.isExchangeOrder" :transparent="true" color="red" :text="$t('积分兑换')"> </Tag>
                                        <el-tooltip
                                            v-if="item.payLog"
                                            :content="$t('订单使用') + payTypeName(item.payLog.payCode, item) + $t('下单')"
                                            effect="light"
                                            placement="top"
                                        >
                                            <Tag :transparent="true" color="blue" :text="payTypeName(item.payLog.payCode, item)"></Tag>
                                        </el-tooltip>
                                    </th>
                                    <th class="textR">
                                        <div style="gap: 8px; display: flex; justify-content: right">
                                            <DialogForm
                                                :params="{ act: 'detail', id: item.orderId }"
                                                :showClose="false"
                                                :showOnOk="false"
                                                :title="$t('订单详情 ') + item.orderSn"
                                                isDrawer
                                                path="order/order/Info"
                                                width="1000px"
                                                @callback="loadFilter"
                                            >
                                                <el-button bg class="buttonColor mr10" size="small" text type="primary"> {{ $t('查看详情') }}</el-button>
                                            </DialogForm>

                                            <DialogForm
                                                v-if="hasAftersales(item)"
                                                :params="{ act: 'detail', id: getAftersaleId(item) }"
                                                :showClose="false"
                                                :showOnOk="false"
                                                :title="$t('售后详情 ') + (item.aftersalesSn || '')"
                                                isDrawer
                                                path="order/aftersales/Info"
                                                width="800px"
                                            >
                                                <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('售后详情') }} </el-button>
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
                                            <ProductCard
                                                :picThumb="product.picThumb"
                                                :productId="product.productId"
                                                :productName="product.productName"
                                                :productType="product.productType"
                                                :url="product.url"
                                                :skuData="(product as any).skuData"
                                            >
                                                <template #text-pre>
                                                    <span class="pre-text" v-if="!isStore() && !isOverseas() && item.isPresale">{{ $t('预售商品') }}</span>
                                                </template>
                                            </ProductCard>
                                            <div class="displayColumn textR whiteSN widthAuto">
                                                <div>{{ priceFormat(adminType === 'vendor' ? getSupplierOrderItemPrice(product) : product.price) }}</div>
                                                <div>× {{ product.quantity }}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textR fz14">
                                            <div>{{ priceFormat(adminType === 'vendor' ? getSupplierOrderTotal(item) : item.totalAmount) }}</div>
                                            <div class="gray">{{ $t('(含运费：{shippingFee})', { shippingFee: item.shippingFee }) }}</div>
                                            <div class="gray" v-if="getOrderTaxFee(item) > 0">{{ $t('(税费：{getOrderTaxFee})', { getOrderTaxFee: getOrderTaxFee(item) }) }}</div>
                                            <!-- <div v-if="item.shopId > 0" class="fz12">
                                                <div>
                                                    <p>
                                                        佣金：<a
                                                            :href="'storeCommissionLog.html?act=list&shopId=' + item.shopId"
                                                            class="green"
                                                            target="_blank"
                                                            >已结算</a
                                                        >
                                                    </p>
                                                </div>
                                                <div>
                                                    <p>佣金：<span class="gray">未结算</span></p>
                                                </div>
                                            </div> -->
                                            <div>
                                                <DialogForm
                                                    v-if="item.status === 6 || item.status === 7"
                                                    :params="{ act: 'detail', id: item.aftersaleId }"
                                                    isDrawer
                                                    path="order/aftersales/Info"
                                                    :title="$t('售后详情 ') + item.aftersalesSn"
                                                    width="800px"
                                                    @okCallback="loadFilter"
                                                    :showClose="false"
                                                    :showOnOk="false"
                                                >
                                                    <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('售后详情') }} </el-button>
                                                </DialogForm>
                                                <!-- <el-button size="small" text type="primary"> 售后详情 </el-button> -->
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textL">
                                            <div>
                                                <span>{{ item.user?.username || "--" }}</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn fz12 textL" v-if="(!item.isPickup || item.shippingTypeId == 3) && item.orderSource !== 'pos'">
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
                                            <div class="gray">
                                                {{ $t('邮政编码：') }} <span class="black">{{ item.addressData?.postcode || item.postcode || "--" }}</span>
                                            </div>
                                        </div>
                                        <div
                                            class="displayColumn fz12 textL"
                                            v-if="item.isPickup && product.productType !== 5 && product.productType !== 6 && item.orderSource !== 'pos'"
                                        >
                                            <span class="black"> {{ $t('自提订单') }} </span>
                                            <div class="gray">
                                                {{ $t('联系人：') }}
                                                <span class="black">{{ maskString(item.userPickupInfo?.consignee || item.consignee || "--", 1, 1) }}</span>
                                            </div>
                                            <div class="gray">
                                                {{ $t('手机号：') }}
                                                <span class="black"><MobileCard :mobile="item.userPickupInfo?.mobile || item.mobile" /></span>
                                            </div>
                                        </div>
                                        <div class="displayColumn fz12 textL" v-if="item.isPickup && product.productType === 5 && item.orderSource !== 'pos'">
                                            <span class="black"> {{ $t('预约上门订单') }} </span>
                                            <div class="gray">
                                                {{ $t('联系人：') }}
                                                <span class="black">{{ maskString(item.userPickupInfo?.consignee || item.consignee || "--", 1, 1) }}</span>
                                            </div>
                                            <div class="gray">
                                                {{ $t('手机号：') }}
                                                <span class="black"><MobileCard :mobile="item.userPickupInfo?.mobile || item.mobile" /></span>
                                            </div>
                                        </div>
                                        <div class="displayColumn fz12 textL" v-if="item.isPickup && product.productType === 6 && item.orderSource !== 'pos'">
                                            <span class="black"> {{ $t('预约到店订单') }} </span>
                                            <div class="gray">
                                                {{ $t('联系人：') }}
                                                <span class="black">{{ maskString(item.userPickupInfo?.consignee || item.consignee || "--", 1, 1) }}</span>
                                            </div>
                                            <div class="gray">
                                                {{ $t('手机号：') }}
                                                <span class="black"><MobileCard :mobile="item.userPickupInfo?.mobile || item.mobile" /></span>
                                            </div>
                                        </div>
                                        <div class="displayColumn fz12 textL" v-if="item.orderSource === 'pos'">
                                            <span class="black"> {{ $t('收银台订单') }} </span>
                                        </div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <div class="displayColumn textL">
                                            <div>
                                                <span>{{ item.payTypeId === 1 ? $t('在线支付') : item.payTypeId === 2 ? $t('货到付款') : $t('线下支付') }}</span>
                                            </div>
                                            <div v-if="item.logisticsName">
                                                {{ item.shippingTypeName }}
                                                <span class="gray" v-if="item.shippingTypeId !== 3">{{ "" + item.logisticsName + "" }}</span>
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
                                            <!-- 拼团失败显示- -->
                                            {{ item.joinStatus && item.joinStatus === 3 ? "-" : item.orderStatusName }}
                                        </span>
                                        <div v-if="item.cancelRemark?.trim()" class="fz12 cancel-reason">
                                            {{ item.cancelRemark }}
                                        </div>
                                        <div v-if="item.afterSalesIng" class="aftersales-tag">售后中</div>
                                    </td>
                                    <td v-if="index == 0" :rowspan="item.items.length">
                                        <!-- 已付款，待成团不显示操作按钮 -->
                                        <div class="buttonStyle" v-if="(item.joinStatus === 1 && item.orderStatus === 1) || item.joinStatus === 3">-</div>
                                        <div class="buttonStyle" v-else>
                                            <DialogForm
                                                v-if="item.availableActions.cancelOrder"
                                                :params="{ act: 'cancelOrder', id: item.orderId }"
                                                isDrawer
                                                path="order/order/src/Operation"
                                                :title="$t('取消订单')"
                                                width="600px"
                                                @okCallback="loadFilter"
                                            >
                                                <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('取消订单') }} </el-button>
                                            </DialogForm>
                                            <el-button
                                                v-if="item.availableActions.delOrder"
                                                bg
                                                size="small"
                                                text
                                                type="primary"
                                                @click="onDelClick(item.orderId)"
                                            > {{ $t('删除订单') }} </el-button>
                                            <DialogForm
                                                :params="{ act: 'detail', id: item.orderId }"
                                                :title="$t('订单发货 ') + item.orderSn"
                                                isDrawer
                                                path="order/order/src/ToShip"
                                                width="900px"
                                                @okCallback="loadFilter"
                                                v-if="canShowDeliverButton(item)"
                                            >
                                                <el-button bg size="small" text type="danger"> {{ $t('去发货') }} </el-button>
                                            </DialogForm>
                                            <!-- 其他订单：使用原有备货功能 -->
                                            <DeleteRecord
                                                v-else-if="
                                                    isStore() &&
                                                    item.availableActions.deliver &&
                                                    canCurrentAdminShipOrder(item) &&
                                                    item.orderType !== 9 &&
                                                    item.orderType !== 10 &&
                                                    (item.isPickup || item.shippingTypeId === 3)
                                                "
                                                :params="{
                                                    id: item.orderId,
                                                    shippingMethod: 3,
                                                    deliverData: deliverDataFun(item.items),
                                                    LogisticsId: 0,
                                                    trackingNo: ''
                                                }"
                                                :requestApi="orderDeliver"
                                                :title="$t('该订单备货完成吗？')"
                                                :message="$t('备货完成')"
                                                @afterDelete="loadFilter"
                                            >
                                                <el-button bg size="small" text type="danger"> {{ $t('去备货') }} </el-button>
                                            </DeleteRecord>
                                            <el-button
                                                v-if="!item.isPickup && item.availableActions.confirmReceipt && canShowOrderReceiptEntry(item.shippingTypeId)"
                                                bg
                                                size="small"
                                                text
                                                type="danger"
                                                @click="onReceiptClick(item.orderId)"
                                            >
                                                {{ item.shippingTypeId === 3 ? $t('完成配送') : $t('确认收货') }}
                                            </el-button>

                                            <div v-if="item.isChangeOrderStatus">
                                                <DialogForm
                                                    v-if="item.availableActions.deliver"
                                                    :params="{ id: item.orderId, preOrderStatus: item.preOrderStatus }"
                                                    isDrawer
                                                    path="order/order/src/ChangeStatus"
                                                    :title="$t('取消订单')"
                                                    width="600px"
                                                    @okCallback="loadFilter"
                                                >
                                                    <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('取消订单') }} </el-button>
                                                </DialogForm>
                                                <DialogForm
                                                    v-if="item.availableActions.confirmReceipt"
                                                    :params="{ id: item.orderId, preOrderStatus: item.preOrderStatus }"
                                                    isDrawer
                                                    path="order/order/src/ChangeStatus"
                                                    :title="getCancelDeliverText(item)"
                                                    width="600px"
                                                    @okCallback="loadFilter"
                                                >
                                                    <el-button bg class="buttonColor" size="small" text type="primary">
                                                        {{ getCancelDeliverText(item) }}
                                                    </el-button>
                                                </DialogForm>
                                            </div>

                                            <!-- <el-button v-if="item.shippingTypeId == 3" bg class="buttonColor"
                                                size="small" text type="primary">
                                                完成配送
                                            </el-button> -->
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
        <el-dialog v-model="memberTagDialogVisible" title="按会员标签筛选订单" width="900px" append-to-body destroy-on-close
            @close="onMemberTagDialogClose">
            <FilterTagSelector
                ref="orderTagSelectorRef"
                :initial-tag-ids="filterParams.memberTagIds || []"
                :initial-match-mode="filterParams.memberTagMatchMode === 1 ? 'all' : 'any'"
            />
            <template #footer>
                <el-button @click="memberTagDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmMemberTags">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { Pagination, ProductCard, DeleteRecord } from "@/components/list";
import { message, Modal } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { OrderFilterParams, OrderFilterState } from "@/types/order/order.d";
import { batchSubmit, getOrderList, getOrderStatusCount, operationOrder, getOrderPageConfig, changeOrderStatus, remindDeliver, orderDeliver } from "@/api/order/order";
import { applyOrderStatusCount, buildOrderStatusCountParams } from "@/utils/orderStatusCount";
import { SelectShop, SelectLogisticsCompany, SelectVendor } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { PopSelect } from "@/components/pop-form";
import SortButton from "../../../components/list/src/SortButton.vue";
import dayjs from "dayjs";
import { priceFormat } from "@/utils/format";
import { getSupplierOrderItemPrice, getSupplierOrderItemSubtotal } from "@/utils/orderSupplierPrice";
import { useRoute, useRouter } from "vue-router";
import { Tag } from "@/components/form";
import { SelectTimeInterval } from "@/components/select";
import { isMerchant, isS2b2c, isStore, isPro, isOverseas } from "@/utils/version";
import MobileCard from "@/components/list/src/MobileCard.vue";
import EditSign from "@/views/order/order/src/EditSign.vue";
import SignTag from "@/views/order/order/src/SignTag.vue";
import FilterTagSelector from "@/views/user/user/FilterTagSelector.vue";
import { getMemberTagGroupList } from "@/api/user/userTags";
import type { UserTagFilterState } from "@/types/user/userTags.d";
import { maskString } from "@/utils/util";
import { hasEnabledPrint, triggerPrint } from "@/api/setting/receiptPrint";
import { getShopType, getAdminType } from "@/utils/storage";
import StockUpWithDispatch from "@/components/organize/StockUpWithDispatch.vue";
import { useServiceConfigStore } from "@/store/serviceStaffConfig";
import { getOrderShippingFeeLabel } from "@/utils/orderShippingFee";
import { canCurrentShopWriteOffOrder, hasVendorOrderIdentity, isPlatformSelfOrder } from "@/utils/orderShipPermission";
import { executeMeituanRecall } from "./meituanRecall";
import { getMeituanWaitingText as buildMeituanWaitingText } from "./meituanWaiting";
import { MEMBER_TAG_AUDIENCE_READY } from "@/utils/memberAudienceGate";
import { canShowOrderReceiptEntry } from "@/utils/orderReceiptGate";

/** 当前管理员类型：admin-平台管理员 / shop-店铺管理员 / vendor-供应商 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = ref(localStorage.getItem("adminType"));
console.log(adminType.value);
const config: any = useConfigStore();

/** 订单状态Tab列表配置，count 为角标数量 */
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

/** 发货状态筛选选项 */
const shippingStatusList = reactive([
    { value: 0, label: !isStore() ? t('待发货') : t('待发货/待备货') },
    { value: 1, label: !isStore() ? t('已发货') : t('已发货/待核销') },
    { value: 2, label: t('已收货') },
    { value: 3, label: t('配送中') },
    { value: 4, label: t('配送失败') }
]);

/** 支付状态筛选选项 */
const payStatusList = reactive([
    { value: 0, label: t('未付款') },
    { value: 1, label: t('支付中') },
    { value: 2, label: t('已付款') },
    { value: 3, label: t('退款中') },
    { value: 4, label: t('已退款') }
]);

/**
 * 支付方式筛选选项
 * @description 跨境版额外包含Stripe支付
 */
const payTypeList = computed(() => {
    const list = [
        { value: "balance", label: t('余额支付') },
        { value: "wechat", label: t('微信支付') },
        { value: "alipay", label: t('支付宝') },
        { value: "paypal", label: "PayPal" },
        { value: "applepay", label: "Apple Pay" },
        { value: "huifuWechat", label: t('汇付-微信支付') },
        { value: "huifuAlipay", label: t('汇付-支付宝') },
        { value: "lakalaWechat", label: t('拉卡拉-微信支付') },
        { value: "lakalaAlipay", label: t('拉卡拉-支付宝') },
        // { value: "yabanpay_wechat", label: t('YaBandPay微信') },
        // { value: "yabanpay_alipay", label: t('YaBandPay支付宝') },
        // { value: "yunpay_wechat", label: t('云支付微信') },
        // { value: "yunpay_alipay", label: t('云支付支付宝') },
        // { value: "yunpay_yunshanfu", label: t('云支付云闪付') },
        { value: "cash", label: t('现金') }
    ];
    // 只有跨境版才有 Stripe / Antom / Xendit 选项
    return list;
});

/**
 * 获取支付方式显示名称
 * @description 组合显示支付方式+积分抵扣+余额支付
 * @param {string} payType - 支付方式代码
 * @param {any} item - 订单项数据
 * @returns {string} 支付方式显示文本
 */
const payTypeName = (payType: string, item: any) => {
    // 预售定金使用余额支付时，订单列表只显示“余额”，避免重复显示余额支付+余额
    if (item?.isPresale && payType === "balance") return t("余额");
    let payTypeStr = "";
    let payTypeObj = payTypeList.value.find((item) => item.value === payType);
    payTypeStr = payTypeObj ? payTypeObj.label : "";
    item.usePoints ? (payTypeStr += t('+积分抵扣')) : "";
    item.balance ? (payTypeStr += t('+余额')) : "";
    return payTypeStr;
};

const getOrderTaxFee = (item: any) => Number(item?.taxRateAmount ?? item?.taxFee ?? item?.taxAmount ?? item?.orderTaxFee ?? 0);

const getSupplierOrderTotal = (item: any) =>
    Array.isArray(item?.items) ? item.items.reduce((total: number, product: any) => total + getSupplierOrderItemSubtotal(product), 0) : 0;

/** 是否为周期购主订单（1: 主订单，0: 非主订单） */
const isCycleMainOrder = (item: any) => +item?.isCycleOrder === 1;

const isPlatformAdmin = computed(() => adminType.value === "admin");

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
            adminType.value !== "vendor" && !item.vendorId &&
            canCurrentAdminShipOrder(item) &&
            item?.shippingTypeId !== 3 &&
            ![9, 10].includes(+item?.orderType)) ||
        (item?.availableActions?.deliver && adminType.value === "vendor")
    );
};

/** 订单标记颜色选项列表 */
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

/**
 * @description 订单状态Tab切换处理
 * @param value 订单状态值（-1全部,0待支付,1待发货,2已发货,3已取消,4无效,5已完成,-2已删除）
 */
const onTabChange = (value: number) => {
    filterParams.orderStatus = value;
    filterParams.page = 1;
    selectedIds.value = [];
    loadFilter();
};
/** 订单列表数据 */
const filterState = ref<OrderFilterState[]>([]);
/** 列表加载状态 */
const loading = ref<boolean>(true);
/** 订单总数 */
const total = ref<number>(0);
/** 已选中的订单ID列表 */
const selectedIds = ref<number[]>([]);
/** 全选状态 */
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
/** 筛选参数 */
const filterParams = reactive<OrderFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "orderId",
    sortOrder: "desc",
    keyword: "",
    outTradeNo: "",
    orderStatus: -1,
    address: "",
    email: "",
    mobile: "",
    addTime: [],
    addEndTime: "",
    addStartTime: "",
    logisticsId: "",
    shopId: "",
    vendorId: "",
    payCode: "",
    mark: -1,
    memberTagIds: [],
    memberTagMatchMode: 0
});

/** 会员标签筛选弹窗 */
const memberTagDialogVisible = ref(false);
const orderTagSelectorRef = ref();
/** 会员标签筛选下拉状态：all-全部 / specify-指定标签 */
const memberTagFilterType = ref("all");
/** 标签组数据源（用于已选标签名回显） */
const tagGroupOptions = ref<UserTagFilterState[]>([]);

/** 已选标签名列表 */
const selectedMemberTagNames = computed(() => {
    if (!filterParams.memberTagIds || filterParams.memberTagIds.length === 0) return [];
    const tagIds = new Set(filterParams.memberTagIds);
    return tagGroupOptions.value
        .flatMap((group) => group.tags || [])
        .filter((tag) => tag.id && tagIds.has(tag.id))
        .map((tag) => tag.tagName)
        .filter((name): name is string => !!name);
});

/** 下拉「指定标签」项的展示文案 */
const memberTagFilterDisplayText = computed(() => {
    if (!filterParams.memberTagIds || filterParams.memberTagIds.length === 0) {
        return "指定标签";
    }
    if (selectedMemberTagNames.value.length === 0) {
        return `指定标签（已选${filterParams.memberTagIds.length}项）`;
    }
    if (selectedMemberTagNames.value.length <= 2) {
        return `指定标签：${selectedMemberTagNames.value.join("、")}`;
    }
    return `指定标签：${selectedMemberTagNames.value.slice(0, 2).join("、")}等${selectedMemberTagNames.value.length}项`;
});

const openMemberTagDialog = () => {
    memberTagFilterType.value = "specify";
    memberTagDialogVisible.value = true;
};

const onMemberTagFilterChange = (val: string) => {
    if (val === "specify") {
        memberTagDialogVisible.value = true;
        return;
    }
    filterParams.memberTagIds = [];
    filterParams.memberTagMatchMode = 0;
    onSearchSubmit();
};

const onMemberTagDialogClose = () => {
    if (!filterParams.memberTagIds || filterParams.memberTagIds.length === 0) {
        memberTagFilterType.value = "all";
    }
};

const confirmMemberTags = () => {
    const r = orderTagSelectorRef.value?.getResult?.();
    if (r) {
        filterParams.memberTagIds = r.tagIds;
        filterParams.memberTagMatchMode = r.matchMode === "all" ? 1 : 0;
    }
    memberTagDialogVisible.value = false;
    memberTagFilterType.value = filterParams.memberTagIds && filterParams.memberTagIds.length > 0 ? "specify" : "all";
    onSearchSubmit();
};

const loadMemberTagGroups = async () => {
    try {
        const res = await getMemberTagGroupList({ page: 1, size: 100, sortField: "", sortOrder: "" });
        tagGroupOptions.value = res.records || [];
    } catch (_) {
        /* ignore */
    }
};

/**
 * 重置筛选参数
 * @description 将所有筛选条件恢复为默认值并重新加载列表
 */
const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = config.get("pageSize");
    filterParams.sortField = "orderId";
    filterParams.sortOrder = "desc";
    filterParams.keyword = "";
    filterParams.outTradeNo = "";
    filterParams.orderStatus = -1;
    filterParams.address = "";
    filterParams.email = "";
    filterParams.mobile = "";
    filterParams.shopId = "";
    filterParams.addTime = [];
    filterParams.addEndTime = "";
    filterParams.addStartTime = "";
    filterParams.shippingStatus = "";
    filterParams.logisticsId = "";
    filterParams.payStatus = "";
    filterParams.vendorId = "";
    filterParams.payCode = "";
    filterParams.mark = -1;
    filterParams.memberTagIds = [];
    filterParams.memberTagMatchMode = 0;
    memberTagFilterType.value = "all";
    loadFilter();
};

/** 是否启用小票打印功能 */
const hasPrint = ref(false);
/**
 * @description 获取订单列表数据
 * 为每条记录添加checkBox属性用于多选
 */
/**
 * @description 获取订单状态Tab角标数量
 * 与列表请求并行，失败时角标归零静默降级，不阻塞列表
 */
const loadStatusCount = async () => {
    try {
        const result = await getOrderStatusCount(buildOrderStatusCountParams({ ...filterParams }));
        applyOrderStatusCount(orderStatusList, result);
    } catch (error) {
        applyOrderStatusCount(orderStatusList, null);
    }
};
const loadFilter = async (rethrowError: boolean | Event = false) => {
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
        // 重新叫单需要获知刷新是否成功，才能给出准确的恢复提示；其它调用保持原有页内提示行为。
        if (rethrowError === true) throw error;
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const subOrderCallback = (result: any, item: any) => {
    if (result.changeTitle) {
        item.parentOrderSn = result.orderSn;
    } else {
        loadFilter();
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
 * 批量打印小票
 * @param {number[]} ids - 订单ID数组
 */
const handlePrint = async (ids: number[]) => {
    loadingPrint.value = true;

    try {
        let res = await triggerPrint({ ids: ids });
        message.success(t('正在打印中~'));
    } catch (error) {
        console.log(error);
    } finally {
        loadingPrint.value = false;
    }
};

/** 组件属性定义 */
const props = defineProps({ id: { type: Number, default: 0 }, act: { type: String, default: "" }, isDialog: Boolean });
const route = useRoute();
const query = useRouter().currentRoute.value.query;
/** 当前操作类型 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
/** 当前操作ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** 服务配置Store（同城配送/上门服务派单配置），仅门店版使用 */
const serviceConfigStore = isStore() ? useServiceConfigStore() : null;
const meituanWaitingNow = ref(Date.now());
let meituanWaitingTimer: ReturnType<typeof setInterval> | undefined;

onMounted(() => {
    meituanWaitingTimer = setInterval(() => {
        meituanWaitingNow.value = Date.now();
    }, 1000);
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
    _hasEnabledPrint();
    if (MEMBER_TAG_AUDIENCE_READY) {
        loadMemberTagGroups();
    }
    // 加载同城配送和上门服务配置（用于判断派单模式），仅门店版
    serviceConfigStore?.loadAllConfig();
});

onBeforeUnmount(() => {
    if (meituanWaitingTimer) clearInterval(meituanWaitingTimer);
});
/**
 * 执行搜索
 * @description 重置页码并重新加载列表
 */
const onSearchSubmit = () => {
    filterParams.page = 1;
    loadFilter();
};
const router = useRouter();
/**
 * 新窗口打开页面
 * @param {string} href - 相对路径
 */
const openPage = (href: string) => {
    let base = location.origin + router.options.history.base + href;
    window.open(base, "_blank");
};
/**
 * 判断订单是否申请过售后
 * @description 订单行上存在 aftersalesItem 即申请过（含被拒、已取消）。
 * 不能用 afterSalesStatus / afterSalesIng：售后单全部为终态且无一条 COMPLETE 时，
 * 二者会回落成「未售后」，与从未申请过无法区分
 * @param item 订单行数据
 */
const hasAftersales = (item: OrderFilterState) => {
    return item.items?.some((product) => !!product.aftersalesItem) ?? false;
};
/**
 * 取订单对应的售后单 ID
 * @description 商品行 aftersalesItem 由后端按 aftersaleId 升序后取最后一条（最新售后）。
 * 多商品行时取其中最大 aftersaleId，再回退订单级 aftersaleId。
 * @param item 订单行数据
 */
const getAftersaleId = (item: OrderFilterState) => {
    const ids = (item.items ?? [])
        .map((product) => product.aftersalesItem?.aftersaleId)
        .filter((id): id is number => typeof id === "number" && id > 0);
    if (ids.length) {
        return Math.max(...ids);
    }
    return item.aftersaleId;
};
/**
 * @description 删除订单（软删除，可恢复）
 * @param id 订单ID
 */
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
/**
 * @description 确认收货操作
 * @param id 订单ID
 */
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
/**
 * @description 催促供应商发货
 * @param id 订单ID
 */
const _remindDeliver = (id: any) => {
    Modal.confirm({
        title: t('确认催发货该订单吗？'),
        onOk: async () => {
            try {
                const result = await remindDeliver({ id: id });
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

/** 计算全选框的半选状态 */
const isIndeterminate = computed(() => {
    const selectableOrderIds = selectableOrders.value.map((item) => item.orderId).filter(Boolean);
    const selectedSelectableCount = selectedIds.value.filter((id) => selectableOrderIds.includes(id)).length;
    selectAll.value = selectableOrderIds.length > 0 && selectedSelectableCount === selectableOrderIds.length;
    return selectedSelectableCount > 0 && selectedSelectableCount < selectableOrderIds.length;
});
/** 全选/取消全选处理 */
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
/** 单个订单选中状态变更处理 */
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
 * 判断订单是否为可派单订单
 * @description 同城配送或上门服务订单，且对应服务开启了派单模式
 * @param {any} item - 订单数据
 * @returns {boolean} 是否可派单
 */
const isDispatchableOrder = (item: any): boolean => {
    // 同城配送：shippingTypeId === 3
    if (item.shippingTypeId === 3) {
        // 需要同城配送开启且为派单模式
        return !!serviceConfigStore?.isIntraCityDeliveryEnabled && !!serviceConfigStore?.isIntraCityDeliveryDispatchMode;
    }

    // 上门服务：isPickup && (productType === 5 || productType === 6)
    if (item.isPickup && item.items && item.items.length > 0) {
        const productType = item.items[0].productType;
        if (productType === 5 || productType === 6) {
            // 需要上门服务开启且为派单模式
            return !!serviceConfigStore?.isHomeServiceEnabled && !!serviceConfigStore?.isHomeServiceDispatchMode;
        }
    }

    return false;
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

/**
 * 获取“取消发货/取消备货”按钮文本
 * @description 预约上门订单在待核销状态下显示“取消备货”，其他订单保持“取消发货”
 */
const getCancelDeliverText = (item: any): string => {
    const productType = item?.items?.[0]?.productType;
    const isAppointmentHomeOrder = !!item?.isPickup && productType === 5;
    // 待核销状态：列表中该状态会开放 confirmReceipt（去核销）操作
    if (isAppointmentHomeOrder && item?.availableActions?.confirmReceipt) return t('取消备货');
    return t('取消发货');
};
/**
 * 判断是否为上门服务且需要派单
 * @description 派单模式且未派单的上门服务订单
 * @param {any} item - 订单数据
 * @returns {boolean} 是否需要派单
 */
const isHomeServiceNeedDispatch = (item: any): boolean => {
    if (!item.deliveryOrderInfo) return false;
    // 判断当前是否启用了派单模式
    if (!serviceConfigStore?.isHomeServiceDispatchMode) return false;
    // 上门服务（type=2）且未派单（staffId为null）
    return item.deliveryOrderInfo.type === 2 && !item.deliveryOrderInfo.staffId;
};

/**
 * 判断是否为同城配送且需要派单
 * @description 派单模式且未派单的同城配送订单
 * @param {any} item - 订单数据
 * @returns {boolean} 是否需要派单
 */
const isDeliveryNeedDispatch = (item: any): boolean => {
    if (!item.deliveryOrderInfo) return false;
    // 判断当前是否启用了派单模式
    if (!serviceConfigStore?.isIntraCityDeliveryDispatchMode) return false;
    // 同城配送（type=1）且未派单（staffId为null）
    return item.deliveryOrderInfo.type === 1 && !item.deliveryOrderInfo.staffId;
};

/**
 * 构造发货数据
 * @description 将订单商品项转换为发货数据格式
 * @param {any[]} items - 订单商品项列表
 * @returns {Array} 发货数据数组
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
 * 是否为可操作的美团（第三方）配送订单
 * @description 门店版下，美团运单（carrierType=2）且当前管理员有该订单发货权限
 *（门店管理员看本店订单、平台管理员看平台自营门店订单）
 */
const isMeituanDeliveryOrder = (item: any): boolean => {
    return isStore() && item?.deliveryOrderInfo?.carrierType === 2 && canCurrentAdminShipOrder(item);
};

/**
 * 是否为美团配送「未接单」态
 * @description mtStatus=0（待调度/超时未接单），此时可重新叫单或转商家自送
 */
const isMeituanUnaccepted = (item: any): boolean => {
    return isMeituanDeliveryOrder(item) && item?.deliveryOrderInfo?.mtStatus === 0;
};

/**
 * 已发货待配送等待时长文案
 * @description 基于 mtDispatchTime（日期时间字符串或时间戳）计算分钟差
 */
const getMeituanWaitingText = (item: any): string => {
    return buildMeituanWaitingText(item?.deliveryOrderInfo?.mtDispatchTime, meituanWaitingNow.value, item?.deliveryOrderInfo?.status == 30);
};

const getOperationErrorMessage = (error: unknown): string => {
    if (error instanceof Error) return error.message;
    if (typeof error === "object" && error && "message" in error && typeof error.message === "string") return error.message;
    return "未知错误";
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

    .cancel-reason {
        color: #eeab03;
    }

    .meituan {
        color: #eeab03;
    }

    .gray {
        color: #999999;
    }

    .meituan-delivery-info {
        margin-top: 6px;
        display: flex;
        flex-direction: column;
        gap: 2px;

        .mt-tag-line {
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .mt-status {
            font-size: 12px;
            color: #333333;
        }
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

.pre-text {
    color: var(--el-color-primary);
    display: inline-block;
    margin-right: 8px;
}

.cycleBuyTag {
    color: #000;
    position: relative;
    margin-right: 8px;
    &::after {
        content: "";
        position: absolute;
        right: -4.5px;
        top: 50%;
        transform: translateY(-45%);
        width: 2px;
        height: 70%;
        background-color: #000;
    }
}
</style>
