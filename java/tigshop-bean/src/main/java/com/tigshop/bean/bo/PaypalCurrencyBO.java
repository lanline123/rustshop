package com.tigshop.bean.bo;

import com.tigshop.bean.enums.setting.PaypalCurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * paypal货币类型业务对象
 *
 * @author kidd
 * @since 2025/4/7 10:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaypalCurrencyBO {

    private String name;

    private String value;

    public static List<PaypalCurrencyBO> getYabandPayCurrencyList() {
        return Arrays.stream(PaypalCurrencyEnum.values())
                .map(item -> new PaypalCurrencyBO(item.getDesc(), item.getCode()))
                .toList();
    }


}
