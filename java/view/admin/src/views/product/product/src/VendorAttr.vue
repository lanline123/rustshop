<template>
    <div class="attr-form">
        <el-form-item :label="$t('商品规格')" v-if="attrList.spe.length > 0">
            <div class="attr-list">
                <div class="attr-warp" v-for="(item, index1) in attrList.spe">
                    <div v-if="!disabled" class="btn-remove" @click="removeAttrList('spe', index1)">
                        <i class="iconfont icon-cha1"></i>
                    </div>
                    <div class="attr-warp-title">
                        <div class="flex align-center">
                            <div class="label">{{ $t('规格名：') }}</div>
                            <div>
                                <TigInput
                                    v-model="item.attrName"
                                    style="margin-right: 20px"
                                    width="250px"
                                    :disabled="disabled"
                                    @blur="dragEnd"
                                    :placeholder="$t('请输入规格项，如颜色、尺码、大小')"
                                />
                                <div class="extra mt10">{{ $t('该商品为供应商商品，暂不支持修改规格') }}</div>
                            </div>
                            <!-- specImageMode=2 时每组规格各自显示勾选框；否则仅第一组显示 -->
                            <div v-if="allSpecImageEnabled || index1 === 0" class="spec-image-checkbox">
                                <el-checkbox v-model="item.specImageRequired" :true-label="1" :false-label="0" :disabled="disabled" @change="onSpecImageToggle">
                                    添加规格图片
                                </el-checkbox>
                                <el-tooltip effect="light" placement="top">
                                    <template #content>
                                        <div style="width: 200px; padding: 5px 10px">
                                            {{
                                                allSpecImageEnabled
                                                    ? t('支持为每组规格设置规格图片，建议尺寸800 x 800，大小{imageUploadMaxSize}MB以下', { imageUploadMaxSize: configStore.imageUploadMaxSize })
                                                    : t('仅支持为一种规格设置规格图片建议尺寸800 x 800，大小{imageUploadMaxSize}MB以下', { imageUploadMaxSize: configStore.imageUploadMaxSize })
                                            }}
                                        </div>
                                    </template>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                        </div>
                    </div>
                    <div class="attr-warp-box">
                        <draggable
                            class="attr-row-box"
                            item-key="deagIndex"
                            :list="item.attrList"
                            ghost-class="sortable-ghost"
                            chosen-class="toolChosenClass"
                            animation="300"
                            @start=""
                            @end="dragEnd"
                            :group="{ name: 'advanced', pull: false, put: true }"
                        >
                            <template #item="{ element, index }">
                                <div class="draggable-item">
                                    <div class="attr-row">
                                        <TigInput
                                            v-model="element.attrValue"
                                            :placeholder="$t('请输入规格值，如：白色')"
                                            width="250px"
                                            :disabled="disabled"
                                            :suffix-icon="Rank"
                                        />
                                        <div v-if="item.attrList.length > 1 && !disabled" class="btn-remove2" @click="removeAttr('spe', index1, index)">
                                            <i class="iconfont icon-cha1"></i>
                                        </div>
                                        <!-- 规格图片区域：可编辑范围内且已勾选时可上传；不在可编辑范围但已有图片时只读回显（如切回仅第一组后的其他组） -->
                                        <div
                                            class="upload-img-wrap"
                                            v-if="(canGroupUploadImage(index1) && item.specImageRequired) || (!canGroupUploadImage(index1) && element.attrPicThumb)"
                                        >
                                            <div class="arrow"></div>
                                            <div
                                                class="pic-select-con"
                                                :class="{ 'pic-required-error': canGroupUploadImage(index1) && item.specImageRequired && !element.attrPicThumb }"
                                            >
                                                <div class="pic-select" v-if="canGroupUploadImage(index1) && !disabled">
                                                    <DialogForm type="gallery" @okCallback="addPic" :data="{ type: 'spe', index: index1, idx: index }">
                                                        <div class="pic-select-img">
                                                            <img v-if="element.attrPicThumb" :src="imageFormat(element.attrPicThumb)" />
                                                        </div>
                                                    </DialogForm>
                                                    <i
                                                        @click="removePic('spe', index1, index)"
                                                        v-if="element.attrPicThumb"
                                                        class="pic-select-del iconfont icon-cha"
                                                    ></i>
                                                </div>
                                                <div class="pic-select" v-else>
                                                    <div class="pic-select-img">
                                                        <img v-if="element.attrPicThumb" :src="imageFormat(element.attrPicThumb)" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <el-button
                                        v-if="index === item.attrList.length - 1"
                                        :disabled="disabled"
                                        @click="addAttr('spe', index1)"
                                        link
                                        type="primary"
                                        class="ml10"
                                        style="margin-top: 8px"
                                        ><i class="btn-ico">+</i>{{ $t('增加规格值') }}</el-button
                                    >
                                </div>
                            </template>
                        </draggable>
                    </div>
                    <div v-if="index1 < 1" class="extra mt10" style="text-align: center">
                        <template v-if="allSpecImageEnabled">支持为每组规格设置规格图片，买家选择不同规格会看到对应规格图片，建议尺寸：800 x 800像素</template>
                        <template v-else>{{ $t('仅支持为第一组规格设置规格图片，买家选择不同规格会看到对应规格图片，建议尺寸：800 x 800像素') }}</template>
                    </div>
                </div>
                <div class="attr-warp-title">
                    <el-button :disabled="disabled" @click="addAttrSpeList('spe')"><i class="btn-ico">+</i>{{ $t('添加商品规格') }}</el-button>
                </div>
            </div>
            <div class="red">{{ skuErrorText }}</div>
            <div v-if="specImageErrorText" class="red mt10">{{ specImageErrorText }}</div>
        </el-form-item>
        <el-form ref="productFormRef" :model="productList" label-width="100px" name="form_in_modal">
            <el-form-item :label="$t('规格明细')" prop="productList">
                <div class="lyecs-form-table">
                    <el-table :data="displaySkuList" style="width: 100%">
                        <!-- 多规格时显示动态规格列（左侧固定，与平台端一致） -->
                        <el-table-column
                            fixed="left"
                            v-if="productForm.length > 0"
                            v-for="attr in productForm[0].attrs"
                            prop="attrs"
                            :label="attr.attrName"
                            align="center"
                            min-width="100"
                        >
                            <template #default="{ row, $index }">
                                {{ getAttrValue(row.attrs, attr.attrName) }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuSn" :label="$t('规格编码')" align="center" min-width="120">
                            <template #default="{ row, $index }">
                                {{ row.skuSn || "-" }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuTsn" :label="$t('规格条码')" align="center" min-width="100">
                            <template #default="{ row, $index }">
                                {{ row.skuTsn || "-" }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="supplyPrice" :label="$t('供货价')" align="center" min-width="100">
                            <template #default="{ row, $index }">
                                {{ row.supplyPrice || "-" }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="marketPrice" :label="$t('市场价')" min-width="120">
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`productList.${rowIndex(row)}.marketPrice`">
                                    <TigInput type="decimal" v-model="row.marketPrice">
                                        <template #append> {{ config.dollarSign }} </template>
                                    </TigInput>
                                </el-form-item>
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuPrice" :label="$t('售价')" min-width="120">
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`productList.${rowIndex(row)}.skuPrice`" :rules="[{ required: true, validator: validatePrice }]">
                                    <TigInput
                                        type="decimal"
                                        v-model="row.skuPrice"
                                        @focus="_getVendorMaxPrice(rowIndex(row), row.vendorProductSkuId)"
                                        :max="row.maxPrice"
                                    >
                                        <template #append> {{ config.dollarSign }} </template>
                                    </TigInput>
                                </el-form-item>
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuStock" :label="$t('库存')" align="center" min-width="100">
                            <template #header>
                                <div class="flex flex-align-center flex-justify-center">{{ $t('库存') }}</div>
                            </template>
                            <template #default="{ row, $index }">
                                {{ row.skuStock }}
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
                <div v-if="paginationEnabled" class="sku-pagination">
                    <Pagination v-model:page="skuCurrentPage" v-model:size="skuPageSize" :total="productList.length" :page-sizes="[...SKU_PAGE_SIZES]" />
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { PopForm } from "@/components/pop-form";
import { Rank, QuestionFilled } from "@element-plus/icons-vue";
import draggable from "vuedraggable";
import { ref, onMounted, watch, computed, nextTick } from "vue";
import { Modal } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import request from "@/utils/request";
import { imageFormat } from "@/utils/format";
import PriceInput from "@/views/product/product/src/PriceInput.vue";
import { getVendorMaxPrice } from "@/api/vendor/product";
import { useConfigStore } from "@/store/config";
import { buildProductForm } from "@/utils/skuFormBuilder";
import { useSkuTablePagination, SKU_PAGE_SIZES } from "@/hooks/useSkuTablePagination";
import { Pagination } from "@/components/list";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
const config = configStore.config;
// 规格图片可上传范围：specImageMode=2 时全部规格组均可上传图片，否则仅第一组（由 getAdminBase 全局下发）
const allSpecImageEnabled = computed(() => config.specImageMode === 2);
const specImageErrorText = ref<string>(""); // 规格图片验证错误提示
// “添加规格图片”勾选状态按规格组维护在 item.specImageRequired 上（0/1）
/** 判断某规格组是否已存在规格图片 */
const groupHasImages = (group: any) => {
    const attrs = group?.attrList || [];
    return Array.isArray(attrs) && attrs.some((a: any) => a?.attrPicThumb || a?.attrPic);
};
/** 判断某规格组是否可上传规格图片（specImageMode=2 时全部组可上传，否则仅第一组） */
const canGroupUploadImage = (groupIndex: number) => allSpecImageEnabled.value || groupIndex === 0;
// 是否存在“可上传且已勾选”的规格组
const anySpecImageChecked = () =>
    (attrList.value?.spe || []).some((group: any, gi: number) => canGroupUploadImage(gi) && group?.specImageRequired);
// 验证规格图片：逐组校验“可上传且已勾选添加规格图片”的规格组，其规格值必须都已上传图片
const validateSpecImages = () => {
    const spe = attrList.value?.spe;
    if (Array.isArray(spe) && spe.length > 0) {
        for (let gi = 0; gi < spe.length; gi++) {
            const group = spe[gi];
            if (!canGroupUploadImage(gi) || !group?.specImageRequired) continue;
            const attrs = group?.attrList || [];
            for (let i = 0; i < attrs.length; i++) {
                if (!attrs[i].attrPicThumb) {
                    specImageErrorText.value = "请先完善规格图片";
                    return false;
                }
            }
        }
    }
    specImageErrorText.value = "";
    return true;
};
const checkSpecImagesValidation = () => {
    if (anySpecImageChecked()) {
        validateSpecImages();
    } else {
        specImageErrorText.value = "";
    }
};
const onSpecImageToggle = () => {
    checkSpecImagesValidation();
};
// 保存前：仅清空“可编辑范围内但未勾选添加规格图片”的规格组图片；
// 不在可编辑范围的规格组（如切回仅第一组后的其他组）保留已上传图片用于只读回显，不清空
const syncSpecImagesBeforeSave = () => {
    const spe = attrList.value?.spe;
    if (!Array.isArray(spe) || spe.length === 0) return;
    spe.forEach((group: any, gi: number) => {
        // 不在可编辑范围：保留已上传图片
        if (!canGroupUploadImage(gi)) return;
        // 可编辑且已勾选：保留
        if (group?.specImageRequired) return;
        // 可编辑但未勾选：清空该组图片
        const attrs = group?.attrList || [];
        if (!Array.isArray(attrs)) return;
        attrs.forEach((attr: any) => {
            attr.attrPicThumb = "";
            attr.attrPic = "";
            attr.attrPicId = "";
        });
    });
    specImageErrorText.value = "";
};
const visible = ref(false);
const props = defineProps({
    attrList: { type: Object, default: { normal: [], spe: [], extra: [] } },
    productList: { type: [Object, Array], default: [] },
    attrChanged: { type: Boolean, default: false },
    disabled: { type: Boolean, default: true },
    action: { type: String, default: "add" },
    attrTplList: {
        type: Array
    }
});
const productFormRef = ref();
const dialogTitle = ref("");
const skuErrorText = defineModel<string>("skuErrorText", { default: "" });
const emit = defineEmits(["update:productList", "update:attrList", "update:attrChanged"]);

const typeId = {
    normal: 0,
    spe: 1,
    extra: 2
};
const attrTplId = ref(null);

const batchInput = ref<any>({});
const attrList = ref<any>(props.attrList);
const productList = ref<any>(props.productList);
// 初始化各规格组的“添加规格图片”勾选状态：已有规格图的组默认勾选；仅在未初始化时赋值，避免覆盖用户操作
watch(
    () => attrList.value?.spe,
    (newValue) => {
        if (!Array.isArray(newValue)) return;
        newValue.forEach((group: any) => {
            if (group && (group.specImageRequired === undefined || group.specImageRequired === null)) {
                group.specImageRequired = groupHasImages(group) ? 1 : 0;
            }
        });
    },
    { deep: true, immediate: true }
);

// ==================== SKU 前端分页（供应商商品：规格不可改，仅分页浏览/改价） ====================
const {
    currentPage: skuCurrentPage,
    pageSize: skuPageSize,
    paginationEnabled,
    pagedList: pagedSkuList,
    rowIndex,
    goToIndex
} = useSkuTablePagination<any>(() => productList.value || []);
const displaySkuList = computed(() => (paginationEnabled.value ? pagedSkuList.value : productList.value));

const getAttrValue = (attrs: any[], attrName: string) => {
    const attr = attrs.find((attr) => attr.attrName === attrName);
    return attr ? attr.attrValue : "";
};
const totalStock = computed(() => {
    let total = 0;
    for (let i in productList.value) {
        total += Number(productList.value[i].skuStock);
    }
    return total;
});
const isImg = ref<any>(false);
const newAttrState = ref({
    name: "",
    type: ""
});
let watchLoaded = false;
// 动态规格表单>

onMounted(() => {
    emit("update:productList", productList);
    emit("update:attrList", attrList);
});
const validateStock = (rule: any, value: any, callback: any) => {
    const index = rule.field.split(".")[1];
    let stock = Number(productList.value[index].skuStock);
    productList.value[index].skuStock = stock;
    if (isNaN(stock)) {
        callback(new Error(t('请输入数字')));
        return;
    } else if (stock == null || stock == undefined || Number.isNaN(stock)) {
        callback(new Error(t('请输入商品库存')));
        return;
    } else if (props.action != "add" && stock < 0) {
        callback(new Error(t('库存不能是负数')));
        return;
    } else if (!Number.isInteger(stock)) {
        callback(new Error(t('商品库存必须是整数')));
        return;
    } else {
        callback();
    }
};
// 售价校验规则（唯一真相源）：el-form validator 与分页兜底共用。maxPrice 未聚焦时为 NaN，上限比较自然跳过
const getSkuPriceError = (row: any): string => {
    const price = Number(row?.skuPrice);
    const maxPrice = Number(row?.maxPrice);
    const supplyPrice = Number(row?.supplyPrice);
    if (!price) return t('请输入商品售价');
    if (price > maxPrice) return t('价格最大不可超过') + maxPrice;
    if (price < supplyPrice) return t('售价不可小于供货价') + supplyPrice;
    return "";
};
const validatePrice = (rule: any, value: any, callback: any) => {
    const index = rule.field.split(".")[1];
    const err = getSkuPriceError(productList.value[index]);
    callback(err ? new Error(err) : undefined);
};
const _getVendorMaxPrice = async (index: number, vendorProductSkuId: any) => {
    console.log("_getVendorMaxPrice", vendorProductSkuId);
    try {
        const result = await getVendorMaxPrice({ vendorProductSkuId });
        productList.value[index].maxPrice = result;
    } catch (error: any) {
        console.log(error);
    }
};
// 分页下非当前页 el-form 未挂载，兜底全量扫描（售价为唯一可编辑校验项）
const validateSkuRow = (row: any): string => getSkuPriceError(row);
const findFirstInvalidSku = (): { index: number; message: string } | null => {
    const list = productList.value || [];
    for (let i = 0; i < list.length; i++) {
        const msg = validateSkuRow(list[i]);
        if (msg) return { index: i, message: msg };
    }
    return null;
};
const validateProductSku = async () => {
    // 保存时清理未勾选/不可上传规格组的图片，并校验已勾选规格组的图片完整性
    syncSpecImagesBeforeSave();
    if (!validateSpecImages()) {
        return Promise.reject(new Error(specImageErrorText.value || "请先完善规格图片"));
    }
    const bad = findFirstInvalidSku();
    if (bad) {
        if (paginationEnabled.value) {
            goToIndex(bad.index);
            await nextTick();
        }
        try {
            await productFormRef.value.validate();
        } catch (e) {}
        return Promise.reject(new Error(bad.message));
    }
    await productFormRef.value.validate();
};
const dragEnd = (event: any) => {
    recomputeProductForm();
};
// 添加属性项
const addAttr = (type: string, index: number) => {
    attrList.value[type][index].attrList.push({
        attrValue: "",
        attrType: (typeId as any)[type],
        attrName: attrList.value[type][index].attrName
    });
    checkSpecImagesValidation();
};
// 移除属性项
const removeAttr = (type: string, index: number, idx: number) => {
    attrList.value[type][index].attrList.splice(idx, 1);
    recomputeProductForm();
    checkSpecImagesValidation();
};
// 移除类型
const removeAttrList = (type: string, index: number) => {
    attrList.value[type].splice(index, 1);
    recomputeProductForm();
};
// 新增类型
const addAttrList = (type: string, title: string) => {
    visible.value = true;
    dialogTitle.value = title;
    newAttrState.value.name = "";
    newAttrState.value.type = type;
};
const addAttrSpeList = (type: string) => {
    attrList.value[type].push({
        attrName: "",
        attrList: [
            {
                attrType: (typeId as any)[type],
                attrName: "",
                attrValue: ""
            }
        ]
    });
};
// 添加图片
const addPic = (result: any, data: any) => {
    attrList.value[data.type][data.index].attrList[data.idx].attrPicId = result[0].picId;
    attrList.value[data.type][data.index].attrList[data.idx].attrPic = result[0].picUrl;
    attrList.value[data.type][data.index].attrList[data.idx].attrPicThumb = result[0].picThumb;
    checkSpecImagesValidation();
};
// 移除图片
const removePic = (type: string, index: number, idx: number) => {
    attrList.value[type][index].attrList[idx].attrPic = "";
    attrList.value[type][index].attrList[idx].attrPicThumb = "";
    attrList.value[type][index].attrList[idx].attrPicId = "";
    checkSpecImagesValidation();
};
//导入模板
const importAttrTpl = () => {
    Modal.confirm({
        title: t('导入模板会清空并重置已设置的属性、规格、规格库存等设置，确认导入吗？'),
        async onOk() {
            request({
                url: "product/productAttributesTpl/detail?id=" + attrTplId.value,
                method: "get"
            }).then((result: any) => {
                let data = result.tplData;
                for (let type in data) {
                    attrList.value[type] = [];
                    for (let i in data[type]) {
                        attrList.value[type].push({
                            attrName: data[type][i].attrName,
                            attrList: [
                                {
                                    attrValue: "",
                                    attrType: (typeId as any)[type],
                                    attrName: data[type][i].attrName
                                }
                            ]
                        });
                    }
                }
            });
        }
    });
};
// 批量操作：
const onBatchClick = () => {
    let i = 1;
    for (let index in productList.value) {
        if (batchInput.value.skuStock) productList.value[index].skuStock = batchInput.value.skuStock;
        if (batchInput.value.skuSn) productList.value[index].skuSn = batchInput.value.skuSn + "-" + i;
        if (batchInput.value.skuTsn) productList.value[index].skuTsn = batchInput.value.skuTsn + "-" + i;
        if (batchInput.value.skuPrice) productList.value[index].skuPrice = batchInput.value.skuPrice;
        i++;
    }
};
// 添加属性名称
const formRef = ref();
const onOk = (e: any) => {
    e.validate((valid: any) => {
        if (valid) {
            let isDuplicated = attrList.value[newAttrState.value.type].find((obj: any) => obj.attrName === newAttrState.value.name);
            if (isDuplicated) {
                Modal.error({
                    title: t('属性名称“') + newAttrState.value.name + t('”已存在')
                });
                return;
            }
            attrList.value[newAttrState.value.type].push({
                attrName: newAttrState.value.name,
                attrList: [
                    {
                        attrType: (typeId as any)[newAttrState.value.type],
                        attrName: newAttrState.value.name,
                        attrValue: ""
                    }
                ]
            });
            visible.value = false;
            dialogTitle.value = "";
        }
    });
};
const productForm = ref<any>([]);

const recomputeProductForm = () => {
    if (!attrList.value?.spe || attrList.value.spe.length === 0) {
        // 无规格：保留已有单 SKU 明细，仅清空动态规格列
        productForm.value = [];
        return;
    }
    // 规格组名同步到子行（builder 为纯函数，不改入参）
    (attrList.value.spe || []).forEach((group: any) => {
        if (group?.attrList?.length) {
            group.attrList.forEach((attr: any) => {
                if (attr.attrName !== group.attrName) attr.attrName = group.attrName;
            });
        }
    });
    const spePlain = JSON.parse(JSON.stringify(attrList.value.spe));
    const existingClone = JSON.parse(JSON.stringify(productList.value || []));
    productList.value = buildProductForm(spePlain, existingClone, { useMap: true, variant: "vendor" });
    productForm.value = productList.value;
};
// 监听attrList值是否有变化，如果有，则会更新参数
watch(
    () => attrList.value,
    (newValue, oldValue) => {
        // 判断值是否有变化
        if (watchLoaded == true) {
            emit("update:attrChanged", true);
            // stopWatch()
        }
        recomputeProductForm();
        watchLoaded = true;
    },
    { deep: true, immediate: true }
);
defineExpose({ skuErrorText, validateProductSku });
</script>

<style lang="less" scoped>
// 规格图片必填错误样式
.pic-required-error {
    :deep(.pic-select) {
        border: 1px dashed var(--el-color-danger) !important;
    }
}

// 规格图片复选框样式
.spec-image-checkbox {
    display: flex;
    align-items: center;
    margin-left: 15px;
}

.lyecs-form-table {
    width: 100%;
    :deep(.el-table .cell) {
        overflow: visible !important;
    }
    :deep(.el-form-item__error) {
        position: static !important;
        width: 100%;
        line-height: 16px;
        white-space: normal;
        overflow-wrap: anywhere;
    }
    :deep(.el-form-item) {
        margin-bottom: 5px;
    }
    :deep(.el-input-group__append, .el-input-group__prepend) {
        padding: 0 10px;
    }
    .rate {
        :deep(.el-form-item__error) {
            position: absolute !important;
            z-index: 99;
            top: 32px !important;
            right: 83px !important;
            left: inherit !important;
        }
    }
    .down_salesman_rate {
        :deep(.el-form-item__error) {
            position: absolute !important;
            z-index: 99;
            top: 52px !important;
            right: 83px !important;
            left: inherit !important;
        }
    }
}

.sku-pagination {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
}
.attr-list {
    width: 100%;
    border: 1px solid #eeeeee;
    padding: 10px;
    .attr-warp-title {
        background-color: #f7f8fa;
        padding: 10px;
        cursor: pointer;
        .title {
            width: 100px;
            .label {
                flex: 1;
                text-align: right;
                white-space: nowrap; /* 防止文字换行 */
                overflow: hidden; /* 隐藏溢出的文字 */
                text-overflow: ellipsis; /* 显示省略号来代替溢出的文字 */
            }
        }
    }
    .attr-warp {
        margin-bottom: 20px;
        position: relative;

        .attr-warp-box {
            display: flex;
            margin-left: 60px;
            .attr-row-box {
                flex: 1;
                margin-top: 20px;
                display: flex;
                flex-wrap: wrap;
                gap: 10px;
                .attr-row {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }
                .draggable-item {
                    display: flex;
                    align-items: flex-start;
                    .attr-row {
                        position: relative;
                        .btn-remove2 {
                            position: absolute;
                            top: -5px;
                            right: -5px;
                            color: #fff;
                            background-color: #7b7b7b;
                            width: 16px;
                            height: 16px;
                            line-height: 14px;
                            text-align: center;
                            border-radius: 50%;
                            cursor: pointer;
                            opacity: 0;
                            i {
                                font-size: 8px;
                                font-weight: bold;
                            }
                        }
                        &:hover {
                            .btn-remove2 {
                                opacity: 1;
                            }
                        }
                    }
                }
            }
            .attr-btn {
                margin-top: 15px;
                margin-left: 10px;
            }
        }
        .btn-remove {
            position: absolute;
            top: 10px;
            right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 18px;
            height: 18px;
            line-height: 16px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            opacity: 0;
            i {
                font-size: 10px;
                font-weight: bold;
            }
        }
        &:hover {
            .btn-remove {
                opacity: 1;
            }
        }
    }
}
.other-attr {
    .attr-warp-title {
        .btn-remove3 {
            margin-right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 16px;
            height: 16px;
            line-height: 11px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            opacity: 0;
            i {
                font-size: 8px;
                font-weight: bold;
            }
        }
        .btn-remove4 {
            margin-right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 16px;
            height: 16px;
            line-height: 11px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            i {
                font-size: 8px;
                font-weight: bold;
            }
        }
        &:hover {
            .btn-remove3 {
                opacity: 1;
            }
        }
    }
}
.upload-img-wrap {
    background: #fff;
    border: 1px solid #dcdcdc;
    border-radius: 4px;
    padding: 2px;
    width: 84px;
    margin-top: 10px;
}
.upload-img-wrap .arrow {
    border-color: transparent;
    border-style: solid;
    border-bottom: 5px solid #dcdcdc;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    height: 0;
    left: 44%;
    position: relative;
    top: -7px;
    width: 0;
    &::after {
        border-color: transparent transparent #fff;
        border-style: solid;
        border-width: 0 10px 10px;
        content: "";
        display: block;
        height: 0;
        margin-left: -10px;
        position: absolute;
        top: 1px;
        width: 0;
    }
}
.pic-select-con :deep(.pic-select) {
    display: flex;
    align-items: center;
    justify-content: center;
    padding-left: 0;
    position: relative;
}

.pic-select-con :deep(.pic-select-img) {
    height: 70px;
    width: 70px;
    padding-left: 0;
    background-color: #fff;
    border-radius: 2px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
}

.pic-select-con :deep(.pic-select-img:after) {
    content: "\e637";
    font-family: "admin-iconfont";
    font-size: 30px;
    position: absolute;
    top: 0;
    line-height: 70px;
    width: 70px;
    left: 0;
    z-index: 1;
    text-align: center;
    color: #a6a5a5;
}

.pic-select-con :deep(.pic-select .pic-select-del) {
    position: absolute;
    cursor: pointer;
    right: -8px;
    top: -13px;
    color: #fff;
    background: #dbdbdb;
    border-radius: 50%;
    z-index: 2;
    width: 16px;
    height: 16px;
    text-align: center;
    line-height: 16px;
    font-size: 12px;
    display: none;
}

.pic-select-con:hover :deep(.pic-select .pic-select-del) {
    display: block;
    background-color: #7b7b7b;
}

.pic-select-con :deep(.pic-select .pic-select-img img) {
    width: 70px;
    height: 70px;
    position: relative;
    z-index: 2;
}

@media (max-width: 768px) {
    .attr-list {
        .attr-warp-title {
            .flex.align-center, .title-box.flex.align-center, .title.flex, .title {
                flex-wrap: wrap;
                width: 100% !important;

                .label {
                    width: 100%;
                    text-align: left;
                    white-space: normal;
                    margin-bottom: 5px;
                }

                > div:not(.label) {
                    width: 100% !important;
                    margin-top: 0;
                }

                :deep(.el-input) {
                    width: 100% !important;
                }
            }
        }

        .attr-warp {
            .attr-warp-box {
                margin-left: 0;
            }
            .attr-row-box {
                flex-direction: column;
                align-items: flex-start;

                .draggable-item {
                    width: 100%;
                    flex-wrap: wrap;
                    align-items: center;

                    .attr-row {
                        width: 100%;
                        flex-direction: row;
                        align-items: center;

                        > div {
                            flex: 1;
                        }

                        :deep(.el-input) {
                            width: 100% !important;
                        }
                    }

                    > .el-button {
                        margin-top: 10px !important;
                        margin-left: 0 !important;
                    }
                }
            }
        }

        :deep(.business-data-wrap) {
            width: 100% !important;
        }
    }
}
</style>
