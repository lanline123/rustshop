package com.tigshop.service.sms;

public class SmsResult {

    private boolean success;
    private String code;
    private String message;

    public SmsResult(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static SmsResult ok() {
        return new SmsResult(true, "OK", "发送成功");
    }

    public static SmsResult fail(String code, String message) {
        return new SmsResult(false, code, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
