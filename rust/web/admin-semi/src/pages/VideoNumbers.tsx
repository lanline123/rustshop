import { useState } from 'react';
import { Button, Input, Modal, Space, Toast, Typography } from '@douyinfe/semi-ui';
import { apiPost } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

interface VideoAccountRow {
  id?: number;
  finderId?: string;
  nickname?: string;
  relationTime?: string;
}

/** 视频号：对应旧版 content/videoNumber/List.vue，接口 /content/wechatVideoAccount/*。 */
export default function VideoNumbers() {
  const [reloadKey, setReloadKey] = useState(0);
  const [addOpen, setAddOpen] = useState(false);
  const [addForm, setAddForm] = useState<{ finderId: string; nickname: string }>({ finderId: '', nickname: '' });
  const [saving, setSaving] = useState(false);

  const reload = () => setReloadKey((key) => key + 1);

  const submitAdd = async () => {
    if (!String(addForm.finderId || '').trim()) {
      Toast.error({ content: '请输入视频号ID' });
      return;
    }
    setSaving(true);
    try {
      await apiPost('/content/wechatVideoAccount/create', addForm);
      Toast.success({ content: '关联成功' });
      setAddOpen(false);
      setAddForm({ finderId: '', nickname: '' });
      reload();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '关联失败' });
    } finally {
      setSaving(false);
    }
  };

  const deleteAccount = (row: VideoAccountRow) => {
    Modal.confirm({
      title: '确认解除该视频号关联吗？',
      onOk: async () => {
        try {
          await apiPost('/content/wechatVideoAccount/delete', { id: row.id });
          Toast.success({ content: '删除成功' });
          reload();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
        }
      },
    });
  };

  const columns: ColumnConfig<VideoAccountRow>[] = [
    {
      title: '视频号ID',
      dataIndex: 'finderId',
      width: 220,
      render: (value: string) => <span>{value || '-'}</span>,
    },
    {
      title: '视频号昵称',
      dataIndex: 'nickname',
      width: 200,
      render: (value: string) => <span>{value || '-'}</span>,
    },
    { title: '关联时间', dataIndex: 'relationTime', width: 180 },
  ];

  return (
    <>
      <ListPage<VideoAccountRow>
      key={reloadKey}
      api="/content/wechatVideoAccount/list"
      rowKey="id"
      pageSize={10}
      actionWidth={130}
      columns={columns}
      rowActions={(row) => (
        <Space spacing={2}>
          <Button theme="borderless" type="danger" size="small" onClick={() => deleteAccount(row)}>
            解除关联
          </Button>
        </Space>
      )}
      toolbarExtra={
        <Button theme="solid" type="primary" onClick={() => setAddOpen(true)}>
          关联视频号
        </Button>
      }
      />
      <Modal
        title="关联视频号"
        visible={addOpen}
        confirmLoading={saving}
        onOk={submitAdd}
        onCancel={() => setAddOpen(false)}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <Text strong>视频号ID</Text>
          <Input
            placeholder="请输入视频号ID（finderId）"
            value={addForm.finderId}
            onChange={(next) => setAddForm((c) => ({ ...c, finderId: next }))}
          />
          <Text strong>视频号昵称（可选）</Text>
          <Input
            placeholder="请输入视频号昵称"
            value={addForm.nickname}
            onChange={(next) => setAddForm((c) => ({ ...c, nickname: next }))}
          />
        </div>
      </Modal>
    </>
  );
}
