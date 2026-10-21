/**
 * @file 支付记录列表
 * @description 展示订单支付记录（功能待完善）
 * @features
 * - 支付状态筛选
 * - 订单号搜索
 * - 展示支付方式、金额、退款金额等
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item label="" prop="brandDesc">
                        <el-select style="width: 100px" v-model="formState.payStatus">
                            <el-option :value="1" :label="$t('已支付')" />
                            <el-option :value="2" :label="$t('全部')" />
                        </el-select>
                        <TigInput width="200px" v-model="formState.keyword" name="keyword" :placeholder="$t('输入订单号')" style="margin-left: 16px">
                            <template #append>
                                <el-button @click="loadFilter"><span class="iconfont icon-chakan1"></span></el-button>
                            </template>
                        </TigInput>
                    </el-form-item>
                </el-form>
                <el-table>
                    <el-table-column :label="$t('订单编号')"></el-table-column>
                    <el-table-column :label="$t('订单收件人')"></el-table-column>
                    <el-table-column :label="$t('支付方式')"></el-table-column>
                    <el-table-column :label="$t('支付金额')"></el-table-column>
                    <el-table-column :label="$t('已退款金额')"></el-table-column>
                    <el-table-column :label="$t('支付日期')" sortable></el-table-column>
                    <el-table-column :label="$t('支付状态')"></el-table-column>
                    <el-table-column :label="$t('操作')"></el-table-column>
                </el-table>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import request from "@/utils/request";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
// 父组件回调
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);
//获取来自父组件的参数
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: String,
    isDialog: Boolean
});
const action = ref();
const id = ref(0); //表单Ref
const loading = ref(true);
if (!props.isDialog) {
    //获取来自路由的参数
    const { currentRoute } = useRouter();
    const query = <any>currentRoute.value.query;
    action.value = query.act; // add | edit
    id.value = query.id;
} else {
    // 获取来自组件的参数
    action.value = props.act; // add | edit
    id.value = props.id;
}

// 判断是处理更新还是添加
const operation = action.value == "add" ? "insert" : "update";

// 表单参数初使化
const formRef = ref(); //表单Ref
const formState = reactive<any>({}); //表单数据

onMounted(async () => {
    loadFilter();
});

const loadFilter = () => {
    loading.value = false;
    // 获取详情数据
    request({
        url: "brand/" + action.value + "/",
        method: "get",
        params: {
            id: id.value
        }
    })
        .then((result: any) => {
            loading.value = false;
            // 合并前端的初使参数和获取到的参数
            Object.assign(formState, result);
        })
        .catch((error: any) => {
            message.error(error.message);
            emit("close");
        });
};

defineExpose({});
</script>
