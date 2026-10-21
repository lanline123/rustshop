type BalanceSource = {
    shopMoney?: number | string | null;
    offlineBalance?: number | string | null;
    onlineBalance?: number | string | null;
    availableBalance?: number | string | null;
    wechatBalance?: number | string | null;
    channelBalances?: Record<string, number | string | null> | null;
};

export const ONLINE_CHANNEL_ORDER = ["WECHAT", "HUIFU", "ALIPAY"] as const;
export type OnlineChannelCode = (typeof ONLINE_CHANNEL_ORDER)[number];

export type OnlineChannelBalance = {
    code: OnlineChannelCode;
    amount: number;
    labelKey: string;
};

export type ShopBalanceParts = {
    offline: number;
    online: number;
    available: number;
    onlineChannels: OnlineChannelBalance[];
};

export type BalanceFilterKey = "all" | "offline" | OnlineChannelCode;

const CHANNEL_LABEL_KEY: Record<OnlineChannelCode, string> = {
    WECHAT: "微信分账余额",
    HUIFU: "汇付分账余额",
    ALIPAY: "支付宝分账余额"
};

const toNumber = (value: unknown): number | undefined => {
    if (value === undefined || value === null || value === "") return undefined;
    const num = Number(value);
    return Number.isFinite(num) ? num : undefined;
};

const firstNumber = (...values: unknown[]): number | undefined => {
    for (const value of values) {
        const num = toNumber(value);
        if (num !== undefined) return num;
    }
    return undefined;
};

/** 只列出 dualBalance.channelBalances 里查到且余额大于 0 的渠道。 */
export const listQueryableOnlineChannels = (
    channels?: Record<string, number | string | null> | null
): OnlineChannelBalance[] => {
    if (!channels || typeof channels !== "object") return [];
    return ONLINE_CHANNEL_ORDER.flatMap((code) => {
        if (!Object.prototype.hasOwnProperty.call(channels, code)) return [];
        const amount = toNumber(channels[code]);
        if (amount === undefined || amount <= 0) return [];
        return [{ code, amount, labelKey: CHANNEL_LABEL_KEY[code] }];
    });
};

const sumOnlineChannels = (channels: OnlineChannelBalance[]): number =>
    channels.reduce((sum, channel) => sum + channel.amount, 0);

export const resolveBalanceLogFilter = (key: BalanceFilterKey) => {
    if (key === "all") return { balanceType: "all", payChannel: "" };
    if (key === "offline") return { balanceType: "offline", payChannel: "" };
    return { balanceType: "online", payChannel: key };
};

const CHANNEL_SPLIT_LABEL: Record<string, string> = {
    WECHAT: "微信分账",
    HUIFU: "汇付分账",
    ALIPAY: "支付宝分账"
};

/** 明细列表「分账类型」：对齐 logList 的 balanceType / payChannel。 */
export const formatAccountSplitType = (row?: {
    balanceType?: number | string | null;
    payChannel?: string | null;
} | null): string => {
    const channel = String(row?.payChannel || "").trim().toUpperCase();
    if (channel && CHANNEL_SPLIT_LABEL[channel]) return CHANNEL_SPLIT_LABEL[channel];
    const type = row?.balanceType;
    if (type === 2 || type === "2" || type === "online") return "线上分账";
    if (type === 1 || type === "1" || type === "offline") return "线下分账";
    return "线下分账";
};

/** 可用余额 = 各可查线上渠道 + 线下。线上不能用 onlineBalance（它只等于微信）。 */
export const resolveShopBalances = (account?: BalanceSource | null, dual?: BalanceSource | null): ShopBalanceParts => {
    const offline =
        firstNumber(account?.offlineBalance, dual?.offlineBalance, dual?.shopMoney, account?.shopMoney) ?? 0;
    const onlineChannels = listQueryableOnlineChannels(dual?.channelBalances);
    const online = sumOnlineChannels(onlineChannels);
    const available = firstNumber(account?.availableBalance, dual?.availableBalance) ?? offline + online;
    return { offline, online, available, onlineChannels };
};
