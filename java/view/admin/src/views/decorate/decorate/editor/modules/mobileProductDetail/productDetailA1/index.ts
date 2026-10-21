import { registerModule } from "../../../config/module-registry";
import { createProductDetailDefaults } from "../_shared/defaults";

export type { ProductDetailModule } from "../_shared/types";

/** 商品详情 A1（标准风） */
export const productDetailA1Defaults = createProductDetailDefaults("A1");

registerModule({
    type: "productDetailA1",
    label: "商品详情A1",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileProductDetail",
    platform: "mobile",
    order: 1,
    exclusive: true,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => createProductDetailDefaults("A1")
});
