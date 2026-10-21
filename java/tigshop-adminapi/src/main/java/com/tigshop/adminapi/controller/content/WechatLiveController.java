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
package com.tigshop.adminapi.controller.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.content.WechatLiveGoodsAddDTO;
import com.tigshop.bean.dto.content.WechatLiveListDTO;
import com.tigshop.bean.dto.content.WechatLiveRoomCreateDTO;
import com.tigshop.bean.dto.content.WechatLiveUpdateDTO;
import com.tigshop.bean.feign.wechat.GetFinderLiveRecordListResult;
import com.tigshop.bean.vo.content.WechatLiveGoodsVO;
import com.tigshop.bean.vo.content.WechatLivePromoteVO;
import com.tigshop.bean.vo.content.WechatLivePushUrlVO;
import com.tigshop.bean.vo.content.WechatLiveVO;
import com.tigshop.service.content.WechatLiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信直播控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/content/wechatLive")
@Tag(name = "微信直播", description = "微信直播管理接口")
@PreAuthorize("@pms.hasPermission('wechatLive')")
@Validated
public class WechatLiveController {
    @Resource
    private WechatLiveService wechatLiveService;

    /**
     * 获取直播间列表
     * <p>请求参数格式：Query（URL参数）</p>
     * @param listDTO 查询条件
     * @return 分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取直播间列表", description = "请求参数格式：Query（URL参数）")
    public Page<WechatLiveVO> list(WechatLiveListDTO listDTO) {
        return wechatLiveService.list(listDTO);
    }

    /**
     * 获取直播间详情
     * <p>请求参数格式：Query（URL参数）</p>
     * @param id 直播间ID
     * @return 直播间详情
     */
    @GetMapping("/detail")
    @Operation(summary = "获取直播间详情", description = "请求参数格式：Query（URL参数）")
    public WechatLiveVO detail(@RequestParam Integer id) {
        return wechatLiveService.detail(id);
    }

    /**
     * 更新直播间
     * <p>请求体格式：JSON</p>
     * @param updateDTO 更新参数
     */
    @PostMapping("/update")
    @Operation(summary = "更新直播间", description = "请求体格式：JSON")
    @PreAuthorize("@pms.hasPermission('wechatLiveModifyManage')")
    public void update(@Valid @RequestBody WechatLiveUpdateDTO updateDTO) {
        wechatLiveService.update(updateDTO);
    }

    /**
     * 刷新直播间信息
     * <p>请求参数格式：无参数</p>
     */
    @GetMapping("/refresh")
    @Operation(summary = "刷新直播间信息", description = "请求参数格式：无参数")
    public void refresh() {
        wechatLiveService.refresh();
    }

    /**
     * 创建直播间
     * <p>请求体格式：JSON</p>
     * @param createDTO 创建参数
     * @return 直播间ID
     */
    @PostMapping("/create")
    @Operation(summary = "创建直播间", description = "请求体格式：JSON")
    @PreAuthorize("@pms.hasPermission('wechatLiveModifyManage')")
    public Integer create(@Valid @RequestBody WechatLiveRoomCreateDTO createDTO) {
        return wechatLiveService.create(createDTO);
    }

    /**
     * 删除直播间
     * <p>请求参数格式：Query（URL参数）</p>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除直播间", description = "请求参数格式：Query（URL参数）")
    @PreAuthorize("@pms.hasPermission('wechatLiveModifyManage')")
    public void delete(@RequestBody OperateDTO dto) {
        wechatLiveService.delete(dto.getId());
    }

    /**
     * 获取推流地址
     * <p>请求参数格式：Path（路径参数）</p>
     * @param roomId 直播间ID
     * @return 推流地址信息
     */
    @GetMapping("/pushUrl/{roomId}")
    @Operation(summary = "获取推流地址", description = "请求参数格式：Path（路径参数）")
    public WechatLivePushUrlVO getPushUrl(@PathVariable Integer roomId) {
        return wechatLiveService.getPushUrl(roomId);
    }

    /**
     * 获取直播间推广信息
     * <p>请求参数格式：Path（路径参数）</p>
     * @param roomId 直播间ID
     * @return 推广信息
     */
    @GetMapping("/promote/{roomId}")
    @Operation(summary = "获取直播间推广信息", description = "请求参数格式：Path（路径参数）")
    public WechatLivePromoteVO getPromoteData(@PathVariable Integer roomId) {
        return wechatLiveService.getPromoteData(roomId);
    }

    /**
     * 从微信同步直播间
     * <p>请求参数格式：无参数</p>
     */
    @GetMapping("/syncFromWechat")
    @Operation(summary = "从微信同步直播间", description = "请求参数格式：无参数")
    public void syncFromWechat() {
        wechatLiveService.syncFromWechat();
    }

    /**
     * 同步到微信
     * <p>请求参数格式：Query（URL参数）</p>
     * @param id 直播间ID
     */
    @PostMapping("/syncToWechat")
    @Operation(summary = "同步到微信", description = "请求参数格式：Query（URL参数）")
    @PreAuthorize("@pms.hasPermission('wechatLiveModifyManage')")
    public void syncToWechat(@RequestParam Integer id) {
        wechatLiveService.syncToWechat(id);
    }

    /**
     * 添加已导入商品到直播间
     * <p>请求体格式：JSON</p>
     * @param addDTO 添加商品参数
     */
    @PostMapping("/addToRoom")
    @Operation(summary = "添加已导入商品到直播间", description = "请求体格式：JSON")
    @PreAuthorize("@pms.hasPermission('wechatLiveModifyManage')")
    public void addGoods(@Valid @RequestBody WechatLiveGoodsAddDTO addDTO) {
        wechatLiveService.addGoods(addDTO.getRoomId(), addDTO.getGoodsIds());
    }

    /**
     * 从直播间移除商品
     * <p>请求参数格式：Query（URL参数）</p>
     * @param goodsId 直播商品主表ID
     * @param roomId 直播间ID
     */
    @PostMapping("/removeFromRoom")
    @Operation(summary = "从直播间移除商品", description = "请求参数格式：Query（URL参数）")
    @PreAuthorize("@pms.hasPermission('wechatLiveModifyManage')")
    public void removeGoods(@RequestParam Long goodsId,
                            @RequestParam Integer roomId) {
        wechatLiveService.removeGoods(roomId, goodsId);
    }

    /**
     * 获取直播间商品列表
     * <p>请求参数格式：Path（路径参数）</p>
     * @param roomId 直播间ID
     * @return 商品列表
     */
    @GetMapping("/room/{roomId}")
    @Operation(summary = "获取直播间商品列表", description = "请求参数格式：Path（路径参数）")
    public List<WechatLiveGoodsVO> getGoods(@PathVariable Integer roomId) {
        return wechatLiveService.getRoomGoods(roomId);
    }

    /**
     * 获取视频号当前的直播记录
     * <p>请求参数格式：无参数</p>
     * @return 直播记录列表
     */
    @GetMapping("/finderLiveRecord")
    @Operation(summary = "获取视频号当前的直播记录", description = "获取当前正在进行的直播信息")
    public GetFinderLiveRecordListResult.LiveRecord getFinderLiveRecord(@RequestParam(value = "id") String id) {
        return wechatLiveService.getFinderLiveRecord(id);
    }
}
