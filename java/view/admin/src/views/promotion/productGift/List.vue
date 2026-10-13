<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入赠品名称')" clearable @clear="onSearchSubmit">
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <DialogForm isDrawer @okCallback="loadFilter" :title="$t('添加赠品')" width="900px"
                                                        path="promotion/productGift/Info" :params="{ act: 'add' }">
                                                <el-button type="primary">{{ $t('添加赠品') }}</el-button>
                                            </DialogForm>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm isDrawer @okCallback="loadFilter" title="添加赠品" width="900px"
                                                path="promotion/productGift/Info" :params="{ act: 'add' }">
                                        <el-button type="primary">添加赠品</el-button>
                                    </DialogForm>
                                   <el-popconfirm title="您确认要批量删除所选数据吗？" @confirm="onBatchSubmit('del')">
                                       <template #reference>
                                           <el-button :disabled="selectedIds.length === 0">批量删除</el-button>
                                       </template>
                                   </el-popconfirm>
                                   <span v-if="selectedIds.length > 0">
                                       已选择：<b>{{ selectedIds.length }}</b> 项
                                   </span>

                                </el-space>
                            </div>
                        </div> -->
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="id" @selection-change="onSelectChange" :total="total"
                            @sort-change="onSortChange" :loading="loading">
<!--                             <el-table-column type="selection" width="32" />-->
                            <el-table-column :label="$t('赠品名称')" prop="giftName" min-width="150"></el-table-column>
                            <el-table-column :label="$t('商品信息')" prop="productName" min-width="250">
                                <template #default="{ row }">
                                    <div class="product-info">
                                        <img :src="imageFormat(row.productInfo.picThumb)" height="50" width="50" style="min-width: 50px;" />
                                        <p>{{ row.productInfo.productName }}</p>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('赠品库存')" prop="giftStock" sortable="custom" min-width="110"></el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="isMobile ? 100 : 150">
                                <template #default="{ row }">
                                    <el-space :size="0" wrap>
                                        <DialogForm isDrawer @okCallback="loadFilter" :title="$t('编辑赠品')" width="900px"
                                            path="promotion/productGift/Info" :params="{ act: 'detail', id: row.giftId }">
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord @afterDelete="loadFilter" :requestApi="delProductGift"
                                            :params="{ id: row.giftId }">{{ $t('删除') }}</DeleteRecord>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import '@/style/css/list.less'
import { DialogForm } from '@/components/dialog'
import { DeleteRecord, Pagination } from '@/components/list';
import { ProductGiftFilterState, ProductGiftFilterParams } from '@/types/promotion/productGift.d';
import { getProductGiftList, delProductGift, batchSubmit } from "@/api/promotion/productGift";
import { useConfigStore } from "@/store/config"
import { imageFormat } from "@/utils/format";
import { ref, onMounted, onUnmounted } from "vue";
const config:any = useConfigStore();
// 基本参数定义
import { useListRequest } from '@/hooks/useListRequest';
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
} = useListRequest<ProductGiftFilterState, ProductGiftFilterParams>({
  apiFunction: getProductGiftList,
  idKey: 'id',
  defaultParams: {
      sortField: '',
      sortOrder: '',
      keyword: '',
      page: 1,
      size: config.get("pageSize"),
  }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
  await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();

const isMobile = ref(document.body.clientWidth < 768);
const handleResize = () => {
    isMobile.value = document.body.clientWidth < 768;
};

onMounted(() => {
    window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
    window.removeEventListener("resize", handleResize);
});

</script>
<style lang="less" scoped>
.product-info{
    display: flex;
    align-items: center;
    img{
        margin-right: 10px;
    }
}
</style>
