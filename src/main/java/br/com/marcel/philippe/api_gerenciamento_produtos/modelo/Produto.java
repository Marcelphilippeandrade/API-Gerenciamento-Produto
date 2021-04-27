package br.com.marcel.philippe.api_gerenciamento_produtos.modelo;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Produto implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	
	@NotNull(message = "Nome não pode ser vazio!")
	@Size(min = 3, message = "Nome deve ter no mínimo 3 letras!")
	private String nome;
	
	@NotNull(message = "Modelo não pode ser vazio!")
	private String modelo;
	
	@NotNull(message = "Código não pode ser vazio!")
	private int codigo;
	
	@NotNull(message = "Preço não pode ser vazio!")
	private float preco;
	
	private int quantidade;

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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(int quantidade) {
		this.quantidade = quantidade;
	}
}
