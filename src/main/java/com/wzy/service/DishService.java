package com.wzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.dto.DishDto;
import com.wzy.entity.Dish;

/**
 * @author wzy
 * @creat 2023-06-21-21:10
 */
public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和口味信息
    DishDto getByIdWithFlavor(Long id);

    //
    void updateWithFlavor(DishDto dishDto);
}
