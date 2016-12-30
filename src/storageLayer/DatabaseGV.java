package storageLayer;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import gestioneVendite.Annuncio;
import gestioneVendite.CondizioneLibro;
import gestioneVendite.DettagliAnnuncio;

public class DatabaseGV {
	
	private static String queryAddAnnuncio;
	private static String queryDettagliAnnuncio;
	private static String queryListAnnunciUtente;
	private static String queryDettagliAnnunci;
	private static String queryRicercaTitolo;
	private static String queryRicercaAutore;
	static ArrayList<DettagliAnnuncio>listDettagli;
	static ArrayList<Annuncio>listAnnunci;
	static ArrayList<Annuncio>listAnnunciTitolo;
	static ArrayList<Annuncio>listAnnunciAutore;
	
	/**
	 * @author Pasquale Settembre
	 * <b>Permette l'inserimento di un annuncio nel database</b>
	 * @param annuncio  annuncio che si vuole inserire
	 * @param dett      dettagliAnnunci 
	 * @return true		restituisce che l'annuncio � stato inserito correttamente
	 * @throws SQLException
	 */
	public static boolean addAnnuncio(Annuncio annuncio, DettagliAnnuncio dett) throws SQLException {
		Connection connection = null;
		PreparedStatement psAddAnnuncio= null;
		PreparedStatement psAddDettagliAnnuncio = null;
		int lastID=0;
		try {
			connection = Database.getConnection();
			psAddAnnuncio = connection.prepareStatement(queryAddAnnuncio, Statement.RETURN_GENERATED_KEYS);
			
			psAddAnnuncio.setString(1, annuncio.getTitolo());
			psAddAnnuncio.setString(2, annuncio.getAutore());
			psAddAnnuncio.setString(3, annuncio.getCorso());
			psAddAnnuncio.setString(4, annuncio.getProprietario());
			psAddAnnuncio.setString(5, annuncio.getCondizioneLibro().name());
			psAddAnnuncio.setDouble(6, annuncio.getPrezzo());
			System.out.println(psAddAnnuncio.toString());
			psAddAnnuncio.executeUpdate();
			
			ResultSet rs =psAddAnnuncio.getGeneratedKeys();
			if(rs.next()){
				lastID = rs.getInt(1);
				System.out.println("ID "+rs.getInt(1));
			}
			
			connection.commit();
			
			java.sql.Date sqlDate = new java.sql.Date(dett.getData().getTime());
			
			psAddDettagliAnnuncio = connection.prepareStatement(queryDettagliAnnuncio); 
			psAddDettagliAnnuncio.setInt(1, lastID);
			psAddDettagliAnnuncio.setString(2, dett.getEditore());
			psAddDettagliAnnuncio.setInt(3, dett.getAnno());
			psAddDettagliAnnuncio.setString(4, dett.getDescrizione());
			psAddDettagliAnnuncio.setDate(5, sqlDate);
			psAddDettagliAnnuncio.setString(6, dett.getFoto());
			System.out.println(psAddDettagliAnnuncio.toString());
			
			psAddDettagliAnnuncio.executeUpdate();
			
			connection.commit();
			
		} finally {
			try {
				if(psAddAnnuncio != null)
					psAddAnnuncio.close();
				if(psAddDettagliAnnuncio !=null)
					psAddDettagliAnnuncio.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
			 finally {
				Database.releaseConnection(connection);
			}
		}
		return true;
	}
	
	/**
	 * @author Pasquale Settembre
	 * <b>Permette di prendere dal database la lista degli annunci di un determinato utente</b>
	 * @param email dell'utente 
	 * @return    restituisce la lista degli annunci 
	 * @throws SQLException
	 */
	public static ArrayList<Annuncio>getListaAnnunciUtente(String email) throws SQLException{
		Connection connection = null;
		PreparedStatement psListAnnunciUtente= null;
		listAnnunci = new ArrayList();
		listDettagli = new ArrayList<>();
		try{
			connection = Database.getConnection();
			psListAnnunciUtente = connection.prepareStatement(queryListAnnunciUtente);
			
			psListAnnunciUtente.setString(1, email);
			ResultSet rs = psListAnnunciUtente.executeQuery();
			
			while(rs.next()){
				Annuncio ann = new Annuncio();
				ann.setTitolo(rs.getString("Titolo"));
				ann.setPrezzo(rs.getDouble("prezzo"));
				//Date data = rs.getDate("Data");
				//String foto = rs.getString("Foto");
				listAnnunci.add(ann);
				selectDettagliAnnuncio(rs.getInt("idAnnuncio"));
			}
		}
		finally {
			try {
				if(psListAnnunciUtente != null)
					psListAnnunciUtente.close();
				if(psListAnnunciUtente !=null)
					psListAnnunciUtente.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
			 finally {
				connection.close();
				Database.releaseConnection(connection);
			}
		}
		return listAnnunci;
	}
	
	
	public static void selectDettagliAnnuncio(int id) throws SQLException{
		Connection connection = null;
		PreparedStatement psListAnnunciUtente= null;
		
		try{
			connection = Database.getConnection();
			psListAnnunciUtente = connection.prepareStatement(queryDettagliAnnunci);
			
			psListAnnunciUtente.setInt(1, id);
			ResultSet rs = psListAnnunciUtente.executeQuery();
			
			while(rs.next()){
				DettagliAnnuncio dett = new DettagliAnnuncio();
				dett.setData(rs.getDate("Data"));
				dett.setFoto(rs.getString("Foto"));
				//Date data = rs.getDate("Data");
				//String foto = rs.getString("Foto");
				listDettagli.add(dett);	
			}
		}
		finally {
			try {
				if(psListAnnunciUtente != null)
					psListAnnunciUtente.close();
				if(psListAnnunciUtente !=null)
					psListAnnunciUtente.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
			 finally {
				Database.releaseConnection(connection);
			}
		}
	}
	
	public static ArrayList<DettagliAnnuncio>getListaDettagli(){
		return listDettagli;
	}
	
	/**
	 * @author Francesco Garofalo
	 * <b>Permette di cercare nel database la lista degli annunci con il titolo desiderato</b>
	 * @param titolo dell'annuncio 
	 * @return restituisce la lista degli annunci correlati al titolo inserito
	 * @throws SQLException
	 */
	public static ArrayList<Annuncio>getListaAnnunciRicercaTitolo(String titolo) throws SQLException{
		Connection connection = null;
		PreparedStatement psListAnnunciTitolo = null;
		listAnnunciTitolo = new ArrayList();
		try{
			connection = Database.getConnection();
			psListAnnunciTitolo = connection.prepareStatement(queryRicercaTitolo);
			
			psListAnnunciTitolo.setString(1, titolo);
			ResultSet rs = psListAnnunciTitolo.executeQuery();
			
			while(rs.next()){
				Annuncio ann = new Annuncio();
				ann.setTitolo(rs.getString("Titolo"));
				ann.setAutore(rs.getString("Autore"));
				ann.setCorso(rs.getString("Corso"));
				ann.setProprietario(rs.getString("Proprietario"));
				//CONDIZIONE LIBRO ENUM VA CONVERTITO IN STRING
				//ann.setCondizioneLibro(rs.getObject("Condizione"));
				ann.setPrezzo(rs.getDouble("prezzo"));
				
				listAnnunciTitolo.add(ann);
			}
		}
		finally {
			try {
				if(psListAnnunciTitolo != null)
					psListAnnunciTitolo.close();
				if(psListAnnunciTitolo !=null)
					psListAnnunciTitolo.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
			 finally {
				connection.close();
				Database.releaseConnection(connection);
			}
		}
		return listAnnunciTitolo;
	}
	
	/**
	 * @author Francesco Garofalo
	 * <b>Permette di cercare nel database la lista degli annunci con l'autore desiderato</b>
	 * @param autore del libro 
	 * @return restituisce la lista degli annunci correlati all'autore del libro inserito
	 * @throws SQLException
	 */
	public static ArrayList<Annuncio>getListaAnnunciRicercaAutore(String autore) throws SQLException{
		Connection connection = null;
		PreparedStatement psListAnnunciAutore = null;
		listAnnunciAutore = new ArrayList();
		try{
			connection = Database.getConnection();
			psListAnnunciAutore = connection.prepareStatement(queryRicercaAutore);
			
			psListAnnunciAutore.setString(1, autore);
			ResultSet rs = psListAnnunciAutore.executeQuery();
			
			while(rs.next()){
				Annuncio ann = new Annuncio();
				ann.setTitolo(rs.getString("Titolo"));
				ann.setAutore(rs.getString("Autore"));
				ann.setCorso(rs.getString("Corso"));
				ann.setProprietario(rs.getString("Proprietario"));
				//CONDIZIONE LIBRO ENUM VA CONVERTITO IN STRING
				//ann.setCondizioneLibro(rs.getObject("Condizione"));
				ann.setPrezzo(rs.getDouble("prezzo"));

				listAnnunciAutore.add(ann);
			}
		}
		finally {
			try {
				if(psListAnnunciAutore != null)
					psListAnnunciAutore.close();
				if(psListAnnunciAutore !=null)
					psListAnnunciAutore.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
			 finally {
				connection.close();
				Database.releaseConnection(connection);
			}
		}
		return listAnnunciAutore;
	}
	
	static {
		queryAddAnnuncio = "INSERT INTO redteam.annuncio (Titolo, Autore, Corso, Proprietario, CondizioneLibro,Prezzo) VALUES (?,?,?,?,?,?)";
		queryDettagliAnnuncio = "INSERT INTO redteam.dettagliannuncio (id, Editore, Anno, Descrizione, Data, Foto) VALUES (?,?,?,?,?,?)";
		queryDettagliAnnunci = "SELECT data,foto FROM dettagliannuncio WHERE id=?";
		queryListAnnunciUtente = "SELECT a.idAnnuncio,a.titolo,a.prezzo,det.data,det.foto from Annuncio as a, Dettagliannuncio as det where a.proprietario=? and a.idAnnuncio=det.id;";
		queryRicercaTitolo = "SELECT * FROM Annuncio WHERE titolo = ?";
		queryRicercaAutore = "SELECT * FROM Annuncio WHERE autore = ?";
	}
}
