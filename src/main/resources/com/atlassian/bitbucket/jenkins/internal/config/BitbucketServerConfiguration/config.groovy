package com.atlassian.bitbucket.jenkins.internal.config.BitbucketServerConfiguration

def f = namespace(lib.FormTagLib)
def c = namespace(lib.CredentialsTagLib)


f.section(title: _("bitbucket")) {
    f.entry(title: _("bitbucket.server.id"), field: "id") {
        f.readOnlyTextbox(clazz: "bbs-server-id-textbox")
    }

    f.entry(title: _("bitbucket.server.name"), field: "serverName") {
        f.textbox(placeholder: "Descriptive name of server")
    }

    f.entry(title: _("bitbucket.url"), field: "baseUrl") {
        f.textbox(placeholder: "http://your-bitbucket-server.org")
    }

    f.entry(title: _("bitbucket.admin.credentials"), field: "adminCredentialsId") {
        c.select(context: app, includeUser: false, expressionAllowed: false)
    }

    f.entry(title: _("bitbucket.credentials"), field: "credentialsId") {
        c.select(context: app, includeUser: false, expressionAllowed: false)
    }

    f.block() {
        f.validateButton(
                title: _("bitbucket.test.connection"),
                progress: _("bitbucket.test.connection"),
                method: "testConnection",
                with: "adminCredentialsId,baseUrl,credentialsId"
        )
    }
}