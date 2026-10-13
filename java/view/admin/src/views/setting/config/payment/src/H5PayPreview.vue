<template>
    <div class="h5-preview">
        <div class="phone">
            <div class="phone-header">
                <div class="back">‹</div>
                <div class="title">{{ $t('订单支付') }}</div>
                <div class="spacer"></div>
            </div>

            <div class="card amount-card">
                <div class="amount">￥76.30</div>
                <div class="order-no">{{ $t('订单号：20251224110753258295') }}</div>
            </div>

            <div v-if="availableItems.length" class="card methods-card">
                <div v-for="item in availableItems" :key="item.key" class="method-item">
                    <div class="left">
                        <div class="icon">
                            <img
                                v-if="payIconUrlByKey[item.iconKey] && !brokenIconKeys.has(item.key)"
                                class="icon-img"
                                :src="payIconUrlByKey[item.iconKey]"
                                :alt="item.name"
                                @error="onIconError(item.key)"
                            />
                            <div v-else class="icon-fallback" :class="item.iconKey">{{ item.iconText }}</div>
                        </div>
                        <div class="name">{{ item.name }}</div>
                    </div>
                    <div class="right">
                        <div v-if="effectiveSelected === item.key" class="checked"></div>
                        <div v-else class="unchecked"></div>
                    </div>
                </div>
            </div>

            <div class="card other-card">
                <div class="other-title">{{ $t('其它信息') }}</div>
                <div class="other-row">
                    <span class="k">{{ $t('支付方式：') }}</span>
                    <span class="v">{{ $t('在线支付') }}</span>
                </div>
                <div class="other-row">
                    <span class="k">{{ $t('配送方式：') }}</span>
                    <span class="v">{{ $t('普通快递') }}</span>
                </div>
                <div class="other-row">
                    <span class="k">{{ $t('下单时间：') }}</span>
                    <span class="v">2025-12-24 11:07:50</span>
                </div>
            </div>

            <div class="pay-bar">
                <div class="pay-btn">{{ $t('立即支付') }}</div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import {
    getAliPaySettings,
    getBasicSettings,
    getPayPalSettings,
    getWechatSettings,
    getYunPaySettings
} from "@/api/setting/payment";
import { getApplePaySettings } from "@/api/setting/payment";
import { getHuifuSettings } from "@/api/setting/payment";
import { getLakalaSettings } from "@/api/setting/payment";
import { getYeepaySettings } from "@/api/setting/payment";
import { isOverseas } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type PayKey =
    | "wechat"
    | "alipay"
    | "paypal"
    | "yunpay"
    | "huifuWechat"
    | "huifuAlipay"
    | "lakalaWechat"
    | "lakalaAlipay"
    | "yeepayWechat"
    | "yeepayAlipay"
    | "applepay"
    ;

const payIconModules = import.meta.glob("../../../../../assets/icons/payment/pay_*.png", { eager: true, as: "url" });
const payIconUrlByKey = Object.fromEntries(
    Object.entries(payIconModules)
        .map(([path, url]) => {
            const match = path.match(/pay_([a-z0-9_]+)\.png$/i);
            if (!match) return null;
            return [match[1], url];
        })
        .filter(Boolean) as Array<[string, string]>
) as Partial<Record<PayKey, string>>;

const brokenIconKeys = ref<Set<PayKey>>(new Set());
const onIconError = (key: PayKey) => {
    brokenIconKeys.value.add(key);
};

const props = defineProps<{
    selected?: PayKey;
    hiddenKeys?: readonly PayKey[];
    /** 可选：父组件传入的临时支付顺序（用于预览实时跟随拖拽排序） */
    paymentOrder?: readonly PayKey[];
}>();

const enabledKeysLoaded = ref(false);
const enabledKeys = ref<Set<PayKey>>(new Set());

const normalizeEnabled = (val: unknown) => Number(val) === 1;

const paymentOrderLoaded = ref(false);
const paymentOrder = ref<PayKey[]>([]);

const normalizePaymentOrderCode = (code: string): PayKey | undefined => {
    const normalized = String(code ?? "").trim().toLowerCase();
    if (!normalized) return undefined;
    // 后端基础设置里：wechat,alipax,paypal,stripe（alipax 对应支付宝）
    if (normalized === "alipax" || normalized === "alipay") return "alipay";
    if (normalized === "wechat") return "wechat";
    if (normalized === "paypal") return "paypal";
    if (normalized === "yunpay") return "yunpay";
    if (normalized === "huifuwechat") return "huifuWechat";
    if (normalized === "huifualipay") return "huifuAlipay";
    if (normalized === "lakalawechat") return "lakalaWechat";
    if (normalized === "lakalaalipay") return "lakalaAlipay";
    if (normalized === "yeepaywechat") return "yeepayWechat";
    if (normalized === "yeepayalipay") return "yeepayAlipay";
    if (normalized === "applepay") return "applepay";
    return undefined;
};

const loadPaymentOrder = async () => {
    try {
        const basic = await getBasicSettings().catch(() => null);
        const raw = String((basic as any)?.paymentOrder ?? "").trim();
        const parts = raw
            .split(",")
            .map((x) => x.trim())
            .filter(Boolean);

        const uniq: PayKey[] = [];
        for (const p of parts) {
            const key = normalizePaymentOrderCode(p);
            if (!key) continue;
            if (uniq.includes(key)) continue;
            uniq.push(key);
        }
        paymentOrder.value = uniq;
    } finally {
        paymentOrderLoaded.value = true;
    }
};

const loadEnabledKeys = async () => {
    try {
        const [wechat, alipay, paypal, yunpay] = await Promise.all([
            getWechatSettings().catch(() => null),
            getAliPaySettings().catch(() => null),
            getPayPalSettings().catch(() => null),
            getYunPaySettings().catch(() => null)
        ]);

        const next = new Set<PayKey>();
        if (wechat && normalizeEnabled((wechat as any).useWechat)) next.add("wechat");
        if (alipay && normalizeEnabled((alipay as any).useAlipay)) next.add("alipay");
        if (paypal && normalizeEnabled((paypal as any).usePaypal)) next.add("paypal");
        if (yunpay && normalizeEnabled((yunpay as any).useYunpay)) next.add("yunpay");


        {
            const huifu = await getHuifuSettings().catch(() => null);
            if (huifu && normalizeEnabled((huifu as any).useHuifu)) {
                // 汇付开启后，收银台展示为独立的微信/支付宝两项（payCode 与原生不同）
                next.add("huifuWechat");
                next.add("huifuAlipay");
            }
        }
        {
            const lakala = await getLakalaSettings().catch(() => null);
            if (lakala && normalizeEnabled((lakala as any).useLakala)) {
                // 拉卡拉开启后，收银台展示为独立的微信/支付宝两项
                next.add("lakalaWechat");
                next.add("lakalaAlipay");
            }
        }
        {
            const yeepay = await getYeepaySettings().catch(() => null);
            if (yeepay && normalizeEnabled((yeepay as any).useYeepay)) {
                // 易宝开启后，收银台展示为独立的微信/支付宝两项
                next.add("yeepayWechat");
                next.add("yeepayAlipay");
            }
        }
        {
            const applepay = await getApplePaySettings().catch(() => null);
            if (applepay && normalizeEnabled((applepay as any).useApplePay)) next.add("applepay");
        }

        enabledKeys.value = next;
    } finally {
        enabledKeysLoaded.value = true;
    }
};

onMounted(() => {
    loadEnabledKeys();
    loadPaymentOrder();
});

const items = computed(() => {
    const hidden = new Set(props.hiddenKeys ?? []);
    const all = [
        { key: "wechat" as const, name: "微信支付", iconText: "微" },
        { key: "alipay" as const, name: "支付宝支付", iconText: "支" },
        { key: "paypal" as const, name: "PayPal支付", iconText: "P" },
        { key: "yunpay" as const, name: "云支付", iconText: "云" },
        // 汇付：收银台展示为独立的微信/支付宝两项，图标与配色复用原生微信/支付宝
        { key: "huifuWechat" as const, name: t('微信支付'), iconText: t('微'), iconKey: "wechat" as const },
        { key: "huifuAlipay" as const, name: t('支付宝支付'), iconText: t('支'), iconKey: "alipay" as const },
        // 拉卡拉：收银台展示为独立的微信/支付宝两项，图标与配色复用原生微信/支付宝
        { key: "lakalaWechat" as const, name: t('微信支付'), iconText: t('微'), iconKey: "wechat" as const },
        { key: "lakalaAlipay" as const, name: t('支付宝支付'), iconText: t('支'), iconKey: "alipay" as const },
        // 易宝：收银台展示为独立的微信/支付宝两项，图标与配色复用原生微信/支付宝
        { key: "yeepayWechat" as const, name: t('微信支付'), iconText: t('微'), iconKey: "wechat" as const },
        { key: "yeepayAlipay" as const, name: t('支付宝支付'), iconText: t('支'), iconKey: "alipay" as const },
        { key: "applepay" as const, name: "Apple Pay", iconText: "" }
    ];

    return all.map((it) => {
        // 未加载完配置前，默认都不展示，避免先全展示再“闪一下”消失
        const backendEnabled = enabledKeysLoaded.value ? enabledKeys.value.has(it.key) : false;
        const isCurrent = props.selected === it.key;
        const visible = enabledKeysLoaded.value && !hidden.has(it.key) && (isCurrent || backendEnabled);
        // iconKey：用于取图标/配色，默认与 key 相同（汇付两项复用微信/支付宝）
        const iconKey = ((it as any).iconKey ?? it.key) as PayKey;
        return { ...it, visible, iconKey };
    });
});

const availableItems = computed(() => {
    const base = items.value.filter((it) => it.visible);
    const order = (props.paymentOrder && props.paymentOrder.length ? props.paymentOrder : paymentOrder.value) as PayKey[];
    if (!order.length) return base;

    const idx = new Map<PayKey, number>(order.map((k, i) => [k, i]));
    return base
        .map((it, originalIndex) => ({ it, originalIndex, orderIndex: idx.get(it.key) }))
        .sort((a, b) => {
            const ao = a.orderIndex ?? Number.POSITIVE_INFINITY;
            const bo = b.orderIndex ?? Number.POSITIVE_INFINITY;
            if (ao !== bo) return ao - bo;
            return a.originalIndex - b.originalIndex;
        })
        .map((x) => x.it);
});

const effectiveSelected = computed<PayKey | undefined>(() => {
    const available = availableItems.value;
    if (!available.length) return undefined;
    const preferred = props.selected;
    if (preferred && available.some((it) => it.key === preferred)) return preferred;
    return available[0].key;
});
</script>

<style lang="less" scoped>
.h5-preview {
    display: flex;
    justify-content: center;
    min-height: 0;
    padding: 4px 0;
    min-height: 600px;
}

.phone {
    width: 290px;
    border-radius: 22px;
    background: #f5f6f8;
    border: 1px solid #e8eaee;
    overflow: hidden;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.06);
    position: relative;
    padding-bottom: 72px; // 预留底部支付按钮区域，避免遮挡内容
}

.phone-header {
    height: 46px;
    background: #ffffff;
    display: flex;
    align-items: center;
    padding: 0 12px;
    border-bottom: 1px solid #eef0f3;
    .back {
        width: 24px;
        font-size: 22px;
        line-height: 22px;
        color: #333;
    }
    .title {
        flex: 1;
        text-align: center;
        font-weight: 600;
        color: #222;
    }
    .spacer {
        width: 24px;
    }
}

.card {
    background: #fff;
    margin-bottom: 10px;
    border-radius: 8px;
    overflow: hidden;
}

.amount-card {
    border-top-left-radius: 0;
    border-top-right-radius: 0;
    padding: 14px 12px;
    .amount {
        font-size: 24px;
        font-weight: 700;
        color: #ff2b2b;
        text-align: center;
        margin-bottom: 6px;
    }
    .order-no {
        font-size: 12px;
        color: #666;
        text-align: center;
    }
}

.methods-card {
    padding: 6px 0;
    .method-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px 12px;
        border-top: 1px solid #f2f3f5;
        &:first-child {
            border-top: none;
        }
        .left {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .name {
            font-size: 13px;
            color: #222;
        }
        .icon {
            width: 22px;
            height: 22px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            flex: 0 0 auto;
        }
        .icon-img {
            width: 100%;
            height: 100%;
            display: block;
            object-fit: contain;
        }
        .icon-fallback {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            font-weight: 700;
            color: #fff;
            &.wechat {
                background: #2bb640;
            }
            &.alipay {
                background: #1677ff;
            }
            &.paypal {
                background: #0a3d91;
            }
            &.stripe {
                background: #635bff;
            }
            &.antom {
                background: #1677ff;
            }
            &.xendit {
                background: #fa1f4e;
            }
            &.yunpay {
                background: #13c2c2;
            }
            &.applepay {
                background: #000000;
            }
        }
        .checked {
            width: 18px;
            height: 18px;
            border-radius: 999px;
            background: #ff2b2b;
            position: relative;
            &::after {
                content: "";
                position: absolute;
                left: 5px;
                top: 4px;
                width: 7px;
                height: 4px;
                border-left: 2px solid #fff;
                border-bottom: 2px solid #fff;
                transform: rotate(-45deg);
            }
        }
        .unchecked {
            width: 18px;
            height: 18px;
            border-radius: 999px;
            border: 1px solid #d7dbe0;
            background: #fff;
        }
    }
}

.other-card {
    padding: 12px;
    .other-title {
        font-size: 13px;
        font-weight: 600;
        color: #222;
        margin-bottom: 8px;
    }
    .other-row {
        font-size: 12px;
        color: #666;
        line-height: 18px;
        .k {
            color: #999;
        }
        .v {
            color: #666;
        }
    }
}

.pay-bar {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 12px 10px 14px;
    background: #ffffff;
    border-top: 1px solid #eef0f3;
    .pay-btn {
        height: 40px;
        border-radius: 999px;
        background: #ff2b2b;
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        letter-spacing: 1px;
    }
}
</style>
