package au.org.ala.fielddata

import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient

class WebService {

    def grailsApplication

    def doPost(String url) {
        try {
            def httpclient = new DefaultHttpClient();
            def post = new HttpPost(url)
            def response = httpclient.execute(post)
            def content = response.getEntity().getContent()
            def jsonSlurper = new JsonSlurper()
            def json = jsonSlurper.parse(new InputStreamReader(content))
            return [error:  null, resp: json]
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out calling web service. URL= \${url}."]
            println error.error
            return [error: error]
        } catch (Exception e) {
            def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} ${e} URL= ${url}."]
            println error.error
            return [error: error]
        }
    }

    def doPost(String url, String body) {
        try {
            def httpclient = new DefaultHttpClient();
            def post = new HttpPost(url)
            post.
            def response = httpclient.execute(post)
            def content = response.getEntity().getContent()
            def jsonSlurper = new JsonSlurper()
            def json = jsonSlurper.parse(new InputStreamReader(content))
            return [error:  null, resp: json]
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out calling web service. URL= \${url}."]
            println error.error
            return [error: error]
        } catch (Exception e) {
            def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} ${e} URL= ${url}."]
            println error.error
            return [error: error]
        }
    }
}
