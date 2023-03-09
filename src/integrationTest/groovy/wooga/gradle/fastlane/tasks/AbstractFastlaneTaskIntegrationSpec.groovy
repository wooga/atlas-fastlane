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
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.TaskIntegrationSpec
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.fastlane.FastlaneIntegrationSpec

import static com.wooga.gradle.test.writers.PropertySetInvocation.getMethod
import static com.wooga.gradle.test.writers.PropertySetInvocation.getProviderSet
import static com.wooga.gradle.test.writers.PropertySetInvocation.getSetter

abstract class AbstractFastlaneTaskIntegrationSpec<T extends AbstractFastlaneTask> extends FastlaneIntegrationSpec implements TaskIntegrationSpec<T> {

    abstract String getWorkingFastlaneTaskConfig()

    def setup() {
        buildFile << workingFastlaneTaskConfig
        //set a mock executable for the tests
        buildFile << """
        ${subjectUnderTestName}.setExecutable(${wrapValueBasedOnType(fastlaneMock, "File")})
        """.stripIndent()
    }

    @Unroll("property #property #valueMessage sets argument #expectedCommandlineFlag")
    @Requires({ PlatformUtils.mac })
    def "fastlane cli arguments"() {
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
        "username"            | providerSet | "test"                     | "String"       || "--username ${rawValue}"
        "teamId"              | providerSet | "test"                     | "String"       || "--team_id ${rawValue}"
        "teamName"            | providerSet | "test"                     | "String"       || "--team_name ${rawValue}"
        "apiKeyPath"          | providerSet | "/path/to/key.json"        | "File"         || "--api_key_path ${rawValue}"
        "appIdentifier"       | providerSet | "com.test.app"             | "String"       || "--app_identifier ${rawValue}"
        "apiKey"              | providerSet | "test"                     | "String"       || "--api_key ${rawValue}"
        "additionalArguments" | setter      | ["--verbose", "--foo bar"] | "List<String>" || "--verbose --foo bar"
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @Unroll("can set property #property with #setMethod and type #type")
    @Requires({ PlatformUtils.mac })
    def "can set property #property with #setMethod and type #type base"() {
        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, get, set).matches(rawValue)


        where:
        property     | setMethod   | rawValue                       | type
        "logFile"    | method      | osPath("/some/path/test1.log") | "File"
        "logFile"    | method      | osPath("/some/path/test2.log") | "Provider<RegularFile>"
        "logFile"    | providerSet | osPath("/some/path/test3.log") | "File"
        "logFile"    | providerSet | osPath("/some/path/test4.log") | "Provider<RegularFile>"
        "logFile"    | setter      | osPath("/some/path/test5.log") | "File"
        "logFile"    | setter      | osPath("/some/path/test6.log") | "Provider<RegularFile>"

        "apiKeyPath" | method      | osPath("/some/path/key1.json") | "File"
        "apiKeyPath" | method      | osPath("/some/path/key2.json") | "Provider<RegularFile>"
        "apiKeyPath" | providerSet | osPath("/some/path/key3.json") | "File"
        "apiKeyPath" | providerSet | osPath("/some/path/key4.json") | "Provider<RegularFile>"
        "apiKeyPath" | setter      | osPath("/some/path/key5.json") | "File"
        "apiKeyPath" | setter      | osPath("/some/path/key6.json") | "Provider<RegularFile>"

        "apiKey"     | method      | "name1"                        | "String"
        "apiKey"     | method      | "name2"                        | "Provider<String>"
        "apiKey"     | providerSet | "name3"                        | "String"
        "apiKey"     | providerSet | "name4"                        | "Provider<String>"
        "apiKey"     | setter      | "name5"                        | "String"
        "apiKey"     | setter      | "name6"                        | "Provider<String>"

        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(setMethod)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)
    }

    @Requires({ PlatformUtils.mac })
    @Unroll("can set property #property with #setMethod and type #type")
    def "can set property #property with #setMethod and type #type base2"() {
        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, get, set).matches(rawValue)

        where:
        property              | setMethod   | rawValue                      | type
        "executableName"      | providerSet | "fastlane_3"                  | "String"
        "executableName"      | providerSet | "fastlane_4"                  | "Provider<String>"
        "executableName"      | setter      | "fastlane_5"                  | "String"
        "executableName"      | setter      | "fastlane_6"                  | "Provider<String>"

        "executableDirectory" | providerSet | osPath("/path/to/fastlane_1") | "File"
        "executableDirectory" | providerSet | osPath("/path/to/fastlane_4") | "Provider<Directory>"
        "executableDirectory" | setter      | osPath("/path/to/fastlane_5") | "File"
        "executableDirectory" | setter      | osPath("/path/to/fastlane_6") | "Provider<Directory>"

        "executable"          | setter      | "fastlane_5"                  | "String"
        "executable"          | setter      | osPath("/path/to/fastlane_5") | "String"
        "executable"          | setter      | osPath("/path/to/fastlane_5") | "File"
        "executable"          | setter      | osPath("/path/to/fastlane_6") | "Provider<RegularFile>"
        "executable"          | setter      | osPath("/path/to/fastlane_6") | "Provider<File>"
        "executable"          | setter      | "fastlane_6"                  | "Provider<String>"
        "executable"          | setter      | osPath("/path/to/fastlane_6") | "Provider<String>"
        "appIdentifier"       | method      | "com.test.app1"               | "String"
        "appIdentifier"       | method      | "com.test.app2"               | "Provider<String>"
        "appIdentifier"       | providerSet | "com.test.app1"               | "String"
        "appIdentifier"       | providerSet | "com.test.app2"               | "Provider<String>"
        "appIdentifier"       | setter      | "com.test.app3"               | "String"
        "appIdentifier"       | setter      | "com.test.app4"               | "Provider<String>"

        "teamId"              | method      | "1234561"                     | "String"
        "teamId"              | method      | "1234562"                     | "Provider<String>"
        "teamId"              | providerSet | "1234561"                     | "String"
        "teamId"              | providerSet | "1234562"                     | "Provider<String>"
        "teamId"              | setter      | "1234563"                     | "String"
        "teamId"              | setter      | "1234564"                     | "Provider<String>"

        "teamName"            | method      | "someTeam1"                   | "String"
        "teamName"            | method      | "someTeam2"                   | "Provider<String>"
        "teamName"            | providerSet | "someTeam3"                   | "String"
        "teamName"            | providerSet | "someTeam4"                   | "Provider<String>"
        "teamName"            | setter      | "someTeam5"                   | "String"
        "teamName"            | setter      | "someTeam6"                   | "Provider<String>"

        "username"            | method      | "someName1"                   | "String"
        "username"            | method      | "someName2"                   | "Provider<String>"
        "username"            | providerSet | "someName3"                   | "String"
        "username"            | providerSet | "someName4"                   | "Provider<String>"
        "username"            | setter      | "someName5"                   | "String"
        "username"            | setter      | "someName6"                   | "Provider<String>"

        "password"            | method      | "1234561"                     | "String"
        "password"            | method      | "1234562"                     | "Provider<String>"
        "password"            | providerSet | "1234561"                     | "String"
        "password"            | providerSet | "1234562"                     | "Provider<String>"
        "password"            | setter      | "1234563"                     | "String"
        "password"            | setter      | "1234564"                     | "Provider<String>"

        "apiKeyPath"          | method      | "/some/path/1.json"           | "File"
        "apiKeyPath"          | method      | "/some/path/2.json"           | "Provider<RegularFile>"
        "apiKeyPath"          | providerSet | "/some/path/3.json"           | "File"
        "apiKeyPath"          | providerSet | "/some/path/4.json"           | "Provider<RegularFile>"
        "apiKeyPath"          | setter      | "/some/path/5.json"           | "File"
        "apiKeyPath"          | setter      | "/some/path/6.json"           | "Provider<RegularFile>"

        set = new PropertySetterWriter(subjectUnderTestName, property)
                .set(rawValue, type)
                .toScript(setMethod)
                .serialize(wrapValueFallback)

        get = new PropertyGetterTaskWriter(set)
    }

    @Unroll("property #property #valueMessage sets environment #expectedEnvironmentPair")
    @Requires({ PlatformUtils.mac })
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
        "password"       | "password.set"       | "secretValue" | "String"  || ["FASTLANE_PASSWORD": "secretValue"]
        value = wrapValueBasedOnType(rawValue, type)
        valueMessage = (rawValue != _) ? "with value ${value}" : "without value"
    }

    @Requires({ PlatformUtils.mac })
    def "task writes log output"() {
        given: "a future log file"
        def logFile = new File(projectDir, "build/logs/${subjectUnderTestName}.log")
        assert !logFile.exists()

        and: "the logfile configured"
        buildFile << """${subjectUnderTestName}.logFile = ${wrapValueBasedOnType(logFile.path, File)}"""

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        logFile.exists()
        !logFile.text.empty
    }

    @Requires({ PlatformUtils.mac })
    def "prints fastlane log to console and logfile"() {
        given: "a future log file"
        def logFile = new File(projectDir, "build/logs/${subjectUnderTestName}.log")
        assert !logFile.exists()

        and: "the logfile configured"
        appendToSubjectTask("""
            logToStdout = true
            logFile = ${wrapValueBasedOnType(logFile.path, File)} 
        """.stripIndent())
        buildFile << """${subjectUnderTestName}.logFile = ${wrapValueBasedOnType(logFile.path, File)}"""

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        outputContains(result, logFile.text)
    }
}
