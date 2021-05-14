package exercise.find.roots;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class CalculateRootsService extends IntentService {

  private final long MAX_TIME = 20000;

  public CalculateRootsService() {
    super("CalculateRootsService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent == null) return;
    long timeStartMs = System.currentTimeMillis();
    long numberToCalculateRootsFor = intent.getLongExtra("number_for_service", 0);
    if (numberToCalculateRootsFor <= 0) {
      Log.e("CalculateRootsService", "can't calculate roots for non-positive input" + numberToCalculateRootsFor);
      return;
    }

    long root1, root2;
    root1 = calculateRoots(numberToCalculateRootsFor,timeStartMs);
    long timeTook = System.currentTimeMillis() - timeStartMs;
    root2 = numberToCalculateRootsFor / root1;
    Intent broadcastIntent = new Intent("found_roots");
    broadcastIntent.putExtra("original_number", numberToCalculateRootsFor);
    broadcastIntent.putExtra("root1", root1);
    broadcastIntent.putExtra("root2", root2);
    broadcastIntent.putExtra("time", timeTook);
    sendBroadcast(broadcastIntent);
  }

  long calculateRoots(long numberToCalculateRootsFor ,long timeStartMs){
    boolean is_prime = true;
    long i = 2;
    while (i < (numberToCalculateRootsFor/2)){
      if ((System.currentTimeMillis() - timeStartMs) > MAX_TIME){
        Intent broadcastIntent = new Intent("stopped_calculations");
        broadcastIntent.putExtra("original_number", numberToCalculateRootsFor);
        broadcastIntent.putExtra("time_until_give_up_seconds", MAX_TIME);
        sendBroadcast(broadcastIntent);
      }
      if ((numberToCalculateRootsFor % i) == 0){
        is_prime = false;
        break;
      }
      i++;
    }
    if (is_prime){
      return 1;
    }
    return i;
  }
}