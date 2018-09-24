package edu.pucmm.isc.sjw;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import spark.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

/**
 * 
 */
public class Main {

    //Trabajando con el cache desde Google Guava y no tener que acceder nuevamente.
    private static final LoadingCache<String, String> WEBJARS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(365, TimeUnit.DAYS)
            .build(
                    new CacheLoader<String, String>() {
                        @Override
                        public String load(String fullPath) throws IOException {
                            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(fullPath);
                            return IOUtils.toString(inputStream);
                        }
                    });

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        //Ruta estatica a nuestros recursos..
        staticFiles.location("/publico");

        /**
         * Hola Mundo con Webjars
         */
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return render(model, "index.ftl");
        });

        /**
         *  Ruta para obtener los recursos desde el webjars.
         */
        get("/webjars/*",  (req, res) -> {
            //obteniendo la ruta del webjars.
            String ruta = req.splat()[0];
            if(ruta.endsWith(".js")){
                res.type("application/javascript");
            }
            if(ruta.endsWith(".css")){
                res.type("text/css");
            }
            String fullPath = "META-INF/resources/webjars/" + ruta;
            return WEBJARS_CACHE.get(fullPath); //aplicando el cachea los recursos recuperados.
        });
        
    }

    /**
     * 
     * @param model
     * @param templatePath
     * @return
     */
    public static String render(Map<String, Object> model, String templatePath) {
        return new FreeMarkerEngine().render(new ModelAndView(model, templatePath));
    }
}
