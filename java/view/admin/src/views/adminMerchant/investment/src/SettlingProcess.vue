<template>
    <div class="bg-info">
        <div class="entry-main">
            <img class="bg-image" src="@/assets/join/ruzhubg.png" />
            <div class="title-row">
                <div class="title-txt-1">{{ $t('阶段一：入驻前准备') }}</div>
                <div class="title-txt-2">{{ $t('阶段二：入驻申请') }}</div>
                <div class="title-txt-3">{{ $t('阶段三：资质审核') }}</div>
                <div class="title-txt-4">{{ $t('阶段四：开店任务') }}</div>
            </div>
            <div class="module-part" :class="['part-' + index, 'size-' + item.size]" v-for="(item, index) in module">
                <h3 class="part-title" :class="['title-' + item.align]">{{ item.title }}</h3>
                <span class="content" v-html="item.content"></span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, PropType } from "vue";
interface moduleItem {
    size: number;
    align: number;
    title: string;
    content: string;
}
const props = defineProps({
    module: {
        type: Array as PropType<moduleItem[]>,
        default: []
    }
});
</script>
<style lang="less" scoped>
.bg-info {
    //transform: scale(0.5);
    transform-origin: top left;
    display: flex;
    align-items: center;
    width: 100%;
    height: auto;
    background-color: #ffffff;
    justify-content: center;
    .entry-main {
        position: relative;
        width: 95%;

        .bg-image {
            width: 100%;
        }

        .title-row {
            width: 100%;
            height: 100%;
            position: absolute;
            top: 0;
            left: 0;
            z-index: 10;
            display: flex;
            flex-direction: column;
            color: #fff;
            font-size: 16px;
            div {
                position: absolute;
                left: 60px;
            }
            .title-txt-1 {
                top: 1.5%;
            }
            .title-txt-2 {
                top: 23.2%;
            }
            .title-txt-3 {
                top: 54.1%;
            }
            .title-txt-4 {
                top: 78.2%;
            }
        }

        .module-part {
            position: absolute;
            z-index: 11;
            background-color: transparent;
            display: flex;
            flex-direction: column;
            gap: 15px;
            //transform: scale(2);

            .part-title {
                color: #3d97f7;
                font-weight: bold;
                font-size: 20px;
                box-sizing: border-box;
            }

            .title-1 {
                padding-left: 44px;
                text-align: left;
            }

            .title-2 {
                text-align: left;
            }

            .content {
                overflow-y: auto;
                font-size: 13px;
                white-space: pre-wrap; /* 保留空格和换行符，允许文本换行 */
                word-wrap: break-word; /* 允许长单词和 URL 断开换行 */
                word-break: break-all; /* 强制单词在任意位置断开换行 */
                line-height: 24px;

                &::-webkit-scrollbar {
                    display: none;
                }
            }
        }

        .part-0 {
            left: 31%;
            top: 6.1%;
        }

        .part-1 {
            left: 38.6%;
            top: 13.8%;
        }

        .part-2 {
            left: 41.3%;
            top: 27.9%;
        }

        .part-3 {
            left: 40.2%;
            top: 36.9%;
        }

        .part-4 {
            left: 40.5%;
            top: 46.4%;
        }

        .part-5 {
            left: 29.3%;
            top: 58%;
        }

        .part-6 {
            left: 47%;
            top: 69.8%;
        }

        .part-7 {
            left: 23.8%;
            top: 83.5%;
        }
        .part-8 {
            left: 48.3%;
            top: 93.6%;
        }

        .size-0 {
            width: 22%;
            height: 3.8%;
            //background-color: red;
        }

        .size-1 {
            width: 28.7%;
            height: 3.8%;
            //background-color: #ffae00;
        }

        .size-2 {
            width: 31%;
            height: 4.8%;
            //background-color: #00b2ff;
        }
        .size-3 {
            width: 31%;
            height: 6%;
            //background-color: rgba(58, 58, 58, 0.9);
        }
        .size-4 {
            width: 26%;
            height: 3.8%;
            //background-color: rgba(58, 58, 233, 0.9);
        }
        .size-5 {
            width: 26%;
            height: 3.8%;
            //background-color: rgba(222, 222, 222, 0.9);
        }
    }
}
@media only screen and (max-width: 3000px) {
    .title-row {
        font-size: 22px !important;
        div {
            left: 100px !important;
        }
    }
}
@media only screen and (max-width: 2200px) {
    .title-row {
        font-size: 20px !important;
        div {
            left: 80px !important;
        }
    }
}
@media only screen and (max-width: 1950px) {
    .title-row {
        font-size: 18px !important;
        div {
            left: 60px !important;
        }
    }
}
@media only screen and (max-width: 1750px) {
    .title-row {
        font-size: 14px !important;
        div {
            left: 60px !important;
        }
    }
}
@media only screen and (max-width: 1600px) {
    .title-row {
        font-size: 14px !important;
        div {
            left: 60px !important;
        }
    }
    .title-1 {
        padding-left: 30px !important;
    }
    .part-title {
        font-size: 14px !important;
    }
    .content {
        font-size: 10px !important;
        line-height: 16px !important;
    }
}
@media only screen and (max-width: 1500px) {
    .title-row {
        font-size: 12px !important;
        div {
            left: 45px !important;
        }
    }
}
@media only screen and (max-width: 1300px) {
    .title-row {
        font-size: 10px !important;
        div {
            left: 30px !important;
        }
    }
}
@media only screen and (max-width: 1200px) {
    .title-row {
        font-size: 8px !important;
        div {
            left: 30px !important;
        }
    }
    .title-1 {
        padding-left: 18px !important;
    }
    .part-title {
        font-size: 8px !important;
    }
    .content {
        font-size: 7px !important;
        line-height: 12px !important;
    }
}
@media only screen and (max-width: 750px) {
     .title-row {
        font-size: 6px !important;
        div {
            left: 30px !important;
        }
    }
    .title-1 {
        padding-left: 19px !important;
    }
    .part-title {
        font-size: 7px !important;
    }
    .content {
        font-size: 5px !important;
        line-height: 8px !important;
    }
}
</style>
