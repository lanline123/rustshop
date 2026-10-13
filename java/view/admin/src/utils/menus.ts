import { getAllAuthority, getAllAuthorityData } from "@/api/authority/authority";
import type { MainMenu } from "@/types/common/common.d";
import { adminRouters } from "@/router/asyncRoutes";

import { merchantRouters } from "@/router/asyncRoutes";


import { useMenusStore } from "@/store/menu";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";
import { resolveAccessibleRedirect } from "@/utils/menuRedirect";

/** 「语音播报」功能权限的权限位（后端 authority 表 authoritySn） */
const VOICE_BROADCAST_AUTHORITY_SN = "voiceBroadcastManage";

/** 平台客户管理菜单的展示文案（权限标识保持与后端一致） */
const CUSTOMER_AUTHORITY_LABELS: Record<string, string> = {
    user: "客户",
    userManagement: "客户管理",
    userManage: "客户管理",
    levelManageManage: "等级权益",
    userLevelProManage: "等级权益",
    memberTagManage: "客户标签",
    shopMemberSettingManage: "客户设置",
    feedbackManage: "留言反馈",
    userCertificationManage: "客户认证"
};

export const updateMenu = async () => {
    try {
        const result = await getAllAuthority();
        return result as MainMenu[];
    } catch (error) {}
};

export const getMenu = async () => {
    try {
        const result = await getAllAuthorityData();
        const adminType = localStorage.getItem("adminType");
        const authorities =
            adminType === "admin"
                ? (result || []).map((authority: any) => {
                      const authorityName = CUSTOMER_AUTHORITY_LABELS[authority.authoritySn];
                      return authorityName ? { ...authority, authorityName } : authority;
                  })
                : result || [];
        // 权限位回填必须早于空列表早退，否则切店铺/切角色后会残留上一次的 true（REQ-17）
        useVoiceBroadcastStore().setPermission(!!result?.some((item: any) => item.authoritySn === VOICE_BROADCAST_AUTHORITY_SN));
        if (!authorities.length) {
            return [];
        }
        const menus = useMenusStore();
        let arr: any = authorities;
        let routers: any = [];
        menus.setLicensed(arr.find((item: any) => item.authoritySn === "licensed" && item.isShow === 1) ? true : false);

        if (adminType === "shop" || adminType === "store") {
            routers = merchantRouters;
            arr = [...arr, { authoritySn: "accountManage", authorityName: "账户列表" }];
        }

        if (adminType === "admin") {
            routers = adminRouters;
            const fallbackAuthorities = [{ authoritySn: "accountEditingManage", authorityName: "个人中心", isShow: 0 }];
            fallbackAuthorities.forEach((authority) => {
                if (!arr.some((item: any) => item.authoritySn === authority.authoritySn)) {
                    arr = [...arr, authority];
                }
            });
        }

        return filterRoutesByAuthority(routers, arr) || [];
    } catch (error) {
        // 权限接口失败时按 fail-closed 收回语音播报权限，避免沿用上一次的判定结果（REQ-17）
        useVoiceBroadcastStore().setPermission(false);
    }
};

/**
 * 路由项类型定义
 */
interface RouteItem {
    path: string;
    name: string;
    component?: any;
    redirect?: string;
    meta?: {
        title?: string;
        is404?: boolean;
        isShowInHeader?: boolean;
        queryParam?: string;
        [key: string]: any;
    };
    children?: RouteItem[];
    [key: string]: any;
}

/**
 * 权限项类型定义
 */
interface AuthorityItem {
    authoritySn: string;
    authorityName: string;
    authorityIco: string;
    isShow: boolean;
}

/**
 * 根据 authority 数组匹配路由，并自动修正 redirect 路径
 * @param routes 原始路由配置
 * @param authorities 权限项数组
 * @returns 匹配后的路由列表
 */
function filterRoutesByAuthority(routes: RouteItem[], authorities: AuthorityItem[]): RouteItem[] {
    const routers: RouteItem[] = [];

    // 创建权限映射，方便查找
    const authMap: Record<string, AuthorityItem> = {};
    authorities.forEach((auth) => {
        authMap[auth.authoritySn] = auth;
    });

    // 递归查找匹配的路由
    function findMatchedRoutes(routeList: RouteItem[], parentPath: string = "", resultContainer?: RouteItem[]): void {
        routeList.forEach((route) => {
            const currentFullPath = parentPath ? `${parentPath}/${route.path}`.replace(/\/+/g, "/") : route.path;

            // 检查当前路由是否在权限列表中
            if (authMap[route.name]) {
                const filteredRoute: RouteItem = { ...route };
                filteredRoute.meta = { ...(filteredRoute.meta || {}), ...authMap[route.name], current: true };

                if (route.children && route.children.length) {
                    filteredRoute.children = [];
                    findMatchedRoutes(route.children, currentFullPath, filteredRoute.children);

                    // 修正 redirect 路径，传入完整的父路径
                    filteredRoute.redirect = resolveAccessibleRedirect(route, filteredRoute.children, currentFullPath);
                }

                if (resultContainer) {
                    resultContainer.push(filteredRoute);
                } else {
                    routers.push(filteredRoute);
                }
            }
            // 即使当前路由不匹配，仍需检查其子路由
            else if (route.children && route.children.length) {
                const matchedChildren: RouteItem[] = [];
                findMatchedRoutes(route.children, currentFullPath, matchedChildren);

                // 如果有匹配的子路由，则保留父路由
                if (matchedChildren.length > 0) {
                    const filteredRoute: RouteItem = { ...route, children: matchedChildren };

                    // 修正 redirect 路径
                    filteredRoute.redirect = resolveAccessibleRedirect(route, matchedChildren, currentFullPath);

                    if (resultContainer) {
                        resultContainer.push(filteredRoute);
                    } else {
                        routers.push(filteredRoute);
                    }
                }
            }
        });
    }

    findMatchedRoutes(routes);

    return routers;
}
