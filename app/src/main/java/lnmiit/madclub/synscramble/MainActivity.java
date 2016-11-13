package lnmiit.madclub.synscramble;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button b1, b2, b3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        b1= (Button) findViewById(R.id.button1);
        b2= (Button) findViewById(R.id.button2);
        b3= (Button) findViewById(R.id.button3);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  b1.setBackgroundColor(Color.RED);
                Intent intent= new Intent(MainActivity.this, Main.class);
                intent.putExtra("var", "easy");
                startActivity(intent);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, Main.class);
                intent.putExtra("var", "medium");
                startActivity(intent);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, Main.class);
                intent.putExtra("var", "hard");
                startActivity(intent);
            }
        });

    }
}
