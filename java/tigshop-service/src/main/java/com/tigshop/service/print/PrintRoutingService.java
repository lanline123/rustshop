package com.tigshop.service.print;

import com.tigshop.bean.model.print.Print;

import java.util.List;

public interface PrintRoutingService {
    List<Print> resolve(Integer shopId, String deviceCode);
}
