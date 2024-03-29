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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import wooga.gradle.fastlane.internal.DefaultFastlanePluginExtension
import wooga.gradle.fastlane.tasks.AbstractFastlaneTask
import wooga.gradle.fastlane.tasks.PilotUpload
import wooga.gradle.fastlane.tasks.SighRenew

import static wooga.gradle.fastlane.FastlanePluginConventions.*

class FastlanePlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = "fastlane"
    static final String FASTLANE_GROUP = "fastlane"
    static final String UPLOAD_GROUP = "upload" //BasePlugin.UPLOAD_GROUP not present anymore on gradle >= 7,

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        def extension = project.extensions.create(FastlanePluginExtension, EXTENSION_NAME, DefaultFastlanePluginExtension, project)

        configureExtension(extension, project)
        configureTasks(project, extension)
    }

    private static void configureExtension(FastlanePluginExtension extension, Project project) {
        extension.username.convention(USERNAME_LOOKUP.getStringValueProvider(project))
        extension.password.convention(PASSWORD_LOOKUP.getStringValueProvider(project))
        extension.apiKeyPath.convention(API_KEY_PATH_LOOKUP.getFileValueProvider(project))
        extension.apiKey.convention(API_KEY_LOOKUP.getStringValueProvider(project))
        extension.skip2faUpgrade.convention(SKIP_2FA_UPGRADE.getBooleanValueProvider(project))
        extension.executableName.convention(EXECUTABLE_NAME.getStringValueProvider(project))
        extension.executableDirectory.convention(EXECUTABLE_DIRECTORY.getDirectoryValueProvider(project))
    }

    private static void configureTasks(Project project, extension) {

        project.tasks.withType(AbstractFastlaneTask, new Action<AbstractFastlaneTask>() {
            @Override
            void execute(AbstractFastlaneTask task) {
                task.executableName.convention(extension.executableName)
                task.executableDirectory.convention(extension.executableDirectory)
                task.apiKeyPath.convention(extension.apiKeyPath)
                task.apiKey.convention(extension.apiKey)
                task.logToStdout.convention(true)
                task.skip2faUpgrade.convention(extension.skip2faUpgrade)
                task.username.convention(extension.username)
                task.password.convention(extension.password)
            }
        })

        project.tasks.withType(SighRenew, new Action<SighRenew>() {
            @Override
            void execute(SighRenew task) {
                task.group = FASTLANE_GROUP
                task.description = "runs fastlane sigh renew"
            }
        })

        project.tasks.withType(PilotUpload, new Action<PilotUpload>() {
            @Override
            void execute(PilotUpload task) {
                task.group = UPLOAD_GROUP
                task.description = "runs fastlane pilot upload"

                task.username.convention(extension.username)
                task.password.convention(extension.password)
            }
        })
    }
}
