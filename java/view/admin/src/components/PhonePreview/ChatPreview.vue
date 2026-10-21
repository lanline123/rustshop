<template>
    <div class="chat-preview">
        <div class="msg-list">
            <div v-for="(msg, idx) in messages" :key="idx" class="msg-row" :class="msg.role === 'user' ? 'is-user' : 'is-mp'">
                <div v-if="msg.role === 'mp'" class="avatar avatar--mp">
                    <img v-if="mpAvatarUrl" :src="mpAvatarUrl" class="avatar-img avatar-img--contain" />
                </div>

                <template v-if="msg.payload.msgType === 1">
                    <div
                        class="bubble bubble--text"
                        :class="[msg.role === 'user' ? 'bubble--green' : 'bubble--white', { 'bubble--placeholder': !msg.payload.content }]"
                    >
                        {{ msg.payload.content ? replaceVars(msg.payload.content) : $t('等待输入回复内容…') }}
                    </div>
                </template>

                <template v-else-if="msg.payload.msgType === 2 && msg.role === 'mp'">
                    <a class="news-card" :href="msg.payload.newsUrl || '#'" target="_blank" rel="noopener noreferrer" @click.prevent>
                        <div class="news-title" :class="{ 'is-placeholder': !msg.payload.newsTitle }">
                            {{ replaceVars(msg.payload.newsTitle || "") || $t('图文标题') }}
                        </div>
                        <div class="news-desc" :class="{ 'is-placeholder': !msg.payload.newsDescription }">
                            {{ replaceVars(msg.payload.newsDescription || "") || $t('图文摘要') }}
                        </div>
                        <div class="news-cover">
                            <img
                                v-if="msg.payload.newsCoverUrl && !failedSet.has(idx)"
                                :src="msg.payload.newsCoverUrl"
                                :alt="msg.payload.newsTitle || ''"
                                @error="onImgError(idx)"
                            />
                            <div v-else class="news-cover-fallback">{{ $t('封面') }}</div>
                        </div>
                    </a>
                </template>

                <div v-if="msg.role === 'user'" class="avatar avatar--user">
                    <img :src="userAvatarUrl" class="avatar-img" />
                </div>
            </div>
        </div>

        <div class="input-bar">
            <div class="input">{{ inputPlaceholder || $t('点此输入') }}</div>
            <div class="send-btn">+</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
import { imageFormat } from "@/utils/format";
import { returnAvatar, avatarFileList } from "@/utils/avatar";
import type { PreviewMessage } from "@/types/setting/wechat.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    messages: PreviewMessage[];
    nickname?: string;
    shopName?: string;
    inputPlaceholder?: string;
    shopLogo?: string;
}

const props = withDefaults(defineProps<Props>(), {
    nickname: undefined,
    shopName: undefined,
    inputPlaceholder: undefined,
    shopLogo: ""
});

const userAvatarUrl = returnAvatar(10) || returnAvatar(0);

const mpAvatarUrl = computed(() => {
    if (!props.shopLogo) return "";
    return imageFormat(props.shopLogo);
});

const failedSet = ref<Set<number>>(new Set());

function onImgError(idx: number) {
    failedSet.value.add(idx);
}

function replaceVars(text: string): string {
    if (!text) return "";
    const today = dayjs().format("YYYY-MM-DD");
    return text
        .replace(/\{nickname\}/g, props.nickname ?? t('用户'))
        .replace(/\{shopName\}/g, props.shopName ?? t('商城'))
        .replace(/\{date\}/g, today);
}
</script>

<style lang="less" scoped>
.chat-preview {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

.msg-list {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    background: #ededed;
    padding: 12px 10px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.msg-row {
    display: flex;
    align-items: flex-start;
    gap: 6px;

    &.is-user {
        justify-content: flex-end;
    }
}

.avatar {
    width: 28px;
    height: 28px;
    border-radius: 4px;
    flex: 0 0 auto;
    overflow: hidden;
    position: relative;

    &--mp {
        background: #d8dadf;
    }

    &--user {
        background: #b6d8a4;
    }
}

.avatar-img {
    display: block;
    width: 100%;
    height: 100%;

    &--contain {
        object-fit: contain;
        background: #fff;
    }
}

.bubble {
    max-width: 70%;
    padding: 8px 10px;
    border-radius: 6px;
    font-size: 13px;
    line-height: 1.5;
    word-break: break-word;
    white-space: pre-wrap;

    &--green {
        background: #95ec69;
        color: #222;
    }

    &--white {
        background: #ffffff;
        color: #222;
        border: 1px solid #ececec;
    }

    &--placeholder {
        color: #bbb;
    }
}

.news-card {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 60px;
    grid-template-areas:
        "title title"
        "desc  cover";
    column-gap: 10px;
    row-gap: 6px;
    width: 260px;
    padding: 10px;
    background: #ffffff;
    border-radius: 6px;
    border: 1px solid #ececec;
    text-decoration: none;
    color: inherit;
    cursor: default;
}

.news-title {
    grid-area: title;
    font-size: 14px;
    font-weight: 600;
    color: #222;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: break-word;

    &.is-placeholder {
        color: #bbb;
        font-weight: 400;
    }
}

.news-desc {
    grid-area: desc;
    align-self: start;
    font-size: 12px;
    color: #888;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: break-word;

    &.is-placeholder {
        color: #bbb;
    }
}

.news-cover {
    grid-area: cover;
    align-self: end;
    width: 60px;
    height: 60px;
    background: #f0f0f0;
    border-radius: 3px;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        display: block;
        object-fit: cover;
    }

    .news-cover-fallback {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #999;
        font-size: 12px;
        background: #f0f0f0;
    }
}

.input-bar {
    height: 44px;
    background: #f5f5f5;
    border-top: 1px solid #dddddd;
    display: flex;
    align-items: center;
    padding: 0 10px;
    gap: 8px;
    flex: 0 0 auto;

    .input {
        flex: 1;
        height: 32px;
        background: #ffffff;
        border: 1px solid #dddddd;
        border-radius: 4px;
        display: flex;
        align-items: center;
        padding: 0 10px;
        font-size: 13px;
        color: #999;
    }

    .send-btn {
        width: 32px;
        height: 32px;
        border-radius: 4px;
        background: #ffffff;
        border: 1px solid #dddddd;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        color: #666;
        cursor: pointer;
        user-select: none;
    }
}
</style>
