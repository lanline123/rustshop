/**
 * @file 用户状态管理Store
 * @description 管理后台用户相关的全局状态，包括：
 *   - 用户信息（管理员信息）
 *   - 店铺信息（店铺管理员专用）
 *   - 供应商信息（供应商管理员专用）
 *   - 访问令牌
 *   - 消息数量
 * @module store/user
 */
import { defineStore } from "pinia";
import router from "@/router";
import { useMenusStore } from "@/store/menu";
import { getMineInfo, getShopInfo, LogOut } from "@/api/authority/accountEditing";
import type { UserInfo } from "@/types/authority/accountEditing.d";
import { loginOut } from "@/utils/storage";
import { vendorSetting } from "@/api/vendor/setting";
import { version } from "../../package.json";

/**
 * 用户Store状态接口
 * @interface State
 */
interface State {
    /** 用户基本信息 */
    userInfo: UserInfo;
    /** 店铺信息（店铺/POS管理员使用） */
    shopInfo: any;
    /** 供应商信息（供应商管理员使用） */
    vendorInfo: any;
    /** 未读消息数量 */
    messageCount: number;
    /** 访问令牌 */
    accessToken: string;
}

export const useUserStore = defineStore("user", {
    state: (): State => {
        return {
            userInfo: {
                userId: 0,
                adminId: 0,
                avatar: "",
                username: "",
                authList: [],
                shopId: "",
                parentId: "",
                suppliersId: "",
                lastTime: ""
            },
            shopInfo: {},
            vendorInfo: {},
            messageCount: 0,
            accessToken: ""
        };
    },
    getters: {
        /**
         * 获取访问令牌
         * @returns {string} 访问令牌
         */
        getAccessToken(state) {
            return state.accessToken;
        }
    },
    actions: {
        /**
         * 更新用户信息
         * @description 根据管理员类型获取对应的用户/店铺/供应商信息
         */
        updateUserInfo() {
            try {
                const result = getMineInfo();
                result.then((result) => {
                    this.userInfo = result as UserInfo;
                    localStorage.setItem("user", JSON.stringify({ userInfo: result }));
                });
                // 店铺/门店管理员需要加载店铺信息
                if (localStorage.getItem("adminType") == "shop" || localStorage.getItem("adminType") == "store") {
                    const result2 = getShopInfo();
                    result2.then((result) => {
                        this.shopInfo = result as any;
                        localStorage.setItem("shopInfo", JSON.stringify(result));
                        localStorage.setItem("shopType", result.shopType);
                    });
                }
                // 供应商管理员需要加载供应商信息
                if (localStorage.getItem("adminType") == "vendor") {
                    const result3 = vendorSetting();
                    result3.then((result) => {
                        this.vendorInfo = result as any;
                        localStorage.setItem("vendorInfo", JSON.stringify(result));
                    });
                }
            } catch (error) {
            }
        },
        /**
         * 用户登出
         * @description 调用登出接口并清除本地状态
         * @param {boolean} jump - 是否跳转（默认true）
         */
        async logout(jump = true) {
            try {
                await LogOut();
            } catch (error) {
            } finally {
                // 不论后端是否响应成功都要做退出操作
                loginOut();
                // 避免在版本升级场景中被 versionManager 触发 logout 后立刻 reload，导致 version 尚未写入而无限循环刷新
                try {
                    localStorage.setItem("version", version);
                } catch (e) {
                    // ignore
                }
                const menus = useMenusStore();
                menus.clearMenus();
                window.location.reload();
            }
        },
        /**
         * 设置用户信息
         * @param {any} data - 用户信息数据
         */
        setUserInfo(data: any) {
            this.userInfo = data;
        },
        /**
         * 设置店铺信息
         * @param {any} data - 店铺信息数据
         */
        setShopInfo(data: any) {
            this.shopInfo = data;
        },
        /**
         * 设置供应商信息
         * @param {any} data - 供应商信息数据
         */
        setVendorInfo(data: any) {
            this.vendorInfo = data;
        },
        /**
         * 清除缓存
         * @description 清除菜单栏缓存，刷新页面后会自动更新
         */
        clearCache() {
            const menus = useMenusStore();
            menus.mainMenu = null;
        },
        /**
         * 设置访问令牌
         * @param {string} token - 访问令牌
         */
        setAccessToken(token: string) {
            this.accessToken = token;
        }
    },
    persist: {
        enabled: true,
        strategies: [
            { storage: sessionStorage, paths: ["details", "messageCount"] },
            { storage: localStorage, paths: ["accessToken", "userInfo", "shopInfo", "vendorInfo"] }
        ]
    }
});
