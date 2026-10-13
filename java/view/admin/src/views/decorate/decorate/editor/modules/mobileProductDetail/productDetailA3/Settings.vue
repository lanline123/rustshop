<template>
    <perfect-scrollbar class="decorate-edit-con product-detail-a3-settings">
        <div class="dec-edit-title">
            <h3>{{ $t("商品详情") }}</h3>
            <div class="dec-edit-desc">{{ $t("配置跨境风商品详情页的图片、标题、价格与购买栏样式") }}</div>
        </div>


        <div class="dec-edit-title-box">
            <div class="title">{{ $t("商品图片区") }}</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("轮播图比例") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.imageRatio">
                    <el-radio-button label="4:3">4:3</el-radio-button>
                    <el-radio-button label="1:1">1:1</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("轮播图圆角") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.imageRadiusStyle">
                    <el-radio-button label="square">{{ $t("直角") }}</el-radio-button>
                    <el-radio-button label="round">{{ $t("圆角") }}</el-radio-button>
                    <el-radio-button label="large">{{ $t("大圆角") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("轮播图左右边距") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.imageMargin" :min="0" :max="50" show-input :show-input-controls="false" size="small" input-size="default" />
                </div>
            </div>
        </div>

        <div class="dec-edit-title-box">
            <div class="title">{{ $t("商品标题及价格") }}</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("标题大小") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.titleSize">
                    <el-radio-button label="small">{{ $t("小") }}</el-radio-button>
                    <el-radio-button label="medium">{{ $t("中") }}</el-radio-button>
                    <el-radio-button label="large">{{ $t("大") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("标题颜色") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.titleColor" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("最大展示行数") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.titleMaxLines">
                    <el-radio-button :label="1">{{ $t("一行") }}</el-radio-button>
                    <el-radio-button :label="2">{{ $t("两行") }}</el-radio-button>
                    <el-radio-button :label="3">{{ $t("三行") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("价格颜色") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.priceColor" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("商品划线价颜色") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.marketPriceColor" />
            </div>
        </div>

        <template v-if="isMerchant()">
            <div class="dec-edit-title-box">
                <div class="title">{{ $t("店铺设置") }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t("显示店铺信息") }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group v-model="visShop">
                        <el-radio-button :label="1">{{ $t("显示") }}</el-radio-button>
                        <el-radio-button :label="0">{{ $t("隐藏") }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </template>

        <div class="dec-edit-title-box">
            <div class="title">{{ $t("购买按钮区") }}</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("底部悬浮购买样式") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.buyBarStyle">
                    <el-radio-button :label="2">{{ $t("简约风") }}</el-radio-button>
                    <el-radio-button :label="1">{{ $t("经典风") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="module.buyBarStyle === 2" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("按钮圆角") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.buttonRadius" :min="0" :max="50" show-input :show-input-controls="false" size="small" input-size="default" />
                </div>
            </div>
        </div>

        <div class="dec-edit-title-box">
            <div class="title">{{ $t("背景设置") }}</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("背景颜色") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.backgroundColor" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("内容背景颜色") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.contentBackgroundColor" />
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
/**
 * 商品详情 A3（跨境风）- 设置组件
 * 对应 tigshop-overseas-uniapp 商品详情页：图片、标题、价格、购买栏样式与背景。
 * 内容区块固定展示，不提供显隐配置；底部购买栏为两枚整宽按钮。
 */
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { ProductDetailModule } from "../_shared/types";
import { isMerchant } from "@/utils/version";

defineOptions({ name: "ProductDetailA3Settings" });

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ProductDetailModule);

const visShop = computed({
    get: () => (module.value.showShop !== false ? 1 : 0),
    set: (value: number) => {
        module.value.showShop = value === 1;
    }
});

</script>
<style lang="scss" scoped>
.product-detail-a3-settings {
    height: 100%;
}
</style>
