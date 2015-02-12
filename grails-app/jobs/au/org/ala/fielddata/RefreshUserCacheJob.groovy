package au.org.ala.fielddata



class RefreshUserCacheJob {

  static triggers = {
     cron name:'refreshUserCache', startDelay:10000, cronExpression: '0 0 * * * ?'
  }

  def userService

  def execute() {
      log.debug("****** Refreshing user details ****** " + new Date())
      userService.refreshUserDetails()
      log.debug("****** Completed refreshing user details ******" + new Date())
  }
}
