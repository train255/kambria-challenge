package covid19.helper;

import android.os.Handler;

import com.dd.processbutton.ProcessButton;

import java.util.Random;

public class ProgressGenerator {
    private int mProgress;
    public void start(final ProcessButton button) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (button.getProgress() < 90) {
                    mProgress += 1;
                    button.setProgress(mProgress);
                    handler.postDelayed(this, generateDelay());
                } else {
                    handler.removeCallbacksAndMessages(null);
                }
            }
        }, generateDelay());
    }

    private Random random = new Random();

    private int generateDelay() {
        return random.nextInt(1000);
    }
}
