import React, { useState } from 'react';
import {
  Button,
  Card,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Switch,
  Tag,
  TextArea,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { IconPlus } from '@douyinfe/semi-icons';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';
import { apiGet, apiPost } from '../api';

const { Text } = Typography;

/* ==================================================================
   1. 供应商管理 (SupplierList / supplierManage)
================================================================== */
interface SupplierItem {
  id: number;
  code: string;
  name: string;
  categoryName?: string;
  contactName?: string;
  phone?: string;
  status?: number; // 1: 正常, 0: 冻结
  createTime?: string;
}

export function SupplierList() {
  const [reloadKey, setReloadKey] = useState(0);
  const [formVisible, setFormVisible] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formState, setFormState] = useState({
    code: '',
    name: '',
    categoryId: 1,
    contactName: '',
    phone: '',
    address: '',
    status: 1,
  });

  const openForm = async (record?: SupplierItem) => {
    if (record) {
      setEditingId(record.id);
      try {
        const res = await apiGet<any>('/supplyChain/supplier/detail', { id: record.id });
        setFormState({
          code: res?.code || record.code || '',
          name: res?.name || record.name || '',
          categoryId: res?.categoryId || 1,
          contactName: res?.contactName || record.contactName || '',
          phone: res?.phone || record.phone || '',
          address: res?.address || '',
          status: res?.status ?? record.status ?? 1,
        });
      } catch {
        setFormState({
          code: record.code,
          name: record.name,
          categoryId: 1,
          contactName: record.contactName || '',
          phone: record.phone || '',
          address: '',
          status: record.status ?? 1,
        });
      }
    } else {
      setEditingId(null);
      setFormState({
        code: `SUP${Date.now().toString().slice(-6)}`,
        name: '',
        categoryId: 1,
        contactName: '',
        phone: '',
        address: '',
        status: 1,
      });
    }
    setFormVisible(true);
  };

  const handleFormSubmit = async () => {
    if (!formState.name.trim()) {
      Toast.error('供应商名称不能为空');
      return;
    }
    setFormLoading(true);
    try {
      if (editingId) {
        await apiPost('/supplyChain/supplier/update', { id: editingId, ...formState });
        Toast.success('更新成功');
      } else {
        await apiPost('/supplyChain/supplier/create', formState);
        Toast.success('创建成功');
      }
      setFormVisible(false);
      setReloadKey((k) => k + 1);
    } catch {
      // Handled
    } finally {
      setFormLoading(false);
    }
  };

  const columns: ColumnConfig<SupplierItem>[] = [
    {
      title: '供应商编号',
      dataIndex: 'code',
      width: 140,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '供应商名称',
      dataIndex: 'name',
      render: (val: any) => <span style={{ fontWeight: 500 }}>{val || '--'}</span>,
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      width: 130,
      render: (val: any) => val || '默认分类',
    },
    {
      title: '联系人',
      dataIndex: 'contactName',
      width: 120,
      render: (val: any) => val || '--',
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      width: 140,
      render: (val: any) => val || '--',
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (val: any, row: SupplierItem) => (
        <Switch
          checked={val === 1}
          onChange={async (c) => {
            await apiPost('/supplyChain/supplier/status', { id: row.id, status: c ? 1 : 0 });
            Toast.success('状态更新成功');
            setReloadKey((k) => k + 1);
          }}
        />
      ),
    },
    {
      title: '操作',
      width: 120,
      render: (_: any, record: SupplierItem) => (
        <Button
          theme="borderless"
          type="primary"
          size="small"
          onClick={() => openForm(record)}
        >
          编辑
        </Button>
      ),
    },
  ];

  return (
    <div>
      <ListPage<SupplierItem>
        key={reloadKey}
        api="/supplyChain/supplier/list"
        rowKey="id"
        columns={columns}
        toolbarExtra={
          <Button icon={<IconPlus />} type="primary" onClick={() => openForm()}>
            添加供应商
          </Button>
        }
        filters={[
          {
            field: 'keyword',
            label: '供应商',
            placeholder: '输入供应商名称/编号/联系人',
            type: 'input',
          },
        ]}
      />

      <Modal
        title={editingId ? '编辑供应商' : '添加供应商'}
        visible={formVisible}
        onCancel={() => setFormVisible(false)}
        onOk={handleFormSubmit}
        confirmLoading={formLoading}
        width={580}
        okText="确定"
        cancelText="取消"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <div style={{ marginBottom: 6, fontWeight: 500 }}>供应商名称</div>
            <Input
              placeholder="请输入供应商全称"
              value={formState.name}
              onChange={(val) => setFormState({ ...formState, name: val })}
            />
          </div>
          <div>
            <div style={{ marginBottom: 6, fontWeight: 500 }}>供应商分类</div>
            <Select
              style={{ width: '100%' }}
              value={formState.categoryId}
              onChange={(val) => setFormState({ ...formState, categoryId: Number(val) })}
            >
              <Select.Option value={1}>核心制造厂商</Select.Option>
              <Select.Option value={2}>品牌一级代理</Select.Option>
              <Select.Option value={3}>综合批发渠道</Select.Option>
            </Select>
          </div>
          <div>
            <div style={{ marginBottom: 6, fontWeight: 500 }}>联系人姓名</div>
            <Input
              placeholder="请输入对接联系人"
              value={formState.contactName}
              onChange={(val) => setFormState({ ...formState, contactName: val })}
            />
          </div>
          <div>
            <div style={{ marginBottom: 6, fontWeight: 500 }}>联系电话</div>
            <Input
              placeholder="请输入手机号或座机"
              value={formState.phone}
              onChange={(val) => setFormState({ ...formState, phone: val })}
            />
          </div>
          <div>
            <div style={{ marginBottom: 6, fontWeight: 500 }}>经营地址</div>
            <Input
              placeholder="请输入经营或发货地址"
              value={formState.address}
              onChange={(val) => setFormState({ ...formState, address: val })}
            />
          </div>
        </div>
      </Modal>
    </div>
  );
}

// width: 580

/* ==================================================================
   2. 供应商分类 (SupplierCategories / supplierCategoryManage)
================================================================== */
interface SupplierCategoryItem {
  id: number;
  name: string;
  sortOrder: number;
  status?: number;
}

export function SupplierCategories() {
  const [reloadKey, setReloadKey] = useState(0);
  const [formVisible, setFormVisible] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formState, setFormState] = useState({
    name: '',
    sortOrder: 50,
  });

  const openForm = (record?: SupplierCategoryItem) => {
    if (record) {
      setEditingId(record.id);
      setFormState({ name: record.name, sortOrder: record.sortOrder });
    } else {
      setEditingId(null);
      setFormState({ name: '', sortOrder: 50 });
    }
    setFormVisible(true);
  };

  const handleFormSubmit = async () => {
    if (!formState.name.trim()) {
      Toast.error('分类名称不能为空');
      return;
    }
    setFormLoading(true);
    try {
      await apiPost('/supplyChain/supplierCategory/save', { id: editingId, ...formState });
      Toast.success('保存成功');
      setFormVisible(false);
      setReloadKey((k) => k + 1);
    } catch {
      // Handled
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    await apiPost('/supplyChain/supplierCategory/delete', { id });
    Toast.success('删除成功');
    setReloadKey((k) => k + 1);
  };

  const columns: ColumnConfig<SupplierCategoryItem>[] = [
    {
      title: '分类名称',
      dataIndex: 'name',
      render: (val: any) => <span style={{ fontWeight: 500 }}>{val || '--'}</span>,
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      width: 100,
      render: (val: any) => val ?? 50,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (val: any, row: SupplierCategoryItem) => (
        <Switch
          checked={val === 1}
          onChange={async (c) => {
            await apiPost('/supplyChain/supplierCategory/status', { id: row.id, status: c ? 1 : 0 });
            Toast.success('状态更新成功');
            setReloadKey((k) => k + 1);
          }}
        />
      ),
    },
    {
      title: '操作',
      width: 140,
      render: (_: any, record: SupplierCategoryItem) => (
        <Space>
          <Button
            theme="borderless"
            type="primary"
            size="small"
            onClick={() => openForm(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="您确认要删除此分类吗？"
            okType="danger"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button theme="borderless" type="danger" size="small">
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <ListPage<SupplierCategoryItem>
        key={reloadKey}
        api="/supplyChain/supplierCategory/list"
        rowKey="id"
        columns={columns}
        toolbarExtra={
          <Button icon={<IconPlus />} type="primary" onClick={() => openForm()}>
            添加分类
          </Button>
        }
        filters={[
          {
            field: 'keyword',
            label: '分类名称',
            placeholder: '输入分类名称',
            type: 'input',
          },
        ]}
      />

      <Modal
        title={editingId ? '编辑分类' : '添加分类'}
        visible={formVisible}
        onCancel={() => setFormVisible(false)}
        onOk={handleFormSubmit}
        confirmLoading={formLoading}
        width={580}
        okText="确定"
        cancelText="取消"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <div style={{ marginBottom: 6, fontWeight: 500 }}>分类名称</div>
            <Input
              placeholder="请输入供应商分类名称"
              value={formState.name}
              onChange={(val) => setFormState({ ...formState, name: val })}
            />
          </div>
          <div>
            <div style={{ marginBottom: 6, fontWeight: 500 }}>排序</div>
            <InputNumber
              style={{ width: '100%' }}
              value={formState.sortOrder}
              onChange={(val) => setFormState({ ...formState, sortOrder: Number(val) || 50 })}
            />
          </div>
        </div>
      </Modal>
    </div>
  );
}

// width: 580

/* ==================================================================
   3. 供应商商品 (SupplierProducts / supplierProductManage)
================================================================== */
interface SupplierProductItem {
  id: number;
  productName: string;
  productSn: string;
  supplierName?: string;
  purchasePrice: number | string;
  supplyStock: number;
  updateTime?: string;
}

export function SupplierProducts() {
  const [reloadKey, setReloadKey] = useState(0);

  const columns: ColumnConfig<SupplierProductItem>[] = [
    {
      title: '商品名称',
      dataIndex: 'productName',
      render: (val: any, row: SupplierProductItem) => (
        <div>
          <div style={{ fontWeight: 500 }}>{val}</div>
          <Text type="tertiary" size="small">货号: {row.productSn}</Text>
        </div>
      ),
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      width: 180,
      render: (val: any) => val || '--',
    },
    {
      title: '供货价',
      dataIndex: 'purchasePrice',
      width: 140,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#165dff' }}>¥{val}</span>,
    },
    {
      title: '供货库存',
      dataIndex: 'supplyStock',
      width: 120,
      render: (val: any) => val ?? 0,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      width: 170,
      render: (val: any) => val || '--',
    },
  ];

  return (
    <ListPage<SupplierProductItem>
      key={reloadKey}
      api="/supplyChain/supplierProduct/list"
      rowKey="id"
      columns={columns}
      filters={[
        {
          field: 'keyword',
          label: '商品',
          placeholder: '输入商品名称/货号',
          type: 'input',
        },
        {
          field: 'supplierId',
          label: '供应商',
          placeholder: '输入供应商名称',
          type: 'input',
        },
      ]}
    />
  );
}

/* ==================================================================
   4. 供应链设置 (PurchaseSetting / purchaseSettingManage)
================================================================== */
export function PurchaseSetting() {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [config, setConfig] = useState({
    autoInbound: 1,
    reconciliationCycle: 30,
    notifyThresholdDays: 3,
  });

  const loadConfig = async () => {
    setLoading(true);
    try {
      const res = await apiGet<any>('/supplyChain/purchaseSetting/detail');
      if (res) setConfig((c) => ({ ...c, ...res }));
    } catch {
      // keep
    } finally {
      setLoading(false);
    }
  };

  useState(() => {
    loadConfig();
  });

  const handleSave = async () => {
    setSaving(true);
    try {
      await apiPost('/supplyChain/purchaseSetting/save', config);
      Toast.success('供应链设置保存成功');
    } catch {
      // handled
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card title="采购与供应链设置" loading={loading}>
      <div style={{ maxWidth: 650, display: 'flex', flexDirection: 'column', gap: 20 }}>
        <div>
          <div style={{ marginBottom: 6, fontWeight: 500 }}>采购收货后自动入库</div>
          <Switch
            checked={config.autoInbound === 1}
            onChange={(c) => setConfig({ ...config, autoInbound: c ? 1 : 0 })}
          />
        </div>
        <div>
          <div style={{ marginBottom: 6, fontWeight: 500 }}>默认对账结算账期 (天)</div>
          <InputNumber
            style={{ width: '100%' }}
            min={1}
            max={365}
            value={config.reconciliationCycle}
            onChange={(val) => setConfig({ ...config, reconciliationCycle: Number(val) || 30 })}
          />
        </div>
        <div>
          <div style={{ marginBottom: 6, fontWeight: 500 }}>对账到期提前预警 (天)</div>
          <InputNumber
            style={{ width: '100%' }}
            min={1}
            max={30}
            value={config.notifyThresholdDays}
            onChange={(val) => setConfig({ ...config, notifyThresholdDays: Number(val) || 3 })}
          />
        </div>
        <div>
          <Button theme="solid" type="primary" loading={saving} onClick={handleSave}>
            保存设置
          </Button>
        </div>
      </div>
    </Card>
  );
}
