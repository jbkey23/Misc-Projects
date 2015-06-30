package com.example.tictactoe;

import java.io.IOException;
import java.net.DatagramPacket;

import android.util.Log;

public class WorkerThread extends Thread {
	
	public static final int MAX_PACKET_SIZE = 512;
	
	@Override
	public void run() {
		
		
		while(true)
		{
			byte[] buf = new byte[MAX_PACKET_SIZE];
			
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			
			try {
				MainActivity.socket.receive(p);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			
			String payload = new String(p.getData(), 0, p.getLength()).trim();
			
			if(payload.startsWith("REGISTERED"))
			{
				Log.d("Received", payload);
				MainActivity.handler.obtainMessage(MainActivity.REGISTERED, payload).sendToTarget();
			}
			else if(payload.startsWith("MESSAGE"))
			{
				Log.d("Received", payload);
				MainActivity.handler.obtainMessage(MainActivity.PUT_LETTER, payload).sendToTarget();
			}
			else if(payload.startsWith("ERROR"))
			{
				Log.d("Received", payload);
				MainActivity.handler.obtainMessage(MainActivity.ERROR).sendToTarget();
			}
			else if(payload.startsWith("You joined:"))
			{
				Log.d("Received", payload);
				MainActivity.handler.obtainMessage(MainActivity.JOINED).sendToTarget();
			}
			else if(payload.startsWith("READY"))
			{
				Log.d("Received", payload);
				MainActivity.handler.obtainMessage(MainActivity.READY).sendToTarget();
			}
			else if(payload.startsWith("NOT READY"))
			{
				Log.d("Received", payload);
				MainActivity.handler.obtainMessage(MainActivity.NOT_READY).sendToTarget();
			}
			else if(payload.startsWith("TOO MANY"))
			{
				Log.d("Received", payload);
				MainActivity.handler.obtainMessage(MainActivity.TOO_MANY).sendToTarget();
			}
		}
	}

}
