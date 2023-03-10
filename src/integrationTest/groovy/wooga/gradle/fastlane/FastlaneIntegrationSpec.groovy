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

import com.wooga.gradle.test.BatchmodeWrapper
import com.wooga.gradle.test.IntegrationSpec

import java.nio.file.Paths

abstract class FastlaneIntegrationSpec extends IntegrationSpec {

    File fastlaneMock
    File fastlaneMockPath

    def setupFastlaneMock() {
        fastlaneMock = new BatchmodeWrapper("fastlane").withEnvironment(true).toTempFile()
        fastlaneMockPath = fastlaneMock.parentFile

    }

    def setup() {
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            this.gradleVersion = gradleVersion
            fork = true
        }
        environmentVariables.clear("FASTLANE_USERNAME", "FASTLANE_PASSWORD", "FASTLANE_API_KEY_PATH", "SPACESHIP_SKIP_2FA_UPGRADE", "FASTLANE_SKIP_2FA_UPGRADE")

        setupFastlaneMock()
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        return rawValue.toString()
    }

    // TODO: Replace with newer test API. subStr is an object since we invoke this for any types then discard
    Object substitutePath(Object expectedValue, Object value, String typeName) {

        if (typeName != "File" && typeName != "Provider<RegularFile>") {
            return expectedValue
        }

        def path = (String) value
        if (path == null) {
            return expectedValue
        }

        // If it's an absolute path starting from the current volume
        if (Paths.get(path).isAbsolute()) {
            return expectedValue
        }

        def modifiedPath = typeName == "Provider<RegularFile>"
                ? "/build/${path}"
                : path

        expectedValue.replace(path, new File(projectDir, modifiedPath).path)
    }
}
