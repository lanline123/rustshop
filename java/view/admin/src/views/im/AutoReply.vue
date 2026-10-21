<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeName" class="lyecs-tabs" tab-position="top">
                <el-tab-pane label="客服首句欢迎语" :name="1">
                    <FirstGreeting v-if="activeName === 1" />
                </el-tab-pane>
                <el-tab-pane label="客服忙提醒语" :name="2">
                    <QueueGreeting v-if="activeName === 2" />
                </el-tab-pane>
                <el-tab-pane label="客服离线提醒语" :name="3">
                    <EndGreeting v-if="activeName === 3" />
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import FirstGreeting from "@/views/im/src/FirstGreeting.vue";
import QueueGreeting from "@/views/im/src/QueueGreeting.vue";
import EndGreeting from "@/views/im/src/EndGreeting.vue";

const activeName = ref(1);
</script>
