import { useEffect, useState } from 'react';
import {
  Avatar,
  Button,
  Form,
  InputNumber,
  Modal,
  SideSheet,
  Space,
  Table,
  Tag,
  TextArea,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost, isDemo } from '../api';

const { Text } = Typography;

interface OrderItem {
  itemId: number;
  productName: string;
  productSn: string;
  picThumb: string;
  price: string;
  quantity: number;
  deliveryQuantity: number;
}

interface OrderDetail extends Record<string, any> {
  orderId: number;
  orderSn: string;
  orderStatus: number;
  orderStatusName: string;
  payStatusName: string;
  shippingStatusName: string;
  consignee: string;
  mobile: string;
  address: string;
  buyerNote: string;
  adminNote: string;
  totalAmount: string;
  paidAmount: string;
  productAmount: string;
  shippingFee: string;
  invoiceFee: string;
  serviceFee: string;
  discountAmount: string;
  couponAmount: string;
  addTime: string;
  payTime: string;
  shippingTime: string;
  logisticsName: string;
  trackingNo: string;
  items: OrderItem[];
}

interface LogisticsOption {
  id: number;
  name: string;
}

interface OrderDetailModalProps {
  visible: boolean;
  orderId?: number;
  onCancel: () => void;
  onChanged: () => void;
}

const statusColor: Record<string, string> = {
  待付款: 'orange',
  待发货: 'blue',
  待收货: 'cyan',
  已完成: 'green',
  已取消: 'grey',
};

export default function OrderDetailModal({ visible, orderId, onCancel, onChanged }: OrderDetailModalProps) {
  const [detail, setDetail] = useState<OrderDetail | null>(null);
  const [logistics, setLogistics] = useState<LogisticsOption[]>([]);
  const [deliverOpen, setDeliverOpen] = useState(false);
  const [noteOpen, setNoteOpen] = useState(false);
  const [moneyOpen, setMoneyOpen] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!visible || !orderId) return;
    let alive = true;
    apiGet<OrderDetail>('/order/order/detail', { id: orderId })
      .then((result) => alive && setDetail(result))
      .catch((error) => alive && Toast.error({ content: error.message }));
    apiGet<{ records: Array<{ logisticsId: number; logisticsName: string }> }>(
      '/setting/logisticsCompany/list',
      { page: 1, size: 100 },
    )
      .then((result) => {
        if (!alive) return;
        setLogistics(
          (result.records || []).map((item) => ({ id: item.logisticsId, name: item.logisticsName })),
        );
      })
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [visible, orderId, reloadKey]);

  const refresh = () => {
    setReloadKey((key) => key + 1);
    onChanged();
  };

  const canDeliver = detail?.availableActions?.deliver === true;
  const canCancel = detail?.availableActions?.cancelOrder === true;
  const canModifyMoney = detail?.availableActions?.modifyOrderMoney === true;

  return (
    <>
      <SideSheet
        title="订单详情"
        visible={visible}
        onCancel={onCancel}
        width={900}
        placement="right"
        bodyStyle={{ padding: '8px 24px 20px', overflowY: 'auto' }}
        footer={
          <Space>
            <Button onClick={() => setNoteOpen(true)}>备注</Button>
            {canModifyMoney && <Button onClick={() => setMoneyOpen(true)}>改价</Button>}
            {canCancel && (
              <Button type="danger" theme="borderless" onClick={() => setCancelOpen(true)}>
                取消订单
              </Button>
            )}
            {canDeliver && (
              <Button theme="solid" type="primary" onClick={() => setDeliverOpen(true)}>
                发货
              </Button>
            )}
            <Button onClick={onCancel}>关闭</Button>
          </Space>
        }
      >
        {detail && (
          <div className="order-detail">
            <div className="order-detail-hero">
              <div>
                <div className="order-detail-label">订单号</div>
                <div className="order-detail-number mono">{detail.orderSn}</div>
                <div className="order-detail-time">下单时间 {detail.addTime || '-'}</div>
              </div>
              <Space wrap>
                <Tag color={(statusColor[detail.orderStatusName] || 'blue') as any}>
                  {detail.orderStatusName}
                </Tag>
                <Tag color="green">{detail.payStatusName}</Tag>
                {detail.shippingStatusName && <Tag color="cyan">{detail.shippingStatusName}</Tag>}
              </Space>
            </div>

            <div className="order-detail-grid">
              <div className="order-detail-card">
                <div className="order-detail-card-title">收货信息</div>
                <div className="order-info-row">
                  <span>收货人</span>
                  <b>{detail.consignee || '-'}</b>
                </div>
                <div className="order-info-row">
                  <span>联系电话</span>
                  <b>{detail.mobile || '-'}</b>
                </div>
                <div className="order-info-row is-block">
                  <span>收货地址</span>
                  <b>{detail.address || '-'}</b>
                </div>
              </div>

              <div className="order-detail-card">
                <div className="order-detail-card-title">订单信息</div>
                <div className="order-info-row">
                  <span>支付方式</span>
                  <b>{detail.payName || detail.payStatusName || '-'}</b>
                </div>
                <div className="order-info-row">
                  <span>支付时间</span>
                  <b>{detail.payTime || '-'}</b>
                </div>
                <div className="order-info-row">
                  <span>发货时间</span>
                  <b>{detail.shippingTime || '-'}</b>
                </div>
                <div className="order-info-row">
                  <span>物流公司</span>
                  <b>{detail.logisticsName || '-'}</b>
                </div>
                <div className="order-info-row">
                  <span>物流单号</span>
                  <b className="mono">{detail.trackingNo || '-'}</b>
                </div>
              </div>
            </div>

            <div className="order-detail-card order-detail-goods">
              <div className="order-detail-card-title">商品明细</div>
              <Table
                dataSource={detail.items || []}
                pagination={false}
                rowKey="itemId"
                columns={[
                  {
                    title: '商品',
                    dataIndex: 'productName',
                    render: (value: string, record: OrderItem) => (
                      <div className="product-cell">
                        {record.picThumb ? (
                          <Avatar shape="square" src={record.picThumb} size="small" />
                        ) : (
                          <div className="product-thumb">W</div>
                        )}
                        <div>
                          <div>{value}</div>
                          <Text type="tertiary" size="small">
                            {record.productSn}
                          </Text>
                        </div>
                      </div>
                    ),
                  },
                  { title: '单价', dataIndex: 'price', width: 110, render: (value: string) => `¥ ${value}` },
                  { title: '数量', dataIndex: 'quantity', width: 70 },
                  { title: '已发货', dataIndex: 'deliveryQuantity', width: 80 },
                ]}
              />

              <div className="order-detail-summary">
                <div>
                  <span>商品金额</span>
                  <b>¥ {detail.productAmount}</b>
                </div>
                <div>
                  <span>运费</span>
                  <b>¥ {detail.shippingFee}</b>
                </div>
                <div>
                  <span>优惠</span>
                  <b>- ¥ {detail.discountAmount || detail.couponAmount}</b>
                </div>
                <div className="order-detail-total">
                  <span>实付金额</span>
                  <b>¥ {detail.totalAmount}</b>
                </div>
              </div>
            </div>

            <div className="order-detail-card">
              <div className="order-detail-card-title">备注信息</div>
              <div className="order-info-row is-block">
                <span>买家备注</span>
                <b>{detail.buyerNote || '-'}</b>
              </div>
              <div className="order-info-row is-block">
                <span>商家备注</span>
                <b>{detail.adminNote || '-'}</b>
              </div>
            </div>
          </div>
        )}
      </SideSheet>

      <DeliverModal
        visible={deliverOpen}
        detail={detail}
        logistics={logistics}
        onCancel={() => setDeliverOpen(false)}
        onSuccess={() => {
          setDeliverOpen(false);
          refresh();
          onCancel();
        }}
      />

      <OrderNoteModal
        visible={noteOpen}
        detail={detail}
        onCancel={() => setNoteOpen(false)}
        onSuccess={() => {
          setNoteOpen(false);
          refresh();
        }}
      />

      <OrderMoneyModal
        visible={moneyOpen}
        detail={detail}
        onCancel={() => setMoneyOpen(false)}
        onSuccess={() => {
          setMoneyOpen(false);
          refresh();
        }}
      />

      <OrderCancelModal
        visible={cancelOpen}
        detail={detail}
        onCancel={() => setCancelOpen(false)}
        onSuccess={() => {
          setCancelOpen(false);
          onChanged();
          onCancel();
        }}
      />
    </>
  );
}

function DeliverModal({
  visible,
  detail,
  logistics,
  onCancel,
  onSuccess,
}: {
  visible: boolean;
  detail: OrderDetail | null;
  logistics: LogisticsOption[];
  onCancel: () => void;
  onSuccess: () => void;
}) {
  const [formApi, setFormApi] = useState<any>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (visible && formApi) formApi.reset();
  }, [visible, formApi]);

  const submit = async () => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持发货' });
      return;
    }
    if (!detail || !formApi) return;
    let values: Record<string, any>;
    try {
      values = await formApi.validate();
    } catch {
      return;
    }
    setSaving(true);
    try {
      const deliverData = (detail.items || [])
        .map((item) => ({
          itemId: item.itemId,
          toDeliveryQuantity: Math.max(0, Number(item.quantity) - Number(item.deliveryQuantity || 0)),
        }))
        .filter((item) => item.toDeliveryQuantity > 0);
      await apiPost('/order/order/deliver', {
        id: detail.orderId,
        shippingMethod: 1,
        logisticsId: values.logisticsId,
        trackingNo: values.trackingNo,
        deliverData,
      });
      Toast.success({ content: '发货成功' });
      onSuccess();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '发货失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      title="订单发货"
      visible={visible}
      onCancel={onCancel}
      width={520}
      footer={
        <Space>
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            确认发货
          </Button>
        </Space>
      }
    >
      <Form getFormApi={setFormApi} labelPosition="top">
        <Form.Select
          field="logisticsId"
          label="物流公司"
          placeholder="请选择物流公司"
          filter
          optionList={logistics.map((item) => ({ label: item.name, value: item.id }))}
          rules={[{ required: true, message: '请选择物流公司' }]}
        />
        <Form.Input
          field="trackingNo"
          label="快递单号"
          placeholder="请输入快递单号"
          rules={[{ required: true, message: '请输入快递单号' }]}
        />
      </Form>
      <Text type="tertiary" size="small">
        发货数量按未发货数量自动填充。
      </Text>
    </Modal>
  );
}

function OrderNoteModal({
  visible,
  detail,
  onCancel,
  onSuccess,
}: {
  visible: boolean;
  detail: OrderDetail | null;
  onCancel: () => void;
  onSuccess: () => void;
}) {
  const [formApi, setFormApi] = useState<any>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (visible && formApi && detail) {
      formApi.setValues({ adminNote: detail.adminNote || '', mark: detail.mark || 0 }, { isOverride: true });
    }
  }, [visible, formApi, detail]);

  const submit = async () => {
    if (!detail) return;
    let values: Record<string, any>;
    try {
      values = await formApi.validate();
    } catch {
      return;
    }
    setSaving(true);
    try {
      await apiPost('/order/order/setAdminNote', {
        id: detail.orderId,
        adminNote: values.adminNote,
        mark: values.mark,
      });
      Toast.success({ content: '备注已保存' });
      onSuccess();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      title="订单备注"
      visible={visible}
      onCancel={onCancel}
      width={520}
      footer={
        <Space>
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            保存
          </Button>
        </Space>
      }
    >
      <Form getFormApi={setFormApi} labelPosition="top">
        <Form.Select
          field="mark"
          label="订单标记"
          optionList={[
            { label: '无标记', value: 0 },
            { label: '红色', value: 1 },
            { label: '黄色', value: 2 },
            { label: '绿色', value: 3 },
            { label: '蓝色', value: 4 },
            { label: '紫色', value: 5 },
          ]}
        />
        <Form.TextArea
          field="adminNote"
          label="商家备注"
          placeholder="仅后台可见"
          autosize={{ minRows: 4, maxRows: 8 }}
        />
      </Form>
    </Modal>
  );
}

function OrderMoneyModal({
  visible,
  detail,
  onCancel,
  onSuccess,
}: {
  visible: boolean;
  detail: OrderDetail | null;
  onCancel: () => void;
  onSuccess: () => void;
}) {
  const [formApi, setFormApi] = useState<any>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (visible && formApi && detail) {
      formApi.setValues(
        {
          shippingFee: Number(detail.shippingFee || 0),
          invoiceFee: Number(detail.invoiceFee || 0),
          serviceFee: Number(detail.serviceFee || 0),
          discountAmount: Number(detail.discountAmount || 0),
        },
        { isOverride: true },
      );
    }
  }, [visible, formApi, detail]);

  const submit = async () => {
    if (!detail) return;
    let values: Record<string, any>;
    try {
      values = await formApi.validate();
    } catch {
      return;
    }
    setSaving(true);
    try {
      await apiPost('/order/order/modifyMoney', {
        id: detail.orderId,
        shippingFee: values.shippingFee,
        invoiceFee: values.invoiceFee,
        serviceFee: values.serviceFee,
        discountAmount: values.discountAmount,
      });
      Toast.success({ content: '订单金额已更新' });
      onSuccess();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '改价失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      title="订单改价"
      visible={visible}
      onCancel={onCancel}
      width={520}
      footer={
        <Space>
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            保存
          </Button>
        </Space>
      }
    >
      <Form getFormApi={setFormApi} labelPosition="top">
        <Form.InputNumber field="shippingFee" label="运费" min={0} precision={2} style={{ width: '100%' }} />
        <Form.InputNumber field="invoiceFee" label="发票费" min={0} precision={2} style={{ width: '100%' }} />
        <Form.InputNumber field="serviceFee" label="服务费" min={0} precision={2} style={{ width: '100%' }} />
        <Form.InputNumber field="discountAmount" label="优惠金额" min={0} precision={2} style={{ width: '100%' }} />
      </Form>
      <Text type="tertiary" size="small">
        修改后将重新计算订单应付金额。
      </Text>
    </Modal>
  );
}

function OrderCancelModal({
  visible,
  detail,
  onCancel,
  onSuccess,
}: {
  visible: boolean;
  detail: OrderDetail | null;
  onCancel: () => void;
  onSuccess: () => void;
}) {
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (visible) setReason('');
  }, [visible]);

  const submit = async () => {
    if (!detail) return;
    if (!reason.trim()) {
      Toast.warning({ content: '请填写取消原因' });
      return;
    }
    setSaving(true);
    try {
      await apiPost('/order/order/cancelOrder', { id: detail.orderId, description: reason.trim() });
      Toast.success({ content: '订单已取消' });
      onSuccess();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '取消失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      title="取消订单"
      visible={visible}
      onCancel={onCancel}
      width={480}
      footer={
        <Space>
          <Button onClick={onCancel}>返回</Button>
          <Button theme="solid" type="danger" loading={saving} onClick={submit}>
            确认取消
          </Button>
        </Space>
      }
    >
      <Text type="warning">取消后订单将不再继续履约，请确认。</Text>
      <TextArea
        value={reason}
        onChange={setReason}
        placeholder="请填写取消原因"
        autosize={{ minRows: 3, maxRows: 6 }}
        style={{ marginTop: 12 }}
      />
    </Modal>
  );
}
