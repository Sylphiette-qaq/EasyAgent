package com.demo.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.agent.model.entity.FileInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息表Mapper接口
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfoEntity> {
}