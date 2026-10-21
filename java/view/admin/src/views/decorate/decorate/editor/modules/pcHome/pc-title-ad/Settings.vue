<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>文本标题</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-title-box">
                    <div class="title">标题设置</div>
                </div>

                <!-- 标题栏风格 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题栏风格</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.title?.titleStyle ?? 1"
                            @update:model-value="(v: string | number | boolean | undefined) => updateTitleField('titleStyle', Number(v))"
                        >
                            <el-radio-button :value="1">风格一</el-radio-button>
                            <el-radio-button :value="2">风格二</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 标题位置 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题位置</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.title?.titleAlign ?? 1"
                            @update:model-value="(v: string | number | boolean | undefined) => updateTitleField('titleAlign', Number(v))"
                        >
                            <el-radio-button :value="1">居左</el-radio-button>
                            <el-radio-button :value="2">居中</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 标题背景色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题背景色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateTitleField('titleBackground', '')">重置</a>
                                <SelectColor :color="localData.title?.titleBackground || ''" @update:color="(v: string) => updateTitleField('titleBackground', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 标题圆角 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题圆角</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.title?.titleRadius ?? 0"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateTitleField('titleRadius', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 标题内容 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题内容</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input :model-value="localData.title?.titleText ?? ''" placeholder="请输入标题内容" @update:model-value="updateTitleText" />
                    </div>
                </div>

                <!-- 标题颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateTitleField('titleColor', '#2a3145')">重置</a>
                                <SelectColor :color="localData.title?.titleColor || '#2a3145'" @update:color="(v: string) => updateTitleField('titleColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 描述内容 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">描述内容</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input :model-value="localData.title?.descText ?? ''" placeholder="请输入描述内容（选填）" @update:model-value="updateDescText" />
                    </div>
                </div>

                <!-- 描述颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">描述颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateTitleField('descColor', '#aaaaaa')">重置</a>
                                <SelectColor :color="localData.title?.descColor || '#aaaaaa'" @update:color="(v: string) => updateTitleField('descColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 显示查看更多 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示查看更多</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="Number(localData.title?.showMore ?? 0)"
                            @update:model-value="(v: string | number | boolean | undefined) => updateTitleField('showMore', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 更多文字颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">更多文字颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateTitleField('moreColor', '#969799')">重置</a>
                                <SelectColor :color="localData.title?.moreColor || '#969799'" @update:color="(v: string) => updateTitleField('moreColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 更多链接（使用链接选择器，映射为 module.title.moreLink{link,title}） -->
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">更多链接</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-link-group">
                            <div class="lyecs-link-select">
                                <SelectLink decorate-type="pc" :model-value="moreLinkSelectValue" @update:model-value="handleMoreLinkSelect" />
                            </div>
                        </div>
                    </div>
                </div>
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>

                <ModuleContainerSettings :model-value="localData.frame" show-inner-padding @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 文本标题模块 - 设置组件
 * 标题字段为本模块核心内容，按 Vort 源逐字段编辑（不复用 ModuleTitleSettings 的开关式面板）
 */
import { computed, ref } from "vue";
import { SelectColor, SelectLink } from "@/components/select";
import type { LinkItem, ModuleTitle } from "../_shared/types";
import type { PcTitleAdModule } from "./types";
import { pcTitleAdDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { SettingsTabs, ModuleContainerSettings } from "../_shared";

defineOptions({ name: "PcTitleAdSettings" });

const { localData, updateField } = useModuleSettings<PcTitleAdModule>(pcTitleAdDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

function updateTitleField<K extends keyof ModuleTitle>(key: K, value: ModuleTitle[K]) {
    updateField("title", {
        ...(localData.value.title || {}),
        [key]: value
    });
}

function updateTitleText(v: string) {
    updateField("title", {
        ...(localData.value.title || {}),
        titleText: v,
        showTitle: 1
    });
}

function updateDescText(v: string) {
    updateTitleField("descText", v);
}

// SelectLink 需要完整链接对象回显（含 path/id/sn/data 等），与 _shared/ModuleTitleSettings 的存储约定一致
const moreLinkSelectValue = computed<LinkItem>(() => {
    const more = localData.value.title?.moreLink ?? {};
    // title 仅用于展示文字，不属于链接选择数据，剔除避免干扰回显
    const { title: _title, ...linkData } = more;
    return {
        ...linkData,
        link: linkData.link || ""
    };
});

function handleMoreLinkSelect(item: LinkItem) {
    const currentTitle = localData.value.title?.moreLink?.title || "";
    updateTitleField("moreLink", {
        // 完整保留 SelectLink 回传的字段（path/id/sn/data/link 等），
        // 否则前台 urlFormat 缺少跳转参数会导致"更多"链接点击无法跳转
        ...item,
        link: item?.link || "",
        // 仅在未填写时使用所选链接名称，避免覆盖用户已配置的链接文字
        title: currentTitle || item?.name || item?.label || ""
    });
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
