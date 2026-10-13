<template>
    <div class="com-main">
        <el-carousel :interval="3000" arrow="always">
            <el-carousel-item v-for="item in module[0].images" :key="item">
                <el-image fit="cover" style="width: 100%; height: 100%" :src="imageFormat(item.picUrl)"></el-image>
            </el-carousel-item>
        </el-carousel>
        <div class="zs-info">
            <div class="container">
                <h2 class="com__title">{{ $t('我要合作') }}</h2>
                <div class="zs-info__wrap">
                    <div class="cooperation-col">
                        <div class="cooperation-image plan"></div>
                        <h3>{{ $t('招商计划') }}</h3>
                        <p>{{ $t('查询发布的热招类目和品牌') }}</p>
                        <div class="cooperation-button">{{ $t('查看招商计划') }}</div>
                    </div>
                    <div class="cooperation-col">
                        <div class="cooperation-image mode"></div>
                        <h3>{{ $t('合作模式') }}</h3>
                        <p>{{ $t('了解可以合作那些模式，各模式的优势和区别。') }}</p>
                        <div class="cooperation-button">{{ $t('查看合作模式') }}</div>
                    </div>
                    <div class="cooperation-col">
                        <div class="cooperation-image standard"></div>
                        <h3>{{ $t('招商标准') }}</h3>
                        <p>{{ $t('查询自身行业所需要提交的资质') }}</p>
                        <div class="cooperation-button">{{ $t('查看招商标准') }}</div>
                    </div>
                    <div class="cooperation-col">
                        <div class="cooperation-image join"></div>
                        <h3>{{ $t('我要入驻') }}</h3>
                        <p>{{ $t('我已了解所有内容，需要立即入驻') }}</p>
                        <div class="cooperation-button active">{{ $t('立即入驻') }}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="ms-bg">
            <div ref="targetElement" class="ms-info">
                <div class="container">
                    <h2 class="com__title">{{ $t('合作模式') }}</h2>
                    <div class="mode-list">
                        <div :class="{ current: tabActive === 1 }" class="mode-col mode-1" @mouseenter="tabClick(1)">
                            <span>{{ $t('平台模式') }}</span>
                        </div>
                        <div :class="{ current: tabActive === 2 }" class="mode-col mode-2" @mouseenter="tabClick(2)">
                            <span>{{ $t('自营模式') }}</span>
                        </div>
                    </div>
                    <div class="mode-detail">
                        <div v-show="tabActive === 1" class="mode-detail-content">
                            <div class="mode-detail-pic">
                                <img :src="imageFormat(module[1].image)" />
                            </div>
                            <h3>{{ module[1].title }}</h3>
                            <span class="content" v-html="module[1].content"></span>
                            <div class="learn-more">{{ $t('了解详情') }}</div>
                        </div>
                        <div v-show="tabActive === 2" class="mode-detail-content">
                            <div class="mode-detail-pic">
                                <img :src="imageFormat(module[2].image)" />
                            </div>
                            <h3>{{ module[2].title }}</h3>
                            <span class="content" v-html="module[2].content"></span>
                            <div class="learn-more">{{ $t('了解详情') }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { imageFormat } from "@/utils/format";

const props = defineProps({
    module: {
        type: Array<any>,
        default: []
    }
});
const tabActive = ref(1);
const tabClick = (value: number) => {
    tabActive.value = value;
};
</script>
<style scoped lang="less">
.com-main {
    background-color: #f5f5f5;
    box-sizing: border-box;
    margin: 0 2.5px;
    transform: scale(0.5);
    transform-origin: top left;
    width: 200%;

    .com__title {
        color: #333333;
        font-size: 26px;
        font-weight: 400;
        padding: 44px 0;
        text-align: center;
    }

    .zs-info {
        transform: scale(0.75);
        transform-origin: top center;
        background-color: #f5f5f5;
        height: 380px;

        .zs-info__wrap {
            display: flex;
            justify-content: space-between;
            padding: 60px 0 50px;
        }

        .cooperation-col {
            background: #ffffff none repeat scroll 0 0;
            border: 1px solid #eeeeee;
            position: relative;
            text-align: center;
            transition: all 0.3s ease 0s;
            width: 24%;

            &:first-child {
                margin-left: 0;
            }

            .cooperation-image {
                background: url("@/assets/join/zhaoshang_z.png");
                border: 1px solid #efefef;
                border-radius: 50%;
                height: 118px;
                left: 50%;
                margin-left: -60px;
                position: absolute;
                top: -60px;
                transition: border 0.3s ease 0s;
                width: 118px;
                z-index: 100;
            }

            .standard {
                background-position: -130px 0;
            }

            .mode {
                background-position: -130px -130px;
            }

            .join {
                background-position: 0 -130px;
            }

            h3 {
                color: #333333;
                font-size: 20px;
                font-weight: 700;
                margin: 70px 0 10px;
            }

            p {
                color: #999999;
                height: 36px;
                padding: 3px 50px;
            }

            .cooperation-button {
                background: #ffffff none repeat scroll 0 0;
                border: 1px solid #ffae00;
                color: #ffae00;
                display: block;
                font-size: 14px;
                height: 38px;
                line-height: 38px;
                margin: 20px auto 40px;
                transition: all 0.3s ease 0s;
                width: 138px;
                cursor: pointer;

                &.active {
                    background: #ffae00;
                    color: #fff;
                    font-weight: 700;
                }
            }
        }
    }

    .ms-bg {
        background-color: #ffffff;

        .ms-info {
            transform: scale(0.75);
            transform-origin: top center;
            background-color: #ffffff;
            height: 1px;

            .mode-list {
                display: flex;
                justify-content: center;
                border-bottom: 1px solid #d9deea;
                position: relative;
                text-align: center;

                .mode-col {
                    padding-top: 50px;
                    height: 35px;
                    background-position: top center;
                    background-repeat: no-repeat;
                    font-size: 16px;
                    color: #919fbc;
                    text-align: center;
                    transition: all 0.2s;
                    width: 40%;

                    span {
                        display: inline-block;
                        vertical-align: middle;
                        position: relative;
                        padding: 0 5px;
                        width: auto;
                        height: 35px;
                    }

                    span::after {
                        display: none;
                        content: "";
                        position: absolute;
                        bottom: -6px;
                        left: 50%;
                        margin-left: -5px;
                        border: 5px solid #3998f0;
                        border-bottom-width: 0;
                        border-left-color: transparent;
                        border-right-color: transparent;
                    }

                    &.current span:after {
                        display: block;
                    }

                    &.mode-1 {
                        background: url("@/assets/join/pic01.png") top center no-repeat;

                        &.current {
                            background: url("@/assets/join/picon01.png") top center no-repeat;
                            color: #3998f0;

                            span {
                                border-bottom: 2px solid #3998f0;
                            }

                            span::after {
                                border-bottom-color: #3998f0;
                            }
                        }
                    }

                    &.mode-2 {
                        background: url("@/assets/join/pic02.png") top center no-repeat;

                        &.current {
                            background: url("@/assets/join/picon02.png") top center no-repeat;
                            color: #86c300;

                            span {
                                border-bottom: 2px solid #86c300;
                            }

                            span::after {
                                border-bottom-color: #86c300;
                                border-top-color: #86c300;
                            }
                        }
                    }
                }
            }

            .mode-detail {
                padding-top: 70px;
                background: center right no-repeat;
                position: relative;

                .mode-detail-content {
                    height: 450px;

                    .mode-detail-pic {
                        position: absolute;
                        right: 0;
                        top: 50px;
                        width: 730px;
                    }

                    h3 {
                        margin: 20px 0;
                        font-size: 20px;
                        font-weight: 700;
                        color: #333;
                    }

                    p {
                        font-size: 14px;
                        line-height: 2;
                    }

                    .learn-more {
                        display: block;
                        margin-top: 20px;
                        padding-left: 30px;
                        width: 130px;
                        height: 40px;
                        line-height: 40px;
                        color: #fff;
                        font-size: 14px;
                        background: url("@/assets/join/zhaoshang_z.png") no-repeat 0px -420px;
                    }
                }
            }
        }
    }
    .content {
        box-sizing: border-box;
        font-size: 14px;
        line-height: 2.5;
    }
}
</style>
