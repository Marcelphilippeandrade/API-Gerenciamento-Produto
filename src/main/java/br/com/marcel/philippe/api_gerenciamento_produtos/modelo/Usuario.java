package br.com.marcel.philippe.api_gerenciamento_produtos.modelo;

import java.security.Principal;
import java.util.Date;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class Usuario implements Principal {

	private long id;

	@Email(message = "Formato de e-mail inválido.")
	private String email;

	@NotNull(message = "Senha não pode ser vazia.")
	private String senha;
	
	private String gcmRegId;
	private Date ultimoLogin;
	private Date ultimoGCMRegister;

	@NotNull(message = "Perfil não pode ser vazio.")
	private String perfil;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getGcmRegId() {
		return gcmRegId;
	}

	public void setGcmRegId(String gcmRegId) {
		this.gcmRegId = gcmRegId;
	}

	public Date getUltimoLogin() {
		return ultimoLogin;
	}

	public void setUltimoLogin(Date ultimoLogin) {
		this.ultimoLogin = ultimoLogin;
	}

	public Date getUltimoGCMRegister() {
		return ultimoGCMRegister;
	}

	public void setUltimoGCMRegister(Date ultimoGCMRegister) {
		this.ultimoGCMRegister = ultimoGCMRegister;
	}

	public String getPerfil() {
		return perfil;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}

	@Override
	public String getName() {
		return this.email;
	}
}
