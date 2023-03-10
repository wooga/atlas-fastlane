package wooga.gradle.fastlane.models

import com.sun.org.apache.xpath.internal.operations.Bool
import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal

trait SighRenewSpec extends BaseSpec {

    private final Property<String> fileName = objects.property(String)

    @Internal
    Property<String> getFileName() {
        fileName
    }

    void setFileName(String value) {
        fileName.set(value)
    }

    void setFileName(Provider<String> value) {
        fileName.set(value)
    }

    private final Property<String> provisioningName = objects.property(String)

    @Internal
    Property<String> getProvisioningName() {
        provisioningName
    }

    void setProvisioningName(String value) {
        provisioningName.set(value)
    }

    void setProvisioningName(Provider<String> value) {
        provisioningName.set(value)
    }

    private final Property<Boolean> adhoc = objects.property(Boolean)

    @Internal
    Property<Boolean> getAdhoc() {
        adhoc
    }

    void setAdhoc(Boolean value) {
        adhoc.set(value)
    }

    void setAdhoc(Provider<Boolean> value) {
        adhoc.set(value)
    }

    private final DirectoryProperty destinationDir = objects.directoryProperty()

    @Internal
    DirectoryProperty getDestinationDir() {
        destinationDir
    }

    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }

    private final Property<Boolean> readOnly = objects.property(Boolean)

    @Internal
    Property<Boolean> getReadOnly() {
        readOnly
    }

    void setReadOnly(Boolean value) {
        readOnly.set(value)
    }

    void setReadOnly(Provider<Boolean> value) {
        readOnly.set(value)
    }

    private final Property<Boolean> ignoreProfilesWithDifferentName = objects.property(Boolean)

    @Internal
    Property<Boolean> getIgnoreProfilesWithDifferentName() {
        ignoreProfilesWithDifferentName
    }

    void setIgnoreProfilesWithDifferentName(Boolean value) {
        ignoreProfilesWithDifferentName.set(value)
    }

    void setIgnoreProfilesWithDifferentName(Provider<Boolean> value) {
        ignoreProfilesWithDifferentName.set(value)
    }

    private final Property<Boolean> skipInstall = objects.property(Boolean)

    @Internal
    Property<Boolean> getSkipInstall() {
        skipInstall
    }

    void setSkipInstall(Provider<Boolean> value) {
        skipInstall.set(value)
    }

    void setSkipInstall(Boolean value) {
        skipInstall.set(value)
    }

    private final Property<Boolean> skipCertificateVerification = objects.property(Boolean)

    @Internal
    Property<Boolean> getSkipCertificateVerification() {
        skipCertificateVerification
    }

    void setSkipCertificateVerification(Provider<Boolean> value) {
        skipCertificateVerification.set(value)
    }

    void setSkipCertificateVerification(Boolean value) {
        skipCertificateVerification.set(value)
    }

    private final Property<Boolean> failOnNameTaken = objects.property(Boolean)

    @Internal
    Property<Boolean> getFailOnNameTaken() {
        failOnNameTaken
    }

    void setFailOnNameTaken(Provider<Boolean> value) {
        failOnNameTaken.set(value)
    }

    void setFailOnNameTaken(Boolean value) {
        failOnNameTaken.set(value)
    }



    private final Property<String> templateName = objects.property(String)

    @Internal
    Property<String> getTemplateName() {
        templateName
    }

    void setTemplateName(Provider<String> value) {
        templateName.set(value)
    }

    void setTemplateName(String value) {
        templateName.set(value)
    }

    private final Property<Boolean> includeAllCertificates = objects.property(Boolean)

    @Internal
    Property<Boolean> getIncludeAllCertificates() {
        includeAllCertificates
    }

    void setIncludeAllCertificates(Provider<Boolean> value) {
        includeAllCertificates.set(value)
    }

    void setIncludeAllCertificates(Boolean value) {
        includeAllCertificates.set(value)
    }

    private final Property<Boolean> skipFetchProfiles = objects.property(Boolean)

    @Internal
    Property<Boolean> getSkipFetchProfiles() {
        skipFetchProfiles
    }

    void setSkipFetchProfiles(Provider<Boolean> value) {
        skipFetchProfiles.set(value)
    }

    void setSkipFetchProfiles(Boolean value) {
        skipFetchProfiles.set(value)
    }

    private final Property<String> certOwnerName = objects.property(String)

    @Internal
    Property<String> getCertOwnerName() {
        certOwnerName
    }

    void setCertOwnerName(Provider<String> value) {
        certOwnerName.set(value)
    }

    void setCertOwnerName(String value) {
        certOwnerName.set(value)
    }


    private final Property<String> certId = objects.property(String)

    @Internal
    Property<String> getCertId() {
        certId
    }

    void setCertId(Provider<String> value) {
        certId.set(value)
    }

    void setCertId(String value) {
        certId.set(value)
    }

    private final Property<Boolean> force = objects.property(Boolean)

    @Internal
    Property<Boolean> getForce() {
        force
    }

    void setForce(Provider<Boolean> value) {
        force.set(value)
    }

    void setForce(Boolean value) {
        force.set(value)
    }

    private final Property<Boolean> development = objects.property(Boolean)

    @Internal
    Property<Boolean> getDevelopment() {
        development
    }

    void setDevelopment(Provider<Boolean> value) {
        development.set(value)
    }

    void setDevelopment(Boolean value) {
        development.set(value)
    }

    private final Property<Boolean> includeMacInProfiles = objects.property(Boolean)

    @Internal
    Property<Boolean> getIncludeMacInProfiles() {
        includeMacInProfiles
    }

    void setIncludeMacInProfiles(Provider<Boolean> value) {
        includeMacInProfiles.set(value)
    }

    void setIncludeMacInProfiles(Boolean value) {
        includeMacInProfiles.set(value)
    }

    private final Property<String> platform = objects.property(String)

    @Internal
    Property<String> getPlatform() {
        platform
    }

    void setPlatform(Provider<String> value) {
        platform.set(value)
    }

    void setPlatform(String value) {
        platform.set(value)
    }


}
