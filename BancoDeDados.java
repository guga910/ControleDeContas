package Sistema.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class BancoDeDados {

	public static Connection getConexao() {

		final String url = "jdbc:mysql://localhost:3306?verifyServerCertificate=false&useSSL=true&useTimezone=true&serverTimezone=UTC";
		final String usuario = "root";
		final String senha = "nn8ft85f";
		final String drive = "com.mysql.jdbc.Driver";

		try {
			Class.forName(drive);
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
		Connection conexao = null;
		try {
			conexao = DriverManager.getConnection(url, usuario, senha);
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return conexao;

	}

	public void criarDbeTabela(String usuario) throws SQLException {
//		BancoDeDados cnx = new BancoDeDados();

		String criarDBC = "CREATE DATABASE IF NOT EXISTS controle_de_contas";
		Statement stmt = BancoDeDados.getConexao().createStatement();
		stmt.execute(criarDBC);
		stmt.close();

		String nomeBD = "CDC_" + usuario;
		String criarTabelas = "create table IF NOT EXISTS controle_de_contas." + nomeBD
				+ "(id INT AUTO_INCREMENT PRIMARY KEY," + " descricao VARCHAR(45) NOT NULL," + " valor DOUBLE,"
				+ " vencimento DATE NOT NULL," + "Paga TINYINT UNSIGNED NULL)";

		PreparedStatement pstm = BancoDeDados.getConexao().prepareStatement(criarTabelas);
		pstm.execute(criarTabelas);
		pstm.close();

	}

	@SuppressWarnings("deprecation")
	public void adicionarContas(String usuario, String descricao, double valor, int dia) throws SQLException {

//		BancoDeDados cnx = new BancoDeDados();
		criarDbeTabela(usuario);
		String nomeBD = "CDC_" + usuario;

		String inserirDados = "INSERT INTO  controle_de_contas." + nomeBD
				+ " (descricao, valor,Vencimento,Paga ) SELECT  ?,?,?,?" + " FROM DUAL"
				+ " WHERE NOT EXISTS( SELECT descricao, Vencimento  FROM controle_de_contas." + nomeBD
				+ " WHERE Vencimento = ? and descricao= ?)";

		GregorianCalendar g = new GregorianCalendar();
		int mesAtual = g.get(GregorianCalendar.MONTH);
		int anoAtual = g.get(GregorianCalendar.YEAR) - 1900;

		PreparedStatement pstm2 = getConexao().prepareStatement(inserirDados);
		pstm2.setString(1, descricao);
		pstm2.setDouble(2, valor);
		pstm2.setDate(3, new Date(anoAtual, mesAtual, dia));
		pstm2.setBoolean(4, false);
		pstm2.setDate(5, new Date(anoAtual, mesAtual, dia));
		pstm2.setString(6, descricao);

		pstm2.execute();
		pstm2.close();

	}

	@SuppressWarnings("deprecation")
	public void adicionarContas(String usuario, String descricao, double valor, int dia, int recorrencia)
			throws SQLException {

		criarDbeTabela(usuario);
		String nomeBD = "CDC_" + usuario;

		String inserirDados = "INSERT INTO  controle_de_contas." + nomeBD
				+ " (descricao, valor,Vencimento,Paga ) SELECT  ?,?,?,?" + " FROM DUAL"
				+ " WHERE NOT EXISTS( SELECT descricao, Vencimento  FROM controle_de_contas." + nomeBD
				+ " WHERE Vencimento = ? and descricao= ?)";

		GregorianCalendar g = new GregorianCalendar();
		int mesAtual = g.get(GregorianCalendar.MONTH);
		int anoAtual = g.get(GregorianCalendar.YEAR) - 1900;

		PreparedStatement pstm2 = BancoDeDados.getConexao().prepareStatement(inserirDados);

		for (int i = 0; i <= recorrencia - 1; i++) {

			pstm2.setString(1, descricao);
			pstm2.setDouble(2, valor);
			pstm2.setDate(3, new Date(anoAtual, mesAtual + i, dia));
			pstm2.setBoolean(4, false);
			pstm2.setDate(5, new Date(anoAtual, mesAtual + i, dia));
			pstm2.setString(6, descricao);

			pstm2.execute();
		}
		pstm2.close();

	}

	public void mostrarContasMes(String usuario, int mes) throws SQLException, ParseException {

		criarDbeTabela(usuario);
		Date data;
		LocalDate hoje = LocalDate.now();
		LocalDate hoje2 = LocalDate.of(hoje.getYear(), mes, 1);
		LocalDate amanha = LocalDate.of(hoje.getYear(), mes, hoje.lengthOfMonth());

		System.out.println("Contas do mes " + hoje2.getMonth() + " a pagar:");

		String nomeBD = "CDC_" + usuario;

		String consultaMes = "SELECT id,descricao, valor, vencimento from controle_de_contas." + nomeBD
				+ " where vencimento" + "  between '" + hoje2 + "'  AND  '" + amanha + "' ";

		System.out.println();

		Statement stmt = BancoDeDados.getConexao().createStatement();
		ResultSet resultado = stmt.executeQuery(consultaMes);

		List<Boletos> boletos = new ArrayList<>();

		while (resultado.next()) {

			int valor = resultado.getInt("valor");
			String descricao = resultado.getString("descricao");
			int indice = resultado.getInt("id");

			while (descricao.length() < 10) {
				descricao += " ";
			}

			data = resultado.getDate("vencimento");

			boletos.add(new Boletos(indice, descricao, valor, data));

		}

		for (Boletos b : boletos) {

			Double v = b.getValor();
			String v2 = v.toString();
			while (v2.length() < 7) {
				v2 += " ";
			}

			System.out.println("Id: " + b.getIndice() + " Descri: " + b.getDescricao() + ": " + v2 + ", venc: "
					+ b.getVencimento());

		}
		System.out.println(boletos.get(1).getIndice());
		stmt.close();
		resultado.close();

	}

	public void mostrarContasMes(String usuario) throws SQLException {

		criarDbeTabela(usuario);
		Date data;
		LocalDate hoje = LocalDate.now();
		LocalDate hoje2 = LocalDate.of(hoje.getYear(), hoje.getMonthValue(), 1);
		LocalDate amanha = LocalDate.of(hoje.getYear(), hoje.getMonthValue(), 30);

		System.out.println("Contas a serem pagas em " + hoje2.getMonth()+":");

		String nomeBD = "CDC_" + usuario;

		String consultaMes = "SELECT id, descricao, valor, vencimento from controle_de_contas." + nomeBD
				+ " where vencimento" + "  between CAST('" + hoje2 + "' AS DATE) AND CAST( '" + amanha
				+ "' AS DATE) order by vencimento ";

		System.out.println();

		Statement stmt = BancoDeDados.getConexao().createStatement();
		ResultSet resultado = stmt.executeQuery(consultaMes);

		List<Boletos> boletos = new ArrayList<>();

		while (resultado.next()) {

			int valor = resultado.getInt("valor");
			String descricao = resultado.getString("descricao");
			int indice = resultado.getInt("id");

			while (descricao.length() < 10) {
				descricao += " ";
			}

			data = resultado.getDate("vencimento");

			boletos.add(new Boletos(indice, descricao, valor, data));

		}

		for (int i = 0; i < boletos.size(); i++) {

			int i2 = i + 1;
			System.out.println("Id: " + 0 + i2 + " Descri: " + boletos.get(i).getDescricao() + ": "
					+ boletos.get(i).getValor() + ", venc: " + boletos.get(i).getVencimento());

		}
		stmt.close();
		resultado.close();

	}

	public void mostrarContasPagas(String usuario) throws SQLException {

		criarDbeTabela(usuario);
		Date data;
		LocalDate hoje = LocalDate.now();
		LocalDate hoje2 = LocalDate.of(hoje.getYear(), hoje.getMonthValue(), 1);

		System.out.println("Contas do mes " + hoje2.getMonth() + " a pagar:");

		String nomeBD = "CDC_" + usuario;

		String consultaMes = "SELECT id, descricao, valor, vencimento from controle_de_contas." + nomeBD
				+ " where paga =1";

		System.out.println();

		Statement stmt = getConexao().createStatement();
		ResultSet resultado = stmt.executeQuery(consultaMes);

		List<Boletos> boletos = new ArrayList<>();

		while (resultado.next()) {

			int valor = resultado.getInt("valor");
			String descricao = resultado.getString("descricao");
			int indice = resultado.getInt("id");

			while (descricao.length() < 10) {
				descricao += " ";
			}

			data = resultado.getDate("vencimento");

			boletos.add(new Boletos(indice, descricao, valor, data));

		}
		for (Boletos b : boletos) {

			Double v = b.getValor();
			String v2 = v.toString();
			while (v2.length() < 7) {
				v2 += " ";
			}

			System.out.println("Id: " + b.getIndice() + " Descri: " + b.getDescricao() + ": " + v2 + ", venc: "
					+ b.getVencimento());

		}
		stmt.close();
		resultado.close();

	}

	public void mostrarContasAPagar(String usuario) throws SQLException {

		criarDbeTabela(usuario);
		Date data;
		LocalDate hoje = LocalDate.now();
		LocalDate hoje2 = LocalDate.of(hoje.getYear(), hoje.getMonthValue(), 1);
		LocalDate amanha = LocalDate.of(hoje.getYear(), hoje.getMonthValue(), hoje.lengthOfMonth());

		
		
		System.out.println("Contas a Pagar do mes de " + hoje2.getMonth() + ": ");

		String nomeBD = "CDC_" + usuario;

		String consultaMes = "SELECT id, descricao, valor, vencimento from controle_de_contas." + nomeBD
				+ " where paga =0 and vencimento between CAST('" + hoje2 + "' AS DATE) AND CAST( '" + amanha+
				"' AS DATE);";
//System.out.println("hoje: "+hoje);
//System.out.println("hoje2: "+hoje2);
//System.out.println("amanha: "+amanha);

		System.out.println();

		Statement stmt = BancoDeDados.getConexao().createStatement();
		ResultSet resultado = stmt.executeQuery(consultaMes);

		List<Boletos> boletos = new ArrayList<>();

		while (resultado.next()) {

			int valor = resultado.getInt("valor");
			String descricao = resultado.getString("descricao");
			int indice = resultado.getInt("id");

			while (descricao.length() < 10) {
				descricao += " ";
			}

			data = resultado.getDate("vencimento");

			boletos.add(new Boletos(indice, descricao, valor, data));

		}
		
		for (int i = 0; i < boletos.size(); i++) {

			int i2 = i + 1;
			System.out.println("Id: " + 0 + i2 + " Descri: " + boletos.get(i).getDescricao() + ": "
					+ boletos.get(i).getValor() + ", venc: " + boletos.get(i).getVencimento());

		}
		stmt.close();
		resultado.close();

	}

	public void pagarContas(String usuario, int id) throws SQLException {

		Date data;
		LocalDate hoje = LocalDate.now();
		LocalDate hoje2 = LocalDate.of(hoje.getYear(), hoje.getMonthValue(), 1);
		LocalDate amanha = LocalDate.of(hoje.getYear(), hoje.getMonthValue(), 30);


		String nomeBD = "CDC_" + usuario;

		String consultaMes = "SELECT id, descricao, valor, vencimento from controle_de_contas." + nomeBD
				+ " where paga =0 and vencimento between CAST('" + hoje2 + "' AS DATE) AND CAST( '" + amanha+
				"' AS DATE);";


		Statement stmt = BancoDeDados.getConexao().createStatement();
		ResultSet resultado = stmt.executeQuery(consultaMes);

		List<Boletos> boletos = new ArrayList<>();

		while (resultado.next()) {

			int valor = resultado.getInt("valor");
			String descricao = resultado.getString("descricao");
			int indice = resultado.getInt("id");

			while (descricao.length() < 10) {
				descricao += " ";
			}

			data = resultado.getDate("vencimento");

			boletos.add(new Boletos(indice, descricao, valor, data));

		}

		stmt.close();

		
		criarDbeTabela(usuario);

		String update = "UPDATE controle_de_contas." + nomeBD + " SET Paga =1 WHERE (id= ?)";
		PreparedStatement stmt1 = getConexao().prepareStatement(update);
		
		int id2= boletos.get(id-1).getIndice();
		
		stmt1.setInt(1,id2);
		stmt1.execute();
		stmt1.close();

	}

	public void excluirContas(String usuario, int id) throws SQLException {

		criarDbeTabela(usuario);
		String nomeBD = "CDC_" + usuario;

		String update = "DELETE FROM controle_de_contas." + nomeBD + " WHERE (id= ?)";
		PreparedStatement stmt = getConexao().prepareStatement(update);
		stmt.setInt(1, id);
		stmt.execute();
		stmt.close();

	}
}

