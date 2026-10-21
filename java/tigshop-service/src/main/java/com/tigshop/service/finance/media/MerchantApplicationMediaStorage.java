package com.tigshop.service.finance.media;

/**
 * 商户进件媒体临时存储
 *
 * @author Tigshop团队
 */
public interface MerchantApplicationMediaStorage {

    void put(String objectName, byte[] content, String contentType);

    byte[] get(String objectName);

    void delete(String objectName);
}
