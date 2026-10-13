import shop from "@/router/asyncRoutes/merchant/shop";
import promotion from "@/router/asyncRoutes/merchant/promotion";
import salesman from "@/router/asyncRoutes/merchant/salesman";
import setting from "@/router/asyncRoutes/merchant/setting";
import customer from "@/router/asyncRoutes/merchant/customer";

import capitalfund from "@/router/asyncRoutes/merchant/capitalfund";
import merchantSetting from "@/router/asyncRoutes/merchant/merchantSetting";
import { createCustomerServiceRoute } from "@/router/asyncRoutes/common/customerService";

export default [
    shop,
    promotion,
    salesman,
    customer,
    createCustomerServiceRoute(),
    setting
];
