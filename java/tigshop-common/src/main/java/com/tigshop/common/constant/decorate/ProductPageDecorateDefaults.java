package com.tigshop.common.constant.decorate;

/**
 * 商品详情页与商品搜索页的内置装修默认数据。
 *
 * <p>这些常量只作为默认模板：数据库已有有效装修时始终以客户数据为准；数据库无记录或内容为空时，
 * 由服务层根据 {@code tigshop.is-overseas} 选择普通版或跨境版。</p>
 */
public final class ProductPageDecorateDefaults {

    private ProductPageDecorateDefaults() {
    }

    /** H5 商品详情页（pageType=8）普通版默认数据，对齐项目 A 的商品详情 A2。 */
    public static final String PRODUCT_DETAIL = """
            {
              "pageModule": {
                "type": "page",
                "module": {},
                "backgroundRepeat": "",
                "backgroundSize": "",
                "style": 0,
                "headerStyle": 1,
                "title": "",
                "titleColor": "",
                "immersive": false,
                "titleBackgroundColor": "",
                "backgroundImage": {"picUrl": "", "picThumb": ""},
                "backgroundColor": ""
              },
              "moduleList": [
                {
                  "type": "productDetailA2",
                  "url": "mobileProductDetail",
                  "label": "商品详情A2",
                  "isShow": true,
                  "module": {
                    "imageRatio": "1:1",
                    "imageMode": 1,
                    "imageRadiusStyle": "square",
                    "titleSize": "medium",
                    "titleColor": "#1a1a1a",
                    "titleMaxLines": 2,
                    "priceColor": "#ff4d4f",
                    "marketPriceColor": "#999",
                    "moduleSortList": [
                      {"key": "activity", "label": "活动信息", "visible": true},
                      {"key": "spec", "label": "规格参数", "visible": true},
                      {"key": "review", "label": "评价入口", "visible": true},
                      {"key": "afterSale", "label": "售后入口", "visible": true},
                      {"key": "coupon", "label": "领劵/优惠", "visible": true}
                    ],
                    "buyBarStyle": 1,
                    "buttonRadius": 5,
                    "showHome": true,
                    "showService": true,
                    "showCart": true,
                    "backgroundColor": "#f5f5f5",
                    "contentBackgroundColor": "#ffffff",
                    "decorateStyle": "A2"
                  },
                  "moduleIndex": 1
                }
              ]
            }
            """;

    /** H5 商品详情页（pageType=8）跨境版默认数据，对齐项目 A 的商品详情 A3。 */
    public static final String OVERSEAS_PRODUCT_DETAIL = """
            {
              "pageModule": {
                "type": "page",
                "module": {},
                "backgroundRepeat": "",
                "backgroundSize": "",
                "style": 0,
                "headerStyle": 1,
                "title": "",
                "titleColor": "",
                "immersive": false,
                "titleBackgroundColor": "",
                "backgroundImage": {"picUrl": "", "picThumb": ""},
                "backgroundColor": ""
              },
              "moduleList": [
                {
                  "type": "productDetailA3",
                  "url": "mobileProductDetail",
                  "label": "商品详情A3",
                  "isShow": true,
                  "module": {
                    "imageRatio": "1:1",
                    "imageMode": 1,
                    "imageRadiusStyle": "square",
                    "titleSize": "small",
                    "titleColor": "#1a1a1a",
                    "titleMaxLines": 2,
                    "priceColor": "#1a1a1a",
                    "marketPriceColor": "#999",
                    "moduleSortList": [
                      {"key": "activity", "label": "活动信息", "visible": true},
                      {"key": "spec", "label": "规格参数", "visible": true},
                      {"key": "review", "label": "评价入口", "visible": true},
                      {"key": "afterSale", "label": "售后入口", "visible": true},
                      {"key": "coupon", "label": "领劵/优惠", "visible": true}
                    ],
                    "buyBarStyle": 2,
                    "buttonRadius": 6,
                    "showHome": true,
                    "showService": true,
                    "showCart": true,
                    "showHeader": true,
                    "backgroundColor": "#f5f5f5",
                    "contentBackgroundColor": "#ffffff",
                    "decorateStyle": "A3"
                  },
                  "moduleIndex": 1
                }
              ]
            }
            """;

    /** H5 商品搜索页（pageType=6）普通版默认数据，对齐项目 A 的完整搜索装修契约。 */
    public static final String SEARCH = """
            {
              "pageModule": {
                "title": "搜索页",
                "titleColor": "#333333",
                "titleBackgroundColor": "#ffffff",
                "backgroundColor": "#f5f5f5"
              },
              "moduleList": [
                {
                  "moduleIndex": 1,
                  "type": "mobileSearchResult",
                  "url": "mobileSearch",
                  "label": "搜索结果",
                  "isShow": true,
                  "module": {
                    "searchResultType": 1,
                    "backgroundColor": "#f5f5f5",
                    "searchItemBackgroundColor": "#ffffff",
                    "searchItemPaddingLeftRight": 10,
                    "itemBackgroundColor": "#ffffff",
                    "themeColor": "#ff4d4f",
                    "showFilter": true,
                    "showShopTag": true,
                    "showSales": true,
                    "showCartButton": true,
                    "showCoupon": true,
                    "layoutStyle": 1,
                    "listStyle": 1,
                    "productTitleColor": "#333333",
                    "productTitleLine": 2,
                    "priceColor": "#ff4d4f",
                    "buyBtnStyle": 1,
                    "margin": 0,
                    "padding": 10,
                    "paddingTopBottom": 10,
                    "paddingLeftRight": 10,
                    "moduleRadius": 0,
                    "productImageRadius": 4,
                    "storeShowRating": 1,
                    "storeShowMinOrder": 1,
                    "storeShowDeliveryFee": 1,
                    "storeShowDistance": 1,
                    "storeShowFollow": 1,
                    "storeShowProductName": 1,
                    "storeProductNameRow": 1,
                    "storeShowProductPrice": 1,
                    "storeProductGap": 8,
                    "storeGap": 10,
                    "storePaddingTopBottom": 12,
                    "storePaddingLeftRight": 12,
                    "storeCardRadius": 8,
                    "storeLogoRadius": 50,
                    "storeShopTitleColor": "#1a1a1a",
                    "storeShopTitleFontSize": 15,
                    "storeShopTitleFontBold": 700
                  }
                }
              ]
            }
            """;

    /** 跨境版搜索仅切换默认布局；其余字段与普通版保持同一契约。 */
    public static final String OVERSEAS_SEARCH =
            SEARCH.replace("\"layoutStyle\": 1", "\"layoutStyle\": 2");
}
