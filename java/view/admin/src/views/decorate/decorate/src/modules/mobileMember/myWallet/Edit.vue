<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('我的钱包') }}</h3>
            <div class="dec-edit-desc">{{ $t('展示用户余额、优惠券、积分等资产信息') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group dec-edit-group-block">
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
            <div class="dec-edit-group" v-if="module.layoutStyle === 2">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('钱包图标') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.walletIcon" :default-value="defaultWalletIconObj"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('内容说明') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-edit-tip">{{ $t('该模块展示用户的资产信息，包括余额、优惠券、积分等，数据自动获取，无需手动配置。') }}</div>
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
type ModuleMyWalletType = Omit<ModuleType, "frame"> & {
    layoutStyle?: number;
    walletIcon?: ModuleIcoPicType;
    frame: ModuleFrameType;
};

const module = defineModel<ModuleMyWalletType>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const defaultWalletIconObj: ModuleIcoPicType = {
    picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779873859zO64yVrNvp2y0lUsOz.png",
    picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779873859zO64yVrNvp2y0lUsOz.png"
};

mergeDefaultModule(module.value, {
    layoutStyle: 1,
    walletIcon: { ...defaultWalletIconObj },
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: 16,
        boxPadding: 5,
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
