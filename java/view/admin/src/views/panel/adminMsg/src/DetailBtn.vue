<template>
    <div v-for="data in dialogConfigs">
        <DialogForm
            v-if="data.type == item.msgType && canOpenDetail(data.type) && showLegacyDialog(data.type)"
            :key="data.type"
            :params="{ act: 'detail', id: getParamId(data.idKey), ...getDialogExtraParams(data.type) }"
            :title="data.title"
            :showClose="data.showClose"
            :showOnOk="data.showOnOk"
            isDrawer
            :path="data.path"
            :width="data.width"
        >
            <span>{{ $t("查看详情") }}</span>
            <span class="iconfont-admin icon-login-youjiantou"></span>
        </DialogForm>
    </div>
    <DialogForm
        v-if="routeAction?.route === 'memberFeedback' && routeAction.id && canOpenRoute('memberFeedback')"
        :params="{ act: 'detail', id: routeAction.id }"
        :title="$t('回复会员留言')"
        :showClose="true"
        :showOnOk="true"
        isDrawer
        path="user/feedback/Info"
        width="560px"
    >
        <span>{{ $t("去处理") }}</span>
        <span class="iconfont-admin icon-login-youjiantou"></span>
    </DialogForm>
    <DialogForm
        v-if="routeAction?.route === 'productInquiry' && routeAction.id && canOpenRoute('productInquiry')"
        :params="{ act: 'detail', id: routeAction.id, closeOnSubmit: true }"
        :title="$t('回复询价')"
        :showOnOk="false"
        isDrawer
        path="product/enquiry/Info"
        width="600px"
    >
        <span>{{ $t("去处理") }}</span>
        <span class="iconfont-admin icon-login-youjiantou"></span>
    </DialogForm>
    <DialogForm
        v-if="
            routeAction &&
            routeAction.id &&
            routeInfoPath &&
            !['memberFeedback', 'productInquiry'].includes(routeAction.route) &&
            canOpenRoute(routeAction.route)
        "
        :params="{ act: 'detail', id: routeAction.id, ...routeAction.params }"
        :title="routeInfoTitle"
        :showOnOk="routeInfoShowOnOk"
        isDrawer
        :path="routeInfoPath"
        :width="routeInfoWidth"
    >
        <span>{{ $t("查看详情") }}</span>
        <span class="iconfont-admin icon-login-youjiantou"></span>
    </DialogForm>
    <DialogForm
        v-if="routeAction?.route === 'deposit' && routeAction.params?.ownerId && canOpenRoute('deposit')"
        :params="routeAction.params"
        :title="$t('保证金详情')"
        :showOnOk="false"
        isDrawer
        path="finance/deposit/shop/src/Detail"
        width="980px"
    >
        <span>{{ $t("查看详情") }}</span>
        <span class="iconfont-admin icon-login-youjiantou"></span>
    </DialogForm>
</template>

<script setup lang="ts">
import { DialogForm } from "@/components/dialog";
import { computed, reactive } from "vue";
import { useUserStore } from "@/store/user";
import { isB2b } from "@/utils/version";
import { getFeedbackId, resolveAdminMsgAction, type AdminMsgRoute } from "@/utils/adminMsgAction";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = defineProps({
    item: { type: Object, default: () => ({}) },
    orderSn: {
        type: String,
        default: ""
    }
});
const userStore = useUserStore();
const canModifyFeedback = computed(() => {
    const authList = userStore.userInfo?.authList || [];
    return authList.includes("all") || authList.includes("feedbackModifyManage");
});
interface DialogConfig {
    type: number;
    title: string;
    idKey: string;
    showClose?: boolean;
    showOnOk?: boolean;
    path?: string;
    width?: string;
}
// 定义每种消息类型的配置
const dialogConfigs = reactive<DialogConfig[]>([
    {
        type: 11,
        title: t("订单详情 {orderSn}", { orderSn: props.orderSn }),
        idKey: "relatedData.orderId",
        path: "order/order/Info",
        width: "880px",
        showClose: false,
        showOnOk: false
    },
    {
        type: 12,
        title: t("订单详情 {orderSn}", { orderSn: props.orderSn }),
        idKey: "relatedData.orderId",
        path: "order/order/Info",
        width: "880px",
        showClose: false,
        showOnOk: false
    },
    {
        type: 13,
        title: t("订单详情 {orderSn}", { orderSn: props.orderSn }),
        idKey: "relatedData.orderId",
        path: "order/order/Info",
        width: "880px",
        showClose: false,
        showOnOk: false
    },
    {
        type: 31,
        title: t("订单详情 {orderSn}", { orderSn: props.orderSn }),
        idKey: "relatedData.orderId",
        path: "order/order/Info",
        width: "880px",
        showClose: false,
        showOnOk: false
    },
    {
        type: 54,
        title: t("订单详情 {orderSn}", { orderSn: props.orderSn }),
        idKey: "relatedData.orderId",
        path: "order/order/Info",
        width: "880px",
        showClose: false,
        showOnOk: false
    },
    {
        type: 21,
        title: t("商品详情"),
        idKey: props.item?.relatedData?.vendorProductId ? "relatedData.vendorProductId" : "relatedData.productId",
        path: props.item?.relatedData?.vendorProductId ? "vendor/supplier/product/Info" : "product/product/Info",
        width: "800px"
    },
    {
        type: 22,
        title: t("商品详情"),
        idKey: props.item?.relatedData?.vendorProductId ? "relatedData.vendorProductId" : "relatedData.productId",
        path: props.item?.relatedData?.vendorProductId ? "vendor/supplier/product/Info" : "product/product/Info",
        width: "800px"
    },
    {
        type: 23,
        title: t("商品详情"),
        idKey: props.item?.relatedData?.vendorProductId ? "relatedData.vendorProductId" : "relatedData.productId",
        path: props.item?.relatedData?.vendorProductId ? "vendor/supplier/product/Info" : "product/product/Info",
        width: "800px"
    },
    {
        type: 24,
        title: t("商品详情"),
        idKey: props.item?.relatedData?.vendorProductId ? "relatedData.vendorProductId" : "relatedData.productId",
        path: props.item?.relatedData?.vendorProductId ? "vendor/supplier/product/Info" : "product/product/Info",
        width: "800px"
    },
    {
        type: 32,
        title: t("售后详情 {aftersalesSn}", { aftersalesSn: props.item.relatedData?.aftersalesSn }),
        idKey: "relatedData.aftersaleId",
        path: "order/aftersales/Info",
        width: "800px",
        showClose: false,
        showOnOk: false
    },
    { type: 33, title: t("查看申请"), idKey: "relatedData.withdrawApplyId", path: "finance/userWithdrawApply/Info", width: "600px" },
    { type: 34, title: t("编辑增票资质申请"), idKey: "relatedData.invoiceId", path: "finance/userInvoice/Info", width: "600px" },
    { type: 35, title: t("编辑发票申请"), idKey: "relatedData.orderInvoiceId", path: "finance/orderInvoice/Info", width: "700px" },
    { type: 41, title: t("申请详情"), idKey: "relatedData.merchantApplyId", path: "adminMerchant/apply/Info", width: "600px" },
    { type: 53, title: t("编辑会员留言"), idKey: "relatedData.id", path: "user/feedback/Info", width: "600px" }
]);

const legacyDialogTypes = new Set(dialogConfigs.map((item) => item.type));
const legacyDialogRoutes: Record<number, AdminMsgRoute> = {
    11: "order",
    12: "order",
    13: "order",
    21: "product",
    22: "product",
    23: "product",
    24: "product",
    31: "order",
    32: "aftersale",
    33: "withdrawal",
    34: "invoice",
    35: "invoice",
    53: "memberFeedback",
    54: "order"
};
const showLegacyDialog = (type: number) => {
    if (!legacyDialogTypes.has(type)) return false;
    if (type === 33 && props.item?.relatedData?.shopWithdrawLogId) return false;
    const explicitRoute = props.item?.relatedData?.route || props.item?.route;
    return !explicitRoute || explicitRoute === legacyDialogRoutes[type];
};
const routeAction = computed(() => {
    const msgType = Number(props.item?.msgType);
    if (legacyDialogTypes.has(msgType) && showLegacyDialog(msgType)) return null;
    return resolveAdminMsgAction(props.item);
});

const canOpenDetail = (type: number) => type !== 53 || canModifyFeedback.value;

const getDialogExtraParams = (type: number) => {
    if ([21, 22, 23, 24].includes(type) && props.item?.relatedData?.shopId) {
        return { shopId: props.item.relatedData.shopId };
    }
    return {};
};

const canOpenRoute = (route: AdminMsgRoute) => {
    const authList = userStore.userInfo?.authList || [];
    const has = (sn: string) => authList.includes("all") || authList.includes(sn);
    if (route === "memberFeedback") return has("feedbackManage");
    if (route === "productInquiry") return isB2b() && has("enquiryManage");
    return true;
};

const routeInfoPath = computed(() => {
    const action = routeAction.value;
    if (!action) return "";
    if (action.route === "order") return "order/order/Info";
    if (action.route === "aftersale") return "order/aftersales/Info";
    if (action.route === "refundApply") return "finance/refundApply/Info";
    if (action.route === "product") return props.item?.relatedData?.vendorProductId ? "vendor/supplier/product/Info" : "product/product/Info";
    if (action.route === "withdrawal") return "finance/userWithdrawApply/Info";
    if (action.route === "shopWithdrawal") return "finance/shopFunds/shopAccountRaply/Info";
    if (action.route === "invoice") return props.item?.relatedData?.invoiceId ? "finance/userInvoice/Info" : "finance/orderInvoice/Info";
    return "";
});

const routeInfoTitle = computed(() => {
    const action = routeAction.value;
    if (!action) return "";
    if (action.route === "order") return t("订单详情 {orderSn}", { orderSn: props.orderSn });
    if (action.route === "aftersale") return t("售后详情");
    if (action.route === "refundApply") return t("编辑退款申请");
    if (action.route === "product") return t("商品详情");
    if (action.route === "withdrawal") return t("查看申请");
    if (action.route === "shopWithdrawal") return t("提现详情");
    if (action.route === "invoice") return t("发票详情");
    return t("查看详情");
});

const routeInfoShowOnOk = computed(() => {
    const action = routeAction.value;
    return !!action && !["order", "aftersale", "refundApply"].includes(action.route);
});

const routeInfoWidth = computed(() => {
    const action = routeAction.value;
    return action?.route === "shopWithdrawal" ? "500" : "800px";
});

// 获取对应的参数ID
const getParamId = (key: any) => {
    if (props.item?.msgType === 53 && key === "relatedData.id") {
        return getFeedbackId(props.item.relatedData);
    }
    if (key.includes(".")) {
        const keys = key.split(".");
        return props.item[keys[0]] ? props.item[keys[0]][keys[1]] : null;
    }
    return props.item[key];
};
</script>
