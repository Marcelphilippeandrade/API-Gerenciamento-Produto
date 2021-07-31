package br.com.marcel.philippe.api_gerenciamento_produtos.authentication;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.DatatypeConverter;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import br.com.marcel.philippe.api_gerenciamento_produtos.modelo.Usuario;
import br.com.marcel.philippe.api_gerenciamento_produtos.servicos.GerenciamentoDeUsuario;

public class AuthFilter implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;

	private static final String ACCESS_UNAUTHORIZED = "Você não tem permissão para acessar esse recurso";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		Method method = resourceInfo.getResourceMethod();

		if (method.isAnnotationPresent(PermitAll.class)) {
			return;
		}

		String auth = requestContext.getHeaderString("Authorization");

		if (auth == null) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(ACCESS_UNAUTHORIZED).build());
			return;
		}

		String[] loginSenha = decode(auth);

		if (loginSenha == null || loginSenha.length != 2) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(ACCESS_UNAUTHORIZED).build());
			return;
		}

		RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
		Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAllowed.value()));
		if (checaCredenciaisEPerfis(loginSenha[0], loginSenha[1], rolesSet, requestContext) == false) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(ACCESS_UNAUTHORIZED).build());
			return;
		}
	}

	private String[] decode(String auth) {
		auth = auth.replaceFirst("[B|b]asic ", "");
		byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);

		if (decodedBytes == null || decodedBytes.length == 0) {
			return null;
		}
		return new String(decodedBytes).split(":", 2);
	}

	private boolean checaCredenciaisEPerfis(String nomeUsuario, String senha, Set<String> perfis, ContainerRequestContext requestContext) {
		
		boolean eUmUsuarioAutorizado = false;
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter emailFilter = new FilterPredicate(GerenciamentoDeUsuario.PROP_EMAIL, FilterOperator.EQUAL, nomeUsuario);
		Query query = new Query(GerenciamentoDeUsuario.USUARIO_KIND).setFilter(emailFilter);
		Entity entidadeUsuario = datastore.prepare(query).asSingleEntity();
		
		if (entidadeUsuario != null) {
			if (senha.equals(entidadeUsuario.getProperty(GerenciamentoDeUsuario.PROP_SENHA)) && perfis.contains(entidadeUsuario.getProperty(GerenciamentoDeUsuario.PROP_PERFIL))) {
				final Usuario usuario = new Usuario();
				usuario.setEmail((String) entidadeUsuario.getProperty(GerenciamentoDeUsuario.PROP_EMAIL));
				usuario.setSenha((String) entidadeUsuario.getProperty(GerenciamentoDeUsuario.PROP_SENHA));
				usuario.setId(entidadeUsuario.getKey().getId());
				usuario.setGcmRegId((String) entidadeUsuario.getProperty(GerenciamentoDeUsuario.PROP_GCM_REG_ID));
				usuario.setUltimoLogin((Date) Calendar.getInstance().getTime());
				usuario.setUltimoGCMRegister((Date) entidadeUsuario.getProperty(GerenciamentoDeUsuario.PROP_ULTIMO_GCM_REGISTER));
				usuario.setPerfil((String) entidadeUsuario.getProperty(GerenciamentoDeUsuario.PROP_PERFIL));
				entidadeUsuario.setProperty(GerenciamentoDeUsuario.PROP_ULTIMO_LOGIN, usuario.getUltimoLogin()); 
				datastore.put(entidadeUsuario);
				
				requestContext.setSecurityContext(new SecurityContext() {

					@Override
					public boolean isUserInRole(String perfil) {
						return perfil.equals(usuario.getPerfil());
					}

					@Override
					public boolean isSecure() {
						return true;
					}

					@Override
					public Principal getUserPrincipal() {
						return usuario;
					}

					@Override
					public String getAuthenticationScheme() {
						return SecurityContext.BASIC_AUTH;
					}
				});
				
				eUmUsuarioAutorizado = true;
			}
		}
		return eUmUsuarioAutorizado;
	}
}
