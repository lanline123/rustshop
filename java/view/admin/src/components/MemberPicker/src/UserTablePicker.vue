/**
 * @file 指定会员表格选择器
 * @description 搜索 + 表格复选选择会员，跨页保留已选
 * @features
 * - 数据源可替换（默认会员列表接口，优惠券发放传入发放专用接口）
 * - v-model 为 { [userId]: 会员 } 的映射，便于父级跨 Tab 汇总已选
 */
<template>
    <div class="member-table-picker">
        <div class="picker-search">
            <div class="picker-search__row">
                <TigInput
                    v-model="filter.keyword"
                    name="keyword"
                    placeholder="会员名称/手机号/邮箱"
                    clearable
                    width="250px"
                    @keyup.enter="onSearch"
                    @clear="onSearch"
                >
                    <template #append>
                        <el-button @click="onSearch"><span class="iconfont icon-chakan1"></span></el-button>
                    </template>
                </TigInput>
                <el-select v-model="filter.fromTag" class="picker-search__select" clearable placeholder="请选择" @change="onSearch">
                    <el-option v-for="item in fromTagOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </div>
            <div class="picker-search__actions">
                <el-button type="primary" @click="onSearch">搜索</el-button>
                <el-button @click="onReset">重置</el-button>
            </div>
        </div>

        <div class="picker-table">
            <el-table
                ref="tableRef"
                :data="records"
                :loading="loading"
                row-key="userId"
                height="100%"
                @selection-change="onSelectionChange"
            >
                <el-table-column type="selection" width="44" reserve-selection />
                <el-table-column label="会员名称" min-width="190">
                    <template #default="{ row }">
                        <div class="user-info">
                            <el-avatar :size="30" :src="imageFormat(row.avatar)" />
                            <div class="user-info__main">
                                <div class="user-name">{{ getUserLabel(row) }}</div>
                                <MobileCard :mobile="row.mobile" />
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column v-if="showCertifiedColumn" label="是否认证" min-width="90">
                    <template #default="{ row }">
                        <span :class="isCertified(row) ? 'status-green' : 'status-orange'">
                            {{ isCertified(row) ? "已认证" : "未认证" }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="是否启用" min-width="90">
                    <template #default="{ row }">
                        {{ row.statusName || (row.status === 0 ? "禁用" : "启用") }}
                    </template>
                </el-table-column>
                <template #empty>
                    <div class="empty-warp">
                        <div v-if="!loading" class="empty-bg">暂无数据</div>
                    </div>
                </template>
            </el-table>
        </div>

        <div v-if="total > 0" class="pagination-con">
            <Pagination v-model:page="filter.page" v-model:size="filter.size" :total="total" @callback="loadList" />
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { useConfigStore } from "@/store/config";
import { getUserList } from "@/api/user/user";
import { imageFormat } from "@/utils/format";
import { isB2CPro, isMerchant } from "@/utils/version";
import type { MemberPickerUser, MemberPickerUserMap } from "@/types/user/memberPicker";

const props = defineProps<{
    /** 已选会员映射，key 为 userId */
    modelValue?: MemberPickerUserMap;
    /** 会员列表数据源，需返回 { records, total } */
    requestApi?: (params: any) => Promise<any>;
    /** 强制隐藏「是否认证」列 */
    hideCertifiedColumn?: boolean;
    /** 强制显示「是否认证」列（优先级高于版本判断，用于多商户等默认隐藏场景） */
    forceCertifiedColumn?: boolean;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: MemberPickerUserMap): void;
}>();

const config: any = useConfigStore();

const fromTagOptions = [
    { value: 1, label: "微信公众号" },
    { value: 2, label: "微信小程序" },
    { value: 3, label: "H5" },
    { value: 4, label: "PC" },
    { value: 5, label: "APP" }
];

const tableRef = ref();
const loading = ref(false);
const total = ref(0);
const records = ref<MemberPickerUser[]>([]);

const filter = reactive({
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    fromTag: "" as number | string
});

const selected = computed<MemberPickerUserMap>(() => props.modelValue ?? {});
const showCertifiedColumn = computed(() => {
    if (props.hideCertifiedColumn) return false;
    if (props.forceCertifiedColumn) return true;
    return !isMerchant() && !isB2CPro();
});

const getUserLabel = (row: MemberPickerUser) => row.memberName || row.username || row.nickname || `会员${row.userId}`;
const isCertified = (row: MemberPickerUser) => (row.isCertified ?? row.mobileValidated) === 1;

/** 把父级维护的已选状态回写到当前页复选框 */
const syncSelection = async () => {
    await nextTick();
    if (!tableRef.value) return;
    records.value.forEach((row) => {
        tableRef.value.toggleRowSelection(row, !!selected.value[row.userId]);
    });
};

const loadList = async () => {
    loading.value = true;
    try {
        const requestApi = props.requestApi ?? getUserList;
        const result: any = await requestApi({ ...filter, keyword: filter.keyword?.trim() || "" });
        records.value = result?.records || [];
        total.value = result?.total || 0;
        await syncSelection();
    } catch (error: any) {
        message.error(error.message || "会员加载失败");
    } finally {
        loading.value = false;
    }
};

const onSearch = () => {
    filter.page = 1;
    loadList();
};

const onReset = () => {
    filter.keyword = "";
    filter.fromTag = "";
    filter.page = 1;
    loadList();
};

const onSelectionChange = (selection: MemberPickerUser[]) => {
    const next: MemberPickerUserMap = { ...selected.value };
    records.value.forEach((row) => {
        delete next[row.userId];
    });
    selection.forEach((row) => {
        next[row.userId] = row;
    });
    emit("update:modelValue", next);
};

onMounted(() => {
    loadList();
});

defineExpose({ refresh: loadList, syncSelection });
</script>

<style lang="less" scoped>
@import "./picker.less";

.picker-search__select {
    width: 200px;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}

.user-info__main {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.user-name {
    color: #303133;
    line-height: 20px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.status-orange {
    color: #f59a23;
}

.status-green {
    color: #67c23a;
}
</style>
