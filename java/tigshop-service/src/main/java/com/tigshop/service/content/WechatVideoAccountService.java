package com.tigshop.service.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.content.WechatVideoAccountCreateDTO;
import com.tigshop.bean.dto.content.WechatVideoAccountListDTO;
import com.tigshop.bean.dto.content.WechatVideoAccountUpdateDTO;
import com.tigshop.bean.model.content.WechatVideoAccount;
import com.tigshop.bean.vo.content.WechatVideoAccountVO;
import com.tigshop.service.common.BaseService;

/**
 * 视频号账号服务
 */
public interface WechatVideoAccountService extends BaseService<WechatVideoAccount> {
    Page<WechatVideoAccountVO> list(WechatVideoAccountListDTO listDTO);

    WechatVideoAccountVO detail(Integer id);

    boolean create(WechatVideoAccountCreateDTO createDTO);

    boolean update(WechatVideoAccountUpdateDTO updateDTO);

    boolean delete(Integer id);
}
