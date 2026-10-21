import NotFound from "@/layouts/blank/blank.vue";
import { getShopType } from "@/utils/storage";

export default {
    path: "/",
    name: "panel",
    redirect: "/",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: getShopType() === 2 ? "门店" : "店铺" },
    children: [
        {
            path: "/",
            name: "consoleManage",
            meta: { title: "概览" },
            component: () => import("@/views/panel/Index.vue")
        },
        {
            path: "",
            name: "orderManagement",
            meta: { title: "订单管理" },
            redirect: "/order/list",
            children: [
                {
                    path: "order/list",
                    name: "orderManage",
                    meta: { title: "全部订单" },
                    component: () => import("@/views/order/order/List.vue")
                },
                {
                    path: "order/deliverlist",
                    name: "deliverManage",
                    meta: { title: "发货管理" },
                    component: () => import("@/views/order/order/Deliverlist.vue")
                },
                {
                    path: "order-export/list",
                    name: "orderExportManage",
                    meta: { title: "订单导出" },
                    component: () => import("@/views/order/orderExport/List.vue")
                },
                {
                    path: "proxy-shopping",
                    name: "proxyShoppingManage",
                    meta: { title: "代客下单" },
                    component: () => import("@/views/order/proxyShopping/Index.vue")
                }
            ]
        },
        {
            path: "",
            name: "aftersaleManagement",
            meta: { title: "售后管理" },
            redirect: "/order/aftersales/list",
            children: [
                {
                    path: "order/aftersales/list",
                    name: "aftersalesManage",
                    meta: { title: "售后订单" },
                    component: () => import("@/views/order/aftersales/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "commodityManage",
            meta: { title: "商品管理" },
            redirect: "/order/product/list",
            children: [
                {
                    path: "product/list",
                    name: "productManage",
                    meta: { title: "商品列表" },
                    component: () => import("@/views/product/product/List.vue")
                },
                 {
                    path: "wholesale/product/list",
                    name: "wholesaleProductManage",
                    meta: { title: "批发商品列表" },
                    component: () => import("@/views/wholesale/product/List.vue")
                },
                {
                    path: "shop-product-category/list",
                    name: "shopProductCategoryManage",
                    meta: { title: "分类管理" },
                    component: () => import("@/views/product/shopProductCategory/List.vue")
                },
                {
                    path: "product-group/list",
                    name: "productGroupManage",
                    meta: { title: "商品分组" },
                    component: () => import("@/views/product/productGroup/List.vue")
                },
                {
                    path: "brand/list",
                    name: "brandManage",
                    meta: { title: "品牌管理" },
                    component: () => import("@/views/product/brand/List.vue")
                },
                {
                    path: "comment/list",
                    name: "commentManage",
                    meta: { title: "评价管理" },
                    component: () => import("@/views/product/comment/List.vue")
                },
                {
                    path: "product-attributes-tpl/list",
                    name: "productAttributesTplManage",
                    meta: { title: "属性模板" },
                    component: () => import("@/views/product/productAttributesTpl/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "productBatch",
            meta: { title: "批量操作" },
            redirect: "/product/export/list",
            children: [
                {
                    path: "export/list",
                    name: "productBatchExportManage",
                    meta: { title: "批量导出" },
                    component: () => import("@/views/product/productBatch/export/List.vue")
                },
                {
                    path: "add/list",
                    name: "productBatchAddManage",
                    meta: { title: "批量上传" },
                    component: () => import("@/views/product/productBatch/add/List.vue")
                },
                {
                    path: "edit/list",
                    name: "productBatchEditManage",
                    meta: { title: "批量修改" },
                    component: () => import("@/views/product/productBatch/edit/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "quotationManage",
            meta: { title: "询价管理" },
            redirect: "/product/enquiry/list",
            children: [
                {
                    path: "enquiry/list",
                    name: "enquiryManage",
                    meta: { title: "商品询价" },
                    component: () => import("@/views/product/enquiry/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "salesOverview",
            meta: { title: "销售统计" },
            redirect: "/order/statistics-order/list",
            children: [
                {
                    path: "statistics-order/list/",
                    name: "statisticsOrder",
                    meta: { title: "销售概览" },
                    component: () => import("@/views/panel/statisticsOrder/List.vue")
                },
                {
                    path: "statistics-sale/list/",
                    name: "statisticsSale",
                    meta: { title: "销售明细" },
                    component: () => import("@/views/panel/statisticsSale/List.vue")
                },
                {
                    path: "statistics-goods/list/",
                    name: "statisticsGoods",
                    meta: { title: "销售排行" },
                    component: () => import("@/views/panel/statisticsGoods/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "stockControl",
            meta: { title: "库存管理" },
            redirect: "/product-inventory-log/list",
            children: [
                {
                    path: "product-inventory-log/list",
                    name: "productInventoryLogManage",
                    meta: { title: "库存日志" },
                    component: () => import("@/views/product/productInventoryLog/List.vue")
                },
                {
                    path: "inventory-calendar/list",
                    name: "storeInventoryCalendarManage",
                    meta: { title: "库存日历" },
                    component: () => import("@/views/product/inventoryCalendar/List.vue")
                },
                {
                    path: "inventory-inventoryQuery/list",
                    name: "inventoryQueryManage",
                    meta: { title: "库存查询" },
                    component: () => import("@/views/product/inventoryQuery/List.vue")
                },
                {
                    path: "inventory-goodsArePutIntoStorage/list",
                    name: "inventoryGoodsArePutIntoStorageManage",
                    meta: { title: "商品入库" },
                    component: () => import("@/views/product/goodsArePutIntoStorage/List.vue")
                },
                {
                    path: "inventory-goodsArePutOutOfStorage/list",
                    name: "inventoryGoodsArePutOutOfStorageManage",
                    meta: { title: "商品出库" },
                    component: () => import("@/views/product/goodsArePutOutOfStorage/List.vue")
                },
                {
                    path: "inventory-adjustment/list",
                    name: "inventoryAdjustmentManage",
                    meta: { title: "库存调整" },
                    component: () => import("@/views/product/inventoryAdjustment/List.vue")
                },
                {
                    path: "inventory-transfer/list",
                    name: "storeInventoryTransferManage",
                    meta: { title: "库存调拨" },
                    component: () => import("@/views/product/inventoryTransfer/List.vue")
                },
                {
                    path: "inventory-stock-warning/list",
                    name: "stockWarningManage",
                    meta: { title: "库存预警" },
                    component: () => import("@/views/product/stockWarning/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "shopQuotationManage",
            meta: { title: "询价管理" },
            redirect: "/product/enquiry/list",
            children: [
                {
                    path: "enquiry/list",
                    name: "shopEnquiryManage",
                    meta: { title: "商品询价" },
                    component: () => import("@/views/product/enquiry/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "capitalfund",
            meta: { title: "财务中心" },
            redirect: "/order/dashboard/index",
            children: [
                {
                    path: "dashboard/index",
                    name: "dashboardManage",
                    meta: { title: "资金概览" },
                    component: () => import("@/views/merchant/capitalfund/dashboard/Index.vue")
                },
                {
                    path: "balance/list",
                    name: "balanceManage",
                    meta: { title: getShopType() === 1 ? "店铺资金" : "门店资金" },
                    component: () => import("@/views/merchant/capitalfund/balance/List.vue")
                },
                {
                    path: "deposit/index",
                    name: "shopDepositManage",
                    meta: { title: "保证金" },
                    component: () => import("@/views/merchant/capitalfund/deposit/Index.vue")
                },
                {
                    path: "withdraw/index",
                    name: "withdrawManage",
                    meta: { title: "提现管理" },
                    component: () => import("@/views/merchant/capitalfund/withdraw/Index.vue")
                },
                {
                    path: "account/list",
                    name: "accountListManage",
                    meta: { title: "账户管理" },
                    component: () => import("@/views/merchant/capitalfund/account/List.vue")
                },
                {
                    path: "statement-download/list",
                    name: "statementDownload",
                    meta: { title: "对账单下载" },
                    component: () => import("@/views/finance/shopFunds/statementDownload/List.vue")
                },
                {
                    path: "statement-details/list",
                    name: "statementDetails",
                    meta: { title: "对账单明细" },
                    component: () => import("@/views/finance/shopFunds/statementDetails/List.vue")
                },
                {
                    path: "financial-log/list",
                    name: "financialLogManage",
                    meta: { title: "资金明细" },
                    component: () => import("@/views/shop/financialLog/List.vue")
                },
                // 线上分账（通用能力）：进件申请
                {
                    path: "merchant-application/apply",
                    name: "merchantApplyManage",
                    meta: { title: "进件申请" },
                    component: () => import("@/views/finance/merchantApplication/Apply.vue")
                }
            ]
        },

        {
            path: "/:pathMatch(.*)",
            name: "notFound",
            meta: { is404: true },
            component: NotFound
        }
    ]
};
