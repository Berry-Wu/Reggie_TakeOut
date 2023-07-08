package com.wzy.dto;


import com.wzy.entity.Setmeal;
import com.wzy.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
