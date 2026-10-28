import { useState } from 'react';
import { Button, Modal, Space, Switch, Toast } from '@douyinfe/semi-ui';
import { apiPost, isDemo } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

interface ShippingTypeRow {
  shippingTypeId: number;
  shippingTypeName?: string;
  shippingTypeDesc?: string;
  logisticsName?: string;
  isSupportCod?: number;
  sortOrder?: number;
}

function CodSwitch({ row, value }: { row: ShippingTypeRow; value?: number }) {
  const [checked, setChecked] = useState(value === 1);
  const toggle = async (next: boolean) => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持操作' });
      return;
    }
    try {
      await apiPost('/setting/shippingType/updateField', {
        id: row.shippingTypeId,
        field: 'isSupportCod',
        val: next ? 1 : 0,
      });
      setChecked(next);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '更新失败' });
    }
  };
  return <Switch checked={checked} onChange={toggle} size="small" />;
}

const columns: ColumnConfig<ShippingTypeRow>[] = [
  { title: '分类名称', dataIndex: 'shippingTypeName', width: 200 },
  { title: '分类描述', dataIndex: 'shippingTypeDesc', render: (value: string) => value || '-' },
  { title: '默认物流', dataIndex: 'logisticsName', width: 180, render: (value: string) => value || '-' },
  {
    title: '货到付款',
    dataIndex: 'isSupportCod',
    width: 110,
    render: (value: number, record) => <CodSwitch row={record} value={value} />,
  },
  { title: '排序', dataIndex: 'sortOrder', width: 90 },
];

export default function ShippingTypes() {
  const [reloadKey, setReloadKey] = useState(0);

  const batchDelete = (ids: number[], clear: () => void) => {
    Modal.confirm({
      title: '批量删除配送类型',
      content: `确定删除选中的 ${ids.length} 个配送类型？`,
      onOk: async () => {
        try {
          await apiPost('/setting/shippingType/batch', { ids, type: 'del' });
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
    <ListPage<ShippingTypeRow>
      key={reloadKey}
      api="/setting/shippingType/list"
      rowKey="shippingTypeId"
      pageSize={10}
      selectable
      actionWidth={150}
      filters={[
        {
          field: 'keyword',
          label: '配送类型',
          type: 'input',
          placeholder: '输入配送类型',
          width: 260,
        },
      ]}
      columns={columns}
      batchActions={(rows, clear) => (
        <Space>
          <Button size="small" onClick={() => batchDelete(rows.map((row) => row.shippingTypeId), clear)}>
            批量删除
          </Button>
        </Space>
      )}
      drawer={{
        title: '配送类型',
        addText: '添加配送方式',
        width: 580,
        labelWidth: 'auto',
        inputWidth: 400,
        createApi: '/setting/shippingType/create',
        updateApi: '/setting/shippingType/update',
        detailApi: '/setting/shippingType/detail',
        deleteApi: '/setting/shippingType/del',
        idField: 'shippingTypeId',
        sections: [
          {
            key: 'basic',
            title: '配送方式信息',
            fields: [
              {
                field: 'shippingTypeName',
                label: '分类名称',
                required: true,
                placeholder: '请输入分类名称',
              },
              {
                field: 'shippingTypeDesc',
                label: '分类描述',
                type: 'textarea',
              },
              {
                field: 'shippingDefaultId',
                label: '默认物流',
                type: 'select',
                placeholder: '请选择物流公司',
                optionsApi: '/setting/logisticsCompany/list',
                optionsParams: { page: 1, pageSize: 100 },
                optionLabel: 'logisticsName',
                optionValue: 'logisticsId',
              },
              {
                field: 'shippingTimeDesc',
                label: '配送时间描述',
                type: 'textarea',
              },
              { field: 'sortOrder', label: '排序', type: 'number', initValue: 50 },
              {
                field: 'isSupportCod',
                label: '货到付款',
                type: 'switch',
                initValue: 0,
                checkedText: '支持',
                uncheckedText: '不支持',
              },
            ],
          },
        ],
        toPayload: (values) => ({
          ...values,
          shippingTypeId: values.shippingTypeId ? Number(values.shippingTypeId) : undefined,
          sortOrder: Number(values.sortOrder ?? 50),
          isSupportCod: values.isSupportCod ? 1 : 0,
        }),
      }}
    />
  );
}
