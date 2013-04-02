package au.org.ala.fielddata

import org.bson.types.ObjectId



class ResubmitRecordsToJMSJob {
    def broadcastService
    static triggers = {
        cron name:'resubmitRecordsToJMS', startDelay:10000, cronExpression: '0 */30 * * * ?'
    }

    def execute() {
        log.info("Checking to see if there are any failed JMS tasks to resend")

        def count = broadcastService.resubmitFailedRecords()

        log.info("Finished checking " + count + " failed records")
    }
}
