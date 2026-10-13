<template>
    <div class="create-container">
        <h2 class="tit">{{ $t('创建数据') }}</h2>
        <div class="content">
            <el-form :model="formState" label-width="auto" size="large" ref="formRef">
                <el-form-item :label="$t('数据库类型：')" :rules="[{ required: true, message: '请选择数据库类型!' }]" >
                    <el-radio-group v-model="formState.sql">
                        <el-radio :value="0">{{ $t('带演示数据') }}</el-radio>
                        <el-radio :value="1">{{ $t('纯净版(无演示数据)') }}</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item :label="$t('数据库地址：')" prop="dbhost" :rules="[{ required: true, message: '请输入数据库地址!' }]">
                    <el-input v-model="formState.dbhost" :placeholder="$t('请输入数据库地址')" />
                </el-form-item>
                <el-form-item :label="$t('数据库名称：')" prop="dbname" :rules="[{ required: true, message: '请输入数据库名称!' }]">
                    <el-input v-model="formState.dbname" :placeholder="$t('请输入数据库名称')" />
                </el-form-item>
                <el-form-item :label="$t('数据库端口：')" prop="dbport" :rules="[{ required: true, message: '请输入数据库端口!' }]">
                    <el-input v-model="formState.dbport" :placeholder="$t('请输入数据库端口')" />
                </el-form-item>
                <el-form-item :label="$t('数据库用户名：')" prop="dbuser" :rules="[{ required: true, message: '请输入数据库用户名!' }]">
                    <el-input v-model="formState.dbuser" :placeholder="$t('请输入数据库用户名')" />
                </el-form-item>
                <el-form-item :label="$t('数据库密码：')" prop="dbpwd">
                    <el-input type="password" v-model="formState.dbpwd" :placeholder="$t('请输入数据库密码')" />
                </el-form-item>
                <el-form-item :label="$t('Redis地址：')" prop="redishost" :rules="[{ required: true, message: '请输入Redis地址!' }]">
                    <el-input v-model="formState.redishost" :placeholder="$t('请输入Redis地址')" />
                </el-form-item>
                <el-form-item :label="$t('Redis端口：')" prop="redisport" :rules="[{ required: true, message: '请输入Redis端口!' }]">
                    <el-input v-model="formState.redisport" :placeholder="$t('请输入Redis端口')" />
                </el-form-item>
                <el-form-item :label="$t('Redis密码：')" prop="redispwd">
                    <el-input type="password" v-model="formState.redispwd" :placeholder="$t('请输入Redis密码')" />
                </el-form-item>
                <el-form-item :label="$t('RedisSelect库：')" prop="redisselect">
                    <el-input v-model="formState.redisselect" :placeholder="$t('请输入RedisSelect库')" />
                </el-form-item>
                <!-- <el-form-item label=" ">
                    <el-button style="width: 100%;" type="primary" size="large" @click="onSubmit">提 交</el-button>
                </el-form-item> -->
            </el-form>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted, computed, shallowRef } from "vue";
import { updateData } from "@/api/install/install";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["onDisabled"]);
const formRef = shallowRef();
const formState = ref<any>({
    sql:0,
    dbhost: "127.0.0.1",
    dbname: "tigshop",

    dbport: '3306',

    dbuser: "root",
    dbpwd: "",
    redispwd: "",

    redishost: '127.0.0.1',
    redisport: "6379",

    redisselect: ""
});
const isTrue = computed(() => {
    const allFieldsNotEmpty = Object.values(formState.value).every(value => value !== "");
    if (allFieldsNotEmpty) {
        return true;
    } else {
        return false;
    }
});
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        const result = await updateData(formState.value);
        message.success(t('操作成功'));
        emit("onDisabled", 'over');
    } catch (error:any) {
        message.error(error.message);
    }
};
onMounted(() => {
    emit("onDisabled", true);
});
defineExpose({ onSubmit });
</script>
<style lang="less" scoped>
.create-container {
    width: 1190px;
    .tit {
        padding: 30px 0;
        color: #333;
        text-align: center;
    }
    .content {
        font-size: 14px;
        padding: 0 30%;
        line-height: 28px;
    }
}
</style>
