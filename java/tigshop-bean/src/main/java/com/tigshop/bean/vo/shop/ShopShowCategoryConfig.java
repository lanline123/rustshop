package com.tigshop.bean.vo.shop;

import lombok.Data;

@Data
public class ShopShowCategoryConfig {
    private Integer type; //1 全部  2 自定义
    private String ids;
}