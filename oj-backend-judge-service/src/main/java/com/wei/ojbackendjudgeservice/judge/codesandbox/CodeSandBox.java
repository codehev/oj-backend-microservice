package com.wei.ojbackendjudgeservice.judge.codesandbox;


import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 定义代码沙箱的接口，提高代码的可重用性，如诺修改直接修改代码沙箱的实现类即可
 */
public interface CodeSandBox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
