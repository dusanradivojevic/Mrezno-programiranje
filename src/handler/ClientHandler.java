package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

/*
 * obrada gresaka pri losem unosu formata jednacina * 
 * provera svih situacija nasilnog prekida (*quit) - resavanje pravljenjem fje za proveru nasilnog 
 * *** izlaza i pozivanje iste kod svakog citanja korisnicke poruke
 * *** ili obrada svakog exeptiona prilikom nasilnog izlaza
 *
 * 
 * registracija
 * 
 * neogranicen broj kalkulacija za prijavljene korisnike
 * maksimum 3 kalkulacije za goste
 * 
 * cuvanje zahteva kalkulacija
 * 
 * 
 * ubaci na git
 *  
 */

public class ClientHandler extends Thread {

	BufferedReader porukaOdKlijenta = null;
	PrintStream porukaZaKlijenta = null;
	Socket soketZaKomunikaciju = null;
	public String username;
	public String password;
	public int sign = -1; // vrsta korisnika
	public String zahtevi; // svi zahtevi koje je korisnik uputio, sluzi za kasnije cuvanje u fajl

	public ClientHandler(Socket soketZaKomunikaciju) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;
	}

	@Override
	public void run() {

		komuniciraj();
		
	}

	private void komuniciraj() {

		try {
			porukaOdKlijenta = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			porukaZaKlijenta = new PrintStream(soketZaKomunikaciju.getOutputStream());

			porukaZaKlijenta.println(">>Dobrodosli, uspesno ste se povezali na server!");
//			porukaZaKlijenta.println("%%%");  // oznaka da klijent prestane sa slusanjem

			prikaziMeni();		

			porukaZaKlijenta.println(">>Zdravo, "+username+"!"); 

			kalkulator(sign);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private void prikaziMeni() {
		porukaZaKlijenta.println(">>Izaberite jednu od sledecih opcija:");
		porukaZaKlijenta.println(">>1 Prijavljivanje\n>>2 Registracija\n>>3 Gost\n>>4 Izlaz\n\n");
		porukaZaKlijenta.println(">>Prijavite se ako zelite da koristite sve nase usluge.\n"
				+ "Ukoliko nemate nalog registrujte se ili koristite usluge servera kao gost.\n"
				+ "Za prekid u bilo kom trenutku unesite *quit\n");
		porukaZaKlijenta.println("%%%"); 

		boolean provera = false;
		int izbor = -1;

		do {
			try {
				izbor = Integer.parseInt(porukaOdKlijenta.readLine());
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

			if (izbor >= 1 && izbor <= 4)
				provera = true;
			else {
				porukaZaKlijenta.println(">>Pogresili ste, pokusajte ponovo!");
				porukaZaKlijenta.println("%%%"); 
			}

		} while (!provera);

		if(sign == -1)
			sign = izbor;
		
		switch (izbor) {
		case 1:
			login();
			break;
		case 2:
			registration();
			break;
		case 3:
			guest();
			break;
		case 4:
			quit();
			break;
		}		
	}

	private void login() {
		try {
			porukaZaKlijenta.println(">>Unesite vas username:");
			porukaZaKlijenta.println("%%%"); 
			username = porukaOdKlijenta.readLine();

			porukaZaKlijenta.println(">>Unesite vas password:");
			porukaZaKlijenta.println("%%%"); 
			password = porukaOdKlijenta.readLine();

			// proveri da li postoji takav korisnik u bazi (otvara fajl sa tim usernamom, ako ga ne otvori znaci nema korisnika u bazi)

			// ako prodje onda je njegov znak 1 tako i za ostale tipove korisnika

			if(username.equals("*quit")||password.equals("*quit"))
				quit();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void registration() {
		try {
			porukaZaKlijenta.println(">>Unesite vas username:");
			porukaZaKlijenta.println("%%%"); 
			username = porukaOdKlijenta.readLine();
			// provera da ne postoji vec korisnik sa istim usernamom

			porukaZaKlijenta.println(">>Unesite vas password:");
			porukaZaKlijenta.println("%%%"); 
			password = porukaOdKlijenta.readLine();

			// kreira fajl ciji je naziv username klijenta i upisuje u prvi red password
			// (ili u sklopu naziva fajla)
			// taj fajl se dalje koristi za upisivanje zahteva koje je korisnik upucivao

			porukaZaKlijenta.println(">>Uspesno ste se registrovali!\n");
			porukaZaKlijenta.println("%%%"); 

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void guest() {
		Random rand = new Random();
		int n = rand.nextInt(9999999) + 1000000;
		username = "Gost_" + n;
	}

	private void quit() {
		porukaZaKlijenta.println(">>Dovidjenja " + username + "!");
		porukaZaKlijenta.println("%%%");
		try {
			soketZaKomunikaciju.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void kalkulator(int sign) {
		String jednacina = null;
		double br1 = 0;
		double br2 = 0;
		char znak;

		try {
			porukaZaKlijenta.println(">>Unesite zeljenu jednacinu u sledecem formatu: broj1 znak_operacije broj2\n");
			porukaZaKlijenta.println("%%%");
			jednacina = porukaOdKlijenta.readLine();

			String[] pom = jednacina.split(" ");
			br1 = Double.parseDouble(pom[0]);
			br2 = Double.parseDouble(pom[2]);
			znak = pom[1].charAt(0);

			double r = -1;

			switch (znak) {
			case '+':
				r = br1 + br2;
				porukaZaKlijenta.println(">>Rezultat je: " + r);
				break;
			case '-':
				r = br1 - br2;
				porukaZaKlijenta.println(">>Rezultat je: " + r);
				break;
			case '*':
				r = br1 * br2;
				porukaZaKlijenta.println(">>Rezultat je: " + r);
				break;
			case '/':
				if (br2 != 0) {
					r = br1 / br2;
					porukaZaKlijenta.println(">>Rezultat je: " + r);
					break;
				} else {
					porukaZaKlijenta.println("Ne mozemo deliti s nulom");
					break;
				} //ako u fajl treba da se pored zahteva pamte i rezultati ovo treba izmeniti
			}

			//ako zeli jos da racuna ili da trazi izvestaj da mu da izbor
			
			porukaZaKlijenta.println(">>KRAJ!");
			quit();			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
