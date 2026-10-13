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

package com.tigshop.service.decorate;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.decorate.DecorateCreateDTO;
import com.tigshop.bean.dto.decorate.DecorateListDTO;
import com.tigshop.bean.dto.decorate.DecorateShareInfoDTO;
import com.tigshop.bean.dto.decorate.DecorateUpdateDTO;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.vo.decorate.DecorateModuleListVO;
import com.tigshop.bean.vo.decorate.DecorateShareInfoVO;
import com.tigshop.bean.vo.decorate.DecorateVO;
import com.tigshop.service.common.BaseService;

import java.util.Map;

/**
 * 页面管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface DecorateService extends BaseService<Decorate> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<DecorateVO> list(DecorateListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    DecorateVO detail(Integer id, Integer localeId, Integer parentId, Integer decorateType);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return 新建装修页ID
     */
    Integer create(DecorateCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(DecorateUpdateDTO updateDTO);

    /**
     * 复制
     *
     * @param id 更新参数
     * @return boolean
     */
    boolean copy(Integer id);

    /**
     * 设置为首页
     *
     * @param id 更新参数
     * @param pageType 页面类型
     * @return boolean
     */
    boolean setHome(Integer id, Integer pageType);

    /**
     * 保存为草稿
     *
     * @param id 更新参数
     * @return boolean
     */
    boolean saveDecorateToDraft(Integer id, JSONObject data, Integer parentId, Integer localeId);

    /**
     * 发布
     * @param id 装修id
     * @param data 数据
     * @return boolean
     */
    boolean publishDecorate(Integer id, JSONObject data, Integer parentId, Integer localeId);

    /**
     * 初始化装修
     * @param shopId 店铺id
     */
    void initDecorate(Integer shopId);

    /**
     * 获取PC首页装修页面配置
     * @param id 装修id
     * @return JSONObject
     */
    JSONObject getPcIndexDecoratePageConfig(Integer id);

    /**
     * 店铺装修
     * @param shopId 店铺id
     * @param previewId 指定装修页id，为null时取该店铺已发布的首页装修
     * @return item
     */
    JSONObject getShopIndexDecorateData(Integer shopId, Integer previewId);

    /**
     * 获取页面模块
     * @param type 类型
     * @param isHome 是否是首页
     * @param status 状态
     * @return JSONObject
     */
    DecorateModuleListVO getDecorateModule(Integer type, Boolean isHome, Integer status);

    /**
     * 获取页面模块（按页面类型）
     * @param type 装修类型
     * @param pageType 页面类型
     * @param isHome 是否是首页
     * @param status 状态
     * @return DecorateModuleListVO
     */
    DecorateModuleListVO getDecorateModule(Integer type, Integer pageType, Boolean isHome, Integer status);

    /**
     * 按主键更新并在提交后失效首页装修缓存。
     * 供绑定/解绑/自定义页更新等直接操作 decorate 行、却会影响首页装修缓存输出（bindedH5Id 等）的写入使用，
     * 避免这些路径绕过 {@code update()} 导致 C 端缓存最长 5 分钟陈旧。
     *
     * @param decorate 待更新实体
     * @return 是否更新成功
     */
    boolean updateByIdRefreshHome(Decorate decorate);

    /**
     * 获取App首页装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getAppHomeDecorate();

    /**
     * 获取PC首页装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getPcHomeDecorate();

    /**
     * 获取PC登录页装修页面配置（decorateType=2, pageType=5）
     * 无生效记录时返回内置默认（现代版 loginMode=2 + 空背景），保证登录页不因未配置而报错
     * @return DecorateModuleListVO
     */
    DecorateModuleListVO getPcLoginDecorate();

    /**
     * 获取PC搜索页装修页面配置（decorateType=PC，pageType=6）
     * @param previewId 预览装修id（>0 时优先取搜索页草稿）
     * @return 装修模块数据；搜索页未配置时返回空 VO（不抛异常）
     */
    DecorateModuleListVO getPcSearchDecorate(Integer previewId);

    /**
     * 获取移动端搜索页装修页面配置（decorateType=H5，pageType=6）
     * @param previewId 预览装修id（>0 时优先取搜索页草稿）
     * @return 装修模块数据；搜索页未配置时返回空 VO（不抛异常）
     */
    DecorateModuleListVO getH5SearchDecorate(Integer previewId);

    /**
     * 获取PC首页头部装修配置（pageModule.pcHeader）
     * @param previewId 预览装修id（>0 时优先取草稿，回退已发布）
     * @return pcHeader 配置对象，未配置返回 null
     */
    Object getPcHeader(Integer previewId);

    /**
     * 获取PC预览装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getPcPreviewDecorate(Integer decorateId,Integer id, Integer pageType);

    /**
     * 获取App预览装修页面配置
     * @return JSONObject
     */
    DecorateModuleListVO getAppPreviewDecorate(Integer decorateId,Integer pageType);

    /**
     * 获取预览装修页面配置
     * @param type 类型
     * @param decorateId 装修id
     * @param id 如果id != null则返回decorate date 数据，否则返回decorate draft_data
     * @return JSONObject
     */
    DecorateModuleListVO getPreviewDecorate(Integer type, Integer decorateId, Integer id,Integer pageType);

    JSONObject loadDraftData(Integer id);

    Object getPreviewDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params);

    Object getDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params);

    DecorateModuleListVO customDecorate(Integer previewId);

    /**
     * 保存装修分享信息
     */
    boolean saveShareInfo(DecorateShareInfoDTO dto);

    /**
     * 获取装修分享信息
     */
    DecorateShareInfoVO getShareInfo(Integer decorateId);
}
