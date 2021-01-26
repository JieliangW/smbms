package com.wang.dao.role;

import com.wang.dao.BaseDao;
import com.wang.pojo.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDaoImpl implements RoleDao{
    public List<Role> getRoleList(Connection connection) throws SQLException {
        PreparedStatement pstm=null;
        ResultSet rs=null;
        List<Role> rolelist = new ArrayList<Role>();
        if(connection!=null){
            String sql="select * from smbms_role";
            Object[] params={};
            rs = BaseDao.execute(connection, sql, params, rs,pstm);

            while (rs.next()){
                Role _role = new Role();
                _role.setId(rs.getInt("id"));
                _role.setRoleCode(rs.getString("roleCode"));
                _role.setRoleName(rs.getString("roleName"));
                rolelist.add(_role);
            }
            BaseDao.closeResource(null,pstm,rs);
        }
        return rolelist;
    }
}
