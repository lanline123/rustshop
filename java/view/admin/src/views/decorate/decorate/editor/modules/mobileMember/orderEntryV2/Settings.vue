<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>我的订单2</h3>
            <div class="dec-edit-desc">卡片+列表样式的订单入口</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">模块标题</div>
                </div>
                <div class="dec-edit-group-con">
                    <TigInput v-model="module.title" placeholder="订单" :maxlength="10" />
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">卡片入口</div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-con">
                    <PicList
                        v-model:photos="module.cardList"
                        :isMultiple="true"
                        :showText="true"
                        :showLink="false"
                        :showAddButton="false"
                        title="添加卡片入口"
                    ></PicList>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">列表入口</div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-con">
                    <PicList
                        v-model:photos="module.listItems"
                        :isMultiple="true"
                        :showText="true"
                        :showLink="false"
                        :showAddButton="false"
                        title="添加列表入口"
                    ></PicList>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">卡片背景色</div>
                    <div class="value">
                        <a class="dec-color-reset" @click="resetCardBgColor">重置</a>
                    </div>
                </div>
                <div class="dec-edit-group-con">
                    <SelectColor v-model:color="module.cardBackgroundColor"></SelectColor>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">角标背景色</div>
                    <div class="value">
                        <a class="dec-color-reset" @click="resetBadgeBgColor">重置</a>
                    </div>
                </div>
                <div class="dec-edit-group-con">
                    <SelectColor v-model:color="module.badgeBgColor"></SelectColor>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">角标文字色</div>
                    <div class="value">
                        <a class="dec-color-reset" @click="resetBadgeTextColor">重置</a>
                    </div>
                </div>
                <div class="dec-edit-group-con">
                    <SelectColor v-model:color="module.badgeTextColor"></SelectColor>
                </div>
            </div>
            <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { PicList } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import { ModulePicListType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { OrderEntryV2Module } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as OrderEntryV2Module);

const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

const defaultCardList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569TRT9GOOm6YGq6RHtPG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569TRT9GOOm6YGq6RHtPG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "待付款"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569I3uI9owxG28WqM4kZq.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569I3uI9owxG28WqM4kZq.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "进行中"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569bsPmvK2X8p96DhrC8f.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569bsPmvK2X8p96DhrC8f.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "已完成"
    }
];
const defaultListItems: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569mV8f2OAjjvfB6GURqx.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569mV8f2OAjjvfB6GURqx.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "评价"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569FJBk502CI7W1gOfH0l.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569FJBk502CI7W1gOfH0l.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "售后"
    }
];

const defaultCardBgColor = "#f6f7fb";
const defaultBadgeBgColor = "#ff4d4f";
const defaultBadgeTextColor = "#ffffff";

mergeDefaultModule(module.value, {
    title: "订单",
    cardList: defaultCardList.map((item) => ({ ...item })),
    listItems: defaultListItems.map((item) => ({ ...item })),
    cardBackgroundColor: defaultCardBgColor,
    badgeBgColor: defaultBadgeBgColor,
    badgeTextColor: defaultBadgeTextColor,
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: 16,
        boxPadding: 0,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
});

const resetCardBgColor = () => {
    module.value.cardBackgroundColor = defaultCardBgColor;
};
const resetBadgeBgColor = () => {
    module.value.badgeBgColor = defaultBadgeBgColor;
};
const resetBadgeTextColor = () => {
    module.value.badgeTextColor = defaultBadgeTextColor;
};
</script>

<style lang="less" scoped>
:deep(.gallery-list-warp) {
    .dec-pic-group-item-con {
        .item-info {
            display: flex;
            flex-direction: column;
            justify-content: center;
        }
    }
}
</style>
