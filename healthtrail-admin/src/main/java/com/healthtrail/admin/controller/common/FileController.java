package com.healthtrail.admin.controller.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.healthtrail.common.constant.Constants.UploadSubDir;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.exception.error.ErrorCode.Business;
import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.common.dto.UploadDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用请求处理
 * TODO 需要重构
 * @author valarchie
 */
@Tag(name = "上传API", description = "上传相关接口")
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {


    /**
     * 通用下载请求
     * download接口  其实不是很有必要
     * @param fileName 文件名称
     */
    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public ResponseEntity<byte[]> fileDownload(String fileName, HttpServletResponse response) {
        try {
            if (!FileUploadUtils.isAllowDownload(fileName)) {
                // 返回类型是ResponseEntity 不能捕获异常， 需要手动将错误填到 ResponseEntity
                ResponseDTO<Object> fail = ResponseDTO.fail(
                    new ApiException(Business.COMMON_FILE_NOT_ALLOWED_TO_DOWNLOAD, fileName));
                return new ResponseEntity<>(JacksonUtil.to(fail).getBytes(), null, HttpStatus.OK);
            }

            HttpHeaders downloadHeader = FileUploadUtils.getDownloadHeader(fileName);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            return new ResponseEntity<>(FileUploadUtils.readBytes(fileName), downloadHeader, HttpStatus.OK);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return null;
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @Operation(summary = "单个上传文件")
    @PostMapping("/upload")
    public ResponseDTO<UploadDTO> uploadFile(MultipartFile file) {
        if (file == null) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_IS_EMPTY);
        }

        // 上传并返回新文件名称
        String fileName = FileUploadUtils.upload(UploadSubDir.UPLOAD_PATH, file);

        UploadDTO uploadDTO = UploadDTO.builder()
            // 统一返回最终可访问地址。
            // 本地存储会补全当前服务域名，MinIO 会返回对象公开访问地址。
            .url(FileUploadUtils.getAccessUrl(fileName))
            // fileName 字段继续保留“存储标识”语义：
            // 1. 本地是 /profile/... 相对路径
            // 2. MinIO 是 object key
            // 这样后续如果接口还要回传给下载、解析、迁移任务复用，会更稳定。
            .fileName(fileName)
            // 新生成的文件名
            .newFileName(FileNameUtil.getName(fileName))
            // 原始的文件名
            .originalFilename(file.getOriginalFilename()).build();

        return ResponseDTO.ok(uploadDTO);
    }

    /**
     * 通用上传请求（多个）
     */
    @Operation(summary = "多个上传文件")
    @PostMapping("/uploads")
    public ResponseDTO<List<UploadDTO>> uploadFiles(List<MultipartFile> files) {
        if (CollUtil.isEmpty(files)) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_IS_EMPTY);
        }

        List<UploadDTO> uploads = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file != null) {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(UploadSubDir.UPLOAD_PATH, file);
                UploadDTO uploadDTO = UploadDTO.builder()
                    .url(FileUploadUtils.getAccessUrl(fileName))
                    .fileName(fileName)
                    .newFileName(FileNameUtil.getName(fileName))
                    .originalFilename(file.getOriginalFilename()).build();

                uploads.add(uploadDTO);

            }
        }
        return ResponseDTO.ok(uploads);
    }

}
