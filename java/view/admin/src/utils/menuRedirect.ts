interface MenuRouteItem {
    path: string;
    component?: unknown;
    redirect?: string;
    children?: MenuRouteItem[];
}

function resolveFullPath(parentPath: string, routePath: string): string {
    if (routePath.startsWith("/")) return routePath;
    if (!routePath) return parentPath;
    return parentPath.endsWith("/") ? `${parentPath}${routePath}` : `${parentPath}/${routePath}`;
}

function collectNavigablePaths(routes: MenuRouteItem[], parentPath: string): string[] {
    return routes.flatMap((route) => {
        const fullPath = resolveFullPath(parentPath, route.path);
        if (route.children?.length) {
            return collectNavigablePaths(route.children, fullPath);
        }
        return route.component ? [fullPath] : [];
    });
}

/** 从过滤后的权限路由中找到第一个真正有页面组件的可访问地址。 */
export function resolveAccessibleRedirect(route: MenuRouteItem, filteredChildren: MenuRouteItem[], parentPath: string): string | undefined {
    if (!filteredChildren.length) return route.redirect;

    const navigablePaths = collectNavigablePaths(filteredChildren, parentPath || route.path || "");
    if (!navigablePaths.length) return route.redirect;
    if (route.redirect && navigablePaths.includes(route.redirect)) return route.redirect;
    return navigablePaths[0];
}
