// Place your Spring DSL code here
beans = {
    jmsConnectionFactory(org.apache.activemq.ActiveMQConnectionFactory) {
	    brokerURL = grailsApplication.config.brokerURL
    }
}
