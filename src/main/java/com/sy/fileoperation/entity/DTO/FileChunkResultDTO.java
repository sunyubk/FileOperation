package com.sy.fileoperation.entity.DTO;

import lombok.Data;

import java.util.Set;

/**
 * @ClassName FileChunkResultDTO
 * @Description
 * @Author sunyu
 * @Date 2024/2/16 11:32
 * @Version 1.0
 **/

@Data
public class FileChunkResultDTO {

    /**
     * 是否跳过上传
     */
    private Boolean skipUpload;

    /**
     * 已上传分片的集合
     */
    private Set<Integer> uploaded;

    public FileChunkResultDTO(Boolean skipUpload) {
        this.skipUpload = skipUpload;
    }

    public FileChunkResultDTO(Boolean skipUpload, Set<Integer> uploaded) {
        this.skipUpload = skipUpload;
        this.uploaded = uploaded;
    }
}
