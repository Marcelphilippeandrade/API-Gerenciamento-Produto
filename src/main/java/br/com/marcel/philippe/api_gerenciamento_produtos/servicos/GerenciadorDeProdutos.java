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
import com.google.appengine.api.datastore.Entity;
import br.com.marcel.philippe.api_gerenciamento_produtos.modelo.Produto;

@Path("/produtos")
public class GerenciadorDeProdutos {

	/**
	 * Método responsável por criar o produto
	 * 
	 * @param codigo
	 * @return Produto
	 */
	private Produto criarProduto(int codigo) {
		Produto produto = new Produto();
		produto.setProdutoID(Integer.toString(codigo));
		produto.setCodigo(codigo);
		produto.setModelo("Modelo " + codigo);
		produto.setNome("Nome " + codigo);
		produto.setPreco(10 * codigo);
		return produto;
	}

	/**
	 * Método responsável por exibir determinado produto
	 * 
	 * @param codigo
	 * @return Produto
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public Produto getProduto(@PathParam("codigo") int codigo) {
		return criarProduto(codigo);
	}

	/**
	 * Método responsável por exibir a lista de produtos cadastrados
	 * 
	 * @return List<Produto>
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Produto> getProdutos() {
		List<Produto> produtos = new ArrayList<>();
		for (int j = 1; j <= 5; j++) {
			produtos.add(criarProduto(j));
		}
		return produtos;
	}

	/**
	 * Método responsável por salvar um produto
	 * 
	 * @param produto
	 * @return Produto
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Produto savarProduto(Produto produto) {
		produto.setProdutoID(Integer.toString(produto.getCodigo()));
		return produto;
	}

	/**
	 * Método responsável por deletar um produto
	 * 
	 * @param codigo
	 * @return
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public String deletarProduto(@PathParam("codigo") int codigo) {
		return "Produto " + codigo + " deletado";
	}

	/**
	 * Método resposável por alterar um produto já cadastrado
	 * 
	 * @param code
	 * @param produto
	 * @return Produto
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public Produto alterProduto(@PathParam("codigo") int code, Produto produto) {
		produto.setNome("Nome alterado");
		return produto;
	}

	/**
	 * Método responsável por converter um objeto do tipo Produto em uma Entidade do
	 * Banco Google Cloud Datastore
	 * 
	 * @param produto
	 * @param entidadeProduto
	 */
	private void TransformarProdutoParaEntidade(Produto produto, Entity entidadeProduto) {
		entidadeProduto.setProperty("ProdutoID", produto.getProdutoID());
		entidadeProduto.setProperty("Nome", produto.getNome());
		entidadeProduto.setProperty("Codigo", produto.getCodigo());
		entidadeProduto.setProperty("Modelo", produto.getModelo());
		entidadeProduto.setProperty("Preco", produto.getPreco());
	}

	/**
	 * Método responsável por converter uma Entidade do Banco Google Cloud Datastore
	 * em um objeto do tipo Produto
	 * 
	 * @param entidadeProduto
	 * @return Produto
	 */
	private Produto TransformarEntidadeParaProduto(Entity entidadeProduto) {
		Produto produto = new Produto();
		produto.setId(entidadeProduto.getKey().getId());
		produto.setProdutoID((String) entidadeProduto.getProperty("ProdutoID"));
		produto.setNome((String) entidadeProduto.getProperty("Nome"));
		produto.setCodigo(Integer.parseInt(entidadeProduto.getProperty("Codigo").toString()));
		produto.setModelo((String) entidadeProduto.getProperty("Modelo"));
		produto.setPreco(Float.parseFloat(entidadeProduto.getProperty("Preco").toString()));
		return produto;
	}
}
