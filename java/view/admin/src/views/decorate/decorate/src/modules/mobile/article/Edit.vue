<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('文章列表') }}</h3>
            <div class="dec-edit-desc">{{ $t('支持展示文章') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'product'">
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('选择风格') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.style">
                        <el-radio-button :value="1">{{ $t('大图展示') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('两列展示(纵向)') }}</el-radio-button>
                        <el-radio-button :value="3">{{ $t('两列展示(横向)') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group pic-change-desc">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文章分类') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-select-group" need-refresh="true">
                        <SelectArticleCategory v-model="module.articleCategoryId"></SelectArticleCategory>
                    </div>
                </div>
            </div>-
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文章数量') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input type="number" v-model="module.articleNum" :placeholder="$t('请输入文章数量')"></el-input>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文章日期时间') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showTime">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文章浏览量') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showPageView">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文章点赞数量') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showLike">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片圆角半径') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="100" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题粗细') }}</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.articleNameWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.articleNameWeight">
                        <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                            <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                            <el-radio-button :value="700"
                                ><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('颜色设置') }}</div>
            </div>
            <div v-if="module.moduleColor">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('文章标题颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.articleNameColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.moduleColor.articleNameColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('时间日期颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.articleTimeColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.moduleColor.articleTimeColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('浏览量颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.articleViewColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.moduleColor.articleViewColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('点赞颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.articleLikeColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.moduleColor.articleLikeColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('统计数量颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.statisticColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.moduleColor.statisticColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="article"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="article"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import SelectArticleCategory from "@/components/select/src/SelectArticleCategory.vue";
import { SelectColor } from "@/components/select";
import Tabs from "@/components/tabs/Index.vue";
import { ref, watch } from "vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { selectLabel, ModuleTheme } from "@/views/decorate/decorate/src/modules/editIndex";
import { ModuleType, ModuleArticleType, TitleFontBaold } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('文章设置'), name: "product" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("product");
const module = defineModel<ModuleType & ModuleArticleType>("module", { default: () => ({}) });
</script>
