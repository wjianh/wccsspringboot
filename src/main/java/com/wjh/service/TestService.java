package com.wjh.service;

import com.wjh.entity.XtGlyxx;
import com.wjh.mapper.XtGlyxxMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TestService {
    @Autowired
    XtGlyxxMapper xtGlyxxMapper;

    public List<XtGlyxx> findAdmin(){
        return xtGlyxxMapper.selectAll();
    }
}
