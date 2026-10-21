// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.common.config;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Tigshop团队
 * @create 2025/6/13 15:16
 */
public class TimestampToDateSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            if (value == null) {
                gen.writeString("");
                return;
            }
            long timestamp;
            if (value instanceof Number) {
                timestamp = ((Number) value).longValue();
            } else {
                timestamp = Long.parseLong(String.valueOf(value));
            }
            if (timestamp > 0) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("Asia/Shanghai"));
                String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                gen.writeString(formatted);
            }else {
                gen.writeString("");
            }
        } catch (Exception e) {
            // 如果解析失败，原样输出
            gen.writeString(String.valueOf(value));
        }
    }
}
