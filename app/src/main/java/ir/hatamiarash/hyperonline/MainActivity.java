package ir.hatamiarash.hyperonline;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import co.ronash.pushe.Pushe;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Pushe.initialize(this, true);
    }
}
