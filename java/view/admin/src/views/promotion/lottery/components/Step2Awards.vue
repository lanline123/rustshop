<template>
    <div class="step2-container">
        <!-- 奖品池设置 -->
        <div class="section-block">
            <div class="formTitle">{{ $t('奖品设置') }}</div>
            <div class="operation-bar">
                <DialogForm
                    ref="addAwardDialogRef"
                    isDrawer
                    @okCallback="onAwardSaved"
                    :title="$t('添加奖品')"
                    width="800px"
                    path="promotion/lottery/components/AwardForm"
                    :params="{ awardData: null, editIndex: -1 }"
                >
                    <el-button type="text" :disabled="formState.prizeList.length >= 7">{{ $t('添加奖品（{length}/7）', { length: formState.prizeList.length }) }}</el-button>
                </DialogForm>
            </div>
            <el-table :data="formState.prizeList" border style="width: 100%; margin-top: 15px;">
                <el-table-column :label="$t('奖品名称')" prop="prizeName" />
                <el-table-column :label="$t('奖品类型')" prop="prizeType">
                    <template #default="{ row }">
                        {{ getTypeName(row.prizeType) }}
                    </template>
                </el-table-column>
                <el-table-column :label="$t('奖品库存')" prop="prizeStock" />
                <el-table-column :label="$t('已发放')" prop="prizeStockUsed" />
                <el-table-column :label="$t('图片')">
                    <template #default="{ row }">
                        <img :src="row.prizeImage" style="width: 40px; height: 40px;" />
                    </template>
                </el-table-column>
                <el-table-column :label="$t('操作')">
                    <template #default="{ row, $index }">
                        <DialogForm
                            isDrawer
                            @okCallback="onAwardSaved"
                            :title="$t('编辑奖品')"
                            width="800px"
                            path="promotion/lottery/components/AwardForm"
                            :params="{ awardData: row, editIndex: $index }"
                        >
                            <el-button link type="primary">{{ $t('编辑') }}</el-button>
                        </DialogForm>
                        <el-divider direction="vertical" />
                        <el-button link type="danger" @click="removeAward($index)">{{ $t('移除') }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 未中奖设置 -->
        <div class="section-block">
            <div class="formTitle">{{ $t('未中奖设置') }} <el-tooltip effect="light" placement="bottom" :show-after="150">
                    <template #content>
                        <div style="width: 200px; text-align: left">{{ $t('当用户未中奖时显示内容配置项，推荐配置提示跳转可引导用户未抽中奖时可跳转至其他页面') }}</div>
                    </template>
                    <el-icon color="#999999" size="16" style="margin-left: 5px"><QuestionFilled /></el-icon>
                </el-tooltip>
            </div>
            <el-form ref="noWinFormRef" :model="formState" :rules="noWinRules" label-width="120px" style="margin-top: 15px;">
                <el-form-item :label="$t('未中奖名称')" prop="noWinName" required>
                    <BusinessData v-model:modelValue="formState.noWinName" :placeholder="$t('谢谢参与')" :dataType="12"></BusinessData>
                </el-form-item>
                <el-form-item :label="$t('图片')" prop="noWinImage" required>
                    <div class="flex-column">
                        <div style="display: flex; align-items: flex-end;">
                            <FormAddGallery v-model:photo="formState.noWinImage" :isLocal="true"></FormAddGallery>
                            <el-button link type="primary" @click="restoreDefaultMissImage" style="margin-left: 10px;">{{ $t('恢复默认图') }}</el-button>
                        </div>
                        <div class="tip">{{ $t('建议尺寸100*100px，支持jpg、png、jpeg格式') }}</div>
                    </div>

                </el-form-item>
                <el-form-item :label="$t('未中奖提示语')" prop="noWinTips" required>
                    <BusinessData v-model:modelValue="formState.noWinTips" :placeholder="$t('很遗憾没有抽中')" :dataType="12"></BusinessData>
                </el-form-item>
            </el-form>
        </div>

        <!-- 转盘设置 -->
        <div class="section-block">
            <div class="formTitle">{{ $t('转盘设置') }}</div>
            <div class="wheel-setting-wrapper">
                <div class="wheel-table-container">
                    <el-table :data="formState.gridConfig" border>
                        <el-table-column :label="$t('转盘位置')" prop="position" width="100" align="center" />
                        <el-table-column :label="$t('奖品名称')">
                            <template #default="{ row }">
                                <!-- 第8个位置固定为谢谢参与 -->
                                <template v-if="row.position === 8">
                                    <span class="fixed-text">{{ formState.noWinName || $t('谢谢参与') }}</span>
                                </template>
                                <template v-else>
                                    <el-select
                                        :model-value="getGridSelectValue(row)"
                                        @update:modelValue="onTempIndexUpdate(row, $event)"
                                        :placeholder="$t('请选择奖品')"
                                        style="width: 100%"
                                        :teleported="true"
                                    >
                                        <el-option :value="NO_WIN_VALUE" :label="formState.noWinName || $t('未中奖')"></el-option>
                                        <el-option
                                            v-for="prize in formState.prizeList"
                                            :key="prize.tempIndex"
                                            :label="prize.prizeName"
                                            :value="prize.tempIndex"
                                        ></el-option>
                                    </el-select>
                                </template>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('奖品图片')" align="center" width="100">
                            <template #default="{ row }">
                                <img :src="getSectorImage(row)" style="width: 30px; height: 30px;" />
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('概率')" width="140">
                            <template #default="{ row }">
                                <TigInput
                                    v-model="row.probability"
                                    classType="tig-form-input"
                                    type="decimal"
                                    :decimalPlaces="4"
                                    :fixedDecimal="false"
                                    :disabled="row.position === 8"
                                    :min="getMinProbability(row)"
                                    :max="getMaxProbability(row)"
                                    @focus="onProbabilityFocus(row)"
                                    @input="onProbabilityInput(row)"
                                    @blur="onProbabilityBlur(row)"
                                    :placeholder="$t('请输入概率')"
                                    width="120px"
                                    class="probability-input"
                                >
                                    <template #append>%</template>
                                </TigInput>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
                <!-- 九宫格位置示意图 -->
                <div class="grid-preview">
                    <div class="grid-container">
                        <div
                            v-for="pos in gridPositions"
                            :key="pos.index"
                            class="grid-cell"
                            :class="{ 'is-center': pos.isCenter }"
                        >
                            <span v-if="!pos.isCenter">{{ pos.number }}</span>
                        </div>
                    </div>
                    <div class="grid-title">{{ $t('转盘位置示意图') }}</div>
                </div>
            </div>
        </div>

    </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { LotteryFormState, PrizeItem } from '@/types/promotion/luckyWheel.d';
import { FormAddGallery } from "@/components/gallery";
import { DialogForm } from "@/components/dialog";
import { QuestionFilled } from '@element-plus/icons-vue';
import { message } from "ant-design-vue";
import type { FormInstance, FormRules } from 'element-plus';
import BusinessData from "@/components/multilingual/BusinessData.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formState = defineModel<LotteryFormState>({ required: true });

const noWinFormRef = ref<FormInstance>();
const noWinRules = reactive<FormRules>({
    noWinName: [{ required: true, message: t('请输入未中奖名称'), trigger: 'blur' }],
    noWinImage: [{
        required: true,
        validator: (rule, value, callback) => {
            const img = formState.value.noWinImage as any;
            if (!img || (typeof img === 'string' && img.trim().length === 0)) {
                callback(new Error(t('请上传未中奖图片')));
                return;
            }
            callback();
        },
        trigger: 'change'
    }],
    noWinTips: [{ required: true, message: t('请输入未中奖提示语'), trigger: 'blur' }],
});

// ElementPlus 的 el-select 会把 null 当成"未选择"，导致下拉高亮但输入框不回显
// 用一个 UI 哨兵值来代表"谢谢参与"，写回 model 时再映射回 null
const NO_WIN_VALUE = -1;
// 4 位小数下，“必须大于 0” 能输入的最小正数
const MIN_POSITIVE_PROBABILITY = 0.0001;

// 生成唯一的临时索引（用于新增奖品）
// 初始化时取 prizeList 中最大的 tempIndex + 1，避免冲突
const getMaxTempIndex = () => {
    const maxIndex = formState.value.prizeList.reduce((max, item) => Math.max(max, item.tempIndex || 0), 0);
    return maxIndex;
};
let tempIndexCounter = 0;
const generateTempIndex = () => {
    if (tempIndexCounter === 0) {
        tempIndexCounter = getMaxTempIndex();
    }
    return ++tempIndexCounter;
};

// 获取九宫格位置的奖品关联值（用于 el-select）
const getGridSelectValue = (row: any): number => {
    return row.tempIndex ?? NO_WIN_VALUE;
};

// 更新九宫格位置的奖品关联
const onTempIndexUpdate = (row: any, val: number) => {
    row.tempIndex = val === NO_WIN_VALUE ? null : val;
};

// 是否“选择了奖品”（不是未中奖/谢谢参与）
const isSelectedPrize = (row: any) => row?.position !== 8 && row?.tempIndex != null;

// 选择了奖品：概率必须 > 0；未选择奖品：允许为 0
const getMinProbability = (row: any) => (isSelectedPrize(row) ? MIN_POSITIVE_PROBABILITY : 0);

const getTypeName = (type: number) => {
    const map: Record<number, string> = { 1: t('积分'), 2: t('商品'), 3: t('优惠券') };
    return map[type] || String(type);
};

// 奖品保存回调（DialogForm的okCallback）- 前端临时存储
const onAwardSaved = (result: { award: PrizeItem; editIndex: number }) => {
    const { award, editIndex } = result;

    if (editIndex === -1) {
        // 新增：分配 tempIndex（用于与九宫格关联）
        if (!award.tempIndex) {
            award.tempIndex = generateTempIndex();
        }
        formState.value.prizeList.push(award);
    } else {
        // 编辑：保留原有的 tempIndex
        const oldPrize = formState.value.prizeList[editIndex];
        if (!award.tempIndex && oldPrize.tempIndex) {
            award.tempIndex = oldPrize.tempIndex;
        }
        formState.value.prizeList[editIndex] = award;
    }
};

const removeAward = (index: number) => {
    const prize = formState.value.prizeList[index];

    formState.value.prizeList.splice(index, 1);
    // Remove from grid if selected
    formState.value.gridConfig.forEach(grid => {
        if (prize.tempIndex != null && grid.tempIndex === prize.tempIndex) {
            grid.tempIndex = null;
        }
    });
};

const restoreDefaultMissImage = () => {
    formState.value.noWinImage = 'https://oss.tigshop.com/img/gallery/202512/1766467150H93CwsgU07PQfSqwj4.jpeg';
    noWinFormRef.value?.clearValidate?.(['noWinImage']);
};

// 获取实际使用的未中奖图片（如果没有设置则使用默认图片）
const actualNoWinImage = computed(() => formState.value.noWinImage || 'https://oss.tigshop.com/img/gallery/202512/1766467150H93CwsgU07PQfSqwj4.jpeg');

const getSectorImage = (grid: any) => {
    // 没有关联奖品时显示未中奖图片
    if (grid?.tempIndex == null) {
        return actualNoWinImage.value;
    }
    // 通过 tempIndex 查找关联的奖品
    const prize = formState.value.prizeList.find(item => item.tempIndex === grid.tempIndex);
    return prize ? prize.prizeImage : actualNoWinImage.value;
};

// 计算概率总和
const totalProbability = computed(() => {
    return formState.value.gridConfig.reduce((sum, item) => sum + (Number(item.probability) || 0), 0);
});

// 概率精度处理：按 4 位小数转成整数相加，避免浮点误差（如 100.0000000002）
const PROB_SCALE = 10000;
const toProbInt = (val: any) => {
    const num = Number(val);
    if (!Number.isFinite(num)) return 0;
    return Math.round(num * PROB_SCALE);
};
const getTotalProbabilityInt = () => {
    return formState.value.gridConfig.reduce((sum, item: any) => sum + toProbInt(item.probability), 0);
};

// 概率输入超上限提示：
// - TigInput 默认在 blur 时才会按 max 裁剪，所以需要在 input 时记录“用户输入的原始值”
const probabilityTypedValue = new Map<number, number>();
const onProbabilityFocus = (row: any) => {
    const key = Number(row?.position);
    if (!Number.isFinite(key)) return;
    probabilityTypedValue.set(key, Number(row?.probability));
};
const onProbabilityInput = (row: any) => {
    const key = Number(row?.position);
    if (!Number.isFinite(key)) return;
    probabilityTypedValue.set(key, Number(row?.probability));
};
const onProbabilityBlur = (row: any) => {
    const key = Number(row?.position);
    if (!Number.isFinite(key)) return;
    const typed = probabilityTypedValue.get(key);
    probabilityTypedValue.delete(key);
    if (typed === undefined || !Number.isFinite(typed)) return;
    // 第 8 个位置禁用输入，不提示
    if (Number(row?.position) === 8) return;
    const maxAllowed = Number(getMaxProbability(row));
    const after = Number(row?.probability);
    if (!Number.isFinite(maxAllowed) || !Number.isFinite(after)) return;
    // 用户输入值大于上限，且最终值被裁剪到 <= 上限，则提示
    if (typed > maxAllowed + 1e-6 && after <= maxAllowed + 1e-6) {
        message.warning(t('检测到输入概率超出上限，已自动调整为允许的最大值。'));
    }
};

// 概率展示：最多 4 位小数，但不强制补 0（例如 12.5000 -> 12.5，2.0000 -> 2）
const normalizeProbability = (val: any) => {
    const num = Number(val);
    if (!Number.isFinite(num)) return "0";
    return String(parseFloat(num.toFixed(4)));
};
const normalizeAllProbabilities = () => {
    formState.value.gridConfig.forEach((row: any) => {
        row.probability = normalizeProbability(row.probability);
    });
};
// 只在 gridConfig 引用变化/首次进入时做一次归一化，避免影响用户输入过程
watch(
    () => formState.value.gridConfig,
    () => normalizeAllProbabilities(),
    { immediate: true }
);

// 当 8 个概率不足 100% 时：剩余概率自动补到第 8 个（谢谢参与）
let syncingNoWinProbability = false;
const syncNoWinProbability = () => {
    if (syncingNoWinProbability) return;
    const noWinRow: any = formState.value.gridConfig.find((r: any) => r.position === 8);
    if (!noWinRow) return;

    const otherTotal = formState.value.gridConfig
        .filter((r: any) => r.position !== 8)
        .reduce((sum: number, r: any) => sum + (Number(r.probability) || 0), 0);

    const remain = Math.max(0, 100 - otherTotal);
    const next = normalizeProbability(remain);
    if (String(noWinRow.probability) === next) return;

    syncingNoWinProbability = true;
    noWinRow.probability = next;
    syncingNoWinProbability = false;
};

watch(
    () => formState.value.gridConfig
        .filter((r: any) => r.position !== 8)
        .map((r: any) => r.probability),
    () => syncNoWinProbability(),
    { immediate: true }
);

// 获取当前行可设置的最大概率（确保总和不超过100%）
const getMaxProbability = (row: any) => {
    const otherTotal = formState.value.gridConfig
        .filter(item => item.position !== row.position)
        .reduce((sum, item) => sum + (Number(item.probability) || 0), 0);
    return Math.max(0, 100 - otherTotal);
};

// 九宫格位置映射：顺时针 1-8，中间为空
// 布局: 1 2 3
//       8 X 4
//       7 6 5
const gridPositions = computed(() => {
    const positions = [
        { index: 0, number: 1, isCenter: false },
        { index: 1, number: 2, isCenter: false },
        { index: 2, number: 3, isCenter: false },
        { index: 3, number: 8, isCenter: false },
        { index: 4, number: null, isCenter: true },
        { index: 5, number: 4, isCenter: false },
        { index: 6, number: 7, isCenter: false },
        { index: 7, number: 6, isCenter: false },
        { index: 8, number: 5, isCenter: false },
    ];
    return positions;
});

const validate = async () => {
    // 检查是否至少添加了一个奖品
    if (formState.value.prizeList.length === 0) {
        message.error(t('请至少添加一个奖品'));
        return false;
    }

    // 检查转盘是否至少选中了一个奖品（排除第8个位置，固定为谢谢参与）
    const hasSelectedPrize = formState.value.gridConfig.some(
        grid => grid.position !== 8 && grid.tempIndex != null
    );
    if (!hasSelectedPrize) {
        message.error(t('转盘至少需要选中一个奖品'));
        return false;
    }

    // 选择了奖品的概率必须大于 0（排除第8个位置，固定为谢谢参与）
    const invalidPrizeProbability = formState.value.gridConfig.find(grid => {
        if (grid.position === 8) return false;
        if (grid.tempIndex == null) return false; // 未中奖/谢谢参与不强制 >0
        const p = Number(grid.probability);
        return !Number.isFinite(p) || p <= 0;
    });
    if (invalidPrizeProbability) {
        message.error(t('转盘位置{position}已选择奖品，概率必须大于0', { position: invalidPrizeProbability.position }));
        return false;
    }

    const validNoWin = await noWinFormRef.value?.validate?.().catch(() => false);
    if (validNoWin === false) {
        return false;
    }
    // Check total probability
    if (getTotalProbabilityInt() > 100 * PROB_SCALE) {
        message.error(t('概率总和不能超过100%，请调整'));
        return false;
    }
    return true;
};

defineExpose({ validate });

watch(
    () => formState.value.noWinImage,
    () => {
        // 选图/清空后及时刷新提示
        noWinFormRef.value?.validateField?.('noWinImage').catch(() => void 0);
    }
);
</script>

<style scoped lang="less">
:deep(.gallery-pic) {
    width: auto;
}
.section-block {
    margin-bottom: 30px;
}
.operation-bar {
    margin-bottom: 10px;
}
.tip {
    font-size: 12px;
    color: #999;
}

// 转盘设置布局
.wheel-setting-wrapper {
    display: flex;
    gap: 20px;
    align-items: flex-start;
}

.wheel-table-container {
    flex: 1;
}

.fixed-text {
    color: #000;
    font-weight: 500;
}

// 九宫格示意图
.grid-preview {
    width: 180px;
    flex-shrink: 0;
}

.grid-title {
    font-size: 14px;
    color: #606266;
    margin-bottom: 12px;
    text-align: center;
}

.grid-container {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
    padding: 10px;
    border-radius: 8px;
}

.grid-cell {
    width: 48px;
    height: 48px;
    background: #f5f6fa;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 600;
    color: #a5adb3;

    &.is-center {
        background: transparent;
    }
}

:deep(.probability-input){
    .el-input-group__append{
        padding: 0 15px;
    }
}
</style>
