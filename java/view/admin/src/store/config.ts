/**
 * @file 全局配置状态管理Store
 * @description 管理后台全局配置信息，包括：
 *   - 分页大小、图标字体、货币符号等基础配置
 *   - 存储配置（本地/云存储）
 *   - 域名配置（PC端、H5端、IM域名）
 *   - 主题配置（布局、主题色、外观）
 *   - 版权和技术支持信息
 * @module store/config
 */
import { defineStore } from "pinia";
import { getAdminBase } from "@/api/setting/config";
import type { AdminConfigState } from "@/types/setting/config";
import { normalizeImageUploadMaxSize } from "@/utils/imageUploadLimit";

/**
 * 配置Store状态接口
 * @interface State
 */
interface State {
    /** 配置数据对象 */
    config: AdminConfigState;
    /** 配置是否已加载 */
    loaded: boolean;
    /** 是否为门店管理员 */
    isStoreAdmin: boolean;
    /** 当前域名 */
    domain: string;
}

export const useConfigStore = defineStore("config", {
    state: (): State => ({
        config: {
            pageSize: 15,                           // 默认分页大小
            icoDefinedCss: "//at.alicdn.com/t/c/font_400590_7he137t8862",  // 图标字体CSS
            dollarSign: "¥",                        // 货币符号
            dollarSignCn: "元",                     // 货币中文名（后端全局配置，跨境版主货币联动）
            amountUnit: "元",                       // 金额单位（统一映射自 dollarSignCn）
            storageType: 0,                         // 存储类型：0本地，1云存储
            storageUrl: "",                         // 云存储URL
            pcDomain: "",                           // PC端域名
            h5Domain: "",                           // H5移动端域名
            imDomain: "",                           // IM客服域名
            shopTitle: "",                          // 店铺名称
            version: "",                            // 系统版本号
            versionType: "",                        // 版本类型
            uploadMaxSize: 100,                     // 最大上传文件大小(MB)
            imageUploadMaxSize: 20,                 // 最大图片上传大小(MB)
            shopCompany: 1,                         // 公司类型
            shopCompanyTxt: "",                     // 公司名称
            defaultTechSupport: "",                 // 默认技术支持信息
            defaultCopyright: "Copyright © 2024 Tigshop. All Rights Reserved",  // 默认版权信息
            poweredBy: 0,                           // 技术支持显示状态
            poweredByLogo: "",                      // 技术支持Logo
            poweredByStatus: 0,                     // 技术支持状态
            clientDefaultUse: 0,                    // 客户端默认使用
            customProductNameLength: 200,           // 商品名称长度限制
            adminLightLogo: null,                   // 后台LOGO
            versionInfoHidden: 0,                   // 是否隐藏版本号
            layout: "default",                      // 导航模式
            navTheme: "dark",                       // 外观主题
            primaryColor: "blue",                   // 主题色
            withdrawSettingVO: {},                  // 提现设置
            productEditStockModify: 1,              // 商品编辑库存是否可修改：1=可修改（默认）；0=不可修改
            isOpenDeposit: 0,                       // 保证金功能开关
            isOpenShopMember: 0,                    // 店铺客户功能平台总开关（能力门）
            shopMemberDesensitize: 0,               // 店铺客户信息脱敏：1开启（店铺端客户电话/姓名脱敏展示）
            shopMemberAllowAdd: 0,                  // 店铺添加客户：1允许店铺自行新增店铺客户
            shopMemberAllowImport: 0,               // 店铺导入客户：1允许店铺导入自有客户
            specImageMode: 1,                       // 规格图片可上传范围：1=仅第一组规格（默认）；2=全部规格
            searchSettings: {}                      // 搜索设置
        },
        loaded: false,
        isStoreAdmin: false,
        domain: ""
    }),
    getters: {
        /**
         * 获取图标字体CSS完整URL
         * @returns {string} 图标字体CSS URL
         */
        icoDefinedCss: (state: State): string => {
            return "https:" + state.config.icoDefinedCss + ".css";
        },
        /**
         * 获取分页大小
         * @returns {number} 分页大小
         */
        pageSize: (state: State): number => {
            return Number(state.config.pageSize);
        },
        /**
         * 获取规范化后的 Admin 图片上传上限（MB）
         */
        imageUploadMaxSize: (state: State): number => {
            return normalizeImageUploadMaxSize(state.config.imageUploadMaxSize);
        },
        /**
         * 商品编辑库存是否可修改：1=可修改（默认）；0=不可修改。
         * 由 getAdminBase 随全局配置一起下发，无需单独调用接口。
         * 关闭时：商品编辑页库存输入只读、表头批量改库存图标隐藏、列表快捷改库存图标隐藏。
         */
        productEditStockModify: (state: State): number => {
            return state.config.productEditStockModify === 0 ? 0 : 1;
        },
        /**
         * 规格图片可上传范围：1=仅第一组规格（默认）；2=全部规格。
         * 由 getAdminBase 随全局配置一起下发，无需单独调用接口。
         * 为 2 时：商品新增/编辑表单勾选“添加规格图片”后，所有规格组的每个规格值都可上传图片。
         */
        specImageMode: (state: State): number => {
            return state.config.specImageMode === 2 ? 2 : 1;
        }
    },
    actions: {
        /**
         * 从服务器更新配置
         * @description 获取最新的后台配置并更新本地状态
         */
        async updateConfig() {
            try {
                const result = await getAdminBase();
                // 货币中文单位统一以后端全局配置 dollarSignCn 为准（跨境版主货币联动），
                // 兼容历史 amountUnit 字段：dollarSignCn 优先，缺省回退原值
                const merged = Object.assign({}, this.config, result) as AdminConfigState;
                if (result?.dollarSignCn) {
                    merged.amountUnit = result.dollarSignCn;
                }
                this.config = merged;
                localStorage.setItem("config", JSON.stringify(merged));
            } catch (error) {
            }
        },
        /**
         * 设置配置项
         * @param {Partial<AdminConfigState>} data - 要设置的配置数据
         */
        setConfig(data: Partial<AdminConfigState>) {
            this.config = Object.assign({}, this.config, data);
        },
        /**
         * 获取指定配置项
         * @param {keyof AdminConfigState} key - 配置项键名
         * @returns 配置项值
         */
        getConfig(key: keyof AdminConfigState): AdminConfigState[keyof AdminConfigState] {
            return this.config[key];
        },
        /**
         * 获取指定配置项（简写方法）
         * @param {keyof AdminConfigState} key - 配置项键名
         * @returns 配置项值
         */
        get(key: keyof AdminConfigState): AdminConfigState[keyof AdminConfigState] {
            return this.config[key] as AdminConfigState[keyof AdminConfigState];
        }
    },
    persist: {
        enabled: true,
        strategies: [
            { storage: localStorage, paths: ["config"] }  // 使用localStorage持久化存储
        ]
    }
});
