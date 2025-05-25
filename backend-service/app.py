from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json()
        if not data or 'message' not in data:
            return jsonify({"error": "No message provided"}), 400
        
        user_message = data['message']
        print(f"Incoming message: {user_message}") # Basic logging
        
        return jsonify({"response": f"Echo: {user_message}"})
    except Exception as e:
        print(f"Error: {e}")
        return jsonify({"error": "Malformed request"}), 400

if __name__ == '__main__':
    app.run(debug=True)
