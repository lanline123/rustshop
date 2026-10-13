<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>我的钱包2</h3>
            <div class="dec-edit-desc">展示用户余额、优惠券、商品收藏、浏览记录等信息</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group" v-for="(item, index) in module.items" :key="item.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ item.label }}图标</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="item.icon" :default-value="defaultIconObjs[item.key]"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">内容说明</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-edit-tip">该模块展示用户的资产与行为数据，包括余额、优惠券、商品收藏、浏览记录，数据自动获取，无需手动配置。</div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { PicSelect } from "@/components/decorate";
import { ModuleIcoPicType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { MyWalletV2Module, WalletV2Item } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as MyWalletV2Module);

const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
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
    { key: "balance", label: "余额", icon: { ...defaultIconObjs.balance } },
    { key: "coupon", label: "优惠券", icon: { ...defaultIconObjs.coupon } },
    { key: "collect", label: "商品收藏", icon: { ...defaultIconObjs.collect } },
    { key: "history", label: "浏览记录", icon: { ...defaultIconObjs.history } }
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
