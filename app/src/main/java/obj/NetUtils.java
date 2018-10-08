package obj;

/**
 * Created by HPA on 2017/11/8.
 */


import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.List;


/**
 * 网络判断工具类
 */
public class NetUtils {
    /**
     * 判断当前网络是否是wifi---用户体验---判断进行下载或者是在线播放
     *
     * @param context
     * @return
     */
    private static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activityInfo = connectivityManager.getActiveNetworkInfo();
        if (activityInfo != null && activityInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 判断网络连接是否可用
     *
     * @param context
     * @return
     */
    private static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Toast.makeText(context, "没有网络", Toast.LENGTH_SHORT).show();
        } else {
            //仅仅用来判断网络连接
            //可以使用connectivityManager.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
            for (int i = 0; i < infos.length; i++) {
                if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断GPS是否打开
     *
     * @param context
     * @return
     */
    private static boolean isGPSEnabled(Context context) {
        LocationManager localManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        List<String> providers = localManager.getProviders(true);
        return providers != null && providers.size() > 0;
    }

    /**
     * 判断wifi是否打开
     *
     * @param context
     * @return
     */
    private static boolean isWifiEnabled(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return ((manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || telManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    /**
     * 判断是否是3g网络
     *
     * @param context
     * @return
     */
    private static boolean is3rd(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
        if (netWorkInfo != null && netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }
}
