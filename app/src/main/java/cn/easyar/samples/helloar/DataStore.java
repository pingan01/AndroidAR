package cn.easyar.samples.helloar;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 数据操作类
 */

public class DataStore {

    private Context mContext;

    public DataStore(Context context) {
        mContext = context;
    }

    /**
     * 将 json 保存到外部 storage 的指定目录下
     * @param fileName: json 文件的名字
     */
    public void saveJson(String json, String fileName) {
        String filePath;
        /**
         * 如果外部存储可用
         */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e("TAG", "外部存储可用！");
            /**
             * 创建文件夹
             */
            String dirPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "arimg";
            File storageDir = new File(dirPath);
            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                    return;
                }
            }
            /**
             * 将 json 文件保存
             */
            filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "arimg"
                    + File.separator + fileName;
            Log.e("TAG", "json 文件保存路径：" + filePath);
            try {
                File file = new File(filePath);
                file.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(json.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
