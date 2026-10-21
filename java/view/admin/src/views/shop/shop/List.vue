<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="container">
                <div class="lyecs-table-list-warp">
                    <!-- <TigTabs v-model="filterParams.status" :tabs="shopStatus" @onTabChange="onChange"></TigTabs> -->
                    <div class="list-table-tool lyecs-search-warp">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("店铺名称：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入店铺名称')"
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
                                <!-- <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>关联账号：</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.account"
                                                name="account"
                                                placeholder="输入关联账号名称"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                            </TigInput>
                                        </div>
                                    </div>
                                </div> -->
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("主账号：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.mainAccount"
                                                name="mainAccount"
                                                :placeholder="$t('输入主账号名称')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("状态 ：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable @change="onSearchSubmit">
                                                <el-option v-for="value in shopStatus" :label="value.label" :value="value.value" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("商户名称：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.corporateName"
                                                name="corporateName"
                                                :placeholder="$t('输入商户名称')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t("搜索") }}</el-button>
                                            <el-button plain @click="resetParams">{{ $t("重置") }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row mt20">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        ref="dialogForm"
                                        :params="{ act: 'add', id: merchantId }"
                                        isDrawer
                                        path="shop/shop/Add"
                                        :title="$t('新增店铺')"
                                        width="800px"
                                        @closeCallback="closeCallback"
                                        @okCallback="okCallback"
                                    >
                                        <el-button type="primary">{{ $t("新增店铺") }}</el-button>
                                    </DialogForm>
                                </el-space>
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
                                <!-- <el-table-column type="selection" width="32" /> -->
                                <el-table-column :label="$t('店铺名称')">
                                    <template #default="{ row }">
                                        {{ row.shopTitle || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('店铺LOGO')">
                                    <template #default="{ row }">
                                        <Image :src="row.shopLogo" fit="contain" style="height: 25px; width: 60px" />
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('商户名称')" :width="200">
                                    <template #default="{ row }">
                                        <div v-if="row.merchant">
                                            <span v-if="row.merchant.type == 2">{{ row.merchant.merchantData?.merchantName || "--" }}</span>
                                            <span v-if="row.merchant.type == 1">{{ row.merchant.corporateName || "--" }}</span>
                                            <DialogForm
                                                :params="{ act: 'detail', id: row.merchant.merchantId }"
                                                isDrawer
                                                path="adminMerchant/merchant/Info"
                                                :title="$t('商户详情')"
                                                width="600px"
                                                :showClose="false"
                                                :showOnOk="false"
                                                @okCallback="loadFilter"
                                            >
                                                <a class="btn-link green" v-if="row.merchant.status == 1">{{ $t("(已认证)") }}</a>
                                                <a class="btn-link grey" v-else>{{ $t("(未认证)") }}</a>
                                            </DialogForm>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('店铺状态')">
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
                                        <span v-if="row.status === 10 || row.status === 4" style="color: red">{{ $t(row.statusText) }}</span>
                                        <span v-else-if="row.status === 1" style="color: green">{{ $t(row.statusText) }}</span>
                                    </template>
                                </el-table-column>
                                <!-- <el-table-column label="店铺佣金" align="center">
                                    <template #default="{ row }">
                                        {{ row.shopCommission || '--'}}
                                    </template>
                                </el-table-column> -->
                                <el-table-column :label="$t('主账号')" align="center">
                                    <template #default="{ row }">
                                        <div v-if="row.merchant && row.merchant.admin">
                                            <DialogForm
                                                :params="{ id: row.shopId, type: 'shop' }"
                                                isDrawer
                                                path="authority/accountManage/Detail"
                                                :title="$t('主账号管理')"
                                                width="900px"
                                                @callback="loadFilter"
                                                :showClose="false"
                                                :showOnOk="false"
                                            >
                                                <a>{{ row.merchant.admin.username || "--" }}</a>
                                            </DialogForm>
                                        </div>
                                        <div v-else>--</div>
                                    </template>
                                </el-table-column>
                                <el-table-column :width="200" fixed="right" :label="$t('操作')">
                                    <template #default="{ row }">
                                        <el-space :size="0">
                                            <DialogForm
                                                :params="{ act: 'detail', id: row.shopId }"
                                                isDrawer
                                                path="shop/shop/Info"
                                                :title="$t('编辑店铺')"
                                                width="700px"
                                                @okCallback="loadFilter"
                                            >
                                                <a class="btn-link">{{ $t("编辑") }}</a>
                                            </DialogForm>
                                            <el-divider direction="vertical" />
                                            <a :href="getShopLink('shop', row.shopId)" target="_blank" class="btn-link">{{ $t("店铺首页") }}</a>
                                            <el-divider direction="vertical" />
                                            <el-dropdown>
                                                <el-icon class="btn-link" size="12">
                                                    <MoreFilled />
                                                </el-icon>
                                                <template #dropdown>
                                                    <el-dropdown-menu>
                                                        <!-- <el-dropdown-item>
                                                            <DialogForm
                                                                :params="{ id: row.shopId }"
                                                                isDrawer
                                                                path="shop/shop/Set"
                                                                title="店铺结算设置"
                                                                width="500px"
                                                                @okCallback="loadFilter"
                                                            >
                                                                <el-button style="width: 100%" link type="primary">结算设置</el-button>
                                                            </DialogForm>
                                                        </el-dropdown-item> -->
                                                        <el-dropdown-item>
                                                            <DialogForm
                                                                :params="{ id: row.shopId, type: 'shop' }"
                                                                isDrawer
                                                                path="authority/accountManage/Detail"
                                                                :title="$t('主账号管理')"
                                                                width="900px"
                                                                @okCallback="loadFilter"
                                                                @callback="loadFilter"
                                                                :showClose="false"
                                                                :showOnOk="false"
                                                            >
                                                                <el-button style="width: 100%" link type="primary">{{ $t("主账号管理") }}</el-button>
                                                            </DialogForm>
                                                        </el-dropdown-item>
                                                        <DeleteRecord
                                                            :params="{ id: row.shopId }"
                                                            :title="$t('删除后店铺信息将无法恢复，请确认是否删除该店铺？')"
                                                            :requestApi="delShop"
                                                            @afterDelete="loadFilter"
                                                        >
                                                            <el-dropdown-item>
                                                                <el-button style="width: 100%" link type="primary">{{ $t("删除") }}</el-button>
                                                            </el-dropdown-item>
                                                        </DeleteRecord>
                                                    </el-dropdown-menu>
                                                </template>
                                            </el-dropdown>
                                        </el-space>
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
import { MoreFilled } from "@element-plus/icons-vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { ShopFilterParams, ShopFilterState } from "@/types/shop/shop.d";
import { batchSubmit, getShopList, updateShopFiled, delShop } from "@/api/shop/shop";
import { ElMessageBox } from "element-plus";
import { getShopLink } from "@/utils/util";
import { useRouter } from "vue-router";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { useListRequest } from "@/hooks/useListRequest";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const shopStatus = ref<any[]>([
    {
        label: t("开业"),
        value: 1,
        isShow: true
    },
    {
        label: t("停业"),
        value: 4,
        isShow: true
    },
    {
        label: t("打烊"),
        value: 10,
        isShow: true
    }
]);
const query = useRouter().currentRoute.value.query;
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
    onBatchAction
} = useListRequest<ShopFilterState, ShopFilterParams>({
    apiFunction: getShopList,
    idKey: "shopId",
    defaultParams: {
        shopType: 1,
        sortField: "",
        sortOrder: "",
        keyword: "",
        account: (query.account as string) || "",
        mainAccount: "",
        corporateName: (query.companyName as string) || "",
        status: "",
        page: 1,
        size: config.get("pageSize")
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};
const resetParams = () => {
    filterParams.keyword = "";
    filterParams.account = "";
    filterParams.mainAccount = "";
    filterParams.corporateName = "";
    filterParams.status = "";
    filterParams.page = 1;
    loadFilter();
};
// 初始化加载
loadFilter();
useRouter().replace({ query: {} });
const onChange = (status: number) => {
    filterParams.status = status;
    filterParams.page = 1;
    loadFilter();
};
const handleSwitchChange = (params: any) => {
    const { data, row } = params;
    ElMessageBox.confirm(t("确认修改当前店铺的开业状态吗?"), t("提示"), { confirmButtonText: t("确认"), cancelButtonText: t("取消"), type: "warning" })
        .then(() => {
            row.status = row.status == 1 ? 10 : 1;
            const result = updateShopFiled({ ...data, id: row.shopId, val: row.status });
            message.success(t("修改成功"));
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
// onMounted(() => {
//     if (query.id) {
//         ElMessageBox.confirm(`为[${query.companyName || "--"}]立即创建店铺?`, "提示", {
//             closeOnClickModal: false,
//             showClose: false,
//             confirmButtonText: "确认",
//             cancelButtonText: "取消",
//             type: "warning"
//         })
//             .then(() => {
//                 merchantId.value = Number(query.id);
//                 dialogForm.value.show();
//             })
//             .catch(() => {
//                 merchantId.value = 0;
//                 clearUrlParams();
//             });
//     }
// });
const closeCallback = () => {
    merchantId.value = 0;
    clearUrlParams();
};
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

.tabs {
    flex-wrap: wrap;
    gap: 10px !important;
    margin-left: 10px;

    .item {
        padding: 0px 10px;
        text-align: center;
        height: 25px;
        line-height: 25px;
        color: #333;
        margin-right: 5px;
        font-size: 14px;
        border-radius: 2px;
        border: 1px solid #fff;
        cursor: pointer;

        &:hover {
            color: var(--tig-primary);
        }
    }

    .active {
        color: var(--tig-primary);
        background: var(--tig-item-active-bg);
        border: 1px solid var(--tig-primary);
    }
}
</style>
