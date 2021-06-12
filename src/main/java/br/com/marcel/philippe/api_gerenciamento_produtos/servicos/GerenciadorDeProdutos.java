package br.com.marcel.philippe.api_gerenciamento_produtos.servicos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
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

	private static final Logger log = Logger.getLogger("ProductManager");

	/**
	 * Método responsável por exibir determinado produto
	 * 
	 * @param codigo
	 * @return Response
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	@PermitAll
	public Response getProduto(@PathParam("codigo") int codigo) {

		log.fine("Pesquisando produto de codigo=[" + codigo + "]");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, codigo);
		Query query = new Query("Produtos").setFilter(codigoFilter);

		if (ExisteProduto(datastore, query)) {
			Entity entidadeProduto = EntidadeProduto(datastore, query).get();
			Produto produto = TransformarEntidadeParaProduto(entidadeProduto);
			log.info("Produto de codigo=[" + codigo + "] Pesquisado com sucesso");
			return Response.ok(produto, MediaType.APPLICATION_JSON).build();
		} else {
			log.severe("Produto de codigo=[" + codigo + "]. Produto nao encontrado!");
			return Response.status(Status.NOT_FOUND).entity("Produto não encontrado!").build();
		}
	}

	/**
	 * Método responsável por exibir a lista de produtos cadastrados
	 * 
	 * @return Response
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getProdutos() {
		
		log.fine("Pesquisando todos os produtos");
		
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
	 * @return Response
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response savarProduto(@Valid Produto produto) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, produto.getCodigo());
		Query query = new Query("Produtos").setFilter(codigoFilter);

		if (ExisteProduto(datastore, query)) {
			return Response.status(Status.NOT_FOUND).entity("Produto já cadastrado!").build();
		} else {
			Key chaveProduto = KeyFactory.createKey("Produtos", "chaveProduto");
			Entity entidadeProduto = new Entity("Produtos", chaveProduto);
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
	 * @return Response
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public Response deletarProduto(@PathParam("codigo") int codigo) {

		log.fine("Deletando o produto de codigo=[" + codigo + "]");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, codigo);
		Query query = new Query("Produtos").setFilter(codigoFilter);

		Optional<Entity> entidadeProduto = EntidadeProduto(datastore, query);

		if (entidadeProduto.isPresent()) {
			datastore.delete(entidadeProduto.get().getKey());
			Produto produto = TransformarEntidadeParaProduto(entidadeProduto.get());
			log.info("Produto de codigo=[" + codigo + "] apagado com sucesso");
			return Response.ok(produto, MediaType.APPLICATION_JSON).build();
		} else {
			log.severe("Erro ao apagar produto de codigo=[" + codigo + "]. Produto nao encontrado!");
			return Response.status(Status.NOT_FOUND).entity("Produto não encontrado!").build();
		}
	}

	/**
	 * Método resposável por alterar um produto já cadastrado
	 * 
	 * @param code
	 * @param produto
	 * @return Response
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{codigo}")
	public Response alterProduto(@PathParam("codigo") int codigo, @Valid Produto produto) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter codigoFilter = new FilterPredicate("Codigo", FilterOperator.EQUAL, codigo);

		Query query = new Query("Produtos").setFilter(codigoFilter);
		Optional<Entity> entidadeProduto = EntidadeProduto(datastore, query);

		if (entidadeProduto.isPresent()) {

			DatastoreService datastoreCodigoProduto = DatastoreServiceFactory.getDatastoreService();
			Filter codigoFilterProduto = new FilterPredicate("Codigo", FilterOperator.EQUAL, produto.getCodigo());

			Query queryBanco = new Query("Produtos").setFilter(codigoFilterProduto);
			Optional<Entity> entidadeCodigoProduto = EntidadeProduto(datastoreCodigoProduto, queryBanco);

			if (!entidadeCodigoProduto.isPresent()) {
				return Response.status(Status.NOT_FOUND).entity("Produto não encontrado!").build();
			}

			if (codigo == produto.getCodigo()) {
				TransformarProdutoParaEntidade(produto, entidadeProduto.get());
				datastore.put(entidadeProduto.get());
				produto.setId(entidadeProduto.get().getKey().getId());
				return Response.ok(produto, MediaType.APPLICATION_JSON).build();
			} else {
				return Response.status(Status.NOT_FOUND).entity("Produto já cadastrado, favor inserir um novo codigo!")
						.build();
			}
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
		entidadeProduto.setProperty("Quantidade", produto.getQuantidade());
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
		produto.setQuantidade(Integer.parseInt(entidadeProduto.getProperty("Quantidade").toString()));
		return produto;
	}

	/**
	 * Verifica a existencia do produto na base de dados
	 * 
	 * @param datastore
	 * @param query
	 * @return Boolean
	 */
	private Boolean ExisteProduto(DatastoreService datastore, Query query) {
		Optional<Entity> optionalEntidadeProduto = Optional.ofNullable(datastore.prepare(query).asSingleEntity());
		return optionalEntidadeProduto.isPresent();
	}

	/**
	 * Retorna um Optional da entidade da base de dados
	 * 
	 * @param datastore
	 * @param query
	 * @return Optional<Entity>
	 */
	private Optional<Entity> EntidadeProduto(DatastoreService datastore, Query query) {
		Optional<Entity> optionalEntidadeProduto = Optional.ofNullable(datastore.prepare(query).asSingleEntity());

		if (optionalEntidadeProduto.isPresent()) {
			return Optional.ofNullable(optionalEntidadeProduto).get();
		} else {
			return Optional.empty();
		}
	}
}
