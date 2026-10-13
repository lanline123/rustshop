<template>
    <div class="module-ad-con module-member-my-service" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="my-service-wrap" :style="wrapStyle">
                <div class="my-service-container" :style="containerStyle">
                    <div class="my-service-header">
                        <div class="my-service-title">{{ module.moduleTitle || $t('服务管理') }}</div>
                    </div>
                    <div v-if="!serviceList.length" class="my-service-empty">
                        <div class="my-service-empty-text">{{ $t('点击添加服务项') }}</div>
                    </div>
                    <template v-else-if="module.layoutStyle === 1">
                        <div class="my-service-list">
                            <div v-for="(item, index) in serviceList" :key="index" class="my-service-list-item">
                                <div class="my-service-list-left">
                                <div class="my-service-list-icon" :style="iconStyle">
                                    <img v-if="productImage(item)" :src="productImage(item)" :alt="item.picTitle" />
                                    </div>
                                    <div class="my-service-list-text">{{ item.picTitle }}</div>
                                </div>
                                <div class="iconfont-h5 icon-a-gengduo20"></div>
                            </div>
                        </div>
                    </template>
                    <template v-else>
                        <div class="my-service-grid">
                            <div v-for="(item, index) in serviceList" :key="index" class="my-service-grid-item">
                                <div class="my-service-grid-icon" :style="iconStyle">
                                    <img v-if="productImage(item)" :src="productImage(item)" :alt="item.picTitle" />
                                </div>
                                <div class="my-service-grid-text">{{ item.picTitle }}</div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, Ref } from "vue";
import { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate.d";
import { productImage } from "@/utils/format";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type ModuleMyServiceType = Omit<ModuleType, "frame"> & {
    moduleTitle?: string;
    layoutStyle?: number;
    iconRadius?: number;
    serviceList?: ModulePicListType[];
    frame: ModuleFrameType;
};

const module = defineModel<ModuleMyServiceType>("module") as Ref<ModuleMyServiceType>;

const defaultInnerPadding = 15;
const defaultIconRadius = 50;
const defaultModule = {
    moduleTitle: t('服务管理'),
    layoutStyle: 1,
    iconRadius: defaultIconRadius,
    serviceList: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 10,
        innerPadding: defaultInnerPadding,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};
mergeDefaultModule(module.value, defaultModule);

const frame = computed(() => module.value.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));
const serviceList = computed(() => module.value.serviceList ?? []);

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));

const iconStyle = computed(() => ({
    borderRadius: `${module.value.iconRadius ?? defaultIconRadius}%`
}));
</script>
<style lang="less" scoped>
.my-service-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.my-service-container {
    box-sizing: border-box;
}
.my-service-header {
    margin-bottom: 15px;
}
.my-service-title {
    font-size: 15px;
    font-weight: 600;
    color: #333;
}
.my-service-empty {
    padding: 30px 20px;
    text-align: center;
    background: #f5f7fa;
    border-radius: 8px;
}
.my-service-empty-text {
    color: #999;
    font-size: 12px;
}
.my-service-list {
    display: flex;
    flex-direction: column;
    .icon-a-gengduo20 {
        font-size: 10px;
    }
}
.my-service-list-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;
    &:last-child {
        border-bottom: none;
    }
}
.my-service-list-left {
    display: flex;
    align-items: center;
    gap: 12px;
}
.my-service-list-icon {
    width: 20px;
    height: 20px;
    overflow: hidden;
    flex-shrink: 0;
    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}
.my-service-list-text {
    font-size: 14px;
    color: #333;
}
.my-service-list-arrow {
    font-size: 10px;
    color: #888b94;
}
.my-service-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px 8px;
}
.my-service-grid-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}
.my-service-grid-icon {
    width: 40px;
    height: 40px;
    overflow: hidden;
    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}
.my-service-grid-text {
    font-size: 12px;
    color: #333;
    text-align: center;
    line-height: 1.4;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>
