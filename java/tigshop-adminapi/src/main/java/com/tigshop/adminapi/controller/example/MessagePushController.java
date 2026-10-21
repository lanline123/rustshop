package com.tigshop.adminapi.controller.example;

import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.service.msgTemplate.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adminapi/messagepush/messagepush")
public class MessagePushController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private TigshopProperties tigshopProperties;

    @GetMapping("/push")
    public void sendMessage(@RequestParam("userId") Integer userId, @RequestParam("orderId") Integer orderId){
        messageService.sendUserMessage(userId,orderId, MessageTypeEnum.NEW_ORDER_SHOP);
    }

    @GetMapping("/tigshopConfig")
    public String tigshopConfig(){
        Integer isMerchant = tigshopProperties.getIsMerchant();
        Integer isB2b = tigshopProperties.getIsB2b();
        Integer isPro = tigshopProperties.getIsPro();
        Integer isOverseas = tigshopProperties.getIsOverseas();
        Integer isVendor = tigshopProperties.getIsVendor();
        Integer isO2o = tigshopProperties.getIsO2o();
        String result = "isMerchant: "+isMerchant+"isB2b: "+isB2b+"isPro: "+isPro+"isOverseas: "+isOverseas+"isVendor: "+isVendor+"isO2o: "+isO2o;
        return result;
    }
}
