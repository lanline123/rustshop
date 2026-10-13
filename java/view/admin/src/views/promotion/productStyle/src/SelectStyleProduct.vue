<template>
    <div class="select-style-product">
        <!-- 左栏：垂直 Tab -->
        <div class="tab-sidebar">
            <div :class="['tab-item', { active: activeTab === 'product' }]" @click="switchTab('product')">{{ $t('商品') }}</div>
            <div :class="['tab-item', { active: activeTab === 'group' }]" @click="switchTab('group')">{{ $t('商品分组') }}</div>
            <div :class="['tab-item', { active: activeTab === 'brand' }]" @click="switchTab('brand')">{{ $t('商品品牌') }}</div>
        </div>

        <!-- 中栏：搜索 + 列表 + 分页 -->
        <div class="main-panel">
            <!-- 商品 Tab -->
            <template v-if="activeTab === 'product'">
                <div class="search-bar">
                    <SelectCategory v-model:categoryId="productFilter.categoryId" class="search-select" @update:categoryId="onProductSearch" />
                    <TigInput v-model="productFilter.keyword" :placeholder="$t('商品名称/商品货号')" class="search-input" @keyup.enter="onProductSearch" />
                    <el-button @click="onProductSearch">{{ $t('搜索') }}</el-button>
                </div>
                <div class="list-area">
                    <a-spin :spinning="productLoading">
                        <el-table
                            ref="productTableRef"
                            :data="productList"
                            :row-key="(row: any) => row.productId"
                            @selection-change="onProductSelectionChange"
                            class="style-table"
                        >
                            <el-table-column type="selection" width="40" :reserve-selection="false" />
                            <el-table-column :label="$t('商品信息')" min-width="260">
                                <template #default="{ row }">
                                    <div class="cell-item">
                                        <img :src="imageFormat(row.picThumb)" class="cell-thumb" />
                                        <div class="cell-info">
                                            <div class="cell-name">{{ row.productName }}</div>
                                            <div class="cell-sub">{{ row.productSn }}</div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <template #empty><div v-if="!productLoading" class="empty-tip">{{ $t('暂无数据') }}</div></template>
                        </el-table>
                    </a-spin>
                </div>
                <div v-if="productTotal > 0" class="list-pagination">
                    <Pagination
                        v-model:page="productFilter.page"
                        v-model:size="productFilter.size"
                        :total="productTotal"
                        @callback="loadProducts"
                        layout="slot, prev, pager, next"
                        :background="false"
                    />
                </div>
            </template>

            <!-- 商品分组 Tab -->
            <template v-if="activeTab === 'group'">
                <div class="search-bar">
                    <TigInput v-model="groupFilter.keyword" :placeholder="$t('分组名称')" class="search-input" @keyup.enter="onGroupSearch" />
                    <el-button type="primary" @click="onGroupSearch">{{ $t('搜索') }}</el-button>
                </div>
                <div class="list-area">
                    <a-spin :spinning="groupLoading">
                        <el-table
                            ref="groupTableRef"
                            :data="groupList"
                            :row-key="(row: any) => row.productGroupId"
                            @selection-change="onGroupSelectionChange"
                            class="style-table"
                        >
                            <el-table-column type="selection" width="40" :reserve-selection="false" />
                            <el-table-column :label="$t('分组名称')" prop="productGroupName" min-width="200" />
                            <template #empty><div v-if="!groupLoading" class="empty-tip">{{ $t('暂无数据') }}</div></template>
                        </el-table>
                    </a-spin>
                </div>
                <div v-if="groupTotal > 0" class="list-pagination">
                    <Pagination
                        v-model:page="groupFilter.page"
                        v-model:size="groupFilter.size"
                        :total="groupTotal"
                        @callback="loadGroups"
                        layout="slot, prev, pager, next"
                        :background="false"
                    />
                </div>
            </template>

            <!-- 商品品牌 Tab -->
            <template v-if="activeTab === 'brand'">
                <div class="search-bar">
                    <TigInput v-model="brandFilter.keyword" :placeholder="$t('品牌名称')" class="search-input" @keyup.enter="onBrandSearch" />
                    <el-button @click="onBrandSearch">{{ $t('搜索') }}</el-button>
                </div>
                <div class="list-area">
                    <a-spin :spinning="brandLoading">
                        <el-table
                            ref="brandTableRef"
                            :data="brandList"
                            :row-key="(row: any) => row.brandId"
                            @selection-change="onBrandSelectionChange"
                            class="style-table"
                        >
                            <el-table-column type="selection" width="40" :reserve-selection="false" />
                            <el-table-column :label="$t('品牌信息')" min-width="260">
                                <template #default="{ row }">
                                    <div class="cell-item">
                                        <img v-if="row.brandLogo" :src="imageFormat(row.brandLogo)" class="cell-brand-logo" />
                                        <div class="cell-info">
                                            <div class="cell-name">{{ row.brandName }}</div>
                                            <!-- <div v-if="row.firstWord" class="cell-sub">{{ row.firstWord }}</div> -->
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <template #empty><div v-if="!brandLoading" class="empty-tip">{{ $t('暂无数据') }}</div></template>
                        </el-table>
                    </a-spin>
                </div>
                <div v-if="brandTotal > 0" class="list-pagination">
                    <Pagination
                        v-model:page="brandFilter.page"
                        v-model:size="brandFilter.size"
                        :total="brandTotal"
                        @callback="loadBrands"
                        layout="slot, prev, pager, next"
                        :background="false"
                    />
                </div>
            </template>
        </div>

        <!-- 右栏：已选面板 -->
        <div class="selected-panel">
            <!-- 商品已选 -->
            <template v-if="activeTab === 'product'">
                <div class="selected-header">
                    <span>{{ $t('已选择商品({length})', { length: selectedProducts.length }) }}</span>
                </div>
                <div class="selected-list">
                    <div v-for="item in selectedProducts" :key="item.productId" class="selected-card">
                        <img :src="imageFormat(item.picThumb)" class="selected-thumb" />
                        <div class="selected-info">
                            <div class="selected-name">{{ item.productName }}</div>
                        </div>
                        <i class="iconfont-pc icon-shanchu remove-btn" @click="removeProduct(item.productId)"></i>
                    </div>
                    <div v-if="selectedProducts.length === 0" class="empty-tip">{{ $t('暂未选择商品') }}</div>
                </div>
            </template>

            <!-- 分组已选 -->
            <template v-if="activeTab === 'group'">
                <div class="selected-header">
                    <span>{{ $t('已选择分组({length})', { length: selectedGroups.length }) }}</span>
                </div>
                <div class="selected-list">
                    <div v-for="item in selectedGroups" :key="item.productGroupId" class="selected-card">
                        <div class="selected-info" style="flex: 1">
                            <div class="selected-name">{{ item.productGroupName }}</div>
                        </div>
                        <i class="iconfont-pc icon-shanchu remove-btn" @click="removeGroup(item.productGroupId!)"></i>
                    </div>
                    <div v-if="selectedGroups.length === 0" class="empty-tip">{{ $t('暂未选择分组') }}</div>
                </div>
            </template>

            <!-- 品牌已选 -->
            <template v-if="activeTab === 'brand'">
                <div class="selected-header">
                    <span>{{ $t('已选择品牌({length})', { length: selectedBrands.length }) }}</span>
                </div>
                <div class="selected-list">
                    <div v-for="item in selectedBrands" :key="item.brandId" class="selected-card">
                        <img v-if="item.brandLogo" :src="imageFormat(item.brandLogo)" class="selected-brand-logo" />
                        <div class="selected-info" style="flex: 1">
                            <div class="selected-name">{{ item.brandName }}</div>
                        </div>
                        <i class="iconfont-pc icon-shanchu remove-btn" @click="removeBrand(item.brandId)"></i>
                    </div>
                    <div v-if="selectedBrands.length === 0" class="empty-tip">{{ $t('暂未选择品牌') }}</div>
                </div>
            </template>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick, watch, type PropType } from "vue";
import { Close } from "@element-plus/icons-vue";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { getProductGroupList } from "@/api/product/productGroup";
import { getBrandList } from "@/api/product/brand";
import { SelectCategory } from "@/components/select";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { ProductFilterState } from "@/types/product/product";
import type { ProductGroupFilterState } from "@/types/product/productGroup";
import type { GroupItemVO, BrandItemVO, ProductStyleItemVO } from "@/types/promotion/productStyle";
import type { BrandFilterState } from "@/types/product/brand";

const productIds = defineModel<number[]>("productIds", { type: Array, default: () => [] });
const groupIds = defineModel<number[]>("groupIds", { type: Array, default: () => [] });
const brandIds = defineModel<number[]>("brandIds", { type: Array, default: () => [] });

const props = defineProps({
    initProductList: { type: Array as () => ProductStyleItemVO[], default: () => [] },
    initGroupList: { type: Array as () => GroupItemVO[], default: () => [] },
    initBrandList: { type: Array as () => BrandItemVO[], default: () => [] },
    initialTab: { type: String as PropType<"product" | "group" | "brand">, default: "product" }
});

const emit = defineEmits(["update:productIds", "update:groupIds", "update:brandIds"]);

const config = useConfigStore();
type SelectTab = "product" | "group" | "brand";
const activeTab = ref<SelectTab>(props.initialTab);

// ===== 商品 =====
const productLoading = ref(false);
const productTotal = ref(0);
const productTableRef = ref();
const productList = ref<ProductFilterState[]>([]);
const selectedProducts = ref<ProductStyleItemVO[]>([]);
const isSyncingProductSelection = ref(false);

const productFilter = reactive({
    page: 1,
    size: config.get("pageSize") as number,
    keyword: "",
    categoryId: 0 as number | string,
    productStatus: 1,
    isDelete: 0,
    checkStatus: 1
});

const emitProductIdsChange = () => {
    const ids = selectedProducts.value.map((item) => item.productId);
    productIds.value = ids;
    emit("update:productIds", ids);
};

const buildSelectedProduct = (row: ProductFilterState): ProductStyleItemVO => ({
    productId: row.productId,
    productName: row.productName,
    picThumb: row.picThumb,
    productPrice: row.productPrice || 0
});

const onProductSearch = () => {
    productFilter.page = 1;
    loadProducts();
};

const loadProducts = async () => {
    productLoading.value = true;
    try {
        const params: any = { ...productFilter };
        if (!params.categoryId) delete params.categoryId;
        const result = await getProductList(params);
        productList.value = result.records;
        productTotal.value = result.total;
        await nextTick();
        syncProductTableSelection();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        productLoading.value = false;
    }
};

const syncProductTableSelection = () => {
    if (!productTableRef.value) return;
    const selectedProductIds = new Set(selectedProducts.value.map((item) => item.productId));
    isSyncingProductSelection.value = true;
    productTableRef.value.clearSelection();
    productList.value.forEach((row) => {
        if (selectedProductIds.has(row.productId)) {
            productTableRef.value.toggleRowSelection(row, true);
        }
    });
    nextTick(() => {
        isSyncingProductSelection.value = false;
    });
};

const onProductSelectionChange = (selection: ProductFilterState[]) => {
    if (isSyncingProductSelection.value) return;
    if (selection.length > 0) {
        clearSelectionsExcept("product");
    }
    const currentPageIds = new Set(productList.value.map((row) => row.productId));
    const preservedProducts = selectedProducts.value.filter((item) => !currentPageIds.has(item.productId));
    selectedProducts.value = [...preservedProducts, ...selection.map((row) => buildSelectedProduct(row))];
    emitProductIdsChange();
};

const removeProduct = (productId: number) => {
    selectedProducts.value = selectedProducts.value.filter((p) => p.productId !== productId);
    emitProductIdsChange();
    const targetRow = productList.value.find((row) => row.productId === productId);
    if (targetRow && productTableRef.value) {
        isSyncingProductSelection.value = true;
        productTableRef.value.toggleRowSelection(targetRow, false);
        nextTick(() => {
            isSyncingProductSelection.value = false;
        });
        return;
    }
    nextTick(() => syncProductTableSelection());
};

const clearProductSelection = () => {
    selectedProducts.value = [];
    emitProductIdsChange();
    if (!productTableRef.value) return;
    isSyncingProductSelection.value = true;
    productTableRef.value.clearSelection();
    nextTick(() => {
        isSyncingProductSelection.value = false;
    });
};

// ===== 商品分组 =====
const groupLoading = ref(false);
const groupTotal = ref(0);
const groupTableRef = ref();
const groupList = ref<ProductGroupFilterState[]>([]);
const selectedGroups = ref<GroupItemVO[]>([]);
const isSyncingGroupSelection = ref(false);

const groupFilter = reactive({ page: 1, size: config.get("pageSize") as number, keyword: "" });

const emitGroupIdsChange = () => {
    const ids = selectedGroups.value.map((item) => item.productGroupId);
    groupIds.value = ids;
    emit("update:groupIds", ids);
};

const buildSelectedGroup = (row: ProductGroupFilterState): GroupItemVO => ({
    productGroupId: row.productGroupId!,
    productGroupName: row.productGroupName || ""
});

const onGroupSearch = () => {
    groupFilter.page = 1;
    loadGroups();
};

const loadGroups = async () => {
    groupLoading.value = true;
    try {
        const result = await getProductGroupList({ ...groupFilter } as any);
        groupList.value = result.records;
        groupTotal.value = result.total;
        await nextTick();
        syncGroupTableSelection();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        groupLoading.value = false;
    }
};

const syncGroupTableSelection = () => {
    if (!groupTableRef.value) return;
    const selectedGroupIds = new Set(selectedGroups.value.map((item) => item.productGroupId));
    isSyncingGroupSelection.value = true;
    groupTableRef.value.clearSelection();
    groupList.value.forEach((row) => {
        if (selectedGroupIds.has(row.productGroupId!)) {
            groupTableRef.value.toggleRowSelection(row, true);
        }
    });
    nextTick(() => {
        isSyncingGroupSelection.value = false;
    });
};

const onGroupSelectionChange = (selection: ProductGroupFilterState[]) => {
    if (isSyncingGroupSelection.value) return;
    if (selection.length > 0) {
        clearSelectionsExcept("group");
    }
    const currentPageIds = new Set(groupList.value.map((row) => row.productGroupId!));
    const preservedGroups = selectedGroups.value.filter((item) => !currentPageIds.has(item.productGroupId));
    selectedGroups.value = [...preservedGroups, ...selection.map((row) => buildSelectedGroup(row))];
    emitGroupIdsChange();
};

const removeGroup = (groupId: number) => {
    selectedGroups.value = selectedGroups.value.filter((g) => g.productGroupId !== groupId);
    emitGroupIdsChange();
    const targetRow = groupList.value.find((row) => row.productGroupId === groupId);
    if (targetRow && groupTableRef.value) {
        isSyncingGroupSelection.value = true;
        groupTableRef.value.toggleRowSelection(targetRow, false);
        nextTick(() => {
            isSyncingGroupSelection.value = false;
        });
        return;
    }
    nextTick(() => syncGroupTableSelection());
};

const clearGroupSelection = () => {
    selectedGroups.value = [];
    emitGroupIdsChange();
    if (!groupTableRef.value) return;
    isSyncingGroupSelection.value = true;
    groupTableRef.value.clearSelection();
    nextTick(() => {
        isSyncingGroupSelection.value = false;
    });
};

// ===== 商品品牌 =====
const brandLoading = ref(false);
const brandTotal = ref(0);
const brandTableRef = ref();
const brandList = ref<BrandFilterState[]>([]);
const selectedBrands = ref<BrandItemVO[]>([]);
const isSyncingBrandSelection = ref(false);

const brandFilter = reactive({ page: 1, size: config.get("pageSize") as number, keyword: "" });

const emitBrandIdsChange = () => {
    const ids = selectedBrands.value.map((item) => item.brandId);
    brandIds.value = ids;
    emit("update:brandIds", ids);
};

const buildSelectedBrand = (row: BrandFilterState): BrandItemVO => ({
    brandId: row.brandId,
    brandName: row.brandName,
    brandLogo: row.brandLogo
});

const onBrandSearch = () => {
    brandFilter.page = 1;
    loadBrands();
};

const loadBrands = async () => {
    brandLoading.value = true;
    try {
        const result = await getBrandList({ ...brandFilter, isShow: 1 });
        brandList.value = result.records;
        brandTotal.value = result.total;
        await nextTick();
        syncBrandTableSelection();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        brandLoading.value = false;
    }
};

const syncBrandTableSelection = () => {
    if (!brandTableRef.value) return;
    const selectedBrandIds = new Set(selectedBrands.value.map((item) => item.brandId));
    isSyncingBrandSelection.value = true;
    brandTableRef.value.clearSelection();
    brandList.value.forEach((row) => {
        if (selectedBrandIds.has(row.brandId)) {
            brandTableRef.value.toggleRowSelection(row, true);
        }
    });
    nextTick(() => {
        isSyncingBrandSelection.value = false;
    });
};

const onBrandSelectionChange = (selection: BrandFilterState[]) => {
    if (isSyncingBrandSelection.value) return;
    if (selection.length > 0) {
        clearSelectionsExcept("brand");
    }
    const currentPageIds = new Set(brandList.value.map((row) => row.brandId));
    const preservedBrands = selectedBrands.value.filter((item) => !currentPageIds.has(item.brandId));
    selectedBrands.value = [...preservedBrands, ...selection.map((row) => buildSelectedBrand(row))];
    emitBrandIdsChange();
};

const removeBrand = (brandId: number) => {
    selectedBrands.value = selectedBrands.value.filter((b) => b.brandId !== brandId);
    emitBrandIdsChange();
    const targetRow = brandList.value.find((row) => row.brandId === brandId);
    if (targetRow && brandTableRef.value) {
        isSyncingBrandSelection.value = true;
        brandTableRef.value.toggleRowSelection(targetRow, false);
        nextTick(() => {
            isSyncingBrandSelection.value = false;
        });
        return;
    }
    nextTick(() => syncBrandTableSelection());
};

const clearBrandSelection = () => {
    selectedBrands.value = [];
    emitBrandIdsChange();
    if (!brandTableRef.value) return;
    isSyncingBrandSelection.value = true;
    brandTableRef.value.clearSelection();
    nextTick(() => {
        isSyncingBrandSelection.value = false;
    });
};

const loadActiveTabData = (tab: SelectTab) => {
    if (tab === "product" && productList.value.length === 0) loadProducts();
    if (tab === "group" && groupList.value.length === 0) loadGroups();
    if (tab === "brand" && brandList.value.length === 0) loadBrands();
};

const clearSelectionsExcept = (tab: SelectTab) => {
    if (tab !== "product") clearProductSelection();
    if (tab !== "group") clearGroupSelection();
    if (tab !== "brand") clearBrandSelection();
};

// ===== 初始化 =====
const switchTab = (tab: SelectTab) => {
    if (activeTab.value === tab) return;
    activeTab.value = tab;
    loadActiveTabData(tab);
};

watch(activeTab, (tab) => {
    nextTick(() => {
        if (tab === "product") syncProductTableSelection();
        if (tab === "group") syncGroupTableSelection();
        if (tab === "brand") syncBrandTableSelection();
    });
});

onMounted(() => {
    if (props.initProductList.length > 0) selectedProducts.value = [...props.initProductList];
    if (props.initGroupList.length > 0) selectedGroups.value = [...props.initGroupList];
    if (props.initBrandList.length > 0) selectedBrands.value = [...props.initBrandList];
    activeTab.value = props.initialTab;
    loadActiveTabData(activeTab.value);
});
</script>

<style lang="less" scoped>
.select-style-product {
    display: flex;
    height: 100%;
    //border: 1px solid #e8e8e8;
    border-radius: 4px;
    overflow: hidden;
    background: #fff;
}

.tab-sidebar {
    width: 130px;
    flex-shrink: 0;
    border: 1px solid #e8e8e8;
    padding: 10px 10px 0 0;
    height: calc(100% - 55px);
    // background: #fafafa;

    .tab-item {
        padding: 10px 12px 10px 10px;
        text-align: left;
        font-size: 14px;
        color: #333;
        cursor: pointer;
        // border-bottom: 1px solid #e8e8e8;
        transition: all 0.2s;
        border-radius: 0 2px 2px 0;

        &.active {
            color: var(--tig-primary);
            background: #e8f0fe;
        }
    }
}

.main-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
    // border-right: 1px solid #e8e8e8;
    padding: 0 20px 0 10px;
}

.search-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-bottom: 1px solid #e8e8e8;
    flex-shrink: 0;
    background: #f5f7fa;
    margin-bottom: 16px;

    .search-select {
        width: 160px;
        flex-shrink: 0;
    }
    .search-input {
        flex: 1;
        min-width: 0;
    }
}

.list-area {
    flex: 1;
    overflow-y: auto;

    .cell-item {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .cell-thumb {
        width: 44px;
        height: 44px;
        border-radius: 4px;
        object-fit: cover;
        flex-shrink: 0;
    }
    .cell-info {
        flex: 1;
        min-width: 0;
    }
    .cell-name {
        font-size: 14px;
        color: #333;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        line-clamp: 2;
        -webkit-line-clamp: 2;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .cell-sub {
        font-size: 12px;
        color: #999;
        margin-top: 2px;
    }
    .cell-brand-logo {
        width: 60px;
        height: 25px;
        object-fit: contain;
        flex-shrink: 0;
    }
    .gray {
        color: #ccc;
    }
}

.list-pagination {
    flex-shrink: 0;
    padding: 6px 12px;
    border-top: 1px solid #e8e8e8;
    background: #fff;
}

.selected-panel {
    width: 320px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    background: #fafafa;
}

.selected-header {
    padding: 0 14px;
    font-size: 14px;
    font-weight: 500;
    color: var(--tig-primary);
    // background: #e8f0fe;
    flex-shrink: 0;
    min-height: 50px;
    line-height: 50px;
    box-sizing: border-box;
    display: flex;
    position: relative;
    span {
        &::after {
            content: "";
            display: block;
            height: 2px;
            background: var(--tig-primary);
            transform: translateY(-5px);
        }
    }
}

.selected-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    .selected-card {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px;
        padding-right: 0;
        background: #fff;
        border-radius: 4px;
        margin-bottom: 6px;
        border: 1px solid #f0f0f0;
        box-sizing: border-box;
        min-height: 45px;

        .selected-thumb {
            width: 40px;
            height: 40px;
            border-radius: 4px;
            object-fit: cover;
            flex-shrink: 0;
        }
        .selected-brand-logo {
            width: 50px;
            height: 22px;
            object-fit: contain;
            flex-shrink: 0;
        }

        .selected-info {
            flex: 1;
            min-width: 0;
            .selected-name {
                font-size: 14px;
                color: #333;
                line-height: 1.4;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                line-clamp: 2;
                -webkit-line-clamp: 2;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .selected-sub {
                font-size: 11px;
                color: #999;
                margin-top: 2px;
            }
        }

        .remove-btn {
            flex-shrink: 0;
            cursor: pointer;
            color: #999;
            font-size: 16px;
            transition: color 0.2s;
            padding: 4px 8px;
            &:hover {
                color: #f56c6c;
            }
        }
    }
}

.empty-tip {
    text-align: center;
    color: #999;
    padding: 40px 0;
    font-size: 13px;
}
</style>
