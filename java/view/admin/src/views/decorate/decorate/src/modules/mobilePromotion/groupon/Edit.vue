<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('拼团') }}</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('选择模板') }}</div>
                <div class="value">{{ selectLabel.style[module.style || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <el-radio-button :value="1">{{ $t('大图模式') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('一行两个') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('一行三个') }}</el-radio-button>
                    <el-radio-button :value="5">{{ $t('详细列表') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('拼团类型') }}</div>
                <div class="value">{{ selectLabel.groupType[module.groupType || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.groupType">
                    <el-radio-button :value="1">{{ $t('普通拼团') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('阶梯拼团') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-activity-select">
                <DialogForm isDrawer @okCallback="handleGrouponActivitySeleted"
                    :params="{ selectedItems: grouponActivityItems, groupType: module.groupType }" :title="$t('选择活动')"
                    width="600px" path="promotion/groupon/src/SelectActivity">
                    <el-button type="primary">{{ $t('选择活动') }}</el-button>
                </DialogForm>
                <div v-if="showActivityError" class="dec-form-error">请选择拼团活动</div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="groupon-activity-list">
                <div class="groupon-activity" v-for="(item, index) in grouponActivityItems">
                    {{ item.name }}
                    <div class="btn-close" @click="removeActivity(index)">
                        <el-icon>
                            <CircleCloseFilled />
                        </el-icon>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品卡样式') }}</div>
                <div class="value">{{ selectLabel.goodsStyle[module.goodsStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsStyle">
                    <el-radio-button :value="1">{{ $t('标准') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('投影') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('描边') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('透明底') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- <div class="dec-edit-group" v-if="module.style === 2">
            <div class="dec-edit-group-title">
                <div class="label">瀑布流</div>
                <div class="value">{{ selectLabel.waterfall[module.waterfall || 0] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.waterfall">
                    <el-radio-button :value="1">启用</el-radio-button>
                    <el-radio-button :value="0">否</el-radio-button>
                </el-radio-group>
            </div>
        </div> -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品卡边角') }}</div>
                <div class="value">{{ selectLabel.goodsRadioStyle[module.goodsRadioStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsRadioStyle">
                    <el-radio-button :value="1">{{ $t('直角') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('圆角') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文本对齐') }}</div>
                <div class="value">{{ selectLabel.textAlign[module.textAlign || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.textAlign">
                    <el-radio-button :value="1">{{ $t('左对齐') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('居中对齐') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文字粗细') }}</div>
                <div class="value">{{ selectLabel.textWeight[module.textWeight || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.textWeight">
                    <el-radio-button :value="1">{{ $t('常规体') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('加粗体') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品名称行数') }}</div>
                <div class="value">{{ selectLabel.goodsNameRow[module.goodsNameRow || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsNameRow">
                    <el-radio-button :value="1">{{ $t('1行') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('2行') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品信息边距') }}</div>
                <div class="value">{{ selectLabel.goodsNamePadding[module.goodsNamePadding || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsNamePadding">
                    <el-radio-button :value="1">{{ $t('有边距') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('无边距') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示商品名称') }}</div>
                <div class="value">{{ selectLabel.showName[module.showName || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showName">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示商品价格') }}</div>
                <div class="value">{{ selectLabel.showPrice[module.showPrice || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showPrice">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.goodsPadding" show-input :show-input-controls="false" size="small"
                        input-size="default" :max="50" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('购买按钮样式') }}</div>
                <div class="value">{{ selectLabel.buyBtnStyle[module.buyBtnStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.buyBtnStyle">
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    <el-radio-button :value="1">{{ $t('样式一') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('样式二') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('样式三') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('样式四') }}</el-radio-button>
                    <el-radio-button :value="5">{{ $t('样式五') }}</el-radio-button>
                    <el-radio-button :value="6">{{ $t('样式六') }}</el-radio-button>
                    <el-radio-button :value="7">{{ $t('样式七') }}</el-radio-button>
                    <el-radio-button :value="8">{{ $t('样式八') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.backgroundColor = defaultFrame.backgroundColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('圆角') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxRadius" show-input :show-input-controls="false" size="small"
                        input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('内边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.innerPadding" show-input :show-input-controls="false" size="small"
                        input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('左右边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPadding" show-input :show-input-controls="false" size="small"
                        input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('上边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingTop" show-input :show-input-controls="false" size="small"
                        input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('下边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingBottom" show-input :show-input-controls="false" size="small"
                        input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <CommonTitleEdit v-model="module.title" decorateType="mobile"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import CommonTitleEdit from "../../src/commonTitle/Edit.vue";
import { ModuleActivityType } from "@/types/decorate/decorate.d";
import { computed, defineModel, onMounted, ref } from "vue";
import { defaultFrame } from "@/views/decorate/decorate/src/modules";
import { SelectColor } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { CircleCloseFilled } from "@element-plus/icons-vue";
import { GrouponFilterState } from "@/types/promotion/groupon";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleActivityType>("module", { default: () => ({}) });

const selectLabel = ref<any>({
    style: {
        1: t('大图模式'),
        2: t('一行两个'),
        3: t('一行三个'),
        5: t('详细列表')
    },
    groupType: {
        1: t('普通拼团'),
        2: t('阶梯拼团')
    },
    goodsStyle: {
        1: t('标准'),
        2: t('投影'),
        3: t('描边'),
        4: t('透明底')
    },
    goodsRadioStyle: {
        1: t('直角'),
        2: t('圆角')
    },
    waterfall: {
        1: t('启用'),
        0: t('否')
    },
    textAlign: {
        1: t('左对齐'),
        2: t('居中对齐')
    },
    textWeight: {
        1: t('常规体'),
        2: t('加粗体')
    },
    goodsNameRow: {
        1: t('1行'),
        2: t('2行')
    },
    goodsNamePadding: {
        1: t('有边距'),
        0: t('无边距')
    },
    showName: {
        1: t('显示'),
        0: t('不显示')
    },
    showBrief: {
        1: t('显示'),
        0: t('不显示')
    },
    showPrice: {
        1: t('显示'),
        0: t('不显示')
    },
    crossedOutPrice: {
        1: t('显示'),
        0: t('不显示')
    },
    buyBtnStyle: {
        0: t('不显示'),
        1: t('样式一'),
        2: t('显示'),
        3: t('显示'),
        4: t('显示'),
        5: t('显示'),
        6: t('显示'),
        7: t('显示'),
        8: t('显示')
    }
});

const grouponActivityItems = ref<GrouponFilterState[]>([]);
const showActivityError = computed(
    () => !!module.value.activitySelectError && !module.value.grouponActivityItem && grouponActivityItems.value.length === 0
);
const clearActivityError = () => {
    if (module.value.activitySelectError) {
        module.value.activitySelectError = false;
    }
};
const handleGrouponActivitySeleted = async (result: any) => {
    grouponActivityItems.value = result;
    module.value.grouponActivityItem = result.length ? grouponActivityItems.value[0] : null;
    if (module.value.grouponActivityItem) {
        clearActivityError();
    }
};
const removeActivity = (index: number) => {
    grouponActivityItems.value.splice(index, 1);
    module.value.grouponActivityItem = null;
};

onMounted(() => {
    if (module.value.grouponActivityItem) {
        grouponActivityItems.value.push(module.value.grouponActivityItem);
    }
});
</script>
<style lang="less" scoped>
.handle {
    box-sizing: border-box;
    position: absolute;
    background: #fff;
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate {
    touch-action: none;
    position: absolute;
    box-sizing: border-box;
    border: 1px solid transparent;
    background: rgba(21, 91, 212, 0.6);
    color: #fff;
}

.vue-drag-resize-rotate.active {
    background: rgba(21, 91, 212, 0.9);
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate:hover {
    border: 1px solid #155bd4;
}

.dec-activity-select {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.dec-form-error {
    margin-top: 8px;
    font-size: 12px;
    line-height: 1.5;
    color: #ff4d4f;
}

.groupon-activity-list {
    flex: 1;
    padding: 10px;
    display: flex;
    flex-direction: column;
    gap: 5px;

    .groupon-activity {
        width: 100%;
        padding-left: 10px;
        height: 25px;
        line-height: 25px;
        font-size: 12px;
        background-color: rgb(223 225 232 / 40%);
        border-radius: 2px;
        position: relative;

        .btn-close {
            position: absolute;
            right: 15px;
            top: 2px;
            cursor: pointer;
            color: #FB0606;
        }

        &:hover {
            background-color: rgb(223 225 232 / 60%);
        }
    }
}
</style>
