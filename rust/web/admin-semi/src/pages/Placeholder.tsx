import { Card, Col, Row, Tag, Typography } from '@douyinfe/semi-ui';
import { IconApps } from '@douyinfe/semi-icons';
import { pageTitles } from '../config/adminMenu';

const { Title, Text, Paragraph } = Typography;

const features = ['列表页', '表单抽屉', '接口接入', 'PageSpec 审计'];

export default function Placeholder({ page }: { page: string }) {
  const title = pageTitles[page] || page;
  return (
    <div>
      <Card className="placeholder-hero" shadows="hover">
        <div className="placeholder-icon">
          <IconApps />
        </div>
        <div>
          <Title heading={3} style={{ margin: 0 }}>
            {title}
          </Title>
          <Paragraph type="tertiary" style={{ margin: '8px 0 0' }}>
            该模块正在迁移中，已按原版菜单保留入口。
          </Paragraph>
        </div>
        <Tag color="orange">迁移中</Tag>
      </Card>

      <Row gutter={16} style={{ marginTop: 20 }}>
        {features.map((feature) => (
          <Col span={6} key={feature}>
            <Card shadows="hover" className="feature-card">
              <Text strong>{feature}</Text>
              <Text type="tertiary" size="small" style={{ display: 'block', marginTop: 8 }}>
                功能对齐 Java Admin，使用 Semi 重新设计
              </Text>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
