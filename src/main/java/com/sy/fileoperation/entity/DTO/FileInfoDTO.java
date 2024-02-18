package com.sy.fileoperation.entity.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName FileInfo
 * @Description
 * @Author sunyu
 * @Date 2024/2/15 12:27
 * @Version 1.0
 **/
@Data
public class FileInfoDTO implements Serializable {

    private Long id;

    private String filename;

    private String identifier;

    private Long totalSize;

    private String type;

    private String location;
}
