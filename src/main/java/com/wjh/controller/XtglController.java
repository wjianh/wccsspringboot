package com.wjh.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wjh.config.UserLoginToken;
import com.wjh.entity.*;
import com.wjh.service.XtdmxxService;
import com.wjh.service.XtglService;
import com.wjh.service.XtglyService;
import com.wjh.utils.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class XtglController {
    @Autowired
    XtglService xtglService;

    @Autowired
    XtdmxxService xtdmxxService;
    //
    @Autowired
    XtglyService xtglyService;

    @UserLoginToken
    @GetMapping("/xtgl/getxtzzxx")
    @ResponseBody
    public List<Map<String,Object>> getXtzz(){
        List<Map<String,Object>> treeList = new ArrayList<>();
        List<XtZzxx> xtzzList = xtglService.getXtzz();
        //Java8新特性处理空指针异常，结合遍历
        Optional.ofNullable(xtzzList).orElse(new ArrayList<XtZzxx>()).stream().forEach(item -> {
//            说明该节点存在父节点，则现在就去寻找父节点
            if(item.getGsdwsjbm() != null && !"".equals(item.getGsdwsjbm())){//证明该节点不为根节点，下一步则寻找father
                boolean allJg = formatZzxxList(treeList, item);
                if(allJg == false){//证明在根列表找不到该节点的父节点，那么在根节点下创建新的节点作为新的节点
                    Map<String, Object> map = new HashMap<>();
                    map.put("zzbm",item.getGsdwbm());
                    map.put("zzmc",item.getGsdwmc());
                    map.put("children",new ArrayList<Map<String,Object>>());
                    treeList.add(map);
                }//否则找到了父节点
            }else{//证明该节点为根节点
                if(treeList.isEmpty()){
                    Map<String, Object> map = new HashMap<>();
                    map.put("zzbm",item.getGsdwbm());
                    map.put("zzmc",item.getGsdwmc());
                    map.put("children",new ArrayList<Map<String,Object>>());
                    treeList.add(map);
                }else{
                    List<Map<String,Object>> list = new ArrayList<>();//存放子节点父节点的子节点
                    treeList.forEach(mapitem ->{
                        list.add(mapitem);
                    });
                    treeList.clear();//先清除根集合保证只存放一个根节点
                    Map<String,Object> faterMap = new HashMap<>();//新建一个对象存放该节点的父节点对象
                    faterMap.put("zzbm",item.getGsdwbm());//将该节点的父节点的组织编码存入map
                    faterMap.put("zzmc",item.getGsdwmc());//将该节点的父节点的组织名称存入map
                    faterMap.put("children",list);//往该节点的父节点新建一个children集合存入
                    treeList.add(faterMap);
                }
            }
        });
        return treeList;
    }

    //格式化组织list
    private boolean formatZzxxList(List<Map<String,Object>> list,XtZzxx xtZzxx){
        AtomicBoolean flag = new AtomicBoolean(false);//默认找不到该节点的父节点
        Optional.ofNullable(list).orElse(new ArrayList<Map<String,Object>>()).forEach(mapItem -> {
            if(Optional.ofNullable(mapItem.get("zzbm")).orElse(new String()).equals(xtZzxx.getGsdwsjbm())){
                Map<String, Object> map = new HashMap<>();
                map.put("zzbm",xtZzxx.getGsdwbm());
                map.put("zzmc",xtZzxx.getGsdwmc());
                //找到父节点后，如果父节点的children为空创建新的，否则直接添加进去
                List<Map<String,Object>> children = (List<Map<String, Object>>) mapItem.get("children");
                //判断子节点list是否为空，如果不为空返回该节点的子节点，如果为空那么返回一个新的list
                if(children == null ){
                    List<Map<String, Object>> childrenList = new ArrayList<Map<String,Object>>();
                    childrenList.add(map);
                    mapItem.put("children",childrenList);
                }else{
                    children.add(map);
                }
                flag.set(true);//证明找到了父亲，
            }
        });
        if(flag.get() == false){//证明该层没有该节点的子节点,接下来继续调用该方法去下一层查找
            Optional.ofNullable(list).orElse(new ArrayList<Map<String, Object>>()).forEach(mapItem -> {
                List<Map<String,Object>> children = (List<Map<String, Object>>) Optional.ofNullable(mapItem.get("children")).orElse(new ArrayList<Map<String, Object>>());
                boolean findJg = formatZzxxList(children, xtZzxx);
                if (findJg == true){
                    flag.set(findJg);
                    return;
                }
            });
        }
        return flag.get();
    }


    //获取系统组织还有管理员
    @UserLoginToken
    @GetMapping("/xtgl/getxtzzgly")
    @ResponseBody
    public List<Map<String,Object>> getXtzzGly(){
        List<Map<String,Object>> treeList = new ArrayList<>();
        List<XtZzxx> xtzzList = xtglService.getXtzz();
        List<XtGlyxx> allXtgly = xtglyService.findAllXtgly();//先获取所有系统管理员
        if(xtzzList != null && !xtzzList.isEmpty()){
            treeList = treeTraversal(xtzzList);
        }

        if(treeList != null && !treeList.isEmpty()){
            for (XtZzxx xtzz:xtzzList) {//遍历系统组织列表，分别给tree List对照添加label

                formatTreeList(treeList,xtzz);//递归进入treeList里面去查找
            }

        }
        if(allXtgly != null && !allXtgly.isEmpty()){//遍历查找出的所有系统管理员然后遍历去查找它的父亲，然后把它添加进入父节点的children列表里面
            for (XtGlyxx xtgly:allXtgly) {//遍历系统组织列表，分别给tree List对照添加系统管理员
                addXtglyToZz(treeList,xtgly);
            }
        }
        return treeList;
    }

    //往各个系统组织下添加系统管理员
    private boolean addXtglyToZz(List<Map<String,Object>> list,XtGlyxx xtGlyxx) {
        boolean findFlag = false;
        for (Map<String,Object> map:list) {
            if(map.get("zzbm") != null && map.get("zzbm").equals(xtGlyxx.getZzbm())){//证明找到了，然后添加一个label属性
                List<Map<String,Object>> childrenList = (List<Map<String,Object>>) map.get("children");
                if(childrenList != null && !childrenList.isEmpty()){
                    Map<String, Object> childMap = new HashMap<>();
                    childMap.put("label",xtGlyxx.getGlymc());
                    childMap.put("glybh",xtGlyxx.getGlybh());//添加一个就角色编号以作为管理员标志，区分管理员还是组织
                    childMap.put("zzbm",xtGlyxx.getGlybh());//将系统管理员编号作为组织编码统一格式前端方便处理
                    childrenList.add(childMap);//将其添加到该组织的子节点
                }else{
                    List<Map<String,Object>> addChildrenList = new ArrayList<>();
                    Map<String, Object> adChildMap = new HashMap<>();
                    adChildMap.put("label",xtGlyxx.getGlymc());
                    adChildMap.put("glybh",xtGlyxx.getGlybh());//添加一个就角色编号以作为管理员标志
                    adChildMap.put("zzbm",xtGlyxx.getGlybh());//将系统管理员编号作为组织编码统一格式前端方便处理
                    addChildrenList.add(adChildMap);//将其添加到该组织的子节点
                    map.put("children",addChildrenList);//给该对象增加一个children节点
                }
                findFlag = true;
                return true;//证明找到了则返回true
            }
        }
        if(findFlag == false){//证明在该层没有找到匹配的父节点，则递归进入下一层找
            for (Map<String,Object> map:list) {
                List<Map<String,Object>> childrenList = (List<Map<String,Object>>) map.get("children");
                if(childrenList != null && !childrenList.isEmpty()){
                    boolean findJieGuo = addXtglyToZz(childrenList, xtGlyxx);
                    if(findJieGuo == true){
                        return true;
                    }
                }

            }
        }
        return false;//默认没有找到
    }
    private boolean formatTreeList(List<Map<String,Object>> mapList,XtZzxx xtZzxx){//格式化treeList,往列表里面添加label
        boolean findFlag = false;//定义该层查询结果flag
        for (Map<String,Object> map:mapList) {//在该层寻找有没有匹配的
            if(map.get("zzbm") != null && map.get("zzbm").equals(xtZzxx.getGsdwbm())){//证明找到了，然后添加一个label属性
                map.put("label",xtZzxx.getGsdwmc());
                findFlag = true;
                return true;//证明找到了则返回true
            }
        }
        if(findFlag == false){//如果没有找到，则进入下一层寻找直到找到为止
            for (Map<String,Object> mapfater:mapList) {//遍历该层获取子节点传入
                List<Map<String,Object>> childrenList = (List<Map<String, Object>>)mapfater.get("children");
                if(childrenList != null && !childrenList.isEmpty()){//不为空则进入下一层
                    Boolean jieguo = formatTreeList(childrenList, xtZzxx);//递归进入下一层找,
                    if(jieguo == true){
                        return jieguo;
                    }
                }
            }

        }

        return false;

    }
    private List<Map<String,Object>> treeTraversal(List<XtZzxx> xtZzxxList ){
        List<Map<String,Object>> returnList = new ArrayList<>();
        for (XtZzxx zzxx:xtZzxxList) {//开始遍历组织信息树
            if (zzxx.getGsdwsjbm() != null && !"".equals(zzxx.getGsdwsjbm())){//该节点存在父节点
                if(returnList == null || returnList.isEmpty()){//returnList为空然后第一次添加进去
                    List<Object> list = new ArrayList<>();//存放子节点父节点的子节点
                    Map<String,Object> childMap = new HashMap<>();//创建一个mapz对象自己存放child信息
                    childMap.put("zzbm",zzxx.getGsdwmc());
                    list.add(childMap);//然后加入集合作为子节点
                    Map<String,Object> faterMap = new HashMap<>();//新建一个对象存放该节点的父节点对象
                    faterMap.put("zzbm",zzxx.getGsdwsjbm());//将该节点的父节点的组织编码存入map
                    //faterMap.put("zzmc",zzxx.getGsdwsj());//将该节点的父节点的组织名称存入map
                    faterMap.put("children",list);//往该节点的父节点新建一个children集合存入
                    returnList.add(faterMap);//往根节点添加//前提是根节点为空的情况下r
                }else{//returnList已经不为空
                    Boolean fater = findFater(returnList, zzxx);
                    if(fater==false){//证明没有找到父节点，没有父节点则在该层创建该节点为父亲节点
                        List<Object> list = new ArrayList<>();//存放子节点父节点的子节点
                        Map<String,Object> childMap = new HashMap<>();//创建一个mapz对象自己存放child信息
                        childMap.put("zzbm",zzxx.getGsdwbm());
                        list.add(childMap);//然后加入集合作为子节点
                        Map<String,Object> faterMap = new HashMap<>();//新建一个对象存放该节点的父节点对象
                        faterMap.put("zzbm",zzxx.getGsdwsjbm());//将该节点的父节点的组织编码存入map
                        //faterMap.put("zzmc",zzxx.getGsdwsjbm());//将该节点的父节点的组织名称存入map
                        faterMap.put("children",list);//往该节点的父节点新建一个children集合存入
                        returnList.add(faterMap);//往根节点添加//前提是根节点为空的情况下r
                    }
//                    for (Map.Entry<String, Object> entry : treeRoot.entrySet()) {
//
//                    }

                }
            }else{//判断是根节点直接在根列表里面添加该对象的map
                if(returnList == null || returnList.isEmpty()){
                    List<Object> list = new ArrayList<>();//存放该节点的子节点
                    Map<String,Object> faterMap = new HashMap<>();//新建一个对象存放该节点的父节点对象
                    faterMap.put("zzbm",zzxx.getGsdwbm());//将该节点的父节点的组织编码存入map
                    //faterMap.put("zzmc",zzxx.getGsdwmc());//将该节点的父节点的组织名称存入map
                    faterMap.put("children",list);//往该节点的父节点新建一个children集合存入
                    returnList.add(faterMap);//往根节点添加//前提是根节点为空的情况下r
                }else{//如果根列表不为空,则将之前的returnList 作为该对象的子节点列表

                    List<Map<String,Object>> list = new ArrayList<>();//存放子节点父节点的子节点
                    for (Map<String,Object> secondChild:returnList) {
                        list.add(secondChild);
                    }
                    returnList.clear();//先清除根集合保证只存放一个根节点
                    Map<String,Object> faterMap = new HashMap<>();//新建一个对象存放该节点的父节点对象
                    faterMap.put("zzbm",zzxx.getGsdwbm());//将该节点的父节点的组织编码存入map
                    //faterMap.put("zzmc",zzxx.getGsdwmc());//将该节点的父节点的组织名称存入map
                    faterMap.put("children",list);//往该节点的父节点新建一个children集合存入
                    returnList.add(faterMap);
                }
            }
        }
        return returnList;
    }
    /*
    * @pram list childrenList
    * @parma zzbm fatherName
    * 寻找组织父亲
    * */
    private Boolean findFater(List<Map<String,Object>> list,XtZzxx zzxx){
        boolean flag = false;//默认在该层没有找到该节点的父节点，
        for (Map<String,Object> childrenMapObj : list) {
            if(childrenMapObj.get("zzbm")!=null && childrenMapObj.get("zzbm").equals(zzxx.getGsdwsjbm())){//如果在传进来的列表里面找到父节点
                flag = true;//认为找到了该节点的父节点
                List<Map<String,Object>> nextChildren = (List<Map<String,Object>>) childrenMapObj.get("children");
                if(nextChildren!=null && !nextChildren.isEmpty()){//子节点列表不为空，直接添加
                    Map<String,Object> childMap = new HashMap<>();
                    childMap.put("zzbm",zzxx.getGsdwbm());
                    nextChildren.add(childMap);
                }else{//为空，然后创建新的列表作为该父节点的子节点列表
                    List<Map<String,Object>> childrenList = new ArrayList<>();
                    Map<String,Object> childMap = new HashMap<>();
                    childMap.put("zzbm",zzxx.getGsdwbm());
                    childrenList.add(childMap);
                    childrenMapObj.put("children",childrenList);
                }

                return true;//代表在该层找到了父节点
            }
        }
        if(flag==false){//在该层没有找到该节点的父节点，则进入下一层找
            for (Map<String,Object> childrenMapObj : list) {//分别进入该层的子节点去查找
                List<Map<String,Object>> nextChildren = (List<Map<String,Object>>) childrenMapObj.get("children");
                if(nextChildren != null && !nextChildren.isEmpty()){//不为空则进入下一层
                    Boolean fater = findFater(nextChildren, zzxx);//递归进入下一层找,
                    if(fater == true){
                        return fater;
                    }
                }
                //为空则结束查找返回false
            }
        }
        return false;//找不到
    }

    @UserLoginToken
    @GetMapping("/xtgl/getxtgnxx")
    @ResponseBody
    public List<Object> getGnxx(HttpSession session) throws Exception{
        List<Object> returnList = new ArrayList<>();
        XtGlyxx xtglyxx = (XtGlyxx) session.getAttribute("xtglyxx");//获取session存入的系统管理员信息
        List<XtGnxx> xtGnxxList = xtglService.findGnxx(xtglyxx.getGlybh());////获取该角色信息下面的功能
        if(xtGnxxList == null || xtGnxxList.size() == 0){
            return returnList;
        }
        xtGnxxList.forEach(item -> {
            if(Optional.ofNullable(item.getGnlbdm()).orElse("").equals("0")){//判断为一级菜单信息那么直接在根节点下创建一个集合存放一级菜单
                if(returnList.isEmpty()){//returnList 如果为空那么创建新的一级菜单存放列表
                    List<Map<String,Object>> firstList=  new ArrayList<>();//存放功能
                    Map<String, Object> firstMenuMap = addMap(item);
                    firstList.add(firstMenuMap);
                    returnList.add(firstList);
                }else{//不为空
                    List<Map<String,Object>> menuList = (List<Map<String, Object>>) returnList.get(0);//这个是存放功能的集合,
                    //然后直接将创建一个节点存入menuList里面
                    Map<String, Object> firstMenuMap = addMap(item);
                    menuList.add(firstMenuMap);
                }
            }else{//如果该菜单项为子菜单，那么下面就去寻找它的父菜单，并且添加到下面
                List<Map<String,Object>> menuList = (List<Map<String, Object>>) returnList.get(0);
                boolean findFlag = findMenuFather(menuList, item);
                if(findFlag == false){//说明没有找到该节点的父亲节点，那么在添加进入一级菜单里面
                    menuList.add(addMap(item));
                }
            }
        });
        List<Map<String, Object>> routeList = findRouteFather((List<Map<String, Object>>) returnList.get(0),"/home");
        returnList.add(routeList);
        return returnList;
    }

    //寻找菜单的父亲方法
    private boolean findMenuFather(List<Map<String,Object>> menuList, XtGnxx xtGnxx){
        AtomicBoolean flag = new AtomicBoolean(false);
        menuList.forEach(item -> {
            if(Optional.ofNullable(item.get("gnbh")).orElse("").equals(xtGnxx.getSjgnbh())){//如果找到了父亲节点
                if((List<Map<String, Object>>) item.get("children") == null){
                    List<Map<String, Object>> childrenMenu = new ArrayList<>();
                    childrenMenu.add(addMap(xtGnxx));//将该节点添加到它父节点下
                    item.put("children",childrenMenu);
                }else{
                    List<Map<String,Object>> childrenMenu = (List<Map<String, Object>>) item.get("children");
                    childrenMenu.add(addMap(xtGnxx));//将该节点添加到它父节点下
                }
                flag.set(true);//找到了父菜单
                return;
            }
        });
        if(flag.get() == false){//如果在该层没找到子菜单，则进入下一层找
            menuList.forEach(item ->{
                List<Map<String,Object>> childrenList = (List<Map<String, Object>>) item.get("children");
                if(childrenList != null && !childrenList.isEmpty()){//如果获取子节点不为空，则进入下一层去找
                    boolean findFlag = findMenuFather(childrenList, xtGnxx);
                    if (findFlag == true){
                        flag.set(findFlag);
                        return;
                    }
                }
            });
        }
        return flag.get();
    }

    //寻找路由父亲方法
    //rootUrl 将前面一级的路由拼接到该节点路由前面，那么就是一个完整的路由
    private List<Map<String,Object>> findRouteFather(List<Map<String,Object>> menuList,String rootUrl){
        List<Map<String,Object>> childRouteList = new ArrayList<>();
        Optional.ofNullable(menuList).orElse(new ArrayList<>()).forEach(item -> {
            Map<String,Object> firsRouteMap = new HashMap<>();
            firsRouteMap.put("url",rootUrl+item.get("gnljdz"));
            firsRouteMap.put("name",item.get("gnmc"));
            firsRouteMap.put("zjljdz",item.get("zjljdz"));
            if(item.get("children") != null && !item.get("children").equals("")){//子节点不为空
                //递归获取子路由，然后将子路由添加到该路由的子路由下面去
                List<Map<String, Object>> childrenRoute = findRouteFather((List<Map<String, Object>>) item.get("children"), rootUrl+(String) item.get("gnljdz"));
                firsRouteMap.put("children",childrenRoute);
            }
            childRouteList.add(firsRouteMap);
            //routeList.add(addRoute(item));//将该层的menu菜单存入进列表里面
        });
        return childRouteList;
    }

    //创建菜单map的静态方法
    private Map<String,Object> addMap(XtGnxx xtGnxx){
        Map<String,Object> firsMenuMap = new HashMap<>();
        firsMenuMap.put("gnbh",xtGnxx.getGnbh());
        firsMenuMap.put("gnmc",xtGnxx.getGnmc());
        firsMenuMap.put("sjgnbh",xtGnxx.getSjgnbh());
        firsMenuMap.put("gnljdz",xtGnxx.getGnljdz());
        firsMenuMap.put("zjljdz",xtGnxx.getZjljdz());
        firsMenuMap.put("icon",xtGnxx.getGnicon());
        return firsMenuMap;
    }

    //查找管理员已分配的功能
    @RequestMapping("/xtgl/getxtallgnxx")
    @ResponseBody
    public Map<String,Object> getXtgnByGlybh(@RequestBody Map<String,Object> dataMap){
        String glybh = (String) dataMap.get("glybh");//获取管理员编号，查询系统为该管理员分配了那些功能权限
        Map<String,Object> returnMap = new HashMap<>();//存放两个列表，一个是功能列表另一个是功能路由列表
        List<String> glyGnList = new ArrayList<>();
        List<Map<String,Object>> xtGnList = new ArrayList<>();
        if(glybh != null && !"".equals(glybh)){
            List<XtGnxx> glyGn = xtglService.findGlyGn(glybh);//获取的是已经分配的功能，放在穿梭框右边的
            List<XtGnxx> xtGn = xtglService.findXtGn();//获取的系统中所有的功能，然后提取出还未分配功能的key
            Optional.ofNullable(glyGn).orElse(new ArrayList<XtGnxx>()).forEach(yfpgn -> {//遍历已分配的系统功能信息
                glyGnList.add(yfpgn.getGnbh());
            });
            Optional.ofNullable(xtGn).orElse(new ArrayList<XtGnxx>()).forEach(wfpgn -> {//遍历未分配的系统功能
                Map<String, Object> wfpgnMap = new HashMap<>();
                wfpgnMap.put("gnbh",wfpgn.getGnbh());
                wfpgnMap.put("gnmc",wfpgn.getGnmc());
                xtGnList.add(wfpgnMap);
            });

        }
        returnMap.put("leftGnxx",xtGnList);//未分配的功能
        returnMap.put("rightGnxx",glyGnList);//已分配的功能
        return  returnMap;
    }

    //更新系统功能权限
    @UserLoginToken
    @RequestMapping("/xtgl/updateXtgnQx")
    @ResponseBody
    @Transactional
    public Map<String,String> updateXtGnxx(HttpServletRequest request, @RequestBody Map<String, Object> dataMap ) throws Exception{//为用户新增系统功能
        Map<String,String> returnMap = new HashMap<>();
        HttpSession session = request.getSession();
        XtGlyxx xtglyxx = (XtGlyxx) session.getAttribute("xtglyxx");
        String xtglybh = xtglyxx.getGlybh();//获取登陆的管理员编号，作为操作人，和创建人
        List<String> changeXtGnKeys = (List<String>) dataMap.get("moveKeyList");
        String glybh = (String) dataMap.get("glybh");
        String mode = (String) dataMap.get("mode");
        if(mode != null && !"".equals(mode)){
        System.out.println(mode);
        if(mode.equals("remove")){//取消功能权限则去从权限表里面删除
            if(changeXtGnKeys != null && glybh != null && !"".equals(glybh)){
                System.out.println(glybh);
                int removeJg = xtglService.removeXtgnQx(changeXtGnKeys, glybh);
                System.out.println(removeJg);
                if(removeJg!=0){
                    returnMap.put("message","取消权限成功！");
                    returnMap.put("type","success");
                }else{
                    returnMap.put("message","取消权限失败！");
                    returnMap.put("type","error");
                }
            }
        }else if(mode.equals("add")){
            if(changeXtGnKeys != null && glybh != null && !"".equals(glybh)){
                List<XtJsgnxx> addXtgnList = new ArrayList<>();
                Date date = new Date();
                Timestamp timeStamp = new Timestamp(date.getTime());
                for (String key:changeXtGnKeys) {
                    XtJsgnxx jsgnxx = new XtJsgnxx();
                    jsgnxx.setGnbh(key);
                    jsgnxx.setGlybh(glybh);
                    if(xtglybh != null && !"".equals(xtglybh)){
                        jsgnxx.setCjrbs(xtglybh);
                        jsgnxx.setCzrbs(xtglybh);
                    }
                    jsgnxx.setCjsj(timeStamp);
                    jsgnxx.setCzsj(timeStamp);
                    addXtgnList.add(jsgnxx);//添加jsgnxx对象，然后执行insert
                }

                int addJg = xtglService.addXtgnQx(addXtgnList);//调用service执行插入数据
                if(addJg!=0){
                    returnMap.put("message","添加权限成功！");
                    returnMap.put("type","success");
                }else{
                    returnMap.put("message","添加权限失败！");
                    returnMap.put("type","error");
                }
            }
        }
        }
        return returnMap;
    }

    //获取格式化的列表
    @UserLoginToken
    @GetMapping("/xtgl/getformater")
    @ResponseBody
    public Map<String,Object> getXtdmxx(){//页面初始化加载格式化代码
        List<Map<String,Object>> groupDmflbm = xtdmxxService.getGroupDmflbm();
        List<XtDmxx> xtdmxx = xtdmxxService.getXtdmxx();
        Map<String,Object> returnGroupDmMap = new HashMap<>();
        for (Map<String,Object> resultMap : groupDmflbm) {//遍历分类代码，然后归类
            List<Map<String,Object>> groupDmMapList = new ArrayList<>();//不同组的代码新建一个列表保存
            for (XtDmxx item : xtdmxx) {
                if (resultMap.get("dmflbm").equals(item.getDmflbm())){//如果是该类的代码则存入
                    Map<String,Object> map = new HashMap<>();//存放每一个代码的map
                    map.put("dmbs",item.getDmbs());
                    map.put("dmmc",item.getDmmc());
                    map.put("dmflbm",item.getDmflbm());
                    groupDmMapList.add(map);
                }
            }
            returnGroupDmMap.put(resultMap.get("dmflbm").toString(),groupDmMapList);//按照代码分类代码添加该组的代码
        }
        List<XtZzxx> xtZzxx = xtglService.getXtzz();
        List<Map<String,Object>> groupDmMapList = new ArrayList<>();
        Optional.ofNullable(xtZzxx).orElse(new ArrayList<>()).forEach(xtzzxx -> {//将系统组织代码存入格式化代码中
            Map<String,Object> map = new HashMap<>();//存放每一个代码的map
            map.put("dmbs",xtzzxx.getGsdwbm());
            map.put("dmmc",xtzzxx.getGsdwmc());
            map.put("dmflbm","ZZLBDM");
            groupDmMapList.add(map);
        });
        returnGroupDmMap.put("ZZLBDM",groupDmMapList);

        return returnGroupDmMap;
    }

    //按查询条件查询系统代码
    @UserLoginToken
    @RequestMapping("/xtgl/getxtdm")
    @ResponseBody
    public Map<String,Object> getXtdmList(@RequestBody Map<String, Object> rqData){
        Map<String,Object> respMap = new HashMap<>();
        int start = (int) rqData.get("start");
        int pageSize = (int) rqData.get("size");
        boolean chooseFlag = false;
        if(rqData.get("dmbs") != null && !"".equals(rqData.get("dmbs"))){
            chooseFlag = true;
        }
        else if(rqData.get("dmmc") != null && !"".equals(rqData.get("dmmc"))){
            chooseFlag = true;
        }
        else if(rqData.get("dmflbm") != null && !"".equals(rqData.get("dmflbm"))){
            chooseFlag = true;
        }
        else if(rqData.get("dmzt") != null && !"".equals(rqData.get("dmzt"))){
            chooseFlag = true;
        }else{
            chooseFlag = false;
        }

        List<XtDmxx> xtdmList = null;
        PageInfo<XtDmxx> pageInfo = null;
        if(chooseFlag == true){//说明携带了参数
            PageHelper.startPage(start,pageSize);
            xtdmList = xtdmxxService.selectXtdmByData(rqData);
            pageInfo =new PageInfo<>(xtdmList);//获取totals
        }else{//说明没带参数
            PageHelper.startPage(start,pageSize);
            xtdmList = xtdmxxService.getFatherXtdmList();
            pageInfo =new PageInfo<>(xtdmList);//获取totals
        }
        respMap.put("total",pageInfo.getTotal());
        respMap.put("xtdmlist",xtdmList);
        return respMap;
    }

    //删除系统代码
    @UserLoginToken
    @RequestMapping("/xtgl/deleteXtdm")
    @ResponseBody
    public Map<String,Object> deleteXtdm(@RequestBody Map<String, Object> rqData){
        String dmbs = (String) rqData.get("dmbs");
        Map<String,Object> respMap = new HashMap<>();
        if(dmbs == null || dmbs.equals("")){
            respMap.put("message","删除失败,代码标识为空！");
            respMap.put("type","error");
            return respMap;
        }
        int deleteResult = xtdmxxService.deleteXtdm(dmbs);
        if(deleteResult == 0){
            respMap.put("message","删除失败!");
            respMap.put("type","error");
            return respMap;
        }else{
            respMap.put("message","删除成功!");
            respMap.put("type","success");
            return respMap;
        }
    }

    //删除选中的系统代码列表
    @RequestMapping("/xtgl/delSelectXtdm")
    @ResponseBody
    public Map<String,Object> delSelectXtdm(@RequestBody Map<String, Object> rqData){
        List<String> dmbsList = (List<String>) rqData.get("dmbslist");
        Map<String,Object> respMap = new HashMap<>();
        if(dmbsList == null || dmbsList.isEmpty()){
            respMap.put("message","删除失败,选中的代码为空！");
            respMap.put("type","error");
            return respMap;
        }
        int deleteResult = xtdmxxService.delSelectXtdm(dmbsList);
        if(deleteResult == 0){
            respMap.put("message","删除失败!");
            respMap.put("type","error");
            return respMap;
        }else{
            respMap.put("message","删除成功!");
            respMap.put("type","success");
            return respMap;
        }
    }

    //加载系统代码子节点方法
    @UserLoginToken
    @RequestMapping("/xtgl/loadXtdmChilds")
    @ResponseBody
    public List<XtDmxx> loadXtdmChilds(@RequestBody Map<String, Object> rqData){
        return rqData.get("dmbs") == null || rqData.get("dmbs").equals("") ? new ArrayList<XtDmxx>() : xtdmxxService.loadXtdmChilds((String)rqData.get("dmbs"));
    }

    //添加系统代码方法
    @UserLoginToken
    @RequestMapping("/xtgl/addXtdm")
    @ResponseBody
    @Transactional
    public Map<String,Object> addXtdm(@RequestBody Map<String, Object> rqData,HttpSession session) throws Exception{
        XtDmxx xtDmxx = new XtDmxx();
        XtGlyxx xtgly = (XtGlyxx) session.getAttribute("xtglyxx");
        xtDmxx.setCjrbs(xtgly.getGlybh());
        xtDmxx.setCzrbs(xtgly.getGlybh());

        XtDmxx xtDmxxCheck = xtglService.selectByPrimaryKey((String) rqData.get("dmbs"));
        if(xtDmxxCheck != null ){//说明已经存在该代码，不能执行插入
            Map<String,Object> map =new HashMap<>();
            map.put("message","代码标识重复，请重新输入！");
            map.put("type","error");
            return map;
        }
        Optional.ofNullable(rqData).orElse(new HashMap<>()).forEach((key,value) -> {
            String valueStr = (String) value;
            switch (key){
                case "dmbs":
                    xtDmxx.setDmbs(valueStr);
                    break;
                case "dmmc":
                    xtDmxx.setDmmc(valueStr);
                    break;
                case "dmbh":
                    xtDmxx.setDmbh(valueStr);
                    break;
                case "dmsjdm":
                    xtDmxx.setDmsjdm(valueStr);
                    break;
                case "dmflbm":
                    xtDmxx.setDmflbm(valueStr);
                    break;
                case "dmzt":
                    xtDmxx.setDmzt(valueStr);
                    break;
            }
            Date systemDate = SystemUtils.getSystemDate();
            xtDmxx.setCjsj(systemDate);
            xtDmxx.setCzsj(systemDate);
        });

        int insertResult = xtglService.addXtdm(xtDmxx);
        if(insertResult == 0 ){
            Map<String,Object> map =new HashMap<>();
            map.put("message","插入失败！");
            map.put("type","error");
            return map;
        }else{
            Map<String,Object> map =new HashMap<>();
            map.put("message","插入成功！");
            map.put("type","success");
            return map;
        }
    }


    //更新系统代码方法
    @UserLoginToken
    @RequestMapping("/xtgl/updateXtdm")
    @ResponseBody
    @Transactional
    public Map<String,Object> updateXtdm(@RequestBody Map<String, Object> rqData,HttpSession session){
        String dmbs = (String) rqData.get("dmbs");
        XtDmxx xtDmxx = xtglService.selectByPrimaryKey(dmbs);
        if(xtDmxx == null){
            Map<String,Object> map =new HashMap<>();
            map.put("message","更新失败，代码标识为空！");
            map.put("type","error");
            return map;
        }
        XtGlyxx xtgly = (XtGlyxx) session.getAttribute("xtglyxx");
        xtDmxx.setCzrbs(xtgly.getGlybh());

        Optional.ofNullable(rqData).orElse(new HashMap<>()).forEach((key,value) -> {
            String valueStr = (String) value;
            switch (key){
                case "dmmc":
                    xtDmxx.setDmmc(valueStr);
                    break;
                case "dmbh":
                    xtDmxx.setDmbh(valueStr);
                    break;
                case "dmsjdm":
                    xtDmxx.setDmsjdm(valueStr);
                    break;
                case "dmflbm":
                    xtDmxx.setDmflbm(valueStr);
                    break;
                case "dmzt":
                    xtDmxx.setDmzt(valueStr);
                    break;
            }
            Date systemDate = SystemUtils.getSystemDate();
            xtDmxx.setCzsj(systemDate);
        });

        int updateResult = xtglService.updateXtdm(xtDmxx);
        if(updateResult == 0 ){
            Map<String,Object> map =new HashMap<>();
            map.put("message","更新失败！");
            map.put("type","error");
            return map;
        }else{
            Map<String,Object> map =new HashMap<>();
            map.put("message","更新成功！");
            map.put("type","success");
            return map;
        }
    }
}
