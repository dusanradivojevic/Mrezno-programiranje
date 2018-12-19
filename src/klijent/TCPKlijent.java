package klijent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class TCPKlijent{

	static Socket soketZaKomunikacijuSaServerom = null;
	static BufferedReader porukaOdServera = null;
	static PrintStream porukaZaServer = null;
	static BufferedReader unosSaTastature = null;
	
	public static void main(String[] args){
		
		try {
			int port = 1755;
			soketZaKomunikacijuSaServerom = new Socket("localhost", port); //dodaj 3 pokusaja konekcije sa serverom
			
			porukaOdServera = new BufferedReader(new InputStreamReader(soketZaKomunikacijuSaServerom.getInputStream()));
			porukaZaServer = new PrintStream(soketZaKomunikacijuSaServerom.getOutputStream());
			unosSaTastature = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.println("Uspostavljena konekcija sa serverom!");
						
			String serverMsg;
			String clientMsg;
			
			while(true) {
				
				while(true) {
					serverMsg = porukaOdServera.readLine();
					
					if(serverMsg.startsWith(">>Dovidjenja")) {						
						break;
					}
					
					if(serverMsg.equals("%%%"))	//oznaka da je server zavrsio sa slanjem poruka
						break;
					
					System.out.println(serverMsg);
				}		
				
				if(serverMsg.startsWith(">>Dovidjenja")) {
					break;
				}
				
				clientMsg = unosSaTastature.readLine();
				if(clientMsg.equals("*quit"))
					break;
				porukaZaServer.println(clientMsg);				
			}
			
			soketZaKomunikacijuSaServerom.close();
			
		} catch (Exception e) {
			System.out.println("Doslo je do greske pri konekciji sa serverom!");
		}
		
		
	}

}
