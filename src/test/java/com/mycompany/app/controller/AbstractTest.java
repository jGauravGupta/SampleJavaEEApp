package com.mycompany.app.controller;

import com.mycompany.app.controller.util.HeaderUtil;
import com.mycompany.app.service.facade.AbstractFacade;
import com.mycompany.app.service.facade.producer.EntityManagerProducer;
import com.mycompany.app.service.facade.producer.LoggerProducer;
import java.io.File;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Abstract class for base application packaging.
 *
 */
@RunWith(Arquillian.class)
public abstract class AbstractTest {

    @ArquillianResource
    private URL deploymentUrl;
    private WebTarget webTarget;
    protected final static MavenResolverSystem RESOLVER = Maven.resolver();

    public static WebArchive buildArchive() {
        File[] jacksonFiles = RESOLVER.resolve("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.7.5").withTransitivity().asFile();
        File[] deltaspikeFiles = RESOLVER.resolve("org.apache.deltaspike.core:deltaspike-core-api:1.5.0").withTransitivity().asFile();
        File[] deltaspikeImplFiles = RESOLVER.resolve("org.apache.deltaspike.core:deltaspike-core-impl:1.5.0").withTransitivity().asFile();
        File[] velocityFiles = RESOLVER.resolve("org.apache.velocity:velocity:1.7").withTransitivity().asFile();
        File[] emailFiles = RESOLVER.resolve("org.apache.commons:commons-email:1.4").withTransitivity().asFile();
        File[] jwtFiles = RESOLVER.resolve("io.jsonwebtoken:jjwt:0.6.0").withTransitivity().asFile();
        File[] slf4jFiles = RESOLVER.resolve("org.slf4j:slf4j-api:1.7.24").withTransitivity().asFile();

//        File[] pomFiles = Maven.resolver().loadPomFromFile("pom.xml").importDependencies(ScopeType.TEST, ScopeType.PROVIDED).resolve().withTransitivity().asFile();

        final WebArchive archive = ShrinkWrap.create(WebArchive.class);
        archive.addClass(AbstractFacade.class).addPackage(HeaderUtil.class.getPackage())
                .addClass(EntityManagerProducer.class).addClass(LoggerProducer.class)
                 .addAsLibraries(jacksonFiles).addAsLibraries(deltaspikeFiles).addAsLibraries(deltaspikeImplFiles)
                .addAsLibraries(velocityFiles).addAsLibraries(emailFiles).addAsLibraries(jwtFiles).addAsLibraries(slf4jFiles)
//                .addAsLibraries(pomFiles)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsResource(new ClassLoaderAsset("META-INF/sql/insert.sql"), "META-INF/sql/insert.sql")
                .setWebXML("web.xml");
        return archive;
    }

    @Before
    public void buildWebTarget() throws Exception {
        webTarget = ClientBuilder.newClient().target(deploymentUrl.toURI().toString() + "resources/");
    }

    protected Invocation.Builder target(String path) {
        return webTarget.path(path).request();
    }

    protected Invocation.Builder target(String path, Map<String, Object> params) {
        WebTarget target = webTarget.path(path);
        for (String key : params.keySet()) {
            if (path.contains(String.format("{%s}", key))) {
                target = target.resolveTemplate(key, params.get(key));
            } else {
                target = target.queryParam(key, params.get(key));
            }
        }
        return target.request();
    }

}
