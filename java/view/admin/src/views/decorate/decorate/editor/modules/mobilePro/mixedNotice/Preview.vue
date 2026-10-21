<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div class="mixed-notice">
                <div class="notices">
                    <template v-for="(item, index) in module.noticeGroups" :key="index">
                        <div class="notices-item-box">
                            <template v-if="item.notices.length === 1">
                                <template v-for="(subItem, subIndex) in item.notices">
                                    <div class="notices-item">
                                        <div class="notices-item-img">
                                            <img :src="imageFormat(subItem.picUrl)" alt="" />
                                        </div>
                                        <div class="notices-item-content">
                                            <div class="content-title">{{ subItem.picTitle }}</div>
                                            <div class="content-subtitle">{{ subItem.picDesc }}</div>
                                        </div>
                                    </div>
                                </template>
                            </template>
                            <template v-else>
                                <Swiper class="swiper" :swiperOption="swiperOption" v-model="item.notices">
                                    <template v-slot:default="{ item: notice }">
                                        <div class="notices-item">
                                            <div class="notices-item-img">
                                                <img :src="imageFormat(notice.picUrl)" alt="" />
                                            </div>
                                            <div class="notices-item-content">
                                                <div class="content-title">{{ notice.picTitle }}</div>
                                                <div class="content-subtitle">{{ notice.picDesc }}</div>
                                            </div>
                                        </div>
                                    </template>
                                </Swiper>
                            </template>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { mixedNoticeDefaults } from "./index";
import type { MixedNoticeModule } from "./types";

const swiperOption = ref<any>({
    pagination: false,
    direction: "vertical",
    speed: 800,
});
const props = defineProps<{ module: MixedNoticeModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, mixedNoticeDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
</script>
<style lang="less" scoped>
.mixed-notice {
    width: 100%;
    .notices {
        display: flex;
        width: 100%;
        overflow: hidden;
        align-items: center;
        column-gap: 40px;

        .notices-item-box {
            flex: 1;
            flex-shrink: 0;

            .swiper {
                height: 40px;
            }

            .notices-item {
                display: flex;
                align-items: center;
                width: 100%;
                height: 40px;
                column-gap: 10px;

                .notices-item-img {
                    width: 20px;
                    height: 20px;

                    img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                    }
                }

                .notices-item-content {
                    flex: 1;
                    display: flex;
                    flex-direction: column;
                    font-size: 10px;
                    line-height: 12px;

                    .content-title {
                        color: #505259;
                        font-weight: 600;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }

                    .content-subtitle {
                        margin-top: 2px;
                        color: #888b94;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }
                }
            }
        }
    }
}

.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
</style>
