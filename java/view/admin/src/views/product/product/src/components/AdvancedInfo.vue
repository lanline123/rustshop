<template>
    <el-form-item v-if="adminType === 'admin'" :label="$t('加入推荐')">
        <Checkbox v-model="formState.isBest" :disabled="examine == 1">
            <template #default> {{ $t('精品') }}</template>
        </Checkbox>
        <Checkbox v-model="formState.isNew" :disabled="examine == 1">
            <template #default> {{ $t('新品') }}</template>
        </Checkbox>
        <Checkbox v-model="formState.isHot" :disabled="examine == 1">
            <template #default> {{ $t('热销') }}</template>
        </Checkbox>
    </el-form-item>
    <!-- <el-form-item class="inner-item" label="供应商" prop="suppliersId">
            <el-select v-model="formState.suppliersId" :disabled="examine == 1" placeholder="请选择">
                <el-option :value="0" label="请选择"></el-option>
                <el-option v-for="item in formState.suppliersList" :key="item.suppliersId" :label="item.suppliersName" :value="item.suppliersId" />
            </el-select>
        </el-form-item> -->

    <el-form-item :label="$t('服务说明')" prop="productServiceIds">
        <Checkbox v-for="(item, index) in formState.serviceList" :key="index" v-model="item.check" :disabled="examine == 1">
            <template #default>{{ item["productServiceName"] }}</template>
        </Checkbox>
    </el-form-item>
    <!-- 少了售后服务 -->
    <!-- <el-form-item label="货到付款" prop="isSupportCod">
                        <el-radio-group v-model="formState.isSupportCod" :disabled="examine == 1" style="width: 100%">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group>
                        <div class="extra">勾选“是”表示该商品可以使用货到付款</div>
                    </el-form-item> -->
    <!-- 少了开售时间 -->
    <!-- 少了定时下架 -->
    <!-- 少了起售数量 -->
    <el-form-item class="inner-item" :label="$t('显示销量')" prop="virtualSales">
        <div>
            <TigInput v-model="formState.virtualSales" :disabled="examine == 1" class="InputBox" type="integer" width="190px"></TigInput>
            <div class="extra">{{ $t('显示销量会随下单而增加，但不是真实销售数据') }}</div>
        </div>
    </el-form-item>
    <el-form-item :label="$t('相关商品')" prop="productRelated">
        <SelectProduct v-if="!loading" v-model:ids="formState.productRelated" :disabled="examine == 1" :max="10"></SelectProduct>
        <div class="extra">{{ $t('最多添加10个商品，仅用于在商品详情页展示') }}</div>
    </el-form-item>
    <template v-if="adminType === 'admin'">
        <el-form-item :label="$t('关联文章')" prop="productArticleList">
            <SelectArticle v-if="!loading" v-model:modelValue="formState.productArticleList" :disabled="examine == 1" :max="10"></SelectArticle>
            <div class="extra">{{ $t('最多添加10篇文章') }}</div>
        </el-form-item>
    </template>
</template>
<script lang="ts" setup>
import { ProductFormState } from "@/types/product/product.d";
import { Checkbox } from "@/components/radio";
import { SelectArticle, SelectProduct } from "@/components/select";
import { getAdminType } from "@/utils/storage";
const adminType = getAdminType();
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: 0
    },
    examine: {
        type: Number,
        default: 0
    },
    loading: {
        type: Boolean,
        default: false
    }
});
const formState = defineModel<ProductFormState>("formState", { default: {} });
</script>
<style lang="less" scoped></style>
