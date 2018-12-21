package klijent;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class TCPKlijent{

	static Socket soketZaKomunikacijuSaServerom = null;
	static BufferedReader porukaOdServera = null;
	static PrintStream porukaZaServer = null;
	static BufferedReader unosSaTastature = null;
	
	public static void main(String[] args){
		
		try {
			int port = 1755;
			String ip = "127.0.0.1"; // "localhost"
			
			System.out.println("Konektujem se na server..");
			for(int i = 0; i < 3; i++) {
				try {
					soketZaKomunikacijuSaServerom = new Socket(ip, port);
					break;
				} catch (Exception e) {
					System.out.println("Neuspesna konekcija broj "+ (i+1) + ". Pokusavam ponovo..");
				} 
			}
				
			porukaOdServera = new BufferedReader(new InputStreamReader(soketZaKomunikacijuSaServerom.getInputStream()));
			porukaZaServer = new PrintStream(soketZaKomunikacijuSaServerom.getOutputStream());
			unosSaTastature = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.println("Uspostavljena konekcija sa serverom!");
						
			String serverMsg;
			String clientMsg;
			
			while(true) {
				
				while(true) {
					serverMsg = porukaOdServera.readLine();
					
					if(serverMsg.equals(">>Fajl sa kalkulacijama se salje...")) {
						preuzmiFajl();
//						break;
					}
					
					if(serverMsg.startsWith(">>Dovidjenja")) {	
						System.out.println(serverMsg);
						break;
					}
					
					if(serverMsg.equals("%%%"))	//oznaka da je server zavrsio sa slanjem poruka
						break;
					
					System.out.println(serverMsg);
				}		
				
				if(serverMsg.startsWith(">>Dovidjenja") || serverMsg.equals("fileIncoming")) {
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
	private static void preuzmiFajl()  {
		try {
			InputStream strimZaPrijem = soketZaKomunikacijuSaServerom.getInputStream();
			int velicinaFajla = Integer.parseInt(porukaOdServera.readLine());
			String username = porukaOdServera.readLine();
						
			///
			File file = new File("Izvestaji\\" + username + ".txt");
			file.getParentFile().mkdir();
			file.createNewFile();
			///
			
			RandomAccessFile raf = new RandomAccessFile("Izvestaji\\" + username + ".txt", "rw");
			
			int n = 0;
			byte[] buffer = new byte[velicinaFajla];
			
			porukaZaServer.println("\nKlijent je spreman za prijem fajla.");
			
			n = strimZaPrijem.read(buffer, 0, buffer.length);
			raf.write(buffer, 0, n);
			
//			porukaZaServer.println("Klijent je primio fajl.");
			
			raf.close();			
		} catch (Exception e) {
			System.out.println("Doslo je do greske pri konekciji sa serverom!");
		}
	}

}
