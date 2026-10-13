// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.setting.MailTemplatesDTO;
import com.tigshop.bean.enums.setting.MailTemplateType;
import com.tigshop.bean.model.setting.MailTemplates;
import com.tigshop.bean.vo.setting.MailTemplatesVO;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.setting.MailTemplatesMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.MailTemplatesService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 邮箱模板
 *
 * @author Tigshop团队
 * @create 2024年12月24日 11:18
 */
@Service
public class MailTemplatesServiceImpl extends BaseServiceImpl<MailTemplatesMapper, MailTemplates> implements MailTemplatesService {
    @Resource
    private AdminLogService adminLogService;
    @Override
    public List<MailTemplatesVO> mailTemplatesList() {
        return this.list().stream().map(item -> {
            MailTemplatesVO vo = new MailTemplatesVO();
            BeanUtils.copyProperties(item, vo);
            String templateName = MailTemplateType.fromCode(item.getTemplateCode());
            vo.setTemplateName(templateName);
            return vo;
        }).toList();
    }

    @Override
    public void updateMailTemplates(MailTemplatesDTO dto) {
        MailTemplates mailTemplates = new MailTemplates();
        BeanUtils.copyProperties(dto, mailTemplates);
        // 更新时间
        mailTemplates.setLastModify(StringUtils.getCurrentTime());
        boolean isUpdated = this.updateById(mailTemplates);
        if (isUpdated){
            adminLogService.createByLogInfo(StrUtil.format("更新邮件模板设置:{}", getTemplateSubject(dto.getTemplateId())));
        }
    }

    /**
     * 获取邮件模板主题
     * @param templateId 邮件模板自增id
     * @return String
     */
    public String getTemplateSubject(Integer templateId) {
        return this.getById(templateId).getTemplateSubject();
    }
}
