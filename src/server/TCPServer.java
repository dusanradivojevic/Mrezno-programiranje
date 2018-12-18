package server;

import java.net.ServerSocket;
import java.net.Socket;

import handler.ClientHandler;


public class TCPServer {

	public static void main(String[] args) {
		
		int port = 1755;
		Socket SoketZaKomunikacijuSaKlijentom = null;
		ServerSocket SSoket = null;
		
		try {
			
			SSoket = new ServerSocket(port);
			
			while(true) {
				
				System.out.println("Server je pokrenut i ceka na komunikaciju..");
				
				SoketZaKomunikacijuSaKlijentom = SSoket.accept();
				
				System.out.println("Komunikacija je uspostavljena!");
			
				ClientHandler klijent = new ClientHandler(SoketZaKomunikacijuSaKlijentom);
								
				klijent.start();
			
			}
			
		} catch (Exception e) {
			System.out.println("Doslo je do greske pri pokretanju servera ili uspostavljanju komunikacije sa klijentom.");
		}

	}

}
