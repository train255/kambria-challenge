import tensorflow as tf
from tensorflow.keras.layers import BatchNormalization, Conv2D, Dense, GlobalAveragePooling2D
from tensorflow.keras.models import Sequential

def create_cnn():
	num_rows = 120
	num_columns = 192
	num_channels = 1
	model = Sequential()
	model.add(Conv2D(16, (7,7), input_shape=(num_rows, num_columns, num_channels), activation='relu', padding="same"))
	model.add(BatchNormalization())
	model.add(Conv2D(32, (3,3), activation='relu', padding="same"))
	model.add(BatchNormalization())
	model.add(Conv2D(64, (3,3), activation='relu', padding="same"))
	model.add(BatchNormalization())
	model.add(Conv2D(128, (3,3), activation='relu', padding="same"))
	model.add(BatchNormalization())
	model.add(Conv2D(256, (3,3), activation='relu', padding="same"))
	model.add(BatchNormalization())
	model.add(Conv2D(512, (1,1), activation='relu', padding="same"))
	model.add(BatchNormalization())
	model.add(GlobalAveragePooling2D())
	model.add(Dense(1, activation='sigmoid'))
	print(model.summary())
	return model


for i in range(1, 6):
	cnn_model = create_cnn()
	cnn_model.load_weights("models/covid_detection_"+str(i)+".hdf5")

	# Convert the model.
	converter = tf.lite.TFLiteConverter.from_keras_model(cnn_model)
	tflite_model = converter.convert()

	# Save the model.
	with open("models/covid_detection_"+str(i)+".tflite", 'wb') as f:
		f.write(tflite_model)
