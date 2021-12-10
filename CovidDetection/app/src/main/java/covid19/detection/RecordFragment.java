package covid19.detection;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jlibrosa.audio.JLibrosa;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

import static android.media.AudioRecord.READ_BLOCKING;
import android.app.ProgressDialog;
import android.widget.Toast;

import covid19.detection.mfcc.MFCC;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordFragment extends Fragment {
    private static final int SAMPLE_RATE = 8000;
    private static final int SAMPLE_DURATION_MS = 5000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private ImageButton startButton;
    private TextView titleText;
    private LinearLayout formUpload;
    private Button uploadButton;
    private Button playButton;
    private RadioGroup radioGroup;
    private String testType = "";
    private Thread recordingThread;
    private Thread recognitionThread;
    boolean shouldContinueRecognition = true;
    boolean shouldContinue = true;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ProgressDialog progressDialog;

    private Interpreter interpreter;
    private AudioRecord recorder = null;
    private String time_count = "";

    // Working variables.
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    int recordingOffset = 0;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Uploading...");

        startButton = (ImageButton) view.findViewById(R.id.start);
        startButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startButton.setClickable(false);
                        startButton.setEnabled(false);
                        startButton.setImageResource(R.drawable.disable_btn);
                        formUpload.setVisibility(View.INVISIBLE);
                        startRecording();
                    }
                });

        uploadButton = (Button) view.findViewById(R.id.button_save);
        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        uploadFile();
                    }
                });

        playButton = (Button) view.findViewById(R.id.play_record_audio);
        playButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playAudio();
                    }
                });


        titleText = (TextView) view.findViewById(R.id.titleView);
        titleText.setTextColor(Color.parseColor("#212121"));

        formUpload = (LinearLayout) view.findViewById(R.id.formUpload);

        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioButton_pcr:
                        testType = "pcr";
                        break;
                    case R.id.radioButton_qtest:
                        testType = "qtest";
                        break;
                }
            }
        });

        try {
            interpreter = new Interpreter(loadModelFile(),null);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("NOT LOAD MODEL");
        }

        return view;
    }

    private void playAudio() {
        String sourceFileUri = getContext().getCacheDir().getAbsolutePath() + "/record.pcm";
        //Reading the file..
        byte[] byteData = null;
        File file = null;
        file = new File(sourceFileUri); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        byteData = new byte[(int) file.length()];
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read( byteData );
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int intSize = android.media.AudioTrack.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);
        if (at!=null) {
            at.play();
            at.write(byteData, 0, byteData.length);
            at.stop();
            at.release();
        }
        else
            Log.d("TCAudio", "audio track is not initialised ");
    }

    private void uploadFile() {
        String sourceFileUri = getContext().getCacheDir().getAbsolutePath() + "/record.pcm";
        progressDialog.show();

        // Map is used to multipart the file using okhttp3.RequestBody
        File file = new File(sourceFileUri);

        // Parsing any Media type file
        RequestBody fbody = RequestBody.create(MediaType.parse("audio/*"), file);
        RequestBody test_type = RequestBody.create(MediaType.parse("text/plain"), testType);

        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);
        Call<ServerResponse> call = getResponse.uploadFile(fbody, test_type);
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

    private MappedByteBuffer loadModelFile() throws IOException
    {
        AssetFileDescriptor assetFileDescriptor = getContext().getAssets().openFd("mobilenetv2.tflite");

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

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];

        recorder = new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }

        recorder.startRecording();
        Log.v(LOG_TAG, "Start recording");

        String filepath = getContext().getCacheDir().getAbsolutePath();
        Log.v(LOG_TAG, filepath);
        FileOutputStream outStr = null;
        try {
            outStr = new FileOutputStream(filepath+"/record.pcm");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (shouldContinue) {
            int numberRead = recorder.read(audioBuffer, 0, audioBuffer.length, READ_BLOCKING);
            int maxLength = recordingBuffer.length;
            recordingBufferLock.lock();
            try {
//                byte[] audioBytes = new byte[Float.BYTES * audioBuffer.length];
////                ByteBuffer.wrap(audioBytes).asFloatBuffer().put(audioBuffer);
//                outStr.write(audioBytes, 0, bufferSize);



                byte bData[] = short2byte(audioBuffer);
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

                if (recordingOffset + numberRead < maxLength) {
                    System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberRead);
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleText.setText("Đang dự đoán...");
                            titleText.setTextColor(Color.parseColor("#212121"));
                        }
                    });
                    shouldContinue = false;
                    Log.v(LOG_TAG, "Save audio success " + filepath);
                    outStr.close();
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

        short[] inputBuffer = new short[RECORDING_LENGTH];
        double[] doubleInputBuffer = new double[RECORDING_LENGTH];
        float[] floatInputBuffer = new float[RECORDING_LENGTH];
        JLibrosa jLibrosa = new JLibrosa();

        recordingBufferLock.lock();
        try {
            int maxLength = recordingBuffer.length;
            System.arraycopy(recordingBuffer, 0, inputBuffer, 0, maxLength);
        } finally {
            recordingBufferLock.unlock();
        }

//        // We need to feed in float values between -1.0 and 1.0, so divide the
//        // signed 16-bit inputs.
//        for (int i = 0; i < RECORDING_LENGTH; ++i) {
//            doubleInputBuffer[i] = inputBuffer[i] / 32767.0;
//        }
//        //MFCC java library.
//        MFCC mfccConvert = new MFCC();
//        float[] floatValues = mfccConvert.process(doubleInputBuffer, 96, 96);

        for (int i = 0; i < RECORDING_LENGTH; ++i) {
            floatInputBuffer[i] = (float)inputBuffer[i];
        }
        float[][] mfccValues = jLibrosa.generateMFCCFeatures(floatInputBuffer, SAMPLE_RATE, 96, 4096, 512, 512);
        float[] floatValues = new float[96 * 96 * 1];
        int count = 0;
        for(int i = 0; i< mfccValues.length; ++i){
            for (int j = 0; j < mfccValues[i].length; ++j) {
                floatValues[count] = (float) mfccValues[i][j];
                count = count + 1;
            }
        }

        System.out.println(floatValues);


        int[] inputShape = new int[]{96, 96, 1};
        TensorBuffer tensorBuffer = TensorBuffer.createFixedSize(inputShape, DataType.FLOAT32);
        tensorBuffer.loadArray(floatValues);
        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1}, DataType.FLOAT32);

        interpreter.run(tensorBuffer.getBuffer(), output.getBuffer());
        final float[] result = output.getFloatArray();

        System.out.println(result[0]);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double percent_db = result[0] * 100.0;
                String final_result = df.format(percent_db) + "% bạn bị dương tính";
                if (result[0] <= 0.5) {
                    percent_db = (1 - result[0]) * 100.0;
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
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        interpreter.close();
    }

}
