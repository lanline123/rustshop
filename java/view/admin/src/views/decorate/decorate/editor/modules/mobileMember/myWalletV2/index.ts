import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import type { WalletV2Item } from "./types";

export type { MyWalletV2Module } from "./types";

const defaultIcons: Record<string, string> = {
    balance: "https://oss.tigshop.com/official/img/pc/202605/177993337419dbsyRWczzneL4mhf.png",
    coupon: "https://oss.tigshop.com/official/img/pc/202605/17799333737quaCAwzzMe1XpmYHd.png",
    collect: "https://oss.tigshop.com/official/img/pc/202605/1779933373JYzFsV1Ky8YZ32BzDg.png",
    history: "https://oss.tigshop.com/official/img/pc/202605/1779933373CFhQRjCp2OMvTY1vjQ.png"
};

const defaultItems: WalletV2Item[] = [
    { key: "balance", label: "余额", icon: { picUrl: defaultIcons.balance, picThumb: defaultIcons.balance } },
    { key: "coupon", label: "优惠券", icon: { picUrl: defaultIcons.coupon, picThumb: defaultIcons.coupon } },
    { key: "collect", label: "商品收藏", icon: { picUrl: defaultIcons.collect, picThumb: defaultIcons.collect } },
    { key: "history", label: "浏览记录", icon: { picUrl: defaultIcons.history, picThumb: defaultIcons.history } }
];

const defaultInnerPadding = 12;

/** 与旧 mobileMember/myWalletV2/View.vue 的 defaultModule 逐字段一致（契约） */
export const myWalletV2Defaults = {
    items: defaultItems.map((i) => ({ ...i, icon: { ...i.icon } })),
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: defaultInnerPadding,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

registerModule({
    type: "myWalletV2",
    label: "我的钱包2",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 7,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(myWalletV2Defaults)
});
