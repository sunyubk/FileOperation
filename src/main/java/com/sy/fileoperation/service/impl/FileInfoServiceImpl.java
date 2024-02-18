package com.sy.fileoperation.service.impl;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sy.fileoperation.entity.DTO.ChunkDTO;
import com.sy.fileoperation.entity.DTO.FileChunkResultDTO;
import com.sy.fileoperation.entity.FileInfo;
import com.sy.fileoperation.mapper.FileInfoMapper;
import com.sy.fileoperation.service.FileInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sy.fileoperation.utils.FileUtils;
import com.sy.fileoperation.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>
 * 文件信息表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-02-14
 */
@Slf4j
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    @Value("${file.upload.path}")
    private String uploadFolder;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 检查文件是否存在，因为前端用的是别人写好的，并没有修改。默认是现在这样，逻辑还可以进行下面的优化：
     * 1、可以在前端上传文件的时候，就进行md5校验，如果文件已经存在，就不用上传了，直接跳过上传和合并分片的步骤。只需要保存文件信息关联即可
     * @return FileChunkResultDTO
     */
    @Override
    public FileChunkResultDTO checkChunk(ChunkDTO chunk) {
        // 检查本地文件是否存在
        String fileFolderPath = getFileFolderPath(chunk.getIdentifier());
        String filePath = getFilePath(chunk);
        File file = new File(filePath);
        boolean exist = file.exists();
        // 检查redis是否存在，并且所有分片都已经上传完毕,是则跳过上传
        Set<Integer> uploaded = (Set<Integer>) redisUtils.getMap(chunk.getIdentifier()).get("uploaded");
        if (uploaded != null && uploaded.size() == chunk.getTotalChunks() && exist) {
            return new FileChunkResultDTO(true);
        }
        // 没有分片文件或者分片文件不完整，则进行断点续传，返回已经上传的分片
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            boolean mkdirs = fileFolder.mkdirs();
            log.info("准备工作,创建文件夹,fileFolderPath:{},mkdirs:{}", fileFolderPath, mkdirs);
        }
        return new FileChunkResultDTO(false, uploaded);
    }

    /**
     * 上传分片,这里可以返回存储到数据的信息或者数据id，后续可以用来关联文件信息，合并文件后更新文件上传状态等操作。
     */
    @Override
    public void uploadChunk(ChunkDTO chunk) {
        // 检查文件目录是否存在，不存在则创建
        String fileChunkFolderPath = getChunkFileFolderPath(chunk.getIdentifier());
        String filePath = getFilePath(chunk);
        File fileFolder = new File(fileChunkFolderPath);
        if (!fileFolder.exists()) {
            boolean mkdirs = fileFolder.mkdirs();
            log.info("准备工作,创建文件夹,fileFolderPath:{},mkdirs:{}", fileChunkFolderPath, mkdirs);
        }
        // 写入分片文件
        // File file = new File(fileChunkFolderPath + chunk.getFilename() + "-" + chunk.getChunkNumber().toString());
        FileInfo fileInfo = new FileInfo();
        try (
                InputStream inputStream = chunk.getFile().getInputStream();
                FileOutputStream outputStream = new FileOutputStream(fileChunkFolderPath + chunk.getFilename() + "-" + chunk.getChunkNumber());
        ) {
            IOUtils.copy(inputStream, outputStream);
            // chunk.getFile().transferTo(file);
            // 上传成功后，将分片信息存入redis
            long uploadedSize = saveRedis(chunk);
            // 将文件信息存储到数据库中
            fileInfo.setFileName(chunk.getFilename());
            fileInfo.setSuff(chunk.getFilename().substring(chunk.getFilename().lastIndexOf(".") + 1));
            fileInfo.setLocation(filePath);
            fileInfo.setMd5(chunk.getIdentifier());
            fileInfo.setStatus(2);
            save(fileInfo);
            log.info("上传分片成功,filePath:{},当前文件分片数量为:{}", filePath, uploadedSize);
        } catch (Exception e) {
            fileInfo.setStatus(0);
            save(fileInfo);
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 合并分片
     * 这里由于检查分片所说的原因，暂时不删除分片信息，后续可以根据业务需求进行删除。只修改文件上传状态即可。
     */
    @Override
    public boolean mergeFile(ChunkDTO chunk) {
        String fileChunkFolderPath = getChunkFileFolderPath(chunk.getIdentifier());
        String filePath = getFilePath(chunk);
        //首先检查文件的分片是否都已经上传完毕
        if (checkChunkExists(fileChunkFolderPath, chunk.getTotalChunks())) {
            try {
                Files.createFile(Path.of(filePath));
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            try(
                    //获取分片文件夹下的所有文件
                    Stream<Path> stream = Files.list(Path.of(fileChunkFolderPath));
                    ) {
                stream.sorted((o1, o2) -> {
                    String p1 = o1.getFileName().toString();
                    String p2 = o2.getFileName().toString();
                    int i1 = p1.lastIndexOf("-");
                    int i2 = p2.lastIndexOf("-");
                    return Integer.valueOf(p2.substring(i2)).compareTo(Integer.valueOf(p1.substring(i1)));
                }).forEach(path -> {
                    try {
                        // 以追加的形式写入文件
                        Files.write(Path.of(filePath), Files.readAllBytes(path), StandardOpenOption.APPEND);
                        // 合并后删除该块
                        // Files.delete(path);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });

            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            //更新文件的上传状态
            LambdaUpdateWrapper<FileInfo> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(FileInfo::getMd5, chunk.getIdentifier());
            updateWrapper.set(FileInfo::getStatus, 1);
            update(updateWrapper);

            // 删除所有文件块，删除删除文件夹以及文件夹下的所有文件
            // deleteFolder(new File(fileChunkFolderPath));
            // 删除redis中的文件信息
            redisUtils.getMap(chunk.getIdentifier()).clear();
            return true;
        }
        return false;
    }

    /**
     * 保存分片信息到redis
     * @param chunk 分片信息
     * @return 已经上传的分片数量
     */
    private synchronized long saveRedis(ChunkDTO chunk) {
        String identifier = chunk.getIdentifier();
        //获取当前文件在redis中是否已经上传过分片
        Set<Integer> uploaded = (Set<Integer>) redisUtils.getMap(identifier).get("uploaded");
        if (uploaded == null) {
            // 如果没有上传过分片，则创建一个set集合,并将当前分片存入
            uploaded = new HashSet<>(Arrays.asList(chunk.getChunkNumber()));
            HashMap<String, Object> map = new HashMap<>();
            map.put("uploaded", uploaded);
            map.put("chunkSize", chunk.getChunkSize());
            map.put("totalChunks", chunk.getTotalChunks());
            map.put("totalSize", chunk.getTotalSize());
            // 将文件信息存入redis
            redisUtils.getMap(identifier).putAll(map);
        } else {
            // 如果上传过分片，则将当前分片存入
            uploaded.add(chunk.getChunkNumber());
            redisUtils.getMap(identifier).put("uploaded", uploaded);
        }
        return uploaded.size();
    }

    /**
     * 检查分片是否都已经上传
     * @param fileChunkFolderPath 分片文件夹路径
     * @param totalChunks 分片总数
     * @return 是否所有分片都已经上传
     */
    private boolean checkChunkExists(String fileChunkFolderPath, Integer totalChunks) {
        try {
            File file = new File(fileChunkFolderPath);
            File[] files = file.listFiles();
            if (files != null && files.length == totalChunks) {
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 获取文件路径
     * @param chunk
     * @return
     */
    private String getFilePath(ChunkDTO chunk) {
        return uploadFolder + File.separator + chunk.getIdentifier() + File.separator + chunk.getFilename();
    }

    /**
     * 获取文件夹路径
     * @param identifier
     * @return
     */
    private String getFileFolderPath(String identifier) {
        return uploadFolder+ File.separator + identifier;
    }

    /**
     * 获取分片文件夹路径
     * @param identifier
     * @return
     */
    private String getChunkFileFolderPath(String identifier) {
        return uploadFolder + File.separator + identifier + File.separator + "chunks"+ File.separator;
    }

    /**
     * 删除文件夹以及文件夹下的所有文件
     */
    private void deleteFolder(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    deleteFolder(f);
                }
            }
            file.delete();
        }
    }

}
