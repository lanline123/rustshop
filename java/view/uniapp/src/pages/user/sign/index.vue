<template>
    <tig-layout :title="$t('签到')">
        <div class="qiandaoBox">
            <div class="title">{{ `${$t("每日打卡领")}${configStore.integralName}` }}</div>
            <div class="wrap">
                <div class="samll-title">
                    {{ $t("今天打卡得") }}<span>{{ signInfo.signPoints }}</span
                    >{{ $t(configStore.integralName) }}
                </div>
                <scroll-view class="selected-scroll" scroll-x :scroll-left="scrollLeft" :show-scrollbar="false">
                    <div class="selectedBox" :style="{ width: `${Math.max(signInfo.days.length, 1) * 50}px` }">
                        <div class="line" />
                        <div v-for="(day, index) in signInfo.days" :key="index" class="item" :class="{ selected: signInfo.record >= day.dayNum }">
                            <div class="item-content">
                                <div v-if="signInfo.record < day.dayNum" class="num">{{ day.points }}</div>
                                <div v-else class="num" />
                                <div class="date">{{ day.name }}</div>
                            </div>
                        </div>
                    </div>
                </scroll-view>
                <div class="qiandaoBtnBox">
                    <div class="qdBtn">
                        <div v-if="signInfo.isSign" class="dk-now">
                            <span>{{ $t("今日已打卡") }}</span>
                        </div>
                        <div v-else class="dk-now" @click="sign">
                            <span>{{ $t("立即打卡") }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <template v-if="signInfo.recommendGoods">
            <div class="recgoodsBox">
                <div class="title">
                    <div class="bg">
                        {{ $t("今日特卖 发现好货") }}
                    </div>
                    <navigator url="/pages/search/result?intro=hot">
                        <div class="moregoods">{{ $t("查看更多") }} <span class="iconfont icon-xiangyou" /></div>
                    </navigator>
                </div>
                <div class="wrapGoods">
                    <view v-for="(item, index) in signInfo.recommendGoods" :key="index" class="item" @click="toProductPage(item)">
                        <image class="rec-img" mode="aspectFill" lazy-load :src="productImage(item)" />
                        <div class="info">
                            <div class="name">{{ item.productName }}</div>
                            <format-price :is-bottom="false" class="price" :price-data="item.productPrice" />
                        </div>
                    </view>
                </div>
            </div>
        </template>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 签到打卡页
 * - 展示连续签到进度和每日可获得积分
 * - 支持每日打卡领取积分
 * - 展示今日特卖推荐商品
 */
import { ref, computed, nextTick } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { staticResource } from "@/utils";
import { productImage } from "@/utils/format";
import { getSignList, signIn } from "@/api/user/sign";
import type { SignInfo } from "@/types/user/sign";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n";

const configStore = useConfigStore();

const { t } = useI18n();

const signInfo = ref<SignInfo>({
    signPoints: 0,
    record: 0,
    isSign: 0,
    days: [],
    recommendGoods: []
});
const scrollLeft = ref(0);
const scrollToCurrent = () => {
    nextTick(() => {
        const index = signInfo.value.days.findIndex((day: any) => day.dayNum >= signInfo.value.record);
        const currentIndex = index < 0 ? signInfo.value.days.length - 1 : index;
        uni.createSelectorQuery()
            .select(".selected-scroll")
            .boundingClientRect((rect: any) => {
                if (rect) scrollLeft.value = Math.max(0, currentIndex * 50 - rect.width / 2 + 25);
            })
            .exec();
    });
};

const __getSignList = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getSignList("user/sign/index");
        Object.assign(signInfo.value, result);
        scrollToCurrent();
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const sign = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        await signIn();
        uni.showToast({
            title: t("打卡成功"),
            duration: 1000,
            icon: "none"
        });
        setTimeout(function () {
            __getSignList();
        }, 1000);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
    } finally {
        uni.hideLoading();
    }
};

const toProductPage = (params: any) => {
    uni.navigateTo({ url: "/pages/product/index?id=" + params.productId });
};

const qiandaoBg = computed(() => {
    return `url(${staticResource("qiandao/bg.png")})`;
});

const hotBg = computed(() => {
    return `url(${staticResource("qiandao/hot_bg.png")})`;
});

onLoad(() => {
    __getSignList();
});
</script>

<style lang="scss" scoped>
.qiandaoBox {
    position: relative;

    .title {
        font-size: 16px;
        font-weight: 500;
        text-align: center;
        color: #fff;
        height: 50px;
        line-height: 50px;
        background: #e45363;
    }

    .wrap {
        background-image: v-bind(qiandaoBg);
        background-repeat: no-repeat;
        background-size: 100% 280px;
        min-height: 300px;
        padding: 40px 30px;
        box-sizing: border-box;

        .samll-title {
            padding-left: 3%;
            font-size: 14px;
            font-weight: 500;
            margin-bottom: 20px;
        }

        .samll-title span {
            color: #dd9260;
            margin: 0 3px;
        }
    }

    .selected-scroll {
        width: 100%;
        white-space: nowrap;
    }
    .selectedBox {
        position: relative;
        display: flex;
        flex-wrap: nowrap;

        .line {
            position: absolute;
            top: 18px;
            left: 10%;
            width: 80%;
            height: 1px;
            background: #d8d9d8;
            z-index: 1;
        }

        .item {
            display: inline-block;
            width: 50px;
            flex: none;
            text-align: center;
            position: relative;
            height: 80px;

            .item-content {
                width: 100%;
                height: 50px;
                color: #dd9260;
                position: absolute;
                top: 0;
                left: 0;
                z-index: 2;
                font-size: 12px;

                .num {
                    margin: 0 auto;
                    width: 30px;
                    height: 30px;
                    line-height: 30px;
                    background-image: url("https://demo.tigshop.cn/static/mini/images/user/qiandao/2.png");
                    background-repeat: no-repeat;
                    background-size: 100%;
                    border-radius: 50%;
                    margin-bottom: 10px;
                }
            }

            &.selected .item-content .num {
                background-image: url("https://demo.tigshop.cn/static/mini/images/user/qiandao/1.png");
            }
        }
    }

    .qiandaoBtnBox {
        padding: 0 10px;
        padding-top: 25px;

        .qdBtn {
            position: relative;
            width: 100%;
            max-width: 375px;
            height: 40px;
            line-height: 40px;
            margin: 0 auto;
            text-align: center;
            color: #fff;
            font-weight: 500;
            font-size: 14px;
            background-image: url("https://demo.tigshop.cn/static/mini/images/user/qiandao/btn-def.png");
            background-repeat: no-repeat;
            background-size: 100% 100%;
        }

        .dk-now::after {
            content: "";
            position: absolute;
            top: 14px;
            margin-left: 5px;
            width: 12px;
            height: 12px;
            background-image: url("https://demo.tigshop.cn/static/mini/images/user/qiandao/gif-icon.png");
            background-repeat: no-repeat;
            background-size: 100% 100%;
        }
    }
}

.recgoodsBox {
    margin: 0 10px;
    background: #fff;
    border-radius: 9px;

    .title {
        display: flex;
        flex-wrap: nowrap;
        height: 50px;
        line-height: 50px;
        justify-content: space-between;

        .bg {
            width: 200px;
            height: 50px;
            background-image: v-bind(hotBg);
            background-repeat: no-repeat;
            background-size: 100% 100%;
            padding-left: 100rpx;
            font-weight: bold;
            line-height: 55px;
            white-space: nowrap;
            box-sizing: border-box;
        }

        .moregoods {
            display: flex;
            align-items: center;
            font-size: 12px;
            color: #999;
            padding-top: 5px;
            padding-right: 10px;
        }
    }

    .wrapGoods {
        padding: 10px;
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        column-gap: 5px;
        row-gap: 10px;

        .item {
            display: flex;
            flex-direction: column;
            text-align: center;

            .rec-img {
                width: 100%;
                height: 80px;
                border-radius: 9px;
                border: 1px solid #efefef;
            }

            .name {
                padding: 0 15px;
                height: 20px;
                line-height: 20px;
                overflow: hidden;
                font-weight: 500;
                color: #333;
                font-size: 12px;
                margin-top: 4px;
            }

            .price {
                font-size: 12px;
                color: #e45363;
            }
        }
    }
}
</style>
