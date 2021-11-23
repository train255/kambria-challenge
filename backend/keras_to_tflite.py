import tensorflow as tf
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D
from tensorflow.keras.layers import Input, Concatenate
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.models import Model

def create_cnn():
    img_input = Input(shape=(96, 96, 1))
    img_conc = Concatenate()([img_input, img_input, img_input])
    base_model = MobileNetV2(include_top=False, weights='imagenet', input_tensor=img_conc)
    avgpool = GlobalAveragePooling2D()(base_model.output)
    outputs = Dense(1, activation='sigmoid')(avgpool)
    model = Model(inputs=base_model.input, outputs=outputs)
    print(model.summary())
    return model

cnn_model = create_cnn()
cnn_model.load_weights("mobilenetv2.hdf5")


# Convert the model.
converter = tf.lite.TFLiteConverter.from_keras_model(cnn_model)
tflite_model = converter.convert()

# Save the model.
with open('mobilenetv2.tflite', 'wb') as f:
	f.write(tflite_model)
