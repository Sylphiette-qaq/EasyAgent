package com.demo.agent.service.fileinfo;

/**
 * 知识库文件上传结果
 */
public record UploadedFileResult(Long fileInfoId, int vectorChunks) {
}
