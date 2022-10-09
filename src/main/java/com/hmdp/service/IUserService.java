package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥 ,mengshou
 * @since 2021-12-22  2022-10-7
 */
public interface IUserService extends IService<User> {
    /**
     * 发送验证码 保存到session
     * @param phone 手机号 作为key
     * @return 返回ok 无data
     */
    Result sendCode(String phone);

    /**
     * 登陆  验证验证码 保存token
     * @param loginForm 用户登录信息
     * @return 返回ok data：请求头信息，存储token
     */
    Result login(LoginFormDTO loginForm);
}
