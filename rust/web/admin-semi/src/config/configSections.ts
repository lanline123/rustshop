export const configSections: Record<string, Array<{ title: string; fields: string[] }>> = {
  "baseBasicManage": [
    {
      "title": "商城信息",
      "fields": [
        "shopLogo",
        "lightShopLogo",
        "shopName",
        "shopCompany",
        "poweredBy",
        "poweredByStatus",
        "kefuAddress"
      ]
    },
    {
      "title": "备案信息",
      "fields": [
        "shopIcpNo",
        "shopIcpNoUrl",
        "shop110No",
        "shop110Link"
      ]
    },
    {
      "title": "经营许可证",
      "fields": [
        "businessLicenseEnabled"
      ]
    },
    {
      "title": "商城状态",
      "fields": [
        "shopRegClosed",
        "closeShop"
      ]
    }
  ],
  "baseProductManage": [
    {
      "title": "商品货币设置",
      "fields": [
        "dollarSign",
        "dollarSignCn"
      ]
    },
    {
      "title": "商品录入设置",
      "fields": [
        "snPrefix",
        "customProductNameLength",
        "marketPriceRate"
      ]
    },
    {
      "title": "商品编辑设置",
      "fields": [
        "productEditStockModify",
        "specImageMode"
      ]
    },
    {
      "title": "商品信息显示设置",
      "fields": [
        "showSelledCount",
        "showMarketprice",
        "isEnquiry",
        "bulkPurchase",
        "enableAttributeFilter"
      ]
    },
    {
      "title": "商品评价设置",
      "fields": [
        "productCommentEnabled",
        "anonymousCommentEnabled"
      ]
    }
  ],
  "baseShoppingManage": [
    {
      "title": "其它",
      "fields": [
        "childAreaNeedRegion",
        "integralName",
        "integralScale",
        "orderSendPoint",
        "integralPercent",
        "commentSendPoint",
        "showSendPoint",
        "useQiandaoPoint",
        "continuousSignIn",
        "canInvoice",
        "invoiceAdded",
        "returnConsignee",
        "returnMobile",
        "returnAddress"
      ]
    }
  ],
  "baseOrderManage": [
    {
      "title": "其它",
      "fields": [
        "autoDeliveryDays",
        "virtualOrderAutoDeliver",
        "autoCancelOrderMinute",
        "isChangeOrderStatus",
        "afterSalesLimitDays",
        "afterSalesReview",
        "autoReturnGoods",
        "autoReturnGoodsDays",
        "autoConfirmReceiptDays",
        "financialReview",
        "afterSalesReasonList",
        "isPlatformCancelPaidOrder",
        "isPlatformCancelDeliverOrder",
        "isShopCancelDeliverOrder",
        "adminShoppingUsePoint",
        "adminShoppingUseBalance"
      ]
    }
  ],
  "kefuSetting": [
    {
      "title": "客服设置",
      "fields": [
        "kefuType",
        "kefuYzfType",
        "kefuYzfSign",
        "kefuWorkwxId",
        "corpId",
        "kefuCode",
        "kefuCodeBlank"
      ]
    },
    {
      "title": "客服信息",
      "fields": [
        "kefuPhone",
        "kefuTime"
      ]
    }
  ],
  "globalSetting": [
    {
      "title": "SEO设置",
      "fields": [
        "shopTitleSuffix",
        "shopTitle",
        "shopKeywords",
        "shopDesc",
        "icoImg",
        "defaultAvatar"
      ]
    },
    {
      "title": "域名设置",
      "fields": [
        "pcDomain",
        "h5Domain",
        "imDomain",
        "autoRedirect",
        "adminDomain"
      ]
    },
    {
      "title": "上传设置",
      "fields": [
        "uploadMaxSize",
        "imageUploadMaxSize",
        "materialUploadNameType"
      ]
    },
    {
      "title": "搜索设置",
      "fields": [
        "searchKeywords",
        "msgHackWord",
        "isOpenPscws",
        "selectSearchType"
      ]
    },
    {
      "title": "地区设置",
      "fields": [
        "shopCountry",
        "defaultCountry"
      ]
    },
    {
      "title": "ICO图标库",
      "fields": [
        "icoDefinedCss"
      ]
    },
    {
      "title": "存储设置",
      "fields": [
        "storageType",
        "storageSaveFullPath",
        "storageLocalUrl",
        "storageOssUrl",
        "storageCosUrl",
        "storageQiniuUrl",
        "storageS3Url",
        "storageOssAccessKeyId",
        "storageCosSecretId",
        "storageQiniuAccessKey",
        "storageS3AccessKey",
        "storageOssAccessKeySecret",
        "storageCosSecretKey",
        "storageQiniuSecretKey",
        "storageS3SecretKey",
        "storageOssBucket",
        "storageCosBucket",
        "storageQiniuBucket",
        "storageS3Bucket",
        "storageOssRegion",
        "storageCosRegion",
        "storageQiniuRegion",
        "storageS3Region",
        "storageS3Endpoint"
      ]
    },
    {
      "title": "MCP 设置",
      "fields": [
        "workbuddyApiKeyExpiresAt"
      ]
    }
  ],
  "loginSetting": [
    {
      "title": "其它",
      "fields": [
        "openWechatOauth",
        "wechatAppId",
        "wechatAppSecret",
        "wechatServerToken",
        "wechatServerSecret",
        "openWechatPcLogin",
        "wechatRegisterBindPhone",
        "openWechatRegister",
        "wechatShareNeedLogin",
        "isOpenMobileAreaCode",
        "usernamePrefix "
      ]
    }
  ],
  "storeAmapSetting": [
    {
      "title": "其它",
      "fields": [
        "mapProvider",
        "field"
      ]
    }
  ],
  "personalizedManage": [
    {
      "title": "其它",
      "fields": [
        "guestLikeGoodsName",
        "isGuestLikeGoods",
        "defaultAvatar",
        "searchSelfStore",
        "indexSearchShop"
      ]
    }
  ],
  "baseNoticeManage": [
    {
      "title": "短信设置",
      "fields": [
        "smsKeyId",
        "smsKeySecret",
        "smsSignName",
        "smsShopMobile"
      ]
    },
    {
      "title": "邮件通知",
      "fields": [
        "mailService",
        "smtpSsl",
        "smtpHost",
        "smtpPort",
        "smtpUser",
        "smtpPass",
        "smtpMail",
        "mailCharset",
        "testMailAddress",
        "serviceEmail",
        "sendConfirmEmail",
        "orderPayEmail",
        "sendServiceEmail",
        "sendShipEmail"
      ]
    }
  ]
};
