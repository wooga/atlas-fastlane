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
import com.wooga.gradle.test.BatchmodeWrapper
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Unroll

import static com.wooga.gradle.test.writers.PropertySetInvocation.*

/**
 * The test examples in this class are not 100% integration/functional tests.
 *
 * We can't run the real fastlane and connect to apple because there is no easy way to setup and maintain a test app and
 * account with necessary credentials. We only test the invocation of fastlane and its parameters.
 */
@Requires({ os.macOs })
abstract class SighRenewBaseIntegrationSpec<T extends AbstractFastlaneTask> extends AbstractFastlaneTaskIntegrationSpec<T> {

    String workingFastlaneTaskConfig = """
        task("${subjectUnderTestName}", type: ${subjectUnderTestTypeName}) {
            appIdentifier = 'test'
            teamId = "fakeTeamId"
            fileName = 'test.mobileprovisioning'
            destinationDir = file('build')
        }
        """.stripIndent()

    @IgnoreIf(value = { instance.class == SighRenewBatchIntegrationSpec && data.property == "appIdentifier" }, reason = "appIdentifier property is not used in SighRenewBatch task")
    @IgnoreIf(value = { instance.class == SighRenewBatchIntegrationSpec && data.property == "fileName" }, reason = "fileName property is not used in SighRenewBatch task")
    @Unroll("property #property #valueMessage sets argument #expectedCommandlineFlag")
    def "fastlane cli arguments"() {
        given: "a set property"
        if (setMethod != _) {
            buildFile << """
            ${setMethod.compose(subjectUnderTestName + "." + property, value)}
            """.stripIndent()
        }

        and: "a fix for an unknown file"
        if (property == "apiKeyPath") {
            createFile(rawValue.toString())
        }

        // TODO: Refactor
        and: "a substitution"
        expectedCommandlineFlag = substitutePath(expectedCommandlineFlag, rawValue, type)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        BatchmodeWrapper.containsArguments(result.standardOutput, expectedCommandlineFlag)

        where:
        property              | setMethod   | rawValue                   | type           || expectedCommandlineFlag
        "provisioningName"    | providerSet | "test"                     | "String"       || "--provisioning_name ${rawValue}"
        "destinationDir"      | providerSet | "some/path"                | "File"         || "--output_path some/path"
        "certId"              | providerSet | "testUser"                 | "String"       || "--cert_id ${rawValue}"
        "certOwnerName"       | providerSet | "testUser"                 | "String"       || "--cert_owner_name ${rawValue}"
        "fileName"            | providerSet | "test2.mobileprovisioning" | "String"       || "--filename ${rawValue}"
        "platform"            | providerSet | "platform"                 | "String"       || "--platform ${rawValue}"
        "templateName"        | providerSet | "name"                     | "String"       || "--template_name ${rawValue}"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @IgnoreIf(value = { instance.class == SighRenewBatchIntegrationSpec && data.property == "appIdentifier" }, reason = "appIdentifier property is not used in SighRenewBatch task")
    @IgnoreIf(value = { instance.class == SighRenewBatchIntegrationSpec && data.property == "fileName" }, reason = "fileName property is not used in SighRenewBatch task")
    @Unroll("property #property #valueMessage #expectedCommandlineFlag")
    def "fastlane cli flags"() {
        given: "a set property"
        if (setMethod != _) {
            buildFile << """
            ${setMethod.compose(subjectUnderTestName + "." + property, value)}
            """.stripIndent()
        }

        and: "a fix for an unknown file"
        if (property == "apiKeyPath") {
            createFile(rawValue.toString())
        }

        // TODO: Refactor
        and: "a substitution"
        expectedCommandlineFlag = substitutePath(expectedCommandlineFlag, rawValue, type)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        BatchmodeWrapper.containsArguments(result.standardOutput, expectedCommandlineFlag) == rawValue

        where:
        property                          | setMethod   | rawValue | type      || expectedCommandlineFlag
        "adhoc"                           | providerSet | true     | "Boolean" || "--adhoc"
        "adhoc"                           | providerSet | false    | "Boolean" || "--adhoc"
        "adhoc"                           | _           | false    | "Boolean" || "--adhoc"
        "development"                     | providerSet | true     | "Boolean" || "--development"
        "development"                     | providerSet | false    | "Boolean" || "--development"
        "development"                     | _           | false    | "Boolean" || "--development"
        "skipInstall"                     | providerSet | true     | "Boolean" || "--skip_install"
        "skipInstall"                     | providerSet | false    | "Boolean" || "--skip_install"
        "skipInstall"                     | _           | false    | "Boolean" || "--skip_install"
        "force"                           | providerSet | true     | "Boolean" || "--force"
        "force"                           | providerSet | false    | "Boolean" || "--force"
        "force"                           | _           | false    | "Boolean" || "--force"
        "includeMacInProfiles"            | providerSet | true     | "Boolean" || "--include_mac_in_profiles"
        "includeMacInProfiles"            | providerSet | false    | "Boolean" || "--include_mac_in_profiles"
        "includeMacInProfiles"            | _           | false    | "Boolean" || "--include_mac_in_profiles"
        "ignoreProfilesWithDifferentName" | providerSet | true     | "Boolean" || "--ignore_profiles_with_different_name"
        "ignoreProfilesWithDifferentName" | providerSet | false    | "Boolean" || "--ignore_profiles_with_different_name"
        "ignoreProfilesWithDifferentName" | _           | false    | "Boolean" || "--ignore_profiles_with_different_name"
        "skipFetchProfiles"               | providerSet | true     | "Boolean" || "--skip_fetch_profiles"
        "skipFetchProfiles"               | providerSet | false    | "Boolean" || "--skip_fetch_profiles"
        "skipFetchProfiles"               | _           | false    | "Boolean" || "--skip_fetch_profiles"
        "includeAllCertificates"          | providerSet | true     | "Boolean" || "--include_all_certificates"
        "includeAllCertificates"          | providerSet | false    | "Boolean" || "--include_all_certificates"
        "includeAllCertificates"          | _           | false    | "Boolean" || "--include_all_certificates"
        "skipCertificateVerification"     | providerSet | true     | "Boolean" || "--skip_certificate_verification"
        "skipCertificateVerification"     | providerSet | false    | "Boolean" || "--skip_certificate_verification"
        "skipCertificateVerification"     | _           | false    | "Boolean" || "--skip_certificate_verification"
        "readOnly"                        | providerSet | true     | "Boolean" || "--readonly"
        "readOnly"                        | providerSet | false    | "Boolean" || "--readonly"
        "readOnly"                        | _           | false    | "Boolean" || "--readonly"
        "failOnNameTaken"                 | providerSet | true     | "Boolean" || "--fail_on_name_taken"
        "failOnNameTaken"                 | providerSet | false    | "Boolean" || "--fail_on_name_taken"
        "failOnNameTaken"                 | _           | false    | "Boolean" || "--fail_on_name_taken"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = rawValue ? "with value ${value} sets flag" : "without value will not set flag"
    }


    @Unroll("can set property #property with #setMethod and type #type")
    @Requires({ PlatformUtils.mac })
    def "can set property SighRenew"() {
        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, get, set).matches(rawValue)

        where:
        property                          | setMethod   | rawValue            | type
        "appIdentifier"                   | method      | "com.test.app1"     | "String"
        "appIdentifier"                   | method      | "com.test.app2"     | "Provider<String>"
        "appIdentifier"                   | providerSet | "com.test.app1"     | "String"
        "appIdentifier"                   | providerSet | "com.test.app2"     | "Provider<String>"
        "appIdentifier"                   | setter      | "com.test.app3"     | "String"
        "appIdentifier"                   | setter      | "com.test.app4"     | "Provider<String>"

        "teamId"                          | method      | "1234561"           | "String"
        "teamId"                          | method      | "1234562"           | "Provider<String>"
        "teamId"                          | providerSet | "1234561"           | "String"
        "teamId"                          | providerSet | "1234562"           | "Provider<String>"
        "teamId"                          | setter      | "1234563"           | "String"
        "teamId"                          | setter      | "1234564"           | "Provider<String>"

        "teamName"                        | method      | "someTeam1"         | "String"
        "teamName"                        | method      | "someTeam2"         | "Provider<String>"
        "teamName"                        | providerSet | "someTeam3"         | "String"
        "teamName"                        | providerSet | "someTeam4"         | "Provider<String>"
        "teamName"                        | setter      | "someTeam5"         | "String"
        "teamName"                        | setter      | "someTeam6"         | "Provider<String>"

        "username"                        | method      | "someName1"         | "String"
        "username"                        | method      | "someName2"         | "Provider<String>"
        "username"                        | providerSet | "someName3"         | "String"
        "username"                        | providerSet | "someName4"         | "Provider<String>"
        "username"                        | setter      | "someName5"         | "String"
        "username"                        | setter      | "someName6"         | "Provider<String>"

        "password"                        | method      | "1234561"           | "String"
        "password"                        | method      | "1234562"           | "Provider<String>"
        "password"                        | providerSet | "1234561"           | "String"
        "password"                        | providerSet | "1234562"           | "Provider<String>"
        "password"                        | setter      | "1234563"           | "String"
        "password"                        | setter      | "1234564"           | "Provider<String>"

        "fileName"                        | method      | "name1"             | "String"
        "fileName"                        | method      | "name2"             | "Provider<String>"
        "fileName"                        | providerSet | "name3"             | "String"
        "fileName"                        | providerSet | "name4"             | "Provider<String>"
        "fileName"                        | setter      | "name5"             | "String"
        "fileName"                        | setter      | "name6"             | "Provider<String>"

        "provisioningName"                | method      | "name1"             | "String"
        "provisioningName"                | method      | "name2"             | "Provider<String>"
        "provisioningName"                | providerSet | "name3"             | "String"
        "provisioningName"                | providerSet | "name4"             | "Provider<String>"
        "provisioningName"                | setter      | "name5"             | "String"
        "provisioningName"                | setter      | "name6"             | "Provider<String>"

        "certId"                          | method      | "name1"             | "String"
        "certId"                          | method      | "name2"             | "Provider<String>"
        "certId"                          | providerSet | "name3"             | "String"
        "certId"                          | providerSet | "name4"             | "Provider<String>"
        "certId"                          | setter      | "name5"             | "String"
        "certId"                          | setter      | "name6"             | "Provider<String>"

        "certOwnerName"                   | method      | "name1"             | "String"
        "certOwnerName"                   | method      | "name2"             | "Provider<String>"
        "certOwnerName"                   | providerSet | "name3"             | "String"
        "certOwnerName"                   | providerSet | "name4"             | "Provider<String>"
        "certOwnerName"                   | setter      | "name5"             | "String"
        "certOwnerName"                   | setter      | "name6"             | "Provider<String>"

        "platform"                        | method      | "name1"             | "String"
        "platform"                        | method      | "name2"             | "Provider<String>"
        "platform"                        | providerSet | "name3"             | "String"
        "platform"                        | providerSet | "name4"             | "Provider<String>"
        "platform"                        | setter      | "name5"             | "String"
        "platform"                        | setter      | "name6"             | "Provider<String>"

        "adhoc"                           | method      | true                | "Boolean"
        "adhoc"                           | method      | false               | "Boolean"
        "adhoc"                           | method      | true                | "Provider<Boolean>"
        "adhoc"                           | method      | false               | "Provider<Boolean>"
        "adhoc"                           | providerSet | true                | "Boolean"
        "adhoc"                           | providerSet | false               | "Boolean"
        "adhoc"                           | providerSet | true                | "Provider<Boolean>"
        "adhoc"                           | providerSet | false               | "Provider<Boolean>"
        "adhoc"                           | setter      | true                | "Boolean"
        "adhoc"                           | setter      | false               | "Boolean"
        "adhoc"                           | setter      | true                | "Provider<Boolean>"
        "adhoc"                           | setter      | false               | "Provider<Boolean>"

        "includeAllCertificates"          | method      | true                | "Boolean"
        "includeAllCertificates"          | method      | false               | "Boolean"
        "includeAllCertificates"          | method      | true                | "Provider<Boolean>"
        "includeAllCertificates"          | method      | false               | "Provider<Boolean>"
        "includeAllCertificates"          | providerSet | true                | "Boolean"
        "includeAllCertificates"          | providerSet | false               | "Boolean"
        "includeAllCertificates"          | providerSet | true                | "Provider<Boolean>"
        "includeAllCertificates"          | providerSet | false               | "Provider<Boolean>"
        "includeAllCertificates"          | setter      | true                | "Boolean"
        "includeAllCertificates"          | setter      | false               | "Boolean"
        "includeAllCertificates"          | setter      | true                | "Provider<Boolean>"
        "includeAllCertificates"          | setter      | false               | "Provider<Boolean>"

        "readOnly"                        | method      | true                | "Boolean"
        "readOnly"                        | method      | false               | "Boolean"
        "readOnly"                        | method      | true                | "Provider<Boolean>"
        "readOnly"                        | method      | false               | "Provider<Boolean>"
        "readOnly"                        | providerSet | true                | "Boolean"
        "readOnly"                        | providerSet | false               | "Boolean"
        "readOnly"                        | providerSet | true                | "Provider<Boolean>"
        "readOnly"                        | providerSet | false               | "Provider<Boolean>"
        "readOnly"                        | setter      | true                | "Boolean"
        "readOnly"                        | setter      | false               | "Boolean"
        "readOnly"                        | setter      | true                | "Provider<Boolean>"
        "readOnly"                        | setter      | false               | "Provider<Boolean>"

        "skipCertificateVerification"     | method      | true                | "Boolean"
        "skipCertificateVerification"     | method      | false               | "Boolean"
        "skipCertificateVerification"     | method      | true                | "Provider<Boolean>"
        "skipCertificateVerification"     | method      | false               | "Provider<Boolean>"
        "skipCertificateVerification"     | providerSet | true                | "Boolean"
        "skipCertificateVerification"     | providerSet | false               | "Boolean"
        "skipCertificateVerification"     | providerSet | true                | "Provider<Boolean>"
        "skipCertificateVerification"     | providerSet | false               | "Provider<Boolean>"
        "skipCertificateVerification"     | setter      | true                | "Boolean"
        "skipCertificateVerification"     | setter      | false               | "Boolean"
        "skipCertificateVerification"     | setter      | true                | "Provider<Boolean>"
        "skipCertificateVerification"     | setter      | false               | "Provider<Boolean>"

        "failOnNameTaken"                 | method      | true                | "Boolean"
        "failOnNameTaken"                 | method      | false               | "Boolean"
        "failOnNameTaken"                 | method      | true                | "Provider<Boolean>"
        "failOnNameTaken"                 | method      | false               | "Provider<Boolean>"
        "failOnNameTaken"                 | providerSet | true                | "Boolean"
        "failOnNameTaken"                 | providerSet | false               | "Boolean"
        "failOnNameTaken"                 | providerSet | true                | "Provider<Boolean>"
        "failOnNameTaken"                 | providerSet | false               | "Provider<Boolean>"
        "failOnNameTaken"                 | setter      | true                | "Boolean"
        "failOnNameTaken"                 | setter      | false               | "Boolean"
        "failOnNameTaken"                 | setter      | true                | "Provider<Boolean>"
        "failOnNameTaken"                 | setter      | false               | "Provider<Boolean>"

        "skipFetchProfiles"               | method      | true                | "Boolean"
        "skipFetchProfiles"               | method      | false               | "Boolean"
        "skipFetchProfiles"               | method      | true                | "Provider<Boolean>"
        "skipFetchProfiles"               | method      | false               | "Provider<Boolean>"
        "skipFetchProfiles"               | providerSet | true                | "Boolean"
        "skipFetchProfiles"               | providerSet | false               | "Boolean"
        "skipFetchProfiles"               | providerSet | true                | "Provider<Boolean>"
        "skipFetchProfiles"               | providerSet | false               | "Provider<Boolean>"
        "skipFetchProfiles"               | setter      | true                | "Boolean"
        "skipFetchProfiles"               | setter      | false               | "Boolean"
        "skipFetchProfiles"               | setter      | true                | "Provider<Boolean>"
        "skipFetchProfiles"               | setter      | false               | "Provider<Boolean>"

        "skipInstall"                     | method      | true                | "Boolean"
        "skipInstall"                     | method      | false               | "Boolean"
        "skipInstall"                     | method      | true                | "Provider<Boolean>"
        "skipInstall"                     | method      | false               | "Provider<Boolean>"
        "skipInstall"                     | providerSet | true                | "Boolean"
        "skipInstall"                     | providerSet | false               | "Boolean"
        "skipInstall"                     | providerSet | true                | "Provider<Boolean>"
        "skipInstall"                     | providerSet | false               | "Provider<Boolean>"
        "skipInstall"                     | setter      | true                | "Boolean"
        "skipInstall"                     | setter      | false               | "Boolean"
        "skipInstall"                     | setter      | true                | "Provider<Boolean>"

        "force"                           | method      | true                | "Boolean"
        "force"                           | method      | false               | "Boolean"
        "force"                           | method      | true                | "Provider<Boolean>"
        "force"                           | method      | false               | "Provider<Boolean>"
        "force"                           | providerSet | true                | "Boolean"
        "force"                           | providerSet | false               | "Boolean"
        "force"                           | providerSet | true                | "Provider<Boolean>"
        "force"                           | providerSet | false               | "Provider<Boolean>"
        "force"                           | setter      | true                | "Boolean"
        "force"                           | setter      | false               | "Boolean"
        "force"                           | setter      | true                | "Provider<Boolean>"
        "force"                           | setter      | false               | "Provider<Boolean>"
        "force"                           | setter      | false               | "Provider<Boolean>"

        "includeMacInProfiles"            | method      | true                | "Boolean"
        "includeMacInProfiles"            | method      | false               | "Boolean"
        "includeMacInProfiles"            | method      | true                | "Provider<Boolean>"
        "includeMacInProfiles"            | method      | false               | "Provider<Boolean>"
        "includeMacInProfiles"            | providerSet | true                | "Boolean"
        "includeMacInProfiles"            | providerSet | false               | "Boolean"
        "includeMacInProfiles"            | providerSet | true                | "Provider<Boolean>"
        "includeMacInProfiles"            | providerSet | false               | "Provider<Boolean>"
        "includeMacInProfiles"            | setter      | true                | "Boolean"
        "includeMacInProfiles"            | setter      | false               | "Boolean"
        "includeMacInProfiles"            | setter      | true                | "Provider<Boolean>"
        "includeMacInProfiles"            | setter      | false               | "Provider<Boolean>"
        "includeMacInProfiles"            | setter      | false               | "Provider<Boolean>"

        "ignoreProfilesWithDifferentName" | method      | true                | "Boolean"
        "ignoreProfilesWithDifferentName" | method      | false               | "Boolean"
        "ignoreProfilesWithDifferentName" | method      | true                | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | method      | false               | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | providerSet | true                | "Boolean"
        "ignoreProfilesWithDifferentName" | providerSet | false               | "Boolean"
        "ignoreProfilesWithDifferentName" | providerSet | true                | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | providerSet | false               | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | setter      | true                | "Boolean"
        "ignoreProfilesWithDifferentName" | setter      | false               | "Boolean"
        "ignoreProfilesWithDifferentName" | setter      | true                | "Provider<Boolean>"
        "ignoreProfilesWithDifferentName" | setter      | false               | "Provider<Boolean>"

        "destinationDir"                  | method      | "/some/path/1"      | "File"
        "destinationDir"                  | method      | "/some/path/2"      | "Provider<Directory>"
        "destinationDir"                  | providerSet | "/some/path/3"      | "File"
        "destinationDir"                  | providerSet | "/some/path/4"      | "Provider<Directory>"
        "destinationDir"                  | setter      | "/some/path/5"      | "File"
        "destinationDir"                  | setter      | "/some/path/6"      | "Provider<Directory>"

        "apiKeyPath"                      | method      | "/some/path/1.json" | "File"
        "apiKeyPath"                      | method      | "/some/path/2.json" | "Provider<RegularFile>"
        "apiKeyPath"                      | providerSet | "/some/path/3.json" | "File"
        "apiKeyPath"                      | providerSet | "/some/path/4.json" | "Provider<RegularFile>"
        "apiKeyPath"                      | setter      | "/some/path/5.json" | "File"
        "apiKeyPath"                      | setter      | "/some/path/6.json" | "Provider<RegularFile>"

        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(setMethod)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)
    }

    @Unroll("property #property #valueMessage sets environment #expectedEnvironmentPair")
    def "fastlane cli environment"() {
        given: "a set property"
        if (setMethod != _) {
            buildFile << """
            ${subjectUnderTestName}.${setMethod}($value)
            """.stripIndent()
        }

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        BatchmodeWrapper.containsEnvironment(result.standardOutput, expectedEnvironmentPair)

        where:
        property         | setMethod            | rawValue      | type      || expectedEnvironmentPair
        "skip2faUpgrade" | "skip2faUpgrade.set" | true          | "Boolean" || ["SPACESHIP_SKIP_2FA_UPGRADE": "1"]
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @Issue("https://github.com/wooga/atlas-build-unity/issues/38")
    def "task is never up-to-date"() {
        given: "call import tasks once"
        def r = runTasks(subjectUnderTestName)

        when: "no parameter changes"
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !result.wasUpToDate(subjectUnderTestName)
    }
}
