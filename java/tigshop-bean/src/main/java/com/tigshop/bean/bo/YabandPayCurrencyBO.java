package com.tigshop.bean.bo;

import com.tigshop.bean.enums.setting.YabandPayCurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * yaBand支付货币类型业务对象
 *
 * @author kidd
 * @since 2025/4/7 10:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YabandPayCurrencyBO {

    private String name;

    private String value;

    public static List<YabandPayCurrencyBO> getYabandPayCurrencyList() {
        return Arrays.stream(YabandPayCurrencyEnum.values())
                .map(item -> new YabandPayCurrencyBO(item.getDesc(), item.getCode()))
                .toList();
    }
}
