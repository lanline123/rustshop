package com.tigshop.service.deposit;

import com.tigshop.bean.dto.deposit.DepositSettingDTO;
import com.tigshop.bean.vo.deposit.DepositSettingVO;

/**
 * 店铺保证金设置服务（复用 ConfigService 键值存储，不建表）。
 *
 * @author Tigshop团队
 */
public interface DepositSettingService {

    /**
     * 读取保证金设置（无配置时返回默认值）。
     */
    DepositSettingVO detail();

    /**
     * 保存保证金设置（逐项写入 config 键值）。
     */
    void save(DepositSettingDTO dto);
}
