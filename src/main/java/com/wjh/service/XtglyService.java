package com.wjh.service;

import com.wjh.entity.XtGlyxx;
import com.wjh.mapper.XtGlyxxMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("xtglyService")
public class XtglyService {

    @Autowired
    XtGlyxxMapper xtGlyxxMapper;

    public XtGlyxx selectXtglybyPrimary(String glybh,String zzbm){
        return xtGlyxxMapper.selectByPrimaryKey(glybh,zzbm);
    }

    public XtGlyxx findXtglyById(String dlzh){
        return xtGlyxxMapper.findXtglyById(dlzh);
    }

    public List<XtGlyxx> findXtglyByData(Map<String,Object> data) {
       return xtGlyxxMapper.findXtglyByData(data);
    }

    public List<XtGlyxx> findAllXtgly(){
        return xtGlyxxMapper.selectAll();
    }

    public int updateXtgly(XtGlyxx xtGlyxx){
        return xtGlyxxMapper.updateByPrimaryKey(xtGlyxx);
    }

    public int addXtglyService(XtGlyxx addXtglyData){
        return xtGlyxxMapper.insert(addXtglyData);
    }

    public int delXtgly(String glybh, String zzbm){
        return xtGlyxxMapper.deleteByPrimaryKey(glybh,zzbm);
    }

    public int delXtglys(List<Object> rqData){
        return xtGlyxxMapper.delByPrimaryKeyList(rqData);
    }

    public int updateXtglyData(Map<String,Object> rqData){
        return xtGlyxxMapper.updateXtglyData(rqData);
    }

    public int resetPassword(List<Map<String,Object>> rqDataList){
        return xtGlyxxMapper.resetPassword(rqDataList);
    }
}
