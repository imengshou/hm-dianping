package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.constantPool.CacheConstantUtils;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;



/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥 mengshou
 * @since 2021-12-22 2022-10-8
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    public IShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Result queryById(Long id) {
        String key= CacheConstantUtils.PRE_SHOP_CACHE + id;
        //1.从redis中查询缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2. 存在即返回
        if (StrUtil.isNotBlank(shopJson)) {
            return Result.ok(JSONUtil.toBean(shopJson, Shop.class));
        }
        //判断是否是空值 解决缓存穿透
        if (shopJson!=null){

            return Result.fail("店铺不存在");
        }

        //3. 缓存没有 根据id查询数据库
        Shop shop = shopService.getById(id);
        //3.1 数据库没有 则返回异常
        if (shop ==null){
            //缓存空值 防止缓存穿透
            stringRedisTemplate.opsForValue().set(key,"",CacheConstantUtils.SHOP_CACHE_NULL_TIMEOUT,TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }
        //4. 添加到缓存
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop), CacheConstantUtils.SHOP_CACHE_TIMEOUT, TimeUnit.MINUTES);
        //5.返回数据
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result updateShop(Shop shop) {

        Long id = shop.getId();
        if (id==null){
            return Result.fail("店铺id不能为空");
        }
        //1.更新数据库
        shopService.updateById(shop);
        //2.删除缓存
        stringRedisTemplate.delete(CacheConstantUtils.PRE_SHOP_CACHE +id);
        return Result.ok();
    }

    /**
     * 获取锁
     * @param key 标识key
     * @return 锁状态
     */
    private boolean tryLock(String key){
        Boolean lockFlag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 3L, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(lockFlag);
    }

    /**
     * 释放锁
     * @param key 标识key
     */
    private void MoveLock(String key){
        stringRedisTemplate.delete(key);
    }

}
