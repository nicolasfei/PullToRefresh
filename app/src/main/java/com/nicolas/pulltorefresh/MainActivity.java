package com.nicolas.pulltorefresh;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

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
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshFinish();
            }
        });
    }

    private void refreshFinish() {
        listView.refreshFinish();
    }
}
