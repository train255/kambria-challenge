import tensorflow as tf
from keras.layers import BatchNormalization, Conv2D, Dense, GlobalAveragePooling2D
from keras.models import Sequential

from keras import backend as K
from tensorflow.python.framework import graph_util


def export_model_for_mobile(model_name, input_node_name, output_node_name):
	tf.train.write_graph(K.get_session().graph_def, 'out', \
		model_name + '_graph.pbtxt')

	input_checkpoint = 'out/' + model_name + '.chkt'
	tf.train.Saver().save(K.get_session(), input_checkpoint)

	graph = tf.get_default_graph()
	# for op in graph.get_operations():
	# 	if "dense" in op.name:
	# 		print op.name
	input_graph_def = graph.as_graph_def()

	with tf.Session() as sess:
		# tf.sg_init(sess)
		saver = tf.train.Saver()
		saver.restore(sess, tf.train.latest_checkpoint('out'))
		# Output model's graph details for reference.
		tf.train.write_graph(sess.graph_def, 'out', 'graph.txt', as_text=True)

		# Freeze the output graph.
		output_graph_def = graph_util.convert_variables_to_constants(sess,input_graph_def,[output_node_name])
		# Write it into .pb file.
		with tf.gfile.FastGFile("out/"+model_name+".pb", "wb") as f:
				f.write(output_graph_def.SerializeToString())


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
	return model



def convert():
	cnn_model = create_cnn()
	cnn_model.load_weights("models/cough_detection.hdf5")

	export_model_for_mobile('cough_detection', "conv2d_1_input", "dense_1/Sigmoid")


convert()