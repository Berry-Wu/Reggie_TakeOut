package com.wzy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.common.R;
import com.wzy.dto.DishDto;
import com.wzy.entity.Category;
import com.wzy.entity.Dish;
import com.wzy.entity.DishFlavor;
import com.wzy.entity.Setmeal;
import com.wzy.service.CategoryService;
import com.wzy.service.DishFlavorService;
import com.wzy.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wzy
 * @creat 2023-06-21-21:13
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //原本的分页信息，但是不包含类别名称
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //新建一个dishDto的分页构造器，其包含了categoryName，后续将原本分页信息赋值给这里
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);

        //分页查询常规流程
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Dish::getName, name);
        lqw.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, lqw);

        //对象拷贝，这里只需要拷贝一下查询到的条目数，不拷贝records，也就是数据本身，因为此时dish数据缺少一项类别内容
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        //这里通过分页信息获取records，也就是具体的每条信息
        List<Dish> records = pageInfo.getRecords();
        //新建一个DishDto的列表，后续添加dishDto数据（既包含dish也有categoryName）
        List<DishDto> list = new ArrayList<>();

        //遍历records数据dish，获取其类别id，然后调用类别控制器根据id查找对应的类别，然后获取其name
        for (Dish dish : records) {
            //获取一下dish对象的category_id属性
            Long categoryId = dish.getCategoryId();
            //根据这个属性，获取到Category对象（这里需要用@Autowired注入一个CategoryService对象）
            Category category = categoryService.getById(categoryId);
            //随后获取Category对象的name属性，也就是菜品分类名称
            String categoryName = category.getName();

            DishDto dishDto = new DishDto();
            //将数据赋给dishDto对象
            BeanUtils.copyProperties(dish, dishDto);
            //并且设置其类别名称
            dishDto.setCategoryName(categoryName);
            //将dishDto对象封装成一个集合，作为我们的最终结果
            list.add(dishDto);
        }
        //将新的分页信息中原本被忽略的records属性进行赋值，也就是新的dishDto数据
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 菜品新增
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("菜品新增数据{}", dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        //新增清除原本redis中缓存对应类别的数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("菜品新增成功");
    }


    /**
     * 根据id查询菜品信息和口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        log.info("dishDto:{}", dishDto);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("菜品修改数据{}", dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        //修改商品后清除原本redis中保存的该类别的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("菜品修改成功");
    }

    /**
     * 套餐管理页面-新增套餐-添加菜品部分，根据类别id获取对应的菜品加载在此界面
     * 因为后续的front页面显示需要获取菜品对应的口味，所以这里完善这个方法
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> get(Dish dish){
//        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(Dish::getCategoryId, dish.getCategoryId());
//        lqw.eq(Dish::getStatus, 1);
//        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(lqw);
//        return R.success(list);
//    }

    /**
     * 完善上面的类别菜品，使之可以显示口味
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> get(Dish dish) {
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        List<DishDto> dishDtoList;
        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，则直接返回，无需查询数据库
        if (dishDtoList != null){
            return R.success(dishDtoList);
        }
        //如果不存在则执行下述数据库查询操作，查询后将数据缓存在redis
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lqw.eq(Dish::getStatus, 1);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lqw);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor> flavorlqw = new LambdaQueryWrapper<>();
            flavorlqw.eq(DishFlavor::getDishId, id);
            List<DishFlavor> flavors = dishFlavorService.list(flavorlqw);

            dishDto.setFlavors(flavors);
            //将dishDto作为结果返回
            return dishDto;
            //将所有返回结果收集起来，封装成List
        }).collect(Collectors.toList());

        //将数据缓存在redis中，设置持续时间60min
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

    /**
     * 修改菜品的停售或者启售
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> stop(@PathVariable int status, @RequestParam List<Long> ids) {
        LambdaUpdateWrapper<Dish> luw = new LambdaUpdateWrapper<>();
        luw.in(Dish::getId, ids);
        luw.set(Dish::getStatus, status);

        //新增redis清除缓存
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId, ids);
        for (Dish dish : dishService.list(lqw)) {
            String key = "dish_" + dish.getCategoryId() + "_1";
            redisTemplate.delete(key);
        }

        dishService.update(luw);
        return R.success("状态更新成功");
    }

    /**
     * 菜品删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId, ids);
        dishService.remove(lqw);
        return R.success("删除成功");
    }

}
