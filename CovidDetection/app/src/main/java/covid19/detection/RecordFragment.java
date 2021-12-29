package covid19.detection;

import covid19.helper.FileUtils;
import covid19.helper.WavHelper;
import covid19.request.ApiConfig;
import covid19.request.AppConfig;
import covid19.request.ServerResponse;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;

//import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import android.app.ProgressDialog;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

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
    private ImageButton startButton;
    private ImageButton testImage;
    private TextView titleText;
    private LinearLayout formUpload;
    private LinearLayout reportErrorForm;
    private Button uploadButton;
    private Button sendErrorBtn;

//    private TensorFlowInferenceInterface inferenceInterface;
//    private static final String INPUT_DATA_NAME = "conv2d_1_input";
//    private static final String OUTPUT_SCORES_NAME = "dense_1/Sigmoid";

    //    private Button playButton;
    private String predictCovid = "";
    private String predictCough = "";
    private Thread recordingThread;
    private Thread recognitionThread;
    boolean shouldContinueRecognition = true;
    boolean shouldContinue = true;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ProgressDialog progressDialog;


    private Interpreter coughInterpreter;
    private Interpreter covidInterpreter;

    private AudioRecord recorder = null;
    private String time_count = "";

    private Uri selectedImageUri;

    int SELECT_PICTURE = 200;

    // Working variables.
    int recordingOffset = 0;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

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
                        startButton.setImageResource(R.drawable.disable_btn);
                        formUpload.setVisibility(View.GONE);
                        reportErrorForm.setVisibility(View.GONE);
                        startRecording();
                    }
                });

        uploadButton = (Button) view.findViewById(R.id.button_save);
        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.show();
                        try {
                            uploadFile(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        sendErrorBtn = (Button) view.findViewById(R.id.sendErrorBtn);
        sendErrorBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.show();
                        try {
                            uploadFile(true);
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


        titleText = (TextView) view.findViewById(R.id.titleView);
        titleText.setTextColor(Color.parseColor("#212121"));

        formUpload = (LinearLayout) view.findViewById(R.id.formUpload);
        reportErrorForm = (LinearLayout) view.findViewById(R.id.reportErrorForm);

        testImage = (ImageButton) view.findViewById(R.id.testResult);
        testImage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageChooser();
                    }
                });

//        inferenceInterface = new TensorFlowInferenceInterface(getContext().getAssets(),  "cough_detection.pb");

        try {
            covidInterpreter = new Interpreter(loadModelFile("covid_detection.tflite"), null);
            coughInterpreter = new Interpreter(loadModelFile("mobilenetV2_cough_detection.tflite"), null);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("NOT LOAD MODEL");
        }

        return view;
    }

    private void imageChooser() {
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    testImage.setImageURI(selectedImageUri);
                }
            }
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


    private void uploadFile(boolean only_audio) throws Exception {
        String sourceFileUri = getContext().getCacheDir().getAbsolutePath() + "/record.wav";

        File audioFile = new File(sourceFileUri);
        RequestBody audio_body = RequestBody.create(MediaType.parse("audio/*"), audioFile);
        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);

        if (only_audio) {
            RequestBody predict_cough = RequestBody.create(MediaType.parse("text/plain"), predictCough);
            Call<ServerResponse> call = getResponse.sendError(audio_body, predict_cough);

            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        if (serverResponse.getSuccess()) {
                            Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        assert serverResponse != null;
                        Log.v("Response", serverResponse.toString());
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {

                }
            });
        } else {
            File imageFile = FileUtils.getFileFromUri(getContext(), selectedImageUri);
            RequestBody image_body = RequestBody.create(MediaType.parse("image/*"), imageFile);
            RequestBody predict_result = RequestBody.create(MediaType.parse("text/plain"), predictCovid);
            Call<ServerResponse> call = getResponse.uploadFile(audio_body, image_body, predict_result);

            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        if (serverResponse.getSuccess()) {
                            Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        assert serverResponse != null;
                        Log.v("Response", serverResponse.toString());
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {

                }
            });
        }
    }

    private MappedByteBuffer loadModelFile(String model_name) throws IOException
    {
        AssetFileDescriptor assetFileDescriptor = getContext().getAssets().openFd(model_name);

        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();

        long startOffset = assetFileDescriptor.getStartOffset();
        long len = assetFileDescriptor.getLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,len);
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

        String filepath = getContext().getCacheDir().getAbsolutePath()+"/record.wav";
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
                byte bData[] = WavHelper.short2byte(audioBuffer);
                outStr.write(bData, 0, bufferSize);

                time_count = "";
                if (recordingOffset == 0)
                    time_count = "5";
                else if (recordingOffset == SAMPLE_RATE)
                    time_count = "4";
                else if (recordingOffset == 2*SAMPLE_RATE)
                    time_count = "3";
                else if (recordingOffset == 3*SAMPLE_RATE)
                    time_count = "2";
                else if (recordingOffset == 4*SAMPLE_RATE)
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
                            titleText.setText("Đang dự đoán...");
                            titleText.setTextColor(Color.parseColor("#212121"));
                        }
                    });
                    Log.v(LOG_TAG, "Save audio success " + filepath);
                    outStr.close();
                    File wavFile = new File(filepath);
                    WavHelper.updateWavHeader(wavFile);
                    shouldContinue = false;
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

    private void recognize() {
        Log.v(LOG_TAG, "Start recognition");

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

        float[][] mfccValues = jLibrosa.generateMFCCFeatures(audioFeatureValues, SAMPLE_RATE, NUM_ROWS, 4096, 512, 512);
        final float[] floatValues = new float[NUM_ROWS * NUM_COLUMNS * NUM_CHANNELS];
        int count = 0;
        for(int i = 0; i< mfccValues.length; ++i){
            for (int j = 0; j < mfccValues[i].length; ++j) {
                floatValues[count] = (float) mfccValues[i][j];
                count = count + 1;
            }
        }

//        // Run the model.
//        String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};
//        float outputScores[] = new float[1];
//        inferenceInterface.feed(INPUT_DATA_NAME, floatValues, 1, NUM_ROWS, NUM_COLUMNS, 1);
//        inferenceInterface.run(outputScoresNames);
//        inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);
//        Log.v(LOG_TAG, "OUTPUT tf1======> " + Arrays.toString(outputScores));

        final TensorBuffer inputCoughBuffer = TensorBuffer.createFixedSize(inputShape, DataType.FLOAT32);
        inputCoughBuffer.loadArray(floatValues, inputShape);
        TensorBuffer outputCoughBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);
//
////        TensorProcessor inputProcessor =
////                new TensorProcessor.Builder().add(new NormalizeOp(0, 1)).build();
////        TensorBuffer dequantizedBuffer = inputProcessor.process(inputCoughBuffer);
//
//        // Check cough
        coughInterpreter.run(inputCoughBuffer.getBuffer(), outputCoughBuffer.getBuffer());
//
////        TensorProcessor probabilityProcessor =
////                new TensorProcessor.Builder().add(new NormalizeOp(0, 0)).build();
////        TensorBuffer dequantizedBuffer = probabilityProcessor.process(outputCoughBuffer);
////        float[] tmp_result = dequantizedBuffer.getFloatArray();
////        Log.v(LOG_TAG, Arrays.toString(tmp_result));
//
        final float[] cough_result = outputCoughBuffer.getFloatArray();
        Log.v(LOG_TAG, "OUTPUT tf2======> " + Arrays.toString(cough_result));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cough_result[0] < 0.5) {
                    predictCough = String.valueOf(cough_result[0]);
                    titleText.setText("Không nhận dạng được tiếng ho");
                    startButton.setClickable(true);
                    startButton.setEnabled(true);
                    startButton.setImageResource(R.drawable.active_btn);
                    reportErrorForm.setVisibility(View.VISIBLE);
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
                    } else {
                        titleText.setTextColor(Color.parseColor("#ff1744"));
                    }
                    titleText.setText(final_result);
                    startButton.setClickable(true);
                    startButton.setEnabled(true);
                    startButton.setImageResource(R.drawable.active_btn);
                    formUpload.setVisibility(View.VISIBLE);
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
