import { registerModule } from "../../../config/module-registry";
import { createProductDetailDefaults } from "../_shared/defaults";

export type { ProductDetailModule } from "../_shared/types";

/** 商品详情 A2（门店风）：主图默认 1:1，页面底色用于承接卡片间距 */
export const productDetailA2Defaults = {
    ...createProductDetailDefaults("A2"),
    imageRatio: "1:1" as const,
    imageMargin: 0,
    backgroundColor: "#f5f5f5"
};

registerModule({
    type: "productDetailA2",
    label: "商品详情A2",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileProductDetail",
    platform: "mobile",
    order: 2,
    exclusive: true,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => ({ ...productDetailA2Defaults })
});
