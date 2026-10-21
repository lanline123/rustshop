import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import pinia from "@/store/index";
import { clearJsonLd } from "@/utils/seo";
const whiteList = [
    "/pages/login/index",
    "/pages/login/mallAgreement",
    "/pages/register/index",
    "/pages/register/agreement",
    "/pages/index/index",
    "/pages/search/index",
    "/pages/search/result",
    "/pages/productCate/index",
    "/pages/cart/index",
    "/pages/member/index",
    "/pages/product/index",
    "/pages/shop/index",
    "/pages/category/index",
    "/pages/im/index",
    "/pages/user/bindMobilePhone/index",
    "/pages/salesman/salesmanCard",
    "/pages/reset/index",
    "/pages/customPage/index",
    "/pages/store/index",
    "/pages/storeList/index",
    "/pages/region/index",
    "/pages/buyerShow/index",
    "/pages/buyerShow/addBuyerShow",
    "/pages/buyerShow/buyerShowDetail",
];

function hasPermission(url: string): boolean {
    const userStore = useUserStore(pinia);
    let currentUrl = url;
    if (url.includes("?")) {
        currentUrl = url.split("?")[0];
    }
    // 在白名单中或有token，直接跳转
    if (whiteList.indexOf(currentUrl) !== -1 || uni.getStorageSync("token") || userStore.token) {
        return true;
    }
    return false;
}

const jumpInterceptorList = ["navigateTo", "redirectTo", "reLaunch", "switchTab"];

//应用权限校验至拦截器
jumpInterceptorList.forEach((item) => {
    uni.addInterceptor(item, {
        //拦截前触发
        invoke(args) {
            // #ifdef H5
            clearJsonLd();
            // #endif
            if (!hasPermission(args.url)) {
                const userStore = useUserStore(pinia);
                const configStore = useConfigStore(pinia);
                if (
                    configStore.openWechatOauth === 1 &&
                    configStore.openWechatRegister === 1 &&
                    (configStore.XClientType === "miniProgram" || configStore.XClientType === "wechat")
                ) {
                    userStore.setAuthType("wechatLogin");
                } else {
                    uni.setStorageSync("URL", args.url);
                    uni.navigateTo({
                        url: "/pages/login/index"
                    });
                }
                return false;
            }
            return true;
        },
        fail(e) {
            console.error(`为 ${item} 拦截器添加权限校验失败：`, e);
        }
    });
});
