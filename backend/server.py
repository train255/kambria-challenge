from flask import Flask, request, jsonify
import os
import uuid
from werkzeug.datastructures import ImmutableMultiDict

# Convert pcm to wav
# ffmpeg -f s16le -ar 8k -i record.pcm file.wav

UPLOAD_DIR = 'uploads'

if not os.path.exists(UPLOAD_DIR):
	os.makedirs(UPLOAD_DIR)


app = Flask(__name__)


def saveFile(file, filename, subfolder):
	dir_pth = os.path.join(UPLOAD_DIR, subfolder)
	if not os.path.exists(dir_pth):
		os.makedirs(dir_pth)
	file.save(os.path.join(dir_pth, filename))

def saveResult(txt, filename, subfolder):
	dir_pth = os.path.join(UPLOAD_DIR, subfolder)
	if not os.path.exists(dir_pth):
		os.makedirs(dir_pth)
	f = open(os.path.join(dir_pth, filename),"w")
	f.write(txt)
	f.close()

@app.route('/uploadAudio',methods=['POST'])
def saveAudioFile():
	if request.method == 'POST':
		data = dict(request.form)
		print(data)
		request_id = str(uuid.uuid4())
		
		audio_file = request.files['audio']
		audio_filename =  audio_file.filename
		saveFile(audio_file, audio_filename, request_id)
		if "predict_covid" not in data:
			predict_result = data["predict_cough"]
			saveResult(predict_result, "result.txt", request_id)
		else:
			predict_result = data["predict_covid"]
			saveResult(predict_result, "result.txt", request_id)

			test_image = request.files['test_image']
			test_image_filename = test_image.filename
			saveFile(test_image, test_image_filename, request_id)

		return jsonify(
			success=True,
			message="Upload success"
		)
	else:
		return jsonify(
			success=False,
			message="Required Field missing"
		)

app.run(port=9000,debug=True) 