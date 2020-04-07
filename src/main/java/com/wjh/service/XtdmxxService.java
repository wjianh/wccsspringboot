package com.wjh.service;

import com.wjh.entity.XtDmxx;
import com.wjh.mapper.XtDmxxMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class XtdmxxService {
    @Autowired
    XtDmxxMapper xtDmxxMapper;

    public List<XtDmxx> getXtdmxx(){
        return xtDmxxMapper.selectAll();
    }

    public List<XtDmxx> getFatherXtdmList(){
        return xtDmxxMapper.selectFatherByData();
    }

    public List<XtDmxx> selectXtdmByData(Map<String,Object> rqData){
        return xtDmxxMapper.selectXtdmByData(rqData);
    }

    public int deleteXtdm(String dmbs){
        return xtDmxxMapper.deleteByPrimaryKey(dmbs);
    }

    public List<Map<String,Object>> getGroupDmflbm(){return  xtDmxxMapper.getGroupDmflbm();}

    public int delSelectXtdm(List<String> rqList){
       return xtDmxxMapper.delSelectXtdm(rqList);
    }

    public List<XtDmxx> loadXtdmChilds(String dmbs) {
        return xtDmxxMapper.loadXtdmChilds(dmbs);
    }
}
