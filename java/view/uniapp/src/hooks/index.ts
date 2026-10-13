import { useConfigStore } from "@/store/config";
import { ref, toRefs } from "vue";
import { onPageScroll } from "@dcloudio/uni-app";

/**
 * @description 页面滚动位置hook
 * @param handleScroll 自定义滚动处理函数
 * @returns scrollTop 当前滚动位置
 */
export function useScrollTop(handleScroll?: any) {
    const scrollTop = ref(0);

    if (handleScroll && typeof handleScroll === "function") {
        handleScroll((e: any) => {
            scrollTop.value = e.scrollTop;
        });
    } else {
        onPageScroll((e) => {
            scrollTop.value = e.scrollTop;
        });
    }
    return {
        scrollTop
    };
}

/**
 * @description 获取安全区域边距hook
 * @returns saveTop 顶部安全距离, safeBottom 底部安全距离
 */
export function useSafeAreaInsets() {
    const configStore = useConfigStore();
    const { top, bottom } = toRefs(configStore.safeAreaInsets);
    return {
        saveTop: top,
        safeBottom: bottom
    };
}

/**
 * @description 获取小程序胶囊按钮信息hook
 * @returns 胶囊按钮位置及尺寸
 */
export function useMenuButtonInfo() {
    const configStore = useConfigStore();
    const { left, right, top, height } = toRefs(configStore.menuButtonInfo);
    return {
        menuLeft: left,
        menuRight: right,
        menuTop: top,
        menuHeight: height
    };
}

/**
 * @description 计算顶部导航区域高度hook
 * @param contentHeight 内容高度
 * @returns 计算后的高度信息
 */
export function useSaveTopBoxHeight(contentHeight = 0) {
    const contentHeightRef = ref(contentHeight);
    const contentWidthRef = ref("100%");
    const height = ref(0);
    const { saveTop } = useSafeAreaInsets();
    const { menuTop, menuHeight, menuLeft } = useMenuButtonInfo();
    // #ifdef APP-PLUS || H5
    height.value = saveTop.value + contentHeightRef.value;
    // #endif
    // #ifndef H5 || APP-PLUS || MP-ALIPAY
    contentWidthRef.value = menuLeft.value + "px";
    contentHeightRef.value = (menuTop.value - saveTop.value) * 2 + menuHeight.value;
    height.value = contentHeightRef.value + saveTop.value;
    //  #endif
    return {
        height,
        contentHeight: contentHeightRef,
        contentWidth: contentWidthRef,
        statusBarHeight: saveTop
    };
}

export { useResetReactive, useResettableRef } from "./useResetState";
export { default as useList } from "./useList";
export { useProductStyle } from "./useProductStyle";
export { default as useStorageData } from "./useStorageData";
export { useCreditInfo } from "./useCreditInfo";
