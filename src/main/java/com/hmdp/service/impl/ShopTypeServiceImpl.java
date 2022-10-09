package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.constantPool.CacheConstantUtils;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;



/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private IShopTypeService typeService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = CacheConstantUtils.SHOP_TYPE_CACHE;
        //1.先查缓存
        String shopListJson = stringRedisTemplate.opsForValue().get(key);
        //2.缓存存在 直接返回
        if (StrUtil.isNotBlank(shopListJson)){
            return Result.ok(JSONUtil.toList(shopListJson, ShopType.class));
        }
        //3.缓存不存在 查询数据库
        List<ShopType> typeList =typeService.query().orderByAsc("sort").list();
        //4.数据库不存在返回异常
        if (typeList==null){
            return Result.fail("商品类型列表信息无");
        }
        //5.数据库存在 添加到缓存
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(typeList));
        //6.返回数据
        return Result.ok(typeList);
    }
}
