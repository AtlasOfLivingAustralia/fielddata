class BootStrap {

    def userService

    def init = { servletContext ->
        userService.refreshUserDetails()
    }
    def destroy = {
    }
}
