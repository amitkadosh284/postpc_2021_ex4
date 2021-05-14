package exercise.find.roots;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

  private BroadcastReceiver broadcastReceiverForSuccess = null;
  private BroadcastReceiver broadcastReceiverForFail = null;
  private boolean waitForResult = false;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ProgressBar progressBar = findViewById(R.id.progressBar);
    EditText editTextUserInput = findViewById(R.id.editTextInputNumber);
    Button buttonCalculateRoots = findViewById(R.id.buttonCalculateRoots);

    // set initial UI:
    progressBar.setVisibility(View.GONE); // hide progress
    editTextUserInput.setText(""); // cleanup text in edit-text
    editTextUserInput.setEnabled(true); // set edit-text as enabled (user can input text)
    buttonCalculateRoots.setEnabled(false); // set button as disabled (user can't click)

    // set listener on the input written by the keyboard to the edit-text
    editTextUserInput.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      public void afterTextChanged(Editable s) {
        // text did change
        String newText = editTextUserInput.getText().toString();
        // todo: check conditions to decide if button should be enabled/disabled (see spec below)
        if (checkValidityUserText(newText) && (!waitForResult)) {
          buttonCalculateRoots.setEnabled(true);
        }
      }
    });

    // set click-listener to the button
    buttonCalculateRoots.setOnClickListener(v -> {
      Intent intentToOpenService = new Intent(MainActivity.this, CalculateRootsService.class);
      String userInputString = editTextUserInput.getText().toString();
      long userInputLong = Long.parseLong(userInputString);
      intentToOpenService.putExtra("number_for_service", userInputLong);
      startService(intentToOpenService);
      waitForResult = true;

      // set views states for progress
      progressBar.setVisibility(View.VISIBLE);
      editTextUserInput.setEnabled(false);
      buttonCalculateRoots.setEnabled(false);
    });

    // register a broadcast-receiver to handle action "found_roots"
    broadcastReceiverForSuccess = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent incomingIntent) {
        if (incomingIntent == null || !incomingIntent.getAction().equals("found_roots")) {
          return;
        }

        //set state for a new input
        waitForResult = false;
        progressBar.setVisibility(View.GONE); // hide progress
        editTextUserInput.setText(""); // cleanup text in edit-text
        editTextUserInput.setEnabled(true); // set edit-text as enabled (user can input text)
        buttonCalculateRoots.setEnabled(false); // set button as disabled (user can't click)

        // crate intent to success activity and send all the information from calculation
        Intent intentForSuccess = new Intent(this, SuccessActivity);
        intentForSuccess.putExtra("original_number",
                incomingIntent.getLongExtra("original_number", 0));
        intentForSuccess.putExtra("root1",
                incomingIntent.getLongExtra("root1", 0));
        intentForSuccess.putExtra("root2",
                incomingIntent.getLongExtra("root2", 0));
        intentForSuccess.putExtra("time",
                incomingIntent.getLongExtra("time", 0));

        startActivity(intentForSuccess);
      }
    };

    registerReceiver(broadcastReceiverForSuccess, new IntentFilter("found_roots"));

    broadcastReceiverForFail = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent incomingIntent) {
        if (incomingIntent == null || !incomingIntent.getAction().equals("stopped_calculations")) {
          return;
        }

        long time = incomingIntent.getLongExtra("time", 0);
        String toastText = "calculation aborted after " + time + " seconds";
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
      }
    };

    registerReceiver(broadcastReceiverForFail, new IntentFilter("stopped_calculations"));

    /*
    todo:
     add a broadcast-receiver to listen for abort-calculating as defined in the spec (below)
     to show a Toast, use this code:
     `Toast.makeText(this, "text goes here", Toast.LENGTH_SHORT).show()`
     */
  }

  /**
   * function that check if the user text is positive valid long number
   * @param newText - the user input text
   * @return true- if it is valid input. false otherwise.
   */
    private boolean checkValidityUserText (String newText){
      try {
        long num = Long.parseLong(newText);
        return num >= 0;
      } catch (NumberFormatException exception) {
        return false;
      }
    }

    @Override
    protected void onDestroy () {
      super.onDestroy();

      this.unregisterReceiver(broadcastReceiverForFail);
      this.unregisterReceiver(broadcastReceiverForSuccess);
    }

    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState){
      super.onSaveInstanceState(outState);

      EditText editTextUserInput = findViewById(R.id.editTextInputNumber);

      // insert data to bundle
      outState.putBoolean("waitForResult", waitForResult);
      outState.putString("userInput", String.valueOf(editTextUserInput.getText()));
    }

    @Override
    protected void onRestoreInstanceState (@NonNull Bundle savedInstanceState){
      super.onRestoreInstanceState(savedInstanceState);

      // load the old data from the bundle
      waitForResult = savedInstanceState.getBoolean("waitForResult");
      String input = savedInstanceState.getString("userInput");

      // finding all views
      ProgressBar progressBar = findViewById(R.id.progressBar);
      EditText editTextUserInput = findViewById(R.id.editTextInputNumber);
      Button buttonCalculateRoots = findViewById(R.id.buttonCalculateRoots);

      // insert the input was before
      editTextUserInput.setText(input);

      // sets all states according if it wait for calculate or not
      if (waitForResult){
        editTextUserInput.setEnabled(false);
        buttonCalculateRoots.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
      }
      else{
        editTextUserInput.setEnabled(true);
        buttonCalculateRoots.setEnabled(true);
        progressBar.setVisibility(View.GONE);
      }
    }
}


/*

TODO:
the spec is:

upon launch, Activity starts out "clean":
* progress-bar is hidden
* "input" edit-text has no input and it is enabled
* "calculate roots" button is disabled

the button behavior is:
* when there is no valid-number as an input in the edit-text, button is disabled
* when we triggered a calculation and still didn't get any result, button is disabled
* otherwise (valid number && not calculating anything in the BG), button is enabled

the edit-text behavior is:
* when there is a calculation in the BG, edit-text is disabled (user can't input anything)
* otherwise (not calculating anything in the BG), edit-text is enabled (user can tap to open the keyboard and add input)

the progress behavior is:
* when there is a calculation in the BG, progress is showing
* otherwise (not calculating anything in the BG), progress is hidden

when "calculate roots" button is clicked:
* change states for the progress, edit-text and button as needed, so user can't interact with the screen

when calculation is complete successfully:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* open a new "success" screen showing the following data:
  - the original input number
  - 2 roots combining this number (e.g. if the input was 99 then you can show "99=9*11" or "99=3*33"
  - calculation time in seconds

when calculation is aborted as it took too much time:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* show a toast "calculation aborted after X seconds"


upon screen rotation (saveState && loadState) the new screen should show exactly the same state as the old screen. this means:
* edit-text shows the same input
* edit-text is disabled/enabled based on current "is waiting for calculation?" state
* progress is showing/hidden based on current "is waiting for calculation?" state
* button is enabled/disabled based on current "is waiting for calculation?" state && there is a valid number in the edit-text input


 */