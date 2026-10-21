<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>门店导航</h3>
            <div class="dec-edit-desc">展示附近门店，仅多门店首页可用</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-title-box">
                <div class="title">样式设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">门店样式</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.storeStyle">
                            <el-radio-button :value="StoreNavStyle.One">样式一</el-radio-button>
                            <el-radio-button :value="StoreNavStyle.Two">样式二</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">展示门店</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.storeSource">
                            <el-radio-button :value="StoreNavSource.Auto">自动获取</el-radio-button>
                            <el-radio-button :value="StoreNavSource.Assign">指定门店</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group store-num-group" v-if="module.storeSource === StoreNavSource.Auto">
                <div class="dec-edit-group-title">
                    <div class="label">门店数量</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input-number
                            v-model="storeNum"
                            :min="STORE_NAV_MIN_AUTO_NUM"
                            :max="STORE_NAV_MAX_AUTO_NUM"
                            :step="1"
                            step-strictly
                            controls-position="right"
                            style="width: 100px"
                        />
                    </div>
                </div>
                <div class="dec-edit-group-desc">根据定位数据展示由近到远门店择优展示</div>
            </div>
            <template v-if="module.storeSource === StoreNavSource.Assign">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">选择门店</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <DialogForm
                            isDrawer
                            @okCallback="onStoreSelected"
                            title="选择门店"
                            width="600px"
                            path="decorate/decorate/editor/modules/mobilePro/storeNav/SelectStorePop"
                            :params="{ selectedList: module.storeList ?? [], maxSelection: STORE_NAV_MAX_STORE }"
                        >
                            <el-button type="primary">选择门店</el-button>
                        </DialogForm>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-con">
                        <draggable
                            v-if="module.storeList && module.storeList.length > 0"
                            class="store-nav-store-list"
                            item-key="shopId"
                            :list="module.storeList"
                            handle=".drag-handle"
                            ghost-class="ghost"
                            chosen-class="chosenClass"
                            animation="300"
                        >
                            <template #item="{ element, index }">
                                <div class="store-nav-store-item">
                                    <span class="drag-handle"><i v-for="n in 6" :key="n"></i></span>
                                    <div class="item-title">{{ element.shopTitle }}</div>
                                    <a class="item-del" @click="removeStore(index)">删除</a>
                                </div>
                            </template>
                        </draggable>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div>最多支持{{ STORE_NAV_MAX_STORE }}个门店</div>
                    </div>
                </div>
            </template>
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">模块圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.borderRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div> -->
            <CommonSubTitleEdit v-if="module.storeStyle !== StoreNavStyle.Two" v-model="module.subTitle" title="标题" type="categoryRec"></CommonSubTitleEdit>
            <template v-if="module.storeStyle !== StoreNavStyle.Two">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t("标题上下间距") }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.titlePaddingY" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t("标题左右间距") }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.titlePaddingX" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                        </div>
                    </div>
                </div>
            </template>
            <CommonSubTitleEdit v-model="module.storeTitleStyle" title="门店标题" type="categoryRec"></CommonSubTitleEdit>
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">描述文字</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.showDesc">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">关注按钮</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.showFollow">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div> -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">门店评分</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.showRating">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <template v-if="module.storeStyle === StoreNavStyle.Two">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">关注按钮</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-radio-group">
                            <el-radio-group class="dec-radio-group" v-model="module.showFollow">
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">起送价</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-radio-group">
                            <el-radio-group class="dec-radio-group" v-model="module.showMinOrder">
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">配送费</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-radio-group">
                            <el-radio-group class="dec-radio-group" v-model="module.showDeliveryFee">
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">距离</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-radio-group">
                            <el-radio-group class="dec-radio-group" v-model="module.showDistance">
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
            </template>
            <!-- <div class="dec-edit-group bg-grey" v-if="module.showDesc == 1">
                <div class="dec-edit-group-title">
                    <div class="label">描述文字颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.descColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.descColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div> -->
            <ContentStyleEdit v-model="module.contentStyle" type="noticeA2"></ContentStyleEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">门店间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.storeGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">门店背景颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.storeBackgroundColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.storeBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">门店上下间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.storePaddingY" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">门店左右间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.storePaddingX" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">门店圆角半径</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.storeRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <template v-if="module.storeStyle === StoreNavStyle.Two">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品项间距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.productGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品名称</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-radio-group">
                            <el-radio-group class="dec-radio-group" v-model="module.showProductName">
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group bg-grey" v-if="module.showProductName === 1">
                    <div class="dec-edit-group-title">
                        <div class="label">商品名称行数</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-radio-group">
                            <el-radio-group class="dec-radio-group" v-model="module.productNameRow">
                                <el-radio-button :value="1">一行</el-radio-button>
                                <el-radio-button :value="2">两行</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品价格</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-radio-group">
                            <el-radio-group class="dec-radio-group" v-model="module.showProductPrice">
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
            </template>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="noticeA2"></ModuleStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import { DialogForm } from "@/components/dialog";
import Tabs from "@/components/tabs/Index.vue";
import draggable from "vuedraggable";
import { ref, reactive, computed, watch } from "vue";
import { cloneDeep } from "lodash";
import CommonSubTitleEdit from "@/views/decorate/decorate/src/modules/src/commonSubTitle/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import {
    StoreNavSource,
    StoreNavStyle,
    STORE_NAV_MAX_STORE,
    STORE_NAV_MIN_AUTO_NUM,
    STORE_NAV_MAX_AUTO_NUM,
    type StoreNavModule,
    type StoreNavShopItem
} from "./types";
import { storeNavDefaults } from "./index";
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as StoreNavModule);

// 旧数据缺字段时补齐门店标题样式，保证 CommonSubTitleEdit 可双向绑定
watch(
    module,
    (mod) => {
        if (!mod || typeof mod !== "object") return;
        if (!mod.storeTitleStyle) {
            mod.storeTitleStyle = cloneDeep(storeNavDefaults.storeTitleStyle);
        }
    },
    { immediate: true }
);
const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

// 输入框清空时会回传 null，统一收敛到合法区间再写回模块
const storeNum = computed<number>({
    get: () => module.value.storeNum ?? STORE_NAV_MIN_AUTO_NUM,
    set: (val) => {
        const num = Number(val);
        if (!Number.isFinite(num)) {
            module.value.storeNum = STORE_NAV_MIN_AUTO_NUM;
            return;
        }
        module.value.storeNum = Math.min(Math.max(Math.round(num), STORE_NAV_MIN_AUTO_NUM), STORE_NAV_MAX_AUTO_NUM);
    }
});

const onStoreSelected = (list: StoreNavShopItem[]) => {
    module.value.storeList = Array.isArray(list)
        ? list.map((item) => ({
              shopId: item.shopId,
              shopTitle: item.shopTitle || "",
              shopLogo: item.shopLogo || ""
          }))
        : [];
};

const removeStore = (index: number) => {
    module.value.storeList?.splice(index, 1);
};
</script>
<style lang="less" scoped>
// 让描述换行到数量输入框下方，保持标签与控件仍在同一行
.store-num-group {
    flex-wrap: wrap;

    .dec-edit-group-desc {
        width: 100%;
    }
}

.store-nav-store-list {
    width: 100%;

    .store-nav-store-item {
        display: flex;
        align-items: center;
        column-gap: 10px;
        padding: 10px 12px;
        margin-bottom: 8px;
        background: #f7f8fa;
        border-radius: 4px;

        &:last-child {
            margin-bottom: 0;
        }

        .drag-handle {
            flex: none;
            display: grid;
            grid-template-columns: repeat(2, 3px);
            grid-template-rows: repeat(3, 3px);
            gap: 2px;
            cursor: move;

            i {
                width: 3px;
                height: 3px;
                border-radius: 50%;
                background: #c8c9cc;
            }
        }

        .item-title {
            flex: 1;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            font-size: 13px;
            font-weight: 500;
            color: #323233;
        }

        .item-del {
            flex: none;
            font-size: 12px;
            color: #969799;
            cursor: pointer;

            &:hover {
                color: var(--tig-primary);
            }
        }
    }
}
</style>
