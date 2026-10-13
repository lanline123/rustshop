<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="userId" :label="$t('绑定账号')" :rules="[{ required: true, message: '请选择账号', trigger: 'change' }]">
                        <el-select
                            v-if="action === 'add'"
                            style="width: 400px"
                            v-model="formState.userId"
                            filterable
                            remote
                            reserve-keyword
                            :placeholder="$t('可输入手机号查询绑定会员')"
                            :remote-method="remoteMethod"
                            :loading="loadingVal"
                        >
                            <el-option v-for="item in userInfo" :key="item.key" :label="item.label + '(' + item.mobile + ')'" :value="item.value" />
                        </el-select>
                        <p v-else>{{ formState.nickname || formState.baseUserInfo?.username || "--" }}</p>
                    </el-form-item>
                    <el-form-item prop="level" :label="$t('等级')" :rules="[{ required: true, message: '请选择分销员等级', trigger: 'change' }]">
                        <SelectPromoteMode style="width: 400px" v-model:level="formState.level"></SelectPromoteMode>
                    </el-form-item>
                    <el-form-item prop="groupId" :label="$t('分组')" :rules="[{ required: true, message: '请选择分销员分组', trigger: 'change' }]">
                        <SelectSalesmanGroup style="width: 400px" v-model:groupId="formState.groupId"></SelectSalesmanGroup>
                    </el-form-item>
                    <el-form-item prop="nickname" :label="$t('上级分销员')">
                        <el-select
                            style="width: 400px"
                            v-model="formState.pid"
                            filterable
                            remote
                            reserve-keyword
                            :placeholder="$t('可输入手机号查询上级分销员')"
                            :remote-method="getSalesnamList"
                            :loading="loadingSalesnam"
                        >
                            <el-option v-for="item in salesnamList" :key="item.key" :label="item.label" :value="item.value" />
                        </el-select>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { promoterFormState } from "@/types/salesman/promoter.d";
import { getsalesman, getsalesmanList, updatesalesman } from "@/api/salesman/promoter";
import { SelectSalesmanGroup, SelectPromoteMode } from "@/components/select";
import { getUserList } from "@/api/user/user";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
    isDialog: {
        type: Boolean,
        default: false
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<promoterFormState>({});
const loadingVal = ref(false);
const userInfo = ref<any>([]);
const remoteMethod = async (query: string) => {
    try {
        if (query) {
            loadingVal.value = true;
            const params = { keyword: query };
            // 使用公共方法来获取数据
            const result = await getUserList(params);

            // 设置用户信息
            result.records.forEach((item: any) => {
                item.value = item.userId;
                item.key = item.userId;
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
const salesnamList = ref<any[]>([]);
const loadingSalesnam = ref(false);
// 获取列表的查询结果
const getSalesnamList = async (query: string) => {
    try {
        if (query) {
            loadingSalesnam.value = true;
            const params = { mobile: query };
            // 使用公共方法来获取数据
            const result = await getsalesmanList(params);
            // 设置用户信息
            result.records.forEach((item: any) => {
                item.value = item.salesmanId;
                item.key = item.salesmanId;
                item.label = item.baseUserInfo?.nickname || item.baseUserInfo?.username;
            });
            salesnamList.value = result.records;
        } else {
            salesnamList.value = [];
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loadingSalesnam.value = false;
    }
};
onMounted(() => {
    if (action.value === "detail") {
        fetchSalesman();
    } else {
        loading.value = false;
    }
});
const fetchSalesman = async () => {
    try {
        const result = await getsalesman(action.value, {
            id: id.value
        });
        if(result.salesmanVO.pid == 0 || result.salesmanVO.pid == null){
            result.salesmanVO.pid = "";
        }
        Object.assign(formState.value, result.salesmanVO);
        formState.value.userId = result.salesmanVO.baseUserInfo ? String(result.salesmanVO.baseUserInfo.userId) : "";
        formState.value.nickname = result.salesmanVO.baseUserInfo ? result.salesmanVO.baseUserInfo.nickname : "";

        if (formState.value.pidUserInfo) {
            getSalesnamList(formState.value.pidUserInfo.baseUserInfo.mobile || "");
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updatesalesman(operation, { id: id.value, ...formState.value });
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
<style lang="less" scoped></style>
