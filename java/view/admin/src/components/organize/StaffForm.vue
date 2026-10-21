<template>
    <el-form ref="formRef" :model="localFormData" :rules="formRules" label-width="auto">
        <!-- 姓名 -->
        <el-form-item :label="staffType === 'deliveryman' ? $t('配送员名称') : $t('服务员名称')" prop="name">
            <el-input v-model="localFormData.name" :placeholder="$t('请输入姓名')" maxlength="10" show-word-limit />
            <div class="form-tip">{{ $t('请填写{staffTypeText}真实姓名，该名称会显示在客户订单详情中', { staffTypeText }) }}</div>
        </el-form-item>
        <!-- 会员账号 -->
        <el-form-item :label="$t('会员账号')" prop="memberId">
            <div class="user-select-wrapper">
                <SelectUser
                    v-model:userId="localFormData.memberId"
                    @change="handleSelectUserChange"
                    :username="localFormData.memberUsername"
                    :mobile="localFormData.memberMobile"
                    :multiple="false"
                    :show-tips="false"
                    :show-member-name-prefix="false"
                    style="width: 400px"
                />
                <DialogForm :params="{ act: 'add' }" isDrawer path="user/user/Info" :title="$t('添加会员')" width="560px" @okCallback="handleCreateMemberOk">
                    <el-button type="primary" link :icon="Plus">{{ $t('新建会员账号') }}</el-button>
                </DialogForm>
            </div>
            <div class="form-tip">
                <span style="margin-right: 4px">{{ $t('添加{staffTypeText}，需绑定商城账号，添加成功后该用户在商城个人中心显示{staffTypeText1}入口', { staffTypeText, staffTypeText1: staffTypeText }) }}</span>
                <el-popover class="box-item" title="" placement="bottom" width="350" trigger="hover">
                    <div class="example-content">
                        <div class="example-title">{{ $t('商城个人中心{staffTypeText}入口示例', { staffTypeText }) }}</div>
                        <div class="example-desc">
                            <p>{{ $t('当用户被添加为{staffTypeText}后，在商城个人中心会显示"{staffTypeText1}"入口，点击可进入{staffTypeText2}工作台。', { staffTypeText, staffTypeText1: staffTypeText, staffTypeText2: staffTypeText }) }}</p>
                            <!-- <p class="example-note">示例位置：商城个人中心 → {{ staffTypeText }}</p> -->
                        </div>
                        <div class="example-image">
                            <el-image
                                class="example-image__img"
                                :src="staffExampleImg"
                                :preview-src-list="[staffExampleImg]"
                                preview-teleported
                                fit="contain"
                            />
                        </div>
                    </div>
                    <template #reference>
                        <span class="btn-link">{{ $t('示例') }}</span>
                    </template>
                </el-popover>
            </div>
        </el-form-item>
        <!-- 所属门店 -->
        <!-- <el-form-item v-if="!isShopAdmin" label="所属门店" prop="storeId">
            <SelectShop v-model:shopId="localFormData.storeId" :multiple="false" :type="2" placeholder="请选择门店" />
        </el-form-item> -->
        <!-- 联系电话 -->
        <el-form-item :label="$t('手机号')" prop="phone">
            <el-input v-model="localFormData.phone" :placeholder="$t('请输入手机号码')" maxlength="11" />
        </el-form-item>
        <!-- 账号状态 -->
        <el-form-item :label="staffType === 'deliveryman' ? $t('配送员状态') : $t('服务员状态')" prop="status">
            <el-radio-group v-model="localFormData.status">
                <el-radio :value="1">{{ $t('启用') }}</el-radio>
                <el-radio :value="0">{{ $t('禁用') }}</el-radio>
            </el-radio-group>
        </el-form-item>
    </el-form>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import SelectUser from "@/components/select/src/SelectUser.vue";
import { DialogForm } from "@/components/dialog";
import { Plus } from "@element-plus/icons-vue";
import type { StaffFormData } from "@/api/organize/serviceStaff";
import type { FormInstance, FormRules } from "element-plus";
import deliveryMineImg from "@/style/images/serviceStaff/delivery_mine.png";
import serviceMineImg from "@/style/images/serviceStaff/service_mine.png";

/**
 * 人员表单组件
 * 功能：配送员/服务员新增和编辑的表单
 */

// ===== Props =====
import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    modelValue: StaffFormData;
    staffType: "deliveryman" | "serviceman";
    isEdit?: boolean;
}

const props = defineProps<Props>();

// ===== Emits =====
const emit = defineEmits<{
    "update:modelValue": [value: StaffFormData];
}>();

// ===== 响应式状态 =====
const formRef = ref<FormInstance>();

const localFormData = computed({
    get: () => props.modelValue,
    set: (val) => emit("update:modelValue", val)
});

// 人员类型文本
const staffTypeText = computed(() => {
    return props.staffType === "deliveryman" ? t('配送员') : t('服务员');
});

// 示例图：根据人员类型展示
const staffExampleImg = computed(() => {
    return props.staffType === "deliveryman" ? deliveryMineImg : serviceMineImg;
});

/**
 * 从 SelectUser 组件返回的选中会员信息中直接回显
 */
const handleSelectUserChange = (user: any) => {
    if (!user) {
        localFormData.value.memberUsername = "";
        localFormData.value.memberMobile = "";
        return;
    }

    localFormData.value.memberUsername = user.username || "";
    localFormData.value.memberMobile = user.mobile || "";
    if (user.mobile) {
        localFormData.value.phone = user.mobile;
    }
};

// ===== 表单验证规则 =====
const formRules = computed<FormRules>(() => ({
    memberId: [{ required: true, message: t('请选择会员'), trigger: "change" }],
    name: [
        { required: true, message: t('请输入姓名'), trigger: "blur" },
        { min: 2, max: 20, message: t('姓名长度在 2 到 20 个字符'), trigger: "blur" }
    ],
    phone: [
        { required: true, message: t('请输入手机号码'), trigger: "blur" },
        {
            pattern: /^1[3-9]\d{9}$/,
            message: t('请输入正确的11位手机号'),
            trigger: "blur"
        }
    ]
}));

/**
 * 新建会员成功后回显到 SelectUser
 * DialogForm 会将子表单提交的 result 透传出来
 */
const handleCreateMemberOk = async (result: any) => {
    const userId = result.userId;
    const username = result.username;
    const mobile = result.mobile;

    if (userId) {
        localFormData.value.memberId = userId;
        if (username) localFormData.value.memberUsername = username;
        if (mobile) localFormData.value.memberMobile = mobile;
        if (mobile) localFormData.value.phone = mobile;
    }
};

// ===== Expose =====
defineExpose({
    validate: () => formRef.value?.validate(),
    resetFields: () => formRef.value?.resetFields(),
    clearValidate: () => formRef.value?.clearValidate()
});
</script>

<style lang="less" scoped>
.user-select-wrapper {
    display: flex;
    flex: 1;
    align-items: center;
    gap: 8px;
}

.form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
    line-height: 1.5;
    display: flex;
    align-items: center;
    gap: 4px;
}

.example-content {
    padding: 8px;
    .example-title {
        font-size: 14px;
        font-weight: 600;
        color: #333;
        margin-bottom: 12px;
    }
    .example-desc {
        font-size: 13px;
        color: #666;
        line-height: 1.6;
        p {
            margin: 8px 0;
        }
        .example-note {
            margin-top: 12px;
            padding: 8px;
            background-color: #f5f7fa;
            border-radius: 4px;
            color: #909399;
            font-size: 12px;
        }
    }
    .example-image {
        margin-top: 12px;
        .example-image__img {
            width: 100%;
            height: 220px;
            background: #fff;
        }
    }
}
</style>
