<template>
    <div class="success-container">
        <div class="content">
            <div class="tit flex flex-align-center flex-justify-center">
                <el-icon color="#87b848" size="40"><CircleCheckFilled /></el-icon>
                <div>{{ $t('安装成功') }}</div>
            </div>
            <div class="text">
                <div>{{ $t('安装完成后建议删除public/install下的文件') }}</div>
                <div>{{ $t('初始后台管理员账户（账号：admin 密码：admin123 ）') }}</div>
            </div>
            <div class="btns">
                <el-popover placement="bottom" :width="200" offset="30" trigger="hover">
                    <template #reference>
                        <el-button @click="toPage(url + '/mobile/')" class="mr10" size="large">{{ $t('进入前台(H5端)') }}</el-button>
                    </template>
                    <template #default>
                        <QRCode :size="180" :value="url + '/mobile/'"></QRCode>
                        <div style="text-align: center;margin-top: 10px;">
                            <div>{{ $t('打开手机微信扫码访问') }}</div>
                            <div>{{ $t('或者直接点击上方按钮访问') }}</div>
                        </div>
                    </template>
                </el-popover>
                <el-button @click="toPage(url + '/')" class="mr10" size="large">{{ $t('进入前台(PC端)') }}</el-button>
                <el-button @click="toPage(url + '/admin/')" type="primary" size="large">{{ $t('进入后台') }}</el-button>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted, computed, shallowRef } from "vue";
import { QRCode } from "ant-design-vue";
import { CircleCheckFilled, CircleCloseFilled } from "@element-plus/icons-vue";
const url = ref(`${window.location.protocol}//${window.location.host.split(':')[0]}`)
const toPage = (path: string) => {
    window.open(path, "_blank");
};
onMounted(() => {
});
</script>
<style lang="less" scoped>
.success-container {
    width: 1190px;
    .content {
        width: 100%;
        font-size: 14px;
        line-height: 28px;
        .tit {
            margin-top: 20%;
            font-size: 30px;
            font-weight: bold;
            div {
                margin-left: 20px;
                margin-bottom: 5px;
            }
        }
        .text {
            margin-top: 6%;
            font-size: 16px;
            text-align: center;
        }
        .btns {
            margin-top: 6%;
            text-align: center;
        }
    }
}
</style>
