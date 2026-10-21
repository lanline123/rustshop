/**
 * @file 权限选择器组件
 * @description 树形权限选择器，用于管理员权限分配，支持：
 * @features
 * - 全选/全不选功能
 * - 多级权限树（一级/二级/三级菜单）
 * - 下拉子权限选择
 * - 半选状态（indeterminate）
 * - 联动选择（选择子级自动选择父级）
 */
<template>
    <el-form label-width="auto"> </el-form>
    <div class="right-body">
        <div class="title">
            <div class="left-div">
                <el-checkbox :model-value="isCheckAll" @update:model-value="handleCheckAll" :indeterminate="isCheckAllIndeterminate">
                    <span >{{ $t('全选') }}</span>
                </el-checkbox>
            </div>
            <div class="right-div">{{ $t('权限列表') }}</div>
        </div>
        <a-spin :spinning="loading" style="width: 100%; margin-top: 100px"></a-spin>
        <template v-if="!loading">
            <div v-for="item in authorityList" :key="item.authorityId" class="info">
                <div class="left-div">
                    <el-checkbox
                        :model-value="isChildAllChecked(item)"
                        :indeterminate="isChildIndeterminate(item)"
                        @update:model-value="(e: any) => handleCheckChildAll(e, item)"
                    >
                        <span >{{ item.authorityName }}</span>
                    </el-checkbox>
                </div>
                <div class="right-div">
                    <div class="child-div mb10" v-for="value in item.children" :key="value.authorityId">
                        <div class="ml10">
                            <el-checkbox
                                :indeterminate="isChildIndeterminate2(value)"
                                :model-value="isChildAllChecked2(value)"
                                @update:model-value="(e: any) => handleCheckChildAll2(e, value, item)"
                            >
                                <el-dropdown v-if="value.childAuth && value.childAuth.length > 0" :hide-on-click="false">
                                    <span
                                        >{{ value.authorityName }}<el-icon class="el-icon--right"><arrow-down /></el-icon
                                    ></span>
                                    <template #dropdown>
                                        <el-dropdown-menu>
                                            <!-- <el-dropdown-item>
                                                <el-checkbox
                                                    style="width: 100%"
                                                    :model-value="isChildAllChecked2(value)"
                                                    :indeterminate="isChildIndeterminate2(value)"
                                                    @update:model-value="(e: any) => handleCheckChildAll2(e, value, item)"
                                                >
                                                    <span >{{ value.authorityName }}</span>
                                                </el-checkbox>
                                            </el-dropdown-item> -->
                                            <el-dropdown-item v-for="auth in value.childAuth" @click.native="() => {}">
                                                <el-checkbox
                                                    style="width: 100%"
                                                    :model-value="isChildChecked(auth)"
                                                    @update:model-value="(e: any) => handleChildChecked(e, auth, value, item)"
                                                >
                                                    <span >{{ auth.authName }}</span>
                                                </el-checkbox>
                                            </el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                                <span v-else>{{ value.authorityName }}</span>
                            </el-checkbox>
                        </div>
                        <div class="child-box flex" v-if="value.children && value.children.length > 0">
                            <div class="child2-div" v-for="value2 in value.children" :key="value2.authorityId">
                                <el-checkbox
                                    :indeterminate="isChildIndeterminate2(value2)"
                                    :model-value="isChildAllChecked2(value2)"
                                    @update:model-value="(e: any) => handleCheckChildAll2(e, value2, value, item)"
                                >
                                    <el-dropdown v-if="value2.childAuth && value2.childAuth.length > 0" :hide-on-click="false">
                                        <div class="flex align-center">
                                            {{ value2.authorityName }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                                        </div>
                                        <template #dropdown>
                                            <el-dropdown-menu>
                                                <!-- <el-dropdown-item>
                                                    <el-checkbox
                                                        style="width: 100%"
                                                        :model-value="isChildAllChecked2(value2)"
                                                        :indeterminate="isChildIndeterminate2(value2)"
                                                        @update:model-value="(e: any) => handleCheckChildAll2(e, value2, item)"
                                                    >
                                                        <span >{{ value2.authorityName }}</span>
                                                    </el-checkbox>
                                                </el-dropdown-item> -->
                                                <el-dropdown-item v-for="auth2 in value2.childAuth" @click.native="() => {}">
                                                    <el-checkbox
                                                        style="width: 100%"
                                                        :model-value="isChildChecked(auth2)"
                                                        @update:model-value="(e: any) => handleChildChecked(e, auth2, value2, value, item)"
                                                    >
                                                        <span >{{ auth2.authName }}</span>
                                                    </el-checkbox>
                                                </el-dropdown-item>
                                            </el-dropdown-menu>
                                        </template>
                                    </el-dropdown>
                                    <span v-else>{{ value2.authorityName }}</span>
                                </el-checkbox>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { AuthorityFormState } from "@/types/authority/authority";
import { getAllAuthority } from "@/api/authority/authority";
import { message } from "ant-design-vue";
import { ArrowDown, SemiSelect, Select } from "@element-plus/icons-vue";

const props = defineProps({
    /** 已选中的权限标识数组 */
    modelValue: { type: Array, default: [] },
    /** 后台类型：admin/shop/store/vendor */
    adminType: { type: String, default: "" }
});
const loading = ref<boolean>(true);
const checkAll = ref(false);

/** 是否全选 */
const isCheckAll = computed(() => {
    return props.modelValue.includes("all");
});

/** 是否为半选状态 */
const isCheckAllIndeterminate = computed(() => {
    return props.modelValue.length > 0 && !props.modelValue.includes("all");
});

/** 处理全选/取消全选 */
const handleCheckAll = (e: any) => {
    console.log(e);
    if (e === false) {
        emit("update:modelValue", []);
    } else {
        emit("update:modelValue", ["all"]);
    }
};

/** 有all且取消某一个子菜单时，展开所有权限标识 */
const returnCheckedIfAll = () => {
    if (props.modelValue.includes("all")) {
        const authoritySns: string[] = collectAllAuthoritySns(authorityList.value);
        return authoritySns;
    } else {
        return props.modelValue;
    }
};

/** 收集所有权限标识（递归遍历权限树） */
const collectAllAuthoritySns = (data: any) => {
    const result: any[] = [];

    const traverse = (items: any) => {
        items.forEach((item: any) => {
            // 添加当前菜单的 authoritySn
            if (item.authoritySn) {
                result.push(item.authoritySn);
            }

            // 添加子权限的 authSn
            if (item.childAuth?.length) {
                item.childAuth.forEach((auth: any) => {
                    if (auth.authSn) {
                        result.push(auth.authSn);
                    }
                });
            }

            // 递归处理子菜单
            if (item.children?.length) {
                traverse(item.children);
            }
        });
    };

    traverse(data);
    return result;
};

/** 递归获取节点及其所有子节点的权限标识（含中间分组节点 sn 及 childAuth） */
const recursion = (item: any): string[] => {
    let authoritySns: string[] = [item.authoritySn]; // 初始化authoritySns数组，包含当前节点的authoritySn
    if (item.children && item.children.length > 0) {
        item.children.forEach((authority: any) => {
            const childAuthSNs = authority.childAuth?.map((child: any) => child.authSn) || [];
            // 递归调用并合并结果
            authoritySns.push(...childAuthSNs, ...recursion(authority));
        });
    } else {
        const childAuthSNs = item.childAuth?.map((child: any) => child.authSn) || [];
        authoritySns.push(...childAuthSNs);
    }
    return authoritySns;
};

/** 处理一级菜单的全选/取消 */
const handleCheckChildAll = (e: any, item: any) => {
    let resultArray = <any>[];
    resultArray = returnCheckedIfAll();
    const authoritySns: string[] = recursion(item);
    if (e === false) {
        resultArray = resultArray.filter((auth: any) => !authoritySns.includes(auth));
    } else {
        resultArray = Array.from(new Set([...resultArray, ...authoritySns]));
    }
    emit("update:modelValue", resultArray);
};

/**
 * 检查一级菜单是否完全勾选（显示 ✓）。
 * 业务规则：只有"明确点击全选"时才显示 ✓——即节点自身 sn、所有子节点 sn、
 * 所有 childAuth.authSn 都在 modelValue 中。
 * 子节点被勾选导致父节点 sn 被"带上"时，不算 ✓，应显示为半选 -。
 */
const isChildAllChecked = (item: any) => {
    if (props.modelValue.includes("all")) {
        return true;
    }
    const authoritySns: string[] = recursion(item);
    return authoritySns.every((element) => props.modelValue.includes(element));
};

/** 检查一级菜单是否为半选状态（显示 -） */
const isChildIndeterminate = (item: any) => {
    if (props.modelValue.includes("all")) {
        return false;
    }
    const authoritySns: string[] = recursion(item);
    return !authoritySns.every((element) => props.modelValue.includes(element)) && authoritySns.some((element) => props.modelValue.includes(element));
};

/** 处理二级菜单的全选/取消 */
const handleCheckChildAll2 = (e: any, item: any, parentItem: any, grandParentItem?: any) => {
    let resultArray = <any>[];
    resultArray = returnCheckedIfAll();
    const authoritySns: string[] = recursion(item);
    // 合并结果
    if (e === false) {
        resultArray = resultArray.filter((auth: any) => !authoritySns.includes(auth));
        if (resultArray.length <= 2) {
            resultArray = resultArray.filter((auth: any) => !parentItem.authoritySn.includes(auth));
            if (grandParentItem) {
                resultArray = resultArray.filter((auth: any) => !grandParentItem.authoritySn.includes(auth));
            }
        }
    } else {
        resultArray = Array.from(new Set([...[parentItem.authoritySn], ...resultArray, ...authoritySns]));
        if (grandParentItem) {
            resultArray = Array.from(new Set([...[grandParentItem.authoritySn], ...resultArray]));
        }
    }
    emit("update:modelValue", resultArray);
};

/**
 * 检查二级菜单是否完全勾选（显示 ✓）。
 * 与一级菜单同样的严格判断：仅当 recursion 收集到的全部 sn 都在 modelValue 中才算。
 */
const isChildAllChecked2 = (item: any) => {
    if (props.modelValue.includes("all")) {
        return true;
    }
    const authoritySns: string[] = recursion(item);
    return authoritySns.every((element) => props.modelValue.includes(element));
};

/** 检查二级菜单是否为半选状态（显示 -） */
const isChildIndeterminate2 = (item: any) => {
    if (props.modelValue.includes("all")) {
        return false;
    }
    const authoritySns: string[] = recursion(item);
    return !authoritySns.every((element) => props.modelValue.includes(element)) && authoritySns.some((element) => props.modelValue.includes(element));
};

/** 获取节点的所有父级ID */
const getParentIds = (item: any, authorityList: any[]): string[] => {
    const parentIds: string[] = [];
    const findParent = (childId: string, list: any[]) => {
        list.forEach((node: any) => {
            if (node.children) {
                node.children.forEach((child: any) => {
                    if (child.authorityId === childId) {
                        parentIds.push(node.authorityId);
                        findParent(node.authorityId, authorityList);
                    }
                });
            }
        });
    };
    findParent(item.authorityId, authorityList);
    return parentIds;
};

/** 检查三级菜单是否选中 */
const isChildChecked = (item: any) => {
    if (props.modelValue.includes("all")) {
        return true;
    }
    if (props.modelValue.includes(item.authSn)) {
        return true;
    }
    return false;
};

/** 处理三级菜单的选中/取消 */
const handleChildChecked = (e: any, item: any, parentItem: any, grandParentItem: any, greatGrandParentItem?: any) => {
    let resultArray = <any>[];
    resultArray = returnCheckedIfAll();
    if (e === false) {
        resultArray = resultArray.filter((element: any) => element !== item.authSn);
        // resultArray = resultArray.filter((element: any) => !parentItem.authoritySn.includes(element));
        // resultArray = resultArray.filter((element: any) => !grandParentItem.authoritySn.includes(element));
        // if (greatGrandParentItem) {
            // resultArray = resultArray.filter((element: any) => !greatGrandParentItem.authoritySn.includes(element));
        // }
    } else {
        resultArray = Array.from(new Set([...resultArray, ...[item.authSn], ...[parentItem.authoritySn], ...[grandParentItem.authoritySn]]));
        if (greatGrandParentItem) {
            console.log(resultArray);
            resultArray = Array.from(new Set([...resultArray, ...[greatGrandParentItem.authoritySn]]));
        }
    }
    console.log(resultArray);
    emit("update:modelValue", resultArray);
};

const emit = defineEmits(["update:modelValue"]);
/** 权限列表数据 */
const authorityList = ref<AuthorityFormState[]>([]);

/**
 * 根据全量权限树和当前 modelValue，补齐中间分组节点的 authoritySn。
 *
 * 业务规则：子节点勾选了，父节点的 authoritySn 也要"带上"（写入 modelValue）。
 * 这里只补齐数据，不改变显示状态——父节点是否显示为 ✓ 仍由 isChildAllChecked 严格判断
 * （要求自身 sn、所有子节点 sn、所有 childAuth.authSn 都齐全），否则显示为 -（半选）。
 *
 * 用途：
 *   1) 修复后端历史数据 authorityList 中省略中间分组节点 sn 的问题
 *      （如 commodityManage、salesOverview、distributionSettings 等）；
 *   2) 即使用户不做任何修改直接保存，也能让提交数据完整、与菜单层级对齐。
 *
 * 逻辑：对每个分组节点，只要其任意一个后代（子节点 sn 或 childAuth.authSn）在 modelValue 中，
 *      就把该分组节点的 authoritySn 也加入 modelValue。幂等。
 */
const normalizeModelValue = () => {
    const current = props.modelValue as string[];
    if (!current || current.length === 0 || current.includes("all")) {
        return;
    }
    const next = [...current];
    let changed = false;

    const traverse = (items: any[]) => {
        items.forEach((item) => {
            if (item.children && item.children.length > 0) {
                traverse(item.children);
                if (!item.authoritySn || next.includes(item.authoritySn)) {
                    return;
                }
                // 收集所有后代 sn（排除自身），只要有一个在 modelValue 中，就补齐父节点 sn
                const descendantSns = recursion(item).filter((sn) => sn !== item.authoritySn);
                if (descendantSns.some((sn) => next.includes(sn))) {
                    next.push(item.authoritySn);
                    changed = true;
                }
            }
        });
    };
    traverse(authorityList.value);

    if (changed) {
        emit("update:modelValue", next);
    }
};

/** 获取所有权限列表 */
const getAllAuthorityList = async () => {
    try {
        const result = await getAllAuthority({ type: 1, adminType: props.adminType });
        authorityList.value = result;
        normalizeModelValue();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    getAllAuthorityList().then(() => {});
});
</script>

<style lang="less" scoped>
.right-body {
    display: flex;
    width: 100%;
    flex-direction: column;

    .title {
        height: 60px;
        background-color: #f5f6fa;
        display: flex;
        flex-direction: row;
        padding: 16px;
        box-sizing: border-box;
        font-weight: 500;
    }

    .left-div {
        min-width: 100px;
        font-weight: 500;
    }

    .info {
        padding: 16px;
        display: flex;
        flex-direction: row;
        border-bottom: 1px solid #f0f0f0;
    }

    .right-div {
        width: 100%;
    }

    .child-box {
        background-color: #f5f7fa;
        flex-wrap: wrap;
        justify-content: flex-start;
        padding: 5px 10px;
        gap: 15px;
        .child2-div {
            width: calc(25%);
        }
    }
}
</style>
