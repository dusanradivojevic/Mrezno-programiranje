package server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;

import handler.ClientHandler;


public class TCPServer {

	public static void main(String[] args) {
		
		int port = 1755;
		Socket SoketZaKomunikacijuSaKlijentom = null;
		ServerSocket SSoket = null;
		setUpServer();
		
		try {
			
			SSoket = new ServerSocket(port);
			
			
			while(true) {
				
				System.out.println("Server je pokrenut i ceka na konekciju..");
				
				SoketZaKomunikacijuSaKlijentom = SSoket.accept();
				
				System.out.println("Konekciju je uspostavljena!");
				System.out.println("----------------------------");
			
				ClientHandler klijent = new ClientHandler(SoketZaKomunikacijuSaKlijentom);
								
				klijent.start();
			
			}
			
		} catch (Exception e) {
			System.out.println("Doslo je do greske pri pokretanju servera ili uspostavljanju konekcije sa klijentom.");
		}

	}
	
	private static void setUpServer() {
		File file = new File("Korisnici\\test.txt");
		file.getParentFile().mkdir();
		try {
			file.createNewFile();
			System.out.println(file.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("Iskocilo nesto u setUpServer");
		}
	}

}
