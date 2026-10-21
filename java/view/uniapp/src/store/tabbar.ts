import { t } from "@/utils/i18n";
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getMobileNav } from "@/api/tabbar";
import { useConfigStore } from "@/store/config";
import { urlFormat } from "@/utils/format";
import ico1 from "@/static/images/common/ico_1.png";
import ico2 from "@/static/images/common/ico_2.png";
import ico3 from "@/static/images/common/ico_3.png";
import ico4 from "@/static/images/common/ico_4.png";
import ico1h from "@/static/images/common/ico_1h.png";
import ico2h from "@/static/images/common/ico_2h.png";
import ico3h from "@/static/images/common/ico_3h.png";
import ico4h from "@/static/images/common/ico_4h.png";

export const useTabbarStore = defineStore("tabbar", () => {
    const tabbarListDefault = ref<any[]>([]);
    const tabbarEnableAnimation = ref(false);
    const tabbarHeightNum = ref(60);
    const currRoute = ref("");
    // 后台导航装修：背景与配色配置
    const tabbarBackgroundColorType = ref(1); // 1=系统色 2=自定义颜色
    const tabbarBackgroundColor = ref("");
    const tabbarCornerStyle = ref(1); // 1=直角 2=圆角 3=大圆角
    const tabbarIconColorType = ref(1); // 1=系统配色 2=自定义配置
    const tabbarSelectedTextColor = ref("");
    const tabbarUnselectedTextColor = ref("");
    const configStore = useConfigStore();
    const tabbarList = computed(() => {
        if (configStore.buyerShowEnabled !== 1) {
            return tabbarListDefault.value.filter((item) => {
                return item?.link?.link !== "/pages/buyerShow/index" && item?.link?.link !== "buyerShow";
            });
        } else {
            return tabbarListDefault.value;
        }
    });

    const tabbarHeight = computed(() => {
        const configStore = useConfigStore();
        return tabbarHeightNum.value + configStore.safeBottom + "px";
    });

    // 自定义背景色，系统色时返回空让组件走默认
    const tabbarBackground = computed(() => {
        return tabbarBackgroundColorType.value === 2 && tabbarBackgroundColor.value ? tabbarBackgroundColor.value : "";
    });

    // 顶部圆角：直角/圆角/大圆角
    const tabbarRadius = computed(() => {
        switch (tabbarCornerStyle.value) {
            case 2:
                return "16rpx 16rpx 0 0";
            case 3:
                return "32rpx 32rpx 0 0";
            default:
                return "0";
        }
    });

    const formatTabarList = computed(() => {
        return tabbarList.value.map((item) => {
            return urlFormat(item.link).split("?")[0];
        });
    });

    const currentActiveValue = computed(() => {
        const index = formatTabarList.value.findIndex((item) => {
            return `/${currRoute.value}` === item;
        });
        return index;
    });

    async function getTabbarList() {
        try {
            const result = await getMobileNav({ isNew: 1 });
            // request 已解包，result 即响应体：
            // 1) 新结构：result 直接包含 navList / enableAnimation
            // 2) 旧结构：navList 位于 result.data（对象或 JSON 字符串）中
            const root = result && typeof result === "object" ? (result as Record<string, any>) : {};
            let navSource: Record<string, any> = root;
            if (!Array.isArray(root.navList) && root.data) {
                navSource = typeof root.data === "string" ? JSON.parse(root.data) : root.data;
            }
            const navList = Array.isArray(navSource?.navList) ? navSource.navList : [];
            // 开启缩放动画效果开关（兼容 true/false 或 1/0）
            tabbarEnableAnimation.value = navSource?.enableAnimation === true || navSource?.enableAnimation === 1 || navSource?.enableAnimation === "1";
            // 背景与配色配置（老数据缺字段时用系统默认）
            tabbarBackgroundColorType.value = Number(navSource?.backgroundColorType) || 1;
            tabbarBackgroundColor.value = navSource?.backgroundColor || "";
            tabbarCornerStyle.value = Number(navSource?.cornerStyle) || 1;
            tabbarIconColorType.value = Number(navSource?.iconColorType) || 1;
            tabbarSelectedTextColor.value = navSource?.selectedTextColor || "";
            tabbarUnselectedTextColor.value = navSource?.unselectedTextColor || "";
            tabbarListDefault.value = navList.map((item: any) => {
                return {
                    link: item.picLink,
                    image: item.picThumb,
                    activeImage: item.picActiveThumb,
                    text: item.picTitle
                };
            });
        } catch (error) {
            tabbarEnableAnimation.value = false;
            tabbarBackgroundColorType.value = 1;
            tabbarBackgroundColor.value = "";
            tabbarCornerStyle.value = 1;
            tabbarIconColorType.value = 1;
            tabbarSelectedTextColor.value = "";
            tabbarUnselectedTextColor.value = "";
            tabbarListDefault.value = [
                {
                    link: "/pages/index/index",
                    image: ico1,
                    activeImage: ico1h,
                    text: t("首页")
                },
                {
                    link: "/pages/productCate/index",
                    image: ico2,
                    activeImage: ico2h,
                    text: t("分类")
                },
                {
                    link: "/pages/cart/index",
                    image: ico3,
                    activeImage: ico3h,
                    text: t("购物车")
                },
                {
                    link: "/pages/member/index",
                    image: ico4,
                    activeImage: ico4h,
                    text: t("我的")
                }
            ];
            console.error(error);
        }
    }

    return {
        tabbarListDefault,
        tabbarEnableAnimation,
        tabbarList,
        tabbarHeightNum,
        tabbarHeight,
        tabbarBackgroundColorType,
        tabbarBackgroundColor,
        tabbarCornerStyle,
        tabbarIconColorType,
        tabbarSelectedTextColor,
        tabbarUnselectedTextColor,
        tabbarBackground,
        tabbarRadius,
        formatTabarList,
        currentActiveValue,
        currRoute,
        getTabbarList
    };
});
