<template>
    <div class="group-list-edit">
        <draggable
            item-key="id"
            :list="localGroups"
            ghost-class="ghost"
            chosen-class="chosen-class"
            :animation="300"
            handle=".drag-handle"
            :move="onDragMove"
            @end="handleChange"
        >
            <template #item="{ element, index }">
                <div class="collapse-item" :class="{ 'collapse-item--fixed': index === 0 }">
                    <div class="title-box" :class="{ 'drag-handle': index > 0 }">第【{{ index + 1 }}】组</div>

                    <!-- 标题 -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">标题</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group">
                                <el-input v-model="element.title" placeholder="请输入标题内容" @change="handleChange" />
                            </div>
                        </div>
                    </div>

                    <!-- 标题图片 -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">标题图片</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-pic-group dec-picSingle-group">
                                <PicSelect
                                    :photo="element.titlePic || { picUrl: '', picThumb: '' }"
                                    @update:photo="(v: PicItem) => handleTitlePicChange(index, v)"
                                />
                            </div>
                        </div>
                    </div>

                    <!-- 商品来源（放在分组内部控制；默认"选择商品"） -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">商品来源</div>
                        </div>
                        <div class="dec-edit-group-con" @mousedown.stop @click.stop>
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="element.sourceType || 'products'"
                            >
                                <el-radio-button value="products" @click.stop="handleSourceTypeChange(index, 'products')">
                                    选择商品
                                </el-radio-button>
                                <el-radio-button value="productGroup" @click.stop="handleSourceTypeChange(index, 'productGroup')">
                                    商品分组
                                </el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <!-- 选择商品（手动） -->
                    <template v-if="(element.sourceType || 'products') === 'products'">
                        <div class="dec-edit-group dec-edit-group-block">
                            <div class="dec-edit-group-title">
                                <div class="label">选择商品</div>
                            </div>
                            <div class="dec-edit-group-con">
                                <div class="dec-goods-group">
                                    <draggable
                                        class="dec-goods-group-list"
                                        item-key="productId"
                                        :list="element.productList"
                                        ghost-class="ghost"
                                        chosen-class="chosen-class"
                                        :animation="300"
                                        @end="handleChange"
                                    >
                                        <template #item="{ element: productItem, index: productIndex }">
                                            <div class="dec-goods-group-item dec-tip dec-goods-group-item-goods">
                                                <div class="dec-goods-group-item-con">
                                                    <div class="item-goods-con">
                                                        <a href="javascript:;" :title="productItem.productName">
                                                            <img class="item-goods-img" :src="productImage(productItem)" />
                                                        </a>
                                                    </div>
                                                    <div class="del-item" @click="handleProductRemove(index, productIndex)">
                                                        <i class="ico-decorate icon-dec-cha"></i>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                        <template #footer>
                                            <DialogForm
                                                isDrawer
                                                title="选择商品"
                                                width="600px"
                                                path="product/product/src/SelectProduct"
                                                :params="{ selectedIds: getProductIds(element), isMultiple: true }"
                                                @okCallback="(ids: number[]) => handleProductAdd(index, ids)"
                                            >
                                                <div class="dec-goods-group-item dec-goods-group-add dec-tip" data-title="添加商品">
                                                    <div class="dec-goods-group-item-con">
                                                        <div class="item-goods-con">
                                                            <a href="javascript:;"><i class="ico-decorate icon-dec-jia"></i></a>
                                                        </div>
                                                    </div>
                                                </div>
                                            </DialogForm>
                                        </template>
                                    </draggable>
                                </div>
                            </div>
                            <div class="dec-edit-group-desc">
                                <div>提示：您可以通过拖拽进行商品排序</div>
                            </div>
                        </div>
                    </template>

                    <!-- 商品分组：只能选择/更换分组 -->
                    <template v-else>
                        <div class="dec-edit-group dec-edit-group-block">
                            <div class="dec-edit-group-title">
                                <div class="label">商品分组</div>
                            </div>
                            <div class="dec-edit-group-con">
                                <div class="dec-input-group">
                                    <DialogForm
                                        isDrawer
                                        title="选择商品分组"
                                        width="600px"
                                        path="product/productGroup/src/SelectProductGroup"
                                        :params="{ selectedIds: getExcludeIds(element.productGroupId), isMultiple: false }"
                                        @okCallback="(result: { ids: number[] }) => onSelectGroup(index, result)"
                                    >
                                        <button type="button" class="dec-pic-add-btn">
                                            <i class="ico-decorate icon-dec-jia"></i>
                                            {{ element.productGroupName ? "更换商品分组" : "添加商品分组" }}
                                        </button>
                                    </DialogForm>
                                </div>
                            </div>
                        </div>

                        <!-- 已选分组名称 -->
                        <div v-if="element.productGroupName" class="dec-edit-group dec-edit-group-block">
                            <div class="dec-edit-group-title">
                                <div class="label">已选分组</div>
                            </div>
                            <div class="dec-edit-group-con">
                                <div class="selected-group-name">{{ element.productGroupName }}</div>
                            </div>
                            <div class="dec-edit-group-desc">
                                <div>提示：商品来源于商品分组，仅支持切换商品分组，不能单独增删商品</div>
                            </div>
                        </div>
                    </template>

                    <!-- 删除按钮（第一组不可删除） -->
                    <div v-if="isMultiple && showClose && index > 0" class="del-item" @click="removeItem(index)">
                        <i class="ico-decorate icon-dec-cha"></i>
                    </div>
                </div>
            </template>
        </draggable>

        <!-- 添加按钮 -->
        <div v-if="isMultiple && showAdd" class="cobot-btn">
            <a class="dec-pic-add-btn" @click="onAdd"> <i class="ico-decorate icon-dec-jia"></i>{{ title }} </a>
        </div>
    </div>
</template>

<script lang="ts" setup>
/**
 * 商品分组列表编辑组件（移植自 Vort GroupListEdit，控件 Element 化）
 * - 选商品：复用 Tigshop 现成弹窗 product/product/src/SelectProduct（okCallback 返回 id 数组），
 *   再经 getProductList({ ids }) 取回商品对象写入 productList
 * - 选分组：复用 Tigshop 现成弹窗 product/productGroup/src/SelectProductGroup，
 *   经 getProductGroupList({ productGroupIds }) 取回分组名称与商品数
 */
import { ref, watch } from "vue";
import draggable from "vuedraggable";
import { message } from "ant-design-vue";
import type { ProductGroupItem, ProductItem, ProductSourceType } from "../types";
import type { PicItem } from "../../_shared/types";
import { PicSelect } from "@/components/decorate";
import { DialogForm } from "@/components/dialog";
import { productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { getProductGroupList } from "@/api/product/productGroup";

defineOptions({ name: "GroupListEdit" });

const props = withDefaults(
    defineProps<{
        productGroups?: ProductGroupItem[];
        title?: string;
        isMultiple?: boolean;
        showClose?: boolean;
        showAdd?: boolean;
    }>(),
    {
        productGroups: () => [],
        title: "",
        isMultiple: false,
        showClose: true,
        showAdd: true
    }
);

const emit = defineEmits<{
    (e: "update:productGroups", value: ProductGroupItem[]): void;
}>();

// 本地列表：vuedraggable 会原地改 :list，不能与 props 共用引用
const localGroups = ref<ProductGroupItem[]>([]);

let _idSeq = 0;
function genId(): string {
    return `g_${Date.now().toString(36)}_${(_idSeq++).toString(36)}_${Math.random().toString(36).slice(2, 7)}`;
}

/** 从 props 同步本地列表；若项引用未变则跳过，避免切换来源时 spread 重建对象导致 PicSelect 重载 */
function syncLocalGroupsFromProps(val: ProductGroupItem[] | undefined) {
    const list = val || [];
    if (!list.length) {
        localGroups.value = [];
        return;
    }
    if (list.length === localGroups.value.length && list.every((g, i) => g === localGroups.value[i])) {
        return;
    }
    localGroups.value = list.map((g) => ({
        ...g,
        id: g.id || genId(),
        sourceType: (g.sourceType || "products") as ProductSourceType,
        productList: [...(g.productList || [])]
    }));
}

watch(() => props.productGroups, syncLocalGroupsFromProps, { immediate: true });

/** 通知父组件：基于当前 localGroups 浅拷贝出去，元素引用保持不变，方便 syncLocalGroupsFromProps early-return */
function emitFromLocal() {
    emit("update:productGroups", [...localGroups.value]);
}

/**
 * 添加新分组
 */
function onAdd() {
    const newGroup: ProductGroupItem = {
        id: genId(),
        sourceType: "products",
        title: "",
        titlePic: { picUrl: "", picThumb: "" },
        productList: [],
        bannerPic: { picUrl: "", picThumb: "" },
        bannerLink: { link: "", name: "" },
        titleLink: { link: "", name: "" }
    };
    localGroups.value.push(newGroup);
    emitFromLocal();
}

/**
 * 拖动限制：第一组不可被拖动，也不允许其他项拖到第一组位置
 */
function onDragMove(evt: { draggedContext: { index: number }; relatedContext: { index: number } }) {
    if (evt.draggedContext.index === 0 || evt.relatedContext.index === 0) return false;
    return true;
}

/**
 * 删除分组（第一组不可删除）
 */
function removeItem(index: number) {
    if (index === 0) return;
    localGroups.value.splice(index, 1);
    emitFromLocal();
}

/**
 * 触发更新（draggable 直接原地排序 / 输入框 @change 等）
 */
function handleChange() {
    emitFromLocal();
}

/**
 * 更新标题图片
 */
function handleTitlePicChange(index: number, pic: PicItem) {
    const current = localGroups.value[index];
    if (!current) return;
    current.titlePic = pic;
    emitFromLocal();
}

/**
 * 更新商品列表
 */
function handleProductListChange(index: number, productList: ProductItem[]) {
    const current = localGroups.value[index];
    if (!current) return;
    current.productList = productList;
    emitFromLocal();
}

/** 当前分组已选商品 id（弹窗回显排重用） */
function getProductIds(element: ProductGroupItem): number[] {
    return (element.productList || []).map((p) => p.productId).filter((id): id is number => !!id && id > 0);
}

/**
 * 选商品弹窗确认：按 id 拉取商品对象追加到分组商品列表
 */
async function handleProductAdd(index: number, ids: number[]) {
    if (!ids || !ids.length) return;
    const current = localGroups.value[index];
    if (!current) return;
    try {
        const result = await getProductList({ ids: ids.join(","), page: 1, size: 100 });
        const records = (result.records || []) as ProductItem[];
        if (!records.length) return;
        handleProductListChange(index, [...(current.productList || []), ...records]);
    } catch (error: unknown) {
        message.error(error instanceof Error ? error.message : "获取商品失败");
    }
}

/**
 * 删除分组内单个商品
 */
function handleProductRemove(index: number, productIndex: number) {
    const current = localGroups.value[index];
    if (!current) return;
    const newList = [...(current.productList || [])];
    newList.splice(productIndex, 1);
    handleProductListChange(index, newList);
}

function handleSourceTypeChange(index: number, type: ProductSourceType) {
    const current = localGroups.value[index];
    if (!current) return;

    const prev = (current.sourceType || "products") as ProductSourceType;
    if (prev === type) return;

    if (!current.id) current.id = genId();
    current.sourceType = type;
    if (type === "products") {
        current.productGroupId = undefined;
        current.productGroupName = undefined;
        current.productNumType = undefined;
        current.productNum = undefined;
    } else {
        current.productList = [];
        if (!current.productNumType) current.productNumType = "number";
        if (!current.productNum) current.productNum = 6;
    }

    emitFromLocal();
}

function getExcludeIds(currentId?: string | number): (string | number)[] {
    const ids = (localGroups.value || []).map((g) => g.productGroupId).filter((id): id is string | number => id !== undefined && id !== null && id !== "");
    return ids.filter((id) => String(id) !== String(currentId));
}

/**
 * 选分组弹窗确认：取回分组名称/商品数写入分组项
 */
async function onSelectGroup(index: number, result: { ids: number[] }) {
    const groupId = result?.ids?.[0];
    if (!groupId) return;
    const current = localGroups.value[index];
    if (!current) return;
    try {
        const res = await getProductGroupList({ productGroupIds: String(groupId), page: 1, size: 1 });
        const item = res.records?.[0];
        if (!item || !item.productGroupId) return;

        if (!current.id) current.id = genId();
        current.productGroupId = item.productGroupId;
        current.productGroupName = item.productGroupName;
        if (!current.productNum) current.productNum = item.productIds?.length ? item.productIds.length : 6;
        if (!current.title) current.title = item.productGroupName || "";

        emitFromLocal();
    } catch (error: unknown) {
        message.error(error instanceof Error ? error.message : "获取商品分组失败");
    }
}
</script>

<style scoped lang="less">
.group-list-edit {
    padding: 0 10px 15px 10px;

    .collapse-item {
        width: 100%;
        position: relative;
        margin-top: 12px;
        border-radius: 2px;
        background-color: #fff;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);

        .title-box {
            padding: 15px;
            font-size: 14px;
            border-bottom: 1px solid #f0f0f0;
            cursor: default;

            &.drag-handle {
                cursor: move;
            }
        }

        > .del-item {
            position: absolute;
            cursor: pointer;
            right: -10px;
            top: -10px;
            color: #fff;
            background: #bbb;
            border-radius: 50%;
            z-index: 2;
            width: 18px;
            height: 18px;
            display: none;
            align-items: center;
            justify-content: center;

            .ico-decorate {
                font-size: 12px;
            }
        }

        &:hover > .del-item {
            display: flex;
        }
    }

    .ghost {
        opacity: 0.5;
        background: #c8ebfb;
    }

    .chosen-class {
        background-color: #f3f3f3;
    }

    .dec-pic-add-btn {
        width: 100%;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 9px 16px;
        border: 1px solid var(--tig-primary, #72b76c);
        border-radius: 2px;
        background: #fff;
        color: var(--tig-primary, #72b76c);
        font-size: 14px;
        line-height: 20px;
        cursor: pointer;
        text-decoration: none;
        outline: none;
        appearance: none;
        font-family: inherit;

        i {
            margin-right: 4px;
        }
    }

    :deep(.dec-input-group) {
        .dialog-form-trigger {
            width: 100%;
        }
    }

    .selected-group-name {
        padding: 6px 12px;
        background-color: #f7f8fa;
        border-radius: 4px;
        font-size: 13px;
        color: #333;
        line-height: 20px;
    }

    .cobot-btn .dec-pic-add-btn {
        margin-top: 12px;
    }
}
</style>
