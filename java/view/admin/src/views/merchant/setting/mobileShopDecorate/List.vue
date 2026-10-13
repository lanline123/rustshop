<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeName" class="demo-tabs">
                <el-tab-pane :label="$t('店铺页面')" :name="1">
                    <div class="lyecs-table-list-warp">
                        <div class="list-table-tool lyecs-search-warp">
                            <el-form :model="filterParams">
                                <div class="advanced-search-warp list-table-tool-row">
                                    <div class="simple-form-warp">
                                        <div class="simple-form-field">
                                            <div class="form-group">
                                                <div class="control-container">
                                                    <TigInput
                                                        v-model="filterParams.keyword"
                                                        name="keyword"
                                                        :placeholder="$t('输入页面名称')"
                                                        @keyup.enter="onSearchSubmit"
                                                    >
                                                        <template #append>
                                                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                        </template>
                                                    </TigInput>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="simple-form-field">
                                            <div class="form-group">
                                                <el-button type="primary" @click="onCreatePage">{{ $t('添加页面') }}</el-button>
                                            </div>
                                        </div>
                                        <div class="simple-form-field">
                                            <div class="form-group">
                                                <el-button @click="importDecorate">{{ $t('导入装修链接') }}</el-button>
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
                                    </div>
                                </div>
                            </el-form>
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
                                                        v-if="row.isHome === 1"
                                                        effect="light"
                                                        placement="top"
                                                        :content="$t('首页')"
                                                    >
                                                        <span class="default-page-icon">
                                                            <SvgIcon name="decorate-home" />
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
                                                    </PopForm>
                                                </el-space>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('发布状态')" prop="status" sortable="custom" :width="150">
                                        <template #default="{ row }">
                                            <span class="text-status" :class="{ success: row.status == 1, default: row.status != 1 }">
                                                {{ row.status == 1 ? $t('已发布') : $t('未发布') }}
                                            </span>
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('更新时间')" prop="updateTime" sortable="custom">
                                        <template #default="{ row }">
                                            {{ row.updateTime }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('操作')" fixed="right" width="220">
                                        <template #default="{ row }">
                                            <el-space :size="0">
                                                <a class="btn-link" @click="toPage(props.decorateType, 0, row.decorateId, 0, true)">{{ $t('编辑') }}</a>
                                                <template v-if="row.isHome == 1">
                                                    <el-divider direction="vertical" />
                                                    <DialogForm
                                                        :title="$t('更换首页')"
                                                        width="800px"
                                                        :showFooter="false"
                                                        path="decorate/mobileDecorate/SwitchDefaultPage"
                                                        :params="{ currentRow: row, decorateType: props.decorateType }"
                                                        @okCallback="loadFilter"
                                                    >
                                                        <a class="btn-link">{{ $t('更换') }}</a>
                                                    </DialogForm>
                                                </template>
                                                <template v-if="row.isHome == 0">
                                                    <el-divider direction="vertical" />
                                                    <a class="btn-link" @click="onCopy(row)">{{ $t('复制') }}</a>
                                                </template>
                                                <el-divider direction="vertical" />
                                                <el-dropdown trigger="hover" :hide-on-click="false">
                                                    <el-icon class="btn-link" size="12">
                                                        <MoreFilled />
                                                    </el-icon>
                                                    <template #dropdown>
                                                        <el-dropdown-menu>
                                                            <el-dropdown-item v-if="row.isHome == 1">
                                                                <el-button style="width: 100%" link type="primary" @click="onCopy(row)">{{ $t('复制') }}</el-button>
                                                            </el-dropdown-item>
                                                            <DeleteRecord
                                                                v-if="row.isHome == 0"
                                                                @afterDelete="loadFilter"
                                                                :requestApi="delDecorate"
                                                                :params="{ id: row.decorateId }"
                                                            >
                                                                <el-dropdown-item>
                                                                    <el-button style="width: 100%" link type="primary">{{ $t('删除') }}</el-button>
                                                                </el-dropdown-item>
                                                            </DeleteRecord>
                                                            <el-dropdown-item>
                                                                <el-button style="width: 100%" link type="primary" @click="onShare(row.decorateId)">{{ $t('分享') }}</el-button>
                                                            </el-dropdown-item>
                                                            <el-dropdown-item v-if="isTemplateExport">
                                                                <el-button style="width: 100%" link type="primary" @click="onExport(row)">{{ $t('导出') }}</el-button>
                                                            </el-dropdown-item>
                                                            <el-dropdown-item>
                                                                <DialogForm
                                                                    :title="$t('推广')"
                                                                    width="900px"
                                                                    :show-footer="false"
                                                                    path="decorate/mobileDecorate/Advertise"
                                                                    :params="{
                                                                        decorateItem: row,
                                                                        decorateType: props.decorateType,
                                                                        toPath: '/pages/shop/index',
                                                                        title: row.decorateTitle || '店铺首页'
                                                                    }"
                                                                >
                                                                    <el-button style="width: 100%" link type="primary">{{ $t('推广') }}</el-button>
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
                    </div>
                </el-tab-pane>
                <el-tab-pane :label="$t('其他模块')" :name="2">
                    <OtherPage></OtherPage>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { MoreFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { ref, reactive, onMounted } from "vue";
import { DeleteRecord, Switch, Pagination } from "@/components/list";
import { Image } from "@/components/image";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { FilterState, FilterParams } from "@/types/decorate/mobileDecorate.d";
import {
    getDecorateList,
    getDecorate,
    batchSubmit,
    updateDecorateFiled,
    delDecorate,
    updateDecorate,
    decorateShare,
    importDecorateLink
} from "@/api/decorate/mobileDecorate";
import { Tag } from "@/components/form";
import OtherPage from "@/views/merchant/setting/mobileShopDecorate/OtherPage.vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { copyDomText } from "@/utils/util";
import { isOverseas } from "@/utils/version";
import { baseDirFormat } from "@/utils/format";
import { useDecorateStore } from "@/store/decorate";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { setDecorateId, setLocaleId, setIsDefault, resetDecorateInfo } = useDecorateStore();
const props = defineProps({
    decorateType: {
        type: Number,
        default: 1
    }
});
const activeName = ref(1);
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<FilterState[]>();
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const advancedSearch = ref<boolean>(false);
const filterParams = reactive<FilterParams>({
    page: 1,
    decorateType: props.decorateType,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: ""
});
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

const importDecorate = () => {
    ElMessageBox.prompt("请把分享链接粘贴在此处", t('导入装修链接'), {
        confirmButtonText: t('确定'),
        cancelButtonText: t('取消')
    }).then(({ value }) => {
        _importDecorateLink(value);
    });
};
const _importDecorateLink = async (url: string) => {
    loading.value = true;
    try {
        const urlEncode = decodeURIComponent(url);
        const result = await importDecorateLink({ url: urlEncode });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getDecorateList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};

// 批量操作
const onBatchSubmit = async (action: string) => {
    try {
        const result = await batchSubmit(action, { ids: selectedIds.value });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
// 多选操作
const onSelectChange = (e: FilterState[]) => {
    selectedIds.value = e.map((item) => item.decorateId);
};
// 复制
const onCopy = async (row: FilterState) => {
    try {
        const result = await updateDecorate("copy", { id: row.decorateId });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

const { VITE_BASE_DIR } = import.meta.env;
const isTemplateExport = import.meta.env.VITE_IS_TEMPLATE_EXPORT || 0;

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

const onCreatePage = () => {
    resetDecorateInfo();
    setIsDefault(true);
    window.open(
        `${baseDirFormat(VITE_BASE_DIR)}/decorate/index?act=add&decorateType=${props.decorateType}&pageType=1&previewUrl=/pages/shop/index`,
        "_blank"
    );
};

const toPage = (type: number, parentId: number, id: number, localeId: number, isDefault: boolean) => {
    resetDecorateInfo();
    setDecorateId(parentId || id || 0);
    setLocaleId(localeId);
    setIsDefault(isDefault);
    if (type === 1) {
        window.open(`${baseDirFormat(VITE_BASE_DIR)}/decorate/index?id=${id}&pageType=1&previewUrl=/pages/shop/index`, "_blank");
    } else {
        window.open(`${baseDirFormat(VITE_BASE_DIR)}/decorate/pc?id=${id}`, "_blank");
    }
};
</script>

<style lang="less" scoped>
.default-page-icon {
    display: inline-flex;
    align-items: center;
    color: var(--el-color-primary);

    :deep(.icon) {
        width: 18px;
        height: 18px;
    }
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
