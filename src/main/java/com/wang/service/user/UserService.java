package com.wang.service.user;

import com.wang.pojo.User;

import java.sql.Connection;
import java.util.List;

public interface UserService {
    //用户登录
    public User login(String userCode,String password);

    //根据用户ID修改密码
    public boolean updatePwd(int id ,String pwd);

    //查询记录数
    public int getUserCount(String username,int userroler);

   // 根据条件查询用户列表
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize);

    // 增加用户信息
    public boolean add(User user);

    //根据userCode获取user  添加用户时需要用这个方法判断填入的userCode是否被占用
    public User selectUserCodeExist(String userCode);

    // 根据ID删除user
    public boolean deleteUserById(Integer delId);

    //根据ID查找user
    public User getUserById(String id);

    //修改用户信息
    public boolean modify(User user);
}
