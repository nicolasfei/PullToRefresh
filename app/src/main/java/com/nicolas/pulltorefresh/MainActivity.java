package com.nicolas.pulltorefresh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.nicolas.library.PullToRefreshListView;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private PullToRefreshListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.pullToRefreshListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"item1", "item2", "item3", "item4", "item5", "item6"});
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh: ");
                new Thread(runnable).start();
            }
        });
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                handler.sendEmptyMessage(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    refreshFinish();
                    break;
                default:
                    break;
            }
        }
    };

    private void refreshFinish() {
        Toast.makeText(this, "刷新完成", Toast.LENGTH_SHORT).show();
        listView.refreshFinish();
    }
}
