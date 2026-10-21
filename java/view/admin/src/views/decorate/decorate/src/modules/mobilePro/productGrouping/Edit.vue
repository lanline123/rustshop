<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品分组D') }}</h3>
            <div class="dec-edit-desc">{{ $t('个性轮播展示，可自动切换，效果突出醒目，默认一行2个') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'product'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('内容设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品来源') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productSource">
                        <el-radio-button :value="'products'">{{ $t('手动选择') }}</el-radio-button>
                        <el-radio-button :value="'productGroup'">{{ $t('商品分组') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div v-if="module.productSource == 'products'">
                <GroupListEdit v-model="module.productGroups" :isMultiple="true" :title="$t('添加一组')"></GroupListEdit>
            </div>
            <div v-if="module.productSource == 'productGroup'">
                <ProductGroupSelect v-model:productGroups="module.groupList" :isMultiple="true" type="productGroup" :showCustomImage="true"></ProductGroupSelect>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('商品参数设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label"></div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productCardStyle">
                        <el-radio-button :value="1">{{ $t('无边白底') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('卡片投影') }}</el-radio-button>
                        <el-radio-button :value="3">{{ $t('描边白底') }}</el-radio-button>
                        <el-radio-button :value="4">{{ $t('无边透明') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('列表样式') }}</div>
                    <div class="value">{{ selectLabel.productListStyle[module.listStyle as ProductListStyleType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.listStyle">
                        <el-tooltip effect="light" placement="bottom" :content="$t('大图模式')" :show-after="100">
                            <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('一行两个')" :show-after="100">
                            <el-radio-button :value="2"><i class="ico-decorate icon-dec-small"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('一行三个')" :show-after="100">
                            <el-radio-button :value="3"><i class="ico-decorate icon-dec-three"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('一行四个')" :show-after="100">
                            <el-radio-button :value="4"><i class="ico-decorate icon-dec-cuberow1"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('一大两小')" :show-after="100">
                            <el-radio-button :value="5"><i class="ico-decorate icon-dec-hybrid"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片比例') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picScale">
                        <el-radio-button :value="'3:2'">3:2</el-radio-button>
                        <el-radio-button :value="'1:1'">1:1</el-radio-button>
                        <el-radio-button :value="'3:4'">3:4</el-radio-button>
                        <el-radio-button :value="'16:9'">16:9</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit>
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题对齐方式</div>
                    <div class="value">{{ selectLabel.textAlignment[module.textAlignment as TextAlignmentType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textAlignment">
                        <el-tooltip effect="light" placement="bottom" content="左对齐" :show-after="100">
                            <el-radio-button :value="'left'"><i class="iconfont-admin icon-alignLeft"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="居中对齐" :show-after="100">
                            <el-radio-button :value="'center'"><i class="iconfont-admin icon-center"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div> -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品横竖间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.cardMargin" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品内部边距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.cardPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品圆角半径') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.cardRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片底部圆角') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picBottomRadius">
                        <el-radio-button :value="1">{{ $t('圆角') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('直角') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品标题') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductName">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProductName === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题行数') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productNameRow">
                        <el-radio-button :value="1">{{ $t('一行') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('两行') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProductName === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题粗细') }}</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.productNameWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productNameWeight">
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
            <div class="dec-edit-group" v-if="module.showProductName === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题大小') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.productNameSize"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="16"
                            :min="12"
                        />
                    </div>
                </div>
            </div>

            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品描述</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductDesc">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div> -->

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品价格') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductPrice">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProductPrice == 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('价格文字粗细') }}</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.productPriceWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPriceWeight">
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
            <div class="dec-edit-group" v-if="module.showProductPrice == 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('价格文字大小') }}</div>
                    <div class="value">{{ selectLabel.bigTitleFontSize[module.productPriceSize as BigTitleFontSize] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPriceSize">
                        <el-tooltip effect="light" placement="bottom" :content="$t('小(12号)')" :show-after="100">
                            <el-radio-button :value="12"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 12px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('小(14号)')" :show-after="100">
                            <el-radio-button :value="14"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 12px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('中(16号)')" :show-after="100">
                            <el-radio-button :value="16"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 14px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('大(18号)')" :show-after="100">
                            <el-radio-button :value="18"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 16px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProductPrice === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('价格标签图(宽不限,高20)') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-pic-group dec-picSingle-group">
                        <PicSelect v-model:photo="module.productPricePic"> </PicSelect>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProductPrice === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('价格上距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.productPriceMarginTop"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="30"
                            :min="5"
                        />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品划线价') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.crossedOutPrice">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <BuyBtnEdit v-model="module.buyBtnStyle" :show-wrap-option="true"></BuyBtnEdit>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('商品颜色设置') }}</div>
            </div>
            <div v-if="module.productColor">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('信息区顶部内距') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.productColor.infoTopPadding"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="30"
                                :min="4"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('信息区底部内距') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.productColor.infoBottomPadding"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="30"
                                :min="4"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('信息区左右内距') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.productColor.infoLeftRightPadding"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="20"
                                :min="0"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('商品背景颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.productColor.backgroundColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.productColor.backgroundColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('商品标题颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.productColor.titleColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.productColor.titleColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('商品价格颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.productColor.priceColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.productColor.priceColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('商品划线价颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.productColor.crossedOutPriceColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.productColor.crossedOutPriceColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('查看更多背景颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.productColor.moreBtnBgColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.productColor.moreBtnBgColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('查看更多边框颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.productColor.moreBtnBorderColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.productColor.moreBtnBorderColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('查看更多文字颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.productColor.moreBtnTextColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.productColor.moreBtnTextColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('分组标题设置') }}</div>
            </div>
            <div v-if="module.groupTitle">
                <!-- <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题是否吸顶</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.groupTitle.effectType">
                            <el-radio-button :value="'ceiling'">吸顶</el-radio-button>
                            <el-radio-button :value="'default'">不吸顶</el-radio-button>
                        </el-radio-group>
                    </div>
                </div> -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('分组标题间距') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.groupTitle.marginLeftRight"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="20"
                                :min="0"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('分组大标题') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.groupTitle.showTitle">
                            <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                            <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('大标题大小') }}</div>
                        <div class="value">{{ selectLabel.bigTitleFontSize[module.groupTitle.bigTitleFontSize as BigTitleFontSize] }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.groupTitle.bigTitleFontSize">
                            <el-tooltip effect="light" placement="bottom" :content="$t('小(14号)')" :show-after="100">
                                <el-radio-button :value="14"
                                    ><i class="iconfont-admin icon-zitiyanse" style="font-size: 12px; font-weight: bold"></i
                                ></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" :content="$t('中(16号)')" :show-after="100">
                                <el-radio-button :value="16"
                                    ><i class="iconfont-admin icon-zitiyanse" style="font-size: 14px; font-weight: bold"></i
                                ></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" :content="$t('大(18号)')" :show-after="100">
                                <el-radio-button :value="18"
                                    ><i class="iconfont-admin icon-zitiyanse" style="font-size: 16px; font-weight: bold"></i
                                ></el-radio-button>
                            </el-tooltip>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('大标题默认颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.groupTitle.titleColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.groupTitle.titleColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('大标题选中颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.groupTitle.activeTitleColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.groupTitle.activeTitleColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('分组副标题') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.groupTitle.showSubTitle">
                            <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                            <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showSubTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('副标题大小') }}</div>
                        <div class="value">{{ selectLabel.titleFontSize[module.groupTitle.titleFontSize as TitleFontSize] }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.groupTitle.titleFontSize">
                            <el-tooltip effect="light" placement="bottom" :content="$t('小(12号)')" :show-after="100">
                                <el-radio-button :value="12"
                                    ><i class="iconfont-admin icon-zitiyanse" style="font-size: 12px; font-weight: bold"></i
                                ></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" :content="$t('中(14号)')" :show-after="100">
                                <el-radio-button :value="14"
                                    ><i class="iconfont-admin icon-zitiyanse" style="font-size: 14px; font-weight: bold"></i
                                ></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" :content="$t('大(16号)')" :show-after="100">
                                <el-radio-button :value="16"
                                    ><i class="iconfont-admin icon-zitiyanse" style="font-size: 16px; font-weight: bold"></i
                                ></el-radio-button>
                            </el-tooltip>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showSubTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('副标题默认颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.groupTitle.subTitleColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.groupTitle.subTitleColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showSubTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('副标题选中颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.groupTitle.activeSubTitleColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.groupTitle.activeSubTitleColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showSubTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('副标题渐变方向') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.groupTitle.subTitleGradientType">
                            <el-tooltip effect="light" placement="bottom" :content="$t('上下')" :show-after="100">
                                <el-radio-button :value="'upDown'">{{ $t('上下') }}</el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" :content="$t('左右')" :show-after="100">
                                <el-radio-button :value="'leftRight'">{{ $t('左右') }}</el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" :content="$t('斜向')" :show-after="100">
                                <el-radio-button :value="'diagonal'">{{ $t('斜向') }}</el-radio-button>
                            </el-tooltip>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showSubTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('副标题渐变颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <div class="flex flex-align-center">
                                    <div><SelectColor v-model:color="module.groupTitle.gradientColorA"></SelectColor></div>
                                    <div style="margin: 0 10px">-</div>
                                    <div><SelectColor v-model:color="module.groupTitle.gradientColorB"></SelectColor></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.groupTitle.showSubTitle == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('副标题圆角半径') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.groupTitle.subTitleRadius"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="15"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('标题分割线颜色') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.groupTitle.splitLineColor = ''">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.groupTitle.splitLineColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">吸顶标题背景颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.groupTitle.ceilingBgColor = ''">重置</a>
                                <SelectColor v-model:color="module.groupTitle.ceilingBgColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div> -->
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="productGrouping"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="productGrouping"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicSelect, ProductGroupSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref } from "vue";
import CommonImageFillEdit from "../../src/commonImageFill/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import BuyBtnEdit from "../../src/buyBtn/Edit.vue";
import GroupListEdit from "./src/GroupListEdit.vue";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
import {
    ModuleType,
    ModuleProductGroupingType,
    TitleFontBaold,
    TitleFontSize,
    ProductListStyleType,
    BigTitleFontSize
} from "@/types/decorate/decorate.d";
import { min } from "lodash-es";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('商品设置'), name: "product" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("product");
const module = defineModel<ModuleType & ModuleProductGroupingType>("module", { default: () => ({}) });
</script>
