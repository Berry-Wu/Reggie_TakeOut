package com.wzy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.entity.DishFlavor;
import com.wzy.mapper.DishFlavorMapper;
import com.wzy.service.DishFlavorService;
import org.springframework.stereotype.Service;

/**
 * @author wzy
 * @creat 2023-06-22-15:57
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
