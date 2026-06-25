package com.example.aiagent.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileParserUtil {

    public static String parseFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".docx")) {
            return parseDocx(file);
        } else if (fileName.endsWith(".pdf")) {
            return parsePdf(file);
        } else if (fileName.endsWith(".txt")) {
            return parseTxt(file);
        } else if (fileName.endsWith(".md")) {
            return parseTxt(file);
        } else {
            throw new IOException("不支持的文件格式: " + fileName);
        }
    }

    public static String parseDocx(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text.trim()).append("\n\n");
                }
            }
        }
        return content.toString().trim();
    }

    public static String parsePdf(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            content.append(stripper.getText(document));
        }
        return content.toString().trim();
    }

    public static String parseTxt(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    public static List<File> getAllFiles(String directory) {
        List<File> files = new ArrayList<>();
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return files;
        }
        collectFiles(dir, files);
        return files;
    }

    private static void collectFiles(File dir, List<File> files) {
        File[] fileList = dir.listFiles();
        if (fileList == null) {
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                collectFiles(file, files);
            } else {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".docx") || name.endsWith(".pdf") || 
                    name.endsWith(".txt") || name.endsWith(".md")) {
                    files.add(file);
                }
            }
        }
    }
}