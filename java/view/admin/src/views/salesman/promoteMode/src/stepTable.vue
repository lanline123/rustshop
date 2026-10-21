<template>
    <div class="lyecs-form-table">
        <el-form ref="formRef" :rules="rules" :model="form" label-width="auto">
            <el-form-item prop="typeData">
                <div class="flex mb10">
                    <div class="flex">
                        <el-button type="primary" :icon="Plus" link @click="addPreferential" :disabled="form.typeData.length >= 10">
                            {{ form.typeData.length >= 10 ? $t("最多添加10级") : `${t("添加等级")}(${form.typeData.length}/10)` }}
                        </el-button>
                    </div>
                </div>
            </el-form-item>
            <el-form-item prop="typeData">
                <el-table :data="form.typeData" style="width: 100%" border>
                    <el-table-column :label="$t('序号')" width="80">
                        <template #default="{ $index }">
                            {{ $index + 1 }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('等级名')" prop="name" width="130">
                        <template #default="{ row, $index }">
                            <el-form-item :prop="'typeData.' + $index + '.name'" :rules="rules.name">
                                <div class="flex flex-align-center">
                                    <TigInput v-model="row.name" width="100px" :placeholder="$t('请输入等级名')"></TigInput>
                                </div>
                            </el-form-item>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('升级条件')" prop="condition" min-width="240">
                        <template #default="{ row, $index }">
                            <div class="condition">
                                <el-form-item :prop="'typeData.' + $index + '.condition'" :rules="rules.condition">
                                    <div class="flex flex-align-center">
                                        <div class="condition-text">
                                            <div v-if="$index === 0">
                                                <p>{{ $t("初始等级，无需设置") }}</p>
                                            </div>
                                            <div v-if="$index > 0 && row.condition">
                                                <p v-if="row.condition.selfBuyAmount?.checked && row.condition.selfBuyAmount?.value > 0">
                                                    {{ $t('自购金额满') }}{{ row.condition.selfBuyAmount?.value }}{{ config.get("amountUnit") }}
                                                </p>
                                                <p v-if="row.condition.salesAmount?.checked && row.condition.salesAmount?.value > 0">
                                                    {{ $t('推广金额满') }}{{ row.condition.salesAmount?.value }}{{ config.get("amountUnit") }}
                                                </p>
                                                <p v-if="row.condition.salesInviteUsers?.checked && row.condition.salesInviteUsers?.value > 0">
                                                    {{ $t("发展客户数满{value}人", { value: row.condition.salesInviteUsers?.value }) }}
                                                </p>
                                                <!-- <p v-if="row.condition.inviteSales?.checked">邀请分销员满{{ row.condition.inviteSales?.value }}人</p> -->
                                            </div>
                                            <div v-if="$index > 0 && !row.condition">
                                                <p class="gray">{{ $t("未设置") }}</p>
                                            </div>
                                        </div>
                                        <popover
                                            class="edit-popover"
                                            :index="$index"
                                            :conditionsData="row.condition"
                                            :superiorData="$index > 1 ? form.typeData[$index - 1].condition : null"
                                            @submitCallback="popoverCallback"
                                        >
                                            <el-icon v-if="$index > 0" class="edit-condition-icon" size="15" color="#333"><Edit /></el-icon>
                                            <span v-else></span>
                                        </popover>
                                    </div>
                                </el-form-item>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="rate" width="320">
                        <template #header>
                            <div class="flex flex-align-center">
                                <p>{{ $t("佣金比例") }}</p>
                                <el-tooltip placement="top-end" effect="light">
                                    <template #content>
                                        <div style="width: 450px">
                                            <template v-if="distributionMode === WECHAT_BIZ_MODE && distributionLevel === COMMISSION_EXPLAIN_LEVEL">
                                                <p>
                                                    <el-icon size="12" color="#155bd4"><InfoFilled /></el-icon>
                                                    {{ $t("修改后的比例只影响之后产生的新订单，不影响历史订单。") }}
                                                </p>
                                                <p>
                                                    {{
                                                        $t(
                                                            "商品佣金比例：分销员自行购买分销商品后，获得的商品佣金 = 商品佣金比例 × 商品实付金额（扣除运费、税费和附加费）。"
                                                        )
                                                    }}
                                                </p>
                                                <p>
                                                    {{
                                                        $t(
                                                            "下级分销员邀请奖励比例：下级分销员绑定后，自行购买分销商品时，上级分销员自动获得邀请奖励 = 下级分销员邀请奖励比例 × 商品实付金额（扣除运费、税费和附加费），无需上级每次再分享推广链接。"
                                                        )
                                                    }}
                                                </p>
                                                <p>
                                                    {{
                                                        $t(
                                                            "举例：A 发展了 B 成为下级分销员。B发展C为下级分销员，C 自行购买一件分销商品，实付 100 元（已扣运费等）。若 B 为铜牌（商品佣金 2%，邀请奖励 1%），则 B 获得商品佣金 2 元，A 获得邀请奖励 1 元。"
                                                        )
                                                    }}
                                                </p>
                                            </template>
                                            <template v-else>
                                                <p>
                                                    <el-icon size="12" color="#155bd4"><InfoFilled /></el-icon> {{ $t("修改后的比例只影响之后升级的佣金发放") }}
                                                </p>
                                                <template v-if="distributionLevel === COMMISSION_EXPLAIN_LEVEL">
                                                    <p>
                                                        {{
                                                            $t(
                                                                "商品佣金比例：分销员推广商品后获得的商品佣金=商品佣金比例*商品实付金额（扣除运费、税费和附加费）。"
                                                            )
                                                        }}
                                                    </p>
                                                    <p>
                                                        {{
                                                            $t(
                                                                "下级分销员邀请奖励比例：下级分销员推广后，上级分销员获得邀请奖励=下级分销员邀请奖励比例*商品实付金额（扣除运费、税费和附加费）。"
                                                            )
                                                        }}
                                                    </p>
                                                    <p>{{ $t("举例：B是A发展的分销员，B成功推广商品后，B可获得商品佣金，A可获得邀请奖励。") }}</p>
                                                </template>
                                                <p>
                                                    {{
                                                        $t(
                                                            "当分销员升至对应等级即会发放对应佣金，降级时按着当时等级发放，频繁调整分销员等级会导致佣金多次发放。"
                                                        )
                                                    }}
                                                </p>
                                            </template>
                                        </div>
                                    </template>
                                    <el-icon size="14" color="#969799"><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                        </template>
                        <template #default="{ row, $index }">
                            <div>
                                <el-form-item class="rate" :prop="'typeData.' + $index + '.rate'" :rules="rules.rate">
                                    <div class="rate-item">
                                        <div class="rate-label">{{ $t("商品佣金") }}</div>
                                        <div class="inp">
                                            <TigInput
                                                type="decimal"
                                                v-model="row.rate"
                                                style="width: 120px"
                                                :min="0"
                                                :fixed-decimal="false"
                                                :formatter="formatRate"
                                            >
                                                <template #append>%</template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </el-form-item>
                                <el-form-item
                                    v-if="distributionLevel === COMMISSION_EXPLAIN_LEVEL"
                                    class="down_salesman_rate"
                                    :prop="'typeData.' + $index + '.downSalesmanRate'"
                                    :rules="rules.downSalesmanRate"
                                >
                                    <div class="rate-item mt20">
                                        <div class="rate-label">{{ $t("下级分销员邀请奖励") }}</div>
                                        <div class="inp">
                                            <TigInput
                                                type="decimal"
                                                v-model="row.downSalesmanRate"
                                                style="width: 120px"
                                                :min="0"
                                                :fixed-decimal="false"
                                                :formatter="formatRate"
                                            >
                                                <template #append>%</template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </el-form-item>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('操作')" fixed="right" width="100" align="center">
                        <template #default="{ row, $index }">
                            <div class="btn" v-if="$index > 0 && form.typeData.length > 1">
                                <el-button type="primary" link @click="del($index)">{{ $t("删除") }}</el-button>
                            </div>
                            <div class="btn" v-else>-</div>
                        </template>
                    </el-table-column>
                </el-table>
            </el-form-item>
        </el-form>
    </div>
</template>
<script lang="ts" setup>
import { Plus, QuestionFilled, Edit, InfoFilled } from "@element-plus/icons-vue";
import { onMounted, reactive, watch, shallowRef } from "vue";
import type { FormRules } from "element-plus";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import popover from "./popover.vue";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const COMMISSION_EXPLAIN_LEVEL = 2 as const;
const WECHAT_BIZ_MODE = 2 as const;

const config = useConfigStore();
const props = defineProps({
    distributionLevel: {
        //分销等级 1: 一级分销 2: 二级分销
        type: Number,
        default: 1
    },
    distributionMode: {
        //分销模式 1: 推广员等级模式 2: 微商模式
        type: Number,
        default: 1
    },
    typeData: {
        type: Array,
        default: () => []
    }
});
const formRef = shallowRef();
export interface FormExpose {
    typeData: any[];
}
// 比例格式化：有小数保留两位，整数不补 .00
const formatRate = (value: any) => {
    if (value === "" || value === null || value === undefined) return value;
    const num = Number(value);
    if (!Number.isFinite(num)) return value;
    return Number.isInteger(num) ? String(num) : num.toFixed(2);
};
// 归一化数据中的比例字段，保证首次渲染即按规则展示
const normalizeTypeData = (list: any[]) => {
    if (!Array.isArray(list)) return [];
    list.forEach((item: any) => {
        if (item.rate !== undefined) item.rate = formatRate(item.rate);
        if (item.downSalesmanRate !== undefined) item.downSalesmanRate = formatRate(item.downSalesmanRate);
    });
    return list;
};
const form = reactive<FormExpose>({
    typeData: normalizeTypeData(Array.isArray(props.typeData) ? props.typeData : [])
});
const validateCondition = (rule: any, value: any, callback: any, rowIndex: number) => {
    let condition = (form.typeData[rowIndex] as any).condition;
    if (rowIndex > 0) {
        if (condition.salesAmount.checked && condition.salesAmount.value <= 0) {
            callback(new Error(t("请设置升级条件")));
            return;
        }
        if (condition.salesInviteUsers.checked && condition.salesInviteUsers.value <= 0) {
            callback(new Error(t("请设置升级条件")));
            return;
        }
        if (condition.selfBuyAmount.checked && condition.selfBuyAmount.value <= 0) {
            callback(new Error(t("请设置升级条件")));
            return;
        }
        if (!condition.salesAmount.checked && !condition.salesInviteUsers.checked && !condition.selfBuyAmount.checked) {
            callback(new Error(t("请设置升级条件")));
            return;
        }
        callback();
    } else {
        callback();
    }
};
interface RuleForm {
    name: string;
    condition: any;
    rate: number;
    downSalesmanRate: number;
}
const rules = reactive<FormRules<RuleForm>>({
    name: [
        { required: true, message: t("请输入等级名"), trigger: "blur" },
        {
            validator: (rule: any, value: string, callback: any) => {
                const currentIndex = form.typeData.findIndex((item) => item.name === value);
                const isDuplicate = form.typeData.some((item, index) => item.name === value && index !== currentIndex);
                if (isDuplicate) {
                    callback(new Error(t("等级名不能重复")));
                } else {
                    callback();
                }
            },
            trigger: "blur"
        }
    ],
    condition: [
        {
            required: true,
            validator: (rule: any, value, callback) => {
                const regex = /\.(\d+)\./;
                const field = String(rule?.field ?? "");
                const match: any = field.match(regex);
                const rowIndex = Number(match?.[1] ?? 0);
                validateCondition(rule, value, callback, rowIndex);
            },
            trigger: "blur"
        }
    ],
    rate: [{ required: true, message: t("请输入"), trigger: "blur" }],
    downSalesmanRate: [{ required: true, message: t("请输入"), trigger: "blur" }]
});
const addPreferential = async () => {
    await formRef.value.validate();
    if (form.typeData.length <= 10) {
        form.typeData.push({
            id: form.typeData.length + 1,
            name: "",
            rate: 0,
            downSalesmanRate: 0,
            condition: {
                selfBuyAmount: { checked: false, disabled: false, value: 0.0, title: t("自购金额"), unit: config.get("amountUnit") ?? t("元") },
                salesAmount: { checked: false, disabled: false, value: 0.0, title: t("推广金额"), unit: config.get("amountUnit") ?? t("元") },
                salesInviteUsers: { checked: false, disabled: false, value: 0, title: t("发展客户数"), unit: t("人") }
                // inviteSales: { checked: false, value: 0, title: "邀请分销员", unit: "人" }
            }
        });
        await formRef.value.validate();
    }
};
const del = (index: number) => {
    form.typeData.splice(index, 1);
};
const popoverCallback = (data: any) => {
    let index: number = data.index;
    form.typeData[index].condition = data.conditionsOpts;
};
onMounted(() => {
    if (form.typeData.length < 1) {
        addPreferential();
    }
});

watch(
    () => props.typeData,
    (val) => {
        form.typeData = normalizeTypeData(Array.isArray(val) ? val : []);
    }
);
defineExpose({ formRef, form });
</script>
<style lang="less" scoped>
.lyecs-form-table {
    width: 780px;
    margin-top: 15px;
    font-size: 16px;
    :deep(.el-table .cell) {
        overflow: visible !important;
    }
    :deep(.el-form-item__error) {
        position: absolute !important;
        z-index: 99;
        top: 32px !important;
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
@media (max-width: 1000px) {
    .lyecs-form-table {
        width: 100%;
    }
}
.rate-item {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 8px;

    .rate-label {
        width: 150px;
        flex: 0 0 auto;
        white-space: nowrap;
    }

    .inp {
        flex: 0 0 120px;
    }
}
.btn {
    text-align: center;
}
.condition {
    :deep(.el-form-item__error) {
        position: relative !important;
        top: 0 !important;
        left: 0 !important;
    }
    // 文字不允许换行；宽度由内容自适应，同时保留一个最小宽度避免"未设置"等短文案时编辑图标贴得太近
    .condition-text {
        min-width: 110px;
        white-space: nowrap;
    }
    p {
        margin: 0;
        line-height: 1.6;
        white-space: nowrap;
    }
}
.edit-condition-icon {
    margin-left: 12px;
    cursor: pointer;
}
// popover 的 reference 外层套了 el-form-item，去掉其下边距，使编辑按钮与升级条件文字垂直居中对齐
// 同时禁止被 flex 布局压缩、允许超出可见，防止金额过长时图标被隐藏或裁切
.edit-popover {
    flex-shrink: 0;
    overflow: visible;
    :deep(.el-form-item) {
        margin-bottom: 0;
    }
}
</style>
