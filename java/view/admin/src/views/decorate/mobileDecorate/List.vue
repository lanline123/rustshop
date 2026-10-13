<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <!-- Tab 切换区域（仅移动端，且未外部固定 pageType 时） -->
                <el-tabs v-if="isMobile && !hasFixedPageType" v-model="activeTab" @tab-change="onTabChange" class="decorate-tabs">
                    <el-tab-pane v-for="tab in tabList" :key="tab.key" :label="tab.label" :name="tab.key" />
                </el-tabs>

                <!-- 旧版个人中心装修：仅在「个人中心页」签内切换显示；tabs 保留，可继续装修其他页面 -->
                <component :is="LegacyMemberDecorate" v-if="showLegacy" @switch-new="onSwitchToNew" />
                <template v-else>
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('输入页面名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <!-- 移动端：直接进装修编辑器（新建模式），编辑器内再弹模板库 -->
                                    <el-button v-if="isMobile" type="primary" @click="onCreatePage">{{ $t('添加页面') }}</el-button>
                                    <!-- PC 端（decorateType=2）：保留原有「添加页面」抽屉表单流程，PC 无模板市场 -->
                                    <DialogForm
                                        v-else
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('添加页面')"
                                        width="600px"
                                        path="decorate/mobileDecorate/Info"
                                        :params="{ act: 'add', decorateType, ...(hasFixedPageType ? { pageType: props.pageType } : {}) }"
                                    >
                                        <el-button type="primary">{{ $t('添加页面') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-button @click="importDecorate">{{ $t('导入装修模板') }}</el-button>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-space>
                                        <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                            <template #reference>
                                                <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                            </template>
                                        </el-popconfirm>
                                        <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                    </el-space>
                                </div>
                            </div>
                            <!-- 个人中心页：切换到旧版会员中心装修入口 -->
                            <div class="simple-form-field switch-legacy-field" v-if="isMobile && activeTab === 'user'">
                                <div class="form-group">
                                    <el-button class="switch-legacy-btn" type="primary" plain @click="onSwitchLegacy">
                                        <el-icon class="switch-legacy-icon"><Switch /></el-icon>
                                        <span>{{ $t('切换旧版') }}</span>
                                        <el-tooltip effect="light" placement="top" :show-after="300">
                                            <template #content>
                                                <div style="width: 260px; padding: 5px 10px; line-height: 1.7">
                                                    {{ $t('系统现已上线个人中心装修新版，功能更丰富、操作更便捷；为保障老用户原有装修模板、操作习惯不受影响，平台保留历史旧版装修入口，您可自由切换新旧版本使用。') }}<br /><br />
                                                    {{ $t('温馨提示：新旧版本装修数据相互独立，切换后不会同步另一版本页面内容。') }}
                                                </div>
                                            </template>
                                            <el-icon class="switch-legacy-help" @click.stop><QuestionFilled /></el-icon>
                                        </el-tooltip>
                                    </el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" @selection-change="onSelectChange" :total="total" @sort-change="onSortChange" :loading="loading">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('页面名称')" prop="decorateId" :width="300">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <el-space>
                                            <el-tooltip
                                                v-if="row.isHome === 1 && pageTypeIconMap[row.pageType ?? 1]"
                                                effect="light"
                                                placement="top"
                                                :content="pageTypeIconMap[row.pageType ?? 1].title"
                                            >
                                                <span class="default-page-icon">
                                                    <SvgIcon :name="pageTypeIconMap[row.pageType ?? 1].icon" />
                                                </span>
                                            </el-tooltip>
                                            <PopForm
                                                :label="$t('页面名称')"
                                                type="textarea"
                                                :requestApi="updateDecorateFiled"
                                                v-model:org-value="row.decorateTitle"
                                                :params="{ id: row.decorateId, field: 'decorateTitle' }"
                                                :max="50"
                                            >
                                                <div>{{ row.decorateTitle }}</div>
                                                <el-tag
                                                    v-if="decorateType === 2 && row.pageType === 1 && row.homeStyleVersion === 'old'"
                                                    class="legacy-tag"
                                                    size="small"
                                                    type="info"
                                                    disable-transitions
                                                >
                                                    旧版
                                                </el-tag>
                                            </PopForm>
                                        </el-space>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('发布状态')" prop="status" sortable="custom" :width="150">
                                <template #default="{ row }">
                                    <span class="text-status" :class="{ success: row.status == 1, default: row.status != 1 }">{{ row.status == 1 ? $t('已发布') : $t('未发布') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('更新时间')" prop="updateTime" sortable="custom" :min-width="180">
                                <template #default="{ row }">
                                    {{ row.updateTime }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="bingdingH5Name" :min-width="180" v-if="decorateType == 1 && activeTab === 'home' && !hasFixedPageType">
                                <template #header>
                                    <div class="flex flex-align-center">
                                        <div>{{ $t('已绑定PC页面') }}</div>
                                        <el-tooltip class="box-item" effect="light" placement="top" show-after="300">
                                            <template #content>
                                                <div style="width: 180px; padding: 5px 10px">{{ $t('识别客户打开的设备，自动跳转至对应设备适配的页面') }}</div>
                                            </template>
                                            <el-icon style="margin-left: 5px" size="14" color="#999">
                                                <QuestionFilled />
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                </template>
                                <template #default="{ row }">
                                    {{ row.bingdingH5Name || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="operationColumnWidth">
                                <template #default="{ row }">
                                    <el-space>
                                        <a class="btn-link" @click="toPage(props.decorateType, 0, row.decorateId, 0, true)">{{ $t('编辑') }}</a>
                                        <el-divider direction="vertical" />
                                        <BindMobilePage
                                            v-if="isMobile && activeTab === 'home' && !hasFixedPageType"
                                            :bindedH5Id="row.bindedH5Id"
                                            :decorate-id="row.decorateId"
                                            type="bindCustom"
                                            @okCallback="loadFilter"
                                        />
                                        <el-divider v-if="isMobile && activeTab === 'home' && !hasFixedPageType" direction="vertical" />
                                        <DialogForm
                                            v-if="row.isHome === 1 && row.pageType !== 3"
                                            @okCallback="loadFilter"
                                            :title="getSwitchDialogTitle(row)"
                                            width="800px"
                                            :showFooter="false"
                                            path="decorate/mobileDecorate/SwitchDefaultPage"
                                            :params="{ currentRow: row, decorateType }"
                                        >
                                            <a class="btn-link">{{ $t('更换') }}</a>
                                        </DialogForm>
                                        <el-divider v-if="row.isHome === 1 && row.pageType !== 3" direction="vertical" />
                                        <el-dropdown trigger="hover" :hide-on-click="false">
                                            <el-icon class="btn-link" size="12"><MoreFilled /></el-icon>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item>
                                                        <a class="btn-link" @click="onCopy(row)">{{ $t('复制') }}</a>
                                                    </el-dropdown-item>
                                                    <el-dropdown-item v-if="row.isHome == 0">
                                                        <DeleteRecord @afterDelete="loadFilter" :requestApi="delDecorate" :params="{ id: row.decorateId }"
                                                            >{{ $t('删除') }}</DeleteRecord
                                                        >
                                                    </el-dropdown-item>
                                                    <el-dropdown-item>
                                                        <a class="btn-link" @click="onShare(row.decorateId)">{{ $t('分享') }}</a>
                                                    </el-dropdown-item>
                                                    <el-dropdown-item v-if="isTemplateExport">
                                                        <a class="btn-link" @click="onExport(row)">{{ $t('导出') }}</a>
                                                    </el-dropdown-item>
                                                    <el-dropdown-item v-if="canPromote(row)">
                                                        <DialogForm
                                                            :title="$t('推广')"
                                                            width="900px"
                                                            :show-footer="false"
                                                            path="decorate/mobileDecorate/Advertise"
                                                            :params="getPromoteParams(row)"
                                                        >
                                                            <a class="btn-link">{{ $t('推广') }}</a>
                                                        </DialogForm>
                                                    </el-dropdown-item>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
                </template>

            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { ref, computed, defineAsyncComponent } from "vue";
import { useRouter } from "vue-router";
import { isOverseas } from "@/utils/version";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination } from "@/components/list";
import { message, Modal } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useDecorateStore } from "@/store/decorate";
import { MoreFilled, QuestionFilled, Switch } from "@element-plus/icons-vue";
import { FilterState, FilterParams } from "@/types/decorate/mobileDecorate.d";
import BindMobilePage from "@/views/decorate/pcCustomPage/src/BindMobilePage.vue";
import {
    getDecorateList,
    batchSubmit,
    updateDecorateFiled,
    delDecorate,
    updateDecorate,
    decorateShare,
    importDecorateLink,
    getDecorate
} from "@/api/decorate/mobileDecorate";
import { getMemberCenterDecorateSetting } from "@/api/setting/config";
import { ElMessageBox } from "element-plus";
import { copyDomText } from "@/utils/util";
import { baseDirFormat } from "@/utils/format";
import { useListRequest } from "@/hooks/useListRequest";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = defineProps({
    decorateType: { type: Number, default: 1 },
    // 外部固定 pageType：>0 表示只展示/操作该页面类型，隐藏 Tab 并将该 pageType 透传到请求、添加、复制、推广、编辑器跳转
    pageType: { type: Number, default: 0 }
});
const config: any = useConfigStore();
const { setDecorateId, setLocaleId, setIsDefault, resetDecorateInfo } = useDecorateStore();

const decorateType = props.decorateType;
const isMobile = decorateType === 1;
const isTemplateExport = import.meta.env.VITE_IS_TEMPLATE_EXPORT || 0;
// 是否由外部固定了 pageType（PC 搜索页装修等场景走这条路径）
const hasFixedPageType = props.pageType > 0;

// ===================== Tab 定义 =====================
interface TabItem {
    key: string;
    label: string;
    pageType: number;
}

const tabList: TabItem[] = [
    { key: "home", label: t('商城首页'), pageType: 1 },
    { key: "classify", label: t('分类页'), pageType: 4 },
    { key: "user", label: t('个人中心页'), pageType: 2 },
    { key: "activity", label: t('自定义活动页'), pageType: 3 },
    { key: "login", label: t('登录页'), pageType: 5 },
    { key: "search", label: t('搜索页'), pageType: 6 },
    { key: "productDetail", label: t('商品详情页'), pageType: 8 }
];

const TAB_PAGE_TYPE_MAP: Record<string, number> = {
    home: 1,
    classify: 4,
    user: 2,
    activity: 3,
    login: 5,
    search: 6,
    productDetail: 8
};

const router = useRouter();
const route = router.currentRoute.value;
const initialTab = (() => {
    if (!isMobile) return "home";
    const t = route.query.tab as string;
    return t && TAB_PAGE_TYPE_MAP[t] ? t : "home";
})();
const activeTab = ref<string>(initialTab);

// ===================== 旧版个人中心装修（页面内切换，新旧版开关由后端持久化） =====================
// userOverseas/User.vue 在非跨境版打包时会被 feature-manifest 剥离，动态 import 必须用条件编译隔离，
// 否则非跨境版构建会因解析不到已删除的文件而失败
const loadLegacyMemberDecorate = () => {
    return import("./user/User.vue");
};
const LegacyMemberDecorate = defineAsyncComponent(loadLegacyMemberDecorate);
// useNewMemberCenter：1=新版 0=旧版（C 端实际使用版本，仅在「保存/发布」后更新）
const useNewMemberCenter = ref(1);
// legacyViewChoice：用户本会话手动选择的编辑视图（true=旧版 false=新版 null=跟随后端使用版本）
const legacyViewChoice = ref<boolean | null>(null);
// showLegacy 控制当前「编辑视图」：用户手动切换过则保持其选择（切换其他页签来回也不丢），否则跟随后端使用版本
const showLegacy = computed(() => {
    if (!isMobile || activeTab.value !== "user") return false;
    return legacyViewChoice.value !== null ? legacyViewChoice.value : useNewMemberCenter.value === 0;
});

// 读取新旧版开关，并按当前使用版本决定初始编辑视图（仅个人中心页签）
const fetchMemberCenterSwitch = async () => {
    try {
        const res: any = await getMemberCenterDecorateSetting();
        useNewMemberCenter.value = Number(res?.useNewMemberCenter ?? 1);
    } catch (error) {
        useNewMemberCenter.value = 1;
    }
};

// 优先使用外部固定 pageType；否则移动端跟随 Tab；PC 端不带 pageType
const currentPageType = computed(() => {
    if (hasFixedPageType) return props.pageType;
    if (isMobile) return TAB_PAGE_TYPE_MAP[activeTab.value] ?? 1;
    return undefined;
});

const pageTypeIconMap: Record<number, { icon: string; title: string }> = {
    1: { icon: "decorate-home", title: t('首页') },
    2: { icon: "decorate-user", title: t('个人中心') },
    3: { icon: "decorate-activity", title: t('活动页') },
    4: { icon: "decorate-classify", title: t('分类页') },
    5: { icon: "decorate-login", title: t('登录页') },
    6: { icon: "decorate-sousuo", title: t('搜索页') },
    8: { icon: "decorate-classify", title: t('商品详情页') }
};

const switchDialogTitleMap: Record<number, string> = {
    1: t('更换首页'),
    2: t('更换个人中心'),
    3: t('更换活动页'),
    4: t('更换分类页'),
    5: t('更换登录页'),
    6: t('更换搜索页'),
    8: t('更换商品详情页')
};

const getSwitchDialogTitle = (row: FilterState) => switchDialogTitleMap[row.pageType ?? 1] ?? t('更换页面');

const operationColumnWidth = computed(() => {
    if (isMobile && activeTab.value === "home") return 340;
    return 260;
});

// ===================== 列表请求 =====================
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction
} = useListRequest<FilterState, FilterParams>({
    apiFunction: getDecorateList,
    idKey: "decorateId",
    defaultParams: {
        page: 1,
        decorateType: props.decorateType,
        ...(hasFixedPageType ? { pageType: props.pageType } : isMobile ? { pageType: TAB_PAGE_TYPE_MAP[initialTab] ?? 1 } : {}),
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: ""
    }
});

const onTabChange = (tab: string | number) => {
    const tabKey = String(tab);
    filterParams.pageType = TAB_PAGE_TYPE_MAP[tabKey] ?? 1;
    filterParams.page = 1;
    filterParams.keyword = "";
    selectedIds.value = [];
    loadFilter();
    // 进入个人中心页时读取后端使用版本（用户本会话手动切换的视图选择会保持）
    if (tabKey === "user") fetchMemberCenterSwitch();
};

const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

loadFilter();
// 初始进入个人中心页时读取新旧版开关
if (isMobile && initialTab === "user") fetchMemberCenterSwitch();

// ===================== 推广参数 =====================
/** 按 decorateType + pageType 取前端路由路径 */
const getPath = (pageType: number): string => {
    const pathMapByDecorateType: Record<number, Record<number, string>> = {
        1: {
            1: "/",
            2: "/pages/member/index",
            3: "/pages/activity/index",
            4: "/pages/productCate/index",
            5: "/pages/login/index",
            6: "/pages/search/index",
            8: "/pages/product/index"
        },
        2: {
            1: "/",
            3: "/activity"
        }
    };
    return pathMapByDecorateType[props.decorateType]?.[pageType] ?? "";
};

/** 按 row.pageType 取页面标题 */
const promoteTitleMap: Record<number, string> = {
    1: t('商城首页'),
    2: t('个人中心'),
    3: t('自定义活动页'),
    4: t('分类页'),
    5: t('登录页'),
    6: t('搜索页'),
    8: t('商品详情页')
};
const getPromoteTitle = (row: FilterState): string => promoteTitleMap[row.pageType ?? 1] ?? row.decorateTitle ?? "";

/** 推广弹窗参数 */
const getPromoteParams = (row: FilterState) => ({
    decorateItem: row,
    decorateType: props.decorateType,
    toPath: getPath(row.pageType ?? 1),
    title: getPromoteTitle(row)
});

// ===================== 分享 & 导入 =====================
/** 是否允许推广：已发布 + 页面类型在白名单内 */
const canPromote = (row: FilterState) => {
    if (Number(row?.status) !== 1) return false;
    const pageType = row.pageType ?? 0;
    if (props.decorateType === 2) {
        return [1, 3].includes(pageType);
    }
    return [1, 2, 3, 4, 5, 6, 8].includes(pageType);
};
const onShare = async (id: number) => {
    loading.value = true;
    try {
        const result = await decorateShare({ decorateId: id });
        copyDomText(`${window.location.protocol}//${window.location.host.split(":")[0]}${result.apiUrl}`);
        message.success(t('分享链接已经复制到剪贴板, 导入时可直接粘贴使用'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onExport = async (row: FilterState) => {
    loading.value = true;
    try {
        const result = await getDecorate("detail", {
            id: row.decorateId,
            decorateType: props.decorateType,
            localeId: 0,
            parentId: 0
        });
        const jsonText = JSON.stringify(result, null, 2);
        try {
            await navigator.clipboard.writeText(jsonText);
        } catch {
            copyDomText(jsonText);
        }
        message.success(t('模板详情JSON已复制到剪贴板'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const importDecorate = () => {
    ElMessageBox.prompt("请把分享链接粘贴在此处", t('导入装修链接'), { confirmButtonText: t('确定'), cancelButtonText: t('取消') }).then(({ value }) => {
        _importDecorateLink(value);
    });
};

const _importDecorateLink = async (url: string) => {
    loading.value = true;
    try {
        const urlEncode = decodeURIComponent(url);
        await importDecorateLink({ url: urlEncode });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

// ===================== 复制 =====================
const onCopy = async (row: FilterState) => {
    try {
        await updateDecorate("copy", { id: row.decorateId });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};

// ===================== 切换旧版/新版个人中心装修（仅切换编辑视图，应用版本放到各自保存/发布动作） =====================
const onSwitchLegacy = () => {
    Modal.confirm({
        title: t('确认切换至旧版？'),
        icon: () => null,
        content: t('新旧版本装修数据相互独立，互不兼容。切换至旧版后，请您重新配置并保存装修方案，保存后用户端即可实时同步最新展示效果。'),
        okText: t('确认切换'),
        cancelText: t('取消'),
        onOk() {
            legacyViewChoice.value = true;
        }
    });
};

// 旧版组件触发：切回新版编辑视图
const onSwitchToNew = () => {
    Modal.confirm({
        title: t('确认切换至新版？'),
        icon: () => null,
        content: t('新旧版本装修数据相互独立，互不兼容。切换至新版后，请您重新配置发布装修方案，并把此装修方案更换设为个人中心页，用户端即可实时同步最新展示效果。'),
        okText: t('确认切换'),
        cancelText: t('取消'),
        onOk() {
            legacyViewChoice.value = false;
        }
    });
};

// ===================== 跳转装修编辑器 =====================
const { VITE_BASE_DIR } = import.meta.env;

/** 新建页面（移动端）：直接打开装修编辑器进入新建模式，编辑器内自动弹出对应页面类型的模板库 */
const onCreatePage = () => {
    resetDecorateInfo();
    setIsDefault(true);
    const pageType = currentPageType.value ?? 1;
    window.open(`${baseDirFormat(VITE_BASE_DIR)}/decorate/index?act=add&pageType=${pageType}`, "_blank");
};

const toPage = (type: number, parentId: number, id: number, localeId: number, isDefault: boolean) => {
    resetDecorateInfo();
    setDecorateId(parentId || id || 0);
    setLocaleId(localeId);
    setIsDefault(isDefault);
    const pageTypeQuery = currentPageType.value ? `&pageType=${currentPageType.value}` : "";
    if (type === 1) {
        window.open(`${baseDirFormat(VITE_BASE_DIR)}/decorate/index?id=${id}${pageTypeQuery}`, "_blank");
    } else {
        // PC 端固定 pageType（搜索页 6、登录页 5 等）必须带上 pageType，否则编辑器按 pageType=1 首页处理
        if (currentPageType.value) {
            window.open(`${baseDirFormat(VITE_BASE_DIR)}/decorate/pc?id=${id}${pageTypeQuery}`, "_blank");
        } else {
            window.open(`${baseDirFormat(VITE_BASE_DIR)}/decorate/pc?id=${id}`, "_blank");
        }
    }
};
</script>
<style lang="less" scoped>
.decorate-tabs {
    margin-bottom: 16px;
    :deep(.el-tabs__header) {
        margin-bottom: 0;
    }
}
.switch-legacy-field {
    margin-left: auto;
}
.switch-legacy-btn {
    display: inline-flex;
    align-items: center;
    .switch-legacy-icon {
        margin-right: 4px;
    }
    .switch-legacy-help {
        margin-left: 6px;
        font-size: 14px;
        cursor: help;
    }
}
.default-page-icon {
    display: inline-flex;
    align-items: center;
    color: var(--el-color-primary);
    :deep(.icon) {
        width: 18px;
        height: 18px;
    }
}

.legacy-tag {
    background-color: #fafafa;
    border-color: transparent;
    color: #8c8c8c;
    font-size: 11px;
    padding: 0 6px;
    height: 18px;
    line-height: 18px;
    border-radius: 2px;
    font-weight: 500;
    transition: none;
    animation: none;
    margin-left: 4px;
}

.text-status {
    &.success {
        &:before {
            content: "";
            display: inline-block;
            width: 6px;
            height: 6px;
            background-color: #52c41a;
            border-radius: 50%;
            margin-right: 4px;
            transform: translateY(-2px);
        }
    }
    &.default {
        &:before {
            content: "";
            display: inline-block;
            width: 6px;
            height: 6px;
            background-color: #929292;
            border-radius: 50%;
            margin-right: 4px;
            transform: translateY(-2px);
        }
    }
}
</style>
