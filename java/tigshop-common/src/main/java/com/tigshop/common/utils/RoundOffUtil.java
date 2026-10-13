package com.tigshop.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundOffUtil {

    /**
     * 清除指定小数位
     * @param value 原金额
     * @param digitIndex 1=角, 2=分
     * @return 清除后的金额 + 减去的金额
     */
    public static Result clearDecimal(BigDecimal value, int digitIndex) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }

        BigDecimal result;
        BigDecimal reduced;

        switch (digitIndex) {
            // 清除“角”
            case 1 -> {
                // 保留到角，然后把角位清 0
                // 整数部分
                BigDecimal yuanPart = value.setScale(0, RoundingMode.DOWN);
                // 小数点右移一位
                BigDecimal jiaoDigit = value.movePointRight(1)
                        // 取角位
                        .remainder(BigDecimal.TEN)
                        // 去小数
                        .setScale(0, RoundingMode.DOWN);
                // 转回小数形式 0.X
                BigDecimal newCents = jiaoDigit.movePointLeft(1);
                // 原小数部分
                BigDecimal oldCents = value.remainder(BigDecimal.ONE);
                // 去掉角位
                BigDecimal replaced = oldCents.subtract(jiaoDigit.movePointLeft(1));
                result = yuanPart.add(replaced).setScale(2, RoundingMode.DOWN);
                reduced = value.subtract(result).abs();
            }
            // 清除“分”
            case 2 -> {
                result = value.multiply(new BigDecimal("10"))
                        .setScale(0, RoundingMode.DOWN)
                        .divide(new BigDecimal("10"))
                        .setScale(2, RoundingMode.DOWN);
                reduced = value.subtract(result).abs();
            }
            default -> throw new IllegalArgumentException("digitIndex 必须为 1 或 2");
        }

        return new Result(result, reduced);
    }

    /** 返回结果封装类 */
    public static class Result {
        private final BigDecimal newValue;
        private final BigDecimal reducedAmount;

        public Result(BigDecimal newValue, BigDecimal reducedAmount) {
            this.newValue = newValue;
            this.reducedAmount = reducedAmount;
        }

        public BigDecimal getNewValue() { return newValue; }
        public BigDecimal getReducedAmount() { return reducedAmount; }

        @Override
        public String toString() {
            return "新金额=" + newValue + "，减去金额=" + reducedAmount;
        }
    }
}
