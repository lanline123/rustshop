// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.im;

import com.tigshop.api.socket.WebSocketServerHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket 消息控制器
 * <p>
 * 注意：此接口仅供内部服务调用，需要在请求头中携带内部 API 密钥
 * <p>
 * 调用方式：
 * <pre>
 * GET /api/socket/send?targetUserId=xxx&message=xxx
 * Header: X-Internal-Api-Key: your-internal-api-key
 * </pre>
 *
 * @author Tigshop团队
 */
@RestController
@Tag(name = "消息控制器（内部接口）", description = "仅供内部服务调用，需携带 X-Internal-Api-Key 请求头")
@RequestMapping("/api/socket")
public class WebSocketController {

    private final WebSocketServerHandler webSocketServerHandler;

    public WebSocketController(WebSocketServerHandler webSocketServerHandler) {
        this.webSocketServerHandler = webSocketServerHandler;
    }

    /**
     * 服务器端发送消息给指定用户
     * <p>
     * 此接口需要内部 API 密钥认证，请在请求头中添加：
     * X-Internal-Api-Key: {your-internal-api-key}
     *
     * @param targetUserId 目标用户ID，格式为 "isAdmin::userId"，例如 "false::123" 或 "true::1"
     * @param message      要发送的消息内容
     * @return 发送结果
     */
    @GetMapping("/send")
    @Operation(summary = "服务器端发送消息", description = "内部接口，需要 X-Internal-Api-Key 请求头认证")
    public String sendMessage(@RequestParam String targetUserId, @RequestParam String message) {
        webSocketServerHandler.sendMessageToUser(targetUserId, message);
        return "已发送消息给用户：" + targetUserId;
    }
}