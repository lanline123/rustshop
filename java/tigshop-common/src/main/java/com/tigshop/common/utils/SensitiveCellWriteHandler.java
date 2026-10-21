package com.tigshop.common.utils;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EasyExcel 脱敏 CellWriteHandler。
 * 在写入单元格后，检测对应字段是否有 @Sensitive 注解，如有则对值进行脱敏。
 */
public class SensitiveCellWriteHandler implements CellWriteHandler {

    private final Class<?> clazz;
    private final boolean enabled;
    private final Map<String, SensitiveType> sensitiveFieldMap = new ConcurrentHashMap<>();
    private boolean initialized = false;

    public SensitiveCellWriteHandler(Class<?> clazz, boolean enabled) {
        this.clazz = clazz;
        this.enabled = enabled;
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        if (!enabled || Boolean.TRUE.equals(isHead) || head == null) {
            return;
        }

        if (!initialized) {
            initSensitiveFields();
        }

        String fieldName = head.getFieldName();
        if (fieldName == null) {
            return;
        }
        SensitiveType type = sensitiveFieldMap.get(fieldName);
        if (type != null && cell.getCellType() == CellType.STRING) {
            String originalValue = cell.getStringCellValue();
            if (originalValue != null && !originalValue.isEmpty()) {
                cell.setCellValue(mask(originalValue, type));
            }
        }
    }

    private void initSensitiveFields() {
        synchronized (this) {
            if (initialized) return;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Sensitive sensitive = field.getAnnotation(Sensitive.class);
                if (sensitive != null) {
                    sensitiveFieldMap.put(field.getName(), sensitive.type());
                }
            }
            initialized = true;
        }
    }

    private static String mask(String value, SensitiveType type) {
        return switch (type) {
            case PHONE -> maskPhone(value);
            case ID_CARD -> maskIdCard(value);
            case EMAIL -> maskEmail(value);
            case BANK_CARD -> maskBankCard(value);
        };
    }

    private static String maskPhone(String phone) {
        if (phone.length() < 7) {
            return phone.charAt(0) + "****" + phone.charAt(phone.length() - 1);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String maskIdCard(String idCard) {
        if (idCard.length() < 7) {
            return idCard.charAt(0) + "****" + idCard.charAt(idCard.length() - 1);
        }
        return idCard.substring(0, 3) + "****" + idCard.substring(idCard.length() - 4);
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private static String maskBankCard(String card) {
        if (card.length() < 8) {
            return card.charAt(0) + "****" + card.charAt(card.length() - 1);
        }
        return card.substring(0, 4) + "****" + card.substring(card.length() - 4);
    }
}
