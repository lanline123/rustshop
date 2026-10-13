<template>
    <div class="proxy-shopping-check" style="position: relative">
        <a-spin :spinning="loading">
            <el-row :gutter="12">
                <!-- 信息填写（全宽） -->
                <el-col :xs="24" :md="24">
                    <!-- 客户信息 -->
                    <div class="container-card">
                        <div class="card-head">
                            <div class="back-btn" role="button" tabindex="0" @click="onBackToProxyShopping"
                                @keydown.enter.prevent="onBackToProxyShopping"
                                @keydown.space.prevent="onBackToProxyShopping">
                                <el-icon class="back-arrow">
                                    <Back />
                                </el-icon>
                                <span class="back-text">{{ $t('返回') }}</span>
                            </div>
                            <div class="title">{{ $t('下单客户') }}</div>
                        </div>
                        <div class="user-info-card">
                            <div class="info-card">
                                <div class="user-info-card-left">
                                    <el-avatar :size="52" :src="imageFormat(userInfo?.avatar)"></el-avatar>
                                    <div class="card-info">
                                        <div class="name">
                                            {{ userInfo?.nickname || userInfo?.username || "-" }}
                                        </div>
                                        <div>{{ $t('手机号：') }}<MobileCard :mobile="userInfo?.mobile"></MobileCard>
                                        </div>
                                    </div>
                                </div>
                                <div class="user-info-card-right">
                                    <div>
                                        <span class="label">{{ $t('可用余额：') }}</span>
                                        <span class="value">{{ priceFormat(Number(userInfo?.balance || 0)) }}</span>
                                    </div>
                                    <div>
                                        <span class="label">{{ $t('可用{integralName}：', { integralName }) }}</span>
                                        <span class="value">{{ userInfo?.points ?? 0 }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 订单信息 -->
                    <div class="container-card">
                        <div class="title">{{ $t('订单信息') }}</div>
                        <div class="block order-table-block">
                            <div class="order-table-inner">
                                <el-table :data="productListForTable" :fit="false">
                                    <el-table-column :label="$t('商品名称')" min-width="200">
                                        <template #default="{ row }">
                                            <div class="product-cell">
                                                <div class="product-thumb">
                                                    <img v-if="row.pic || row.picUrl || row.picThumb || row.productPic"
                                                        class="product-thumb-img"
                                                        :src="imageFormat(row.pic || row.picUrl || row.picThumb || row.productPic)"
                                                        alt="" />
                                                    <div v-else class="product-thumb-placeholder" />
                                                </div>
                                                <div class="product-info">
                                                    <div class="product-name">{{ row.productName || "-" }}</div>
                                                    <div v-if="formatSkuDataText(row.skuData)" class="gray">{{
                                                        formatSkuDataText(row.skuData) }}</div>
                                                    <div v-if="row.extraSkuData && row.extraSkuData.length"
                                                        class="extra-service-box">
                                                        <div v-for="(extra, idx) in row.extraSkuData"
                                                            :key="String(extra?.attributesId ?? idx)"
                                                            class="extra-service-row">
                                                            <div class="extra-service-left">
                                                                <span class="extra-service-left-text">
                                                                    {{ (extra?.attrName || "-") + "｜" +
                                                                        (extra?.attrValue ||
                                                                    "-") }}
                                                                </span>
                                                            </div>
                                                            <div class="extra-service-price">
                                                                {{
                                                                    extra?.attrPrice === null || extra?.attrPrice ===
                                                                        undefined
                                                                        ? "-"
                                                                        : priceFormat(Number(extra.attrPrice))
                                                                }}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('商品信息')" min-width="200">
                                        <template #default="{ row }">
                                            <div>
                                                <div style="white-space: nowrap">
                                                    <span class="gray">{{ $t('货号：') }}</span>
                                                    {{ row.productSn || "-" }}
                                                </div>
                                                <div>
                                                    <span class="gray">{{ $t('单价：') }}</span>
                                                    <span style="color: #fb0606">{{
                                                        priceFormat(getRowDisplayUnitPrice(row))
                                                        }}</span>
                                                </div>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('库存')">
                                        <template #default="{ row }">
                                            {{ row.stock ?? "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('数量')">
                                        <template #default="{ row }">
                                            {{ row.quantity ?? "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('小计')">
                                        <template #default="{ row }">
                                            {{ priceFormat(getRowDisplaySubtotal(row)) ?? "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('改价')" width="200">
                                        <template #default="{ row }">
                                            <div class="change-price-cell-render">
                                                <el-input :model-value="ensureRowChangeDraft(row).changePriceValue"
                                                    @input="(val: any) => onRowChangeValueInput(row, val)"
                                                    @update:model-value="(val: any) => onRowChangeValueInput(row, val)"
                                                    @change="() => onRowChangeValueChange(row)" type="number">
                                                    <template #prepend>
                                                        <el-select
                                                            :model-value="ensureRowChangeDraft(row).changePriceType"
                                                            class="change-price-select"
                                                            popper-class="change-price-popper"
                                                            @update:model-value="(val: any) => onRowChangeTypeChange(row, val)">
                                                            <template v-for="type in changePriceTypeList"
                                                                :key="type.value">
                                                                <el-option :label="type.label" :value="type.value" />
                                                            </template>
                                                        </el-select>
                                                    </template>
                                                    <template #suffix>
                                                        <div class="change-price-unit">
                                                            {{
                                                                changePriceUnitMap[ensureRowChangeDraft(row).changePriceType].unit
                                                            }}
                                                        </div>
                                                    </template>
                                                </el-input>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <template #empty>
                                        <div class="empty-warp">
                                            <div class="empty-bg">{{ $t('暂无商品') }}</div>
                                        </div>
                                    </template>
                                </el-table>
                            </div>
                        </div>

                        <div v-if="hasGift" class="block form-block gift-block">
                            <div class="block-title">{{ $t('赠品') }}</div>
                            <div class="block-content">
                                <div class="gift-cards">
                                    <div v-for="gift in giftListForDisplay" :key="getGiftItemKey(gift)"
                                        class="gift-card">
                                        <div class="product-thumb gift-thumb">
                                            <img v-if="gift.pic || gift.picUrl || gift.picThumb || gift.productPic"
                                                class="product-thumb-img"
                                                :src="imageFormat(gift.pic || gift.picUrl || gift.picThumb || gift.productPic)"
                                                alt="" />
                                            <div v-else class="product-thumb-placeholder" />
                                        </div>
                                        <div class="gift-info">
                                            <div class="gift-name">{{ gift.productName || "-" }}</div>
                                        </div>
                                        <div class="gift-num">x{{ getGiftNum(gift) }}</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="block form-block delivery-method-block">
                            <div class="block-title">{{ $t('配送方式') }}</div>
                            <div class="block-content">
                                <DeliveryMethod :cart-list="checkInfo?.cartList || []"
                                    :shipping-type-data="effectiveShippingTypeData"
                                    :allowed-type-ids="isStore() && !deliveryPreSelected ? null : allowedDeliveryTypeIds"
                                    :locked-type-id="lockedDeliveryTypeId" v-model:shippingType="formState.shippingType"
                                    @change="onDeliveryMethodChange" />
                            </div>
                        </div>

                        <template v-if="formState.deliveryType === 2">
                            <div class="block form-block address-block--pickup">
                                <div class="block-title">{{ $t('自提点') }}</div>
                                <div class="block-content">
                                    <div class="address-cards">
                                        <div v-for="point in pickupPointList"
                                            :key="Number(point?.pickupId || point?.shopId || 0)" class="address-card"
                                            :class="{ active: isActivePickupPoint(point) }"
                                            @click="onPickupPointClick(point)">
                                            <div class="addr-col-icon">
                                                <el-icon class="addr-ico">
                                                    <Location />
                                                </el-icon>
                                            </div>
                                            <div class="addr-col-content">
                                                <div class="pickup-line1">{{ getPickupPointName(point) }}</div>
                                                <div class="addr-line2 pickup-line2">{{ getPickupPointAddress(point) }}
                                                </div>
                                                <div class="addr-line3 pickup-line3">{{
                                                    getPickupPointBusinessHours(point) }}</div>
                                            </div>
                                        </div>
                                        <div v-if="pickupPointList.length === 0" class="gray">{{ $t('暂无可用自提点') }}</div>
                                    </div>
                                </div>
                            </div>

                            <div class="block form-block address-block--pickup">
                                <div class="block-title">{{ $t('自提联系人') }}</div>
                                <div class="block-content">
                                    <div class="address-cards pickup-contact-cards"
                                        ref="pickupContactCardsContainerRef">
                                        <DialogForm class="address-add-card" isDrawer :title="$t('新增自提联系人')" width="520px"
                                            path="order/proxyShopping/src/EditPickupContactDrawer"
                                            :params="{ userId: userId }" @okCallback="onPickupContactCreated">
                                            <div class="address-card address-card-add pickup-contact-card pickup-contact-card-add"
                                                role="button" tabindex="0">
                                                <el-icon class="add-ico">
                                                    <Plus />
                                                </el-icon>
                                                <div class="add-text">{{ $t('添加自提联系人') }}</div>
                                            </div>
                                        </DialogForm>

                                        <div v-for="contact in visiblePickupContactList"
                                            :key="Number(contact?.contactId || contact?.id || 0)"
                                            class="address-card pickup-contact-card"
                                            :class="{ active: isActivePickupContact(contact) }"
                                            @click="onPickupContactClick(contact)">
                                            <div class="addr-col-content">
                                                <div class="pickup-contact-line pickup-contact-name">
                                                    <el-icon class="pickup-contact-line-icon">
                                                        <User />
                                                    </el-icon>
                                                    <span class="pickup-contact-line-text">{{
                                                        getPickupContactName(contact) }}</span>
                                                </div>
                                                <div class="pickup-contact-line pickup-contact-phone">
                                                    <el-icon class="pickup-contact-line-icon">
                                                        <Phone />
                                                    </el-icon>
                                                    <span class="pickup-contact-line-text">{{
                                                        getPickupContactMobile(contact) || "-" }}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div v-if="showPickupContactExpandToggle" class="address-cards-footer">
                                        <el-button link type="primary" class="address-expand-toggle"
                                            @click="togglePickupContactListExpand">
                                            <span>{{ pickupContactListExpanded ? $t('收起') : $t('展开更多') }}</span>
                                            <el-icon class="address-expand-ico">
                                                <ArrowUp v-if="pickupContactListExpanded" />
                                                <ArrowDown v-else />
                                            </el-icon>
                                        </el-button>
                                    </div>
                                </div>
                            </div>
                        </template>
                        <div v-else class="block form-block">
                            <div class="block-title">{{ $t('收货地址') }}</div>
                            <div class="block-content">
                                <div class="address-cards" ref="addressCardsContainerRef">
                                    <DialogForm class="address-add-card" isDrawer :title="$t('新增收货地址')" width="600px"
                                        path="order/proxyShopping/src/EditAddressDrawer" :params="{ userId: userId }"
                                        @okCallback="onAddressCreated">
                                        <div class="address-card address-card-add" role="button" tabindex="0">
                                            <el-icon class="add-ico">
                                                <Plus />
                                            </el-icon>
                                            <div class="add-text"> {{ $t('添加代客下单地址') }} <el-popover placement="top" trigger="hover" :width="360"
                                                    popper-class="price-popover" :show-arrow="false">
                                                    <template #reference>
                                                        <el-icon class="help-ico add-help-ico" role="button"
                                                            tabindex="0" aria-label="提示：地址仅后台展示" @click.stop
                                                            @keydown.enter.stop.prevent @keydown.space.stop.prevent>
                                                            <QuestionFilled />
                                                        </el-icon>
                                                    </template>
                                                    <div class="add-address-tip">{{ $t('注意：此处添加的地址仅在后台展示，不会同步到用户端') }}</div>
                                                </el-popover>
                                            </div>
                                        </div>
                                    </DialogForm>

                                    <div v-for="addr in visibleAddressList" :key="addr.addressId || formatAddress(addr)"
                                        class="address-card" :class="{
                                            active: isActiveAddress(addr),
                                            'address-card--editable': Number(addr?.isSystem || 0) === 1
                                        }" @click="onAddressCardClick(addr)">
                                        <div class="addr-col-icon">
                                            <el-icon class="addr-ico">
                                                <Location />
                                            </el-icon>
                                        </div>
                                        <div class="addr-col-content">
                                            <div class="addr-line1">
                                                <span class="addr-region">{{ getRegionText(addr) }}</span>
                                            </div>
                                            <div class="addr-line2">{{ getFullAddressText(addr) }}</div>
                                            <div class="addr-line3">
                                                <span class="addr-consignee">{{ addr?.consignee || "-" }}</span>
                                                <span class="addr-mobile">{{ maskMobile(addr?.mobile ||
                                                    addr?.telephone || "") }}</span>
                                            </div>
                                        </div>

                                        <!-- isSystem=1：系统地址允许编辑。hover 时在底部显示操作条 -->
                                        <DialogForm v-if="Number(addr?.isSystem || 0) === 1" isDrawer :title="$t('编辑收货地址')"
                                            width="600px" path="order/proxyShopping/src/EditAddressDrawer"
                                            :params="{ userId: userId, address: addr }" @okCallback="onAddressUpdated">
                                            <div class="addr-action-bar">
                                                <span class="iconfont icon-bianji1"></span>
                                                <span class="txt">{{ $t('编辑') }}</span>
                                            </div>
                                        </DialogForm>
                                    </div>
                                </div>
                                <div v-if="showAddressExpandToggle" class="address-cards-footer">
                                    <el-button link type="primary" class="address-expand-toggle"
                                        @click="toggleAddressListExpand">
                                        <span>{{ addressListExpanded ? $t('收起') : $t('展开更多') }}</span>
                                        <el-icon class="address-expand-ico">
                                            <ArrowUp v-if="addressListExpanded" />
                                            <ArrowDown v-else />
                                        </el-icon>
                                    </el-button>
                                </div>
                            </div>
                        </div>

                        <div class="block form-block">
                            <div class="block-title">{{ $t('支付方式') }}</div>
                            <div class="block-content">
                                <PayType :pay-type-id="formState.payTypeId" :payment-type-list="paymentTypeList"
                                    @change="onPayTypeChange" />
                            </div>
                        </div>

                        <div class="block form-block">
                            <div class="block-title">{{ $t('优惠券') }}</div>
                            <div class="block-content">
                                <div class="coupon-row">
                                    <div class="coupon-action">
                                        <div class="coupon-action-line">
                                            <DialogForm isDrawer :title="$t('优惠券')" width="880px"
                                                path="order/proxyShopping/src/CouponDrawer" :params="{
                                                    couponList: checkInfo?.couponList || [],
                                                    userId: userId,
                                                    productIds: couponProductIds,
                                                    selectedCouponId: selectedCouponId,
                                                    enableUsePromotion: couponEnableUsePromotion,
                                                    afterClaimRefresh: afterClaimRefresh
                                                }" @okCallback="onCouponSelected">
                                                <el-button type="primary">{{ $t('选择优惠券') }}</el-button>
                                            </DialogForm>

                                            <div class="coupon-selected-meta" v-if="selectedCouponCount > 0">
                                                <span>{{ $t('已选择{selectedCouponCount}张优惠券', { selectedCouponCount }) }}</span>
                                            </div>
                                        </div>

                                        <div class="coupon-selected" v-if="selectedCouponForView">
                                            <div class="couponItem couponItem--selected">
                                                <el-icon class="coupon-remove-ico" role="button" tabindex="0"
                                                    aria-label="移除当前使用的优惠券" @click.stop="onClearCoupon"
                                                    @keydown.enter.prevent="onClearCoupon"
                                                    @keydown.space.prevent="onClearCoupon">
                                                    <CircleCloseFilled />
                                                </el-icon>
                                                <div class="couponItemLeft">
                                                    <div v-if="selectedCouponViewType === 1" class="price">
                                                        <span>{{ currency }}</span> {{ selectedCouponViewMoney || "-" }}
                                                    </div>
                                                    <div v-else-if="selectedCouponViewType === 2" class="price">{{ $t('{selectedCouponViewDiscount}折', { selectedCouponViewDiscount: selectedCouponViewDiscount ||
                                                        "-" }) }}</div>
                                                    <div v-else class="price">-</div>
                                                </div>
                                                <div class="couponItemRight">
                                                    <div class="title">{{ selectedCouponViewName }}</div>
                                                    <div class="scene">
                                                        <span>{{ selectedCouponViewType === 2 ? $t('折扣券') : $t('满减券') }}</span>
                                                        <span v-if="selectedCouponViewIsGlobal"
                                                            class="coupon-globle">{{ $t('全场券') }}</span>
                                                    </div>
                                                    <div class="date">
                                                        <span class="label">{{ $t('有效期至：') }}</span>
                                                        <span class="value">{{ selectedCouponViewEndDate }}</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="block form-block" v-if="canUsePoint">
                            <div class="block-title">{{ $t('使用{integralName}', { integralName }) }}</div>
                            <div class="block-content">
                                <div>
                                    <div class="inline-row">
                                        <el-checkbox v-model="usePointEnabled" :disabled="pointToggleDisabled"
                                            @change="onUsePointToggle">{{ $t('使用') }}</el-checkbox>
                                        <el-input v-model="usePointInput" class="ml8" style="width: 80px"
                                            :disabled="!usePointEnabled || pointToggleDisabled" inputmode="numeric"
                                            :placeholder="$t('请输入')" @change="onPointChange" />
                                        <span class="ml8">{{ integralName }}</span>
                                    </div>
                                    <div class="gray mt6 point-info">
                                        <div>{{ $t('当前账号{integralName}：', { integralName }) }}<span class="point-num">{{ userInfo?.points ?? 0
                                                }}</span>
                                        </div>
                                        <div v-if="checkInfo?.availablePoints !== undefined"> {{ $t('，最多可用') }}<span class="point-num">{{ checkInfo?.availablePoints }}</span>{{
                                                integralName }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="block form-block" v-if="canUseBalance">
                            <div class="block-title">{{ $t('使用余额') }}</div>
                            <div class="block-content">
                                <div>
                                    <div class="inline-row">
                                        <el-checkbox v-model="useBalanceEnabled"
                                            @change="onUseBalanceToggle">{{ $t('使用余额') }}</el-checkbox>
                                    </div>
                                    <div class="gray mt6"> {{ $t('当前账户余额：') }}<span class="point-num">{{ priceFormat(Number(userInfo?.balance || 0))
                                            }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 价格明细（对齐 PC 端结算页展示；数据来源：checkInfo.total） -->
                    <div class="container-card price-card">
                        <div class="title">{{ $t('价格明细') }}</div>
                        <div class="price-content">
                            <div class="price-row">
                                <span class="label">{{ $t('商品总价') }}</span>
                                <span class="value">
                                    {{ priceFormat(Number(checkInfo?.total?.productAmount || 0)) }}
                                </span>
                            </div>

                            <div class="price-row" v-if="Number(checkInfo?.total?.serviceFee || 0) > 0">
                                <span class="label">{{ $t('附加费用') }}</span>
                                <span class="value">{{ priceFormat(Number(checkInfo?.total?.serviceFee || 0)) }}</span>
                            </div>

                            <div class="price-row">
                                <span class="label">{{ shippingFeeLabel }}</span>
                                <span class="value">
                                    {{ isFreeShipping ? "" : "+" }}
                                    {{ isFreeShipping ? $t('包邮') : priceFormat(Number(checkInfo?.total?.shippingFee || 0)) }}
                                    <el-popover placement="top" trigger="hover" :width="360"
                                        popper-class="price-popover" :show-arrow="false">
                                        <template #reference>
                                            <el-icon class="help-ico">
                                                <QuestionFilled />
                                            </el-icon>
                                        </template>
                                        <div class="popover-box">
                                            <div class="popover-title">{{ $t('运费明细') }}</div>
                                            <div class="goods-list">
                                                <div class="goods-col" v-for="store in checkInfo?.cartList || []"
                                                    :key="store.shopId">
                                                    <div class="shop-name">
                                                        {{ store.shopId == 0 ? $t('自营') : store.shopTitle || $t('商家') }}
                                                    </div>
                                                    <div class="txt">
                                                        {{ shippingFeeLabel }}：{{ Number((checkInfo?.total?.storeShippingFee || {})[(store as
                                                                any).shopId] || 0) > 0
                                                                ? priceFormat(Number((checkInfo?.total?.storeShippingFee ||
                                                                    {})[(store as
                                                                        any).shopId] || 0))
                                                                : $t('包邮') }}
                                                    </div>
                                                    <div class="goods-thumbs">
                                                        <img v-for="(p, idx) in ((store as any).carts || []).slice(0, 8)"
                                                            :key="idx" class="thumb"
                                                            :src="imageFormat(p.picThumb || p.pic || p.picUrl || p.productPic)"
                                                            alt="" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </el-popover>
                                </span>
                            </div>

                            <div class="price-row" v-if="Number(checkInfo?.total?.balance || 0) > 0">
                                <span class="label">{{ $t('使用余额') }}</span>
                                <span class="value">- {{ priceFormat(Number(checkInfo?.total?.balance || 0)) }}</span>
                            </div>

                            <div class="price-row" v-if="Number(checkInfo?.total?.giftCardAmount || 0) > 0">
                                <span class="label">{{ $t('使用礼品卡') }}</span>
                                <span class="value">- {{ priceFormat(Number(checkInfo?.total?.giftCardAmount || 0))
                                    }}</span>
                            </div>

                            <div class="price-row"
                                v-if="Number(checkInfo?.total?.pointsAmount || 0) > 0 && flowType !== 3">
                                <span class="label">{{ $t('{integralName}抵扣', { integralName }) }}</span>
                                <span class="value">- {{ priceFormat(Number(checkInfo?.total?.pointsAmount || 0))
                                    }}</span>
                            </div>

                            <div class="price-row" v-if="Number(checkInfo?.total?.discounts || 0) > 0">
                                <span class="label">{{ $t('共优惠') }}</span>
                                <span class="value">
                                    - {{ priceFormat(Number(checkInfo?.total?.discounts || 0)) }}
                                    <el-popover placement="top" trigger="hover" :width="320"
                                        popper-class="price-popover" :show-arrow="false">
                                        <template #reference>
                                            <el-icon class="help-ico">
                                                <QuestionFilled />
                                            </el-icon>
                                        </template>
                                        <div class="popover-box discount">
                                            <div class="discount-title">
                                                <div class="title-text">{{ $t('优惠明细') }}</div>
                                            </div>
                                            <div class="discount-content">
                                                <div class="discount-item-box hassub">
                                                    <div class="flex justify-between" style="align-items: center">
                                                        <div class="item-label">{{ $t('共优惠') }}</div>
                                                        <div class="item-value discounts-value">
                                                            - {{ priceFormat(Number(checkInfo?.total?.discounts || 0))
                                                            }}
                                                        </div>
                                                    </div>

                                                    <div class="sub-item-box"
                                                        v-if="Number(checkInfo?.total?.discountCouponAmount || 0) > 0">
                                                        <div class="sub-item-label">{{ $t('优惠券') }}</div>
                                                        <div class="sub-item-value discounts-value">
                                                            - {{
                                                                priceFormat(Number(checkInfo?.total?.discountCouponAmount ||
                                                                    0)) }}
                                                        </div>
                                                    </div>

                                                    <div class="sub-item-box"
                                                        v-if="Number(checkInfo?.total?.discountDiscountAmount || 0) > 0">
                                                        <div class="sub-item-label">{{ $t('其他优惠') }}</div>
                                                        <div class="sub-item-value discounts-value">
                                                            - {{
                                                                priceFormat(Number(checkInfo?.total?.discountDiscountAmount
                                                                    || 0)) }}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </el-popover>
                                </span>
                            </div>

                            <div class="price-row price-pay">
                                <span class="label">{{ $t('应付金额') }}</span>
                                <span class="value">
                                    <span class="pay-amount">{{ priceFormat(orderOriginalPayableAmount) }}</span>
                                </span>
                            </div>
                            <div class="price-row price-pay" v-if="hasChangedPayableAmountForDisplay">
                                <span class="label">{{ $t('改价后金额') }}</span>
                                <span class="value">
                                    <span class="pay-amount">{{ priceFormat(orderPayableAmount) }}</span>
                                </span>
                            </div>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </a-spin>

        <!-- 底部结算栏（对齐 Index.vue：避免在侧边菜单布局下错位） -->
        <div class="selected-action-warp selected-warp-left proxy-checkout-warp" :style="{ left: bottomBarLeft }">
            <div class="selected-action">
                <DialogForm isDrawer :title="$t('整单改价')" width="520px" path="order/proxyShopping/src/AlterPriceDrawer"
                    :params="alterPriceDialogParams" @okCallback="onAlterPriceConfirm">
                    <el-button @click="prepareAlterPriceDialog">{{ $t('整单改价(含运费)') }}</el-button>
                </DialogForm>
                <el-button type="primary" :loading="submitting" @click="onSubmit"
                    style="margin-left: 10px;">{{ $t('提交订单') }}</el-button>
            </div>
        </div>
        <!-- 未付金额提示 -->
        <UnpaidOrderTipDialog v-model:open="unpaidDialogOpen" :orderSn="unpaidDialogOrderSn" @ok="onUnpaidDialogOk" />

    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { imageFormat, priceFormat } from "@/utils/format";
import { ArrowDown, ArrowUp, Back, CircleCloseFilled, Location, Phone, Plus, QuestionFilled, User } from "@element-plus/icons-vue";
import { useConfigStore } from "@/store/config";
import { useThemeStore } from "@/store/theme";
import { getConfigOrder, getConfigShopping } from "@/api/setting/config";
import type { UserFormState } from "@/types/user/user.d";
import type { ProxyOrderCheckVO, ProxyPaymentType, ProxyShoppingAddress, ProxyStoreShippingTypeData } from "@/types/order/proxyShopping.d";
import {
    getProxyOrderCheckIndex,
    getProxyAvailablePaymentType,
    getProxyStoreShippingType,
    getProxyPickupList,
    getProxyPickupContactList,
    previewProxyOrderChangePrice,
    confirmProxyOrderChangePrice,
    submitProxyOrder,
    updateProxyOrderCheck,
    updateProxyOrderCheckCoupon
} from "@/api/proxyShopping/orderCheck";
import { updateProxyCartCheck } from "@/api/proxyShopping/cart";
import { getProxyShoppingAddressList, setSelectedAddress } from "@/api/proxyShopping/address";
import { DialogForm } from "@/components/dialog";
import DeliveryMethod from "./DeliveryMethod.vue";
import PayType from "./PayType.vue";
import UnpaidOrderTipDialog from "./UnpaidOrderTipDialog.vue";
import { getAdminType } from "@/utils/storage";
import { isStore } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = withDefaults(
    defineProps<{
        userId: number;
        /** 上一步选择会员时透传的用户信息；结算页不再调用 getUser 接口，必须通过 props 传递 */
        userInfo?: (UserFormState & Record<string, any>) | null;
        flowType?: number;
        /** 用户是否在主页面（DeliveryOptionsDrawer）显式选择了配送方式 */
        deliveryPreSelected?: boolean;
        /** 进入结算前需同步的购物车选中状态，在 loadData 中先于 check/index、check/update 调用 */
        cartCheckData?: { cartId: number; isChecked: number }[];
        active?: boolean;
    }>(),
    { active: true, deliveryPreSelected: false, cartCheckData: () => [] }
);
const emit = defineEmits<{ (e: "back"): void }>();

const router = useRouter();
const configStore = useConfigStore();
const { themeInfo } = useThemeStore();
const currency = computed(() => configStore.get("dollarSign") ?? "¥");
const isFreeShipping = computed(() => Number(checkInfo.value?.total?.shippingFee || 0) === 0);

const integralName = ref<string>(t('积分'));
const integralNameLoaded = ref(false);
const loadShoppingConfigOnce = async () => {
    if (integralNameLoaded.value) return;
    integralNameLoaded.value = true;
    try {
        const res: any = await getConfigShopping();
        const name = String(res?.integralName || "").trim();
        if (name) integralName.value = name;
    } catch { }
};

const isShopLogin = computed(() => getAdminType() === "shop");

const orderConfigLoaded = ref(false);
const orderConfigAllowUsePoint = ref(false);
const orderConfigAllowUseBalance = ref(false);
const loadOrderConfigOnce = async () => {
    if (orderConfigLoaded.value) return;
    orderConfigLoaded.value = true;
    try {
        const res: any = await getConfigOrder();
        orderConfigAllowUsePoint.value = Number(res?.adminShoppingUsePoint ?? 0) === 1;
        orderConfigAllowUseBalance.value = Number(res?.adminShoppingUseBalance ?? 0) === 1;
    } catch { }
};

const canUsePoint = computed(() => orderConfigAllowUsePoint.value && !isShopLogin.value);
const canUseBalance = computed(() => orderConfigAllowUseBalance.value && !isShopLogin.value);

const userId = computed(() => Number(props.userId));
const flowType = computed(() => {
    const n = Number(props.flowType);
    return Number.isFinite(n) && n > 0 ? n : 1;
});

// 参考 PC 端 deliveryOption 能力位：根据接口返回的 deliveryOption 决定哪些配送方式可用
const allowedDeliveryTypeIds = computed<number[] | null>(() => {
    const opt = (checkInfo.value as any)?.deliveryOption;
    if (!opt || typeof opt !== "object") return null;
    const ids: number[] = [];
    if (Array.isArray(opt.deliveryProducts) && opt.deliveryProducts.length > 0) ids.push(1);
    if (Array.isArray(opt.pickupProducts) && opt.pickupProducts.length > 0) ids.push(2);
    if (Array.isArray(opt.shopDeliveryProducts) && opt.shopDeliveryProducts.length > 0) ids.push(3);
    return ids.length > 0 ? ids : null;
});

const getDeliveryTypeLabel = (deliveryType: any) => {
    if (!isStore()) return t('普通快递');
    const type = Number(deliveryType || 0);
    if (type === 2) return t('到店自提');
    if (type === 3) return t('同城配送');
    return t('商家配送');
};

const shippingFeeLabel = computed(() => getDeliveryTypeLabel(formState.value.deliveryType));

const loading = ref(false);
const submitting = ref(false);

const bottomBarLeft = computed(() => (themeInfo.layout !== "topMenu" ? "23.0625rem" : "16.875rem"));

const unpaidDialogOpen = ref(false);
const unpaidDialogOrderSn = ref<string>("");
const unpaidDialogHasNavigated = ref(false);
const goOrderList = () => {
    if (unpaidDialogHasNavigated.value) return;
    unpaidDialogHasNavigated.value = true;
    router.push("/order/list").catch(() => { });
};
const onUnpaidDialogOk = () => {
    goOrderList();
};

const onBackToProxyShopping = () => {
    emit("back");
};

const userInfo = ref<UserFormState | null>(null);
const checkInfo = ref<ProxyOrderCheckVO | null>(null);
const addressList = ref<ProxyShoppingAddress[]>([]);
const shippingTypeData = ref<ProxyStoreShippingTypeData>({});

const effectiveShippingTypeData = computed<ProxyStoreShippingTypeData>(() => {
    if (!isStore()) {
        const data: ProxyStoreShippingTypeData = {};
        for (const [key, list] of Object.entries(shippingTypeData.value || {})) {
            if (Array.isArray(list)) {
                const onlyExpress = list.filter((x) => Number(x?.shippingTypeId) === 1);
                if (onlyExpress.length > 0) {
                    data[key] = onlyExpress;
                }
            }
        }
        if (Object.keys(data).length === 0) {
            const stores = (checkInfo.value?.cartList || []) as any[];
            for (const store of stores) {
                if (store?.noShipping === 1) continue;
                const key = String(Number(store?.shopId || 0));
                data[key] = [{ shippingTypeId: 1, shippingTypeName: t('普通快递'), shopId: Number(store?.shopId || 0) }];
            }
        }
        return data;
    }
    return shippingTypeData.value;
});

const lockedDeliveryTypeId = computed<number | null>(() => {
    if (!isStore()) return null;
    if (!props.deliveryPreSelected) return null;
    return flowType.value;
});

const paymentTypeList = ref<ProxyPaymentType[]>([]);
const pickupPointList = ref<any[]>([]);
const pickupContactList = ref<any[]>([]);

const settingFromResponse = ref(false);

const formState = ref({
    addressId: undefined as number | undefined,
    address: undefined as any,
    deliveryType: 1,
    // 非多门店：按 shopId 维度选择配送方式（对齐 PC shippingType 结构）
    shippingType: {} as Record<string, { typeId: number; shopId: number; typeName: string }>,
    pickupId: undefined as number | undefined,
    pickupContactId: undefined as number | undefined,
    pickupContactName: "",
    pickupContactMobile: "",
    payTypeId: undefined as number | undefined,
    usePoint: 0,
    useBalance: 0,
    useCouponIds: [] as number[],
    selectUserCouponIds: [] as number[],
    // 0/undefined: 不使用“默认最优券”；1: 使用“默认最优券”（对齐 PC useDefaultCouponIds）
    useDefaultCouponIds: 0 as 0 | 1,
    // 整单改价
    useChangePrice: false,
    totalChangePriceType: undefined as number | undefined,
    changePriceValue: undefined as number | undefined,
    originalPrice: undefined as number | undefined,
    // 行改价（与 POS 一致）
    cartChangePrices: [] as any[],
    buyerNote: ""
});

const usePointEnabled = ref(false);
const useBalanceEnabled = ref(false);

const maxAvailablePoints = computed(() => {
    const n = Number((checkInfo.value as any)?.availablePoints ?? 0);
    if (!Number.isFinite(n) || n <= 0) return 0;
    return Math.floor(n);
});

const pointToggleDisabled = computed(() => canUsePoint.value && maxAvailablePoints.value <= 0);

const clampUsePoint = (val: any) => {
    const n = Number(val ?? 0);
    if (!Number.isFinite(n) || n <= 0) return 0;
    const max = maxAvailablePoints.value;
    if (max <= 0) return 0;
    return Math.min(Math.floor(n), max);
};

const discountAfterAmount = computed(() => {
    const productAmount = Number(checkInfo.value?.total?.productAmount || 0);
    const discounts = Number(checkInfo.value?.total?.discounts || 0);
    const n = productAmount - discounts;
    return n > 0 ? n : 0;
});

const usePointInput = computed<string>({
    get: () => {
        const n = Number(formState.value.usePoint || 0);
        return n ? String(n) : "";
    },
    set: (val) => {
        const digits = String(val ?? "").replace(/[^\d]/g, "");
        formState.value.usePoint = clampUsePoint(digits ? Number(digits) : 0);
    }
});

// 订单商品列表：直接使用接口返回的 cartList[].carts[]（不再依赖兼容字段 productList）
const productListForTable = computed<any[]>(() => {
    const stores = (checkInfo.value as any)?.cartList || [];
    if (!Array.isArray(stores) || stores.length === 0) return [];
    const rows: any[] = [];
    for (const store of stores) {
        const carts = store?.carts || [];
        if (Array.isArray(carts) && carts.length > 0) rows.push(...carts);
    }
    return rows;
});

// 赠品：接口挂在 cartList[].gift（店铺维度）；此处直接扁平化展示（不展示店铺标题）
const giftListForDisplay = computed<any[]>(() => {
    const stores = checkInfo.value?.cartList || [];
    if (!Array.isArray(stores) || stores.length === 0) return [];
    const list: any[] = [];
    for (const store of stores as any[]) {
        const gifts = (store as any)?.gift || [];
        if (Array.isArray(gifts) && gifts.length > 0) list.push(...gifts);
    }
    return list;
});
const hasGift = computed(() => giftListForDisplay.value.length > 0);
const getGiftNum = (gift: any) => {
    const n = Number(gift?.num ?? gift?.quantity ?? 1);
    return Number.isFinite(n) && n > 0 ? n : 1;
};
const getGiftItemKey = (gift: any) => {
    const giftId = gift?.giftId;
    if (giftId !== undefined && giftId !== null && String(giftId) !== "") return `gift-${giftId}`;
    const pid = gift?.productId ?? "";
    const sku = gift?.skuId ?? "";
    const type = gift?.type ?? gift?.rulesType ?? "";
    return `gift-${pid}-${sku}-${type}`;
};

// 优惠券列表接口（visible-list）需要携带商品ID集合（对齐收银台实现）
const couponProductIds = computed<number[]>(() => {
    const ids = (productListForTable.value || []).map((x: any) => Number(x?.productId)).filter((x: any) => Number.isFinite(x) && x > 0);
    // 去重
    return Array.from(new Set(ids));
});

// 结算接口 cartList 中返回的“当前订单可用优惠活动”（用于优惠券可用性判断）
// 字段路径：enableUsePromotion[i].data.couponId
const couponEnableUsePromotion = computed<any[]>(() => {
    const stores = (checkInfo.value as any)?.cartList || [];
    if (!Array.isArray(stores) || stores.length === 0) return [];
    const list: any[] = [];
    for (const store of stores) {
        const promos = (store as any)?.enableUsePromotion;
        if (Array.isArray(promos) && promos.length > 0) list.push(...promos);
    }
    return list;
});

// 期望展示：只展示 value，用中文逗号拼接（兼容 string / JSON string / array）
const formatSkuDataText = (skuData: any): string => {
    if (!skuData) return "";

    if (typeof skuData === "string") {
        const s = skuData.trim();
        if (!s) return "";
        // 兼容 JSON 字符串
        if ((s.startsWith("[") && s.endsWith("]")) || (s.startsWith("{") && s.endsWith("}"))) {
            try {
                return formatSkuDataText(JSON.parse(s));
            } catch {
                return "";
            }
        }

        // 兼容 "颜色:红|内存:12G" / "颜色：红；内存：12G" 等字符串形态：尽量提取值
        const parts = s
            .split(/[|；;]/)
            .map((x) => String(x ?? "").trim())
            .filter(Boolean);
        const values = parts
            .map((p) => {
                const seg = p.split(/[:：]/);
                return String(seg[seg.length - 1] ?? "").trim();
            })
            .filter(Boolean);
        if (values.length >= 2) return values.join("，");
        return "";
    }

    if (!Array.isArray(skuData)) return "";

    return skuData
        .map((x: any) => x?.value ?? x?.attrValue ?? x?.val ?? "")
        .filter((v: any) => v !== undefined && v !== null && String(v).trim() !== "")
        .map((v: any) => String(v).trim())
        .join("，");
};

const getRowChangePriceType = (row: any) => {
    const type = Number(row?.changePriceType ?? row?.priceChangeType ?? row?.cartChangePriceType ?? row?.totalChangePriceType ?? 0);
    return Number.isFinite(type) ? type : 0;
};

const getRowChangePriceValue = (row: any) => {
    const value = row?.changePriceValue ?? row?.priceChangeValue ?? row?.cartChangePriceValue ?? row?.changePrice ?? row?.discountChangePrice;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
};

const hasRowChangePrice = (row: any) => {
    if (row?.hasChangePrice === true || Number(row?.hasChangePrice || 0) === 1) return true;
    return getRowChangePriceType(row) > 0 && getRowChangePriceValue(row) !== null;
};

const getRowChangePriceTypeLabel = (row: any) => {
    const type = getRowChangePriceType(row);
    if (type === 2) return t('折扣');
    if (type === 3) return t('减价');
    return t('一口价');
};

const getRowChangePriceDisplayValue = (row: any) => {
    const type = getRowChangePriceType(row);
    const value = getRowChangePriceValue(row);
    if (value === null) return "-";
    if (type === 2) return `${value}%`;
    return priceFormat(value);
};

const changePriceTypeList = [
    { label: t('一口价'), value: 1 },
    { label: t('折扣'), value: 2 },
    { label: t('减少'), value: 3 }
];

const changePriceUnitMap: Record<number, { unit: string }> = {
    1: { unit: t('元') },
    2: { unit: "%" },
    3: { unit: t('元') }
};

const getRowChangePriceRenderType = (row: any) => {
    const type = getRowChangePriceType(row);
    return type === 2 || type === 3 ? type : 1;
};

const getRowChangePriceInputValue = (row: any) => {
    const value = getRowChangePriceValue(row);
    if (value !== null) return value;
    return "";
};

type RowChangeDraft = {
    cartId?: number;
    productId?: number;
    skuId?: number;
    originalPrice: number;
    originalUnitPrice: number;
    changePriceType: number;
    changePriceValue: number | string;
};

const rowChangeDraftMap = ref<Record<string, RowChangeDraft>>({});
let rowChangePreviewTimer: any = null;
let rowChangePreviewSeq = 0;

const getRowChangeDraftKey = (row: any) => {
    const cartId = Number(row?.cartId || 0);
    if (cartId > 0) return `cart_${cartId}`;
    return `prod_${Number(row?.productId || 0)}_sku_${Number(row?.skuId || 0)}`;
};

const normalizeRowChangeType = (type: any) => {
    const n = Number(type || 1);
    return n === 2 || n === 3 ? n : 1;
};

const getRowOriginalPrice = (row: any) => {
    const subtotal = Number(row?.subtotal);
    if (Number.isFinite(subtotal) && subtotal >= 0) return Number(subtotal.toFixed(2));
    const price = Number(row?.price || 0);
    const quantity = Number(row?.quantity || 0);
    const amount = price * quantity;
    return Number.isFinite(amount) && amount >= 0 ? Number(amount.toFixed(2)) : 0;
};

const getRowCurrentUnitPrice = (row: any) => {
    const price = Number(row?.price);
    if (Number.isFinite(price) && price >= 0) return Number(price.toFixed(2));
    const quantity = Number(row?.quantity || 0);
    if (quantity <= 0) return 0;
    const subtotal = Number(row?.subtotal);
    if (!Number.isFinite(subtotal) || subtotal < 0) return 0;
    return Number((subtotal / quantity).toFixed(2));
};

const normalizeRowChangeValue = (value: any, type: number, maxReduceAmount?: number) => {
    const n = Number(value);
    if (!Number.isFinite(n)) return "";
    if (type === 2) {
        if (n < 0) return 0;
        if (n > 100) return 100;
        return n;
    }
    if (type === 3) {
        if (n < 0) return 0;
        if (Number.isFinite(Number(maxReduceAmount)) && n > Number(maxReduceAmount)) {
            return Number(maxReduceAmount);
        }
        return n;
    }
    return n < 0 ? 0 : n;
};

const createRowChangeDraft = (row: any): RowChangeDraft => {
    const type = getRowChangePriceRenderType(row);
    const value = getRowChangePriceInputValue(row);
    return {
        cartId: Number(row?.cartId || 0) || undefined,
        productId: Number(row?.productId || 0) || undefined,
        skuId: Number(row?.skuId || row?.sku?.skuId || 0) || undefined,
        originalPrice: getRowOriginalPrice(row),
        originalUnitPrice: getRowCurrentUnitPrice(row),
        changePriceType: type,
        changePriceValue: value === "" ? getRowOriginalPrice(row) : value
    };
};

const ensureRowChangeDraft = (row: any): RowChangeDraft => {
    const key = getRowChangeDraftKey(row);
    const current = rowChangeDraftMap.value[key];
    if (current) return current;
    const next = createRowChangeDraft(row);
    rowChangeDraftMap.value[key] = next;
    return next;
};

const hasRowDraftChanged = (draft?: RowChangeDraft) => {
    if (!draft) return false;
    const type = normalizeRowChangeType(draft.changePriceType);
    const normalized = normalizeRowChangeValue(draft.changePriceValue, type, draft.originalPrice);
    if (normalized === "") return false;
    const currentValue = Number(normalized);
    return type !== 1 || Number(currentValue.toFixed(2)) !== Number(Number(draft.originalPrice || 0).toFixed(2));
};

const getRowDisplayUnitPrice = (row: any) => {
    const draft = rowChangeDraftMap.value[getRowChangeDraftKey(row)];
    if (draft && (hasRowDraftChanged(draft) || hasRowChangePrice(row))) {
        return Number(draft.originalUnitPrice || 0);
    }
    return getRowCurrentUnitPrice(row);
};

const getRowDisplaySubtotal = (row: any) => {
    const subtotal = Number(row?.subtotal);
    if (Number.isFinite(subtotal) && subtotal >= 0) return Number(subtotal.toFixed(2));
    const unitPrice = getRowDisplayUnitPrice(row);
    const quantity = Number(row?.quantity || 0);
    const amount = unitPrice * quantity;
    return Number.isFinite(amount) && amount >= 0 ? Number(amount.toFixed(2)) : 0;
};

const syncRowChangeDraftMap = (rows: any[]) => {
    const nextKeys = new Set<string>();
    for (const row of rows || []) {
        const key = getRowChangeDraftKey(row);
        nextKeys.add(key);
        if (!rowChangeDraftMap.value[key]) {
            rowChangeDraftMap.value[key] = createRowChangeDraft(row);
        } else {
            const currentDraft = rowChangeDraftMap.value[key];
            if (!hasRowDraftChanged(currentDraft) && !hasRowChangePrice(row)) {
                currentDraft.originalPrice = getRowOriginalPrice(row);
                currentDraft.originalUnitPrice = getRowCurrentUnitPrice(row);
            }
            if (rowChangeDraftMap.value[key].cartId === undefined) {
                rowChangeDraftMap.value[key].cartId = Number(row?.cartId || 0) || undefined;
            }
            if (rowChangeDraftMap.value[key].productId === undefined) {
                rowChangeDraftMap.value[key].productId = Number(row?.productId || 0) || undefined;
            }
            if (rowChangeDraftMap.value[key].skuId === undefined) {
                rowChangeDraftMap.value[key].skuId = Number(row?.skuId || row?.sku?.skuId || 0) || undefined;
            }
        }
    }
    Object.keys(rowChangeDraftMap.value).forEach((k) => {
        if (!nextKeys.has(k)) delete rowChangeDraftMap.value[k];
    });
};

watch(
    productListForTable,
    (rows) => {
        syncRowChangeDraftMap(rows || []);
    },
    { immediate: true }
);

const collectRowChangePrices = () => {
    const rows = productListForTable.value || [];
    const result: any[] = [];
    for (const row of rows) {
        const draft = ensureRowChangeDraft(row);
        const type = normalizeRowChangeType(draft.changePriceType);
        const normalized = normalizeRowChangeValue(draft.changePriceValue, type, draft.originalPrice);
        if (normalized === "") continue;

        const currentValue = Number(normalized);
        const originalPrice = Number(draft.originalPrice || 0);
        const changed = type !== 1 || Number(currentValue.toFixed(2)) !== Number(originalPrice.toFixed(2));
        if (!changed) continue;

        result.push({
            cartId: Number(draft.cartId || row?.cartId || 0),
            productId: Number(draft.productId || row?.productId || 0),
            skuId: draft.skuId ? Number(draft.skuId) : undefined,
            originalPrice,
            changePriceType: type,
            changePriceValue: currentValue
        });
    }
    return result.filter((x) => Number.isFinite(Number(x.cartId || 0)) && Number(x.cartId || 0) > 0);
};

const syncRowChangeToFormState = () => {
    const cartChangePrices = collectRowChangePrices();
    formState.value.cartChangePrices = cartChangePrices;
    if (cartChangePrices.length > 0) {
        // 行改价和整单改价互斥，后操作覆盖前操作
        formState.value.useChangePrice = true;
        formState.value.totalChangePriceType = undefined;
        formState.value.changePriceValue = undefined;
        formState.value.originalPrice = undefined;
    } else if (!formState.value.totalChangePriceType) {
        formState.value.useChangePrice = false;
    }
    return cartChangePrices;
};

const runRowChangePreview = async () => {
    if (!userId.value || settingFromResponse.value) return;
    const cartChangePrices = syncRowChangeToFormState();
    if (cartChangePrices.length === 0) return;

    const seq = ++rowChangePreviewSeq;
    try {
        const res = await previewProxyOrderChangePrice({
            userId: userId.value,
            flowType: flowType.value,
            ...formState.value,
            useChangePrice: true,
            cartChangePrices
        } as any);
        if (seq !== rowChangePreviewSeq) return;
        checkInfo.value = res as any;
        applyResponseToForm(res as any);
    } catch {
        // 输入过程预览失败不打断编辑
    }
};

const scheduleRowChangePreview = () => {
    clearTimeout(rowChangePreviewTimer);
    rowChangePreviewTimer = setTimeout(() => {
        runRowChangePreview();
    }, 250);
};

const onRowChangeTypeChange = (row: any, type: any) => {
    const draft = ensureRowChangeDraft(row);
    draft.changePriceType = normalizeRowChangeType(type);
    const currentValue = Number(draft.changePriceValue);
    if (Number.isFinite(currentValue)) {
        draft.changePriceValue = normalizeRowChangeValue(currentValue, draft.changePriceType, draft.originalPrice);
    } else {
        draft.changePriceValue = "";
    }
    syncRowChangeToFormState();
};

const onRowChangeValueInput = (row: any, value: any) => {
    const draft = ensureRowChangeDraft(row);
    const nextValue = String(value ?? "");
    if (nextValue.trim() === "") {
        draft.changePriceValue = "";
        syncRowChangeToFormState();
        return;
    }

    const n = Number(nextValue);
    if (!Number.isFinite(n)) {
        draft.changePriceValue = nextValue;
        syncRowChangeToFormState();
        return;
    }

    const type = normalizeRowChangeType(draft.changePriceType);
    draft.changePriceValue = normalizeRowChangeValue(n, type, draft.originalPrice);
    syncRowChangeToFormState();
};

const onRowChangeValueChange = (row: any) => {
    const draft = ensureRowChangeDraft(row);
    const type = normalizeRowChangeType(draft.changePriceType);
    const rawValue = Number(draft.changePriceValue);
    const maxReduceAmount = Number(draft.originalPrice || 0);
    if (type === 3 && Number.isFinite(rawValue) && rawValue > maxReduceAmount) {
        message.warning(t('减价金额必须小于等于商品小计'));
    }
    draft.changePriceValue = normalizeRowChangeValue(draft.changePriceValue, type, maxReduceAmount);
    syncRowChangeToFormState();
    scheduleRowChangePreview();
};

const extractRowChangePricesFromResponse = (res: any) => {
    const stores = (res as any)?.cartList || [];
    if (!Array.isArray(stores) || stores.length === 0) return [];
    const result: any[] = [];
    for (const store of stores) {
        const carts = (store as any)?.carts || [];
        if (!Array.isArray(carts)) continue;
        for (const row of carts) {
            if (!hasRowChangePrice(row)) continue;
            const type = getRowChangePriceRenderType(row);
            const value = getRowChangePriceInputValue(row);
            const n = Number(value);
            if (!Number.isFinite(n)) continue;
            result.push({
                cartId: Number(row?.cartId || 0),
                productId: Number(row?.productId || 0),
                skuId: Number(row?.skuId || row?.sku?.skuId || 0) || undefined,
                originalPrice: getRowOriginalPrice(row),
                changePriceType: type,
                changePriceValue: n
            });
        }
    }
    return result.filter((x) => Number(x.cartId || 0) > 0);
};

const getStoreTitle = (store: any) => (Number(store?.shopId) === 0 ? t('自营') : store?.shopTitle || t('商家'));

const isPaymentTypeShown = (p: any) => p?.isShow !== false;
const isPaymentTypeEnabled = (p: any) => p?.disabled !== true;

const hasEnabledPaymentType = computed(() => {
    const list = paymentTypeList.value || [];
    if (!Array.isArray(list) || list.length === 0) return false;
    return list.some((p: any) => isPaymentTypeShown(p) && isPaymentTypeEnabled(p) && Number(p?.typeId || 0) > 0);
});

const ensureValidPayTypeId = () => {
    const list = paymentTypeList.value || [];
    if (!Array.isArray(list) || list.length === 0) {
        formState.value.payTypeId = undefined;
        return;
    }

    const current = Number(formState.value.payTypeId || 0);
    const currentOk = current > 0 && list.some((p: any) => Number(p?.typeId || 0) === current && isPaymentTypeShown(p) && isPaymentTypeEnabled(p));

    if (currentOk) return;

    const firstEnabled = list.find((p: any) => isPaymentTypeShown(p) && isPaymentTypeEnabled(p) && Number(p?.typeId || 0) > 0);
    formState.value.payTypeId = firstEnabled?.typeId ? Number(firstEnabled.typeId) : undefined;
};

const loadPaymentTypes = async () => {
    const res = await getProxyAvailablePaymentType({ flowType: flowType.value });
    const list = Array.isArray(res) ? (res as any[]) : [];
    paymentTypeList.value = list.filter((p: any) => isPaymentTypeShown(p));
    ensureValidPayTypeId();
};

const getSubmitMissingReasons = () => {
    const reasons: string[] = [];

    const products = productListForTable.value || [];
    if (!userId.value || Number.isNaN(userId.value)) reasons.push(t('缺少用户信息，请从代客下单入口进入'));
    if (products.length === 0) reasons.push(t('购物车为空'));

    // 地址要求：除自提外都需要 addressId 或后端返回的 addressInfo / 临时地址
    if (formState.value.deliveryType !== 2) {
        const hasAddressId = !!formState.value.addressId;
        const hasAddressInfo = !!checkInfo.value?.addressInfo;
        const hasCustomAddress = !!formState.value.address;
        if (!hasAddressId && !hasAddressInfo && !hasCustomAddress) reasons.push(t('请选择收货地址'));
    } else {
        if (!Number(formState.value.pickupId || 0)) reasons.push(t('请选择自提点'));
        const hasContactId = Number(formState.value.pickupContactId || 0) > 0;
        const hasContactName = !!String(formState.value.pickupContactName || "").trim();
        const hasContactMobile = !!String(formState.value.pickupContactMobile || "").trim();
        if (!hasContactId && !(hasContactName && hasContactMobile)) reasons.push(t('请选择自提联系人'));
    }

    // 支付方式：必须存在可选项且需要选中（与 PC 端提交校验对齐）
    if (!hasEnabledPaymentType.value) {
        reasons.push(t('暂无可用支付方式'));
    } else {
        const current = Number(formState.value.payTypeId || 0);
        const ok =
            current > 0 &&
            (paymentTypeList.value || []).some((p: any) => Number(p?.typeId || 0) === current && isPaymentTypeShown(p) && isPaymentTypeEnabled(p));
        if (!ok) reasons.push(t('请选择支付方式'));
    }

    // 按店铺校验 shippingTypeData 与已选 shippingType
    const stores = (checkInfo.value?.cartList || []) as any[];
    if (stores.length === 0 && products.length > 0) reasons.push(t('订单店铺信息缺失，请刷新重试'));

    for (const store of stores) {
        if (!store || store?.noShipping === 1) continue;
        const key = String(store?.shopId);
        const title = getStoreTitle(store);
        const list = shippingTypeData.value?.[key] || [];

        if (!Array.isArray(list) || list.length === 0) {
            reasons.push(t('店铺【{title}】暂无可选配送方式', { title }));
            continue;
        }

        const selected = formState.value.shippingType?.[key];
        if (!selected?.typeId) {
            reasons.push(t('请选择店铺【{title}】配送方式', { title }));
            continue;
        }

        const ok = list.some((x: any) => Number(x?.shippingTypeId) === Number(selected.typeId));
        if (!ok) reasons.push(t('店铺【{title}】配送方式已失效，请重新选择', { title }));
    }

    if (canUsePoint.value && Number(formState.value.usePoint || 0) > maxAvailablePoints.value) {
        reasons.push(t('使用{value}数不能超过最多可用{value1}（{value2}）', { value: integralName.value, value1: integralName.value, value2: maxAvailablePoints.value }));
    }

    return reasons;
};

const isActiveAddress = (addr: any) => {
    const id = Number(addr?.addressId || 0);
    if (id) return Number(formState.value.addressId || 0) === id;
    // 无 addressId：认为是“临时地址”，用 address 字段判断
    return !!formState.value.address && formatAddress(addr) === formatAddress(formState.value.address);
};

const formatAddress = (addr: any) => {
    if (!addr) return "-";
    const region = addr.regionName || (Array.isArray(addr.regionNames) ? addr.regionNames.join("") : "");
    const detail = addr.address || "";
    const consignee = addr.consignee || "";
    const mobile = addr.mobile || addr.telephone || "";
    const head = [region, detail].filter(Boolean).join(" ");
    const tail = [consignee, mobile].filter(Boolean).join(" ");
    return [head, tail].filter(Boolean).join("，");
};

const getRegionText = (addr: any) => {
    if (!addr) return "-";
    return addr.regionName || (Array.isArray(addr.regionNames) ? addr.regionNames.join("") : "") || "-";
};

const getFullAddressText = (addr: any) => {
    return addr?.address || "";
};

const maskMobile = (mobile: string) => {
    const s = String(mobile || "").trim();
    if (!s) return "-";
    if (s.length < 7) return s;
    return `${s.slice(0, 3)}****${s.slice(-4)}`;
};

/** 与卡片样式一致：宽 300px + gap 10px，用于估算每行个数 */
const ADDRESS_CARD_WIDTH = 300;
const ADDRESS_CARD_GAP = 10;

const addressCardsContainerRef = ref<HTMLElement | null>(null);
const addressCardsPerRow = ref(3);
const addressListExpanded = ref(false);

const recomputeAddressCardsPerRow = () => {
    const el = addressCardsContainerRef.value;
    if (!el) return;
    const w = el.clientWidth;
    if (w <= 0) return;
    const per = Math.max(1, Math.floor((w + ADDRESS_CARD_GAP) / (ADDRESS_CARD_WIDTH + ADDRESS_CARD_GAP)));
    if (per !== addressCardsPerRow.value) addressCardsPerRow.value = per;
};

let addressCardsResizeObserver: ResizeObserver | null = null;

const setupAddressCardsResizeObserver = () => {
    nextTick(() => {
        recomputeAddressCardsPerRow();
        const el = addressCardsContainerRef.value;
        addressCardsResizeObserver?.disconnect();
        addressCardsResizeObserver = null;
        if (!el || typeof ResizeObserver === "undefined") return;
        addressCardsResizeObserver = new ResizeObserver(() => recomputeAddressCardsPerRow());
        addressCardsResizeObserver.observe(el);
    });
};

/** 结算面板展示的完整地址序列（含临时地址；选中项靠前；不再截断为 15 条，展开后一览全部已加载项） */
const fullPanelAddressList = computed<any[]>(() => {
    const ordered = [...(addressList.value as any[])];
    const selectedId = Number(formState.value.addressId || 0);
    if (selectedId) {
        const idx = ordered.findIndex((x: any) => Number(x?.addressId || 0) === selectedId);
        if (idx > 0) ordered.unshift(...ordered.splice(idx, 1));
    }

    if (formState.value.address) return [formState.value.address, ...ordered];

    if (ordered.length > 0) return ordered as any[];
    if (checkInfo.value?.addressInfo) return [checkInfo.value.addressInfo] as any[];
    return [];
});

const addressTwoRowSlotCount = computed(() => Math.max(1, addressCardsPerRow.value) * 2);

const showAddressExpandToggle = computed(() => {
    const totalSlots = 1 + fullPanelAddressList.value.length;
    return totalSlots > addressTwoRowSlotCount.value;
});

const visibleAddressList = computed(() => {
    const full = fullPanelAddressList.value;
    const maxSlots = addressTwoRowSlotCount.value;
    const totalSlots = 1 + full.length;
    if (addressListExpanded.value || totalSlots <= maxSlots) return full;
    const addrVisible = Math.max(0, maxSlots - 1);
    return full.slice(0, addrVisible);
});

const toggleAddressListExpand = () => {
    addressListExpanded.value = !addressListExpanded.value;
};

const onAddressCardClick = async (addr: any) => {
    const addrId = Number(addr?.addressId || 0);
    // 临时地址：直接写入 address，并清空 addressId
    if (!addrId) {
        formState.value.addressId = undefined;
        formState.value.address = addr;
        await refreshCheck({ addressId: undefined, address: formState.value.address });
        return;
    }
    if (Number(formState.value.addressId || 0) === addrId) return;

    // 有 addressId 的地址：先调用接口确认切换成功，再更新本地状态
    try {
        await setSelectedAddress(addrId);
    } catch (e: any) {
        message.error(e?.message || t('切换地址失败'));
        return;
    }

    formState.value.addressId = addrId;
    formState.value.address = undefined;
    await onAddressChange();
};

const parseListResult = (res: any) => {
    if (Array.isArray(res)) return res;
    if (Array.isArray(res?.records)) return res.records;
    if (Array.isArray(res?.list)) return res.list;
    if (Array.isArray(res?.data?.records)) return res.data.records;
    if (Array.isArray(res?.data?.list)) return res.data.list;
    if (Array.isArray(res?.data)) return res.data;
    return [];
};

const getPickupPointId = (point: any) => {
    const n = Number(point?.pickupId || point?.shopId || point?.id || 0);
    return n > 0 ? n : 0;
};

const getPickupContactId = (contact: any) => {
    const n = Number(contact?.contactId || contact?.id || 0);
    return n > 0 ? n : 0;
};

const getPickupPointName = (point: any) => {
    return point?.shopTitle || point?.shopName || point?.name || "-";
};

const getPickupPointAddress = (point: any) => {
    return point?.shopDetailedAddress || point?.address || point?.detailAddress || "-";
};

const getPickupPointBusinessHours = (point: any) => {
    if (point?.businessHours) return String(point.businessHours);
    const config = point?.shopOpenCloseConfig;
    if (config && Array.isArray(config?.times)) {
        const text = config.times
            .map((x: any) => `${String(x?.start || "").trim()}-${String(x?.end || "").trim()}`.replace(/^-|-$|--/g, ""))
            .filter(Boolean)
            .join(",");
        if (text) return text;
    }
    const raw = point?.shopOpenCloseJson;
    if (!raw) return "-";
    try {
        const parsed = typeof raw === "string" ? JSON.parse(raw) : raw;
        const times = Array.isArray(parsed?.times) ? parsed.times : [];
        if (!times.length) return "-";
        const text = times
            .map((x: any) => `${x?.start || ""}-${x?.end || ""}`.replace(/^-|-$|--/g, ""))
            .filter(Boolean)
            .join(",");
        return text || "-";
    } catch {
        return String(raw || "-");
    }
};

const getPickupContactName = (contact: any) => {
    return contact?.name || contact?.contactName || "-";
};

const getPickupContactMobile = (contact: any) => {
    return String(contact?.mobile || contact?.contactMobile || contact?.contactPhone || "");
};

/** 自提联系人卡片：宽 200px + gap 8px（与 .pickup-contact-cards 一致） */
const PICKUP_CONTACT_CARD_WIDTH = 200;
const PICKUP_CONTACT_CARD_GAP = 8;

const pickupContactCardsContainerRef = ref<HTMLElement | null>(null);
const pickupContactCardsPerRow = ref(4);
const pickupContactListExpanded = ref(false);

const recomputePickupContactCardsPerRow = () => {
    const el = pickupContactCardsContainerRef.value;
    if (!el) return;
    const w = el.clientWidth;
    if (w <= 0) return;
    const g = PICKUP_CONTACT_CARD_GAP;
    const per = Math.max(1, Math.floor((w + g) / (PICKUP_CONTACT_CARD_WIDTH + g)));
    if (per !== pickupContactCardsPerRow.value) pickupContactCardsPerRow.value = per;
};

let pickupContactCardsResizeObserver: ResizeObserver | null = null;

const setupPickupContactCardsResizeObserver = () => {
    nextTick(() => {
        recomputePickupContactCardsPerRow();
        const el = pickupContactCardsContainerRef.value;
        pickupContactCardsResizeObserver?.disconnect();
        pickupContactCardsResizeObserver = null;
        if (!el || typeof ResizeObserver === "undefined") return;
        pickupContactCardsResizeObserver = new ResizeObserver(() => recomputePickupContactCardsPerRow());
        pickupContactCardsResizeObserver.observe(el);
    });
};

const fullPanelPickupContactList = computed<any[]>(() => {
    const ordered = [...(pickupContactList.value || [])];
    const selectedId = Number(formState.value.pickupContactId || 0);
    if (selectedId) {
        const idx = ordered.findIndex((x: any) => getPickupContactId(x) === selectedId);
        if (idx > 0) ordered.unshift(...ordered.splice(idx, 1));
    }
    return ordered;
});

const pickupContactTwoRowSlotCount = computed(() => Math.max(1, pickupContactCardsPerRow.value) * 2);

const showPickupContactExpandToggle = computed(() => {
    const totalSlots = 1 + fullPanelPickupContactList.value.length;
    return totalSlots > pickupContactTwoRowSlotCount.value;
});

const visiblePickupContactList = computed(() => {
    const full = fullPanelPickupContactList.value;
    const maxSlots = pickupContactTwoRowSlotCount.value;
    const totalSlots = 1 + full.length;
    if (pickupContactListExpanded.value || totalSlots <= maxSlots) return full;
    const n = Math.max(0, maxSlots - 1);
    return full.slice(0, n);
});

const togglePickupContactListExpand = () => {
    pickupContactListExpanded.value = !pickupContactListExpanded.value;
};

const syncPickupOrAddressCardsResizeObserver = () => {
    const t = Number(formState.value.deliveryType || 0);
    addressCardsResizeObserver?.disconnect();
    addressCardsResizeObserver = null;
    pickupContactCardsResizeObserver?.disconnect();
    pickupContactCardsResizeObserver = null;
    if (t === 2) {
        setupPickupContactCardsResizeObserver();
    } else {
        setupAddressCardsResizeObserver();
    }
};

onMounted(() => {
    syncPickupOrAddressCardsResizeObserver();
});

watch(
    () => Number(formState.value.deliveryType || 0),
    () => {
        syncPickupOrAddressCardsResizeObserver();
    }
);

const isActivePickupPoint = (point: any) => Number(formState.value.pickupId || 0) === getPickupPointId(point);
const isActivePickupContact = (contact: any) => Number(formState.value.pickupContactId || 0) === getPickupContactId(contact);

const getCurrentPickupShopId = () => {
    const shippingType = formState.value.shippingType || {};
    const entries = Object.values(shippingType) as any[];
    const pickupEntry = entries.find((x: any) => Number(x?.typeId || 0) === 2 && Number.isFinite(Number(x?.shopId)));
    if (pickupEntry) return Number(pickupEntry.shopId);
    const firstEntry = entries.find((x: any) => Number.isFinite(Number(x?.shopId)));
    if (firstEntry) return Number(firstEntry.shopId);
    const stores = (checkInfo.value?.cartList || []) as any[];
    if (stores.length === 1 && Number.isFinite(Number(stores[0]?.shopId))) return Number(stores[0].shopId);
    return undefined;
};

const loadPickupPointData = async () => {
    if (!userId.value) return;
    try {
        const shopId = getCurrentPickupShopId();
        if (shopId === undefined) {
            pickupPointList.value = [];
            formState.value.pickupId = undefined;
            return;
        }
        const res = await getProxyPickupList({
            shopId
        } as any);
        const list = parseListResult(res);
        pickupPointList.value = list;
        if (!pickupPointList.value.length) {
            formState.value.pickupId = undefined;
            return;
        }
        const current = Number(formState.value.pickupId || 0);
        const exists = pickupPointList.value.some((x: any) => getPickupPointId(x) === current);
        if (!exists) {
            formState.value.pickupId = getPickupPointId(pickupPointList.value[0]);
        }
    } catch {
        pickupPointList.value = [];
        formState.value.pickupId = undefined;
    }
};

const loadPickupContactData = async () => {
    if (!userId.value) return;
    try {
        const res = await getProxyPickupContactList({ userId: userId.value } as any);
        const list = parseListResult(res);
        pickupContactList.value = list;
        if (!pickupContactList.value.length) {
            formState.value.pickupContactId = undefined;
            formState.value.pickupContactName = "";
            formState.value.pickupContactMobile = "";
            return;
        }
        const current = Number(formState.value.pickupContactId || 0);
        const selected = pickupContactList.value.find((x: any) => getPickupContactId(x) === current) || pickupContactList.value[0];
        formState.value.pickupContactId = getPickupContactId(selected);
        formState.value.pickupContactName = getPickupContactName(selected);
        formState.value.pickupContactMobile = getPickupContactMobile(selected);
    } catch {
        pickupContactList.value = [];
        formState.value.pickupContactId = undefined;
        formState.value.pickupContactName = "";
        formState.value.pickupContactMobile = "";
    }
};

const onPickupPointClick = async (point: any) => {
    const pickupId = getPickupPointId(point);
    if (!pickupId) return;
    if (Number(formState.value.pickupId || 0) === pickupId) return;
    formState.value.pickupId = pickupId;
    await refreshCheck({ pickupId });
};

const onPickupContactClick = async (contact: any) => {
    const pickupContactId = getPickupContactId(contact);
    if (!pickupContactId) return;
    if (Number(formState.value.pickupContactId || 0) === pickupContactId) return;
    const pickupContactName = getPickupContactName(contact);
    const pickupContactMobile = getPickupContactMobile(contact);
    formState.value.pickupContactId = pickupContactId;
    formState.value.pickupContactName = pickupContactName;
    formState.value.pickupContactMobile = pickupContactMobile;
    await refreshCheck({ pickupContactId, pickupContactName, pickupContactMobile });
};

const onPickupContactCreated = async (result: any) => {
    await loadPickupContactData();
    const createdId = Number(result?.contactId || result?.id || 0);
    const createdName = String(result?.name || result?.contactName || "");
    const createdMobile = String(result?.mobile || result?.contactMobile || result?.contactPhone || "");
    const selected =
        pickupContactList.value.find((x: any) => getPickupContactId(x) === createdId) ||
        pickupContactList.value.find((x: any) => getPickupContactName(x) === createdName && getPickupContactMobile(x) === createdMobile) ||
        pickupContactList.value[0];
    if (selected) await onPickupContactClick(selected);
};

const selectedCouponId = computed(() => formState.value.useCouponIds?.[0] || 0);
// couponList 兼容：后端可能返回数组，或 { enableCoupons, disableCoupons }
const couponListForView = computed(() => {
    const raw: any = checkInfo.value?.couponList;
    if (Array.isArray(raw)) return raw;
    const enable = Array.isArray(raw?.enableCoupons) ? raw.enableCoupons : [];
    const disable = Array.isArray(raw?.disableCoupons) ? raw.disableCoupons : [];
    return [...enable, ...disable];
});
const selectedCouponForView = computed(() => {
    if (!selectedCouponId.value) return null as any;
    const list = couponListForView.value || [];

    const getCouponId = (c: any) => Number(c?.couponId || 0);

    return (selectedCouponId.value ? list.find((c: any) => getCouponId(c) === selectedCouponId.value) : null) || null;
});
const selectedCouponViewMoney = computed(() => {
    return selectedCouponForView.value?.couponMoney ?? "";
});
const selectedCouponViewName = computed(() => {
    return selectedCouponForView.value?.couponName ?? "";
});
const selectedCouponViewType = computed(() => {
    return Number(selectedCouponForView.value?.couponType ?? 0) || 0;
});
const selectedCouponViewDiscount = computed(() => {
    return selectedCouponForView.value?.couponDiscount ?? "";
});
const selectedCouponViewIsGlobal = computed(() => {
    return Boolean(selectedCouponForView.value?.isGlobal);
});
const selectedCouponViewEndDate = computed(() => {
    return selectedCouponForView.value?.endDate ?? "";
});

const selectedCouponCount = computed(() => {
    const couponIds = Array.isArray(formState.value.useCouponIds) ? formState.value.useCouponIds : [];
    const keys = new Set<number>();
    for (let i = 0; i < couponIds.length; i++) {
        const key = Number(couponIds[i] || 0);
        if (key) keys.add(key);
    }
    return keys.size;
});

const orderPayableAmount = computed(() => {
    return Number(checkInfo.value?.total?.totalAmount);
});

const orderOriginalPayableAmount = computed(() => {
    return Number(checkInfo.value?.total?.productAmount) + Number(checkInfo.value?.total?.shippingFee);
});

const hasChangedPayableAmountForDisplay = computed(() => {
    const hasChangePriceFlag =
        (checkInfo.value as any)?.item?.hasChangePrice === true ||
        Number((checkInfo.value as any)?.item?.hasChangePrice || 0) === 1 ||
        (checkInfo.value as any)?.hasChangePrice === true ||
        Number((checkInfo.value as any)?.hasChangePrice || 0) === 1 ||
        !!formState.value.useChangePrice;
    if (hasChangePriceFlag) return true;
    return Number(orderOriginalPayableAmount.value.toFixed(2)) !== Number(orderPayableAmount.value.toFixed(2));
});

const orderChangePriceBaseAmount = computed(() => {
    return orderOriginalPayableAmount.value;
});

const hasOuterChangedAmount = computed(() => {
    return hasChangedPayableAmountForDisplay.value;
});

const alterPriceInitialType = ref(1);
const alterPriceInitialValue = ref<number | string>("");

const getOrderChangePriceType = () => {
    const type = Number(
        (checkInfo.value as any)?.item?.totalChangePriceType ??
        (checkInfo.value as any)?.totalChangePriceType ??
        formState.value.totalChangePriceType ??
        0
    );
    return type === 2 || type === 3 ? type : 1;
};

const getOrderChangePriceValue = () => {
    const raw =
        (checkInfo.value as any)?.item?.changePriceValue ??
        (checkInfo.value as any)?.changePriceValue ??
        formState.value.changePriceValue;
    const n = Number(raw);
    return Number.isFinite(n) ? n : "";
};

const prepareAlterPriceDialog = () => {
    alterPriceInitialType.value = getOrderChangePriceType();
    alterPriceInitialValue.value = getOrderChangePriceValue();
};

const alterPriceDialogParams = computed(() => ({
    userId: userId.value,
    flowType: flowType.value,
    deliveryType: formState.value.deliveryType,
    payableAmount: orderChangePriceBaseAmount.value,
    originalPrice: orderChangePriceBaseAmount.value,
    changedAmount: orderPayableAmount.value,
    hasChangedAmount: hasOuterChangedAmount.value,
    initialType: alterPriceInitialType.value,
    initialValue: alterPriceInitialValue.value
}));

const applyResponseToForm = (res: ProxyOrderCheckVO) => {
    settingFromResponse.value = true;
    try {
        let deliveryType = Number(
            (res as any).deliveryType
            || (res as any)?.item?.deliveryType
            || formState.value.deliveryType
            || 1
        );
        if (!isStore()) {
            deliveryType = 1;
        } else if (props.deliveryPreSelected) {
            deliveryType = flowType.value;
        }
        formState.value.deliveryType = deliveryType;
        formState.value.pickupId = Number((res as any)?.pickupId ?? (res as any)?.item?.pickupId ?? formState.value.pickupId ?? 0) || undefined;
        formState.value.pickupContactId =
            Number((res as any)?.pickupContactId ?? (res as any)?.item?.pickupContactId ?? formState.value.pickupContactId ?? 0) || undefined;
        formState.value.pickupContactName = String(
            (res as any)?.pickupContactName ?? (res as any)?.item?.pickupContactName ?? formState.value.pickupContactName ?? ""
        );
        formState.value.pickupContactMobile = String(
            (res as any)?.pickupContactMobile ?? (res as any)?.item?.pickupContactMobile ?? formState.value.pickupContactMobile ?? ""
        );

        const addrId = Number((res as any).addressId || res.addressInfo?.addressId || formState.value.addressId || 0);
        formState.value.addressId = addrId || undefined;

        // 同步后端回传的 shippingType（若存在）
        // 当 shippingType 各 typeId 与已确定的 deliveryType 不匹配时跳过覆盖，
        // 交由 initShippingTypeSelection 根据 deliveryType 修正
        const incomingShippingType = (res as any)?.item?.shippingType ?? (res as any)?.shippingType;
        if (incomingShippingType && typeof incomingShippingType === "object") {
            const allMatch = !deliveryType || Object.values(incomingShippingType as any).every(
                (v: any) => !v?.typeId || Number(v.typeId) === deliveryType
            );
            if (allMatch) {
                formState.value.shippingType = incomingShippingType;
            }
        }

        // 积分/余额：以表单为准（后端也可能回传 usePoint/useBalance）
        const usePoint = Number((res as any).usePoint ?? formState.value.usePoint ?? 0);
        const useBalance = Number((res as any).useBalance ?? formState.value.useBalance ?? 0);
        formState.value.usePoint = Number.isFinite(usePoint) ? usePoint : 0;
        formState.value.useBalance = Number.isFinite(useBalance) ? useBalance : 0;

        // 读取“平台代客下单设置”配置：关闭时强制不可用（店铺端同样强制不可用）
        if (!canUsePoint.value) formState.value.usePoint = 0;
        if (!canUseBalance.value) formState.value.useBalance = 0;

        usePointEnabled.value = formState.value.usePoint > 0;
        useBalanceEnabled.value = formState.value.useBalance > 0;

        // 优惠券：以接口返回为准（兼容 root / item）
        const useCouponIds = (res as any)?.useCouponIds ?? (res as any)?.item?.useCouponIds ?? formState.value.useCouponIds ?? [];
        const selectUserCouponIds = (res as any)?.selectUserCouponIds ?? (res as any)?.item?.selectUserCouponIds ?? formState.value.selectUserCouponIds ?? [];
        formState.value.useCouponIds = Array.isArray(useCouponIds) ? useCouponIds : [];
        formState.value.selectUserCouponIds = Array.isArray(selectUserCouponIds) ? selectUserCouponIds : [];

        const totalChangePriceType = Number((res as any)?.item?.totalChangePriceType ?? (res as any)?.totalChangePriceType ?? 0);
        const changePriceValue = Number((res as any)?.item?.changePriceValue ?? (res as any)?.changePriceValue);
        const hasOrderChangePrice = totalChangePriceType > 0 && Number.isFinite(changePriceValue);
        const rowChangePrices = extractRowChangePricesFromResponse(res as any);
        formState.value.useChangePrice = hasOrderChangePrice || rowChangePrices.length > 0;
        formState.value.totalChangePriceType = hasOrderChangePrice ? totalChangePriceType : undefined;
        formState.value.changePriceValue = hasOrderChangePrice ? changePriceValue : undefined;
        formState.value.originalPrice = hasOrderChangePrice
            ? Number(((res as any)?.item?.originalPrice ?? (res as any)?.originalPrice ?? orderPayableAmount.value) || 0)
            : undefined;
        formState.value.cartChangePrices = rowChangePrices;
    } finally {
        // 小延迟避免 watch 立刻触发
        setTimeout(() => (settingFromResponse.value = false), 0);
    }
};

// 更新优惠券（对齐 PC：支持 useDefaultCouponIds，并以返回值回填 useCouponIds / selectUserCouponIds）
const updateCoupon = async (useDefault: boolean = false) => {
    if (!userId.value) return;
    if (settingFromResponse.value) return;

    loading.value = true;
    try {
        formState.value.useDefaultCouponIds = useDefault ? 1 : 0;
        const res = await updateProxyOrderCheckCoupon({
            userId: userId.value,
            flowType: flowType.value,
            ...formState.value
        } as any);
        checkInfo.value = res as any;

        // 以接口返回为准回填（兼容 root / item）
        const useCouponIds = (res as any)?.useCouponIds ?? (res as any)?.item?.useCouponIds ?? [];
        const selectUserCouponIds = (res as any)?.selectUserCouponIds ?? (res as any)?.item?.selectUserCouponIds ?? [];
        formState.value.useCouponIds = Array.isArray(useCouponIds) ? useCouponIds : [];
        formState.value.selectUserCouponIds = Array.isArray(selectUserCouponIds) ? selectUserCouponIds : [];

        applyResponseToForm(res as any);
        return res;
    } catch (error: any) {
        message.error(error.message || t('更新优惠券失败'));
        return null as any;
    } finally {
        loading.value = false;
    }
};

// 抽屉领取优惠券后刷新：仅刷新结算态（不改 useCouponIds），让抽屉基于最新 enableUsePromotion 决定可用/禁用
const afterClaimRefresh = async () => {
    await updateCoupon(false);
};

const initShippingTypeSelection = (stores: any[], data: ProxyStoreShippingTypeData) => {
    let changed = false;
    const current = { ...(formState.value.shippingType || {}) };
    const dt = Number(formState.value.deliveryType || 0);
    const allowed = allowedDeliveryTypeIds.value;

    for (const store of stores || []) {
        if (!store || store.noShipping === 1) continue;
        const shopId = Number(store.shopId);
        const key = String(shopId);
        const list = (data as any)?.[key];
        if (!Array.isArray(list) || list.length === 0) continue;

        const selected = (current as any)[key];
        const inList = selected?.typeId && list.some((x: any) => Number(x?.shippingTypeId) === Number(selected.typeId));
        const matchesDt = !dt || Number(selected?.typeId) === dt;
        const isAllowed = !allowed || !selected?.typeId || allowed.includes(Number(selected.typeId));
        const selectedOk = inList && matchesDt && isAllowed;

        if (!selectedOk) {
            const matchDt = dt ? list.find((x: any) => Number(x?.shippingTypeId) === dt) : null;
            const matchAllowed = !matchDt && allowed
                ? list.find((x: any) => allowed.includes(Number(x?.shippingTypeId)))
                : null;
            const target = matchDt || matchAllowed || list[0];
            (current as any)[key] = {
                typeId: Number((target as any).shippingTypeId),
                shopId,
                typeName: String((target as any).shippingTypeName || "")
            };
            changed = true;
        }
    }

    if (changed) {
        formState.value.shippingType = current as any;
    }

    return changed;
};

const loadStoreShippingTypeData = async () => {
    if (!userId.value) return;
    try {
        const lng = Number(formState.value.address?.longitude || 0) || undefined;
        const lat = Number(formState.value.address?.latitude || 0) || undefined;
        const res = await getProxyStoreShippingType({
            userId: userId.value,
            flowType: flowType.value,
            addressId: formState.value.addressId,
            longitude: lng,
            latitude: lat
        });
        console.log("loadStoreShippingTypeData", res);
        shippingTypeData.value = res || ({} as any);
    } catch {
        shippingTypeData.value = {};
    }
};

const loadData = async () => {
    // if (!userId.value || Number.isNaN(userId.value)) {
    //     message.error(t('缺少 userId，请从代客下单入口进入。'));
    //     router.replace("/order/proxy-shopping");
    //     return;
    // }

    addressListExpanded.value = false;
    pickupContactListExpanded.value = false;
    loading.value = true;
    try {
        // userInfo 可能为空（如从独立结算页直接进入），此处不阻断初始化流程
        userInfo.value = (props.userInfo as any) || null;

        // 先同步购物车选中状态，确保在 check/index、check/update 之前完成
        if (props.cartCheckData && props.cartCheckData.length > 0) {
            await updateProxyCartCheck({ userId: userId.value, data: props.cartCheckData });
        }

        const c = await getProxyOrderCheckIndex({ userId: userId.value, flowType: flowType.value });
        checkInfo.value = c as any;
        applyResponseToForm(c as any);

        // 拉取可用支付方式并初始化默认选中（对齐 PC：进入页面必有 payTypeId）
        await loadPaymentTypes();

        // 拉取地址列表（用于选择 addressId）
        try {
            const addrRes: any = await getProxyShoppingAddressList({
                userId: userId.value,
                page: 1,
                size: 9999
            });
            const list =
                (Array.isArray(addrRes) ? addrRes : null) ||
                (Array.isArray(addrRes?.records) ? addrRes.records : null) ||
                (Array.isArray(addrRes?.list) ? addrRes.list : null) ||
                [];
            addressList.value = list as ProxyShoppingAddress[];
            // 如果没有 addressId，优先用默认地址，其次用第一个地址
            if (!formState.value.addressId) {
                const def = addressList.value.find((a) => a.isDefault === 1);
                if (def?.addressId) formState.value.addressId = def.addressId;
                else if (addressList.value?.[0]?.addressId) formState.value.addressId = Number(addressList.value[0].addressId);
            }
        } catch (e: any) {
            // 地址列表失败不阻塞结算页
            addressList.value = [];
        }

        // 拉取配送方式列表并初始化默认选中
        await loadStoreShippingTypeData();
        const changed = initShippingTypeSelection(checkInfo.value?.cartList as any[], effectiveShippingTypeData.value);
        if (changed) {
            await refreshCheck({ shippingType: formState.value.shippingType });
        }
        if (Number(formState.value.deliveryType || 0) === 2) {
            await Promise.all([loadPickupPointData(), loadPickupContactData()]);
        }
    } catch (error: any) {
        message.error(error.message || t('加载结算信息失败'));
    } finally {
        loading.value = false;
    }
};

const refreshCheck = async (patch: Partial<typeof formState.value>) => {
    if (!userId.value) return;
    if (settingFromResponse.value) return;

    loading.value = true;
    try {
        const latestRowChangePrices = collectRowChangePrices();
        const payload = {
            userId: userId.value,
            flowType: flowType.value,
            ...formState.value,
            cartChangePrices: latestRowChangePrices,
            ...patch
        };
        const res = await updateProxyOrderCheck(payload as any);
        checkInfo.value = res as any;
        Object.assign(formState.value, patch);
        applyResponseToForm(res as any);
    } catch (error: any) {
        message.error(error.message || t('更新结算信息失败'));
    } finally {
        loading.value = false;
    }
};

const onDeliveryMethodChange = async (shippingTypeId?: number) => {
    const deliveryType = Number(shippingTypeId || 0) > 0
        ? Number(shippingTypeId)
        : Number(formState.value.deliveryType || 0);
    await refreshCheck({
        shippingType: formState.value.shippingType,
        deliveryType
    });
    if (deliveryType === 2) {
        await Promise.all([loadPickupPointData(), loadPickupContactData()]);
    } else {
        pickupPointList.value = [];
        pickupContactList.value = [];
        formState.value.pickupId = undefined;
        formState.value.pickupContactId = undefined;
        formState.value.pickupContactName = "";
        formState.value.pickupContactMobile = "";
    }
};

const onAddressChange = async () => {
    await refreshCheck({ addressId: formState.value.addressId, address: undefined });
    await loadPaymentTypes();
    await loadStoreShippingTypeData();
    const changed = initShippingTypeSelection(checkInfo.value?.cartList as any[], effectiveShippingTypeData.value);
    if (changed) {
        await refreshCheck({ shippingType: formState.value.shippingType });
    }
};

const onPayTypeChange = async (payTypeId: number) => {
    if (settingFromResponse.value) return;
    const id = Number(payTypeId || 0);
    if (!Number.isFinite(id) || id <= 0) return;
    if (Number(formState.value.payTypeId || 0) === id) return;

    formState.value.payTypeId = id;
    await refreshCheck({ payTypeId: id });
};

let pointTimer: any = null;
const onPointChange = () => {
    if (settingFromResponse.value) return;
    clearTimeout(pointTimer);
    pointTimer = setTimeout(() => {
        const v = clampUsePoint(formState.value.usePoint);
        formState.value.usePoint = v;
        refreshCheck({ usePoint: v });
    }, 350);
};

const onUsePointToggle = (val: boolean) => {
    if (!canUsePoint.value) {
        usePointEnabled.value = false;
        formState.value.usePoint = 0;
        return;
    }
    if (maxAvailablePoints.value <= 0) {
        usePointEnabled.value = false;
        formState.value.usePoint = 0;
        if (val) message.warning(t('当前订单不可使用{value}', { value: integralName.value }));
        return;
    }
    if (!val) {
        formState.value.usePoint = 0;
        refreshCheck({ usePoint: 0 });
        return;
    }
    // 开启默认填 1（避免“开启但为0”无效果）
    if (!formState.value.usePoint) formState.value.usePoint = 1;
    const v = clampUsePoint(formState.value.usePoint);
    formState.value.usePoint = v;
    refreshCheck({ usePoint: v });
};

const onUseBalanceToggle = (val: boolean) => {
    if (!canUseBalance.value) {
        useBalanceEnabled.value = false;
        formState.value.useBalance = 0;
        return;
    }
    const all = Number(userInfo.value?.balance || 0);
    if (!val) {
        formState.value.useBalance = 0;
        refreshCheck({ useBalance: 0 });
        return;
    }
    formState.value.useBalance = all;
    refreshCheck({ useBalance: all });
};

const onCouponSelected = async (payload: { couponId?: number } | null) => {
    if (!userId.value) return;
    if (settingFromResponse.value) return;

    const couponId = Number(payload?.couponId || 0);

    formState.value.useCouponIds = couponId ? [couponId] : [];
    // 只使用 couponId
    formState.value.selectUserCouponIds = [];

    // 手动选择优惠券：关闭默认最优券逻辑（对齐 PC）
    await updateCoupon(false);
};

const onClearCoupon = async () => {
    if (!userId.value) return;
    if (settingFromResponse.value) return;

    formState.value.useCouponIds = [];
    formState.value.selectUserCouponIds = [];

    // 清空后同步更新结算信息与优惠券使用情况
    await updateCoupon(false);
};

const onAddressCreated = async (result: any) => {
    if (!result) return;

    try {
        const createdId = Number(result?.addressId);
        const addrRes: any = await getProxyShoppingAddressList({
            userId: userId.value,
            page: 1,
            size: 9999
        });
        const list =
            (Array.isArray(addrRes) ? addrRes : null) ||
            (Array.isArray(addrRes?.records) ? addrRes.records : null) ||
            (Array.isArray(addrRes?.list) ? addrRes.list : null) ||
            [];
        addressList.value = list as ProxyShoppingAddress[];

        // 新增成功后：自动选中并使用当前地址（调用 setSelectedAddress + 刷新结算）
        if (createdId) {
            const created = (addressList.value || []).find((a: any) => Number(a?.addressId || 0) === createdId) || { addressId: createdId };
            await onAddressCardClick(created);
        }
    } catch {
        // ignore
    }
};

const onAddressUpdated = async (result: any) => {
    if (!result) return;
    try {
        const editedId = Number(result?.addressId || result?.id || result?.data?.addressId || result?.data?.id || 0);
        const addrRes: any = await getProxyShoppingAddressList({
            userId: userId.value,
            page: 1,
            size: 9999
        });
        const list =
            (Array.isArray(addrRes) ? addrRes : null) ||
            (Array.isArray(addrRes?.records) ? addrRes.records : null) ||
            (Array.isArray(addrRes?.list) ? addrRes.list : null) ||
            [];
        addressList.value = list as ProxyShoppingAddress[];

        // 若编辑的是当前选中地址：刷新结算信息（地址内容可能影响配送方式/运费）
        if (editedId && Number(formState.value.addressId || 0) === editedId) {
            await onAddressChange();
        }
    } catch {
        // ignore
    }
};

const onAlterPriceConfirm = async (payload: { type: number; value: number; finalAmount: number }) => {
    if (!userId.value) return;
    try {
        loading.value = true;
        const originalPrice = orderChangePriceBaseAmount.value;
        const patch = {
            userId: userId.value,
            flowType: flowType.value,
            deliveryType: formState.value.deliveryType,
            useChangePrice: true,
            totalChangePriceType: Number(payload.type),
            changePriceValue: Number(payload.value),
            originalPrice,
            cartChangePrices: []
        };
        const res = await confirmProxyOrderChangePrice(patch as any);
        checkInfo.value = res as any;
        Object.assign(formState.value, {
            useChangePrice: true,
            totalChangePriceType: Number(payload.type),
            changePriceValue: Number(payload.value),
            originalPrice,
            cartChangePrices: []
        });
        rowChangeDraftMap.value = {};
        applyResponseToForm(res as any);
    } catch {
        message.error(t('保存改价失败'));
    } finally {
        loading.value = false;
    }
};

const doSubmit = async () => {
    if (!userId.value) return null as any;
    submitting.value = true;
    try {
        const rowCartChangePrices = collectRowChangePrices();
        const hasTotalChangePrice = Number(formState.value.totalChangePriceType || 0) > 0 && Number.isFinite(Number(formState.value.changePriceValue));
        const hasAnyChangePrice = hasTotalChangePrice || rowCartChangePrices.length > 0;
        const payload = {
            userId: userId.value,
            flowType: flowType.value,
            addressId: formState.value.deliveryType === 2 ? undefined : formState.value.addressId || undefined,
            address: formState.value.deliveryType === 2 ? undefined : formState.value.addressId ? undefined : formState.value.address || undefined,
            deliveryType: formState.value.deliveryType,
            pickupId: formState.value.deliveryType === 2 ? formState.value.pickupId || undefined : undefined,
            pickupContactId: formState.value.deliveryType === 2 ? formState.value.pickupContactId || undefined : undefined,
            pickupContactName: formState.value.deliveryType === 2 ? formState.value.pickupContactName || undefined : undefined,
            pickupContactMobile: formState.value.deliveryType === 2 ? formState.value.pickupContactMobile || undefined : undefined,
            payTypeId: formState.value.payTypeId,
            usePoint: Number(formState.value.usePoint || 0),
            useBalance: Number(formState.value.useBalance || 0),
            useCouponIds: formState.value.useCouponIds,
            selectUserCouponIds: formState.value.selectUserCouponIds,
            useChangePrice: hasAnyChangePrice,
            totalChangePriceType: hasTotalChangePrice ? formState.value.totalChangePriceType : undefined,
            changePriceValue: hasTotalChangePrice ? formState.value.changePriceValue : undefined,
            originalPrice: hasTotalChangePrice ? formState.value.originalPrice : undefined,
            cartChangePrices: rowCartChangePrices,
            buyerNote: formState.value.buyerNote
        };
        const res: any = await submitProxyOrder(payload as any);
        return res;
    } catch (error: any) {
        message.error(error.message || t('提交订单失败'));
        return null as any;
    } finally {
        submitting.value = false;
    }
};

const onSubmit = async () => {
    if (submitting.value) return;
    const reasons = getSubmitMissingReasons();
    if (reasons.length > 0) {
        // 一次只提示一条：按优先级返回第一条缺失项
        message.warning(reasons[0]);
        return;
    }

    const res: any = await doSubmit();
    if (!res) return;

    const orderSn = String(res?.orderSn || "");
    const unpaidAmount = Number(checkInfo.value?.total?.unpaidAmount || 0);

    // 先提交订单，拿到订单号；若未付金额>0，则提示用户核对货款到账后再发货
    if (unpaidAmount > 0) {
        unpaidDialogOrderSn.value = orderSn;
        unpaidDialogHasNavigated.value = false;
        unpaidDialogOpen.value = true;
        return;
    }

    message.success(t('下单成功{orderSnorderSn}', { orderSnorderSn: orderSn ? t('，订单号：') + orderSn : "" }));
    router.push("/order/list");
};

const lastLoadedKey = ref<string>("");
const ensureLoadedIfActive = async () => {
    if (!props.active) return;
    await loadShoppingConfigOnce();
    await loadOrderConfigOnce();
    const key = `${userId.value || 0}_${flowType.value || 0}`;
    if (key === lastLoadedKey.value && checkInfo.value) return;
    lastLoadedKey.value = key;
    await loadData();
};

watch(
    () => [props.active, userId.value, flowType.value],
    () => {
        ensureLoadedIfActive();
    },
    { immediate: true }
);

onBeforeUnmount(() => {
    clearTimeout(rowChangePreviewTimer);
    addressCardsResizeObserver?.disconnect();
    addressCardsResizeObserver = null;
    pickupContactCardsResizeObserver?.disconnect();
    pickupContactCardsResizeObserver = null;
});
</script>

<style scoped lang="less">
.proxy-shopping-check {
    padding-bottom: 72px; // 给底部固定栏留空间
}

.page-head {
    padding: 8px 0 12px;
    display: flex;
    align-items: baseline;
    gap: 10px;
}

.page-title {
    font-size: 16px;
    font-weight: 600;
}

.page-sub {
    color: #909399;
    font-size: 12px;
}

.container-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 12px;
}

.card-head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 15px;
}

.card-head .title {
    margin-bottom: 0;
}

.back-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 10px;
    background: #f5f7fa;
    color: #2a496e;
    border-radius: 6px;
    cursor: pointer;
    user-select: none;
}

.back-btn:hover {
    background: #eef2f6;
}

.back-btn:active {
    background: #e7ecf2;
}

.back-arrow {
    font-size: 16px;
    line-height: 1;
}

.title {
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 15px;
}

.user-info-card {
    display: flex;

    .info-card {
        padding: 12px;
        background: #f5f7fa;
        border-radius: 8px;
        min-width: 300px;

        .user-info-card-left {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .user-info-card-right {
            margin-top: 12px;
            display: flex;
            flex-direction: column;
            gap: 14px;
        }

        .card-info {
            display: flex;
            flex-direction: column;
            gap: 8px;
            flex: 1;
        }

        .name {
            font-size: 14px;
            font-weight: 600;
        }

        .label {
            color: #333;
        }

        .value {
            color: var(--el-color-primary);
        }
    }
}

.block {
    margin-top: 30px;
}

.order-table-block {
    width: 100%;
    max-width: 100%;
    overflow-x: auto;
}

.order-table-inner {
    min-width: 920px;
}

@media (max-width: 376px) {
    .user-info-card .info-card {
        min-width: 280px;
    }
}

.block-title {
    // font-weight: 600;
    margin-bottom: 8px;
    text-align: right;
    // color: #333;
}

.form-block {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.form-block .block-title {
    margin-bottom: 0;
    width: 70px;
    flex: 0 0 70px;
    padding-top: 8px;
}

.block-content {
    flex: 1;
    min-width: 0;
}

.delivery-method-block {
    display: flex;
    align-items: center;

    .block-title {
        padding-top: 0;
    }
}

.address-block--pickup {
    align-items: flex-start;

    .block-title {
        padding-top: 0;
    }
}

.pickup-section {
    margin-bottom: 14px;
}

.pickup-section:last-child {
    margin-bottom: 0;
}

.pickup-section-title {
    margin-bottom: 8px;
    color: #606266;
    font-size: 13px;
}

.pickup-line1 {
    color: #101010;
    font-weight: 500;
    margin-bottom: 10px;
    line-height: 18px;
    min-height: 18px;
    word-break: break-all;
}

.pickup-line2 {
    white-space: normal;
    min-height: 0;
}

.pickup-line3 {
    gap: 0;
    color: #606266;
    font-size: 14px;
    line-height: 20px;
    white-space: nowrap;
}

.pickup-contact-cards {
    gap: 8px;
}

.pickup-contact-card {
    width: 200px !important;
    height: 80px;
    flex: 0 0 200px;
    max-width: 200px;
    padding: 12px 12px;
    gap: 0;
    box-sizing: border-box;
}

.pickup-contact-card .addr-col-content {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.pickup-contact-line {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
    color: #303133;
    line-height: 18px;
}

.pickup-contact-line-icon {
    flex: 0 0 auto;
    font-size: 15px;
    color: #606266;
}

.pickup-contact-line-text {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.pickup-contact-name .pickup-contact-line-text {
    font-weight: 500;
    color: #101010;
}

.pickup-contact-phone .pickup-contact-line-text {
    color: #606266;
}

.pickup-contact-card-add {
    height: 80px;
}

.o2o-delivery-group {
    display: inline-flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 24px;
}

.o2o-delivery-group :deep(.el-radio) {
    margin-right: 0;
}

.o2o-delivery-group :deep(.el-radio.is-disabled .el-radio__label) {
    color: #303133;
}

.inline-row {
    display: flex;
    align-items: center;
}

.ml8 {
    margin-left: 8px;
}

.mt6 {
    margin-top: 6px;
}

.address-cards {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.address-cards-footer {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 10px;
}

.address-expand-toggle {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.address-expand-ico {
    font-size: 14px;
}

.address-card {
    position: relative;
    border: 1px solid #dfe1e8;
    border-radius: 3px;
    width: 300px;
    padding: 18px 12px 12px;
    cursor: pointer;
    background: #fff;
    display: flex;
    gap: 10px;
    align-items: flex-start;
    transition:
        border-color 0.18s ease,
        box-shadow 0.18s ease,
        transform 0.18s ease;
}

.addr-col-icon {
    flex: 0 0 18px;
    padding-top: 2px;
    display: flex;
    align-items: flex-start;
    justify-content: center;
}

.addr-col-content {
    flex: 1;
    min-width: 0;
}

.address-card.active {
    border: 1px solid var(--el-color-primary) !important;
    background: #fff;
}

.address-card.active::after {
    content: "";
    position: absolute;
    bottom: 0;
    right: 0;
    width: 0;
    height: 0;
    border-left: 30px solid transparent;
    border-bottom: 30px solid var(--el-color-primary);
}

.address-card.active::before {
    content: "\2713";
    position: absolute;
    right: 5px;
    bottom: 3px;
    color: #fff;
    z-index: 2;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
}

.address-card:hover {
    border-color: var(--el-color-primary);
}

.addr-action-bar {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 34px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    border-bottom-left-radius: 3px;
    border-bottom-right-radius: 3px;

    opacity: 0;
    pointer-events: none;
    transition: opacity 0.15s ease;

    .iconfont {
        font-size: 18px;
        line-height: 1;
    }

    .txt {
        font-size: 14px;
        line-height: 1;
    }
}

.address-card:hover .addr-action-bar {
    opacity: 1;
    pointer-events: auto;
}

.addr-line1 {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #6a6a6a;
    margin-bottom: 10px;

    .addr-region {
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}

.addr-ico {
    color: #101010;
    font-size: 17px;
    flex: 0 0 auto;
}

.addr-line2 {
    color: #101010;
    margin-bottom: 10px;
    line-height: 18px;
    min-height: 18px;
    word-break: break-all;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.addr-line3 {
    display: flex;
    gap: 10px;
    color: #909399;
    font-size: 12px;

    .addr-consignee {
        color: #606266;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .addr-mobile {
        flex: 0 0 auto;
        color: #606266;
    }
}

.address-card-add {
    border-style: solid;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    min-height: 80px;
    color: #909399;

    .add-ico {
        font-size: 18px;
        margin-bottom: 12px;
        color: #101010;
    }

    .add-text {
        color: #01070f;
        font-size: 13px;
        display: inline-flex;
        align-items: center;
        gap: 6px;

        .add-help-ico {
            font-size: 13px;
        }

        .add-address-tip {
            font-size: 12px;
            color: #606266;
            line-height: 18px;
        }
    }
}

.coupon-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
}

.coupon-action {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
}

.coupon-action-line {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

.coupon-selected-meta {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #606266;
    white-space: nowrap;
}

.coupon-selected {
    flex: none;
    min-width: 0;
    color: #606266;
}

/* 已选优惠券回显：对齐 CouponDrawer 卡片结构，但尺寸固定 280x100 */
.couponItem--selected {
    // width: 280px;
    height: 80px;
    display: flex;
    position: relative;
    overflow: hidden;
    background: #fff;
    border-radius: 6px;
    box-sizing: border-box;
    filter: drop-shadow(0 0 4px rgba(30, 14, 14, 0.08));
}

.couponItem--selected .coupon-remove-ico {
    position: absolute;
    top: 0px;
    right: 0px;
    font-size: 16px;
    color: #00000080;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 2;
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    transition: opacity 0.16s ease;
}

.couponItem--selected:hover .coupon-remove-ico,
.couponItem--selected:focus-within .coupon-remove-ico {
    opacity: 1;
    visibility: visible;
    pointer-events: auto;
}

.couponItem--selected .couponItemRight {
    flex: 1;
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 12px 18px 18px;
    box-sizing: border-box;
}

.couponItem--selected .couponItemRight .title {
    font-weight: bold;
    font-size: 14px;
    word-break: break-all;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex-shrink: 0;
    margin-bottom: 0;
}

.couponItem--selected .couponItemRight .scene {
    font-size: 12px;
}

.couponItem--selected .couponItemRight .date {
    font-size: 12px;
    display: flex;
    white-space: nowrap;
}

.couponItem--selected .couponItemRight .date .label {
    flex-shrink: 0;
}

.couponItem--selected .couponItemRight::before,
.couponItem--selected .couponItemRight::after {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    transform: translate(-50%, -50%);
    width: 10px;
    height: 10px;
    background-color: #f5f5f5;
    border-radius: 50%;
}

.couponItem--selected .couponItemRight::after {
    top: 100%;
}

.couponItem--selected .couponItemLeft {
    width: 25%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 18px;
    box-sizing: border-box;
    position: relative;
}

.couponItem--selected .couponItemLeft .price {
    color: var(--tig-primary);
    font-size: 22px;
    display: flex;
}

.couponItem--selected .couponItemLeft .price span {
    font-size: 20px;
    align-self: flex-end;
}

.couponItem--selected .couponItemLeft::after {
    display: block;
    content: "";
    width: 1px;
    height: calc(100% + 40px);
    position: absolute;
    top: -20px;
    border-right: 2px dashed #f5f5f5;
    left: calc(100% - 1px);
}

.coupon-globle {
    margin-left: 6px;
    padding: 1px 6px;
    border-radius: 10px;
    background: rgba(20, 86, 240, 0.1);
    color: var(--tig-primary);
}

.price-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.product-cell {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    min-width: 0;
}

.product-thumb {
    width: 70px;
    height: 70px;
    flex: 0 0 70px;
    border-radius: 4px;
    overflow: hidden;
    background: #f5f7fa;
    border: 1px solid #ebeef5;
}

.product-thumb-img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
}

.product-thumb-placeholder {
    width: 100%;
    height: 100%;
}

.product-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
    flex: 1;
}

.product-name {
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    white-space: normal;
    word-break: break-word;
}

.gray {
    color: #909399;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.extra-service-box {
    margin-top: 4px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
}

.extra-service-row {
    background: #f7f8fa;
    border-radius: 4px;
    padding: 4px 8px;
    display: flex;
    align-items: center;
    max-width: 250px;
    justify-content: space-between;
    flex-wrap: nowrap;
    gap: 10px;
    min-width: 0;
    box-sizing: border-box;
}

.extra-service-left {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    min-width: 0;
    flex: 1 1 auto;
    overflow: hidden;
}

.extra-service-left-text {
    display: block;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #606266;
    line-height: 18px;
}

.extra-service-split {
    flex: 0 0 auto;
    color: #c0c4cc;
}

.extra-service-price {
    flex: 0 0 auto;
    color: #606266;
    font-weight: 500;
    white-space: nowrap;
}

.change-price-cell-render {
    width: 180px;
    box-sizing: border-box;
}

.change-price-select {
    min-width: 70px;
    height: 38px;
    margin: 0;
}

.change-price-unit {
    font-size: 14px;
    width: 18px;
    padding-right: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    top: -2px;
}

:deep(.change-price-cell-render .el-form-item) {
    margin-bottom: 0;
}

:deep(.change-price-cell-render .el-input__inner) {
    text-align: right;
    font-size: 15px;
    padding-right: 3px;

    &::-webkit-outer-spin-button,
    &::-webkit-inner-spin-button {
        -webkit-appearance: none;
    }

    &[type="number"] {
        appearance: textfield;
        -moz-appearance: textfield;
    }
}

:deep(.change-price-cell-render .el-input-group__prepend) {
    width: 70px;
    padding: 0;
    border-top-left-radius: 6px;
    border-bottom-left-radius: 6px;
    box-sizing: border-box;
}

:deep(.change-price-cell-render .el-input-group--prepend .el-input-group__prepend .el-select .el-select__wrapper) {
    background-color: #f5f5f5;
    gap: 2px;
    padding: 0 5px;
    border-top-left-radius: 6px;
    border-bottom-left-radius: 6px;
    box-sizing: border-box;

    &:hover {
        box-shadow: 0 0 0 1px var(--el-input-border-color) inset;
    }
}

:deep(.change-price-cell-render .el-input__wrapper) {
    width: 90px;
    box-shadow: 0 0 0 1px var(--el-input-border-color, var(--el-border-color)) inset;
    padding: 0 6px;
    border-top-right-radius: 6px;
    border-bottom-right-radius: 6px;
    box-sizing: border-box;
}

:deep(.change-price-cell-render .el-select__caret) {
    color: #000;
}

/* 强制显示 disabled 状态下的下拉箭头（Element Plus 默认会隐藏 disabled 状态的箭头） */
:deep(.change-price-cell-render .el-select .el-select__wrapper.is-disabled .el-select__suffix),
:deep(.change-price-cell-render .el-select .el-select__wrapper.is-disabled .el-select__caret) {
    display: inline-flex !important;
    opacity: 1 !important;
}

:deep(.change-price-cell-render .el-select .el-select__wrapper.is-disabled .el-select__caret) {
    color: #a8abb2 !important;
}

:deep(.change-price-cell-render .el-select__placeholder) {
    text-align: center;
}

:deep(.change-price-cell-render .el-select__wrapper) {
    height: 100%;
}

:global(.change-price-popper .el-select-dropdown__item) {
    padding: 0 12px;
    text-align: center;
}

.gift-store {
    &:not(:last-child) {
        margin-bottom: 10px;
    }

    max-width: 400px;
}

.gift-store-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
}

.gift-cards {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.gift-card {
    position: relative;
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 6px;
    background: #f5f7fa;
    border: 1px solid #ebeef5;
    max-width: 400px;
}

.gift-thumb {
    background: #fff;
}

.gift-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.gift-name {
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    white-space: normal;
    word-break: break-word;
}

.gift-num {
    position: absolute;
    right: 12px;
    bottom: 10px;
    color: #606266;
    font-weight: 600;
}

.price-card {
    .price-row {
        display: flex;
        gap: 20px;
        color: #303133;
        align-items: center;

        .label {
            flex: 0 0 70px;
            text-align: right;
            display: inline-flex;
            align-items: center;
            justify-content: flex-end;
            gap: 6px;
        }

        .value {
            flex: 1 1 auto;
            min-width: 0;
            display: inline-flex;
            align-items: center;
            justify-content: flex-start;
            gap: 6px;
            text-align: left;
            font-weight: 600;
        }
    }

    .price-pay .pay-amount {
        color: var(--el-color-primary);
    }
}

.help-ico {
    font-size: 14px;
    color: #acacac;
    cursor: pointer;
}

.popover-box {
    .popover-title {
        font-size: 13px;
        font-weight: 600;
        margin-bottom: 10px;
        color: #303133;
    }

    .goods-list {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .goods-col {
            .shop-name {
                font-size: 12px;
                color: var(--tig-primary);
                margin-bottom: 4px;
            }

            .txt {
                font-size: 12px;
                color: #303133;
                margin-bottom: 8px;
            }

            .goods-thumbs {
                display: flex;
                gap: 8px;
                flex-wrap: wrap;
            }

            .thumb {
                width: 36px;
                height: 36px;
                border-radius: 4px;
                object-fit: cover;
                background: #f5f7fa;
                border: 1px solid #ebeef5;
            }
        }
    }
}

.discount-title {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    margin-bottom: 5px;
}

.discount-content {
    font-size: 13px;

    .discount-item-box {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .discounts-value {
            font-weight: 600;
        }

        .item-value {
            font-size: 13px;
        }

        &.hassub {
            display: block;
        }

        .sub-item-box {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding-top: 2px;
            color: #909399;
            font-size: 12px;
        }

        &:last-child {
            padding-top: 0;
        }
    }
}

.proxy-checkout-warp {
    right: 16px;
    z-index: 10;
}

.proxy-checkout-warp .selected-action {
    padding-left: 0;
    padding: 0 16px;
    width: 100%;
}

.point-info {
    display: flex;
    align-items: center;
}

.point-num {
    color: var(--el-color-primary);
}
</style>
