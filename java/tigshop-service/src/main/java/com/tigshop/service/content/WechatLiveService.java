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

package com.tigshop.service.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.content.*;
import com.tigshop.bean.dto.content.WechatLiveListDTO;
import com.tigshop.bean.dto.content.WechatLiveRoomCreateDTO;
import com.tigshop.bean.dto.content.WechatLiveUpdateDTO;
import com.tigshop.bean.feign.wechat.GetFinderLiveRecordListResult;
import com.tigshop.bean.model.content.WechatLive;
import com.tigshop.bean.vo.content.WechatLiveGoodsVO;
import com.tigshop.bean.vo.content.WechatLivePromoteVO;
import com.tigshop.bean.vo.content.WechatLivePushUrlVO;
import com.tigshop.bean.vo.content.WechatLiveVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 微信直播服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface WechatLiveService extends BaseService<WechatLive> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<WechatLiveVO> list(WechatLiveListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    WechatLiveVO detail(Integer id);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(WechatLiveUpdateDTO updateDTO);

    /**
     * 直播刷新
     *
     * @return boolean
     */
    boolean refresh();

    /**
     * 创建直播间
     *
     * @param createDTO 创建参数
     * @return 创建成功的直播间ID
     */
    Integer create(WechatLiveRoomCreateDTO createDTO);

    /**
     * 删除直播间
     *
     * @param id 主键ID
     * @return boolean
     */
    boolean delete(Integer id);

    /**
     * 获取推流地址
     *
     * @param roomId 直播间ID
     * @return 推流信息
     */
    WechatLivePushUrlVO getPushUrl(Integer roomId);

    /**
     * 获取直播间推广信息
     *
     * @param roomId 直播间ID
     * @return 推广信息
     */
    WechatLivePromoteVO getPromoteData(Integer roomId);

    /**
     * 从微信同步直播间到系统
     *
     * @return boolean
     */
    boolean syncFromWechat();

    /**
     * 将系统直播间同步到微信
     *
     * @param id 主键ID
     * @return boolean
     */
    boolean syncToWechat(Integer id);

    /**
     * 导入直播商品主表
     *
     * @param goodsItems 系统商品项列表
     * @return boolean
     */
    boolean importGoods(List<WechatLiveGoodsItemDTO> goodsItems);

    /**
     * 获取直播商品主表列表
     *
     * @param listDTO 查询参数
     * @return 商品列表
     */
    Page<WechatLiveGoodsVO> getGoodsLibrary(WechatLiveGoodsListDTO listDTO);

    /**
     * 查询直播商品（带直播间加入状态）
     *
     * @param queryDTO 查询参数
     * @return 商品列表
     */
    Page<WechatLiveGoodsVO> queryGoods(WechatLiveGoodsQueryDTO queryDTO);

    /**
     * 获取直播间商品列表
     *
     * @param roomId 直播间ID
     * @return 商品列表
     */
    List<WechatLiveGoodsVO> getRoomGoods(Integer roomId);

    /**
     * 添加已导入商品到直播间
     *
     * @param roomId   直播间ID
     * @param goodsIds 直播商品主表ID列表
     * @return boolean
     */
    boolean addGoods(Integer roomId, List<Long> goodsIds);

    /**
     * 从直播间移除商品
     *
     * @param roomId  直播间ID
     * @param goodsId 直播商品主表ID
     * @return boolean
     */
    boolean removeGoods(Integer roomId, Long goodsId);

    /**
     * 获取视频号当前的直播记录
     *
     * @return 当前直播记录，无直播时返回null
     */
    GetFinderLiveRecordListResult.LiveRecord getFinderLiveRecord(String id);
}
