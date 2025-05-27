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

        # Here you would typically process the message, e.g., call a model or service
        # For demonstration, if user_message contains "manager", we simulate a response as "name: John Smith | tel: [12345678901](tel:12345678901)"
        if "manager" in user_message.lower():
            return jsonify({"response": "name: John Smith | tel: [12345678901](tel:12345678901)"})
        # if user_message contains "customer", we simulate a response as "name: Mary Davis | location: [Company HQ](location: 1600 Amphitheatre Parkway, Mountain View, CA)"
        elif "customer" in user_message.lower():
            return jsonify({"response": "name: Mary Davis | location: [Company HQ](location: 1600 Amphitheatre Parkway, Mountain View, CA)"})
        # otherwise, we just echo the message back
        else:
            return jsonify({"response": f"You said: {user_message}"})
    except Exception as e:
        print(f"Error: {e}")
        return jsonify({"error": "Malformed request"}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
