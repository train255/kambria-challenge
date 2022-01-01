package covid19.detection;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.dd.processbutton.iml.ActionProcessButton;
import com.jlibrosa.audio.JLibrosa;

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

import covid19.helper.ProgressGenerator;
import covid19.helper.WavHelper;
import covid19.request.ApiConfig;
import covid19.request.AppConfig;
import covid19.request.ServerResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordFragment extends Fragment {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 5000;
    private static final int NUM_ROWS = 120;
    private static final int NUM_COLUMNS = 192;
    private static final int NUM_CHANNELS = 1;
    private int[] inputShape = new int[]{1, NUM_ROWS, NUM_COLUMNS, NUM_CHANNELS};
    private int[] outputShape = new int[]{1, 1};
    private static final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    private ImageButton startButton;
    private ImageButton giftBtn;
    private TextView titleText;
    private LinearLayout formUpload;
    private LinearLayout giftLayout;
    private LinearLayout reportErrorForm;
    private Button uploadButton;
    private Button retryBtn;
    private ActionProcessButton sendErrorBtn;

    //    private Button playButton;
    private String predictCovid = "";
    private String predictCough = "";
    private Thread recordingThread;
    private Thread recognitionThread;
    boolean shouldContinueRecognition = true;
    boolean shouldContinue = true;
    //    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private ResultFragment resultDialog;

    private Interpreter coughInterpreter;
    private Interpreter covidInterpreter;

    private AudioRecord recorder = null;
    private String time_count = "";

    // Working variables.
    int recordingOffset = 0;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        titleText = (TextView) view.findViewById(R.id.titleView);
        titleText.setTextColor(Color.parseColor("#212121"));

        formUpload = (LinearLayout) view.findViewById(R.id.formUpload);
        reportErrorForm = (LinearLayout) view.findViewById(R.id.reportErrorForm);
        giftLayout = (LinearLayout) view.findViewById(R.id.giftLayout);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Uploading...");
        progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        startButton = (ImageButton) view.findViewById(R.id.start);
        startButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startButton.setClickable(false);
                        startButton.setEnabled(false);
                        startButton.setImageResource(R.drawable.ic_baseline_mic_none_24);
                        startButton.setColorFilter(Color.parseColor("#a6a6a7"));
                        formUpload.setVisibility(View.GONE);
                        giftLayout.setVisibility(View.INVISIBLE);
                        reportErrorForm.setVisibility(View.GONE);
                        startRecording();
                    }
                });

        retryBtn = (Button) view.findViewById(R.id.retryRecord);
        retryBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startButton.setImageResource(R.drawable.ic_baseline_mic_none_24);
                        startButton.setColorFilter(Color.parseColor("#a6a6a7"));
                        formUpload.setVisibility(View.GONE);
                        giftLayout.setVisibility(View.INVISIBLE);
                        reportErrorForm.setVisibility(View.GONE);
                        startRecording();
                    }
                });

        uploadButton = (Button) view.findViewById(R.id.button_save);
        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialog(predictCovid);
                    }
                });

        giftBtn = (ImageButton) view.findViewById(R.id.gift);
        giftBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getPokemonImage();
                    }
                });

        final ProgressGenerator progressGenerator = new ProgressGenerator();
        sendErrorBtn = (ActionProcessButton) view.findViewById(R.id.sendErrorBtn);
        sendErrorBtn.setMode(ActionProcessButton.Mode.ENDLESS);
        sendErrorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressGenerator.start(sendErrorBtn);
                sendErrorBtn.setEnabled(false);
                try {
                    uploadFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
//            coughInterpreter = new Interpreter(loadModelFile("mobilenetV2_cough_detection.tflite"), null);
            coughInterpreter = new Interpreter(loadModelFile("cough_detection.tflite"), null);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("NOT LOAD MODEL");
        }

        return view;
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
                        sendErrorBtn.setProgress(100);
//                        Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        sendErrorBtn.setProgress(-1);
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
        recordingThread =
                new Thread(
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
//            recordingBufferLock.lock();
            try {
                byte bData[] = WavHelper.short2byte(audioBuffer);
                outStr.write(bData, 0, bufferSize);

                time_count = "";
                if (recordingOffset == 0)
                    time_count = "5";
                else if (recordingOffset == SAMPLE_RATE)
                    time_count = "4";
                else if (recordingOffset == 2 * SAMPLE_RATE)
                    time_count = "3";
                else if (recordingOffset == 3 * SAMPLE_RATE)
                    time_count = "2";
                else if (recordingOffset == 4 * SAMPLE_RATE)
                    time_count = "1";

                if (time_count != "") {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleText.setText(time_count + " giây");
                            titleText.setTextColor(Color.parseColor("#212121"));
                        }
                    });
                }

                if (recordingOffset + numberRead >= maxLength) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleText.setText("Kiểm tra tiếng ho...");
                            titleText.setTextColor(Color.parseColor("#212121"));
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
//                recordingBufferLock.unlock();
            } finally {
//                recordingBufferLock.unlock();
            }
        }

        recorder.stop();
        recorder.release();
        recorder = null;
        recordingThread = null;
        recognitionThread = null;
        recordingOffset = 0;
        startRecognition();

    }

    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
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

    private void recognize() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v(LOG_TAG, "Start recognition");

                float[] audioFeatureValues = new float[RECORDING_LENGTH];
                JLibrosa jLibrosa = new JLibrosa();

//        String audioFilePath = getContext().getCacheDir().getAbsolutePath() + "/record.wav";
//        try {
//            audioFeatureValues = jLibrosa.loadAndRead(audioFilePath, SAMPLE_RATE, -1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (WavFileException e) {
//            e.printStackTrace();
//        } catch (FileFormatNotSupportedException e) {
//            e.printStackTrace();
//        }

                for (int i = 0; i < recordingBuffer.length; ++i) {
                    audioFeatureValues[i] = (float) recordingBuffer[i];
                }

                float[][] mfccValues = jLibrosa.generateMFCCFeatures(audioFeatureValues, SAMPLE_RATE, NUM_ROWS, 4096, 512, 512);
                float[] floatValues = new float[NUM_ROWS * NUM_COLUMNS * NUM_CHANNELS];
                int count = 0;
                for (int i = 0; i < mfccValues.length; ++i) {
                    for (int j = 0; j < mfccValues[i].length; ++j) {
                        floatValues[count] = (float) mfccValues[i][j];
                        count = count + 1;
                    }
                }

                final TensorBuffer inputCoughBuffer = TensorBuffer.createFixedSize(inputShape, DataType.FLOAT32);
                inputCoughBuffer.loadArray(floatValues, inputShape);
                TensorBuffer outputCoughBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);

                coughInterpreter.run(inputCoughBuffer.getBuffer(), outputCoughBuffer.getBuffer());

                float[] cough_result = outputCoughBuffer.getFloatArray();
                Log.v(LOG_TAG, "OUTPUT tf2======> " + Arrays.toString(cough_result));


                if (cough_result[0] < 0.5) {
                    predictCough = String.valueOf(cough_result[0]);
                    titleText.setText("Không nhận dạng được tiếng ho");
                    startButton.setClickable(true);
                    startButton.setEnabled(true);
                    startButton.setImageResource(R.drawable.ic_baseline_mic_24);
                    startButton.setColorFilter(Color.parseColor("#3F51B5"));
                    reportErrorForm.setVisibility(View.VISIBLE);
                    sendErrorBtn.setProgress(0);
                    sendErrorBtn.setClickable(true);
                    sendErrorBtn.setEnabled(true);
                } else {
                    TensorBuffer outputCovidBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);

                    covidInterpreter.run(inputCoughBuffer.getBuffer(), outputCovidBuffer.getBuffer());
                    float[] covidResult = outputCovidBuffer.getFloatArray();

                    predictCovid = String.valueOf(covidResult[0]);

                    double percent_db = covidResult[0] * 100.0;
                    String final_result = df.format(percent_db) + "% bạn bị dương tính";

                    if (covidResult[0] <= 0.5) {
                        percent_db = (1 - covidResult[0]) * 100.0;
                        final_result = df.format(percent_db) + "% bạn âm tính";
                        titleText.setTextColor(Color.parseColor("#00e676"));
                        startButton.setImageResource(R.drawable.ic_baseline_verified_user_24);
                        startButton.setColorFilter(Color.parseColor("#00e676"));
                    } else {
                        titleText.setTextColor(Color.parseColor("#ff1744"));
                        startButton.setImageResource(R.drawable.ic_baseline_privacy_tip_24);
                        startButton.setColorFilter(Color.parseColor("#ff1744"));
                    }
                    titleText.setText(final_result);
//                    startButton.setClickable(true);
//                    startButton.setEnabled(true);
//                    startButton.setImageResource(R.drawable.active_btn);
                    formUpload.setVisibility(View.VISIBLE);
                    giftLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        covidInterpreter.close();
        coughInterpreter.close();
    }
}
