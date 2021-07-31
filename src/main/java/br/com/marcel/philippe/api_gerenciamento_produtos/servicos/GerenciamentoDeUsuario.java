package br.com.marcel.philippe.api_gerenciamento_produtos.servicos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
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

import br.com.marcel.philippe.api_gerenciamento_produtos.modelo.Usuario;

@Path("/usuarios")
public class GerenciamentoDeUsuario {

	private static final Logger log = Logger.getLogger("GerenciamentoDeUsuario");

	@Context
	SecurityContext securityContext;

	public static final String USUARIO_KIND = "Usuario";
	public static final String PROP_EMAIL = "email";
	public static final String PROP_SENHA = "senha";
	public static final String PROP_GCM_REG_ID = "gcmRegId";
	public static final String PROP_ULTIMO_LOGIN = "ultimoLogin";
	public static final String PROP_ULTIMO_GCM_REGISTER = "ultimoCMRegister";
	public static final String PROP_PERFIL = "perfil";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "ADMIN" })
	public Response getUsuarios() {

		log.fine("Pesquisando todos os usuario!");

		List<Usuario> usuarios = new ArrayList<>();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(USUARIO_KIND).addSort(PROP_EMAIL, SortDirection.ASCENDING);
		List<Entity> entidadesUsuarios = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		for (Entity entidadeUsuario : entidadesUsuarios) {
			Usuario usuario = TransformarEntidadeParaUsuario(entidadeUsuario);
			usuarios.add(usuario);
		}
		return Response.ok().entity(usuarios).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "ADMIN", "USER" })
	@Path("/{email}")
	public Response getUsuario(@PathParam(PROP_EMAIL) String email) {

		log.fine("Pesquisando usuario de email=[" + email + "]");

		if (securityContext.getUserPrincipal().getName().equals(email) || securityContext.isUserInRole("ADMIN")) {
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Filter emailFilter = new FilterPredicate(PROP_EMAIL, FilterOperator.EQUAL, email);
			Query query = new Query(USUARIO_KIND).setFilter(emailFilter);
			Entity entidadeUsuario = datastore.prepare(query).asSingleEntity();

			if (entidadeUsuario != null) {
				Usuario usuario = TransformarEntidadeParaUsuario(entidadeUsuario);
				log.info("Usuario=[" + email + "] pesquisado com sucesso!");
				return Response.ok(usuario, MediaType.APPLICATION_JSON).build();
			} else {
				log.severe("Usuário=[" + email + "] não encontrado!");
				return Response.status(Status.NOT_FOUND).entity("Usuário=[" + email + "] não encontrado!").build();
			}
		} else {
			log.severe("Usuário não tem permissão para execução do serviço ou não é usuário ADMIN!");
			return Response.status(Status.FORBIDDEN).entity("Usuário não tem permissão para execução do serviço ou não é usuário ADMIN!").build();
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "ADMIN", "USER" })
	public Response salvarUsuario(@Valid @NotNull Usuario usuario) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		if (!checaSeJaExisteEmailCadastrado(usuario)) {
			if (!securityContext.isUserInRole("ADMIN")) {
				usuario.setPerfil("USER");
			}
			Key userKey = KeyFactory.createKey(USUARIO_KIND, "userKey");
			Entity entidadeUsuario = new Entity(USUARIO_KIND, userKey);

			usuario.setGcmRegId("");
			usuario.setUltimoGCMRegister(null);
			usuario.setUltimoLogin(null);

			TransformaUsuarioParaEntidade(usuario, entidadeUsuario);
			datastore.put(entidadeUsuario);

			usuario.setId(entidadeUsuario.getKey().getId());
		} else {
			log.severe("Usuário de email=[" + usuario.getEmail() + "] já cadastrado!");
			return Response.status(Status.BAD_REQUEST).entity("Usuário=[" + usuario.getEmail() + "] já cadastrado!").build();
		}

		log.info("Salvando o usuário email=[" + usuario.getEmail() + "]");
		return Response.ok(usuario, MediaType.APPLICATION_JSON).entity("Usuário=[" + usuario.getEmail() + "] salvo com sucesso!").build();
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{email}")
	@RolesAllowed({ "ADMIN", "USER" })
	public Response alterarUsuario(@PathParam("email") String email, @Valid @NotNull Usuario usuario) {
		if (usuario.getId() != 0) {
			if (securityContext.getUserPrincipal().getName().equals(email) || securityContext.isUserInRole("ADMIN")) {
				if (!checaSeJaExisteEmailCadastrado(usuario)) {
					DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
					Filter emailFilter = new FilterPredicate(PROP_EMAIL, FilterOperator.EQUAL, email);
					Query query = new Query(USUARIO_KIND).setFilter(emailFilter);
					Entity entidadeUsuario = datastore.prepare(query).asSingleEntity();
					if (entidadeUsuario != null) {
						TransformaUsuarioParaEntidade(usuario, entidadeUsuario);
						if (!securityContext.isUserInRole("ADMIN")) {
							usuario.setPerfil("USER");
						}
						datastore.put(entidadeUsuario);
						return Response.ok(usuario, MediaType.APPLICATION_JSON)
								.entity("Usuário=[" + email + "] alterado com sucesso!").build();
					} else {
						log.severe("Usuário=[" + email + "] não encontrado!");
						return Response.status(Status.NOT_FOUND).entity("Usuário=[" + email + "] não encontrado!").build();
					}
				} else {
					log.severe("Usuário=[" + usuario.getEmail() + "] já cadastrado!");
					return Response.status(Status.BAD_REQUEST).entity("Usuário=[" + usuario.getEmail() + "] já cadastrado!").build();
				}
			} else {
				log.severe("Usuário não tem permissão para execução do serviço ou não é um usuário ADMI!");
				return Response.status(Status.FORBIDDEN).entity("Usuário=[" + email + "] não tem permissão para execução do serviço ou não é um usuário ADMI!").build();
			}
		} else {
			log.severe("O ID do usuário=[" + email + "] deve ser informado para ser alterado!");
			return Response.status(Status.BAD_REQUEST).entity("O ID do usuário=[" + email + "] deve ser informado para ser alterado!").build();
		}
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{email}")
	@RolesAllowed({ "ADMIN", "USER" })
	public Response deletarUsuario(@PathParam("email") String email) {
		if (securityContext.getUserPrincipal().getName().equals(email) || securityContext.isUserInRole("ADMIN")) {
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

			Filter emailFilter = new FilterPredicate(PROP_EMAIL, FilterOperator.EQUAL, email);
			Query query = new Query(USUARIO_KIND).setFilter(emailFilter);

			Entity usuarioEntidade = datastore.prepare(query).asSingleEntity();

			if (usuarioEntidade != null) {
				datastore.delete(usuarioEntidade.getKey());

				Usuario usuario = TransformarEntidadeParaUsuario(usuarioEntidade);

				log.info("Usuário=[" + email + "] excluido com sucesso!");
				return Response.ok(usuario, MediaType.APPLICATION_JSON).entity("Usuário=[" + email + "] excluido com sucesso!").build();
			} else {
				log.info("Usuário=[" + email + "] não encontrado!");
				return Response.status(Status.NOT_FOUND).entity("Usuário=[" + email + "] não encontrado!").build();
			}
		} else {
			return Response.status(Status.FORBIDDEN).entity("Usuário=[" + email + "] não tem permissão para execução do serviço ou não é um usuário ADMI!").build();
		}
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update_gcm_reg_id/{gcmRegId}")
	@RolesAllowed({ "USER" })
	public Response updateGCMRegId(@PathParam("gcmRegId") String gcmRegId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter emailFilter = new FilterPredicate(PROP_EMAIL, FilterOperator.EQUAL, securityContext.getUserPrincipal().getName());
		Query query = new Query(USUARIO_KIND).setFilter(emailFilter);

		Entity entidadeUsuario = datastore.prepare(query).asSingleEntity();

		if (entidadeUsuario != null) {
			entidadeUsuario.setProperty(PROP_GCM_REG_ID, gcmRegId);
			entidadeUsuario.setProperty(PROP_ULTIMO_GCM_REGISTER, Calendar.getInstance().getTime());

			Usuario usuario = TransformarEntidadeParaUsuario(entidadeUsuario);

			datastore.put(entidadeUsuario);

			log.info("O GCMRegId do usuário=[" + usuario.getEmail() + "] foi atualizado com sucesso!");
			return Response.ok(usuario, MediaType.APPLICATION_JSON).entity("O GCMRegId do usuário=[" + usuario.getEmail() + "] foi atualizado com sucesso!").build();
		} else {
			log.severe("Usuário não encontrado!");
			return Response.status(Status.NOT_FOUND).entity("Usuário não encontrado!").build();
		}
	}

	private boolean checaSeJaExisteEmailCadastrado(Usuario usuario) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter emailFilter = new FilterPredicate(PROP_EMAIL, FilterOperator.EQUAL, usuario.getEmail());

		Query query = new Query(USUARIO_KIND).setFilter(emailFilter);
		Entity userEntity = datastore.prepare(query).asSingleEntity();

		if (userEntity == null) {
			return false;
		} else {
			if (userEntity.getKey().getId() == usuario.getId()) {
				return false;
			} else {
				return true;
			}
		}
	}

	private void TransformaUsuarioParaEntidade(Usuario usuario, Entity entidadeUsuario) {
		entidadeUsuario.setProperty(PROP_EMAIL, usuario.getEmail());
		entidadeUsuario.setProperty(PROP_SENHA, usuario.getSenha());
		entidadeUsuario.setProperty(PROP_GCM_REG_ID, usuario.getGcmRegId());
		entidadeUsuario.setProperty(PROP_ULTIMO_LOGIN, usuario.getUltimoLogin());
		entidadeUsuario.setProperty(PROP_ULTIMO_GCM_REGISTER, usuario.getUltimoGCMRegister());
		entidadeUsuario.setProperty(PROP_PERFIL, usuario.getPerfil());
	}

	private Usuario TransformarEntidadeParaUsuario(Entity entidadeUsuario) {

		Usuario usuario = new Usuario();
		usuario.setId(entidadeUsuario.getKey().getId());
		usuario.setEmail((String) entidadeUsuario.getProperty(PROP_EMAIL));
		usuario.setSenha((String) entidadeUsuario.getProperty(PROP_SENHA));
		usuario.setGcmRegId((String) entidadeUsuario.getProperty(PROP_GCM_REG_ID));
		usuario.setId(entidadeUsuario.getKey().getId());
		usuario.setUltimoLogin((Date) entidadeUsuario.getProperty(PROP_ULTIMO_LOGIN));
		usuario.setUltimoGCMRegister((Date) entidadeUsuario.getProperty(PROP_ULTIMO_GCM_REGISTER));
		usuario.setPerfil((String) entidadeUsuario.getProperty(PROP_PERFIL));

		return usuario;
	}
}
