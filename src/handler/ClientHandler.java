package handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ClientHandler extends Thread {

	BufferedReader porukaOdKlijenta = null;
	PrintStream porukaZaKlijenta = null;
	Socket soketZaKomunikaciju = null;
	public String username;
	public String password;
	public int sign = -1; // vrsta korisnika

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
				System.out.println("SOCKET EXCEPTION");
				e.printStackTrace();
			}
		}
	}

	private void komuniciraj() throws SocketException {

		try {
			porukaOdKlijenta = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			porukaZaKlijenta = new PrintStream(soketZaKomunikaciju.getOutputStream());

			porukaZaKlijenta.println(">>Dobrodosli, uspesno ste se povezali na server!");
//			porukaZaKlijenta.println("%%%");  // oznaka da klijent prestane sa slusanjem

			prikaziMeni();

			porukaZaKlijenta.println(">>Zdravo " + username + ", uspesno ste se povezali na server!");

			kalkulator(sign);

		} catch (IOException e) {
			if(e instanceof SocketException)
				quit(2);
			else
				System.out.println(e.getMessage());
		}

	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void prikaziMeni() throws SocketException {
		porukaZaKlijenta.println(">>Izaberite jednu od sledecih opcija:");
		porukaZaKlijenta.println(">>1 Prijavljivanje\n>>2 Registracija\n>>3 Gost\n>>4 Izvestaj kalkulacija\n>>5 Izlaz\n");
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
				if(e instanceof SocketException)
					quit(2);

				izbor = -1;
			}

			if (izbor >= 1 && izbor <= 5)
				provera = true;
			else {
				porukaZaKlijenta.println(">>Pogresili ste, pokusajte ponovo!");
				porukaZaKlijenta.println("%%%");
			}

		} while (!provera);

		if (sign == -1)
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
			login();
			posaljiIzvestaj();
			break;
		case 5:
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
					if(e instanceof SocketException)
						quit(2);
					else
						porukaZaKlijenta.println(">>Neispravno korisnicko ime ili lozinka, pokusajte ponovo.");
				}
				
				int izbor = 0;
				if(!checker) {
					porukaZaKlijenta.println(">>Ako zelite da se registrujete unesite 1.");
					porukaZaKlijenta.println(">>Za ponovni pokusaj unesite bilo sta drugo, za izlaz *quit .");
					porukaZaKlijenta.println("%%%");
					
					try {
						izbor = Integer.parseInt(porukaOdKlijenta.readLine());
					} catch (NumberFormatException e) {	
						
					}					
				}
				
				if(izbor == 1) {
					registration();
					return;
				}
				
			} while (!checker);
		} catch (IOException e) {
			if(e instanceof SocketException)
				quit(2);
			else
				e.printStackTrace();
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void registration() throws SocketException {
		String putanjaDoFajla = null;
		try {
			boolean checker = false;
			do {
				porukaZaKlijenta.println(">>Unesite vase korisnicko ime:");
				porukaZaKlijenta.println("%%%");
				username = porukaOdKlijenta.readLine();
				
				putanjaDoFajla = postojiUsername(username);

				if (putanjaDoFajla == null) {
					porukaZaKlijenta.println(">>Postoji korisnik sa tim korisnickim imenom! Pokusajte ponovo.");
					continue;
				}
			
				porukaZaKlijenta.println(
						">>Unesite vasu lozinku:\n>>Napomena:" + " lozinka se mora sastojati iz minimum 8 karaktera, "
								+ "minimum jednog velikog slova" + "(A-Z) i minimum jednog broja (0-9)");
				porukaZaKlijenta.println("%%%");

				password = porukaOdKlijenta.readLine();

				checker = checkPassword(password);

				if (!checker)
					porukaZaKlijenta.println(">>Neispravna lozinka! Pokusajte ponovo.");

			} while (!checker);

			PrintWriter writer = new PrintWriter(putanjaDoFajla, "UTF-8");
			writer.println(password);
			writer.close();

			porukaZaKlijenta.println(">>Uspesno ste se registrovali!\n");

		} catch (IOException e) {
			if(e instanceof SocketException)
				quit(2);
			else
				e.printStackTrace();
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////	
	private boolean checkPassword(String pw) throws SocketException {
	    char ch;
	    boolean velikoSlovo = false;
	    boolean broj = false;
	    
	    if (pw.length() < 8)
	    	return false;
	    
	    for(int i=0; i < pw.length(); i++) {
	        ch = pw.charAt(i);
	        
	        if(Character.isDigit(ch)) {
	        	broj = true;
	        }
	        else if(Character.isUpperCase(ch)) {
	        	velikoSlovo = true;
	        } 
	        
	        if(broj && velikoSlovo)
	            return true;
	    }
	    
	    return false;
	}
//////////////////////////////////////////////////////////////////////////////////////////
	private String postojiUsername(String u) throws SocketException {
		if (u == null || u.length() < 3) {
			porukaZaKlijenta.println(">>Korisnicko ime se mora sastojati od najmanje 3 karaktera!");
			return null;
		}
		
		File file = new File("Korisnici\\" + u + ".txt");

		try {
			if (file.createNewFile()) {
				return file.getAbsolutePath();
			} else {
				return null;
			}
		} catch (IOException e) {
			if(e instanceof SocketException)
				quit(2);
			else
				e.printStackTrace();
		}

		return null;
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void guest() throws SocketException {
		Random rand = new Random();
		int n = rand.nextInt(9999999) + 1000000;
		username = "Gost_" + n;
	}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void quit(int exitCode) throws SocketException { // exitCode == 2 oznacava da je pukla konekcija
		
		if (exitCode == 2) {
			try {
				
				soketZaKomunikaciju.close();

			} catch (IOException e) {
				System.out.println("Greska u funckiji quit() pri zatvaranju soketa za komunikaciju sa klijentom.");
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
				break;
			}

			if (iterator != 0 && sign == 3) {
				porukaZaKlijenta.println(">>Broj preostalih kalkulacija: " + (ogranicenje - iterator) + ".");
			}

			porukaZaKlijenta.println("\n>>Unesite zeljenu jednacinu u sledecem formatu: broj1 znak_operacije broj2\n");
			porukaZaKlijenta.println("%%%");
			try {
				jednacina = porukaOdKlijenta.readLine();
			} catch (IOException e) {
				if(e instanceof SocketException)
					quit(2);
				else
					e.printStackTrace();
			}

			while (!proveraFormataJednacine(jednacina)) {
				porukaZaKlijenta.println("\n>>Format zahteva nije odgovarajuci, pokusajte ponovo!");
				porukaZaKlijenta.println("\n>>Unesite zeljenu jednacinu u sledecem formatu: broj1 znak_operacije broj2\n");
				porukaZaKlijenta.println("%%%");
				try {
					jednacina = porukaOdKlijenta.readLine();
				} catch (IOException e) {
					if(e instanceof SocketException)
						quit(2);				
				}
			}

			String[] pom = jednacina.split(" ");

			br1 = Double.parseDouble(pom[0]);
			br2 = Double.parseDouble(pom[2]);
			znak = pom[1].charAt(0);
			
			rezultat = izracunaj(br1, znak, br2);
			
			porukaZaKlijenta.println(">>Rezultat je: " + rezultat);
			
			Date datum = new Date();
			String vreme = new SimpleDateFormat("HH:mm:ss").format(datum);
			
			kalkulacije += "\n" + vreme + " " + br1 + " " + znak + " " + br2 + " = " + rezultat;
			
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
					if(e instanceof SocketException)
						quit(2);
					
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
				upisiKalkulacije(kalkulacije);
				posaljiIzvestaj(); 
				break;
			} else if (izbor != 1)
				break;			
		}
		
		if(sign != 3 && izbor != 2) {
			upisiKalkulacije(kalkulacije);
		}
		
		quit(1);
	}

//////////////////////////////////////////////////////////////////////////////////////////
	private double izracunaj(double a, char znak, double b) throws SocketException {
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
	private boolean proveraFormataJednacine(String jed) throws SocketException {
		if(jed == null)
			return false;
		
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
		} catch (NumberFormatException e) {
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
	private void upisiKalkulacije(String kalkulacije) throws SocketException {		
		String putanjaDoFajla = "Korisnici\\"+username+".txt";
		String[] pom = kalkulacije.split("\n");
		int d = pom.length;
		
		try {
			PrintWriter	writer = new PrintWriter(new BufferedWriter(new FileWriter(putanjaDoFajla, true)));
			
			for(int i = 0; i < d; i++) {
				if (!pom[i].equals("") && !pom[i].equals(" ") && !(pom[i] == null))
					writer.println(pom[i]);
			}
			
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			if(e instanceof SocketException)
				quit(2);
			else
				e.printStackTrace();
		}		
	}
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	private void posaljiIzvestaj() throws SocketException {
		String putanjaDoFajla = pripremiFajlZaSlanje();
		
		if(putanjaDoFajla == null)
			porukaZaKlijenta.println(">>Doslo je do greske prilikom pravljenja izvestaja.");
		
		try {
			File f = new File(putanjaDoFajla);
			OutputStream strimZaSlanje = soketZaKomunikaciju.getOutputStream();		
			
			byte[] buffer = new byte[(int) f.length()];
			RandomAccessFile raf = new RandomAccessFile(putanjaDoFajla, "r");
			
			porukaZaKlijenta.println(">>Fajl sa kalkulacijama se salje...");
			porukaZaKlijenta.println(f.length());
			porukaZaKlijenta.println(username);
			
			int n = raf.read(buffer);
			
			System.out.println(porukaOdKlijenta.readLine()); //potvrda da je slanje prihvaceno		
			
			strimZaSlanje.write(buffer, 0, n);
			
			System.out.println(porukaOdKlijenta.readLine()); //potvrda da je fajl preuzet
			
			porukaZaKlijenta.println(">>Fajl je uspesno poslat!");
			
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			if (e instanceof SocketException)
				quit(2);
			else
				e.printStackTrace();
		}

		
	}
//////////////////////////////////////////////////////////////////////////////////////////
	private String pripremiFajlZaSlanje() throws SocketException {
		String putanjaDoFajla = "Korisnici\\" + username + ".txt";
		String tekstFajla = "";
		String pom = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(putanjaDoFajla));
			while(true) {
				pom = reader.readLine();
				
				if(pom == null)
					break;
				
				tekstFajla += pom + "\n";
			}
			reader.close();
			
			File file = new File("Za slanje\\" + username + ".txt");
			file.getParentFile().mkdir();
			file.createNewFile();
			
			PrintWriter	writer = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath())));
			String[] pom2 = tekstFajla.split("\n");
			
			writer.println("Izvestaj o kalkulacijama za korisnika "+username+":");
			
			for(int i = 1; i < pom2.length; i++) // da bih preskocio red sa lozinkom
				writer.println(pom2[i]);
				
			writer.close();
			
			return file.getAbsolutePath();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			if(e instanceof SocketException)
				quit(2);
			else
				e.printStackTrace();
		}
		
		return null;
	}
}
