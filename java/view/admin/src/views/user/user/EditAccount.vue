/** 客户账户调节 - 调节客户的余额、冻结资金、成长积分、消费积分 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('当前客户')">
                        <p>{{ filterState.username }}</p>
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: '帐户变动原因不能为空!' }]" :label="$t('帐户变动原因')" prop="changeDesc">
                        <TigInput classType="tig-form-input" type="textarea" v-model="formState.changeDesc" />
                    </el-form-item>

                    <el-form-item :label="$t('可用资金帐户')" prop="balance" v-if="type < 3">
                        <div class="flex">
                            <el-select v-model="formState.typeBalance" style="width: 90px; margin-right: 10px">
                                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                            </el-select>
                            <TigInput type="decimal" v-model="formState.balance" style="width: 100px; margin-right: 10px" :min="0" :step="0.01" />
                            <p>{{ $t("当前值：¥{balance}", { balance: filterState.balance }) }}</p>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('冻结资金帐户')" prop="frozenBalance" v-if="type < 3">
                        <div class="flex">
                            <el-select v-model="formState.typeFrozenBalance" style="width: 90px; margin-right: 10px">
                                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                            </el-select>
                            <TigInput type="decimal" v-model="formState.frozenBalance" style="width: 100px; margin-right: 10px" :min="0" :step="0.01" />
                            <p>{{ $t("当前值：¥{frozenBalance}", { frozenBalance: filterState.frozenBalance }) }}</p>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('成长积分帐户')" prop="growthPoints" v-if="type > 2">
                        <div class="flex">
                            <el-select v-model="formState.typeGrowthPoints" style="width: 90px; margin-right: 10px">
                                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                            </el-select>
                            <TigInput type="integer" v-model="formState.growthPoints" style="width: 100px; margin-right: 10px" :min="0" :step="0.01" />
                            <p>{{ $t("当前值：{growthPoints}", { growthPoints: filterState.growthPoints }) }}</p>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('消费积分帐户')" prop="points" v-if="type > 2">
                        <div class="flex">
                            <el-select v-model="formState.typePoints" style="width: 90px; margin-right: 10px">
                                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                            </el-select>
                            <TigInput type="integer" v-model="formState.points" style="width: 100px; margin-right: 10px" :min="0" :step="0.01" />
                            <p>{{ $t("当前值：{points}", { points: filterState.points }) }}</p>
                        </div>
                    </el-form-item>

                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t("提交") }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { UserFundManagementFormState, UserFormState } from "@/types/user/user.d";
import { getUser, updateUserFundManagement } from "@/api/user/user";
import { SelectRankList } from "@/components/select";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    form: {
        type: Object,
        default: {}
    },
    type: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "insert" : "update";
const formRef = shallowRef();
/** 表单数据 */
const formState = ref<UserFundManagementFormState>({
    changeDesc: "",
    typeBalance: 1,
    balance: 0,
    typeFrozenBalance: 1,
    frozenBalance: 0,
    typePoints: 1,
    points: 0,
    typeGrowthPoints: 1,
    growthPoints: 0
});
/** 当前客户信息 */
const filterState = ref<any>({});
/** 操作类型选项 */
const options = [
    {
        value: 1,
        label: t("增加")
    },
    {
        value: 2,
        label: t("减少")
    }
];

/** 获取客户详情 */
const fetchUser = async () => {
    try {
        loading.value = true;
        const result = await getUser(action.value, { id: id.value });
        Object.assign(filterState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    fetchUser();
    loading.value = false;
});

/** 表单提交 - 调节账户 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateUserFundManagement({ id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

/** 对外暴露的表单提交方法 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style scoped>
.content_wrapper :deep(.el-select) {
    min-width: none;
}
</style>
