import { useState } from 'react';
import { Button, Modal, Space, Switch, Tag, Toast } from '@douyinfe/semi-ui';
import { apiPost, isDemo } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

interface TaxRateRow {
  taxRateId: number;
  regionId: number;
  regionName?: string;
  rate?: number | string;
  isActive?: number;
  remark?: string;
  createTime?: string;
}

function ActiveSwitch({ row, value }: { row: TaxRateRow; value?: number }) {
  const [checked, setChecked] = useState(value === 1);
  const toggle = async (next: boolean) => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持操作' });
      return;
    }
    try {
      await apiPost('/setting/taxRate/update', {
        taxRateId: row.taxRateId,
        regionId: row.regionId,
        rate: row.rate,
        isActive: next ? 1 : 0,
        remark: row.remark,
      });
      setChecked(next);
      Toast.success({ content: '已更新' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '更新失败' });
    }
  };
  return <Switch checked={checked} onChange={toggle} size="small" />;
}

const columns: ColumnConfig<TaxRateRow>[] = [
  { title: '国家/地区', dataIndex: 'regionName', width: 220, render: (value: string) => value || '-' },
  {
    title: '税率(%)',
    dataIndex: 'rate',
    width: 130,
    render: (value: number | string) => `${value ?? 0}%`,
  },
  {
    title: '是否生效',
    dataIndex: 'isActive',
    width: 110,
    render: (value: number, record) => <ActiveSwitch row={record} value={value} />,
  },
  { title: '备注', dataIndex: 'remark', render: (value: string) => value || '-' },
  { title: '创建时间', dataIndex: 'createTime', width: 180, render: (value: string) => value || '-' },
];

export default function TaxRates() {
  const [reloadKey, setReloadKey] = useState(0);

  const batchDelete = (ids: number[], clear: () => void) => {
    Modal.confirm({
      title: '批量删除税率规则',
      content: `确定删除选中的 ${ids.length} 条规则？`,
      onOk: async () => {
        try {
          await apiPost('/setting/taxRate/batch', { ids, type: 'del' });
          Toast.success({ content: '批量删除成功' });
          clear();
          setReloadKey((key) => key + 1);
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '批量删除失败' });
        }
      },
    });
  };

  return (
    <ListPage<TaxRateRow>
      key={reloadKey}
      api="/setting/taxRate/list"
      rowKey="taxRateId"
      pageSize={10}
      selectable
      actionWidth={150}
      filters={[
        {
          field: 'regionName',
          label: '国家/地区',
          type: 'input',
          placeholder: '输入国家/地区名称',
          width: 240,
        },
        {
          field: 'isActive',
          label: '是否生效',
          type: 'select',
          width: 130,
          options: [
            { label: '是', value: 1 },
            { label: '否', value: 0 },
          ],
        },
      ]}
      columns={columns}
      batchActions={(rows, clear) => (
        <Space>
          <Button size="small" onClick={() => batchDelete(rows.map((row) => row.taxRateId), clear)}>
            批量删除
          </Button>
        </Space>
      )}
      drawer={{
        title: '税率规则',
        addText: '新增税率规则',
        width: 480,
        labelWidth: 'auto',
        inputWidth: 320,
        createApi: '/setting/taxRate/create',
        updateApi: '/setting/taxRate/update',
        detailApi: '/setting/taxRate/detail',
        deleteApi: '/setting/taxRate/del',
        idField: 'taxRateId',
        sections: [
          {
            key: 'basic',
            title: '税率信息',
            fields: [
              {
                field: 'regionId',
                label: '国家/地区',
                type: 'select',
                required: true,
                placeholder: '请选择国家/地区',
                optionsApi: '/setting/region/list',
                optionsParams: { parentId: 0, page: 1, size: 100 },
                optionLabel: 'regionName',
                optionValue: 'regionId',
              },
              {
                field: 'rate',
                label: '税率(%)',
                type: 'number',
                required: true,
                placeholder: '0-100',
                extraText: '用户支付税费 = 订单支付金额 × 税率',
              },
              {
                field: 'isActive',
                label: '是否生效',
                type: 'switch',
                initValue: 1,
                checkedText: '启用',
                uncheckedText: '停用',
              },
              { field: 'remark', label: '备注', type: 'textarea' },
            ],
          },
        ],
        toPayload: (values) => ({
          ...values,
          taxRateId: values.taxRateId ? Number(values.taxRateId) : undefined,
          regionId: Number(values.regionId),
          rate: Number(values.rate),
          isActive: values.isActive ? 1 : 0,
        }),
      }}
    />
  );
}
