# Backend AI Chat Box (Echo Service)

This is a simple Flask-based backend service designed for the "Wearable AI Chat" Android Wear OS application. For demonstration, if message from the Wear OS app contains "manager", return a response as "name: John Smith | tel: [12345678901](tel:12345678901)"; if message contains "customer", return a response as "name: Mary Davis | location: [Company HQ](location: 1600 Amphitheatre Parkway, Mountain View, CA)"; otherwise returns the same message prefixed with "Echo: ". 

This service helps demonstrate the end-to-end functionality of the chat application, including network requests and responses, without requiring complex AI integration on the backend.

## Requirements

*   Python 3.7+

## Setup and Run

1.  **Navigate to the `backend-service` directory:**
    ```bash
    cd path/to/your/project/backend-service
    ```

2.  **Create a virtual environment (recommended):**
    ```bash
    python -m venv venv
    ```

3.  **Activate the virtual environment:**
    *   On macOS and Linux:
        ```bash
        source venv/bin/activate
        ```
    *   On Windows:
        ```bash
        venv\Scripts\activate
        ```

4.  **Install dependencies:**
    ```bash
    pip install -r requirements.txt
    ```

5.  **Run the Flask application:**
    You can run the application using the Flask CLI:
    ```bash
    flask run
    ```
    Alternatively, for development mode which provides live reloading and a debugger:
    ```bash
    export FLASK_APP=app.py  # On macOS/Linux
    # set FLASK_APP=app.py    # On Windows
    export FLASK_ENV=development # On macOS/Linux
    # set FLASK_ENV=development # On Windows
    flask run
    ```
    The service will start and listen for incoming requests.

## Service Details

*   **Host:** `127.0.0.1` (localhost)
*   **Port:** `5000`
*   **Base URL:** `http://127.0.0.1:5000/`
*   **Chat Endpoint:** `/chat`
    *   **Method:** `POST`

## API Contract

### Request

The service expects a JSON payload with a single key, `message`.

*   **Endpoint:** `/chat`
*   **Method:** `POST`
*   **Headers:** `Content-Type: application/json`
*   **Body Format:**
    ```json
    {
        "message": "Your message text here"
    }
    ```

### Response

*   **Success (HTTP 200 OK):**
    The service echoes the message back.
    ```json
    {
        "response": "Echo: Your message text here"
    }
    ```

*   **Error - No Message Provided (HTTP 400 Bad Request):**
    If the `message` field is missing or empty in the request.
    ```json
    {
        "error": "No message provided"
    }
    ```

*   **Error - Malformed Request (HTTP 400 Bad Request):**
    If the request body is not valid JSON.
    ```json
    {
        "error": "Malformed request"
    }
    ```

---

This service must be running for the Wearable AI Chat application to function correctly. See the Wearable App's README for instructions on how it connects to this backend.
