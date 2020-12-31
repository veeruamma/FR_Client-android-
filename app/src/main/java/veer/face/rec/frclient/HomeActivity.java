package veer.face.rec.frclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    private Button recButton;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button recButton = (Button) findViewById(R.id.recButton);
        Button addButton = (Button) findViewById(R.id.addButton);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddActivity();
            }
        });
        recButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecActivity();
            }
        });
    }

    private void openRecActivity() {
        Intent intent = new Intent(this, RecogniseActivity.class);
        startActivity(intent);
    }

    private void openAddActivity() {
        Intent intent = new Intent(this, AddFaceActivity.class);
        startActivity(intent);

    }
}