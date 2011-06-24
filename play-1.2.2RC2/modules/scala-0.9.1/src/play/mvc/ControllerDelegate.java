package play.mvc;

import play.mvc.Controller;
import play.mvc.Router.ActionDefinition;
import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.results.RenderTemplate;

import java.io.InputStream;
import java.io.File;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Future;

/**
 * Provides java interop
 */
public abstract class ControllerDelegate {

    // ~~~~

    public static boolean templateExists(String name) {
        return Controller.templateExists(name);
    }

    public static ActionDefinition reverseForScala() {
        return Controller.reverse();
    }

    public ActionDefinition reverse() {
        return Controller.reverse();
    }

    public static RenderTemplate renderTemplateForScala(String template, Map<String,Object> args) {
        try {
            if (template == null) {
                Request theRequest = Request.current();
                String format = theRequest.format;
                String action = play.classloading.enhancers.ControllersEnhancer.currentAction.get().peek();
                if(action.startsWith("controllers")) {
                    action = action.substring("controllers".length());
                }
                template = action.replace(".", "/") + "." + (format == null ? "html" : format);
            }
            Controller.renderTemplate(template, args);
        } catch(Throwable t) {
            if(t instanceof RenderTemplate) {
                return (RenderTemplate)t;
            }
            if(t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            throw new UnexpectedException(t);
        }
        return null;
    }

}

