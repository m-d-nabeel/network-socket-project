# Project README

## Description

This project is a client-server application designed to facilitate secure communication and various interactive tasks between clients. The communication is based on a shared secret key established using the Diffie-Hellman key exchange algorithm. The server manages the key exchange, allowing clients to securely communicate with each other.

## Prerequisites

Before running the client, ensure that the server is started to establish a connection successfully. Additionally, verify that both server and client are configured to run on the same port locally.

## Dependencies

This project relies on the GSON library for JSON serialization and deserialization. Ensure that the library is added to your project before running.

## How to Run

### Server

1. Go to `src/main/java/com/network/MainServer.java`.
2. Run the main function.

### Client

1. Go to `src/main/java/com/network/MainClient.java`.
2. Run the main function.

**Note:** Always start the server before the client to ensure proper functionality.

## Key Variables

### Diffie-Hellman Key Exchange

- `P`: A prime number used for key exchange (Same for both server and client).
- `G`: A generator used in the key exchange process (Same for both server and client).
- `serverPrivateKey`: Private key of the server.
- `clientPrivateKey`: Private key of the client.

### Network Configuration

- `PORT`: Port number used for communication.
- `SERVER`: Server address, set to "localhost" for local testing.

### ANSI Escape Codes

These codes are used for text colors in the console.

- `RESET`
- `RED`
- `GREEN`
- `YELLOW`
- `BLUE`
- `BRIGHT_BLACK`

## Data Structures

- `textMessagesSent`: Map to store sent messages categorized by users.
- `textMessagesReceived`: Map to store received messages categorized by users.
- `userList`: Map to store user information.
- `previousSentMessage`: Map to store the previous sent message.
- `hashMap`: HashMap for internal use.

## Execution Steps

1. Start the server by executing the appropriate command or running the server application.
2. Once the server is running, start the client by executing the appropriate command or running the client application.
3. Verify that the client is running on the same port as the server to establish a successful connection.

By following these steps, you can ensure that the server and client are properly configured and connected. This project provides a foundation for secure communication and interactive tasks between multiple clients.
