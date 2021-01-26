package com.wang.service.role;

import com.wang.pojo.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface RoleService {
    public List<Role> getRoleList()throws SQLException;
}
