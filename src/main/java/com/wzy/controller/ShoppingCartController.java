package com.wzy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wzy.common.BaseContext;
import com.wzy.common.R;
import com.wzy.dto.DishDto;
import com.wzy.entity.Dish;
import com.wzy.entity.ShoppingCart;
import com.wzy.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wzy
 * @creat 2023-06-29-19:24
 */
@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("加入购物车的数据：{}", shoppingCart.toString());
        // 设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //获取当前菜品id
        Long dishId = shoppingCart.getDishId();

        //条件查询，查询当前用户的购物车信息
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);

        //获取当前菜品id
        if (dishId!=null){
            //添加的是菜品
            lqw.eq(ShoppingCart::getDishId, dishId);
        }else {
            //添加的是套餐
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //查询当前菜品或者套餐是否已经在购物车中了
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);

        if (cartServiceOne!=null){
            //如果已存在就在当前的数量上加1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //如果不存在，则添加到购物车，数量默认为1; 并且设置创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //这里是为了统一结果，最后都返回cartServiceOne会比较方便
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 购物车显示
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(lqw);
        return R.success("购物车清空成功!");
    }

    /**
     * 购物车商品减掉
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        ShoppingCart item = null;

        if (dishId != null){
            lqw.eq(ShoppingCart::getDishId, dishId);
            item = shoppingCartService.getOne(lqw);
        }

        if (setmealId != null){
            lqw.eq(ShoppingCart::getSetmealId, setmealId);
            item = shoppingCartService.getOne(lqw);
        }

        Integer number = item.getNumber();
        log.info("num:{}", number);
        if (number == 1){
            shoppingCartService.removeById(item);
        }else {
            item.setNumber(number-1);
            shoppingCartService.updateById(item);
        }
        return R.success("修改成功");
    }



}
