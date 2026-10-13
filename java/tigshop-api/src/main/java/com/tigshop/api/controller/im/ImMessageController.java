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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.ImMessageListDTO;
import com.tigshop.bean.dto.im.ImMessageSendDTO;
import com.tigshop.bean.dto.im.ImSetReadDTO;
import com.tigshop.bean.vo.im.ImMessageListVO;
import com.tigshop.bean.vo.im.ImMessageSendVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.service.im.ImMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.Constants.X_CLIENT_TYPE;

/**
 * IM消息制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/im/conversation/message")
@Tag(name = "消息列表", description = "消息列表")
public class ImMessageController {
    @Resource
    private ImMessageService imMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public ImMessageSendVO send(@RequestBody ImMessageSendDTO dto){
        boolean isAdmin = TigUtils.getClientTypeByToken();
        if(isAdmin) {
            Integer shopId =  HeaderUtils.getShopId();
            if (shopId != null) {
                dto.setShopId(shopId);
            }
            dto.setType(2);
            dto.setServantId(SecurityUtils.getCurrentAdminId());
        } else {
            dto.setType(1);
            dto.setUserId(SecurityUtils.getCurrentUserId());
        }
        dto.setUserFrom(HeaderUtils.getHeaderValue(X_CLIENT_TYPE));
        return imMessageService.send(dto);
    }

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ImMessageListVO> list(ImMessageListDTO listDTO) {
        //判断是否是后台
        boolean isAdmin = TigUtils.getClientTypeByToken();
        if (isAdmin) {
            listDTO.setServantId(SecurityUtils.getCurrentAdminId());
        } else {
            listDTO.setUserId(SecurityUtils.getCurrentUserId());
        }
        return imMessageService.list(listDTO);
    }

    @PostMapping("/setRead")
    @Operation(summary = "设置已读")
    public boolean setRead(@RequestBody ImSetReadDTO dto) {
        return imMessageService.setRead(dto);
    }
}
