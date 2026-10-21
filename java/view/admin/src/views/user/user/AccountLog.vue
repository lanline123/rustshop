/** 客户账目明细 - 展示客户的资金和积分变动记录，支持筛选和调节 */
<template>
    <div>
        <div class="list-table-tool lyecs-search-warp">
            <div class="notice-warp">
                <p class="flex flex-justify-between">
                    <span>{{ $t("当前客户：{username}", { username: formState.username }) }}</span>
                    <span>{{ $t("可用资金帐户：{priceFormat}", { priceFormat: priceFormat(formState.balance) }) }}</span>
                    <span>{{ $t("冻结资金帐户：{priceFormat}", { priceFormat: priceFormat(formState.frozenBalance) }) }}</span>
                    <span>{{ $t("成长积分帐户：{growthPoints}", { growthPoints: formState.growthPoints }) }}</span>
                    <span>{{ $t("消费积分帐户：{points}", { points: formState.points }) }}</span>
                </p>
            </div>
            <el-form :model="filterParams">
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <el-select v-model="filterParams.fromTag" clearable @change="onSearchSubmit">
                                <el-option :value="1" :label="$t('可用资金账户')" />
                                <el-option :value="2" :label="$t('冻结资金账户')" />
                                <el-option :value="3" :label="$t('成长积分账户')" />
                                <el-option :value="4" :label="$t('消费积分账户')" />
                            </el-select>
                            <TigInput
                                v-model="filterParams.keyword"
                                name="keyword"
                                :placeholder="$t('输入账户变动原因')"
                                @keyup.enter="onSearchSubmit"
                                clearable
                                @clear="onSearchSubmit"
                            >
                                <template #append>
                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                </template>
                            </TigInput>
                        </el-space>
                    </div>
                    <div class="list-table-tool-col mb10">
                        <DialogForm
                            :params="{ act: 'detail', id: id }"
                            isDrawer
                            path="user/user/EditAccount"
                            :title="$t('调节客户帐户')"
                            width="600px"
                            @okCallback="loadFilter"
                        >
                            <el-button>{{ $t("调节客户账户") }}</el-button>
                        </DialogForm>
                    </div>
                </div>
            </el-form>
        </div>
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="filterState" :loading="loading" :total="total" row-key="logId" @sort-change="onSortChange">
                    <el-table-column :label="$t('变动时间')" prop="changeTime" sortable="custom">
                        <template #default="{ row }">
                            <ul>
                                <li>{{ row.changeTime }}</li>
                            </ul>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('变动原因')" prop="regTime">
                        <template #default="{ row }">
                            <el-tooltip :content="row.changeDesc" placement="top" :disabled="!row.changeDesc">
                                <div class="change-desc-text">{{ row.changeDesc }}</div>
                            </el-tooltip>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('可用资金')" prop="balance">
                        <template #default="{ row }">
                            <div v-if="row.balance > 0">
                                <span v-if="row.changeType === 1" style="color: #0000ff">+{{ row.balance }}</span>
                                <span v-else-if="row.changeType === 2" style="color: #ff0000">-{{ row.balance }}</span>
                                <span v-else>{{ row.balance }}</span>
                            </div>
                            <div v-else>0.00</div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('冻结资金')" prop="frozenBalance">
                        <template #default="{ row }">
                            <div v-if="row.frozenBalance > 0">
                                <span v-if="row.changeType === 1" style="color: #0000ff">+{{ row.frozenBalance }}</span>
                                <span v-else-if="row.changeType === 2" style="color: #ff0000">-{{ row.frozenBalance }}</span>
                                <span v-else>{{ row.frozenBalance }}</span>
                            </div>
                            <div v-else>0.00</div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('成长积分')" prop="points">
                        <template #default="{ row }">
                            <div v-if="filterParams.fromTag == 3">
                                <span v-if="row.changeType === 1" style="color: #0000ff">+{{ row.points }}</span>
                                <span v-else-if="row.changeType === 2" style="color: #ff0000">-{{ row.points }}</span>
                                <span v-else>{{ row.points }}</span>
                            </div>
                            <div v-else>0.00</div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('消费积分')" prop="points">
                        <template #default="{ row }">
                            <div v-if="filterParams.fromTag == 4">
                                <span v-if="row.changeType === 1" style="color: #0000ff">+{{ row.points }}</span>
                                <span v-else-if="row.changeType === 2" style="color: #ff0000">-{{ row.points }}</span>
                                <span v-else>{{ row.points }}</span>
                            </div>
                            <div v-else>0.00</div>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
                        </div>
                    </template>
                </el-table>
            </a-spin>
            <div v-if="total > 0" class="pagination-con">
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
            </div>
        </div>
        <div v-if="selectedIds.length > 0" class="selected-action-warp selected-warp-left">
            <div class="selected-action">
                <el-space>
                    <span
                        >{{ $t("已选择：") }}<b>{{ selectedIds.length }}</b> {{ $t("项") }}</span
                    >
                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                        <template #reference>
                            <el-button>{{ $t("批量删除") }}</el-button>
                        </template>
                    </el-popconfirm>
                </el-space>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { UserFilterParams, UserFormState, UserFundList } from "@/types/user/user.d";
import { getUser, getUserFundList } from "@/api/user/user";
import { useRouter } from "vue-router";
import { priceFormat } from "@/utils/format";

const config: any = useConfigStore();

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    type: {
        type: Number,
        default: 1
    },
    isDialog: Boolean
});
const query = useRouter().currentRoute.value.query;
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const type = ref<number>(props.isDialog ? props.type : Number(query.type));
// 基本参数定义
const filterState = ref(<UserFundList[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filter = ref<any>({});
const selectedIds = ref<number[]>([]);
/** 筛选参数 */
const filterParams = reactive<UserFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    fromTag: type.value
});
/** 客户基本信息 */
const formState = ref<UserFormState>({
    balance: 0,
    frozenBalance: 0,
    avatar: ""
});

/** 获取账目明细列表 */
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getUserFundList({ userId: id.value, ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/** 获取客户基本信息 */
const fetchUser = async () => {
    try {
        const result = await getUser("detail", { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/** 排序变更处理 */
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};
onMounted(() => {
    loadFilter();
    fetchUser();
});

/** 搜索提交 */
const onSearchSubmit = () => {
    loadFilter();
};
const onBatchSubmit = (str: string) => {};
</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}

.change-desc-text {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    word-break: break-all;
    line-height: 1.5;
}
</style>
