import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getWholesaleIdentity } from "@/api/wholesale/me";
import type { WholesaleIdentity } from "@/types/wholesale/identity";

/**
 * @description 批发商身份 store
 * - 持有 GET /api/wholesale/me 返回的 identity
 * - 提供 loadIdentity（带缓存 + 防并发）/ showWholesale（总开关 + 身份）/ reset
 * - 与 useUserStore 独立，避免污染零售用户态
 */

const defaultIdentity = (): WholesaleIdentity => ({
    isWholesaler: false,
    gradeId: 0,
    gradeName: "",
    globalEnabled: false
});

/** 并发请求复用，避免短时间多处触发重复调用 */
let pending: Promise<WholesaleIdentity> | null = null;

export const useWholesaleStore = defineStore("wholesale", () => {
    const identity = ref<WholesaleIdentity>(defaultIdentity());
    const identityLoaded = ref(false);

    /**
     * 拉取批发身份
     * - 未登录态：后端会回 isWholesaler=false，不报错
     * - 已登录：拿真实身份
     * @param force 强制刷新，忽略缓存
     */
    const loadIdentity = async (force = false): Promise<WholesaleIdentity> => {
        if (!force && identityLoaded.value) return identity.value;
        if (pending) return pending;
        pending = getWholesaleIdentity()
            .then((res) => {
                identity.value = { ...defaultIdentity(), ...res };
                identityLoaded.value = true;
                return identity.value;
            })
            .catch((err) => {
                console.error("[wholesale] loadIdentity failed:", err);
                identity.value = defaultIdentity();
                identityLoaded.value = true;
                return identity.value;
            })
            .finally(() => {
                pending = null;
            });
        return pending;
    };

    /** 渲染批发能力的充要条件：总开关 + 身份 */
    const showWholesale = computed(() => identity.value.globalEnabled && identity.value.isWholesaler);

    /** 登出 / 切号清空状态 */
    const reset = () => {
        identity.value = defaultIdentity();
        identityLoaded.value = false;
        pending = null;
    };

    return {
        identity,
        identityLoaded,
        loadIdentity,
        showWholesale,
        reset
    };
});
