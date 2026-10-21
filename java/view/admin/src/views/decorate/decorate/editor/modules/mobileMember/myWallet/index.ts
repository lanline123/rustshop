import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { MyWalletModule } from "./types";

const defaultWalletIcon = "https://oss.tigshop.com/official/img/pc/202605/1779873859zO64yVrNvp2y0lUsOz.png";
const defaultInnerPadding = 16;

/** 与旧 mobileMember/myWallet/View.vue 的 defaultModule 逐字段一致（契约） */
export const myWalletDefaults = {
    layoutStyle: 1,
    walletIcon: { picUrl: defaultWalletIcon, picThumb: defaultWalletIcon },
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: defaultInnerPadding,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

registerModule({
    type: "myWallet",
    label: "我的钱包",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 6,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(myWalletDefaults)
});
