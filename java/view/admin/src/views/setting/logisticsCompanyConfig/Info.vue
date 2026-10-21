<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '请选择物流公司!' }]" :label="$t('物流公司')" prop="logisticsId">
                        <div class="full-width">
                            <div class="company-row">
                                <el-select
                                    v-model="formState.logisticsId"
                                    class="full-width logistics-company-select"
                                    filterable
                                    remote
                                    reserve-keyword
                                    :remote-method="remoteSearch"
                                    :loading="selectLoading"
                                    :suffix-icon="Search"
                                    :remote-show-suffix="true"
                                    :empty-values="[0, undefined, null, '']"
                                    :no-data-text="$t('暂无数据')"
                                    no-match-text="暂无数据"
                                    popper-class="logistics-company-select-dropdown"
                                    @visible-change="handleVisibleChange"
                                    @change="handleLogisticsChange"
                                >
                                    <el-option
                                        v-if="hiddenSelectedCompany && !selectVisible"
                                        class="selected-cache-option"
                                        :label="hiddenSelectedCompany.logisticsName"
                                        :value="hiddenSelectedCompany.logisticsId"
                                    />
                                    <el-option
                                        v-for="item in availableCompanies"
                                        :key="item.logisticsId"
                                        :label="item.logisticsName"
                                        :value="item.logisticsId"
                                        :disabled="isBoundOptionDisabled(item)"
                                    >
                                        <div class="company-option">
                                            <div class="company-main">
                                                <span>{{ item.logisticsName }}</span>
                                                <el-tag v-if="isBoundOption(item)" size="small" type="warning" effect="plain">{{ $t('已绑定') }}</el-tag>
                                                <el-tag v-if="isDefaultOption(item)" size="small" type="primary" effect="plain">{{ $t('默认') }}</el-tag>
                                            </div>
                                            <span class="company-code">{{ isKdniao ? (item.apiLogisticsCode || item.logisticsCode || "") : (item.kuaidi100Code || item.logisticsCode || "") }}</span>
                                        </div>
                                    </el-option>
                                    <el-option v-if="loadingMore" disabled value="__loading__" class="load-more-option">
                                        <span class="load-more-text">{{ $t('加载中...') }}</span>
                                    </el-option>
                                    <el-option v-else-if="hasMore && availableCompanies.length > 0" disabled value="__loadmore__" class="load-more-option">
                                        <span class="load-more-text" @click.stop="loadMore">{{ $t('点击加载更多...') }}</span>
                                    </el-option>
                                    <el-option v-else-if="!hasMore && availableCompanies.length > 0" disabled value="__nomore__" class="load-more-option">
                                        <span class="no-more-text">{{ $t('没有更多数据了') }}</span>
                                    </el-option>
                                </el-select>
                                <a v-if="canOpenGlobalPage" class="btn-link" @click="openCreateCompanyDrawer">{{ $t('去新增') }}</a>
                            </div>
                            <div class="extra">{{ $t('若列表里没有所需物流公司，请联系平台管理员处理。') }}</div>
                        </div>
                    </el-form-item>
                    <template v-if="isKdniao">
                        <el-form-item :label="$t('客户编码')" prop="customerName">
                            <TigInput classType="tig-form-input" v-model="formState.customerName" />
                            <div class="extra">{{ $t('请联系合作快递网点获取客户编码，允许为空。') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('密码')" prop="customerPwd">
                            <TigInput classType="tig-form-input" v-model="formState.customerPwd" />
                            <div class="extra">{{ $t('请联系合作快递网点获取密码。') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('月结号')" prop="monthCode">
                            <TigInput classType="tig-form-input" v-model="formState.monthCode" />
                            <div class="extra">{{ $t('请联系合作快递网点获取月结号。') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('网点编码')" prop="sendSite">
                            <TigInput classType="tig-form-input" v-model="formState.sendSite" />
                            <div class="extra">{{ $t('请联系合作快递网点获取网点编码。') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('网点名称')" prop="sendStaff">
                            <TigInput classType="tig-form-input" v-model="formState.sendStaff" />
                            <div class="extra">{{ $t('请联系合作快递网点获取网点名称。') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('快递业务类型')" prop="expType">
                            <TigInput classType="tig-form-input" v-model="formState.expType" type="number" />
                            <div class="extra">{{ $t('例如：顺丰特快填 1，顺丰标快填 2。') }}</div>
                        </el-form-item>
                    </template>
                    <template v-else>
                        <el-form-item :label="$t('partnerId')" prop="kuaidi100PartnerId">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100PartnerId" />
                            <div class="extra">{{ $t('请联系合作快递网点获取电子面单账号/月结账号') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('partnerKey')" prop="kuaidi100PartnerKey">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100PartnerKey" />
                            <div class="extra">{{ $t('请联系合作快递网点获取电子面单密码，部分快递可不填') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('partnerSecret')" prop="kuaidi100PartnerSecret">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100PartnerSecret" />
                            <div class="extra">{{ $t('请联系合作快递网点获取电子面单密钥，部分快递可不填') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('收件网点名称')" prop="kuaidi100Net">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100Net" />
                            <div class="extra">{{ $t('快递公司网点提供，揽收网点编码 / 网点名称') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('承载快递员名')" prop="kuaidi100CheckMan">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100CheckMan" />
                            <div class="extra">{{ $t('快递公司网点提供，填写揽收业务员姓名') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('产品类型')" prop="kuaidi100ExpType">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100ExpType" />
                            <div class="extra">{{ $t('快递100文档查询枚举值（标准件 / 特惠件等编码）') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('面单模板ID')" prop="kuaidi100TempId">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100TempId" />
                            <div class="extra">{{ $t('快递100企业后台 -> 电子面单与云打印 -> 电子面单模板，复制模板ID；自定义模板在这里创建后获取ID，非NON模式必填') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('子单模板ID')" prop="kuaidi100ChildTempId">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100ChildTempId" />
                            <div class="extra">{{ $t('快递100模板页面，子单、回单模板的ID，没有回单业务可不填') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('回单模板ID')" prop="kuaidi100BackTempId">
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100BackTempId" />
                            <div class="extra">{{ $t('快递100模板页面，子单、回单模板的ID，没有回单业务可不填') }}</div>
                        </el-form-item>
                    </template>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item :label="$t('是否启用')" prop="isEnabled">
                        <el-radio-group v-model="formState.isEnabled">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <DialogForm
                    ref="createCompanyDialogRef"
                    isDrawer
                    :title="$t('添加物流公司')"
                    width="600px"
                    path="setting/logisticsCompany/Info"
                    :params="{ act: 'add' }"
                    style="display: none"
                    @okCallback="handleCreateCompanyOk"
                >
                    <span></span>
                </DialogForm>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { Search } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { getLogisticsCompanyAll } from "@/api/setting/logisticsCompany";
import { getLogisticsCompanyConfig, updateLogisticsCompanyConfig } from "@/api/setting/logisticsCompanyConfig";
import { getLogisticsProvider, LOGISTICS_PROVIDER_KDNIAO } from "@/api/setting/baseLogistics";
import type { LogisticsCompanyFormState } from "@/types/setting/logisticsCompany";
import type { LogisticsCompanyConfigFormState } from "@/types/setting/logisticsCompanyConfig";
import type { LogisticsProvider } from "@/types/setting/baseLogistics.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});

const loading = ref(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const router = useRouter();
const pageSize = 100;

const formState = ref<LogisticsCompanyConfigFormState>({
    sortOrder: 50,
    isEnabled: 1
});
const globalCompanies = ref<LogisticsCompanyFormState[]>([]);
const selectedCompany = ref<LogisticsCompanyFormState | null>(null);
const createCompanyDialogRef = ref<any>();
const selectLoading = ref(false);
const selectVisible = ref(false);
const loadingMore = ref(false);
const currentPage = ref(1);
const total = ref(0);
const hasMore = ref(true);
const keyword = ref("");
const canOpenGlobalPage = computed(() => router.getRoutes().some((route) => route.path === "/setting/logistics-company/list"));
const provider = ref<LogisticsProvider>(LOGISTICS_PROVIDER_KDNIAO);
const isKdniao = computed(() => provider.value !== 2);

const currentSelectedCompany = computed<LogisticsCompanyFormState | null>(() => {
    if (!formState.value.logisticsId) {
        return null;
    }
    const matched = globalCompanies.value.find((item) => item.logisticsId === formState.value.logisticsId);
    if (matched) {
        return matched;
    }
    if (selectedCompany.value?.logisticsId === formState.value.logisticsId) {
        return selectedCompany.value;
    }
    if (!formState.value.logisticsName) {
        return null;
    }
    return {
        logisticsId: formState.value.logisticsId,
        logisticsName: formState.value.logisticsName,
        logisticsCode: formState.value.logisticsCode,
        apiLogisticsCode: formState.value.apiLogisticsCode,
        logisticsDesc: formState.value.logisticsDesc,
        isEnabled: formState.value.isEnabled,
        isBound: 1,
        isDefault: formState.value.isDefault
    };
});

const availableCompanies = computed(() => {
    return [...globalCompanies.value];
});

const hiddenSelectedCompany = computed<LogisticsCompanyFormState | null>(() => {
    const current = currentSelectedCompany.value;
    if (!current) {
        return null;
    }
    return availableCompanies.value.some((item) => item.logisticsId === current.logisticsId) ? null : current;
});

const isBoundOption = (item: LogisticsCompanyFormState) => {
    return Number(item.isBound || 0) === 1;
};

const isDefaultOption = (item: LogisticsCompanyFormState) => {
    const logisticsId = Number(item.logisticsId || 0);
    const currentLogisticsId = Number(formState.value.logisticsId || 0);
    if (logisticsId > 0 && logisticsId === currentLogisticsId) {
        return Number(item.isDefault ?? formState.value.isDefault ?? 0) === 1;
    }
    return Number(item.isDefault || 0) === 1;
};

const isBoundOptionDisabled = (item: LogisticsCompanyFormState) => {
    const logisticsId = Number(item.logisticsId || 0);
    return isBoundOption(item) && logisticsId !== Number(formState.value.logisticsId || 0);
};

const mergeCompanyRecords = (records: LogisticsCompanyFormState[], isLoadMore: boolean) => {
    if (!isLoadMore) {
        globalCompanies.value = records;
        return;
    }
    const recordMap = new Map<number, LogisticsCompanyFormState>();
    for (const item of globalCompanies.value) {
        recordMap.set(Number(item.logisticsId || 0), item);
    }
    for (const item of records) {
        recordMap.set(Number(item.logisticsId || 0), item);
    }
    globalCompanies.value = Array.from(recordMap.values());
};

const fetchGlobalCompanies = async (page = 1, isLoadMore = false, searchKeyword = keyword.value) => {
    if (isLoadMore) {
        loadingMore.value = true;
    } else {
        selectLoading.value = true;
    }

    try {
        const result = await getLogisticsCompanyAll({
            page,
            size: pageSize,
            isEnabled: 1,
            keyword: searchKeyword,
            providerType: provider.value
        });
        const records = result.records || [];
        mergeCompanyRecords(records, isLoadMore);
        total.value = Number(result.total || 0);
        currentPage.value = page;
        hasMore.value = globalCompanies.value.length < total.value;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        selectLoading.value = false;
        loadingMore.value = false;
    }
};

const fetchDetail = async () => {
    const result = await getLogisticsCompanyConfig(action.value, { id: id.value });
    Object.assign(formState.value, result);
    selectedCompany.value = {
        logisticsId: result.logisticsId,
        logisticsName: result.logisticsName,
        logisticsCode: result.logisticsCode,
        apiLogisticsCode: result.apiLogisticsCode,
        logisticsDesc: result.logisticsDesc,
        isEnabled: result.isEnabled,
        isBound: 1,
        isDefault: result.isDefault
    };
};

const remoteSearch = (query: string) => {
    keyword.value = query.trim();
    currentPage.value = 1;
    fetchGlobalCompanies(1);
};

const loadMore = () => {
    if (!hasMore.value || loadingMore.value) {
        return;
    }
    fetchGlobalCompanies(currentPage.value + 1, true);
};

const handleLogisticsChange = (logisticsId: number) => {
    selectedCompany.value = availableCompanies.value.find((item) => item.logisticsId === logisticsId) || null;
};

const handleVisibleChange = (visible: boolean) => {
    selectVisible.value = visible;
};

const openCreateCompanyDrawer = () => {
    createCompanyDialogRef.value?.show?.();
};

const extractCreatedCompany = (result: any) => {
    const candidates = [
        result,
        result?.item,
        result?.data
    ];

    for (const candidate of candidates) {
        if (!candidate || typeof candidate !== "object") {
            continue;
        }

        const logisticsId = Number(candidate.logisticsId ?? candidate.id ?? 0);
        const logisticsName = candidate.logisticsName;

        if (logisticsId > 0 || logisticsName) {
            return {
                logisticsId: logisticsId > 0 ? logisticsId : undefined,
                logisticsName
            };
        }
    }

    return {
        logisticsId: Number(result) > 0 ? Number(result) : undefined,
        logisticsName: undefined
    };
};

const handleCreateCompanyOk = async (result: any) => {
    const { logisticsId, logisticsName } = extractCreatedCompany(result);
    const searchKeyword = logisticsName ? String(logisticsName).trim() : "";

    await fetchGlobalCompanies(1, false, searchKeyword);

    let createdCompany = null;
    if (logisticsId) {
        createdCompany = globalCompanies.value.find((item) => Number(item.logisticsId || 0) === logisticsId) || null;
    }
    if (!createdCompany && logisticsName) {
        createdCompany = globalCompanies.value.find((item) => item.logisticsName === logisticsName) || null;
    }

    if (createdCompany) {
        formState.value.logisticsId = createdCompany.logisticsId;
        selectedCompany.value = createdCompany;
    }
};

onMounted(async () => {
    try {
        provider.value = await getLogisticsProvider();
        await fetchGlobalCompanies(1);
        if (action.value === "detail") {
            await fetchDetail();
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateLogisticsCompanyConfig(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.full-width {
    width: 100%;
}

.company-row {
    display: flex;
    align-items: center;
    gap: 12px;
}

.company-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
}

.company-main {
    display: inline-flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    min-width: 0;
}

.company-code {
    color: #909399;
    font-size: 12px;
    flex-shrink: 0;
}
</style>

<style lang="less">
.logistics-company-select {
    .el-select__caret {
        transform: none !important;
        transition: none !important;

        &.is-reverse {
            transform: none !important;
        }
    }
}

.logistics-company-select-dropdown {
    .selected-cache-option {
        display: none !important;
    }

    .load-more-option {
        &.el-select-dropdown__item {
            display: flex !important;
            justify-content: center !important;
            align-items: center !important;
        }
    }

    .load-more-text {
        color: var(--el-color-primary);
        cursor: pointer;

        &:hover {
            opacity: 0.8;
        }
    }

    .no-more-text {
        color: #999;
        cursor: default;
    }
}
</style>
