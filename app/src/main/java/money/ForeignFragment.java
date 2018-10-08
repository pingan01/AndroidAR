package money;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.easyar.samples.helloar.R;

/**
 * Created time : 2017/6/16 13:21.

 */

public class ForeignFragment extends Fragment {
    private MediaPlayer player;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_foreign, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FlakeView flakeView=new FlakeView(getContext());
        ((ViewGroup) view.findViewById(R.id.camera_money)).addView(flakeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        player = MediaPlayer.create(getContext(), R.raw.shake);
        player.start();
    }
}
