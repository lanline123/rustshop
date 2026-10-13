import { ref, watchEffect, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import { getCollectProduct, delCollectProduct, updateCollectProduct } from "@/api/product/product";

export interface UseProductCollectReturn {
    /** 当前收藏状态 */
    isCollect: Ref<boolean>;
    /** 切换收藏 / 取消收藏 */
    toggleCollect: () => Promise<void>;
    /** 主动刷新收藏状态 */
    fetchStatus: () => Promise<void>;
}

/**
 * 商品收藏逻辑 Composable
 *
 * @description 管理商品收藏状态的查询与切换，随 productId 变化自动刷新状态
 * @param productId 商品 ID（字符串透传，避免雪花 id 精度丢失）
 */
export function useProductCollect(productId: Ref<string>): UseProductCollectReturn {
    const { t } = useI18n();
    const isCollect = ref(false);

    const fetchStatus = async (): Promise<void> => {
        if (!productId.value) return;
        try {
            const result = await getCollectProduct({ id: productId.value });
            isCollect.value = Boolean(result);
        } catch (error: any) {
            // 未登录时不提示，静默置为未收藏
            if (error?.code === 401) {
                isCollect.value = false;
                return;
            }
            uni.showToast({ title: error?.message, icon: "none" });
        }
    };

    const toggleCollect = async (): Promise<void> => {
        try {
            if (isCollect.value) {
                await delCollectProduct({ id: productId.value });
            } else {
                await updateCollectProduct({ productId: productId.value });
            }
            uni.showToast({
                title: isCollect.value ? t("取消收藏成功") : t("收藏成功"),
                icon: "none"
            });
            await fetchStatus();
        } catch (error: any) {
            uni.showToast({ title: error?.message, icon: "none" });
        }
    };

    watchEffect(() => {
        if (productId.value) fetchStatus();
    });

    return { isCollect, toggleCollect, fetchStatus };
}

export default useProductCollect;
