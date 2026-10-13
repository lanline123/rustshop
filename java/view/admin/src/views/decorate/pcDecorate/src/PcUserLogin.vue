<template>
    <div class="decorate-content">
        <div class="decorateWrap">
            <div><img class="img" src="@/style/images/decorate/example/pc_user_bg.jpg" /></div>
        </div>
        <div class="right-box-warp">
            <div class="dec-spread-title">
                <div class="title">{{ $t('登录页广告设置') }}</div>
            </div>
            <a-spin :spinning="loading">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">{{ $t('建议尺寸：高度不要超过580px，宽度可以1700px以内，请结合背景色设置') }}</div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="title">{{ $t('背景广告图') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-pic-group">
                            <PicSelect v-model:photo="module.backgroundPic"></PicSelect>
                        </div>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="title">{{ $t('背景色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.backgroundColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
            </a-spin>
            <div class="dec-edit-save">
                <el-button :loading="confirmLoading" @click="onSubmit" type="primary" size="large" class="save-btn">{{ $t('保存') }}</el-button>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { ref, shallowRef, onMounted } from "vue";
import { PicList, PicSelect } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import { getDecorateDiscrete, updateDecorateDiscrete } from "@/api/decorate/decorateDiscrete";
import { DecorateDiscreteFormState } from "@/types/decorate/decorateDiscrete.d";
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const type = ref("pcUserLogin");
const loading = ref(true);
const confirmLoading = ref(false);
const module = ref<DecorateDiscreteFormState>({
    backgroundColor: "",
    backgroundPic: {
        picUrl: "",
        picThumb: ""
    }
});
// 表单通过验证后提交
onMounted(() => {
    // 获取详情数据
    fetchDecorateDiscrete();
});
const fetchDecorateDiscrete = async () => {
    try {
        const result = await getDecorateDiscrete({
            decorateSn: type.value
        });
        Object.assign(module.value, result.data);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const onSubmit = async () => {
    try {
        confirmLoading.value = true;
        const result = await updateDecorateDiscrete({
            decorateSn: type.value,
            data: module.value
        });
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped></style>
