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

package wooga.gradle.fastlane

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew
import wooga.gradle.fastlane.tasks.SighRenewBatch
import wooga.gradle.fastlane.tasks.SighRenewBatchIntegrationSpec

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.test.writers.PropertySetInvocation.*

class FastlanePluginIntegrationSpec extends FastlaneIntegrationSpec {

    def setup() {
        buildFile << """
              group = 'test'
              ${applyPlugin(FastlanePlugin)}
           """.stripIndent()
    }

    @Unroll()
    @IgnoreIf(value = { os.windows && data.property == "apiKeyPath" }, reason = "file based tests are not really working on windows")
    @IgnoreIf(value = { os.windows && data.property == "executableDirectory" }, reason = "file based tests are not really working on windows")
    @IgnoreIf(value = { os.windows && data.property == "executable" }, reason = "file based tests are not really working on windows")
    @Requires({ PlatformUtils.mac })
    def "extension property #property of type #type sets #rawValue when #location"() {
        expect:
        runPropertyQuery(get, set).matches(rawValue)

        where:
        property              | setMethod   | rawValue                      | type                    | location
        "username"            | _           | "someUser1"                   | _                       | PropertyLocation.environment
        "username"            | _           | "someUser2"                   | _                       | PropertyLocation.property
        "username"            | assignment  | "someUser3"                   | "String"                | PropertyLocation.script
        "username"            | assignment  | "someUser4"                   | "Provider<String>"      | PropertyLocation.script
        "username"            | providerSet | "someUser5"                   | "String"                | PropertyLocation.script
        "username"            | providerSet | "someUser6"                   | "Provider<String>"      | PropertyLocation.script
        "username"            | method      | "someUser7"                   | "String"                | PropertyLocation.script
        "username"            | method      | "someUser8"                   | "Provider<String>"      | PropertyLocation.script
        "username"            | _           | null                          | _                       | PropertyLocation.none

        "password"            | _           | "somePassword1"               | _                       | PropertyLocation.environment
        "password"            | _           | "somePassword2"               | _                       | PropertyLocation.property
        "password"            | assignment  | "somePassword3"               | "String"                | PropertyLocation.script
        "password"            | assignment  | "somePassword4"               | "Provider<String>"      | PropertyLocation.script
        "password"            | providerSet | "somePassword5"               | "String"                | PropertyLocation.script
        "password"            | providerSet | "somePassword6"               | "Provider<String>"      | PropertyLocation.script
        "password"            | method      | "somePassword7"               | "String"                | PropertyLocation.script
        "password"            | method      | "somePassword8"               | "Provider<String>"      | PropertyLocation.script
        "password"            | _           | null                          | _                       | PropertyLocation.none

        "apiKeyPath"          | _           | osPath("/path/to/key1.json")  | _                       | PropertyLocation.environment
        "apiKeyPath"          | _           | osPath("/path/to/key2.json")  | "File"                  | PropertyLocation.property
        "apiKeyPath"          | assignment  | osPath("/path/to/key3.json")  | "File"                  | PropertyLocation.script
        "apiKeyPath"          | assignment  | osPath("/path/to/key4.json")  | "Provider<RegularFile>" | PropertyLocation.script
        "apiKeyPath"          | providerSet | osPath("/path/to/key5.json")  | "Provider<RegularFile>" | PropertyLocation.script
        "apiKeyPath"          | providerSet | osPath("/path/to/key6.json")  | "File"                  | PropertyLocation.script
        "apiKeyPath"          | method      | osPath("/path/to/key7.json")  | "Provider<RegularFile>" | PropertyLocation.script
        "apiKeyPath"          | method      | null                          | _                       | PropertyLocation.none

        "apiKey"              | _           | "someAPIKey1"                 | _                       | PropertyLocation.environment
        "apiKey"              | _           | "someAPIKey2"                 | _                       | PropertyLocation.property
        "apiKey"              | assignment  | "someAPIKey3"                 | "String"                | PropertyLocation.script
        "apiKey"              | assignment  | "someAPIKey4"                 | "Provider<String>"      | PropertyLocation.script
        "apiKey"              | providerSet | "someAPIKey5"                 | "String"                | PropertyLocation.script
        "apiKey"              | providerSet | "someAPIKey6"                 | "Provider<String>"      | PropertyLocation.script
        "apiKey"              | method      | "someAPIKey7"                 | "String"                | PropertyLocation.script
        "apiKey"              | method      | "someAPIKey8"                 | "Provider<String>"      | PropertyLocation.script
        "apiKey"              | _           | null                          | _                       | PropertyLocation.none

        "skip2faUpgrade"      | _           | true                          | _                       | PropertyLocation.environment
        "skip2faUpgrade"      | _           | false                         | _                       | PropertyLocation.property
        "skip2faUpgrade"      | assignment  | true                          | "Boolean"               | PropertyLocation.script
        "skip2faUpgrade"      | assignment  | false                         | "Provider<Boolean>"     | PropertyLocation.script
        "skip2faUpgrade"      | providerSet | true                          | "Boolean"               | PropertyLocation.script
        "skip2faUpgrade"      | providerSet | false                         | "Provider<Boolean>"     | PropertyLocation.script
        "skip2faUpgrade"      | method      | true                          | "Boolean"               | PropertyLocation.script
        "skip2faUpgrade"      | method      | false                         | "Provider<Boolean>"     | PropertyLocation.script
        "skip2faUpgrade"      | _           | false                         | _                       | PropertyLocation.none

        "executableName"      | _           | "fastlane_2"                  | _                       | PropertyLocation.environment
        "executableName"      | _           | "fastlane_3"                  | _                       | PropertyLocation.property
        "executableName"      | providerSet | "fastlane_3"                  | "String"                | PropertyLocation.script
        "executableName"      | providerSet | "fastlane_4"                  | "Provider<String>"      | PropertyLocation.script
        "executableName"      | assignment  | "fastlane_5"                  | "String"                | PropertyLocation.script
        "executableName"      | assignment  | "fastlane_6"                  | "Provider<String>"      | PropertyLocation.script
        "executableName"      | _           | "fastlane"                    | _                       | PropertyLocation.none

        "executableDirectory" | _           | osPath("/path/to/fastlane_2") | _                       | PropertyLocation.environment
        "executableDirectory" | _           | osPath("/path/to/fastlane_3") | _                       | PropertyLocation.property
        "executableDirectory" | providerSet | osPath("/path/to/fastlane_3") | "File"                  | PropertyLocation.script
        "executableDirectory" | providerSet | osPath("/path/to/fastlane_4") | "Provider<Directory>"   | PropertyLocation.script
        "executableDirectory" | assignment  | osPath("/path/to/fastlane_5") | "File"                  | PropertyLocation.script
        "executableDirectory" | assignment  | osPath("/path/to/fastlane_6") | "Provider<Directory>"   | PropertyLocation.script
        "executableDirectory" | _           | null                          | _                       | PropertyLocation.none

        "executable"          | assignment  | osPath("/path/to/fastlane_5") | "String"                | PropertyLocation.script
        "executable"          | assignment  | osPath("/path/to/fastlane_6") | "Provider<String>"      | PropertyLocation.script
        "executable"          | _           | "fastlane"                    | _                       | PropertyLocation.none

        extensionName = "fastlane"
        set = new PropertySetterWriter(extensionName, property)
                .serialize(wrapValueFallback)
                .set(rawValue, type)
                .to(location)
                .use(setMethod)

        get = new PropertyGetterTaskWriter(set)
    }

    @Unroll("property #property of type #tasktype.simpleName is bound to property #extensionProperty of extension #extensionName")
    def "task property is connected with extension"() {
        given:
        buildFile << """
            task ${taskName}(type: ${tasktype.name})            
            ${extensionName}.${invocation}
        """.stripIndent()

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
            testValue = testValue.replaceAll("#taskName#", taskName)
        }

        when: ""
        def query = new PropertyQueryTaskWriter("${taskName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property              | extensionProperty     | tasktype       | rawValue                     | expectedValue | type      | useProviderApi
        "username"            | "username"            | SighRenew      | "userName1"                  | _             | "String"  | true
        "username"            | "username"            | SighRenewBatch | "userName1"                  | _             | "String"  | true
        "username"            | "username"            | PilotUpload    | "userName2"                  | _             | "String"  | true

        "executableName"      | "executableName"      | SighRenew      | "fastlane_1"                 | _             | "String"  | true
        "executableName"      | "executableName"      | SighRenewBatch | "fastlane_1"                 | _             | "String"  | true
        "executableName"      | "executableName"      | PilotUpload    | "fastlane_1"                 | _             | "String"  | true

        "executableDirectory" | "executableDirectory" | SighRenew      | osPath("/path/to/")          | _             | "File"    | true
        "executableDirectory" | "executableDirectory" | SighRenewBatch | osPath("/path/to/")          | _             | "File"    | true
        "executableDirectory" | "executableDirectory" | PilotUpload    | osPath("/path/to/")          | _             | "File"    | true

        "password"            | "password"            | SighRenew      | "password1"                  | _             | "String"  | true
        "password"            | "password"            | SighRenewBatch | "password1"                  | _             | "String"  | true
        "password"            | "password"            | PilotUpload    | "password2"                  | _             | "String"  | true

        "apiKeyPath"          | "apiKeyPath"          | SighRenew      | osPath("/path/to/key1.json") | _             | "File"    | true
        "apiKeyPath"          | "apiKeyPath"          | SighRenewBatch | osPath("/path/to/key2.json") | _             | "File"    | true
        "apiKeyPath"          | "apiKeyPath"          | PilotUpload    | osPath("/path/to/key3.json") | _             | "File"    | true

        "apiKey"              | "apiKey"              | SighRenew      | "someAPIKey"                 | _             | "String"  | true
        "apiKey"              | "apiKey"              | SighRenewBatch | "someAPIKey"                 | _             | "String"  | true
        "apiKey"              | "apiKey"              | PilotUpload    | "someAPIKey"                 | _             | "String"  | true

        "skip2faUpgrade"      | "skip2faUpgrade"      | SighRenew      | true                         | _             | "Boolean" | true
        "skip2faUpgrade"      | "skip2faUpgrade"      | SighRenewBatch | true                         | _             | "Boolean" | true
        "skip2faUpgrade"      | "skip2faUpgrade"      | PilotUpload    | true                         | _             | "Boolean" | true

        extensionName = "fastlane"
        taskName = "fastlaneTask"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = "${extensionProperty}.set(${escapedValue})"
        providerInvocation = (useProviderApi) ? ".getOrNull()" : ""
    }
}
