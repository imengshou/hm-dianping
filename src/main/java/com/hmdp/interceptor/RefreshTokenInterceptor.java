package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.constantPool.CacheConstantUtils;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;



/**
 * @author mengshou
 * 对token保存及刷新登陆时长，延时
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //TODO 1.获取token
        String token = request.getHeader("authorization");
        //TODO 2.获取基于token的redis中的用户
        if (token==null || token.isEmpty()){
            return true;
        }
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(CacheConstantUtils.PRE_LOGIN_TOKEN + token);

        //3.判断用户是否存在
        if (userMap.isEmpty()){
            return true;
        }
        //5.1.将查询到的Hash对象转换成userDto对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //5.2.存在则保存在ThreadLocal
        UserHolder.saveUser(userDTO);
        //6.刷新token有效期
        stringRedisTemplate.expire(CacheConstantUtils.PRE_LOGIN_TOKEN + token,CacheConstantUtils.LOGIN_TOKEN_TIMEOUT, TimeUnit.MINUTES);
        //7.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户
        UserHolder.removeUser();
    }
}
