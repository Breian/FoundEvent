package di.unipi.it.foundevent;


import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Objects;


/**
 * Activity with buttons "registration" and "login". Is the first activity that the user see.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    FloatingActionButton buttonReg;
    FloatingActionButton buttonLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.scale_left_to_right, R.anim.scale_right_to_left);
        buttonReg = findViewById(R.id.registration);
        buttonReg.setOnClickListener(this);
        buttonLog = findViewById(R.id.login);
        buttonLog.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView welcome = findViewById(R.id.welcome_textView);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        welcome.startAnimation(animation);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.about){
            overridePendingTransition(R.anim.scale_right_to_left, R.anim.scale_left_to_right);
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registration:
                overridePendingTransition(R.anim.scale_right_to_left, R.anim.scale_left_to_right);
                Intent intentReg = new Intent(this, RegActivity.class);
                startActivity(intentReg);
                break;
            case R.id.login:
                Intent intentLog = new Intent(this, LogActivity.class);
                startActivity(intentLog);
                break;
        }
    }


}




