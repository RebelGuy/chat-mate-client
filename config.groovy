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
        STUDIO_URL = 'https://www.chat-mate-sandbox.com'
    }

    release {
        ENVIRONMENT = 'release'
        SERVER_URL = 'https://server.chat-mate.com'
        STUDIO_URL = 'https://www.chat-mate.com'
    }
}
