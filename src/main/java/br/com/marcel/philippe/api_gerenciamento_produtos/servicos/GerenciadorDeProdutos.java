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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

import br.com.marcel.philippe.api_gerenciamento_produtos.modelo.Produto;

@Path("/produtos")
public class GerenciadorDeProdutos {

	/**
	 * Método responsável por exibir determinado produto
	 * 
	 * @param codigo
	 * @return Produto
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public Response getProduto(@PathParam("codigo") int codigo) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, codigo);
		Query query = new Query("Produtos").setFilter(codigoFilter);

		Entity entidadeProduto = datastore.prepare(query).asSingleEntity();

		Produto produto = null;

		if (entidadeProduto != null) {
			produto = TransformarEntidadeParaProduto(entidadeProduto);
			return Response.ok(produto, MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.NOT_FOUND).entity("Produto não encontrado!").build();
		}
	}

	/**
	 * Método responsável por exibir a lista de produtos cadastrados
	 * 
	 * @return List<Produto>
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProdutos() {
		List<Produto> produtos = new ArrayList<>();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query query;
		query = new Query("Produtos").addSort("Codigo", SortDirection.ASCENDING);

		List<Entity> entidadesProdutos = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		if (!entidadesProdutos.isEmpty()) {
			for (Entity entidadeProduto : entidadesProdutos) {
				Produto produto = TransformarEntidadeParaProduto(entidadeProduto);
				produtos.add(produto);
			}
			return Response.ok().entity(produtos).build();
		} else {
			return Response.status(Status.NOT_FOUND).entity("Lista vazia!").build();
		}
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
	public Response savarProduto(Produto produto) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, produto.getCodigo());
		Query query = new Query("Produtos").setFilter(codigoFilter);

		Entity entidadeProduto = datastore.prepare(query).asSingleEntity();
		
		if (entidadeProduto != null) {
			return Response.status(Status.NOT_FOUND).entity("Produto já cadastrado!").build();
		} else {
			Key chaveProduto = KeyFactory.createKey("Produtos", "chaveProduto");
			entidadeProduto = new Entity("Produtos", chaveProduto);
			TransformarProdutoParaEntidade(produto, entidadeProduto);
			datastore.put(entidadeProduto);
			produto.setId(entidadeProduto.getKey().getId());
			return Response.ok(produto, MediaType.APPLICATION_JSON).build();
		}
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
	public Response deletarProduto(@PathParam("codigo") int codigo) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, codigo);
		Query query = new Query("Produtos").setFilter(codigoFilter);

		Entity entidadeProduto = datastore.prepare(query).asSingleEntity();
		if (entidadeProduto != null) {
			datastore.delete(entidadeProduto.getKey());
			Produto produto = TransformarEntidadeParaProduto(entidadeProduto);
			return Response.ok(produto, MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.NOT_FOUND).entity("Produto não encontrado!").build();
		}
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
	public Response alterProduto(@PathParam("codigo") int codigo, Produto produto) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, codigo);

		Query query = new Query("Produtos").setFilter(codigoFilter);
		Entity entidadeProduto = datastore.prepare(query).asSingleEntity();

		if (entidadeProduto != null) {
			TransformarProdutoParaEntidade(produto, entidadeProduto);
			datastore.put(entidadeProduto);
			produto.setId(entidadeProduto.getKey().getId());
			return Response.ok(produto, MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.NOT_FOUND).entity("Produto não encontrado!").build();
		}
	}

	/**
	 * Método responsável por converter um objeto do tipo Produto em uma Entidade do
	 * Banco Google Cloud Datastore
	 * 
	 * @param produto
	 * @param entidadeProduto
	 */
	private void TransformarProdutoParaEntidade(Produto produto, Entity entidadeProduto) {
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
		produto.setNome((String) entidadeProduto.getProperty("Nome"));
		produto.setCodigo(Integer.parseInt(entidadeProduto.getProperty("Codigo").toString()));
		produto.setModelo((String) entidadeProduto.getProperty("Modelo"));
		produto.setPreco(Float.parseFloat(entidadeProduto.getProperty("Preco").toString()));
		return produto;
	}
}
