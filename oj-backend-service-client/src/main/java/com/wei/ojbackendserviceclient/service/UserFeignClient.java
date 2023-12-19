package com.wei.ojbackendserviceclient.service;


import com.wei.ojbackendcommon.common.ErrorCode;
import com.wei.ojbackendcommon.constant.UserConstant;
import com.wei.ojbackendcommon.exception.BusinessException;
import com.wei.ojbackendmodel.model.entity.User;
import com.wei.ojbackendmodel.model.enums.UserRoleEnum;
import com.wei.ojbackendmodel.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

/**
 * 用户服务
 *
 * @author wei
 */
@FeignClient(name = "oj-backend-user-service", path = "/api/user/inner")
public interface UserFeignClient {

    /**
     * 根据id获取用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/get/{id}")
    User getById(@PathVariable("id") Long userId);


    /**
     * 获取用户信息列表
     *
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("idList") Collection<Long> idList);

    /**
     * 获取当前登录用户
     * 简单的逻辑，使用默认实现，相当于工具类，提高性能，不需远程调用
     * java8的关键字default
     *
     * @param request
     * @return
     */
    default User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
//        long userId = currentUser.getId();
//        currentUser = this.getById(userId);
//        if (currentUser == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//        }
        return currentUser;
    }

    /**
     * default表示接口中的默认方法
     * 默认方法可以在接口中直接实现，而不需要在实现接口的类中进行显式实现。
     * 默认方法可以有任意的默认实现，也可以没有默认实现，让实现接口的类自行决定如何实现该方法。
     */

    /**
     * 是否为管理员
     * 简单的逻辑，使用默认实现，相当于工具类，提高性能，不需远程调用
     *
     * @param user
     * @return
     */
    default boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }


    /**
     * 获取脱敏的用户信息
     * 简单的逻辑，使用默认实现，相当于工具类，提高性能，不需远程调用
     *
     * @param user
     * @return
     */
    default UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

}
