package exercise.find.roots;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Intent intentCreatedMe = getIntent();

        TextView timeCalculating = findViewById(R.id.timeDeclaratiom);
        TextView calculation = findViewById(R.id.calculation);
        TextView time = findViewById(R.id.timeItTook);

        timeCalculating.setVisibility(View.VISIBLE);
        calculation.setVisibility(View.GONE);
        time.setVisibility(View.GONE);

        String originalNum = String.valueOf(intentCreatedMe.getLongExtra("original_number",
                0));
        String root1 = String.valueOf(intentCreatedMe.getLongExtra("root1", 0));
        String root2 = String.valueOf(intentCreatedMe.getLongExtra("root2", 0));
        String timeCalculate = String.valueOf(intentCreatedMe.getLongExtra("time",
                0));
        String strToShow = originalNum + " = " + root1 + " * " + root2;

        calculation.setText(strToShow);
        time.setText(timeCalculate);

        calculation.setVisibility(View.VISIBLE);
        time.setVisibility(View.VISIBLE);
    }
}
