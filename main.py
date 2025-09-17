import time
import os
import uuid
import shutil
from flask import Flask, request, jsonify
from flask_cors import CORS
from model import *

app = Flask(__name__)
CORS(app)
model = load_model()
#model = None

@app.route('/status', methods=['GET'])
def status():
    """
    This is a health check endpoint to check if the server is running
    :return: a json object with a key "result" and value "ok"
    """
    return jsonify({"result": "ok"})

@app.route('/image', methods=['POST'])
def image_predict():
    """
    This is the main endpoint for the image prediction algorithm
    :return: a json object with a key "result" and value a dictionary with keys "obstacle_id" and "image_id"
    """
    file = request.files['file']
    filename = file.filename
    
    # Save to uploads folder first
    file.save(os.path.join('uploads', filename))
    
    # filename format: "<timestamp>_<obstacle_id>_<signal>.jpeg"
    constituents = file.filename.split("_")
    obstacle_id = constituents[1]
    
    ## Week 8 ## 
    signal = constituents[2].strip(".jpg")
    image_id = predict_image(filename, model, signal)
    
    ## Week 9 ## 
    # We don't need to pass in the signal anymore
    #image_id = predict_image_week_9(filename,model)
    
    # Create results folder
    results_folder = 'results'
    if not os.path.exists(results_folder):
        os.makedirs(results_folder)
    
    # Generate UUID
    unique_id = str(uuid.uuid4())
    
    # Create new filename format: {UUID}_Label.png
    new_filename = f"{unique_id}_{image_id}.png"
    
    # Copy original image to results folder with new name
    original_path = os.path.join('uploads', filename)
    new_path = os.path.join(results_folder, new_filename)
    
    try:
        # Copy original file without any processing
        shutil.copy2(original_path, new_path)
        print(f"Image saved to: {new_path}")
    except Exception as e:
        print(f"Error saving image: {e}")
    
    # Return the obstacle_id and image_id
    result = {
        "obstacle_id": obstacle_id,
        "image_id": image_id
    }
    return jsonify(result)

@app.route('/stitch', methods=['GET'])
def stitch():
    """
    This is the main endpoint for the stitching command. Stitches the images using two different functions, in effect creating two stitches, just for redundancy purposes
    """
    img = stitch_image()
    img.show()
    img2 = stitch_image_own()
    img2.show()
    return jsonify({"result": "ok"})

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=8080, debug=True)
