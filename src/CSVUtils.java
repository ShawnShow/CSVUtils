/**
 * Created by 594829 on 2015/9/2.
 */


    import java.io.BufferedWriter;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;
    import java.io.OutputStreamWriter;
    import java.net.URLEncoder;
    import java.util.Iterator;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.zip.ZipEntry;
    import java.util.zip.ZipOutputStream;

    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;

    import net.shopxx.exception.CustomerException;

    import org.apache.commons.beanutils.BeanUtils;
    import org.apache.commons.lang3.StringUtils;

    public class CSVUtils {

        /**
         * 生成为CVS文件
         * @param exportData
         *       源数据List
         * @param map
         *       csv文件的列表头map
         * @param outPutPath
         *       文件路径
         * @param fileName
         *       文件名称
         * @return
         */
        @SuppressWarnings("rawtypes")
        public static File createCSVFile(List exportData, LinkedHashMap map, String outPutPath,
                                         String fileName) {
            File csvFile = null;
            BufferedWriter csvFileOutputStream = null;
            try {
                File file = new File(outPutPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                //定义文件名格式并创建
                csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));
                System.out.println("csvFile：" + csvFile);
                // UTF-8使正确读取分隔符","
                csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                        csvFile), "GBK"), 1024);
                System.out.println("csvFileOutputStream：" + csvFileOutputStream);
                // 写入文件头部
                for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
                    java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                    csvFileOutputStream
                            .write("\"" + (String) propertyEntry.getValue() != null ? (String) propertyEntry.getValue() : "" + "\"");
                    if (propertyIterator.hasNext()) {
                        csvFileOutputStream.write(",");
                    }
                }
                csvFileOutputStream.newLine();
                // 写入文件内容
                for (Iterator iterator = exportData.iterator(); iterator.hasNext();) {
                    Object row = (Object) iterator.next();
                    for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator
                            .hasNext();) {
                        java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator
                                .next();
                        csvFileOutputStream.write((String) BeanUtils.getProperty(row,
                                (String) propertyEntry.getKey()) == null ? "":(String) BeanUtils.getProperty(row,
                                (String)propertyEntry.getKey().toString())+"\t");
                        if (propertyIterator.hasNext()) {
                            csvFileOutputStream.write(",");
                        }
                    }
                    if (iterator.hasNext()) {
                        csvFileOutputStream.newLine();
                    }
                }
                csvFileOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    csvFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return csvFile;
        }

        /**
         * 下载文件
         * @param response
         * @param csvFilePath
         *       文件路径
         * @param fileName
         *       文件名称
         * @throws IOException
         */
        public static void exportFile(HttpServletResponse response, String csvFilePath, String fileName)
                throws IOException {
            response.setContentType("application/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            InputStream in = null;
            try {
                in = new FileInputStream(csvFilePath);
                int len = 0;
                byte[] buffer = new byte[1024];
                response.setCharacterEncoding("UTF-8");
                OutputStream out = response.getOutputStream();
                while ((len = in.read(buffer)) > 0) {
                    out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
                    out.write(buffer, 0, len);
                }
            } catch (FileNotFoundException e) {
                System.out.println(e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        /**
         * 删除该目录filePath下的所有文件
         * @param filePath
         *      文件目录路径
         */
        public static void deleteFiles(String filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        files[i].delete();
                    }
                }
            }
        }

        /**
         * 删除单个文件
         * @param filePath
         *     文件目录路径
         * @param fileName
         *     文件名称
         */
        public static void deleteFile(String filePath, String fileName) {
            File file = new File(filePath);
            if (file.exists()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        if (files[i].getName().equals(fileName)) {
                            files[i].delete();
                            return;
                        }
                    }
                }
            }
        }

        /**
         * @author 594829
         * @date 2015年6月25日
         * @param srcfile
         * @param zipfile
         */
        public static void ZipFiles(java.io.File[] srcfile, java.io.File zipfile) {

            byte[] buf = new byte[1024];
            try {
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                        zipfile));
                for (int i = 0; i < srcfile.length; i++) {
                    FileInputStream in = new FileInputStream(srcfile[i]);
                    out.putNextEntry(new ZipEntry(srcfile[i].getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public static void exportZip(HttpServletRequest request,HttpServletResponse response, File zipFilePath, String fileName)
                throws IOException {

            final String userAgent = request.getHeader("USER-AGENT");

            String finalFileName = null;
            if(StringUtils.contains(userAgent, "MSIE")){//IE浏览器
                finalFileName = URLEncoder.encode(fileName,"UTF8");
            }else if(StringUtils.contains(userAgent, "Mozilla")){//google,火狐浏览器
                finalFileName = new String(fileName.getBytes(), "ISO8859-1");
            }else{
                finalFileName = URLEncoder.encode(fileName,"UTF8");//其他浏览器
            }

            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + finalFileName
                    + ".zip");
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            try{
                OutputStream out = response.getOutputStream();
                FileInputStream inStream = new FileInputStream(zipFilePath);
                byte[] buf = new byte[4096];
                int readLength;
                while (((readLength = inStream.read(buf)) != -1)) {
                    out.write(buf, 0, readLength);
                }
                inStream.close();
            }catch (FileNotFoundException e){
                throw new CustomerException("未生成相应的文件",e);
            }

        }


    }



