package com.wei.ojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.wei.ojbackendcommon.common.ErrorCode;
import com.wei.ojbackendcommon.exception.BusinessException;
import com.wei.ojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * 远程代码沙箱（实际调用接口的沙箱）
 * 只有加了@Component才能使用@Value
 */
@Component
public class RemoteCodeSandBox implements CodeSandBox {
    /**
     * 定义鉴权请求头和密钥(实现比较简单，适合内部系统之间相互调用，缺点不够灵活，如果key泄露或变更，需要重启代码)
     */
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRETKEY = "secretkey";

    @Value("${codeSandBox.url:http://127.0.0.1:8105/api/executeCode}")
    private String codeSandboxUrl;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");

//        String codeSandboxUrl = "http://192.168.200.142:8105/api/executeCode";
        String codeSandboxUrl = "http://127.0.0.1:8105/api/executeCode";
        if (codeSandboxUrl == null) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "远程代码沙箱请求失败：请求地址为空");
        }

        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        HttpResponse httpResponse = HttpUtil.createPost(codeSandboxUrl)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRETKEY)
                .body(jsonStr)
                .execute();
        String responseStr = httpResponse.body();
        if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "远程代码沙箱请求失败：" + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
