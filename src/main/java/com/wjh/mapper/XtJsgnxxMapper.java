package com.wjh.mapper;

import com.wjh.entity.XtJsgnxx;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface XtJsgnxxMapper {
    int insertByList(@Param("addXtgnList") List<XtJsgnxx> addXtgnList);

    int delGnxxByGnbhList (@Param("gnbhList") List<String> gnbhList,@Param("glybh") String glybh);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_jsgnxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    int deleteByPrimaryKey(@Param("jsbh") String jsbh, @Param("gnbh") String gnbh);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_jsgnxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    int insert(XtJsgnxx record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_jsgnxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    XtJsgnxx selectByPrimaryKey(@Param("jsbh") String jsbh, @Param("gnbh") String gnbh);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_jsgnxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    List<XtJsgnxx> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table xt_jsgnxx
     *
     * @mbg.generated Sat Feb 15 02:02:53 CST 2020
     */
    int updateByPrimaryKey(XtJsgnxx record);
}