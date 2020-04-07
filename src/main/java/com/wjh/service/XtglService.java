package com.wjh.service;

import com.wjh.entity.XtDmxx;
import com.wjh.entity.XtGnxx;
import com.wjh.entity.XtJsgnxx;
import com.wjh.entity.XtZzxx;
import com.wjh.mapper.XtDmxxMapper;
import com.wjh.mapper.XtGnxxMapper;
import com.wjh.mapper.XtJsgnxxMapper;
import com.wjh.mapper.XtZzxxMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("xtglService")
public class XtglService {
    @Autowired
    XtZzxxMapper xtZzxxMapper;

    @Autowired
    XtJsgnxxMapper xtJsgnxxMapper;

    @Autowired
    XtGnxxMapper xtGnxxMapper;

    @Autowired
    XtDmxxMapper xtDmxxMapper;

    public List<XtZzxx> getXtzz(){
        return xtZzxxMapper.selectAll();
    }

    public List<XtGnxx> findGnxx(String jsbh){
        return xtGnxxMapper.findGnxx(jsbh);
    }

    //查找该管理员下分配的功能
    public List<XtGnxx> findGlyGn(String glybh){//查找已经给管理员分配的功能
        return xtGnxxMapper.findGlyGnByGlybh(glybh);
    }

    //查找系统所有的系统未分配的功能
    public List<XtGnxx> findXtGn(){
        return xtGnxxMapper.selectAll();
    }

    //取消该角色下的系统权限
    public int removeXtgnQx(List<String> gnbhList,String glybh){
        return xtJsgnxxMapper.delGnxxByGnbhList(gnbhList,glybh);
    }

    public int addXtgnQx(List<XtJsgnxx> addXtgnList){
        return xtJsgnxxMapper.insertByList(addXtgnList);
    }

    public int addXtdm(XtDmxx xtDmxx){
        return xtDmxxMapper.insert(xtDmxx);
    }

    public int updateXtdm(XtDmxx xtDmxx){
        return xtDmxxMapper.updateByPrimaryKey(xtDmxx);
    }

    public XtDmxx selectByPrimaryKey(String dmbs){
        return  xtDmxxMapper.selectByPrimaryKey(dmbs);
    }
}
