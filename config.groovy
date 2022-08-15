// from stackoverflow.com/questions/37101589
environments {
    local {
        ENVIRONMENT = 'local'
        SERVER_URL = 'http://localhost:3010'
        STUDIO_URL = 'http://localhost:3000'
    }

    debug {
        ENVIRONMENT = 'debug'
        SERVER_URL = 'http://chat-mate-sandbox.azurewebsites.net'
        STUDIO_URL = 'https://nice-coast-05a39c31e.1.azurestaticapps.net'
    }

    release {
        ENVIRONMENT = 'release'
        SERVER_URL = 'http://chat-mate-prod.azurewebsites.net'
        STUDIO_URL = 'https://kind-rock-0d2509e10.1.azurestaticapps.net'
    }
}
