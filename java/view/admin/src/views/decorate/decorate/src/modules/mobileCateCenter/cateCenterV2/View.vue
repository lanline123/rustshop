<template>
    <div
        class="module-ad-con dec-cate-center-v2"
        :class="moduleClasses"
        :style="[
            frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom,
            decorateVars
        ]"
    >
        <!-- 搜索栏 -->
        <div :class="isOverseas() ? 'search-bar-box-overseas' : 'search-bar-box'">
            <div v-if="!isOverseas()" class="home-btn">
                <i class="iconfont-h5 icon-shouye"></i>
            </div>
            <div class="search-input-wrap">
                <i v-if="!isOverseas()" class="iconfont-h5 icon-sousuo"></i>
                <span class="search-placeholder">{{ $t('搜索商品') }}</span>
            </div>
        </div>

        <div class="dec-cate-center-v2__body">
            <!-- 左侧一级分类 -->
            <div class="dec-cate-center-v2__aside">
                <div
                    v-for="cat in rootCategories"
                    :key="cat.id"
                    class="dec-cate-center-v2__aside-item"
                    :class="{ 'is-active': activeRootId === cat.id }"
                    @click="handleRootClick(cat.id)"
                >
                    <span class="dec-cate-center-v2__aside-text">{{ cat.name }}</span>
                </div>
            </div>

            <!-- 右侧内容 -->
            <div class="dec-cate-center-v2__main">
                <!-- 二级分类标签 -->
                <div v-if="subCategories.length > 0" class="dec-cate-center-v2__subtabs-wrap">
                    <div class="dec-cate-center-v2__subtabs">
                        <div
                            v-for="sub in subCategories"
                            :key="sub.id"
                            class="dec-cate-center-v2__subtab"
                            :class="{ 'is-active': activeSubId === sub.id }"
                            @click="handleSubClick(sub.id)"
                        >
                            {{ sub.name }}
                        </div>
                    </div>
                    <div class="dec-cate-center-v2__subtabs-arrow">
                        <svg class="dec-cate-center-v2__subtabs-arrow-icon" viewBox="0 0 1024 1024" width="12" height="12" aria-hidden="true">
                            <path
                                d="M512 710.4L181.3 379.7c-12.5-12.5-12.5-32.8 0-45.3s32.8-12.5 45.3 0L512 619.7l285.3-285.3c12.5-12.5 32.8-12.5 45.3 0s12.5 32.8 0 45.3L512 710.4z"
                                fill="#999"
                            />
                        </svg>
                    </div>
                </div>

                <div class="dec-cate-center-v2__content">
                    <!-- 样式二：双列网格 (对应图一) -->
                    <div v-if="moduleConfig.styleType !== 3" class="dec-cate-center-v2__grid">
                        <div v-for="p in productList" :key="p.id" class="dec-cate-center-v2__grid-item">
                            <div class="pic"><img :src="p.cover" alt="" /></div>
                            <div v-if="moduleConfig.showName" class="title">{{ p.title }}</div>
                            <div class="action">
                                <div class="price">
                                    <span class="price-currency">{{ priceParts(p.price, config.config.dollarSign).currency }}</span>
                                    <span class="price-integer">{{ priceParts(p.price, config.config.dollarSign).integer }}</span>
                                    <span class="price-decimal">{{ priceParts(p.price, config.config.dollarSign).decimal }}</span>
                                </div>
                                <div v-if="moduleConfig.buyBtnStyle !== 0" class="item-buy">
                                    <a href="javascript:;" class="buy-btn lyecs-buyBtn">
                                        <template v-if="moduleConfig.buyBtnStyle === 1">
                                            <span class="iconfont-pc icon-gouwuche iconh5-style"></span>
                                        </template>
                                        <template v-if="moduleConfig.buyBtnStyle === 2">
                                            <span class="iconfont-pc icon-gouwuche1 iconh5-style"></span>
                                        </template>
                                        <template v-if="moduleConfig.buyBtnStyle === 3">
                                            <span class="iconfont-pc icon-jia iconh5-style"></span>
                                        </template>
                                        <template v-if="moduleConfig.buyBtnStyle === 4">
                                            <span class="iconfont-pc icon-jia1 iconh5-style"></span>
                                        </template>
                                        <template v-if="moduleConfig.buyBtnStyle === 5 || moduleConfig.buyBtnStyle === 6">
                                            <span :class="moduleConfig.buyBtnStyle === 6 ? 'activeBtnStyle' : 'outlineBtnStyle'">{{ $t('购买') }}</span>
                                        </template>
                                        <template v-if="moduleConfig.buyBtnStyle === 7 || moduleConfig.buyBtnStyle === 8">
                                            <span :class="moduleConfig.buyBtnStyle === 8 ? 'activeBtnStyle' : 'outlineBtnStyle'">{{ $t('马上抢') }}</span>
                                        </template>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 样式三：图文列表 (对应图二) -->
                    <div v-else class="dec-cate-center-v2__list">
                        <div v-for="p in productList" :key="p.id" class="dec-cate-center-v2__list-item">
                            <div class="pic"><img :src="p.cover" alt="" /></div>
                            <div class="info">
                                <div v-if="moduleConfig.showName" class="title">{{ p.title }}</div>
                                <div class="meta">
                                    <div class="price-box">
                                        <div class="price">
                                            <span class="price-currency">{{ priceParts(p.price, config.config.dollarSign).currency }}</span>
                                            <span class="price-integer">{{ priceParts(p.price, config.config.dollarSign).integer }}</span>
                                            <span class="price-decimal">{{ priceParts(p.price, config.config.dollarSign).decimal }}</span>
                                        </div>
                                    </div>
                                    <div v-if="moduleConfig.buyBtnStyle !== 0" class="item-buy">
                                        <a href="javascript:;" class="buy-btn lyecs-buyBtn">
                                            <template v-if="moduleConfig.buyBtnStyle === 1">
                                                <span class="iconfont-pc icon-gouwuche iconh5-style"></span>
                                            </template>
                                            <template v-if="moduleConfig.buyBtnStyle === 2">
                                                <span class="iconfont-pc icon-gouwuche1 iconh5-style"></span>
                                            </template>
                                            <template v-if="moduleConfig.buyBtnStyle === 3">
                                                <span class="iconfont-pc icon-jia iconh5-style"></span>
                                            </template>
                                            <template v-if="moduleConfig.buyBtnStyle === 4">
                                                <span class="iconfont-pc icon-jia1 iconh5-style"></span>
                                            </template>
                                            <template v-if="moduleConfig.buyBtnStyle === 5 || moduleConfig.buyBtnStyle === 6">
                                                <span :class="moduleConfig.buyBtnStyle === 6 ? 'activeBtnStyle' : 'outlineBtnStyle'">{{ $t('购买') }}</span>
                                            </template>
                                            <template v-if="moduleConfig.buyBtnStyle === 7 || moduleConfig.buyBtnStyle === 8">
                                                <span :class="moduleConfig.buyBtnStyle === 8 ? 'activeBtnStyle' : 'outlineBtnStyle'">{{ $t('马上抢') }}</span>
                                            </template>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch, Ref } from "vue";
import { isOverseas } from "@/utils/version";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const module = defineModel<any>("module") as Ref<any>;
const defaultModule = {
    styleType: 2,
    subTabStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    showName: 1,
    buyBtnStyle: 1,
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0 }
};
mergeDefaultModule(module.value, defaultModule);
const { frame } = module.value;
const frameFormat = computed(() => formatFrame(frame ?? {}));

const moduleConfig = computed(() => ({
    styleType: module.value.styleType ?? 2,
    subTabStyle: module.value.subTabStyle ?? 1,
    textAlign: module.value.textAlign ?? 1,
    textWeight: module.value.textWeight ?? 1,
    goodsNameRow: module.value.goodsNameRow ?? 2,
    showName: module.value.showName !== 0,
    buyBtnStyle: module.value.buyBtnStyle ?? 1
}));

// 装修颜色通过 CSS 变量传递给商品标题、价格和购买按钮
const decorateVars = computed(() => ({
    "--dec-cate-title": module.value.productTitleColor || "",
    "--dec-cate-price": module.value.priceColor || "",
    "--dec-cate-buy-btn": module.value.buyBtnColor || ""
}));

// 模块修饰类（与 Vort 原版一致）
const moduleClasses = computed(() => {
    const cls: string[] = [];
    cls.push(`dec-cate-center-v2--subtab-style-${moduleConfig.value.subTabStyle}`);
    cls.push(`dec-cate-center-v2--text-align-${moduleConfig.value.textAlign}`);
    cls.push(`dec-cate-center-v2--text-weight-${moduleConfig.value.textWeight}`);
    cls.push(`dec-cate-center-v2--name-row-${moduleConfig.value.goodsNameRow}`);
    cls.push(`dec-cate-center-v2--buy-btn-${moduleConfig.value.buyBtnStyle}`);
    return cls;
});

interface MockCategory {
    id: string;
    name: string;
    children: Array<{ id: string; name: string }>;
}
interface MockProduct {
    id: string;
    title: string;
    price: string;
    cover: string;
}

const rootCategories = ref<MockCategory[]>([
    {
        id: "c1",
        name: t('女装'),
        children: [
            { id: "c1-1", name: t('内衬') },
            { id: "c1-2", name: t('上装') },
            { id: "c1-3", name: t('裤装') },
            { id: "c1-4", name: t('裙装') }
        ]
    },
    {
        id: "c2",
        name: t('男装'),
        children: [
            { id: "c2-1", name: t('上装') },
            { id: "c2-2", name: t('裤装') }
        ]
    },
    {
        id: "c3",
        name: t('内衣'),
        children: [
            { id: "c3-1", name: t('睡衣') },
            { id: "c3-2", name: t('家居服') }
        ]
    },
    {
        id: "c4",
        name: t('鞋包'),
        children: [
            { id: "c4-1", name: t('女单鞋') },
            { id: "c4-2", name: t('女休闲鞋') },
            { id: "c4-3", name: t('女皮鞋') }
        ]
    },
    {
        id: "c5",
        name: t('母婴'),
        children: [
            { id: "c5-1", name: t('童装') },
            { id: "c5-2", name: t('玩具') }
        ]
    },
    {
        id: "c6",
        name: t('运动'),
        children: [
            { id: "c6-1", name: t('跑鞋') },
            { id: "c6-2", name: t('卫衣') }
        ]
    },
    {
        id: "c7",
        name: t('美妆'),
        children: [
            { id: "c7-1", name: t('彩妆') },
            { id: "c7-2", name: t('护肤') }
        ]
    },
    {
        id: "c8",
        name: t('配饰'),
        children: [
            { id: "c8-1", name: t('首饰') },
            { id: "c8-2", name: t('包包') }
        ]
    },
    { id: "c9", name: t('家电'), children: [{ id: "c9-1", name: t('厨房家电') }] },
    { id: "c10", name: t('家居'), children: [{ id: "c10-1", name: t('家纺') }] },
    { id: "c11", name: t('家装'), children: [{ id: "c11-1", name: t('绿植') }] },
    { id: "c12", name: t('食品'), children: [{ id: "c12-1", name: t('零食') }] },
    { id: "c13", name: t('国际'), children: [{ id: "c13-1", name: t('海淘') }] },
    { id: "c14", name: t('医药'), children: [{ id: "c14-1", name: t('常备') }] }
]);

const ALL_SUB_SUFFIX = "-all";
const buildAllSubId = (rootId: string) => `${rootId}${ALL_SUB_SUFFIX}`;

const activeRootId = ref<string>(rootCategories.value[0]?.id ?? "");
const activeSubId = ref<string>(buildAllSubId(rootCategories.value[0]?.id ?? ""));

const subCategories = computed(() => {
    const root = rootCategories.value.find((x) => x.id === activeRootId.value);
    if (!root) return [];
    return [{ id: buildAllSubId(root.id), name: t('全部商品') }, ...(root.children ?? [])];
});

// 源自 c:\Users\10132\Desktop\新建 文本文档 (2).txt 的真实数据
const TEXT_FILE_PRODUCTS = [
    {
        productName: t('【气质甜美衬衫短裤套装】夏季复古方领刺绣衬衣西裤两件'),
        price: "248.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680762243Myvkn91XZndQaLrMTf!!pic400x400.jpeg"
    },
    {
        productName: t('法式U领露锁骨短袖T恤女2023年夏季奶系温柔泡泡袖上衣'),
        price: "78.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680589546tPrOZyKK683sfEOCq7!!pic400x400.jpeg"
    },
    {
        productName: t('陕西正宗奶油亚特猕猴桃新鲜水果奇异果孕妇水果现货绿心猕猴桃 带箱2斤中果70-90g（超值装）'),
        price: "9.90",
        picUrl: "https://oss.tigshop.com/img/gallery/202509/1756782675WSx4GEiEA1a2TPj5V6.jpeg"
    },
    {
        productName: t('鲜生美国进口车厘子 黑珍珠大樱桃新鲜当季时令节日水果生鲜礼盒 1000g(净重) 超大果 JJ 28-30mm'),
        price: "168.00",
        picUrl: "https://oss.tigshop.com/img/gallery/202509/175678163620Qgvoe1xOz7mmmfcn.jpeg"
    },
    {
        productName: t('【时尚套装】23年春季新款气质西服领九分袖搭收腰显瘦纱裙套装'),
        price: "339.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/16807622759lrB7tSYTaDMl21ver!!pic400x400.jpeg"
    },
    {
        productName: t('法式西装领两件套纯色西装套裙女春秋复古设计感小众连衣裙两件套'),
        price: "287.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680762215SVhVhwTAK3TCRQdOQC!!pic400x400.jpeg"
    },
    {
        productName: t('羽绒服秋冬时尚休闲保暖舒适小清新外套上衣女'),
        price: "399.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680762133yqpJm3JLTLInfq4Gb1!!pic400x400.jpeg"
    },
    {
        productName: t('直筒90绒轻薄女士内胆圆领短款休闲外套'),
        price: "254.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/168076207428pJgHL7h3bVwGN5fN!!pic400x400.jpeg"
    },
    {
        productName: t('【精选好物】简洁可脱帽印花内里潮酷中长款保暖女式羽绒服'),
        price: "799.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680762049xrpLXPKPv7LRtNFhO8!!pic400x400.jpeg"
    },
    {
        productName: t('毛呢外套女春季翻领长袖英伦时尚百搭中长款上衣'),
        price: "374.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680761949huXDrN7BfQHZ6R0G6w!!pic400x400.jpeg"
    }
];

// 高保真图二(鞋子)数据，当点击鞋包类目时展示，使演示100%还原真实图片
const SHOES_PRODUCTS = [
    {
        productName: t('春季新款高帮女鞋时尚女潮板鞋百搭小白鞋女'),
        price: "179.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680589546tPrOZyKK683sfEOCq7!!pic400x400.jpeg"
    },
    {
        productName: t('韩版厚底高帮鞋潮流爆款熊猫鞋子女2023春季百搭休闲运动...'),
        price: "207.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680762243Myvkn91XZndQaLrMTf!!pic400x400.jpeg"
    },
    {
        productName: t('【简约高帮板鞋】清爽透气舒适女鞋双鞋带休闲板鞋耐磨耐穿...'),
        price: "160.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/16807622759lrB7tSYTaDMl21ver!!pic400x400.jpeg"
    },
    {
        productName: t('春季新款舒适旅游鞋小白鞋男士板鞋时尚休闲鞋潮流百搭运动...'),
        price: "239.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680762215SVhVhwTAK3TCRQdOQC!!pic400x400.jpeg"
    },
    {
        productName: t('【软底软面】2023春季新款女鞋子贝壳头小白鞋运动休闲鞋女'),
        price: "149.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/1680762133yqpJm3JLTLInfq4Gb1!!pic400x400.jpeg"
    },
    {
        productName: t('【热销爆款】春季小白鞋女板鞋舒适运动鞋休闲鞋女'),
        price: "149.00",
        picUrl: "https://oss.tigshop.com/img/item/demo/168076207428pJgHL7h3bVwGN5fN!!pic400x400.jpeg"
    }
];

const productList = ref<MockProduct[]>([]);

function buildProducts(rootId: string, subId: string) {
    const list: MockProduct[] = [];

    // 如果是鞋包(c4)，我们优先展示鞋子列表(完美对齐图二)
    const activePool = rootId === "c4" ? SHOES_PRODUCTS : TEXT_FILE_PRODUCTS;

    activePool.forEach((item, index) => {
        list.push({
            id: `${rootId}_${subId}_${index}`,
            title: item.productName,
            price: item.price,
            cover: item.picUrl
        });
    });

    productList.value = list;
}

function priceParts(value: string, currency: string) {
    const [integer = "0", decimal = ".00"] = String(value).split(".");
    return { currency, integer, decimal: `.${decimal.padEnd(2, "0").slice(0, 2)}` };
}

function handleRootClick(id: string) {
    activeRootId.value = id;
    activeSubId.value = buildAllSubId(id);
    buildProducts(activeRootId.value, activeSubId.value);
}

function handleSubClick(id: string) {
    activeSubId.value = id;
    buildProducts(activeRootId.value, activeSubId.value);
}

watch(
    () => [activeRootId.value, activeSubId.value],
    () => {
        const subId = activeSubId.value || activeRootId.value || "default";
        buildProducts(activeRootId.value || "default", subId);
    },
    { immediate: true }
);
</script>

<style lang="less" scoped>
.dec-cate-center-v2 {
    box-sizing: border-box;
    --rpx: 0.5px;
    --primary-color: var(--tig-primary, #e93b3d);
    --tag-text: var(--primary-color);
    --tag-bg: rgba(233, 59, 61, 0.12);

    display: flex;
    flex-direction: column;
    height: 700px;
    background-color: #fff;
    overflow: hidden;
    position: relative;

    // ========== 搜索栏 ==========
    .search-bar-box {
        display: flex;
        align-items: center;
        padding: 0 calc(24 * var(--rpx)) calc(16 * var(--rpx));
        background: #fff;
        gap: calc(16 * var(--rpx));
        flex-shrink: 0;

        .home-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: calc(64 * var(--rpx));
            height: calc(64 * var(--rpx));
            border-radius: 50%;
            border: 1px solid #eef0f5;
            background-color: #fff;
            cursor: pointer;
            box-sizing: border-box;
        }

        .search-input-wrap {
            display: flex;
            align-items: center;
            flex: 1;
            height: calc(68 * var(--rpx));
            background-color: #f5f6fa;
            border-radius: calc(34 * var(--rpx));
            box-sizing: border-box;
            padding: 0 calc(24 * var(--rpx));
            cursor: pointer;

            .icon-sousuo {
                font-size: calc(28 * var(--rpx));
                color: #888b94;
                margin-right: calc(10 * var(--rpx));
            }

            .search-placeholder {
                font-size: calc(24 * var(--rpx));
                color: #888b94;
            }
        }
    }

    // ========== 跨境版搜索栏 ==========
    .search-bar-box-overseas {
        display: flex;
        align-items: center;
        padding: 0 calc(24 * var(--rpx)) calc(16 * var(--rpx));
        background: #fff;
        flex-shrink: 0;
        gap: calc(16 * var(--rpx));

        .back-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: calc(48 * var(--rpx));
            height: calc(48 * var(--rpx));
            cursor: pointer;
        }

        .search-input-wrap {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex: 1;
            height: calc(68 * var(--rpx));
            border: 1px solid #cccccc;
            background-color: #fff;
            border-radius: calc(12 * var(--rpx));
            box-sizing: border-box;
            cursor: pointer;
            padding: 0 calc(24 * var(--rpx));

            .icon-sousuo {
                font-size: calc(28 * var(--rpx));
                color: #333333;
            }

            .search-placeholder {
                font-size: calc(24 * var(--rpx));
                color: #999999;
            }
        }
    }

    &__body {
        display: flex;
        flex: 1;
        min-height: 0;
        width: 100%;
        overflow: hidden;
        background-color: #fff;
        border-top: 1px solid #f5f6fa;
    }

    // ========== 左侧一级分类 ==========
    &__aside {
        width: calc(180 * var(--rpx));
        height: 100%;
        background-color: #f5f6fa;
        overflow-y: auto;
        overflow-x: hidden;
        flex-shrink: 0;

        &::-webkit-scrollbar {
            display: none;
        }
    }
    &__aside-item {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        min-height: calc(100 * var(--rpx));
        padding: calc(16 * var(--rpx)) calc(12 * var(--rpx));
        font-size: calc(26 * var(--rpx));
        color: #666;
        text-align: center;
        cursor: pointer;
        user-select: none;
        position: relative;
        background-color: #f5f6fa;
        box-sizing: border-box;
        word-break: break-all;
        transition: all 0.2s ease;

        &.is-active {
            background-color: #fff;
            color: var(--dec-cate-title, var(--primary-color));
            font-weight: bold;

            &::before {
                content: "";
                position: absolute;
                left: 0;
                top: 50%;
                transform: translateY(-50%);
                width: calc(6 * var(--rpx));
                height: calc(32 * var(--rpx));
                background-color: var(--primary-color);
                border-radius: 0 calc(4 * var(--rpx)) calc(4 * var(--rpx)) 0;
            }
        }
    }
    &__aside-text {
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: normal;
        line-height: 1.3;
        width: 100%;
    }

    // ========== 右侧内容 ==========
    &__main {
        flex: 1;
        height: 100%;
        min-height: 0;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        background-color: #fff;
    }
    &__subtabs-wrap {
        flex: 0 0 auto;
        display: flex;
        align-items: center;
        width: 100%;
        height: calc(88 * var(--rpx));
        background-color: #fff;
        border-bottom: 1px solid #fcfcfd;
        box-sizing: border-box;
    }
    &__subtabs {
        flex: 1;
        display: flex;
        align-items: center;
        height: 100%;
        gap: calc(14 * var(--rpx));
        padding: 0 calc(12 * var(--rpx)) 0 calc(20 * var(--rpx));
        overflow-x: auto;
        overflow-y: hidden;
        white-space: nowrap;
        box-sizing: border-box;

        &::-webkit-scrollbar {
            display: none;
        }
    }
    &__subtab {
        flex: 0 0 auto;
        padding: calc(12 * var(--rpx)) calc(24 * var(--rpx));
        font-size: calc(24 * var(--rpx));
        border-radius: 999px;
        cursor: pointer;
        user-select: none;
        white-space: nowrap;
        line-height: 1;
        background: #f5f6fa;
        color: #666;
        border: 1px solid transparent;
        transition: all 0.2s ease;
        box-sizing: border-box;
    }
    &__subtabs-arrow {
        flex: 0 0 auto;
        align-self: stretch;
        display: flex;
        align-items: center;
        justify-content: center;
        width: calc(64 * var(--rpx));
        height: 100%;
        cursor: pointer;
        border-left: 1px solid #f5f6fa;
        background-color: #fff;
        line-height: 0;
    }
    &__subtabs-arrow-icon {
        display: block;
        flex-shrink: 0;
        width: 12px;
        height: 12px;
    }

    &__content {
        flex: 1;
        height: 100%;
        min-height: 0;
        overflow-y: auto;
        padding: calc(16 * var(--rpx));
        box-sizing: border-box;
        background-color: #fff;

        &::-webkit-scrollbar {
            display: none;
        }
    }

    // ========== 样式二：双列网格 (对应图一) ==========
    &__grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: calc(20 * var(--rpx)) calc(16 * var(--rpx));
        padding-bottom: calc(40 * var(--rpx));
    }
    &__grid-item {
        background: #fff;
        overflow: hidden;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;

        .pic {
            width: 100%;
            height: calc(260 * var(--rpx));
            background: #f9f9f9;
            border-radius: 8px;
            overflow: hidden;
            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                display: block;
            }
        }
        .title {
            padding: calc(12 * var(--rpx)) 0 0;
            font-size: calc(24 * var(--rpx));
            color: var(--dec-cate-title, #1a1a1a);
            line-height: 1.35;
            overflow: hidden;
            text-overflow: ellipsis;
            word-break: break-all;
        }
        .action {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding-top: calc(12 * var(--rpx));
        }
        .price {
            font-weight: bold;
            color: var(--dec-cate-price, var(--primary-color));

            .price-currency { font-size: 11px; }
            .price-integer { font-size: 16px; }
            .price-decimal { font-size: 12.5px; }
        }
    }

    // ========== 样式三：图文列表 (对应图二) ==========
    &__list {
        display: flex;
        flex-direction: column;
        gap: calc(24 * var(--rpx));
        padding-bottom: calc(40 * var(--rpx));
    }
    &__list-item {
        display: flex;
        gap: calc(20 * var(--rpx));
        background: #fff;
        box-sizing: border-box;

        .pic {
            flex: 0 0 auto;
            width: calc(180 * var(--rpx));
            height: calc(180 * var(--rpx));
            background: #f9f9f9;
            border-radius: 8px;
            overflow: hidden;
            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                display: block;
            }
        }
        .info {
            flex: 1;
            min-width: 0;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: calc(4 * var(--rpx)) 0;
        }
        .title {
            font-size: calc(24 * var(--rpx));
            color: var(--dec-cate-title, #1a1a1a);
            line-height: 1.35;
            overflow: hidden;
            text-overflow: ellipsis;
            word-break: break-all;
        }
        .meta {
            display: flex;
            align-items: flex-end;
            justify-content: space-between;
            gap: calc(12 * var(--rpx));
        }
        .price-box {
            display: flex;
            flex-direction: column;
        }
        .price {
            font-weight: bold;
            color: var(--dec-cate-price, var(--primary-color));

            .price-currency { font-size: 11px; }
            .price-integer { font-size: 16px; }
            .price-decimal { font-size: 12.5px; }
        }
    }

    // ========== 购买按钮通用样式（参考拼团装修） ==========
    .item-buy {
        display: flex;
        align-items: center;
        flex-shrink: 0;
    }
    .buy-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
        cursor: pointer;
        line-height: 1;
    }
    .lyecs-buyBtn {
        span {
            color: var(--dec-cate-buy-btn, var(--primary-color));
            font-size: 12px;
            line-height: 1;
            white-space: nowrap;
        }
        .activeBtnStyle {
            display: inline-block;
            background: var(--primary-color);
            height: 22px;
            box-sizing: border-box;
            padding: 0 10px;
            font-size: 12px;
            color: #fff;
            border-radius: 22px;
            line-height: 22px;
            text-align: center;
            white-space: nowrap;
        }
        .outlineBtnStyle {
            display: inline-block;
            height: 22px;
            padding: 0 8px;
            font-size: 12px;
            color: var(--dec-cate-buy-btn, var(--primary-color));
            border: 1px solid var(--dec-cate-buy-btn, var(--primary-color));
            border-radius: 2px;
            line-height: 20px;
            text-align: center;
            white-space: nowrap;
            box-sizing: border-box;
        }
    }
    span.iconh5-style {
        color: var(--dec-cate-buy-btn, var(--primary-color));
        font-size: 21px !important;
        line-height: 21px !important;
    }

    // ========== 修饰类：二级分类标签风格 ==========
    // 风格 1：实心高亮 - 选中态为主题色背景 + 白字
    &--subtab-style-1 {
        .dec-cate-center-v2__subtab {
            background: #f5f6fa;
            color: #666;
            border-color: transparent;

            &.is-active {
                background-color: var(--primary-color);
                border-color: var(--primary-color);
                color: #fff;
                font-weight: 500;
            }
        }
    }
    // 风格 2：描边高亮 - 选中态为白底 + 主题色边 + 主题色字
    &--subtab-style-2 {
        .dec-cate-center-v2__subtab {
            background: #f5f6fa;
            color: #666;
            border-color: transparent;

            &.is-active {
                background-color: #fff;
                border-color: var(--primary-color);
                color: var(--primary-color);
                font-weight: 500;
            }
        }
    }

    // ========== 修饰类：文本对齐 ==========
    &--text-align-1 {
        .dec-cate-center-v2__grid-item {
            text-align: left;
            .action {
                justify-content: space-between;
            }
        }
        .dec-cate-center-v2__list-item .info {
            text-align: left;
        }
        .dec-cate-center-v2__list-item .meta {
            justify-content: space-between;
        }
    }
    &--text-align-2 {
        .dec-cate-center-v2__grid-item {
            text-align: center;
            .action {
                justify-content: center;
                gap: calc(16 * var(--rpx));
            }
        }
        .dec-cate-center-v2__list-item .info {
            text-align: center;
        }
        .dec-cate-center-v2__list-item .meta {
            justify-content: center;
            gap: calc(16 * var(--rpx));
        }
    }

    // ========== 修饰类：文字粗细 ==========
    &--text-weight-1 {
        .dec-cate-center-v2__grid-item .title,
        .dec-cate-center-v2__list-item .title {
            font-weight: 400;
        }
    }
    &--text-weight-2 {
        .dec-cate-center-v2__grid-item .title,
        .dec-cate-center-v2__list-item .title {
            font-weight: 600;
        }
    }

    // ========== 修饰类：商品名称行数 ==========
    &--name-row-1 {
        .dec-cate-center-v2__grid-item .title,
        .dec-cate-center-v2__list-item .title {
            display: block;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            -webkit-line-clamp: 1;
            line-clamp: 1;
        }
    }
    &--name-row-2 {
        .dec-cate-center-v2__grid-item .title,
        .dec-cate-center-v2__list-item .title {
            display: -webkit-box;
            -webkit-box-orient: vertical;
            white-space: normal;
            overflow: hidden;
            text-overflow: ellipsis;
            word-break: break-all;
            -webkit-line-clamp: 2;
            line-clamp: 2;
        }
    }

    // ========== 修饰类：购买按钮 0=不显示 ==========
    &--buy-btn-0 {
        .item-buy {
            display: none;
        }
    }
}

.modules-item.module-item-active .module-ad-con:before {
    left: 0px;
    right: 0px;
}
</style>
