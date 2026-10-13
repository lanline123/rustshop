<template>
    <div class="container">
        <div class="decoratePgaeWrap">
            <div class="flex box-wrapper">
                <div class="tab-box">
                    <div class="flex">
                        <a-spin :spinning="loading">
                            <div class="decorateWrap">
                                <div class="decorate-page-window" style="margin-top: 0px">
                                    <div class="theme-modules-warp" v-if="!loading">
                                        <div class="top-img">
                                            <img src="@/style/images/decorate/user/decorate-bg.png" />
                                            <div class="mask"></div>
                                        </div>
                                        <div v-for="(element, index) in toolList">
                                            <Modules
                                                class="modules-box"
                                                :class="{ active: index == type }"
                                                @click="type = index"
                                                v-if="element.type"
                                                :module="element.module"
                                                :moduleType="element.type"
                                            ></Modules>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </a-spin>
                        <div class="right-box-warp">
                            <div class="advertise">
                                <DialogForm
                                    :title="$t('推广')"
                                    width="900px"
                                    :show-footer="false"
                                    path="decorate/mobileDecorate/Advertise"
                                    :params="{
                                        decorateItem: { decorateTitle: '会员首页' },
                                        decorateType: 1,
                                        toPath: '/pages/me/index',
                                        title: '会员首页'
                                    }"
                                >
                                    <a class="btn-link">{{ $t('推广') }}</a>
                                </DialogForm>
                                <el-button class="switch-new-btn" type="primary" plain @click="onSwitchNew">
                                    <el-icon class="switch-new-icon"><Switch /></el-icon>
                                    <span>{{ $t('切换新版') }}</span>
                                    <el-tooltip effect="light" placement="top" :show-after="300">
                                        <template #content>
                                            <div style="width: 260px; padding: 5px 10px; line-height: 1.7">
                                                {{ $t('系统现已上线个人中心装修新版，功能更丰富、操作更便捷；为保障老用户原有装修模板、操作习惯不受影响，平台保留历史旧版装修入口，您可自由切换新旧版本使用。') }}<br /><br />
                                                {{ $t('温馨提示：新旧版本装修数据相互独立，切换后不会同步另一版本页面内容。') }}
                                            </div>
                                        </template>
                                        <el-icon class="switch-new-help" @click.stop><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                </el-button>
                            </div>
                            <div style="max-width: 510px">
                                <a-spin :spinning="loading">
                                    <div class="">
                                        <div v-for="(element, index) in toolList" :key="index" v-show="index == type">
                                            <ModulesEdit v-model:module="element.module" v-model:moduleType="element.type"></ModulesEdit>
                                        </div>
                                    </div>
                                </a-spin>
                                <div class="dec-edit-save" v-if="!loading">
                                    <el-button :loading="confirmLoading" @click="onSubmit" type="primary" size="large" class="save-btn">{{ $t('保存') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { ref, onMounted } from "vue";
import { getDecorateDiscrete, updateDecorateDiscrete, getMenberDecorateData } from "@/api/decorate/decorateDiscrete";
import { saveMemberCenterDecorate, getMemberCenterDecorateSetting } from "@/api/setting/config";
import { DecorateUserFormState } from "@/types/decorate/decorateDiscrete.d";
import { message } from "ant-design-vue";
import Modules from "./Module.vue";
import ModulesEdit from "./ModuleEdit.vue";
import { DialogForm } from "@/components/dialog";
import { Switch, QuestionFilled } from "@element-plus/icons-vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["switch-new"]);
const loading = ref(true);
const confirmLoading = ref(false);

const toolList = ref<DecorateUserFormState[]>([
    { type: "bannerList", label: t('广告位'), module: { bannerList: { picUrl: "" } } },
    { type: "menus", label: t('用户服务'), module: { menuType: 1, menuList: [] } }
]);
const type = ref(0);
// 当前 C 端使用的新旧版开关，保存时据此判断是否需要写入（避免无意义的重复写入）
const currentUseNewMemberCenter = ref<number | null>(null);
// 表单通过验证后提交
onMounted(async () => {
    // 获取详情数据
    await _getMenberDecorateData();
    await fetchDecorateDiscrete();
    fetchMemberCenterSwitch();
});
const fetchMemberCenterSwitch = async () => {
    try {
        const res: any = await getMemberCenterDecorateSetting();
        currentUseNewMemberCenter.value = Number(res?.useNewMemberCenter ?? 1);
    } catch {
        currentUseNewMemberCenter.value = null;
    }
};
const menus = ref<any>([]);
const _getMenberDecorateData = async () => {
    try {
        const result = await getMenberDecorateData();
        menus.value = result.menus;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const fetchDecorateDiscrete = async () => {
    try {
        const result = await getDecorateDiscrete({
            decorateSn: "memberDecorate"
        });
        if (result && result.data) {
            toolList.value = result.data;
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const onSubmit = async (newNumber?: number) => {
    try {
        confirmLoading.value = true;
        await updateDecorateDiscrete({
            decorateSn: "memberDecorate",
            data: toolList.value
        });
        // 保存旧版数据成功后，应用旧版到用户端；当前已是旧版则跳过，避免无意义的重复写入
        if (Number(currentUseNewMemberCenter.value) !== 0) {
            await saveMemberCenterDecorate({ useNewMemberCenter: 0 });
            currentUseNewMemberCenter.value = 0;
        }
        if (newNumber !== 1) {
            message.success(t('保存成功，已应用旧版个人中心'));
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

// 切换回新版个人中心装修（确认弹窗在列表页统一处理）
const onSwitchNew = () => {
    emit("switch-new");
};
</script>
<style lang="less" scoped>
.mx-1 {
    margin: 5px;
    cursor: pointer;
}
.right-box-warp {
    position: relative;
    :deep(.el-tag) {
        border: none !important;
        background-color: #f2f2f2 !important;
        color: #666 !important;
    }
    .advertise {
        position: absolute;
        top: 0px;
        right: 16px;
        z-index: 10;
        display: flex;
        align-items: center;
        gap: 16px;
    }
}
.decorate-page-window {
    background: linear-gradient(90deg, #fcf6fc, #f3f3fe);
    padding: 1px;
}
.theme-modules-warp {
    .top-img {
        position: relative;
        img {
            width: 100%;
        }
        .mask {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0;
            left: 0;
            background-color: rgba(255, 255, 255, 0.5);
        }
    }
    .modules-box {
        cursor: pointer;
        position: relative; // 添加定位上下文
        &:before {
            display: none;
            content: "";
            position: absolute;
            height: 100%;
            left: -1px;
            right: -1px;
            top: 0;
            box-sizing: border-box;
            border: 1px dashed #155bd4;
            z-index: 99;
            cursor: pointer;
            pointer-events: none;
        }
        &:hover {
            &::before {
                display: block;
            }
        }
    }
    // 更具体的选择器，确保只应用于直接子元素
    > div > .modules-box.active {
        &::before {
            border: 2px solid #155bd4 !important;
            display: block;
            left: -2px;
            right: -2px;
        }
    }
}

.tab-box {
    width: 100%;
}
.switch-new-btn {
    display: inline-flex;
    align-items: center;
    .switch-new-icon {
        margin-right: 4px;
    }
    .switch-new-help {
        margin-left: 6px;
        font-size: 14px;
        cursor: help;
    }
}

:deep(.dec-spread-title) {
    padding-top: 0px;
}

.dec-edit-save {
    max-width: 100%;
}
</style>
