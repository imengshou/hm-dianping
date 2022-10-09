package com.hmdp.service.impl;

import cn.hutool.Hutool;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constantPool.CacheConstantUtils;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Override
    public Result sendCode(String phone) {
        //1.校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            //2. 不符合则返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3. 符合则生成验证码
        String code = RandomUtil.randomNumbers(6);

        //4. 验证码保存到redis 2s
        stringRedisTemplate.opsForValue().set(CacheConstantUtils.PRE_LOGIN_PHONE_CODE+phone,code,CacheConstantUtils.LOGIN_CODE_TIMEOUT, TimeUnit.SECONDS);
        //5. 发送验证码 调用
        log.info("验证码发送成功，{}",code);
        //返回ok
        return Result.ok();
    }

    @Transactional
    @Override
    public Result login(LoginFormDTO loginForm) {
        if (loginForm==null || loginForm.getPhone()==null ||loginForm.getCode()==null){
            throw new RuntimeException("loginForm为空，无法登陆操作");
        }
        //TODO 1.校验手机号和验证码
        String loginCode = stringRedisTemplate.opsForValue().get(CacheConstantUtils.PRE_LOGIN_PHONE_CODE + loginForm.getPhone());
        String code = loginForm.getCode();
        if (loginCode==null || !loginCode.equals(code)){
            //2.不一致则返回失败
            return Result.fail("验证失败，请确认手机号和验证码是否正确");
        }
        //3.手机号查询数据库是否存在
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,loginForm.getPhone());
        User user = getOne(queryWrapper);
        //4.手机号查询 不存在 则创建新用户 用户的手机号和昵称
        if (user ==null){
            user=new User(loginForm.getPhone(), CacheConstantUtils.AUTO_NICK_NAME);
            userService.save(user);
        }
        // 5.保存用户信息到缓存 redis中
        // 5.1.随机token 作为令牌
        String token = UUID.randomUUID().toString().replace("-","");
        UserDTO userDTO= BeanUtil.copyProperties(user,UserDTO.class);
        // 5.2.将user对象转为Hash存储或者序列化
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((filedName,filedVal)->filedVal.toString()));
        // 5.3.保存数
        stringRedisTemplate.opsForHash().putAll(CacheConstantUtils.PRE_LOGIN_TOKEN+token,userMap);
        //5.4 设置有效期
        stringRedisTemplate.expire(CacheConstantUtils.PRE_LOGIN_TOKEN+token,CacheConstantUtils.LOGIN_TOKEN_TIMEOUT,TimeUnit.MINUTES);
        //6.返回token
        return Result.ok(token);
    }
}
