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

@app.route('/uploadAudio',methods=['POST'])
def saveAudioFile():
	if request.method == 'POST' and 'audio' in request.files:
		data = dict(request.form)

		file = request.files['audio']
		filename = str(uuid.uuid4()) + file.filename
		dir_pth = os.path.join(UPLOAD_DIR, data["test_type"])
		if not os.path.exists(dir_pth):
			os.makedirs(dir_pth)

		file.save(os.path.join(dir_pth, filename))
		return jsonify(
			success=True,
			message="Upload success"
		)
	else:
		return jsonify(
			success=False,
			message="Required Field missing"
		)

app.run(port=9000) 