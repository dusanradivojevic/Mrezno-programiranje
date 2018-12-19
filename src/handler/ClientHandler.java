package handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
 * regulacija da na svakoj tacki procesa korisnik moze da unese *quit
 * i prekine konekciju bez greski
 * 
 * PISI OPET KALKULATOR FJU UZ:
 * 		neogranicen broj kalkulacija za prijavljene korisnike
 * 		maksimum 3 kalkulacije za goste 
 * 		cuvanje liste kalkulacija
 * 
 * slanje iste
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

		try {

			komuniciraj();

		} catch (SocketException se) {

			try {
				quit(2);
			} catch (SocketException e) {
				// exitCode == 2 sprecava ovaj exception
				e.printStackTrace();
			}
		}
	}

	private void komuniciraj() throws SocketException {

		try {
			porukaOdKlijenta = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			porukaZaKlijenta = new PrintStream(soketZaKomunikaciju.getOutputStream());

			/*
			 * try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 */

			porukaZaKlijenta.println(">>Dobrodosli, uspesno ste se povezali na server!");
//			porukaZaKlijenta.println("%%%");  // oznaka da klijent prestane sa slusanjem

// 	socketexception

			prikaziMeni();

			porukaZaKlijenta.println(">>Zdravo " + username + ", uspesno ste se povezali na server!");

			kalkulator(sign);

		} catch (IOException e) {

			System.out.println(e.getMessage());
		}

	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void prikaziMeni() throws SocketException {
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
			} catch (NumberFormatException | IOException e) {
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

		if (sign == -1)
			sign = izbor; // proveri da ne sme da dobije sign ako se neuspesno loguje

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
			quit(1);
			break;
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void login() throws SocketException {
		try {
			boolean checker = false;
			do {
				porukaZaKlijenta.println(">>Unesite korisnicko ime:");
				porukaZaKlijenta.println("%%%");
				username = porukaOdKlijenta.readLine();

				porukaZaKlijenta.println(">>Unesite lozinku:");
				porukaZaKlijenta.println("%%%");
				password = porukaOdKlijenta.readLine();

				try {
					BufferedReader reader = new BufferedReader(new FileReader("Korisnici\\" + username + ".txt"));
					String pom = reader.readLine();
					reader.close();

					if (pom.equals(password)) {
						checker = true;
					} else {
						porukaZaKlijenta.println(">>Neispravno korisnicko ime ili lozinka, pokusajte ponovo.");
					}
				} catch (IOException e) {
					porukaZaKlijenta.println(">>Neispravno korisnicko ime ili lozinka, pokusajte ponovo.");
				}

			} while (!checker);

			if (username.equals("*quit") || password.equals("*quit"))
				quit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void registration() throws SocketException {
		String putanjaDoFajla = null;
		try {
			do {
				porukaZaKlijenta.println(">>Unesite vase korisnicko ime:");
				porukaZaKlijenta.println("%%%");
				username = porukaOdKlijenta.readLine();

				putanjaDoFajla = postojiUsername(username);

				if (putanjaDoFajla == null) {
					porukaZaKlijenta.println(">>Postoji korisnik sa tim korisnickim imenom! Pokusajte ponovo.");
				} else
					break;

			} while (true);

			boolean checker = false;
			do {
				porukaZaKlijenta.println(
						">>Unesite vasu lozinku:\n>>Napomena:" + " lozinka se mora sastojati iz minimum 8 karaktera, "
								+ "minimum jednog velikog slova" + "(A-Z) i minimum jednog broja (0-9)");
				porukaZaKlijenta.println("%%%");

				password = porukaOdKlijenta.readLine();

				checker = checkPassword(password);

				if (!checker)
					porukaZaKlijenta.println(">>Neispravna lozinka! Pokusajte ponovo.");

			} while (!checker);

			// upisi password u fajl

			PrintWriter writer = new PrintWriter(putanjaDoFajla, "UTF-8");
			writer.println(password);
			writer.close();

			porukaZaKlijenta.println(">>Uspesno ste se registrovali!\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////	
	private boolean checkPassword(String pw) {
		char ch;
		boolean velikoSlovo = false;
		boolean numberFlag = false;

		if (pw.length() <= 8)
			return false;

		for (int i = 0; i < pw.length(); i++) {
			ch = pw.charAt(i);

			if (Character.isDigit(ch)) {
				numberFlag = true;
			} else if (Character.isUpperCase(ch)) {
				velikoSlovo = true;
			}

			if (numberFlag && velikoSlovo)
				return true;
		}

		return false;
	}

//////////////////////////////////////////////////////////////////////////////////////////
	private String postojiUsername(String u) {
		File file = new File("Korisnici\\" + u + ".txt");

		try {
			if (file.createNewFile()) {
				return file.getAbsolutePath();
			} else {
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void guest() {
		Random rand = new Random();
		int n = rand.nextInt(9999999) + 1000000;
		username = "Gost_" + n;
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void quit(int exitCode) throws SocketException { // exitCode == 2 oznacava da je pukla konekcija
		//?dodaj da dobija string kalkulacije i da pre zatvaranja konekcije upise u fajl korisnika 

		if (exitCode == 2) {
			try {
				// dodaj posaljiIzvestaj() da bi se sacuvao poslednji zahtev
				// to takodje uradi svuda gde moze da dodje do gubljenja podataka
				// (eventualno stavi samo gde hvatas socketexception)
				soketZaKomunikaciju.close();

			} catch (IOException e) {
				System.out.println("Greska u funckiji quit() pri zatvaranju soketa za komunikaciju sa klijentom.");
				e.printStackTrace();
			}

			return;
		}

		porukaZaKlijenta.println(">>Dovidjenja " + username + "!");

		try {
			soketZaKomunikaciju.close();

		} catch (IOException e) {
			System.out.println("Greska u funckiji quit() pri zatvaranju soketa za komunikaciju sa klijentom.");
			e.printStackTrace();
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void kalkulator(int sign) throws SocketException {
		String kalkulacije = "";
		String jednacina = null;
		double br1 = 0;
		double br2 = 0;
		char znak = 'z';
		double rezultat = 0;
		int ogranicenje;
		int iterator = 0; // broj izvrsenih kalkulacija

		int izbor = 0;
		
		if (sign == 3)
			ogranicenje = 3;
		else
			ogranicenje = -1;

		if (ogranicenje == 3) {
			porukaZaKlijenta.println(">>Kao gost imate pravo na 3 kalkulacije.");
		} else {
			porukaZaKlijenta.println(">>Kao registrovani korisnik imate pravo na neogranicen broj kalkulacija.");
		}

		while (true) {
			if (iterator == ogranicenje) {
				porukaZaKlijenta.println(">>Kao gost nemate pravo na dodatne kalkulacije.");
				break; // i/ili drugi nacin prekida
			}

			if (iterator != 0) {
				porukaZaKlijenta.println(">>Broj preostalih kalkulacija: " + (ogranicenje - iterator) + ".");
			}

			porukaZaKlijenta.println("\n>>Unesite zeljenu jednacinu u sledecem formatu: broj1 znak_operacije broj2\n");
			porukaZaKlijenta.println("%%%");
			try {
				jednacina = porukaOdKlijenta.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (!proveraFormataJednacine(jednacina)) {
				porukaZaKlijenta.println("\n>>Format zahteva nije odgovarajuci, pokusajte ponovo!");
				porukaZaKlijenta.println("\n>>Unesite zeljenu jednacinu u sledecem formatu: broj1 znak_operacije broj2\n");
				porukaZaKlijenta.println("%%%");
				try {
					jednacina = porukaOdKlijenta.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			String[] pom = jednacina.split(" ");

			br1 = Double.parseDouble(pom[0]);
			br2 = Double.parseDouble(pom[2]);
			znak = pom[1].charAt(0);
			
			rezultat = izracunaj(br1, znak, br2);
			
			porukaZaKlijenta.println(">>Rezultat je: " + rezultat);
			
			kalkulacije += "\n" + br1 + " " + znak + " " + br2 + " = " + rezultat;
			
			while(true) {
				porukaZaKlijenta.println(">>Za novu kalkulaciju unesite 1,\n"
						+ ">>Za izvestaj o kalkulacijama unesite 2,\n"
						+ ">>Za prekid unesite bilo sta drugo.");
				porukaZaKlijenta.println("%%%");
			
				try {
					izbor = Integer.parseInt(porukaOdKlijenta.readLine());
				} catch (NumberFormatException e) {
					izbor = 0;
				} catch (IOException e) {
					izbor = 0;
				}
				
				if(izbor == 2 && sign == 3) {
					porukaZaKlijenta.println(">>Ova opcija je moguca samo za registrovane korisnike.");
				} else {
					break;
				}
			}
			
			iterator++;
			
			if (izbor == 2 && sign != 3) {
				posaljiIzvestaj(); //dodaj String kalkulacije
				break;
			} else if (izbor != 1)
				break;			
		}
		
		quit(1);
	}

//////////////////////////////////////////////////////////////////////////////////////////
	private double izracunaj(double a, char znak, double b) {
		switch (znak) {
		case '+':
			return a + b;
		case '-':
			return a - b;
		case '*':
			return a * b;
		case '/':
			if(b != 0)
				return a / b;
			else {
				porukaZaKlijenta.println("Ne moze se deliti nulom!");
				return 0;
			}
		}
		
		return 0;
	}

//////////////////////////////////////////////////////////////////////////////////////////
	private boolean proveraFormataJednacine(String jed) {
		String[] pom = jed.split(" ");
		boolean oznakaDuzine = false;
		boolean oznakaParsera = false;
		boolean oznakaOperacije = false;
		double br1 = 0;
		double br2 = 0;
		char znak = 'z';

		if (pom.length == 3)
			oznakaDuzine = true;
		else {
			return false;
		}

		try {
			br1 = Double.parseDouble(pom[0]);
			br2 = Double.parseDouble(pom[2]);
			znak = pom[1].charAt(0);

			oznakaParsera = true;
		} catch (Exception e) {
			return false;
		}

		if (znak == '+' || znak == '-' || znak == '*' || znak == '/')
			oznakaOperacije = true;
		else
			return false;

		return oznakaDuzine && oznakaParsera && oznakaOperacije;
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void posaljiIzvestaj() {
		// izmeni ime fajla u izvestaj, izbrisi sifru iz prvog reda i posalji onda, a
		// nakon
		// slanja vrati sve kako je bilo

	}
}
