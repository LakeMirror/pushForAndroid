package learn.hugy.com.pushsocket.activity;

import android.app.Activity;
import android.os.Bundle;

import learn.hugy.com.pushsocket.R;

/**
 * @author hugy
 */
public class DefaultActivity extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_default);
    }
}
