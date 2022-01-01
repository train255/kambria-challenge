```bash
virtualenv --python=/usr/bin/python2.7 ~/.py27
source ~/.py27/bin/activate
pip install tensorflow==1.5.0 keras==2.2.4
python keras_to_tensorflow_android.py
```

```java
private TensorFlowInferenceInterface inferenceInterface;
private static final String INPUT_DATA_NAME = "conv2d_1_input";
private static final String OUTPUT_SCORES_NAME = "dense_1/Sigmoid";

private static final int NUM_ROWS = 120;
private static final int NUM_COLUMNS = 192;
private static final int NUM_CHANNELS = 1;


// app/src/main/assets/cough_detection.pb
// In Activity
inferenceInterface = new TensorFlowInferenceInterface(getAssets(),  "cough_detection.pb");
// In fragment
// inferenceInterface = new TensorFlowInferenceInterface(getContext().getAssets(),  "cough_detection.pb");

// Run the model.
String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};
float outputScores[] = new float[1];
inferenceInterface.feed(INPUT_DATA_NAME, floatValues, 1, NUM_ROWS, NUM_COLUMNS, 1);
inferenceInterface.run(outputScoresNames);
inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);
Log.v(LOG_TAG, "OUTPUT tf1======> " + Arrays.toString(outputScores));

```