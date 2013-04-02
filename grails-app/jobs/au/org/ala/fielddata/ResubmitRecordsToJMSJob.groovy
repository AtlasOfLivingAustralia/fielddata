package au.org.ala.fielddata

import org.bson.types.ObjectId



class ResubmitRecordsToJMSJob {
    def broadcastService
    static triggers = {
        cron name:'resubmitRecordsToJMS', startDelay:10000, cronExpression: '0 */30 * * * ?'
    }

    def execute() {
        log.info("Checking to see if there are any failed JMS tasks to resend")

        FailedRecord.list().each {
            log.debug("Trying to resend " + it.record + " type " + it.updateType+ " props " + it.properties)

            broadcastService.resubmit(it.updateType, it.record.id)
        }
    }
}
