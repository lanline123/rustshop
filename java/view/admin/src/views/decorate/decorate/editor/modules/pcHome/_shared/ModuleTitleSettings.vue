<template>
    <div class="module-title-settings">
        <!-- 显示标题开关 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示标题</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch :model-value="!!localTitle.showTitle" @update:model-value="(v: string | number | boolean) => updateTitleField('showTitle', v ? 1 : 0)" />
            </div>
        </div>

        <!-- 标题设置（仅显示标题时展示） -->
        <template v-if="localTitle.showTitle">
            <!-- 标题样式 -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题样式</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" :model-value="localTitle.titleStyle" @update:model-value="(v: string | number | boolean | undefined) => updateTitleField('titleStyle', Number(v))">
                        <el-radio-button :value="1">样式一</el-radio-button>
                        <el-radio-button :value="2">样式二</el-radio-button>
                        <el-radio-button :value="3">样式三</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <!-- 标题对齐 -->
            <div v-if="!hideTitleAlign" class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题对齐</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" :model-value="localTitle.titleAlign" @update:model-value="(v: string | number | boolean | undefined) => updateTitleField('titleAlign', Number(v))">
                        <el-radio-button :value="1">左对齐</el-radio-button>
                        <el-radio-button :value="2">居中</el-radio-button>
                        <el-radio-button :value="3">右对齐</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <!-- 标题内容 -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题内容</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-input :model-value="localTitle.titleText" placeholder="请输入标题内容" @update:model-value="(v: string) => updateTitleField('titleText', v)" />
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
                            <SelectColor :color="localTitle.titleColor || '#2a3145'" @update:color="(v: string) => updateTitleField('titleColor', v)" />
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
                    <el-input :model-value="localTitle.descText" placeholder="请输入描述内容（选填）" @update:model-value="(v: string) => updateTitleField('descText', v)" />
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
                            <SelectColor :color="localTitle.descColor || '#aaaaaa'" @update:color="(v: string) => updateTitleField('descColor', v)" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 标题背景色 -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">背景颜色</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="updateTitleField('titleBackground', '')">重置</a>
                            <SelectColor :color="localTitle.titleBackground || ''" @update:color="(v: string) => updateTitleField('titleBackground', v)" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 标题圆角 -->
            <div v-if="!hideTitleRadius" class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            :model-value="localTitle.titleRadius || 0"
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

            <template v-if="enableTitleVerticalPadding">
                <!-- 标题上边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题上边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localTitle.titlePaddingTop ?? 20"
                                :max="60"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateTitleField('titlePaddingTop', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 标题下边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题下边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localTitle.titlePaddingBottom ?? 20"
                                :max="60"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateTitleField('titlePaddingBottom', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>

            <!-- 显示更多链接 / 右侧内容总开关 -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ enableMoreType ? "显示右侧内容" : "显示更多链接" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-switch :model-value="!!localTitle.showMore" @update:model-value="(v: string | number | boolean) => updateTitleField('showMore', v ? 1 : 0)" />
                </div>
            </div>

            <template v-if="localTitle.showMore">
                <!-- 右侧内容类型：更多链接 / 倒计时（仅拼团、秒杀等支持倒计时的模块开启） -->
                <div v-if="enableMoreType" class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">右侧内容</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="moreType"
                            @update:model-value="(v: string | number | boolean | undefined) => updateTitleField('moreType', Number(v))"
                        >
                            <el-radio-button :value="MORE_TYPE_LINK">显示更多</el-radio-button>
                            <el-radio-button :value="MORE_TYPE_COUNTDOWN">显示倒计时</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 更多链接设置 -->
                <template v-if="moreType === MORE_TYPE_LINK">
                    <!-- 更多链接文字 -->
                    <div v-if="!hasFixedMoreLinkTitle" class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">链接文字</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-input :model-value="localTitle.moreLink?.title || ''" placeholder="更多" @update:model-value="(v: string) => updateMoreLink('title', v)" />
                        </div>
                    </div>

                    <!-- 更多链接颜色 -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">链接颜色</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="updateTitleField('moreColor', moreColorDefault)">重置</a>
                                    <SelectColor :color="localTitle.moreColor || moreColorDefault" @update:color="(v: string) => updateTitleField('moreColor', v)" />
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 更多链接地址（与 commonTitle/Edit 一致，标签与控件同行，不换行） -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">链接地址</div>
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

                <!-- 倒计时设置（固定跳转专区，不需要配置链接地址） -->
                <template v-else>
                    <!-- 倒计时前缀文字 -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">前缀文字</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-input
                                :model-value="localTitle.countdownLabel || ''"
                                :placeholder="countdownLabelPlaceholder"
                                @update:model-value="(v: string) => updateTitleField('countdownLabel', v)"
                            />
                        </div>
                    </div>

                    <!-- 倒计时文字颜色 -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">文字颜色</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="updateTitleField('moreColor', moreColorDefault)">重置</a>
                                    <SelectColor :color="localTitle.moreColor || moreColorDefault" @update:color="(v: string) => updateTitleField('moreColor', v)" />
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </template>
        </template>
    </div>
</template>

<script setup lang="ts">
/**
 * 模块标题设置面板（移植自 Vort-Admin，控件 Element 化）
 * 编辑的字段结构与 Vort ModuleTitle 完全一致（契约对齐）
 */
import { computed } from "vue";
import { SelectColor, SelectLink } from "@/components/select";
import { MORE_TYPE_COUNTDOWN, MORE_TYPE_LINK } from "../../../utils/titleMore";
import type { LinkItem, ModuleTitle } from "./types";

const props = withDefaults(
    defineProps<{
        /** 标题配置对象 */
        modelValue?: ModuleTitle;
        /** 固定"更多链接文字"，传入后将隐藏"链接文字"输入 */
        fixedMoreLinkTitle?: string;
        /** 是否显示标题上/下边距设置 */
        enableTitleVerticalPadding?: boolean;
        /** 是否隐藏标题对齐设置 */
        hideTitleAlign?: boolean;
        /** 是否隐藏标题圆角设置 */
        hideTitleRadius?: boolean;
        /** 是否开启"更多链接 / 倒计时"二选（拼团、秒杀等支持倒计时的模块） */
        enableMoreType?: boolean;
        /** 倒计时前缀文字输入框的占位文案（如"距结束"、"倒计时"） */
        countdownLabelPlaceholder?: string;
    }>(),
    {
        fixedMoreLinkTitle: "",
        enableTitleVerticalPadding: false,
        hideTitleAlign: false,
        hideTitleRadius: false,
        enableMoreType: false,
        countdownLabelPlaceholder: "倒计时"
    }
);

const { enableTitleVerticalPadding, hideTitleAlign, hideTitleRadius, enableMoreType } = props;
const hasFixedMoreLinkTitle = computed(() => !!props.fixedMoreLinkTitle?.trim());

const emit = defineEmits<{
    (e: "update:modelValue", value: ModuleTitle): void;
}>();

/** 拼团/秒杀倒计时默认黑字，普通「更多链接」模块仍用灰色 */
const moreColorDefault = computed(() => (enableMoreType ? "#1a1a1a" : "#969799"));

// 默认标题配置（与 Vort 一致；moreColor 由 moreColorDefault 按模块覆盖）
const defaultTitle: ModuleTitle = {
    showTitle: 0,
    titleStyle: 1,
    titleAlign: 1,
    titleBackground: "",
    titleBackground2: "",
    titleBackgroundPic: { picUrl: "", picThumb: "" },
    titleRadius: 0,
    titlePaddingTop: 20,
    titlePaddingBottom: 20,
    titleText: "标题内容",
    titleColor: "#2a3145",
    descText: "",
    descColor: "#aaaaaa",
    showMore: 0,
    moreType: MORE_TYPE_LINK,
    countdownLabel: "",
    moreLink: { link: "", title: "" },
    moreColor: "#969799"
};

const localTitle = computed(() => ({
    ...defaultTitle,
    ...props.modelValue,
    moreLink: {
        ...defaultTitle.moreLink,
        ...props.modelValue?.moreLink,
        title: hasFixedMoreLinkTitle.value ? props.fixedMoreLinkTitle : (props.modelValue?.moreLink?.title ?? defaultTitle.moreLink?.title ?? "")
    }
}));

/** 未开启二选的模块恒为"更多链接"，避免老模块被 moreType 脏数据影响 */
const moreType = computed(() => {
    if (!enableMoreType) return MORE_TYPE_LINK;
    return Number(localTitle.value.moreType) === MORE_TYPE_COUNTDOWN ? MORE_TYPE_COUNTDOWN : MORE_TYPE_LINK;
});

function normalizeTitlePayload(payload: ModuleTitle): ModuleTitle {
    if (!hasFixedMoreLinkTitle.value) return payload;
    return {
        ...payload,
        moreLink: {
            ...payload.moreLink,
            title: props.fixedMoreLinkTitle
        }
    };
}

// SelectLink 组件需要的值：完整链接对象（含 path/id/sn/data 等用于弹窗回显选中项）
const moreLinkSelectValue = computed<LinkItem>(() => {
    const more = localTitle.value.moreLink ?? { link: "", title: "" };
    // title 仅用于展示文字，不属于链接选择数据，剔除避免干扰回显
    const { title: _title, ...linkData } = more;
    return {
        ...linkData,
        link: linkData.link || "",
        label: linkData.label || "",
        name: linkData.name || "",
        path: linkData.path || ""
    };
});

function updateTitleField<K extends keyof ModuleTitle>(key: K, value: ModuleTitle[K]) {
    emit(
        "update:modelValue",
        normalizeTitlePayload({
            ...localTitle.value,
            [key]: value
        })
    );
}

function updateMoreLink(key: "link" | "title", value: string) {
    emit(
        "update:modelValue",
        normalizeTitlePayload({
            ...localTitle.value,
            moreLink: {
                ...localTitle.value.moreLink,
                [key]: value
            }
        })
    );
}

function handleMoreLinkSelect(item: LinkItem) {
    const currentTitle = localTitle.value.moreLink?.title || "";
    emit(
        "update:modelValue",
        normalizeTitlePayload({
            ...localTitle.value,
            moreLink: {
                // 完整保留 SelectLink 回传的字段（path/id/sn/data/link 等），
                // 否则前台 urlFormat 缺少跳转参数会导致"更多"链接点击无法跳转
                ...item,
                link: item?.link || "",
                // 仅在未填写时使用所选链接名称，避免覆盖用户手填的链接文字
                title: currentTitle || item?.name || item?.label || ""
            }
        })
    );
}
</script>
