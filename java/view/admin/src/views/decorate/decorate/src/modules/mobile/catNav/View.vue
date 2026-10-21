<template>
    <div
        class="catnav-main module-ad-con module-cat_nav"
        :style="mainFormat['background-color'] + mainFormat['padding-top'] + mainFormat['padding-bottom'] + mainFormat['background-image']"
    >
        <img class="catnav-logo" :style="logoFormat.logoHeight" :src="imageFormat(logoFormat.logoPic?.picUrl || '')" />

        <div class="default-search" :style="searchFormat['padding-right'] + searchFormat['padding-left']">
            <div class="default-search-config catnav-search" :style="searchFormat['itemBackgroundColor'] + searchFormat['itemRadius']">
                <el-icon style="margin-left: 10px" color="#999" :size="18"><Search /></el-icon>
                <div class="catnav-search-text" :style="searchFormat['searchTextColor']">{{ searchFormat.searchText }}</div>
            </div>
        </div>
        <div class="catnav-menu">
            <ul class="catnav-flex">
                <li class="catnav-menu-item" :style="catitemFormat.witdh" v-for="(item, index) in catNavList" :key="item.mobileCatNavId">
                    <div :class="{ active: index === 0 }" :style="catitemFormat.color">{{ item.categoryName }}</div>
                </li>
            </ul>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onUnmounted, onBeforeMount, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleCatNavType, ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultIcoPic, defaultBackgroundPic } from "@/views/decorate/decorate/src/modules/";
import { getMobileCatNavList } from "@/api/decorate/mobileCatNav";
import { Search } from "@element-plus/icons-vue";
import type { MobileCatNavFilterState } from "@/types/decorate/mobileCatNav.d";
// 在modules加入要使用的模块
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleCatNavType>("module") as Ref<ModuleType & ModuleCatNavType>;

const defaultModule = ref({
    navBackgroundPic: defaultBackgroundPic,
    logoPic: defaultIcoPic,
    itemWidth: 50,
    textColor: "",
    backgroundColor: "",
    itemBackgroundColor: "",
    searchTextColor: "",
    isGanged: 0,
    logoHeight: 0,
    searchText: t('搜索商品'),
    boxPadding: 10,
    itemRadius: 3,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    frame: defaultFrame
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const mainFormat = computed(() => {
    return {
        "background-color": "background-color:" + module?.value.backgroundColor + ";",
        "padding-top": "padding-top:" + module?.value.boxPaddingTop + "px;",
        "padding-bottom": "padding-bottom:" + module?.value.boxPaddingBottom + "px;",
        "background-image": `background: url('${imageFormat(module?.value?.navBackgroundPic?.picUrl)}') no-repeat center center`
    };
});
const logoFormat = computed(() => {
    return {
        logoHeight: "height:" + module?.value.logoHeight + "px;",
        logoPic: module?.value.logoPic
    };
});
const searchFormat = computed(() => {
    return {
        searchText: module?.value.searchText ? module.value.searchText : t('搜索商品'),
        itemBackgroundColor: "background-color:" + module?.value.itemBackgroundColor + ";",
        searchTextColor: "color:" + module?.value.searchTextColor + ";",
        itemRadius: "border-radius:" + module?.value.itemRadius + "px;",
        "padding-right": "padding-right:" + module?.value.boxPadding + "px;",
        "padding-left": "padding-left:" + module?.value.boxPadding + "px;"
    };
});
const catitemFormat = computed(() => {
    return {
        witdh: "width:" + module?.value.itemWidth + "%;",
        color: "color:" + module?.value.textColor
    };
});

const catNavList = ref<MobileCatNavFilterState[]>([]);
const getMobileCatNavData = async () => {
    try {
        const result = await getMobileCatNavList({ paging: 0, isShow: 1 });
        catNavList.value = [{ categoryName: t('推荐'), mobileCatNavId: 0 }, ...result.records];
    } catch (error) {
        console.error(error);
    }
};

onBeforeMount(async () => {
    getMobileCatNavData();
});
onUnmounted(() => {});
</script>
<style lang="less" scoped>
.catnav-logo {
    box-sizing: border-box;
    margin-left: 10px;
    margin-top: 2.5px;
    margin-bottom: 5px;
    object-fit: scale-down;
    max-width: 110px;
}
.default-search {
    padding: 0 10px;
    margin-bottom: 5px;
}
.default-search-config {
    background-color: #fff;
    border-radius: 3px;
}
.catnav-search {
    display: flex;
    align-items: center;
    height: 35px;
}
.catnav-search-text {
    font-size: 14px;
    margin-left: 5px;
}
.catnav-menu {
    height: 40px;
    width: 100%;
    overflow: hidden;
    box-sizing: content-box;
    .catnav-flex {
        height: 100%;
        width: 100%;
        white-space: nowrap;
        .catnav-menu-item {
            height: 40px;
            line-height: 40px;
            text-align: center;
            display: inline-block;
            margin: 0 5px;
            div {
                width: 100%;
                font-size: 16px;
                font-weight: bold;
                position: relative;

                &.active {
                    font-size: 22px;
                }

                &.active::after {
                    content: "";
                    position: absolute;
                    bottom: 1px;
                    left: 20%;
                    right: 20%;
                    background: rgba(255, 255, 255, 0.7);
                    border-radius: 3px;
                    height: 3px;
                }
            }
        }

        .catnav-menu-item:nth-child(1) {
            margin-left: 10px;
        }
    }
}
</style>
