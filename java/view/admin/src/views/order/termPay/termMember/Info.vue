/**
 * @file 添加/修改授信会员（抽屉）
 * @description 对接接口：GET /credit/user/detail，POST /credit/user/create，POST /credit/user/update
 */
<template>
    <div class="drawer-container">
        <el-form ref="formRef" :model="form" label-width="100px">
            <template v-if="act === 'add'">
                <el-form-item :label="$t('会员类型')" required>
                    <RadioType
                        v-model:modelValue="form.memberType"
                        :radioList="memberTypeRenderList"
                        width="180px"
                    />
                    <div class="form-tip">
                        {{ form.memberType === 2 ? $t('“企业“在营业执照上的主体类型一般为：有限公司、有限责任公司等。') : $t('“个人”为根据法律法规和相关规定免于办理工商登记，无营业执照的商家。') }}
                    </div>
                </el-form-item>
            </template>
            <template v-else>
                <el-form-item :label="$t('会员类型')" required>
                    <RadioType
                        v-model:modelValue="form.memberType"
                        :radioList="memberTypeDisplayList"
                        width="180px"
                    />
                    <div class="form-tip">
                        {{ form.memberType === 2 ? $t('“企业“在营业执照上的主体类型一般为：有限公司、有限责任公司等。') : $t('“个人”为根据法律法规和相关规定免于办理工商登记，无营业执照的商家。') }}
                    </div>
                </el-form-item>
            </template>

            <el-form-item :label="$t('会员')" required>
                <div style="width: 100%">
                    <el-select
                        v-if="act === 'add'"
                        v-model="form.user"
                        filterable
                        remote
                        reserve-keyword
                        clearable
                        :remote-method="remoteMethod"
                        :loading="loadingUser"
                        :placeholder="$t('输入会员名称/电话进行搜索')"
                        style="width: 100%"
                        value-key="userId"
                        :no-data-text="$t('未搜索到用户')"
                        @clear="clearUser"
                        @change="changeUser"
                    >
                        <el-option
                            v-for="u in userList"
                            :key="u.userId"
                            :label="u.username"
                            :value="u"
                        />
                    </el-select>
                    <el-input v-else v-model="detailDisplay" disabled />
                    <div class="form-tip"> {{ $t('注：会员需关联已存在的客户且完成实名认证；系统提示会员不存在时，请先完成实名认证，认证通过后再次查询。') }} </div>
                </div>
            </el-form-item>

            <el-form-item :label="$t('信用额度')" required>
                <TigInput
                    v-model="form.creditLimit"
                    classType="tig-form-input"
                    type="decimal"
                    :min="0"
                    :decimalPlaces="2"
                    :fixedDecimal="true"
                    :allowNegative="false"
                    style="width: 100%"
                    :disabled="act === 'edit' && form.status === 0"
                />
            </el-form-item>

            <el-form-item :label="$t('结算周期')" required>
                <el-radio-group v-model="form.settlementType" :disabled="act === 'edit' && form.status === 0">
                    <el-radio :value="1">{{ $t('月结') }}</el-radio>
                    <el-radio :value="2">{{ $t('季结') }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('状态')">
                <div>
                    <div
                        @click="toggleStatus"
                        :class="'table-switch table-switch-' + (form.status === 1 ? 'on' : 'off') + (act === 'detail' ? ' disabled' : '')"
                    >
                        <i></i>
                    </div>
                    <div class="form-tip"> {{ $t('授信客户禁用后，将无法使用账期支付功能') }} </div>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed, onMounted, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import RadioType from "@/components/radio/src/RadioType.vue";
import { getUserCompanyList } from "@/api/user/userCertification";
import type { UserCompanyFilterState } from "@/types/user/userCertification.d";
import { createCreditUser, getCreditUserDetail, updateCreditUser } from "@/api/termPay/creditUser";
import type { CreditUserDetail } from "@/types/termPay/creditUser.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    act?: "add" | "edit" | "detail";
    creditUserId?: number;
    isDialog?: boolean;
    confirmLoading?: boolean;
}>();

const emit = defineEmits(["submitCallback", "okType", "update:confirmLoading"]);

const act = computed(() => props.act || "add");

const formRef = ref();

const memberTypeList = [
    { key: 1, title: t('个人') },
    { key: 2, title: t('企业') }
];

// 新增：选中会员后锁定会员类型；清空会员后恢复可切换
const memberTypeDisabled = computed(() => act.value === "add" && !!form.userId);
const memberTypeRenderList = computed(() => {
    // 注意：不要把 disabled 传给 RadioType（其历史逻辑会隐藏未选中的项）
    // 这里通过 item.disabled 禁用点击，同时仍展示全部选项
    return memberTypeList.map((i) => ({
        ...i,
        disabled: memberTypeDisabled.value ? i.key !== form.memberType : false
    }));
});
const memberTypeDisplayList = computed(() => memberTypeList.filter((i) => i.key === form.memberType));

const form = reactive({
    creditUserId: 0,
    /** 仅新增时展示（接口 create 不需要该字段，仅用于筛选可选会员） */
    memberType: 1 as 1 | 2,
    userId: undefined as undefined | number,
    user: undefined as undefined | UserCompanyFilterState,
    creditLimit: "0",
    settlementType: 1 as 1 | 2,
    status: 1 as 0 | 1
});

const detail = ref<CreditUserDetail | null>(null);
const detailDisplay = computed(() => {
    if (!detail.value) return "";
    const d = detail.value;
    return `${d.username}（${d.mobile || "-"}）`;
});

// 选择会员（已认证会员列表）
const loadingUser = ref(false);
const userList = ref<UserCompanyFilterState[]>([]);

const getUserType = (u: UserCompanyFilterState): 1 | 2 => {
    // userCompany/list 的返回在部分版本里没有 type 字段（类型声明也未包含），这里做兼容推断
    const anyU: any = u as any;
    if (anyU.type === 1 || anyU.type === 2) return anyU.type;
    // 有企业名称则按企业，否则按个人
    return u.companyData?.companyName ? 2 : 1;
};

const remoteMethod = async (keyword: string) => {
    loadingUser.value = true;
    try {
        const res = await getUserCompanyList({
            page: 1,
            size: 20,
            // 对齐接口文档：keyword + type + status=2
            keyword: keyword || "",
            type: form.memberType,
            // 这里用“已通过认证”的状态值（如果后端枚举不同，可根据实际调整）
            status: 2
        });
        const records = res.records || [];
        // 已通过接口参数 type 做了筛选，这里再做一次兼容过滤（防止后端忽略 type）
        userList.value = records.filter((r) => (form.memberType ? getUserType(r) === form.memberType : true));
    } finally {
        loadingUser.value = false;
    }
};

const getList = () => remoteMethod("");

const changeUser = (user: UserCompanyFilterState) => {
    form.user = user;
    form.userId = user?.userId;
    // 选择会员后，根据会员自身类型联动切换“会员类型”，并在 UI 上锁定切换
    if (user) {
        form.memberType = getUserType(user);
    }
};

const clearUser = () => {
    form.user = undefined;
    form.userId = undefined;
    userList.value = [];
    // 清空后重新拉取当前会员类型下的可选会员
    getList();
};

const loadDetail = async () => {
    if (!props.creditUserId) return;
    const res = await getCreditUserDetail({ creditUserId: props.creditUserId });
    detail.value = res;
    form.creditUserId = res.creditUserId;
    form.userId = res.userId;
    form.creditLimit = String(res.creditLimit ?? 0);
    form.settlementType = res.settlementType;
    form.status = res.status;
    form.memberType = res.type;
};

const onFormSubmit = async () => {
    emit("update:confirmLoading", true);
    try {
        if (act.value === "add") {
            if (!form.userId) {
                message.warning(t('请选择会员'));
                return;
            }
            if (!form.creditLimit || Number(form.creditLimit) <= 0) {
                message.warning(t('信用额度必须大于0'));
                return;
            }
            await createCreditUser({
                userId: form.userId,
                creditLimit: Number(form.creditLimit),
                settlementType: form.settlementType,
                status: form.status
            });
        } else if (act.value === "edit") {
            if (!form.creditUserId) {
                message.warning(t('缺少授信会员ID'));
                return;
            }
            await updateCreditUser({
                creditUserId: form.creditUserId,
                creditLimit: Number(form.creditLimit),
                settlementType: form.settlementType,
                status: form.status
            });
        }
        emit("submitCallback", true, true);
    } catch (error: any) {
        message.error(error.message);
    } finally  {
        emit("update:confirmLoading", false);
    }
};

const toggleStatus = () => {
    if (act.value === "detail") return;
    form.status = form.status === 1 ? 0 : 1;
};

defineExpose({ onFormSubmit });

// 新增：新增场景下，切换会员类型时刷新可选会员（仅在未选中会员时允许切换）
watch(
    () => form.memberType,
    async () => {
        if (act.value !== "add") return;
        if (form.userId) return; // 已选择会员则锁定，不做刷新
        await remoteMethod("");
    }
);

onMounted(async () => {
    if (act.value === "edit" || act.value === "detail") {
        await loadDetail();
    } else {
        // 初始化拉一批默认可选会员
        await remoteMethod("");
    }
});
</script>

<style scoped lang="less">
.table-switch.disabled {
    cursor: not-allowed;
    opacity: 0.6;
}
.form-tip {
    color: #999;
    font-size: 12px;
    margin-top: 6px;
    line-height: 18px;
}
</style>
