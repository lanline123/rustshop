<template>
    <TotalContainer :module="module.frame" :class="'module-ad-con module-notice'">
        <!-- 占位显示：没有文本时 -->
        <div v-if="!module?.text || module.text.trim() === ''" class="module-ad-empty empty-notice">
            <div class="image-empty-bg"></div>
            <div class="desc">点击添加通知公告</div>
        </div>
        <!-- 有文本时显示通知内容 -->
        <div v-else class="notice-ad-warp">
            <div class="notice-con">
                <div class="notice-item">
                    <div class="item-content">
                        <div class="item-notice-con" :style="itemFormat.itemBackgroundColor + itemFormat.itemHeight + itemFormat.itemRadius">
                            <div class="item-notice-ico" v-if="module?.icoPic?.picUrl">
                                <el-image class="item-img" fit="contain" :src="imageFormat(module.icoPic.picUrl)" />
                            </div>
                            <div class="item-notice-list">
                                <div class="item-notice-input" :style="itemFormat.textColor">{{ module?.text || "" }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </TotalContainer>
</template>
<script lang="ts" setup>
//pc端装修通知公告组件
import { computed } from "vue";
import { TotalContainer } from "@/components/decorate/index";
import { imageFormat } from "@/utils/format";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { pcNoticeDefaults } from "./index";
import type { PcNoticeModule } from "./types";

const props = defineProps<{ module: PcNoticeModule }>();
const module = props.module;

// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, pcNoticeDefaults);

const itemFormat = computed(() => {
    console.log(module, 'props.module');
    return {
        itemBackgroundColor: module?.frame?.itemBackgroundColor ? `background-color:${module.frame.itemBackgroundColor};` : "",
        itemRadius: module?.frame?.itemRadius ? `border-radius:${module.frame.itemRadius}px;` : "",
        itemHeight: module?.frame?.itemHeight ? `height:${module.frame.itemHeight}px;` : "height:20px;",
        textColor: module?.frame?.textColor ? `color:${module.frame.textColor};` : ""
    };
});
</script>
<style lang="less" scoped>
:deep(.base-class){
    background: transparent !important;
}
.notice-ad-warp {
    width: 100%;
    box-sizing: border-box;

    .notice-con {
        width: 100%;
    }

    .item-notice-con {
        background: #fff;
        height: 20px;
        display: flex;
        align-items: center;
        padding: 5px 0;
        color: #555555;

        .item-notice-ico {
            margin-left: 10px;
            width: 16px;
            height: 16px;
            flex-shrink: 0;

            .item-img {
                width: 100%;
                height: 100%;
            }
        }

        .item-notice-list {
            margin-left: 8px;
            flex: 1;
            overflow: hidden;

            .item-notice-input {
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
        }
    }
}

/* 占位图片样式 */
.module-ad-empty {
    padding-top: 30px;
    padding-bottom: 50px;
    text-align: center;
    background-color: #f2f4f6;

    .image-empty-bg {
        background: url("@/views/decorate/decorate/src/img/empty-img-bg.png") no-repeat center center;
        background-size: 100% auto;
        width: 100px;
        margin: 0 auto 10px;
        height: 100px;
    }

    .desc {
        text-align: center;
        color: #888;
        font-size: 14px;
    }
}
</style>
