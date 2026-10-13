package com.tigshop.bean.vo.login;

//**---------------------------------------------------------------------+
//** 服务层文件 -- 会员
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+


import lombok.Getter;
import lombok.Setter;

/**
 * @author Jayce
 * @create 2024/9/30 11:11
 */
@Getter
@Setter
public class LoginVO {
    private String token;

    private String adminType;

    public LoginVO(String token, String adminType){
        this.token = token;
        this.adminType = adminType;
    }

    public LoginVO(String token){
        this.token = token;
    }
}
