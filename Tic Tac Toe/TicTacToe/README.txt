The protocol for my Tic Tac Toe game works as follows:

When the app is opened, the client automatically registers with the server and a clientID is returned to the client.

When the new game button is pressed, the client is prompted to enter the name of the group that he wants to play in. This uses the server's JOIN command.

If the group that is entered does not yet have two players in it, a progress dialog will be displayed until another players joins the group. This is done using the LEGAL command of the server.

When the game is started, a button press triggers a message to be sent to the server with the SEND command which will send the message to all people in the group.

If the player does not have the current turn, his buttons will be disabled. 

The game will specifiy who wins the game, or whether the game is a draw.

For this prototype, the game is only playable one time.

In summary, my tic tac toe game uses the following commands from my server:

-REGISTER
-JOIN
-LEGAL
-SEND