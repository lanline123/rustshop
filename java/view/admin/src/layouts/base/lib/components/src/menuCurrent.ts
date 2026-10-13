interface MenuItem {
    component?: unknown;
    meta?: {
        isShow?: boolean | number;
        current?: boolean;
    };
    children?: MenuItem[];
}

function activateFirstNavigableBranch(menuItem: MenuItem): boolean {
    for (const child of menuItem.children || []) {
        if (!child.meta?.isShow) continue;

        if (child.children?.length) {
            if (activateFirstNavigableBranch(child)) {
                child.meta.current = true;
                return true;
            }
            continue;
        }

        // 工作台等动作入口没有页面组件，不作为侧边栏默认选中项。
        if (child.component) {
            child.meta.current = true;
            return true;
        }
    }
    return false;
}

/** 当前路由不属于可见菜单时，激活第一个可见一级菜单及其首个可访问页面。 */
export function ensureVisibleCurrentMenu(menuItems: MenuItem[]): void {
    const hasVisibleCurrentMenu = menuItems.some((menuItem) => menuItem.meta?.isShow && menuItem.meta.current);
    if (hasVisibleCurrentMenu) return;

    const firstVisibleMenu = menuItems.find((menuItem) => menuItem.meta?.isShow);
    if (firstVisibleMenu?.meta) {
        firstVisibleMenu.meta.current = true;
        activateFirstNavigableBranch(firstVisibleMenu);
    }
}
