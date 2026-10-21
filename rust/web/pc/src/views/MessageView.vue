<template>
  <div class="tig-message-page">
    <div class="container message-center">
      <aside class="mc-side-bar">
        <div class="nav-card">
          <div class="nav-group-title">消息中心</div>
          <div class="nav-item active">账户信息</div>
        </div>
      </aside>

      <div class="msg-list">
        <div class="msg-toolbar">
          <el-button @click="onlyUnread = !onlyUnread; onFilterChange()">{{ onlyUnread ? '查看全部' : '只看未读' }}</el-button>
          <el-button @click="markAllRead">全部标记为已读</el-button>
        </div>

        <div v-loading="loading" class="list">
          <template v-if="messages.length">
            <div
              v-for="item in messages"
              :key="item.messageId"
              class="msg-item"
              :class="item.isRead ? 'is-read' : 'is-unread'"
              @click="readItem(item)"
            >
              <div class="msg-card">
                <div class="msg-card-head">
                  <div class="msg-source">
                    <el-icon class="msg-icon"><Bell /></el-icon>
                    <span class="msg-label">系统通知</span>
                    <span v-if="!item.isRead" class="msg-dot"></span>
                  </div>
                  <div class="msg-date">{{ item.addTimeDate }}</div>
                </div>
                <h3 class="msg-title">{{ item.title }}</h3>
                <p class="msg-txt">{{ item.content }}</p>
                <el-icon class="msg-del" @click.stop="removeMessage(item)"><Delete /></el-icon>
              </div>
            </div>
            <div v-if="total > pageSize" class="pagination-wrap">
              <el-pagination
                :current-page="page"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                background
                @current-change="onPageChange"
              />
            </div>
          </template>
          <el-empty v-else-if="!loading" description="暂无消息" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 站内信（对齐原版 `pages/member/userMessage/List.vue`）。
 *
 * 结构：左侧「消息中心 / 账户信息」导航 + 右侧工具栏（只看未读 / 全部标记为已读）
 * + 消息卡片列表（未读圆点、日期、标题、正文、删除）。接口：
 * `GET /user/message/list`、`POST /user/message/updateMessageRead`、
 * `POST /user/message/updateAllRead`、`POST /user/message/del`。
 */
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Bell, Delete } from '@element-plus/icons-vue';
import { messageApi } from '@/api';
import { pageItems } from '@/api/types';
import type { UserMessage } from '@/api/types';
import { applySeo } from '@/composables/useSeo';

const loading = ref(false);
const messages = ref<UserMessage[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const onlyUnread = ref(false);

function goLink(item: UserMessage) {
  const raw = item.link;
  if (!raw) return;
  let url = '';
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      url = parsed?.data ?? '';
    } catch {
      url = raw;
    }
  } else if (typeof raw === 'object') {
    url = raw.data ?? raw.url ?? '';
  }
  if (typeof url === 'string' && url) {
    window.open(url, '_blank', 'noopener');
  }
}

async function load() {
  loading.value = true;
  try {
    const result = await messageApi.list({
      page: page.value,
      size: pageSize.value,
      unread: onlyUnread.value ? 1 : 0,
    });
    messages.value = pageItems(result);
    total.value = Number(result?.total ?? 0) || 0;
  } catch {
    messages.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function onFilterChange() {
  page.value = 1;
  load();
}

function onPageChange(next: number) {
  page.value = next;
  load();
}

async function readItem(item: UserMessage) {
  try {
    if (Number(item.isRead) !== 1) {
      await messageApi.updateRead(item.messageId);
      item.isRead = 1;
    }
    goLink(item);
  } catch {
    // 标记已读失败不阻塞跳转
  }
}

async function markAllRead() {
  await messageApi.updateAllRead();
  ElMessage.success('已全部标记为已读');
  onlyUnread.value = false;
  await load();
}

async function removeMessage(item: UserMessage) {
  await ElMessageBox.confirm('确定删除该消息？', '删除消息', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  });
  await messageApi.remove(item.messageId);
  ElMessage.success('已删除');
  await load();
}

onMounted(() => {
  applySeo({ title: '站内信', noindex: true });
  load();
});
</script>

<style scoped lang="less">
.tig-message-page {
  background: #f5f6fa;
  padding: 20px 0 50px;
}

.message-center {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.mc-side-bar {
  width: 216px;
  flex: 0 0 216px;
  background: #fff;
  border-radius: 8px;
  padding: 12px 0;

  .nav-group-title {
    padding: 8px 24px;
    font-weight: 700;
    font-size: 14px;
    margin-bottom: 4px;
  }

  .nav-item {
    padding: 10px 24px;
    font-size: 14px;
    color: #555;
    border-left: 3px solid transparent;

    &.active {
      color: var(--tig-red);
      border-left-color: var(--tig-red);
      background: #fff7f6;
      font-weight: 600;
    }
  }
}

.msg-list {
  flex: 1;
  min-width: 0;

  .msg-toolbar {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-bottom: 12px;
  }
}

.list {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  min-height: 300px;
}

.msg-item {
  border-bottom: 1px solid #f5f5f5;
  padding: 12px 8px;

  &.is-unread .msg-title { color: #222; font-weight: 700; }
  &.is-read .msg-title { color: #666; font-weight: 400; }
}

.msg-card {
  position: relative;
  background: #fafafa;
  border-radius: 6px;
  padding: 12px 16px;

  .msg-card-head {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #999;
    margin-bottom: 6px;
  }

  .msg-source {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #888;
    .msg-icon { font-size: 14px; }
    .msg-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--tig-red); }
  }

  .msg-title { font-size: 14px; margin-bottom: 6px; }
  .msg-txt { color: #777; font-size: 13px; line-height: 1.7; }

  .msg-del {
    position: absolute;
    right: 14px;
    bottom: 12px;
    color: #bbb;
    cursor: pointer;
    &:hover { color: var(--tig-red); }
  }
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>
