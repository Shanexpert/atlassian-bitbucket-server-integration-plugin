package com.atlassian.bitbucket.jenkins.internal.BitbucketSCM

def f = namespace(lib.FormTagLib)
def c = namespace(lib.CredentialsTagLib)
def st = namespace('jelly:stapler')

f.section() {
    f.entry(title: _("bitbucket.scm.credentials"), field: "credentialsId") {
        c.select(context: app, includeUser: false, expressionAllowed: false)
    }

    f.entry(title: _("bitbucket.scm.server"), field: "serverId") {
        f.select(context: app)
    }

    f.entry(title: _("bitbucket.scm.projectKey"), field: "projectKey") {
        f.textbox(placeholder: "Project key")
    }

    f.entry(title: _("bitbucket.scm.repositorySlug"), field: "repositorySlug") {
        f.textbox(placeholder: "repository slug")
    }


    f.entry(title: _("Branches to build")) {
        f.repeatableProperty(field: "branches", addCaption: _("Add branch"), hasHeader: "true", minimum: "1", noAddButton: "true")
    }


    if (descriptor.showGitToolOptions) {

        f.entry(title: "Git executable", field: "gitTool", values: "$descriptor.gitTools") {
            f.select(context: app)
        }
    }
    f.entry(title: _("Additional Behaviours")) {
        f.repeatableHeteroProperty(field: "extensions", items: "extensions", descriptors: "${descriptor.extensionDescriptors}", addCaption: _("Add"), hasHeader: "true")
    }

    if (instance != null && instance.id != null) {
        f.invisibleEntry(field: "id") {
            f.input(type: "hidden", name: "id", value: "${instance.id}")
        }
    }
}
