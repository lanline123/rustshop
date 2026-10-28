import { useState } from 'react';
import { Button, Modal, Space, Typography } from '@douyinfe/semi-ui';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

interface AdminLogRow {
  logId: number;
  username?: string;
  ipAddress?: string;
  logTime?: string;
  logInfo?: string;
  requestLog?: string;
}

const columns: ColumnConfig<AdminLogRow>[] = [
  { title: '编号', dataIndex: 'logId', width: 90 },
  { title: '操作者', dataIndex: 'username', width: 160 },
  { title: 'IP地址', dataIndex: 'ipAddress', width: 160 },
  { title: '操作日期', dataIndex: 'logTime', width: 180 },
  {
    title: '操作记录',
    dataIndex: 'logInfo',
    render: (value: string) => (
      <Text ellipsis={{ showTooltip: true }} style={{ maxWidth: 420 }}>
        {value || '-'}
      </Text>
    ),
  },
];

export default function AdminLogs() {
  const [detail, setDetail] = useState<AdminLogRow | null>(null);

  return (
    <>
      <ListPage<AdminLogRow>
        api="/authority/adminLog/list"
        rowKey="logId"
        pageSize={10}
        actionWidth={100}
        filters={[
          {
            field: 'keyword',
            label: '操作日志',
            type: 'input',
            placeholder: '输入关键词或IP地址',
            width: 260,
          },
        ]}
        columns={columns}
        rowActions={(record) =>
          record.requestLog ? (
            <Button theme="borderless" size="small" onClick={() => setDetail(record)}>
              查看
            </Button>
          ) : null
        }
      />

      <Modal
        title="操作详情"
        visible={Boolean(detail)}
        onCancel={() => setDetail(null)}
        width={620}
        footer={
          <Space>
            <Button onClick={() => setDetail(null)}>关闭</Button>
          </Space>
        }
      >
        <div style={{ marginBottom: 12 }}>{detail?.logInfo}</div>
        <pre className="admin-log-request">{detail?.requestLog}</pre>
      </Modal>
    </>
  );
}
