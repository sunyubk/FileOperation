package com.sy.fileoperation.controller;

import com.sy.fileoperation.entity.DTO.ChunkDTO;
import com.sy.fileoperation.entity.DTO.FileChunkResultDTO;
import com.sy.fileoperation.entity.DTO.FileInfoDTO;
import com.sy.fileoperation.response.RestApiResponse;
import com.sy.fileoperation.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.sy.fileoperation.service.FileInfoService;
import com.sy.fileoperation.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sy.fileoperation.utils.FileUtils.generatePath;
import static com.sy.fileoperation.utils.FileUtils.merge;

/**
 * 文件操作
 *
 * @author sy
 * @since 2024-02-14
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileInfoController {

    @Value("${file.upload.path}")
    private String uploadFolder;

    @Autowired
    private FileInfoService fileInfoService;

    @GetMapping()
    public ResponseEntity<FileInfo> getById(@RequestParam("id") String id) {
        return new ResponseEntity<>(fileInfoService.getById(id), HttpStatus.OK);
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<Object> upload(ChunkDTO chunkDTO) {
        // fileService.save(params);
        return new ResponseEntity<>("upload successfully", HttpStatus.OK);
    }

    /**
     * 检查分块是否存在
     *
     * @param chunk
     * @return
     */
    @GetMapping("/chunk")
    public RestApiResponse checkChunk(ChunkDTO chunk) {
        FileChunkResultDTO fileChunkCheckDTO;
        try {
            fileChunkCheckDTO = fileInfoService.checkChunk(chunk);
            return RestApiResponse.success(fileChunkCheckDTO);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return RestApiResponse.error(99999, "文件上传失败");
        }
    }


    @PostMapping("/chunk")
    public RestApiResponse uploadChunk(ChunkDTO chunk) {
        try {
            fileInfoService.uploadChunk(chunk);
            return RestApiResponse.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return RestApiResponse.error(99999, "文件上传失败");
        }
    }



    @PostMapping("/mergeFile")
    public RestApiResponse mergeFile(@RequestBody ChunkDTO chunk) {
        try {
            boolean success = fileInfoService.mergeFile(chunk);
            return RestApiResponse.success(success);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return RestApiResponse.error(99999, "文件上传失败");
        }
    }


    @PostMapping(value = "/create")
    public ResponseEntity<Object> create(@RequestBody FileInfo params) {
        fileInfoService.save(params);
        return new ResponseEntity<>("created successfully", HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") String id) {
        fileInfoService.removeById(id);
        return new ResponseEntity<>("deleted successfully", HttpStatus.OK);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<Object> update(@RequestBody FileInfo params) {
        fileInfoService.updateById(params);
        return new ResponseEntity<>("updated successfully", HttpStatus.OK);
    }
}
