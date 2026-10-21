/**
 * 会员中心（pageType=2）「老会员页用户」兜底拼装
 *
 * 当会员中心装修页没有任何装修内容（moduleList 为空，或仅剩一个固定功能区）时，
 * 自动根据老会员页接口（memberDecorate）拼装一套默认会员中心模块，
 * 让历史用户进入编辑器即可看到一套完整的默认布局，可继续编辑并保存。
 *
 * 拼装顺序：
 *   用户信息 → 我的订单 → 我的钱包 → 广告图 → 我的服务 →（跨境）账号
 * 其中「广告图 / 我的服务」的数据取自 memberDecorate 接口的 bannerList / menus。
 * 固定功能区由 ensureFixedModules 另行处理。
 */

import { getDecorateDiscrete } from "@/api/decorate/decorateDiscrete";
import { isOverseas } from "@/utils/version";

let seq = 0;
const nextIndex = () => Date.now() + seq++;

const makeModule = (type: string, label: string, module: any = {}) => ({
    type,
    url: "mobileMember",
    label,
    isShow: true,
    moduleIndex: nextIndex(),
    module
});

const isFixedFunctionalArea = (m: any) => !!m && (m.type === "fixedFunctionalArea" || m.type === "fixed-functional-area");

/**
 * 判断会员中心是否需要兜底：moduleList 去掉固定功能区后没有任何有效模块
 */
export function shouldBuildMemberDefault(list: any[]): boolean {
    if (!Array.isArray(list)) return true;
    return list.filter((m) => m && m.type && !isFixedFunctionalArea(m)).length === 0;
}

/**
 * 拼装一套默认会员中心模块（含调用 memberDecorate 接口取广告图 / 我的服务数据）
 */
export async function buildMemberCenterDefaultModules(): Promise<any[]> {
    const overseas = isOverseas();
    const list: any[] = [];

    // 用户信息（头部）
    list.push(makeModule(overseas ? "userInfoV2" : "userInfo", overseas ? "用户信息2" : "用户信息"));
    // 我的订单
    list.push(makeModule(overseas ? "orderEntryV2" : "orderEntry", overseas ? "我的订单2" : "我的订单"));
    // 我的钱包
    list.push(makeModule("myWallet", "我的钱包"));

    // 广告图 / 我的服务：取老会员页接口数据
    let bannerModule: any = {};
    let serviceModule: any = {};
    try {
        const result: any = await getDecorateDiscrete({ decorateSn: "memberDecorate" });
        const discreteList: any[] = (result && result.data) || [];
        const banner = discreteList.find((it) => it && it.type === "bannerList");
        const menus = discreteList.find((it) => it && it.type === "menus");
        const bannerData = banner?.module?.bannerList;
        if (bannerData) {
            bannerModule = {
                pic: {
                    picUrl: bannerData.picUrl,
                    picThumb: bannerData.picThumb,
                    picId: bannerData.picId,
                    picName: bannerData.picName
                },
                picLink: bannerData.picLink || { link: "", name: "" }
            };
        }
        const menuList = menus?.module?.menuList;
        if (Array.isArray(menuList) && menuList.length) {
            serviceModule = { serviceList: menuList };
        }
    } catch (error) {
        // 接口异常时保留空模块，由模块自身默认数据兜底渲染
    }
    list.push(makeModule("adImage", "广告图", bannerModule));
    list.push(makeModule("myService", "我的服务", serviceModule));

    // 账号（跨境）
    if (overseas) {
        list.push(makeModule("account", "账号"));
    }

    return list;
}
