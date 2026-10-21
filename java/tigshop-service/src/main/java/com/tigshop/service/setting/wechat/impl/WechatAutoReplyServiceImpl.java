// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.setting.wechat.ReplyPayloadDTO;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.setting.ConfigMapper;
import com.tigshop.service.setting.wechat.WechatAutoReplyBizCodes;
import com.tigshop.service.setting.wechat.WechatAutoReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 公众号自动回复服务实现（M3）。
 * <p>
 * 存储模型：{@code config(biz_code, biz_val)}，biz_val 为 {@link ReplyPayloadDTO} 的 JSON。
 * 写操作使用 {@code synchronized + @Transactional} 串行化同实例并发；
 * 多实例部署需替换为分布式锁（见类内 TODO）。
 * <p>
 * BasePO 的审计字段（createTime/createById/createByName/updateTime/updateById/updateByName/isDel）
 * 由 {@code MybatisPlusMetaObjectHandler} 自动填充，故 insert/updateById 不手动赋值。
 *
 * <p>TODO: 多实例部署需替换 synchronized 为分布式锁（如 Redisson）。
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WechatAutoReplyServiceImpl implements WechatAutoReplyService {

    /** 文本内容最大长度 */
    private static final int MAX_CONTENT_LEN = 600;
    /** 图文标题最大长度 */
    private static final int MAX_NEWS_TITLE_LEN = 64;
    /** 图文摘要最大长度 */
    private static final int MAX_NEWS_DESC_LEN = 120;
    /** URL 字段最大长度（封面图、跳转链接） */
    private static final int MAX_URL_LEN = 500;

    private final ConfigMapper configMapper;

    @Override
    public ReplyPayloadVO getSubscribe() {
        return loadByCode(WechatAutoReplyBizCodes.SUBSCRIBE);
    }

    @Override
    public ReplyPayloadVO getDefault() {
        return loadByCode(WechatAutoReplyBizCodes.DEFAULT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void saveSubscribe(ReplyPayloadDTO dto) {
        upsert(WechatAutoReplyBizCodes.SUBSCRIBE, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void saveDefault(ReplyPayloadDTO dto) {
        upsert(WechatAutoReplyBizCodes.DEFAULT, dto);
    }

    @Override
    public ReplyPayloadVO getLoginSuccess() {
        return loadByCode(WechatAutoReplyBizCodes.LOGIN_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void saveLoginSuccess(ReplyPayloadDTO dto) {
        upsert(WechatAutoReplyBizCodes.LOGIN_SUCCESS, dto);
    }

    /**
     * 按 bizCode 读取并反序列化为 VO；记录不存在 / bizVal 为空 / 解析失败时回落到空载荷。
     */
    private ReplyPayloadVO loadByCode(String bizCode) {
        ConfigPO po = configMapper.selectOne(
                new LambdaQueryWrapper<ConfigPO>()
                        .eq(ConfigPO::getBizCode, bizCode)
                        .last("LIMIT 1"));

        if (po == null || StrUtil.isBlank(po.getBizVal())) {
            return defaultPayloadVO();
        }
        try {
            ReplyPayloadVO vo = JSON.parseObject(po.getBizVal(), ReplyPayloadVO.class);
            return vo == null ? defaultPayloadVO() : vo;
        } catch (Exception e) {
            log.warn("解析公众号自动回复配置失败，bizCode={}, bizVal={}", bizCode, po.getBizVal(), e);
            return defaultPayloadVO();
        }
    }

    /**
     * 空载荷：用于无配置或解析失败时返回，避免前端拿到 null。
     */
    private ReplyPayloadVO defaultPayloadVO() {
        ReplyPayloadVO vo = new ReplyPayloadVO();
        vo.setMsgType(1);
        vo.setContent("");
        return vo;
    }

    /**
     * 按 bizCode 维度的 upsert：存在则 updateById，不存在则 insert。
     * <p>
     * 在 {@link #saveSubscribe(ReplyPayloadDTO)} / {@link #saveDefault(ReplyPayloadDTO)}
     * 的 synchronized 临界区 + 事务边界内调用，事务由调用方的 {@link Transactional} 保证。
     */
    private void upsert(String bizCode, ReplyPayloadDTO dto) {
        validateReplyPayload(dto);

        String json = JSON.toJSONString(dto);

        ConfigPO existing = configMapper.selectOne(
                new LambdaQueryWrapper<ConfigPO>()
                        .eq(ConfigPO::getBizCode, bizCode)
                        .last("LIMIT 1"));

        if (existing != null) {
            existing.setBizVal(json);
            configMapper.updateById(existing);
        } else {
            configMapper.insert(new ConfigPO(bizCode, json));
        }
    }

    /**
     * 与 M2 关键字回复同款的六条校验规则：
     * <ol>
     *   <li>msgType 必填，且仅允许 1（文本）/ 2（图文）</li>
     *   <li>msgType=1 时 content 非空且 ≤ {@value #MAX_CONTENT_LEN}</li>
     *   <li>msgType=2 时 newsTitle 非空且 ≤ {@value #MAX_NEWS_TITLE_LEN}</li>
     *   <li>msgType=2 时 newsDescription 非空且 ≤ {@value #MAX_NEWS_DESC_LEN}</li>
     *   <li>msgType=2 时 newsCoverUrl 非空、≤ {@value #MAX_URL_LEN} 且为 http(s) 链接</li>
     *   <li>msgType=2 时 newsUrl 非空、≤ {@value #MAX_URL_LEN} 且为 http(s) 链接</li>
     * </ol>
     */
    private void validateReplyPayload(ReplyPayloadDTO dto) {
        if (dto == null || dto.getMsgType() == null) {
            throw new GlobalException("消息类型不能为空");
        }
        Integer msgType = dto.getMsgType();
        if (msgType != 1 && msgType != 2) {
            throw new GlobalException("消息类型仅支持 1=文本、2=图文");
        }
        if (msgType == 1) {
            if (StrUtil.isBlank(dto.getContent())) {
                throw new GlobalException("文本内容不能为空");
            }
            if (dto.getContent().length() > MAX_CONTENT_LEN) {
                throw new GlobalException("文本内容长度不能超过 " + MAX_CONTENT_LEN);
            }
            return;
        }
        // msgType == 2
        if (StrUtil.isBlank(dto.getNewsTitle()) || dto.getNewsTitle().length() > MAX_NEWS_TITLE_LEN) {
            throw new GlobalException("图文标题不能为空且长度不能超过 " + MAX_NEWS_TITLE_LEN);
        }
        if (StrUtil.isBlank(dto.getNewsDescription()) || dto.getNewsDescription().length() > MAX_NEWS_DESC_LEN) {
            throw new GlobalException("图文摘要不能为空且长度不能超过 " + MAX_NEWS_DESC_LEN);
        }
        if (!isValidHttpUrl(dto.getNewsCoverUrl())) {
            throw new GlobalException("图文封面图 URL 必须为合法的 http/https 链接，且长度不能超过 " + MAX_URL_LEN);
        }
        if (!isValidHttpUrl(dto.getNewsUrl())) {
            throw new GlobalException("图文跳转链接必须为合法的 http/https 链接，且长度不能超过 " + MAX_URL_LEN);
        }
    }

    private boolean isValidHttpUrl(String url) {
        return StrUtil.isNotBlank(url)
                && url.length() <= MAX_URL_LEN
                && (StrUtil.startWithIgnoreCase(url, "http://") || StrUtil.startWithIgnoreCase(url, "https://"));
    }
}
