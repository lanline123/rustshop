import { Card, Typography } from '@douyinfe/semi-ui';

const { Text } = Typography;

interface SupplyChainPageProps {
  title: string;
  description: string;
}

/**
 * 供应链模块占位页:后端 /purchase/* 路由未实现,前端先行占位。
 * 对应菜单键:purchaseOrderManage / purchaseReceiptManage / purchaseReturnManage /
 *            purchaseReconciliationManage / selfVendorManage / selfVendorCategoryManage /
 *            selfVendorProductManage / purchaseSettingManage
 */
export default function SupplyChainPage({ title, description }: SupplyChainPageProps) {
  return (
    <Card shadows="hover">
      <div style={{ padding: '48px 0', textAlign: 'center' }}>
        <Text strong style={{ fontSize: 16 }}>{title}</Text>
        <p style={{ marginTop: 12, color: 'var(--semi-color-text-2)' }}>{description}</p>
        <p style={{ marginTop: 8, color: 'var(--semi-color-warning)' }}>
          供应链模块后端接口(/purchase/*)尚未实现,待后端补齐后接入。
        </p>
      </div>
    </Card>
  );
}
