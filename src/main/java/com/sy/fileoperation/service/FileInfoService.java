package com.sy.fileoperation.service;

import com.sy.fileoperation.entity.DTO.ChunkDTO;
import com.sy.fileoperation.entity.DTO.FileChunkResultDTO;
import com.sy.fileoperation.entity.FileInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 文件信息表 服务类
 * </p>
 *
 * @author 
 * @since 2024-02-14
 */
public interface FileInfoService extends IService<FileInfo> {

    /**
     * 检查文件是否存在
     * @return FileChunkResultDTO
     */
    FileChunkResultDTO checkChunk(ChunkDTO chunk);

    /**
     * 上传文件块
     */
    void uploadChunk(ChunkDTO chunk);

    /**
     * 合并文件
     * @return boolean
     */
    boolean mergeFile(ChunkDTO chunk);
}
