<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('我的订单') }}</h3>
            <div class="dec-edit-desc">{{ $t('用于会员中心展示订单状态入口') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('布局样式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.layoutStyle">
                        <el-radio-button :value="1">{{ $t('样式一') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('样式二') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group" v-if="module.layoutStyle === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块标题') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <TigInput v-model="module.title" :placeholder="$t('我的订单')" :maxlength="10" />
                </div>
            </div>
            <div class="dec-divider-line" v-if="module.layoutStyle === 1"></div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('订单入口列表') }}</div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-con">
                    <PicList
                        v-model:photos="module.entryList"
                        :isMultiple="true"
                        :showText="true"
                        :showLink="false"
                        :showAddButton="false"
                        :title="$t('添加订单入口')"
                    ></PicList>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('角标背景色') }}</div>
                    <div class="value">
                        <a class="dec-color-reset" @click="resetBadgeBgColor">{{ $t('重置') }}</a>
                    </div>
                </div>
                <div class="dec-edit-group-con">
                    <SelectColor v-model:color="module.badgeBgColor"></SelectColor>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('角标文字色') }}</div>
                    <div class="value">
                        <a class="dec-color-reset" @click="resetBadgeTextColor">{{ $t('重置') }}</a>
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
import { ref, watch } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { PicList } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type ModuleOrderEntryType = Omit<ModuleType, "frame"> & {
    title?: string;
    layoutStyle?: number;
    entryList: ModulePicListType[];
    badgeBgColor?: string;
    badgeTextColor?: string;
    frame: ModuleFrameType;
};

const getDefaultBadgeBgColor = (layoutStyle?: number) => (layoutStyle === 1 ? "#4d9be4" : "#ff4d4f");
const defaultBadgeTextColor = "#ffffff";

const module = defineModel<ModuleOrderEntryType>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const style1EntryList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296LEk49r7zpXWPuZYHE1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296LEk49r7zpXWPuZYHE1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待付款')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296MMBp6rwui5XUCK23J1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296MMBp6rwui5XUCK23J1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待发货')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296yHIquz41WhOu77OOOM.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296yHIquz41WhOu77OOOM.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待收货')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688297NOaA9Zx6mSws3oqgh6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688297NOaA9Zx6mSws3oqgh6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待评价')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688297PzS7ITTZI1NC5BXiG4.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688297PzS7ITTZI1NC5BXiG4.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('退换/售后')
    }
];

const style2EntryList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948168bVpgRKHWQeyik4YB5i.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948168bVpgRKHWQeyik4YB5i.png",
        picTitle: t('待付款')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948167OzuyBnYUTGf6sMQcS4.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948167OzuyBnYUTGf6sMQcS4.png",
        picTitle: t('待发货')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948168Ool4xEFRoCZqQ5eTDq.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948168Ool4xEFRoCZqQ5eTDq.png",
        picTitle: t('待收货')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948168iIVh0NpHDDSf41hFPN.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948168iIVh0NpHDDSf41hFPN.png",
        picTitle: t('退换/售后')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948169KmcNUMPH14twh8qBdD.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948169KmcNUMPH14twh8qBdD.png",
        picTitle: t('我的订单')
    }
];

mergeDefaultModule(module.value, {
    title: t('我的订单'),
    layoutStyle: 1,
    entryList: style1EntryList.map((item) => ({ ...item })),
    badgeBgColor: getDefaultBadgeBgColor(module.value.layoutStyle ?? 1),
    badgeTextColor: defaultBadgeTextColor,
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: 16,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
});

const resetBadgeBgColor = () => {
    module.value.badgeBgColor = getDefaultBadgeBgColor(module.value.layoutStyle);
};
const resetBadgeTextColor = () => {
    module.value.badgeTextColor = defaultBadgeTextColor;
};

watch(
    () => module.value.layoutStyle,
    (newStyle) => {
        const list = newStyle === 2 ? style2EntryList : style1EntryList;
        module.value.entryList = list.map((item) => ({ ...item }));
        module.value.badgeBgColor = getDefaultBadgeBgColor(newStyle);
    }
);
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
