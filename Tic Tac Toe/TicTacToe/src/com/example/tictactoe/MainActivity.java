package com.example.tictactoe;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public final String LOCAL_ADDRESS = "10.0.0.3";
	public final String SERVER_ADDRESS = "54.186.235.124";
	public final int SERVER_PORT = 20000;
	
	private Button newGameButton;
	private Button pos0_0;
	private Button pos0_1;
	private Button pos0_2;
	private Button pos1_0;
	private Button pos1_1;
	private Button pos1_2;
	private Button pos2_0;
	private Button pos2_1;
	private Button pos2_2;
	private TextView statusText;
	private boolean myTurn;
	private boolean player1;
	private boolean gameOver;
	private boolean draw;
	private int buttonClicks;
	public static final int PUT_LETTER = 1;
	public static final int REGISTERED = 2;
	public static final int ERROR = 3;
	public static final int JOINED = 4;
	public static final int READY = 5;
	public static final int NOT_READY = 6;
	public static final int TOO_MANY = 7;
	
	private int clientID = -1;
	private String groupName;
	private boolean readyToStart = false;
	
	private ArrayList<String> fullGroups = new ArrayList<String>();
	private ProgressDialog ringProgressDialog = null;
	
	public static MyHandler handler = null;
	
	public static DatagramSocket socket = null;
	public static InetSocketAddress serverSocketAddress;
	public static WorkerThread wt = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		newGameButton = (Button)findViewById(R.id.newGameButton);
		pos0_0 = (Button)findViewById(R.id.button00);
		pos0_1 = (Button)findViewById(R.id.button01);
		pos0_2 = (Button)findViewById(R.id.button02);
		pos1_0 = (Button)findViewById(R.id.button10);
		pos1_1 = (Button)findViewById(R.id.button11);
		pos1_2 = (Button)findViewById(R.id.button12);
		pos2_0 = (Button)findViewById(R.id.button20);
		pos2_1 = (Button)findViewById(R.id.button21);
		pos2_2 = (Button)findViewById(R.id.button22);
		statusText = (TextView)findViewById(R.id.statusText);
		player1 = false;
		gameOver = false;
		draw = false;
		buttonClicks = 0;
		
		if(socket == null)
		{
			try {
				socket = new DatagramSocket();
				serverSocketAddress = new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		if(handler == null)
		{
			handler = new MyHandler(this);
		}
		
		if(wt == null)
		{
			wt = new WorkerThread();
			wt.start();
		}
		
		if(clientID == -1)
		{
			String command = "REGISTER";
			sendToServer(command);
		}
	}

	static class MyHandler extends Handler
 	{
 		private final WeakReference<MainActivity> mActivity;
 		
 		public MyHandler(MainActivity activity)
 		{
 			mActivity = new WeakReference<MainActivity>(activity);
 		}
 		@Override
 		public void handleMessage(Message msg)
 		{	
 			MainActivity activity = mActivity.get();
 			
 			if(activity != null)
 			{
 				switch(msg.what)
 				{
 				case PUT_LETTER:
 					Log.d("Handled", (String)msg.obj);
 					activity.putLetter((String)msg.obj);
 					break;
 				case REGISTERED:
 					Log.d("Handled", (String)msg.obj);
 					activity.setID((String)msg.obj);
 					break;
 				case JOINED:
 					Log.d("Handled", "joined");
 					activity.showJoinStatus(true);
 					break;
 				case ERROR:
 					Log.d("Handled", "error");
 					activity.showError();
 					break;
 				case READY:
 					Log.d("Handled", "ready");
 					activity.showReadyStatus(true);
 					break;
 				case NOT_READY:
 					Log.d("Handled", "not ready");
 					activity.showReadyStatus(false);
 					break;
 				case TOO_MANY:
 					Log.d("Handled", "too many");
 					activity.showJoinStatus(false);
 					break;
 				}
 			}
 		}
 	}
	
	
	
	public void showReadyStatus(boolean ready)
	{
		if(ready)
		{
			readyToStart = true;
			if(ringProgressDialog != null)
				ringProgressDialog.dismiss();
			if(player1)
			{
				myTurn = true;
				setBoardStatus(true);
			}
			else
			{
				setBoardStatus(false);
			}
			
			if (myTurn)
				statusText.setText("Your turn");
			else if (!myTurn)
				statusText.setText("Opponent's turn");
			
			setGameStatus();
		}
		else
		{
			player1 = true;
			//Toast.makeText(this, "No opponent available. Game will start when opponent joins.", Toast.LENGTH_LONG).show();
			ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Waiting for opponent to join game ...", true);
		}
	}
	
	public void showError()
	{
		Toast.makeText(this, "Error occured!!", Toast.LENGTH_SHORT).show();
	}
	
	public void showJoinStatus(boolean successful)
	{
		if(successful)
		{
			Toast.makeText(this, "Joined group: " + groupName, Toast.LENGTH_SHORT).show();
			sendToServer("LEGAL " + groupName);
		}
		else
		{
			Toast.makeText(this, "Group is full. Please enter a different groupname.", Toast.LENGTH_LONG).show();
			fullGroups.add(groupName);
		}
	}
	
	public void setID(String message)
	{
		String [] tokens = message.split(":");
		
		clientID = Integer.parseInt(tokens[1].trim());
		
		Toast.makeText(this, "Client ID is: " + clientID, Toast.LENGTH_SHORT).show();
	}
	
	public void sendToServer(String message)
	{
		new AsyncTask<String,Void, Void>()
		{

			@Override
			protected Void doInBackground(String... args) {
				
				String msg = args[0];
				
				try {
					DatagramPacket txPacket = new DatagramPacket(msg.getBytes(), msg.length(), serverSocketAddress);
					socket.send(txPacket);
				}
				catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				return null;
			}
			
		}.execute(message);
	}
	
	
	public void putLetter(String msg)
	{
		//if(!myTurn)
			//setBoardStatus(true);
		
		
		
		String [] tokens = msg.split(": ");
		
		String [] tokens2 = tokens[1].trim().split(" ");
		
		String pos = tokens2[0];
		String letter = tokens2[1];
		
		if(pos.equals("00"))
		{
			if(pos0_0.getText().equals(""))
			{
				pos0_0.setText(letter);
				pos0_0.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("01"))
		{
			if(pos0_1.getText().equals(""))
			{
				pos0_1.setText(letter);
				pos0_1.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("02"))
		{
			if(pos0_2.getText().equals(""))
			{
				pos0_2.setText(letter);
				pos0_2.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("10"))
		{
			if(pos1_0.getText().equals(""))
			{
				pos1_0.setText(letter);
				pos1_0.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("11"))
		{
			if(pos1_1.getText().equals(""))
			{
				pos1_1.setText(letter);
				pos1_1.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("12"))
		{
			if(pos1_2.getText().equals(""))
			{
				pos1_2.setText(letter);
				pos1_2.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("20"))
		{
			if(pos2_0.getText().equals(""))
			{
				pos2_0.setText(letter);
				pos2_0.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("21"))
		{
			if(pos2_1.getText().equals(""))
			{
				pos2_1.setText(letter);
				pos2_1.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("22"))
		{
			if(pos2_2.getText().equals(""))
			{
				pos2_2.setText(letter);
				pos2_2.setClickable(false);
				setBoardStatus(true);
				buttonClicks++;
			}
			updateStatusText();
		}
		else if(pos.equals("GAME") && letter.equals("OVER"))
		{
			gameOver = true;
			if(tokens2[2].equals("DRAW"))
				draw = true;
			
			setBoardStatus(false);
			displayWinner();
		}
	}
	
	public void updateStatusText()
	{
		if(statusText.getText().equals("Your turn"))
			statusText.setText("Opponent's turn");
		else
			statusText.setText("Your turn");
	}
	public void newGame(View v)
	{
		groupNamePrompt();
		
		while(fullGroups.contains(groupName))
		{
			groupNamePrompt();
		}
		
		
			newGameButton.setEnabled(false);
		/*	pos0_0.setEnabled(true);
			pos0_0.setClickable(true);
			pos0_0.setText("");
			pos0_1.setEnabled(true);
			pos0_1.setClickable(true);
			pos0_1.setText("");
			pos0_2.setEnabled(true);
			pos0_2.setClickable(true);
			pos0_2.setText("");
			pos1_0.setEnabled(true);
			pos1_0.setClickable(true);
			pos1_0.setText("");
			pos1_1.setEnabled(true);
			pos1_1.setClickable(true);
			pos1_1.setText("");
			pos1_2.setEnabled(true);
			pos1_2.setClickable(true);
			pos1_2.setText("");
			pos2_0.setEnabled(true);
			pos2_0.setClickable(true);
			pos2_0.setText("");
			pos2_1.setEnabled(true);
			pos2_1.setClickable(true);
			pos2_1.setText("");
			pos2_2.setEnabled(true);
			pos2_2.setClickable(true);
			pos2_2.setText("");*/

			//setGameStatus();
		
	}
	
	public void groupNamePrompt()
	{
		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(this);

		View promptView = layoutInflater.inflate(R.layout.prompt, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set prompts.xml to be the layout file of the alertdialog builder
		alertDialogBuilder.setView(promptView);

		final EditText input = (EditText) promptView.findViewById(R.id.userInput);

		// setup a dialog window
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// get user input and set it to result
								
								groupName = input.getText().toString();
								
								String message = "JOIN " + clientID + " " + groupName + "\n";
								
								sendToServer(message);
							}
						});
				/*.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								buttonCancelled = true;
								dialog.cancel();
							}
						});*/

		// create an alert dialog
		AlertDialog alertD = alertDialogBuilder.create();

		alertD.show();
	}
	
	public void displayWinner()
	{
		String winner = "";
		
		/*if (player1 && myTurn)
		{
			winner = "Player 1 Wins!";
		}
		else if (player1 && !myTurn)
		{
			winner = "Player 2 Wins!";
		}
		else if (!player1 && myTurn)
		{
			winner = "Player 2 Wins!";
		}
		else if (!player1 && !myTurn)
		{
			winner = "Player 1 Wins!";
		}*/
		
		if(myTurn)
			winner = "You Win!";
		else
			winner = "Opponent Wins!";
		
		if(draw)
			winner = "Draw!";
		
		statusText.setText("GAME OVER!! " + winner);
	}
	public void setGameStatus()
	{
		if(gameOver)
		{
			/*String winner;
			if(player1 && myTurn)
				winner = "Player 1 Wins!";
			else
				winner = "Player 2 Wins!";
			
			if(draw)
				winner = "Draw!";
			
			
			
			statusText.setText("GAME OVER!! " + winner);*/
			if(draw)
				sendToServer("SEND " + clientID + " " + groupName + " GAME OVER DRAW");
			else
				sendToServer("SEND " + clientID + " " + groupName + " GAME OVER NO");
			
			displayWinner();
			
			newGameButton.setEnabled(false);
			pos0_0.setEnabled(false);
			pos0_1.setEnabled(false);
			pos0_2.setEnabled(false);
			pos1_0.setEnabled(false);
			pos1_1.setEnabled(false);
			pos1_2.setEnabled(false);
			pos2_0.setEnabled(false);
			pos2_1.setEnabled(false);
			pos2_2.setEnabled(false);
			
			buttonClicks = 0;
			draw = false;
			gameOver = !gameOver;
		}
		/*else if (myTurn)
			statusText.setText("Your turn");
		else if (!myTurn)
			statusText.setText("Opponent's turn");*/
		
		//setBoardStatus(myTurn);
	}
	
	public void setBoardStatus(boolean myTurn)
	{
		pos0_0.setEnabled(myTurn);
		pos0_1.setEnabled(myTurn);
		pos0_2.setEnabled(myTurn);
		pos1_0.setEnabled(myTurn);
		pos1_1.setEnabled(myTurn);
		pos1_2.setEnabled(myTurn);
		pos2_0.setEnabled(myTurn);
		pos2_1.setEnabled(myTurn);
		pos2_2.setEnabled(myTurn);
	}
	
	public void gameBoardPressed(View v)
	{
		buttonClicks++;
		
		String letter;
		String message = "SEND " + clientID + " " + groupName + " ";
		
		if(player1)
		{
			letter = "X";
		}
		else
		{
			letter = "O";
		}
		
		if(v.getId() == pos0_0.getId())
		{
			pos0_0.setText(letter);
			message += "00 " + letter + "\n";
			pos0_0.setClickable(false);
		}
		else if(v.getId() == pos0_1.getId())
		{
			pos0_1.setText(letter);
			message += "01 " + letter + "\n";
			pos0_1.setClickable(false);
		}
		else if(v.getId() == pos0_2.getId())
		{
			pos0_2.setText(letter);
			message += "02 " + letter + "\n";
			pos0_2.setClickable(false);
		}
		else if(v.getId() == pos1_0.getId())
		{
			pos1_0.setText(letter);
			message += "10 " + letter + "\n";
			pos1_0.setClickable(false);
		}
		else if(v.getId() == pos1_1.getId())
		{
			pos1_1.setText(letter);
			message += "11 " + letter + "\n";
			pos1_1.setClickable(false);
		}
		else if(v.getId() == pos1_2.getId())
		{
			pos1_2.setText(letter);
			message += "12 " + letter + "\n";
			pos1_2.setClickable(false);
		}
		else if(v.getId() == pos2_0.getId())
		{
			pos2_0.setText(letter);
			message += "20 " + letter + "\n";
			pos2_0.setClickable(false);
		}
		else if(v.getId() == pos2_1.getId())
		{
			pos2_1.setText(letter);
			message += "21 " + letter + "\n";
			pos2_1.setClickable(false);
		}
		else if(v.getId() == pos2_2.getId())
		{
			pos2_2.setText(letter);
			message += "22 " + letter + "\n";
			pos2_2.setClickable(false);
		}
		
		sendToServer(message);
		
		if(gameOver())
		{
			gameOver = true;
			setGameStatus();
			return;
		}
		
		myTurn = !myTurn;
		setBoardStatus(false);
		setGameStatus();
	}
	
	public boolean gameOver()
	{
		if(pos0_0.getText() == pos0_1.getText() && pos0_1.getText() == pos0_2.getText() && pos0_0.getText() != "")
		{
			return true;
		}
		else if(pos1_0.getText() == pos1_1.getText() && pos1_1.getText() == pos1_2.getText() && pos1_0.getText() != "")
		{
			return true;
		}
		else if(pos2_0.getText() == pos2_1.getText() && pos2_1.getText() == pos2_2.getText() && pos2_0.getText() != "")
		{
			return true;
		}
		else if(pos0_0.getText() == pos1_0.getText() && pos1_0.getText() == pos2_0.getText() && pos0_0.getText() != "")
		{
			return true;
		}
		else if(pos0_1.getText() == pos1_1.getText() && pos1_1.getText() == pos2_1.getText() && pos0_1.getText() != "")
		{
			return true;
		}
		else if(pos0_2.getText() == pos1_2.getText() && pos1_2.getText() == pos2_2.getText() && pos0_2.getText() != "")
		{
			return true;
		}
		else if(pos0_0.getText() == pos1_1.getText() && pos1_1.getText() == pos2_2.getText() && pos0_0.getText() != "")
		{
			return true;
		}
		else if(pos0_2.getText() == pos1_1.getText() && pos1_1.getText() == pos2_0.getText() && pos0_2.getText() != "")
		{
			return true;
		}
		else if(buttonClicks == 9)
		{
			draw = true;
			return true;
		}
		
		return false;
	}
}
