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


import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import wooga.gradle.fastlane.models.PilotUploadSpec

class PilotUpload extends AbstractFastlaneTask implements PilotUploadSpec {

    @SkipWhenEmpty
    @InputFiles
    FileCollection getInputFiles() {
        if (ipa.present) {
            return project.files(ipa)
        }
        project.files()
    }

    PilotUpload() {
        super()

        internalArguments = project.provider({

            List<String> arguments = new ArrayList<String>()

            arguments << "pilot" << "upload"

            addDefaultArguments(arguments)

            addOptionalArgument(arguments, "--dev_portal_team_id", devPortalTeamId)
            addOptionalArgument(arguments, "--itc_provider", itcProvider)
            addFlag(arguments, "--skip_submission", skipSubmission)
            addFlag(arguments, "--skip_waiting_for_build_processing", skipWaitingForBuildProcessing)

            arguments << "--ipa" << ipa.get().asFile.path

            arguments
        })
    }


}
