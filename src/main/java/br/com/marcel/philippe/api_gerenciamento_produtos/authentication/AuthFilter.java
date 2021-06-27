package br.com.marcel.philippe.api_gerenciamento_produtos.authentication;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

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

		String[] loginPassword = decode(auth);

		if (loginPassword == null || loginPassword.length != 2) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(ACCESS_UNAUTHORIZED).build());
			return;
		}

		RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
		Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAllowed.value()));
		if (checkCredentialsAndRoles(loginPassword[0], loginPassword[1], rolesSet) == false) {
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

	private boolean checkCredentialsAndRoles(String username, String password, Set<String> roles) {
		boolean isUserAllowed = false;

		if (username.equals("Admin") && password.equals("Admin")) {
			if (roles.contains("ADMIN")) {
				isUserAllowed = true;
			}
		}
		if (isUserAllowed == false) {
			if (username.equals("User") && password.equals("User")) {
				if (roles.contains("USER")) {
					isUserAllowed = true;
				}
			}
		}
		return isUserAllowed;
	}
}
