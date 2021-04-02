package br.com.marcel.philippe.api_gerenciamento_produtos.modelo;

import java.io.Serializable;

public class Produto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String produtoID;
	private String nome;
	private String modelo;
	private int codigo;
	private float preco;

	public String getProdutoID() {
		return produtoID;
	}

	public void setProdutoID(String produtoID) {
		this.produtoID = produtoID;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public float getPreco() {
		return preco;
	}

	public void setPreco(float preco) {
		this.preco = preco;
	}
}
