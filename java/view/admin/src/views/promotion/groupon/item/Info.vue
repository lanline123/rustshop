<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('商品名称：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('请输入商品名称')"
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
                                    <label class="control-label"><span>商品分组：</span></label>
                                    <div class="control-container">
                                        <SelectProductGroup v-model:productGroupId="filterParams.productGroupId"
                                            @onChange="onSearchSubmit">
                                        </SelectProductGroup>
                                    </div>
                                </div>
                            </div> -->
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('商品品牌：') }}</span></label>
                                    <div class="control-container">
                                        <SelectBrand v-model:brandId="filterParams.brandId" @onChange="onSearchSubmit"> </SelectBrand>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="handleReset">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="product.productId" @selection-change="onSelectChange" :total="total" :loading="loading">
                            <!-- <el-table-column type="selection" width="32" /> -->
                            <el-table-column :label="$t('商品信息')" prop="productName" width="330">
                                <template #default="{ row }">
                                    <div class="product-info">
                                        <img :src="imageFormat(row.product.picThumb)" height="50" width="50" />
                                        <p>{{ row.product.productName }}</p>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('销售价')" align="center">
                                <template #default="{ row }">
                                    {{ row.product?.productPrice }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="210" :label="$t('操作')" fixed="right">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <span v-if="row.isO2oPlatformNotAllowPlatform">{{ $t('无法添加，该商品未分配至该组织') }}</span>
                                        <span v-else-if="row.isTimeDiscountProduct">{{ $t('【限时折扣商品】不支持参加此活动') }}</span>
                                        <span v-else-if="row.isReservationProduct">{{ $t('【预约商品】不支持参加此活动') }}</span>
                                        <span v-else-if="row.isPresaleProduct">{{ $t('【预售商品】不支持参加此活动') }}</span>
                                        <span v-else-if="row.isSeckillProduct">{{ $t('【秒杀商品】不支持参加此活动') }}</span>
                                        <span v-else-if="row.isOtherGrouponProduct">{{ $t('已参加其他拼团活动') }}</span>
                                        <span v-else-if="row.isAdded === 1">{{ $t('已添加') }}</span>
                                        <DialogForm
                                            v-else
                                            isDrawer
                                            :title="$t('拼团价设置')"
                                            :width="groupType == 2 ? '750px' : '660px'"
                                            path="promotion/groupon/item/setup/Info"
                                            @okCallback="loadFilter"
                                            :params="{
                                                act: 'add',
                                                grouponPromotionId,
                                                product: {
                                                    ...row.product,
                                                    productSkus: row.productSkus
                                                },
                                                groupType: groupType
                                            }"
                                        >
                                            <a class="btn-link">{{ $t('添加') }}</a>
                                        </DialogForm>
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
                </div>
                <div v-if="total > 0" class="pagination-con">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { useConfigStore } from "@/store/config";
import { getGrouponItemProductList } from "@/api/promotion/groupon";
import { GrouponItemProductFilterParams, GrouponItemProductFilterState } from "@/types/promotion/groupon";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { message } from "ant-design-vue";
import { reactive, ref } from "vue";
import { SelectBrand } from "@/components/select";
import SelectProductGroup from "@/components/select/src/SelectProductGroup.vue";
import { useRouter } from "vue-router";
const config: any = useConfigStore();
const props = defineProps({
    grouponPromotionId: {
        type: Number,
        default: 0
    },
    groupType: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
// const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const groupType = ref<number>(props.isDialog ? props.groupType : Number(query.groupType));
const filterParams = reactive<GrouponItemProductFilterParams>({
    sortField: "",
    sortOrder: "",
    keyword: "",
    page: 1,
    size: config.get("pageSize"),
    grouponPromotionId: props.grouponPromotionId
});
const loading = ref<boolean>(true);
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const total = ref<number>(0);
const filterState = ref<GrouponItemProductFilterState[]>([]);
const selectedIds = ref<number[]>([]);
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getGrouponItemProductList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSelectChange = (e: any) => {
    if (e.checkBox) {
        if (selectedIds.value.indexOf(e.grouponPromotionId) === -1) {
            selectedIds.value.push(e.grouponPromotionId);
        }
    } else {
        let index = selectedIds.value.indexOf(e.grouponPromotionId);
        if (index !== -1) {
            selectedIds.value.splice(index, 1);
        }
    }
};

const handleReset = () => {
    console.log("handleReset");
    filterParams.keyword = "";
    filterParams.page = 1;
    filterParams.sortField = "";
    filterParams.sortOrder = "";
    filterParams.brandId = undefined;
    filterParams.productGroupId = undefined;
    loadFilter();
};

const onSearchSubmit = () => {
    loadFilter();
};

loadFilter();

// 表单提交
const onFormSubmit = () => {
    emit("submitCallback", true);
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.product-info {
    display: flex;
    align-items: center;
    min-height: 50px;

    img {
        margin-right: 10px;
    }
}
</style>
