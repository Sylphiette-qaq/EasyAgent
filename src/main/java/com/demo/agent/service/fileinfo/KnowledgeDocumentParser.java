package com.demo.agent.service.fileinfo;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

/**
 * 按扩展名选择文档解析器：txt/md 用纯文本，docx/pdf 用 Apache Tika。
 */
@Component
public class KnowledgeDocumentParser {

    private static final Set<String> TIKA_EXTENSIONS = Set.of("docx", "pdf");

    private final DocumentParser textParser = new TextDocumentParser();
    private final DocumentParser tikaParser = new ApacheTikaDocumentParser();

    public Document parse(File file) throws IOException {
        String ext = extension(file.getName());
        DocumentParser parser = TIKA_EXTENSIONS.contains(ext) ? tikaParser : textParser;
        try (InputStream in = new FileInputStream(file)) {
            return parser.parse(in);
        }
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
