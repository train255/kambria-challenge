from flask import Flask

app = Flask(__name__)

@app.route('/uploadAudio',methods=['POST'])
def GetNoteText():
    if request.method == 'POST':
        file = request.files['audio']
        filename = file.filename
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
    else:
        return "Success"

app.run(port=9000) 