import { useState } from 'react';
import {
  Button,
  Form,
  Input,
  Modal,
  Popconfirm,
  Space,
  Switch,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { IconPlus } from '@douyinfe/semi-icons';
import { apiPost } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

interface GradeRow {
  gradeId: number;
  gradeName: string;
  isDefault?: boolean | number;
  userCount?: number;
  status: 0 | 1;
  addTime?: string;
  createTime?: string;
}

export function WholesalerGrades() {
  const [reloadKey, setReloadKey] = useState(0);
  const [editVisible, setEditVisible] = useState(false);
  const [editLoading, setEditLoading] = useState(false);
  const [editingRow, setEditingRow] = useState<GradeRow | null>(null);
  const [gradeName, setGradeName] = useState('');

  const openEdit = (row?: GradeRow) => {
    if (row) {
      setEditingRow(row);
      setGradeName(row.gradeName || '');
    } else {
      setEditingRow(null);
      setGradeName('');
    }
    setEditVisible(true);
  };

  const handleStatusChange = async (record: GradeRow, checked: boolean) => {
    const val = checked ? 1 : 0;
    try {
      const res: any = await apiPost('/wholesale/grade/setStatus', {
        gradeId: record.gradeId,
        status: val,
      });
      if (res && res.warning) {
        Toast.warning({ content: res.warning });
      } else {
        Toast.success({ content: '状态修改成功' });
      }
      setReloadKey((k) => k + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '状态修改失败' });
    }
  };

  const handleDelete = async (record: GradeRow) => {
    if (record.isDefault) {
      Modal.warning({
        title: '提示',
        content: '默认等级不能删除，如需删除请前往批发商设置修改默认批发商',
        okText: '确定',
      });
      return;
    }
    if ((record.userCount ?? 0) > 0) {
      Modal.warning({
        title: '删除失败',
        content: '该类型下已有批发商用户。在删除该类型之前，必须先删除该类型下的所有批发商用户。',
        okText: '确定',
      });
      return;
    }
    try {
      await apiPost('/wholesale/grade/delete', { gradeId: record.gradeId });
      Toast.success({ content: '操作成功' });
      setReloadKey((k) => k + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
    }
  };

  const handleEditSubmit = async () => {
    if (!gradeName.trim()) {
      Toast.warning({ content: '请输入批发商等级' });
      return;
    }
    setEditLoading(true);
    try {
      await apiPost('/wholesale/grade/save', {
        gradeId: editingRow ? editingRow.gradeId : undefined,
        gradeName: gradeName.trim(),
      });
      Toast.success({ content: '操作成功' });
      setEditVisible(false);
      setReloadKey((k) => k + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setEditLoading(false);
    }
  };

  const columns: ColumnConfig[] = [
    {
      title: '批发商等级',
      dataIndex: 'gradeName',
      width: 220,
      render: (v: unknown, row: unknown) => {
        const record = row as GradeRow;
        return (
          <Space align="center" spacing={8}>
            <Text strong>{String(v || '--')}</Text>
            {Boolean(record.isDefault) && (
              <Tag size="small" color="blue">
                默认等级
              </Tag>
            )}
          </Space>
        );
      },
    },
    {
      title: '已绑定客户数',
      dataIndex: 'userCount',
      width: 140,
      render: (v: unknown) => <span>{String(v ?? 0)}</span>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      render: (v: unknown, row: unknown) => {
        const record = row as GradeRow;
        return (
          <Switch
            checked={Number(v) === 1}
            onChange={(checked) => handleStatusChange(record, checked)}
          />
        );
      },
    },
    {
      title: '创建时间',
      dataIndex: 'addTime',
      width: 180,
      render: (v: unknown, row: unknown) => {
        const record = row as GradeRow;
        return <span>{String(v || record.createTime || '--')}</span>;
      },
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 150,
      render: (_: unknown, row: unknown) => {
        const record = row as GradeRow;
        return (
          <Space spacing={4}>
            <Button
              theme="borderless"
              type="primary"
              size="small"
              onClick={() => openEdit(record)}
            >
              编辑
            </Button>
            <Button
              theme="borderless"
              type="danger"
              size="small"
              onClick={() => handleDelete(record)}
            >
              删除
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <ListPage
        key={reloadKey}
        api="/wholesale/grade/list?withUserCount=true"
        rowKey="gradeId"
        columns={columns}
        toolbarExtra={
          <Button icon={<IconPlus />} type="primary" onClick={() => openEdit()}>
            新增批发商等级
          </Button>
        }
      />

      <Modal
        title={editingRow ? '编辑批发商等级' : '新增批发商等级'}
        visible={editVisible}
        onCancel={() => setEditVisible(false)}
        onOk={handleEditSubmit}
        confirmLoading={editLoading}
        width={480}
        okText="确定"
        cancelText="取消"
      >
        <div>
          <div style={{ marginBottom: 6, fontWeight: 500 }}>批发商等级</div>
          <Input
            placeholder="请输入批发商等级"
            maxLength={50}
            value={gradeName}
            onChange={(val) => setGradeName(val)}
          />
        </div>
      </Modal>
    </div>
  );
}
