import panel from "@/router/asyncRoutes/admin/panel";
import product from "@/router/asyncRoutes/admin/product";
import promotion from "@/router/asyncRoutes/admin/promotion";
import order from "@/router/asyncRoutes/admin/order";
import decorate from "@/router/asyncRoutes/admin/decorate";
import salesman from "@/router/asyncRoutes/admin/salesman";
import content from "@/router/asyncRoutes/admin/content";
import live from "@/router/asyncRoutes/admin/live";
import finance from "@/router/asyncRoutes/admin/finance";
import user from "@/router/asyncRoutes/admin/user";
import authority from "@/router/asyncRoutes/admin/authority";
import setting from "@/router/asyncRoutes/admin/setting";
import organize from "@/router/asyncRoutes/admin/organize";
import wholesale from "@/router/asyncRoutes/admin/wholesale";
import { createCustomerServiceRoute } from "@/router/asyncRoutes/common/customerService";

export default [
    panel,
    product,
    order,
    promotion,
    organize,
    wholesale,
    decorate,
    salesman,
    live,
    content,
    finance,
    user,
    createCustomerServiceRoute(),
    authority,
    setting
];
