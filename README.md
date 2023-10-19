# Removed codes from the codebase

```java
if (msg.equalsIgnoreCase("n")) {
    msg = DISCONNECT_MESSAGE;
    sendMessage(msg);
    connected = false;
    continue;
} else if (msg.equalsIgnoreCase("y")) {
    sendMessage(GAME_START);
    String serverMsg = receiveMessage();
    System.out.println(serverMsg);
    showStartMessage = false;
} else {
    coloredPrint("Enter a VALID option [y/Y] or [n/N]", YELLOW);
    continue;
}
```