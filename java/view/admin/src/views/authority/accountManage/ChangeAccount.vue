/** 更换主账号表单 - 将店铺/门店/供应商绑定到新的会员或管理员账号 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item>
                        <template #label> <span class="required">*</span>{{ $t('绑定账号') }} </template>
                        <div class="one-item" style="width: 100%">
                            <div class="to-time" style="width: 100%">
                                <el-radio-group v-model="formState.adminData.type">
                                    <el-radio :value="1">{{ $t('会    员') }}</el-radio>
                                </el-radio-group>
                                <el-form-item
                                    style="width: 100%"
                                    :rules="formState.adminData.type == 1 ? [{ required: true, message: '请选择会员!' }] : []"
                                    prop="adminData.userId"
                                >
                                    <el-select
                                        style="width: 100%"
                                        v-model="formState.adminData.userId"
                                        :disabled="formState.adminData.type == 2"
                                        filterable
                                        remote
                                        clearable
                                        reserve-keyword
                                        :placeholder="$t('可输入手机号查询绑定会员')"
                                        :remote-method="remoteMethod"
                                        :loading="loadingVal"
                                    >
                                        <el-option v-for="item in userInfo" :key="item.key" :label="item.label" :value="item.value" />
                                    </el-select>
                                </el-form-item>
                            </div>
                            <div class="to-time" style="width: 100%">
                                <el-radio-group v-model="formState.adminData.type">
                                    <el-radio :value="2">{{ $t('管理员') }}</el-radio>
                                </el-radio-group>
                                <el-form-item
                                    style="width: 100%"
                                    :rules="formState.adminData.type == 2 ? [{ required: true, message: '请选择管理员!' }] : []"
                                    prop="adminData.adminId"
                                >
                                    <el-select
                                        style="width: 100%"
                                        v-model="formState.adminData.adminId"
                                        :disabled="formState.adminData.type == 1"
                                        filterable
                                        clearable
                                        remote
                                        reserve-keyword
                                        :placeholder="$t('可输入手机号查询绑定管理员')"
                                        :remote-method="remoteMethod"
                                        :loading="loadingVal"
                                    >
                                        <el-option v-for="item in userInfo" :key="item.key" :label="item.label" :value="item.value" />
                                    </el-select>
                                </el-form-item>
                            </div>
                        </div>
                        <div class="extra" style="width: 100%; margin-top: 16px">{{ $t('必须绑定一位{type2}。', { type2: formState.adminData.type == 2 ? "「管理员」" : "「会员」" }) }}</div>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { bindMainAccount } from "@/api/authority/accountManage";
import { getAdminUserList } from "@/api/authority/adminUser";
import { getUserList } from "@/api/user/user";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = ref(true);

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
    type: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const formRef = shallowRef();
/** 表单数据 */
const formState = ref<any>({
    id: props.id,
    type: props.type,
    adminData: {
        type: 1,
        userId: null,
        adminId: null
    }
});
const loadingVal = ref(false);
/** 搜索到的用户列表 */
const userInfo = ref<any>([]);

/** 远程搜索用户/管理员 */
const remoteMethod = async (query: string) => {
    try {
        if (query) {
            loadingVal.value = true;
            const params = { keyword: query };
            // 使用公共方法来获取数据
            const result = await getUserFetch(params);
            // 设置用户信息
            result.records.forEach((item: any) => {
                if (formState.value.adminData.type === 2) {
                    item.value = item.adminId;
                    item.key = item.adminId;
                } else {
                    item.value = item.userId;
                    item.key = item.userId;
                }
                item.label = item.username;
            });
            userInfo.value = result.records;
        } else {
            userInfo.value = [];
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loadingVal.value = false;
    }
};

/** 根据账号类型获取用户列表 */
const getUserFetch = async (params: { keyword: string }) => {
    if (formState.value.adminData.type === 2) {
        return await getAdminUserList(params);
    } else {
        return await getUserList(params);
    }
};

/** 表单提交 - 绑定新主账号 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await bindMainAccount(formState.value);
        emit("submitCallback", result);
        message.success(t('操作成功'));
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
<style lang="less" scoped>
.required {
    color: var(--el-color-danger);
    margin-right: 4px;
}
.one-item {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .to-time {
        display: flex;
        align-items: center;
        gap: 20px;
    }
}
</style>
