package au.org.ala.fielddata



class RefreshUserCacheJob {

  static triggers = {
     cron name:'refreshUserCache', startDelay:10000, cronExpression: '0 */5 * * * ?'
  }

  def userService

  def execute() {
      log.info("****** Refreshing user details ****** " + new Date())
      userService.refreshUserDetails()
      log.info("****** Completed refreshing user details ******" + new Date())
  }
}
