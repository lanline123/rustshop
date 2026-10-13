import type { WholesaleIdentity } from "@/types/wholesale/identity";

/**
 * 默认访客身份；登出、总开关关、未加载时使用
 */
const defaultIdentity = (): WholesaleIdentity => ({
    isWholesaler: false,
    gradeId: null,
    gradeName: "",
    globalEnabled: false
});

/**
 * 批发身份与总开关 Store
 * @description
 * - 与 useUserStore 解耦：批发态独立放此 store，不污染零售用户态
 * - 由 hooks/useWholesaleIdentity.ts 统一负责拉取与缓存
 */
export const useWholesaleStore = defineStore("wholesale", () => {
    /** 当前批发身份（含总开关） */
    const identity = ref<WholesaleIdentity>(defaultIdentity());
    /** 是否已加载过身份（带防并发） */
    const identityLoaded = ref(false);
    /** 身份数据代次；reset/set 均递增，供详情和列表立即失效旧账号数据并在身份就绪后重拉。 */
    const identityRevision = ref(0);

    /** 写入身份（成功响应后调用） */
    const set = (next: WholesaleIdentity) => {
        identity.value = { ...defaultIdentity(), ...next };
        identityLoaded.value = true;
        identityRevision.value++;
    };

    /** 清空身份（登出、切号、运行期发现总开关关时调用） */
    const reset = () => {
        identity.value = defaultIdentity();
        identityLoaded.value = false;
        identityRevision.value++;
    };

    return {
        identity,
        identityLoaded,
        identityRevision,
        set,
        reset
    };
});
