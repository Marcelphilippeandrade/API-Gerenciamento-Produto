package br.com.marcel.philippe.api_gerenciamento_produtos.authentication;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

public class AuthFilter implements ContainerRequestFilter {

	private static final String ACCESS_UNAUTHORIZED = "Você não tem permissão para acessar esse recurso";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
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

		if (loginPassword[0].equals("Admin") && loginPassword[1].equals("Admin")) {
			return;
		} else {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(ACCESS_UNAUTHORIZED).build());
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
}
