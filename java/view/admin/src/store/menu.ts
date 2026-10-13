import { defineStore } from "pinia";
import { ref } from "vue";
import type { MainMenu } from "@/types/common/common.d";

export interface useMenus {
    mainMenu: MainMenu[] | null;
    routers: any[];
    menuActive: boolean;
    childMenuShow: boolean;
    hasAddRoutes: boolean;
}

export const useMenusStore = defineStore(
    "menus",
    () => {
        // 状态定义
        const mainMenu = ref<MainMenu[] | null>([]);
        const menuActive = ref(false);
        const childMenuShow = ref(true);
        const routers = ref<any[]>([]);
        const hasAddRoutes = ref(false);
        const isLicensed = ref(true);

        function setMenus(data: any) {
            mainMenu.value = data;
        }

        function setRouters(routes: any[]) {
            localStorage.setItem("routers", JSON.stringify(routes));
            routers.value = routes;
        }

        function setLicensed(value: boolean) {
            isLicensed.value = value;
        }
        function setHasAddRoutes(value: boolean) {
            hasAddRoutes.value = value;
        }

        function clearMenus() {
            mainMenu.value = [];
            hasAddRoutes.value = false;
        }

        return {
            mainMenu,
            menuActive,
            childMenuShow,
            routers,
            hasAddRoutes,
            isLicensed,
            clearMenus,
            setHasAddRoutes,
            setMenus,
            setRouters,
            setLicensed
        };
    },
    {
        persist: {
            enabled: true,
            strategies: [
                // { storage: sessionStorage, paths: [] }, //用sessionStorage存储
                { storage: localStorage, paths: ["mainMenu"] } // 用 localstorage存储（长效）
            ]
        }
    }
);
