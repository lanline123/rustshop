/** 代码文件生成器 - 根据表名自动生成控制器、模型、服务、验证器等基础代码文件 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="notice-warp">
                <p>{{ $t('操作会按照后台app/adminapi/controller/example/example.php等基本文件进行创建，您可以自行修改example对应的控制器、模型、服务、验证器等基本文件代码') }}</p>
                <p>{{ $t('提示：所生成的任何文件如果在所在目录下已存在相同名称的文件，操作会失败！') }}</p>
            </div>
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="150">
                    <el-form-item prop="catalog" :label="$t('目录名')" :rules="[{ required: true, message: '目录不能为空!' }]">
                        <TigInput v-model="formState.catalog" />
                        <div class="extra">{{ $t('必须英文，小驼峰，如：product') }}</div>
                    </el-form-item>
                    <el-form-item prop="talbeName" :label="$t('库表名')" :rules="[{ required: true, message: '目录不能为空!' }]">
                        <TigInput v-model="formState.talbeName" />
                        <div class="extra">{{ $t('如：brand') }}</div>
                    </el-form-item>
                    <el-form-item prop="name" :label="$t('中文名称')" :rules="[{ required: true, message: '目录不能为空!' }]">
                        <TigInput v-model="formState.name" />
                        <div class="extra">{{ $t('如：品牌') }}</div>
                    </el-form-item>
                    <el-form-item prop="brandName" :label="$t('主键字段名')" :rules="[{ required: true, message: '目录不能为空!' }]">
                        <TigInput v-model="formState.brandName" />
                        <div class="extra">{{ $t('如：brandId') }}</div>
                    </el-form-item>
                    <el-form-item prop="brandName" :label="$t('名称字段名')" :rules="[{ required: true, message: '目录不能为空!' }]">
                        <TigInput v-model="formState.brandName" />
                        <div class="extra">{{ $t('如：brandName') }}</div>
                    </el-form-item>
                    <el-form-item label="">
                        <div v-if="formState.catalog && formState.talbeName">
                            <p>{{ $t('将生成控制器文件：app/admin/controller/{catalog}/{capitalize}.php', { catalog: formState.catalog, capitalize: capitalize(formState.talbeName) }) }}</p>
                            <p>{{ $t('将生成模型层文件：app/model/{catalog}/{capitalize}.php', { catalog: formState.catalog, capitalize: capitalize(formState.talbeName) }) }}</p>
                            <p>{{ $t('将生成服务层文件：app/service/{catalog}/{capitalize}Service.php', { catalog: formState.catalog, capitalize: capitalize(formState.talbeName) }) }}</p>
                            <p>{{ $t('将生成验证器文件：app/validate/{catalog}/{capitalize}Validate.php', { catalog: formState.catalog, capitalize: capitalize(formState.talbeName) }) }}</p>
                        </div>
                    </el-form-item>
                    <el-form-item :wrapper-col="{ offset: 4, span: 16 }">
                        <a-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提 交') }}</a-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue"
import request from '@/utils/request'
import { capitalize } from '@/utils/util'
import { message } from "ant-design-vue";
import { } from './type.d';

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formRef = shallowRef();
/** 表单数据 */
const formState = ref(<any>{});
const loading = ref(false);

/** 表单提交 - 生成代码文件 */
const onSubmit = () => {
    formRef.value.validate((valid: boolean) => {
        if (valid) {
            request({
                url: '//',
                method: 'post',
                data: {
                    id: id.value,
                    ...formState.value
                }
            }).then((result) => {
                emit("submitCallback", result);
                message.success(t('操作成功'))
            }).catch((error) => {
                message.error(error.message)
            }).finally(() => {
                setTimeout(() => { emit('update:confirmLoading', false) }, 600);
            });
        }
    })
}

</script>