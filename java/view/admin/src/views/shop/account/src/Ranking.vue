<template>
    <div class="tit-box">
        <div class="tit">
            <span>{{ $t('{shopTypeName}余额排行', { shopTypeName }) }}</span>
        </div>
    </div>
    <div class="lyecs-table-list-warp">
        <div class="table-container">
            <el-table :data="filterState" :loading="loading" :total="total" row-key="logId" v-loading="loading">
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
                <el-table-column :label="$t('商户名称')" :width="200">
                    <template #default="{ row }">
                        <div v-if="row.merchant">
                            <span>{{ row.merchant.corporateName || "--" }}</span>
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
                                <a class="btn-link green" v-if="row.merchant.status == 1">{{ $t('(已认证)') }}</a>
                                <a class="btn-link grey" v-else>{{ $t('(未认证)') }}</a>
                            </DialogForm>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="`${shopTypeName}${$t('余额')}(${config.config.dollarSignCn})`">
                    <template #default="{ row }">
                        {{ priceFormat(row.shopMoney) || 0.0 }}
                    </template>
                </el-table-column>
                <el-table-column :label="$t('{shopTypeName}状态', { shopTypeName })">
                    <template #default="{ row }">
                        <template v-if="row.status == 10 || row.status == 4">
                            <StatusDot color="red" :flicker="true"></StatusDot>
                        </template>
                        <template v-if="row.status == 1">
                            <StatusDot color="green" :flicker="true"></StatusDot>
                        </template>
                        <span v-if="row.status === 10 || row.status == 4" style="color: red">{{ row.statusText }}</span>
                        <span v-else-if="row.status === 1" style="color: green">{{ row.statusText }}</span>
                    </template>
                </el-table-column>
                <template #empty>
                    <div class="empty-warp">
                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                    </div>
                </template>
            </el-table>
            <div v-if="total > 0" class="pagination-con">
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { Image } from "@/components/image";
import { DialogForm } from "@/components/dialog";
import { priceFormat } from "@/utils/format";
import { OrderFilterParams } from "@/types/order/order.d";
import { getShopList } from "@/api/shop/shop";
import { isMerchant, isStore } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopTypeName = isStore() ? t('门店') : isMerchant() ? t('店铺') : t('店铺')
const config: any = useConfigStore();
import { useListRequest } from '@/hooks/useListRequest';
const {
  listData: filterState,
  loading,
  total,
  selectedIds,
  filterParams,
  loadData: loadFilter,
} = useListRequest<any, OrderFilterParams>({
  apiFunction: getShopList,
  idKey: 'shopId',
  defaultParams: {
      shopType: isStore() ? 2 : isMerchant() ? 1 : 1,
      sortField: 'shopMoney',
      sortOrder: 'desc',
      page: 1,
      size: config.get("pageSize"),
  }
});

// 初始化加载
loadFilter();
</script>
<style lang="less" scoped>
.tit-box {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 0;
    margin-bottom: 20px;
    .tit {
        border-left: 3px solid #155bd4;
        padding-left: 10px;
        font-size: 14px;
    }
}
</style>
