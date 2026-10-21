<template>
    <div v-if="!hideTrigger" :class="avatar ? 'have' : 'no-have'" class="system-img">
        <div v-if="!avatar" class="dialog-link" @click="show()">
            <i class="iconfont icon-tianjiatupian"></i>
            <span>{{ $t('系统头像') }}</span>
        </div>
        <div v-else class="have-image">
            <el-image :src="returnAvatar(current)" style="width: 80px; height: 80px"></el-image>
            <div class="bar">
                <span class="iconfont icon-bianji1" @click="show()"></span>
                <span class="iconfont icon-cha" style="margin-left: 6px" @click="del()"></span>
            </div>
        </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="$t('系统头像')" width="830">
        <div class="image-list">
            <div
                v-for="(item, index) in avatarFileList"
                :class="current == returnAvatarString(index) ? 'select' : ''"
                class="image-item"
                @click="selectImage(index)"
            >
                <el-image :src="item" style="width: 100px; height: 100px"></el-image>
            </div>
        </div>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="dialogVisible = false">{{ $t('取消') }}</el-button>
                <el-button type="primary" @click="save()"> {{ $t('确认') }} </el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { ref, watch } from "vue";
import { returnAvatar, avatarList, avatarFileList, returnAvatarString } from "@/utils/avatar";
const props = defineProps({
    avatar: {
        type: String,
        default: ""
    },
    hideTrigger: {
        type: Boolean,
        default: false
    }
});

const selectImage = (value: number) => {
    current.value = returnAvatarString(value);
};
const emit = defineEmits(["update:avatar"]);
const dialogVisible = ref(false);
const current = ref<string | number>(props.avatar);

watch(
    () => props.avatar,
    (val) => {
        current.value = val;
    }
);

const show = () => {
    dialogVisible.value = true;
};
const del = () => {
    emit("update:avatar", "");
    current.value = avatarList[0];
};
const save = () => {
    emit("update:avatar", current.value);
    dialogVisible.value = false;
};

defineExpose({ show });
</script>
<style lang="less" scoped>
.system-img {
    min-width: 80px;
    min-height: 80px;
    max-width: 80px;
    max-height: 80px;

    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    flex-direction: column;

    .dialog-link {
        display: inline-flex;
        flex-direction: column;
        line-height: 25px;
        font-size: 12px;
        color: #999999;

        & > i {
            font-size: 20px;
            color: #999999;
            text-align: center;
        }
    }

    .have-image {
        width: 80px;
        height: 80px;
        position: relative;

        .bar {
            display: none; /* 初始隐藏 */
            position: absolute;
            bottom: 0;
            left: 0;
            width: 100%;
            line-height: 20px;
            transition: all 0.3s;
            color: white;
            height: 20px;
            background: rgba(0, 0, 0, 0.6);
            padding: 0 10px;
            box-sizing: border-box;
        }
    }

    .have-image:hover .bar {
        display: block; /* 鼠标移入时显示 */
    }
}

.system-img:hover {
    border: 1px dashed var(--tig-primary);
}

.have {
    border: 1px solid #ddd;
}

.no-have {
    border: 1px dashed #ddd;
}

.image-list {
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    justify-content: left;
    gap: 10px;
    padding: 10px 0;

    .image-item {
        display: flex;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        border: 2px solid #eee;
        position: relative;
    }

    .select {
        border: 2px solid var(--tig-primary);
    }

    /* 三角形 */

    .select::after {
        content: "";
        position: absolute;
        top: 0;
        right: 0;
        border-top: 30px solid var(--tig-primary);
        border-left: 30px solid transparent;
    }
}
</style>
