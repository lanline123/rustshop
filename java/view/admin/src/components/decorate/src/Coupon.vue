<template>
    <div class="mod_coupon" v-if="couponList.length > 0">
        <div class="coupon_tit">
            <div class="coupon_Left">
                <h4>{{ $t('领券中心') }}</h4>
                <p class="en_tit">COUPON</p>
                <p class="go_coupon">GO <em>&gt;&gt;</em></p>
            </div>
            <div class="coupon_edge"></div>
        </div>
        <div class="coupon_list">
            <div class="item" v-for="(item, index) in couponList" :key="index">
<!--                 <div class="coupon_edge"></div>-->
                <a>
                    <div class="coupon_detail">
                        <div class="price">
                            <template v-if="item.couponType == 1">
                                <Price
                                    :currencyStyle="{
                                        position: 'relative',
                                        bottom: '-7px'
                                    }"
                                    :decimalsStyle="{
                                        fontSize: '14px',
                                        position: 'relative',
                                        bottom: '-5px'
                                    }"
                                    :showText="false"
                                    :completeDecimal="false"
                                    v-model="item.couponMoney"
                                ></Price>
                            </template>
                            <template v-else>
                                <div class="coupon_discount">
                                    {{ item.couponDiscount }}<span>{{ $t('折') }}</span>
                                </div>
                            </template>
                        </div>
                        <div class="direction">
                            <el-tooltip
                                effect="light"
                                :content="item.couponName"
                                :disabled="!!(item.couponName && item.couponName.length < 10)"
                                placement="top"
                            >
                                <p class="coupon_sort">{{ item.couponName }}</p>
                            </el-tooltip>

                            <p>{{ item.couponDesc }}</p>
                        </div>
                    </div>
                    <p class="use_btn">{{ couponStatus(item.isReceive!, item.limitNum!, item.receiveNum!) }}<em>&nbsp;&gt;</em></p>
                </a>
                <div class="coupon_edge"></div>
            </div>
            <div class="more_coupon">
                <a href="">{{ $t('查看更多') }}</a>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { getHomeCoupon } from "@/api/product/product";
import Price from "./Price.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const couponList = ref<any[]>([
    {
        couponId: 1,
        couponName: t('满60减5'),
        couponType: 1,
        couponMoney: 5,
        couponDiscount: 0,
        limitNum: 0,
        receiveNum: 0,
        isReceive: 0
    },
    {
        couponId: 2,
        couponName: t('折扣券'),
        couponType: 2,
        couponMoney: 10,
        couponDiscount: 8,
        limitNum: 0,
        receiveNum: 0,
        isReceive: 0
    },
]);

const couponStatus = (isReceive: number, limitNum: number, receiveNum: number) => {
    if (limitNum === 0) {
        if (isReceive === 1) {
            return t('已领取');
        } else if (receiveNum > 0) {
            return t('再次领取');
        } else {
            return t('马上领');
        }
    } else {
        if (isReceive === 0) {
            return t('马上领');
        } else if (receiveNum >= limitNum) {
            return t('已领取');
        } else {
            return t('再次领取');
        }
    }
};
</script>
<style lang="less" scoped>
.text {
    font-size: 12px;
}
/* 首页领券中心模块*/
.mod_coupon {
    height: 106px;
    margin-top: 25px;
    display: flex;
}
.mod_coupon .coupon_Left:hover {
    text-decoration: none;
    color: #EE0A24;
}
.mod_coupon .coupon_tit {
    display: flex;
}
.mod_coupon .coupon_tit .coupon_Left {
    width: 153px;
    height: 100%;
    padding: 12px 0 0 18px;
    border-radius: 2px 0 0 2px;
    background-color: #ec3639;
    color: #fff;
}
.mod_coupon .coupon_tit h4 {
    height: 30px;
    line-height: 30px;
    font-size: 22px;
    font-weight: normal;
}
.mod_coupon .coupon_tit .en_tit {
    font-size: 14px;
    line-height: 28px;
    color: #f7afb0;
}
.mod_coupon .coupon_tit .go_coupon {
    transition:
        transform 0.4s ease,
        -webkit-transform 0.4s ease,
        -moz-transform 0.4s ease,
        -o-transform 0.4s ease;
    margin-top: 8px;
    font-size: 12px;
    font-family: "Microsoft YaHei", "微软雅黑";
}
.mod_coupon .coupon_tit .coupon_Left:hover {
    color: #fff !important;
}
.mod_coupon .coupon_tit a:hover .go_coupon {
    -webkit-transform: translate3d(5px, 0, 0);
    -moz-transform: translate3d(5px, 0, 0);
    transform: translate3d(5px, 0, 0);
}
.mod_coupon .coupon_tit .coupon_edge {
    float: left;
    width: 18px;
    height: 106px;
    background: url("@/assets/decorate/indexbody_sprite.png") no-repeat 0 0;
}
.mod_coupon .coupon_list {
    display: flex;
    height: 106px;
    width: 100%;
}
.mod_coupon .coupon_list .item {
    display: flex;
    flex: 1;
    overflow: hidden;
    max-width: 246px;
}
.mod_coupon .coupon_list .item a {
    width: 100%;
    max-width: 229px;
    height: 106px;
    padding: 0 5px;
    background: #fff url("@/assets/decorate/dotbg_repeat.png") repeat 55px 1px;
    box-shadow: 1px 0px 2px #ccc;
    color: #999;
    box-sizing: border-box;
}
.mod_coupon .coupon_list .item .coupon_detail {
    height: 52px;
    padding-top: 20px;
    display: flex;
    align-items: center;
}
.mod_coupon .coupon_list .item .price {
    min-width: 95px;
    margin-top: 3px;
    color: #ff7070;
    line-height: 32px;
    font-weight: bold;
    font-size: 32px;
    justify-content: center;
    .coupon_discount {
        font-weight: bold;
        text-align: center;
        line-height: 32px;
        & > span {
            font-size: 12px;
            margin-left: 5px;
        }
    }
}
.mod_coupon .coupon_list .item .direction {
    flex: 1;
    color: #999;
    line-height: 20px;
    overflow: hidden;
    padding-right: 10px;
}
.mod_coupon .coupon_list .item .direction p {
    height: 20px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
}
.mod_coupon .coupon_list .item .coupon_sort {
    font-size: 14px;
    font-weight: bold;
    color: #ff7070;
}
.mod_coupon .coupon_list .item .use_btn {
    border-top: 1px solid #f3f3f3;
    text-align: center;
    line-height: 30px;
    margin-top: 20px;
    em {
        vertical-align: middle;
    }
}
.mod_coupon .coupon_list .item .use_btn:hover {
    color: var(--main-bg);
}
.mod_coupon .coupon_list .coupon_edge {
    float: left;
    width: 18px;
    height: 106px;
    background: url("@/assets/decorate/indexbody_sprite.png") no-repeat -30px 0;
}
.mod_coupon .coupon_list .more_coupon {
    position: relative;
    width: 20px;
    overflow: hidden;
}
.mod_coupon .coupon_list .more_coupon a {
    position: absolute;
    top: -5px;
    left: -9px;
    width: 30px;
    height: calc(100% + 10px);
    overflow: hidden;
    padding-top: 30px;
    padding-left: 10px;
    background: url("@/assets/decorate/indexbody_sprite.png") repeat -250px 0;
    text-align: left;
    line-height: 14px;
    color: #ccc;
}

:deep(.price-row) {
    .integer{
        align-self: flex-end;
    }
}
</style>
