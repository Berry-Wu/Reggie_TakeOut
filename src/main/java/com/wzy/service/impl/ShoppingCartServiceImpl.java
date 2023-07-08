package com.wzy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.entity.ShoppingCart;
import com.wzy.mapper.ShoppingCartMapper;
import com.wzy.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @author wzy
 * @creat 2023-06-29-19:23
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
