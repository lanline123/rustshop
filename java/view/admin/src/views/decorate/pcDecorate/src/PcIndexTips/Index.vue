I
<template>
    <div class="decorate-content">
        <!-- <div class="decorate-nav-warp">
            <View></View>
        </div> -->
        <div class="right-box-warp">
            <div class="dec-spread-title">
                <div class="title">{{ $t('首页顶部提示') }}</div>
            </div>
            <a-spin :spinning="loading">
                <perfect-scrollbar class="decorate-edit-con">
                    <ComboGroupNoticeEdit :title="$t('新增提示')" v-model="module.noticeGroups" :activeNames="[1, 2, 3, 4]"> </ComboGroupNoticeEdit>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('是否可关闭') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group class="dec-radio-group" v-model="module.isClose">
                                <el-radio-button :value="1">{{ $t('可关闭') }}</el-radio-button>
                                <el-radio-button :value="2">{{ $t('不可关闭') }}</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('标题颜色') }}</div>
                            <div class="value"></div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="module.titleColor = ''">{{ $t('重置') }}</a>
                                    <SelectColor v-model:color="module.titleColor"></SelectColor>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('描述颜色') }}</div>
                            <div class="value"></div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="module.descColor = ''">{{ $t('重置') }}</a>
                                    <SelectColor v-model:color="module.descColor"></SelectColor>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('背景色') }}</div>
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
                </perfect-scrollbar>
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
import { message } from "ant-design-vue";
import ComboGroupNoticeEdit from "@/views/decorate/decorate/src/modules/src/comboGroup/NoticeEdit.vue";
import View from "./View.vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const type = ref("PcIndexTips");
const loading = ref(false);
const confirmLoading = ref(false);
const groups = [
    {
        notices: [
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('免运费'),
                picDesc: t('前三单购物均可免运费！')
            }
        ]
    },
    {
        notices: [
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('24*7客户支持'),
                picDesc: t('随时随地提供保障')
            },
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('24*7客户支持'),
                picDesc: t('随时随地提供保障')
            },
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('24*7客户支持'),
                picDesc: t('随时随地提供保障')
            }
        ]
    }
];
const module = ref<any>({
    noticeGroups: groups,
    isClose: 1,
    titleColor: "#ffffff",
    descColor: "#c7c6c7",
    backgroundColor: "#1a1a1a"
});
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
<style lang="less" scoped>
:deep(.el-collapse-item__header) {
    width: auto;
}
</style>
