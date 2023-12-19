package com.wei.ojbackenduserservice.controller.inner;

import cn.hutool.core.collection.CollUtil;
import com.wei.ojbackendcommon.common.ErrorCode;
import com.wei.ojbackendcommon.exception.BusinessException;
import com.wei.ojbackendmodel.model.entity.User;
import com.wei.ojbackendserviceclient.service.UserFeignClient;
import com.wei.ojbackenduserservice.service.UserService;
import org.apache.commons.collections4.SetUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 该服务仅供内部调用
 *
 * @author wei
 * @email 2529799312@qq.com
 * @description TODO
 * @date 2023-12-19 21:12
 */
@RestController("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserService userService;


    /**
     * 根据id获取用户信息
     *
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/get/{id}")
    public User getById(@PathVariable("id") Long userId) {
        if (userId != null) {
            User user = userService.getById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            return user;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }


    /**
     * 获取用户信息列表
     *
     * @param idList
     * @return
     */
    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList) {
        if (CollUtil.isNotEmpty(idList)) {
            List<User> users = userService.listByIds(idList);
            if (CollUtil.isEmpty(users)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            return users;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }
}
