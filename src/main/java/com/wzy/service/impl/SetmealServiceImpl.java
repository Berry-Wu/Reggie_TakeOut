package com.wzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.common.CustomException;
import com.wzy.common.R;
import com.wzy.dto.SetmealDto;
import com.wzy.entity.Setmeal;
import com.wzy.entity.SetmealDish;
import com.wzy.mapper.SetmealMapper;
import com.wzy.service.SetmealDishService;
import com.wzy.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wzy
 * @creat 2023-06-21-21:16
 */
@Service
@Transactional
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        Long id = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(id);

        }

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 修改套餐内容时回显套餐详情，需要联合setmeal和setmealDish两个表
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithFlavor(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmeal, setmealDto);

        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(lqw);

        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    /**
     * 删除套餐，不仅删除setmeal数据，还需要删除setmealDish数据
     * @param ids
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //先判断一下能不能删，如果status为1，则套餐在售，不能删
        //select * from setmeal where id in (ids) and status = 1
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId, ids);
        lqw.eq(Setmeal::getStatus, 1);

        //如果不可以删除，则抛出一个业务异常
        int count = this.count(lqw);
        if (count>0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);

        //删除关系表(SetmealDish)中的数据
        LambdaQueryWrapper<SetmealDish> dishLqw = new LambdaQueryWrapper<>();
        dishLqw.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishLqw);
    }
}
