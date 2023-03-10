/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.fastlane.tasks

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Requires
import spock.lang.Unroll
import static com.wooga.gradle.test.writers.PropertySetInvocation.getMethod
import static com.wooga.gradle.test.writers.PropertySetInvocation.getProviderSet
import static com.wooga.gradle.test.writers.PropertySetInvocation.getSetter

/**
 * The test examples in this class are not 100% integration/functional tests.
 *
 * We can't run the real fastlane and connect to apple because there is no easy way to setup and maintain a test app and
 * account with necessary credentials. We only test the invocation of fastlane and its parameters.
 */
@Requires({ PlatformUtils.mac })
class PilotUploadIntegrationSpec extends AbstractFastlaneTaskIntegrationSpec<PilotUpload> {

    def ipaFile = File.createTempFile("mockIpa", ".ipa")

    String workingFastlaneTaskConfig = """
        task("${subjectUnderTestName}", type: ${subjectUnderTestTypeName}) {
            ipa = file("${PlatformUtils.escapedPath(ipaFile.path)}")
        }
        """.stripIndent()

    @Unroll("property #property #valueMessage sets argument #expectedCommandlineFlag")
    def "constructs arguments"() {
        given: "a set property"
        if (setMethod != _) {
            buildFile << """
            ${setMethod.compose(subjectUnderTestName + "." + property, value)}
            """.stripIndent()
        }

        // TODO: Refactor
        and: "a substitution"
        expectedCommandlineFlag = substitutePath(expectedCommandlineFlag, rawValue, type)

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.arguments", ".get().join(\" \")")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        outputContains(result, expectedCommandlineFlag)

        where:
        property              | setMethod   | rawValue                   | type           || expectedCommandlineFlag
        "devPortalTeamId"     | providerSet | "test"                     | "String"       || "--dev_portal_team_id test"
        "itcProvider"         | providerSet | "iphone"                   | "String"       || "--itc_provider iphone"
        "ipa"                 | providerSet | "/path/to/test2.ipa"       | "File"         || "--ipa /path/to/test2.ipa"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @Unroll("property #property #valueMessage #expectedCommandlineFlag")
    def "fastlane cli flags"() {
        given: "a set property"
        if (setMethod != _) {
            buildFile << """
            ${subjectUnderTestName}.${setMethod}($value)
            """.stripIndent()
        }

        // TODO: Refactor
        and: "a substitution"
        expectedCommandlineFlag = substitutePath(expectedCommandlineFlag, rawValue, type)

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.arguments", ".get().join(\" \")")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        outputContains(result, expectedCommandlineFlag) == rawValue

        where:
        property                        | setMethod                           | rawValue || expectedCommandlineFlag
        "skipSubmission"                | "skipSubmission.set"                | true     || "--skip_submission"
        "skipSubmission"                | "skipSubmission.set"                | false    || "--skip_submission"
        "skipSubmission"                | _                                   | false    || "--skip_submission"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | true     || "--skip_waiting_for_build_processing"
        "skipWaitingForBuildProcessing" | "skipWaitingForBuildProcessing.set" | false    || "--skip_waiting_for_build_processing"
        "skipWaitingForBuildProcessing" | _                                   | false    || "--skip_waiting_for_build_processing"
        type = "Boolean"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = rawValue ? "with value ${value} sets flag" : "without value will not set flag"
    }

    @Unroll("property #property #valueMessage sets environment #expectedEnvironmentPair")
    def "constructs process environment"() {
        given: "a task to read the build arguments"
        buildFile << """
            task("readValue") {
                doLast {
                    println("arguments: " + ${subjectUnderTestName}.environment.get().collect {k,v -> k + '=' + v}.join("\\n"))
                }
            }
        """.stripIndent()

        and: "a set property"
        if (setMethod != _) {
            buildFile << """
            ${subjectUnderTestName}.${setMethod}($value)
            """.stripIndent()
        }

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, expectedEnvironmentPair)

        where:
        property   | setMethod      | rawValue      | type     || expectedEnvironmentPair
        "password" | "password.set" | "secretValue" | "String" || "FASTLANE_PASSWORD=secretValue"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @Unroll("can set property #property with #setMethod and type #type")
    def "can set property"() {
        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, get, set).matches(rawValue)

        where:
        property                        | setMethod   | rawValue             | type
        "appIdentifier"                 | method      | "com.test.app1"      | "String"
        "appIdentifier"                 | method      | "com.test.app2"      | "Provider<String>"
        "appIdentifier"                 | providerSet | "com.test.app1"      | "String"
        "appIdentifier"                 | providerSet | "com.test.app2"      | "Provider<String>"
        "appIdentifier"                 | setter      | "com.test.app3"      | "String"
        "appIdentifier"                 | setter      | "com.test.app4"      | "Provider<String>"

        "teamId"                        | method      | "1234561"            | "String"
        "teamId"                        | method      | "1234562"            | "Provider<String>"
        "teamId"                        | providerSet | "1234561"            | "String"
        "teamId"                        | providerSet | "1234562"            | "Provider<String>"
        "teamId"                        | setter      | "1234563"            | "String"
        "teamId"                        | setter      | "1234564"            | "Provider<String>"

        "devPortalTeamId"               | method      | "1234561"            | "String"
        "devPortalTeamId"               | method      | "1234562"            | "Provider<String>"
        "devPortalTeamId"               | providerSet | "1234561"            | "String"
        "devPortalTeamId"               | providerSet | "1234562"            | "Provider<String>"
        "devPortalTeamId"               | setter      | "1234563"            | "String"
        "devPortalTeamId"               | setter      | "1234564"            | "Provider<String>"

        "teamName"                      | method      | "someTeam1"          | "String"
        "teamName"                      | method      | "someTeam2"          | "Provider<String>"
        "teamName"                      | providerSet | "someTeam3"          | "String"
        "teamName"                      | providerSet | "someTeam4"          | "Provider<String>"
        "teamName"                      | setter      | "someTeam5"          | "String"
        "teamName"                      | setter      | "someTeam6"          | "Provider<String>"

        "username"                      | method      | "someName1"          | "String"
        "username"                      | method      | "someName2"          | "Provider<String>"
        "username"                      | providerSet | "someName3"          | "String"
        "username"                      | providerSet | "someName4"          | "Provider<String>"
        "username"                      | setter      | "someName5"          | "String"
        "username"                      | setter      | "someName6"          | "Provider<String>"

        "password"                      | method      | "1234561"            | "String"
        "password"                      | method      | "1234562"            | "Provider<String>"
        "password"                      | providerSet | "1234561"            | "String"
        "password"                      | providerSet | "1234562"            | "Provider<String>"
        "password"                      | setter      | "1234563"            | "String"
        "password"                      | setter      | "1234564"            | "Provider<String>"

        "itcProvider"                   | method      | "test1"              | "String"
        "itcProvider"                   | method      | "test2"              | "Provider<String>"
        "itcProvider"                   | providerSet | "test1"              | "String"
        "itcProvider"                   | providerSet | "test2"              | "Provider<String>"
        "itcProvider"                   | setter      | "test3"              | "String"
        "itcProvider"                   | setter      | "test4"              | "Provider<String>"

        "ipa"                           | method      | "/path/to/test1.ipa" | "File"
        "ipa"                           | method      | "/path/to/test2.ipa" | "Provider<RegularFile>"
        "ipa"                           | providerSet | "/path/to/test3.ipa" | "File"
        "ipa"                           | providerSet | "/path/to/test4.ipa" | "Provider<RegularFile>"
        "ipa"                           | setter      | "/path/to/test5.ipa" | "File"
        "ipa"                           | setter      | "/path/to/test6.ipa" | "Provider<RegularFile>"

        "skipSubmission"                | method      | true                 | "Boolean"
        "skipSubmission"                | method      | false                | "Boolean"
        "skipSubmission"                | method      | true                 | "Provider<Boolean>"
        "skipSubmission"                | method      | false                | "Provider<Boolean>"
        "skipSubmission"                | providerSet | true                 | "Boolean"
        "skipSubmission"                | providerSet | false                | "Boolean"
        "skipSubmission"                | providerSet | true                 | "Provider<Boolean>"
        "skipSubmission"                | providerSet | false                | "Provider<Boolean>"
        "skipSubmission"                | setter      | true                 | "Boolean"
        "skipSubmission"                | setter      | false                | "Boolean"
        "skipSubmission"                | setter      | true                 | "Provider<Boolean>"
        "skipSubmission"                | setter      | false                | "Provider<Boolean>"

        "skipWaitingForBuildProcessing" | method      | true                 | "Boolean"
        "skipWaitingForBuildProcessing" | method      | false                | "Boolean"
        "skipWaitingForBuildProcessing" | method      | true                 | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | method      | false                | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | providerSet | true                 | "Boolean"
        "skipWaitingForBuildProcessing" | providerSet | false                | "Boolean"
        "skipWaitingForBuildProcessing" | providerSet | true                 | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | providerSet | false                | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | setter      | true                 | "Boolean"
        "skipWaitingForBuildProcessing" | setter      | false                | "Boolean"
        "skipWaitingForBuildProcessing" | setter      | true                 | "Provider<Boolean>"
        "skipWaitingForBuildProcessing" | setter      | false                | "Provider<Boolean>"

        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(setMethod)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)
    }

    @Requires({ PlatformUtils.mac })
    def "task is never up-to-date"() {
        given: "call tasks once"
        def r = runTasks(subjectUnderTestName)

        when: "no parameter changes"
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !result.wasUpToDate(subjectUnderTestName)
    }

    def "task skips with no-source when ipa is not set"() {
        given: "call tasks once"
        def result = runTasks(subjectUnderTestName)
        assert !outputContains(result, "Task :${subjectUnderTestName} NO-SOURCE")

        when: "the task with ipa param set to null"
        buildFile << """
        ${subjectUnderTestName}.ipa = null
        """.stripIndent()

        result = runTasksSuccessfully(subjectUnderTestName)

        then:
        outputContains(result, "Task :${subjectUnderTestName} NO-SOURCE")
    }
}
