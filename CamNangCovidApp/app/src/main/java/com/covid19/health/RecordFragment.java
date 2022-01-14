package com.covid19.health;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.covid19.health.helper.PulseDrawable;
import com.covid19.health.helper.WavHelper;
import com.covid19.health.request.ApiConfig;
import com.covid19.health.request.AppConfig;
import com.covid19.health.request.ServerResponse;
import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import info.abdolahi.CircularMusicProgressBar;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import com.dd.processbutton.iml.ActionProcessButton;


public class RecordFragment<handlerAnimation> extends Fragment {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 5000;
    private static final int NUM_ROWS = 120;
    private static final int NUM_COLUMNS = 192;
    private static final int NUM_CHANNELS = 1;
    private static final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDING_LENGTH = SAMPLE_RATE * SAMPLE_DURATION_MS / 1000;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private final int[] inputShape = new int[]{1, NUM_ROWS, NUM_COLUMNS, NUM_CHANNELS};
    private final int[] outputShape = new int[]{1, 1};
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    boolean shouldContinueRecognition = true;
    boolean shouldContinue = true;
    CircularMusicProgressBar micProgressBar;
    // Working variables.
    int recordingOffset = 0;
    private String time_count = "";
    //    private int time_count = 5;
    private int process_value = 0;
    private ImageButton startButton;
    private ImageButton giftBtn;
    //    private ActionProcessButton sendErrorBtn;
    private TextView titleText;
    private LinearLayout formUpload;
    private LinearLayout giftLayout;
    private LinearLayout reportErrorForm;
    private Button uploadButton;
    private Button retryBtn;
    //    private Button playButton;
    private ImageView resultImg;
    private String predictCovid = "";
    private String predictCough = "";
    private Thread recordingThread;
    private Thread recognitionThread;
    private ProgressDialog progressDialog;
    private ResultFragment resultDialog;
    private Interpreter coughInterpreter;
    private Interpreter covidInterpreter;
    private AudioRecord recorder = null;
    private RecognizeTask recognize_task;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        titleText = view.findViewById(R.id.titleView);

        formUpload = view.findViewById(R.id.formUpload);
        reportErrorForm = view.findViewById(R.id.reportErrorForm);
        giftLayout = view.findViewById(R.id.giftLayout);
        resultImg = view.findViewById(R.id.resultImg);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Uploading...");
        progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        startButton = view.findViewById(R.id.start);
        startButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickStartRecordBtn();
                    }
                });

        startButton.setBackground(new PulseDrawable(Color.GREEN));

        retryBtn = view.findViewById(R.id.retryRecord);
        retryBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetVoiceBtn();
                        titleText.setText("Ghi âm tiếng ho để chẩn đoán");
                        titleText.setTextColor(getResources().getColor(R.color.miniTitleColor));
                        startButton.setVisibility(View.VISIBLE);
                        micProgressBar.setVisibility(View.GONE);
                        resultImg.setVisibility(View.GONE);
                        formUpload.setVisibility(View.GONE);
                        giftLayout.setVisibility(View.INVISIBLE);
                        reportErrorForm.setVisibility(View.GONE);
                    }
                });

        uploadButton = view.findViewById(R.id.button_save);
        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialog(predictCovid);
                    }
                });

        giftBtn = view.findViewById(R.id.gift);
        giftBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getPokemonImage();
                    }
                });
        giftBtn.setBackground(new PulseDrawable(Color.RED));

        micProgressBar = view.findViewById(R.id.voice_processbar);

//        final ProgressGenerator progressGenerator = new ProgressGenerator();
//        sendErrorBtn = (ActionProcessButton) view.findViewById(R.id.sendErrorBtn);
//        sendErrorBtn.setMode(ActionProcessButton.Mode.ENDLESS);
//        sendErrorBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                progressGenerator.start(sendErrorBtn);
//                sendErrorBtn.setEnabled(false);
//                try {
//                    uploadFile();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

//        playButton = (Button) view.findViewById(R.id.play_record_audio);
//        playButton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        playAudio();
//                    }
//                });


        try {
            covidInterpreter = new Interpreter(loadModelFile("covid_detection.tflite"), null);
            coughInterpreter = new Interpreter(loadModelFile("mobilenetV2_cough_detection.tflite"), null);
//            coughInterpreter = new Interpreter(loadModelFile("cough_detection.tflite"), null);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("NOT LOAD MODEL");
        }

        return view;
    }

    private void resetVoiceBtn() {
        startButton.setClickable(true);
        startButton.setEnabled(true);
        startButton.setImageResource(R.drawable.ic_baseline_mic_24);
        startButton.setColorFilter(getResources().getColor(R.color.gradientLightGreen));
    }

    private void clickStartRecordBtn() {
        micProgressBar.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.GONE);
        resultImg.setVisibility(View.GONE);
        formUpload.setVisibility(View.GONE);
        giftLayout.setVisibility(View.INVISIBLE);
        reportErrorForm.setVisibility(View.GONE);
        startRecording();
    }

    private void getPokemonImage() {
        String[] list;
        try {
            list = getContext().getAssets().list("images");
            ArrayList<String> listImages = new ArrayList<String>(Arrays.asList(list));
            final int min = 0;
            final int max = list.length - 1;
            final int random = new Random().nextInt((max - min) + 1) + min;

            Dialog builder = new Dialog(getContext());
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            builder.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    //nothing;
                }
            });

            ImageView imageView = new ImageView(getContext());
            InputStream inputstream = null;
            try {
                inputstream = getContext().getAssets().open("images/"
                        + listImages.get(random));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Drawable drawable = Drawable.createFromStream(inputstream, null);
            imageView.setImageDrawable(drawable);
            builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            builder.show();
            giftLayout.setVisibility(View.INVISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void playAudio() {
//        String sourceFileUri = getContext().getCacheDir().getAbsolutePath() + "/record.wav";
//        //Reading the file..
//        byte[] byteData = null;
//        File file = null;
//        file = new File(sourceFileUri); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
//        byteData = new byte[(int) file.length()];
//        FileInputStream in = null;
//        try {
//            in = new FileInputStream( file );
//            in.read( byteData );
//            in.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int intSize = android.media.AudioTrack.getMinBufferSize(SAMPLE_RATE,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);
//        if (at!=null) {
//            at.play();
//            at.write(byteData, 0, byteData.length);
//            at.stop();
//            at.release();
//        }
//        else
//            Log.d("TCAudio", "audio track is not initialised ");
//    }


    private void uploadFile() throws Exception {
        String sourceFileUri = getContext().getCacheDir().getAbsolutePath() + "/record.wav";

        File audioFile = new File(sourceFileUri);
        RequestBody audio_body = RequestBody.create(MediaType.parse("audio/*"), audioFile);
        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);

        RequestBody predict_cough = RequestBody.create(MediaType.parse("text/plain"), predictCough);
        Call<ServerResponse> call = getResponse.sendError(audio_body, predict_cough);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
//                        sendErrorBtn.setProgress(100);
//                        Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
//                        sendErrorBtn.setProgress(-1);
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    private MappedByteBuffer loadModelFile(String model_name) throws IOException {
        AssetFileDescriptor assetFileDescriptor = getContext().getAssets().openFd(model_name);

        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();

        long startOffset = assetFileDescriptor.getStartOffset();
        long len = assetFileDescriptor.getLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, len);
    }

    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        ((MainActivity) getActivity()).enableNavigation(false);
        recordingThread = new Thread(
                new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
    }


    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        // Estimate the buffer size we'll need for this device.
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                CHANNEL_MASK,
                ENCODING);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_MASK,
                ENCODING,
                bufferSize);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }

        recorder.startRecording();
        Log.v(LOG_TAG, "Start recording");

        String filepath = getContext().getCacheDir().getAbsolutePath() + "/record.wav";
        Log.v(LOG_TAG, filepath);
        FileOutputStream outStr = null;
        try {
            outStr = new FileOutputStream(filepath);
            WavHelper.writeWavHeader(outStr, CHANNEL_MASK, SAMPLE_RATE, ENCODING);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (shouldContinue) {
            int numberRead = recorder.read(audioBuffer, 0, audioBuffer.length);
            int maxLength = RECORDING_LENGTH;
            recordingBufferLock.lock();
            try {
                byte[] bData = WavHelper.short2byte(audioBuffer);
                outStr.write(bData, 0, bufferSize);

                time_count = "";
                if (recordingOffset == 0) {
                    time_count = "5";
                    process_value = 0;
                } else if (recordingOffset == SAMPLE_RATE) {
                    time_count = "4";
                    process_value = 20;
                } else if (recordingOffset == 2 * SAMPLE_RATE) {
                    time_count = "3";
                    process_value = 40;
                } else if (recordingOffset == 3 * SAMPLE_RATE) {
                    time_count = "2";
                    process_value = 60;
                } else if (recordingOffset == 4 * SAMPLE_RATE) {
                    time_count = "1";
                    process_value = 80;
                }

                if (time_count != "") {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleText.setText(time_count + " giây");
                            micProgressBar.setValue(process_value);
                        }
                    });
                }

                if (recordingOffset + numberRead >= maxLength) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            micProgressBar.setValue(100);
                            titleText.setText("Kiểm tra tiếng ho...");
                        }
                    });
                    Log.v(LOG_TAG, "Save audio success " + filepath);
                    outStr.close();
                    File wavFile = new File(filepath);
                    WavHelper.updateWavHeader(wavFile);
                    shouldContinue = false;
                } else {
                    System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberRead);
                }
                recordingOffset += numberRead;
            } catch (IOException e) {
                e.printStackTrace();
                recordingBufferLock.unlock();
            } finally {
                recordingBufferLock.unlock();
            }
        }

        recorder.stop();
        recorder.release();
        recorder = null;
        recordingThread = null;
        recognitionThread = null;
        recordingOffset = 0;
        Log.v(LOG_TAG, "recorder stop and release");
        startRecognition();

    }

    public synchronized void startRecognition() {
        recognize_task = new RecognizeTask();
        recognize_task.execute();
    }

    private void showDialog(String result) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Supply num input as an argument.
        resultDialog = ResultFragment.newInstance(result);
        resultDialog.show(ft, "dialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        covidInterpreter.close();
        coughInterpreter.close();
    }

    class RecognizeTask extends AsyncTask<Integer, Integer, Float> {
        protected Float doInBackground(Integer... arg0) {
            float[] audioFeatureValues = new float[RECORDING_LENGTH];
            JLibrosa jLibrosa = new JLibrosa();

            String audioFilePath = getContext().getCacheDir().getAbsolutePath() + "/record.wav";
            try {
                audioFeatureValues = jLibrosa.loadAndRead(audioFilePath, SAMPLE_RATE, -1);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WavFileException e) {
                e.printStackTrace();
            } catch (FileFormatNotSupportedException e) {
                e.printStackTrace();
            }


//            for (int i = 0; i < recordingBuffer.length; ++i) {
//                audioFeatureValues[i] = (float)recordingBuffer[i];
//                Log.v(LOG_TAG, "features " + String.valueOf(audioFeatureValues[i]));
//            }

            float[][] mfccValues = jLibrosa.generateMFCCFeatures(audioFeatureValues, SAMPLE_RATE, NUM_ROWS, 4096, 512, 512);
            float[] floatValues = new float[NUM_ROWS * NUM_COLUMNS * NUM_CHANNELS];
            int count = 0;
            for (int i = 0; i < mfccValues.length; ++i) {
                for (int j = 0; j < mfccValues[i].length; ++j) {
                    floatValues[count] = mfccValues[i][j];
                    count = count + 1;
                }
            }

            final TensorBuffer inputCoughBuffer = TensorBuffer.createFixedSize(inputShape, DataType.FLOAT32);
            inputCoughBuffer.loadArray(floatValues, inputShape);
            TensorBuffer outputCoughBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);

            coughInterpreter.run(inputCoughBuffer.getBuffer(), outputCoughBuffer.getBuffer());

            float[] cough_result = outputCoughBuffer.getFloatArray();
            Log.v(LOG_TAG, "OUTPUT COUGH ======> " + Arrays.toString(cough_result));


            if (cough_result[0] < 0.5) {
                predictCough = String.valueOf(cough_result[0]);
                return null;
            } else {
                TensorBuffer outputCovidBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);

                covidInterpreter.run(inputCoughBuffer.getBuffer(), outputCovidBuffer.getBuffer());
                float[] covidResult = outputCovidBuffer.getFloatArray();
                Log.v(LOG_TAG, "OUTPUT COVID ======> " + Arrays.toString(covidResult));
                return covidResult[0];
            }

        }

        protected void onProgressUpdate(Integer... arg1) {
            //called when background task calls publishProgress
            //in doInBackground
        }

        protected void onPostExecute(final Float result) {
            //result comes from return value of doInBackground
            //runs on UI thread, not called if task cancelled
            if (result == null) {
                titleText.setText("Không nhận dạng được tiếng ho");
                resetVoiceBtn();
                reportErrorForm.setVisibility(View.VISIBLE);
//                    sendErrorBtn.setProgress(0);
//                    sendErrorBtn.setClickable(true);
//                    sendErrorBtn.setEnabled(true);
            } else {
                predictCovid = String.valueOf(result);

                double percent_db = result * 100.0;
                String final_result = df.format(percent_db) + "% bạn bị dương tính";

                if (result <= 0.5) {
                    percent_db = (1 - result) * 100.0;
                    final_result = df.format(percent_db) + "% bạn âm tính";
                    titleText.setTextColor(getResources().getColor(R.color.gradientLightGreen));
                    resultImg.setImageResource(R.drawable.ic_baseline_verified_user_24);
                    resultImg.setColorFilter(getResources().getColor(R.color.gradientLightGreen));
                } else {
                    titleText.setTextColor(getResources().getColor(R.color.colorAccent));
                    resultImg.setImageResource(R.drawable.ic_baseline_privacy_tip_24);
                    resultImg.setColorFilter(getResources().getColor(R.color.colorAccent));
                }
                micProgressBar.setVisibility(View.GONE);
                resultImg.setVisibility(View.VISIBLE);
                titleText.setText(final_result);
//                    startButton.setClickable(true);
//                    startButton.setEnabled(true);
//                    startButton.setImageResource(R.drawable.active_btn);
                formUpload.setVisibility(View.VISIBLE);
                giftLayout.setVisibility(View.VISIBLE);
            }
            ((MainActivity) getActivity()).enableNavigation(true);
        }

        protected void onCancelled() {
            //run on UI thread if task is cancelled
            ((MainActivity) getActivity()).enableNavigation(true);
        }
    }


}
