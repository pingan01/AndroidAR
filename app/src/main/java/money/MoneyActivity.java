package money;

import android.media.MediaPlayer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import cn.easyar.samples.helloar.Constants;
import cn.easyar.samples.helloar.HelloAR;
import cn.easyar.samples.helloar.R;

public class MoneyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);
        Constants.flag=0;

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        CameraFragment fragment = new CameraFragment();
        fragmentTransaction.add(R.id.money, fragment);
        ForeignFragment fragment1 = new ForeignFragment();
        fragmentTransaction.add(R.id.money, fragment1);
        fragmentTransaction.show(fragment);
        fragmentTransaction.show(fragment1);

        fragmentTransaction.commit();
        /**
         *  FlakeView flakeView=new FlakeView(this);

         ((ViewGroup) findViewById(R.id.money)).addView(flakeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HelloAR.handler.sendEmptyMessageDelayed(1,1000);
    }
}
