package br.com.marcel.philippe.api_gerenciamento_produtos.util;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import br.com.marcel.philippe.api_gerenciamento_produtos.servicos.GerenciamentoDeUsuario;

public class ServletInicializaContextoApp implements ServletContextListener {

	private static final Logger log = Logger.getLogger("ServletInicializaContextoApp");

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		log.info("Api gerenciamento de produtos iniciada!");
		inicializarEntidadeUsuario();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

	private void inicializarEntidadeUsuario() {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter roleFilter = new FilterPredicate(GerenciamentoDeUsuario.PROP_PERFIL, FilterOperator.EQUAL, "ADMIN");
		Query query = new Query(GerenciamentoDeUsuario.USUARIO_KIND).setFilter(roleFilter);

		List<Entity> entidades = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));

		if (entidades.size() == 0) {
			log.info("Nenhum usuário encontrado. Inicializando o tipo Usuario no Datastore");
			Key chaveUsuario = KeyFactory.createKey(GerenciamentoDeUsuario.USUARIO_KIND, "userkey");
			Entity entidadeUsuario = new Entity(GerenciamentoDeUsuario.USUARIO_KIND, chaveUsuario);
			entidadeUsuario.setProperty(GerenciamentoDeUsuario.PROP_EMAIL, "admin@marcelphilippe.com");
			entidadeUsuario.setProperty(GerenciamentoDeUsuario.PROP_SENHA, "Admin#7");
			entidadeUsuario.setProperty(GerenciamentoDeUsuario.PROP_GCM_REG_ID, "");
			entidadeUsuario.setProperty(GerenciamentoDeUsuario.PROP_ULTIMO_LOGIN, Calendar.getInstance().getTime());
			entidadeUsuario.setProperty(GerenciamentoDeUsuario.PROP_ULTIMO_GCM_REGISTER,Calendar.getInstance().getTime());
			entidadeUsuario.setProperty(GerenciamentoDeUsuario.PROP_PERFIL, "ADMIN");

			datastore.put(entidadeUsuario);
		}
	}
}
