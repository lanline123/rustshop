import { Button, Card, Col, Row, Tag, Typography } from '@douyinfe/semi-ui';
import {
  IconComment,
  IconCreditCard,
  IconGift,
  IconSetting,
} from '@douyinfe/semi-icons';

const { Title, Text, Paragraph } = Typography;

const config: Record<string, { icon: React.ReactNode; title: string; desc: string; features: string[] }> = {
  aftersales: {
    icon: <IconComment />,
    title: '售后管理',
    desc: '统一处理退款、退货、换货与售后协商。',
    features: ['售后申请列表', '退款审核', '退货物流', '售后协商记录'],
  },
  marketing: {
    icon: <IconGift />,
    title: '营销中心',
    desc: '优惠券、秒杀、拼团、满减等营销工具。',
    features: ['优惠券管理', '秒杀活动', '拼团活动', '会员营销'],
  },
  finance: {
    icon: <IconCreditCard />,
    title: '财务中心',
    desc: '订单收款、退款、对账与发票管理。',
    features: ['财务流水', '退款记录', '发票管理', '对账单'],
  },
  settings: {
    icon: <IconSetting />,
    title: '系统设置',
    desc: '站点配置、权限、角色与系统参数。',
    features: ['基础设置', '管理员与角色', '权限分配', '操作日志'],
  },
};

export default function Placeholder({ page }: { page: string }) {
  const item = config[page] || config.settings;
  return (
    <div>
      <Card className="placeholder-hero" shadows="hover">
        <div className="placeholder-icon">{item.icon}</div>
        <div>
          <Title heading={3} style={{ margin: 0 }}>
            {item.title}
          </Title>
          <Paragraph type="tertiary" style={{ margin: '8px 0 0' }}>
            {item.desc}
          </Paragraph>
        </div>
        <Tag color="orange">规划中</Tag>
      </Card>

      <Row gutter={16} style={{ marginTop: 20 }}>
        {item.features.map((feature) => (
          <Col span={6} key={feature}>
            <Card shadows="hover" className="feature-card">
              <Text strong>{feature}</Text>
              <Text type="tertiary" size="small" style={{ display: 'block', marginTop: 8 }}>
                功能对齐 Java Admin，使用 Semi 重新设计
              </Text>
              <Button theme="borderless" size="small" style={{ marginTop: 12, paddingLeft: 0 }}>
                查看规划
              </Button>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
