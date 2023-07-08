package com.wzy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wzy.common.R;
import com.wzy.entity.User;
import com.wzy.service.UserService;
import com.wzy.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wzy
 * @creat 2023-06-25-22:06
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送验证码
     *
     * @param user
     * @param session
     * @return
     * @throws MessagingException
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
        //获取手机号/邮箱
        String phone = user.getPhone();

        //调用工具类完成验证码发送
        if (!phone.isEmpty()) {
            //随机生成一个验证码
            String code = MailUtils.achieveCode();
            log.info("生成的验证码：{}", code);

            //这里的phone其实就是邮箱，code是我们生成的验证码
            MailUtils.sendTestMail(phone, code); //这部分抛出异常

            //[old]将要发送的验证码保存在session，然后与用户填入的验证码进行对比
//            session.setAttribute(phone, code);

            //[new]将生成的验证码缓存到Redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("验证码发送成功");
        }
        return R.error("验证码发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        //获取邮箱
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();

        //[old]从session中获取保存的验证码
//        Object codeInSession = session.getAttribute(phone);
//        log.info("session中的缓存验证码：{}", codeInSession);

        //[new]从Redis中获取缓存验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);
        log.info("redis中的缓存验证码：{}", codeInSession);

        //进行验证码的比对
        if (codeInSession != null && codeInSession.equals(code)) {
            //判断一下当前用户是否存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            //从数据库中查询是否有其邮箱
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            //如果不存在，则创建一个，存入数据库
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                user.setName("游客" + codeInSession);
            }
            //登录成功后，需要保存session，表示登录状态，因为前面的过滤器进行了用户登录判断
            session.setAttribute("user", user.getId());

            //[new]如果用户登录成功，删除redis中缓存的验证码
            redisTemplate.delete(phone);

            //并将其作为结果返回
            return R.success(user);
        }
        return R.error("登录失败");
    }

    /**
     * 用户登出
     *
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }

}
