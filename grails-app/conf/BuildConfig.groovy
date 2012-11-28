grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        mavenRepo "http://maven.ala.org.au/repository/"
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()

        // uncomment these to enable remote dependency resolution from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"

    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.16'
        //build 'au.org.ala:ala-cas-client:1.0-SNAPSHOT'
        //build 'org.jasig.cas:cas-client-core:3.1.10'

        //build 'javax.media:jai-core:1.1.3'
       // build 'javax.media:jai-codec:1.1.3'
       // build 'javax.media:jai-imageio:1.1'
        runtime 'org.imgscalr:imgscalr-lib:4.2'
        runtime 'org.apache.httpcomponents:httpcore:4.1.2'
        runtime 'org.apache.httpcomponents:httpclient:4.1.2'
        runtime 'org.apache.httpcomponents:httpcore:4.1.2'
        runtime 'org.apache.httpcomponents:httpclient:4.1.2'
        compile 'org.codehaus.gpars:gpars:0.11'
    }

    plugins {
        //runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.7.1"
        runtime ":resources:1.1.6"
        compile ":mongodb:1.0.0.GA"
        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:$grailsVersion"
    }
}
