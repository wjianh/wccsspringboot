package com.wjh.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wjh.config.PassToken;
import com.wjh.config.UserLoginToken;
import com.wjh.entity.XtGlyxx;
import com.wjh.service.XtglyService;
import com.wjh.utils.AesUtil;
import com.wjh.utils.SystemUtils;
import com.wjh.utils.TokenCreateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Controller
public class XtglyController {
    @Autowired
    XtglyService xtglyService;

    private final long lockTime = 1800L;

    //获取验证码
    @GetMapping("/xtgly/getimage")
    public void getImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        response.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        // 生成随机字串
        String verifyCode = com.admin.demo.utli.VerifyCodeUtils.generateVerifyCode(4);
        // 存入会话session
        HttpSession session = request.getSession(true);
        // 删除以前的
        session.removeAttribute("verCode");
        session.removeAttribute("codeTime");
        session.setAttribute("verCode", verifyCode.toLowerCase());		//生成session
        session.setAttribute("codeTime", LocalDateTime.now());
        System.out.println(session.getAttribute("verCode"));

        // 生成图片
        int w = 100, h = 30;
        OutputStream out = response.getOutputStream();
        com.admin.demo.utli.VerifyCodeUtils.outputImage(w, h, out, verifyCode);
        try {
            out.flush();
        } finally {
            out.close();
        }
    }

    //如果在这里添加一个@UserLoginToken表示该方法需要验证token
    //登陆方法
    @RequestMapping("/xtgly/login")
    @ResponseBody
    public Map<String,Object> login(HttpServletRequest request, @RequestBody Map<String, Object> data) throws Exception{

        String dlzh = (String) data.get("username");
        String password = (String) data.get("password");//获取前端加密后的密码
        String yzm = (String) data.get("yzm");//获取前端传过来的验证码

        HttpSession session = request.getSession(true);
        String sessionYzm = (String) session.getAttribute("verCode");

        final String KEY = "abcdefgabcdefg12";
        password = new AesUtil().Decrypt(password,KEY);//后台调用aes解密将加密的密码解密
        System.out.println("后端验证码：=================="+sessionYzm+"前端验证码:"+yzm);
        Map<String,Object> map = new HashMap<>();//存放返回给前端数据的map

        if(yzm!=null && !"".equals(yzm) && sessionYzm != null && !"".equals(sessionYzm)) {//现进行验证码判断是否正确，如果验证码正确在进行下面判断
            if (yzm.equals(sessionYzm)) {//验证码是否正确判断，如果验证码正确后再判断该用户是否存在
                XtGlyxx xtglyxx = xtglyService.findXtglyById(dlzh);//通过用户名去查询登陆密码然后用map接收
                if (xtglyxx != null) {// && xtglyxx.getDlmm() != null && !"".equals(xtglyxx.getDlmm())
                    if(xtglyxx.getGlyzt()!=null && "1".equals(xtglyxx.getGlyzt())){//正常用户，判断
                        map = checkPassword(session, password, xtglyxx);

                    }else if(xtglyxx.getGlyzt() != null && "2".equals(xtglyxx.getGlyzt())){//线判断用户是否被锁定了
                        //如果被锁定了，但是时间超过了被锁定的时间，则解锁然后判断该次登陆请求密码是否正确，如果正确则登陆成功
                        if(lockTime<LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - xtglyxx.getDlsbsdsj()){//先判断是否到达锁定时间，
                            xtglyxx.setYdlsbcs("0");//重新激活登陆失败次数
                            xtglyxx.setDlsbsdsj(null);//清空锁定时间
                            xtglyxx.setGlyzt("1");//正常用户
                            xtglyService.updateXtgly(xtglyxx);
                            map = checkPassword(session, password, xtglyxx);
                        }else {//说明还没超过锁定时间，然后提醒用户多久后再尝试登陆
                            System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - xtglyxx.getDlsbsdsj());
                            long shenyuDateTime =lockTime  + xtglyxx.getDlsbsdsj();
                            System.out.println(shenyuDateTime);
                            System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
                            LocalDateTime dateTime =LocalDateTime.ofEpochSecond(shenyuDateTime,0,ZoneOffset.ofHours(8));
                            System.out.println(dateTime);
                           // LocalDateTime dateTime=Instant.ofEpochSecond(shenyuDateTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
                            map.put("message","你的账户已被锁定！将于"+dateTime.getYear()+"-"+dateTime.getMonthValue()+"-"+dateTime.getDayOfMonth()+" "+dateTime.getHour() +":"+dateTime.getMinute()+":"+dateTime.getSecond()+"后解锁！");
                            map.put("code", "500");
                            return map;
                        }
                    }else{ //被停用的用户，返回
                        map.put("message", "该用户已被停用！");
                        map.put("code", "500");
                    }
                } else {//查询密码为空，该帐户不存在
                    map.put("message", "用户名不存在");
                    map.put("code", "500");
                }
            }else{
                map.put("message","验证码错误！");
                map.put("code","500");
            }
        }else {//验证码为空！，
            map.put("message","验证码为空！");
            map.put("code","500");
        }
        return map;
    }

    private Map<String,Object> checkPassword(HttpSession session,String loginPassword, XtGlyxx xtGlyxx){
        Map<String,Object> map = new HashMap<>();
        if (xtGlyxx.getDlmm().equals(loginPassword)) {//查询出来的登陆密码去匹配用户登陆输入进来的登陆密码
            System.out.println("登陆成功！");
            String token = new TokenCreateUtil().getToken(xtGlyxx.getDlzh(), xtGlyxx.getDlmm());//生成token
            session.setAttribute("token", token);//将token存放到session里面
            session.setAttribute("xtglyxx",xtGlyxx);
            map.put("code", "200");
            map.put("glybh",xtGlyxx.getGlybh());
            map.put("zzbm",xtGlyxx.getZzbm());
            map.put("token", token);
            map.put("message", "登陆成功！");
        } else {//密码不匹配，则将登陆失败次数加一
            try {
                map = checkLoginNum(xtGlyxx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private Map<String,Object> checkLoginNum(XtGlyxx xtGlyxx) throws Exception{
        Map<String,Object> returnMap = new HashMap<>();
        //先判断用户状态是否为被锁定的，如果为锁定的
        if(xtGlyxx.getYdlsbcs() == null || !"".equals(xtGlyxx.getYdlsbcs())){//判断登陆失败次数是否为空
            if(1 < (Integer.parseInt(xtGlyxx.getKydlsbcs()) - Integer.parseInt(xtGlyxx.getYdlsbcs()))){//如果可以登陆失败次数减掉已登陆失败次数大于0次
                int newYdlsbcs = Integer.parseInt(xtGlyxx.getYdlsbcs()) + 1;
                int newKydlsbcs = Integer.parseInt(xtGlyxx.getKydlsbcs()) - Integer.parseInt(xtGlyxx.getYdlsbcs())-1;
                returnMap.put("message","登陆失败！用户名和密码不匹配！你还有"+newKydlsbcs+"次机会！");
                returnMap.put("type","500");
                xtGlyxx.setYdlsbcs(String.valueOf(newYdlsbcs));//然后更新后台已登陆失败次数
                xtglyService.updateXtgly(xtGlyxx);
            }else if(1 >= (Integer.parseInt(xtGlyxx.getKydlsbcs()) - Integer.parseInt(xtGlyxx.getYdlsbcs()))){//如果可以登陆失败次数减掉已登陆失败次数小于等于0次
                if(xtGlyxx.getDlsbsdsj() == null || xtGlyxx.getDlsbsdsj() ==0){//说明没被锁定过
                    long nowDateTime = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
                    xtGlyxx.setDlsbsdsj(nowDateTime);//登陆失败锁定时间
                    xtGlyxx.setGlyzt("2");//设置管理员状态被锁定
                    int newYdlsbcs = Integer.parseInt(xtGlyxx.getYdlsbcs()) + 1;
                    xtGlyxx.setYdlsbcs(String.valueOf(newYdlsbcs));
                    xtglyService.updateXtgly(xtGlyxx);
                    returnMap.put("message","登陆失败！用户名和密码不匹配！你已经被锁定！");
                    returnMap.put("code","500");
                }
            }
        }
        return returnMap;
    }

    //查询管理员方法列表方法
    @UserLoginToken
    @RequestMapping("/xtgly/getxtgly")
    @ResponseBody
    public Map<String,Object> getXtgly(@RequestBody Map<String, Object> data) throws Exception{
        int start = (int) data.get("start");
        int pageSize = (int) data.get("size");
        PageHelper.startPage(start,pageSize);
        List<XtGlyxx> xtglyList = xtglyService.findXtglyByData(data);
        PageInfo<XtGlyxx> pageInfo =new PageInfo<XtGlyxx>(xtglyList);//获取totals
        Map<String,Object> map = new HashMap<>();
        map.put("total",pageInfo.getTotal());
        map.put("xtglylist",xtglyList);
        return map;
    }

    //更新管理员方法
    @UserLoginToken
    @RequestMapping("/xtgly/updatextgly")
    @ResponseBody
    @Transactional
    public Map<String,Object> updateXtgly(HttpServletRequest request,@RequestBody Map<String, Object> data) throws Exception{
        Map<String,Object> map = new HashMap<>();
        HttpSession session = request.getSession();
        XtGlyxx loginXtglyxx = (XtGlyxx) session.getAttribute("xtglyxx");
        XtGlyxx updateXtGlyxx = xtglyService.selectXtglybyPrimary((String) data.get("glybh"), (String) data.get("zzbm"));
        if(updateXtGlyxx == null){
            map.put("message","操作失败，不存在该管理员，请检查管理员编号！");
            map.put("type","error");
            return map;
        }
        data.forEach((keys,value) -> {
            String key = keys;
            //判断如果传过来的值为空或者为空字符，那么不进行set
            if(value != null && !"".equals(value)){
                switch (key){
                    case "glymc"://管理员名称
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setGlymc((String) value);
                        }
                        break;
                    case "jsbh"://管理员角色编号
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setJsbh((String) value);
                        }
                        break;
                    case "glyzc"://管理员职称
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setGlyzc((String) value);
                        }
                        break;
                    case "glylxfs"://管理员联系方式
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setGlylxfs((String) value);
                        }
                        break;
                    case "dlzh"://登陆账号
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setDlzh((String) value);
                        }
                        break;
                    case "glyzt"://管理员状态
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setGlyzt((String) value);
                        }
                        break;
                    case "kydlsbcs"://管理员可以登陆失败次数
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setKydlsbcs((String) value);
                        }
                        break;
                    case "ydlsbcs"://登陆已失败次数
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setYdlsbcs((String) value);
                        }
                        break;
                    case "glycsrq"://管理员出生日期
                        try {
                            if(value != null && !"".equals(value)){
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                updateXtGlyxx.setGlycsrq(format.parse((String) value));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "dlsdsj"://登陆失败锁定时间
                        if(value != null && !"".equals(value)){
                            updateXtGlyxx.setDlsbsdsj((long) value);
                        }
                        break;
                    case "dlmm"://登陆密码
                        //如果发现原始密码与更新密码相同，则不需要做任何操作，
                        if(value != null && !"".equals(value)){
                            try {
                                String newPassword = new AesUtil().Decrypt((String) value, "abcdefgabcdefg12");//后台调用aes解密将加密的密码解密
                                if(!updateXtGlyxx.getDlmm().equals(newPassword)){//如果不相同，则证明密码有改动，那么需要解密后再存入
                                    updateXtGlyxx.setDlmm(newPassword);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        });

        updateXtGlyxx.setCzrbh(loginXtglyxx.getGlybh());///设置操作人编号为登陆的系统管理员编号
        //ZoneId zoneId = ZoneId.systemDefault();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
//        String nowDateTime = LocalDateTime.now().format(formatter);//先格式化当前时间，然后格式化当前时间
        //LocalDateTime fLocalDateTime = LocalDateTime.parse(nowDateTime);//然后讲格式好的当前时间传为localDateTime类型
        Date systemDate = SystemUtils.getSystemDate();
        updateXtGlyxx.setCzsj(systemDate);

        int updateResult = xtglyService.updateXtgly(updateXtGlyxx);
        if(updateResult == 1){//更新成功
            map.put("message","更新成功！");
            map.put("type","success");
        }else{
            map.put("message","更新失败！");
            map.put("type","error");
        }
        return map;
    }

    //根据主键查询管理员
    @UserLoginToken
    @RequestMapping("/xtgly/getxglybypri")
    @ResponseBody
    public Map<String, Object> selectXtglyByPri(@RequestBody Map<String,Object> requestData){
        String glybh = (String) requestData.get("glybh");
        String zzbm = (String) requestData.get("zzbm");
        Optional.ofNullable(glybh).orElse("");
        Optional.ofNullable(zzbm).orElse("");
        XtGlyxx xtGlyxx = xtglyService.selectXtglybyPrimary(glybh, zzbm);
        if(xtGlyxx == null){
            return new HashMap<>();
        }
        Map<String,Object> returnMap = new HashMap<>();
        returnMap.put("glybh",xtGlyxx.getGlybh());
        returnMap.put("zzbm",xtGlyxx.getZzbm());
        returnMap.put("glylxfs",xtGlyxx.getGlylxfs());
        returnMap.put("glymc",xtGlyxx.getGlymc());
        returnMap.put("jsbh",xtGlyxx.getJsbh());
        returnMap.put("dlzh",xtGlyxx.getDlzh());
        returnMap.put("glyzt",xtGlyxx.getGlyzt());
        returnMap.put("kydlsbcs",xtGlyxx.getKydlsbcs());
        return returnMap;
    }

    //根据主键查询管理员
    @UserLoginToken
    @RequestMapping("/xtgly/getXglyData")
    @ResponseBody
    public XtGlyxx getXglyDataByPri(@RequestBody Map<String,Object> requestData){
        String glybh = (String) requestData.get("glybh");
        String zzbm = (String) requestData.get("zzbm");
        Optional.ofNullable(glybh).orElse("");
        Optional.ofNullable(zzbm).orElse("");
        XtGlyxx xtGlyxx = xtglyService.selectXtglybyPrimary(glybh, zzbm);
        if(xtGlyxx == null){
            return new XtGlyxx();
        }
        return xtGlyxx;
    }

    //添加管理员方法
    @UserLoginToken
    @PostMapping("/xtgly/addxtgly")
    @ResponseBody
    @Transactional
    public Map<String, Object> addXtgly(HttpServletRequest request, @RequestBody Map<String,Object> rqData) throws Exception{
        Map<String,Object> respMap = new HashMap<>();
        HttpSession session = request.getSession();
        XtGlyxx loginXtglyxx = (XtGlyxx) session.getAttribute("xtglyxx");//获取操作人编号
        XtGlyxx checkXtGlyxxOne = xtglyService.selectXtglybyPrimary((String) rqData.get("glybh"), (String) rqData.get("zzbm"));//查看管理员编号是否被创建过了
        if(checkXtGlyxxOne != null){//说明没被创建过
            respMap.put("message","该管理员编号被创建过了！");
            respMap.put("type","error");
            return respMap;
        }
        Map<String,Object> reqMap = new HashMap<>();
        reqMap.put("dlzh",rqData.get("dlzh"));
        reqMap.put("zzbm",rqData.get("zzbm"));
        List<XtGlyxx> checkXtglyTwo= xtglyService.findXtglyByData(reqMap);//根据管理员登陆账号和组织编码去查询，是否存在
        if(checkXtglyTwo != null && !checkXtglyTwo.isEmpty()){//如果查询结果不为空，那么说明该登陆账号被注册过，
            respMap.put("message","该登陆账号被创建过了！");
            respMap.put("type","error");
            return respMap;
        }

        XtGlyxx addXtgly = new XtGlyxx();
        rqData.forEach((keys,value) ->{
            String key = keys;
            //判断如果传过来的值为空或者为空字符，那么不进行set
            if(value != null && !"".equals(value)){
                switch (key){
                    case "glybh":
                        addXtgly.setGlybh((String) value);
                        break;
                    case "zzbm":
                        addXtgly.setZzbm((String) value);
                        break;
                    case "glymc":
                        addXtgly.setGlymc((String) value);
                        break;
                    case "jsbh":
                        addXtgly.setJsbh((String) value);
                        break;
                    case "dlzh":
                        addXtgly.setDlzh((String) value);
                        break;
                    case "glyzt":
                        addXtgly.setGlyzt((String) value);
                        break;
                    case "kydlsbcs":
                        addXtgly.setKydlsbcs((String) value);
                        break;
                }
            }
        });
        addXtgly.setDlmm("0375d2353d97e8defa58451487faed5b");///初始化管理员登陆密码为123456
        addXtgly.setCzrbh(loginXtglyxx.getGlybh());///设置操作人编号为登陆的系统管理员编号
        addXtgly.setCjrbh(loginXtglyxx.getGlybh());///设置操作人编号为登陆的系统管理员编号
        Date systemDate = SystemUtils.getSystemDate();
        addXtgly.setCzsj(systemDate);//设置管理员操作时间
        addXtgly.setCjsj(systemDate);//设置管理员创建时间
        int addResult = xtglyService.addXtglyService(addXtgly);
        if(addResult == 1){
            respMap.put("message","添加成功！");
            respMap.put("type","success");
            return respMap;
        }else{
            respMap.put("message","添加失败！");
            respMap.put("type","error");
            return respMap;
        }
    }


    //删除管理员
    @UserLoginToken
    @PostMapping("/xtgly/delxtgly")
    @ResponseBody
    @Transactional
    public Map<String, Object> deltglyX(@RequestBody Map<String,Object> rqData) throws Exception{
        Map<String,Object> respMap = new HashMap<>();
        String glybh = (String) rqData.get("glybh");
        String zzbm = (String) rqData.get("zzbm");
        //以下为了获取都不为空，再去执行查询
        if(glybh == null || "".equals(glybh)){
            respMap.put("message","删除失败！管理员编号为空！");
            respMap.put("type","error");
            return respMap;
        }

        if(zzbm == null || "".equals(zzbm)){
            respMap.put("message","删除失败！组织编码为空！");
            respMap.put("type","error");
            return respMap;
        }
        int delResult = xtglyService.delXtgly(glybh, zzbm);
        if(delResult == 1){
            respMap.put("message","删除成功！");
            respMap.put("type","success");
            return respMap;
        }else{
            respMap.put("message","删除失败！");
            respMap.put("type","error");
            return respMap;
        }
    }

    //删除选中的管理员
    @UserLoginToken
    @PostMapping("/xtgly/delxtglys")
    @ResponseBody
    @Transactional
    public Map<String, Object> delxtglys(@RequestBody List<Object> rqData) throws Exception{
        System.out.println(rqData.get(0).toString());
        Map<String,Object> respMap = new HashMap<>();
        int delResult = xtglyService.delXtglys(rqData);
        if(delResult >= 1){
            respMap.put("message","删除成功！");
            respMap.put("type","success");
            return respMap;
        }else{
            respMap.put("message","删除失败！");
            respMap.put("type","error");
            return respMap;
        }
    }


    //更新管理资料
    @UserLoginToken
    @PostMapping("/xtgly/upXtglyData")
    @ResponseBody
    @Transactional
    public Map<String, Object> updateXtglyData(@RequestBody Map<String,Object> rqData) throws Exception{
        Map<String,Object> respMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        rqData.put("glycsrq",format.parse((String) rqData.get("glycsrq")));
        int updateResult = xtglyService.updateXtglyData(rqData);
        if(updateResult >= 1){
            respMap.put("message","资料修改成功！");
            respMap.put("type","success");
            return respMap;
        }else{
            respMap.put("message","资料修改失败！");
            respMap.put("type","error");
            return respMap;
        }
    }

    //更新管理资料
    @UserLoginToken
    @PostMapping("/xtgly/resetPassword")
    @ResponseBody
    @Transactional
    public Map<String, Object> resetPassword(@RequestBody List<Map<String,Object>> rqDataList) throws Exception{
        Map<String,Object> respMap = new HashMap<>();
        int delResult = xtglyService.resetPassword(rqDataList);
        if(delResult >= 1){
            respMap.put("message","密码重置成功！");
            respMap.put("type","success");
            return respMap;
        }else{
            respMap.put("message","密码重置失败！");
            respMap.put("type","error");
            return respMap;
        }
    }
}
