package org.jfrog.artifactory.client;

import junit.framework.Assert;
import org.jfrog.artifactory.client.model.SystemInfo;
import org.jfrog.artifactory.client.model.Version;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author jryan
 * @since 7/9/13
 */
public class SystemTests extends ArtifactoryTestsBase {
    @Test
    public void testPingOnLiveServer() {
        assertTrue(artifactory.system().ping());
    }

    @Test
    public void testLoadVersion() {
        Version version = artifactory.system().version();
        assertNotNull(version.getVersion());
        assertTrue(version.getVersion().contains("."));
        String revision = version.getRevision();
        // From version 5.5.0, running a snapshot produced also dev revision.
        if (revision.equals("${buildNumber.prop}") || revision.equals("dev")) {
            assertTrue(version.getVersion().contains("-SNAPSHOT"));
        } else {
            int rev = Integer.parseInt(revision);
            assertTrue(rev > 0);
        }
        assertTrue(version.getAddons().size() > 5);
        assertNotNull(version.getLicense()); // Since to even perform REST API calls, we need a license
    }

    @Test
    public void testDownloadOfSystemConfiguration() {
        String xml = artifactory.system().configuration();
        assertTrue(xml.contains("backups"));
        assertTrue(xml.contains("localRepositories"));
        assertTrue(xml.contains("repoLayouts"));
    }

    @Test
    public void testPatchOfProxy() {
        final String proxyName = "proxy1";
        String yaml = "proxies:\n"
                + "  " + proxyName + ":\n"
                + "    host: hostproxy1\n"
                + "    port: 0 \n";
        String artifactory7Yaml = yaml + "    platformDefault: false\n";
        artifactory.system().yamlConfiguration(artifactory7Yaml); // First, try Artifactory 7 style yaml

        String updatedXml = artifactory.system().configuration();
        if (!updatedXml.contains(proxyName)) { // If request failed, try Artifactory 6 style yaml
            String artifactory6Yaml = yaml + "    defaultProxy: false\n";
            artifactory.system().yamlConfiguration(artifactory6Yaml);
            updatedXml = artifactory.system().configuration();
            assertTrue(updatedXml.contains(proxyName));
        }

        // Restore
        String deleteProxy = "proxies:\n"
                + "  " + proxyName + ": null\n";
        artifactory.system().yamlConfiguration(deleteProxy);
    }

    @Test
    public void testSystemInfo() {
        SystemInfo info = artifactory.system().info();
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getCommittedVirtualMemorySize() >= 0L);
        Assert.assertTrue(info.getTotalSwapSpaceSize() >= 0L);
        Assert.assertTrue(info.getFreeSwapSpaceSize() >= 0L);
        Assert.assertTrue(info.getProcessCpuTime() >= 0L);
        Assert.assertTrue(info.getTotalPhysicalMemorySize() >= 0L);
        Assert.assertTrue(info.getOpenFileDescriptorCount() >= 0L);
        Assert.assertTrue(info.getMaxFileDescriptorCount() >= 0L);
        Assert.assertTrue(info.getProcessCpuLoad() >= 0.0D);
        Assert.assertTrue(info.getSystemCpuLoad() >= 0.0D);
        Assert.assertTrue(info.getFreePhysicalMemorySize() >= 0L);
        Assert.assertTrue(info.getNumberOfCores() >= 0L);
        Assert.assertTrue(info.getHeapMemoryUsage() >= 0L);
        Assert.assertTrue(info.getNoneHeapMemoryUsage() >= 0L);
        Assert.assertTrue(info.getThreadCount() >= 0);
        Assert.assertTrue(info.getNoneHeapMemoryMax() >= 0L);
        Assert.assertTrue(info.getHeapMemoryMax() >= 0L);
        Assert.assertTrue(info.getJvmUpTime() >= 0L);
    }
}
