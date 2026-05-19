package com.healthtrail.domain.health.problem.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 健康问题状态变更日志 Mapper */
@Mapper
public interface HealthProblemStatusLogMapper extends BaseMapper<HealthProblemStatusLogEntity> {
}