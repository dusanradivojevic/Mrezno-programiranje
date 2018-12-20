package klijent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketException;

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
					
					if(serverMsg.equals("fileIncoming"))
						preuzmiFajl();
					
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
/////////////////////////////////////////////////////////////////////////////////////////////
	private static void preuzmiFajl() {
		try {
			InputStream strimZaPrijem = soketZaKomunikacijuSaServerom.getInputStream();
			int velicinaFajla = Integer.parseInt(porukaOdServera.readLine());
			String username = porukaOdServera.readLine();
			
			///
			File file = new File("Izvestaji\\" + username + ".txt");
			file.getParentFile().mkdir();
			file.createNewFile();
			///
			
			RandomAccessFile raf = new RandomAccessFile("Izvestaji\\"+username+".txt", "rw");
			
			int n = 0;
			byte[] buffer = new byte[velicinaFajla];
			
			porukaZaServer.println("Spreman za prijem fajla.");
			n = strimZaPrijem.read(buffer, 0, buffer.length);
			raf.write(buffer, 0, n);
			raf.close();
			porukaZaServer.println("Fajl je primljen.");
			
		} catch (Exception e) {
			System.out.println("Doslo je do greske pri konekciji sa serverom!");
		}
	}

}
