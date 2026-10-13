<template>
    <tig-layout :title="navbarTitle" :shop-id="shopId" :background="pageModulesData.titleBackgroundColor" :color="pageModulesData.titleColor">
        <view class="decorate-page-window" :style="{ ...cssVariable }">
            <modules v-if="Object.keys(shopInfo).length > 0" :modules="modulesList" :shop-info="shopInfo" @refresh-shop-detail="getShopInfo" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { onLoad, onShareAppMessage } from "@dcloudio/uni-app";
import { ref, computed, onMounted } from "vue";
import { getShopDecorate, getShopDetail } from "@/api/shop/shop";
import modules from "@/pages/customPage/modules/shopModules.vue";
import type { ShopDetailItem } from "@/types/shop/shop";
import type { PageModule } from "@/types/home/home";
import { useConfigStore } from "@/store/config";
import { setPageMeta } from "@/utils/seo";
import { parseQueryString } from "@/utils";
import { handleDecorateLoadError } from "@/utils/handleDecorateLoadError";

const { t } = useI18n();
// 装修模块位于 customPage 分包，小程序端通过分包异步化引用，占位组件由 bundle-optimizer 注入
defineOptions({
    componentPlaceholder: {
        modules: "view"
    }
});

const configStore = useConfigStore();
const shopId = ref(0);
const previewId = ref<number | undefined>(undefined);

onMounted(() => {
    // #ifdef H5
    setPageMeta({
        title: `${t("店铺首页")} - ${configStore.baseInfo.shopTitle}`,
        description: `${t("欢迎访问")}${configStore.baseInfo.shopTitle}`
    });
    // #endif
});

onLoad((options) => {
    // #ifdef MP-WEIXIN
    if (options?.scene) {
        Object.assign(options, parseQueryString(decodeURIComponent(options.scene)));
    }
    // #endif
    previewId.value = options?.previewId ? Number(options.previewId) : undefined;
    if (options && options.shopId) {
        shopId.value = options.shopId;
        getShopInfo();
        __getShopDecorate();
    }
});

const navbarTitle = ref(t("店铺详情"));

const modulesList = ref<any[]>([]);
const pageModulesData = ref<PageModule>({} as PageModule);

const __getShopDecorate = async () => {
    try {
        const result = await getShopDecorate(shopId.value, previewId.value);
        modulesList.value = result.moduleList;
        pageModulesData.value = result.pageModule;
    } catch (error) {
        if (await handleDecorateLoadError(error)) {
            return;
        }
        console.error(error);
    }
};

const cssVariable = computed(() => {
    return {
        "background-color": `${pageModulesData.value.backgroundColor}`,
        "background-image": `url("${pageModulesData.value.backgroundImage?.picUrl}")`,
        "background-size": `${getSize(pageModulesData.value?.backgroundSize)}`,
        "background-repeat": `${getRepeat(pageModulesData.value?.backgroundRepeat)}`
    };
});

const getSize = (key: string) => {
    let repeat: { [key: string]: any } = {
        "1": "100% auto",
        "2": "auto 100%",
        "3": "100% 100%"
    };
    return repeat[key];
};

const getRepeat = (key: string) => {
    let repeat: { [key: string]: any } = {
        "1": "no-repeat",
        "2": "repeat",
        "3": "repeat-x",
        "4": "repeat-y"
    };
    return repeat[key];
};

const shopInfo = ref<ShopDetailItem>({} as ShopDetailItem);
const getShopInfo = async () => {
    try {
        const result = await getShopDetail(shopId.value);
        shopInfo.value = result;
        navbarTitle.value = result.shopTitle;
    } catch (error: any) {
        console.error(error);
    }
};

onShareAppMessage(() => {
    return {
        title: shopInfo.value.shopTitle
    };
});
</script>

<style lang="scss" scoped></style>
