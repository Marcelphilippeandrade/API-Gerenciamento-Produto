package br.com.marcel.philippe.api_gerenciamento_produtos.servicos;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.com.marcel.philippe.api_gerenciamento_produtos.modelo.Produto;

@Path("/produtos")
public class GerenciadorDeProdutos {

	private Produto criarProduto(int codigo) {
		Produto produto = new Produto();
		produto.setProdutoID(Integer.toString(codigo));
		produto.setCodigo(codigo);
		produto.setModelo("Modelo " + codigo);
		produto.setNome("Nome " + codigo);
		produto.setPreco(10 * codigo);
		return produto;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public Produto getProduto(@PathParam("codigo") int codigo) {
		return criarProduto(codigo);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Produto> getProdutos() {
		List<Produto> produtos = new ArrayList<>();
		for (int j = 1; j <= 5; j++) {
			produtos.add(criarProduto(j));
		}
		return produtos;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Produto savarProduto(Produto produto) {
		produto.setProdutoID(Integer.toString(produto.getCodigo()));
		return produto;
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public String deletarProduto(@PathParam("codigo") int codigo) {
		return "Produto " + codigo + " deletado";
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public Produto alterProduto(@PathParam("codigo") int code, Produto produto) {
		produto.setNome("Nome alterado");
		return produto;
	}
}
