package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.PickupContactCreateDTO;
import com.tigshop.bean.dto.user.PickupContactListDTO;
import com.tigshop.bean.dto.user.PickupContactUpdateDTO;
import com.tigshop.bean.model.user.PickupContact;
import com.tigshop.bean.vo.user.PickupContactVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 自提联系人服务接口
 *
 * @author Tigshop
 */
public interface PickupContactService extends BaseService<PickupContact> {

    /**
     * 自提联系人分页列表
     *
     * @param listDTO 查询参数
     * @return 分页结果
     */
    Page<PickupContactVO> list(PickupContactListDTO listDTO);

    /**
     * 获取用户的全部自提联系人
     *
     * @param userId 用户ID
     * @return 联系人列表
     */
    List<PickupContactVO> listByUserId(Integer userId);

    /**
     * 自提联系人详情
     *
     * @param id 主键ID
     * @return 联系人信息
     */
    PickupContactVO detail(Long id);

    /**
     * 创建自提联系人
     *
     * @param createDTO 创建参数
     * @return 新记录ID
     */
    Long create(PickupContactCreateDTO createDTO);

    /**
     * 更新自提联系人
     *
     * @param updateDTO 更新参数
     * @return 是否成功
     */
    boolean update(PickupContactUpdateDTO updateDTO);

    /**
     * 删除自提联系人
     *
     * @param id 主键ID
     * @return 是否成功
     */
    boolean del(Long id);
}
