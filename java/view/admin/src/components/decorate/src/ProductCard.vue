<template>
    <div :style="{ padding: module.goodsPadding + 'px', width: 100 / size + '%' }" class="product-card">
        <div
            class="car-info"
            :class="[
                module.goodsStyle == 2
                    ? 'product-card-border-2'
                    : module.goodsStyle == 3
                      ? 'product-card-border-3'
                      : module.goodsStyle == 1
                        ? ''
                        : 'product-card-border-4',
                module.goodsRadioStyle == 2 ? 'box-radius' : ''
            ]"
        >
            <div class="item-photo">
                <div class="item-content">
                    <el-image
                        :class="[module.goodsRadioStyle == 2 ? 'image-box-radius' : '']"
                        class="image-box"
                        :src="productImage(product)"
                    ></el-image>
                </div>
            </div>
            <div class="item-info" :style="{ padding: module.goodsNamePadding == 1 ? '0 10px' : '' }">
                <div class="item-name" :style="{ textAlign: module.textAlign == 1 ? 'left' : 'center' }">
                    <div
                        v-if="module.showName == 1"
                        class="item-product-name"
                        :style="{ fontWeight: module.textWeight == 1 ? '' : 'bold', height: module.goodsNameRow == 1 ? '20px' : '40px' }"
                    >
                        {{ product.productName }}
                    </div>
                    <div v-if="module.showBrief" class="item-brief">
                        {{ product.productBrief }}
                    </div>
                </div>
                <div class="item-action" :style="{ flexDirection: module.textAlign == 1 ? 'row' : 'column' }">
                    <div v-if="module.showPrice == 1" class=" ">
                        <Price
                            :fontStyle="{ color: '#FF0000', fontSize: '24px', fontWeight: module.textWeight == 1 ? '' : 'bold' }"
                            v-model="product.productPrice"
                        ></Price>
                    </div>
                    <div v-if="module.buyBtnStyle > 0" class="item-buy">
                        <ProductButton :buyBtnStyle="module.buyBtnStyle"></ProductButton>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import ProductButton from "./ProductButton.vue";
import Price from "./Price.vue";

const props = defineProps({
    module: { type: Object, default: {} },
    product: { type: Object, default: {} },
    size: { type: Number, default: 1 }
});
import { productImage } from "@/utils/format";
</script>
<style scoped lang="less">
.product-card {
    display: flex;
    box-sizing: border-box;

    .car-info {
        padding-bottom: 10px;
        box-sizing: border-box;
        width: 100%;
        display: flex;
        flex-direction: column;
        background-color: white;

        .item-photo {
            width: 100%;
            display: flex;

            .item-content {
                width: 100%;

                .image-box {
                    flex: 1;
                    width: 100%;
                }
            }
        }

        .item-info {
            display: flex;
            flex-direction: column;

            box-sizing: border-box;

            .item-name {
                display: flex;
                flex-direction: column;

                box-sizing: border-box;
                padding-bottom: 10px;

                .item-product-name {
                    font-size: 16px;
                    overflow: hidden; /* 隐藏超出容器的内容 */
                    text-overflow: ellipsis; /* 超出部分用省略号表示 */
                    line-height: 20px;
                }

                .item-brief {
                    line-height: 20px;
                    height: 20px;
                    color: #aaaaaa;
                    white-space: nowrap; /* 不换行 */
                    overflow: hidden; /* 隐藏超出容器的内容 */
                    text-overflow: ellipsis; /* 超出部分用省略号表示 */
                }
            }

            .item-action {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 10px;

                .item-price {
                    font-size: 20px;
                    color: var(--general);
                    line-height: 30px;
                    height: 30px;
                }

                .item-buy {
                    display: flex;
                    justify-content: flex-end;
                }
            }
        }
    }

    .product-card-border-2 {
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
    }

    .product-card-border-3 {
        border: 1px solid rgba(50, 50, 51, 0.1);
    }

    .product-card-border-4 {
        background-color: transparent;
    }
}

.box-radius {
    border-radius: 6px;
}

.image-box-radius {
    border-top-left-radius: 6px;
    border-top-right-radius: 6px;
}
</style>
