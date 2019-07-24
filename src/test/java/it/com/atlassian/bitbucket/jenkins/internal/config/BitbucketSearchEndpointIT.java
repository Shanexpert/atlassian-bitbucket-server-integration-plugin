package it.com.atlassian.bitbucket.jenkins.internal.config;

import com.atlassian.bitbucket.jenkins.internal.fixture.BitbucketJenkinsRule;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketNamedLink;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketPage;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketProject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.atlassian.bitbucket.jenkins.internal.config.BitbucketSearchEndpoint.BITBUCKET_SERVER_SEARCH_URL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BitbucketSearchEndpointIT {

    private static final String FIND_REPO_URL = BITBUCKET_SERVER_SEARCH_URL + "/findRepositories";
    private static final String FIND_PROJECT_URL = BITBUCKET_SERVER_SEARCH_URL + "/findProjects";
    @ClassRule public static BitbucketJenkinsRule bitbucketJenkinsRule = new BitbucketJenkinsRule();

    @Test
    public void testFindProjects() throws Exception {
        HudsonResponse<BitbucketPage<BitbucketProject>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("credentialsId", bitbucketJenkinsRule.getCredentialsId())
                        .queryParam("name", "proj")
                        .get(bitbucketJenkinsRule.getURL() + FIND_PROJECT_URL)
                        .getBody()
                        .as(new TypeRef<HudsonResponse<BitbucketPage<BitbucketProject>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<BitbucketProject> results = response.getData();
        assertThat(results.getSize(), equalTo(1));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        List<BitbucketProject> values = results.getValues();
        assertThat(values.size(), equalTo(1));
        BitbucketProject project = values.get(0);
        assertThat(project.getKey(), equalTo("PROJECT_1"));
        assertThat(project.getName(), equalTo("Project 1"));
    }

    @Test
    public void testFindProjectsWithBlankCredentialsId() throws Exception {
        HudsonResponse<BitbucketPage<BitbucketProject>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("credentialsId", "")
                        .queryParam("name", "proj")
                        .get(bitbucketJenkinsRule.getURL() + FIND_PROJECT_URL)
                        .getBody()
                        .as(new TypeRef<HudsonResponse<BitbucketPage<BitbucketProject>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<BitbucketProject> results = response.getData();
        assertThat(results.getSize(), equalTo(0));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        assertThat(results.getValues().size(), equalTo(0));
    }

    @Test
    public void testFindProjectsWithBlankServerId() throws Exception {
        RestAssured.expect()
                .statusCode(400)
                .given()
                .queryParam("serverId", "")
                .get(bitbucketJenkinsRule.getURL() + FIND_PROJECT_URL);
    }

    @Test
    public void testFindProjectsWithInvalidCredentialsId() throws Exception {
        RestAssured.expect()
                .statusCode(400)
                .given()
                .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                .queryParam("credentialsId", "some-invalid-credentials")
                .get(bitbucketJenkinsRule.getURL() + FIND_PROJECT_URL);
    }

    @Test
    public void testFindProjectsWithoutCredentials() throws Exception {
        HudsonResponse<BitbucketPage<BitbucketProject>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("name", "proj")
                        .get(bitbucketJenkinsRule.getURL() + FIND_PROJECT_URL)
                        .getBody()
                        .as(new TypeRef<HudsonResponse<BitbucketPage<BitbucketProject>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<BitbucketProject> results = response.getData();
        assertThat(results.getSize(), equalTo(0));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        assertThat(results.getValues().size(), equalTo(0));
    }

    @Test
    public void testFindProjectsWithoutQuery() throws Exception {
        HudsonResponse<BitbucketPage<BitbucketProject>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("credentialsId", bitbucketJenkinsRule.getCredentialsId())
                        .get(bitbucketJenkinsRule.getURL() + FIND_PROJECT_URL)
                        .getBody()
                        .as(new TypeRef<HudsonResponse<BitbucketPage<BitbucketProject>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<BitbucketProject> results = response.getData();
        assertThat(results.getSize(), equalTo(1));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        List<BitbucketProject> values = results.getValues();
        assertThat(values.size(), equalTo(1));
        BitbucketProject project = values.get(0);
        assertThat(project.getKey(), equalTo("PROJECT_1"));
        assertThat(project.getName(), equalTo("Project 1"));
    }

    @Test
    public void testFindProjectsWithoutServerId() throws Exception {
        RestAssured.expect()
                .statusCode(400)
                .given()
                .get(bitbucketJenkinsRule.getURL() + BITBUCKET_SERVER_SEARCH_URL + "/findProjects");
    }

    @Ignore("BBSDEV-20424")
    @Test
    public void testFindRepo() throws Exception {
        HudsonResponse<BitbucketPage<JenkinsBitbucketRepository>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("credentialsId", bitbucketJenkinsRule.getCredentialsId())
                        .queryParam("projectKey", "PROJECT_1")
                        .queryParam("filter", "rep")
                        .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL)
                        .getBody()
                        .as(
                                new TypeRef<
                                        HudsonResponse<
                                                BitbucketPage<JenkinsBitbucketRepository>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<JenkinsBitbucketRepository> results = response.getData();
        assertThat(results.getSize(), equalTo(1));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        List<JenkinsBitbucketRepository> values = results.getValues();
        assertThat(values.size(), equalTo(1));
        JenkinsBitbucketRepository repo = values.get(0);
        assertThat(repo.getSlug(), equalTo("rep_1"));
        assertThat(repo.getName(), equalTo("rep_1"));
        assertThat(repo.getCloneUrls().size(), equalTo(2));
        assertThat(repo.getCloneUrls().get(0).getName(), equalTo("http"));
        assertThat(
                repo.getCloneUrls().get(0).getHref(),
                equalTo(
                        bitbucketJenkinsRule.getBitbucketServer().getBaseUrl()
                                + "/scm/project_1/rep_1.git"));
        BitbucketProject project = repo.getProject();
        assertThat(project.getKey(), equalTo("PROJECT_1"));
        assertThat(project.getName(), equalTo("Project 1"));
    }

    @Ignore("BBSDEV-20424")
    @Test
    public void testFindReposWithBlankCredentialsId() throws Exception {
        HudsonResponse<BitbucketPage<JenkinsBitbucketRepository>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("credentialsId", "")
                        .queryParam("projectKey", "PROJECT_1")
                        .queryParam("filter", "rep")
                        .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL)
                        .getBody()
                        .as(
                                new TypeRef<
                                        HudsonResponse<
                                                BitbucketPage<JenkinsBitbucketRepository>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<JenkinsBitbucketRepository> results = response.getData();
        assertThat(results.getSize(), equalTo(1));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        List<JenkinsBitbucketRepository> values = results.getValues();
        assertThat(values.size(), equalTo(1));
        JenkinsBitbucketRepository repo = values.get(0);
        assertThat(repo.getSlug(), equalTo("rep_1"));
        assertThat(repo.getName(), equalTo("rep_1"));
        assertThat(repo.getCloneUrls().size(), equalTo(2));
        assertThat(repo.getCloneUrls().get(0).getName(), equalTo("http"));
        assertThat(
                repo.getCloneUrls().get(0).getHref(),
                equalTo(
                        bitbucketJenkinsRule.getBitbucketServer().getBaseUrl()
                                + "/scm/project_1/rep_1.git"));
        BitbucketProject project = repo.getProject();
        assertThat(project.getKey(), equalTo("PROJECT_1"));
        assertThat(project.getName(), equalTo("Project 1"));
    }

    @Test
    public void testFindReposWithBlankServerId() throws Exception {
        RestAssured.expect()
                .statusCode(400)
                .given()
                .queryParam("serverId", "")
                .queryParam("credentialsId", bitbucketJenkinsRule.getCredentialsId())
                .queryParam("projectKey", "PROJECT_1")
                .queryParam("filter", "rep")
                .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL);
    }

    @Test
    public void testFindReposWithInvalidCredentialsId() throws Exception {
        RestAssured.expect()
                .statusCode(400)
                .given()
                .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                .queryParam("credentialsId", "some-invalid-credentials")
                .queryParam("projectKey", "PROJECT_1")
                .queryParam("filter", "rep")
                .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL);
    }

    @Ignore("BBSDEV-20424")
    @Test
    public void testFindReposWithoutCredentials() throws Exception {
        HudsonResponse<BitbucketPage<JenkinsBitbucketRepository>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("projectKey", "PROJECT_1")
                        .queryParam("filter", "rep")
                        .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL)
                        .getBody()
                        .as(
                                new TypeRef<
                                        HudsonResponse<
                                                BitbucketPage<JenkinsBitbucketRepository>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<JenkinsBitbucketRepository> results = response.getData();
        assertThat(results.getSize(), equalTo(1));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        List<JenkinsBitbucketRepository> values = results.getValues();
        assertThat(values.size(), equalTo(1));
        JenkinsBitbucketRepository repo = values.get(0);
        assertThat(repo.getSlug(), equalTo("rep_1"));
        assertThat(repo.getName(), equalTo("rep_1"));
        assertThat(repo.getCloneUrls().size(), equalTo(2));
        assertThat(repo.getCloneUrls().get(0).getName(), equalTo("http"));
        assertThat(
                repo.getCloneUrls().get(0).getHref(),
                equalTo(
                        bitbucketJenkinsRule.getBitbucketServer().getBaseUrl()
                                + "/scm/project_1/rep_1.git"));
        BitbucketProject project = repo.getProject();
        assertThat(project.getKey(), equalTo("PROJECT_1"));
        assertThat(project.getName(), equalTo("Project 1"));
    }

    @Test
    public void testFindReposWithoutProjectKey() throws Exception {
        RestAssured.expect()
                .statusCode(400)
                .given()
                .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                .queryParam("credentialsId", bitbucketJenkinsRule.getCredentialsId())
                .queryParam("filter", "rep")
                .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL);
    }

    @Ignore("BBSDEV-20424")
    @Test
    public void testFindReposWithoutQuery() throws Exception {
        HudsonResponse<BitbucketPage<JenkinsBitbucketRepository>> response =
                RestAssured.expect()
                        .statusCode(200)
                        .log()
                        .ifError()
                        .given()
                        .queryParam("serverId", bitbucketJenkinsRule.getServerId())
                        .queryParam("credentialsId", bitbucketJenkinsRule.getCredentialsId())
                        .queryParam("projectKey", "PROJECT_1")
                        .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL)
                        .getBody()
                        .as(
                                new TypeRef<
                                        HudsonResponse<
                                                BitbucketPage<JenkinsBitbucketRepository>>>() {});
        assertThat(response.getStatus(), equalTo("ok"));
        BitbucketPage<JenkinsBitbucketRepository> results = response.getData();
        assertThat(results.getSize(), equalTo(1));
        assertThat(results.getStart(), equalTo(0));
        assertThat(results.getLimit(), equalTo(25));
        List<JenkinsBitbucketRepository> values = results.getValues();
        assertThat(values.size(), equalTo(1));
        JenkinsBitbucketRepository repo = values.get(0);
        assertThat(repo.getSlug(), equalTo("rep_1"));
        assertThat(repo.getName(), equalTo("rep_1"));
        assertThat(repo.getCloneUrls().size(), equalTo(2));
        assertThat(repo.getCloneUrls().get(0).getName(), equalTo("http"));
        assertThat(
                repo.getCloneUrls().get(0).getHref(),
                equalTo(
                        bitbucketJenkinsRule.getBitbucketServer().getBaseUrl()
                                + "/scm/project_1/rep_1.git"));
        BitbucketProject project = repo.getProject();
        assertThat(project.getKey(), equalTo("PROJECT_1"));
        assertThat(project.getName(), equalTo("Project 1"));
    }

    @Test
    public void testFindReposWithoutServerId() throws Exception {
        RestAssured.expect()
                .statusCode(400)
                .given()
                .queryParam("credentialsId", bitbucketJenkinsRule.getCredentialsId())
                .queryParam("projectKey", "PROJECT_1")
                .get(bitbucketJenkinsRule.getURL() + FIND_REPO_URL);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HudsonResponse<T> {

        private T data;
        private String status;

        public T getData() {
            return data;
        }

        public String getStatus() {
            return status;
        }
    }

    // We wrap the actual BitbucketRepository response and hide the 'links' field behind a
    // 'cloneUrls' field so the
    // response from Jenkins is actually different to the one from Bitbucket
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JenkinsBitbucketRepository {

        private List<BitbucketNamedLink> cloneUrls;
        private String name;
        private BitbucketProject project;
        private String slug;

        public List<BitbucketNamedLink> getCloneUrls() {
            return cloneUrls;
        }

        public String getName() {
            return name;
        }

        public BitbucketProject getProject() {
            return project;
        }

        public String getSlug() {
            return slug;
        }
    }
}
