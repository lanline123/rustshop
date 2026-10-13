package com.tigshop.service.print;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.param.print.PrintAddParam;
import com.tigshop.bean.param.print.PrintUpdateParam;
import com.tigshop.bean.query.print.PrintPageQuery;
import com.tigshop.bean.vo.print.PrintDetailVO;
import com.tigshop.bean.vo.print.PrintPageVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
* @author Admin
* @description 针对表【print(打印机表)】的数据库操作Service
* @createDate 2025-07-22 09:22:01
*/
public interface PrintService extends BaseService<Print> {

    /**
     * 列表
     */
    Page<PrintPageVO> list(PrintPageQuery pageQuery);

    /**
     * 详情
     */
    PrintDetailVO detail(Integer id);

    /**
     * 添加打印机
     *
     * @param printAddParam 打印机信息
     * @return 添加结果
     */
    boolean create(PrintAddParam printAddParam);

    /**
     * 更新打印机
     *
     * @param printUpdateParam 打印机更新信息
     * @return 更新结果
     */
    boolean update(PrintUpdateParam printUpdateParam);

    /**
     * 删除打印机
     *
     * @param id 打印机ID
     */
    boolean del(Integer id);

    /**
     * 是否有启用的打印机
     *
     * @return 是否有启用的打印机
     */
    boolean hasEnabled();

    /** 查询当前设备实际可用的打印机。 */
    List<PrintPageVO> devicePrinters(String deviceCode);

}
