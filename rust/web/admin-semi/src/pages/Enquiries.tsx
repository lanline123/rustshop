import { useState } from 'react';
import {
  Button,
  Modal,
  Space,
  TextArea,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

interface EnquiryRow {
  id: number;
  productId?: number;
  product?: { productName?: string; productSn?: string; picThumb?: string };
  content?: string;
  replyContent?: string;
  status?: number;
  mobile?: string;
  createTime?: string;
}

/** 商品询价：对应旧版 product/enquiry/List.vue + Info.vue，端点 /product/priceInquiry/*。 */
export default function Enquiries() {
  const [reloadKey, setReloadKey] = useState(0);
  const [replyTarget, setReplyTarget] = useState<EnquiryRow | null>(null);
  const [replyContent, setReplyContent] = useState('');
  const [replySaving, setReplySaving] = useState(false);

  const reload = () => setReloadKey((key) => key + 1);

  const openReply = async (row: EnquiryRow) => {
    setReplyContent(row.replyContent || '');
    setReplyTarget(row);
    try {
      const detail = await apiGet<any>('/product/priceInquiry/detail', { id: row.id });
      setReplyTarget({ ...row, ...(detail || {}) });
    } catch {
      // 保留行数据兜底
    }
  };

  const submitReply = async () => {
    if (!replyTarget) return;
    if (!replyContent.trim()) {
      Toast.error({ content: '回复内容不能为空' });
      return;
    }
    setReplySaving(true);
    try {
      await apiPost('/product/priceInquiry/reply', { id: replyTarget.id, replyContent });
      Toast.success({ content: '操作成功' });
      setReplyTarget(null);
      reload();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '回复失败' });
    } finally {
      setReplySaving(false);
    }
  };

  const deleteEnquiry = (row: EnquiryRow) => {
    Modal.confirm({
      title: '确认删除该询价吗？',
      onOk: async () => {
        try {
          await apiPost('/product/priceInquiry/del', { id: row.id });
          Toast.success({ content: '删除成功' });
          reload();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
        }
      },
    });
  };

  const columns: ColumnConfig<EnquiryRow>[] = [
    {
      title: '商品名称',
      dataIndex: 'productId',
      width: 300,
      render: (_: unknown, row: EnquiryRow) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {row.product?.picThumb ? (
            <img src={row.product.picThumb} alt="" width={40} height={40} style={{ borderRadius: 4, objectFit: 'cover' }} />
          ) : null}
          <div>
            <div>商品名称：{row.product?.productName || '-'}</div>
            <Text type="tertiary" size="small">
              商品SN：{row.product?.productSn || '-'}
            </Text>
          </div>
        </div>
      ),
    },
    {
      title: '询价内容',
      dataIndex: 'content',
      width: 260,
      render: (value: string) => <span>{value || '-'}</span>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (value: number) =>
        Number(value) === 1 ? (
          <span style={{ color: 'var(--semi-color-success)' }}>已回复</span>
        ) : (
          <span style={{ color: 'var(--semi-color-text-2)' }}>未回复</span>
        ),
    },
    {
      title: '联系电话',
      dataIndex: 'mobile',
      width: 150,
      render: (value: string) => <span>{value || '-'}</span>,
    },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
  ];

  return (
    <>
      <ListPage<EnquiryRow>
      key={reloadKey}
      api="/product/priceInquiry/list"
      rowKey="id"
      pageSize={10}
      actionWidth={150}
      columns={columns}
      rowActions={(row) => (
        <Space spacing={2}>
          <Button theme="borderless" size="small" onClick={() => openReply(row)}>
            {Number(row.status) === 1 ? '详情' : '回复'}
          </Button>
          <Button theme="borderless" type="danger" size="small" onClick={() => deleteEnquiry(row)}>
            删除
          </Button>
        </Space>
      )}
      />
      <Modal
        title={
          replyTarget && Number(replyTarget.status) === 1
            ? '查看询价'
            : '回复询价'
        }
        visible={Boolean(replyTarget)}
        confirmLoading={replySaving}
        onOk={submitReply}
        onCancel={() => setReplyTarget(null)}
        okText={replyTarget && Number(replyTarget.status) === 1 ? '关闭' : '提交回复'}
      >
        {replyTarget ? (
          <div className="enquiry-form">
            <Text strong>
              类型：商品【{replyTarget.product?.productName || '-'}】
            </Text>
            <div>
              <Text type="tertiary">联系电话：</Text>
              <span>{replyTarget.mobile || '-'}</span>
            </div>
            <div>
              <Text type="tertiary">邮箱：</Text>
              <span>{(replyTarget as any).email || '-'}</span>
            </div>
            <div>
              <Text type="tertiary">询价内容：</Text>
              <span>{replyTarget.content || '-'}</span>
            </div>
            <div>
              <Text type="tertiary">创建时间：</Text>
              <span>{replyTarget.createTime || '-'}</span>
            </div>
            {Number(replyTarget.status) === 1 ? (
              <div>
                <Text type="tertiary">回复内容：</Text>
                <span>{replyTarget.replyContent || '-'}</span>
              </div>
            ) : (
              <>
                <Text strong>回复内容</Text>
                <TextArea
                  value={replyContent}
                  onChange={setReplyContent}
                  placeholder="请输入回复内容"
                  autosize={{ minRows: 4, maxRows: 8 }}
                />
              </>
            )}
          </div>
        ) : null}
      </Modal>
    </>
  );
}
