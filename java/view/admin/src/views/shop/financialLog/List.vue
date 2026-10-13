<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="container">
                <div class="lyecs-table-list-warp">
                    <div class="list-table-tool lyecs-search-warp">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入{shopTypeName}名称', { shopTypeName })"
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
                                        <label class="control-label"><span>{{ $t('时间范围：') }}</span></label>
                                        <SelectTimeInterval
                                            v-model:end-date="filterParams.endTime"
                                            v-model:start-date="filterParams.startTime"
                                            :clearable="false"
                                            type="date"
                                            value-format="YYYY-MM-DD"
                                        ></SelectTimeInterval>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                                <div class="simple-form-field">
                                    <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="table-container">
                        <a-spin :spinning="loading">
                            <el-table :data="filterState" :loading="loading" :total="total" row-key="shopId" @sort-change="onSortChange">
                                <el-table-column :label="$t('账户变动时间')" width="200">
                                    <template #default="{ row }">
                                        {{ row.addTime || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('{shopTypeName}名称', { shopTypeName })">
                                    <template #default="{ row }">
                                        {{ row.shopTitle || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('变动资金账户')" width="250">
                                    <template #default="{ row }">
                                        <span v-if="row.newShopMoney - row.changeAmount > 0" style="color: #0000ff"
                                            >+{{ (row.newShopMoney - row.changeAmount).toFixed(2) }}</span
                                        >
                                        <span v-else-if="row.newShopMoney - row.changeAmount == 0">0</span>
                                        <span v-else style="color: #ff0000">-{{ (row.changeAmount - row.newShopMoney).toFixed(2) }}</span>
                                        <span v-if="row.newShopMoney" style="color: #999; font-size: 12px">{{ $t('（变更后金额:{newShopMoney}）', { newShopMoney: row.newShopMoney }) }}</span>
                                        <!-- <span v-if="(row.type == 1|| row.type == 4) && row.changeAmount > 0" style="color: #0000ff">+{{ row.changeAmount }}</span>
                                        <span v-else-if="row.type == 2" style="color: #ff0000">-{{ row.changeAmount }}</span>
                                        <span v-else>{{ row.changeAmount }}</span>
                                        <span v-if="row.newShopMoney" style="color: #999; font-size: 12px">（变更后金额:{{ row.newShopMoney }}）</span> -->
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('冻结资金账户')" width="250">
                                    <template #default="{ row }">
                                        <span v-if="row.newFrozenMoney - row.frozenMoney > 0" style="color: #0000ff"
                                            >+{{ (row.newFrozenMoney - row.frozenMoney).toFixed(2) }}</span
                                        >
                                        <span v-else-if="row.newFrozenMoney - row.frozenMoney == 0">0</span>
                                        <span v-else style="color: #ff0000">-{{ (row.frozenMoney - row.newFrozenMoney).toFixed(2) }}</span>
                                        <span v-if="row.newFrozenMoney" style="color: #999; font-size: 12px">{{ $t('（变更后金额:{newFrozenMoney}）', { newFrozenMoney: row.newFrozenMoney }) }}</span>
                                        <!-- 
                                        <span v-if="(row.type == 1 || row.type == 4) && row.frozenMoney > 0" style="color: #0000ff"
                                            >+{{ row.frozenMoney }}</span
                                        >
                                        <span v-else-if="row.type == 2" style="color: #ff0000">-{{ row.frozenMoney }}</span>
                                        <span v-else>{{ row.frozenMoney }}</span>
                                        <span v-if="row.newFrozenMoney" style="color: #999; font-size: 12px">（变更后金额:{{ row.newFrozenMoney }}）</span> -->
                                    </template>
                                </el-table-column>

                                <el-table-column :label="$t('变动原因')">
                                    <template #default="{ row }">
                                        <span class="ellipsis">{{ row.changeReasons }}</span>
                                    </template>
                                </el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
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
                                    >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                                >
                                <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                    <template #reference>
                                        <el-button>{{ $t('批量删除') }}</el-button>
                                    </template>
                                </el-popconfirm>
                            </el-space>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { ShopFilterParams, ShopFilterState } from "@/types/shop/shop.d";
import { batchSubmit, getShopAccountLog, updateShopFiled } from "@/api/shop/shop";
import { ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import { SelectTimeInterval } from "@/components/select";
import { useListRequest } from "@/hooks/useListRequest";
import { getShopType } from "@/utils/storage";
import { isStore } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopTypeName = isStore() ? t('门店') : t('店铺');
const query = useRouter().currentRoute.value.query;
const props = defineProps({ shopId: { type: Number, default: 0 } });
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onBatchAction,
    resetParams
} = useListRequest<ShopFilterState, ShopFilterParams>({
    apiFunction: getShopAccountLog,
    idKey: "shopId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        shopId: props.shopId || "",
        endTime: "",
        startTime: "",
        shopType: getShopType() === 1 ? 1 : 2
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
const handleSwitchChange = (params: any) => {
    const { data, row } = params;
    ElMessageBox.confirm(t('确认修改当前店铺的开业状态吗?'), t('提示'), { confirmButtonText: t('确认'), cancelButtonText: t('取消'), type: "warning" })
        .then(() => {
            row.status = row.status == 1 ? 10 : 1;
            const result = updateShopFiled({ ...data, id: row.shopId, val: row.status });
            message.success(t('修改成功'));
        })
        .catch(() => {});
};
const okCallback = () => {
    clearUrlParams();
    loadFilter();
};
const clearUrlParams = () => {
    // 获取当前页面的完整URL
    let currentUrl = window.location.href;
    // 使用URL对象解析当前URL
    let urlObj = new URL(currentUrl);
    // 清除查询字符串
    urlObj.search = "";
    // 将修改后的URL设置为当前页面的URL
    window.history.replaceState({}, document.title, urlObj.toString());
};
const dialogForm = ref<any>();
const merchantId = ref<number>(0);
onMounted(() => {
    if (query.id) {
        ElMessageBox.confirm(t('为[{companyName}]立即创建店铺?', { companyName: query.companyName || "--" }), t('提示'), {
            closeOnClickModal: false,
            showClose: false,
            confirmButtonText: t('确认'),
            cancelButtonText: t('取消'),
            type: "warning"
        })
            .then(() => {
                merchantId.value = Number(query.id);
                dialogForm.value.show();
            })
            .catch(() => {
                merchantId.value = 0;
                clearUrlParams();
            });
    }
});
</script>
<style lang="less" scoped>
.status-switch {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    width: 150px;
}

.status-switch > div {
    word-break: keep-all;
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.display-col {
    display: flex;
    align-items: center;
    /* 垂直居中 */
    gap: 8px;

    .image {
        width: 30px;
        height: 30px;

        .image-style {
            width: 100%;
            height: 100%;
            object-fit: cover;
            /* 保持图片比例 */
        }
    }

    .image_comment {
        min-width: 50px;
        min-height: 50px;
        max-width: 50px;
        max-height: 50px;

        .image-style {
            width: 100%;
            height: 100%;
            object-fit: cover;
            /* 保持图片比例 */
        }
    }

    .text {
        flex-grow: 1;
        /* 占据剩余空间 */
        white-space: nowrap;
        /* 不换行 */
        overflow: hidden;
        /* 隐藏超出部分 */
        text-overflow: ellipsis;
        /* 超出显示省略号 */
        margin-left: 8px;
        /* 与左侧图片的间距 */
    }

    .text_comment {
        flex-grow: 1;
        /* 占据剩余空间 */
    }
}

.multiline_hiding {
    height: 3.6em;
    /* 基于字体大小的高度, 3行文本高度 */
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    /* 限制在3行 */
    -webkit-box-orient: vertical;
    text-overflow: ellipsis;
    line-height: 1.2em;
    /* 调整这个值以匹配你的字体大小 */
}

.green {
    margin-left: 8px;
    color: green !important;
}

.grey {
    margin-left: 8px;
    color: #999 !important;
}

.ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    width: 90%;
}
</style>
