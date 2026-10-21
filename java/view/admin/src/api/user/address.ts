import request from "@/utils/request";

/**
 * 会员收货地址（Admin）
 *
 * 注意：你当前项目里只有地址列表接口（pos/user/userAddressList）。
 * 新增地址接口在本仓库里未找到现成用法，因此这里提供一个“可配置/可替换”的默认实现。
 *
 * 如果后端实际路径不同，请把下面的 URL 改成你们真实接口即可。
 */

export interface CreateUserAddressPayload {
    userId: number;
    consignee: string;
    mobile: string;
    email?: string;
    regionIds: number[];
    address: string;
    longitude?: number;
    latitude?: number;
    isDefault?: number; // 1/0
    [key: string]: any;
}

/**
 * 新增地址并返回 addressId
 *
 * 默认尝试：pos/user/userAddressCreate
 * - 如果你们后端不是这个路径，请替换成真实路径
 */
export const createUserAddress = async (data: CreateUserAddressPayload): Promise<number> => {
    const res: any = await request<any>({
        url: "pos/user/userAddressCreate",
        method: "post",
        data
    });

    // 兼容不同返回结构：直接 number / { addressId } / { id }
    if (typeof res === "number") return res;
    if (res?.addressId) return Number(res.addressId);
    if (res?.id) return Number(res.id);
    return Number(res?.data?.addressId || res?.data?.id || 0);
};


