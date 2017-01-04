package kth.sebarm.awsome;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import model.UserPojo;

/**
 * Created by sebastian markström on 2017-01-04.
 */
public class MongoDbEndPoint extends AbstractVerticle{

    private static final Logger logger = LoggerFactory.getLogger(MongoDbEndPoint.class);
    //Database
    private MongoClient mongoClient;
    private static final String USER_COLLECTION = "db_users";

    @Override
    public void start() throws Exception {
        mongoClient = MongoClient.createShared(vertx, config());
        Router router = Router.router(vertx);
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        router.route("/register/user*").handler(BodyHandler.create());
        router.post("/register/user").handler(this::registerUser);
        router.route().failureHandler(errorHandler());

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 5500));
    }

    private void registerUser(RoutingContext routingContext) {
        UserPojo user = new UserPojo(routingContext.getBodyAsJson());
        mongoClient.insert(USER_COLLECTION, user.toJson(), handler -> {
            if(handler.succeeded()){
                logger.info("user successfully saved in database");
                routingContext.response().setStatusCode(201);
                routingContext.response().end("success");
            } else {
                logger.error("user not saved in database");
                routingContext.response().setStatusCode(400);
                routingContext.response().end("Failure. could not save data into database");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        logger.info("server stopped");
    }

    private ErrorHandler errorHandler() {
        return ErrorHandler.create();
    }
}
