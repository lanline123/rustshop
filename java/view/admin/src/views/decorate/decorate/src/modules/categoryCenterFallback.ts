/**
 * 分类页（pageType=4）「老分类页用户」兜底拼装
 *
 * 当分类页没有任何装修内容（moduleList 为空）时，读取老分类页配置
 * （productCategoryDecorateType），自动拼装对应的分类中心模块，让历史用户
 * 进入编辑器即可看到与原分类页一致的布局，可继续编辑并保存。
 *
 * 映射关系：
 *   样式1 → 分类中心V1（cateCenterV1）
 *   样式2（双列网格）→ 分类中心V2（cateCenterV2，styleType=2）
 *   样式3（图文列表）→ 分类中心V2（cateCenterV2，styleType=3）
 */

import { getConfigCategoryDecorate } from "@/api/setting/config";

const makeModule = (type: string, label: string, module: any = {}) => ({
    type,
    url: "mobileCateCenter",
    label,
    isShow: true,
    moduleIndex: Date.now(),
    module
});

/**
 * 判断分类页是否需要兜底：moduleList 没有任何有效模块
 */
export function shouldBuildCategoryDefault(list: any[]): boolean {
    if (!Array.isArray(list)) return true;
    return list.filter((m) => m && m.type).length === 0;
}

/**
 * 根据老分类页配置拼装分类中心模块（仅一个独占模块）
 */
export async function buildCategoryCenterDefaultModules(): Promise<any[]> {
    let decorateType = 1;
    try {
        const result: any = await getConfigCategoryDecorate();
        const type = Number(result?.productCategoryDecorateType);
        if (type === 1 || type === 2 || type === 3) {
            decorateType = type;
        }
    } catch (error) {
        // 接口异常时退回样式1（分类中心V1），与老分类页默认一致
    }

    if (decorateType === 2 || decorateType === 3) {
        return [makeModule("cateCenterV2", "分类中心V2", { styleType: decorateType })];
    }
    return [makeModule("cateCenterV1", "分类中心V1")];
}
