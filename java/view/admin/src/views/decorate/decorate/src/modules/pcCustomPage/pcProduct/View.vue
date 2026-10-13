<template>
    <TotalContainer
        :module="module"
        :class="
            'module-ad-con module-goods_ad ad-style__' +
            module.style +
            ' ad-buy_btn_style__' +
            module.buyBtnStyle +
            ' ad-goods_style__' +
            module.goodsStyle +
            ' ad-goods_radio_style__' +
            module.goodsRadioStyle +
            ' ad-text_align__' +
            module.textAlign +
            ' ad-text_weight__' +
            module.textWeight +
            ' ad-goods_name_row__' +
            module.goodsNameRow +
            ' ad-goods_name_padding__' +
            module.goodsNamePadding +
            ' ad-goods-title_style__' +
            module.title?.titleStyle
        "
    >
        <Title :module="module.title"></Title>
        <!-- 占位显示：没有商品时 -->
        <div v-if="!productList || productList.length === 0" class="module-ad-empty empty-product">
            <div class="image-empty-bg"></div>
            <div class="desc">{{ $t('点击添加商品') }}</div>
        </div>
        <!-- 有商品时显示商品列表 -->
        <template v-else-if="productList.length > 0">
            <div class="goods-ad-warp">
                <div class="goods-ad-con">
                    <ProductCard v-for="item in productList" :key="item.productId" :module="module" :product="item" :size="getSize()"></ProductCard>
                </div>
            </div>
        </template>
    </TotalContainer>
</template>

<script lang="ts" setup>
//商品列表组件
import { ref, computed, Ref } from "vue";
import { getProductList } from "@/api/product/product";
import { TotalContainer, ProductCard } from "@/components/decorate/index";
import Title from "@/views/decorate/decorate/src/modules/pcCustomPage/components/Title.vue";
import { ModuleActivityType, ModuleType } from "@/types/decorate/decorate";
import { defaultFrame, defaultProducts, defaultTitle, mergeDefaultModule } from "@/views/decorate/decorate/src/modules";

const props = defineProps({
    moduleIndex: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: -1
    }
});

const module = defineModel<ModuleType & ModuleActivityType>("module") as Ref<ModuleType & ModuleActivityType>;
const defaultModule = ref({
    style: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    goodsNamePadding: 1,
    showName: 1,
    showBrief: 1,
    showPrice: 1,
    goodsPadding: 5,
    buyBtnStyle: 1,
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    imgPadding: 1,
    waterfall: 0,
    swiperPageColor: "",
    frame: defaultFrame,
    title: defaultTitle,
    products: defaultProducts
});
mergeDefaultModule(module.value, defaultModule.value);

const paramsValueMap: { [key: number]: string[] } = {
    1: ["productIds"],
    2: ["productCategoryId", "productNumber"],
    3: ["productTag", "productNumber"]
};
const paramsKeyMap: { [key: string]: string } = {
    productIds: "ids",
    productCategoryId: "categoryId",
    productNumber: "size",
    productTag: "introType"
};
const productList = ref<any[]>([]);
const _getproductList = async () => {
    let param: any = {};
    if (typeof module.value.products === "object") {
        if (module.value.products.productSelectType) {
            const type = module.value.products.productSelectType;
            const keys = paramsValueMap[type];
            if (type == 1) {
                param[paramsKeyMap[keys[0]]] = module.value.products.productIds.join(",");
                param.size = module.value.products.productIds.length || 15;
            } else {
                param.page = 1;
                keys.forEach((key) => {
                    param[paramsKeyMap[key]] = module.value.products[key];
                });
            }
        }
    }
    if (props.shopId && props.shopId != -1) {
        param.shopId = props.shopId;
    }
    try {
        const result = await getProductList(param);
        productList.value = result.records;
    } catch (error) {
        console.log(error);
    }
};
_getproductList();

const getSize = (): number => {
    // 根据style返回每行显示的商品数量
    const style = typeof module.value.style === "string" ? Number(module.value.style) : module.value.style;

    switch (style) {
        case 1:
            return 4;
        case 2:
            return 5;
        default:
            return 4;
    }
};

// 背景色样式
const backgroundColorStyle = computed(() => {
    return module.value.backgroundColor && module.value.backgroundColor.trim() !== "" ? module.value.backgroundColor : "transparent";
});
</script>

<style lang="less" scoped>
/*商品类型*/
.module-goods_ad {
    .goods-ad-warp {
        width: 100%;
        box-sizing: border-box;

        .goods-ad-con {
            display: flex;
            flex-wrap: wrap;
            width: 100%;
        }
    }
}

/* Style 1: 单列 */
.ad-style__1 .goods-ad-warp .goods-ad-con {
    display: flex;
    // flex-direction: column;
}

/* Style 2: 两列 */
.ad-style__2 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}

/* Style 3: 三列 */
.ad-style__3 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}

/* Style 4: 特殊布局 */
.ad-style__4 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}

/* Style 6: 横向滑动 */
.ad-style__6 .goods-ad-warp {
    overflow: hidden;

    .goods-ad-con {
        display: flex;
        flex-wrap: nowrap;
        overflow-x: auto;
        -webkit-overflow-scrolling: touch;
        scrollbar-width: none; /* Firefox */
        -ms-overflow-style: none; /* IE and Edge */

        &::-webkit-scrollbar {
            display: none; /* Chrome, Safari, Opera */
        }
    }
}

/* 购买按钮样式 */
.ad-buy_btn_style__0 .goods-ad-warp .goods-ad-item .item-info .item-buy {
    display: none;
}

/* 商品样式 */
.ad-goods_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .item-con {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.ad-goods_style__3 .goods-ad-warp .goods-ad-con .goods-ad-item .item-con {
    border: 1px solid rgba(50, 50, 51, 0.1);
}

.ad-goods_style__4 .goods-ad-warp .goods-ad-item .item-info {
    background: transparent;
}

/* 圆角样式 */
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .item-con {
    border-radius: 6px;
}

.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .item-con .item-photo {
    border-radius: 6px 6px 0 0;
    overflow: hidden;
}

.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .item-con .item-info {
    border-radius: 0 0 6px 6px;
}

/* 文本对齐 */
.ad-text_align__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    text-align: center;
}

/* 文本粗细 */
.ad-text_weight__2 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
    font-weight: bold;
}

/* 商品名称行数 */
.ad-goods_name_row__1 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
    line-height: 20px;
    height: 20px;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

/* 商品名称内边距 */
.ad-goods_name_padding__0 .goods-ad-warp .goods-ad-item .item-info .item-name {
    margin-left: 0;
    margin-right: 0;
}

.ad-goods_name_padding__0 .goods-ad-warp .goods-ad-item .item-info .item-action {
    padding-left: 0;
    padding-right: 0;
}

/* 标题样式 */
.ad-goods-title_style__3 .goods-ad-warp {
    border-top-left-radius: 15px;
    margin-top: -25px !important;
    overflow: hidden;
    background: #fff;
    padding-top: 10px;
    position: relative;
}

/* 占位图片样式 */
.module-ad-empty {
    padding-top: 30px;
    padding-bottom: 50px;
    text-align: center;
    background-color: #f2f4f6;

    .image-empty-bg {
        background: url("@/views/decorate/decorate/src/img/empty-img-bg.png") no-repeat center center;
        background-size: 100% auto;
        width: 100px;
        margin: 0 auto 10px;
        height: 100px;
    }

    .desc {
        text-align: center;
        color: #888;
        font-size: 14px;
    }
}

/* 背景色设置 */
:deep(.base-class) {
    background-color: v-bind("backgroundColorStyle") !important;
}
</style>
