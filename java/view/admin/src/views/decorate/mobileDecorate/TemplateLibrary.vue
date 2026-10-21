<template>
    <div class="template-library" :class="{ 'template-library--nav': isNav }">
        <!-- 自定义头部：包含搜索框与关闭按钮 -->
        <div class="tpl-header">
            <div class="header-left">
                <div class="tpl-search">
                    <TigInput v-model="keyword" name="keyword" :placeholder="$t('输入模板编码')" clearable @keyup.enter="onSearch" @clear="onSearch">
                        <template #append>
                            <el-button @click="onSearch">
                                <span class="iconfont icon-chakan1"></span>
                            </el-button>
                        </template>
                    </TigInput>
                </div>
            </div>
            <div class="header-right">
                <span class="close-btn" @click="onClose">
                    <span class="iconfont icon-guanbi"></span>
                </span>
            </div>
        </div>

        <!-- 灰色背景间隔条（完美还原设计图的浅灰色色块间距） -->
        <div class="tpl-divider-bar"></div>

        <!-- 筛选条件区域：分类接口请求完成后再整体渲染，避免加载中先显示「全部」再突变 -->
        <div v-if="!categoryLoading" class="tpl-filters-wrapper">
            <div class="tpl-filters">
                <div class="tpl-filter-row">
                    <span class="tpl-filter-label">{{ $t('行业：') }}</span>
                    <div class="tpl-filter-options">
                        <span
                            v-for="item in industries"
                            :key="item.categoryId"
                            :class="['tpl-filter-tag', { active: industryId === item.categoryId }]"
                            @click="onSelectIndustry(item.categoryId)"
                            >{{ item.categoryName }}</span
                        >
                    </div>
                </div>
                <div class="tpl-filter-row">
                    <span class="tpl-filter-label">{{ $t('节日：') }}</span>
                    <div class="tpl-filter-options">
                        <span
                            v-for="item in festivals"
                            :key="item.categoryId"
                            :class="['tpl-filter-tag', { active: festivalId === item.categoryId }]"
                            @click="onSelectFestival(item.categoryId)"
                            >{{ item.categoryName }}</span
                        >
                    </div>
                </div>
                <div class="tpl-filter-row tpl-filter-row--color">
                    <span class="tpl-filter-label">{{ $t('颜色：') }}</span>
                    <div class="tpl-filter-options tpl-color-options">
                        <span
                            v-for="item in colors"
                            :key="item.categoryId"
                            :class="['tpl-color-dot', { active: colorId === item.categoryId, 'is-all': item.categoryId === 0 }]"
                            :style="item.categoryId ? { backgroundColor: item.color } : {}"
                            :title="item.categoryName"
                            @click="onSelectColor(item.categoryId)"
                        >
                            <!-- 选中时显示设计图对勾（直接使用图片资源） -->
                            <img v-if="colorId === item.categoryId" class="tpl-color-check" :src="colorCheckedIcon" alt="" />
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 可滚动的内容区域 -->
        <div class="tpl-body-scroll">
            <a-spin :spinning="loading">
                <!-- 加载中仅显示 spinner，「新建空白模板」与模板卡片均等加载完成后再渲染 -->
                <div class="tpl-grid">
                    <template v-if="!loading">
                        <!-- 新建空白模板卡片：list 场景(PC)走 Info 表单创建 -->
                        <DialogForm
                            v-if="props.scene === 'list'"
                            class="tpl-card tpl-card-blank"
                            :title="$t('新建页面')"
                            width="600px"
                            path="decorate/mobileDecorate/Info"
                            :params="{ act: 'add', decorateType, pageType }"
                            @okCallback="onBlankCreated"
                        >
                            <div class="tpl-blank-inner">
                                <img class="tpl-blank-plus" :src="blankAddIcon" alt="" />
                                <span class="tpl-blank-text">{{ $t('新建空白模板') }}</span>
                            </div>
                        </DialogForm>
                        <!-- 新建空白模板卡片：editor 场景(移动新建)仅通知编辑器进入空白态，不创建 -->
                        <div v-else class="tpl-card tpl-card-blank" @click="onSelectBlank">
                            <div class="tpl-blank-inner">
                                <img class="tpl-blank-plus" :src="blankAddIcon" alt="" />
                                <span class="tpl-blank-text">{{ $t('新建空白模板') }}</span>
                            </div>
                        </div>

                        <!-- 模板卡片 -->
                        <div v-for="tpl in list" :key="tpl.pageSn" class="tpl-card">
                            <div class="tpl-thumb" :style="getThumbStyle(tpl)">
                                <!-- 悬浮遮罩与使用按钮 -->
                                <div class="tpl-card-mask">
                                    <el-button class="btn-use-template" type="primary" @click.stop="onUseTemplate(tpl)">{{ $t('免费使用') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </a-spin>

            <!-- 授权异常引导态：接口未获授权许可时展示，引导前往运营中心填写授权 -->
            <div v-if="authError && !loading" class="tpl-auth-error">
                <img class="tpl-auth-icon" :src="authorizationIcon" alt="" />
                <p class="tpl-auth-text">
                    未正确填写授权码，无法获取装修主题模板数据，请前往运营中心填写<a class="tpl-auth-link" @click="onGotoAuthorization">（去配置）</a>
                </p>
            </div>
        </div>

        <!-- 底部固定分页栏 -->
        <div class="tpl-footer">
            <div v-if="total > 0" class="tpl-pagination">
                <Pagination v-model:page="page" v-model:size="size" :total="total" layout="total, prev, pager, next" @callback="loadList" />
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { getTemplateCategory, getTemplateList, getTemplateDetail } from "@/api/decorate/mobileDecorate";
import blankAddIcon from "@/assets/icons/decorate/blank-add.png";
import colorCheckedIcon from "@/assets/icons/decorate/color-checked.png";
import authorizationIcon from "@/assets/decorate/authorization.png";

// 授权异常引导跳转地址（运营中心 - 我的授权）
const AUTHORIZATION_URL = "https://www.tigshop.com/download/authorization/list";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    decorateType: { type: Number, default: 1 },
    pageType: { type: Number, default: 1 },
    isDialog: Boolean,
    // 'list' = 列表页(PC)旧逻辑：空白卡走 Info 表单创建；'editor' = 编辑器内新建：空白卡仅选择不创建
    scene: { type: String, default: "list" }
});

const emit = defineEmits(["submitCallback", "close"]);

/** 分类维度 / 颜色筛选项结构（官网 camelCase 字段） */
interface FilterOption {
    categoryId: number;
    categoryName: string;
    color?: string;
}

/** 模板页面项（官网「页面」维度，camelCase 字段） */
interface TemplatePageItem {
    pageSn: string;
    pageType: string;
    pageName: string;
    exampleImg: string;
    coverImg: string;
    templateName: string;
    [key: string]: any;
}

// 官网分类维度一级 categoryId（页面=1 不参与筛选，改用 pageType 枚举）
const DIMENSION_INDUSTRY = 6;
const DIMENSION_FESTIVAL = 19;
const DIMENSION_COLOR = 28;

// List.vue Tab 数字 pageType → 官网 pageType 枚举（自定义活动页(3)复用首页模板；导航 7→nav）
// 未映射的页面类型会落到 home，独占单模块页（搜索页 6）必须显式映射，否则会误列出首页模板
const PAGE_TYPE_MAP: Record<number, string> = {
    1: "home",
    2: "member",
    3: "home",
    4: "category",
    5: "login",
    6: "search",
    7: "nav"
};
// 店铺装修记录仍使用 pageType=1；模板市场沿用同一个 pageType 参数，
// 通过现有 shopId 上下文将首页模板筛选为店铺分类，不新增 scene 参数。
const shopId = localStorage.getItem("shopId");
const isShopContext = !!shopId && shopId !== "0";
const apiPageType = isShopContext && props.pageType === 1 ? "shop" : PAGE_TYPE_MAP[props.pageType] ?? "home";
// 导航场景(pageType=7 → nav)：缩略图为底部导航条，需更少列数 + 更矮卡片，区别于整页模板
const isNav = apiPageType === "nav";

const industries = ref<FilterOption[]>([{ categoryId: 0, categoryName: t('全部') }]);
const festivals = ref<FilterOption[]>([{ categoryId: 0, categoryName: t('全部') }]);
const colors = ref<FilterOption[]>([{ categoryId: 0, categoryName: t('全部'), color: "" }]);

// 分类筛选项加载态：为 true 时整个筛选区域不渲染，待分类接口返回后再一次性展示，避免加载中先显示「全部」再突变
const categoryLoading = ref(true);

const keyword = ref("");
const industryId = ref<number>(0);
const festivalId = ref<number>(0);
const colorId = ref<number>(0);
const page = ref(1);
// 每页固定展示 3 行，并为「新建空白模板」卡片预留一个网格位置
const columnCount = isNav ? 4 : 6;
const size = ref(columnCount * 3 - 1);
const loading = ref(false);
const list = ref<TemplatePageItem[]>([]);
const total = ref(0);
// 授权异常态：接口文案含「授权列表」时展示授权配置引导，替代模板列表
const authError = ref(false);

/** 模板市场并发请求失败时共用同一 key，避免 category/list 同时报错弹出两条相同提示 */
const showTemplateError = (error: any, fallback: string) => {
    message.error({
        content: error?.message || fallback,
        key: "template-market"
    });
};

/** 授权异常判定：仅接口文案含「授权列表 / 授权码」才视为未正确填写授权码，走授权引导态；其余按普通错误提示，避免误判与掩盖真实故障 */
const isAuthorizationError = (error: any) => {
    const msg = typeof error?.message === "string" ? error.message : "";
    return msg.includes("授权列表") || msg.includes("授权码");
};

/** 加载分类树，填充行业 / 节日 / 颜色筛选项 */
const loadCategory = async () => {
    categoryLoading.value = true;
    try {
        const res: any = await getTemplateCategory();
        const result: any[] = res?.filterResult ?? [];
        const childrenOf = (dimId: number) => result.find((d) => d.categoryId === dimId)?.children ?? [];
        industries.value = [{ categoryId: 0, categoryName: t('全部') }, ...childrenOf(DIMENSION_INDUSTRY)];
        festivals.value = [{ categoryId: 0, categoryName: t('全部') }, ...childrenOf(DIMENSION_FESTIVAL)];
        colors.value = [{ categoryId: 0, categoryName: t('全部'), color: "" }, ...childrenOf(DIMENSION_COLOR)];
    } catch (error: any) {
        // 授权异常统一交由列表接口以引导态呈现，此处仅提示非授权类错误，避免与授权引导重复
        if (!isAuthorizationError(error)) {
            showTemplateError(error, t('获取模板分类失败'));
        }
    } finally {
        // 无论成功失败都结束加载态并展示筛选区：成功展示真实分类，失败降级为仅「全部」
        categoryLoading.value = false;
    }
};

/** 加载模板页面列表（后端分页 + 按页面类型 / 分类 / 关键字筛选） */
const loadList = async () => {
    loading.value = true;
    authError.value = false;
    try {
        // 多个分类 id 为「同时满足」的与关系，逗号拼接；0 表示「全部」不参与筛选
        const categoryIds = [industryId.value, festivalId.value, colorId.value].filter((id) => id > 0).join(",");
        const res: any = await getTemplateList({
            keyword: keyword.value.trim(),
            pageType: apiPageType,
            categoryIds,
            page: page.value,
            size: size.value
        });
        list.value = res?.filterResult ?? [];
        total.value = res?.total ?? 0;
    } catch (error: any) {
        list.value = [];
        total.value = 0;
        // 仅「未填授权码」类错误展示授权引导，其余错误照常提示，避免误导与掩盖真实故障
        if (isAuthorizationError(error)) {
            authError.value = true;
        } else {
            showTemplateError(error, "获取模板列表失败");
        }
    } finally {
        loading.value = false;
    }
};

/** 跳转运营中心授权列表页 */
const onGotoAuthorization = () => {
    window.open(AUTHORIZATION_URL, "_blank");
};

const resetAndLoad = () => {
    page.value = 1;
    loadList();
};

const onSelectIndustry = (id: number) => {
    industryId.value = id;
    resetAndLoad();
};

const onSelectFestival = (id: number) => {
    festivalId.value = id;
    resetAndLoad();
};

const onSelectColor = (id: number) => {
    colorId.value = id;
    resetAndLoad();
};

const onSearch = () => {
    resetAndLoad();
};

const getThumbStyle = (tpl: TemplatePageItem) => {
    // 官网已返回图片完整 URL，优先用页面示例图，回退主模板封面图
    const img = tpl.exampleImg || tpl.coverImg;
    if (img) {
        return { backgroundImage: `url(${img})` };
    }
    return { background: "#f7f8fa" };
};

/** 关闭弹窗 */
const onClose = () => {
    emit("close");
};

/** list 场景：新建空白模板成功（Info 表单创建后）关闭模板库并通知列表刷新 */
const onBlankCreated = (result: any) => {
    emit("submitCallback", result);
};

/** editor 场景：选「新建空白」——仅通知编辑器进入空白态，不创建记录 */
const onSelectBlank = () => {
    emit("submitCallback", { blank: true });
};

/**
 * 使用该模板：拉取该页装修详情，剥离官网外层后取最内层 { pageModule, moduleList } 回传。
 * editor 场景由编辑器填充画布，落库走编辑器首次保存/预览时的 create + saveDraft/publish。
 */
const onUseTemplate = async (tpl: TemplatePageItem) => {
    loading.value = true;
    try {
        const res: any = await getTemplateDetail(tpl.pageSn);
        const item = res?.item ?? {};
        let rawJson = item.json;
        if (!rawJson && item.content) {
            try {
                rawJson = JSON.parse(item.content);
            } catch {
                rawJson = {};
            }
        }
        // 官网结构 { decorateId, decorateTitle, data: { pageModule, moduleList } }，编辑器只需最内层 data
        const json = rawJson?.data ?? rawJson ?? {};
        emit("submitCallback", { useTemplate: true, template: { ...tpl, json } });
    } catch (error: any) {
        showTemplateError(error, t('获取模板详情失败'));
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadCategory();
    loadList();
});

// 弹窗无底部确认按钮，提供空实现兼容 DialogForm 的调用约定
const onFormSubmit = () => {};
defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.template-library {
    display: flex;
    flex-direction: column;
    height: 82vh; /* 弹窗高度自适应屏幕，防止超出页面 */
    max-height: 850px;
    min-height: 650px;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-sizing: border-box;

    /* 1. 头部样式 */
    .tpl-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 14px 16px; /* 统一调整为 16px padding */
        background: #fff;
        flex-shrink: 0;

        .header-left {
            display: flex;
            align-items: center;
        }

        .tpl-search {
            width: 260px;

            :deep(.el-input-group__append) {
                background-color: #fff;
                border-left: none;
                padding: 0 12px;

                .el-button {
                    border: none;
                    background: transparent;
                    padding: 0;
                    margin: 0;
                    height: auto;

                    .iconfont {
                        font-size: 16px;
                        color: #86909c;
                    }
                }
            }
        }

        .close-btn {
            cursor: pointer;
            color: #86909c;
            transition: all 0.2s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 28px;
            height: 28px;
            border-radius: 4px;

            &:hover {
                background-color: #f2f3f5;
                color: #1d2129;
            }

            .iconfont {
                font-size: 18px;
            }
        }
    }

    /* 2. 浅灰色背景间隔条（完美还原设计图中的浅灰色背景条） */
    .tpl-divider-bar {
        height: 10px;
        background-color: #f7f8fa; /* 完美的浅灰间隔色 */
        width: 100%;
        flex-shrink: 0;
    }

    /* 3. 筛选条件区域 */
    .tpl-filters-wrapper {
        padding: 16px 16px; /* 统一调整为 16px padding */
        background: #fff;
        flex-shrink: 0;
    }

    .tpl-filters {
        display: flex;
        flex-direction: column;
        gap: 18px; /* 增加行间距 */

        .tpl-filter-row {
            display: flex;
            align-items: flex-start; /* 完美对齐：确保换行时 label 依然与 options 的第一行顶部对齐 */
            font-size: 14px;
        }

        /* 颜色行单行不换行，label 需与 30px 圆点按交叉轴垂直居中（区别于行业/节日的顶部对齐） */
        .tpl-filter-row--color {
            align-items: center;
        }

        .tpl-filter-label {
            flex: 0 0 54px;
            color: #86909c;
            font-weight: 400;
            line-height: 26px; /* 完美对齐：与 options 第一行的高度基准对齐 */
        }

        .tpl-filter-options {
            display: flex;
            flex-wrap: wrap;
            gap: 12px 6px;
            align-items: center;
            flex: 1;
        }

        .tpl-filter-tag {
            padding: 3px 14px;
            font-size: 14px;
            color: #4e5969;
            cursor: pointer;
            border-radius: 2px;
            border: 1px solid transparent;
            transition: all 0.15s ease;
            line-height: 20px;

            &:hover {
                color: var(--el-color-primary); /* 使用 Element Plus 规定的项目主题色 */
            }

            &.active {
                /* 选中态：主题色浅底 + 主题色描边 + 主题色文字（描边风格，浅色填充跟随主题色） */
                color: var(--el-color-primary);
                background-color: var(--el-color-primary-light-9);
                border-color: var(--el-color-primary);
                font-weight: 500;
            }
        }

        .tpl-color-options {
            gap: 16px; /* 颜色圆点之间的水平间距 */
            display: flex;
            align-items: center;
        }

        .tpl-color-dot {
            width: 30px; /* 增大尺寸，完全对齐设计图 */
            height: 30px; /* 增大尺寸，完全对齐设计图 */
            border-radius: 50%;
            cursor: pointer;
            box-sizing: border-box;
            position: relative;
            transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
            display: flex;
            align-items: center;
            justify-content: center;

            /* 选中时的虚线外圈，完美还原设计图 */
            &::after {
                content: "";
                position: absolute;
                inset: -3px;
                border: 1px dashed transparent;
                border-radius: 50%;
                transition: all 0.2s ease;
            }

            &.is-all {
                background: conic-gradient(
                    from 180deg at 50% 50%,
                    #ff0000 0deg,
                    #ff9900 36.35deg,
                    #ccff00 70.96deg,
                    #33ff00 107.31deg,
                    #00ff66 143.65deg,
                    #00ffff 181.73deg,
                    #0066ff 214.62deg,
                    #3300ff 252.69deg,
                    #cc00ff 289.04deg,
                    #ff0099 323.65deg,
                    #ff0000 360deg
                );
            }

            &:hover {
                transform: scale(1.1);
            }

            /* 选中对勾：直接使用设计图图片资源 */
            .tpl-color-check {
                width: 14px;
                height: 12px;
                z-index: 2;
            }
        }
    }

    /* 4. 可滚动的模板网格区域 */
    .tpl-body-scroll {
        flex: 1;
        overflow-y: auto;
        padding: 12px 10px 24px 16px; /* 左右 padding 调整为 16px */
        position: relative; /* 作为授权异常引导态的定位容器 */

        /* 美化滚动条 */
        &::-webkit-scrollbar {
            width: 6px;
        }
        &::-webkit-scrollbar-thumb {
            background-color: #e5e6eb;
            border-radius: 3px;
        }
        &::-webkit-scrollbar-track {
            background: transparent;
        }
    }

    .tpl-grid {
        display: grid;
        grid-template-columns: repeat(6, 1fr);
        gap: 16px; /* 模板之间的间距调整为 16px */
        min-height: 320px; /* 加载中网格内容隐藏，保留高度让 spinner 居中显示 */
    }

    /* 授权异常引导态：居中于内容区，盾牌图标 + 引导文案 + 配置跳转链接 */
    .tpl-auth-error {
        position: absolute;
        top: 30%;
        left: 50%;
        transform: translate(-50%, -50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        pointer-events: none; /* 仅链接可点击，避免遮挡「新建空白模板」卡片 */

        .tpl-auth-icon {
            width: 200px;
            height: auto;
            margin-bottom: 8px;
        }

        .tpl-auth-text {
            margin: 0;
            font-size: 14px;
            color: #86909c;
            line-height: 22px;
            transform: translateY(-40px);
        }

        .tpl-auth-link {
            color: var(--el-color-primary);
            cursor: pointer;
            pointer-events: auto; /* 恢复链接可点击 */
        }
    }

    /* 5. 模板卡片样式 */
    .tpl-card {
        display: flex;
        flex-direction: column;
        height: 320px;
        border-radius: 4px;
        overflow: hidden;
        cursor: pointer;
        background: #fff;
        transition: all 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
        box-sizing: border-box;
        border: 1px solid #f2f2f2;
        transition: 0.3s ease-in-out;

        &:hover {
            transform: translateY(-8px);

            .tpl-card-mask {
                opacity: 1;
            }
        }
    }

    /* 新建空白模板卡片触发器容器样式 */
    :deep(.tpl-card-blank) {
        background: #f7f8fa;
        display: flex;
        flex-direction: column;
        height: 320px;
        border-radius: 4px;
        overflow: hidden;
        cursor: pointer;
        box-sizing: border-box;
        border: 1px solid #f2f2f2;
        transition: 0.3s ease-in-out;

        &:hover {
            transform: translateY(-8px);
        }

        .tpl-blank-inner {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
            width: 100%;
            position: relative;
        }

        .tpl-blank-plus {
            width: 40px;
            height: 40px;
            margin-bottom: 16px;
        }

        .tpl-blank-text {
            font-size: 12px;
            color: #4e5969;
            font-weight: 500;
            color: #666666;
        }
    }

    .tpl-thumb {
        position: relative;
        flex: 1;
        display: flex;
        flex-direction: column;
        background-size: cover;
        background-position: top center;
        background-repeat: no-repeat;
        overflow: hidden;
        background-color: #f7f8fa;
    }

    /* 悬浮遮罩 */
    .tpl-card-mask {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(29, 33, 41, 0.35); /* 稍微降低遮罩暗度，使按钮更突出 */
        opacity: 0;
        transition: opacity 0.2s ease;
        z-index: 3;
    }

    /* 新建空白模板卡片：遮罩仅暗化背景，按钮浮于遮罩之上，避免被半透明遮罩压暗 */
    :deep(.tpl-card-blank) {
        .tpl-card-mask {
            z-index: 1;
        }

        .tpl-blank-btn {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 2;
            opacity: 0;
            transition: opacity 0.2s ease;
        }

        &:hover .tpl-blank-btn {
            opacity: 1;
        }
    }

    .btn-use-template {
        width: 150px; /* 宽度对齐设计图 */
        height: 38px; /* 高度对齐设计图 */
        padding: 0;
        font-size: 14px; /* 字体稍微调大，更显大气 */
        font-weight: 500;
        border-radius: 4px; /* 改为设计图中的直角偏圆角（4px），而不是胶囊圆角 */
        box-shadow: 0 4px 10px rgba(22, 119, 255, 0.3);
        transition: all 0.2s ease;
        text-align: center;
        border: none;
    }

    /* 6. 底部固定分页栏 */
    .tpl-footer {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        padding: 16px 16px; /* 统一调整为 16px padding */
        border-top: 1px solid #f2f3f5;
        background: #fff;
        flex-shrink: 0;
    }

    /* 7. 导航场景(pageType=7)：缩略图为底部导航条，改用更少列数 + 更矮卡片 */
    &.template-library--nav {
        .tpl-grid {
            grid-template-columns: repeat(4, 1fr);
        }

        .tpl-card {
            height: 200px;
        }

        :deep(.tpl-card-blank) {
            height: 200px;
        }

        .tpl-thumb {
            background-size: 100% auto;
            background-position: bottom center;
        }
    }
}
</style>
