/**
 * @file 商品批量导出
 * @description 批量导出商品数据为Excel文件
 * @features
 * - 选择导出范围（全部/指定分类/指定商品）
 * - 导出为可编辑的CSV文件
 * - 支持重新导入修改后的文件
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="notice-warp">
                        <p> {{ $t('提示：') }}</p>
                        <p> {{ $t('1、导出的文件可编辑后进行修改后再通过“商品批量上传”或“商品批量修改修改”功能完成批量导入或修改') }}</p>
                        <p> {{ $t('2、本系统解决了CSV导入导出时的\',\'号和\'"\'号的问题，所以商品描述和商品介绍处可以不用再对这些符合进行额外处理！') }}</p>
                    </div>
                    <div class="lyecs-form-table">
                        <el-form :model="formState" label-width="auto">
                            <el-form-item :label="$t('处理范围')">
                                <SelectGoodsRange v-model:rangeType="formState.dealRange"  v-model:rangeIds="rangeIds" type="productBatch" :apiList="isVendorAdmin ? vendorGetProductList : undefined"></SelectGoodsRange>
                            </el-form-item>
                            <el-form-item label=" ">
                                <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()" :loading="Exportloading">{{ $t('提交') }}</el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import '@/style/css/list.less'
import { reactive, ref, watch } from 'vue';
import SelectGoodsRange from "@/components/select/src/SelectGoodsRange.vue";
import { ProductBatchExportFormState } from '@/types/product/productBatch.d';
import { productBatchExportSubmit } from '@/api/product/productBatch';
import { vendorProductBatchExportSubmit, getProductList as vendorGetProductList } from "@/api/vendor/product";
import { getAdminType } from "@/utils/storage";
import { message } from "ant-design-vue";
import requestExport from "@/utils/export";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const isVendorAdmin = getAdminType() === "vendor";
const rangeIds = ref<number[]>([]);
watch(rangeIds, (newVal, oldVal) => {
    if(newVal.length > 0){
        formState.rangeIds = newVal.join(',')  ;
    }
},{deep: true})
const formState = reactive<ProductBatchExportFormState>({   //初使化用于查询的参数
    dealRange: 0,
    dealType: 0,
    rangeIds: ""
});
const Exportloading = ref<boolean>(false)
const onSubmit = async () => {
    if(formState.dealRange == 1 && formState.rangeIds.length < 1){
        message.error(t('请选择分类'));
        return;
    }
    if(formState.dealRange == 2 && formState.rangeIds.length < 1){
        message.error(t('请选择品牌'));
        return;
    }
    if(formState.dealRange != 0 && formState.rangeIds.length < 1){
        message.error(t('请选择商品'));
        return;
    }
    Exportloading.value = true;
    try {
        const submitApi = isVendorAdmin ? vendorProductBatchExportSubmit : productBatchExportSubmit;
        const result = await submitApi({ ...formState });
        Exportloading.value = false;
        requestExport(result,t('商品批量导出'))
    } catch (error:any) {
        message.error(error.message);
    } finally {
        Exportloading.value = false;
    }
};

</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}

.lyecs-form-table {
    padding: 14px 0;
    background: #fff;
    border-radius: 6px;
    display: flex;
    align-content: center;
    margin-left: 30px;
}
</style>
