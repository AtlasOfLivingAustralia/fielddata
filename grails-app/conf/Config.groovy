def appName = 'fielddata'
def ENV_NAME = "${appName.toUpperCase()}_CONFIG"
default_config = "/data/${appName}/config/${appName}-config.properties"
if(!grails.config.locations || !(grails.config.locations instanceof List)) {
    grails.config.locations = []
}
if(System.getenv(ENV_NAME) && new File(System.getenv(ENV_NAME)).exists()) {
    println "[${appName}] Including configuration file specified in environment: " + System.getenv(ENV_NAME);
    grails.config.locations.add "file:" + System.getenv(ENV_NAME)
} else if(System.getProperty(ENV_NAME) && new File(System.getProperty(ENV_NAME)).exists()) {
    println "[${appName}] Including configuration file specified on command line: " + System.getProperty(ENV_NAME);
    grails.config.locations.add "file:" + System.getProperty(ENV_NAME)
} else if(new File(default_config).exists()) {
    println "[${appName}] Including default configuration file: " + default_config;
    grails.config.locations.add "file:" + default_config
} else {
    println "[${appName}] No external configuration file defined."
}

println "[${appName}] (*) grails.config.locations = ${grails.config.locations}"

headerAndFooter.baseURL = "http://www2.ala.org.au/commonui"
security.cas.urlPattern = ""
security.cas.loginUrl = "https://auth.ala.org.au/cas/login"
security.cas.logoutUrl = "https://auth.ala.org.au/cas/logout"
ala.baseURL = "http://www.ala.org.au/"
bie.baseURL = "http://bie.ala.org.au"
bie.searchPath = "/search"
brokerURL = 'tcp://localhost:61616'
enableJMS = false
userDetailsUrl = "http://auth.ala.org.au/userdetails/userDetails/getUserListFull"
userDetailsSingleUrl = "https://auth.ala.org.au/userdetails/userDetails/getUserDetails"


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
        enableJMS = true //change to allow broadcast to queue
        brokerURL = 'tcp://localhost:61616'
        queueName = "au.org.ala.cs"
    }
    production {
        grails.logging.jul.usebridge = false
        grails.serverURL = "http://fielddata.ala.org.au"
        fielddata.mediaUrl = "http://fielddata.ala.org.au/media/"
        fielddata.mediaDir = "/data/fielddata/media/"
        enableJMS = true //change to allow broadcast to queue
        brokerURL = 'tcp://ala-starr.it.csiro.au:61616'
        queueName = "au.org.ala.cs"
    }
}

// log4j configuration
log4j = {

    appenders {
        environments{
            development {
                console name: "stdout", layout: pattern(conversionPattern: "%d [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.DEBUG
            }
            production {
                rollingFile name: "fielddataLog",
                        maxFileSize: 104857600,
                        file: "/var/log/tomcat6/fielddata.log",
                        threshold: org.apache.log4j.Level.DEBUG,
                        layout: pattern(conversionPattern: "%d [%c{1}]  %m%n")
                rollingFile name: "stacktrace", maxFileSize: 1024, file: "/var/log/tomcat6/fielddata-stacktrace.log"
            }
        }
    }

    root {
        debug  'fielddataLog'
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails.web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
           'org.springframework.jdbc',
           'org.springframework.transaction',
           'org.codehaus.groovy',
           'org.grails',
           'org.apache',
           'grails.spring',
           'grails.util.GrailsUtil'

    debug  'au.org.ala'
}