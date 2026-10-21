import { registerModule } from "../../../config/module-registry";
import { createProductDetailDefaults } from "../_shared/defaults";
import type { ProductDetailModule } from "../_shared/types";

export type { ProductDetailModule } from "../_shared/types";

/** 商品详情 A3（跨境风）默认配置：黑色价格、浅灰页面底色、白底卡片 */
export const productDetailA3Defaults: ProductDetailModule = {
    ...createProductDetailDefaults("A3"),
    imageRatio: "1:1",
    imageMargin: 16,
    imageRadiusStyle: "square",
    titleSize: "small",
    titleColor: "#1a1a1a",
    titleMaxLines: 2,
    priceColor: "#1a1a1a",
    buyBarStyle: 2,
    buttonRadius: 6,
    showHeader: true,
    backgroundColor: "#f5f5f5",
    contentBackgroundColor: "#ffffff"
};

registerModule({
    type: "productDetailA3",
    label: "商品详情A3",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileProductDetail",
    platform: "mobile",
    order: 3,
    exclusive: true,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: (): ProductDetailModule => ({
        ...createProductDetailDefaults("A3"),
        imageRatio: "1:1",
        imageMargin: 16,
        imageRadiusStyle: "square",
        titleSize: "small",
        titleColor: "#1a1a1a",
        titleMaxLines: 2,
        priceColor: "#1a1a1a",
        buyBarStyle: 2,
        buttonRadius: 6,
        showHeader: true,
        backgroundColor: "#f5f5f5",
        contentBackgroundColor: "#ffffff"
    })
});
