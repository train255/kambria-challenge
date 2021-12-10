from flask import Flask, request, jsonify
import os
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
		print(data)
		file = request.files['audio']
		filename = file.filename
		file.save(os.path.join(UPLOAD_DIR, filename))
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