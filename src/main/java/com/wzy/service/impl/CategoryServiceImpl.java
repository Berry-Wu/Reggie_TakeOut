package com.wzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.common.CustomException;
import com.wzy.entity.Category;
import com.wzy.entity.Dish;
import com.wzy.entity.Setmeal;
import com.wzy.mapper.CategoryMapper;
import com.wzy.service.CategoryService;
import com.wzy.service.DishService;
import com.wzy.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wzy
 * @creat 2023-06-19-21:28
 */
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id查询是否关联
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishlqw = new LambdaQueryWrapper<>();
        //添加dish查询条件，根据分类id进行查询
        dishlqw.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishlqw);

        //查看当前分类是否关联了菜品，如果已经关联，则抛出异常
        log.info("dish查询条件，查询到的条目数为：{}",count1);
        if (count1 > 0){
            //已关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmeallqw = new LambdaQueryWrapper<>();
        setmeallqw.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmeallqw);

        //查看当前分类是否关联了套餐，如果已经关联，则抛出异常
        log.info("setmeal查询条件，查询到的条目数为：{}",count2);
        if (count2 > 0){
            //已关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //正常删除
        super.removeById(id);
    }
}
