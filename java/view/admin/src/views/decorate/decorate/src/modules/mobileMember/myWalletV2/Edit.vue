<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('我的钱包2') }}</h3>
            <div class="dec-edit-desc">{{ $t('展示用户余额、优惠券、商品收藏、浏览记录等信息') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group" v-for="(item, index) in module.items" :key="item.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('{label}图标', { label: item.label }) }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="item.icon" :default-value="defaultIconObjs[item.key]"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('内容说明') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-edit-tip">{{ $t('该模块展示用户的资产与行为数据，包括余额、优惠券、商品收藏、浏览记录，数据自动获取，无需手动配置。') }}</div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { PicSelect } from "@/components/decorate";
import { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface WalletV2Item {
    key: string;
    label: string;
    icon: ModuleIcoPicType;
}

type ModuleMyWalletV2Type = Omit<ModuleType, "frame"> & {
    items: WalletV2Item[];
    frame: ModuleFrameType;
};

const module = defineModel<ModuleMyWalletV2Type>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const defaultIcons: Record<string, string> = {
    balance: "https://oss.tigshop.com/official/img/pc/202605/177993337419dbsyRWczzneL4mhf.png",
    coupon: "https://oss.tigshop.com/official/img/pc/202605/17799333737quaCAwzzMe1XpmYHd.png",
    collect: "https://oss.tigshop.com/official/img/pc/202605/1779933373JYzFsV1Ky8YZ32BzDg.png",
    history: "https://oss.tigshop.com/official/img/pc/202605/1779933373CFhQRjCp2OMvTY1vjQ.png"
};

const defaultIconObjs: Record<string, ModuleIcoPicType> = {
    balance: { picUrl: defaultIcons.balance, picThumb: defaultIcons.balance },
    coupon: { picUrl: defaultIcons.coupon, picThumb: defaultIcons.coupon },
    collect: { picUrl: defaultIcons.collect, picThumb: defaultIcons.collect },
    history: { picUrl: defaultIcons.history, picThumb: defaultIcons.history }
};

const defaultItems: WalletV2Item[] = [
    { key: "balance", label: t('余额'), icon: { ...defaultIconObjs.balance } },
    { key: "coupon", label: t('优惠券'), icon: { ...defaultIconObjs.coupon } },
    { key: "collect", label: t('商品收藏'), icon: { ...defaultIconObjs.collect } },
    { key: "history", label: t('浏览记录'), icon: { ...defaultIconObjs.history } }
];

mergeDefaultModule(module.value, {
    items: defaultItems,
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: 12,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
});
</script>
<style lang="less" scoped>
.dec-edit-tip {
    color: #999;
    font-size: 12px;
    line-height: 1.6;
}
</style>
