package Sistema.jdbc;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Boletos {

	private String descricao;
	private double valor;
	private Date vencimento;
	private int indice;
	

	public Boletos() {
		// TODO Auto-generated constructor stub
	}

	

	public Boletos(int indice, String descricao, double valor, Date vencimento) {
		super();
		this.indice= indice;
		this.descricao = descricao;
		this.valor = valor;
		this.vencimento = vencimento;
	}



	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public String getVencimento()  {
		
		   String dataString = "dd/MM";

		   SimpleDateFormat spd = new SimpleDateFormat(dataString);
		   
		   
		return spd.format(vencimento);

		
//		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		
		
		this.vencimento = vencimento;
	}



	public int getIndice() {
		return indice;
	}



	public void setIndice(int indice) {
		this.indice = indice;
	}

}
