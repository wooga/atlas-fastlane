/*
 * Copyright 2018-2022 Wooga GmbH
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
 *
 *
 *
 */

package wooga.gradle.fastlane

class IntegrationSpec extends com.wooga.gradle.test.IntegrationSpec {

    def setup() {
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            this.gradleVersion = gradleVersion
            fork = true
        }
        environmentVariables.clear("FASTLANE_USERNAME", "FASTLANE_PASSWORD", "FASTLANE_API_KEY_PATH", "SPACESHIP_SKIP_2FA_UPGRADE", "FASTLANE_SKIP_2FA_UPGRADE")
    }
}
