package com.tigshop.common.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author wzh
 */
public class RandomInfoUtil {

    public static String getRandomNickname() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        //接口盒子的API
        String url = "http://101.35.2.25/api/zici/sjwm.php?id=10008118&key=6869472222810f3341a053e4bfa61f0a&length2=10";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject object = JSONUtil.parseObj(resp.body());
        return object.get("msg").toString();
    }

    public static String getRandomPhoto() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        //接口盒子的API
        String url = "http://101.35.2.25/api/img/apihzimgtx.php?id=10008118&key=6869472222810f3341a053e4bfa61f0a&type=1&imgtype=0";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject object = JSONUtil.parseObj(resp.body());

        String msg = object.get("msg").toString();

        return object.get("msg").toString();
    }



}
