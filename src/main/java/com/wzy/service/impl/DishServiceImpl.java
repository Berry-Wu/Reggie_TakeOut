package com.wzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.dto.DishDto;
import com.wzy.entity.Dish;
import com.wzy.entity.DishFlavor;
import com.wzy.mapper.DishServiceMapper;
import com.wzy.service.DishFlavorService;
import com.wzy.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wzy
 * @creat 2023-06-21-21:11
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishServiceMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //将菜品数据保存到dish表
        this.save(dishDto);
        //获取dishId
        Long id = dishDto.getId();

        //将获取到的dishId赋值给dishFlavor的dishId属性
        List<DishFlavor> flavors = dishDto.getFlavors();
        for(DishFlavor dishFlavor : flavors){
            dishFlavor.setDishId(id);
        }
        //同时将菜品口味数据保存到dish_flavor表
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(id!=null,DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(lqw);

        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 更新菜品数据，不仅更新dish，而且更新flavors
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新当前菜品数据（dish表）
        this.updateById(dishDto);

        //清理当前菜品的口味数据----dish_flavor的delete
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.like(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lqw);

        //添加当前提交过来的口味数据----dish_flavor的insert
        List<DishFlavor> flavors = dishDto.getFlavors();
        for(DishFlavor dishFlavor : flavors){
            dishFlavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }
}
