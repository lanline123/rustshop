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
import com.tigshop.bean.dto.user.RankConfigInitDTO;
import com.tigshop.bean.dto.user.UserRankDTO;
import com.tigshop.bean.dto.user.UserRankListDTO;
import com.tigshop.bean.enums.user.RankGrowthLogTypeEnum;
import com.tigshop.bean.query.user.UserRankEditParam;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.vo.user.UserLevelInfoVO;
import com.tigshop.bean.vo.user.UserRankDetailVO;
import com.tigshop.bean.vo.user.UserRankListVO;
import com.tigshop.bean.vo.user.UserRankNotProListVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 会员等级服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
public interface UserRankService extends BaseService<UserRank> {

    /**
     * 会员等级列表（pro）
     */
    Page<UserRankListVO> list(UserRankListDTO dto);

    /**
     * 会员等级列表(非pro)
     */
    Page<UserRankNotProListVO> listNotPro(UserRankListDTO dto);

    /**
     * 创建会员等级
     */
    boolean create(UserRankDTO userRank);

    /**
     * 会员等级详情
     */
    UserRankDetailVO detail();

    /**
     * 会员等级更新
     */
    void update(UserRankEditParam param);

    /**
     * 会员等级配置信息
     */
    List<UserRank> getUserRank();

    /**
     * 根据用户积分判断是否升级用户等级
     */
    boolean updateUserRank(User user);

    /**
     * 会员等级配置信息
     */
    RankConfigInitDTO getRankConfigInit();

    /**
     * 获取用户权益信息
     */
    UserLevelInfoVO getRankInfo(Integer rankId);

    /**
     * 更新用户成长等级
     */
    void getRankGrowth(Integer userId);

    /**
     * 扣减用户成长值
     */
    void reduceGrowth(Long refundId);

    /**
     * 增加用户成长值
     */
    void addUserGrowthPoints(Integer userId, RankGrowthLogTypeEnum rankGrowthLogTypeEnum);
}
