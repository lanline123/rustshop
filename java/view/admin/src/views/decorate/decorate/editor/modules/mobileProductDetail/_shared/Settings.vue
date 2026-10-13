<template>
    <perfect-scrollbar class="decorate-edit-con product-detail-settings">
        <div class="dec-edit-title">
            <h3>{{ $t("商品详情") }}</h3>
            <div class="dec-edit-desc">{{ $t("配置商品详情页的图片、标题、价格、信息区块与购买栏样式") }}</div>
        </div>

        <div class="dec-edit-title-box"><div class="title">{{ $t("图片设置") }}</div></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("图片比例") }}</div></div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.imageRatio">
                    <el-radio-button label="4:3">4:3</el-radio-button>
                    <el-radio-button label="1:1">1:1</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("图片圆角") }}</div></div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.imageRadiusStyle">
                    <el-radio-button label="square">{{ $t("直角") }}</el-radio-button>
                    <el-radio-button label="round">{{ $t("小圆角") }}</el-radio-button>
                    <el-radio-button label="large">{{ $t("大圆角") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-title-box"><div class="title">{{ $t("标题设置") }}</div></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("标题大小") }}</div></div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.titleSize">
                    <el-radio-button label="large">{{ $t("大") }}</el-radio-button>
                    <el-radio-button label="medium">{{ $t("中") }}</el-radio-button>
                    <el-radio-button label="small">{{ $t("小") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("标题颜色") }}</div></div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.titleColor" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("最高显示行数") }}</div></div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.titleMaxLines">
                    <el-radio-button :label="1">{{ $t("1行") }}</el-radio-button>
                    <el-radio-button :label="2">{{ $t("2行") }}</el-radio-button>
                    <el-radio-button :label="3">{{ $t("3行") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-title-box"><div class="title">{{ $t("价格设置") }}</div></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("价格颜色") }}</div></div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.priceColor" />
            </div>
        </div>

        <div class="dec-edit-title-box"><div class="title">{{ $t("购买栏设置") }}</div></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("购买栏样式") }}</div></div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.buyBarStyle">
                    <el-radio-button :label="1">{{ $t("圆润风") }}</el-radio-button>
                    <el-radio-button :label="2">{{ $t("直角风") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("按钮圆角") }}</div></div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        v-model="module.buttonRadius"
                        :min="0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                    />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("显示商城首页") }}</div></div>
            <div class="dec-edit-group-con"><el-switch v-model="module.showHome" /></div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("显示客服入口") }}</div></div>
            <div class="dec-edit-group-con"><el-switch v-model="module.showService" /></div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("显示购物车") }}</div></div>
            <div class="dec-edit-group-con"><el-switch v-model="module.showCart" /></div>
        </div>
        <div v-if="isMerchant()" class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("显示店铺信息") }}</div></div>
            <div class="dec-edit-group-con"><el-switch v-model="module.showShop" /></div>
        </div>

        <div class="dec-edit-title-box"><div class="title">{{ $t("背景设置") }}</div></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title"><div class="label">{{ $t("背景颜色") }}</div></div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.backgroundColor" />
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { ProductDetailModule } from "./types";
import { isMerchant } from "@/utils/version";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ProductDetailModule);
</script>
<style lang="scss" scoped>
.product-detail-settings {
}
</style>
