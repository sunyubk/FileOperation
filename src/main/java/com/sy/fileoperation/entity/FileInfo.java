package com.sy.fileoperation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * <p>
 * 文件信息表
 * </p>
 *
 * @author 
 * @since 2024-02-14
 */
@Data
@TableName("file_info")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("file_name")
    private String fileName;

    @TableField("suff")
    private String suff;

    @TableField("location")
    private String location;

    @TableField("md5")
    private String md5;

    @TableField("status")
    private Integer status;

    @TableField("update_time")
    private Date updateTime;

    @TableField("create_time")
    private Date createTime;
}
