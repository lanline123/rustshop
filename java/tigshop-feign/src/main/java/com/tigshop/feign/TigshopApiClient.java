// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Tigshop接口
 *
 * @author Tigshop团队
 * @create 2024年12月17日 15:46
 */
@FeignClient(name = "tigshop-api", url = "https://www.tigshop.com")
public interface TigshopApiClient {
    /**
     * 更新授权
     *
     * @param sn 接口调用凭证
     * @return updateLicenseResult
     */
    @GetMapping("/api/user/auth_credentials/check")
    Map<String, Object> updateLicense(@RequestParam("sn") String sn,
                                      @RequestParam("domain") String domain,
                                      @RequestParam("version") String version);

}
