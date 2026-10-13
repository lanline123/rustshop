<template>
    <LoginSelectShop
        v-if="!isIndex"
        :selectShopFlag="selectShopFlag"
        :myShopList="myShopList"
        :vendorList="vendorList"
        :posList="posList"
        :loading="loading"
        :userinfo="userinfo"
        :adminType="adminType"
        @callBack="toIndex"
    ></LoginSelectShop>
    <IndexSelectShop
        v-if="isIndex"
        :selectShopFlag="selectShopFlag"
        :myShopList="myShopList"
        :vendorList="vendorList"
        :posList="posList"
        :loading="loading"
        :userinfo="userinfo"
        :adminType="adminType"
        @callBack="toIndex"
        @changeShopList="_getShopMyShop"
        @close="closePopup"
    ></IndexSelectShop>
</template>
<script setup lang="ts">
import LoginSelectShop from "./src/LoginSelectShop.vue";
import IndexSelectShop from "./src/IndexSelectShop.vue";
import { onMounted, ref } from "vue";
import { getShopMyShop, chooseShop } from "@/api/merchant/shop";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { useLicensedStore } from "@/store/licensed";
import { useMenusStore } from "@/store/menu";
import { useThemeStore } from "@/store/theme";
import { notification } from "ant-design-vue";
import { useRoute, useRouter } from "vue-router";
import { getMenu } from "@/utils/menus";
import { isMerchant, isS2b2c, isStore } from "@/utils/version";
import { embedRedirectLocation } from "@/utils/loginRedirect";
import dayjs from "dayjs";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    isIndex: {
        type: Boolean,
        default: false
    },
    selectShopFlag: {
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["closePopup", "update:selectShopFlag"]);
const router = useRouter();
const route = useRoute();
const toIndex = async (e: any) => {
    // 收银台入口沿用门店真实身份(shop)，进入后跳转收银台路由
    const posEntry = e.type == "pos";
    const realType = posEntry ? "shop" : e.type;
    localStorage.setItem("adminType", realType);
    localStorage.removeItem("shopId");
    localStorage.removeItem("vendorId");
    if (realType == "shop" || realType == "store") {
        localStorage.setItem("shopId", String(e.id));
    }
    if (realType == "vendor") {
        localStorage.setItem("vendorId", String(e.id));
    }
    const userStore = useUserStore();
    const configStore = useConfigStore();
    const licensedStore = useLicensedStore();
    const menusStore = useMenusStore();
    const themeStore = useThemeStore();

    // 更新后台设置项及授权信息（updateConfig 需先完成，_getLicensed 依赖其写入的 config）
    await Promise.all([userStore.updateUserInfo(), configStore.updateConfig()]);
    await licensedStore._getLicensed();
    themeStore.getThemeInfo();
    notification["success"]({
        message: t('登录成功'),
        placement: "top",
        duration: 1.5,
        description: t('您好，欢迎回来')
    });

    if (props.isIndex && !posEntry) {
        handleIndexShopSwitch();
        return;
    }
    menusStore.clearMenus();
    const routers = await getMenu();
    menusStore.setRouters(routers || []);
    localStorage.setItem("lastOpenTime", String(dayjs().unix()));
    // 仅 web-view 嵌入深链回跳目标页，其余登录一律进首个菜单首页
    const loc = embedRedirectLocation(route.query.redirect);
    if (loc) {
        router.push(loc);
    } else {
        router.push(routers ? firstRouter(routers[0]) : "/");
    }
};

const handleIndexShopSwitch = async () => {
    // 先显示白屏
    const menusStore = useMenusStore();
    const loadingElement = document.createElement("div");
    loadingElement.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: white;
        z-index: 9999;
    `;
    document.body.appendChild(loadingElement);

    try {
        const routers = await getMenu();
        menusStore.setRouters(routers || []);
        emit("closePopup");
        // 切换组织后统一落到该组织首页，避免停留在旧组织的路径导致 404
        const homePath = routers && routers.length ? firstRouter(routers[0]) : "/";
        const path = `${router.options.history.base}${homePath}`;
        window.history.pushState({ path }, "", path);

        // 强制页面重绘前稍作延迟
        await new Promise((resolve) => setTimeout(resolve, 50));
        location.reload();
    } catch (error) {
        // 移除加载元素（如果有错误发生）
        if (document.body.contains(loadingElement)) {
            document.body.removeChild(loadingElement);
        }
        console.error(error);
    }
};

const firstRouter = (router: any) => {
    if (router.children?.[0]) {
        const firstChild = router.children[0];
        if (router.path === firstChild.path) {
            return router.path;
        } else if (firstChild.children?.[0]) {
            return (router.path == "/" ? "" : router.path) + "/" + firstChild.children[0].path;
        }
    }
    return router.path;
};

const closePopup = () => {
    emit("closePopup");
};
const myShopList = ref<any[]>([]);
const vendorList = ref<any[]>([]);
const posList = ref<any[]>([]);
const loading = ref(true);
const userinfo = ref({
    username: ""
});
const adminType = ref<any>("");
const _chooseShop = async (obj: any) => {
    try {
        loading.value = true;
        const result = await chooseShop(obj);
    } catch (error: any) {
    } finally {
        loading.value = false;
    }
};
const _getShopMyShop = async () => {
    try {
        loading.value = true;
        const result = await getShopMyShop({ sortField: "lastLoginTime" });
        if (result.shop.records) {
            myShopList.value = result.shop.records;
        }
        if (result.vendor.records) {
            vendorList.value = result.vendor.records;
        }
        if (result.userinfo) {
            userinfo.value = result.userinfo;
        }
    } catch (error: any) {
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    const accessToken = localStorage.getItem("accessToken");
    adminType.value = localStorage.getItem("adminType");
    if (accessToken && (isMerchant() || isStore() || isS2b2c())) {
        _getShopMyShop();
    } else {
        loading.value = false;
    }
    
});
</script>
