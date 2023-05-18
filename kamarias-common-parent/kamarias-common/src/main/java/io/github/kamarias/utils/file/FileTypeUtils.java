package io.github.kamarias.utils.file;

import com.wyx.common.utils.string.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件类型工具类
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/30 11:53
 */
public class FileTypeUtils {

    private static final List<String> fileTypeList = new ArrayList<>();

    static {
        fileTypeList.add("jpeg");
        fileTypeList.add("jpg");
        fileTypeList.add("png");
        fileTypeList.add("gif");
        fileTypeList.add("doc");
        fileTypeList.add("docx");
        fileTypeList.add("pdf");
        fileTypeList.add("pptx");
        fileTypeList.add("ppt");
        fileTypeList.add("xlsx");
        fileTypeList.add("xls");
        fileTypeList.add("mp4");
        fileTypeList.add("txt");
        fileTypeList.add("img");
        fileTypeList.add("mp3");
    }


    /**
     * 校验文件后缀名是否合法
     * @param fileName 文件名
     * @return  返回结果
     */
    public static boolean checkFileSuffix(String fileName){
        return fileTypeList.contains(getFileType(fileName));
    }

    /**
     * 获取文件类型
     * @param file 文件
     * @return 后缀（不含".")
     */
    public static String getFileType(File file) {
        if (null == file) {
            return StringUtils.EMPTY;
        }
        return getFileType(file.getName());
    }

    /**
     * 获取文件类型
     * @param fileName 文件名
     * @return 后缀（不含".")
     */
    public static String getFileType(String fileName) {
        int separatorIndex = fileName.lastIndexOf(".");
        if (separatorIndex < 0) {
            return "";
        }
        return fileName.substring(separatorIndex + 1).toLowerCase();
    }

    /**
     * 获取文件类型
     * @param file 文件
     * @return 后缀（包含".")
     */
    public static String getFileTypeIncludeDot(File file) {
        if (null == file) {
            return StringUtils.EMPTY;
        }
        return getFileType(file.getName());
    }

    /**
     * 获取文件类型
     * @param fileName 文件名
     * @return 后缀（包含".")
     */
    public static String getFileTypeIncludeDot(String fileName) {
        int separatorIndex = fileName.lastIndexOf(".");
        if (separatorIndex < 0) {
            return "";
        }
        return fileName.substring(separatorIndex).toLowerCase();
    }

}
