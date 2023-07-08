package com.wzy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.common.BaseContext;
import com.wzy.common.CustomException;
import com.wzy.common.R;
import com.wzy.entity.AddressBook;
import com.wzy.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wzy
 * @creat 2023-06-26-22:12
 */
@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;


    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        //首先根据线程中保存的用户id，对地址簿的userid进行设置
//        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook={}", addressBook);

        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(addressBook.getUserId()!=null, AddressBook::getUserId, addressBook.getUserId());
        lqw.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(lqw);

        return R.success(list);
    }

    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        log.info("填入的地址信息：{}",addressBook.toString());
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success("地址保存成功");
    }

    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        Long userId = addressBook.getUserId();
        log.info("当前用户id：{}", userId);

        LambdaUpdateWrapper<AddressBook> luw = new LambdaUpdateWrapper<>();
        luw.eq(addressBook.getUserId() != null, AddressBook::getUserId, userId);
        //将默认地址字段全设置为0
        luw.set(AddressBook::getIsDefault, 0);
        addressBookService.update(luw);

        //然后将本地址设为默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 购物车去结算后跳转到结算页面，首先获取当前用户的默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getAddress(){
        Long id = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(id!=null, AddressBook::getUserId, id);
        lqw.eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(lqw);
        return R.success(addressBook);
    }

    /**
     * 编辑地址的地址回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> edit(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null){
            throw new CustomException("地址信息不存在");
        }
        return R.success(addressBook);
    }

    /**
     * 保存修改的地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updateSave(@RequestBody AddressBook addressBook){
        if (addressBook == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        addressBookService.updateById(addressBook);
        return R.success("地址修改成功");
    }

    /**
     * 删除地址
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){ //因为前端传来的参数是ids
        if (ids == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        AddressBook addressBook = addressBookService.getById(ids);
        if (addressBook == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        addressBookService.removeById(ids);
        return R.success("地址删除成功");
    }

}
