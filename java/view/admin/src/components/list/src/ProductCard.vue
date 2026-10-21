/** 商品卡片组件 - 在列表中展示商品缩略图、名称和SKU信息 */
<template>
    <div class="displayRow">
        <div v-if="picThumb" class="orderImage">
            <el-image style="width: 50px; height: 50px" :src="imageFormat(picThumb)" fit="cover" />
        </div>
        <div style="flex: 1;">
            <div class="product-name-wrap">
                <slot name="text-pre"></slot>
                <el-tooltip :content="productName || ''" effect="light" placement="top" popper-class="product-name-tooltip">
                    <p class="productName">
                        {{ productName }}
                    </p>
                </el-tooltip>
                <slot name="name-suffix"></slot>
            </div>
            <p class="color999 sku" v-if="skuData">
                <div class="sku_item" v-for="(item, index) in skuData">
                    <span>{{ item?.name }}：{{ item?.value }}</span>
                    <span v-if="index < skuData.length - 1">&nbsp;|&nbsp;</span>
                </div>
            </p>
            <slot name="name-append"></slot>
        </div>
        <br/>
        <el-tag v-if="productType === 0" disable-transitions effect="plain" size="small" type="danger">{{ $t('虚拟产品') }} </el-tag>
    </div>
</template>
<script lang="ts" setup>
import {imageFormat} from "@/utils/format";

const props = defineProps({
    /** 商品ID */
    productId: Number,
    /** 商品类型：0-虚拟商品 1-实物商品 */
    productType: { type: Number, define: 1 },
    /** 商品链接 */
    url: String,
    /** 商品缩略图 */
    picThumb: String,
    /** 商品名称 */
    productName: String,
    /** SKU数据数组 */
    skuData: Array,
});

</script>
<style lang="less" scoped>
.displayRow {
    display: flex;
    flex-direction: row;
    gap: 8px;
    flex: 1;
    .sku{
        font-size: 12px !important;
        color: #999;
        display: flex;
        flex-wrap: wrap;
    }

    .color999 {
        color: #666666;
    }
    .product-name-wrap {
        display: flex;
        flex: 1;
    }
    .productName{
        // max-width: 300px;
        flex: 1;
        width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

    }
    .alink{line-height: 22px;display: block;}
    .orderImage {
        min-width: 50px;
        min-height: 50px;
        max-width: 50px;
        max-height: 50px;
        border: 1px solid #eee;
        margin-top: 5px;
    }
}


</style>

<style lang="less">
.product-name-tooltip {
    max-width: 280px;
    white-space: normal;
    word-break: break-word;
}
</style>
