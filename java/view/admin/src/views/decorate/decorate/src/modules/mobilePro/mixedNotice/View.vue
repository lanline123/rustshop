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
import { ref, computed, Ref } from "vue";
import { ModuleType, ModuleMixedNoticeDisplayType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle } from "@/views/decorate/decorate/src/modules/";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const swiperOption = ref<any>({
    pagination: false,
    direction: "vertical",
    speed: 800,
});
const module = defineModel<ModuleType & ModuleMixedNoticeDisplayType>("module") as Ref<ModuleType & ModuleMixedNoticeDisplayType>;
const groups = [
    {
        notices: [
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202673o7FOMjRwwsNOXB8OjP.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202673o7FOMjRwwsNOXB8OjP.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('免运费'),
                picDesc: t('前三单购物均可免运费！')
            }
        ]
    },
    {
        notices: [
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('24*7客户支持'),
                picDesc: t('随时随地提供保障')
            },
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('24*7客户支持'),
                picDesc: t('随时随地提供保障')
            },
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('24*7客户支持'),
                picDesc: t('随时随地提供保障')
            }
        ]
    }
];
const defaultModule = ref({
    noticeGroups: groups, // 公告分组
    moduleColor: {
        // 模块颜色设置
        titleColor: "#171B1E", // 模块大标题颜色
        sellingPointColor: "#999999" // 卖点小标题颜色
    },

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
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
