import { getWholesaleIdentity } from "@/api/wholesale/me";
import { useWholesaleStore } from "@/store/wholesale";
import { useUserStore } from "@/store/user";
import type { WholesaleIdentity } from "@/types/wholesale/identity";
import { createWholesaleIdentityLoader, isWholesaleIdentityEnabled } from "@/utils/wholesaleContract";

/**
 * 防并发：多处同时调用 load() 时，复用同一 promise
 */
type IdentityLoader = ReturnType<typeof createWholesaleIdentityLoader<WholesaleIdentity>>;
const identityLoaders = new WeakMap<object, IdentityLoader>();

/**
 * 批发身份 composable
 * @description
 * - 统一封装身份拉取 + store 缓存 + 登录态判断
 * - showWholesale 是「是否渲染批发能力」的唯一权威判定：globalEnabled && isWholesaler
 * - 未登录态不调用接口，直接走访客身份
 */
export const useWholesaleIdentity = () => {
    const store = useWholesaleStore();
    const userStore = useUserStore();

    let identityLoader = identityLoaders.get(store);
    if (!identityLoader) {
        identityLoader = createWholesaleIdentityLoader<WholesaleIdentity>({
            getToken: () => userStore.token ?? null,
            request: getWholesaleIdentity,
            apply: (identity) => store.set(identity),
            reset: () => store.reset()
        });
        identityLoaders.set(store, identityLoader);
    }

    const reset = () => identityLoader!.reset();

    /**
     * 拉取身份；带缓存（identityLoaded）+ 防并发
     * @param force 强制刷新（登录态变化、总开关疑似变更时使用）
     */
    const load = async (force = false): Promise<WholesaleIdentity> => {
        if (!userStore.isAuthenticated) {
            reset();
            return store.identity;
        }
        if (!force && store.identityLoaded) {
            return store.identity;
        }
        await identityLoader!.load(force);
        return store.identity;
    };

    /** 是否渲染批发能力（运行期权威判定） */
    const showWholesale = computed(() => isWholesaleIdentityEnabled(store.identity));

    return {
        identity: computed(() => store.identity),
        identityLoaded: computed(() => store.identityLoaded),
        showWholesale,
        load,
        reset
    };
};
