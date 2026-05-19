package com.healthtrail.domain.health.problem.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 健康问题与慢病档案关联 Mapper */
@Mapper
public interface HealthProblemChronicProfileLinkMapper extends BaseMapper<HealthProblemChronicProfileLinkEntity> {
}