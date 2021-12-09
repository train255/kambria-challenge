package covid19.detection;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
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

public class RecordFragment extends Fragment {
    private static final int SAMPLE_RATE = 8000;
    private static final int SAMPLE_DURATION_MS = 5000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private ImageButton startButton;
    private TextView titleText;
    private LinearLayout formUpload;
    private Button uploadButton;
    private RadioGroup radioGroup;
    private String testType = "";
    private Thread recordingThread;
    private Thread recognitionThread;
    boolean shouldContinueRecognition = true;
    boolean shouldContinue = true;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Interpreter interpreter;
    private AudioRecord recorder = null;
    private String time_count = "";

    // Working variables.
    float[] recordingBuffer = new float[RECORDING_LENGTH];
    int recordingOffset = 0;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        startButton = (ImageButton) view.findViewById(R.id.start);
        startButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startButton.setClickable(false);
                        startButton.setEnabled(false);
                        startButton.setImageResource(R.drawable.disable_btn);
                        startRecording();
                    }
                });

        uploadButton = (Button) view.findViewById(R.id.button_save);
        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        uploadFile(view);
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

    private String uploadFile(View view) {
        int serverResponseCode = 0;
        try {
            String sourceFileUri = getContext().getCacheDir().getAbsolutePath() + "/record.pcm";;

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(sourceFileUri);

            if (sourceFile.isFile()) {

                try {
                    String upLoadServerUri = "https://c9b6-2402-800-619d-7dc5-00-3.ngrok.io/uploadAudio";

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE",
                            "multipart/form-data");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("audio", sourceFileUri);

                    System.out.println(testType);

                    conn.setRequestProperty("test_id", "select_" + testType);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"audio\";filename=\""
                            + sourceFileUri + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math
                                .min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0,
                                bufferSize);

                    }

                    // send multipart form data necesssary after file
                    // data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens
                            + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn
                            .getResponseMessage();

                    if (serverResponseCode == 200) {

                        // messageText.setText(msg);
                        //Toast.makeText(ctx, "File Upload Complete.",
                        //      Toast.LENGTH_SHORT).show();

                        // recursiveDelete(mDirectory1);

                    }

                    // close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (Exception e) {

                    // dialog.dismiss();
                    e.printStackTrace();

                }
                // dialog.dismiss();

            } // End else block


        } catch (Exception ex) {
            // dialog.dismiss();

            ex.printStackTrace();
        }
        return "Executed";
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

    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        float[] audioBuffer = new float[bufferSize / 2];

        recorder = new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
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
                byte[] audioBytes = new byte[Float.BYTES * audioBuffer.length];
                ByteBuffer.wrap(audioBytes).asFloatBuffer().put(audioBuffer);
                outStr.write(audioBytes, 0, bufferSize);

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
                    Log.v(LOG_TAG, "Save audio success" + filepath);
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

        float[] inputBuffer = new float[RECORDING_LENGTH];
        JLibrosa jLibrosa = new JLibrosa();

        recordingBufferLock.lock();
        try {
            int maxLength = recordingBuffer.length;
            System.arraycopy(recordingBuffer, 0, inputBuffer, 0, maxLength);
        } finally {
            recordingBufferLock.unlock();
        }

        float[][] mfccValues = jLibrosa.generateMFCCFeatures(inputBuffer, SAMPLE_RATE, 96, 4096, 512, 512);
        float[] floatValues = new float[96 * 96 * 1];
        int count = 0;
        for(int i = 0; i< mfccValues.length; ++i){
            for (int j = 0; j < mfccValues[i].length; ++j) {
                floatValues[count] = (float) mfccValues[i][j];
                count = count + 1;
            }
        }

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
