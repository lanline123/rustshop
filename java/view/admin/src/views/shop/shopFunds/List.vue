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
                                                name="username"
                                                :placeholder="$t('输入{shopTypeName}名称', { shopTypeName })"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
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
                                        <TigTabs v-model="filterParams.status" :tabs="shopStatus" @onTabChange="onChange"></TigTabs>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="table-container">
                        <a-spin :spinning="loading">
                            <el-table
                                :data="filterState"
                                :loading="loading"
                                :total="total"
                                row-key="shopId"
                                @selection-change="onSelectChange"
                                @sort-change="onSortChange"
                            >
                                <el-table-column :label="$t('{shopTypeName}名称', { shopTypeName })">
                                    <template #default="{ row }">
                                        {{ row.shopTitle || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="`${shopTypeName}LOGO`">
                                    <template #default="{ row }">
                                        <Image :src="row.shopLogo" fit="contain" style="height: 25px; width: 60px" />
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('{shopTypeName}状态', { shopTypeName })">
                                    <template #default="{ row }">
                                        <template v-if="row.status == 10">
                                            <StatusDot color="red" :flicker="true"></StatusDot>
                                        </template>
                                        <template v-if="row.status == 4">
                                            <StatusDot color="red" :flicker="true"></StatusDot>
                                        </template>
                                        <template v-if="row.status == 1">
                                            <StatusDot color="green" :flicker="true"></StatusDot>
                                        </template>
                                        <span v-if="row.status === 10 || row.status === 4" style="color: red">{{ row.statusText }}</span>
                                        <span v-else-if="row.status === 1" style="color: green">{{ row.statusText }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('{shopTypeName}资金', { shopTypeName })" sortable="custom" prop="shopMoney">
                                    <template #default="{ row }">
                                        {{ priceFormat(row.shopMoney) || 0.0 }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('待结算金额')" sortable="custom" prop="unSettlementOrder">
                                    <template #default="{ row }">
                                        {{ priceFormat(row.unSettlementOrder) || 0.0 }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('不可用资金')" sortable="custom" prop="frozenMoney">
                                    <template #default="{ row }">
                                        {{ priceFormat(row.frozenMoney) || 0.0 }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('开店时间')">
                                    <template #default="{ row }">
                                        {{ row.addTime }}
                                    </template>
                                </el-table-column>
                                <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                    <template #default="{ row }">
                                        <DialogForm
                                            :params="{ shopId: row.shopId }"
                                            :showClose="false"
                                            :showOnOk="false"
                                            isDrawer
                                            path="shop/financialLog/List"
                                            :title="$t('资金日志')"
                                            width="1000px"
                                        >
                                            <a class="btn-link">{{ $t('查看资金日志') }}</a>
                                        </DialogForm>
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
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { Image } from "@/components/image";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { ShopFilterParams, ShopFilterState } from "@/types/shop/shop.d";
import { batchSubmit, getShopAccountList, updateShopFiled } from "@/api/shop/shop";
import { ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import { priceFormat } from "@/utils/format";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { useListRequest } from "@/hooks/useListRequest";
import { getShopType } from "@/utils/storage";
import { isMerchant, isStore } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopTypeName = isMerchant() ? t('店铺') : isStore() ? t('门店') : t('店铺');
const query = useRouter().currentRoute.value.query;
const shopStatus = ref<any[]>([
    {
        label: t('全部'),
        value: 0,
        isShow: true
    },
    {
        label: t('开业'),
        value: 1,
        isShow: true
    },
    {
        label: t('停业'),
        value: 4,
        isShow: true
    },
    {
        label: t('打烊'),
        value: 10,
        isShow: true
    }
]);
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
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<ShopFilterState, ShopFilterParams>({
    apiFunction: getShopAccountList,
    idKey: "shopId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        status: 0,
        page: 1,
        size: config.get("pageSize"),
        shopType: isMerchant() ? 1 : isStore() ? 2 : 1
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
const onChange = (status: number) => {
    filterParams.status = status;
    filterParams.page = 1;
    loadFilter();
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
        ElMessageBox.confirm(t('为[{companyName}]立即创建{shopTypeName}?', { companyName: query.companyName || "--", shopTypeName }), t('提示'), {
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
</style>
