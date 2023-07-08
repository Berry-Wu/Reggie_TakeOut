package com.wzy.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.entity.Category;
import com.wzy.entity.Employee;
import com.wzy.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author wzy
 * @creat 2023-06-16-20:54
 */
@Slf4j
@ResponseBody
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //如果包含Duplicate entry，则说明有条目重复
        if (ex.getMessage().contains("Duplicate entry")) {
            //对字符串切片
            String[] split = ex.getMessage().split(" ");
            //字符串格式是固定的，所以这个位置必然是username
            String msg = split[2] + "已存在";
            //拼串作为错误信息返回
            return R.error(msg);
        }
        //如果是别的错误那我也没招儿了
        return R.error("未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> CustHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }


}
