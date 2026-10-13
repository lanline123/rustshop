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

package com.tigshop.service.decorate.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.decorate.DecorateShareCreateDTO;
import com.tigshop.bean.dto.decorate.DecorateShareListDTO;
import com.tigshop.bean.dto.decorate.DecorateShareUpdateDTO;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.model.decorate.DecorateShare;
import com.tigshop.bean.vo.decorate.DecorateShareVO;
import com.tigshop.bean.vo.decorate.DecorateVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.decorate.DecorateShareMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.decorate.DecorateShareService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.tigshop.common.constant.decorate.DecorateShareConstants.*;

/**
 * 售后申请表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class DecorateShareServiceImpl extends BaseServiceImpl<DecorateShareMapper, DecorateShare> implements DecorateShareService {
    @Resource
    RedisCache redisCache;
    @Resource
    DecorateService decorateService;

    @Override
    public Page<DecorateShareVO> list(DecorateShareListDTO listDTO) {
        // 分页
        Page<DecorateShare> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<DecorateShare> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (IsShowType.fromTypeCode(isShow) != null) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like("", keyword);
        }*/
        // 执行查询
        Page<DecorateShare> decorateSharePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<DecorateShare> decorateSharePageRecords = decorateSharePage.getRecords();
        // 转换为VO
        List<DecorateShareVO> decorateShareVOList = decorateSharePageRecords.stream()
                .map(decorateShare -> {
                    DecorateShareVO decorateShareVO = new DecorateShareVO();
                    BeanUtils.copyProperties(decorateShare, decorateShareVO);
                    return decorateShareVO;
                }).toList();
        return PageUtil.convertPage(decorateSharePage, decorateShareVOList);
    }

    @Override
    public DecorateShareVO detail(Integer id) {
        if (id != null) {
            DecorateShare decorateShare = this.getById(id);
            DecorateShareVO decorateShareVO = new DecorateShareVO();
            BeanUtils.copyProperties(decorateShare, decorateShareVO);
            return decorateShareVO;
        }
        return null;
    }

    @Override
    public boolean create(DecorateShareCreateDTO createDTO) {
        if (createDTO != null) {
            DecorateShare decorateShare = new DecorateShare();
            BeanUtils.copyProperties(createDTO, decorateShare);
            return this.save(decorateShare);
        }
        return false;
    }

    @Override
    public boolean update(DecorateShareUpdateDTO updateDTO) {
        if (updateDTO != null) {
            DecorateShare decorateShare = new DecorateShare();
            BeanUtils.copyProperties(updateDTO, decorateShare);
            return this.updateById(decorateShare);
        }
        return false;
    }

    @Override
    public DecorateShareVO share(int decorateId) {
        String sn = generateUniqueCode(CODE_LENGTH);
        String token = generateUniqueCode(TOKEN_CODE_LENGTH);
        long time = StringUtils.getCurrentTime() + EXPIRE_TIME;
        //存入redis
        redisCache.setCacheObject(sn + token, decorateId);
        //设置过期时间
        redisCache.expire(sn + token, EXPIRE_TIME, TimeUnit.SECONDS);
        //将数据存入数据表
        DecorateShare decorateShare = new DecorateShare();
        decorateShare.setShareSn(sn);
        decorateShare.setShareToken(token);
        decorateShare.setDecorateId(decorateId);
        decorateShare.setValidTime(time);
        decorateShare.setCreateTime(StringUtils.getCurrentTime());
        decorateShare.setUpdateTime(StringUtils.getCurrentTime());
        this.save(decorateShare);
        DecorateShareVO decorateShareVO = new DecorateShareVO();
        decorateShareVO.setSn(sn);
        decorateShareVO.setToken(token);
        decorateShareVO.setApiUrl(String.format(API_URL, sn, token));
        return decorateShareVO;
    }

    @Override
    public boolean importUrl(String url) {
        // 1. 解析URL
        String response = HttpUtil.get(url);
        if (!JSONUtil.isTypeJSON(response)) {
            throw new GlobalException("返回结果有误！");
        }
        JSONObject resJson = JSON.parseObject(response);
        JSONObject decorate = resJson.getJSONObject("data");
        if (decorate == null) {
            throw new GlobalException("未获取到有用的模板信息，请重新导入分享模板链接！");
        }

        // 3. 构建数据
        Decorate decorateModel = new Decorate();
        decorateModel.setDecorateTitle(decorate.getString("decorateTitle"));
        decorateModel.setData(decorate.getString("data"));
        decorateModel.setDraftData(decorate.getString("draftData"));
        decorateModel.setDecorateType(decorate.getInteger("decorateType"));
        decorateModel.setShopId(HeaderUtils.getShopId());
        decorateModel.setUpdateTime(System.currentTimeMillis() / 1000);
        return decorateService.save(decorateModel);
    }

    @Override
    public Object getInfoBySn(String sn, String token) {
        if (sn == null || sn.isEmpty() || token == null || token.isEmpty()) {
            throw new GlobalException("参数缺失！");
        }

        // 尝试从缓存中获取sn
        Integer decorateId = redisCache.getCacheObject(sn + token);
        if (decorateId == null) {
            // 缓存没有，查数据库
            DecorateShare one = getOne(new LambdaQueryWrapper<DecorateShare>().eq(DecorateShare::getShareToken, token));
            if (one == null ) {
                throw new GlobalException("分享链接不存在或已失效!");
            }
            decorateId = one.getDecorateId();
        }

        DecorateVO decorate = decorateService.detail(decorateId, 0, 0, 0);
        if (decorate == null) {
            throw new GlobalException("未查询到分享装修信息!");
        }
        return decorate;
    }


    /**
     * 获取随机6位字符串
     *
     * @return string
     */
    public String generateUniqueCode(Integer codeLength) {
        // 获取当前时间的微秒部分
        long micros = System.nanoTime() / 1000;

        // 将微秒时间转换为字符串
        String microsStr = String.valueOf(micros);

        // 创建一个随机数生成器，使用微秒时间作为种子
        Random random = new Random(micros);

        // 创建一个StringBuilder来构建最终的字符串
        StringBuilder sb = new StringBuilder();

        // 将字符集和微秒时间组合
        sb.append(CHARACTERS);
        sb.append(microsStr);

        // 随机选择6位字符
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            int index = random.nextInt(sb.length());
            code.append(sb.charAt(index));
        }

        return code.toString();
    }

    /**
     * 检查是否是有效的URL并解析URL
     *
     * @param url 要解析的URL字符串
     * @return 包含基础URL和查询参数的Map
     * @throws IllegalArgumentException 如果URL无效
     */
    public static Map<String, Object> parseUrl(String url) {
        try {
            URI uri = new URI(url);

            // 获取基础URL（协议 + 主机名 + 路径）
            String baseUrl = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "") + uri.getPath();

            // 获取查询参数部分
            String queryParams = uri.getQuery();

            // 将查询参数解析为关联数组
            Map<String, String> paramsArray = new HashMap<>();
            if (queryParams != null) {
                String[] pairs = queryParams.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String key = pair.substring(0, idx);
                        String value = pair.substring(idx + 1);
                        paramsArray.put(key, value);
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("base_url", baseUrl);
            result.put("query_params", paramsArray);
            return result;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("无效的链接!", e);
        }
    }

}
