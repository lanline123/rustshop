<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>拼团</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">选择模板</div>
                <div class="value">{{ selectLabel.style[module.style || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <!--                    <el-radio-button :value="1">大图模式</el-radio-button>-->
                    <el-radio-button :value="2">一行四个</el-radio-button>
                    <el-radio-button :value="3">一行五个</el-radio-button>
                    <!--                    <el-radio-button :value="5">详细列表</el-radio-button>-->
                    <el-radio-button :value="6">横向滑动</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">拼团类型</div>
                <div class="value">{{ selectLabel.groupType[module.groupType || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.groupType">
                    <el-radio-button :value="1">普通拼团</el-radio-button>
                    <el-radio-button :value="2">阶梯拼团</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <DialogForm
                isDrawer
                @okCallback="handleGrouponActivitySeleted"
                :params="{ selectedItems: grouponActivityItems, groupType: module.groupType || null }"
                title="选择活动"
                width="600px"
                path="promotion/groupon/src/SelectActivity"
            >
                <el-button type="primary">选择活动</el-button>
            </DialogForm>
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
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">商品卡片样式</div>
                <div class="value">{{ selectLabel.cardStyle[module.cardStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.cardStyle">
                    <el-radio-button :value="1">样式一</el-radio-button>
                    <el-radio-button :value="2">样式二</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">商品卡样式</div>
                <div class="value">{{ selectLabel.goodsStyle[module.goodsStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsStyle">
                    <el-radio-button :value="1">标准</el-radio-button>
                    <el-radio-button :value="2">投影</el-radio-button>
                    <el-radio-button :value="3">描边</el-radio-button>
                    <el-radio-button :value="4">透明底</el-radio-button>
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
                <div class="label">商品卡边角</div>
                <div class="value">{{ selectLabel.goodsRadioStyle[module.goodsRadioStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsRadioStyle">
                    <el-radio-button :value="1">直角</el-radio-button>
                    <el-radio-button :value="2">圆角</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">文本对齐</div>
                <div class="value">{{ selectLabel.textAlign[module.textAlign || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.textAlign">
                    <el-radio-button :value="1">左对齐</el-radio-button>
                    <el-radio-button :value="2">居中对齐</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">文字粗细</div>
                <div class="value">{{ selectLabel.textWeight[module.textWeight || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.textWeight">
                    <el-radio-button :value="1">常规体</el-radio-button>
                    <el-radio-button :value="2">加粗体</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">商品名称行数</div>
                <div class="value">{{ selectLabel.goodsNameRow[module.goodsNameRow || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsNameRow">
                    <el-radio-button :value="1">1行</el-radio-button>
                    <el-radio-button :value="2">2行</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">商品信息边距</div>
                <div class="value">{{ selectLabel.goodsNamePadding[module.goodsNamePadding || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.goodsNamePadding">
                    <el-radio-button :value="1">有边距</el-radio-button>
                    <el-radio-button :value="0">无边距</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示商品名称</div>
                <div class="value">{{ selectLabel.showName[module.showName || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showName">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">不显示</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示商品价格</div>
                <div class="value">{{ selectLabel.showPrice[module.showPrice || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showPrice">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">不显示</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">商品边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.goodsPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">背景颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.backgroundColor = defaultFrame.backgroundColor">重置</a>
                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">圆角</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">内边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.innerPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">左右边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">上边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">下边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <CommonTitleEdit
            v-model="module.title"
            decorateType="mobile"
            enable-more-type
            more-type-legacy="link"
            countdown-label-placeholder="距结束"
        ></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import CommonTitleEdit from "@/views/decorate/decorate/src/modules/src/commonTitle/Edit.vue";
import { onMounted, ref, computed } from "vue";
import { SelectColor } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { CircleCloseFilled } from "@element-plus/icons-vue";
import { GrouponFilterState } from "@/types/promotion/groupon";
import { mergeDefaultModule, defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcGrouponModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcGrouponModule);

// 合并默认值，确保所有配置项都有初始值
const defaultModule = ref({
    style: 2,
    groupType: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 1,
    goodsNamePadding: 0,
    showName: 1,
    showPrice: 1,
    cardStyle: 1,
    goodsPadding: 0,
    innerPadding: 0,
    boxPadding: 0,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    backgroundColor: defaultFrame.backgroundColor,
    boxRadius: 0,
    frame: defaultFrame,
    // 与 index.ts 的 pcGrouponDefaults.title 保持一致
    title: { ...defaultTitle, moreType: 1, countdownLabel: "距结束" }
});
mergeDefaultModule(module.value, defaultModule.value);

const selectLabel = ref<any>({
    style: {
        // 1: "大图模式",
        2: "一行四个",
        3: "一行五个",
        6: "横向滚动"
    },
    groupType: {
        1: "普通拼团",
        2: "阶梯拼团"
    },
    goodsStyle: {
        1: "标准",
        2: "投影",
        3: "描边",
        4: "透明底"
    },
    goodsRadioStyle: {
        1: "直角",
        2: "圆角"
    },
    waterfall: {
        1: "启用",
        0: "否"
    },
    textAlign: {
        1: "左对齐",
        2: "居中对齐"
    },
    textWeight: {
        1: "常规体",
        2: "加粗体"
    },
    goodsNameRow: {
        1: "1行",
        2: "2行"
    },
    goodsNamePadding: {
        1: "有边距",
        0: "无边距"
    },
    showName: {
        1: "显示",
        0: "不显示"
    },
    showCountdown: {
        1: "显示",
        0: "不显示"
    },
    showBrief: {
        1: "显示",
        0: "不显示"
    },
    showPrice: {
        1: "显示",
        0: "不显示"
    },
    crossedOutPrice: {
        1: "显示",
        0: "不显示"
    },
    cardStyle: {
        1: "样式一",
        2: "样式二"
    }
});

const grouponActivityItems = ref<GrouponFilterState[]>([]);
const handleGrouponActivitySeleted = async (result: any) => {
    console.log(result, "result1028");

    grouponActivityItems.value = result;
    module.value.grouponActivityItem = result.length ? grouponActivityItems.value[0] : null;
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
            color: #fb0606;
        }

        &:hover {
            background-color: rgb(223 225 232 / 60%);
        }
    }
}
</style>
