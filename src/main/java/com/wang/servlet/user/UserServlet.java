package com.wang.servlet.user;

import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;
import com.wang.pojo.Role;
import com.wang.pojo.User;
import com.wang.service.role.RoleService;
import com.wang.service.role.RoleServiceImpl;
import com.wang.service.user.UserService;
import com.wang.service.user.UserServiceImpl;
import com.wang.util.Constants;
import com.wang.util.PageSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//实现Servlet复用
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("method");
        if (method != null && method.equals("savepwd")) {
            this.updatePwd(req, resp);
        } else if (method != null && method.equals("pwdmodify")) {
            this.pwdModify(req, resp);
        } else if (method != null && method.equals("query")) {
            this.query(req, resp);
        } else if (method != null && method.equals("ucexist")) {
            this.userCodeExist(req, resp);
        } else if (method != null && method.equals("getrolelist")) {
            this.getRoleList(req, resp);
        } else if (method != null && method.equals("add")) {
            this.add(req, resp);
        } else if (method != null && method.equals("deluser")) {
            this.delUser(req, resp);
        } else if (method != null && method.equals("view")) {
            this.getUserById(req, resp, "userview.jsp");
        }else if(method != null && method.equals("modifyexe")){
            this.modify(req, resp);
        }else if(method != null && method.equals("modify")){
            this.getUserById(req, resp,"usermodify.jsp");
        }


    }


        @Override
        protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doGet(req, resp);
        }

        //修改密码
        public void updatePwd (HttpServletRequest req, HttpServletResponse resp){
            //从Session中获取id
            User user = (User) req.getSession().getAttribute(Constants.USER_SESSION);
            String newpassword = req.getParameter("newpassword");
            boolean flag = false;

            if (user != null && !StringUtils.isNullOrEmpty(newpassword)) {
                UserService userService = new UserServiceImpl();
                flag = userService.updatePwd(user.getId(), newpassword);
                System.out.println("UserServlet:" + newpassword);
                if (flag) {
                    req.setAttribute("message", "修改密码成功，请退出，使用新密码登录");
                    //密码修改成功移除当前Session
                    req.getSession().removeAttribute(Constants.USER_SESSION);
                } else {
                    req.setAttribute("message", "修改密码失败");
                }
            } else {
                req.setAttribute("message", "请检查新密码");
            }

            try {
                req.getRequestDispatcher("pwdmodify.jsp").forward(req, resp);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //验证旧密码，session中有用户的密码
        public void pwdModify (HttpServletRequest req, HttpServletResponse resp) throws IOException {

            User user = (User) req.getSession().getAttribute(Constants.USER_SESSION);
            String oldpassword = req.getParameter("oldpassword");

            //万能的Map:结果集
            Map<String, String> resultMap = new HashMap<String, String>();

            if (user == null) {//Session失效
                resultMap.put("result", "sessionerror");
            } else if (StringUtils.isNullOrEmpty(oldpassword)) {//输入的旧密码为空
                resultMap.put("result", "error");
            } else {
                String userPassword = user.getUserPassword();
                if (oldpassword.equals(userPassword)) {
                    resultMap.put("result", "true");
                } else {
                    resultMap.put("result", "false");
                }
            }


            try {
                resp.setContentType("application/jason");
                PrintWriter writer = resp.getWriter();
                //JSONArray 阿里巴巴Jason工具类,用于转换数据格式
                writer.write(JSONArray.toJSONString(resultMap));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //查询用户列表
        public void query (HttpServletRequest req, HttpServletResponse resp) throws ServletException {

            //从前端获取数据
            String queryUserName = req.getParameter("queryname");
            String temp = req.getParameter("queryUserRole");
            String pageIndex = req.getParameter("pageIndex");
            List<User> userList = null;
            List<Role> roleList = null;

            int queryUserRole = 0;

            //获取用户列表
            UserServiceImpl userService = new UserServiceImpl();

            //第一次走这个请求一定是第一页，页面大小是固定的
            int pageSize = 5;//可以写入配置文件，方便后期修改
            int currentPageNo = 1;
            //public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize)

            if (queryUserName == null) {
                queryUserName = null;
            }
            if (temp != null && !temp.equals("")) {
                queryUserRole = Integer.parseInt(temp);//给查询赋值 0，1，2，3
            }
            if (pageIndex != null) {
                currentPageNo = Integer.parseInt(pageIndex);
            }

            //获取用户总数(分页：上一页，下一页）
            int totalCount = userService.getUserCount(queryUserName, queryUserRole);

            //总页数支持
            PageSupport pageSupport = new PageSupport();
            pageSupport.setCurrentPageNo(currentPageNo);
            pageSupport.setPageSize(pageSize);
            pageSupport.setTotalPageCount(totalCount);

            //控制首页和尾页
            int totalPageCount = ((int) totalCount / pageSize) + 1;
            if (totalPageCount < 1) {//如果页面要小于1了就显示第一页的东西
                currentPageNo = 1;
            } else if (currentPageNo > totalPageCount) {//当前页面大于了最后一页
                currentPageNo = totalPageCount;
            }

            //获取用户列表展示
            userList = userService.getUserList(queryUserName, queryUserRole, currentPageNo, pageSize);
            req.setAttribute("userList", userList);//在前端遍历展示

            //获取角色列表展示
            RoleServiceImpl roleService = new RoleServiceImpl();
            roleList = roleService.getRoleList();
            req.setAttribute("roleList", roleList);

            req.setAttribute("totalCount", totalCount);
            req.setAttribute("currentPageNo", currentPageNo);
            req.setAttribute("totalPageCount", totalPageCount);
            req.setAttribute("queryUserName", queryUserName);
            req.setAttribute("queryUserRoler", queryUserRole);

            //返回前端
            try {
                req.getRequestDispatcher("userlist.jsp").forward(req, resp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //添加用户
        private void add (HttpServletRequest req, HttpServletResponse resp){
            System.out.println("add()=====================");
            //从前端输入获取
            String userCode = req.getParameter("userCode");
            String userName = req.getParameter("userName");
            String userPassword = req.getParameter("userPassword");
            String gender = req.getParameter("gender");
            String birthday = req.getParameter("birthday");
            String phone = req.getParameter("phone");
            String address = req.getParameter("address");
            String userRole = req.getParameter("userRole");

            //封装在User对象中
            User user = new User();
            user.setUserCode(userCode);
            user.setUserName(userName);
            user.setUserPassword(userPassword);
            user.setAddress(address);
            try {
                user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            user.setGender(Integer.valueOf(gender));
            user.setPhone(phone);
            user.setUserRole(Integer.valueOf(userRole));
            user.setCreationDate(new Date());
            //添加者的信息可以从Session中获取
            user.setCreatedBy(((User) req.getSession().getAttribute(Constants.USER_SESSION)).getId());

            UserService userService = new UserServiceImpl();
            if (userService.add(user)) {

                try {
                    resp.sendRedirect(req.getContextPath() + "/jsp/user.do?method=query");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                try {
                    req.getRequestDispatcher("useradd.jsp").forward(req, resp);
                } catch (ServletException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        //添加时判断userCode是否存在
        private void userCodeExist (HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            //判断用户账号是否可用
            String userCode = req.getParameter("userCode");

            HashMap<String, String> resultMap = new HashMap<String, String>();/////////////////
            if (StringUtils.isNullOrEmpty(userCode)) {
                //userCode == null || userCode.equals("")
                resultMap.put("userCode", "exist");////////////////
            } else {
                UserService userService = new UserServiceImpl();
                User user = userService.selectUserCodeExist(userCode);
                if (null != user) {
                    resultMap.put("userCode", "exist");/////////////////
                } else {
                    resultMap.put("userCode", "notexist");////////////////
                }
            }

            //把resultMap转为json字符串以json的形式输出
            //配置上下文的输出类型
            resp.setContentType("application/json");
            //从response对象中获取往外输出的writer对象
            PrintWriter outPrintWriter = resp.getWriter();
            //把resultMap转为json字符串 输出
            outPrintWriter.write(JSONArray.toJSONString(resultMap));//////////////////
            outPrintWriter.flush();//刷新
            outPrintWriter.close();//关闭流
        }

        private void getRoleList (HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            List<Role> roleList = null;

            try {
                RoleService roleService = new RoleServiceImpl();
                roleList = roleService.getRoleList();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            //把roleList转换成json对象输出
            resp.setContentType("application/json");///////////////////
            PrintWriter outPrintWriter = resp.getWriter();/////////////////
            outPrintWriter.write(JSONArray.toJSONString(roleList));///////////////////////
            outPrintWriter.flush();
            outPrintWriter.close();
        }

        public void delUser(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
            String id = req.getParameter("uid");
            Integer delId = 0;
            try {
                delId = Integer.parseInt(id);
            } catch (Exception e) {
                // TODO: handle exception
                delId = 0;
            }
            HashMap<String, String> resultMap = new HashMap<String, String>();
            if (delId <= 0) {
                resultMap.put("delResult", "notexist");
            } else {
                UserService userService = new UserServiceImpl();
                if (userService.deleteUserById(delId)) {
                    resultMap.put("delResult", "true");
                } else {
                    resultMap.put("delResult", "false");
                }
            }

            //把resultMap转换成json对象输出
            resp.setContentType("application/json");
            PrintWriter outPrintWriter = resp.getWriter();
            outPrintWriter.write(JSONArray.toJSONString(resultMap));
            outPrintWriter.flush();
            outPrintWriter.close();
        }

    private void getUserById(HttpServletRequest req, HttpServletResponse resp,String url) throws ServletException, IOException {
        String id = req.getParameter("uid");

        if(!StringUtils.isNullOrEmpty(id)){
            //调用后台方法得到user对象
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById(id);
            req.setAttribute("user", user);
            req.getRequestDispatcher(url).forward(req, resp);
        }

    }

    private void modify(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("uid");
        String userName = request.getParameter("userName");
        String gender = request.getParameter("gender");
        String birthday = request.getParameter("birthday");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String userRole = request.getParameter("userRole");

        User user = new User();

        user.setId(Integer.valueOf(id));
        user.setUserName(userName);
        user.setGender(Integer.valueOf(gender));
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        user.setPhone(phone);
        user.setAddress(address);
        user.setUserRole(Integer.valueOf(userRole));
        user.setModifyBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
        user.setModifyDate(new Date());

        UserService userService = new UserServiceImpl();
        if(userService.modify(user)){
            response.sendRedirect(request.getContextPath()+"/jsp/user.do?method=query");
        }else{
            request.getRequestDispatcher("usermodify.jsp").forward(request, response);
        }

    }
}
