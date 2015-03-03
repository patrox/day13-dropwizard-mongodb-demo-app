package com.shekhar.reader30;

import com.mongodb.MongoURI;
import net.vz.mongodb.jackson.JacksonDBCollection;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.shekhar.reader30.representations.Blog;
import com.shekhar.reader30.resources.BlogResource;
import com.shekhar.reader30.resources.IndexResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class Reader30Service extends Application<Reader30Configuration> {

    public static void main(String[] args) throws Exception {
        new Reader30Service().run(new String[] { "server" });
    }
    
    @Override
    public String getName() {
        return "reader30";
    }

    @Override
    public void initialize(Bootstrap<Reader30Configuration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(Reader30Configuration configuration, Environment environment) throws MongoException, UnknownHostException {
        Mongo mongo;

        if (System.getenv("MONGOLAB_URI") != null) {
            MongoURI mongoURI = new MongoURI(System.getenv("MONGOLAB_URI"));
            mongo = new Mongo(mongoURI);
        }
        else {
            mongo = new Mongo(configuration.mongohost, configuration.mongoport);
        }

        DB db = mongo.getDB(configuration.mongodb);

        JacksonDBCollection<Blog, String> blogs = JacksonDBCollection.wrap(db.getCollection("blogs"), Blog.class, String.class);
        MongoManaged mongoManaged = new MongoManaged(mongo);
        environment.lifecycle().manage(mongoManaged);
        
        environment.healthChecks().register("MongoDBHealthCheck", new MongoHealthCheck(mongo));
        
        environment.jersey().register(new BlogResource(blogs));
        environment.jersey().register(new IndexResource(blogs));
    }
}
