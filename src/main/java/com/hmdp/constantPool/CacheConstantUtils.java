package com.hmdp.constantPool;

import cn.hutool.core.util.RandomUtil;

/**
 * @author mengshou
 * 商铺数据常量池
 */
public class CacheConstantUtils {

    public static final String SHOP_TYPE_CACHE="shopType:cache";

    public static final String PRE_SHOP_CACHE="shop:cache:";

    public static final Long SHOP_CACHE_TIMEOUT=30L;

    public static final Long SHOP_CACHE_NULL_TIMEOUT=2L;

    public static final Long LOGIN_CODE_TIMEOUT=120L;

    public static final String PRE_LOGIN_PHONE_CODE="login:code:";

    public static final String AUTO_NICK_NAME ="游客"+ RandomUtil.randomNumbers(8)+RandomUtil.randomString(8);

    public static final String PRE_LOGIN_TOKEN="login:token:";

    public static final Long LOGIN_TOKEN_TIMEOUT=30L;
}
