package com.wzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.dto.SetmealDto;
import com.wzy.entity.Setmeal;

import java.util.List;

/**
 * @author wzy
 * @creat 2023-06-21-21:15
 */
public interface SetmealService extends IService<Setmeal> {
    //保存套餐时保存对应的菜品
    void saveWithDish(SetmealDto setmealDto);

    //修改套餐内容时回显套餐详情，需要联合setmeal和setmealDish两个表
    SetmealDto getByIdWithFlavor(Long id);

    //删除套餐，不仅删除setmeal数据，还需要删除setmealDish数据
    void removeWithDish(List<Long> ids);

//    void updateStatus();
}
