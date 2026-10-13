<template>
    <div>
        <div class="inp-box flex flex-align-center flex-wrap" v-if="types.length > 0" style="gap: 10px;margin-bottom:10px;">
            <div>{{ $t('批量填充：') }}</div>
            <div v-for="(item, index) in types" :key="index">
                <TigInput v-if="item.type == 'input'" v-model="formState[item.key]" :placeholder="item.lable" width="130px" />
                <el-select v-if="item.type == 'status'" v-model="formState[item.key]" style="width: 130px;" :placeholder="item.lable">
                    <el-option v-for="option in productStatusList" :label="option.label" :value="option.value" />
                </el-select>
                <!-- 运费模板 -->
                <el-select v-if="item.type == 'shippingTpl'" v-model="formState[item.key]" style="width: 130px;" :placeholder="item.lable">
                    <el-option v-for="option in shippingTplList" :label="option.shippingTplName" :value="option.shippingTplId" />
                </el-select>
                <el-select v-if="item.type == 'select'" v-model="formState[item.key]" style="width: 130px;" :placeholder="item.lable">
                    <el-option v-for="option in selectList" :label="option.label" :value="option.value" />
                </el-select>
                <!-- 商品服务（多选） -->
                <el-select
                    v-if="item.type == 'productService'"
                    v-model="formState[item.key]"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    clearable
                    style="width: 200px;"
                    :placeholder="item.lable"
                >
                    <el-option
                        v-for="option in productServiceList"
                        :key="option.productServiceId"
                        :label="option.productServiceName"
                        :value="option.productServiceId"
                    />
                </el-select>
                <SelectCategory v-if="item.type == 'category'" v-model:categoryId="formState[item.key]" :categoryList="categoryList"></SelectCategory>
                <SelectBrand v-if="item.type == 'brand'" v-model:brandId="formState[item.key]" :firstWordList="firstWordList" :brandList="brandList" :allBrandList="allBrandList"></SelectBrand>
            </div>
            <el-button @click="onConfirm">{{ $t('批量填充') }}</el-button>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, PropType } from "vue"
import { SelectBrand, SelectCategory, SelectShop } from "@/components/select";
import { CategoryFilterState } from "@/types/product/category";
import { BrandFilterState } from "@/types/product/brand";
import type { ProductServicesFilterState } from "@/types/product/productServices";
const props = defineProps({
    // orgValue: [String, Number],  //父组件使用v-model双向绑定
    label: { type: String, default: '编辑项' },
    required: { type: Boolean, default: true },
    types: { type: Array as PropType<any[]>, default: [] },
    productStatusList: { type: Array as PropType<any[]>, default: [] },
    selectList: { type: Array as PropType<any[]>, default: [] },
    categoryList: {type: Array as PropType<CategoryFilterState[]>, default: () => []},
    firstWordList: {type: Array as PropType<string[]>, default: () => []},
    brandList: {type: Array as PropType<BrandFilterState[]>, default: () => []},
    allBrandList: {type: Array as PropType<BrandFilterState[]>, default: () => []},
    shippingTplList: {type: Array as PropType<any[]>, default: () => []},
    productServiceList: {type: Array as PropType<ProductServicesFilterState[]>, default: () => []},
})
const visible = ref<any>(false);
const loaded = ref<Boolean>(false);
const formRef = ref();  //表单Ref
let formState = reactive<any>({});
const emit = defineEmits(['callback'])
 
const onConfirm = () => {
    emit('callback', formState)
}

</script>

<style lang="less" scoped>
</style>
