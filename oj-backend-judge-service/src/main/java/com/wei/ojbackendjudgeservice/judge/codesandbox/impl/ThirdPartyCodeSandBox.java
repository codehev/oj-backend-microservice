package com.wei.ojbackendjudgeservice.judge.codesandbox.impl;


import com.wei.ojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 第三方代码沙箱（调用网上现成的沙箱）
 */
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
