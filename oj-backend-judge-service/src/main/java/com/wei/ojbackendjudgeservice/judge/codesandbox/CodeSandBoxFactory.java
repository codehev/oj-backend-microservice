package com.wei.ojbackendjudgeservice.judge.codesandbox;


import com.wei.ojbackendjudgeservice.judge.codesandbox.impl.ExampleCodeSandBox;
import com.wei.ojbackendjudgeservice.judge.codesandbox.impl.RemoteCodeSandBox;
import com.wei.ojbackendjudgeservice.judge.codesandbox.impl.ThirdPartyCodeSandBox;

/**
 * 工厂模式，静态工厂（根据字符串参数创建指定的代码沙箱实例）
 */
public class CodeSandBoxFactory {
    /**
     * 创建代码沙箱实例
     *
     * @param type 沙箱类型
     * @return CodeSandBox接口
     */
    public static CodeSandBox newInstance(String type, String authRequestHeader, String authRequestSecretKey, String codeSandboxUrl) {
        //return的是CodeSandBox的实现类，但是是CodeSandBox类型
        switch (type) {
            case "example":
                return new ExampleCodeSandBox();
            case "remote":
                return new RemoteCodeSandBox(authRequestHeader, authRequestSecretKey, codeSandboxUrl);
            case "thirdParty":
                return new ThirdPartyCodeSandBox();
            default:
                return new ExampleCodeSandBox();
        }
    }
}
