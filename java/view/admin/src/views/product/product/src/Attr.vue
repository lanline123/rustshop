<template>
    <div class="attr-form">
        <el-form-item :label="$t('属性模板')" prop="type" class="inner-item">
            <el-space>
                <el-select v-model="attrTplId" :placeholder="$t('请选择')" filterable clearable :disabled="disabled" @change="importAttrTpl" style="width: 200px">
                    <el-option v-for="item in attrTplList as any[]" :label="item.tplName" :key="item.tplId" :value="item.tplId" />
                </el-select>
            </el-space>
        </el-form-item>
        <el-form-item prop="brandLogo" :label="$t('商品属性')">
            <div class="attr-list">
                <div class="attr-warp" v-for="(item, index1) in attrList.normal">
                    <div v-if="!disabled" class="btn-remove" @click="removeAttrList('normal', index1)">
                        <i class="iconfont icon-cha1"></i>
                    </div>
                    <div class="attr-warp-title">
                        <div class="title-box flex align-center">
                            <div class="label">{{ $t('属性名：') }}</div>
                            <div style="width: 300px">
                                <BusinessData
                                    v-model:modelValue="item.attrName"
                                    width="280px"
                                    :disabled="disabled || item.fromTemplate"
                                    :dataType="8"
                                    type="complete"
                                    :placeholder="$t('请输入属性名')"
                                    :completeTip="getAttrNameTips('normal')"
                                    @input="(newValue) => debounceAttrNameInput(newValue, 'normal', index1)"
                                ></BusinessData>
                                <!--                                <div v-if="!disabled && !attrTplId" class="btn-remove3" @click="removeAttrList('normal', index1)">-->
                                <!--                                    <i class="iconfont icon-cha1"></i>-->
                                <!--                                </div>-->
                            </div>
                        </div>
                    </div>
                    <div class="attr-warp-box">
                        <!--                        <el-space class="attr-row" v-for="(attr, idx) in item.attrList">-->
                        <!--                            <BusinessData v-model:modelValue="attr.attrValue" width="310px" :disabled="disabled" :dataType="8"></BusinessData>-->
                        <!--                        </el-space>-->
                        <draggable
                            class="attr-row-box"
                            item-key="id"
                            :list="item.attrList"
                            ghost-class="sortable-ghost"
                            chosen-class="toolChosenClass"
                            animation="300"
                            @start=""
                            :group="{ name: 'advanced', pull: false, put: true }"
                            handle=".drag-handle"
                        >
                            <template #item="{ element, index }">
                                <div class="draggable-item">
                                    <div class="attr-row">
                                        <div style="width: 250px; display: flex; align-items: center">
                                            <div v-if="!disabled" class="drag-handle">⋮⋮</div>
                                            <BusinessData
                                                v-model:modelValue="element.attrValue"
                                                :disabled="disabled"
                                                :dataType="8"
                                                type="complete"
                                                :completeTip="getAttrValueTips('normal', item.attrName, element.attrValues)"
                                                :placeholder="$t('请输入属性值')"
                                            ></BusinessData>
                                        </div>
                                        <div v-if="item.attrList.length > 1 && !disabled" class="btn-remove2" @click="removeAttr('normal', index1, index)">
                                            <i class="iconfont icon-cha1"></i>
                                        </div>
                                    </div>
                                    <el-button
                                        v-if="index === item.attrList.length - 1"
                                        :disabled="disabled"
                                        @click="addAttr('normal', index1)"
                                        link
                                        type="primary"
                                        class="ml10"
                                        style="margin-top: 8px"
                                        ><i class="btn-ico">+</i>{{ $t('增加属性值') }}</el-button
                                    >
                                </div>
                            </template>
                        </draggable>
                    </div>
                </div>
                <div class="attr-warp-title">
                    <el-button :disabled="disabled" @click="addAttrList('normal', '添加商品属性')"><i class="btn-ico">+</i>{{ $t('添加商品属性') }}</el-button>
                </div>
            </div>
        </el-form-item>

        <el-form-item class="inner-item" :label="$t('销售规格')" prop="skuType">
            <el-radio-group v-model="skuType" @change="changeSkuType" :disabled="disabled" style="width: 100%">
                <el-radio :value="1">{{ $t('统一规格') }}</el-radio>
                <!-- 实体礼品卡(7)、电子礼品卡(8)、卡密商品(3)不显示多规格 -->
                <el-radio :value="2" v-if="![3, 7, 8].includes(formState?.productType || 1)">{{ $t('多规格') }}</el-radio>
            </el-radio-group>
        </el-form-item>

        <el-form-item prop="brandLogo" v-if="skuType === 2" :label="$t('商品规格')" id="spec-image-section">
            <div class="attr-list">
                <div class="attr-warp" v-for="(item, index1) in attrList.spe">
                    <div v-if="!disabled" class="btn-remove" @click="removeAttrList('spe', index1)">
                        <i class="iconfont icon-cha1"></i>
                    </div>
                    <div class="attr-warp-title">
                        <div class="flex align-center">
                            <div class="label"><em class="red">*</em>{{ $t('规格名：') }}</div>

                            <div style="width: 300px">
                                <BusinessData
                                    v-model:modelValue="item.attrName"
                                    :disabled="disabled || item.fromTemplate"
                                    :dataType="8"
                                    type="complete"
                                    :completeTip="getAttrNameTips('spe')"
                                    :placeholder="$t('请输入规格项，如颜色、尺码、大小')"
                                    @blur="onSpecAttrNameBlur"
                                ></BusinessData>
                            </div>
                            <!-- specImageMode=2 时每组规格各自显示勾选框；否则仅第一组显示 -->
                            <div v-if="allSpecImageEnabled || index1 === 0" class="spec-image-checkbox">
                                <el-checkbox v-model="item.specImageRequired" :true-label="1" :false-label="0" :disabled="disabled" @change="onSpecImageToggle">
                                    {{ $t('添加规格图片') }}
                                </el-checkbox>
                                <el-tooltip effect="light" placement="top">
                                    <template #content>
                                        <div style="width: 200px; padding: 5px 10px">
                                            {{
                                                allSpecImageEnabled
                                                    ? $t('支持为每组规格设置规格图片，建议尺寸800 x 800，大小{imageUploadMaxSize}MB以下', { imageUploadMaxSize: configStore.imageUploadMaxSize })
                                                    : $t('仅支持为一种规格设置规格图片建议尺寸800 x 800，大小{imageUploadMaxSize}MB以下', { imageUploadMaxSize: configStore.imageUploadMaxSize })
                                            }}
                                        </div>
                                    </template>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                        </div>
                    </div>
                    <div class="attr-warp-box">
                        <draggable
                            class="attr-row-box"
                            item-key="id"
                            :list="item.attrList"
                            ghost-class="sortable-ghost"
                            chosen-class="toolChosenClass"
                            animation="300"
                            @start=""
                            @end="dragEnd"
                            :group="{ name: 'advanced', pull: false, put: true }"
                            handle=".drag-handle"
                        >
                            <template #item="{ element, index }">
                                <div class="draggable-item">
                                    <div class="attr-row">
                                        <div style="width: 250px">
                                            <BusinessData
                                                v-model:modelValue="element.attrValue"
                                                :disabled="disabled"
                                                :dataType="8"
                                                type="complete"
                                                :completeTip="getAttrValueTips('spe', item.attrName, element.attrValues)"
                                                :placeholder="$t('请输入规格值，如：白色')"
                                                @blur="dragEnd"
                                            ></BusinessData>
                                        </div>
                                        <div v-if="!disabled" class="drag-handle">
                                            <el-icon><Rank /></el-icon>
                                        </div>
                                        <div v-if="item.attrList.length > 1 && !disabled" class="btn-remove2" @click="removeAttr('spe', index1, index)">
                                            <i class="iconfont icon-cha1"></i>
                                        </div>
                                        <!-- 规格图片区域：可编辑范围内且已勾选时可上传；不在可编辑范围但已有图片时只读回显（如切回仅第一组后的其他组） -->
                                        <div
                                            class="upload-img-wrap"
                                            v-if="
                                                (canGroupUploadImage(index1) && item.specImageRequired) ||
                                                (!canGroupUploadImage(index1) && element.attrPicThumb)
                                            "
                                        >
                                            <div class="arrow"></div>
                                            <div
                                                class="pic-select-con"
                                                :class="{
                                                    'pic-required-error': canGroupUploadImage(index1) && item.specImageRequired && !element.attrPicThumb
                                                }"
                                            >
                                                <div class="pic-select" v-if="canGroupUploadImage(index1) && !disabled">
                                                    <DialogForm type="gallery" @okCallback="addPic" :data="{ type: 'spe', index: index1, idx: index }">
                                                        <div class="pic-select-img">
                                                            <BusinessImg
                                                                v-if="element.attrPicThumb"
                                                                v-model:modelValue="element.attrPic"
                                                                :picThumb="element.attrPicThumb"
                                                                :dataId="element.attrPicId"
                                                                :dataType="8"
                                                            >
                                                                <img :src="imageFormat(element.attrPicThumb)" />
                                                            </BusinessImg>
                                                        </div>
                                                    </DialogForm>
                                                    <i
                                                        @click="removePic('spe', index1, index)"
                                                        v-if="element.attrPicThumb"
                                                        class="pic-select-del iconfont icon-cha"
                                                        :class="{ left: isOverseas() }"
                                                    ></i>
                                                </div>
                                                <!-- 只读回显：不在可编辑范围或禁用态，仅展示已上传图片，无上传/删除入口 -->
                                                <div class="pic-select" v-else-if="element.attrPicThumb">
                                                    <div class="pic-select-img">
                                                        <img :src="imageFormat(element.attrPicThumb)" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <el-button
                                        v-if="index === item.attrList.length - 1"
                                        :disabled="disabled"
                                        @click="addAttr('spe', index1)"
                                        link
                                        type="primary"
                                        class="ml10"
                                        style="margin-top: 8px"
                                        ><i class="btn-ico">+</i>{{ $t('增加规格值') }}</el-button
                                    >
                                </div>
                            </template>
                        </draggable>
                    </div>
                    <div v-if="index1 < 1" class="extra mt10" style="text-align: center">
                        <template v-if="allSpecImageEnabled">支持为每组规格设置规格图片，买家选择不同规格会看到对应规格图片，建议尺寸：800 x 800像素</template>
                        <template v-else>{{ $t('仅支持为第一组规格设置规格图片，买家选择不同规格会看到对应规格图片，建议尺寸：800 x 800像素') }}</template>
                    </div>
                </div>
                <div class="attr-warp-title">
                    <el-button :disabled="disabled" @click="addAttrSpeList('spe')"><i class="btn-ico">+</i>{{ $t('添加商品规格') }}</el-button>
                </div>
                <div v-if="specNameErrorText" class="red mt10">{{ specNameErrorText }}</div>
                <div v-if="specImageErrorText" class="red mt10">{{ specImageErrorText }}</div>
            </div>
        </el-form-item>
        <el-form ref="productFormRef" :model="productList" label-width="100px" name="form_in_modal">
            <el-form-item v-if="(skuType === 2 && productForm.length > 0) || (skuType === 1 && productList.length > 0)" prop="productList">
                <template #label><span style="color: var(--el-color-danger); margin-right: 4px">*</span>{{ $t('规格明细') }}</template>
                <div class="lyecs-form-table">
                    <div v-if="skuType === 2" class="sku-status-toolbar">
                        <el-dropdown v-if="!disabled" trigger="click" @command="handleSkuBatchCommand">
                            <el-button>
                                {{ $t('批量操作') }}
                                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item command="enable" :disabled="selectedEnableTargets.length === 0">{{ $t('批量启用') }}</el-dropdown-item>
                                    <el-dropdown-item command="disable" :disabled="selectedDisableTargets.length === 0">{{ $t('批量禁用') }}</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                        <el-select v-model="skuStatusFilter" class="sku-status-filter" :teleported="false">
                            <el-option value="all" :label="$t('全部规格')" />
                            <el-option value="enabled" :label="$t('已启用')" />
                            <el-option value="disabled" :label="$t('已禁用')" />
                        </el-select>
                    </div>
                    <el-table :data="displaySkuList" v-loading="skuFormLoading" style="width: 100%">
                        <el-table-column v-if="skuType === 2 && !disabled" width="48" fixed="left" align="center">
                            <template #header>
                                <el-checkbox :model-value="allPageSelected" :indeterminate="somePageSelected" :disabled="disabled" @change="toggleAllPageSkus" />
                            </template>
                            <template #default="{ row }">
                                <el-checkbox :model-value="isSkuSelected(row)" :disabled="disabled" @change="(checked) => toggleSkuSelection(row, checked)" />
                            </template>
                        </el-table-column>
                        <!-- 多规格时显示动态规格列 -->
                        <el-table-column
                            fixed="left"
                            v-if="skuType === 2"
                            v-for="attr in productForm[0].attrs"
                            prop="attrs"
                            :label="attr.attrName"
                            align="center"
                        >
                            <template #default="{ row }">
                                <span :class="{ 'sku-spec-value--disabled': isSkuRowDisabled(row) }">{{ getAttrValue(row.attrs, attr.attrName) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuPrice" :label="$t('一口价')" width="140">
                            <template #header>
                                <el-tooltip effect="light" placement="top">
                                    <template #content>
                                        <div style="width: 100px; padding: 5px 10px">{{ $t('一口价会覆盖除属性价格外的所有价格，慎重设置') }}</div>
                                    </template>
                                    <PopForm
                                        v-if="!disabled || (getShopType() === 2 && (storeSettingInfo.storeAssignProductPrice === 1 || shopId))"
                                        :isDirect="true"
                                        :isHover="false"
                                        :max="100"
                                        type="decimal"
                                        v-model:org-value="batchInput.skuPrice"
                                        @callback="onBatchClick"
                                        :label="$t('一口价')"
                                    >
                                        <div class="flex flex-align-center flex-justify-center">
                                            <em class="red">*</em>
                                            <div>{{ $t('一口价') }}</div>
                                            <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                        </div>
                                    </PopForm>
                                    <div v-else class="flex flex-align-center flex-justify-center">
                                        <em class="red">*</em>
                                        <div>{{ $t('一口价') }}</div>
                                        <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                    </div>
                                </el-tooltip>
                            </template>
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`productList.${rowIndex(row)}.skuPrice`" :rules="[{ required: true, validator: validatePrice }]">
                                    <PriceInput
                                        v-model:modelValue="row.skuPrice"
                                        :disabled="
                                            isSkuRowDisabled(row) ||
                                            (getShopType() !== 2 && disabled) ||
                                            (getShopType() === 2 && storeSettingInfo.storeAssignProductPrice === 0 && !shopId)
                                        "
                                        @update:modelValue="(val) => handleSkuPriceChange(row, val)"
                                    ></PriceInput>
                                </el-form-item>
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuStock" :label="$t('库存')" width="120" v-if="![3, 5, 6].includes(formState.productType as number)">
                            <template #header>
                                <PopForm
                                    v-if="stockModifiable && (!disabled || (getShopType() === 2 && storeSettingInfo.storeUseSoloProductStock === 1) || shopId)"
                                    :isDirect="true"
                                    :isHover="false"
                                    :max="999999"
                                    v-model:org-value="batchInput.skuStock"
                                    @callback="onBatchClick"
                                    :label="$t('库存')"
                                >
                                    <div class="flex flex-align-center flex-justify-center"><em class="red">*</em> {{ $t('库存') }}</div>
                                </PopForm>
                                <div v-else class="flex flex-align-center flex-justify-center">
                                    <em class="red">*</em> {{ $t('库存') }} <el-tooltip v-if="stockLockedByConfig" effect="light" placement="top">
                                        <template #content>
                                            <div style="width: 240px; padding: 5px 10px"> {{ $t('商品编辑页库存字段已锁定，库存变更请至库存管理菜单下的库存调整功能调整。') }} </div>
                                        </template>
                                        <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                </div>
                            </template>
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`productList.${rowIndex(row)}.skuStock`" :rules="[{ required: true, validator: validateStock }]">
                                    <TigInput
                                        type="integer"
                                        v-model="row.skuStock"
                                        :disabled="
                                            isSkuRowDisabled(row) ||
                                            !stockModifiable ||
                                            (getShopType() !== 2 && disabled) ||
                                            (getShopType() === 2 && storeSettingInfo.storeUseSoloProductStock === 0 && !shopId)
                                        "
                                        :min="0"
                                        :max="999999"
                                    />
                                </el-form-item>
                            </template>
                        </el-table-column>
                        <!-- <el-table-column prop="skuUnit" label="单位" width="120" fixed="right">
                            <template #header>
                                <PopForm
                                    type="select"
                                    :options="unitOptions"
                                    v-if="!disabled || (getShopType() === 2 && storeSettingInfo.storeUseSoloProductStock === 1) || shopId"
                                    :isDirect="true"
                                    :isHover="false"
                                    :max="100"
                                    v-model:org-value="batchInput.skuUnit"
                                    @callback="onBatchClick"
                                    label="单位"
                                >
                                    <div class="flex flex-align-center flex-justify-center"><em class="red">*</em> 单位</div>
                                </PopForm>
                                <div v-else class="flex flex-align-center flex-justify-center"><em class="red">*</em> 单位</div>
                            </template>
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`productList.${$index}.skuUnit`">
                                    <el-select v-model="row.skuUnit" style="width: 100px">
                                        <el-option v-for="item in unitOptions" :key="item.value" :label="item.label" :value="item.value" />
                                    </el-select>
                                </el-form-item>
                            </template>
                        </el-table-column> -->
                        <el-table-column prop="marketPrice" :label="$t('市场价')" width="120">
                            <template #header>
                                <PopForm
                                    v-if="!disabled || (getShopType() === 2 && storeSettingInfo.storeUseSoloProductStock === 1) || shopId"
                                    :isDirect="true"
                                    :isHover="false"
                                    :max="100"
                                    v-model:org-value="batchInput.marketPrice"
                                    @callback="onBatchClick"
                                    :label="$t('市场价')"
                                >
                                    <div class="flex flex-align-center flex-justify-center">{{ $t('市场价') }}</div>
                                </PopForm>
                                <div v-else class="flex flex-align-center flex-justify-center">{{ $t('市场价') }}</div>
                            </template>
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`productList.${rowIndex(row)}.marketPrice`">
                                    <PriceInput
                                        v-model:modelValue="row.marketPrice"
                                        :disabled="
                                            isSkuRowDisabled(row) ||
                                            (getShopType() !== 2 && disabled) ||
                                            (getShopType() === 2 && storeSettingInfo.storeUseSoloProductStock === 0 && !shopId)
                                        "
                                    ></PriceInput>
                                </el-form-item>
                            </template>
                        </el-table-column>
                        <el-table-column prop="costPrice" :label="$t('成本价')" width="120">
                            <template #header>
                                <PopForm
                                    v-if="!disabled || (getShopType() === 2 && storeSettingInfo.storeUseSoloProductStock === 1) || shopId"
                                    :isDirect="true"
                                    :isHover="false"
                                    :max="100"
                                    v-model:org-value="batchInput.costPrice"
                                    @callback="onBatchClick"
                                    :label="$t('成本价')"
                                >
                                    <div class="flex flex-align-center flex-justify-center">{{ $t('成本价') }}</div>
                                </PopForm>
                                <div v-else class="flex flex-align-center flex-justify-center">{{ $t('成本价') }}</div>
                            </template>
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`productList.${rowIndex(row)}.costPrice`">
                                    <PriceInput
                                        v-model:modelValue="row.costPrice"
                                        :disabled="
                                            isSkuRowDisabled(row) ||
                                            (getShopType() !== 2 && disabled) ||
                                            (getShopType() === 2 && storeSettingInfo.storeUseSoloProductStock === 0 && !shopId)
                                        "
                                    ></PriceInput>
                                </el-form-item>
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuSn" :label="$t('规格编码')" min-width="120">
                            <template #header>
                                <PopForm
                                    v-if="!disabled"
                                    :isDirect="true"
                                    :isHover="false"
                                    :max="100"
                                    v-model:org-value="batchInput.skuSn"
                                    @callback="onBatchClick"
                                    :label="$t('规格编码')"
                                >
                                    <div class="flex flex-align-center flex-justify-center">
                                        <div>{{ $t('规格编码') }}</div>
                                    </div>
                                </PopForm>
                                <div v-else class="flex flex-align-center flex-justify-center">
                                    <div>{{ $t('规格编码') }}</div>
                                </div>
                            </template>
                            <template #default="{ row, $index }">
                                <TigInput v-model="row.skuSn" :disabled="disabled || isSkuRowDisabled(row)" />
                            </template>
                        </el-table-column>
                        <el-table-column prop="skuTsn" :label="$t('规格条码')" min-width="120">
                            <template #header>
                                <PopForm
                                    v-if="!disabled"
                                    :isDirect="true"
                                    :isHover="false"
                                    :max="100"
                                    v-model:org-value="batchInput.skuTsn"
                                    @callback="onBatchClick"
                                    :label="$t('规格条码')"
                                >
                                    <div class="flex flex-align-center flex-justify-center">{{ $t('规格条码') }}</div>
                                </PopForm>
                                <div v-else class="flex flex-align-center flex-justify-center">{{ $t('规格条码') }}</div>
                            </template>
                            <template #default="{ row, $index }">
                                <TigInput v-model="row.skuTsn" :disabled="disabled || isSkuRowDisabled(row)" />
                            </template>
                        </el-table-column>
                        <el-table-column v-if="skuType === 2 && !disabled" :label="$t('操作')" width="80" fixed="right" align="center">
                            <template #default="{ row }">
                                <el-button link type="primary" :disabled="disabled" @click="changeSingleSkuStatus(row)">
                                    {{ isSkuEnabled(row) ? $t('禁用') : $t('启用') }}
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
                <div v-if="paginationEnabled" class="sku-pagination">
                    <Pagination v-model:page="skuCurrentPage" v-model:size="skuPageSize" :total="skuFilteredTotal" :page-sizes="[...SKU_PAGE_SIZES]" />
                </div>
                <div class="red">{{ skuErrorText }}</div>
            </el-form-item>
        </el-form>
        <el-form-item label="">
            <template #label>
                <el-tooltip class="box-item" effect="light" placement="bottom" :show-after="300">
                    <template #content>
                        <div style="width: 300px; padding: 5px 10px"> {{ $t('附加规格不仅是商品的详细描述，还能为用户提供额外的服务保障，如保修、 安装或技术支持等，这些服务可能涉及附加服务费。通过清晰地列出这些附加规格， 您可以帮助用户更好地理解商品的价值，并做出更明智的购买决策，从而提升用户体验和销售转化率。') }} </div>
                    </template>
                    <div class="flex flex-align-center">
                        <div>{{ $t('附加服务') }}</div>
                        <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                    </div>
                </el-tooltip>
            </template>
            <div class="attr-list other-attr">
                <div class="attr-warp-title mb10" v-for="(item, index) in attrList.extra">
                    <div class="btn-remove5" v-if="!disabled" @click="removeAttrList('extra', index)">
                        <i class="iconfont icon-cha1"></i>
                    </div>
                    <div>
                        <div class="title flex">
                            <div class="label">{{ $t('附加服务名：') }}</div>
                            <div class="value" style="width: 250px">
                                <BusinessData
                                    v-model:modelValue="item.attrName"
                                    width="260px"
                                    :disabled="disabled || item.fromTemplate"
                                    :dataType="8"
                                    type="complete"
                                    :placeholder="$t('请输入附加服务名')"
                                    :completeTip="getAttrNameTips('extra')"
                                    @input="(newValue) => debounceAttrNameInput(newValue, 'extra', index)"
                                ></BusinessData>
                            </div>
                        </div>
                        <div class="attr-extra-value">
                            <el-space class="attr-row mb10" v-for="(attr, idx) in item.attrList" :size="16">
                                <div style="width: 185px">
                                    <BusinessData
                                        v-model:modelValue="attr.attrValue"
                                        type="complete"
                                        :completeTip="getAttrValueTips('extra', item.attrName, attr.attrValues)"
                                        :disabled="disabled"
                                        :placeholder="$t('请输入附加服务值')"
                                        :dataType="8"
                                    ></BusinessData>
                                </div>
                                <el-space :size="0"> {{ $t('附加金额：') }} <PriceInput :disabled="disabled" v-model:modelValue="attr.attrPrice" width="150px"></PriceInput>
                                </el-space>
                                <div v-if="item.attrList.length > 1 && !disabled" class="btn-remove4" @click="removeAttr('extra', index, idx)">
                                    <i class="iconfont icon-cha1"></i>
                                </div>
                            </el-space>
                        </div>
                    </div>
                    <el-space class="attr-row mt10" style="margin-left: 85px">
                        <el-button :disabled="disabled" @click="addAttr('extra', index)"><i class="btn-ico">+</i>{{ $t('增加附加服务') }}</el-button>
                    </el-space>
                </div>
                <div class="attr-warp-title">
                    <el-button :disabled="disabled" @click="addAttrList('extra', '添加商品附加服务')"><i class="btn-ico">+</i>{{ $t('添加商品附加服务') }}</el-button>
                </div>
            </div>
        </el-form-item>
        <!-- <el-dialog v-model="visible" :title="dialogTitle" width="450">
            <div style="padding-top: 20px">
                <el-form ref="formRef" label-width="auto" :model="newAttrState">
                    <el-form-item prop="name" label="属性名称">
                        <TigInput width="100%" v-model="newAttrState.name" />
                        <div class="extra">提示：添加新的商品规格属性类型，会清空规格库存中的所有设置</div>
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="visible = false">关闭</el-button>
                    <el-button type="primary" @click="onOk(formRef)"> 确定 </el-button>
                </div>
            </template>
        </el-dialog> -->
    </div>
</template>

<script setup lang="ts">
import BusinessImg from "@/components/multilingual/BusinessImg.vue";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { PopForm } from "@/components/pop-form";
import { ArrowDown, Rank, QuestionFilled } from "@element-plus/icons-vue";
import draggable from "vuedraggable";
import { ref, onMounted, watch, computed, nextTick } from "vue";
import { Modal } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import request from "@/utils/request";
import { imageFormat } from "@/utils/format";
import PriceInput from "@/views/product/product/src/PriceInput.vue";
import { getShopType } from "@/utils/storage";
import { isOverseas } from "@/utils/version";
import { ProductFormState } from "@/types/product/product.d";
import { getConfigProduct } from "@/api/setting/config";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useZIndex } from "element-plus";
import { SkuPerfMode, resolveSkuPerfMode } from "@/utils/skuPerfMode";
import { buildProductForm, buildProductFormInWorker } from "@/utils/skuFormBuilder";
import {
    canDisableSkuTargets,
    filterSkusByStatus,
    getEnabledSkus,
    getSelectedSkuTargets,
    getSkuSelectionKey,
    isSkuEnabled,
    updateSkuSelection,
    type SkuStatusFilter,
    type SkuStatusTarget
} from "@/utils/productSkuStatus";
import { useSkuTablePagination, SKU_PAGE_SIZES } from "@/hooks/useSkuTablePagination";
import { Pagination } from "@/components/list";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { nextZIndex } = useZIndex();
const visible = ref(false);
const props = defineProps({
    attrChanged: { type: Boolean, default: false },
    disabled: { type: Boolean, default: false },
    shopId: { type: String, default: "" },
    action: { type: String, default: "add" },
    attrTplList: {
        type: Array
    },
    storeSettingInfo: {
        type: Object,
        default: {}
    }
});
const skuType = defineModel<number>("skuType", { default: 1 });
const formState = defineModel<ProductFormState>("formState", { default: {} });
// 使用 defineModel 处理 productList 和 attrList，确保响应式双向绑定
const productList = defineModel<any[]>("productList", { default: [] });
type AttrListState = { normal: any[]; spe: any[]; extra: any[] };
type AttrListKey = keyof AttrListState;
const attrList = defineModel<AttrListState>("attrList", { default: () => ({ normal: [], spe: [], extra: [] }) });
const productFormRef = ref();
const dialogTitle = ref("");
const skuErrorText = defineModel<string>("skuErrorText", { default: "" });
const specImageErrorText = ref<string>(""); // 规格图片验证错误提示
const specNameErrorText = ref<string>(""); // 规格名验证错误提示（多规格必填）
const emit = defineEmits(["update:attrChanged"]);
const attrTplId = defineModel<number | null>("attrTplId", { default: null });


// ==================== 库存开关联动 ====================
// productEditStockModify 已随 getAdminBase 全局配置下发，直接读取 store，无需单独调用接口
const configStore = useConfigStore();
/** 库存是否可修改（false 时库存输入只读、隐藏表头批量改库存图标）；新增商品不受开关约束 */
const stockModifiable = computed(() => props.action === "add" || configStore.productEditStockModify !== 0);
/** 商品库存是否被全局配置锁定（编辑页不可改，需走库存调整） */
const stockLockedByConfig = computed(() => props.action !== "add" && configStore.productEditStockModify === 0);

// ==================== 规格图片范围联动 ====================
// specImageMode 已随 getAdminBase 全局配置下发，直接读取 store，无需单独调用接口
/** 是否允许全部规格组上传图片（0=仅第一组；1=全部规格），为 true 时所有规格组的规格值都可上传图片 */
const allSpecImageEnabled = computed(() => configStore.specImageMode === 2);

// ==================== SKU 性能档位 ====================
/** 当前实际生效的性能档位（按 SKU 条数自动分档） */
const skuPerfMode = computed(() => resolveSkuPerfMode(productList.value?.length || 0));
/** 规格明细重算中（loading 遮罩） */
const skuFormLoading = ref(false);

// ==================== SKU 状态筛选与跨页选择 ====================
const skuStatusFilter = ref<SkuStatusFilter>("enabled");
const selectedSkuKeys = ref<Set<string>>(new Set());
const filteredSkuList = computed(() => filterSkusByStatus(productList.value, skuStatusFilter.value));
const selectedEnableTargets = computed(() => getSelectedSkuTargets(productList.value, selectedSkuKeys.value, "enable"));
const selectedDisableTargets = computed(() => getSelectedSkuTargets(productList.value, selectedSkuKeys.value, "disable"));
const isSkuRowDisabled = (row: any) => !isSkuEnabled(row);
const isSkuSelected = (row: any) => selectedSkuKeys.value.has(getSkuSelectionKey(row));
const toggleSkuSelection = (row: any, checked: boolean | string | number) => {
    if (props.disabled) return;
    selectedSkuKeys.value = updateSkuSelection(selectedSkuKeys.value, [row], Boolean(checked));
};

// ==================== SKU 分页（多规格且条数超阈值才启用；rowIndex 定位到完整 productList） ====================
const {
    currentPage: skuCurrentPage,
    pageSize: skuPageSize,
    paginationEnabled,
    pagedList: pagedSkuList,
    rowIndex,
    goToIndex,
    resetPage
} = useSkuTablePagination<any>(() => filteredSkuList.value, {
    fullList: () => productList.value || [],
    enabled: () => skuType.value === 2
});

/** 表格实际渲染的数据 */
const displaySkuList = computed(() => (paginationEnabled.value ? pagedSkuList.value : filteredSkuList.value));
const selectedPageCount = computed(() => pagedSkuList.value.filter((row) => selectedSkuKeys.value.has(getSkuSelectionKey(row))).length);
const allPageSelected = computed(() => pagedSkuList.value.length > 0 && selectedPageCount.value === pagedSkuList.value.length);
const somePageSelected = computed(() => selectedPageCount.value > 0 && !allPageSelected.value);
const toggleAllPageSkus = (checked: boolean | string | number) => {
    if (props.disabled) return;
    selectedSkuKeys.value = updateSkuSelection(selectedSkuKeys.value, pagedSkuList.value, Boolean(checked));
};

watch(skuStatusFilter, () => {
    selectedSkuKeys.value = new Set();
    resetPage();
});

watch(
    () => productList.value,
    (list) => {
        (list || []).forEach((row: any) => {
            if (row.isEnabled !== 0 && row.isEnabled !== 1) row.isEnabled = 1;
        });
        const validKeys = new Set((list || []).map(getSkuSelectionKey));
        selectedSkuKeys.value = new Set([...selectedSkuKeys.value].filter((key) => validKeys.has(key)));
    },
    { deep: true, immediate: true }
);

const applySkuStatus = (rows: any[], target: SkuStatusTarget) => {
    rows.forEach((row) => {
        row.isEnabled = target === "enable" ? 1 : 0;
        selectedSkuKeys.value.delete(getSkuSelectionKey(row));
    });
    selectedSkuKeys.value = new Set(selectedSkuKeys.value);
    message.success(target === "enable" ? t("已启用{count}个SKU，保存商品后生效", { count: rows.length }) : t("已禁用{count}个SKU，保存商品后生效", { count: rows.length }));
};

const confirmSkuStatusChange = (rows: any[], target: SkuStatusTarget, isBatch = true) => {
    if (props.disabled || rows.length === 0) return;
    if (target === "disable" && !canDisableSkuTargets(productList.value, rows)) {
        message.error(t("请至少启用一个SKU"));
        return;
    }
    Modal.confirm({
        zIndex: nextZIndex(),
        title: isBatch ? (target === "enable" ? t("确认启用已选择的{count}个SKU吗？", { count: rows.length }) : t("确认禁用已选择的{count}个SKU吗？", { count: rows.length })) : target === "enable" ? t("确认启用该SKU吗？") : t("确认禁用该SKU吗？"),
        okText: t("确定"),
        cancelText: t("取消"),
        onOk: () => applySkuStatus(rows, target)
    });
};
const changeSelectedSkuStatus = (target: SkuStatusTarget) => confirmSkuStatusChange(target === "enable" ? selectedEnableTargets.value : selectedDisableTargets.value, target);
const handleSkuBatchCommand = (command: string | number | object) => {
    if (command === "enable" || command === "disable") changeSelectedSkuStatus(command);
};
const changeSingleSkuStatus = (row: any) => confirmSkuStatusChange([row], isSkuEnabled(row) ? "disable" : "enable", false);
const showDisabledSkus = () => {
    skuStatusFilter.value = "disabled";
};

/**
 * 按性能档位重算 SKU 明细（productList）。
 * LEGACY: O(N²) find 匹配；INCREMENTAL: O(N) Map 匹配；WORKER: 子线程重算。
 */
const recomputeProductForm = async () => {
    if (!attrList.value.spe || attrList.value.spe.length === 0) {
        // 多规格但已无规格组时清空全部 SKU；单规格(skuType=1)时 spe 本就为空，仍保留统一规格行。
        if (skuType.value === 2) productList.value = [];
        return;
    }
    // 把「规格组名」同步到每个规格值行的 attrName，避免子行残留初始空串（addAttrList 时子行 attrName 为 ""）
    // 兼容旧版 getCombinations 内的副作用，同时保持 skuFormBuilder 纯函数不产生副作用
    (attrList.value.spe || []).forEach((group: any) => {
        if (group?.attrList?.length) {
            group.attrList.forEach((attr: any) => {
                if (attr.attrName !== group.attrName) attr.attrName = group.attrName;
            });
        }
    });
    const mode = skuPerfMode.value;
    const spePlain = JSON.parse(JSON.stringify(attrList.value.spe));
    try {
        if (mode === SkuPerfMode.WORKER) {
            skuFormLoading.value = true;
            const existingClone = JSON.parse(JSON.stringify(productList.value || []));
            productList.value = await buildProductFormInWorker(spePlain, existingClone);
        } else {
            const useMap = mode !== SkuPerfMode.LEGACY;
            productList.value = buildProductForm(spePlain, productList.value || [], { useMap });
        }
    } catch (e) {
        // Worker 不可用等异常 → 主线程兜底重算
        productList.value = buildProductForm(spePlain, productList.value || [], { useMap: true });
    } finally {
        skuFormLoading.value = false;
    }
};
const unitOptions = ref([
    { label: t('件'), value: t('件') },
    { label: t('个'), value: t('个') },
    { label: t('套'), value: t('套') },
    { label: t('箱'), value: t('箱') },
    { label: t('瓶'), value: t('瓶') },
    { label: t('包'), value: t('包') },
    { label: t('袋'), value: t('袋') }
]);
const typeId = {
    normal: 0,
    spe: 1,
    extra: 2
};
// const attrTplId = ref(null);

// 保存导入的模板数据用于提示
const importedTplData = ref<any>(null);
// 规格图片“添加规格图片”勾选状态按规格组维护在 item.specImageRequired 上（0/1）
/** 判断某规格组是否已存在规格图片 */
const groupHasImages = (group: any) => {
    const attrs = group?.attrList || [];
    return Array.isArray(attrs) && attrs.some((a: any) => a?.attrPicThumb || a?.attrPic);
};
/** 判断某规格组是否可上传规格图片（specImageMode=2 时全部组可上传，否则仅第一组） */
const canGroupUploadImage = (groupIndex: number) => allSpecImageEnabled.value || groupIndex === 0;

const marketPriceRate = ref<number>(0);

watch(
    () => formState.value.productType,
    (newVal: any) => {
        if ([3].includes(newVal)) {
            skuType.value = 1;
        }
    }
);

// 从导入的模板中提取属性名和属性值
const extractTplData = () => {
    const attrNamesMap: { [key: string]: Set<string> } = {
        normal: new Set(),
        spe: new Set(),
        extra: new Set()
    };
    const attrValuesMap: { [key: string]: { [attrName: string]: Set<string> } } = {
        normal: {},
        spe: {},
        extra: {}
    };

    // 优先使用导入的模板数据
    if (importedTplData.value) {
        const tplData = importedTplData.value;
        (["normal", "spe", "extra"] as const).forEach((type) => {
            if (tplData[type] && Array.isArray(tplData[type])) {
                tplData[type].forEach((item: any) => {
                    if (item.attrName) {
                        attrNamesMap[type].add(item.attrName);

                        // 初始化该属性名的值集合
                        if (!attrValuesMap[type][item.attrName]) {
                            attrValuesMap[type][item.attrName] = new Set();
                        }

                        // 添加属性值
                        if (item.attrValues && Array.isArray(item.attrValues)) {
                            item.attrValues.forEach((val: any) => {
                                const value = typeof val === "object" && val !== null && "value" in val ? val.value : val;
                                if (value) {
                                    attrValuesMap[type][item.attrName].add(value);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    return {
        attrNames: {
            normal: Array.from(attrNamesMap.normal),
            spe: Array.from(attrNamesMap.spe),
            extra: Array.from(attrNamesMap.extra)
        },
        attrValues: {
            normal: Object.fromEntries(Object.entries(attrValuesMap.normal).map(([key, set]) => [key, Array.from(set)])),
            spe: Object.fromEntries(Object.entries(attrValuesMap.spe).map(([key, set]) => [key, Array.from(set)])),
            extra: Object.fromEntries(Object.entries(attrValuesMap.extra).map(([key, set]) => [key, Array.from(set)]))
        }
    };
};

// 计算属性：获取模板数据
const tplData = computed(() => extractTplData());

// 获取属性名的提示列表
const getAttrNameTips = (type: "normal" | "spe" | "extra") => {
    return tplData.value.attrNames[type] || [];
};

// 获取属性值的提示列表（只返回对应属性名下的属性值）
const getAttrValueTips = (type: "normal" | "spe" | "extra", attrName: string, currentAttrValues?: any[]) => {
    const tips = new Set<string>();

    // 如果没有属性名，不返回任何提示
    if (!attrName || !attrName.trim()) {
        return [];
    }

    // 从导入的模板中获取该属性名对应的属性值
    if (tplData.value.attrValues[type] && tplData.value.attrValues[type][attrName]) {
        const templateValues = tplData.value.attrValues[type][attrName];
        if (Array.isArray(templateValues)) {
            templateValues.forEach((val: string) => {
                if (val && typeof val === "string") {
                    tips.add(val);
                }
            });
        }
    }

    // 合并当前已有的属性值（只取同属性名的）
    if (currentAttrValues && Array.isArray(currentAttrValues)) {
        currentAttrValues.forEach((val: any) => {
            const value = typeof val === "object" && val !== null && "value" in val ? val.value : val;
            if (value && typeof value === "string") {
                tips.add(value);
            }
        });
    }

    // 从当前属性列表中获取相同属性名的属性值作为提示
    if (attrList.value[type] && Array.isArray(attrList.value[type])) {
        attrList.value[type].forEach((attrItem: any) => {
            // 只获取相同属性名的属性值
            if (attrItem.attrName === attrName && attrItem.attrList && Array.isArray(attrItem.attrList)) {
                attrItem.attrList.forEach((attr: any) => {
                    if (attr.attrValue && typeof attr.attrValue === "string" && attr.attrValue.trim()) {
                        tips.add(attr.attrValue);
                    }
                });
            }
        });
    }

    return Array.from(tips);
};

// 确保所有属性项都有唯一id
const ensureAttrListHasIds = (list: any) => {
    (["normal", "spe", "extra"] as const).forEach((type) => {
        if (list[type] && Array.isArray(list[type])) {
            list[type].forEach((item: any) => {
                if (item.attrList && Array.isArray(item.attrList)) {
                    item.attrList.forEach((attr: any, index: number) => {
                        if (!attr.id) {
                            attr.id = Date.now() + Math.random() + index;
                        }
                    });
                }
            });
        }
    });
    return list;
};

// 为编辑模式下使用模板的属性添加 fromTemplate 标记
const addFromTemplateFlag = (list: any) => {
    (["normal", "spe", "extra"] as const).forEach((type) => {
        if (list[type] && Array.isArray(list[type])) {
            list[type].forEach((item: any) => {
                // 给整个属性项添加 fromTemplate 标记
                item.fromTemplate = true;
                if (item.attrList && Array.isArray(item.attrList)) {
                    item.attrList.forEach((attr: any) => {
                        // 给每个属性值也添加 fromTemplate 标记
                        attr.fromTemplate = true;
                    });
                }
            });
        }
    });
    return list;
};

// 移除 fromTemplate 标记，使属性名可以编辑
const removeFromTemplateFlag = (list: any) => {
    (["normal", "spe", "extra"] as const).forEach((type) => {
        if (list[type] && Array.isArray(list[type])) {
            list[type].forEach((item: any) => {
                // 移除整个属性项的 fromTemplate 标记
                delete item.fromTemplate;
                if (item.attrList && Array.isArray(item.attrList)) {
                    item.attrList.forEach((attr: any) => {
                        // 移除每个属性值的 fromTemplate 标记
                        delete attr.fromTemplate;
                    });
                }
            });
        }
    });
    return list;
};

const batchInput = ref<any>({});
// 标记是否已经处理过 fromTemplate
let fromTemplateProcessed = false;

// 获取 attrList 的深拷贝：确保有 id，如果是编辑模式且有模板ID，添加 fromTemplate 标记
const getAttrListClone = () => {
    const clonedData = JSON.parse(JSON.stringify(attrList.value));
    ensureAttrListHasIds(clonedData);
    // 如果是编辑模式且有模板ID，添加 fromTemplate 标记
    if (props.action !== "add" && attrTplId.value) {
        addFromTemplateFlag(clonedData);
        fromTemplateProcessed = true; // 标记已处理
    }
    return clonedData;
};
// attrList 和 productList 现在通过 defineModel 定义，无需手动 watch
// 保存原始的规格数据，用于切换规格类型时恢复
const savedSpeData = ref<any[]>([]);
const savedProductListData = ref<any[]>([]);
const getAttrValue = (attrs: any[], attrName: string) => {
    const attr = attrs.find((attr) => attr.attrName === attrName);
    return attr ? attr.attrValue : "";
};
const isImg = ref<any>(false);
const newAttrState = ref<{ name: string; type: AttrListKey }>({
    name: "",
    type: "normal"
});
let watchLoaded = false;
// 动态规格表单>

// attrList 和 productList 使用 defineModel，无需手动 watch 和 emit

// 监听 skuType 变化
watch(
    () => skuType.value,
    (newValue, oldValue) => {
        // 只在实际切换时执行，排除初始化（oldValue === undefined）
        if (newValue == 1 && oldValue !== undefined && oldValue !== newValue) {
            // 重置统一规格的库存
            formState.value.productStock = null;
            // 切换为统一规格时，先保存规格数据再清空
            if (attrList.value.spe && attrList.value.spe.length > 0) {
                savedSpeData.value = JSON.parse(JSON.stringify(attrList.value.spe));
            }
            if (productList.value && productList.value.length > 0 && productList.value[0].skuValue) {
                savedProductListData.value = JSON.parse(JSON.stringify(productList.value));
            }
            attrList.value.spe = [];
            attrList.value.extra = [];
            // 如果是新增或者列表为空，初始化一行空的规格明细
            if (props.action == "add" || productList.value.length === 0) {
                productList.value = [{}];
            }
        }
    },
    { deep: true, immediate: true }
);

// const validateUnit = (rule: any, value: any, callback: any) => {
//     const index = rule.field.split(".")[1];
//     let unit = productList.value[index].skuUnit;
//     if (!unit) {
//         callback(new Error("请选择规格单位"));
//         return;
//     } else {
//         callback();
//     }
// };
// SKU 行校验规则（唯一真相源）：el-form validator 与分页兜底扫描共用，避免漂移
const getSkuPriceError = (row: any): string => {
    const price = row?.skuPrice;
    if (price === "" || price === null || price === undefined) return t('请输入商品一口价');
    if (Number(price) < 0) return t('一口价不能为负数');
    return "";
};
const getSkuStockError = (row: any): string => {
    const stock = Number(row?.skuStock);
    if (isNaN(stock)) return t('请输入数字');
    if (props.action != "add" && stock < 0) return t('库存不能是负数');
    if (stock > 999999) return t('库存最多999999');
    if (!Number.isInteger(stock)) return t('商品库存必须是整数');
    return "";
};
const shouldValidateSkuStock = (): boolean => ![3, 5, 6].includes(Number(formState.value.productType));

const validateStock = (rule: any, value: any, callback: any) => {
    const index = rule.field.split(".")[1];
    const row = productList.value[index];
    if (row) row.skuStock = Number(row.skuStock);
    const err = getSkuStockError(row);
    callback(err ? new Error(err) : undefined);
};
const validatePrice = (rule: any, value: any, callback: any) => {
    const index = rule.field.split(".")[1];
    const err = getSkuPriceError(productList.value[index]);
    callback(err ? new Error(err) : undefined);
};
// 验证规格图片：逐组校验“可上传且已勾选添加规格图片”的规格组，其规格值必须都已上传图片
const validateSpecImages = () => {
    const spe = attrList.value.spe;
    if (Array.isArray(spe) && spe.length > 0) {
        for (let gi = 0; gi < spe.length; gi++) {
            const group = spe[gi];
            if (!canGroupUploadImage(gi) || !group?.specImageRequired) continue;
            const attrs = group?.attrList || [];
            for (let i = 0; i < attrs.length; i++) {
                if (!attrs[i].attrPicThumb) {
                    specImageErrorText.value = t('请先完善规格图片');
                    return false;
                }
            }
        }
    }
    specImageErrorText.value = "";
    return true;
};

// 是否存在“可上传且已勾选”的规格组
const anySpecImageChecked = () => (attrList.value.spe || []).some((group: any, gi: number) => canGroupUploadImage(gi) && group?.specImageRequired);
// 检查并更新规格图片验证状态（用于实时验证）
const checkSpecImagesValidation = () => {
    if (anySpecImageChecked()) {
        validateSpecImages();
    } else {
        // 无任何已勾选规格组时清除错误提示
        specImageErrorText.value = "";
    }
};
const onSpecImageToggle = () => {
    checkSpecImagesValidation();
};

// 保存前：仅清空“可编辑范围内但未勾选添加规格图片”的规格组图片；
// 不在可编辑范围的规格组（如切回仅第一组后的其他组）保留已上传图片用于只读回显，不清空
const syncSpecImagesBeforeSave = () => {
    const spe = attrList.value.spe;
    if (!Array.isArray(spe) || spe.length === 0) return;
    spe.forEach((group: any, gi: number) => {
        // 不在可编辑范围：保留已上传图片
        if (!canGroupUploadImage(gi)) return;
        // 可编辑且已勾选：保留
        if (group?.specImageRequired) return;
        // 可编辑但未勾选：清空该组图片
        const attrs = group?.attrList || [];
        if (!Array.isArray(attrs)) return;
        attrs.forEach((attr: any) => {
            attr.attrPicThumb = "";
            attr.attrPic = "";
            attr.attrPicId = "";
        });
    });
    specImageErrorText.value = "";
};

// 初始化各规格组的“添加规格图片”勾选状态：已有规格图的组默认勾选；仅在未初始化时赋值，避免覆盖用户操作
watch(
    () => attrList.value.spe,
    (newValue) => {
        if (!Array.isArray(newValue)) return;
        newValue.forEach((group: any) => {
            if (group && (group.specImageRequired === undefined || group.specImageRequired === null)) {
                group.specImageRequired = groupHasImages(group) ? 1 : 0;
            }
        });
    },
    { deep: true, immediate: true }
);

// 验证规格名：多规格模式下每组规格必须填写规格名
const validateSpecAttrNames = () => {
    if (skuType.value !== 2) {
        specNameErrorText.value = "";
        return true;
    }
    const speList = attrList.value.spe || [];
    const hasEmpty = speList.some((it: any) => !it?.attrName || !String(it.attrName).trim());
    if (hasEmpty) {
        specNameErrorText.value = t('规格名为必填项');
        return false;
    }
    specNameErrorText.value = "";
    return true;
};

// 规格名输入框 blur：同步规格明细并触发校验
const onSpecAttrNameBlur = (e?: any) => {
    dragEnd(e);
    validateSpecAttrNames();
};

const validateProductSku = async () => {
    // 保存时清理未勾选/不可上传规格组的已有规格图片
    syncSpecImagesBeforeSave();
    // 多规格时：规格名必填（新增/编辑都生效）
    if (!validateSpecAttrNames()) {
        return Promise.reject(new Error(specNameErrorText.value || t('请先完善规格名')));
    }
    // 分页下非当前页 el-form 未挂载，先全量兜底，命中则跳到该行所在页
    if (skuType.value === 2) {
        if (getEnabledSkus(productList.value).length < 1) {
            message.error(t('请至少启用一个SKU'));
            return Promise.reject(new Error(t('请至少启用一个SKU')));
        }
        const bad = findFirstInvalidSku();
        if (bad) {
            skuStatusFilter.value = 'enabled';
            await nextTick();
            if (paginationEnabled.value) {
                goToIndex(bad.index);
                await nextTick();
            }
            try {
                await productFormRef.value.validate();
            } catch (e) {}
            return Promise.reject(new Error(bad.message));
        }
    }
    await productFormRef.value.validate();
};
const dragEnd = (event: any) => {
    // 重新计算 productList（按性能档位路由）
    recomputeProductForm();
};
// 添加属性项
const addAttr = (type: AttrListKey, index: number | string) => {
    const i = Number(index);
    attrList.value[type][i].attrList.push({
        id: Date.now() + Math.random(), // 添加唯一id
        attrValue: "",
        attrValues: attrList.value[type][i]?.attrList[0]?.attrValues,
        attrType: (typeId as any)[type],
        attrName: attrList.value[type][i].attrName
    });
    if (type === "spe") {
        // 仅规格值变动时才需要重算 SKU 明细
        recomputeProductForm();
        // 添加规格值后，若存在已勾选规格图片的规格组则实时校验
        checkSpecImagesValidation();
    }
};
// 移除属性项
const removeAttr = (type: AttrListKey, index: number | string, idx: number | string) => {
    const i = Number(index);
    const j = Number(idx);
    attrList.value[type][i].attrList.splice(j, 1);
    if (type === "spe") {
        // 仅规格值变动时才需要重算 SKU 明细
        recomputeProductForm();
        // 删除规格值后，若存在已勾选规格图片的规格组则实时校验
        checkSpecImagesValidation();
    }
};
const changeSkuType = (value: number) => {
    if (value === 1) {
        // 统一规格时，保留已有数据或设置默认值，避免 NaN
        // const existingData = productList.value[0] || {};
        const existingData: any = {};
        productList.value = [
            {
                skuPrice: existingData.skuPrice || "",
                skuStock: existingData.skuStock ?? 0,
                marketPrice: existingData.marketPrice || "",
                costPrice: existingData.costPrice || "",
                skuSn: existingData.skuSn || "",
                skuTsn: existingData.skuTsn || ""
            }
        ];
    } else {
        // 多规格时，恢复之前保存的规格数据
        if (savedSpeData.value && savedSpeData.value.length > 0) {
            attrList.value.spe = JSON.parse(JSON.stringify(savedSpeData.value));
        } else {
            // 如果没有保存的数据，尝试从 props 初始化
            const initData = getAttrListClone();
            if (initData.spe && initData.spe.length > 0) {
                attrList.value.spe = initData.spe;
            }
        }
        // 兜底：未配置商品规格时，默认插入一行空规格，让用户直接填写
        if (!Array.isArray(attrList.value.spe) || attrList.value.spe.length === 0) {
            attrList.value.spe = [
                {
                    attrName: "",
                    attrList: [
                        {
                            id: Date.now() + Math.random(),
                            attrType: (typeId as any).spe,
                            attrName: "",
                            attrValue: ""
                        }
                    ]
                }
            ];
        }
        // 恢复保存的 productList 数据或重新计算
        if (savedProductListData.value && savedProductListData.value.length > 0) {
            productList.value = JSON.parse(JSON.stringify(savedProductListData.value));
        } else {
            recomputeProductForm();
        }
    }
};
// 移除类型
const removeAttrList = (type: AttrListKey, index: number | string) => {
    const i = Number(index);
    attrList.value[type].splice(i, 1);
    // 重新计算 productList
    if (type === "spe") {
        recomputeProductForm();
        // 删除后如果规格为空/已补全，立即清理规格名必填提示
        validateSpecAttrNames();
    }
};
// 新增类型
const addAttrList = (type: AttrListKey, title: string) => {
    // visible.value = true;
    // dialogTitle.value = title;
    newAttrState.value.name = "";
    newAttrState.value.type = type;
    onOk();
};
const addAttrSpeList = (type: AttrListKey) => {
    attrList.value[type].push({
        attrName: "",
        attrList: [
            {
                id: Date.now() + Math.random(), // 添加唯一id
                attrType: (typeId as any)[type],
                attrName: "",
                attrValue: ""
            }
        ]
    });
    // 添加规格后立即触发规格名必填校验（多规格时）
    validateSpecAttrNames();
};
// 添加图片
const addPic = (result: any, data: { type: AttrListKey; index: number | string; idx: number | string }) => {
    const i = Number(data.index);
    const j = Number(data.idx);
    attrList.value[data.type][i].attrList[j].attrPicId = result[0].picId;
    attrList.value[data.type][i].attrList[j].attrPic = result[0].picUrl;
    attrList.value[data.type][i].attrList[j].attrPicThumb = result[0].picThumb;
    // 添加图片后检查验证状态
    if (data.type === "spe") {
        checkSpecImagesValidation();
    }
};
// 移除图片
const removePic = (type: AttrListKey, index: number | string, idx: number | string) => {
    const i = Number(index);
    const j = Number(idx);
    attrList.value[type][i].attrList[j].attrPic = "";
    attrList.value[type][i].attrList[j].attrPicThumb = "";
    attrList.value[type][i].attrList[j].attrPicId = "";
    // 删除图片后检查验证状态
    if (type === "spe") {
        checkSpecImagesValidation();
    }
};
//导入模板
const importAttrTpl = (tplId: number | null) => {
    // 如果清除了模板选择，清空导入的模板数据，并移除 fromTemplate 标记
    if (!tplId) {
        importedTplData.value = null;
        // 移除所有 fromTemplate 标记，使属性名可以编辑
        removeFromTemplateFlag(attrList.value);
        fromTemplateProcessed = false; // 重置标记，允许重新处理
        return;
    }
    request({
        url: "product/productAttributesTpl/detail?id=" + tplId,
        method: "get"
    }).then((result: any) => {
        const data = result.tplData || {};
        // 保存导入的模板数据用于提示
        importedTplData.value = data;
        if (importedTplData.value.spe?.length && ![3, 7, 8].includes(formState.value.productType as number)) {
            skuType.value = 2;
        }
        (["normal", "spe", "extra"] as const).forEach((type) => {
            const typeData = data[type];
            attrList.value[type] = [];
            if (!Array.isArray(typeData)) return;
            for (let i = 0; i < typeData.length; i++) {
                const listItem: any[] = [];
                // 兼容老数据：当 attrValues 为 null 或 undefined 时，默认为空数组
                const attrValues = typeData[i].attrValues ?? [];
                for (let idx = 0; idx < attrValues.length; idx++) {
                    const item = attrValues[idx];
                    // 兼容新旧数据格式
                    if (typeof item === "object" && item !== null && "value" in item) {
                        // 新格式：对象格式 { value: "", id: xxx }
                        listItem.push({
                            id: item.id || Date.now() + Math.random() + idx, // 确保有唯一id
                            attrValue: item.value,
                            attrValues: attrValues.map((v: any) => (typeof v === "object" ? v.value : v)),
                            attrType: (typeId as any)[type],
                            attrName: typeData[i].attrName,
                            fromTemplate: true // 标记为模板导入
                        });
                    } else {
                        // 旧格式：字符串格式
                        listItem.push({
                            id: Date.now() + Math.random() + idx, // 添加唯一id
                            attrValue: item,
                            attrValues: attrValues.map((v: any) => (typeof v === "object" ? v.value : v)),
                            attrType: (typeId as any)[type],
                            attrName: typeData[i].attrName,
                            fromTemplate: true // 标记为模板导入
                        });
                    }
                }
                // 如果 attrValues 为空，添加一个默认的空属性值
                if (listItem.length === 0) {
                    listItem.push({
                        id: Date.now() + Math.random(),
                        attrValue: "",
                        attrValues: [],
                        attrType: (typeId as any)[type],
                        attrName: typeData[i].attrName,
                        fromTemplate: true
                    });
                }
                attrList.value[type].push({
                    attrName: typeData[i].attrName,
                    attrList: listItem,
                    fromTemplate: true // 标记整个属性项为模板导入
                });
                // attrList.value[type].push({
                //     attrName: data[type][i].attrName,
                //     attrList: [
                //         {
                //             attrValue: "",
                //             attrValues: data[type][i].attrValues,
                //             attrType: (typeId as any)[type],
                //             attrName: data[type][i].attrName
                //         }
                //     ]
                // });
            }
        });
        // 导入模板后，标记已处理 fromTemplate
        fromTemplateProcessed = true;
    });
    // Modal.confirm({
    //     title: "导入模板会清空并重置已设置的属性、规格、附加服务、库存等设置，确认导入吗？",
    //     async onOk() {

    //     }
    // });
};
// 商品类目变化会由父组件清空已选模板，此时同样要解除属性名的模板锁定
watch(attrTplId, (tplId, oldTplId) => {
    if (!tplId && oldTplId) {
        importAttrTpl(null);
    }
});
// 处理一口价变化，自动计算市场价
const handleSkuPriceChange = (row: any, price: number | string) => {
    if (marketPriceRate.value && price) {
        const numPrice = Number(price);
        if (!isNaN(numPrice)) {
            row.marketPrice = (numPrice * marketPriceRate.value).toFixed(2);
        }
    }
};

// 批量操作：
const onBatchClick = () => {
    let i = 1;
    for (const row of getEnabledSkus(productList.value)) {
        if (batchInput.value.skuStock) row.skuStock = batchInput.value.skuStock;
        if (batchInput.value.skuSn) row.skuSn = batchInput.value.skuSn + "-" + i;
        if (batchInput.value.skuTsn) row.skuTsn = batchInput.value.skuTsn + "-" + i;
        if (batchInput.value.skuPrice) {
            row.skuPrice = batchInput.value.skuPrice;
            // 批量设置一口价时，自动计算市场价
            if (marketPriceRate.value) {
                const numPrice = Number(batchInput.value.skuPrice);
                if (!isNaN(numPrice)) {
                    row.marketPrice = (numPrice * marketPriceRate.value).toFixed(2);
                }
            }
        }
        if (batchInput.value.marketPrice) row.marketPrice = batchInput.value.marketPrice;
        if (batchInput.value.costPrice) row.costPrice = batchInput.value.costPrice;
        i++;
    }
};
// 添加属性名称
const formRef = ref();
const onOk = () => {
    // e.validate((valid: any) => {
    // if (valid) {
    if (newAttrState.value.name && newAttrState.value.name.trim() !== "") {
        let isDuplicated = attrList.value[newAttrState.value.type].find((obj: any) => obj.attrName === newAttrState.value.name);
        if (isDuplicated) {
            Modal.error({
                title: t('属性名称“') + newAttrState.value.name + t('”已存在')
            });
            return;
        }
    }
    attrList.value[newAttrState.value.type].push({
        attrName: newAttrState.value.name,
        attrList: [
            {
                id: Date.now() + Math.random(), // 添加唯一id
                attrType: (typeId as any)[newAttrState.value.type],
                attrName: newAttrState.value.name,
                attrValue: ""
            }
        ]
    });
    // visible.value = false;
    // dialogTitle.value = "";
    // }
    // });
};
// productForm 与 productList 内容一致，仅用于规格明细表头列（productForm[0].attrs）
const productForm = computed(() => productList.value || []);

// 分页下非当前页 el-form 未挂载，兜底全量扫描（复用同一套规则函数）
const validateSkuRow = (row: any): string => {
    const priceErr = getSkuPriceError(row);
    if (priceErr) return priceErr;
    if (shouldValidateSkuStock()) {
        const stockErr = getSkuStockError(row);
        if (stockErr) return stockErr;
    }
    return "";
};
const findFirstInvalidSku = (): { index: number; message: string } | null => {
    const list = productList.value || [];
    for (let i = 0; i < list.length; i++) {
        const msg = validateSkuRow(list[i]);
        if (msg) return { index: i, message: msg };
    }
    return null;
};

// 监听attrList值是否有变化，如果有，则会更新参数
watch(
    () => attrList.value,
    (newValue, oldValue) => {
        // 判断值是否有变化
        if (watchLoaded == true) {
            emit("update:attrChanged", true);
            // stopWatch()
        }
        watchLoaded = true;
    },
    { deep: true, immediate: true }
);

watch(
    () => attrList.value.spe,
    () => {
        recomputeProductForm();
    },
    { deep: true, immediate: true }
);

const debounceTimers = ref<{ [key: string]: ReturnType<typeof setTimeout> }>({});
const debounceAttrNameInput = (value: string, type: AttrListKey, index: number) => {
    const timerKey = `${type}-${index}`;
    if (debounceTimers.value[timerKey]) {
        clearTimeout(debounceTimers.value[timerKey]);
    }
    debounceTimers.value[timerKey] = setTimeout(() => {
        handleAttrNameChange(value, type, index);
        delete debounceTimers.value[timerKey];
    }, 10);
};

const handleAttrNameChange = (value: string, type: AttrListKey, index: number) => {
    attrList.value[type][index].attrList.map((attr: any) => {
        attr.attrName = value;
    });
};

const loadMarketPriceRate = async () => {
    try {
        const result = await getConfigProduct();
        marketPriceRate.value = result.marketPriceRate;
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
loadMarketPriceRate();
defineExpose({ skuErrorText, validateProductSku, validateSpecImages, validateSpecAttrNames, showDisabledSkus });
</script>

<style lang="less" scoped>
.lyecs-form-table {
    width: 100%;
    .sku-status-toolbar {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
    }
    .sku-status-filter {
        width: 110px;
    }
    .sku-spec-value--disabled {
        color: var(--el-text-color-disabled);
    }
    :deep(.el-table .cell) {
        overflow: visible !important;
    }
    :deep(.el-form-item__error) {
        position: absolute !important;
        z-index: 99;
        top: 32px !important;
    }
    :deep(.el-form-item) {
        margin-bottom: 5px;
    }
    :deep(.el-input-group__append, .el-input-group__prepend) {
        padding: 0 10px;
    }
    .rate {
        :deep(.el-form-item__error) {
            position: absolute !important;
            z-index: 99;
            top: 32px !important;
            right: 83px !important;
            left: inherit !important;
        }
    }
    .down_salesman_rate {
        :deep(.el-form-item__error) {
            position: absolute !important;
            z-index: 99;
            top: 52px !important;
            right: 83px !important;
            left: inherit !important;
        }
    }
    :deep(.el-select) {
        min-width: 50px !important;
    }
}

.sku-pagination {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
}
.attr-list {
    width: 100%;
    border: 1px solid #eeeeee;
    padding: 10px;
    .attr-warp-title {
        background-color: #f7f8fa;
        padding: 10px;
        cursor: pointer;
        position: relative;
        .title {
            margin-bottom: 10px;
            // width: 100px;
            .label {
                // width: 100px;
                text-align: right;
            }
        }
        .attr-extra-value {
            margin-left: 85px;
        }
        .btn-remove5 {
            position: absolute;
            top: 10px;
            right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 18px;
            height: 18px;
            line-height: 14px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            opacity: 0;
            i {
                font-size: 10px;
                font-weight: bold;
            }
        }
        &:hover {
            .btn-remove5 {
                opacity: 1;
            }
        }
    }
    .attr-warp {
        margin-bottom: 20px;
        position: relative;

        .attr-warp-box {
            display: flex;
            margin-left: 67px;
            .attr-row-box {
                flex: 1;
                margin-top: 20px;
                display: flex;
                flex-wrap: wrap;
                gap: 10px;
                .attr-row {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }
                .draggable-item {
                    display: flex;
                    align-items: flex-start;
                    .attr-row {
                        position: relative;
                        .drag-handle {
                            margin-left: 6px;
                            padding: 6px 4px;
                            cursor: move;
                            color: #999;
                            user-select: none;
                        }
                        .btn-remove2 {
                            position: absolute;
                            top: -5px;
                            right: -5px;
                            color: #fff;
                            background-color: #7b7b7b;
                            width: 16px;
                            height: 16px;
                            line-height: 11px;
                            text-align: center;
                            border-radius: 50%;
                            cursor: pointer;
                            opacity: 0;
                            i {
                                font-size: 8px;
                                font-weight: bold;
                            }
                        }
                        &:hover {
                            .btn-remove2 {
                                opacity: 1;
                            }
                        }
                    }
                }
            }
            .attr-btn {
                margin-top: 15px;
                margin-left: 10px;
            }
        }
        .btn-remove {
            position: absolute;
            top: 10px;
            right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 18px;
            height: 18px;
            line-height: 14px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            opacity: 0;
            z-index: 10;
            i {
                font-size: 10px;
                font-weight: bold;
            }
        }
        &:hover {
            .btn-remove {
                opacity: 1;
            }
        }
    }

    .attr-normal-box {
        position: relative;
        .title-box {
            background-color: #f7f8fa;
            padding: 10px;
            margin-bottom: 10px;
            .tit-value {
                display: flex;
                align-items: center;
                .btn-remove3 {
                    margin-left: 10px;
                    color: #fff;
                    background-color: #7b7b7b;
                    width: 16px;
                    height: 16px;
                    line-height: 11px;
                    text-align: center;
                    border-radius: 50%;
                    cursor: pointer;
                    opacity: 0;
                    i {
                        font-size: 8px;
                        font-weight: bold;
                    }
                }
            }
        }
        .attr-normal-value {
            margin-left: 65px;
        }
        &:hover {
            .btn-remove3 {
                opacity: 1 !important;
            }
        }
    }
}
.other-attr {
    .attr-warp-title {
        .btn-remove4 {
            margin-right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 16px;
            height: 16px;
            line-height: 11px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            i {
                font-size: 8px;
                font-weight: bold;
            }
        }
        &:hover {
            .btn-remove3 {
                opacity: 1;
            }
        }
    }
}
.upload-img-wrap {
    background: #fff;
    border: 1px solid #dcdcdc;
    border-radius: 4px;
    padding: 2px;
    width: 84px;
    margin-top: 10px;
}
.upload-img-wrap .arrow {
    border-color: transparent;
    border-style: solid;
    border-bottom: 5px solid #dcdcdc;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    height: 0;
    left: 44%;
    position: relative;
    top: -7px;
    width: 0;
    &::after {
        border-color: transparent transparent #fff;
        border-style: solid;
        border-width: 0 10px 10px;
        content: "";
        display: block;
        height: 0;
        margin-left: -10px;
        position: absolute;
        top: 1px;
        width: 0;
    }
}
.pic-select-con :deep(.pic-select) {
    display: flex;
    align-items: center;
    justify-content: center;
    padding-left: 0;
    position: relative;
}

.pic-select-con :deep(.pic-select-img) {
    height: 70px;
    width: 70px;
    padding-left: 0;
    background-color: #fff;
    border-radius: 2px;
    cursor: pointer;
    position: relative;
}

.pic-select-con :deep(.pic-select-img:after) {
    content: "\e637";
    font-family: "admin-iconfont";
    font-size: 30px;
    position: absolute;
    top: 0;
    line-height: 70px;
    width: 70px;
    left: 0;
    z-index: 1;
    text-align: center;
    color: #a6a5a5;
}

.pic-select-con :deep(.pic-select .pic-select-del) {
    position: absolute;
    cursor: pointer;
    right: -8px;
    top: -13px;
    color: #fff;
    background: #dbdbdb;
    border-radius: 50%;
    z-index: 2;
    width: 16px;
    height: 16px;
    text-align: center;
    line-height: 16px;
    font-size: 12px;
    display: none;
}
.pic-select-con :deep(.pic-select .left) {
    left: -8px !important;
}

.pic-select-con:hover :deep(.pic-select .pic-select-del) {
    display: block;
    background-color: #7b7b7b;
}

.pic-select-con :deep(.pic-select .pic-select-img img) {
    width: 70px;
    height: 70px;
    position: relative;
    z-index: 2;
}

// 规格图片必填错误样式
.pic-required-error {
    :deep(.pic-select) {
        border: 1px dashed var(--el-color-danger) !important;
    }
}

// 规格图片复选框样式
.spec-image-checkbox {
    display: flex;
    align-items: center;
    margin-left: 15px;
}

@media (max-width: 768px) {
    .attr-list {
        .attr-warp-title {
            .flex.align-center,
            .title-box.flex.align-center,
            .title.flex {
                flex-wrap: wrap;

                .label {
                    width: 100%;
                    text-align: left;
                    margin-bottom: 5px;
                }

                > div:not(.label) {
                    width: 100% !important;
                    margin-top: 0;
                }

                :deep(.el-input) {
                    width: 100% !important;
                }
            }
        }

        .attr-warp {
            .attr-warp-box {
                margin-left: 0;
            }
            .attr-row-box {
                flex-direction: column;
                align-items: flex-start;

                .draggable-item {
                    width: 100%;
                    flex-wrap: wrap;
                    align-items: center;

                    .attr-row {
                        width: 100%;
                        flex-direction: row;
                        align-items: center;

                        > div:not(.drag-handle):not(.btn-remove2) {
                            width: 100% !important;
                            flex: 1;
                        }

                        :deep(.el-input) {
                            width: 100% !important;
                        }
                    }

                    > .el-button {
                        margin-top: 10px !important;
                        margin-left: 0 !important;
                    }
                }
            }
        }

        .other-attr {
            .attr-row {
                margin-left: 0 !important;
            }
        }

        :deep(.business-data-wrap) {
            width: 100% !important;
        }

        .spec-image-checkbox {
            margin-left: 0;
            margin-top: 10px;
        }
    }
}
</style>
