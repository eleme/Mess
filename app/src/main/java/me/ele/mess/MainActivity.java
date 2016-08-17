package me.ele.mess;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.rv) RecyclerView rv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    CustomView customView = (CustomView) findViewById(R.id.custom_view);
    customView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(MainActivity.this, "haha", Toast.LENGTH_SHORT).show();
      }
    });

    rv.setAdapter(new MyAdapter());
  }

  @OnClick(R.id.btn) public void onClickBtn(View v) {
    startActivity(new Intent(MainActivity.this, SecondActivity.class));
  }
}
