package com.wzy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.common.R;
import com.wzy.dto.DishDto;
import com.wzy.dto.SetmealDto;
import com.wzy.entity.Category;
import com.wzy.entity.Dish;
import com.wzy.entity.Setmeal;
import com.wzy.entity.SetmealDish;
import com.wzy.service.CategoryService;
import com.wzy.service.DishService;
import com.wzy.service.SetmealDishService;
import com.wzy.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author wzy
 * @creat 2023-06-21-21:17
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 分页展示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page:{}, pageSize:{}, name:{}",page,pageSize,name);
        //原本的分页信息，但是不包含类别名称
        Page<Setmeal> pageInfo = new Page<Setmeal>(page,pageSize);
        //新建一个setmealDto的分页构造器，其包含了categoryName，后续将原本分页信息赋值给这里
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);

        //分页查询常规流程
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Setmeal::getName, name);
        setmealService.page(pageInfo,lqw);

        //对象拷贝，这里只需要拷贝一下查询到的条目数，不拷贝records，也就是数据本身，因为此时dish数据缺少一项类别内容
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        //这里通过分页信息获取records，也就是具体的每条信息
        List<Setmeal> records = pageInfo.getRecords();
        //新建一个SetmealDto的列表，后续添加SetmealDto数据（既包含Setmeal也有categoryName）
        List<SetmealDto> list = new ArrayList<>();

        //遍历records数据setmeal，获取其类别id，然后调用类别控制器根据id查找对应的类别，然后获取其name
        for(Setmeal setmeal : records){
            Long categoryId = setmeal.getCategoryId();
            Category category = categoryService.getById(categoryId);
            SetmealDto setmealDto = new SetmealDto();

            //将数据赋给setmealDto对象
            BeanUtils.copyProperties(setmeal, setmealDto);
            //设置类别名称
            setmealDto.setCategoryName(category.getName());
            list.add(setmealDto);
        }
        //将新的分页信息中原本被忽略的records属性进行赋值，也就是新的setmealDto数据
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 新建套餐，包括套餐菜品表
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("新建套餐:{}",setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新建成功");
    }

    /**
     * 套餐信息回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        log.info("套餐id:{}",id);
        SetmealDto setmealDto = setmealService.getByIdWithFlavor(id);
        log.info("内容回显：{}",setmealDto.toString());
        return R.success(setmealDto);
    }

    /**
     * 删除套餐,不仅删除setmeal数据，还需要删除setmealDish数据
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除套餐的id：{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 修改菜品销售状态：启售 or 停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> stop(@PathVariable int status, @RequestParam List<Long> ids){
        log.info("套餐状态：{}，套餐ids：{}",status,ids);

        LambdaUpdateWrapper<Setmeal> luw = new LambdaUpdateWrapper<>();
        luw.in(Setmeal::getId, ids);
        if (status==0){
            luw.set( Setmeal::getStatus, 0);
        }else {
            luw.set( Setmeal::getStatus, 1);
        }
        setmealService.update(luw);
        return R.success("修改销售状态成功");
    }

    /**
     * 手机端套餐显示
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, 1);
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);

    }

    /**
     * 点击图片展示套餐详情
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> setmealShow(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(lqw);

        List<DishDto> dishList = new ArrayList<>();

        for(SetmealDish setmealDish : list){
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            dishDto.setCopies(setmealDish.getCopies());
            dishList.add(dishDto);
        }
        return R.success(dishList);
    }




}
