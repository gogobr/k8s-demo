package com.hxl.mapper;

import com.hxl.entity.VisitLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VisitLogMapper {

    @Insert("INSERT INTO visit_log(client_ip, visit_time) VALUES(#{clientIp}, #{visitTime})")
    void insert(VisitLog log);

    @Select("SELECT COUNT(*) FROM visit_log")
    long count();
}
