<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="notice-warp">
                        <p> {{ $t('提示：') }}</p>
                        <p> {{ $t('1、执行后如非需停止任务，请不要刷新或关闭页面！') }}</p>
                        <p> {{ $t('2、批量更新可以通过原图重新生成商品缩略图和主图，包括水印') }}</p>
                        <p> {{ $t('3、长宽大于800px的图片会自动生成水印（需在设置里添加水印图片）') }}</p>
                        <p> {{ $t('4、网络图片会自动忽略') }}</p>
                    </div>
                    <div class="lyecs-form-table">
                        <el-form :model="formState" label-width="auto">
                            <el-form-item :label="$t('处理范围')">
                                <SelectGoodsRange v-model:rangeType="formState.dealRange"  v-model:rangeIds="formState.rangeIds" type="productBatch"></SelectGoodsRange>
                            </el-form-item>
                            <el-form-item :label="$t('处理方式')">
                                <el-radio-group v-model="formState.isReplace">
                                    <el-radio :value="1">{{ $t('覆盖旧图，图片地址不变') }}</el-radio>
                                    <el-radio :value="0">{{ $t('删除旧图，生成新图片地址') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item label=" ">
                                <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit(formRef)">{{ $t('执行操作') }}</el-button>
                                <!-- <el-button ref="submitBtn" class="form-submit-btn">重置</el-button> -->
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
import { reactive, ref, shallowRef} from 'vue';
import SelectGoodsRange from "@/components/select/src/SelectGoodsRange.vue";
import {ProductBatchImageFormState} from '@/types/product/productBatch.d';
import {productBatchDealSubmit} from '@/api/product/productBatch';
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formRef = shallowRef();
const formState = reactive<ProductBatchImageFormState>({   //初使化用于查询的参数
    dealRange: 0,
    dealType: 1,
    isReplace: 1,
    rangeIds: []
});
const onSubmit = async (e:any) => {
    if(formState.dealRange == 1 && formState.rangeIds.length < 1){
        message.error(t('请选择分类'));
        return;
    }
    if(formState.dealRange == 2 && formState.rangeIds.length < 1){
        message.error(t('请选择品牌'));
        return;
    }
    if(formState.dealRange == 3 && formState.rangeIds.length < 1){
        message.error(t('请选择商品'));
        return;
    }
    try {
        const result = await productBatchDealSubmit({...formState });
        console.log(result);
        // message.success(t('操作成功'));
    } catch (error: any) {
        // message.error(error.message);
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
    padding-left: 30px;
}
</style>
