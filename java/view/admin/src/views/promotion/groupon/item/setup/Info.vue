<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <el-form-item :label="$t('商品名称')">
                        <div>{{ product.productName }}</div>
                    </el-form-item>
                    <el-form-item v-if="groupType === 1" class="inner-item" :label="$t('成团人数')" required prop="groupNums">
                        <div style="padding-bottom: 10px">
                            <TigInput class="InputBox" v-model="ladderData[0].groupNum" :placeholder="$t('请输入')" type="integer" width="200px">
                                <template #append>{{ $t('人') }}</template>
                            </TigInput>
                        </div>
                    </el-form-item>
                    <el-form-item
                        v-if="groupType === 2"
                        v-for="(ladder, index) in ladderData"
                        :key="ladder.groupNumKey"
                        :label="$t('第{ladderTextindex}阶梯', { ladderTextindex: ladderText[index] })"
                        :prop="`groupNums.${index}.groupNum`"
                        class="groupNums"
                        :rules="[{ required: true, validator: validateGroupNum, trigger: 'blur' }]"
                        style="margin-bottom: 14px"
                    >
                        <div class="num-box">
                            <TigInput class="InputBox inputBox-margin" :placeholder="$t('请输入')" type="integer" width="200px" v-model="ladderData[index].groupNum">
                                <template #append>{{ $t('人') }}</template>
                            </TigInput>
                            <a v-if="index >= 2" class="btn-link" @click="removeLadder(index)">{{ $t('删除') }}</a>
                        </div>
                    </el-form-item>
                    <el-form-item v-if="groupType === 2 && ladderData.length < 5" label="&nbsp;">
                        <div>
                            <a class="btn-link" @click="addLadder">{{ $t('添加阶梯') }}</a>
                            <div class="desc">{{ $t('最多支持添加5个阶梯') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('拼团信息')">
                        <div class="promotion-table">
                            <el-table style="width: 100%" :data="formState.grouponPromotionDetails">
                                <el-table-column v-for="attr in skuAttributes" :key="attr.name" :prop="attr.name" :label="attr.name" align="center">
                                    <template #default="{ row }">
                                        {{ getSkuAttributeValue(row.skuData, attr.name) }}
                                    </template>
                                </el-table-column>
                                <el-table-column prop="productPrice" :label="$t('原价')" align="center"></el-table-column>
                                <el-table-column v-if="groupType === 1" :label="$t('拼团价')" width="150">
                                    <template #default="{ row, $index }">
                                        <el-form-item
                                            label=""
                                            :prop="`grouponPromotionDetails.${$index}.groupPromotionDetailPrices.0.price`"
                                            :rules="[{ required: true, validator: validatePrice, trigger: 'blur' }]"
                                        >
                                            <PriceInput style="width: 100px" v-model="row.groupPromotionDetailPrices[0].price"> </PriceInput>
                                        </el-form-item>
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    v-for="(_, index) in ladderData.length"
                                    v-if="groupType === 2"
                                    :key="index"
                                    :label="$t('第{ladderTextindex}阶梯', { ladderTextindex: ladderText[index] })"
                                    width="150"
                                >
                                    <template #default="{ row, $index }">
                                        <el-form-item
                                            label=""
                                            :prop="`grouponPromotionDetails.${$index}.groupPromotionDetailPrices.${index}.price`"
                                            :rules="[{ required: true, validator: validatePrice }]"
                                        >
                                            <PriceInput style="width: 100px" v-model="row.groupPromotionDetailPrices[index].price"> </PriceInput>
                                        </el-form-item>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('可售数量')" width="100" prop="limitNum" align="center">
                                    <template #header>
                                        <el-tooltip class="box-item" effect="dark" placement="top" :show-after="300">
                                            <template #content>
                                                <div style="width: 200px; padding: 5px 10px">{{ $t('使用活动价可售的商品数量，超过设置的数量，仅可按原价购买') }}</div>
                                            </template>
                                            <div class="flex flex-align-center">
                                                <div>{{ $t('可售数量') }}</div>
                                                <el-icon style="margin-left: 5px" size="14" color="#999">
                                                    <QuestionFilled />
                                                </el-icon>
                                            </div>
                                        </el-tooltip>
                                    </template>
                                    <template #default="{ row, $index }">
                                        <el-form-item
                                            label=""
                                            :prop="`grouponPromotionDetails.${$index}.limitNum`"
                                            :rules="[{ required: true, validator: validateSaleNum, trigger: 'blur' }]"
                                        >
                                            <TigInput style="width: 60px" type="integer" classType="tig-form-input" v-model="row.limitNum" />
                                        </el-form-item>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="productStock" :label="$t('商品库存')" width="100" align="center"></el-table-column>
                                <el-table-column prop="isJoin" :label="$t('是否参加')" align="center" fixed="right">
                                    <template #default="{ row }">
                                        <el-switch v-model="row.isJoin" :active-value="1" :inactive-value="0" />
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                    </el-form-item>
                    <el-form-item class="inner-item" :label="$t('每单限购')" prop="orderLimitNum" required>
                        <el-radio-group
                            v-model="isLimit"
                            style="width: 100%; margin-bottom: 10px; flex-direction: column; align-items: flex-start"
                            @change="handleLimitChange"
                        >
                            <el-radio :value="false">{{ $t('不限购') }}</el-radio>
                            <el-radio :value="true">
                                <div class="flex">
                                    <div class="mr10">{{ $t('限购') }}</div>
                                    <TigInput v-model="formState.orderLimitNum" :disabled="!isLimit" :placeholder="$t('请输入限购数量')" type="integer" width="200px">
                                        <template #append>{{ $t('件') }}</template>
                                    </TigInput>
                                </div>
                            </el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('自动成团')" prop="isAutoGroup">
                        <div>
                            <Checkbox v-model="formState.isAutoGroup">
                                <template #default>{{ $t('开启') }}</template>
                            </Checkbox>
                            <div class="desc">{{ $t('成团有效期结束时，达到最低成团人数，即可用匿名用户模拟成团') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item
                        class="inner-item"
                        :label="$t('成团最低人数')"
                        prop="minGroupNum"
                        v-if="formState.isAutoGroup"
                        :rules="[{ required: true, validator: validMiniGroupNums, trigger: 'blur' }]"
                    >
                        <div>
                            <TigInput
                                :decimalPlaces="3"
                                class="InputBox"
                                :placeholder="$t('请输入')"
                                type="integer"
                                v-model="formState.minGroupNum"
                                width="200px"
                                @blur="handleMinGroupNumChange"
                            >
                                <template #append>{{ $t('人') }}</template>
                            </TigInput>
                            <div class="extra">{{ $t('可填写1~{groupNumMax}成团人数之间的整数', { groupNumMax }) }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('活动商品排序')" prop="sort">
                        <div>
                            <TigInput classType="tig-form-input" type="integer" width="200px" v-model="formState.sort" />
                            <div class="extra">{{ $t('序号越小，排名越前') }}</div>
                        </div>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, reactive, computed } from "vue";
import { useRouter } from "vue-router";
import { Checkbox } from "@/components/radio";
import PriceInput from "@/views/product/product/src/PriceInput.vue";
import { message } from "ant-design-vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import { getGrouponItem, updateGrouponItem } from "@/api/promotion/groupon";
import { groupNumState, GrouponItemFormState, grouponPromotionDetailState, groupPromotionDetailPriceState } from "@/types/promotion/groupon";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    grouponPromotionId: {
        type: Number,
        default: 0
    },
    id: {
        type: Number,
        default: 0
    },
    groupType: {
        type: Number,
        default: 1
    },
    product: {
        type: Object,
        default: {}
    },
    rowData: {
        type: Object,
        default: {}
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: {
        type: Boolean,
        default: false
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const groupType = ref<number>(props.isDialog ? props.groupType : Number(query.groupType));
const productSkus = ref<any>(props.product.productSkus || []);
const formRef = shallowRef();
const formState = ref<GrouponItemFormState>({
    grouponPromotionDetails: []
});
const isLimit = defineModel<boolean>("isLimit", { default: false });
const operation = action.value === "add" ? "create" : "update";

// 如果是普通团，默认为1个阶梯。阶梯团默认2个，最多5个
const initLadderData = () => {
    if (props.product.groupNum) {
        try {
            ladderData.value = JSON.parse(props.product.groupNum);
        } catch (error) {
            ladderData.value = groupType.value === 1 ? [{ groupNumKey: 1 }] : [{ groupNumKey: 1 }, { groupNumKey: 2 }];
            ladderData.value = [];
            console.log("解析阶梯数据失败");
        }
    } else {
        ladderData.value = groupType.value === 1 ? [{ groupNumKey: 1 }] : [{ groupNumKey: 1 }, { groupNumKey: 2 }];
    }
};
const ladderData = ref<groupNumState[]>([]);

const ladderText = computed(() => {
    const texts = [t('一'), t('二'), t('三'), t('四'), t('五')];
    return texts.slice(0, ladderData.value.length);
});

/**
 * 添加活动商品时候，初始化表单数据
 */
const initFormState = () => {
    formState.value.groupNums = ladderData.value;
    formState.value.grouponPromotionId = props.grouponPromotionId;
    formState.value.productId = props.product.productId;
    formState.value.groupType = groupType.value;

    // 普通团
    if (!productSkus.value || !productSkus.value.length) {
        const productItem: grouponPromotionDetailState = {
            productId: props.product.productId,
            productPrice: props.product.productPrice,
            marketPrice: props.product.marketPrice,
            limitNum: props.product.limitNum,
            skuId: 0,
            productStock: props.product.productStock,
            isJoin: 1,
            groupPromotionDetailPrices: [] as groupPromotionDetailPriceState[]
        };
        // 初始化价格
        ladderData.value.map((item: any) => {
            productItem.groupPromotionDetailPrices?.push({
                groupNumKey: item.groupNumKey,
                price: ""
            });
        });
        formState.value.grouponPromotionDetails = [productItem];
    } else {
        const _productList: grouponPromotionDetailState[] = [];
        const productItem: grouponPromotionDetailState = {
            productId: props.product.productId,
            isJoin: 1,
            groupPromotionDetailPrices: []
        };
        productSkus.value.map((item: any) => {
            try {
                productItem.productPrice = item.skuPrice;
                productItem.marketPrice = item.marketPrice;
                productItem.productStock = item.skuStock;
                productItem.skuId = item.skuId;
                productItem.skuData = item.skuData;
                productItem.limitNum = item.limitNum || "";
                productItem.groupPromotionDetailPrices = [] as groupPromotionDetailPriceState[];
                if (item.groupPromotionDetailPrices) {
                    productItem.groupPromotionDetailPrices = item.groupPromotionDetailPrices;
                } else {
                    ladderData.value.map((item: any, index: number) => {
                        productItem.groupPromotionDetailPrices?.push({
                            groupNumKey: item.groupNumKey,
                            price: ""
                        });
                    });
                }
                _productList.push({ ...productItem });
            } catch {}
        });
        formState.value.grouponPromotionDetails = _productList;
    }
};

// 计算所有SKU属性名称
const skuAttributes = computed(() => {
    const attributes: { name: string }[] = [];
    const attrNames = new Set<string>();
    if (productSkus.value && Array.isArray(productSkus.value)) {
        productSkus.value.forEach((sku: any) => {
            if (sku.skuData) {
                sku.skuData.forEach((attr: any) => {
                    if (attr.name && !attrNames.has(attr.name)) {
                        attrNames.add(attr.name);
                        attributes.push(attr);
                    }
                });
            }
        });
    }
    return attributes;
});

// 添加阶梯
const addLadder = () => {
    if (ladderData.value.length < 5) {
        const newKey = ladderData.value.length > 0 ? Math.max(...ladderData.value.map((item: { groupNumKey?: number }) => item.groupNumKey || 0)) + 1 : 1;
        ladderData.value.push({
            groupNumKey: newKey
        });
        formState.value.groupNums = ladderData.value;
        for (const grouponPromotion of formState.value.grouponPromotionDetails) {
            grouponPromotion.groupPromotionDetailPrices.push({
                groupNumKey: newKey,
                price: ""
            });
        }
    }
};

// 删除阶梯
const removeLadder = (index: number) => {
    // 只能删除第3个及以后的阶梯（索引为2及以上）
    if (index >= 2 && ladderData.value.length > 2) {
        ladderData.value.splice(index, 1);
        formState.value.groupNums = ladderData.value;
        for (const grouponPromotion of formState.value.grouponPromotionDetails) {
            grouponPromotion.groupPromotionDetailPrices.splice(index, 1);
        }
    }
};

// 获取SKU属性值
const getSkuAttributeValue = (skuData: any, attrName: string) => {
    try {
        if (Array.isArray(skuData)) {
            const attr = skuData.find((item) => item.name === attrName);
            return attr ? attr.value : "";
        }
    } catch (e) {
        console.error("解析skuData失败", e);
    }
    return "";
};

const handleLimitChange = (value: any) => {
    if (value) {
        formState.value.orderLimitNum = !formState.value.orderLimitNum ? 1 : formState.value.orderLimitNum;
    } else {
        formState.value.orderLimitNum = undefined;
    }
};

onMounted(() => {
    initLadderData();
    if (action.value === "detail") {
        // 获取详情数据
        fetchGrouponItem();
    } else {
        initFormState();
        loading.value = false;
    }
});
const fetchGrouponItem = async () => {
    try {
        const result = await getGrouponItem(action.value, {
            grouponPromotionItemId: id.value
        });
        Object.assign(formState.value, result);
        productSkus.value = result.productSkus;
        const orderLimitNum = Number(result.orderLimitNum);
        if (orderLimitNum && orderLimitNum > 0) {
            isLimit.value = true;
        }
        // 循环生产列表
        if (formState.value.grouponPromotionDetails && formState.value.grouponPromotionDetails) {
            for (const grouponItem of result.grouponPromotionDetails) {
                // 如果有sku，则原件用skuprice
                if (grouponItem.skuId) {
                    // 从skus中获取sku
                    const sku = productSkus.value.find((sku: any) => sku.skuId === grouponItem.skuId);
                    if (sku) {
                        grouponItem.productPrice = sku.skuPrice;
                        grouponItem.productStock = sku.skuStock;
                        grouponItem.skuData = sku.skuData;
                    }
                } else {
                    grouponItem.productPrice = formState.value.productPrice;
                    grouponItem.productStock = formState.value.productStock;
                }
            }
        }
    } catch (error: any) {
        console.log(error);
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const validatePrice = (rule: any, value: any, callback: any) => {
    // 从rule中获取当前验证字段的路径信息
    const field = rule.fullField; // 例如: "grouponPromotionDetails.0.groupPromotionDetailPrices.0.price"
    const matches = field.match(/grouponPromotionDetails\.(\d+)\.groupPromotionDetailPrices\.(\d+)\.price/);
    if (matches) {
        const detailIndex = parseInt(matches[1]);
        const priceIndex = parseInt(matches[2]);
        const item = formState.value.grouponPromotionDetails[detailIndex];
        // 如果当前商品不参与拼团，则跳过验证
        if (item.isJoin !== 1) {
            callback();
            return;
        }
        const sPromotionPrice = item.groupPromotionDetailPrices[priceIndex].price;
        if (sPromotionPrice === "") {
            callback(new Error(t('拼团价格不能为空')));
            return;
        }
        const promotionPrice = Number(sPromotionPrice);
        const productPrice = Number(item.productPrice);
        if (promotionPrice > productPrice) {
            callback(new Error(t('拼团价格不能大于商品原价')));
            return;
        }

        // 阶梯团价格递减验证
        if (groupType.value === 2) {
            // 检查当前阶梯价格是否比上一阶梯价格低
            if (priceIndex > 0) {
                const prevPrice = Number(item.groupPromotionDetailPrices[priceIndex - 1].price);
                if (!isNaN(prevPrice) && prevPrice > 0 && promotionPrice >= prevPrice) {
                    callback(new Error(t('第{valuepriceIndex}阶梯价格应低于第{valuepriceIndex1}阶梯价格', { valuepriceIndex: ladderText.value[priceIndex], valuepriceIndex1: ladderText.value[priceIndex - 1] })));
                    return;
                }
            }
        }
    }
    callback();
};

/**
 * 验证成团人数
 * @param rule
 * @param value 当前值
 * @param callback 回调函数
 */
const validateGroupNum = (rule: any, _value: any, callback: any) => {
    // 如果是普通团，直接验证第一个阶梯
    if (groupType.value === 1) {
        if (!ladderData.value[0]?.groupNum) {
            callback(new Error(t('成团人数不能为空')));
            return;
        }
        if (ladderData.value[0].groupNum < 1) {
            callback(new Error(t('成团人数须大于0')));
            return;
        }
        callback();
        return;
    }
    // 如果是阶梯团，从rule中获取当前验证字段的路径信息
    const field = rule.fullField; // 例如: "groupNums.0.groupNum"
    const matches = field.match(/groupNums\.(\d+)\.groupNum/);

    if (matches) {
        const index = parseInt(matches[1]);
        const groupNumItem = ladderData.value[index];

        if (!groupNumItem?.groupNum) {
            callback(new Error(t('成团人数不能为空')));
            return;
        }
        if (groupNumItem.groupNum < 1) {
            callback(new Error(t('成团人数必须大于0')));
            return;
        }

        // 验证当前阶梯人数必须大于上一个阶梯的人数（第一个阶梯除外）
        if (index > 0) {
            const prevGroupNum = ladderData.value[index - 1].groupNum;
            // 确保两个值都存在且为数字再进行比较
            if (prevGroupNum && Number(groupNumItem.groupNum) <= Number(prevGroupNum)) {
                callback(new Error(t('第{valueindex}阶梯成团人数必须大于第{valueindex1}阶梯成团人数', { valueindex: ladderText.value[index], valueindex1: ladderText.value[index - 1] })));
                return;
            }
        }
    }

    callback();
};

const groupNumMax = computed(() => {
    // 普通团直接拿最低成团人数
    if (groupType.value === 1) {
        return ladderData.value[0].groupNum;
    }
    // 拿到阶梯的最小成团人数
    // 过滤掉groupNum为空的项，并获取最小值
    const validGroupNums = ladderData.value.filter((item) => item.groupNum !== undefined && item.groupNum !== null).map((item) => item.groupNum);

    if (validGroupNums.length === 0) {
        return null; // 如果没有有效的groupNum，返回null
    }

    return Math.min(...(validGroupNums as number[]));
});

/**
 * 处理成团人数改变
 * @param value
 */
const handleMinGroupNumChange = (value: any) => {
    if (Number(formState.value.minGroupNum) < 1) {
        formState.value.minGroupNum = 1;
    }
    if (Number(formState.value.minGroupNum) > Number(groupNumMax.value)) {
        formState.value.minGroupNum = groupNumMax.value;
    }
};

// 验证可售数量
const validateSaleNum = (rule: any, value: any, callback: any) => {
    // 从rule中获取当前验证字段的路径信息
    const field = rule.fullField; // 例如: "grouponPromotionDetails.0.limitNum"
    const matches = field.match(/grouponPromotionDetails\.(\d+)\.limitNum/);

    if (matches) {
        const detailIndex = parseInt(matches[1]);
        const item = formState.value.grouponPromotionDetails[detailIndex];
        // 如果当前商品不参与拼团，则跳过验证
        if (item.isJoin !== 1) {
            callback();
            return;
        }
        if (!item.limitNum) {
            callback(new Error(t('可售数量不能为空')));
        }
        const limitNum = Number(item.limitNum);
        let productStock = Number(item.productStock);
        if (isNaN(productStock)) {
            productStock = 0;
        }
        if (limitNum === 0) {
            callback(new Error(t('可售数量不能为0')));
            return;
        }
        if (limitNum > productStock) {
            callback(new Error(t('可售数量不能大于商品库存数量')));
            return;
        }
    }
    callback();
};

// 验证限购
const validateOrderLimitNum = (rule: any, value: any, callback: any) => {
    if (isLimit.value) {
        if (!formState.value.orderLimitNum || (formState.value.orderLimitNum as number) < 1) {
            callback(new Error(t('限购数量不能为空')));
            return;
        }
    }
    callback();
};

// 验证成团最低人数
const validMiniGroupNums = (rule: any, value: any, callback: any) => {
    if (!formState.value.minGroupNum) {
        callback(new Error(t('成团最低人数不能为空')));
    } else if (Number(formState.value.minGroupNum) < 1 || Number(formState.value.minGroupNum) > Number(groupNumMax.value)) {
        callback(new Error(t('成团最低人数应在1~{value}成团人数之间的整数', { value: groupNumMax.value })));
    }
    callback();
};

const rules = reactive({
    groupNums: [{ required: true, validator: validateGroupNum, trigger: "blur" }],
    orderLimitNum: [{ required: isLimit, validator: validateOrderLimitNum, trigger: "blur" }]
});

const getFormData = () => {
    formState.value.orderLimitNum ??= 0;
    formState.value.isAutoGroup ??= 0;
    formState.value.minGroupNum ??= 0;
    formState.value.groupNums = ladderData.value;
    // 拿到最低成团价格
    const allPrices: number[] = [];
    formState.value.grouponPromotionDetails.map((item) => {
        item.groupPromotionDetailPrices.map((priceItem) => {
            // 如果没有价格,设置为商品原价
            if (!priceItem.price) {
                priceItem.price = item.productPrice;
            }
            if (priceItem.price) {
                allPrices.push(Number(priceItem.price));
            }
        });
    });
    if (allPrices.length) {
        formState.value.minGroupPrice = Math.min(...allPrices);
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        // 组合提交的数据
        getFormData();
        const result = await updateGrouponItem(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.container {
    .num-box {
        display: flex;
    }

    .inputBox-margin {
        margin-right: 10px;
    }

    .desc {
        font-size: 12px;
        color: #9598a6;
        line-height: 18px;
    }
}

.promotion-table {
    width: 600px;
    overflow-x: auto;

    :deep(.el-form-item__error) {
        position: relative !important;
        top: 0 !important;
        left: 0 !important;
    }
}

.groupNums {
    :deep(.el-form-item__content) {
        display: block;

        .el-form-item__error {
            position: relative !important;
            top: 0 !important;
            left: 0 !important;
        }
    }
}
</style>
