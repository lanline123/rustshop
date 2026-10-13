FROM rabbitmq:3.12-management

# 插件版本
ENV RABBITMQ_DELAYED_PLUGIN_VERSION=3.12.0

# 下载插件到官方插件目录
ADD https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v${RABBITMQ_DELAYED_PLUGIN_VERSION}/rabbitmq_delayed_message_exchange-${RABBITMQ_DELAYED_PLUGIN_VERSION}.ez /opt/rabbitmq/plugins/

# 设置权限（重要）
RUN chmod 644 /opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-${RABBITMQ_DELAYED_PLUGIN_VERSION}.ez

# 启用插件（RabbitMQ 默认会加载 /opt/rabbitmq/plugins）
RUN rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange
