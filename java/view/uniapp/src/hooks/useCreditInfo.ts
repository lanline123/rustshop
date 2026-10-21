import { computed, ref, watch } from "vue";
import { useUserStore } from "@/store/user";
import { getUserCreditDetail } from "@/api/user/user";

/**
 * 账期（授信）信息的共享读取入口。
 *
 * 会员中心有两个账期卡入口：装修版外壳 fixedFunctionalArea 要先判断整块该不该渲染，
 * 卡片 termPayCard 自己也要判断显示与否。两处各算一份判断会让外壳把卡片挡住，
 * 因此判断与补偿请求都收敛到这里。state 放模块作用域，多个组件共用一份、全程只请求一次。
 */
const creditDetail = ref<any>(null);
let cachedToken: unknown;
let pendingRequest: Promise<unknown> | null = null;

export const useCreditInfo = () => {
    const userStore = useUserStore();

    // 仅登录态真正变化时丢弃缓存，否则后挂载的组件会把已取回的数据清掉
    const resetOnTokenChange = (token: unknown) => {
        if (token === cachedToken) return;
        cachedToken = token;
        creditDetail.value = null;
        pendingRequest = null;
    };

    // 登录后用户详情可能先于授信信息完成刷新，使用账期详情接口补充确认授信客户身份。
    // 非授信客户接口会返回业务错误，静默忽略，避免影响个人中心其它内容。
    const loadCreditDetail = () => {
        if (!userStore.token || userStore.userInfo?.creditPaymentInfo) return pendingRequest;
        if (pendingRequest) return pendingRequest;
        pendingRequest = getUserCreditDetail()
            .then((res: any) => {
                creditDetail.value = res;
            })
            .catch(() => {
                creditDetail.value = null;
            })
            .finally(() => {
                pendingRequest = null;
            });
        return pendingRequest;
    };

    watch(
        () => userStore.token,
        (token) => {
            resetOnTokenChange(token);
            loadCreditDetail();
        },
        { immediate: true }
    );

    const creditInfo = computed(() => userStore.userInfo?.creditPaymentInfo || creditDetail.value);

    /** 账期卡是否展示：装修版外壳与卡片自身共用此判断 */
    const showTermPay = computed(() => Boolean(creditInfo.value && Object.keys(creditInfo.value).length > 0));

    return {
        creditInfo,
        showTermPay
    };
};
