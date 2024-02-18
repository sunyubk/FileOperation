package com.sy.fileoperation.entity.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @ClassName Chunk
 * @Description
 * @Author sunyu
 * @Date 2024/2/15 12:15
 * @Version 1.0
 **/
@Data
public class ChunkDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件md5
     */
    private String identifier;
    /**
     * 分块文件
     */
    MultipartFile file;
    /**
     * 当前分块序号
     */
    private Integer chunkNumber;
    /**
     * 分块大小
     */
    private Long chunkSize;
    /**
     * 当前分块大小
     */
    private Long currentChunkSize;
    /**
     * 文件总大小
     */
    private Long totalSize;
    /**
     * 分块总数
     */
    private Integer totalChunks;
    /**
     * 文件名
     */
    private String filename;
}
