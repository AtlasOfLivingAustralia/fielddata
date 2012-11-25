// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

headerAndFooter.baseURL = "http://www2.ala.org.au/commonui"
security.cas.urlPattern = ""
security.cas.loginUrl = "https://auth.ala.org.au/cas/login"
security.cas.logoutUrl = "https://auth.ala.org.au/cas/logout"
ala.baseURL = "http://www.ala.org.au"
bie.baseURL = "http://bie.ala.org.au"
bie.searchPath = "/search"

grails.project.groupId = "au.org.ala.fielddata" // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']


// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// enable query caching by default
grails.hibernate.cache.queries = true

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
        grails.serverURL = "http://moyesyside.ala.org.au:8086"
        fielddata.mediaUrl = "http://moyesyside.ala.org.au/fielddata/"
        fielddata.mediaDir = "/data/fielddata/"
    }
    production {
    /*
        grails.logging.jul.usebridge = false
        grails.serverURL = "http://audax.ala.org.au:8080"
        grails.serverURL = "http://audax.ala.org.au:8080"
        fielddata.mediaUrl = "http://audax.ala.org.au/~mar759/fielddata/media/"
        fielddata.mediaDir = "/Users/mar759/Sites/fielddata/media/"
        */
        
        grails.logging.jul.usebridge = false
        grails.serverURL = "http://115.146.94.38:8080"
        grails.serverURL = "http://115.146.94.38:8080"
        fielddata.mediaUrl = "http://115.146.94.38/fielddata/media/"
        fielddata.mediaDir = "/tmp/fielddata/media/"        
    }
}

// log4j configuration
// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {

        console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.DEBUG
//        rollingFile name: "dev2", layout: pattern(conversionPattern: "[POSTIE] %c{2} %m%n"), maxFileSize: 1024, file: "/tmp/postie.log", threshold: org.apache.log4j.Level.DEBUG

        environments {
            production {
              rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/var/log/tomcat6/fielddata.log", threshold: org.apache.log4j.Level.DEBUG, layout: pattern(conversionPattern: "%d %-5p [%c{1}] %m%n")
              'null' name: "stacktrace"
            }
            development {
              console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.DEBUG
              rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/tmp/fielddata.log", threshold: org.apache.log4j.Level.DEBUG, layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n")
              'null' name: "stacktrace"
            }
            test {
              rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/tmp/fielddata-test.log", threshold: org.apache.log4j.Level.DEBUG, layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n")
              'null' name: "stacktrace"
            }
        }
    }

    root {
        // change the root logger to my tomcatLog file
        error 'tomcatLog'
        warn 'tomcatLog'
        info 'tomcatLog'
        debug 'tomcatLog', 'stdout'
        additivity = true
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails.web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate',
           'org.codehaus.groovy.grails.plugins.orm.auditable',
           'org.mortbay.log', 'org.springframework.webflow',
           'grails.app',
           'org.apache',
           'org',
           'com',
           'au',
           'grails.app',
           'net',
           'grails.util.GrailsUtil'

    debug  'grails.app.domain.ala.postie',
           'grails.app.controller.ala.postie',
           'grails.app.service.ala.postie',
           'grails.app.tagLib.ala.postie'
}

