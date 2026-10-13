import { defineStore } from "pinia";
import { ref, reactive } from "vue";
import { useConfigStore } from "@/store/config";
import { redirect } from "@/utils";
import { getUser } from "@/api/user/user";
import { userLogout } from "@/api/login/login";
import { getServiceConfig } from "@/api/common";
import { useWholesaleStore } from "@/store/wholesale";
import { getOrderNum } from "@/api/user/order";
import type { OrderNumItem } from "@/types/user/order";

/** 客服配置接口 */
interface ServiceConfig {
    url: string;
    openType: number;
    show: number;
    serviceType: number;
    corpId: string;
}

/**
 * 用户状态管理Store
 * 管理用户登录态、用户信息、认证方式及客服配置
 */
export const useUserStore = defineStore("user", () => {
    /** 用户token */
    const token = ref<string>(uni.getStorageSync("token") || "");
    /** 用户信息 */
    const userInfo = ref<AnyObject>(uni.getStorageSync("userInfo") || {});
    /** 认证方式（wechatLogin等） */
    const authType = ref<string>("");
    /** 客服配置 */
    const serviceConfig = reactive<ServiceConfig>({
        url: "",
        openType: 0,
        show: 0,
        serviceType: 0,
        corpId: ""
    });
    /** 订单各状态数量统计（用于会员中心角标） */
    const orderNum = ref<OrderNumItem>({} as OrderNumItem);
    let orderNumPromise: Promise<OrderNumItem> | null = null;

    /**
     * @description 用户退出登录
     */
    async function logout() {
        try {
            await userLogout();
        } catch (error) {
            console.error(error);
        } finally {
            // 无论接口是否成功，本地登录态都必须清掉，否则分享门控会误判仍已登录
            const configStore = useConfigStore();
            clear();
            authType.value = "";
            // #ifdef MP-WEIXIN
            redirect({
                url: "/pages/index/index"
            });
            // #endif
            // #ifndef MP-WEIXIN
            if (configStore.XClientType === "wechat") {
                redirect({
                    url: "/pages/member/index"
                });
            } else {
                uni.reLaunch({
                    url: "/pages/login/index"
                });
            }
            // #endif
        }
    }

    /**
     * @description 清除用户登录态
     */
    function clear() {
        token.value = "";
        uni.removeStorageSync("token");
        uni.removeStorageSync("userInfo");
        userInfo.value = {};
        // 登出/切号清空批发身份，避免下个用户误用上个用户的批发态
        useWholesaleStore().reset();
        orderNum.value = {} as OrderNumItem;
        orderNumPromise = null;
    }

    /**
     * @description 设置用户信息
     * @param data 用户信息对象
     */
    function setUserInfo(data: any) {
        userInfo.value = data;
        uni.setStorageSync("userInfo", data);
    }

    /**
     * @description 设置用户token
     * @param tokenValue token字符串
     */
    function setToken(tokenValue: string) {
        token.value = tokenValue;
        uni.setStorageSync("token", tokenValue);
    }

    /**
     * @description 设置认证方式
     * @param authTypeValue 认证方式
     */
    function setAuthType(authTypeValue: string) {
        authType.value = authTypeValue;
    }

    /**
     * @description 获取用户详情并更新store
     */
    async function getUserInfo() {
        try {
            const result = await getUser();
            setUserInfo(result);
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * @description 获取客服配置并更新store
     */
    async function getServiceConfigData() {
        try {
            const result = await getServiceConfig();
            Object.assign(serviceConfig, result);
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * @description 拉取订单数量角标
     * @param force 是否强制刷新（忽略并发去重）
     * - 未登录时直接返回空对象
     * - 并发调用复用同一个请求，避免重复打接口
     */
    async function fetchOrderNum(force = false): Promise<OrderNumItem> {
        if (!token.value) {
            orderNum.value = {} as OrderNumItem;
            return orderNum.value;
        }
        if (orderNumPromise && !force) {
            return orderNumPromise;
        }
        orderNumPromise = (async () => {
            try {
                const result = (await getOrderNum()) as unknown as OrderNumItem;
                orderNum.value = result || ({} as OrderNumItem);
                return orderNum.value;
            } catch (error) {
                console.error(error);
                return orderNum.value;
            } finally {
                orderNumPromise = null;
            }
        })();
        return orderNumPromise;
    }

    return {
        token,
        userInfo,
        authType,
        serviceConfig,
        orderNum,
        logout,
        clear,
        setUserInfo,
        setToken,
        setAuthType,
        getUserInfo,
        getServiceConfigData,
        getOrderNum: fetchOrderNum
    };
});
