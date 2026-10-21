/**
 * 分销员归因相关工具
 * 集中收口 userInfo.salesman.salesmanId 的访问，规避 AnyObject 拼写错误且无 TS 提示的隐患
 */
import { useUserStore } from "@/store/user";

/** 分销员归因信息 */
export interface SalesmanInfo {
    /** 分销员 ID（后端通常为 number；URL 传输会被序列化为 string） */
    salesmanId: number | string;
    /** 分销员等级 */
    salesmanLevel?: number;
    /** 上级分销员 ID */
    parentId?: number;
}

/**
 * 读取当前登录用户的分销员 ID
 * - 不存在 / 未登录 / 非分销员 时返回 undefined
 * - 返回类型固定为 string，便于拼接 URL / 透传给接口
 */
export function getCurrentSalesmanId(): string | undefined {
    try {
        const userStore = useUserStore();
        const info = (userStore.userInfo ?? {}) as { salesman?: SalesmanInfo };
        const id = info.salesman?.salesmanId;
        if (id === undefined || id === null || id === "") return undefined;
        return String(id);
    } catch {
        return undefined;
    }
}
