<template>

        <Message :status="2" :currentConversationData="currentConversationData" :msgType="2" :preview="true" v-model:conversationId="conversationId"></Message>

</template>
<script setup lang="ts">
import Message from "@/views/im/src/Message.vue";
import { computed } from "vue";

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    currentConversationData: {
        type: Object,
        default: () => ({})
    }
});
const conversationId = computed(() => {
    return props.id;
});
</script>
<style scoped lang="less">

</style>
