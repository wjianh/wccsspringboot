package com.wjh.mapper;

import com.wjh.entity.XtZzxx;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface XtZzxxMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_zzxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    int deleteByPrimaryKey(String gsdwbm);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_zzxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    int insert(XtZzxx record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_zzxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    XtZzxx selectByPrimaryKey(String gsdwbm);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_zzxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    List<XtZzxx> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_zzxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    int updateByPrimaryKey(XtZzxx record);
}