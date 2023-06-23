package wooga.gradle.fastlane.tasks

import spock.lang.Requires

@Requires({ os.macOs })
class SighRenewIntergrationSpec extends SighRenewBaseIntegrationSpec<SighRenew>{
}
