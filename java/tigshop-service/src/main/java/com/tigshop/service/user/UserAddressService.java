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

package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserAddressCreateDTO;
import com.tigshop.bean.dto.user.UserAddressListDTO;
import com.tigshop.bean.dto.user.UserAddressUpdateDTO;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 收货人信息表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserAddressService extends BaseService<UserAddress> {
    /**
     * 获取常用收货地址标签列表（后端预设固定项）
     *
     * @return 标签列表，如 [家, 公司, 学校]
     */
    List<String> getAddressTagList();

    /**
     * 获取列表
     *
     * @param listDTO 参数
     * @return ListResVO
     */
    Page<UserAddressVO> list(UserAddressListDTO listDTO);

    /**
     * 收货地址详情
     *
     * @param id 收货地址id
     * @return ItemVO
     */
    UserAddressVO detail(Integer id);

    /**
     * 收货地址详情（带用户归属校验）
     *
     * @param id     收货地址id
     * @param userId 当前用户id，非空且非0时校验地址归属
     * @return ItemVO
     */
    UserAddressVO detail(Integer id, Integer userId);

    /**
     * 创建收货地址
     * @param createDTO 创建收货地址参数
     * @return boolean
     */
    Integer create(UserAddressCreateDTO createDTO);

    /**
     *
     * @param updateDTO 修改收货地址参数
     * @return boolean
     */
    boolean update(UserAddressUpdateDTO updateDTO);

    /**
     * 设置选中
     */
    void setSelected(Integer id);

    /**
     * 获取选中的收货地址
     */
    UserAddressVO getUserSelectedAddress();

    UserAddressVO getUserSystemSelectedAddress(Integer userId);

}
